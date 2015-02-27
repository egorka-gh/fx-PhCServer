-- main cycle 2015-02-26
SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE form_field
  ADD COLUMN parametr VARCHAR(50) DEFAULT NULL AFTER name;

ALTER TABLE form_field
  ADD COLUMN simplex TINYINT(1) DEFAULT 0 AFTER parametr;

UPDATE form_field SET parametr='pfio' WHERE id=1;
UPDATE form_field SET parametr='ppass_num' WHERE id=2;
UPDATE form_field SET parametr='ppass_info' WHERE id=3;
UPDATE form_field SET parametr='pcity' WHERE id=4;
UPDATE form_field SET parametr='pphone' WHERE id=5;
  
CREATE TABLE form_parametr (
  form int(5) NOT NULL,
  form_field int(5) NOT NULL,
  PRIMARY KEY (form, form_field),
  CONSTRAINT FK_form_parametr_form_field_id FOREIGN KEY (form_field)
  REFERENCES form_field (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_form_parametr_form_id FOREIGN KEY (form)
  REFERENCES form (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO form_parametr(form, form_field) VALUES (2, 1);
INSERT INTO form_parametr(form, form_field) VALUES (2, 2);
INSERT INTO form_parametr(form, form_field) VALUES (2, 3);
INSERT INTO form_parametr(form, form_field) VALUES (2, 4);
INSERT INTO form_parametr(form, form_field) VALUES (2, 5);

INSERT INTO form_field(id, name, parametr, simplex) VALUES (6, 'client_id', 'pclient_id', 1);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (7, 'delivery_name', 'pdelivery_name', 1);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (8, 'execution_date', 'pexecution_date', 1);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (9, 'id', 'pid', 1);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (10, 'id_name', 'pid_name', 1);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (11, 'source_name', 'psource_name', 1);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (12, 'source_code', 'psource_code', 1);
INSERT INTO form_field(id, name, parametr, simplex) VALUES (13, 'orders_num', 'porders_num', 1);