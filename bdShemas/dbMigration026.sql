-- main   
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

DELIMITER $$

CREATE
PROCEDURE printLoadQueueByDev (IN p_dev int)
BEGIN
  SELECT t.lab_device, t.lab, av.value paper_name, t.paper, t.width, SUM(t.height) print_queue_len
    FROM (SELECT ld.id lab_device, ld.lab, pg.id, pg.paper, pg.width, pg.height
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
      UNION
      SELECT ld.id lab_device, ld.lab, pg.id, pg.paper, pg.width, pg.height
        FROM lab_device ld
          INNER JOIN lab l ON ld.lab = l.id AND l.src_type = 3 AND l.hot_nfs != ''
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device AND lr.paper IN (10, 11, 12, 13)
          INNER JOIN lab_print_code lpc ON lpc.src_type = 8 AND lpc.width = lr.width
          INNER JOIN print_group pg ON lpc.width = pg.width
            AND lpc.height >= pg.height
            AND lr.paper = pg.paper
            AND lpc.is_pdf = pg.is_pdf
        WHERE ld.id = p_dev
          AND pg.state = 200) t
      INNER JOIN attr_value av ON av.id = t.paper AND av.attr_tp = 2
    GROUP BY t.lab_device, t.lab, av.value, t.paper, t.width
    ORDER BY SUM(t.height) DESC;
END
$$

DELIMITER ;