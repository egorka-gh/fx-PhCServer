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

INSERT INTO prn_strategy_type(id, name, default_priority, allow_manual) VALUES(3, 'Партия', 100, 1);
UPDATE prn_strategy_type SET allow_manual = 1 WHERE id = 1;

ALTER TABLE prn_queue
  DROP FOREIGN KEY FK_prn_queue_prn_strategy_id;
  
ALTER TABLE prn_queue_items
  DROP FOREIGN KEY FK_prn_queue_items_prn_queue_id;
ALTER TABLE prn_queue_items
  ADD CONSTRAINT FK_prn_queue_items_prn_queue_id FOREIGN KEY (prn_queue)
    REFERENCES prn_queue(id) ON DELETE CASCADE ON UPDATE CASCADE;  