-- main cycle 2015-03-06
-- moskva 2015-03-10
SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (85, 6, 'at тип доставки', 'at_delivery_type', 0, 1);
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 85, 'address.atDeliveryType');

INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (4, 10, 'status');

CREATE TABLE alias_forward (
  id int(11) NOT NULL AUTO_INCREMENT,
  alias varchar(50) DEFAULT NULL,
  state int(5) DEFAULT 450,
  PRIMARY KEY (id),
  CONSTRAINT FK_alias_forward_order_state_id FOREIGN KEY (state)
  REFERENCES order_state (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 1
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE app_config (
  id tinyint(1) NOT NULL DEFAULT 1,
  production int(5) DEFAULT -1,
  PRIMARY KEY (id)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO app_config(id, production) VALUES (1, -1);

CREATE TABLE production (
  id int(5) NOT NULL,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO production(id, name) VALUES (0, 'Не распределен');
INSERT INTO production(id, name) VALUES (1, 'Рос. производство');
INSERT INTO production(id, name) VALUES (2, 'Бел. производство');
INSERT INTO production(id, name) VALUES (3, 'Укр. производство');

ALTER TABLE orders ADD COLUMN production INT(5) DEFAULT 0 AFTER client_id;
ALTER TABLE orders ADD COLUMN forward_state INTEGER(5) DEFAULT 0 AFTER production;

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (86, 2, 'Производство', 'production', 0, 1);
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (4, 86, 'production');

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (-319, 'Ошибка инициализации', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (-320, 'Не назначено производство', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (515, 'Отменен производство', 0, 0, 0, 0);

DELETE FROM order_state WHERE id = 310;

ALTER TABLE form_field_items CHANGE COLUMN delemiter delemiter VARCHAR(10) DEFAULT '';

INSERT INTO form_field(id, name, parametr, simplex) VALUES (14, 'Область/Город', 'pregion_city', 0);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (15, 'Улица/дом/кв', 'pstreet_h_f', 0);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (16, 'Индекс', 'ppostal', 0);

INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (8, 14, 0, 0, 0, 64, ', ');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (9, 14, 1, 0, 0, 65, ', ');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (10, 14, 2, 0, 0, 66, '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (11, 15, 0, 0, 0, 67, ' ');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (12, 15, 1, 0, 0, 68, ' кв. ');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (13, 15, 2, 0, 0, 69, '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (14, 16, 0, 0, 0, 63, '');


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
          INNER JOIN orders o ON o.id = t.id AND o.state = 199 AND o.forward_state=0
        WHERE t.source = pSourceId AND t.is_preload = 0
      ON DUPLICATE KEY UPDATE state_date= NOW();

    -- orders
    UPDATE orders o
      SET state = IF(o.forward_state=0,200,o.forward_state), state_date = NOW(), is_preload = 0
      WHERE o.state = 199 AND o.id IN (SELECT t.id FROM tmp_orders t WHERE t.source = pSourceId AND t.is_preload = 0);

    -- cancel not in sync
    -- cancel print groups
    UPDATE print_group
      SET state = 505, state_date = NOW()
      WHERE order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state BETWEEN 100 AND 200 AND o.sync != vSync);
    -- cancel suborders
    UPDATE suborders s
      SET s.state = 505, s.state_date = NOW()
      WHERE s.order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state BETWEEN 100 AND 200 AND o.sync != vSync);
    -- cancel orders
    UPDATE orders o
      SET state = 505, state_date = NOW()
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
