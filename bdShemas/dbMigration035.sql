-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(98, 6, 'Задолженость', 'debt_sum', 0, 1);
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 98, 'address.debt_sum');

-- refactor delivery_type_dictionary to hold same delivery_type vs different site_id
--
-- Удалить внешний ключ
--
ALTER TABLE delivery_type_dictionary 
   DROP FOREIGN KEY FK_delivery_type_dictionary_delivery_type_id;

--
-- Удалить индекс `FK_delivery_type_dictionary_delivery_type_id` из объекта типа таблица `delivery_type_dictionary`
--
ALTER TABLE delivery_type_dictionary 
   DROP INDEX FK_delivery_type_dictionary_delivery_type_id;

--
-- Удалить внешний ключ
--
ALTER TABLE delivery_type_dictionary 
   DROP FOREIGN KEY FK_delivery_type_dictionary_sources_id;

--
-- Удалить индекс `FK_delivery_type_dictionary_delivery_type_id` из объекта типа таблица `delivery_type_dictionary`
-- Can throw error, ignore it
ALTER TABLE delivery_type_dictionary 
   DROP INDEX FK_delivery_type_dictionary_sources_id;


--
-- Удалить индекс `PRIMARY` из объекта типа таблица `delivery_type_dictionary`
--
ALTER TABLE delivery_type_dictionary 
   DROP PRIMARY KEY;

--
-- Изменить столбец `delivery_type` для таблицы `delivery_type_dictionary`
--
ALTER TABLE delivery_type_dictionary 
  CHANGE COLUMN delivery_type delivery_type INT(5) DEFAULT 0;

--
-- Изменить столбец `site_id` для таблицы `delivery_type_dictionary`
--
ALTER TABLE delivery_type_dictionary 
  CHANGE COLUMN site_id site_id INT(10) NOT NULL DEFAULT 0;

--
-- Создать индекс `PRIMARY` для объекта типа таблица `delivery_type_dictionary`
--
ALTER TABLE delivery_type_dictionary 
  ADD PRIMARY KEY (source, site_id);

--
-- Создать внешний ключ
--
ALTER TABLE delivery_type_dictionary 
  ADD CONSTRAINT FK_delivery_type_dictionary_delivery_type_id FOREIGN KEY (delivery_type)
    REFERENCES delivery_type(id);

--
-- Создать внешний ключ
--
ALTER TABLE delivery_type_dictionary 
  ADD CONSTRAINT FK_delivery_type_dictionary_sources_id FOREIGN KEY (source)
REFERENCES sources (id);

-- 2019-11-14 applied on main cycle