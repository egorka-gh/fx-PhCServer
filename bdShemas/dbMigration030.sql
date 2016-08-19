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
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(102, 'Ожидание загрузки исправлен', 0, 0, 0, 0);

ALTER TABLE services
  ADD COLUMN appkey VARCHAR(50) DEFAULT NULL AFTER connections;
  
INSERT INTO attr_family(id, name) VALUES(7, 'OrderLoad');
INSERT INTO attr_family(id, name) VALUES(8, 'OrderLoadFiles');

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(88, 7, 'id источника', 'src_id', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(89, 7, 'Status источника', 'src_state', 0, 0);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(90, 7, 'filled источника', 'filled', 0, 0);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(91, 7, 'allow_to_change_status', 'canChangeRemoteState', 0, 0);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(92, 7, 'Папка ftp', 'ftp_folder', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(93, 7, 'Кол файлов', 'fotos_num', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(94, 8, 'Имя файла', 'file_name', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(95, 8, 'Размер файла', 'size', 0, 1);
INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(96, 8, 'hash файла', 'hash_remote', 0, 1);

INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 88, 'id');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 89, 'status');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 90, 'filled');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 91, 'allow_to_change_status');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 92, 'folder');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 93, 'quantity');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 94, 'name');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 95, 'size');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 96, 'hash');