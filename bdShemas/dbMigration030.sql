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
INSERT INTO alias_forward(alias, state) VALUES('korob', 450);
INSERT INTO alias_forward(alias, state) VALUES('dvd-portmone', 450);
-- main 2016-09-14

CREATE TABLE print_group_rejects (
  id int(11) NOT NULL AUTO_INCREMENT,
  print_group varchar(50) NOT NULL DEFAULT '',
  thech_unit int(5) DEFAULT 0,
  book int(5) DEFAULT -1,
  sheet int(5) DEFAULT -1,
  activity int(10) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  INDEX IDX_print_group_rejects_activity (activity),
  CONSTRAINT FK_print_group_rejects_print_group_id FOREIGN KEY (print_group)
  REFERENCES print_group (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 2
AVG_ROW_LENGTH = 16384
CHARACTER SET utf8
COLLATE utf8_general_ci;
-- main 2016-10-10

DELIMITER $$

DROP PROCEDURE IF EXISTS prn_queues_create$$

CREATE 
PROCEDURE prn_queues_create(IN p_lab_type int, IN p_strategy_type int, IN p_booksonly int, IN p_reprintsmode int)
BEGIN
  DECLARE vRes int DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vReprint int;

  DECLARE vPaper int;
  DECLARE vWidth int;

  DECLARE vAlias varchar(100);
  DECLARE vBookPart int;
  DECLARE vSheetNum int;

  DECLARE vQueueId int(11);
  DECLARE vPaperName varchar(50);
  DECLARE vBookPartName varchar(50);

  DECLARE vCur1 CURSOR FOR
  SELECT DISTINCT IF(p_reprintsmode=-1, 0, pg.is_reprint) is_reprint, pg.paper, av.value paper_name, pg.width
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN attr_value av ON av.id = pg.paper AND av.attr_tp = 2;

  DECLARE vCur3 CURSOR FOR
  SELECT DISTINCT IF(p_reprintsmode=-1, 0, pg.is_reprint) is_reprint, pg.alias, pg.book_part, bp.name book_part_name, pg.sheet_num
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN book_part bp ON bp.id = pg.book_part;


  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  -- load pg by labtype
  CALL loadPgByLabInternal(p_lab_type, 200);

  -- delete in queue and not book
  DELETE
    FROM tmp_pgid
  WHERE NOT EXISTS (SELECT 1
        FROM print_group
        WHERE print_group.id = tmp_pgid.id
          AND print_group.prn_queue = 0
          AND (p_booksonly != 1 OR print_group.book_type IN (1, 2, 3)));
  -- check has some pg
  SELECT COUNT(*)
  INTO vRes
    FROM tmp_pgid;

  IF vRes > 0
  THEN
    IF p_strategy_type = 1
    THEN
      -- by paper, width
      OPEN vCur1;
    wet1:
    LOOP
      FETCH vCur1 INTO vReprint, vPaper, vPaperName, vWidth;
      IF vIsEnd
      THEN
        LEAVE wet1;
      END IF;
      -- create queue, strategy now is strategy type !!!! 
      INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
        VALUES (p_strategy_type, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vPaper, vWidth, IF(p_booksonly, 'Книги', NULL)), 0, 0, vReprint, p_lab_type);
      SET vQueueId = LAST_INSERT_ID();
      -- add printgroups
      INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
        SELECT vQueueId, 0, t.id
          FROM tmp_pgid t
            INNER JOIN print_group pg ON pg.id = t.id
          WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
            AND pg.paper = vPaper
            AND pg.width = vWidth
          ORDER BY pg.is_reprint DESC, pg.id;
      -- mark print groups
      UPDATE print_group
      SET prn_queue = vQueueId
      WHERE id IN (SELECT qi.print_group
          FROM prn_queue_items qi
          WHERE qi.prn_queue = vQueueId);

    END LOOP wet1;
      CLOSE vCur1;
    ELSEIF p_strategy_type = 3
    THEN
      -- by alias, book part, sheets
      OPEN vCur3;
    wet3:
      LOOP
        FETCH vCur3 INTO vReprint, vAlias, vBookPart, vBookPartName, vSheetNum;
        IF vIsEnd
        THEN
          LEAVE wet3;
        END IF;
        -- create queue, strategy now is strategy type !!!! 
        INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
          VALUES (p_strategy_type, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vAlias, vBookPartName, vSheetNum), 0, 0, vReprint, p_lab_type);
        SET vQueueId = LAST_INSERT_ID();
        -- add printgroups
        INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
          SELECT vQueueId, 0, t.id
            FROM tmp_pgid t
              INNER JOIN print_group pg ON pg.id = t.id
            WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
              AND pg.alias = vAlias
              AND pg.book_part = vBookPart
              AND pg.sheet_num = vSheetNum
          ORDER BY pg.is_reprint DESC, pg.id;
        -- mark print groups
        UPDATE print_group
        SET prn_queue = vQueueId
        WHERE id IN (SELECT qi.print_group
            FROM prn_queue_items qi
            WHERE qi.prn_queue = vQueueId);

      END LOOP wet3;
      CLOSE vCur3;
    ELSE
      SET vRes = 0;
    END IF;
  END IF;

  -- kill temp
  DROP TEMPORARY TABLE tmp_pgid;
  -- return result
  SELECT vRes value;
