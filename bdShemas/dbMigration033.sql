-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

-- new tech flow

CREATE
VIEW generator_04
AS
SELECT 0 AS `n`
UNION ALL
SELECT 1 AS `1`
UNION ALL
SELECT 2 AS `2`
UNION ALL
SELECT 3 AS `3`;

CREATE
VIEW generator_16
AS
SELECT 0 AS `n`
UNION ALL
SELECT 1 AS `1`
UNION ALL
SELECT 2 AS `2`
UNION ALL
SELECT 3 AS `3`
UNION ALL
SELECT 4 AS `4`
UNION ALL
SELECT 5 AS `5`
UNION ALL
SELECT 6 AS `6`
UNION ALL
SELECT 7 AS `7`
UNION ALL
SELECT 8 AS `8`
UNION ALL
SELECT 9 AS `9`
UNION ALL
SELECT 10 AS `10`
UNION ALL
SELECT 11 AS `11`
UNION ALL
SELECT 12 AS `12`
UNION ALL
SELECT 13 AS `13`
UNION ALL
SELECT 14 AS `14`
UNION ALL
SELECT 15 AS `15`;

CREATE
VIEW generator_256
AS
SELECT ((`hi`.`n` << 4) | `lo`.`n`) AS `n`
  FROM (`generator_16` `lo`
    JOIN `generator_16` `hi`);
    
CREATE
VIEW generator_1k
AS
SELECT ((`hi`.`n` << 8) | `lo`.`n`) AS `n`
  FROM (`generator_256` `lo`
    JOIN `generator_04` `hi`)
  ORDER BY 1;
  
