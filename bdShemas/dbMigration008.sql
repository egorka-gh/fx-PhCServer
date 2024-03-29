-- main cycle 2015-02-19

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE tmpt_vch_ids ENGINE = INNODB;
ALTER TABLE tmpt_spy ENGINE = INNODB;


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
    UPDATE orders o
    SET o.state = 250,
        o.state_date = NOW()
    WHERE o.id = vOrderId
    AND o.state < 250;
    IF vSubId != '' THEN
      UPDATE suborders o
      SET o.state = 250,
          o.state_date = NOW()
      WHERE o.order_id = vOrderId
      AND o.sub_id = vSubId
      AND o.state < 250;
    END IF;

    -- chek if all pg started
    SELECT IFNULL(MIN(pg.state), 0) INTO vMinState
    FROM print_group pg
    WHERE pg.order_id = vOrderId
    AND pg.sub_id = vSubId;
    IF vMinState >= 250 THEN
      -- all pg started end order/suborder
      CALL extraStateSet(vOrderId, vSubId, 210, NOW());
    ELSE
      -- start order/suborder
      CALL extraStateStart(vOrderId, vSubId, 210, NOW());
    END IF;

  END IF;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS syncSource$$

CREATE
PROCEDURE syncSource(IN pSourceId int)
  MODIFIES SQL DATA
