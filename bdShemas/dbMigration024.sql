-- main  
-- moskva 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (139, 'Ожидание цветокоррекции', 0, 0, 0, 0);

ALTER TABLE suborders ADD COLUMN color_corr TINYINT(1) DEFAULT 0 AFTER alias;