-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

-- combo orders

-- internal source type for composite orders
INSERT INTO src_type(id, loc_type, name, state, book_part) VALUES(26, 1, 'Internal', 0, 0);

-- composite type table
CREATE TABLE compo_type (
  id tinyint(4) NOT NULL,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

INSERT INTO compo_type(id, name) VALUES
(0, '-'),
(1, 'Часть комбо'),
(2, 'Комбо');

ALTER TABLE book_synonym 
  ADD COLUMN compo_type tinyint(4) NOT NULL DEFAULT 0;
ALTER TABLE book_synonym 
  ADD CONSTRAINT FK_book_synonym_compo_type FOREIGN KEY (compo_type)
    REFERENCES compo_type(id);
    
CREATE TABLE book_synonym_compo (
  parent INT(7) NOT NULL,
  child INT(7) NOT NULL,
  PRIMARY KEY (parent, child)
)
ENGINE = INNODB; 
    
DROP PROCEDURE IF EXISTS bookSynonymClone;

DELIMITER $$

CREATE
PROCEDURE bookSynonymClone (IN pId int)
MODIFIES SQL DATA
BEGIN
  DECLARE vNewId int DEFAULT 0;
  DECLARE vNewSynonym varchar(100);

  SELECT SUBSTR(CONCAT(bs.synonym, '(', COUNT(*), ')'), 1, 100)
  INTO vNewSynonym
    FROM book_synonym bs
      INNER JOIN book_synonym bs1 ON bs.src_type = bs1.src_type
        AND bs.synonym_type = bs1.synonym_type
        AND (bs.synonym = bs1.synonym
        OR bs1.synonym LIKE CONCAT(bs.synonym, '(%)'))
    WHERE bs.id = pId;

  IF vNewSynonym IS NOT NULL
  THEN
    -- create
    INSERT INTO book_synonym (src_type, synonym, book_type, is_horizontal, synonym_type, has_backprint, order_program, compo_type)
      SELECT src_type, vNewSynonym, book_type, is_horizontal, synonym_type, has_backprint, order_program, compo_type
        FROM book_synonym
        WHERE id = pId;
    SET vNewId = LAST_INSERT_ID();
    -- clone templates
    INSERT INTO book_pg_template (book, book_part, width, height, height_add, paper, frame, correction, cutting, is_duplex, is_pdf, is_sheet_ready, sheet_width, sheet_len, page_width, page_len, page_hoffset, font_size, font_offset, fontv_size, fontv_offset, notching, stroke, bar_size, bar_offset, tech_bar, tech_add, tech_bar_step, tech_bar_color,
    is_tech_center, tech_bar_offset, is_tech_top, tech_bar_toffset, is_tech_bot, tech_bar_boffset, backprint, tech_stair_add, tech_stair_step, is_tech_stair_top, is_tech_stair_bot, lab_type, revers, mark_size, mark_offset, reprint_size, reprint_offset, queue_size, queue_offset, laminat)
      SELECT vNewId, book_part, width, height, height_add, paper, frame, correction, cutting, is_duplex, is_pdf, is_sheet_ready, sheet_width, sheet_len, page_width, page_len, page_hoffset, font_size, font_offset, fontv_size, fontv_offset, notching, stroke, bar_size, bar_offset, tech_bar, tech_add, tech_bar_step, tech_bar_color,
        is_tech_center, tech_bar_offset, is_tech_top, tech_bar_toffset, is_tech_bot, tech_bar_boffset, backprint, tech_stair_add, tech_stair_step, is_tech_stair_top, is_tech_stair_bot, lab_type, revers, mark_size, mark_offset, reprint_size, reprint_offset, queue_size, queue_offset, laminat
        FROM book_pg_template bpt
        WHERE bpt.book = pId;
  END IF;

  SELECT vNewId AS id;
END
$$

DELIMITER ;

ALTER TABLE print_group 
  ADD COLUMN compo_type tinyint(4) NOT NULL DEFAULT 0;
/*
  ALTER TABLE print_group 
  ADD CONSTRAINT FK_print_group_compo_type FOREIGN KEY (compo_type)
    REFERENCES compo_type(id);
    */

ALTER TABLE order_books 
  ADD COLUMN compo_type TINYINT(4) DEFAULT 0;
ALTER TABLE order_books 
  ADD COLUMN compo_pg VARCHAR(50) DEFAULT NULL COMMENT 'parent compo book, filled after this book included in compo book';
ALTER TABLE order_books 
  ADD COLUMN compo_book INT(5) DEFAULT NULL;
ALTER TABLE order_books 
  ADD INDEX IDX_order_books_compo(compo_pg, compo_book);
  
DROP PROCEDURE IF EXISTS fill_order;

DELIMITER $$

CREATE
PROCEDURE fill_order (IN p_orderid varchar(50))
MODIFIES SQL DATA
COMMENT 'post process order after fill'
BEGIN
  -- fill  books
  INSERT IGNORE INTO order_books (pg_id, target_pg, book, sheets, state, state_date, compo_type)
    SELECT pg.id,
      pg.id,
      gk.n,
      pg.sheet_num,
      pg.state,
      NOW(),
      pg.compo_type
      FROM print_group pg
        INNER JOIN generator_1k gk ON gk.n BETWEEN 1 AND pg.book_num
      WHERE pg.order_id = p_orderid
        AND pg.is_reprint = 0;
END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS tech_calc_books;

DELIMITER $$

CREATE 
PROCEDURE tech_calc_books (IN p_pg varchar(50))
MODIFIES SQL DATA
BEGIN
  UPDATE order_books obb
  INNER JOIN (SELECT t.pg_id,
      t.book,
      MAX(t.state) state,
      MAX(t.state_date) state_date
      FROM (SELECT ob.pg_id,
          ob.book,
          ob.sheets,
          tp.tech_type state,
          MAX(tl.log_date) state_date
          FROM order_books ob
            INNER JOIN tech_log tl ON ob.pg_id = tl.print_group
              AND ob.book = (tl.sheet DIV 100)
            INNER JOIN tech_point tp ON tp.id = tl.src_id
          WHERE ob.pg_id = p_pg
            AND tp.tech_type > ob.state
          GROUP BY ob.pg_id,
            ob.book,
            tp.tech_type
          HAVING COUNT(DISTINCT tl.sheet) >= ob.sheets) t
      --        HAVING COUNT(DISTINCT tl.sheet) >= IF(tp.tech_type=320, 2, ob.sheets) t
      GROUP BY t.pg_id,
        t.book) tt
    ON tt.pg_id = obb.pg_id
    AND tt.book = obb.book
    SET obb.state = tt.state,
        obb.state_date = tt.state_date;

  -- update childs for compo
  UPDATE order_books obb
  INNER JOIN (SELECT ob.pg_id, ob.book, ob.state, ob.state_date
      FROM order_books ob
      WHERE ob.pg_id = p_pg
        AND ob.compo_type = 2) t
    ON obb.compo_pg = t.pg_id
    AND obb.compo_book = t.book
    AND obb.state < t.state
    SET obb.state = t.state,
        obb.state_date = t.state_date;
END
$$

DELIMITER ;

CREATE TABLE sequences (
  sequence varchar(30) NOT NULL,
  value int(11) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (sequence)
)
ENGINE = INNODB,
AVG_ROW_LENGTH = 16384,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

DELIMITER $$

CREATE 
PROCEDURE sequence_next (IN pSequence varchar(50), OUT pNext int UNSIGNED)
MODIFIES SQL DATA
BEGIN
  INSERT INTO sequences (sequence, value)
    VALUES (pSequence, LAST_INSERT_ID(1))
  ON DUPLICATE KEY UPDATE
  VALUE = LAST_INSERT_ID(value + 1);
  SET pNext = LAST_INSERT_ID();
END
$$

DELIMITER ;

DELIMITER $$

CREATE 
FUNCTION next_order_id (pSource int)
RETURNS int(11)
BEGIN
  DECLARE res int UNSIGNED;
  CALL sequence_next(CONCAT('sid', pSource), res);
  RETURN res;
END
$$

DELIMITER ;

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (185, 'Ожидание комбо', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(162, 'Сборка комбо', 1, 0, 0, 0);

DROP PROCEDURE IF EXISTS syncSource;

DELIMITER $$

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

    IF NOT EXISTS
      (SELECT 1
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
      -- update sync & group_id
      UPDATE orders o
      INNER JOIN tmp_orders t ON o.id = t.id
        SET o.sync = vSync,
            o.group_id = t.group_id,
            o.client_id = t.client_id,
            o.src_date = IF(t.src_date = '1970-01-01 03:00', NOW(), t.src_date)
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
        SET to1.is_new = IFNULL(
        (SELECT 0
            FROM orders o
            WHERE o.id = to1.id), 1)
        WHERE to1.source = pSourceId;
      -- insert new
      INSERT INTO orders (id, source, src_id, src_date, state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts, group_id, client_id)
        SELECT id,
          source,
          src_id,
          IF(src_date = '1970-01-01 03:00', NOW(), src_date),
          state,
          state_date,
          ftp_folder,
          fotos_num,
          sync,
          is_preload,
          data_ts,
          group_id,
          client_id
          FROM tmp_orders to1
          WHERE to1.source = pSourceId
            AND to1.is_new = 1;
      -- remove new
      DELETE
        FROM tmp_orders
      WHERE source = pSourceId
        AND is_new = 1;

      -- check/process preload for compo
      UPDATE tmp_orders to1
        SET to1.is_new = 1
        WHERE to1.source = pSourceId AND to1.is_preload = 0
          AND EXISTS
          (SELECT 1
              FROM print_group pg
              WHERE pg.order_id = to1.id
                AND pg.state = 199
                AND pg.compo_type = 1);
      IF ROW_COUNT() > 0
      THEN
        -- update books
        UPDATE order_books ob
        INNER JOIN print_group pg ON ob.pg_id = pg.id
        INNER JOIN tmp_orders t ON t.id = pg.order_id
          SET ob.state = IF(pg.compo_type = 1, 185, 200),
              ob.state_date = NOW()
          WHERE t.source = pSourceId AND t.is_new = 1;
        -- update printgroup 
        UPDATE print_group
          SET state = IF(compo_type = 1, 185, 200),
              state_date = NOW()
          WHERE order_id IN
            (SELECT t.id
                FROM tmp_orders t
                WHERE t.source = pSourceId
                  AND t.is_new = 1);
        -- suborders
        UPDATE suborders s
        INNER JOIN tmp_orders t ON t.id = s.order_id
          SET s.state = 185,
              s.state_date = NOW()
          WHERE s.state = 199 AND t.source = pSourceId AND t.is_new = 1;
        -- orders 
        UPDATE orders o
        INNER JOIN tmp_orders t ON o.id = t.id
          SET o.state = 185
          WHERE o.state = 199 AND t.source = pSourceId AND t.is_new = 1;
      END IF;

      -- check/process other preload
      -- update books
      UPDATE order_books ob
      INNER JOIN print_group pg ON ob.pg_id = pg.id
        AND pg.state = 199
      INNER JOIN tmp_orders t ON t.id = pg.order_id
        SET ob.state = 200,
            ob.state_date = NOW()
        WHERE t.source = pSourceId AND t.is_preload = 0;

      -- update printgroup 
      UPDATE print_group
        SET state = 200,
            state_date = NOW()
        WHERE state = 199 AND order_id IN
          (SELECT t.id
              FROM tmp_orders t
              WHERE t.source = pSourceId
                AND t.is_preload = 0);
      -- suborders
      UPDATE suborders s
        SET s.state = 200,
            s.state_date = NOW()
        WHERE s.state = 199
          AND s.order_id IN
          (SELECT t.id
              FROM tmp_orders t
              WHERE t.source = pSourceId
                AND t.is_preload = 0);

      -- set extra state
      INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
        SELECT t.id,
          '',
          200,
          NOW(),
          NOW()
          FROM tmp_orders t
            INNER JOIN orders o ON o.id = t.id
              AND o.state = 199
              AND o.forward_state = 0
          WHERE t.source = pSourceId
            AND t.is_preload = 0
      ON DUPLICATE KEY UPDATE state_date = NOW();

      -- orders
      UPDATE orders o
        SET state = IF(o.forward_state = 0, 200, o.forward_state),
            state_date = NOW(),
            is_preload = 0
        WHERE o.state = 199
          AND o.id IN
          (SELECT t.id
              FROM tmp_orders t
              WHERE t.source = pSourceId
                AND t.is_preload = 0);

      -- cancel not in sync
      -- cancel print groups
      UPDATE print_group
        SET state = 505,
            state_date = NOW()
        WHERE order_id IN
          (SELECT id
              FROM orders o
              WHERE o.source = pSourceId
                AND o.state BETWEEN 100 AND 200
                AND o.sync != vSync);
      -- cancel suborders
      UPDATE suborders s
        SET s.state = 505,
            s.state_date = NOW()
        WHERE s.order_id IN
          (SELECT id
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
          AND EXISTS
          (SELECT 1
              FROM orders o
              WHERE o.id = t.id
                AND o.data_ts IS NOT NULL
                AND o.data_ts != o.data_ts
                AND o.state BETWEEN 120 AND 200);

      -- finde reload candidate vs sync cancel (state=505)
      UPDATE tmp_orders t
        SET t.reload = 1
        WHERE t.source = pSourceId
          AND EXISTS
          (SELECT 1
              FROM orders o
              WHERE o.id = t.id
                AND o.state = 505);

      -- clean orders 4 reload
      -- clean print_group
      DELETE
        FROM print_group
      WHERE order_id IN
        (SELECT id
            FROM tmp_orders t
            WHERE t.source = pSourceId
              AND t.reload = 1);
      -- clean extra info
      DELETE
        FROM order_extra_info
      WHERE id IN
        (SELECT id
            FROM tmp_orders t
            WHERE t.source = pSourceId
              AND t.reload = 1);
      -- clean suborder
      DELETE
        FROM suborders
      WHERE order_id IN
        (SELECT id
            FROM tmp_orders t
            WHERE t.source = pSourceId
              AND t.reload = 1);
      -- reset order state
      UPDATE orders o
        SET o.state = 100,
            o.state_date = NOW(),
            o.resume_load = 0,
            o.is_preload = IFNULL(
            (SELECT tt.is_preload
                FROM tmp_orders tt
                WHERE tt.id = o.id), 0)
        WHERE o.id IN
          (SELECT id
              FROM tmp_orders t
              WHERE t.source = pSourceId
                AND t.reload = 1);
      -- set project data time
      UPDATE orders o
        SET o.data_ts =
        (SELECT tt.data_ts
            FROM tmp_orders tt
            WHERE tt.id = o.id)
        WHERE o.source = pSourceId
          AND EXISTS
          (SELECT 1
              FROM tmp_orders t
              WHERE t.id = o.id
                AND t.data_ts IS NOT NULL
                AND t.data_ts != IFNULL(o.data_ts, ''));

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

DROP PROCEDURE IF EXISTS sync;

DELIMITER $$

CREATE 
PROCEDURE sync ()
MODIFIES SQL DATA
COMMENT 'синхронизация всех активных сайтов'
BEGIN
  DECLARE vId integer(7) DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  -- exclude internal sites (s.type 26)
  DECLARE vCur CURSOR FOR
  SELECT s.id
    FROM sources s
    WHERE s.online = 1
      AND s.type != 26;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  /*
  -- log sync
  INSERT INTO sync_log (event_time, id, source, sync_prev)
    SELECT NOW(), t.id, t.source, ss.sync
      FROM tmp_orders t
        LEFT OUTER JOIN sources_sync ss ON t.source = ss.id;
*/

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vId;
    IF vIsEnd
    THEN
      LEAVE wet;
    END IF;
    CALL syncSource(vId);
  END LOOP wet;
  CLOSE vCur;

END
$$

DELIMITER ;

CREATE TABLE tmpt_compo_child (
  pg_id varchar(50) NOT NULL,
  book int(5) NOT NULL,
  width int(11) DEFAULT NULL,
  sheets int(5) DEFAULT NULL,
  forced tinyint(1) DEFAULT 0,
  compo_book int(5) DEFAULT NULL,
  compo_pg int(5) DEFAULT NULL,
  PRIMARY KEY (pg_id, book)
)
ENGINE = INNODB,
AVG_ROW_LENGTH = 8192,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

CREATE TABLE tmpt_compo_parent (
  pg_num int(5) NOT NULL,
  book int(5) NOT NULL,
  book_unbroken int(5) DEFAULT 0,
  width int(11) DEFAULT NULL,
  sheets int(5) DEFAULT NULL,
  forsed tinyint(1) DEFAULT 0,
  PRIMARY KEY (book, pg_num)
)
ENGINE = INNODB,
AVG_ROW_LENGTH = 5461,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

DROP PROCEDURE IF EXISTS orderCancel;

DELIMITER $$

CREATE 
PROCEDURE orderCancel (IN pId varchar(50), IN pState int)
MODIFIES SQL DATA
BEGIN
  DECLARE vDate datetime DEFAULT NOW();
  DECLARE vState int;

  SET vState = IFNULL(pState, 510);

  IF vState < 500
  THEN
    SET vState = 510;
  END IF;

  UPDATE print_group pg
    SET pg.state = vState,
        pg.state_date = vDate
    WHERE pg.order_id = pId;

  -- clear compo links
  UPDATE order_books b
  INNER JOIN print_group pg ON b.compo_pg = pg.id
    SET b.state = 185,
        b.compo_pg = NULL,
        b.compo_book = NULL
    WHERE pg.order_id = pId AND pg.compo_type = 2;

  UPDATE suborders s
    SET s.state = vState,
        s.state_date = vDate
    WHERE s.order_id = pId;

  UPDATE orders o
    SET o.state = vState,
        o.state_date = vDate
    WHERE o.id = pId;

  DELETE
    FROM rack_orders
  WHERE order_id = pId;

END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS fill_order;

DELIMITER $$

CREATE 
PROCEDURE fill_order (IN p_orderid varchar(50))
MODIFIES SQL DATA
COMMENT 'post process order after fill'
BEGIN
  -- fill  books
  INSERT INTO order_books (pg_id, target_pg, book, sheets, state, state_date, compo_type)
    SELECT pg.id,
      pg.id,
      gk.n,
      pg.sheet_num,
      pg.state,
      NOW(),
      pg.compo_type
      FROM print_group pg
        INNER JOIN generator_1k gk ON gk.n BETWEEN 1 AND pg.book_num
      WHERE pg.order_id = p_orderid
        AND pg.is_reprint = 0
  ON DUPLICATE KEY UPDATE state = pg.state, state_date = NOW();

  -- update compo childs
  UPDATE order_books b
  INNER JOIN print_group pg ON b.compo_pg = pg.id
    SET b.state = pg.state,
        b.state_date = NOW()
    WHERE pg.order_id = p_orderid AND pg.compo_type = 2;

  UPDATE print_group pgc
  INNER JOIN order_books b ON b.pg_id = pgc.id
  INNER JOIN print_group pg ON b.compo_pg = pg.id
    SET pgc.state = pg.state,
        pgc.state_date = NOW()
    WHERE pg.order_id = p_orderid AND pg.compo_type = 2;
END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS compo_fulfill;

DELIMITER $$

CREATE 
PROCEDURE compo_fulfill (IN psource_id int, IN pparent_alias int, IN pwaite_limit int)
MODIFIES SQL DATA
this_proc:
  BEGIN
    DECLARE vParentAlias varchar(100);
    DECLARE vParentWidth int;
    DECLARE vParentBookPart int;
    DECLARE vMinWidth int;

    DECLARE vNotFound int;

    DECLARE vCurrPg int;
    DECLARE vCompoPg int;
    DECLARE vCompoMaxBook int;
    DECLARE vCompoBook int;
    DECLARE vCompoSheets int;

    DECLARE vChildPg varchar(50);
    DECLARE vChildBook int;
    DECLARE vChildWidth int;
    DECLARE vChildSheets int;
    DECLARE vChildForced int;

    DECLARE vOrderID varchar(50);

    DECLARE vCur CURSOR FOR
    SELECT pg_id, book, width, sheets, forced
      FROM tmp_compo_child t
      ORDER BY t.sheets, t.forced DESC, t.width DESC, t.pg_id;
    DECLARE vCurP CURSOR FOR
    SELECT t.pg_num, t.book
      FROM tmp_compo_parent t
      ORDER BY t.pg_num, t.book;

    DECLARE CONTINUE HANDLER FOR NOT FOUND
    BEGIN
      SET vNotFound = 1;
    END;

    SET vNotFound = 0;

    -- get compo params
    SELECT bs.synonym, bpt.sheet_len, bpt.book_part
    INTO vParentAlias, vParentWidth, vParentBookPart
      FROM book_synonym bs
        INNER JOIN book_pg_template bpt ON bs.id = bpt.book AND bpt.book_part IN (2, 5)
      WHERE bs.id = pparent_alias LIMIT 1;

    SELECT MIN(bpt.sheet_width)
    INTO vMinWidth
      FROM book_synonym_compo bsc
        INNER JOIN book_pg_template bpt ON bpt.book = bsc.child AND bpt.book_part = vParentBookPart
      WHERE bsc.parent = pparent_alias;
    -- TODO check max child width, exit or exclude?
    IF vNotFound = 1
      OR vMinWidth = 0
    THEN
      LEAVE this_proc;
    END IF;

    -- create or clean temp tables
    -- create temp
    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_compo_parent LIKE tmpt_compo_parent;
    CREATE TEMPORARY TABLE IF NOT EXISTS tmp_compo_child LIKE tmpt_compo_child;
    DELETE
      FROM tmp_compo_parent;
    DELETE
      FROM tmp_compo_child;

  main_block:
    BEGIN
      -- load books
      INSERT INTO tmp_compo_child (pg_id, book, width, sheets, forced)
        SELECT ob.pg_id, ob.book, bpt.sheet_width, pg.sheet_num, IF(pwaite_limit = 0 OR HOUR(TIMEDIFF(NOW(), ob.state_date)) < pwaite_limit, 0, 1) forced
          FROM order_books ob
            INNER JOIN print_group pg ON ob.pg_id = pg.id AND pg.book_part = vParentBookPart
            INNER JOIN book_synonym bs ON pg.alias = bs.synonym
            INNER JOIN book_synonym_compo bsc ON bs.id = bsc.child AND bsc.parent = pparent_alias
            INNER JOIN book_pg_template bpt ON bs.id = bpt.book AND bpt.book_part = vParentBookPart
          WHERE ob.state = 185
            AND ob.compo_type = 1
            AND ob.compo_pg IS NULL;
      --        ORDER BY pg.sheet_num, IF(pwaite_limit = 0 OR HOUR(TIMEDIFF(NOW(), ob.state_date)) < pwaite_limit, 0, 1) DESC, bpt.sheet_width DESC;
      IF ROW_COUNT() = 0
      THEN
        LEAVE main_block;
      END IF;
    scan_books:
      BEGIN
        SET vCompoPg = 0;
        SET vCompoMaxBook = 0;
        SET vCompoBook = 0;
        SET vCompoSheets = 0;

        OPEN vCur;
      wet:
        LOOP
          SET vNotFound = 0;
          FETCH vCur INTO vChildPg, vChildBook, vChildWidth, vChildSheets, vChildForced;
          IF vNotFound = 1
          THEN
            -- complited
            LEAVE wet;
          END IF;
          IF vCompoSheets != vChildSheets
          THEN
            -- new sheets per book, create printgroup
            SET vCompoPg = vCompoPg + 1;
            SET vCompoMaxBook = 0;
            SET vCompoSheets = vChildSheets;
            SET vCompoBook = 0;
          END IF;
          -- look for first book to fit child  
          SELECT t.book
          INTO vCompoBook
            FROM tmp_compo_parent t
            WHERE t.pg_num = vCompoPg
              AND t.width >= vChildWidth
            ORDER BY t.book LIMIT 1;
          IF vNotFound = 1
          THEN
            -- can't fit to any existing books, create new
            SET vCompoMaxBook = vCompoMaxBook + 1;
            -- TODO check vCompoMaxBook >999 and switch printgroup
            SET vCompoBook = vCompoMaxBook;
            SET vNotFound = 0;
          END IF;
          -- add or update parent 
          INSERT INTO tmp_compo_parent (pg_num, book, width, sheets, forsed)
            VALUES (vCompoPg
            , vCompoBook
            , vParentWidth - vChildWidth
            , vCompoSheets
            , vChildForced)
          ON DUPLICATE KEY UPDATE width = width - vChildWidth, forsed = IF(vChildForced = 1, 1, forsed);
          -- update child
          UPDATE tmp_compo_child t
            SET compo_book = vCompoBook,
                compo_pg = vCompoPg
            WHERE t.pg_id = vChildPg AND t.book = vChildBook;

        END LOOP wet;
        CLOSE vCur;
      END;
    finalize_block:
      BEGIN
        -- remove not fulfilled books, keep forced
        DELETE
          FROM tmp_compo_parent
        WHERE width >= vMinWidth
          AND forsed = 0;

        -- check if exists some 
        SET vNotFound = 0;
        SELECT 1
          FROM tmp_compo_parent LIMIT 1;
        IF vNotFound = 1
        THEN
          LEAVE finalize_block;
        END IF;

        -- renumber books!!!
        SET vNotFound = 0;
        SET vCompoMaxBook = 0;
        SET vCurrPg = 0;
        OPEN vCurP;
      wetp:
        LOOP
          FETCH vCurP INTO vCompoPg, vCompoBook;
          IF vNotFound = 1
          THEN
            -- complited
            LEAVE wetp;
          END IF;
          IF vCurrPg != vCompoPg
          THEN
            -- new printgroup, reset book counter
            SET vCompoMaxBook = 0;
            SET vCurrPg = vCompoPg;
          END IF;
          SET vCompoMaxBook = vCompoMaxBook + 1;
          UPDATE tmp_compo_parent
            SET book_unbroken = vCompoMaxBook
            WHERE pg_num = vCompoPg AND book = vCompoBook;
        END LOOP wetp;
        CLOSE vCurp;

        -- get next order id
        SET vOrderID = CONCAT_WS('_', psource_id, next_order_id(psource_id));
        -- run in trans 
        START TRANSACTION;
          -- create compo order, pgs, books
          -- orders state 150 - waite compo preprocess
          INSERT INTO orders (id, source, src_id, src_date, data_ts, state, state_date, ftp_folder, fotos_num)
            VALUES (vOrderID, psource_id, vOrderID, NOW(), NOW(), 150, NOW(), vOrderID, 0);
          -- extra info
          INSERT INTO order_extra_info (id, books)
            SELECT vOrderID, COUNT(*)
              FROM tmp_compo_parent;
          -- print groups
          INSERT INTO print_group (id, order_id, sub_id, state, state_date, width, height, paper, path
          , alias, file_num, book_type, book_part, book_num, sheet_num
          , is_pdf, prints, is_revers, laminat, compo_type)
            SELECT CONCAT_WS('_', vOrderID, p.pg_num), vOrderID, '', 150, NOW(), bpt.width, bpt.height, bpt.paper, CONCAT_WS('_', vOrderID, p.pg_num)
              , bs.synonym, MAX(p.book_unbroken) * p.sheets, bs.book_type, bpt.book_part, MAX(p.book_unbroken), p.sheets
              , bpt.is_pdf, MAX(p.book_unbroken) * p.sheets, bpt.revers, bpt.laminat, 2
              FROM book_synonym bs
                INNER JOIN book_pg_template bpt ON bs.id = bpt.book AND bpt.book_part = vParentBookPart
                INNER JOIN tmp_compo_parent p
              WHERE bs.id = pparent_alias
              GROUP BY p.pg_num;
          -- create books
          INSERT INTO order_books (pg_id, target_pg, book, sheets, state, state_date, compo_type)
            SELECT CONCAT_WS('_', vOrderID, p.pg_num), CONCAT_WS('_', vOrderID, p.pg_num), p.book_unbroken, p.sheets, 150, NOW(), 2
              FROM tmp_compo_parent p;
          -- update child books
          UPDATE order_books b
          INNER JOIN tmp_compo_child t ON b.pg_id = t.pg_id
            AND b.book = t.book
          INNER JOIN tmp_compo_parent tp ON t.compo_pg = tp.pg_num
            AND t.compo_book = tp.book
            SET b.state = 150,
                b.state_date = NOW(),
                b.compo_pg = CONCAT_WS('_', vOrderID, tp.pg_num),
                b.compo_book = tp.book_unbroken;
          -- recalc childs pg state
          UPDATE print_group pg
            SET pg.state = GREATEST(pg.state, IFNULL(
                (SELECT MAX(ob.state)
                    FROM order_books ob
                    WHERE ob.pg_id = pg.id), 0)),
                pg.state_date = NOW()
            WHERE pg.id IN
              (SELECT DISTINCT t.pg_id
                  FROM tmp_compo_child t);
        COMMIT;
      END;
    END;
    -- cleanup
    -- kill temps
    DROP TEMPORARY TABLE tmp_compo_parent;
    DROP TEMPORARY TABLE tmp_compo_child;

  END
  $$

DELIMITER ;
