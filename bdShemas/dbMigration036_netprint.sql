-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

CREATE TABLE group_netprint (
  source int(7) NOT NULL,
  group_id int(11) NOT NULL,
  netprint_id varchar(50) NOT NULL,
  created datetime NOT NULL,
  state int(5) DEFAULT 0,
  box_number int(5) DEFAULT NULL,
  send tinyint(2) DEFAULT 0,
  PRIMARY KEY (source, group_id, netprint_id)
)
ENGINE = INNODB,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

DELIMITER $$

--
-- Создать триггер `group_netprint_bi`
--
CREATE
TRIGGER group_netprint_bi
BEFORE INSERT
ON group_netprint
FOR EACH ROW
BEGIN
  SET NEW.created = NOW();
END
$$

DELIMITER ;

ALTER TABLE sources_sync 
  ADD COLUMN np_sync_tstamp INT(11) DEFAULT 0;
  
  -- main 06.04.2020
  
UPDATE prn_strategy_type SET name = 'Алиас-разворот' WHERE id = 3;
INSERT INTO prn_strategy_type(id, name, default_priority, allow_manual) VALUES(4, 'Алиас-торец', 100, 1);

ALTER TABLE tmpt_vch_ids ADD COLUMN sort INT(11) DEFAULT 0;

DROP PROCEDURE IF EXISTS prn_queues_create1;

DELIMITER $$

CREATE 
PROCEDURE prn_queues_create1 (IN p_lab_type int, IN p_reprintsmode int, OUT p_result int)
BEGIN
  DECLARE vRes int DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vReprint int;
  DECLARE vPaper int;
  DECLARE vWidth int;
  DECLARE vPaperName varchar(50);
  DECLARE vBookPartName varchar(50);
  DECLARE vQueueId int(11);

  DECLARE vCur1 CURSOR FOR
  SELECT DISTINCT IF(p_reprintsmode = -1, 0, pg.is_reprint) is_reprint,
    pg.paper,
    av.value paper_name,
    pg.width
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN attr_value av ON av.id = pg.paper
        AND av.attr_tp = 2;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  SET p_result = 0;
  -- check has some pg
  SELECT COUNT(*)
  INTO vRes
    FROM tmp_pgid;

  IF vRes > 0
  THEN
    SET p_result = 1;
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
        VALUES (1, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vPaper, vWidth), 0, 0, vReprint, p_lab_type);
      SET vQueueId = LAST_INSERT_ID();
      -- add printgroups
      INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
        SELECT vQueueId,
          0,
          t.id
          FROM tmp_pgid t
            INNER JOIN print_group pg ON pg.id = t.id
          WHERE (p_reprintsmode = -1 OR pg.is_reprint = vReprint)
            AND pg.paper = vPaper
            AND pg.width = vWidth
          ORDER BY pg.is_reprint DESC, pg.id;
      -- mark print groups
      UPDATE print_group
        SET prn_queue = vQueueId
        WHERE id IN
          (SELECT qi.print_group
              FROM prn_queue_items qi
              WHERE qi.prn_queue = vQueueId);

    END LOOP wet1;
    CLOSE vCur1;
  END IF;

END
$$

DELIMITER ;

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
  DECLARE vSheetNumBlock int;

  DECLARE vQueueId int(11);
  DECLARE vBookPartName varchar(50);

  DECLARE vCur3 CURSOR FOR
  SELECT DISTINCT IF(p_reprintsmode = -1, 0, pg.is_reprint) is_reprint,
    pg.alias,
    pg.book_part,
    bp.name book_part_name,
    pg.sheet_num,
    pgb.sheet_num sheet_num_block
    FROM tmp_pgid t
      INNER JOIN print_group pg ON pg.id = t.id
      INNER JOIN book_part bp ON bp.id = pg.book_part
      LEFT OUTER JOIN print_group pgb ON pg.book_part = 1
        AND pgb.order_id = pg.order_id
        AND pgb.sub_id = pg.sub_id
        AND pgb.path = pg.path
        AND pgb.book_part = 2
        AND pgb.is_reprint = 0
    ORDER BY pg.alias, IFNULL(pgb.sheet_num, pg.sheet_num), pg.book_part;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  SET p_result = 0;

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
      FETCH vCur3 INTO vReprint, vAlias, vBookPart, vBookPartName, vSheetNum, vSheetNumBlock;
      IF vIsEnd
      THEN
        LEAVE wet3;
      END IF;
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

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  SET p_result = 0;

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
      -- create queue
      INSERT INTO prn_queue (strategy, is_active, created, label, has_sub, lab, is_reprint, lab_type)
        VALUES (4, 1, NOW(), CONCAT_WS(';', IF(vReprint, 'Перепечатка', NULL), vAlias, vBookPartName, vButt), 0, 0, vReprint, p_lab_type);
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
            AND pg.butt = vButt
          ORDER BY pg.is_reprint DESC, t.sort, pg.id;
      -- mark print groups
      UPDATE print_group
        SET prn_queue = vQueueId
        WHERE id IN
          (SELECT qi.print_group
              FROM prn_queue_items qi
              WHERE qi.prn_queue = vQueueId);

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