END
$$

DELIMITER ;

-- main 2016-10-19

DELIMITER $$

DROP PROCEDURE IF EXISTS prn_queues_create$$

CREATE 
PROCEDURE prn_queues_create(IN p_lab_type int, IN p_strategy_type int, IN p_booksonly int, IN p_reprintsmode int)
BEGIN
  DECLARE vRes int DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vReprint int;

  DECLARE vPaper int;
  DECLARE vWidth int;

  DECLARE vAlias varchar(100);
  DECLARE vBookPart int;
  DECLARE vSheetNum int;
  DECLARE vSheetNumBlock int;

  DECLARE vQueueId int(11);
  DECLARE vPaperName varchar(50);
  DECLARE vBookPartName varchar(50);

  DECLARE vCur1 CURSOR FOR
  SELECT DISTINCT IF(p_reprintsmode=-1, 0, pg.is_reprint) is_reprint, pg.paper, av.value paper_name, pg.width
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN attr_value av ON av.id = pg.paper AND av.attr_tp = 2;

  DECLARE vCur3 CURSOR FOR
  SELECT DISTINCT IF(p_reprintsmode=-1, 0, pg.is_reprint) is_reprint, pg.alias, pg.book_part, bp.name book_part_name, pg.sheet_num, pgb.sheet_num sheet_num_block
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN book_part bp ON bp.id = pg.book_part
      LEFT OUTER JOIN print_group pgb ON pg.book_part=1 AND pgb.order_id=pg.order_id AND pgb.sub_id=pg.sub_id AND pgb.book_part=2 AND pgb.is_reprint=0;


  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  -- load pg by labtype
  CALL loadPgByLabInternal(p_lab_type, 200);

  -- delete in queue and not book
  DELETE
    FROM tmp_pgid
  WHERE NOT EXISTS (SELECT 1
        FROM print_group
        WHERE print_group.id = tmp_pgid.id
          AND print_group.prn_queue = 0
          AND (p_booksonly != 1 OR print_group.book_type IN (1, 2, 3)));
  -- check has some pg
  SELECT COUNT(*)
  INTO vRes
    FROM tmp_pgid;

  IF vRes > 0
  THEN
    IF p_strategy_type = 1
    THEN
      -- by paper, width
      OPEN vCur1;
    wet1:
    LOOP
      FETCH vCur1 INTO vReprint, vPaper, vPaperName, vWidth;
      IF vIsEnd
      THEN
        LEAVE wet1;
      END IF;
      -- create queue, strategy now is strategy type !!!! 
      INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
        VALUES (p_strategy_type, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vPaper, vWidth, IF(p_booksonly, 'Книги', NULL)), 0, 0, vReprint, p_lab_type);
      SET vQueueId = LAST_INSERT_ID();
      -- add printgroups
      INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
        SELECT vQueueId, 0, t.id
          FROM tmp_pgid t
            INNER JOIN print_group pg ON pg.id = t.id
          WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
            AND pg.paper = vPaper
            AND pg.width = vWidth
          ORDER BY pg.is_reprint DESC, pg.id;
      -- mark print groups
      UPDATE print_group
      SET prn_queue = vQueueId
      WHERE id IN (SELECT qi.print_group
          FROM prn_queue_items qi
          WHERE qi.prn_queue = vQueueId);

    END LOOP wet1;
      CLOSE vCur1;
    ELSEIF p_strategy_type = 3
    THEN
      -- by alias, book part, sheets in block
      OPEN vCur3;
    wet3:
      LOOP
        FETCH vCur3 INTO vReprint, vAlias, vBookPart, vBookPartName, vSheetNum, vSheetNumBlock;
        IF vIsEnd
        THEN
          LEAVE wet3;
        END IF;
        -- create queue, strategy now is strategy type !!!! 
        INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
          VALUES (p_strategy_type, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vAlias, vBookPartName, vSheetNum), 0, 0, vReprint, p_lab_type);
        SET vQueueId = LAST_INSERT_ID();
        -- add printgroups
        INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
          SELECT vQueueId, 0, t.id
            FROM tmp_pgid t
              INNER JOIN print_group pg ON pg.id = t.id
            WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
              AND pg.alias = vAlias
              AND pg.book_part = vBookPart
              AND pg.sheet_num = vSheetNum
              AND (pg.book_part!=1 OR
                    EXISTS(SELECT 1 FROM print_group pgb 
                                  WHERE pgb.order_id=pg.order_id AND pgb.sub_id=pg.sub_id AND pgb.book_part=2 AND pgb.is_reprint=0 AND pgb.sheet_num=vSheetNumBlock))
          ORDER BY pg.is_reprint DESC, pg.id;
        -- mark print groups
        UPDATE print_group
        SET prn_queue = vQueueId
        WHERE id IN (SELECT qi.print_group
            FROM prn_queue_items qi
            WHERE qi.prn_queue = vQueueId);

      END LOOP wet3;
      CLOSE vCur3;
    ELSE
      SET vRes = 0;
    END IF;
  END IF;

  -- kill temp
  DROP TEMPORARY TABLE tmp_pgid;
  -- return result
  SELECT vRes value;
