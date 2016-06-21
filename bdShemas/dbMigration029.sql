-- main    
-- new main (virt)    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';


ALTER TABLE prn_strategy_type
  ADD COLUMN allow_manual TINYINT(4) DEFAULT 0 AFTER default_priority;

INSERT INTO prn_strategy_type(id, name, default_priority, allow_manual) VALUES(3, 'Партия', 100, 1);
UPDATE prn_strategy_type SET allow_manual = 1 WHERE id = 1;

ALTER TABLE prn_queue
  DROP FOREIGN KEY FK_prn_queue_prn_strategy_id;

ALTER TABLE prn_queue
  ADD COLUMN is_reprint TINYINT(1) DEFAULT 0 AFTER lab;
  
ALTER TABLE prn_queue_items
  DROP FOREIGN KEY FK_prn_queue_items_prn_queue_id;
ALTER TABLE prn_queue_items
  ADD CONSTRAINT FK_prn_queue_items_prn_queue_id FOREIGN KEY (prn_queue)
    REFERENCES prn_queue(id) ON DELETE CASCADE ON UPDATE CASCADE;

DELIMITER $$

DROP PROCEDURE IF EXISTS prn_queue_check$$

CREATE
PROCEDURE prn_queue_check()
BEGIN
  DECLARE vIsEnd int DEFAULT (0);
  DECLARE vQueueId int(11);

  DECLARE vComplited int DEFAULT (0);

  DECLARE vCur CURSOR FOR
  SELECT pq.id
    FROM prn_queue pq
    WHERE pq.is_active
      AND pq.complited IS NULL
      AND pq.strategy IN (1,3);

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vQueueId;
    IF vIsEnd
    THEN
      LEAVE wet;
    END IF;

    -- check sub Queues
    -- cover
    SET vComplited = prn_queue_complited(vQueueId, 0);

    IF vComplited > 0
    THEN
      UPDATE prn_queue
      SET is_active = 0,
          complited = NOW()
      WHERE id = vQueueId;
    END IF;

  END LOOP wet;

  CLOSE vCur;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS prn_queue_delete$$

CREATE
PROCEDURE prn_queue_delete(IN p_queue int)
BEGIN
  DECLARE vIsStarted int DEFAULT (0);

  SELECT IF(pq.started IS NULL, 0, 1)
  INTO vIsStarted
    FROM prn_queue pq
    WHERE pq.id = p_queue;

  IF vIsStarted = 0
  THEN
    -- unmark printgroups
    UPDATE print_group
    SET prn_queue = 0
    WHERE id IN (SELECT qi.print_group
                  FROM prn_queue_items qi
                  WHERE qi.prn_queue = p_queue)
          AND prn_queue = p_queue;
    -- delete
    DELETE
      FROM prn_queue
    WHERE id = p_queue;
    -- send result
    SELECT 1 value;
  ELSE
    -- send result
    SELECT 0 value;
  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS loadPgByLabInternal$$

CREATE
PROCEDURE loadPgByLabInternal(IN p_lab_type int, IN p_state int)
  READS SQL DATA
