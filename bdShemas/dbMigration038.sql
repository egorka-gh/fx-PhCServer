-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE sources 
  ADD COLUMN has_boxes TINYINT(1) DEFAULT 0;
  
ALTER TABLE rack_orders 
  ADD COLUMN is_box TINYINT(1) DEFAULT 0;  

INSERT INTO book_type(id, name) VALUES(10, 'Polaroid');  