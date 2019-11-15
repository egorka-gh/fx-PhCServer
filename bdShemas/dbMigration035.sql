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

INSERT INTO delivery_type(id, name, hideClient) VALUES
(10, 'EMS-Белпочта бесплатно', 0),
(11, 'Белпочта', 0),
(12, 'Белпочта бесплатно', 0);

INSERT INTO delivery_type_form (delivery_type, form)
  SELECT dt.id, f.form
    FROM delivery_type_form f
      INNER JOIN delivery_type dt ON dt.id IN (10, 11, 12)
    WHERE f.delivery_type = 9;

-- 2019-11-14 applied on main cycle