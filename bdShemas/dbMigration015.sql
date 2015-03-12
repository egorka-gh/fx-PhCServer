-- main  2015-03-12
-- moskva 2015-03-11
SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE app_config CHANGE COLUMN production production INT(5) DEFAULT 0;
ALTER TABLE app_config CHANGE COLUMN id id INT(5) NOT NULL DEFAULT 1;

DELIMITER $$

DROP PROCEDURE IF EXISTS orderCancel$$

CREATE
PROCEDURE orderCancel(IN pId VARCHAR(50), IN pState INT)
  MODIFIES SQL DATA
BEGIN
  DECLARE vDate datetime DEFAULT NOW();
  DECLARE vState int;
  
  SET vState = IFNULL(pState,510);
  
  IF vState <500 THEN
    SET vState = 510;
  END IF;

  UPDATE print_group pg
  SET pg.state = vState, pg.state_date = vDate
  WHERE pg.order_id = pId;

  UPDATE suborders s
  SET s.state = vState, s.state_date = vDate
  WHERE s.order_id = pId;

  UPDATE orders o
  SET o.state = vState, o.state_date = vDate
  WHERE o.id = pId;
END
$$

DELIMITER ;
