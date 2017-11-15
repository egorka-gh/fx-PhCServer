-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

-- new extra state flow

DELIMITER $$

DROP PROCEDURE IF EXISTS printStateStart$$
CREATE
PROCEDURE printStateStart(IN pPgroupId varchar(50), IN lab int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vIsReprint int;
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  -- update print_group in transaction (avoid state hang)
  START TRANSACTION;
    UPDATE print_group pg
      SET pg.state = 250,
          pg.state_date = NOW(),
          pg.destination = lab
      WHERE pg.id = pPgroupId
        AND pg.state < 250;
  COMMIT;

  -- forvard meter
  IF ROW_COUNT() > 0
  THEN
    CALL lab_meter_forward_lab(lab, 250, pPgroupId);
  END IF;

  -- set books state
  UPDATE order_books ob
    SET ob.state = 250,
        ob.state_date = NOW()
    WHERE ob.pg_id = pPgroupId AND ob.state < 250;

  SELECT pg.order_id, pg.sub_id, pg.is_reprint
  INTO vOrderId, vSubId, vIsReprint
    FROM print_group pg
    WHERE pg.id = pPgroupId;

  IF vOrderId IS NOT NULL
  THEN
    IF vIsReprint = 0
    THEN
      -- rise order/suborder state
      IF vSubId != ''
      THEN
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
      SELECT IFNULL(MIN(pg.state), 0)
      INTO vMinState
        FROM print_group pg
        WHERE pg.order_id = vOrderId
          AND pg.sub_id = vSubId AND pg.is_reprint=0;
      IF vMinState >= 250
      THEN
        -- all pg started end order/suborder
        INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
          VALUES (vOrderId, vSubId, 210, NOW(), NOW())
        ON DUPLICATE KEY UPDATE state_date = NOW();
        IF vSubId != ''
        THEN
          INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
            VALUES (vOrderId, '', 210, NOW(), NOW())
          ON DUPLICATE KEY UPDATE state_date = NOW();
        END IF;
      ELSE
        -- start order/suborder
        CALL extraStateStart(vOrderId, vSubId, 210, NOW());
      END IF;
    ELSE
      -- stop reprint extrastate
      INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date, is_reject)
        VALUES (vOrderId, pPgroupId, 210, NOW(), NOW(), 1)
      ON DUPLICATE KEY UPDATE state_date = NOW();
      -- close prev transit_date?
    END IF;
  END IF;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS printStateCancel$$
CREATE
PROCEDURE printStateCancel(IN pPgroupId varchar(50))
  MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vIsReprint int;
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  UPDATE print_group pg
    SET pg.state = 200,
        pg.state_date = NOW()
    WHERE pg.id = pPgroupId;

  -- set books state
  UPDATE order_books ob
    SET ob.state = 200,
        ob.state_date = NOW()
    WHERE ob.pg_id = pPgroupId;

  SELECT pg.order_id, pg.sub_id, pg.is_reprint
  INTO vOrderId, vSubId, vIsReprint
    FROM print_group pg
    WHERE pg.id = pPgroupId;

  IF vOrderId IS NOT NULL
  THEN
    IF vIsReprint = 0
    THEN
      -- reset order/suborder state & extra
      UPDATE orders o
        SET o.state = 250,
            o.state_date = NOW()
        WHERE o.id = vOrderId
          AND o.state != 250;
      UPDATE order_extra_state
        SET state_date = NULL
        WHERE id = vOrderId
          AND sub_id = ''
          AND state IN (210, 250);
      IF vSubId != ''
      THEN
        UPDATE suborders o
          SET o.state = 250,
              o.state_date = NOW()
          WHERE o.order_id = vOrderId
            AND o.sub_id = vSubId
            AND o.state != 250;
        UPDATE order_extra_state
          SET state_date = NULL
          WHERE id = vOrderId
            AND sub_id = vSubId
            AND state IN (210, 250);
      END IF;
    ELSE
      -- reset reprint extrastate
      UPDATE order_extra_state
        SET state_date = NULL, transit_date=NULL
        WHERE id = vOrderId
          AND sub_id = pPgroupId
          AND state = 210;
    -- reset transit?
    END IF;

  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS extraStateSetOTK$$
CREATE 
PROCEDURE extraStateSetOTK(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pDate datetime)
  MODIFIES SQL DATA
