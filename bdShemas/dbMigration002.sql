SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (212, 'Размещен напечать', 0, 0, 0, 0);

CREATE TABLE staff_group (
  id int(5) NOT NULL,
  name varchar(20) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO staff_group(id, name) VALUES (0, '-');
INSERT INTO staff_group(id, name) VALUES (1, 'users');

CREATE TABLE staff (
  id int(5) NOT NULL AUTO_INCREMENT,
  staff_group int(5) NOT NULL DEFAULT 1,
  name varchar(50) NOT NULL,
  pwd varchar(50) DEFAULT NULL,
  barcode varchar(50) DEFAULT NULL,
  active tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  CONSTRAINT FK_staff_staff_group_id FOREIGN KEY (staff_group)
  REFERENCES staff_group (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO staff(id, staff_group, name, pwd, barcode, active) VALUES (0, 0, '-', NULL, NULL, 1);

CREATE TABLE staff_activity_group (
  id int(5) NOT NULL AUTO_INCREMENT,
  name varchar(20) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO staff_activity_group(id, name) VALUES (0, '-');
INSERT INTO staff_activity_group(id, name) VALUES (1, 'Перепечатка');

CREATE TABLE staff_activity_type (
  id int(5) NOT NULL AUTO_INCREMENT,
  staff_activity_group int(5) NOT NULL DEFAULT 0,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_staff_activity_type_staff_activity_group_id FOREIGN KEY (staff_activity_group)
  REFERENCES staff_activity_group (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO staff_activity_type(id, staff_activity_group, name) VALUES (0, 0, '-');

CREATE TABLE staff_activity (
  id int(10) NOT NULL AUTO_INCREMENT,
  order_id varchar(50) NOT NULL DEFAULT '',
  pg_id varchar(50) NOT NULL DEFAULT '',
  sa_group int(5) NOT NULL DEFAULT 0,
  sa_type int(5) NOT NULL DEFAULT 0,
  staff int(5) NOT NULL DEFAULT 0,
  log_date datetime NOT NULL,
  remark varchar(250) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_staff_activity_orders_id FOREIGN KEY (order_id)
  REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_staff_activity_staff_activity_group_id FOREIGN KEY (sa_group)
  REFERENCES staff_activity_group (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_staff_activity_staff_activity_type_id FOREIGN KEY (sa_type)
  REFERENCES staff_activity_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_staff_activity_staff_id FOREIGN KEY (staff)
  REFERENCES staff (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;