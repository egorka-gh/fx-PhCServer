-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(98, 6, 'Задолженость', 'debt_sum', 0, 1);
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 98, 'address.debt_sum');
-- 2019-11-14 applied on main cycle
    
DELIMITER $$

--
-- Создать функцию `num_to_text`
--
CREATE
FUNCTION num_to_text (orignum varchar(128))
RETURNS varchar(500) CHARSET utf8
NO SQL
BEGIN
  DECLARE origlen int(11);
  DECLARE iSot int(11);
  DECLARE r int(11);
  DECLARE i int(11);
  DECLARE c1Sot varchar(32);
  DECLARE c2Sot varchar(32);
  DECLARE c3Sot varchar(32);
  DECLARE rtext text;
  DECLARE esuff varchar(32);

  /*
  -- убираем все дробное чтоп работало при любой точности 
  IF LOCATE('.', orignum) > 0 THEN
    SET orignum = SUBSTRING(orignum, 1, LOCATE('.', orignum) - 1);
  END IF;
  IF LOCATE(',', orignum) > 0 THEN
    SET orignum = SUBSTRING(orignum, 1, LOCATE(',', orignum) - 1);
  END IF;
  */

  #последние три цифры в числе считаються копейками, в случае отсутствия разделителя добавляем 000
  IF LOCATE('.', orignum) > 0
    OR LOCATE(',', orignum) > 0
  THEN
    IF LENGTH(SUBSTRING(orignum, LOCATE('.', orignum))) = 2
      OR LENGTH(SUBSTRING(orignum, LOCATE(',', orignum))) = 2
    THEN
      SET orignum = REPLACE(orignum, '.', '0');
      SET orignum = REPLACE(orignum, ',', '0');
      SET orignum = CONCAT(orignum, '0');
    ELSE
      SET orignum = REPLACE(orignum, '.', '0');
      SET orignum = REPLACE(orignum, ',', '0');
    END IF;
  ELSE
    SET orignum = CONCAT(orignum, '000');
  END IF;

  SET origlen = (SELECT LENGTH(orignum));
  SET r = ROUND(origlen / 3 + 0.3);
  SET i = 0;
  -- SET i = 1; -- отключил копейки
  SET rtext = ' ';
  SET esuff = ' ';

  WHILE (i < r) DO
    SET i = i + 1;
    SET iSot = SUBSTRING(LPAD(orignum, r * 3, '0'), -(3 * i), 3);
    #SELECT iSot,orignum;

    CASE SUBSTRING(iSot, -3, 1)
      WHEN 1 THEN SET c1Sot = 'сто';
      WHEN 2 THEN SET c1Sot = 'двести';
      WHEN 3 THEN SET c1Sot = 'триста';
      WHEN 4 THEN SET c1Sot = 'четыреста';
      WHEN 5 THEN SET c1Sot = 'пятьсот';
      WHEN 6 THEN SET c1Sot = 'шестьсот';
      WHEN 7 THEN SET c1Sot = 'семьсот';
      WHEN 8 THEN SET c1Sot = 'восемьсот';
      WHEN 9 THEN SET c1Sot = 'девятьсот';
      WHEN 0 THEN SET c1Sot = ' ';
    END CASE;

    IF i = 1
      OR i = 3
    THEN
      CASE SUBSTRING(iSot, -1, 1)
        WHEN 1 THEN SET c3Sot = 'одна';
        WHEN 2 THEN SET c3Sot = 'две';
        WHEN 3 THEN SET c3Sot = 'три';
        WHEN 4 THEN SET c3Sot = 'четыре';
        WHEN 5 THEN SET c3Sot = 'пять';
        WHEN 6 THEN SET c3Sot = 'шесть';
        WHEN 7 THEN SET c3Sot = 'семь';
        WHEN 8 THEN SET c3Sot = 'восемь';
        WHEN 9 THEN SET c3Sot = 'девять';
        WHEN 0 THEN SET c3Sot = ' ';
      END CASE;
    ELSE
      CASE SUBSTRING(iSot, -1, 1)
        WHEN 1 THEN SET c3Sot = 'один';
        WHEN 2 THEN SET c3Sot = 'два';
        WHEN 3 THEN SET c3Sot = 'три';
        WHEN 4 THEN SET c3Sot = 'четыре';
        WHEN 5 THEN SET c3Sot = 'пять';
        WHEN 6 THEN SET c3Sot = 'шесть';
        WHEN 7 THEN SET c3Sot = 'семь';
        WHEN 8 THEN SET c3Sot = 'восемь';
        WHEN 9 THEN SET c3Sot = 'девять';
        WHEN 0 THEN SET c3Sot = ' ';
      END CASE;
    END IF;

    CASE SUBSTRING(iSot, -2, 1)
      WHEN 1 THEN BEGIN
          SET c2Sot = ' ';
          CASE SUBSTRING(iSot, -1, 1)
            WHEN 1 THEN SET c3Sot = 'одиннадцать';
            WHEN 2 THEN SET c3Sot = 'двенадцать';
            WHEN 3 THEN SET c3Sot = 'тринадцать';
            WHEN 4 THEN SET c3Sot = 'четырнадцать';
            WHEN 5 THEN SET c3Sot = 'пятнадцать';
            WHEN 6 THEN SET c3Sot = 'шестнадцать';
            WHEN 7 THEN SET c3Sot = 'семнадцать';
            WHEN 8 THEN SET c3Sot = 'восемнадцать';
            WHEN 9 THEN SET c3Sot = 'девятнадцать';
            WHEN 0 THEN SET c3Sot = 'десять';
          END CASE;
        END;
      WHEN 2 THEN SET c2Sot = 'двадцать';
      WHEN 3 THEN SET c2Sot = 'тридцать';
      WHEN 4 THEN SET c2Sot = 'сорок';
      WHEN 5 THEN SET c2Sot = 'пятьдесят';
      WHEN 6 THEN SET c2Sot = 'шестьдесят';
      WHEN 7 THEN SET c2Sot = 'семьдесят';
      WHEN 8 THEN SET c2Sot = 'восемьдесят';
      WHEN 9 THEN SET c2Sot = 'девяносто';
      WHEN 0 THEN SET c2Sot = ' ';
    END CASE;

    CASE i
      WHEN 1 THEN BEGIN
          SET c1Sot = '';
          SET c2Sot = '';
          SET c3Sot = iSot;
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'копейка';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'копейки';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'копеек';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'копеек';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'копеек';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 0
            AND SUBSTRING(iSot, -1, 1) = 0
          THEN
            -- SET esuff = 'ноль копеек';
            SET esuff = 'копеек';
          END IF;

        END;
      WHEN 2 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'рубль';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'рубля';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'рублей';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'рублей';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'рублей';
          END IF;
        END;
      WHEN 3 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'тысяча';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'тысячи';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'тысяч';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'тысяч';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'тысяч';
          END IF;
          IF SUBSTRING(iSot, -3, 1) = 0
            AND SUBSTRING(iSot, -2, 1) = 0
            AND SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = ' ';
          END IF;
        END;
      WHEN 4 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'миллион';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'миллиона';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'миллионов';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'миллионов';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'миллионов';
          END IF;
          IF SUBSTRING(iSot, -3, 1) = 0
            AND SUBSTRING(iSot, -2, 1) = 0
            AND SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = ' ';
          END IF;
        END;
      WHEN 5 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'миллиард';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'миллиарда';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'миллиардов';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'миллиардов';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'миллиардов';
          END IF;
          IF SUBSTRING(iSot, -3, 1) = 0
            AND SUBSTRING(iSot, -2, 1) = 0
            AND SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = ' ';
          END IF;
        END;
      ELSE SET esuff = 'ERR:слишком большая сумма';
    END CASE;
    SET rtext = CONCAT(c1Sot, ' ', c2Sot, ' ', c3Sot, ' ', esuff, ' ', rtext);
  END WHILE;
  SET rtext = REPLACE(REPLACE(REPLACE(REPLACE(LTRIM(rtext), '  ', ' '), '  ', ' '), '  ', ' '), '  ', ' ');
  IF LOCATE('рубл', rtext) = 1
  THEN
    SET rtext = CONCAT('ноль ', rtext);
  END IF;

  RETURN rtext;