BEGIN
  DECLARE vMinSubState int;
  DECLARE vMinPhotoState int;

  IF pDate IS NULL
  THEN
    SET pDate = NOW();
  END IF;

  IF pSubOrder != ''
  THEN
    -- fix suborder
    -- forvard suborder pgs state
    UPDATE print_group pg
      SET pg.state = 450,
          pg.state_date = pDate
      WHERE pg.order_id = pOrder
        AND pg.sub_id = pSubOrder
        AND pg.state < 450
    ORDER BY pg.id;

    -- set suborder state
    UPDATE suborders s
      SET s.state = 450,
          s.state_date = pDate
      WHERE s.order_id = pOrder
        AND s.sub_id = pSubOrder
        AND s.state < 450;

    -- fix suborder extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      VALUES (pOrder, pSubOrder, 450, pDate, pDate)
    ON DUPLICATE KEY UPDATE state_date = pDate;

  END IF;

  -- calc min state by photo pgs 
  SELECT MIN(pg.state)
  INTO vMinPhotoState
    FROM print_group pg
    WHERE pg.order_id = pOrder
      AND pg.is_reprint = 0
      AND pg.book_type = 0;

  -- calc min state by suborders
  SELECT MIN(so.state)
  INTO vMinSubState
    FROM suborders so
    WHERE so.order_id = pOrder;

  -- attempt to forvard order state
  IF (vMinPhotoState IS NULL
    OR vMinPhotoState >= 450)
    AND (vMinSubState IS NULL
    OR vMinSubState >= 450)
  THEN
    -- no photo or photo pgs pass OTK and no suborders or all suborders pass otk

    -- forvard pgs state
    UPDATE print_group pg
      SET pg.state = 450,
          pg.state_date = pDate
      WHERE pg.order_id = pOrder
        AND pg.sub_id = ''
        AND pg.state < 450
    ORDER BY pg.id;

    -- set order state
    UPDATE orders o
      SET o.state = 450,
          o.state_date = pDate
      WHERE o.id = pOrder
        AND o.state < 450;

    -- fix order extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      VALUES (pOrder, '', 450, pDate, pDate)
    ON DUPLICATE KEY UPDATE state_date = pDate;

    -- stop all started order extra states
    UPDATE order_extra_state es
      SET es.state_date = pDate
      WHERE es.id = pOrder
        -- AND es.sub_id = ''
        AND es.state < 450
        AND es.state_date IS NULL
    ORDER BY es.sub_id, es.state;

  END IF;

  CALL setEntireBookState(pOrder, pSubOrder, -1, 450);
  -- fix reprints extrastates
  UPDATE order_extra_state oes
  INNER JOIN print_group pg
    ON oes.id = pg.order_id
    AND oes.sub_id = pg.id
    AND oes.is_reject != 0
    SET oes.state_date = IFNULL(oes.state_date, pDate),
        oes.transit_date = IFNULL(oes.transit_date, pDate)
    WHERE pg.order_id = pOrder
      AND pg.sub_id = pSubOrder
      AND pg.is_reprint = 1;
