-- main    2015-03-19
-- moskva 2015-03-23
-- valichek 2015-03-30
SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';


INSERT INTO form_field(id, name, parametr, simplex) VALUES (17, 'AT До двери', 'pat_to_door', 1);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (18, 'Адрес', 'paddress', 0);

INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (15, 18, 0, 1, 14, 0, '', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (16, 18, 1, 1, 15, 0, ' ', '', '');

INSERT INTO form_parametr(form, form_field) VALUES (2, 17);
INSERT INTO form_parametr(form, form_field) VALUES (2, 18);

INSERT INTO attr_value(id, attr_tp, value, locked) VALUES (38, 2, 'Мелованная170ламин', 1);

ALTER TABLE package ADD INDEX IDX_package_state (state);

DELIMITER $$

CREATE
PROCEDURE extraStateFix (IN pOrder varchar(50), IN pState int, IN pDate datetime)
BEGIN
  INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
    SELECT pOrder, '', pState, pDate, pDate
    UNION ALL
    SELECT s.order_id, s.sub_id, pState, pDate, pDate
      FROM suborders s
      WHERE s.order_id = pOrder
  ON DUPLICATE KEY UPDATE state_date = pDate;
END
$$

DELIMITER ;