END
$$

DELIMITER ;

DELIMITER $$

--
-- Создать функцию `num_to_text_k`
--
CREATE
FUNCTION num_to_text_k (orignum varchar(128))
RETURNS varchar(500) CHARSET utf8
NO SQL
BEGIN
  DECLARE origlen int(11);
  DECLARE iSot int(11);
  DECLARE r int(11);
  DECLARE i int(11);
  DECLARE c1Sot varchar(32);
  DECLARE c2Sot varchar(32);
  DECLARE c3Sot varchar(32);
  DECLARE rtext text;
  DECLARE esuff varchar(32);

  /*
  -- убираем все дробное чтоп работало при любой точности 
  IF LOCATE('.', orignum) > 0 THEN
    SET orignum = SUBSTRING(orignum, 1, LOCATE('.', orignum) - 1);
  END IF;
  IF LOCATE(',', orignum) > 0 THEN
    SET orignum = SUBSTRING(orignum, 1, LOCATE(',', orignum) - 1);
  END IF;
  */

  #последние три цифры в числе считаються копейками, в случае отсутствия разделителя добавляем 000
  IF LOCATE('.', orignum) > 0
    OR LOCATE(',', orignum) > 0
  THEN
    IF LENGTH(SUBSTRING(orignum, LOCATE('.', orignum))) = 2
      OR LENGTH(SUBSTRING(orignum, LOCATE(',', orignum))) = 2
    THEN
      SET orignum = REPLACE(orignum, '.', '0');
      SET orignum = REPLACE(orignum, ',', '0');
      SET orignum = CONCAT(orignum, '0');
    ELSE
      SET orignum = REPLACE(orignum, '.', '0');
      SET orignum = REPLACE(orignum, ',', '0');
    END IF;
  ELSE
    SET orignum = CONCAT(orignum, '000');
  END IF;

  SET origlen = (SELECT LENGTH(orignum));
  SET r = ROUND(origlen / 3 + 0.3);
  SET i = 0;
  -- SET i = 1; -- отключил копейки
  SET rtext = ' ';
  SET esuff = ' ';

  WHILE (i < r) DO
    SET i = i + 1;
    SET iSot = SUBSTRING(LPAD(orignum, r * 3, '0'), -(3 * i), 3);
    #SELECT iSot,orignum;

    CASE SUBSTRING(iSot, -3, 1)
      WHEN 1 THEN SET c1Sot = 'сто';
      WHEN 2 THEN SET c1Sot = 'двести';
      WHEN 3 THEN SET c1Sot = 'триста';
      WHEN 4 THEN SET c1Sot = 'четыреста';
      WHEN 5 THEN SET c1Sot = 'пятьсот';
      WHEN 6 THEN SET c1Sot = 'шестьсот';
      WHEN 7 THEN SET c1Sot = 'семьсот';
      WHEN 8 THEN SET c1Sot = 'восемьсот';
      WHEN 9 THEN SET c1Sot = 'девятьсот';
      WHEN 0 THEN SET c1Sot = ' ';
    END CASE;

    IF i = 1
      OR i = 3
    THEN
      CASE SUBSTRING(iSot, -1, 1)
        WHEN 1 THEN SET c3Sot = 'одна';
        WHEN 2 THEN SET c3Sot = 'две';
        WHEN 3 THEN SET c3Sot = 'три';
        WHEN 4 THEN SET c3Sot = 'четыре';
        WHEN 5 THEN SET c3Sot = 'пять';
        WHEN 6 THEN SET c3Sot = 'шесть';
        WHEN 7 THEN SET c3Sot = 'семь';
        WHEN 8 THEN SET c3Sot = 'восемь';
        WHEN 9 THEN SET c3Sot = 'девять';
        WHEN 0 THEN SET c3Sot = ' ';
      END CASE;
    ELSE
      CASE SUBSTRING(iSot, -1, 1)
        WHEN 1 THEN SET c3Sot = 'один';
        WHEN 2 THEN SET c3Sot = 'два';
        WHEN 3 THEN SET c3Sot = 'три';
        WHEN 4 THEN SET c3Sot = 'четыре';
        WHEN 5 THEN SET c3Sot = 'пять';
        WHEN 6 THEN SET c3Sot = 'шесть';
        WHEN 7 THEN SET c3Sot = 'семь';
        WHEN 8 THEN SET c3Sot = 'восемь';
        WHEN 9 THEN SET c3Sot = 'девять';
        WHEN 0 THEN SET c3Sot = ' ';
      END CASE;
    END IF;

    CASE SUBSTRING(iSot, -2, 1)
      WHEN 1 THEN BEGIN
          SET c2Sot = ' ';
          CASE SUBSTRING(iSot, -1, 1)
            WHEN 1 THEN SET c3Sot = 'одиннадцать';
            WHEN 2 THEN SET c3Sot = 'двенадцать';
            WHEN 3 THEN SET c3Sot = 'тринадцать';
            WHEN 4 THEN SET c3Sot = 'четырнадцать';
            WHEN 5 THEN SET c3Sot = 'пятнадцать';
            WHEN 6 THEN SET c3Sot = 'шестнадцать';
            WHEN 7 THEN SET c3Sot = 'семнадцать';
            WHEN 8 THEN SET c3Sot = 'восемнадцать';
            WHEN 9 THEN SET c3Sot = 'девятнадцать';
            WHEN 0 THEN SET c3Sot = 'десять';
          END CASE;
        END;
      WHEN 2 THEN SET c2Sot = 'двадцать';
      WHEN 3 THEN SET c2Sot = 'тридцать';
      WHEN 4 THEN SET c2Sot = 'сорок';
      WHEN 5 THEN SET c2Sot = 'пятьдесят';
      WHEN 6 THEN SET c2Sot = 'шестьдесят';
      WHEN 7 THEN SET c2Sot = 'семьдесят';
      WHEN 8 THEN SET c2Sot = 'восемьдесят';
      WHEN 9 THEN SET c2Sot = 'девяносто';
      WHEN 0 THEN SET c2Sot = ' ';
    END CASE;

    CASE i
      WHEN 1 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'копейка';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'копейки';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'копеек';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'копеек';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'копеек';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 0
            AND SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'ноль копеек';
          END IF;

        END;
      WHEN 2 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'рубль';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'рубля';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'рублей';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'рублей';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'рублей';
          END IF;
        END;
      WHEN 3 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'тысяча';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'тысячи';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'тысяч';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'тысяч';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'тысяч';
          END IF;
          IF SUBSTRING(iSot, -3, 1) = 0
            AND SUBSTRING(iSot, -2, 1) = 0
            AND SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = ' ';
          END IF;
        END;
      WHEN 4 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'миллион';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'миллиона';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'миллионов';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'миллионов';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'миллионов';
          END IF;
          IF SUBSTRING(iSot, -3, 1) = 0
            AND SUBSTRING(iSot, -2, 1) = 0
            AND SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = ' ';
          END IF;
        END;
      WHEN 5 THEN BEGIN
          IF SUBSTRING(iSot, -1, 1) = 1
          THEN
            SET esuff = 'миллиард';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 1
          THEN
            SET esuff = 'миллиарда';
          END IF;
          IF SUBSTRING(iSot, -1, 1) > 4
          THEN
            SET esuff = 'миллиардов';
          END IF;
          IF SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = 'миллиардов';
          END IF;
          IF SUBSTRING(iSot, -2, 1) = 1
          THEN
            SET esuff = 'миллиардов';
          END IF;
          IF SUBSTRING(iSot, -3, 1) = 0
            AND SUBSTRING(iSot, -2, 1) = 0
            AND SUBSTRING(iSot, -1, 1) = 0
          THEN
            SET esuff = ' ';
          END IF;
        END;
      ELSE SET esuff = 'ERR:слишком большая сумма';
    END CASE;
    SET rtext = CONCAT(c1Sot, ' ', c2Sot, ' ', c3Sot, ' ', esuff, ' ', rtext);
  END WHILE;
  SET rtext = REPLACE(REPLACE(REPLACE(REPLACE(LTRIM(rtext), '  ', ' '), '  ', ' '), '  ', ' '), '  ', ' ');
  IF LOCATE('рубл', rtext) = 1
  THEN
    SET rtext = CONCAT('ноль ', rtext);
  END IF;

  RETURN rtext;