END
$$

DELIMITER ;

-- main 2016-11-03

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(507, 'Отменен', 0, 0, 0, 0);
-- main 2016-11-10
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(449, 'Ожидает ОТК', 0, 0, 0, 0);

DELIMITER $$

DROP PROCEDURE IF EXISTS extraStateReset$$
CREATE 
PROCEDURE extraStateReset(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int)
BEGIN
  DECLARE vMinExtraState int;
  DECLARE vNow datetime DEFAULT NOW();

  IF pState = 450
  THEN
    -- reset otk - reset extra_state.state_date & set order and order  childs state
    -- order esate and related suborder or all suborders
    UPDATE order_extra_state
      SET state_date = NULL
      WHERE id = pOrder AND (sub_id IN ('', pSubOrder) OR pSubOrder = '') AND state = pState;
    -- reset printgroups state
    UPDATE print_group pg
      SET pg.state = 449
      WHERE pg.order_id = pOrder AND (pg.sub_id = pSubOrder OR pSubOrder = '') AND pg.is_reprint = 0;
    -- reset suborders state
    UPDATE suborders s
      SET s.state = 449
      WHERE s.order_id = pOrder AND (s.sub_id = pSubOrder OR pSubOrder = '');
    -- reset order state
    UPDATE orders o
      SET o.state = 449
      WHERE o.id = pOrder;
  ELSE
    -- common reset - del extra_state
    -- clear
    DELETE
      FROM order_extra_state
    WHERE id = pOrder
      AND sub_id = pSubOrder
      AND state = pState;

    IF pSubOrder = ''
    THEN
      -- no suborders
      SELECT IFNULL(MIN(oes.state), 210)
      INTO vMinExtraState
        FROM order_extra_state oes
        WHERE oes.id = pOrder
          AND oes.state_date IS NOT NULL;
      UPDATE orders o
        SET o.state = vMinExtraState,
            o.state_date = vNow
        WHERE o.id = pOrder
          AND o.state > 210
          AND o.state != vMinExtraState; -- 210 > Print post???
    ELSE
      -- set suborder state
      SELECT IFNULL(MIN(oes.state), 210)
      INTO vMinExtraState
        FROM order_extra_state oes
        WHERE oes.id = pOrder
          AND oes.sub_id = pSubOrder
          AND oes.state_date IS NOT NULL;
      UPDATE suborders o
        SET o.state = vMinExtraState,
            o.state_date = vNow
        WHERE o.order_id = pOrder
          AND o.sub_id = pSubOrder
          AND o.state > 210
          AND o.state != vMinExtraState;
      -- set order extrastate
      SELECT IFNULL(MIN(oes.state), 210)
      INTO vMinExtraState
        FROM suborders so
          LEFT OUTER JOIN order_extra_state oes ON oes.id = so.order_id
            AND so.sub_id = oes.sub_id
            AND oes.state_date IS NOT NULL
        WHERE so.order_id = pOrder;
      -- del order extra state
      DELETE
        FROM order_extra_state
      WHERE id = pOrder
        AND sub_id = ''
        AND state > vMinExtraState;
      -- set order state
      UPDATE orders o
        SET o.state = vMinExtraState,
            o.state_date = vNow
        WHERE o.id = pOrder
          AND o.state > 210
          AND o.state != vMinExtraState; -- 210 > Print ???
    END IF;
  END IF;
