-- main 
-- moskva 
-- reserv 
-- valichek

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

UPDATE order_state
SET id = 150, runtime = 0
WHERE id = 114;

UPDATE order_state
SET id = 170, runtime = 1
WHERE id = 140;

DELETE FROM order_state
WHERE id IN(124,125);

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES
(151, 'Подготовить в первую очередь', 1, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES
(155, 'Проверка web статуса', 1, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES
(156, 'Web ok', 1, 0, 0, 0);

UPDATE order_state
SET id = 160
WHERE id = 115;

UPDATE order_state
SET id = 165
WHERE id = 120;

UPDATE order_state
SET id = 130
WHERE id = 113;

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(140, 'Цветокоррекция', 0, 0, 0, 0);