DROP PROCEDURE IF EXISTS prn_queues_create;

DELIMITER $$

CREATE 
PROCEDURE prn_queues_create (IN p_lab_type int, IN p_strategy_type int, IN p_booksonly int, IN p_reprintsmode int)
BEGIN
  -- p_reprintsmode: if -1 include reprints in queue else create separate queue 4 reprints
  DECLARE vRes int DEFAULT (0);
  -- load pg by labtype
  CALL loadPgByLabInternal(p_lab_type, 200);

  -- delete in queue and not book
  DELETE
    FROM tmp_pgid
  WHERE NOT EXISTS
    (SELECT 1
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
      CALL prn_queues_create1(p_lab_type, p_reprintsmode, vRes);
    ELSEIF p_strategy_type = 3
    THEN
      -- by alias, book part, sheets in block
      CALL prn_queues_create3(p_lab_type, p_reprintsmode, vRes);
    ELSEIF p_strategy_type = 4
    THEN
      -- by alias, book part, sheets in block
      CALL prn_queues_create4(p_lab_type, p_reprintsmode, vRes);
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

DROP PROCEDURE IF EXISTS printLoadQueueByLab;

DELIMITER $$

CREATE 
PROCEDURE printLoadQueueByLab (IN p_lab int, IN p_strategy int, IN p_booksonly int, IN p_noqueued int)
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
    SELECT pg.is_reprint,
      av.value paper_name,
      pg.paper,
      pg.width,
      MIN(pg.state_date) state_date,
      SUM(pg.prints) prints,
      SUM(pg.height * pg.prints) height
      FROM tmp_pgid tp
        INNER JOIN lab l ON l.id = p_lab
        INNER JOIN lab_device ld ON ld.lab = l.id
        INNER JOIN lab_rolls lr ON ld.id = lr.lab_device
        INNER JOIN print_group pg ON pg.id = tp.id
          AND pg.width = lr.width
          AND pg.paper = lr.paper
        INNER JOIN attr_value av ON av.id = pg.paper
          AND av.attr_tp = 2
      WHERE (p_noqueued = 0 || pg.prn_queue = 0)
        AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))
      GROUP BY pg.is_reprint,
        av.value,
        pg.paper,
        pg.width
      ORDER BY pg.is_reprint DESC, SUM(pg.height * pg.prints) DESC;
  ELSEIF p_strategy = 3
  THEN
    -- by alias & book part & sheet number (part)
    SELECT pg.is_reprint,
      pg.alias,
      pg.book_part,
      bp.name book_part_name,
      pg.sheet_num,
      pg.paper,
      av.value paper_name,
      pg.width,
      MIN(pg.state_date) state_date,
      SUM(pg.prints) prints,
      SUM(pg.height * pg.prints) height
      FROM tmp_pgid tp
        INNER JOIN print_group pg ON pg.id = tp.id
        INNER JOIN book_part bp ON bp.id = pg.book_part
        INNER JOIN attr_value av ON av.id = pg.paper
          AND av.attr_tp = 2
      WHERE (p_noqueued = 0 || pg.prn_queue = 0)
        AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))
      GROUP BY pg.is_reprint,
        pg.alias,
        pg.book_part,
        bp.name,
        pg.sheet_num,
        pg.paper,
        av.value,
        pg.width
      ORDER BY pg.is_reprint DESC, SUM(pg.prints) DESC;
  ELSEIF p_strategy = 4
  THEN
    -- by alias & book part & butt (part)
    SELECT pg.is_reprint,
      pg.alias,
      pg.book_part,
      bp.name book_part_name,
      pg.butt,
      pg.paper,
      av.value paper_name,
      MAX(pg.width) width,
      MIN(pg.state_date) state_date,
      SUM(pg.prints) prints,
      SUM(pg.height * pg.prints) height
      FROM tmp_pgid tp
        INNER JOIN print_group pg ON pg.id = tp.id
        INNER JOIN book_part bp ON bp.id = pg.book_part
        INNER JOIN attr_value av ON av.id = pg.paper
          AND av.attr_tp = 2
      WHERE (p_noqueued = 0 || pg.prn_queue = 0)
        AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))
      GROUP BY pg.is_reprint,
        pg.alias,
        pg.book_part,
        bp.name,
        pg.butt,
        pg.paper,
        av.value
      ORDER BY pg.is_reprint DESC, SUM(pg.prints) DESC;
  END IF;

  -- kill temp
  DROP TEMPORARY TABLE tmp_pgid;

END
$$

DELIMITER ;

-- ? applied on main cycle