/*
  INSERT IGNORE INTO order_extra_state (id, sub_id, state, start_date, state_date, transit_date, is_reject)
    SELECT pg.order_id, pg.id, 450, pDate, pDate, pDate, 1
      FROM print_group pg
      WHERE pg.order_id = pOrder
        AND pg.sub_id = pSubOrder
        AND pg.is_reprint = 1;
        */
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techLogPg$$
CREATE
PROCEDURE techLogPg(IN pPgroup varchar(50), IN pSheet int, IN pTechPoint int, IN pDate datetime, IN pCalc int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vDataValid int;
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vState int;
  DECLARE vBookPart int;
  DECLARE vBooks int;
  DECLARE vPrints int;
  DECLARE vIsReprint int;

  DECLARE vMinBookState int;
  DECLARE vMinBookTime datetime;
  DECLARE vMaxBookTime datetime;

  BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET vDataValid = 0;

    IF pDate IS NULL
    THEN
      SET pDate = NOW();
    END IF;


    SELECT pg.order_id, pg.sub_id, pg.book_num, pg.prints, pg.is_reprint, 1
    INTO vOrderId, vSubId, vBooks, vPrints, vIsReprint, vDataValid
      FROM print_group pg
      WHERE pg.id = pPgroup;

    SELECT tp.tech_type, os.book_part, 1
    INTO vState, vBookPart, vDataValid
      FROM tech_point tp
        INNER JOIN order_state os ON os.id = tp.tech_type
      WHERE tp.id = pTechPoint;
  END;

  IF vDataValid=1
  THEN
    -- log
    INSERT INTO tech_log (order_id, sub_id, print_group, sheet, src_id, log_date)
      VALUES (vOrderId, vSubId, pPgroup, pSheet, pTechPoint, pDate);

    -- recalc
    IF pCalc > 0
    THEN
      IF vIsReprint = 0
      THEN
        -- check TODO incorparate into code (do not need sub call)
        CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vPrints);
      END IF;
      -- recalc book
      IF NOT (vState > 300 AND vBookPart = 0)
      THEN
        /*
        -- book join or OTK
        CALL setEntireBookState(vOrderId, vSubId, (pSheet DIV 100), vState);
      ELSE
        */
        CALL tech_calc_books(pPgroup);
      END IF;

      IF vIsReprint != 0
      THEN
        -- recalc rejects extra
        -- get books state
        SELECT IFNULL(MIN(ob.state), 0),
          IFNULL(MIN(IF(ob.state = vState, ob.state_date, NULL)), pDate),
          IFNULL(MAX(IF(ob.state = vState, ob.state_date, NULL)), pDate)
        INTO vMinBookState, vMinBookTime, vMaxBookTime
          FROM order_books ob
          WHERE ob.pg_id = pPgroup;

        -- forward pg state
        UPDATE print_group pg
          SET pg.state = vMinBookState,
              pg.state_date = vMaxBookTime
          WHERE pg.id = pPgroup AND pg.state < vMinBookState;

        -- forward extra state
        IF vMinBookState >= vState
        THEN
          -- TODO what if common state (vBookPart=0), what about parent pg extra?
          -- stop extra
          INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
            VALUES (vOrderId, pPgroup, vState, vMinBookTime, vMaxBookTime)
          ON DUPLICATE KEY UPDATE state_date = vMaxBookTime;
        ELSE
          -- start extrastate
          INSERT IGNORE INTO order_extra_state (id, sub_id, state, start_date, is_reject)
            VALUES (vOrderId, pPgroup, vState, vMinBookTime, 1);
        END IF;
        -- close prev state_date & transit_date
        UPDATE order_extra_state es
          SET es.state_date = IFNULL(es.state_date, vMinBookTime),
              es.transit_date = IFNULL(es.transit_date, vMinBookTime)
          WHERE es.id = vOrderId AND es.sub_id = pPgroup AND es.state < vState AND (es.state_date IS NULL OR es.transit_date IS NULL);
      END IF;

    END IF;
  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techUnitCalc$$
