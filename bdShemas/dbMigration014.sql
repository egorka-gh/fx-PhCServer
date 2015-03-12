-- main 2015-03-09 
-- moskva 2015-03-10
SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO form(id, name, report) VALUES (3, 'Бел EMS', 'frmBelEMS');
INSERT INTO delivery_type_form(delivery_type, form) VALUES (9, 3);
INSERT INTO form_parametr(form, form_field) VALUES (3, 1);
INSERT INTO form_parametr(form, form_field) VALUES (3, 5);
INSERT INTO form_parametr(form, form_field) VALUES (3, 11);
INSERT INTO form_parametr(form, form_field) VALUES (3, 14);
INSERT INTO form_parametr(form, form_field) VALUES (3, 15);
INSERT INTO form_parametr(form, form_field) VALUES (3, 16);

ALTER TABLE form_field_items
  ADD COLUMN prefix VARCHAR(20) DEFAULT '' AFTER delemiter,
  ADD COLUMN sufix VARCHAR(20) DEFAULT '' AFTER prefix;
  
DELETE FROM form_field_items;

INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (1, 1, 0, 0, 0, 56, '', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (2, 1, 1, 0, 0, 57, ' ', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (3, 1, 2, 0, 0, 58, ' ', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (4, 2, 0, 0, 0, 61, '', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (5, 3, 0, 0, 0, 62, '', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (6, 4, 0, 0, 0, 66, '', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (7, 5, 0, 0, 0, 59, '', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (8, 14, 0, 0, 0, 64, '', '', ' обл.');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (9, 14, 1, 0, 0, 65, ', ', '', ' р-н');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (10, 14, 2, 0, 0, 66, ', ', 'г. ', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (11, 15, 0, 0, 0, 67, '', 'ул. ', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (12, 15, 1, 0, 0, 68, ' ', '', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (13, 15, 2, 0, 0, 69, ' ', 'кв. ', '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES (14, 16, 0, 0, 0, 63, '', '', '');

DELIMITER $$

DROP PROCEDURE IF EXISTS printStateCancel$$

CREATE
PROCEDURE printStateCancel(IN pPgroupId varchar(50))
  MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  UPDATE print_group pg
  SET pg.state = 200,
      pg.state_date = NOW()
  WHERE pg.id = pPgroupId;

  SELECT pg.order_id, pg.sub_id INTO vOrderId, vSubId
  FROM print_group pg
  WHERE pg.id = pPgroupId;

  IF vOrderId IS NOT NULL THEN
    -- reset order/suborder state & extra
    UPDATE orders o
    SET o.state = 250,
        o.state_date = NOW()
    WHERE o.id = vOrderId
    AND o.state != 250;
    UPDATE order_extra_state
    SET state_date = NULL
    WHERE id = vOrderId
    AND sub_id = ''
    AND state IN (210, 250);
    IF vSubId != '' THEN
      UPDATE suborders o
      SET o.state = 250,
          o.state_date = NOW()
      WHERE o.id = vOrderId
      AND o.sub_id = vSubId
      AND o.state != 250;
      UPDATE order_extra_state
      SET state_date = NULL
      WHERE id = vOrderId
      AND sub_id = vSubId
      AND state IN (210, 250);
    END IF;

  END IF;

END
$$

DELIMITER ;
