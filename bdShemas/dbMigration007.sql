-- main cycle 2015-02-18

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE book_pg_template
  ADD COLUMN lab_type INT(5) DEFAULT 0 AFTER is_tech_stair_bot;

ALTER TABLE book_synonym
  DROP COLUMN lab_type;