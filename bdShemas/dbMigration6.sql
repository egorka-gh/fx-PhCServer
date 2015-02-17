-- main cycle 

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

DELIMITER $$

DROP PROCEDURE IF EXISTS printLoad4PrintByDev$$

CREATE DEFINER = 'root'@'localhost'
PROCEDURE printLoad4PrintByDev(IN p_devlst TEXT, IN p_photo INT)
BEGIN
  DECLARE vIdx integer(11) DEFAULT (0);
  DECLARE vDev integer(7) DEFAULT (0);

  -- create temps
  CREATE TEMPORARY TABLE IF NOT EXISTS tmp_pgs LIKE tmpt_vch_ids;
  CREATE TEMPORARY TABLE IF NOT EXISTS tmp_res LIKE tmpt_vch_ids;
  
  set vIdx=LOCATE(',',p_devlst);
  WHILE (vIdx>0)
  DO
    -- p_devlst iteration
    set vDev=LEFT(p_devlst,vIdx-1);
    set p_devlst= SUBSTR(p_devlst,vIdx+1);
    set vIdx=LOCATE(',',p_devlst);

    -- load pgs by device
    -- TODO add noritsu nhf as primary lab?
    INSERT IGNORE INTO tmp_pgs(id)
      SELECT pg.id
        FROM lab_device ld
          INNER JOIN lab l ON ld.lab = l.id
          INNER JOIN lab_rolls lr ON ld.id = lr.lab_device AND lr.is_online = 1
          INNER JOIN lab_print_code lpc ON l.src_type = lpc.src_type AND lpc.width = lr.width AND lpc.paper = lr.paper
          INNER JOIN print_group pg ON lpc.width = pg.width AND lpc.height = pg.height
            AND lpc.paper =pg.paper
            AND lpc.frame IN (-1, pg.frame) 
            AND lpc.correction IN (-1, pg.correction) 
            AND lpc.cutting IN (-1, pg.cutting)
            AND lpc.is_duplex IN (-1, pg.is_duplex)
            AND lpc.is_pdf = pg.is_pdf
        WHERE ld.id = vDev
          AND pg.state = 200 AND (p_photo=1 OR pg.book_type>0) AND NOT EXISTS(SELECT 1 FROM tmp_res WHERE tmp_res.id=pg.id)
        ORDER BY pg.state_date
      LIMIT 4;
    -- add noritsu nhf (secondary lab)
    INSERT IGNORE INTO tmp_pgs(id)
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
          AND pg.state = 200 AND (p_photo=1 OR pg.book_type>0) AND NOT EXISTS(SELECT 1 FROM tmp_res WHERE tmp_res.id=pg.id)
        ORDER BY pg.state_date
      LIMIT 4;
      
      -- mysq: You cannot refer to a TEMPORARY table more than once in the same query.
      -- use additional temp - tmp_pgs
      INSERT IGNORE INTO tmp_res(id)
        SELECT id FROM tmp_pgs;

      DELETE FROM tmp_pgs;

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