main:
BEGIN
  DECLARE vSync integer(11) DEFAULT (0);
  DECLARE vCnt integer(11) DEFAULT (0);

  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vSync = 0;

  IF NOT EXISTS
    (SELECT 1
      FROM tmp_orders t
      WHERE t.source = pSourceId) THEN
    LEAVE main;
  END IF;


  -- fix sync iteration in transaction
  START TRANSACTION;
    -- get next sync
    SELECT ss.sync INTO vSync
      FROM sources_sync ss
      WHERE ss.id = pSourceId;
    SET vSync = IFNULL(vSync, 0) + 1;

    -- save sync and reset sync state
    INSERT INTO sources_sync (id, sync, sync_date, sync_state)
                      VALUES (pSourceId, vSync, NOW(), 0)
    ON DUPLICATE KEY UPDATE sync = vSync, sync_date = NOW(), sync_state = 0;
    
    -- orders vs state >200 can be locked while sync complite
    -- any updates - print and so on - will be aborted
    -- to prevent locks update orders (release row lock after COMMIT?)
    -- update sync & group_id
    UPDATE orders o
      INNER JOIN tmp_orders t ON o.id=t.id
      SET o.sync = vSync, o.group_id=t.group_id, o.client_id=t.client_id
      WHERE t.source = pSourceId;

  COMMIT;

  -- keep in transaction
  START TRANSACTION;
    -- set sync
    UPDATE tmp_orders to1
      SET to1.sync = vSync
      WHERE to1.source = pSourceId;

    -- add new
    -- search new
    UPDATE tmp_orders to1
      SET to1.is_new = IFNULL((SELECT 0 FROM orders o WHERE o.id = to1.id), 1)
      WHERE to1.source = pSourceId;
    -- insert new
    INSERT INTO orders
      (id, source, src_id, src_date, state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts, group_id, client_id)
      SELECT id, source, src_id, IF(src_date='1970-01-01 03:00',NOW(),src_date), state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts, group_id, client_id
        FROM tmp_orders to1
        WHERE to1.source = pSourceId AND to1.is_new = 1;
    -- remove new
    DELETE FROM tmp_orders
      WHERE source = pSourceId AND is_new = 1;

    -- check/process preload
    -- update printgroup 
    UPDATE print_group
      SET state = 200, state_date = NOW()
      WHERE state = 199 AND order_id IN (SELECT t.id FROM tmp_orders t WHERE t.source = pSourceId AND t.is_preload = 0);
    -- suborders
    UPDATE suborders s
      SET s.state = 200, s.state_date = NOW()
      WHERE s.state = 199 AND s.order_id IN (SELECT t.id FROM tmp_orders t WHERE t.source = pSourceId AND t.is_preload = 0);

    -- set extra state
    INSERT INTO order_extra_state
      (id, sub_id, state, start_date, state_date)
      SELECT t.id, '', 200, NOW(), NOW()
        FROM tmp_orders t
          INNER JOIN orders o ON o.id = t.id AND o.state = 199
        WHERE t.source = pSourceId AND t.is_preload = 0
      ON DUPLICATE KEY UPDATE state_date= NOW();

    -- orders
    UPDATE orders o
      SET state = 200, state_date = NOW(), is_preload = 0
      WHERE o.state = 199 AND o.id IN (SELECT t.id FROM tmp_orders t WHERE t.source = pSourceId AND t.is_preload = 0);

    -- cancel not in sync
    -- cancel print groups
    UPDATE print_group
      SET state = 510, state_date = NOW()
      WHERE order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state BETWEEN 100 AND 200 AND o.sync != vSync);
    -- cancel suborders
    UPDATE suborders s
      SET s.state = 510, s.state_date = NOW()
      WHERE s.order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state BETWEEN 100 AND 200 AND o.sync != vSync);
    -- cancel orders
    UPDATE orders o
      SET state = 510, state_date = NOW()
      WHERE o.source = pSourceId AND o.state BETWEEN 100 AND 200 AND o.sync != vSync;

    -- finde reload candidate by project data time
    UPDATE tmp_orders t
      SET t.reload = 1
      WHERE t.source = pSourceId AND t.data_ts IS NOT NULL
        AND EXISTS (SELECT 1 FROM orders o WHERE o.id = t.id AND o.data_ts IS NOT NULL AND o.data_ts != o.data_ts AND o.state BETWEEN 199 AND 200);
    -- clean orders 4 reload
    -- clean print_group
    DELETE FROM print_group
      WHERE order_id IN
        (SELECT id FROM tmp_orders t WHERE t.source = pSourceId AND t.reload = 1);
    -- clean extra info
    DELETE FROM order_extra_info
      WHERE id IN
        (SELECT id FROM tmp_orders t WHERE t.source = pSourceId AND t.reload = 1);
    -- clean suborder
    DELETE FROM suborders
      WHERE order_id IN
        (SELECT id FROM tmp_orders t WHERE t.source = pSourceId AND t.reload = 1);
    -- reset order state
    UPDATE orders o
      SET o.state = 100, o.state_date = NOW()
      WHERE o.id IN
        (SELECT id FROM tmp_orders t WHERE t.source = pSourceId AND t.reload = 1);
    -- set project data time
    UPDATE orders o
      SET o.data_ts = (SELECT tt.data_ts FROM tmp_orders tt WHERE tt.id = o.id)
      WHERE o.source = pSourceId
        AND EXISTS
          (SELECT 1 FROM tmp_orders t WHERE t.id = o.id AND t.data_ts IS NOT NULL AND t.data_ts != IFNULL(o.data_ts, ''));

    -- forvard state to mail send (site) state (466)
    -- print groups
    UPDATE print_group
      SET state = 466, state_date = NOW()
      WHERE order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state IN (450,465) AND o.sync != vSync);
    -- suborders
    UPDATE suborders s
      SET s.state = 466, s.state_date = NOW()
      WHERE s.order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state IN (450,465) AND o.sync != vSync);
    -- orders
    UPDATE orders o
      SET state = 466, state_date = NOW()
      WHERE o.source = pSourceId AND o.state IN (450,465) AND o.sync != vSync;

    -- finalize
    DELETE FROM tmp_orders
      WHERE source = pSourceId;
    UPDATE sources_sync 
      SET sync = vSync, sync_date = NOW(), sync_state = 1
    WHERE id=pSourceId;
  COMMIT;
END
$$

DELIMITER ;