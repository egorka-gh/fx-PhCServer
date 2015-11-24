-- main 2015-11-02   
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

CREATE TABLE lab_meter_type (
  id int(7) NOT NULL,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO lab_meter_type(id, name) VALUES
(0, 'Размещение');
INSERT INTO lab_meter_type(id, name) VALUES
(1, 'Печать');
INSERT INTO lab_meter_type(id, name) VALUES
(10, 'Стоп');

DELIMITER $$

DROP PROCEDURE IF EXISTS printLoadInPrintQueueByLab$$

CREATE
PROCEDURE printLoadInPrintQueueByLab (IN p_lab int)
BEGIN
  /* 4 byDevice vercion ?
  SELECT t.lab_name, t.lab, av.value paper_name, t.paper, t.width, SUM(t.height) / 1000 print_queue_len
    FROM (SELECT l.name lab_name, ld.lab, pg.id, pg.paper, pg.width, pg.height * pg.prints height
        FROM lab l
          INNER JOIN lab_device ld ON ld.lab = l.id
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device
          INNER JOIN lab_print_code lpc ON l.src_type = lpc.src_type AND lpc.width = lr.width AND lpc.paper = lr.paper
          INNER JOIN print_group pg ON pg.destination = l.id
            AND lpc.width = pg.width AND lpc.height >= pg.height
            AND lpc.paper = pg.paper
            AND lpc.frame IN (-1, pg.frame)
            AND lpc.correction IN (-1, pg.correction)
            AND lpc.cutting IN (-1, pg.cutting)
            AND lpc.is_duplex IN (-1, pg.is_duplex)
            AND lpc.is_pdf = pg.is_pdf
        WHERE l.id = p_lab
          AND pg.state IN (250, 255)
      UNION
      SELECT l.name lab_name, ld.lab, pg.id, pg.paper, pg.width, pg.height * pg.prints height
        FROM lab l
          INNER JOIN lab_device ld ON ld.lab = l.id
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device AND lr.paper IN (10, 11, 12, 13)
          INNER JOIN lab_print_code lpc ON lpc.src_type = 8 AND lpc.width = lr.width
          INNER JOIN print_group pg ON pg.destination = l.id
            AND lpc.width = pg.width
            AND lpc.height >= pg.height
            AND lr.paper = pg.paper
            AND lpc.is_pdf = pg.is_pdf
        WHERE l.id = p_lab
          AND l.src_type = 3
          AND l.hot_nfs != ''
          AND pg.state IN (250, 255)) t
      INNER JOIN attr_value av ON av.id = t.paper AND av.attr_tp = 2
    GROUP BY t.lab_name, t.lab, av.value, t.paper, t.width
    ORDER BY SUM(t.height) DESC;
  */
  SELECT t.lab_name, t.lab, av.value paper_name, t.paper, t.width, SUM(t.height) / 1000 print_queue_len
    FROM (SELECT l.name lab_name, l.id lab, pg.id, pg.paper, pg.width, pg.height * pg.prints height
        FROM lab l
          INNER JOIN print_group pg ON pg.destination = l.id
          INNER JOIN orders o ON pg.order_id = o.id
        WHERE l.id = p_lab
          AND pg.state IN (250, 255)
          AND o.state < 450) t
      INNER JOIN attr_value av ON av.id = t.paper AND av.attr_tp = 2
    GROUP BY t.lab_name, t.lab, av.value, t.paper, t.width
    ORDER BY SUM(t.height) DESC;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS printLoad4PrintByDev$$

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
            AND lpc.is_duplex IN (-1, pg.is_duplex)
            AND lpc.is_pdf = pg.is_pdf
        WHERE ld.id = vDev
          AND pg.state = 200
          AND (p_photo = 1 OR pg.book_type IN (1, 2, 3))
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
          AND (p_photo = 1 OR pg.book_type IN (1, 2, 3))
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

  SELECT pg.*, o.source source_id, o.ftp_folder order_folder, IFNULL(s.alias, pg.path) alias
    FROM tmp_res
      INNER JOIN print_group pg ON tmp_res.id = pg.id
      INNER JOIN orders o ON pg.order_id = o.id
      LEFT OUTER JOIN suborders s ON s.order_id = pg.order_id AND s.sub_id = pg.sub_id;

  -- kill temps
  DROP TEMPORARY TABLE tmp_pgs;
  DROP TEMPORARY TABLE tmp_res;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS printLoadQueueByDev$$

CREATE
PROCEDURE printLoadQueueByDev (IN p_dev int, IN p_booksonly int)
BEGIN
  SELECT t.lab_device, t.lab, av.value paper_name, t.paper, t.width, SUM(t.height) print_queue_len
    FROM (SELECT ld.id lab_device, ld.lab, pg.id, pg.paper, pg.width, pg.height * pg.prints height
        FROM lab_device ld
          INNER JOIN lab l ON ld.lab = l.id
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device
          INNER JOIN lab_print_code lpc ON l.src_type = lpc.src_type AND lpc.width = lr.width AND lpc.paper = lr.paper
          INNER JOIN print_group pg ON lpc.width = pg.width AND lpc.height >= pg.height
            AND lpc.paper = pg.paper
            AND lpc.frame IN (-1, pg.frame)
            AND lpc.correction IN (-1, pg.correction)
            AND lpc.cutting IN (-1, pg.cutting)
            AND lpc.is_duplex IN (-1, pg.is_duplex)
            AND lpc.is_pdf = pg.is_pdf
        WHERE ld.id = p_dev
          AND pg.state = 200
          AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))
      UNION
      SELECT ld.id lab_device, ld.lab, pg.id, pg.paper, pg.width, pg.height * pg.prints height
        FROM lab_device ld
          INNER JOIN lab l ON ld.lab = l.id AND l.src_type = 3 AND l.hot_nfs != ''
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device AND lr.paper IN (10, 11, 12, 13)
          INNER JOIN lab_print_code lpc ON lpc.src_type = 8 AND lpc.width = lr.width
          INNER JOIN print_group pg ON lpc.width = pg.width
            AND lpc.height >= pg.height
            AND lr.paper = pg.paper
            AND lpc.is_pdf = pg.is_pdf
        WHERE ld.id = p_dev
          AND pg.state = 200
          AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))) t
      INNER JOIN attr_value av ON av.id = t.paper AND av.attr_tp = 2
    GROUP BY t.lab_device, t.lab, av.value, t.paper, t.width
    ORDER BY SUM(t.height) DESC;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS printLoadQueueByLab$$

