-- main 
-- moskva 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

-- main
ALTER TABLE orders
  ADD COLUMN lock_owner VARCHAR(50) DEFAULT NULL AFTER resume_load;
-- main  
CREATE TABLE log_capture (
  id int(11) NOT NULL AUTO_INCREMENT,
  log_date datetime DEFAULT NULL,
  lock_key varchar(100) DEFAULT NULL,
  lock_owner varchar(50) DEFAULT NULL,
  from_state int(5) DEFAULT NULL,
  to_state int(5) DEFAULT NULL,
  result int(5) DEFAULT NULL,
  PRIMARY KEY (id),
  INDEX IDX_capture_log (log_date, lock_key)
)
ENGINE = INNODB
AUTO_INCREMENT = 1
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE book_pg_alt_paper (
  id int(11) NOT NULL AUTO_INCREMENT,
  template int(7) DEFAULT NULL,
  sh_from int(5) DEFAULT 0,
  sh_to int(5) DEFAULT 0,
  paper int(5) DEFAULT 0,
  interlayer int(5) DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT FK_book_pg_alt_paper_attr_value_id FOREIGN KEY (paper)
  REFERENCES attr_value (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_book_pg_alt_paper_book_pg_template_id FOREIGN KEY (template)
  REFERENCES book_pg_template (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 1
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO book_part(id, name) VALUES (5, 'БлокОбложка');

ALTER TABLE print_group_file ADD COLUMN book_part TINYINT(2) DEFAULT 0 AFTER caption;

DELIMITER $$
DROP PROCEDURE IF EXISTS orderCleanUp$$
CREATE 
PROCEDURE orderCleanUp(IN pId varchar(50), IN pState int)
  MODIFIES SQL DATA
BEGIN
  DECLARE vDate datetime DEFAULT NOW();

  DELETE FROM print_group WHERE order_id = pId;

  IF pState=100 THEN
    DELETE FROM order_extra_info WHERE id = pId;
    DELETE FROM suborders WHERE order_id = pId;
  END IF;

  UPDATE orders
    SET state = pState, state_date = vDate, resume_load=0
    WHERE id = pId;

  INSERT INTO state_log
  (order_id, state, state_date, comment)
    VALUES (pId, pState, vDate, 'reset');
END
$$
DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS printStateCancel$$

CREATE 
PROCEDURE printStateCancel(IN pPgroupId varchar(50))
  MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  UPDATE print_group pg
  SET pg.state = 200,
      pg.state_date = NOW()
  WHERE pg.id = pPgroupId;

  SELECT pg.order_id, pg.sub_id INTO vOrderId, vSubId
  FROM print_group pg
  WHERE pg.id = pPgroupId;

  IF vOrderId IS NOT NULL THEN
    -- reset order/suborder state & extra
    UPDATE orders o
    SET o.state = 250,
        o.state_date = NOW()
    WHERE o.id = vOrderId
    AND o.state != 250;
    UPDATE order_extra_state
    SET state_date = NULL
    WHERE id = vOrderId
    AND sub_id = ''
    AND state IN (210, 250);
    IF vSubId != '' THEN
      UPDATE suborders o
      SET o.state = 250,
          o.state_date = NOW()
      WHERE o.order_id = vOrderId
      AND o.sub_id = vSubId
      AND o.state != 250;
      UPDATE order_extra_state
      SET state_date = NULL
      WHERE id = vOrderId
      AND sub_id = vSubId
      AND state IN (210, 250);
    END IF;

  END IF;

END
$$

DELIMITER ;

