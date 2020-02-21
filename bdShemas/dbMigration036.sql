-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

-- combo orders

-- internal source type for composite orders
INSERT INTO src_type(id, loc_type, name, state, book_part) VALUES(26, 1, 'Internal', 0, 0);

-- composite type table
CREATE TABLE compo_type (
  id tinyint(4) NOT NULL,
  name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
)
ENGINE = INNODB,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

INSERT INTO compo_type(id, name) VALUES
(0, '-'),
(1, 'Часть комбо'),
(2, 'Комбо');

ALTER TABLE book_synonym 
  ADD COLUMN compo_type tinyint(4) NOT NULL DEFAULT 0;
ALTER TABLE book_synonym 
  ADD CONSTRAINT FK_book_synonym_compo_type FOREIGN KEY (compo_type)
    REFERENCES compo_type(id);
    
CREATE TABLE book_synonym_compo (
  parent INT(7) NOT NULL,
  child INT(7) NOT NULL,
  PRIMARY KEY (parent, child)
)
ENGINE = INNODB; 
    
DROP PROCEDURE IF EXISTS bookSynonymClone;

DELIMITER $$

CREATE
PROCEDURE bookSynonymClone (IN pId int)
MODIFIES SQL DATA
BEGIN
  DECLARE vNewId int DEFAULT 0;
  DECLARE vNewSynonym varchar(100);

  SELECT SUBSTR(CONCAT(bs.synonym, '(', COUNT(*), ')'), 1, 100)
  INTO vNewSynonym
    FROM book_synonym bs
      INNER JOIN book_synonym bs1 ON bs.src_type = bs1.src_type
        AND bs.synonym_type = bs1.synonym_type
        AND (bs.synonym = bs1.synonym
        OR bs1.synonym LIKE CONCAT(bs.synonym, '(%)'))
    WHERE bs.id = pId;

  IF vNewSynonym IS NOT NULL
  THEN
    -- create
    INSERT INTO book_synonym (src_typ, synonym, book_type, is_horizontal, synonym_type, has_backprint, order_program, compo_type)
      SELECT src_type, vNewSynonym, book_type, is_horizontal, synonym_type, has_backprint, order_program, compo_type
        FROM book_synonym
        WHERE id = pId;
    SET vNewId = LAST_INSERT_ID();
    -- clone templates
    INSERT INTO book_pg_template (book, book_part, width, height, height_add, paper, frame, correction, cutting, is_duplex, is_pdf, is_sheet_ready, sheet_width, sheet_len, page_width, page_len, page_hoffset, font_size, font_offset, fontv_size, fontv_offset, notching, stroke, bar_size, bar_offset, tech_bar, tech_add, tech_bar_step, tech_bar_color,
    is_tech_center, tech_bar_offset, is_tech_top, tech_bar_toffset, is_tech_bot, tech_bar_boffset, backprint, tech_stair_add, tech_stair_step, is_tech_stair_top, is_tech_stair_bot, lab_type, revers, mark_size, mark_offset, reprint_size, reprint_offset, queue_size, queue_offset, laminat)
      SELECT vNewId, book_part, width, height, height_add, paper, frame, correction, cutting, is_duplex, is_pdf, is_sheet_ready, sheet_width, sheet_len, page_width, page_len, page_hoffset, font_size, font_offset, fontv_size, fontv_offset, notching, stroke, bar_size, bar_offset, tech_bar, tech_add, tech_bar_step, tech_bar_color,
        is_tech_center, tech_bar_offset, is_tech_top, tech_bar_toffset, is_tech_bot, tech_bar_boffset, backprint, tech_stair_add, tech_stair_step, is_tech_stair_top, is_tech_stair_bot, lab_type, revers, mark_size, mark_offset, reprint_size, reprint_offset, queue_size, queue_offset, laminat
        FROM book_pg_template bpt
        WHERE bpt.book = pId;
  END IF;

  SELECT vNewId AS id;
END
$$

DELIMITER ;

ALTER TABLE print_group 
  ADD COLUMN compo_type tinyint(4) NOT NULL DEFAULT 0;
/*
  ALTER TABLE print_group 
  ADD CONSTRAINT FK_print_group_compo_type FOREIGN KEY (compo_type)
    REFERENCES compo_type(id);
    */

ALTER TABLE order_books 
  ADD COLUMN compo_type TINYINT(4) DEFAULT 0;
ALTER TABLE order_books 
  ADD COLUMN compo_pg VARCHAR(50) DEFAULT NULL COMMENT 'parent compo book, filled after this book included in compo book';
ALTER TABLE order_books 
  ADD COLUMN compo_book INT(5) DEFAULT NULL;
ALTER TABLE order_books 
  ADD INDEX IDX_order_books_compo(compo_pg, compo_book);
  
DROP PROCEDURE IF EXISTS fill_order;

DELIMITER $$

CREATE
PROCEDURE fill_order (IN p_orderid varchar(50))
MODIFIES SQL DATA
COMMENT 'post process order after fill'
BEGIN
  -- fill  books
  INSERT IGNORE INTO order_books (pg_id, target_pg, book, sheets, state, state_date)
    SELECT pg.id,
      pg.id,
      gk.n,
      pg.sheet_num,
      pg.state,
      NOW()
      FROM print_group pg
        INNER JOIN generator_1k gk ON gk.n BETWEEN 1 AND pg.book_num
      WHERE pg.order_id = p_orderid
        AND pg.is_reprint = 0;
END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS tech_calc_books;

DELIMITER $$

CREATE 
PROCEDURE tech_calc_books (IN p_pg varchar(50))
MODIFIES SQL DATA
BEGIN
  UPDATE order_books obb
  INNER JOIN (SELECT t.pg_id,
      t.book,
      MAX(t.state) state,
      MAX(t.state_date) state_date
      FROM (SELECT ob.pg_id,
          ob.book,
          ob.sheets,
          tp.tech_type state,
          MAX(tl.log_date) state_date
          FROM order_books ob
            INNER JOIN tech_log tl ON ob.pg_id = tl.print_group
              AND ob.book = (tl.sheet DIV 100)
            INNER JOIN tech_point tp ON tp.id = tl.src_id
          WHERE ob.pg_id = p_pg
            AND tp.tech_type > ob.state
          GROUP BY ob.pg_id,
            ob.book,
            tp.tech_type
          HAVING COUNT(DISTINCT tl.sheet) >= ob.sheets) t
      --        HAVING COUNT(DISTINCT tl.sheet) >= IF(tp.tech_type=320, 2, ob.sheets) t
      GROUP BY t.pg_id,
        t.book) tt
    ON tt.pg_id = obb.pg_id
    AND tt.book = obb.book
    SET obb.state = tt.state,
        obb.state_date = tt.state_date;

  -- update childs for compo
  UPDATE order_books obb
  INNER JOIN (SELECT ob.pg_id, ob.book, ob.state, ob.state_date
      FROM order_books ob
      WHERE ob.pg_id = p_pg
        AND ob.compo_type = 2) t
    ON obb.compo_pg = t.pg_id
    AND obb.compo_book = t.book
    AND obb.state < t.state
    SET obb.state = t.state,
        obb.state_date = t.state_date;
END
$$

DELIMITER ;