BEGIN
  -- create temp
  CREATE TEMPORARY TABLE IF NOT EXISTS tmp_pgid LIKE tmpt_vch_ids;
  -- clear temp
  DELETE
    FROM tmp_pgid;

  -- fill temp vs id
  INSERT INTO tmp_pgid (id)
    SELECT t.id
      FROM (SELECT pg.id, IFNULL(IFNULL(MAX(IF(s.type = 7 AND bs.synonym_type = 1, bpt.lab_type, NULL)), MAX(IF(bs.synonym_type = 0, bpt.lab_type, NULL))), 0) lab_type
          FROM lab_print_code lpc
            INNER JOIN print_group pg ON lpc.width = pg.width AND lpc.height >= pg.height
              AND lpc.paper = pg.paper
              AND lpc.frame IN (-1, pg.frame)
              AND lpc.correction IN (-1, pg.correction)
              AND lpc.cutting IN (-1, pg.cutting)
              AND lpc.is_duplex IN (-1, pg.is_duplex)
              AND lpc.is_pdf = pg.is_pdf
            INNER JOIN orders o ON pg.order_id = o.id
            INNER JOIN sources s ON o.source = s.id
            INNER JOIN book_part bp ON bp.id = pg.book_part
            LEFT OUTER JOIN book_synonym bs ON bs.synonym = pg.alias AND bs.synonym_type != -1
            LEFT OUTER JOIN book_pg_template bpt ON bs.id = bpt.book AND bpt.book_part = pg.book_part
          WHERE lpc.src_type = p_lab_type
            AND pg.state = p_state
          GROUP BY pg.id
        UNION
        -- noritsu nfh
        SELECT pg.id, IFNULL(IFNULL(MAX(IF(s.type = 7 AND bs.synonym_type = 1, bpt.lab_type, NULL)), MAX(IF(bs.synonym_type = 0, bpt.lab_type, NULL))), 0) lab_type
          FROM lab_print_code lpc 
            INNER JOIN print_group pg ON lpc.width = pg.width AND lpc.height >= pg.height
              AND lpc.paper = pg.paper
              AND lpc.frame IN (-1, pg.frame)
              AND lpc.correction IN (-1, pg.correction)
              AND lpc.cutting IN (-1, pg.cutting)
              AND lpc.is_duplex IN (-1, pg.is_duplex)
              AND lpc.is_pdf = pg.is_pdf
            INNER JOIN orders o ON pg.order_id = o.id
            INNER JOIN sources s ON o.source = s.id
            INNER JOIN book_part bp ON bp.id = pg.book_part
            LEFT OUTER JOIN book_synonym bs ON bs.synonym = pg.alias AND bs.synonym_type != -1
            LEFT OUTER JOIN book_pg_template bpt ON bs.id = bpt.book AND bpt.book_part = pg.book_part
          WHERE p_lab_type=3
            AND lpc.src_type = 8 
            AND pg.state = p_state
          GROUP BY pg.id) t
      WHERE t.lab_type IN (0, p_lab_type);

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS printLoadQueueByLab$$

CREATE
PROCEDURE printLoadQueueByLab(IN p_lab int, IN p_strategy int, IN p_booksonly int, IN p_noqueued int)
BEGIN
  DECLARE vLabType int;

  SELECT MIN(l.src_type)
  INTO vLabType
    FROM lab l
    WHERE l.id = p_lab;

  -- load pg by lab
  CALL loadPgByLabInternal(vLabType, 200);

  -- get queues for strategy
  IF p_strategy = 1
  THEN
    -- by paper & width (roll)
    -- exclude roll?
    SELECT pg.is_reprint, av.value paper_name, pg.paper, pg.width, MIN(pg.state_date) state_date, SUM(pg.prints) prints, SUM(pg.height * pg.prints) height
      FROM tmp_pgid tp
        INNER JOIN lab l ON l.id = p_lab
        INNER JOIN lab_device ld ON ld.lab = l.id
        INNER JOIN lab_rolls lr ON ld.id = lr.lab_device
        INNER JOIN print_group pg ON pg.id = tp.id AND pg.width = lr.width AND pg.paper = lr.paper
        INNER JOIN attr_value av ON av.id = pg.paper AND av.attr_tp = 2
      WHERE (p_noqueued = 0 || pg.prn_queue = 0)
        AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))
      GROUP BY pg.is_reprint, av.value, pg.paper, pg.width
      ORDER BY pg.is_reprint DESC, SUM(pg.height * pg.prints) DESC;
  ELSEIF p_strategy = 3
  THEN
    -- by alias & book part & sheet number (part)
    SELECT pg.is_reprint, pg.alias, pg.book_part, bp.name book_part_name, pg.sheet_num, pg.paper, av.value paper_name, pg.width, MIN(pg.state_date) state_date, SUM(pg.prints) prints, SUM(pg.height * pg.prints) height
      FROM tmp_pgid tp
        INNER JOIN print_group pg ON pg.id = tp.id
        INNER JOIN book_part bp ON bp.id = pg.book_part
        INNER JOIN attr_value av ON av.id = pg.paper AND av.attr_tp = 2
      WHERE (p_noqueued = 0 || pg.prn_queue = 0)
        AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))
      GROUP BY pg.is_reprint, pg.alias, pg.book_part, bp.name, pg.sheet_num, pg.paper, av.value, pg.width
      ORDER BY pg.is_reprint DESC, SUM(pg.prints) DESC;
  END IF;

  -- kill temp
  DROP TEMPORARY TABLE tmp_pgid;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS prn_queue1_create$$

