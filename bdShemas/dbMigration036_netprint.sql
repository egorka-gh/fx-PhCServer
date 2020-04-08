-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

CREATE TABLE group_netprint (
  source int(7) NOT NULL,
  group_id int(11) NOT NULL,
  netprint_id varchar(50) NOT NULL,
  created datetime NOT NULL,
  state int(5) DEFAULT 0,
  box_number int(5) DEFAULT NULL,
  send tinyint(2) DEFAULT 0,
  PRIMARY KEY (source, group_id, netprint_id)
)
ENGINE = INNODB,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

DELIMITER $$

--
-- Создать триггер `group_netprint_bi`
--
CREATE
TRIGGER group_netprint_bi
BEFORE INSERT
ON group_netprint
FOR EACH ROW
BEGIN
  SET NEW.created = NOW();
END
$$

ALTER TABLE sources_sync 
  ADD COLUMN np_sync_tstamp INT(11) DEFAULT 0;
  
  -- main 06.04.2020