CREATE
PROCEDURE techUnitCalc(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pPgroup varchar(50), IN pState int, IN pBooks int, IN pPrints int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vDone int;
  DECLARE vStart datetime;
  DECLARE vEnd datetime;
  DECLARE vCount int;
  DECLARE vMinState int;
  DECLARE vParentPg varchar(50);

  IF pPgroup IS NULL
  THEN
    SET pPgroup = '';
  END IF;
  IF pSubOrder IS NULL
  THEN
    SET pSubOrder = '';
  END IF;
  -- calc prints
  SELECT IFNULL(COUNT(DISTINCT tl.sheet), 0), IFNULL(MIN(tl.log_date), NOW()), IFNULL(MAX(tl.log_date), NOW())
  INTO vDone, vStart, vEnd
    FROM tech_log tl
      INNER JOIN tech_point tp ON tl.src_id = tp.id
    WHERE tl.order_id = pOrder
      AND tl.sub_id = pSubOrder
      AND tl.print_group = pPgroup
      AND tp.tech_type = pState;

  -- check complited
  IF vDone = pPrints
    OR (pState = 320 AND vDone = pBooks * 2) -- 320 - TECH_FOLDING (log first & end sheet per book)
    OR (pState = 360 AND vDone > 0) -- 360 - TECH_CUTTING (log first scan)
    OR (pState = 380 AND vDone = pBooks) -- 380 - TECH_JOIN   (log books)
  THEN
    -- complited
    IF pPgroup != ''
    THEN
      IF pState != 300
      THEN
        -- set pg state 
        UPDATE print_group pg
        SET pg.state = pState,
            pg.state_date = vEnd
        WHERE pg.id = pPgroup;
      ELSE
        -- print
        /*
        -- get parent printgroup
        SELECT IFNULL(pg.reprint_id, pg.id)
        INTO vParentPg
          FROM print_group pg
          WHERE pg.id = pPgroup;
        -- set pg state, if reprint set parent state and it's all open reprints
        UPDATE print_group pg
        SET pg.state = pState,
            pg.state_date = vEnd,
            pg.prints_done = LEAST(pg.prints, vDone)
        WHERE pg.order_id = pOrder
        AND pg.state >= 250
        AND pg.state < 300
        AND vParentPg IN (pg.id, pg.reprint_id);
        */
        UPDATE print_group pg
        SET pg.state = pState,
            pg.state_date = vEnd,
            pg.prints_done = LEAST(pg.prints, vDone)
        WHERE pg.id = pPgroup
        AND pg.state >= 250
        AND pg.state < 300;
      END IF;
    END IF;
    IF pState = 300
    THEN
      -- print state, check pringroups
      SELECT IFNULL(MIN(pg.state), 0)
      INTO vMinState
        FROM print_group pg
        WHERE pg.order_id = pOrder
          AND pg.sub_id = pSubOrder AND pg.is_reprint=0;
      IF vMinState >= 300
      THEN
        -- all pg printed end order/suborder
        CALL extraStateSet(pOrder, pSubOrder, pState, vEnd);
      ELSE
        -- start
        CALL extraStateStart(pOrder, pSubOrder, pState, vStart);
      END IF;
    ELSE
      -- end order/suborder
      CALL extraStateSet(pOrder, pSubOrder, pState, vEnd);
    END IF;
  ELSE
    -- incomplited
    -- update prints
    IF pState = 300 AND pPgroup != ''
    THEN
      UPDATE print_group pg
      SET pg.prints_done = LEAST(pg.prints, vDone)
      WHERE pg.id = pPgroup;
    END IF;
    -- start extraState
    CALL extraStateStart(pOrder, pSubOrder, pState, vStart);
  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techCalcPg$$
CREATE
PROCEDURE techCalcPg(IN pPgroup varchar(50), IN pTechPoint int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vDataValid int;
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vrepgroup varchar(50);
  DECLARE vState int;
  DECLARE vBooks int;
  DECLARE vPrints int;
  DECLARE vIsReprint int;
  DECLARE vDate datetime;
  DECLARE vBookPart int;

  DECLARE vMinBookState int;
  DECLARE vMinBookTime datetime;
  DECLARE vMaxBookTime datetime;

  BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET vDataValid = 0;

    SET vDate = NOW();

    SELECT pg.order_id, pg.sub_id, pg.book_num, pg.prints, pg.is_reprint, 1
    INTO vOrderId, vSubId, vBooks, vPrints, vIsReprint, vDataValid
      FROM print_group pg
      WHERE pg.id = pPgroup;

    SELECT tp.tech_type, os.book_part, 1
    INTO vState, vBookPart, vDataValid
      FROM tech_point tp
        INNER JOIN order_state os ON os.id = tp.tech_type
      WHERE tp.id = pTechPoint;
  END;

  IF vDataValid = 1
  THEN
    IF vIsReprint = 0
    THEN
      -- check TODO incorparate into code (do not need sub call)
      CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vPrints);
    END IF;
    IF NOT (vState > 300
      AND vBookPart = 0)
    THEN
      CALL tech_calc_books(pPgroup);
    END IF;

    IF vIsReprint != 0
    THEN
      -- recalc rejects extra
      -- get books state
      SELECT IFNULL(MIN(ob.state), 0),
        IFNULL(MIN(IF(ob.state = vState, ob.state_date, NULL)), pDate),
        IFNULL(MAX(IF(ob.state = vState, ob.state_date, NULL)), pDate)
      INTO vMinBookState, vMinBookTime, vMaxBookTime
        FROM order_books ob
        WHERE ob.pg_id = pPgroup;

      -- forward pg state
      UPDATE print_group pg
        SET pg.state = vMinBookState,
            pg.state_date = vMaxBookTime
        WHERE pg.id = pPgroup AND pg.state < vMinBookState;

      -- forward extra state
      IF vMinBookState >= vState
      THEN
        -- TODO what if common state (vBookPart=0), what about parent pg extra?
        -- stop extra
        INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
          VALUES (vOrderId, pPgroup, vState, vMinBookTime, vMaxBookTime)
        ON DUPLICATE KEY UPDATE state_date = vMaxBookTime;
      ELSE
        -- start extrastate
        INSERT IGNORE INTO order_extra_state (id, sub_id, state, start_date, is_reject)
          VALUES (vOrderId, pPgroup, vState, vMinBookTime, 1);
      END IF;
      -- close prev state_date & transit_date
      UPDATE order_extra_state es
        SET es.state_date = IFNULL(es.state_date, vMinBookTime),
            es.transit_date = IFNULL(es.transit_date, vMinBookTime)
        WHERE es.id = vOrderId AND es.sub_id = pPgroup AND es.state < vState AND (es.state_date IS NULL OR es.transit_date IS NULL);
    END IF;

  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS setBookState$$
CREATE
PROCEDURE setBookState(IN pPgroup varchar(50), IN pBook int, IN pState int, IN pResetReject int)
BEGIN
  UPDATE order_books obb
  INNER JOIN print_group pg
    ON obb.target_pg = IFNULL(pg.reprint_id, pg.id)
    SET obb.state = pState,
        obb.state_date = NOW(),
        obb.is_rejected = obb.is_rejected * IF(pResetReject = 0, 0, 1)
    WHERE pg.id = pPgroup AND obb.book = pBook AND obb.state < pState;
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

  -- forvard pg state (if common, and state over print)
  IF vBookPart = 0 AND pState>300 THEN
    UPDATE print_group pg
    SET pg.state = pState,
        pg.state_date = pDate
    WHERE pg.order_id = pOrder
    AND pg.sub_id = pSubOrder
    AND pg.is_reprint=0 -- don't now what to do vs reprints
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

DROP VIEW suborderotkv CASCADE;
CREATE
VIEW suborderotkv
AS
SELECT `es`.`id` AS `order_id`, `es`.`sub_id` AS `sub_id`, `es`.`state` AS `state`, `es`.`start_date` AS `state_date`, COUNT(DISTINCT `tl`.`sheet`) AS `books_done`, IFNULL(`s`.`prt_qty`, IFNULL((SELECT MAX(`pg`.`book_num`)
      FROM `print_group` `pg`
      WHERE ((`pg`.`order_id` = `es`.`id`) AND (`pg`.`sub_id` = `es`.`sub_id`))), 0)) AS `prt_qty`, (SELECT IFNULL(MAX(`pg`.`book_type`), 0)
      FROM `print_group` `pg`
      WHERE ((`pg`.`order_id` = `es`.`id`) AND (`pg`.`sub_id` = `es`.`sub_id`) AND (`pg`.`is_reprint` = 0) AND (`pg`.`state` < 450))) AS `proj_type`
  FROM ((((`order_extra_state` `es`
    LEFT JOIN `orders` `o` ON ((`o`.`id` = `es`.`id`)))
    LEFT JOIN `suborders` `s` ON (((`es`.`id` = `s`.`order_id`) AND (`es`.`sub_id` = `s`.`sub_id`))))
    LEFT JOIN `tech_point` `tp` ON ((`tp`.`tech_type` = `es`.`state`)))
    LEFT JOIN `tech_log` `tl` ON (((`tl`.`src_id` = `tp`.`id`) AND (`tl`.`order_id` = `es`.`id`) AND (`tl`.`sub_id` = `es`.`sub_id`) AND (`tl`
      .`sheet` <> 0))))
  WHERE ((`es`.`state` = 450) AND ISNULL(`es`.`state_date`) AND (`es`.`is_reject` = 0))
  GROUP BY `es`.`id`, `es`.`sub_id`
  ORDER BY `es`.`start_date`;
  
CREATE TABLE tech_timeline (
  tech_process int(5) NOT NULL DEFAULT 0,
  tech_type int(5) NOT NULL,
  oper_time int(11) DEFAULT NULL,
  pass_time int(11) DEFAULT NULL,
  PRIMARY KEY (tech_process, tech_type)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

DROP PROCEDURE IF EXISTS extraStateStop$$
CREATE
PROCEDURE extraStateStop(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pDate datetime)
BEGIN

  IF pDate IS NULL
  THEN
    SET pDate = NOW();
  END IF;
  UPDATE order_extra_state es
    SET es.state_date = IFNULL(es.state_date, pDate),
        es.transit_date = IFNULL(es.transit_date, pDate)
    WHERE es.id = pOrder AND es.sub_id = pSubOrder AND es.state <= pState AND (es.state_date IS NULL OR es.transit_date IS NULL);

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS loadSpyRejects$$
CREATE DEFINER = 'root'@'localhost'
PROCEDURE loadSpyRejects(IN pFromState int, IN pToState int)
BEGIN
SELECT t.id, t.sub_id, t.state, t.start_date, t.state_date, t.transit_date, t.is_reject,
  t.lastDate, TIMESTAMPDIFF(MINUTE, lastDate, NOW()) delay,
  pg.book_type, pg.alias, os.name op_name, pg.book_part, bp.name bp_name, bt.name bt_name
  FROM (SELECT id, sub_id, state, start_date, state_date, transit_date, is_reject, IFNULL(oes.transit_date, IFNULL(oes.state_date, oes.start_date)) lastDate
      FROM order_extra_state oes
      WHERE oes.state_date IS NULL
        AND oes.is_reject != 0
        AND oes.state BETWEEN pFromState AND pToState
    UNION ALL
    SELECT id, sub_id, state, start_date, state_date, transit_date, is_reject, IFNULL(oes.transit_date, IFNULL(oes.state_date, oes.start_date)) lastDate
      FROM order_extra_state oes
      WHERE oes.transit_date IS NULL
        AND oes.is_reject != 0
        AND oes.state BETWEEN pFromState AND pToState) t
    INNER JOIN order_state os ON t.state = os.id
    INNER JOIN print_group pg ON t.sub_id = pg.id AND pg.book_type > 0
    INNER JOIN book_part bp ON pg.book_part = bp.id
    INNER JOIN book_type bt ON bt.id = pg.book_type
    INNER JOIN tech_timeline tt ON tt.tech_process = 0 AND t.state = tt.tech_type
      AND (IFNULL(t.state_date, t.start_date) > DATE_ADD(NOW(), INTERVAL tt.oper_time MINUTE) OR
      lastDate > DATE_ADD(NOW(), INTERVAL tt.pass_time MINUTE));
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techCalcPg$$
CREATE
PROCEDURE techCalcPg(IN pPgroup varchar(50), IN pTechPoint int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vDataValid int;
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vrepgroup varchar(50);
  DECLARE vState int;
  DECLARE vBooks int;
  DECLARE vPrints int;
  DECLARE vIsReprint int;
  DECLARE vDate datetime;
  DECLARE vBookPart int;

  DECLARE vMinBookState int;
  DECLARE vMinBookTime datetime;
  DECLARE vMaxBookTime datetime;

  BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET vDataValid = 0;

    SET vDate = NOW();

    SELECT pg.order_id, pg.sub_id, pg.book_num, pg.prints, pg.is_reprint, 1
    INTO vOrderId, vSubId, vBooks, vPrints, vIsReprint, vDataValid
      FROM print_group pg
      WHERE pg.id = pPgroup;

    SELECT tp.tech_type, os.book_part, 1
    INTO vState, vBookPart, vDataValid
      FROM tech_point tp
        INNER JOIN order_state os ON os.id = tp.tech_type
      WHERE tp.id = pTechPoint;
  END;

  IF vDataValid = 1
  THEN
    IF vIsReprint = 0
    THEN
      -- check TODO incorparate into code (do not need sub call)
      CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vPrints);
    END IF;
    IF NOT (vState > 300
      AND vBookPart = 0)
    THEN
      CALL tech_calc_books(pPgroup);
    END IF;

    IF vIsReprint != 0
    THEN
      -- recalc rejects extra
      -- get books state
      SELECT IFNULL(MIN(ob.state), 0),
        IFNULL(MIN(IF(ob.state = vState, ob.state_date, NULL)), vDate),
        IFNULL(MAX(IF(ob.state = vState, ob.state_date, NULL)), vDate)
      INTO vMinBookState, vMinBookTime, vMaxBookTime
        FROM order_books ob
        WHERE ob.pg_id = pPgroup;

      -- forward pg state
      UPDATE print_group pg
        SET pg.state = vMinBookState,
            pg.state_date = vMaxBookTime
        WHERE pg.id = pPgroup AND pg.state < vMinBookState;

      -- forward extra state
      IF vMinBookState >= vState
      THEN
        -- TODO what if common state (vBookPart=0), what about parent pg extra?
        -- stop extra
        INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date, is_reject)
          VALUES (vOrderId, pPgroup, vState, vMinBookTime, vMaxBookTime, 1)
        ON DUPLICATE KEY UPDATE state_date = vMaxBookTime;
      ELSE
        -- start extrastate
        INSERT IGNORE INTO order_extra_state (id, sub_id, state, start_date, is_reject)
          VALUES (vOrderId, pPgroup, vState, vMinBookTime, 1);
      END IF;
      -- close prev state_date & transit_date
      UPDATE order_extra_state es
        SET es.state_date = IFNULL(es.state_date, vMinBookTime),
            es.transit_date = IFNULL(es.transit_date, vMinBookTime)
        WHERE es.id = vOrderId AND es.sub_id = pPgroup AND es.state < vState AND (es.state_date IS NULL OR es.transit_date IS NULL);
    END IF;

  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techLogPg$$
CREATE
PROCEDURE techLogPg(IN pPgroup varchar(50), IN pSheet int, IN pTechPoint int, IN pDate datetime, IN pCalc int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vDataValid int;
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vState int;
  DECLARE vBookPart int;
  DECLARE vBooks int;
  DECLARE vPrints int;
  DECLARE vIsReprint int;

  DECLARE vMinBookState int;
  DECLARE vMinBookTime datetime;
  DECLARE vMaxBookTime datetime;

  BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET vDataValid = 0;

    IF pDate IS NULL
    THEN
      SET pDate = NOW();
    END IF;


    SELECT pg.order_id, pg.sub_id, pg.book_num, pg.prints, pg.is_reprint, 1
    INTO vOrderId, vSubId, vBooks, vPrints, vIsReprint, vDataValid
      FROM print_group pg
      WHERE pg.id = pPgroup;

    SELECT tp.tech_type, os.book_part, 1
    INTO vState, vBookPart, vDataValid
      FROM tech_point tp
        INNER JOIN order_state os ON os.id = tp.tech_type
      WHERE tp.id = pTechPoint;
  END;

  IF vDataValid=1
  THEN
    -- log
    INSERT INTO tech_log (order_id, sub_id, print_group, sheet, src_id, log_date)
      VALUES (vOrderId, vSubId, pPgroup, pSheet, pTechPoint, pDate);

    -- recalc
    IF pCalc > 0
    THEN
      IF vIsReprint = 0
      THEN
        -- check TODO incorparate into code (do not need sub call)
        CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vPrints);
      END IF;
      -- recalc book
      IF NOT (vState > 300 AND vBookPart = 0)
      THEN
        /*
        -- book join or OTK
        CALL setEntireBookState(vOrderId, vSubId, (pSheet DIV 100), vState);
      ELSE
        */
        CALL tech_calc_books(pPgroup);
      END IF;

      IF vIsReprint != 0
      THEN
        -- recalc rejects extra
        -- get books state
        SELECT IFNULL(MIN(ob.state), 0),
          IFNULL(MIN(IF(ob.state = vState, ob.state_date, NULL)), pDate),
          IFNULL(MAX(IF(ob.state = vState, ob.state_date, NULL)), pDate)
        INTO vMinBookState, vMinBookTime, vMaxBookTime
          FROM order_books ob
          WHERE ob.pg_id = pPgroup;

        -- forward pg state
        UPDATE print_group pg
          SET pg.state = vMinBookState,
              pg.state_date = vMaxBookTime
          WHERE pg.id = pPgroup AND pg.state < vMinBookState;

        -- forward extra state
        IF vMinBookState >= vState
        THEN
          -- TODO what if common state (vBookPart=0), what about parent pg extra?
          -- stop extra
          INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date, is_reject)
            VALUES (vOrderId, pPgroup, vState, vMinBookTime, vMaxBookTime, 1)
          ON DUPLICATE KEY UPDATE state_date = vMaxBookTime;
        ELSE
          -- start extrastate
          INSERT IGNORE INTO order_extra_state (id, sub_id, state, start_date, is_reject)
            VALUES (vOrderId, pPgroup, vState, vMinBookTime, 1);
        END IF;
        -- close prev state_date & transit_date
        UPDATE order_extra_state es
          SET es.state_date = IFNULL(es.state_date, vMinBookTime),
              es.transit_date = IFNULL(es.transit_date, vMinBookTime)
          WHERE es.id = vOrderId AND es.sub_id = pPgroup AND es.state < vState AND (es.state_date IS NULL OR es.transit_date IS NULL);
      END IF;

    END IF;
  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techLogPg$$
