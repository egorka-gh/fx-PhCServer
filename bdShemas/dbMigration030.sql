-- main    
-- new main (virt)    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

DELETE FROM order_state WHERE id IN (111,112);

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(114, 'Ожидание проверки', 1, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(115, 'Проверка', 1, 0, 0, 0);