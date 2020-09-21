-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE order_books 
  ADD COLUMN num_in_batch INT(5) DEFAULT 0;

ALTER TABLE prn_queue_items 
  ADD COLUMN books_offset INT(11) DEFAULT 0;

ALTER TABLE book_pg_template 
  ADD COLUMN queue_book_size INT(5) DEFAULT 0;

ALTER TABLE book_pg_template 
  ADD COLUMN queue_book_offset VARCHAR(10) DEFAULT '+0+0';

DROP PROCEDURE IF EXISTS prn_queues_create3;

DELIMITER $$

CREATE 
PROCEDURE prn_queues_create3 (IN p_lab_type int, IN p_reprintsmode int, OUT p_result int)
BEGIN
  -- p_reprintsmode: if -1 include reprints in queue else create separate queue 4 reprints
  DECLARE vRes int DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vReprint int;
  DECLARE vAlias varchar(100);
  DECLARE vBookPart int;
  DECLARE vSheetNum int;
  DECLARE vBookPartName varchar(50);
  -- DECLARE vSheetNumBlock int;

  DECLARE vQueueId int(11);
  DECLARE vPgID varchar(50);
  DECLARE vSeq int;
  DECLARE vSheets int;
  DECLARE vSheetCounter int;
  DECLARE vSheetLimit int;

  DECLARE vBooks int;
  DECLARE vBooksCounter int;

  DECLARE vCur3 CURSOR FOR
  SELECT IF(p_reprintsmode = -1, 0, pg.is_reprint) is_reprint,
    pg.alias,
    pg.book_part,
    bp.name book_part_name,
    t.sort sheet_num
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN book_part bp ON bp.id = pg.book_part
    GROUP BY IF(p_reprintsmode = -1, 0, pg.is_reprint), pg.alias, t.sort, pg.book_part
    ORDER BY pg.alias, t.sort, pg.book_part;

  DECLARE vCurIt CURSOR FOR
  SELECT t.id, pg.prints, pg.book_num
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
    WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
      AND pg.alias = vAlias
      AND pg.book_part = vBookPart
      AND t.sort = vSheetNum
    ORDER BY pg.is_reprint DESC, pg.butt, pg.id;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  SET p_result = 0;

  -- calc sheets (get blok sheets count 4 cover)
  UPDATE tmp_pgid t
    SET sort =
    (SELECT IFNULL(pgb.sheet_num, pg.sheet_num)
        FROM print_group pg
          LEFT OUTER JOIN print_group pgb ON pg.book_part = 1
            AND pgb.order_id = pg.order_id
            AND pgb.sub_id = pg.sub_id
            AND pgb.path = pg.path
            AND pgb.book_part = 2
            AND pgb.is_reprint = 0
        WHERE pg.id = t.id);

  DELETE
    FROM tmp_pgid
  WHERE sort = 0
    OR sort IS NULL;

  -- check has some pg
  SELECT COUNT(*)
  INTO vRes
    FROM tmp_pgid;

  IF vRes > 0
  THEN
    SELECT ac.pq_sheet_limit
    INTO vSheetLimit
      FROM app_config ac;


    SET p_result = 1;
    -- by alias, book part, sheets in block
    OPEN vCur3;
  wet3:
    LOOP
      FETCH vCur3 INTO vReprint, vAlias, vBookPart, vBookPartName, vSheetNum;
      IF vIsEnd
      THEN
        LEAVE wet3;
      END IF;
      -- reset 
      SET vQueueId = 0;
      SET vSeq = 0;
      SET vSheets = 0;
      SET vSheetCounter = 0;
      SET vBooksCounter = 0;

      OPEN vCurIt;
    wetIt:
      LOOP
        FETCH vCurIt INTO vPgID, vSheets, vBooks;
        IF vIsEnd
        THEN
          SET vIsEnd = 0;
          LEAVE wetIt;
        END IF;
        IF vQueueId = 0
          OR (vSheetLimit > 0
          AND vSheetCounter >= vSheetLimit)
        THEN
          -- create queue
          INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
            VALUES (3, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vAlias, vBookPartName, vSheetNum), 0, 0, vReprint, p_lab_type);
          SET vQueueId = LAST_INSERT_ID();
          SET vSeq = 0;
          SET vSheetCounter = 0;
          SET vBooksCounter = 0;
        END IF;
        SET vSeq = vSeq + 1;
        -- add printgroup
        INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group, seq, books_offset)
          VALUES (vQueueId, 0, vPgID, vSeq, vBooksCounter);
        -- mark print group
        UPDATE print_group
          SET prn_queue = vQueueId
          WHERE id = vPgID;
        -- numerate books in batch
        UPDATE order_books ob
          SET ob.num_in_batch = vBooksCounter + ob.book
          WHERE ob.pg_id = vPgID;

        SET vSheetCounter = vSheetCounter + vSheets;
        SET vBooksCounter = vBooksCounter + vBooks;
      END LOOP wetIt;
      CLOSE vCurIt;

    /*
      -- create queue, strategy now is strategy type !!!! 
      INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
        VALUES (3, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vAlias, vBookPartName, vSheetNum), 0, 0, vReprint, p_lab_type);
      SET vQueueId = LAST_INSERT_ID();
      -- add printgroups
      INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
        SELECT vQueueId,
          0,
          t.id
          FROM tmp_pgid t
            INNER JOIN print_group pg ON pg.id = t.id
          WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
            AND pg.alias = vAlias
            AND pg.book_part = vBookPart
            AND pg.sheet_num = vSheetNum
            AND (pg.book_part != 1 OR EXISTS
            (SELECT 1
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
        WHERE id IN
          (SELECT qi.print_group
              FROM prn_queue_items qi
              WHERE qi.prn_queue = vQueueId);
*/
    END LOOP wet3;

    CLOSE vCur3;

    -- create prn_queue cover/block links
    INSERT INTO prn_queue_link (prn_queue, book_part, prn_queue_link, book_part_link)
      SELECT DISTINCT pg.prn_queue,
        pg.book_part,
        pg1.prn_queue,
        pg1.book_part
        FROM tmp_pgid t
          INNER JOIN print_group pg ON pg.id = t.id
          INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id
            AND pg.sub_id = pg1.sub_id
            AND pg.book_part != pg1.book_part
            AND pg1.book_part IN (1, 2)
            AND pg1.is_reprint = 0
            AND pg.prn_queue != pg1.prn_queue
            AND pg1.prn_queue != 0
        WHERE pg.book_part IN (1, 2)
          AND pg.is_reprint = 0
          AND pg.prn_queue != 0;
  END IF;