CREATE
PROCEDURE techLogPg(IN pPgroup varchar(50), IN pSheet int, IN pTechPoint int, IN pDate datetime, IN pCalc int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vDataValid int;
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vState int;
  DECLARE vBookPart int;
  DECLARE vBooks int;
  DECLARE vPrints int;
  DECLARE vIsReprint int;

  DECLARE vMinBookState int;
  DECLARE vMinBookTime datetime;
  DECLARE vMaxBookTime datetime;

  BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET vDataValid = 0;

    IF pDate IS NULL
    THEN
      SET pDate = NOW();
    END IF;


    SELECT pg.order_id, pg.sub_id, pg.book_num, pg.prints, pg.is_reprint, 1
    INTO vOrderId, vSubId, vBooks, vPrints, vIsReprint, vDataValid
      FROM print_group pg
      WHERE pg.id = pPgroup;

    SELECT tp.tech_type, os.book_part, LEAST(vDataValid, 1)
    INTO vState, vBookPart, vDataValid
      FROM tech_point tp
        INNER JOIN order_state os ON os.id = tp.tech_type
      WHERE tp.id = pTechPoint;
  END;

  IF vDataValid=1
  THEN
    -- log
    INSERT INTO tech_log (order_id, sub_id, print_group, sheet, src_id, log_date)
      VALUES (vOrderId, vSubId, pPgroup, pSheet, pTechPoint, pDate);

    -- recalc
    IF pCalc > 0
    THEN
      IF vIsReprint = 0
      THEN
        -- check TODO incorparate into code (do not need sub call)
        CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vPrints);
      END IF;
      -- recalc book
      IF NOT (vState > 300 AND vBookPart = 0)
      THEN
        /*
        -- book join or OTK
        CALL setEntireBookState(vOrderId, vSubId, (pSheet DIV 100), vState);
      ELSE
        */
        CALL tech_calc_books(pPgroup);
      END IF;

      IF vIsReprint != 0
      THEN
        -- recalc rejects extra
        -- get books state
        SELECT IFNULL(MIN(ob.state), 0),
          IFNULL(MIN(IF(ob.state = vState, ob.state_date, NULL)), pDate),
          IFNULL(MAX(IF(ob.state = vState, ob.state_date, NULL)), pDate)
        INTO vMinBookState, vMinBookTime, vMaxBookTime
          FROM order_books ob
          WHERE ob.pg_id = pPgroup;

        -- forward pg state
        UPDATE print_group pg
          SET pg.state = vMinBookState,
              pg.state_date = vMaxBookTime
          WHERE pg.id = pPgroup AND pg.state < vMinBookState;

        -- forward extra state
        IF vMinBookState >= vState
        THEN
          -- TODO what if common state (vBookPart=0), what about parent pg extra?
          -- stop extra
          INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date, is_reject)
            VALUES (vOrderId, pPgroup, vState, vMinBookTime, vMaxBookTime, 1)
          ON DUPLICATE KEY UPDATE state_date = vMaxBookTime;
        ELSE
          -- start extrastate
          INSERT IGNORE INTO order_extra_state (id, sub_id, state, start_date, is_reject)
            VALUES (vOrderId, pPgroup, vState, vMinBookTime, 1);
        END IF;
        -- close prev state_date & transit_date
        UPDATE order_extra_state es
          SET es.state_date = IFNULL(es.state_date, vMinBookTime),
              es.transit_date = IFNULL(es.transit_date, vMinBookTime)
          WHERE es.id = vOrderId AND es.sub_id = pPgroup AND es.state < vState AND (es.state_date IS NULL OR es.transit_date IS NULL);
      END IF;

    END IF;
  END IF;