END
$$

DELIMITER ;


INSERT INTO form_field(id, name, parametr, simplex) VALUES(19, 'К оплате', 'pdebt_sum', 0);
INSERT INTO form_field_items(form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES(19, 0, 0, 0, 98, '', '', '');
-- 2019-11-22 applied on main cycle

DELETE FROM delivery_type WHERE ID >9;
INSERT INTO delivery_type(id, name, hideClient) VALUES
(10, 'EMS-Белпочта', 0),
(11, 'EMS-Белпочта бесплатно', 0),
(12, 'Белпочта', 0),
(13, 'Белпочта бесплатно', 0),
(14, 'Курьером по Минску', 0),
(15, 'Курьером по Минску бесплатно', 0);

INSERT INTO form(id, name, report) VALUES
(6, 'БП Перевод', 'frmBPperevod'),
(7, 'БП Конверт', 'frmBPkonvert'),
(8, 'Курьер Бланк', 'frmKMblank'),
(9, 'Курьер Конверт', 'frmKMkonvert');

DELETE FROM delivery_type_form WHERE delivery_type > 9;

INSERT INTO delivery_type_form(delivery_type, form) VALUES
(10, 6),
(10, 7),
(11, 6),
(11, 7),
(12, 6),
(12, 7),
(13, 6),
(13, 7),
(14, 8),
(14, 9),
(15, 8),
(15, 9);

INSERT INTO form_parametr(form, form_field) VALUES(3, 19);
INSERT INTO form_parametr(form, form_field) VALUES(4, 19);

INSERT INTO form_parametr(form, form_field)
SELECT f.id, form_field FROM form f
  INNER JOIN form_parametr fp ON fp.form = 3 
  WHERE f.id > 5;
  
INSERT INTO form_parametr(form, form_field) VALUES(9, 9);

DELETE FROM delivery_type_dictionary WHERE source=23;
INSERT INTO delivery_type_dictionary(source, delivery_type, site_id) VALUES
(23, 8, 31373),
(23, 10, 32074),
(23, 11, 34929),
(23, 12, 34931),
(23, 13, 35010),
(23, 14, 31370),
(23, 15, 34930);  

-- 2019-11-25 applied on main cycle

ALTER TABLE package_barcode 
  ADD COLUMN box_number INT(5) DEFAULT 0;

DELIMITER $$

--
-- Создать функцию `package_field`
--
CREATE 
FUNCTION package_field (pSource int, pPackage int, pField int)
RETURNS varchar(1000) CHARSET utf8
READS SQL DATA
BEGIN
  DECLARE vResult varchar(1000) DEFAULT ('');
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vIsField int;
  DECLARE vChildField int;
  DECLARE vDelemiter varchar(20);
  DECLARE vPrefix varchar(20);
  DECLARE vSufix varchar(20);
  DECLARE vValue varchar(255);


  DECLARE vCur CURSOR FOR
  SELECT ffi.is_field, ffi.child_field, ffi.delemiter, ffi.prefix, ffi.sufix, pp.value
    FROM form_field_items ffi
      INNER JOIN attr_type at ON ffi.attr_type = AT.id
      LEFT OUTER JOIN package_prop pp ON pp.property = AT.field AND pp.source = pSource AND pp.id = pPackage AND LENGTH(pp.value) > 0
    WHERE ffi.form_field = pField
      AND (pp.id IS NOT NULL OR ffi.is_field = 1)
    ORDER BY ffi.sequence;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vIsField, vChildField, vDelemiter, vPrefix, vSufix, vValue;
    IF vIsEnd
    THEN
      LEAVE wet;
    END IF;
    IF vIsField = 1
    THEN
      SET vValue = package_field2(pSource, pPackage, vChildField);
    END IF;
    SET vResult = CONCAT_WS('', vResult, IF(LENGTH(vResult) > 0, vDelemiter, ''), vPrefix, vValue, vSufix);
  END LOOP wet;
  CLOSE vCur;

  RETURN vResult;
END
$$

DELIMITER ;

DELIMITER $$

--
-- Создать функцию `package_field2`
--
CREATE
FUNCTION package_field2 (pSource int, pPackage int, pField int)
RETURNS varchar(1000) CHARSET utf8
READS SQL DATA
BEGIN
  DECLARE vResult varchar(1000) DEFAULT ('');
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vDelemiter varchar(20);
  DECLARE vPrefix varchar(20);
  DECLARE vSufix varchar(20);
  DECLARE vValue varchar(255);


  DECLARE vCur CURSOR FOR
  SELECT ffi.delemiter, ffi.prefix, ffi.sufix, pp.value
    FROM form_field_items ffi
      INNER JOIN attr_type at ON ffi.attr_type = AT.id
      INNER JOIN package_prop pp ON pp.property = AT.field AND pp.source = pSource AND pp.id = pPackage AND LENGTH(pp.value) > 0
    WHERE ffi.form_field = pField
      AND ffi.is_field = 0
    ORDER BY ffi.sequence;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vDelemiter, vPrefix, vSufix, vValue;
    IF vIsEnd
    THEN
      LEAVE wet;
    END IF;
    SET vResult = CONCAT_WS('', vResult, IF(LENGTH(vResult) > 0, vDelemiter, ''), vPrefix, vValue, vSufix);
  END LOOP wet;
  CLOSE vCur;

  RETURN vResult;
END
$$

DELIMITER ;

INSERT INTO xrep_report(id, src_type, name, rep_group, hidden) VALUES('otkDailyRep', 1, 'ОТК', 0, 0);
INSERT INTO xrep_report_params(report, parameter) VALUES('otkDailyRep', 'period');
-- 2019-01-28 applied on main cycle

INSERT INTO attr_type(id, attr_fml, name, field, list, persist) VALUES(99, 2, 'Ламинат', 'laminat', 1, 1);
--INSERT INTO attr_value(id, attr_tp, value, locked) VALUES(44, 99, 'Нет', 1);
INSERT INTO attr_value(id, attr_tp, value, locked) VALUES(45, 99, 'Глянец', 1);
INSERT INTO attr_value(id, attr_tp, value, locked) VALUES(46, 99, 'Матовый', 1);

ALTER TABLE book_pg_template 
  ADD COLUMN laminat INT(5) DEFAULT 0;
ALTER TABLE print_group 
  ADD COLUMN laminat INT(5) DEFAULT 0;  