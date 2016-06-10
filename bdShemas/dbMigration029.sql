-- main    
-- new main (virt)    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';


ALTER TABLE prn_strategy_type
  ADD COLUMN allow_manual TINYINT(4) DEFAULT 0 AFTER default_priority;
  
