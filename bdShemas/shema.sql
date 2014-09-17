--
-- Скрипт сгенерирован Devart dbForge Studio for MySQL, Версия 6.2.280.0
-- Домашняя страница продукта: http://www.devart.com/ru/dbforge/mysql/studio
-- Дата скрипта: 17.09.2014 10:57:49
-- Версия сервера: 5.1.73-community
-- Версия клиента: 4.1
--


CREATE TABLE attr_family (
  id int(5) NOT NULL,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 4096
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE book_part (
  id int(5) NOT NULL,
  name varchar(20) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 3276
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE book_synonym_type (
  id int(5) NOT NULL,
  name varchar(20) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 8192
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE book_type (
  id int(5) NOT NULL,
  name varchar(20) DEFAULT 'Наименование',
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 1820
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE content_filter (
  id int(5) NOT NULL AUTO_INCREMENT,
  name varchar(50) DEFAULT NULL,
  is_photo_allow tinyint(1) NOT NULL DEFAULT 0,
  is_retail_allow tinyint(1) NOT NULL DEFAULT 0,
  is_pro_allow tinyint(1) NOT NULL DEFAULT 0,
  is_alias_filter tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AUTO_INCREMENT = 1
AVG_ROW_LENGTH = 8192
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE lab_resize (
  id int(5) NOT NULL AUTO_INCREMENT,
  width int(5) NOT NULL,
  pixels int(5) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE INDEX width (width)
)
ENGINE = INNODB
AUTO_INCREMENT = 13
AVG_ROW_LENGTH = 1365
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE layer (
  id int(5) NOT NULL AUTO_INCREMENT,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AUTO_INCREMENT = 5
AVG_ROW_LENGTH = 2730
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE layer_group (
  id int(5) NOT NULL,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 4096
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE layer_tray (
  id int(5) NOT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 2048
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE layerset_group (
  id int(5) NOT NULL AUTO_INCREMENT,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AUTO_INCREMENT = 2
AVG_ROW_LENGTH = 8192
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE layerset_type (
  id int(5) NOT NULL,
  name varchar(20) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 5461
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE location_type (
  id int(5) NOT NULL,
  name varchar(30) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 4096
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE order_exstate_prolong (
  id varchar(50) NOT NULL DEFAULT '',
  sub_id varchar(50) NOT NULL DEFAULT '',
  state int(10) DEFAULT 0,
  state_date datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  comment char(255) DEFAULT NULL,
  PRIMARY KEY (id, sub_id, state_date)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 3276
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE order_extra_info (
  id varchar(50) NOT NULL DEFAULT '',
  sub_id varchar(50) NOT NULL DEFAULT '',
  endpaper varchar(100) DEFAULT NULL,
  interlayer varchar(100) DEFAULT NULL,
  calc_type varchar(100) DEFAULT NULL,
  cover varchar(100) DEFAULT NULL,
  format varchar(100) DEFAULT NULL,
  corner_type varchar(100) DEFAULT NULL,
  kaptal varchar(100) DEFAULT NULL,
  tempId varchar(50) DEFAULT NULL,
  PRIMARY KEY (id, sub_id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 377
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE order_extra_state (
  id varchar(50) NOT NULL DEFAULT '',
  sub_id varchar(50) NOT NULL DEFAULT '',
  state int(10) NOT NULL DEFAULT 0,
  start_date datetime DEFAULT NULL,
  state_date datetime DEFAULT NULL,
  reported tinyint(1) DEFAULT 0,
  PRIMARY KEY (id, sub_id, state)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 216
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE order_state (
  id int(5) NOT NULL,
  name varchar(50) DEFAULT NULL,
  runtime tinyint(1) DEFAULT 0,
  extra tinyint(1) DEFAULT 0,
  tech tinyint(1) DEFAULT 0,
  book_part int(5) DEFAULT 0,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 297
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE orders (
  id varchar(50) NOT NULL DEFAULT '',
  source int(7) NOT NULL DEFAULT 0,
  src_id varchar(50) NOT NULL DEFAULT '',
  src_date datetime DEFAULT NULL,
  data_ts varchar(20) DEFAULT NULL,
  state int(5) DEFAULT 0,
  state_date datetime DEFAULT NULL,
  ftp_folder varchar(50) DEFAULT NULL,
  local_folder varchar(50) DEFAULT NULL,
  fotos_num int(5) DEFAULT 0,
  sync int(11) DEFAULT 0,
  is_preload tinyint(1) DEFAULT 0,
  reported_state int(5) DEFAULT 0,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 170
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE roll (
  width int(5) NOT NULL,
  pixels int(5) NOT NULL DEFAULT 0,
  PRIMARY KEY (width)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 1820
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE sources_sync (
  id int(7) NOT NULL,
  sync int(11) NOT NULL DEFAULT 0,
  sync_date datetime DEFAULT NULL,
  sync_state tinyint(1) DEFAULT 0,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 2048
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE src_type_prop (
  id int(5) NOT NULL,
  name varchar(30) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 1638
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE tech_point (
  id int(5) NOT NULL AUTO_INCREMENT,
  tech_type int(5) DEFAULT NULL,
  name varchar(50) DEFAULT NULL,
  tech_typeOld int(5) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AUTO_INCREMENT = 13
AVG_ROW_LENGTH = 1260
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE tech_unit (
  order_id varchar(50) NOT NULL DEFAULT '',
  sub_id varchar(50) NOT NULL DEFAULT '',
  pg_id varchar(50) NOT NULL DEFAULT '',
  state int(5) NOT NULL,
  start_date datetime DEFAULT NULL,
  end_date datetime DEFAULT NULL,
  books int(5) DEFAULT 0,
  sheets int(5) DEFAULT 0,
  done int(5) DEFAULT 0,
  PRIMARY KEY (order_id, sub_id, pg_id, state)
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE tmp_orders (
  id varchar(50) NOT NULL DEFAULT '',
  source int(7) NOT NULL DEFAULT 0,
  src_id varchar(50) NOT NULL DEFAULT '',
  src_date datetime DEFAULT NULL,
  data_ts varchar(20) DEFAULT NULL,
  state int(5) DEFAULT 0,
  state_date datetime DEFAULT NULL,
  ftp_folder varchar(50) DEFAULT NULL,
  local_folder varchar(50) DEFAULT NULL,
  fotos_num int(5) DEFAULT 0,
  sync int(11) DEFAULT 0,
  reload tinyint(1) DEFAULT 0,
  is_new tinyint(1) DEFAULT 0,
  is_preload tinyint(1) DEFAULT 0,
  PRIMARY KEY (id),
  INDEX IDX_tmp_orders_source (source)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 100
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE tmpt_spy (
  row_num int(11) NOT NULL AUTO_INCREMENT,
  id varchar(50) DEFAULT '',
  sub_id varchar(50) DEFAULT '',
  state int(10) NOT NULL DEFAULT 0,
  start_date datetime DEFAULT NULL,
  state_date datetime DEFAULT NULL,
  lastDate datetime DEFAULT NULL,
  resetDate datetime DEFAULT NULL,
  reset tinyint(4) DEFAULT 0,
  book_type int(5) DEFAULT 0,
  delay int(10) DEFAULT 0,
  alias varchar(50) DEFAULT NULL,
  PRIMARY KEY (row_num)
)
ENGINE = MYISAM
AUTO_INCREMENT = 1
AVG_ROW_LENGTH = 216
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE week_days (
  id int(5) NOT NULL,
  name varchar(20) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 2340
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE attr_type (
  id int(5) NOT NULL AUTO_INCREMENT,
  attr_fml int(5) NOT NULL,
  name varchar(50) NOT NULL,
  field varchar(20) NOT NULL,
  list tinyint(1) NOT NULL DEFAULT 0,
  persist tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  CONSTRAINT FK_attr_type_attr_family_id FOREIGN KEY (attr_fml)
  REFERENCES attr_family (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 31
AVG_ROW_LENGTH = 655
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE book_synonym (
  id int(7) NOT NULL AUTO_INCREMENT,
  src_type int(7) NOT NULL,
  synonym varchar(50) NOT NULL DEFAULT '',
  book_type int(5) NOT NULL DEFAULT 0,
  is_horizontal tinyint(1) DEFAULT 0,
  synonym_type int(5) DEFAULT 0,
  fb_alias varchar(50) DEFAULT NULL,
  idOld int(7) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX UK_book_synonym (src_type, synonym, synonym_type),
  CONSTRAINT FK_book_synonym_book_synonym_type_id FOREIGN KEY (synonym_type)
  REFERENCES book_synonym_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_book_synonym_book_type_id FOREIGN KEY (book_type)
  REFERENCES book_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 284
AVG_ROW_LENGTH = 155
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE content_filter_alias (
  filter int(5) NOT NULL,
  alias int(11) NOT NULL,
  PRIMARY KEY (filter, alias),
  CONSTRAINT FK_content_filter_alias_content_filter_id FOREIGN KEY (filter)
  REFERENCES content_filter (id) ON DELETE CASCADE ON UPDATE RESTRICT
)
ENGINE = INNODB
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE layerset (
  id int(5) NOT NULL AUTO_INCREMENT,
  layerset_group int(5) NOT NULL DEFAULT 0,
  subset_type int(5) NOT NULL DEFAULT 0,
  name varchar(50) DEFAULT NULL,
  book_type int(5) DEFAULT 0,
  pdf tinyint(1) DEFAULT 0,
  interlayer_thickness int(5) DEFAULT 0,
  passover tinyint(1) DEFAULT 0,
  book_check_off tinyint(1) DEFAULT 0,
  epaper_check_off tinyint(1) DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT FK_layerset_layerset_group_id FOREIGN KEY (layerset_group)
  REFERENCES layerset_group (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_layerset_layerset_type_id FOREIGN KEY (subset_type)
  REFERENCES layerset_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 18
AVG_ROW_LENGTH = 963
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE print_group (
  id varchar(50) NOT NULL DEFAULT '',
  order_id varchar(50) NOT NULL,
  sub_id varchar(50) NOT NULL DEFAULT '',
  state int(5) DEFAULT NULL,
  state_date datetime DEFAULT NULL,
  width int(5) NOT NULL,
  height int(5) NOT NULL,
  paper int(5) DEFAULT 0,
  frame int(5) DEFAULT 0,
  correction int(5) DEFAULT 0,
  cutting int(5) DEFAULT 0,
  path varchar(100) DEFAULT '',
  file_num int(5) DEFAULT 0,
  destination int(5) DEFAULT NULL,
  book_type int(5) DEFAULT 0,
  book_part int(5) DEFAULT 0,
  book_num int(5) DEFAULT 0,
  sheet_num int(5) DEFAULT 0,
  is_pdf tinyint(1) DEFAULT 0,
  is_duplex tinyint(1) DEFAULT 0,
  is_reprint tinyint(1) DEFAULT 0,
  reprint_id varchar(50) DEFAULT NULL,
  prints int(10) DEFAULT 0,
  PRIMARY KEY (id),
  INDEX IDX_print_group_state (state),
  CONSTRAINT FK_print_group_orders_id FOREIGN KEY (order_id)
  REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 228
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE src_type (
  id int(5) NOT NULL AUTO_INCREMENT,
  loc_type int(5) NOT NULL DEFAULT 0,
  name varchar(50) DEFAULT NULL,
  state int(5) DEFAULT 0,
  book_part int(5) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT FK_src_type_book_part_id FOREIGN KEY (book_part)
  REFERENCES book_part (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_src_type_location_type_id FOREIGN KEY (loc_type)
  REFERENCES location_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 12
AVG_ROW_LENGTH = 712
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE srvc_type (
  id int(5) NOT NULL,
  loc_type int(5) NOT NULL,
  name varchar(30) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_srvc_type_location_type_id FOREIGN KEY (loc_type)
  REFERENCES location_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AVG_ROW_LENGTH = 4096
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE state_log (
  id int(10) NOT NULL AUTO_INCREMENT,
  order_id varchar(50) NOT NULL DEFAULT '',
  sub_id varchar(50) NOT NULL DEFAULT '',
  pg_id varchar(50) DEFAULT NULL,
  state int(5) NOT NULL DEFAULT 0,
  state_date datetime NOT NULL,
  comment varchar(250) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_state_log_orders_id FOREIGN KEY (order_id)
  REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 79974
AVG_ROW_LENGTH = 67
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE suborders (
  id varchar(50) NOT NULL DEFAULT '',
  order_id varchar(50) NOT NULL DEFAULT '',
  sub_id varchar(50) NOT NULL DEFAULT '',
  src_type int(5) NOT NULL DEFAULT 0,
  state int(5) DEFAULT 0,
  state_date datetime DEFAULT NULL,
  ftp_folder varchar(100) DEFAULT NULL,
  prt_qty int(5) DEFAULT 0,
  proj_type int(5) DEFAULT 1,
  PRIMARY KEY (order_id, sub_id),
  CONSTRAINT FK_suborders_orders_id FOREIGN KEY (order_id)
  REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 109
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE tech_log (
  id int(10) NOT NULL AUTO_INCREMENT,
  order_id varchar(50) NOT NULL DEFAULT '',
  sub_id varchar(50) NOT NULL DEFAULT '',
  print_group varchar(50) NOT NULL DEFAULT '',
  sheet int(5) NOT NULL DEFAULT 0,
  src_id int(7) NOT NULL,
  log_date datetime DEFAULT NULL,
  PRIMARY KEY (id),
  INDEX FK_tech_log_print_group_id (print_group),
  INDEX IDX_tech_log (src_id, log_date),
  CONSTRAINT FK_tech_log_orders_id FOREIGN KEY (order_id)
  REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 431162
AVG_ROW_LENGTH = 69
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE attr_json_map (
  src_type int(5) NOT NULL,
  attr_type int(5) NOT NULL,
  json_key varchar(50) NOT NULL,
  PRIMARY KEY (src_type, attr_type),
  CONSTRAINT FK_attr_json_map_attr_type_id FOREIGN KEY (attr_type)
  REFERENCES attr_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_attr_json_map_src_type_id FOREIGN KEY (src_type)
  REFERENCES src_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AVG_ROW_LENGTH = 819
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE attr_value (
  id int(5) NOT NULL AUTO_INCREMENT,
  attr_tp int(5) NOT NULL,
  value varchar(50) NOT NULL,
  locked tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  CONSTRAINT FK_attr_value_attr_type_id FOREIGN KEY (attr_tp)
  REFERENCES attr_type (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 36
AVG_ROW_LENGTH = 712
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE lab (
  id int(5) NOT NULL AUTO_INCREMENT,
  src_type int(5) NOT NULL,
  name varchar(50) DEFAULT NULL,
  hot varchar(100) DEFAULT NULL,
  hot_nfs varchar(100) DEFAULT NULL,
  queue_limit int(5) DEFAULT 0,
  is_active tinyint(1) DEFAULT 1,
  is_managed tinyint(1) DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT FK_lab_src_type_id FOREIGN KEY (src_type)
  REFERENCES src_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 19
AVG_ROW_LENGTH = 1820
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE layer_sequence (
  layerset int(5) NOT NULL,
  mode int(5) DEFAULT 0,
  layer_group int(5) NOT NULL,
  seqorder int(5) NOT NULL,
  seqlayer int(5) NOT NULL,
  PRIMARY KEY (layerset, layer_group, seqorder),
  CONSTRAINT FK_layer_sequence_layer_group_id FOREIGN KEY (layer_group)
  REFERENCES layer_group (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_layer_sequence_layer_id FOREIGN KEY (seqlayer)
  REFERENCES layer (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_layer_sequence_layerset_id FOREIGN KEY (layerset)
  REFERENCES layerset (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AVG_ROW_LENGTH = 372
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE layerset_synonym (
  id int(7) NOT NULL AUTO_INCREMENT,
  item_id int(5) NOT NULL,
  synonym varchar(50) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_layerset_synonym_layerset_id FOREIGN KEY (item_id)
  REFERENCES layerset (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 7
AVG_ROW_LENGTH = 3276
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE print_group_file (
  id int(10) NOT NULL AUTO_INCREMENT,
  print_group varchar(50) NOT NULL DEFAULT '',
  file_name varchar(100) DEFAULT '',
  prt_qty int(5) DEFAULT 0,
  book_num int(5) DEFAULT 0,
  page_num int(5) DEFAULT 0,
  caption varchar(100) DEFAULT '',
  PRIMARY KEY (id),
  CONSTRAINT FK_print_group_file_print_group_id FOREIGN KEY (print_group)
  REFERENCES print_group (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 2081448
AVG_ROW_LENGTH = 87
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE sources (
  id int(7) NOT NULL AUTO_INCREMENT,
  name varchar(50) DEFAULT 'Наименование',
  type int(5) NOT NULL,
  online tinyint(1) NOT NULL DEFAULT 0,
  code char(1) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_sources_src_type_id FOREIGN KEY (type)
  REFERENCES src_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 21
AVG_ROW_LENGTH = 780
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE src_type_prop_val (
  id int(5) NOT NULL AUTO_INCREMENT,
  src_type int(5) NOT NULL,
  src_type_prop int(5) NOT NULL,
  value varchar(100) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_src_type_prop_val_src_type_id FOREIGN KEY (src_type)
  REFERENCES src_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_src_type_prop_val_src_type_prop_id FOREIGN KEY (src_type_prop)
  REFERENCES src_type_prop (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 17
AVG_ROW_LENGTH = 1024
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE suborders_template (
  id int(5) NOT NULL AUTO_INCREMENT,
  src_type int(5) NOT NULL,
  sub_src_type int(5) NOT NULL,
  folder varchar(50) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_suborders_template_src_type_id FOREIGN KEY (src_type)
  REFERENCES src_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_suborders_template_sub_src_type_id FOREIGN KEY (sub_src_type)
  REFERENCES src_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 2
AVG_ROW_LENGTH = 16384
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE attr_synonym (
  id int(7) NOT NULL AUTO_INCREMENT,
  src_type int(5) NOT NULL,
  attr_val int(5) NOT NULL,
  synonym varchar(50) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_attr_synonym_attr_value_id FOREIGN KEY (attr_val)
  REFERENCES attr_value (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_attr_synonym_src_type_id FOREIGN KEY (src_type)
  REFERENCES src_type (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 90
AVG_ROW_LENGTH = 287
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE book_pg_template (
  id int(7) NOT NULL AUTO_INCREMENT,
  book int(7) NOT NULL,
  book_part int(5) NOT NULL,
  width int(5) NOT NULL,
  height int(5) NOT NULL,
  height_add int(5) DEFAULT 0,
  paper int(5) NOT NULL DEFAULT 0,
  frame int(5) NOT NULL DEFAULT 0,
  correction int(5) NOT NULL DEFAULT 0,
  cutting int(5) NOT NULL DEFAULT 0,
  is_duplex tinyint(1) NOT NULL DEFAULT 0,
  is_pdf tinyint(1) NOT NULL DEFAULT 0,
  is_sheet_ready tinyint(1) DEFAULT 0,
  sheet_width int(5) DEFAULT NULL,
  sheet_len int(5) DEFAULT NULL,
  page_width int(5) DEFAULT NULL,
  page_len int(5) DEFAULT NULL,
  page_hoffset int(5) DEFAULT 0,
  font_size int(5) DEFAULT 0,
  font_offset varchar(10) DEFAULT '+500+0',
  fontv_size int(5) DEFAULT 0,
  fontv_offset varchar(10) DEFAULT '+0+500',
  notching int(5) DEFAULT 0,
  stroke int(5) DEFAULT 0,
  bar_size int(5) DEFAULT 0,
  bar_offset varchar(10) DEFAULT '+0+0',
  tech_bar int(5) DEFAULT 0,
  tech_add int(5) DEFAULT 4,
  tech_bar_step float(5, 2) DEFAULT 4.00,
  tech_bar_color varchar(6) DEFAULT '200000',
  is_tech_center tinyint(1) DEFAULT 1,
  tech_bar_offset varchar(10) DEFAULT '+0-200',
  is_tech_top tinyint(1) DEFAULT 0,
  tech_bar_toffset varchar(10) DEFAULT '+0+0',
  is_tech_bot tinyint(1) DEFAULT 0,
  tech_bar_boffset varchar(10) DEFAULT '+0+0',
  backprint int(5) DEFAULT 0,
  tech_stair_add int(5) DEFAULT 0,
  tech_stair_step int(5) DEFAULT 0,
  is_tech_stair_top tinyint(1) DEFAULT 0,
  is_tech_stair_bot tinyint(1) DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE INDEX UK_book_pg_template (book, book_part),
  CONSTRAINT FK_book_pg_template_attr_value_id FOREIGN KEY (paper)
  REFERENCES attr_value (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_book_pg_template_book_part_id FOREIGN KEY (book_part)
  REFERENCES book_part (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_book_pg_template_book_synonym_id FOREIGN KEY (book)
  REFERENCES book_synonym (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 474
AVG_ROW_LENGTH = 267
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE lab_device (
  id int(7) NOT NULL AUTO_INCREMENT,
  lab int(5) NOT NULL,
  name varchar(50) DEFAULT NULL,
  speed1 float(5, 2) DEFAULT 0.00,
  speed2 float(5, 2) DEFAULT 0.00,
  tech_point int(5) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_lab_device_lab_id FOREIGN KEY (lab)
  REFERENCES lab (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_lab_device_tech_point_id FOREIGN KEY (tech_point)
  REFERENCES tech_point (id) ON DELETE SET NULL ON UPDATE CASCADE
)
ENGINE = INNODB
AUTO_INCREMENT = 11
AVG_ROW_LENGTH = 2048
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE lab_print_code (
  id int(7) NOT NULL AUTO_INCREMENT,
  src_type int(5) NOT NULL,
  src_id int(5) DEFAULT 0,
  prt_code varchar(20) NOT NULL DEFAULT '',
  width int(5) NOT NULL,
  height int(5) NOT NULL,
  paper int(5) NOT NULL,
  frame int(5) NOT NULL,
  correction int(5) NOT NULL,
  cutting int(5) NOT NULL,
  is_duplex tinyint(1) DEFAULT 0,
  is_pdf tinyint(1) DEFAULT 0,
  roll int(5) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_lab_print_code_attr_value_corr FOREIGN KEY (correction)
  REFERENCES attr_value (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_lab_print_code_attr_value_cut FOREIGN KEY (cutting)
  REFERENCES attr_value (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_lab_print_code_attr_value_frame FOREIGN KEY (frame)
  REFERENCES attr_value (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_lab_print_code_attr_value_paper FOREIGN KEY (paper)
  REFERENCES attr_value (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_lab_print_code_src_type_id FOREIGN KEY (src_type)
  REFERENCES src_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AUTO_INCREMENT = 2246
AVG_ROW_LENGTH = 80
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE services (
  src_id int(7) NOT NULL,
  srvc_id int(5) NOT NULL,
  url varchar(250) DEFAULT NULL,
  user varchar(50) DEFAULT NULL,
  pass varchar(50) DEFAULT NULL,
  connections int(5) DEFAULT 3,
  PRIMARY KEY (src_id, srvc_id),
  CONSTRAINT FK_services_sources_id FOREIGN KEY (src_id)
  REFERENCES sources (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_services_srvc_type_id FOREIGN KEY (srvc_id)
  REFERENCES srvc_type (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AVG_ROW_LENGTH = 356
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE lab_rolls (
  lab_device int(7) NOT NULL,
  width int(5) NOT NULL,
  paper int(5) NOT NULL,
  len_std int(7) DEFAULT 0,
  len int(7) DEFAULT 0,
  is_online tinyint(1) DEFAULT 0,
  PRIMARY KEY (lab_device, width, paper),
  CONSTRAINT FK_lab_rolls_attr_value_id FOREIGN KEY (paper)
  REFERENCES attr_value (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT FK_lab_rolls_lab_device_id FOREIGN KEY (lab_device)
  REFERENCES lab_device (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_lab_rolls_roll_width FOREIGN KEY (width)
  REFERENCES roll (width) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AVG_ROW_LENGTH = 585
CHARACTER SET utf8
COLLATE utf8_general_ci;

CREATE TABLE lab_timetable (
  lab_device int(7) NOT NULL,
  day_id int(5) NOT NULL,
  time_from datetime NOT NULL DEFAULT '2000-01-01 00:00:00',
  time_to datetime NOT NULL DEFAULT '2000-01-01 00:00:00',
  is_online tinyint(1) DEFAULT 0,
  PRIMARY KEY (lab_device, day_id),
  CONSTRAINT FK_lab_timetable_lab_device_id FOREIGN KEY (lab_device)
  REFERENCES lab_device (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FK_lab_timetable_week_days_id FOREIGN KEY (day_id)
  REFERENCES week_days (id) ON DELETE RESTRICT ON UPDATE RESTRICT
)
ENGINE = INNODB
AVG_ROW_LENGTH = 292
CHARACTER SET utf8
COLLATE utf8_general_ci;

DELIMITER $$

CREATE PROCEDURE extraStateProlong (IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pComment varchar(250))
MODIFIES SQL DATA
BEGIN
  INSERT IGNORE INTO order_exstate_prolong
  (id, sub_id, state, state_date, comment)
    VALUES (pOrder, pSubOrder, pState, NOW(), pComment);
END
$$

CREATE PROCEDURE extraStateReset (IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int)
BEGIN
  DECLARE vMinExtraState int;
  DECLARE vNow datetime DEFAULT NOW();

  -- clear
  DELETE
    FROM order_extra_state
  WHERE id = pOrder
    AND sub_id = pSubOrder
    AND state = pState;

  IF pSubOrder = '' THEN
    -- no suborders
    SELECT IFNULL(MIN(oes.state), 210) INTO vMinExtraState
    FROM order_extra_state oes
    WHERE oes.id = pOrder
    AND oes.state_date IS NOT NULL;
    UPDATE orders o
    SET o.state = vMinExtraState,
        o.state_date = vNow
    WHERE o.id = pOrder
    AND o.state > 210
    AND o.state != vMinExtraState; -- 210 > Print post???
  ELSE
    -- set suborder state
    SELECT IFNULL(MIN(oes.state), 210) INTO vMinExtraState
    FROM order_extra_state oes
    WHERE oes.id = pOrder
    AND oes.sub_id = pSubOrder
    AND oes.state_date IS NOT NULL;
    UPDATE suborders o
    SET o.state = vMinExtraState,
        o.state_date = vNow
    WHERE o.order_id = pOrder
    AND o.sub_id = pSubOrder
    AND o.state > 210
    AND o.state != vMinExtraState;
    -- set order extrastate
    SELECT IFNULL(MIN(oes.state), 210) INTO vMinExtraState
    FROM suborders so
      LEFT OUTER JOIN order_extra_state oes ON oes.id = so.order_id
        AND so.sub_id = oes.sub_id
        AND oes.state_date IS NOT NULL
    WHERE so.order_id = pOrder;
    -- del order extra state
    DELETE
      FROM order_extra_state
    WHERE id = pOrder
      AND sub_id = ''
      AND state > vMinExtraState;
    -- set order state
    UPDATE orders o
    SET o.state = vMinExtraState,
        o.state_date = vNow
    WHERE o.id = pOrder
    AND o.state > 210
    AND o.state != vMinExtraState; -- 210 > Print ???
  END IF;
END
$$

CREATE PROCEDURE extraStateSet (IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pDate datetime)
MODIFIES SQL DATA
BEGIN
  DECLARE vMinExtraState int;
  DECLARE vBookPart int;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vBookPart = -1;

  IF pDate IS NULL THEN
    SET pDate = NOW();
  END IF;

  -- fix
  INSERT INTO order_extra_state
  (id, sub_id, state, start_date, state_date)
    VALUES (pOrder, pSubOrder, pState, pDate, pDate)
  ON DUPLICATE KEY UPDATE state_date = pDate;

  SELECT os.book_part INTO vBookPart
  FROM order_state os
  WHERE os.id = pState;

  IF pSubOrder = '' THEN
    -- set order state
    UPDATE orders o
    SET o.state = pState,
        o.state_date = pDate
    WHERE o.id = pOrder;
  ELSE
    -- set suborder state
    UPDATE suborders s
    SET s.state = pState,
        s.state_date = pDate
    WHERE s.order_id = pOrder
    AND s.sub_id = pSubOrder;

    IF vBookPart = 0 THEN
      -- common state, check update order state
      -- start order
      INSERT IGNORE INTO order_extra_state
      (id, sub_id, state, start_date)
        VALUES (pOrder, '', pState, pDate);
      -- forvard order extra state check suborders
      SELECT IFNULL(MIN(oes.state), 0) INTO vMinExtraState
      FROM suborders so
        LEFT OUTER JOIN order_extra_state oes ON oes.id = so.order_id
          AND so.sub_id = oes.sub_id
          AND oes.state_date IS NOT NULL
      WHERE so.order_id = pOrder;
      IF vMinExtraState > 0 THEN
        -- set order extra state
        -- update IF started
        UPDATE order_extra_state
        SET state_date = pDate
        WHERE id = pOrder
        AND sub_id = ''
        AND state = vMinExtraState
        AND state_date IS NULL;
        -- insert if not exists
        INSERT IGNORE INTO order_extra_state
        (id, sub_id, state, start_date, state_date)
          VALUES (pOrder, '', vMinExtraState, pDate, pDate);
        -- set order state
        UPDATE orders o
        SET o.state = vMinExtraState,
            o.state_date = pDate
        WHERE o.id = pOrder
        AND o.state > 209
        AND o.state != vMinExtraState; -- 210 > Print post
      END IF;
    END IF;
  END IF;

  -- update pg state
  IF vBookPart > 0 THEN
    UPDATE print_group pg
    SET pg.state = pState,
        pg.state_date = pDate
    WHERE pg.order_id = pOrder
    AND pg.sub_id = pSubOrder
    AND pg.book_part = vBookPart;
  END IF;

END
$$

CREATE PROCEDURE extraStateSetByPGroup (IN pPrintGroup varchar(50), IN pState int)
MODIFIES SQL DATA
COMMENT 'dont use 4 print states'
BEGIN
  DECLARE vOrder varchar(50);
  DECLARE vSubOrder varchar(50);

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrder = NULL;

  SELECT pg.order_id, pg.sub_id INTO vOrder, vSubOrder
  FROM print_group pg
  WHERE pg.id = pPrintGroup;

  IF vOrder IS NOT NULL THEN
    UPDATE print_group pg
    SET pg.state = vState,
        pg.state_date = NOW()
    WHERE pg.id = pPrintGroup;

    CALL extraStateSet(vOrder, vSubOrder, pTechPoint);
  END IF;

END
$$

CREATE PROCEDURE extraStateStart (IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pDate datetime)
MODIFIES SQL DATA
BEGIN
  IF pDate IS NULL THEN
    SET pDate = NOW();
  END IF;
  INSERT IGNORE INTO order_extra_state
  (id, sub_id, state, start_date)
    VALUES (pOrder, pSubOrder, pState, pDate);
  IF pSubOrder != '' THEN
    -- start order
    INSERT IGNORE INTO order_extra_state
    (id, sub_id, state, start_date)
      VALUES (pOrder, '', pState, pDate);
  END IF;
END
$$

CREATE PROCEDURE loadSpy (IN pDate datetime, IN pFromState int, IN pToState int, IN pBookPart int)
BEGIN
  -- create temp
  CREATE TEMPORARY TABLE IF NOT EXISTS tmp_spy LIKE tmpt_spy;
  -- get last states by condition
  INSERT INTO tmp_spy
  (id, sub_id, state, start_date, state_date, lastDate)
    SELECT t.id, t.sub_id, t.state, t.start_date, t.state_date, t.lastDt
    FROM
    (SELECT oes.id, oes.sub_id, oes.state, oes.start_date, oes.state_date, IFNULL(oes.state_date, oes.start_date) lastDt
      FROM order_extra_state oes
        INNER JOIN order_state os ON oes.state = os.id
      WHERE oes.state BETWEEN pFromState AND pToState
      AND (pBookPart = 0
      OR os.book_part IN (0, pBookPart))
      AND NOT EXISTS
      (SELECT 1
        FROM order_extra_state oes1
        WHERE oes.id = oes1.id
        AND oes.sub_id = oes1.sub_id
        AND oes1.state = 450)
      ORDER BY IFNULL(oes.state_date, oes.start_date) DESC) t
    GROUP BY t.id, t.sub_id;
  -- remove by date
  DELETE
    FROM tmp_spy
  WHERE lastDate >= pDate;
  -- remove photo
  DELETE
    FROM tmp_spy
  WHERE NOT EXISTS
    (SELECT 1
      FROM print_group pg
      WHERE pg.order_id = tmp_spy.id
      AND pg.sub_id = tmp_spy.sub_id
      AND pg.book_type > 0);
  -- set reset date
  UPDATE tmp_spy
  SET resetDate =
  (SELECT MAX(oep.state_date)
    FROM order_exstate_prolong oep
    WHERE oep.id = tmp_spy.id
    AND oep.sub_id = tmp_spy.sub_id
    AND oep.state = tmp_spy.state)
  WHERE EXISTS
  (SELECT 1
    FROM order_exstate_prolong oep
    WHERE oep.id = tmp_spy.id
    AND oep.sub_id = tmp_spy.sub_id
    AND oep.state = tmp_spy.state);
  UPDATE tmp_spy
  SET reset = 1
  WHERE resetDate >= pDate;
  -- set book type
  UPDATE tmp_spy t
  SET book_type =
  (SELECT MAX(pg.book_type)
    FROM print_group pg
    WHERE pg.order_id = t.id
    AND pg.sub_id = t.sub_id);
  -- set book alias
  UPDATE tmp_spy t
  SET alias =
  (SELECT MAX(pg.path)
    FROM print_group pg
    WHERE pg.order_id = t.id
    AND pg.sub_id = t.sub_id)
  WHERE t.sub_id = '';
  -- calc delay in hours
  UPDATE tmp_spy
  SET delay = TIMESTAMPDIFF(HOUR, lastDate, NOW());

  -- result select
  SELECT t.id, t.sub_id, t.state, t.start_date, t.state_date, t.lastDate, t.resetDate, t.reset, t.book_type, t.delay, t.alias, os.name op_name, os.book_part, bp.name bp_name, bt.name bt_name
  FROM tmp_spy t
    INNER JOIN order_state os ON t.state = os.id
    INNER JOIN book_part bp ON os.book_part = bp.id
    INNER JOIN book_type bt ON bt.id = t.book_type;
  -- kill temp
  DROP TEMPORARY TABLE tmp_spy;
END
$$

CREATE PROCEDURE logStateByPg (IN pPgId varchar(50), IN pSate int, IN pMsg varchar(250))
MODIFIES SQL DATA
BEGIN
  INSERT INTO state_log
  (order_id, sub_id, pg_id, state, state_date, comment)
    SELECT pg.order_id, pg.sub_id, pg.id, pSate, NOW(), pMsg
    FROM print_group pg
    WHERE pg.id = pPgId;
END
$$

CREATE PROCEDURE orderCancel (IN pId varchar(50))
MODIFIES SQL DATA
BEGIN
  DECLARE vDate datetime DEFAULT NOW();

  UPDATE orders o
  SET o.state = 510,
      o.state_date = vDate
  WHERE o.id = pId;

  UPDATE print_group pg
  SET pg.state = 510,
      pg.state_date = vDate
  WHERE pg.order_id = pId;

  UPDATE suborders s
  SET s.state = 510,
      s.state_date = vDate
  WHERE s.order_id = pId;

END
$$

CREATE PROCEDURE orderCleanUp (IN pId varchar(50))
MODIFIES SQL DATA
BEGIN
  DECLARE vDate datetime DEFAULT NOW();

  DELETE
    FROM print_group
  WHERE order_id = pId;
  DELETE
    FROM order_extra_info
  WHERE id = pId;
  DELETE
    FROM suborders
  WHERE order_id = pId;
  UPDATE orders
  SET state = 100,
      state_date = vDate
  WHERE id = pId;
  INSERT INTO state_log
  (order_id, state, state_date, comment)
    VALUES (pId, 100, vDate, 'reset');
END
$$

CREATE PROCEDURE printMarkInPrint (IN pPgroupId varchar(50))
MODIFIES SQL DATA
BEGIN
  DECLARE vPgId varchar(50);

  SET vPgId = printPg2Reprint(pPgroupId);
  UPDATE print_group pg
  SET pg.state = 255,
      pg.state_date = NOW()
  WHERE pg.id = vPgId
  AND pg.state != 255;
END
$$

CREATE PROCEDURE printStateCancel (IN pPgroupId varchar(50))
MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  UPDATE print_group pg
  SET pg.state = 215,
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

CREATE PROCEDURE printStateEnd (IN pPgroupId varchar(50))
MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vParent varchar(50);
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  UPDATE print_group pg
  SET pg.state = 300,
      pg.state_date = NOW()
  WHERE pg.id = pPgroupId;

  SELECT pg.order_id, pg.sub_id, pg.reprint_id INTO vOrderId, vSubId, vParent
  FROM print_group pg
  WHERE pg.id = pPgroupId;

  IF vOrderId IS NOT NULL THEN
    -- check reprint
    IF vParent IS NOT NULL THEN
      UPDATE print_group pg
      SET pg.state = 300,
          pg.state_date = NOW()
      WHERE pg.id = vParent;
    END IF;
    -- chek if all pg started
    SELECT IFNULL(MIN(pg.state), 0) INTO vMinState
    FROM print_group pg
    WHERE pg.order_id = vOrderId
    AND pg.sub_id = vSubId;
    IF vMinState >= 300 THEN
      -- all pg printed end order/suborder
      CALL extraStateSet(vOrderId, vSubId, 300, NOW());
    ELSE
      -- start order/suborder, 4 manual set printed
      CALL extraStateStart(vOrderId, vSubId, 300, NOW());
    END IF;

  END IF;

END
$$

CREATE PROCEDURE printStateStart (IN pPgroupId varchar(50), IN lab int)
MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vMinState int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  UPDATE print_group pg
  SET pg.state = 250,
      pg.state_date = NOW(),
      pg.destination = lab
  WHERE pg.id = pPgroupId
  AND pg.state != 250;

  SELECT pg.order_id, pg.sub_id INTO vOrderId, vSubId
  FROM print_group pg
  WHERE pg.id = pPgroupId;

  IF vOrderId IS NOT NULL THEN
    -- rise order/suborder state
    UPDATE orders o
    SET o.state = 250,
        o.state_date = NOW()
    WHERE o.id = vOrderId
    AND o.state < 250;
    IF vSubId != '' THEN
      UPDATE suborders o
      SET o.state = 250,
          o.state_date = NOW()
      WHERE o.id = vOrderId
      AND o.sub_id = vSubId
      AND o.state < 250;
    END IF;

    -- chek if all pg started
    SELECT IFNULL(MIN(pg.state), 0) INTO vMinState
    FROM print_group pg
    WHERE pg.order_id = vOrderId
    AND pg.sub_id = vSubId;
    IF vMinState >= 250 THEN
      -- all pg started end order/suborder
      CALL extraStateSet(vOrderId, vSubId, 210, NOW());
    ELSE
      -- start order/suborder
      CALL extraStateStart(vOrderId, vSubId, 210, NOW());
    END IF;

  END IF;
END
$$

CREATE PROCEDURE sync ()
MODIFIES SQL DATA
COMMENT 'синхронизация всех активных сайтов'
BEGIN
  DECLARE vId integer(7) DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);
  DECLARE vCur CURSOR FOR
  SELECT s.id
  FROM sources s
  WHERE s.online = 1;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vId;
    IF vIsEnd THEN
      LEAVE wet;
    END IF;
    CALL syncSource(vId);
  END LOOP wet;
  CLOSE vCur;

END
$$

CREATE PROCEDURE syncSource (IN pSourceId int)
MODIFIES SQL DATA
main:
BEGIN
  DECLARE vSync integer(11) DEFAULT (0);
  DECLARE vCnt integer(11) DEFAULT (0);

  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vSync = 0;

  IF NOT EXISTS
    (SELECT 1
      FROM tmp_orders t
      WHERE t.source = pSourceId) THEN
    LEAVE main;
  END IF;

  -- reset sync state
  UPDATE sources_sync ss
  SET ss.sync_date = NOW(),
      ss.sync_state = 0
  WHERE ss.id = pSourceId;

  -- keep in transaction
  START TRANSACTION;

    -- get next sync
    SELECT ss.sync INTO vSync
    FROM sources_sync ss
    WHERE ss.id = pSourceId;
    SET vSync = IFNULL(vSync, 0) + 1;

    -- set sync
    UPDATE tmp_orders to1
    SET to1.sync = vSync
    WHERE to1.source = pSourceId;

    -- add new
    -- search new
    UPDATE tmp_orders to1
    SET to1.is_new = IFNULL(
    (SELECT 0
      FROM orders o
      WHERE o.id = to1.id), 1)
    WHERE to1.source = pSourceId;
    -- insert new
    INSERT INTO orders
    (id, source, src_id, src_date, state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts)
      SELECT id, source, src_id, src_date, state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts
      FROM tmp_orders to1
      WHERE to1.source = pSourceId
      AND to1.is_new = 1;
    -- remove new
    DELETE
      FROM tmp_orders
    WHERE source = pSourceId
      AND is_new = 1;

    -- update sync
    UPDATE orders
    SET sync = vSync
    WHERE orders.id IN
    (SELECT id
      FROM tmp_orders to1
      WHERE to1.source = pSourceId);

    -- check/process preload
    -- update printgroup 
    UPDATE print_group
    SET state = 200,
        state_date = NOW()
    WHERE state = 199
    AND order_id IN
    (SELECT t.id
      FROM tmp_orders t
      WHERE t.source = pSourceId
      AND t.is_preload = 0);
    -- suborders
    UPDATE suborders s
    SET s.state = 200,
        s.state_date = NOW()
    WHERE s.state = 199
    AND s.order_id IN
    (SELECT t.id
      FROM tmp_orders t
      WHERE t.source = pSourceId
      AND t.is_preload = 0);
    -- orders
    UPDATE orders o
    SET state = 200,
        state_date = NOW(),
        is_preload = 0
    WHERE o.state = 199
    AND o.id IN
    (SELECT t.id
      FROM tmp_orders t
      WHERE t.source = pSourceId
      AND t.is_preload = 0);

    -- cancel not in sync
    -- cancel print groups
    UPDATE print_group
    SET state = 510,
        state_date = NOW()
    WHERE order_id IN
    (SELECT id
      FROM orders o
      WHERE o.source = pSourceId
      AND o.state BETWEEN 100 AND 200
      AND o.sync != vSync);
    -- cancel suborders
    UPDATE suborders s
    SET s.state = 510,
        s.state_date = NOW()
    WHERE s.order_id IN
    (SELECT id
      FROM orders o
      WHERE o.source = pSourceId
      AND o.state BETWEEN 100 AND 200
      AND o.sync != vSync);
    -- cancel orders
    UPDATE orders o
    SET state = 510,
        state_date = NOW()
    WHERE o.source = pSourceId
    AND o.state BETWEEN 100 AND 200
    AND o.sync != vSync;

    -- finde reload candidate by project data time
    UPDATE tmp_orders t
    SET t.reload = 1
    WHERE t.source = pSourceId
    AND t.data_ts IS NOT NULL
    AND EXISTS
    (SELECT 1
      FROM orders o
      WHERE o.id = t.id
      AND o.data_ts IS NOT NULL
      AND o.data_ts != o.data_ts
      AND o.state BETWEEN 199 AND 200);
    -- clean orders 4 reload
    -- clean print_group
    DELETE
      FROM print_group
    WHERE order_id IN
      (SELECT id
        FROM tmp_orders t
        WHERE t.source = pSourceId
        AND t.reload = 1);
    -- clean extra info
    DELETE
      FROM order_extra_info
    WHERE id IN
      (SELECT id
        FROM tmp_orders t
        WHERE t.source = pSourceId
        AND t.reload = 1);
    -- clean suborder
    DELETE
      FROM suborders
    WHERE order_id IN
      (SELECT id
        FROM tmp_orders t
        WHERE t.source = pSourceId
        AND t.reload = 1);
    -- reset order state
    UPDATE orders o
    SET o.state = 100,
        o.state_date = NOW()
    WHERE o.id IN
    (SELECT id
      FROM tmp_orders t
      WHERE t.source = pSourceId
      AND t.reload = 1);
    -- set project data time
    UPDATE orders o
    SET o.data_ts =
    (SELECT tt.data_ts
      FROM tmp_orders tt
      WHERE tt.id = o.id)
    WHERE o.source = pSourceId
    AND EXISTS
    (SELECT 1
      FROM tmp_orders t
      WHERE t.id = o.id
      AND t.data_ts IS NOT NULL
      AND t.data_ts != IFNULL(o.data_ts, ''));

    -- finalize
    DELETE
      FROM tmp_orders
    WHERE source = pSourceId;
    INSERT INTO sources_sync
    (id, sync, sync_date, sync_state)
      VALUES (pSourceId, vSync, NOW(), 1)
    ON DUPLICATE KEY UPDATE sync = vSync, sync_date = NOW(), sync_state = 1;
  COMMIT;
END
$$

CREATE PROCEDURE techLogPg (IN pPgroup varchar(50), IN pSheet int, IN pTechPoint int, IN pDate datetime)
MODIFIES SQL DATA
BEGIN
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vState int;
  DECLARE vBooks int;
  DECLARE vSheets int;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrderId = NULL;

  SELECT pg.order_id, pg.sub_id, pg.book_num, pg.sheet_num INTO vOrderId, vSubId, vBooks, vSheets
  FROM print_group pg
  WHERE pg.id = pPgroup;

  SELECT tp.tech_type INTO vState
  FROM tech_point tp
  WHERE tp.id = pTechPoint;

  IF vOrderId IS NOT NULL THEN
    IF vState <= 300 THEN
      -- may be reprint
      SET pPgroup = printPg2Reprint(pPgroup);
    END IF;
    -- log
    INSERT INTO tech_log
    (order_id, sub_id, print_group, sheet, src_id, log_date)
      VALUES (vOrderId, vSubId, pPgroup, pSheet, pTechPoint, pDate);
    -- check
    CALL techUnitCalc(vOrderId, vSubId, pPgroup, vState, vBooks, vSheets);
  END IF;

END
$$

CREATE PROCEDURE techUnitCalc (IN pOrder varchar(50), IN pSubOrder varchar(50), IN pPgroup varchar(50), IN pState int, IN pBooks int, IN pSheets int)
MODIFIES SQL DATA
BEGIN
  DECLARE vDone int;
  DECLARE vStart datetime;
  DECLARE vEnd datetime;
  DECLARE vCount int;
  DECLARE vMinState int;


  IF pPgroup IS NULL THEN
    SET pPgroup = '';
  END IF;
  IF pSubOrder IS NULL THEN
    SET pSubOrder = '';
  END IF;

  SELECT IFNULL(COUNT(DISTINCT tl.sheet), 0), IFNULL(MIN(tl.log_date), NOW()), IFNULL(MAX(tl.log_date), NOW()) INTO vDone, vStart, vEnd
  FROM tech_log tl
    INNER JOIN tech_point tp ON tl.src_id = tp.id
  WHERE tl.order_id = pOrder
  AND tl.sub_id = pSubOrder
  AND tl.print_group = pPgroup
  AND tp.tech_type = pState;
  /*
  INSERT INTO tech_unit (order_id, sub_id, pg_id, state, start_date, end_date, books, sheets, done)
                    VALUES( pOrder, pSubOrder, pPgroup, pState, vStart, vEnd, pBooks, pSheets, vDone)
    ON DUPLICATE KEY UPDATE start_date=vStart, end_date=vEnd, done=vDone;

  SELECT ROW_COUNT() INTO vCount;
  IF vCount =1 THEN
    -- inserted
    CALL extraStateStart(pOrder, pSubOrder, pState, vStart);
  END IF;
  */

  IF vDone = pBooks * pSheets
    OR (pState = 320
    AND vDone = pBooks * 2) THEN -- 320 - TECH_FOLDING (log first & end sheet per book)
    -- complited
    IF pPgroup != '' THEN
      UPDATE print_group pg
      SET pg.state = pState,
          pg.state_date = vEnd
      WHERE pg.id = pPgroup;
    END IF;
    IF pState = 300 THEN
      -- print state, check pringroups
      SELECT IFNULL(MIN(pg.state), 0) INTO vMinState
      FROM print_group pg
      WHERE pg.order_id = pOrder
      AND pg.sub_id = pSubOrder;
      IF vMinState >= 300 THEN
        -- all pg printed end order/suborder
        CALL extraStateSet(pOrder, pSubOrder, pState, vEnd);
      ELSE
        -- start
        CALL extraStateStart(pOrder, pSubOrder, pState, vStart);
      END IF;
    ELSE
      CALL extraStateSet(pOrder, pSubOrder, pState, vEnd);
    END IF;
  ELSE
    -- start
    CALL extraStateStart(pOrder, pSubOrder, pState, vStart);
  END IF;

END
$$

CREATE FUNCTION printPg2Reprint (pPgroupId varchar(50))
RETURNS varchar(50) charset utf8
READS SQL DATA
BEGIN
  DECLARE vPgId varchar(50);
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vPgId = pPgroupId;

  SELECT pg1.id INTO vPgId
  FROM print_group pg
    INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id
      AND pg.id = pg1.reprint_id
      AND pg1.state < 300
  WHERE pg.id = pPgroupId
  AND pg.is_reprint = 0
  LIMIT 1;


  RETURN vPgId;
END
$$

CREATE TRIGGER tg_orders_ai
AFTER INSERT
ON orders
FOR EACH ROW
BEGIN
  INSERT INTO state_log
  (order_id, state, state_date)
    VALUES (NEW.id, NEW.state, NOW());
END
$$

CREATE TRIGGER tg_orders_au
AFTER UPDATE
ON orders
FOR EACH ROW
BEGIN
  IF NOT (OLD.state <=> NEW.state) THEN
    INSERT INTO state_log
    (order_id, state, state_date)
      VALUES (NEW.id, NEW.state, NOW());
  END IF;
END
$$

CREATE TRIGGER tg_print_group_ai
AFTER INSERT
ON print_group
FOR EACH ROW
BEGIN
  INSERT INTO state_log
  (order_id, sub_id, pg_id, state, state_date)
    VALUES (NEW.order_id, NEW.sub_id, NEW.id, NEW.state, NOW());
END
$$

CREATE TRIGGER tg_print_group_au
AFTER UPDATE
ON print_group
FOR EACH ROW
BEGIN
  IF NOT (OLD.state <=> NEW.state) THEN
    INSERT INTO state_log
    (order_id, sub_id, pg_id, state, state_date)
      VALUES (NEW.order_id, NEW.sub_id, NEW.id, NEW.state, NOW());
  END IF;
END
$$

CREATE TRIGGER tg_suborders_ai
AFTER INSERT
ON suborders
FOR EACH ROW
BEGIN
  INSERT INTO state_log
  (order_id, sub_id, state, state_date)
    VALUES (NEW.order_id, NEW.sub_id, NEW.state, NOW());
END
$$

CREATE TRIGGER tg_suborders_au
AFTER UPDATE
ON suborders
FOR EACH ROW
BEGIN
  IF NOT (OLD.state <=> NEW.state) THEN
    INSERT INTO state_log
    (order_id, sub_id, state, state_date)
      VALUES (NEW.order_id, NEW.sub_id, NEW.state, NOW());
  END IF;
END
$$

DELIMITER ;

CREATE OR REPLACE
VIEW suborderotkv
AS
SELECT `es`.`id` AS `order_id`, `es`.`sub_id` AS `sub_id`, `es`.`state` AS `state`, `es`.`start_date` AS `state_date`, COUNT(DISTINCT `tl`.`sheet`) AS `books_done`, ifnull(`s`.`prt_qty`, ifnull((SELECT MAX(`pg`.`book_num`) FROM `print_group` `pg` WHERE ((`pg`.`order_id` = `es`.`id`) AND (`pg`.`sub_id` = `es`.`sub_id`))), 0)) AS `prt_qty`
FROM ((((`order_extra_state` `es`
  LEFT JOIN `orders` `o` ON ((`o`.`id` = `es`.`id`)))
  LEFT JOIN `suborders` `s` ON (((`es`.`id` = `s`.`order_id`)
    AND (`es`.`sub_id` = `s`.`sub_id`))))
  LEFT JOIN `tech_point` `tp` ON ((`tp`.`tech_type` = `es`.`state`)))
  LEFT JOIN `tech_log` `tl` ON (((`tl`.`src_id` = `tp`.`id`)
    AND (`tl`.`order_id` = `es`.`id`)
    AND (`tl`.`sub_id` = `es`.`sub_id`)
    AND (`tl`.`sheet` <> 0))))
WHERE ((`es`.`state` = 450)
AND isnull(`es`.`state_date`))
GROUP BY `es`.`id`, `es`.`sub_id`
ORDER BY `es`.`start_date`;