END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS prn_queues_create4;

DELIMITER $$

CREATE 
PROCEDURE prn_queues_create4 (IN p_lab_type int, IN p_reprintsmode int, OUT p_result int)
BEGIN
  -- TODO not tested
  DECLARE vRes int DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vReprint int;
  DECLARE vAlias varchar(100);
  DECLARE vBookPart int;
  DECLARE vBookPartName varchar(50);
  DECLARE vButt int;

  DECLARE vQueueId int(11);
  DECLARE vPgID varchar(50);
  DECLARE vSeq int;
  DECLARE vSheets int;
  DECLARE vSheetCounter int;
  DECLARE vSheetLimit int;

  DECLARE vBooks int;
  DECLARE vBooksCounter int;

  DECLARE vCur3 CURSOR FOR
  SELECT IF(p_reprintsmode = -1, 0, pg.is_reprint) is_reprint,
    pg.alias,
    pg.book_part,
    bp.name book_part_name,
    pg.butt
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN book_part bp ON bp.id = pg.book_part
    GROUP BY IF(p_reprintsmode = -1, 0, pg.is_reprint),
      pg.alias,
      pg.book_part,
      pg.butt
    ORDER BY pg.alias, pg.butt, pg.book_part;

  DECLARE vCurIt CURSOR FOR
  SELECT t.id, pg.prints, pg.book_num
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
    WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
      AND pg.alias = vAlias
      AND pg.book_part = vBookPart
      AND pg.butt = vButt
    ORDER BY pg.is_reprint DESC, t.sort, pg.id;


  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  SET p_result = 0;

  SELECT ac.pq_sheet_limit
  INTO vSheetLimit
    FROM app_config ac;

  -- remove without butt
  DELETE
    FROM tmp_pgid
  WHERE NOT EXISTS
    (SELECT 1
        FROM print_group
        WHERE print_group.id = tmp_pgid.id
          AND print_group.butt > 0);

  -- sort by sheet number (get from block for covers)
  UPDATE tmp_pgid t
    SET t.sort =
    (SELECT IFNULL(pgb.sheet_num, pg.sheet_num)
        FROM print_group pg
          LEFT OUTER JOIN print_group pgb ON pg.book_part = 1
            AND pgb.order_id = pg.order_id
            AND pgb.sub_id = pg.sub_id
            AND pgb.path = pg.path
            AND pgb.book_part = 2
            AND pgb.is_reprint = 0
        WHERE pg.id = t.id);

  -- check has some pg
  SELECT COUNT(*)
  INTO vRes
    FROM tmp_pgid;

  IF vRes > 0
  THEN
    SET p_result = 1;
    -- by alias, book part, sheets in block
    OPEN vCur3;
  wet3:
    LOOP
      FETCH vCur3 INTO vReprint, vAlias, vBookPart, vBookPartName, vButt;
      IF vIsEnd
      THEN
        LEAVE wet3;
      END IF;
      SET vQueueId = 0;
      SET vSeq = 0;
      SET vSheets = 0;
      SET vSheetCounter = 0;
      SET vBooksCounter = 0;

      OPEN vCurIt;
    wetIt:
      LOOP
        FETCH vCurIt INTO vPgID, vSheets, vBooks;
        IF vIsEnd
        THEN
          SET vIsEnd = 0;
          LEAVE wetIt;
        END IF;
        IF vQueueId = 0
          OR (vSheetLimit > 0
          AND vSheetCounter >= vSheetLimit)
        THEN
          -- create queue
          INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
            VALUES (4, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vAlias, vBookPartName, vButt), 0, 0, vReprint, p_lab_type);
          SET vQueueId = LAST_INSERT_ID();
          SET vSeq = 0;
          SET vSheetCounter = 0;
          SET vBooksCounter = 0;
        END IF;
        SET vSeq = vSeq + 1;
        -- add printgroup
        INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group, seq, books_offset)
          VALUES (vQueueId, 0, vPgID, vSeq, vBooksCounter);
        -- mark print group
        UPDATE print_group
          SET prn_queue = vQueueId
          WHERE id = vPgID;
        -- numerate books in batch
        UPDATE order_books ob
          SET ob.num_in_batch = vBooksCounter + ob.book
          WHERE ob.pg_id = vPgID;

        SET vSheetCounter = vSheetCounter + vSheets;
        SET vBooksCounter = vBooksCounter + vBooks;
      END LOOP wetIt;
      CLOSE vCurIt;

    END LOOP wet3;
    CLOSE vCur3;

    -- create prn_queue cover/block links
    INSERT INTO prn_queue_link (prn_queue, book_part, prn_queue_link, book_part_link)
      SELECT DISTINCT pg.prn_queue,
        pg.book_part,
        pg1.prn_queue,
        pg1.book_part
        FROM tmp_pgid t
          INNER JOIN print_group pg ON pg.id = t.id
          INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id
            AND pg.sub_id = pg1.sub_id
            AND pg.book_part != pg1.book_part
            AND pg1.book_part IN (1, 2)
            AND pg1.is_reprint = 0
            AND pg.prn_queue != pg1.prn_queue
            AND pg1.prn_queue != 0
        WHERE pg.book_part IN (1, 2)
          AND pg.is_reprint = 0
          AND pg.prn_queue != 0;
  END IF;

END
$$

DELIMITER ;

-- 2020-09-01 applied on test cycle
-- 2020-09-14 applied on main cycle