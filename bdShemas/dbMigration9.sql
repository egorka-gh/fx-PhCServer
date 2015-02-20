-- main cycle 

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

CREATE TABLE delivery_type (
  id int(5) NOT NULL AUTO_INCREMENT,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AUTO_INCREMENT = 1
AVG_ROW_LENGTH = 4096
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO delivery_type(id, name) VALUES (0, '-');

CREATE TABLE delivery_type_dictionary (
  source int(7) NOT NULL DEFAULT 0,
  delivery_type int(5) NOT NULL DEFAULT 0,
  site_id int(5) DEFAULT 0,
  PRIMARY KEY (source, delivery_type),
  CONSTRAINT FK_delivery_type_dictionary_delivery_type_id FOREIGN KEY (delivery_type)
  REFERENCES delivery_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_delivery_type_dictionary_sources_id FOREIGN KEY (source)
  REFERENCES sources (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE form (
  id int(5) NOT NULL AUTO_INCREMENT,
  name varchar(50) DEFAULT NULL,
  report varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AUTO_INCREMENT = 2
AVG_ROW_LENGTH = 4096
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO form(id, name, report) VALUES (0, '-', NULL);

CREATE TABLE form_field (
  id int(5) NOT NULL AUTO_INCREMENT,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AUTO_INCREMENT = 1
AVG_ROW_LENGTH = 4096
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO form_field(id, name) VALUES (0, '-');
INSERT INTO form_field(id, name) VALUES (1, 'ФИО');
INSERT INTO form_field(id, name) VALUES (2, '№ паспорта');
INSERT INTO form_field(id, name) VALUES (3, 'паспорт выдан');
INSERT INTO form_field(id, name) VALUES (4, 'Город');
INSERT INTO form_field(id, name) VALUES (5, '№ телефона');

CREATE TABLE form_field_items (
  id int(7) NOT NULL AUTO_INCREMENT,
  form_field int(5) NOT NULL DEFAULT 0,
  sequence int(5) DEFAULT 0,
  is_field tinyint(1) DEFAULT 0,
  child_field int(5) DEFAULT 0,
  attr_type int(5) DEFAULT 0,
  delemiter varchar(3) DEFAULT '',
  PRIMARY KEY (id),
  CONSTRAINT FK_form_field_items_attr_type_id FOREIGN KEY (attr_type)
  REFERENCES attr_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_form_field_items_child_field_id FOREIGN KEY (child_field)
  REFERENCES form_field (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_form_field_items_form_field_id FOREIGN KEY (form_field)
  REFERENCES form_field (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 1
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (1, 1, 0, 0, 0, 56, ' ');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (2, 1, 1, 0, 0, 57, ' ');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (3, 1, 2, 0, 0, 58, '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (4, 2, 0, 0, 0, 61, '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (5, 3, 0, 0, 0, 62, '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (6, 4, 0, 0, 0, 66, '');
INSERT INTO form_field_items(id, form_field, sequence, is_field, child_field, attr_type, delemiter) VALUES (7, 5, 0, 0, 0, 59, '');