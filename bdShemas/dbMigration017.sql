-- main 2015-03-18   
-- moskva 2015-03-18
SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

CREATE TABLE package_message (
  source int(7) NOT NULL,
  id int(11) NOT NULL,
  log_key varchar(25) NOT NULL DEFAULT '',
  log_user varchar(50) DEFAULT NULL,
  message varchar(255) DEFAULT NULL,
  PRIMARY KEY (source, id, log_key),
  CONSTRAINT FK_package_message FOREIGN KEY (source, id)
  REFERENCES package (source, id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 16384
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE rack_type (
  id int(5) NOT NULL AUTO_INCREMENT,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AUTO_INCREMENT = 1
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO rack_type(id, name) VALUES (0, '-');

CREATE TABLE rack (
  id int(5) NOT NULL AUTO_INCREMENT,
  rack_type int(5) NOT NULL DEFAULT 0,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_rack_rack_type_id FOREIGN KEY (rack_type)
  REFERENCES rack_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 1
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO rack(id, name) VALUES (0, '-');

CREATE TABLE rack_space (
  id int(5) NOT NULL AUTO_INCREMENT,
  rack int(5) NOT NULL DEFAULT 0,
  name varchar(50) DEFAULT NULL,
  width int(5) DEFAULT 0,
  height int(5) DEFAULT 0,
  weight int(5) DEFAULT 0,
  package_source int(7) DEFAULT 0,
  package_id int(11) DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT FK_rack_space_rack_id FOREIGN KEY (rack)
  REFERENCES rack (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 1
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE rack_orders (
  order_id varchar(50) NOT NULL DEFAULT '',
  space int(5) NOT NULL DEFAULT 0,
  PRIMARY KEY (order_id),
  CONSTRAINT FK_rack_orders_rack_space_id FOREIGN KEY (space)
  REFERENCES rack_space (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE rack_tech_point (
  rack int(5) NOT NULL DEFAULT 0,
  tech_point int(5) NOT NULL DEFAULT 0,
  PRIMARY KEY (rack, tech_point),
  CONSTRAINT FK_rack_tech_point_rack_id FOREIGN KEY (rack)
  REFERENCES rack (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_rack_tech_point_tech_point_id FOREIGN KEY (tech_point)
  REFERENCES tech_point (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (511, 'Группа объединена', 0, 0, 0, 0);
INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (457, 'Упакован', 0, 0, 0, 0);

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES (87, 5, 'Сообщения менеджера', 'rawMessages', 0, 1);
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES (0, 87, 'messages');

