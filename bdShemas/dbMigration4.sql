-- main cycle 2014-02-13

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

-- mail tables and states

CREATE TABLE package (
  source int(7) NOT NULL,
  id int(11) NOT NULL,
  id_name varchar(50) DEFAULT NULL,
  client_id int(11) DEFAULT 0,
  state int(5) DEFAULT 0,
  state_date datetime DEFAULT NULL,
  execution_date datetime DEFAULT NULL,
  delivery_id int(5) DEFAULT NULL,
  delivery_name varchar(255) DEFAULT NULL,
  src_state int(5) DEFAULT 0,
  src_state_name varchar(100) DEFAULT NULL,
  mail_service int(5) DEFAULT 0,
  PRIMARY KEY (source, id)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE package_prop (
  source int(7) NOT NULL,
  id int(11) NOT NULL,
  property varchar(50) NOT NULL DEFAULT '',
  value varchar(255) DEFAULT NULL,
  PRIMARY KEY (source, id, property),
  CONSTRAINT FK_package_prop FOREIGN KEY (source, id)
  REFERENCES package (source, id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

-- mail states
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (455, 'Упаковка', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (460, 'Отправка', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (465, 'Отправлен', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (466, 'Отправлен (сайт)', 0, 0, 0, 0);

-- mail fields def
INSERT INTO attr_family(id, name) VALUES (5, 'Упаковка поля');
INSERT INTO attr_family(id, name) VALUES (6, 'Упаковка доп атрибуты');

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (46, 5, 'id упаковки', 'id', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (47, 5, 'id подпись', 'id_name', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (48, 5, 'id клиента', 'client_id', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (49, 5, 'дата исполнения', 'execution_date', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (50, 5, 'тип доставки', 'delivery_id', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (51, 5, 'тип доставки подпись', 'delivery_name', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (52, 5, 'статус источника', 'src_state', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (53, 5, 'статус источника подпись', 'src_state_name', 0, 1);

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (54, 6, 'количество', 'amount', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (55, 6, 'вес', 'weight', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (56, 6, 'фамилия', 'lastname', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (57, 6, 'имя', 'firstname', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (58, 6, 'отчество', 'middlename', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (59, 6, 'телефон', 'phone', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (60, 6, 'email', 'email', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (61, 6, 'паспорт №', 'passport', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (62, 6, 'паспорт выдан', 'passport_date', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (63, 6, 'почтовый код', 'postal', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (64, 6, 'область', 'region', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (65, 6, 'район', 'district', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (66, 6, 'город', 'city', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (67, 6, 'улица', 'street', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (68, 6, 'дом', 'home', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (69, 6, 'квартира', 'flat', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (70, 6, 'коментарий', 'comment', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (71, 6, 'invisible', 'invisible', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (72, 6, 'еще адрес', 'address', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (73, 6, 'станция метро', 'metro', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (74, 6, 'время доставки', 'delivery_time', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (75, 6, 'sl id города', 'sl_city_id', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (76, 6, 'sl pickup id', 'sl_pickup_id', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (77, 6, 'sl город', 'sl_city', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (78, 6, 'sl pickup name', 'sl_pickup_name', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (79, 6, 'sl pickup type', 'sl_pickup_type', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (80, 6, 'sl max вес', 'sl_pickup_max_weight', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (81, 6, 'sl тип доставки', 'sl_delivery_type', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (82, 6, 'sl номер заказа', 'sl_delivery_number', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (83, 6, 'sl код заказа', 'sl_delivery_code', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (84, 6, 'sl статус', 'sl_delivery_status', 0, 1);

INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 46, 'id');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 47, 'number');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 48, 'member_id');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 49, 'execution_date');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 50, 'delivery_id');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 51, 'delivery_title');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 52, 'status');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 53, 'status_text');

-- clean up print codes

INSERT INTO attr_value(id, attr_tp, value, locked) VALUES (-1, 0, 'Any', 1);

DELETE FROM lab_print_code
	WHERE width = 0 OR height = 0;
  
-- LAB_NORITSU_NHF
UPDATE lab_print_code
SET paper = -1,
    frame = -1,
    correction = -1,
    cutting = -1,
    is_duplex = 0,
    is_pdf = 0
WHERE src_type = 8;

-- LAB_XEROX
UPDATE lab_print_code
SET frame = -1,
    correction = -1,
    cutting = -1,
    is_pdf = 1
WHERE src_type = 5;

-- LAB_PLOTTER
UPDATE lab_print_code
SET frame = -1,
    correction = -1,
    cutting = -1,
    is_duplex = 0,
    is_pdf = 0
WHERE src_type = 6;

-- LAB_FUJI
UPDATE lab_print_code
SET correction = -1,
    cutting = -1,
    is_duplex = 0,
    is_pdf = 0
WHERE src_type = 2;

-- new orders idx 4 mail
ALTER TABLE orders
  ADD INDEX IDX_orders_state (source, state);

ALTER TABLE orders
  ADD INDEX IDX_orders_group (source, group_id);

ALTER TABLE orders
  ADD INDEX IDX_orders_client (source, client_id);

-- template for temp table vs varchar ids  
CREATE TABLE tmpt_vch_ids (
  id varchar(50) NOT NULL,
  PRIMARY KEY (id)
)
ENGINE = MYISAM
CHARACTER SET utf8
COLLATE utf8_general_ci;  

DELIMITER $$
--
-- update syncSource
--
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

  -- reset sync state
  UPDATE sources_sync ss
  SET ss.sync_date = NOW(),
      ss.sync_state = 0
  WHERE ss.id = pSourceId;

  -- keep in transaction
  START TRANSACTION;

    -- get next sync
    SELECT ss.sync INTO vSync
      FROM sources_sync ss
      WHERE ss.id = pSourceId;
    SET vSync = IFNULL(vSync, 0) + 1;

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

    -- update sync & group_id
    UPDATE orders o
      INNER JOIN tmp_orders t ON o.id=t.id
      SET o.sync = vSync, o.group_id=t.group_id, o.client_id=t.client_id
      WHERE t.source = pSourceId;

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
    INSERT INTO sources_sync (id, sync, sync_date, sync_state)
                      VALUES (pSourceId, vSync, NOW(), 1)
    ON DUPLICATE KEY UPDATE sync = vSync, sync_date = NOW(), sync_state = 1;
  COMMIT;
END
$$

DELIMITER ;

DELIMITER $$

CREATE 
PROCEDURE printLoad4PrintByDev (IN p_devlst text, IN p_photo int)
BEGIN
  DECLARE vIdx integer(11) DEFAULT (0);
  DECLARE vDev integer(7) DEFAULT (0);

  -- create temps
  CREATE TEMPORARY TABLE IF NOT EXISTS tmp_pgs LIKE tmpt_vch_ids;
  CREATE TEMPORARY TABLE IF NOT EXISTS tmp_res LIKE tmpt_vch_ids;

  SET vIdx = LOCATE(',', p_devlst);
  WHILE (vIdx > 0)
    DO
    -- p_devlst iteration
    SET vDev = LEFT(p_devlst, vIdx - 1);
    SET p_devlst = SUBSTR(p_devlst, vIdx + 1);
    SET vIdx = LOCATE(',', p_devlst);

    -- load pgs by device
    -- TODO add noritsu nhf as primary lab?
    INSERT IGNORE INTO tmp_pgs (id)
      SELECT pg.id
        FROM lab_device ld
          INNER JOIN lab l ON ld.lab = l.id
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device AND lr.is_online = 1
          INNER JOIN lab_print_code lpc ON l.src_type = lpc.src_type AND lpc.width = lr.width AND lpc.paper = lr.paper
          INNER JOIN print_group pg ON lpc.width = pg.width AND lpc.height = pg.height
            AND lpc.paper = pg.paper
            AND lpc.frame IN (-1, pg.frame)
            AND lpc.correction IN (-1, pg.correction)
            AND lpc.cutting IN (-1, pg.cutting)
            AND lpc.is_duplex = pg.is_duplex
            AND lpc.is_pdf = pg.is_pdf
        WHERE ld.id = vDev
          AND pg.state = 200
          AND (p_photo = 1 OR pg.book_type > 0)
          AND NOT EXISTS (SELECT 1
              FROM tmp_res
              WHERE tmp_res.id = pg.id)
        ORDER BY pg.state_date
      LIMIT 4;
    -- add noritsu nhf (secondary lab)
    INSERT IGNORE INTO tmp_pgs (id)
      SELECT pg.id
        FROM lab_device ld
          INNER JOIN lab l ON ld.lab = l.id AND l.src_type = 3 AND l.hot_nfs != ''
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device AND lr.paper IN (10, 11, 12, 13) AND lr.is_online = 1
          INNER JOIN lab_print_code lpc ON lpc.src_type = 8 AND lpc.width = lr.width
          INNER JOIN print_group pg ON lpc.width = pg.width
            AND lpc.height = pg.height
            AND lr.paper = pg.paper
            AND lpc.is_pdf = pg.is_pdf
        WHERE ld.id = vDev
          AND pg.state = 200
          AND (p_photo = 1 OR pg.book_type > 0)
          AND NOT EXISTS (SELECT 1
              FROM tmp_res
              WHERE tmp_res.id = pg.id)
        ORDER BY pg.state_date
      LIMIT 4;

    -- mysq: You cannot refer to a TEMPORARY table more than once in the same query.
    -- use additional temp - tmp_pgs
    INSERT IGNORE INTO tmp_res (id)
      SELECT id
        FROM tmp_pgs;

    DELETE
      FROM tmp_pgs;

  END WHILE;

  SELECT pg.*
    FROM tmp_res
      INNER JOIN print_group pg ON tmp_res.id = pg.id;

  -- kill temps
  DROP TEMPORARY TABLE tmp_pgs;
  DROP TEMPORARY TABLE tmp_res;
END
$$

DELIMITER ;
