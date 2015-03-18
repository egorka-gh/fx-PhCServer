-- main  2015-03-12
-- moskva 2015-03-11
SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

DELIMITER $$

DROP PROCEDURE IF EXISTS printStateStart$$

CREATE 
PROCEDURE printStateStart(IN pPgroupId varchar(50), IN lab int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  -- update print_group in transaction (avoid state hang)
  START TRANSACTION;
  UPDATE print_group pg
  SET pg.state = 250,
      pg.state_date = NOW(),
      pg.destination = lab
  WHERE pg.id = pPgroupId
  AND pg.state != 250;
  COMMIT;

  SELECT pg.order_id, pg.sub_id INTO vOrderId, vSubId
  FROM print_group pg
  WHERE pg.id = pPgroupId;

  IF vOrderId IS NOT NULL THEN
    -- rise order/suborder state
    IF vSubId != '' THEN
      UPDATE suborders o
      SET o.state = 250,
          o.state_date = NOW()
      WHERE o.order_id = vOrderId
      AND o.sub_id = vSubId
      AND o.state < 250;
    END IF;
    UPDATE orders o
    SET o.state = 250,
        o.state_date = NOW()
    WHERE o.id = vOrderId
    AND o.state < 250;

    -- chek if all pg started
    SELECT IFNULL(MIN(pg.state), 0) INTO vMinState
      FROM print_group pg
      WHERE pg.order_id = vOrderId
        AND pg.sub_id = vSubId;
    IF vMinState >= 250 THEN
      -- all pg started end order/suborder
      INSERT INTO order_extra_state  (id, sub_id, state, start_date, state_date)
                              VALUES (vOrderId, vSubId, 210, NOW(), NOW())
        ON DUPLICATE KEY UPDATE state_date = NOW();
      IF vSubId != '' THEN
        INSERT INTO order_extra_state  (id, sub_id, state, start_date, state_date)
                                VALUES (vOrderId, '', 210, NOW(), NOW())
          ON DUPLICATE KEY UPDATE state_date = NOW();
      END IF;
    ELSE
      -- start order/suborder
      CALL extraStateStart(vOrderId, vSubId, 210, NOW());
    END IF;

  END IF;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS extraStateSet$$

CREATE 
PROCEDURE extraStateSet(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pDate datetime)
  MODIFIES SQL DATA
BEGIN
  DECLARE vMinExtraState int;
  DECLARE vBookPart int;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vBookPart = -1;

  IF pDate IS NULL THEN
    SET pDate = NOW();
  END IF;

  SELECT os.book_part INTO vBookPart
    FROM order_state os
    WHERE os.id = pState;

  -- forvard pg state (if common)
  IF vBookPart = 0 THEN
    UPDATE print_group pg
    SET pg.state = pState,
        pg.state_date = pDate
    WHERE pg.order_id = pOrder
    AND pg.sub_id = pSubOrder
    AND pg.state<pState
    ORDER BY pg.id;
  END IF;

  IF pSubOrder = '' THEN

    -- set order state
    UPDATE orders o
    SET o.state = pState,
        o.state_date = pDate
    WHERE o.id = pOrder AND o.state<pState;

    -- fix extra state
    INSERT INTO order_extra_state
    (id, sub_id, state, start_date, state_date)
      VALUES (pOrder, pSubOrder, pState, pDate, pDate)
    ON DUPLICATE KEY UPDATE state_date = pDate;
  ELSE
    -- set suborder state
    UPDATE suborders s
    SET s.state = pState,
        s.state_date = pDate
    WHERE s.order_id = pOrder AND s.sub_id = pSubOrder AND s.state<pState;
    
    -- calc min extra by suborders filter by book part
    SELECT IFNULL(MIN(t.state), 0) INTO vMinExtraState
      FROM
      (SELECT IFNULL(MAX(IF(so.sub_id=pSubOrder, GREATEST( IFNULL(os.id,0), pState), os.id)),0) state
        FROM suborders so
          LEFT OUTER JOIN order_extra_state oes ON oes.id = so.order_id AND so.sub_id = oes.sub_id AND oes.state_date IS NOT NULL
          LEFT OUTER JOIN order_state os ON oes.state=os.id AND os.book_part = vBookPart
        WHERE so.order_id = pOrder
        GROUP BY so.sub_id) t;

    IF vMinExtraState > 0 THEN
      -- forvard order state
      IF vBookPart = 0 THEN
        UPDATE orders o
        SET o.state = vMinExtraState,
            o.state_date = pDate
        WHERE o.id = pOrder
          AND o.state > 209 -- 210 Print post
          AND o.state < vMinExtraState;
      END IF;

      -- close order extra states
      -- stop started order extra states
      UPDATE order_extra_state es
        SET es.state_date = pDate
      WHERE es.id = pOrder
        AND es.sub_id = ''
        AND es.state <= vMinExtraState
        AND es.state_date IS NULL
        AND (vBookPart=0 OR EXISTS(SELECT 1 FROM order_state os WHERE os.id=es.state AND os.book_part = vBookPart))
      ORDER BY es.state;

    END IF;
    
    -- fix suborder extra state
    INSERT INTO order_extra_state
    (id, sub_id, state, start_date, state_date)
      VALUES (pOrder, pSubOrder, pState, pDate, pDate)
    ON DUPLICATE KEY UPDATE state_date = pDate;

  END IF;

END
$$

DELIMITER ;
