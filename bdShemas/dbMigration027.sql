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

INSERT INTO attr_value(id, attr_tp, value, locked) VALUES(40, 11, '254', 1);
INSERT INTO attr_value(id, attr_tp, value, locked) VALUES(41, 12, '200', 1);
INSERT INTO lab_resize(id, width, pixels) VALUES(13, 254, 3000);
INSERT INTO lab_resize(id, width, pixels) VALUES(14, 200, 2362);

INSERT INTO attr_synonym(id, src_type, attr_val, synonym) VALUES(92, 1, 40, '20_25_');
INSERT INTO attr_synonym(id, src_type, attr_val, synonym) VALUES(93, 1, 41, '20_25_');
INSERT INTO attr_synonym(id, src_type, attr_val, synonym) VALUES(94, 4, 40, '20x25_');
INSERT INTO attr_synonym(id, src_type, attr_val, synonym) VALUES(95, 4, 41, '20x25_');
INSERT INTO attr_synonym(id, src_type, attr_val, synonym) VALUES(96, 7, 40, '20_25_');
INSERT INTO attr_synonym(id, src_type, attr_val, synonym) VALUES(97, 7, 41, '20_25_');

CREATE TABLE prn_queue (
  id int(11) NOT NULL AUTO_INCREMENT,
  strategy int(11) NOT NULL,
  is_active tinyint(2) DEFAULT 1,
  created datetime DEFAULT NULL,
  started datetime DEFAULT NULL,
  complited datetime DEFAULT NULL,
  label varchar(500) DEFAULT NULL,
  has_sub tinyint(1) DEFAULT 0,
  lab int(5) DEFAULT 0,
  PRIMARY KEY (id),
  INDEX IDX_prn_queue_is_active (is_active),
  CONSTRAINT FK_prn_queue_prn_strategy_id FOREIGN KEY (strategy)
  REFERENCES prn_strategy (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 1
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE prn_sub_queue (
  prn_queue int(11) NOT NULL,
  sub_queue int(5) NOT NULL DEFAULT 0,
  started datetime DEFAULT NULL,
  complited datetime DEFAULT NULL,
  lab int(5) DEFAULT 0,
  PRIMARY KEY (prn_queue, sub_queue),
  CONSTRAINT FK_prn_sub_queue_prn_queue_id FOREIGN KEY (prn_queue)
  REFERENCES prn_queue (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE prn_queue_items (
  prn_queue int(11) NOT NULL,
  sub_queue int(5) NOT NULL DEFAULT 0,
  print_group varchar(50) NOT NULL,
  PRIMARY KEY (prn_queue, print_group, sub_queue),
  CONSTRAINT FK_prn_queue_items_prn_queue_id FOREIGN KEY (prn_queue)
  REFERENCES prn_queue (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

CREATE
FUNCTION prn_queue_complited (p_queue int(11), p_subqueue int(5))
RETURNS int(5)
BEGIN
  DECLARE vResult int(5) DEFAULT (0);

  IF p_subqueue > 0
  THEN
    SELECT IFNULL(MAX(2), 0)
    INTO vResult
      FROM prn_sub_queue psq
      WHERE psq.prn_queue = p_queue
        AND psq.sub_queue = p_subqueue
        AND psq.complited IS NOT NULL;
  END IF;

  IF vResult = 0
  THEN
    -- check by printgroups
    SELECT IFNULL(MIN(IF(pg.state < 250, 0, 1)), 1)
    INTO vResult
      FROM prn_queue_items pqi
        INNER JOIN print_group pg ON pg.id = pqi.print_group
      WHERE pqi.prn_queue = p_queue
        AND pqi.sub_queue = p_subqueue
        AND pg.state >= 200;
  END IF;

  RETURN vResult;
END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE prn_queue_start (IN p_queue int, IN p_subqueue int, IN p_lab int)
BEGIN

  UPDATE prn_queue pq
  SET pq.started = IFNULL(pq.started, NOW()),
      pq.lab = IF(p_subqueue > 0, 0, p_lab)
  WHERE pq.id = p_queue;

  IF p_subqueue > 0
  THEN
    UPDATE prn_sub_queue psq
    SET psq.started = IFNULL(psq.started, NOW()),
        psq.lab = p_lab
    WHERE psq.prn_queue = p_queue
    AND psq.sub_queue = p_subqueue;
  END IF;

END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE prn_queue2_check ()
BEGIN
  DECLARE vIsEnd int DEFAULT (0);
  DECLARE vQueueId int(11);

  DECLARE vCoverComplited int DEFAULT (0);
  DECLARE vBlockComplited int DEFAULT (0);

  DECLARE vCur CURSOR FOR
  SELECT pq.id
    FROM prn_queue pq
      INNER JOIN prn_strategy ps ON pq.strategy = ps.id
    WHERE pq.is_active
      AND pq.complited IS NULL
      AND ps.strategy_type = 2;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vQueueId;
    -- check sub Queues
    -- cover
    SET vCoverComplited = prn_queue_complited(vQueueId, 1);
    IF vCoverComplited = 1
    THEN
      UPDATE prn_sub_queue
      SET complited = NOW()
      WHERE prn_queue = vQueueId
      AND sub_queue = 1;
    END IF;
    -- block
    SET vBlockComplited = prn_queue_complited(vQueueId, 2);
    IF vBlockComplited = 1
    THEN
      UPDATE prn_sub_queue
      SET complited = NOW()
      WHERE prn_queue = vQueueId
      AND sub_queue = 2;
    END IF;

    IF vCoverComplited > 0
      AND vBlockComplited > 0
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

CREATE
PROCEDURE prn_strategy2_start (IN p_strategy int)
BEGIN
  DECLARE vIsEnd int DEFAULT (0);
  DECLARE vFormat varchar(200) DEFAULT ('');
  DECLARE vSheets integer(5) DEFAULT (0);
  DECLARE vQueueId int(11);

  DECLARE vCur CURSOR FOR
  SELECT oei.format, oei.sheets -- , SUM(pg.prints) prints
    FROM print_group pg
      INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id AND pg.sub_id = pg1.sub_id
      INNER JOIN order_extra_info oei ON pg.order_id = oei.id AND pg.sub_id = oei.sub_id
    WHERE pg.state = 200
      AND pg.sub_id = ''
      AND pg.is_reprint = 0
      AND pg.is_pdf = 1
      AND pg.book_type > 0
      AND pg.book_part = 2
      AND pg1.state = 200
      AND pg1.is_pdf = 1
      AND pg1.book_type > 0
      AND pg1.book_part = 1
      AND pg1.is_reprint = 0
    GROUP BY oei.format, oei.sheets
    ORDER BY SUM(pg.prints) DESC;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vFormat, vSheets;
    IF vIsEnd
    THEN
      LEAVE wet;
    END IF;
    -- create queue
    INSERT INTO prn_queue (strategy, is_active, created, label, has_sub)
      VALUES (p_strategy, 1, NOW(), CONCAT_WS('x', vFormat, vSheets), 1);
    SET vQueueId = LAST_INSERT_ID();

    -- create covers sub queue
    INSERT INTO prn_sub_queue (prn_queue, sub_queue, is_active)
      VALUES (vQueueId, 1, 1);
    -- fill covers sub queue
    INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
      SELECT vQueueId, 1, pg.id
        FROM print_group pg
          INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id AND pg.sub_id = pg1.sub_id
          INNER JOIN order_extra_info oei ON pg.order_id = oei.id AND pg.sub_id = oei.sub_id
        WHERE pg.state = 200
          AND pg.sub_id = ''
          AND pg.is_reprint = 0
          AND pg.is_pdf = 1
          AND pg.book_type > 0
          AND pg.book_part = 1
          AND pg1.state = 200
          AND pg1.is_pdf = 1
          AND pg1.book_type > 0
          AND pg1.book_part = 2
          AND pg1.is_reprint = 0
          AND oei.format = vFormat
          AND oei.sheets = vSheets;

    -- create block sub queue
    INSERT INTO prn_sub_queue (prn_queue, sub_queue, is_active)
      VALUES (vQueueId, 2, 1);
    -- fill block sub queue
    INSERT INTO prn_queue_items (prn_queue, sub_queue, print_group)
      SELECT vQueueId, 2, pg.id
        FROM print_group pg
          INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id AND pg.sub_id = pg1.sub_id
          INNER JOIN order_extra_info oei ON pg.order_id = oei.id AND pg.sub_id = oei.sub_id
        WHERE pg.state = 200
          AND pg.sub_id = ''
          AND pg.is_reprint = 0
          AND pg.is_pdf = 1
          AND pg.book_type > 0
          AND pg.book_part = 2
          AND pg1.state = 200
          AND pg1.is_pdf = 1
          AND pg1.book_type > 0
          AND pg1.book_part = 1
          AND pg1.is_reprint = 0
          AND oei.format = vFormat
          AND oei.sheets = vSheets;

    -- mark print groups
    UPDATE print_group
    SET prn_queue = vQueueId
    WHERE id IN (SELECT qi.print_group
        FROM prn_queue_items qi
        WHERE qi.prn_queue = vQueueId);

  END LOOP wet;
  CLOSE vCur;

END
$$

DELIMITER ;