CREATE
PROCEDURE prn_queue1_create(IN p_lab int, IN p_reprint int, IN p_paper int, IN p_width int, IN p_booksonly int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vRes int;
  DECLARE vQueueId int(11);
  DECLARE vPaper varchar(50);
  DECLARE vLabType int;

  SELECT MIN(l.src_type)
  INTO vLabType
    FROM lab l
    WHERE l.id = p_lab;

  -- load pg by lab
  CALL loadPgByLabInternal(vLabType, 200);

  DELETE
    FROM tmp_pgid
  WHERE NOT EXISTS (SELECT 1
        FROM print_group
        WHERE print_group.id = tmp_pgid.id
          AND print_group.is_reprint = p_reprint
          AND print_group.paper = p_paper
          AND print_group.width = p_width
          AND print_group.prn_queue = 0
          AND (p_booksonly != 1 OR print_group.book_type IN (1, 2, 3)));
  
  -- check 
  SELECT COUNT(*)
  INTO vRes
    FROM tmp_pgid;

  IF vRes > 0
  THEN
    -- get paper name
    SELECT av.value
    INTO vPaper
      FROM attr_value av
      WHERE av.id = p_paper
        AND av.attr_tp = 2;

    -- create queue, strategy now is strategy type !!!! 
    INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint)
      VALUES (1, 1, NOW(), CONCAT_WS(';', IF(p_reprint, 'Перепечатка', NULL), vPaper, p_width, IF(p_booksonly,'Книги',NULL)), 0, p_lab, p_reprint);
    SET vQueueId = LAST_INSERT_ID();

    -- add printgroups
    INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
      SELECT vQueueId, 0, tp.id
        FROM tmp_pgid tp;

    -- mark printgroups
    UPDATE print_group
    SET prn_queue = vQueueId
    WHERE id IN (SELECT qi.print_group
        FROM prn_queue_items qi
        WHERE qi.prn_queue = vQueueId);

    -- send result
    SELECT 1 value;
  ELSE
    SELECT 0 value;
  END IF;

  -- kill temp
  DROP TEMPORARY TABLE tmp_pgid;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS prn_queue3_create$$

CREATE
PROCEDURE prn_queue3_create(IN p_lab int, IN p_reprint int, IN p_alias varchar(200), IN p_book_part int, IN p_sheet_num int, IN p_booksonly int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vRes int;
  DECLARE vQueueId int(11);
  DECLARE vBookPart varchar(50);
  DECLARE vLabType int;

  SELECT MIN(l.src_type)
  INTO vLabType
    FROM lab l
    WHERE l.id = p_lab;

  -- load pg by lab
  CALL loadPgByLabInternal(vLabType, 200);

  DELETE
    FROM tmp_pgid
  WHERE NOT EXISTS (SELECT 1
        FROM print_group
        WHERE print_group.id = tmp_pgid.id
          AND print_group.is_reprint = p_reprint
          AND print_group.alias = p_alias
          AND print_group.book_part = p_book_part
          AND print_group.sheet_num = p_sheet_num
          AND print_group.prn_queue = 0
          AND (p_booksonly != 1 OR print_group.book_type IN (1, 2, 3)));
  
  -- check 
  SELECT COUNT(*)
  INTO vRes
    FROM tmp_pgid;

  IF vRes > 0
  THEN
    -- get book part name
    SELECT bp.name
    INTO vBookPart
      FROM book_part bp
      WHERE bp.id=p_book_part;

    -- create queue, strategy now is strategy type !!!! 
    INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint)
      VALUES (3, 1, NOW(), CONCAT_WS(';', IF(p_reprint, 'Перепечатка', NULL), p_alias, vBookPart, p_sheet_num, IF(p_booksonly,'Книги',NULL)), 0, p_lab, p_reprint);
    SET vQueueId = LAST_INSERT_ID();

    -- add printgroups
    INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
      SELECT vQueueId, 0, tp.id
        FROM tmp_pgid tp;

    -- mark printgroups
    UPDATE print_group
    SET prn_queue = vQueueId
    WHERE id IN (SELECT qi.print_group
        FROM prn_queue_items qi
        WHERE qi.prn_queue = vQueueId);

    -- send result
    SELECT 1 value;
  ELSE
    SELECT 0 value;
  END IF;

  -- kill temp
  DROP TEMPORARY TABLE tmp_pgid;

END
$$

DELIMITER ;

ALTER TABLE print_group
  ADD COLUMN is_finalizeprint TINYINT(1) DEFAULT 0 AFTER butt,
  ADD COLUMN sheets_per_file INT(5) DEFAULT 1 AFTER is_finalizeprint,
  ADD COLUMN is_revers TINYINT(1) DEFAULT 0 AFTER sheets_per_file;
  