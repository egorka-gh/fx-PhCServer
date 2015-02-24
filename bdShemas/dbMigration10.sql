-- main cycle 2015-02-24

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

UPDATE lab_print_code
SET correction = 0, frame = 0
WHERE src_type = 8;

CREATE TABLE delivery_type_form (
  delivery_type int(5) NOT NULL DEFAULT 0,
  form int(5) NOT NULL DEFAULT 0,
  PRIMARY KEY (form, delivery_type),
  CONSTRAINT FK_delivery_type_form_delivery_type_id FOREIGN KEY (delivery_type)
  REFERENCES delivery_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_delivery_type_form_form_id FOREIGN KEY (form)
  REFERENCES form (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO form(id, name, report) VALUES (1, 'Печать ШК', 'mpBarcodeFrm');
INSERT INTO delivery_type_form(delivery_type, form) VALUES (0, 1);