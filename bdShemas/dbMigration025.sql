-- main 2015-08-24  
-- moskva 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO lab_stop_type(id, name) VALUES(3, 'Постановка на печать');
INSERT INTO lab_stop_type(id, name) VALUES(4, 'Нет подходящего заказа');
INSERT INTO lab_stop_type(id, name) VALUES(5, 'Не подходит рулон');

CREATE TABLE lab_meter (
  lab int(5) NOT NULL,
  lab_device int(7) NOT NULL DEFAULT 0 COMMENT '0 - lab level; !=0 - device meter',
  meter_type tinyint(2) NOT NULL DEFAULT 0 COMMENT '0 - post; 1 - print; 10 - stop ',
  start_time datetime DEFAULT NULL,
  last_time datetime DEFAULT NULL,
  print_group varchar(50) DEFAULT NULL,
  state int(7) NOT NULL COMMENT 'state or lab stop type',
  amt int(7) DEFAULT 1,
  PRIMARY KEY (lab, lab_device, meter_type),
  CONSTRAINT FK_lab_meter_lab_id FOREIGN KEY (lab)
  REFERENCES lab (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE lab_meter_log (
  id int(11) NOT NULL AUTO_INCREMENT,
  lab int(5) DEFAULT NULL,
  lab_device int(7) DEFAULT NULL,
  start_time datetime DEFAULT NULL,
  end_time datetime DEFAULT NULL,
  state int(7) DEFAULT NULL,
  print_group varchar(50) DEFAULT NULL,
  amt int(7) DEFAULT 1,
  PRIMARY KEY (id),
  INDEX IDX_lab_meter_log (lab_device, start_time, end_time),
  CONSTRAINT FK_lab_meter_log_lab_id FOREIGN KEY (lab)
  REFERENCES lab (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 1
CHARACTER SET utf8
COLLATE utf8_general_ci;


DELIMITER $$

DROP PROCEDURE IF EXISTS techLogPg$$

CREATE
PROCEDURE techLogPg (IN pPgroup varchar(50), IN pSheet int, IN pTechPoint int, IN pDate datetime)
MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vState int;
  DECLARE vBooks int;
  DECLARE vSheets int;

  -- used for paper count
  DECLARE vWidth int;
  DECLARE vHeight int;
  DECLARE vPaper int;
  -- 

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  SELECT pg.order_id, pg.sub_id, pg.book_num, pg.sheet_num, pg.width, pg.height, pg.paper
  INTO vOrderId, vSubId, vBooks, vSheets, vWidth, vHeight, vPaper
    FROM print_group pg
    WHERE pg.id = pPgroup;

  SELECT tp.tech_type
  INTO vState
    FROM tech_point tp
    WHERE tp.id = pTechPoint;

  IF vOrderId IS NOT NULL
  THEN
    /* bug due to restart in java after dead lock
    IF vState = 300 THEN
      -- count paper after printing
      UPDATE lab_rolls lr SET lr.len=lr.len-vHeight 
        WHERE lr.lab_device IN (SELECT lb.id FROM lab_device lb WHERE lb.tech_point=pTechPoint) AND lr.width=vWidth AND lr.paper=vPaper AND lr.len>0;
    END IF;
   */
    -- log
    IF vState <= 300
    THEN
      -- may be reprint
      SET pPgroup = printPg2Reprint(pPgroup);
    END IF;
    INSERT INTO tech_log (order_id, sub_id, print_group, sheet, src_id, log_date)
      VALUES (vOrderId, vSubId, pPgroup, pSheet, pTechPoint, pDate);
    -- check TODO incorparate into code (do not need sub call)
    CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vSheets);
  END IF;

END
$$

DELIMITER ;

DROP TABLE lab_stop_log;

CREATE TABLE lab_stop_log (
  id int(11) NOT NULL AUTO_INCREMENT,
  lab int(5) DEFAULT NULL,
  lab_device int(7) DEFAULT NULL,
  lab_stop_type int(7) DEFAULT 0,
  time_from datetime DEFAULT NULL,
  time_to datetime DEFAULT NULL,
  log_comment varchar(255) DEFAULT NULL,
  time_created datetime DEFAULT NULL,
  time_updated datetime DEFAULT NULL,
  PRIMARY KEY (id),
  INDEX IDX_lab_stop_log_device (lab_device),
  INDEX IDX_lab_stop_log_time (time_from, time_to),
  CONSTRAINT FK_lab_stop_log_lab_id FOREIGN KEY (lab)
  REFERENCES lab (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_lab_stop_log_stop_type FOREIGN KEY (lab_stop_type)
  REFERENCES lab_stop_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 10
AVG_ROW_LENGTH = 1820
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

CREATE
PROCEDURE lab_meter_forward (IN plab int(5), IN pdevice int(7), IN pstate int(5), IN pprintgroup varchar(50))
BEGIN
  -- forward meters
  IF pstate = 255
  THEN
    -- print
    -- log if printgroup changed
    INSERT INTO lab_meter_log (lab, lab_device, start_time, end_time, state, print_group, amt)
      SELECT lm.lab, lm.lab_device, lm.start_time, IFNULL(lm.last_time, NOW()), 255, lm.print_group, lm.amt
        FROM lab_meter lm
        WHERE lm.lab = plab
          AND lm.lab_device = pdevice
          AND lm.meter_type = 1
          AND lm.print_group != pprintgroup;
    -- update meter
    INSERT INTO lab_meter (lab, lab_device, meter_type, start_time, print_group, state, amt)
      VALUES (plab, pdevice, 1, NOW(), pprintgroup, 255, 1)
    ON DUPLICATE KEY UPDATE start_time = IF(pprintgroup = print_group, start_time, NOW()),
    last_time = IF(pprintgroup = print_group, NOW(), NULL),
    amt = IF(pprintgroup = print_group, amt + 1, 1),
    print_group = pprintgroup,
    state = 255;

    -- log & reset all stops by device (or lab common vs devise==0)
    INSERT INTO lab_stop_log (lab, lab_device, lab_stop_type, time_from, time_to)
      SELECT lm.lab, lm.lab_device, lm.state, lm.start_time, NOW()
        FROM lab_meter lm
        WHERE lm.lab = plab
          AND lm.lab_device IN (0, pdevice)
          AND lm.meter_type = 10;
    DELETE
      FROM lab_meter
    WHERE lab = plab
      AND lab_device IN (0, pdevice)
      AND meter_type = 10;
  ELSE
    -- post
    -- log if print_group state forwarded exclude capture state (203)
    INSERT INTO lab_meter_log (lab, lab_device, start_time, end_time, state, print_group)
      SELECT lm.lab, lm.lab_device, lm.start_time, NOW(), lm.state, lm.print_group
        FROM lab_meter lm
        WHERE lm.lab = plab
          AND lm.lab_device = pdevice
          AND lm.meter_type = 0
          AND lm.print_group = pprintgroup
          AND lm.state > 203
          AND lm.state < pstate;
    -- update meter
    INSERT INTO lab_meter (lab, lab_device, meter_type, start_time, print_group, state)
      VALUES (plab, pdevice, 0, NOW(), pprintgroup, pstate)
    ON DUPLICATE KEY UPDATE start_time = NOW(), print_group = pprintgroup, state = pstate;

  /* only print resets stop
  -- log & reset all stops vs NO_ORDER(1) & POST_WAITE(3) state
  INSERT INTO lab_stop_log(lab, lab_device, lab_stop_type, time_from, time_to)
    SELECT lm.lab, lm.lab_device, lm.state, lm.start_time, NOW() 
      FROM lab_meter lm
      WHERE lm.lab=plab AND lm.meter_type=10 AND lm.state IN (1,3);
  DELETE FROM lab_meter 
      WHERE lab=plab AND meter_type=10 AND state IN (1,3);
  */

  END IF;
END
$$

DELIMITER ;

DELIMITER $$

CREATE 
PROCEDURE lab_meter_forward_lab (IN plab int(5), IN pstate int(5), IN pprintgroup varchar(50))
BEGIN
  CALL lab_meter_forward(plab, 0, pstate, pprintgroup);
END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE lab_meter_forward_tp (IN ptechpoint int(7), IN pprintgroup varchar(50))
BEGIN
  DECLARE vLab int;
  DECLARE vDevice int;
  DECLARE EXIT HANDLER FOR NOT FOUND BEGIN
  END;

  -- only print tech point allowed
  SELECT ld.id, ld.lab
  INTO vDevice, vLab
    FROM tech_point tp
      INNER JOIN lab_device ld ON tp.id = ld.tech_point
    WHERE tp.id = ptechpoint
      AND tp.tech_type = 300
  LIMIT 1;

  IF vDevice IS NOT NULL
    AND vDevice != 0
    AND vLab IS NOT NULL
    AND vLab != 0
  THEN
    CALL lab_meter_forward(vLab, vDevice, 255, pprintgroup);
  END IF;
END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE lab_meter_fix_stop (IN plab int(5), IN pdevice int(7), IN pstoptype int(7), IN ptime datetime)
BEGIN
  DECLARE vadd int DEFAULT 1;
  DECLARE CONTINUE HANDLER FOR NOT FOUND
  SET vadd = 1;

  IF ptime > NOW()
  THEN
    SET ptime = NOW();
  END IF;

  -- log & reset current if stop type differ
  INSERT INTO lab_stop_log (lab, lab_device, lab_stop_type, time_from, time_to)
    SELECT lm.lab, lm.lab_device, lm.state, lm.start_time, ptime
      FROM lab_meter lm
      WHERE lm.lab = plab
        AND lm.lab_device = pdevice
        AND lm.meter_type = 10
        AND lm.state != pstoptype
        AND lm.start_time < ptime;
  DELETE
    FROM lab_meter
  WHERE lab = plab
    AND lab_device = pdevice
    AND meter_type = 10
    AND state != pstoptype;

  -- check if has print meters after stop time
  SELECT 0
  INTO vadd
    FROM lab_meter lm
    WHERE lm.lab = plab
      AND lm.lab_device = pdevice
      AND lm.meter_type = 1
      AND IFNULL(lm.last_time, lm.start_time) > ptime
  LIMIT 1;

  -- fix stop
  IF vadd = 1
  THEN
    INSERT IGNORE INTO lab_meter (lab, lab_device, meter_type, start_time, state)
      VALUES (plab, pdevice, 10, ptime, pstoptype);
  END IF;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS printStateStart$$

CREATE
PROCEDURE printStateStart (IN pPgroupId varchar(50), IN lab int)
MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  -- update print_group in transaction (avoid state hang)
  START TRANSACTION;
    UPDATE print_group pg
    SET pg.state = 250,
        pg.state_date = NOW(),
        pg.destination = lab
    WHERE pg.id = pPgroupId
    AND pg.state != 250;
  COMMIT;

  -- forvard meter
  CALL lab_meter_forward_lab(lab, 250, pPgroupId);

  SELECT pg.order_id, pg.sub_id
  INTO vOrderId, vSubId
    FROM print_group pg
    WHERE pg.id = pPgroupId;

  IF vOrderId IS NOT NULL
  THEN
    -- rise order/suborder state
    IF vSubId != ''
    THEN
      UPDATE suborders o
      SET o.state = 250,
          o.state_date = NOW()
      WHERE o.order_id = vOrderId
      AND o.sub_id = vSubId
      AND o.state < 250;
    END IF;
    UPDATE orders o
    SET o.state = 250,
        o.state_date = NOW()
    WHERE o.id = vOrderId
    AND o.state < 250;

    -- chek if all pg started
    SELECT IFNULL(MIN(pg.state), 0)
    INTO vMinState
      FROM print_group pg
      WHERE pg.order_id = vOrderId
        AND pg.sub_id = vSubId;
    IF vMinState >= 250
    THEN
      -- all pg started end order/suborder
      INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
        VALUES (vOrderId, vSubId, 210, NOW(), NOW())
      ON DUPLICATE KEY UPDATE state_date = NOW();
      IF vSubId != ''
      THEN
        INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
          VALUES (vOrderId, '', 210, NOW(), NOW())
        ON DUPLICATE KEY UPDATE state_date = NOW();
      END IF;
    ELSE
      -- start order/suborder
      CALL extraStateStart(vOrderId, vSubId, 210, NOW());
    END IF;

  END IF;
END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE lab_meter_end_stop (IN plab int(5), IN pdevice int(7), IN ptime datetime)
BEGIN
  IF ptime IS NULL
    OR ptime > NOW()
  THEN
    SET ptime = NOW();
  END IF;

  -- log & reset current
  INSERT INTO lab_stop_log (lab, lab_device, lab_stop_type, time_from, time_to)
    SELECT lm.lab, lm.lab_device, lm.state, lm.start_time, ptime
      FROM lab_meter lm
      WHERE lm.lab = plab
        AND lm.lab_device = pdevice
        AND lm.meter_type = 10;
  DELETE
    FROM lab_meter
  WHERE lab = plab
    AND lab_device = pdevice
    AND meter_type = 10;

END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE packageSetOrderSpace (IN pOrderId varchar(50), IN pSpace int)
BEGIN
  DECLARE vSource int;
  DECLARE vPackage int;
  DECLARE vOrderWeight float DEFAULT (0);
  DECLARE vResult int DEFAULT (0);

  DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN
    SET vPackage = NULL;
    SET vResult = 0;
  END;

  -- get order data
  SELECT o.source, o.group_id, IFNULL(oei.weight, 0)
  INTO vSource, vPackage, vOrderWeight
    FROM orders o
      LEFT OUTER JOIN order_extra_info oei ON o.id = oei.id AND oei.sub_id = ''
    WHERE o.id = pOrderId;

  IF vPackage IS NOT NULL
  THEN
    -- check if space has different package
    SELECT IFNULL(MIN(-1), 1)
    INTO vResult
      FROM rack_orders ro
        INNER JOIN orders o ON ro.order_id = o.id
      WHERE ro.space = pSpace
        AND ro.order_id != pOrderId
        AND o.source != vSource
        AND o.group_id != vPackage;
    IF vResult > 0
    THEN
      -- check wieght
      SELECT IF(rs.weight < ROUND(((IFNULL(SUM(oei.weight), 0) + vOrderWeight) / 1000), 1), -2, 1)
      INTO vResult
        FROM rack_space rs
          LEFT OUTER JOIN rack_orders ro ON rs.id = ro.space
          LEFT OUTER JOIN order_extra_info oei ON ro.order_id = oei.id AND oei.sub_id = ''
        WHERE rs.id = pSpace
          AND ro.order_id != pOrderId;
    END IF;
    IF vResult > 0
    THEN
      -- set order space
      INSERT IGNORE INTO rack_orders (order_id, space)
        VALUES (pOrderId, pSpace);
    END IF;
  END IF;

  -- return result ?
  SELECT vResult AS value;
END
$$

DELIMITER ;

DELIMITER $$

DROP FUNCTION IF EXISTS printPg2Reprint$$

CREATE
FUNCTION printPg2Reprint (pPgroupId varchar(50))
RETURNS varchar(50) charset utf8
READS SQL DATA
BEGIN
  DECLARE vPgId varchar(50);
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vPgId = pPgroupId;

  -- get first reprint vs minimal prints (print state must be closed)
  -- state between posted and printed 
  SELECT pg1.id
  INTO vPgId
    FROM print_group pg
      INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id
        AND pg.id = pg1.reprint_id
        AND pg1.state >= 250 AND pg1.state < 300
    WHERE pg.id = pPgroupId
      AND pg.is_reprint = 0
    ORDER BY pg1.prints
  LIMIT 1;


  RETURN vPgId;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS techUnitCalc$$

CREATE
PROCEDURE techUnitCalc (IN pOrder varchar(50), IN pSubOrder varchar(50), IN pPgroup varchar(50), IN pState int, IN pBooks int, IN pSheets int)
MODIFIES SQL DATA
BEGIN
  DECLARE vDone int;
  DECLARE vStart datetime;
  DECLARE vEnd datetime;
  DECLARE vCount int;
  DECLARE vMinState int;
  DECLARE vParentPg varchar(50);


  IF pPgroup IS NULL
  THEN
    SET pPgroup = '';
  END IF;
  IF pSubOrder IS NULL
  THEN
    SET pSubOrder = '';
  END IF;
  -- calc prints
  SELECT IFNULL(COUNT(DISTINCT tl.sheet), 0), IFNULL(MIN(tl.log_date), NOW()), IFNULL(MAX(tl.log_date), NOW())
  INTO vDone, vStart, vEnd
    FROM tech_log tl
      INNER JOIN tech_point tp ON tl.src_id = tp.id
    WHERE tl.order_id = pOrder
      AND tl.sub_id = pSubOrder
      AND tl.print_group = pPgroup
      AND tp.tech_type = pState;

  IF vDone = pBooks * pSheets
    OR (pState = 320
    AND vDone = pBooks * 2) -- 320 - TECH_FOLDING (log first & end sheet per book)
    OR (pState = 360
    AND vDone > 0) -- 360 - TECH_CUTTING (log first scan)
    OR (pState = 380
    AND vDone = pBooks) -- 380 - TECH_JOIN   (log books)
  THEN
    -- complited
    IF pPgroup != ''
    THEN
      IF pState != 300
      THEN
        -- set pg state 
        UPDATE print_group pg
        SET pg.state = pState,
            pg.state_date = vEnd
        WHERE pg.id = pPgroup;
      ELSE
        -- print
        -- get parent printgroup
        SELECT IFNULL(pg.reprint_id, pg.id)
        INTO vParentPg
          FROM print_group pg
          WHERE pg.id = pPgroup;
        -- set pg state, if reprint set parent state and it's all open reprints
        UPDATE print_group pg
        SET pg.state = pState,
            pg.state_date = vEnd
        WHERE pg.order_id = pOrder
        AND pg.state >= 250
        AND pg.state < 300
        AND vParentPg IN (pg.id, pg.reprint_id);
      END IF;
    END IF;
    IF pState = 300
    THEN
      -- print state, check pringroups
      SELECT IFNULL(MIN(pg.state), 0)
      INTO vMinState
        FROM print_group pg
        WHERE pg.order_id = pOrder
          AND pg.sub_id = pSubOrder;
      IF vMinState >= 300
      THEN
        -- all pg printed end order/suborder
        CALL extraStateSet(pOrder, pSubOrder, pState, vEnd);
      ELSE
        -- start
        CALL extraStateStart(pOrder, pSubOrder, pState, vStart);
      END IF;
    ELSE
      -- end order/suborder
      CALL extraStateSet(pOrder, pSubOrder, pState, vEnd);
    END IF;
  ELSE
    -- start
    CALL extraStateStart(pOrder, pSubOrder, pState, vStart);
  END IF;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS printMarkInPrint$$

CREATE
PROCEDURE printMarkInPrint (IN pPgroupId varchar(50))
MODIFIES SQL DATA
BEGIN
  DECLARE vPgId varchar(50);

  SET vPgId = printPg2Reprint(pPgroupId);
  UPDATE print_group pg
  SET pg.state = 255,
      pg.state_date = NOW()
  WHERE pg.id = vPgId
  AND pg.state < 255;
END
$$

DELIMITER ;