END
$$

DELIMITER ;
-- main 2016-11-29

CREATE TABLE prn_queue_link (
  prn_queue int(11) NOT NULL,
  book_part int(5) DEFAULT NULL,
  prn_queue_link int(11) DEFAULT NULL,
  book_part_link int(5) DEFAULT NULL,
  PRIMARY KEY (prn_queue),
  CONSTRAINT FK_prn_queue_link_prn_queue FOREIGN KEY (prn_queue)
  REFERENCES prn_queue (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

DROP PROCEDURE IF EXISTS prn_queues_create$$
CREATE 
PROCEDURE prn_queues_create(IN p_lab_type int, IN p_strategy_type int, IN p_booksonly int, IN p_reprintsmode int)
BEGIN
  DECLARE vRes int DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vReprint int;

  DECLARE vPaper int;
  DECLARE vWidth int;

  DECLARE vAlias varchar(100);
  DECLARE vBookPart int;
  DECLARE vSheetNum int;
  DECLARE vSheetNumBlock int;

  DECLARE vQueueId int(11);
  DECLARE vPaperName varchar(50);
  DECLARE vBookPartName varchar(50);

  DECLARE vCur1 CURSOR FOR
  SELECT DISTINCT IF(p_reprintsmode = -1, 0, pg.is_reprint) is_reprint, pg.paper, av.value paper_name, pg.width
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN attr_value av ON av.id = pg.paper AND av.attr_tp = 2;

  DECLARE vCur3 CURSOR FOR
  SELECT DISTINCT IF(p_reprintsmode = -1, 0, pg.is_reprint) is_reprint, pg.alias, pg.book_part, bp.name book_part_name, pg.sheet_num, pgb.sheet_num sheet_num_block
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN book_part bp ON bp.id = pg.book_part
      LEFT OUTER JOIN print_group pgb ON pg.book_part = 1 AND pgb.order_id = pg.order_id AND pgb.sub_id = pg.sub_id AND pgb.book_part = 2 AND pgb.is_reprint = 0;


  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  -- load pg by labtype
  CALL loadPgByLabInternal(p_lab_type, 200);

  -- delete in queue and not book
  DELETE
    FROM tmp_pgid
  WHERE NOT EXISTS (SELECT 1
        FROM print_group
        WHERE print_group.id = tmp_pgid.id
          AND print_group.prn_queue = 0
          AND (p_booksonly != 1 OR print_group.book_type IN (1, 2, 3)));
  -- check has some pg
  SELECT COUNT(*)
  INTO vRes
    FROM tmp_pgid;

  IF vRes > 0
  THEN
    IF p_strategy_type = 1
    THEN
      -- by paper, width
      OPEN vCur1;
    wet1:
      LOOP
        FETCH vCur1 INTO vReprint, vPaper, vPaperName, vWidth;
        IF vIsEnd
        THEN
          LEAVE wet1;
        END IF;
        -- create queue, strategy now is strategy type !!!! 
        INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
          VALUES (p_strategy_type, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vPaper, vWidth, IF(p_booksonly, 'Книги', NULL)), 0, 0, vReprint, p_lab_type);
        SET vQueueId = LAST_INSERT_ID();
        -- add printgroups
        INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
          SELECT vQueueId, 0, t.id
            FROM tmp_pgid t
              INNER JOIN print_group pg ON pg.id = t.id
            WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
              AND pg.paper = vPaper
              AND pg.width = vWidth
            ORDER BY pg.is_reprint DESC, pg.id;
        -- mark print groups
        UPDATE print_group
          SET prn_queue = vQueueId
          WHERE id IN (SELECT qi.print_group
                FROM prn_queue_items qi
                WHERE qi.prn_queue = vQueueId);

      END LOOP wet1;
      CLOSE vCur1;
    ELSEIF p_strategy_type = 3
    THEN
      -- by alias, book part, sheets in block
      OPEN vCur3;
    wet3:
      LOOP
        FETCH vCur3 INTO vReprint, vAlias, vBookPart, vBookPartName, vSheetNum, vSheetNumBlock;
        IF vIsEnd
        THEN
          LEAVE wet3;
        END IF;
        -- create queue, strategy now is strategy type !!!! 
        INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
          VALUES (p_strategy_type, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vAlias, vBookPartName, vSheetNum), 0, 0, vReprint, p_lab_type);
        SET vQueueId = LAST_INSERT_ID();
        -- add printgroups
        INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
          SELECT vQueueId, 0, t.id
            FROM tmp_pgid t
              INNER JOIN print_group pg ON pg.id = t.id
            WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
              AND pg.alias = vAlias
              AND pg.book_part = vBookPart
              AND pg.sheet_num = vSheetNum
              AND (pg.book_part != 1 OR EXISTS (SELECT 1
                  FROM print_group pgb
                  WHERE pgb.order_id = pg.order_id
                    AND pgb.sub_id = pg.sub_id
                    AND pgb.book_part = 2
                    AND pgb.is_reprint = 0
                    AND pgb.sheet_num = vSheetNumBlock))
            ORDER BY pg.is_reprint DESC, pg.id;
        -- mark print groups
        UPDATE print_group
          SET prn_queue = vQueueId
          WHERE id IN (SELECT qi.print_group
                FROM prn_queue_items qi
                WHERE qi.prn_queue = vQueueId);

      END LOOP wet3;

      CLOSE vCur3;

      -- create prn_queue cover/block links
      INSERT INTO prn_queue_link (prn_queue, book_part, prn_queue_link, book_part_link)
        SELECT DISTINCT pg.prn_queue, pg.book_part, pg1.prn_queue, pg1.book_part
          FROM tmp_pgid t
            INNER JOIN print_group pg ON pg.id = t.id
            INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id AND pg.sub_id = pg1.sub_id
              AND pg.book_part != pg1.book_part AND pg1.book_part IN (1, 2) AND pg1.is_reprint = 0 AND pg.prn_queue != pg1.prn_queue AND pg1.prn_queue != 0
          WHERE pg.book_part IN (1, 2)
            AND pg.is_reprint = 0
            AND pg.prn_queue != 0;
    ELSE
      SET vRes = 0;
    END IF;
  END IF;

  -- kill temp
  DROP TEMPORARY TABLE tmp_pgid;
  -- return result
  SELECT vRes value;
END
$$

DELIMITER ;