-- main    
-- new main (virt)    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

DELETE FROM order_state WHERE id IN (111,112);

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(114, 'Ожидание проверки', 1, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(115, 'Проверка', 1, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(102, 'Ожидание загрузки исправлен', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(-325, 'Ошибка FTP', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(-326, 'Ошибка проверки', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(-327, 'Ошибка проверки MD5', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(-328, 'Ошибка проверки IM', 0, 0, 0, 0);

ALTER TABLE services
  ADD COLUMN appkey VARCHAR(50) DEFAULT NULL AFTER connections;
  
INSERT INTO attr_family(id, name) VALUES(7, 'OrderLoad');
INSERT INTO attr_family(id, name) VALUES(8, 'OrderLoadFiles');

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(88, 7, 'id источника', 'src_id', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(89, 7, 'Status источника', 'src_state', 0, 0);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(90, 7, 'filled источника', 'filled', 0, 0);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(91, 7, 'allow_to_change_status', 'canChangeRemoteState', 0, 0);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(92, 7, 'Папка ftp', 'ftp_folder', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(93, 7, 'Кол файлов', 'fotos_num', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(94, 8, 'Имя файла', 'file_name', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(95, 8, 'Размер файла', 'size', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(96, 8, 'hash файла', 'hash_remote', 0, 1);

INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 88, 'id');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 89, 'status');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 90, 'filled');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 91, 'allow_to_change_status');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 92, 'folder');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 93, 'quantity');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 94, 'name');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 95, 'size');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 96, 'hash');

CREATE TABLE orders_load (
  id varchar(50) NOT NULL DEFAULT '',
  source int(7) NOT NULL DEFAULT 0,
  src_id varchar(50) NOT NULL DEFAULT '',
  src_state int(5) DEFAULT 10,
  state int(5) DEFAULT 0,
  state_date datetime DEFAULT NULL,
  ftp_folder varchar(300) DEFAULT NULL,
  fotos_num int(5) DEFAULT 0,
  sync int(11) DEFAULT 0,
  clean_fs tinyint(1) DEFAULT 0,
  resume_load tinyint(1) DEFAULT 0,
  PRIMARY KEY (id),
  INDEX IDX_orders_state (source, state)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 170
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$
CREATE 
TRIGGER tg_orders_ftp_ai
	AFTER INSERT
	ON orders_load
	FOR EACH ROW
BEGIN
  INSERT INTO state_log (order_id, state, state_date)
                 VALUES (NEW.id, NEW.state, NOW());
END
$$

DELIMITER ;

DELIMITER $$

CREATE 
TRIGGER tg_orders_ftp_au
	AFTER UPDATE
	ON orders_load
	FOR EACH ROW
BEGIN
   IF NOT (OLD.state <=> NEW.state) THEN
    INSERT INTO state_log (order_id, state, state_date)
      VALUES (NEW.id, NEW.state, NOW());
  END IF;
END
$$

DELIMITER ;

CREATE TABLE order_files (
  order_id varchar(50) NOT NULL,
  file_name varchar(100) NOT NULL,
  state int(5) NOT NULL DEFAULT 0,
  state_date datetime DEFAULT NULL,
  previous_state int(5) DEFAULT 0,
  size int(11) DEFAULT 0,
  hash_remote varchar(100) DEFAULT NULL,
  hash_local varchar(100) DEFAULT NULL,
  chk tinyint(1) DEFAULT 0,
  PRIMARY KEY (order_id, file_name),
  CONSTRAINT FK_order_files_orders_load_id FOREIGN KEY (order_id)
  REFERENCES orders_load (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 165
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

CREATE 
TRIGGER tg_order_files_au
	AFTER UPDATE
	ON order_files
	FOR EACH ROW
BEGIN
  IF NOT (OLD.state <=> NEW.state) THEN
    INSERT INTO state_log (order_id, state, state_date, comment)
      VALUES (NEW.order_id, NEW.state, NOW(), CONCAT('file:', NEW.file_name));
  END IF;
END
$$

DELIMITER ;

DELIMITER $$

CREATE 
TRIGGER tg_order_files_bu
	BEFORE UPDATE
	ON order_files
	FOR EACH ROW
BEGIN
   IF NOT (OLD.state <=> NEW.state) THEN
    SET NEW.previous_state=OLD.state;
  END IF;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS sync_4load$$

CREATE 
PROCEDURE sync_4load()
  MODIFIES SQL DATA
  COMMENT 'синхронизация для загрузки всех активных сайтов типа фотокнига'
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
    CALL syncSource4load(vId);
  END LOOP wet;
  CLOSE vCur;

END
$$

DELIMITER ;

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
    -- remove canceled or complited, will be added as new  
    DELETE
      FROM orders_load
    WHERE source = pSourceId
      AND sync = vSync
      AND state > 465;
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

    -- cancel not in sync
    -- cancel orders
    UPDATE orders_load o
    SET state = 505,
        state_date = NOW()
    WHERE o.source = pSourceId
    AND o.state IN (100,102)
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

--- TODO lkz PhotoLoader ручками меняем связь state_log на orders_load

ALTER TABLE services
  ADD COLUMN priority INT(5) DEFAULT 0 AFTER appkey;

ALTER TABLE services
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (src_id, srvc_id, appkey);
  
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(97, 2, 'FTP источники', 'ftpAppKeys', 0, 0);
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(4, 97, 'appkey');

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(-329, 'Не загружен на FTP', 1, 0, 0, 0);

DELETE FROM alias_forward;
INSERT INTO alias_forward(alias, state) VALUES('obrazec', 450);

-- main 2016-09-14