CREATE TABLE order_books (
  pg_id varchar(50) NOT NULL,
  target_pg varchar(50) NOT NULL,
  book int(5) NOT NULL,
  sheets int(5) DEFAULT NULL,
  state int(5) DEFAULT NULL,
  state_date datetime DEFAULT NULL,
  is_rejected tinyint(1) DEFAULT 0,
  is_reject tinyint(1) DEFAULT 0,
  PRIMARY KEY (pg_id, book),
  INDEX IDX_order_books_target (target_pg),
  CONSTRAINT FK_order_books_pg FOREIGN KEY (pg_id)
  REFERENCES print_group (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;




ALTER TABLE order_extra_state
  ADD COLUMN transit_date DATETIME DEFAULT NULL AFTER state_date;
ALTER TABLE order_extra_state
  ADD COLUMN is_reject TINYINT(1) DEFAULT 0 AFTER transit_date;
  
DELIMITER $$

CREATE
PROCEDURE fill_books_reject(IN p_pgid varchar(50))
BEGIN
  DECLARE vPgId varchar(50);

  -- add reprint books
  INSERT IGNORE INTO order_books (pg_id, target_pg, book, sheets, state, state_date, is_reject)
    SELECT pg.id, pg.reprint_id, pgr.book, IF(MAX(pgr.thech_unit) = 0, COUNT(*), pg.sheet_num) sheets, 145, NOW(), 1
      FROM print_group pg
        INNER JOIN print_group_rejects pgr ON pg.id = pgr.print_group
      WHERE pg.id = p_pgid
      GROUP BY pgr.book;

  -- mark target books as rejected
  UPDATE order_books ob
  INNER JOIN order_books obs
    ON ob.pg_id = obs.target_pg
    AND ob.book = obs.book
    SET ob.is_rejected = 1
    WHERE obs.pg_id = p_pgid;

  -- mark whole books as rejected (thech_unit=3)
  SELECT MAX(pg1.id)
  INTO vPgId
    FROM print_group pg
      INNER JOIN print_group pg2 ON pg2.id = pg.reprint_id
      INNER JOIN print_group pg1 ON pg2.order_id = pg1.order_id AND pg2.sub_id = pg1.sub_id AND pg2.id != pg1.id AND pg1.is_reprint = 0
    WHERE pg.id = p_pgid;
  IF vPgId IS NOT NULL
  THEN
    UPDATE order_books ob
    INNER JOIN (SELECT DISTINCT pgr.book
        FROM print_group_rejects pgr
        WHERE pgr.print_group = p_pgid
          AND pgr.thech_unit > 2) t
      ON ob.pg_id = vPgId
      AND ob.book = t.book
      SET ob.is_rejected = 1;
  END IF;
END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE tech_calc_books(IN p_pg varchar(50))
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
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techCalcPg$$
CREATE
PROCEDURE techCalcPg(IN pPgroup varchar(50), IN pTechPoint int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vrepgroup varchar(50);
  DECLARE vState int;
  DECLARE vBooks int;
  DECLARE vPrints int;


  BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

    SELECT pg.order_id, pg.sub_id, pg.book_num, pg.prints
    INTO vOrderId, vSubId, vBooks, vPrints
      FROM print_group pg
      WHERE pg.id = pPgroup;

    SELECT tp.tech_type
    INTO vState
      FROM tech_point tp
      WHERE tp.id = pTechPoint;
  END;

  IF vOrderId IS NOT NULL
  THEN
    /*
    IF vState <= 300
    THEN
      -- may be reprint
      BEGIN
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET vrepgroup = NULL;
        SET vrepgroup = printPg2Reprint(pPgroup);
      END;

      IF vrepgroup IS NOT NULL AND pPgroup!=vrepgroup THEN
        SET pPgroup=vrepgroup;
        SELECT pg.prints
          INTO vPrints
          FROM print_group pg
          WHERE pg.id = pPgroup;
      END IF; 
    END IF;
  */

    -- check TODO incorparate into code (do not need sub call)
    CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vPrints);
    CALL tech_calc_books(pPgroup);

  END IF;

END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE fill_order(IN p_orderid VARCHAR(50))
  MODIFIES SQL DATA
  COMMENT 'post process order after fill'
BEGIN
  -- fill  books
  INSERT IGNORE INTO order_books (pg_id, target_pg, book, sheets, state, state_date)
    SELECT pg.id, pg.id, gk.n, pg.sheet_num, 200, NOW()
      FROM print_group pg
        INNER JOIN generator_1k gk ON gk.n BETWEEN 1 AND pg.book_num
      WHERE pg.order_id = p_orderid
        AND pg.is_reprint = 0;
END
$$

DELIMITER ;

-- main 2017-09-01
-- cycle rep 2017-09-07

DELIMITER $$

DROP PROCEDURE IF EXISTS setEntireBookState$$
CREATE 
PROCEDURE setEntireBookState(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pBook int, IN pState int)
BEGIN
  UPDATE order_books obb
  INNER JOIN print_group pg ON pg.order_id = pOrder AND pg.sub_id = pSubOrder AND obb.pg_id = pg.id
    SET obb.state = pState, obb.is_rejected=0, obb.state_date=NOW()
  WHERE pBook = -1 OR obb.book = pBook AND obb.state < pState;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techLogPg$$
CREATE
PROCEDURE techLogPg(IN pPgroup varchar(50), IN pSheet int, IN pTechPoint int, IN pDate datetime, IN pCalc int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vrepgroup varchar(50);
  DECLARE vTrgetPg varchar(50);
  DECLARE vState int;
  DECLARE vBookPart int;
  DECLARE vBooks int;
  DECLARE vPrints int;

  -- used for paper count
  DECLARE vWidth int;
  DECLARE vHeight int;
  DECLARE vPaper int;
  -- 

  BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

    SELECT pg.order_id, pg.sub_id, pg.book_num, pg.prints, pg.width, pg.height, pg.paper, IFNULL(pg.reprint_id, pg.id)
    INTO vOrderId, vSubId, vBooks, vPrints, vWidth, vHeight, vPaper, vTrgetPg
      FROM print_group pg
      WHERE pg.id = pPgroup;

    SELECT tp.tech_type, os.book_part
    INTO vState, vBookPart
      FROM tech_point tp
        INNER JOIN order_state os ON os.id = tp.tech_type
      WHERE tp.id = pTechPoint;
  END;

  IF vOrderId IS NOT NULL
  THEN
    /* bug due to restart in java after dead lock
    IF vState = 300 THEN
      -- count paper after printing
      UPDATE lab_rolls lr SET lr.len=lr.len-vHeight 
        WHERE lr.lab_device IN (SELECT lb.id FROM lab_device lb WHERE lb.tech_point=pTechPoint) AND lr.width=vWidth AND lr.paper=vPaper AND lr.len>0;
    END IF;
   */

    /*
    IF vState <= 300
    THEN
      -- may be reprint
      BEGIN
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET vrepgroup = NULL;
        SET vrepgroup = printPg2Reprint(pPgroup);
      END;

      IF vrepgroup IS NOT NULL AND pPgroup!=vrepgroup THEN
        SET pPgroup=vrepgroup;
        SELECT pg.prints
          INTO vPrints
          FROM print_group pg
          WHERE pg.id = pPgroup;
      END IF; 

    END IF;
  */

    -- log
    INSERT INTO tech_log (order_id, sub_id, print_group, sheet, src_id, log_date)
      VALUES (vOrderId, vSubId, pPgroup, pSheet, pTechPoint, pDate);

    -- recalc
    IF pCalc > 0
    THEN
      -- check TODO incorparate into code (do not need sub call)
      CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vPrints);
      -- recalc book
      IF vState > 300 AND vBookPart = 0
      THEN
        -- book join or OTK
        CALL setEntireBookState(vOrderId, vSubId, (pSheet DIV 100), vState);
      ELSE
        CALL tech_calc_books(pPgroup);
      END IF;
    END IF;
  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS extraStateSetOTK$$
CREATE
PROCEDURE extraStateSetOTK(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pDate datetime)
  MODIFIES SQL DATA
BEGIN
  DECLARE vMinSubState int;
  DECLARE vMinPhotoState int;

  IF pDate IS NULL
  THEN
    SET pDate = NOW();
  END IF;

  IF pSubOrder != ''
  THEN
    -- fix suborder
    -- forvard suborder pgs state
    UPDATE print_group pg
    SET pg.state = 450,
        pg.state_date = pDate
    WHERE pg.order_id = pOrder
    AND pg.sub_id = pSubOrder
    AND pg.state < 450
    ORDER BY pg.id;

    -- set suborder state
    UPDATE suborders s
    SET s.state = 450,
        s.state_date = pDate
    WHERE s.order_id = pOrder
    AND s.sub_id = pSubOrder
    AND s.state < 450;

    -- fix suborder extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      VALUES (pOrder, pSubOrder, 450, pDate, pDate)
    ON DUPLICATE KEY UPDATE state_date = pDate;

  END IF;

  -- calc min state by photo pgs 
  SELECT MIN(pg.state)
  INTO vMinPhotoState
    FROM print_group pg
    WHERE pg.order_id = pOrder
      AND pg.is_reprint = 0
      AND pg.book_type = 0;

  -- calc min state by suborders
  SELECT MIN(so.state)
  INTO vMinSubState
    FROM suborders so
    WHERE so.order_id = pOrder;

  -- attempt to forvard order state
  IF (vMinPhotoState IS NULL
    OR vMinPhotoState >= 450)
    AND (vMinSubState IS NULL
    OR vMinSubState >= 450)
  THEN
    -- no photo or photo pgs pass OTK and no suborders or all suborders pass otk

    -- forvard pgs state
    UPDATE print_group pg
    SET pg.state = 450,
        pg.state_date = pDate
    WHERE pg.order_id = pOrder
    AND pg.sub_id = ''
    AND pg.state < 450
    ORDER BY pg.id;

    -- set order state
    UPDATE orders o
    SET o.state = 450,
        o.state_date = pDate
    WHERE o.id = pOrder
    AND o.state < 450;

    -- fix order extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      VALUES (pOrder, '', 450, pDate, pDate)
    ON DUPLICATE KEY UPDATE state_date = pDate;

    -- stop all started order extra states
    UPDATE order_extra_state es
    SET es.state_date = pDate
    WHERE es.id = pOrder
    -- AND es.sub_id = ''
    AND es.state < 450
    AND es.state_date IS NULL
    ORDER BY es.sub_id, es.state;

  END IF;

  CALL setEntireBookState(pOrder, pSubOrder, -1, 450);
END
$$

DELIMITER ;

-- main 2017-09-14