CREATE
PROCEDURE printLoadQueueByLab (IN p_lab int, IN p_booksonly int)
BEGIN
  SELECT t.lab_name, t.lab, av.value paper_name, t.paper, t.width, SUM(t.height) / 1000 print_queue_len
    FROM (SELECT l.name lab_name, ld.lab, pg.id, pg.paper, pg.width, pg.height * pg.prints height
        FROM lab l
          INNER JOIN lab_device ld ON ld.lab = l.id
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device
          INNER JOIN lab_print_code lpc ON l.src_type = lpc.src_type AND lpc.width = lr.width AND lpc.paper = lr.paper
          INNER JOIN print_group pg ON lpc.width = pg.width AND lpc.height >= pg.height
            AND lpc.paper = pg.paper
            AND lpc.frame IN (-1, pg.frame)
            AND lpc.correction IN (-1, pg.correction)
            AND lpc.cutting IN (-1, pg.cutting)
            AND lpc.is_duplex IN (-1, pg.is_duplex)
            AND lpc.is_pdf = pg.is_pdf
        WHERE l.id = p_lab
          AND pg.state = 200
          AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))
      UNION
      SELECT l.name lab_name, ld.lab, pg.id, pg.paper, pg.width, pg.height * pg.prints height
        FROM lab l
          INNER JOIN lab_device ld ON ld.lab = l.id
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device AND lr.paper IN (10, 11, 12, 13)
          INNER JOIN lab_print_code lpc ON lpc.src_type = 8 AND lpc.width = lr.width
          INNER JOIN print_group pg ON lpc.width = pg.width
            AND lpc.height >= pg.height
            AND lr.paper = pg.paper
            AND lpc.is_pdf = pg.is_pdf
        WHERE l.id = p_lab
          AND l.src_type = 3
          AND l.hot_nfs != ''
          AND pg.state = 200
          AND (p_booksonly != 1 OR pg.book_type IN (1, 2, 3))) t
      INNER JOIN attr_value av ON av.id = t.paper AND av.attr_tp = 2
    GROUP BY t.lab_name, t.lab, av.value, t.paper, t.width
    ORDER BY SUM(t.height) DESC;

END
$$

DELIMITER ;