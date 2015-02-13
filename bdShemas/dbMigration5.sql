SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE package
  ADD COLUMN orders_num INT(5) DEFAULT 0 AFTER mail_service;
  
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 54, 'amount');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 55, 'weight');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 56, 'address.lastname');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 57, 'address.firstname');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 58, 'address.middlename');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 59, 'address.phone');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 60, 'address.email');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 61, 'address.passport');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 62, 'address.passport_date');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 63, 'address.postal');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 64, 'address.region');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 65, 'address.district');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 66, 'address.city');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 67, 'address.street');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 68, 'address.home');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 69, 'address.flat');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 70, 'address.comment');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 71, 'address.invisible');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 72, 'address.address');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 73, 'address.metro');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 74, 'address.delivery_time');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 75, 'address.slCity');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 76, 'address.slPickup');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 77, 'shoplogistics.city');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 78, 'shoplogistics.pickup_name');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 79, 'shoplogistics.pickup_type');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 80, 'shoplogistics.pickup_max_weight');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 81, 'shoplogistics.delivery_type');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 82, 'shoplogistics.delivery_number');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 83, 'shoplogistics.delivery_code');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES
(0, 84, 'shoplogistics.delivery_status');  

CREATE TABLE package_barcode (
  source int(7) NOT NULL,
  id int(11) NOT NULL,
  barcode varchar(50) NOT NULL DEFAULT '',
  bar_type tinyint(4) DEFAULT 0,
  PRIMARY KEY (source, id, barcode)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;