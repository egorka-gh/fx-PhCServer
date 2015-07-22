-- main 
-- moskva 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE orders
  ADD COLUMN lock_owner VARCHAR(50) DEFAULT NULL AFTER resume_load;
  
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