END
$$

DELIMITER ;
-- main 2017-10-26

DELIMITER $$

DROP PROCEDURE IF EXISTS loadSpyRejects$$
CREATE
PROCEDURE loadSpyRejects(IN pFromState int, IN pToState int)
BEGIN
  SELECT t.id, t.sub_id, t.state, t.start_date, t.state_date, t.transit_date, t.is_reject,
    t.lastDate, TIMESTAMPDIFF(MINUTE, lastDate, NOW()) delay,
    pg.book_type, pg.alias, os.name op_name, pg.book_part, bp.name bp_name, bt.name bt_name
    FROM (SELECT id, sub_id, state, start_date, state_date, transit_date, is_reject, IFNULL(oes.transit_date, IFNULL(oes.state_date, oes.start_date)) lastDate
        FROM order_extra_state oes
        WHERE oes.state_date IS NULL
          AND oes.is_reject != 0
          AND oes.state BETWEEN pFromState AND pToState
      UNION ALL
      SELECT id, sub_id, state, start_date, state_date, transit_date, is_reject, IFNULL(oes.transit_date, IFNULL(oes.state_date, oes.start_date)) lastDate
        FROM order_extra_state oes
        WHERE oes.transit_date IS NULL
          AND oes.is_reject != 0
          AND oes.state BETWEEN pFromState AND pToState) t
      INNER JOIN order_state os ON t.state = os.id
      INNER JOIN print_group pg ON t.sub_id = pg.id AND pg.book_type > 0
      INNER JOIN book_part bp ON pg.book_part = bp.id
      INNER JOIN book_type bt ON bt.id = pg.book_type
      INNER JOIN tech_timeline tt ON tt.tech_process = 0 AND t.state = tt.tech_type
        AND ((t.state_date IS NULL AND t.start_date < DATE_SUB(NOW(), INTERVAL tt.oper_time MINUTE)) OR
        (t.state_date IS NOT NULL AND t.state_date < DATE_SUB(NOW(), INTERVAL tt.pass_time MINUTE)))
    ORDER BY t.lastDate;
END
$$

DELIMITER ;

-- main 2017-11-15