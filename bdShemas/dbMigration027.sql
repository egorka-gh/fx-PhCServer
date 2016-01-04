-- main    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

-- ALTER TABLE lab_device ADD COLUMN prn_strategy INT(5) DEFAULT 0 AFTER tech_point;
ALTER TABLE print_group
  ADD COLUMN prints_done INT(10) DEFAULT 0 AFTER prints,
  ADD COLUMN prn_queue INT(11) DEFAULT 0 AFTER prints_done,
  ADD COLUMN butt INT(5) DEFAULT 0 AFTER prn_queue;
  
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

  -- check complited
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
            pg.state_date = vEnd,
            pg.prints_done = LEAST(pg.prints, vDone)
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
    -- update prints
    IF pState = 300
      AND pPgroup != ''
    THEN
      UPDATE print_group pg
      SET pg.prints_done = LEAST(pg.prints, vDone)
      WHERE pg.id = pPgroup;
    END IF;
    -- start extraState
    CALL extraStateStart(pOrder, pSubOrder, pState, vStart);
  END IF;

END
$$

DELIMITER ;
-- main 30.12.2015  

CREATE TABLE prn_limit_type (
  id int(5) NOT NULL DEFAULT 0,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 4096
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO prn_limit_type(id, name) VALUES(0, '-');
INSERT INTO prn_limit_type(id, name) VALUES(1, 'По длине');
INSERT INTO prn_limit_type(id, name) VALUES(2, 'По времени');
INSERT INTO prn_limit_type(id, name) VALUES(3, 'Кол отпечатков');

CREATE TABLE prn_strategy_type (
  id int(5) NOT NULL DEFAULT 0,
  name varchar(50) DEFAULT NULL,
  default_priority int(5) DEFAULT 0,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 5461
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO prn_strategy_type(id, name, default_priority) VALUES(0, 'Пихалка', 0);
INSERT INTO prn_strategy_type(id, name, default_priority) VALUES(1, 'Выбор рулона', 10);
INSERT INTO prn_strategy_type(id, name, default_priority) VALUES(2, 'Партия pdf', 100);


CREATE TABLE prn_strategy (
  id int(11) NOT NULL AUTO_INCREMENT,
  lab int(5) NOT NULL DEFAULT 0,
  is_active tinyint(2) NOT NULL DEFAULT 1,
  strategy_type int(5) NOT NULL DEFAULT 0,
  priority int(5) DEFAULT 0,
  time_start datetime DEFAULT NULL,
  last_start datetime DEFAULT NULL,
  time_end datetime DEFAULT NULL,
  refresh_interval int(5) DEFAULT 0,
  width int(5) DEFAULT 0,
  paper int(5) DEFAULT 0,
  limit_type int(5) DEFAULT 0,
  limit_val int(5) DEFAULT 0,
  limit_done int(5) DEFAULT 0,
  order_type int(5) DEFAULT 0,
  PRIMARY KEY (id),
  INDEX IDX_prn_strategy (lab, is_active),
  CONSTRAINT FK_prn_strategy_prn_limit_type_id FOREIGN KEY (limit_type)
  REFERENCES prn_limit_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_prn_strategy_prn_order_type_id FOREIGN KEY (order_type)
  REFERENCES prn_order_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_prn_strategy_prn_strategy_type_id FOREIGN KEY (strategy_type)
  REFERENCES prn_strategy_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 1
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

CREATE
TRIGGER tg_prn_strategy_bi
BEFORE INSERT
ON prn_strategy
FOR EACH ROW
BEGIN
  DECLARE vPriority int DEFAULT 0;

  IF NEW.priority = 0
  THEN
    SELECT MAX(st.default_priority)
    INTO vPriority
      FROM prn_strategy_type st
      WHERE st.id = NEW.strategy_type;
    SET NEW.priority = vPriority;
  END IF;
END
$$

DELIMITER ;

