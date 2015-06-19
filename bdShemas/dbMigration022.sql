-- main 
-- moskva 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

UPDATE order_state
SET id = 150, runtime = 0
WHERE id = 114;

UPDATE order_state SET id = 180, runtime = 1 WHERE id = 140;

DELETE FROM order_state
WHERE id IN(124,125);

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES
(151, 'Подготовить в первую очередь', 1, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES
(155, 'Проверка web статуса', 1, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES
(156, 'Web ok', 1, 0, 0, 0);

UPDATE order_state
SET id = 160
WHERE id = 115;

UPDATE order_state
SET id = 165
WHERE id = 120;

UPDATE order_state SET id = 130 WHERE id = 113;

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(140, 'Цветокоррекция', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (120, 'Ошибка загрузки', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (170, 'Ошибка подготовки', 0, 0, 0, 0);

UPDATE order_state
SET id = 103
WHERE id = 105;

UPDATE order_state
SET id = 104
WHERE id = 106;

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (105, 'Заблокирован для загрузки', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (157, 'Заблокирован для подготовки', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(-321, 'Блокирован другим процессом', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (-322, 'Не верный статус', 1, 0, 0, 0);

ALTER TABLE orders
  ADD COLUMN resume_load TINYINT(1) DEFAULT 0 AFTER clean_fs;
  
  
DELIMITER $$

DROP PROCEDURE IF EXISTS syncSource$$

CREATE
PROCEDURE syncSource (IN pSourceId int)
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
          WHERE t.source = pSourceId) THEN
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
      -- update sync & group_id
      UPDATE orders o
      INNER JOIN tmp_orders t
        ON o.id = t.id
      SET o.sync = vSync,
          o.group_id = t.group_id,
          o.client_id = t.client_id
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
      SET to1.is_new = IFNULL((SELECT 0
          FROM orders o
          WHERE o.id = to1.id), 1)
      WHERE to1.source = pSourceId;
      -- insert new
      INSERT INTO orders (id, source, src_id, src_date, state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts, group_id, client_id)
        SELECT id, source, src_id, IF(src_date = '1970-01-01 03:00', NOW(), src_date), state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts, group_id, client_id
          FROM tmp_orders to1
          WHERE to1.source = pSourceId
            AND to1.is_new = 1;
      -- remove new
      DELETE
        FROM tmp_orders
      WHERE source = pSourceId
        AND is_new = 1;

      -- check/process preload
      -- update printgroup 
      UPDATE print_group
      SET state = 200,
          state_date = NOW()
      WHERE state = 199
      AND order_id IN (SELECT t.id
          FROM tmp_orders t
          WHERE t.source = pSourceId
            AND t.is_preload = 0);
      -- suborders
      UPDATE suborders s
      SET s.state = 200,
          s.state_date = NOW()
      WHERE s.state = 199
      AND s.order_id IN (SELECT t.id
          FROM tmp_orders t
          WHERE t.source = pSourceId
            AND t.is_preload = 0);

      -- set extra state
      INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
        SELECT t.id, '', 200, NOW(), NOW()
          FROM tmp_orders t
            INNER JOIN orders o ON o.id = t.id AND o.state = 199 AND o.forward_state = 0
          WHERE t.source = pSourceId
            AND t.is_preload = 0
      ON DUPLICATE KEY UPDATE state_date = NOW();

      -- orders
      UPDATE orders o
      SET state = IF(o.forward_state = 0, 200, o.forward_state),
          state_date = NOW(),
          is_preload = 0
      WHERE o.state = 199
      AND o.id IN (SELECT t.id
          FROM tmp_orders t
          WHERE t.source = pSourceId
            AND t.is_preload = 0);

      -- cancel not in sync
      -- cancel print groups
      UPDATE print_group
      SET state = 505,
          state_date = NOW()
      WHERE order_id IN (SELECT id
          FROM orders o
          WHERE o.source = pSourceId
            AND o.state BETWEEN 100 AND 200
            AND o.sync != vSync);
      -- cancel suborders
      UPDATE suborders s
      SET s.state = 505,
          s.state_date = NOW()
      WHERE s.order_id IN (SELECT id
          FROM orders o
          WHERE o.source = pSourceId
            AND o.state BETWEEN 100 AND 200
            AND o.sync != vSync);
      -- cancel orders
      UPDATE orders o
      SET state = 505,
          state_date = NOW()
      WHERE o.source = pSourceId
      AND o.state BETWEEN 100 AND 200
      AND o.sync != vSync;

      -- finde reload candidate by project data time
      UPDATE tmp_orders t
      SET t.reload = 1
      WHERE t.source = pSourceId
      AND t.data_ts IS NOT NULL
      AND EXISTS (SELECT 1
          FROM orders o
          WHERE o.id = t.id
            AND o.data_ts IS NOT NULL
            AND o.data_ts != o.data_ts
            AND o.state BETWEEN 120 AND 200);

      -- finde reload candidate vs sync cancel (state=505)
      UPDATE tmp_orders t
      SET t.reload = 1
      WHERE t.source = pSourceId
      AND EXISTS (SELECT 1
          FROM orders o
          WHERE o.id = t.id
            AND o.state = 505);

      -- clean orders 4 reload
      -- clean print_group
      DELETE
        FROM print_group
      WHERE order_id IN (SELECT id
            FROM tmp_orders t
            WHERE t.source = pSourceId
              AND t.reload = 1);
      -- clean extra info
      DELETE
        FROM order_extra_info
      WHERE id IN (SELECT id
            FROM tmp_orders t
            WHERE t.source = pSourceId
              AND t.reload = 1);
      -- clean suborder
      DELETE
        FROM suborders
      WHERE order_id IN (SELECT id
            FROM tmp_orders t
            WHERE t.source = pSourceId
              AND t.reload = 1);
      -- reset order state
      UPDATE orders o
      SET o.state = 100,
          o.state_date = NOW(),
          o.resume_load = 0,
          o.is_preload = IFNULL((SELECT tt.is_preload
              FROM tmp_orders tt
              WHERE tt.id = o.id), 0)
      WHERE o.id IN (SELECT id
          FROM tmp_orders t
          WHERE t.source = pSourceId
            AND t.reload = 1);
      -- set project data time
      UPDATE orders o
      SET o.data_ts = (SELECT tt.data_ts
          FROM tmp_orders tt
          WHERE tt.id = o.id)
      WHERE o.source = pSourceId
      AND EXISTS (SELECT 1
          FROM tmp_orders t
          WHERE t.id = o.id
            AND t.data_ts IS NOT NULL
            AND t.data_ts != IFNULL(o.data_ts, ''));

      /*
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
    */

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

DROP PROCEDURE IF EXISTS orderCleanUp$$

CREATE 
PROCEDURE orderCleanUp (IN pId varchar(50))
MODIFIES SQL DATA
BEGIN
  DECLARE vDate datetime DEFAULT NOW();

  DELETE
    FROM print_group
  WHERE order_id = pId;
  DELETE
    FROM order_extra_info
  WHERE id = pId;
  DELETE
    FROM suborders
  WHERE order_id = pId;
  UPDATE orders
  SET state = 100,
      state_date = vDate,
      resume_load = 0
  WHERE id = pId;
  INSERT INTO state_log (order_id, state, state_date, comment)
    VALUES (pId, 100, vDate, 'reset');
END
$$

DELIMITER ;

CREATE TABLE app_locks (
  lock_key varchar(100) NOT NULL,
  lock_time datetime NOT NULL,
  lock_owner varchar(50) DEFAULT NULL,
  lock_fixed tinyint(1) DEFAULT 0,
  PRIMARY KEY (lock_key)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

DROP PROCEDURE IF EXISTS lock_get$$

CREATE 
PROCEDURE lock_get (IN pkey varchar(100), IN powner varchar(50))
MODIFIES SQL DATA
BEGIN
  CALL lock_release(pkey, powner);
  INSERT INTO app_locks (lock_key, lock_time, lock_owner)
    VALUES (pkey, NOW(), powner);
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS lock_release$$

CREATE
PROCEDURE lock_release (IN pkey varchar(100), IN powner varchar(50))
MODIFIES SQL DATA
BEGIN
  -- lock stay alive 10 minutes if not fixed

  DELETE FROM app_locks
  WHERE lock_key = pkey 
    AND (lock_owner=powner 
          OR (lock_time < (NOW() - INTERVAL 10 MINUTE ) AND lock_fixed=0));

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS lock_clear$$

CREATE
PROCEDURE lock_clear ()
MODIFIES SQL DATA
BEGIN
  -- remove all overdue not fixed locks
  DELETE FROM app_locks
  WHERE lock_time < (NOW() - INTERVAL 10 MINUTE)
    AND lock_fixed = 0;
END
$$

DELIMITER ;