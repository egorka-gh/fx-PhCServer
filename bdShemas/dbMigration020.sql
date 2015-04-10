-- main 2015-04-10
-- moskva 2015-04-10
-- valichek

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';


DROP TABLE rack_space;

CREATE TABLE rack_space (
  id int(5) NOT NULL AUTO_INCREMENT,
  rack int(5) NOT NULL DEFAULT 0,
  name varchar(50) DEFAULT NULL,
  width int(5) DEFAULT 0,
  height int(5) DEFAULT 0,
  weight float(4, 1) DEFAULT 0.0,
  package_source int(7) DEFAULT 0,
  package_id int(11) DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT FK_rack_space_rack_id FOREIGN KEY (rack)
  REFERENCES rack (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE rack_orders_log (
  rowId int(11) NOT NULL AUTO_INCREMENT,
  event_time datetime DEFAULT NULL,
  order_id varchar(50) DEFAULT '',
  rack_name varchar(50) DEFAULT NULL,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (rowId)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 100
CHARACTER SET utf8
COLLATE utf8_general_ci;

DROP TABLE rack_orders;

CREATE TABLE rack_orders (
  order_id varchar(50) NOT NULL DEFAULT '',
  space int(5) NOT NULL DEFAULT 0,
  PRIMARY KEY (order_id),
  INDEX IDX_rack_orders_space (space),
  CONSTRAINT FK_rack_orders_rack_space_id FOREIGN KEY (space)
  REFERENCES rack_space (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

DROP TRIGGER IF EXISTS tg_rack_orders_log$$

CREATE
TRIGGER tg_rack_orders_log
AFTER INSERT
ON rack_orders
FOR EACH ROW
BEGIN
  INSERT INTO rack_orders_log (event_time, order_id, rack_name, name)
    SELECT NOW(), new.order_id, r.name, rs.name
      FROM rack_space rs
        INNER JOIN rack r ON r.id = rs.rack
      WHERE rs.id = new.space;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS sync$$

CREATE
PROCEDURE sync ()
MODIFIES SQL DATA
COMMENT 'синхронизация всех активных сайтов'
BEGIN
  DECLARE vId integer(7) DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vCur CURSOR FOR
  SELECT s.id
    FROM sources s
    WHERE s.online = 1;

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
    IF vIsEnd THEN
      LEAVE wet;
    END IF;
    CALL syncSource(vId);
  END LOOP wet;
  CLOSE vCur;

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS packageGetSpaces$$

CREATE
PROCEDURE packageGetSpaces (IN pOrderId varchar(50), IN pTechPoint int)
READS SQL DATA
BEGIN
  DECLARE vSource int;
  DECLARE vPackage int;

  DECLARE vOrderWeight float DEFAULT (0);
  DECLARE vOrderSpace int;

  DECLARE vFullWeight float DEFAULT (0);
  DECLARE vDoneWeight float DEFAULT (0);

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vPackage = NULL;

  SELECT o.source, o.group_id, IFNULL(oei.weight, 0), IFNULL(ro.space, -1)
  INTO vSource, vPackage, vOrderWeight, vOrderSpace
    FROM orders o
      LEFT OUTER JOIN order_extra_info oei ON o.id = oei.id AND oei.sub_id = ''
      LEFT OUTER JOIN rack_orders ro ON ro.order_id = o.id
    WHERE o.id = pOrderId;

  IF vPackage IS NOT NULL THEN
    IF vOrderSpace > 0 THEN
      -- order has space
      SELECT r.name rack_name, rs.*, 0 unused_weight, -1 rating
        FROM rack_space rs
          INNER JOIN rack r ON r.id = rs.rack
        WHERE rs.id = vOrderSpace;
    ELSE
      -- build space list

      -- calc package weights
      SELECT IFNULL(SUM(oei.weight), 0), IFNULL(SUM(IF(ro.space IS NOT NULL, oei.weight, 0)), 0)
      INTO vFullWeight, vDoneWeight
        FROM orders o
          LEFT OUTER JOIN order_extra_info oei ON o.id = oei.id AND oei.sub_id = ''
          LEFT OUTER JOIN rack_orders ro ON ro.order_id = o.id
        WHERE o.source = vSource
          AND o.group_id = vPackage;

      SELECT t.*
        FROM (
          -- package spaces
          SELECT r.name rack_name, rs.*, (rs.weight - IFNULL(SUM(oei.weight), 0) / 1000) unused_weight, -1 rating
            FROM orders o
              INNER JOIN rack_orders ro ON ro.order_id = o.id
              INNER JOIN rack_space rs ON rs.id = ro.space
              INNER JOIN rack r ON r.id = rs.rack
              LEFT OUTER JOIN order_extra_info oei ON o.id = oei.id AND oei.sub_id = ''
            WHERE o.source = vSource
              AND o.group_id = vPackage
            GROUP BY r.id, rs.id
            HAVING (rs.weight - IFNULL(SUM(oei.weight), 0) / 1000) >= (vOrderWeight / 1000)
          UNION ALL
          -- free spaces
          SELECT r.name rack_name, rs.*, rs.weight unused_weight, (rs.weight - (vFullWeight - vDoneWeight) / 1000) rating
            FROM rack_space rs
              INNER JOIN rack r ON r.id = rs.rack
            WHERE NOT EXISTS (SELECT 1
                  FROM rack_orders ro
                  WHERE ro.space = rs.id)
              AND (pTechPoint = 0 OR EXISTS (SELECT 1
                  FROM rack_tech_point rtp
                  WHERE r.id = rtp.rack
                    AND rtp.tech_point = pTechPoint))
              AND rs.weight >= ((vFullWeight - vDoneWeight) / 1000)) t
        ORDER BY t.rating;

    END IF;
  ELSE
    -- empty responce
    SELECT r.name rack_name, rs.*, 0 unused_weight, -1 rating
      FROM rack_space rs
        INNER JOIN rack r ON r.id = rs.rack
      WHERE rs.id = NULL;
  END IF;

END
$$

DELIMITER ;
