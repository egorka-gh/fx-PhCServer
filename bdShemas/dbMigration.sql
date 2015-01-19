CREATE TABLE `lab_stop_type` (
  `id` int(7) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `lab_stop_type` (`id`, `name`) VALUES (0, 'Другое');

CREATE TABLE `lab_stop_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lab_device` int(7) NOT NULL,
  `lab_stop_type` int(7) NOT NULL,
  `time_from` datetime DEFAULT NULL,
  `time_to` datetime DEFAULT NULL,
  `log_comment` text,
  `time_created` datetime NOT NULL,
  `time_updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lab_device` (`lab_device`),
  KEY `FK_lab_stop_log_stop_type` (`lab_stop_type`),
  CONSTRAINT `FK_lab_stop_log_lab_device_id` FOREIGN KEY (`lab_device`) REFERENCES `lab_device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_lab_stop_log_stop_type` FOREIGN KEY (`lab_stop_type`) REFERENCES `lab_stop_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP PROCEDURE `techLogPg`;

DELIMITER ;;
CREATE PROCEDURE `techLogPg`(IN pPgroup varchar(50), IN pSheet int, IN pTechPoint int, IN pDate datetime)
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

  SELECT pg.order_id, pg.sub_id, pg.book_num, pg.sheet_num, pg.width, pg.height, pg.paper INTO vOrderId, vSubId, vBooks, vSheets, vWidth, vHeight, vPaper
  FROM print_group pg
  WHERE pg.id = pPgroup;

  SELECT tp.tech_type INTO vState
  FROM tech_point tp
  WHERE tp.id = pTechPoint;

  IF vOrderId IS NOT NULL THEN
    IF vState <= 300 THEN
      -- may be reprint
      SET pPgroup = printPg2Reprint(pPgroup);
    END IF;
    IF vState = 300 THEN
      -- count paper after printing
      UPDATE lab_rolls lr SET lr.len=lr.len-vHeight 
        WHERE lr.lab_device IN (SELECT lb.id FROM lab_device lb WHERE lb.tech_point=pTechPoint) AND lr.width=vWidth AND lr.paper=vPaper;
    END IF;
    -- log
    INSERT INTO tech_log
    (order_id, sub_id, print_group, sheet, src_id, log_date)
      VALUES (vOrderId, vSubId, pPgroup, pSheet, pTechPoint, pDate);
    -- check
    CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vSheets);
  END IF;

END;;
DELIMITER ;