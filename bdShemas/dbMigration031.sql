-- main    
-- new main (virt)    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

-- качалка сихронизация актуальности
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(529, 'К удалению (качалка)', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(530, 'Удален (качалка)', 0, 0, 0, 0);

DELIMITER $$

DROP PROCEDURE IF EXISTS syncSource4load$$
CREATE
PROCEDURE syncSource4load(IN pSourceId int)
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

  IF NOT EXISTS (SELECT 1
        FROM tmp_orders t
        WHERE t.source = pSourceId)
  THEN
    LEAVE main;
  END IF;


-- fix sync iteration in transaction
START TRANSACTION;
  -- get next sync
  SELECT ss.sync
  INTO vSync
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
  -- update sync 
  UPDATE orders_load o
  INNER JOIN tmp_orders t
    ON o.id = t.id
    SET o.sync = vSync
    WHERE t.source = pSourceId;
/*
  -- remove canceled or complited, will be added as new  
  DELETE
    FROM orders_load
  WHERE source = pSourceId
    AND sync = vSync
    AND state > 465;
*/
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
    SET to1.is_new = IFNULL((SELECT 0
        FROM orders_load o
        WHERE o.id = to1.id), 1)
    WHERE to1.source = pSourceId;
  -- insert new
  INSERT INTO orders_load (id, source, src_id, state, state_date, sync)
    SELECT id, source, src_id, 100, NOW(), sync
      FROM tmp_orders to1
      WHERE to1.source = pSourceId
        AND to1.is_new = 1;
  -- remove new
  DELETE
    FROM tmp_orders
  WHERE source = pSourceId
    AND is_new = 1;

  -- reset if err state or reload
  UPDATE orders_load o
    SET state = 102,
        state_date = NOW()
    WHERE o.source = pSourceId
      AND o.state BETWEEN 105 AND 130
      AND o.sync = vSync;

  -- reset canceled or complited
  -- finde reload candidate 
  UPDATE tmp_orders t
    SET t.reload = 1
    WHERE t.source = pSourceId
      AND EXISTS (SELECT 1
          FROM orders_load o
          WHERE o.id = t.id
            AND o.state > 465);
  -- del files
  DELETE
    FROM order_files
  WHERE order_id IN (SELECT id
        FROM tmp_orders t
        WHERE t.source = pSourceId
          AND t.reload = 1);
  -- reset orders
  UPDATE orders_load o
    SET o.state = 100,
        o.state_date = NOW(),
        o.ftp_folder = NULL,
        o.fotos_num = 0
    WHERE o.id IN (SELECT id
          FROM tmp_orders t
          WHERE t.source = pSourceId
            AND t.reload = 1);

  -- cancel not in sync
  -- cancel orders
  UPDATE orders_load o
    SET state = 505,
        state_date = NOW()
    WHERE o.source = pSourceId
      AND o.state IN (100, 102)
      AND o.sync != vSync;

  -- finalize
  DELETE
    FROM tmp_orders
  WHERE source = pSourceId;

  UPDATE sources_sync
    SET sync = vSync,
        sync_date = NOW(),
        sync_state = 1
    WHERE id = pSourceId;
COMMIT;
END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE syncS4Lvalid(IN pSourceId int)
  MODIFIES SQL DATA
main:
BEGIN

  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;

  IF NOT EXISTS (SELECT 1
        FROM tmp_orders t
        WHERE t.source = pSourceId)
  THEN
    LEAVE main;
  END IF;

  -- keep in transaction
  START TRANSACTION;

    -- cancel not in sync
    UPDATE orders_load o
    SET state = 505,
        state_date = NOW()
    WHERE o.source = pSourceId
    AND o.state < 465
    AND NOT EXISTS(SELECT 1
        FROM tmp_orders t
        WHERE t.source = pSourceId AND t.id=o.id);

    -- finalize
    DELETE
      FROM tmp_orders
    WHERE source = pSourceId;

  COMMIT;
END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE sync_4Lvalid()
  MODIFIES SQL DATA
  COMMENT 'синхронизация актуальности заказов для качалки'
BEGIN
  DECLARE vId integer(7) DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vCur CURSOR FOR
  SELECT s.id
    FROM sources s
    WHERE s.online = 1 AND s.type=4;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vId;
    IF vIsEnd THEN
      LEAVE wet;
    END IF;
    CALL syncS4Lvalid(vId);
  END LOOP wet;
  CLOSE vCur;
  UPDATE orders_load ol SET ol.state = IF(ol.ftp_folder IS NULL, 530, 529), state_date = NOW() WHERE ol.state = 505;
END
$$

DELIMITER ;
