-- main    
-- new main (virt)    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';


CREATE TABLE order_program (
  id int(5) NOT NULL,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB
AVG_ROW_LENGTH = 297
CHARACTER SET utf8
COLLATE utf8_general_ci;

INSERT INTO order_program(id, name) VALUES(0, '');
INSERT INTO order_program(id, name) VALUES(1, 'Свадебная');
INSERT INTO order_program(id, name) VALUES(2, 'Школьная');

ALTER TABLE book_synonym
  ADD COLUMN order_program INT(5) DEFAULT 0 AFTER idOld;

ALTER TABLE book_synonym
  ADD CONSTRAINT FK_book_synonym_order_program_id FOREIGN KEY (order_program)
    REFERENCES order_program(id) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE book_pg_template
  ADD COLUMN mark_size INT(5) DEFAULT 0 AFTER revers,
  ADD COLUMN mark_offset VARCHAR(10) DEFAULT '+0+0' AFTER mark_size;    

  -- main 2016.04.06

ALTER TABLE book_synonym_glue
  ADD COLUMN add_layers INT(5) DEFAULT 0 AFTER glue_cmd;
  
 -- main 2016.04.13
  