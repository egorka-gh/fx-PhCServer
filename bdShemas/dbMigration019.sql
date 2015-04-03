-- main 2015-04-03   
-- moskva 
SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

DELIMITER $$

CREATE
PROCEDURE extraStateSetOTK (IN pOrder varchar(50), IN pSubOrder varchar(50), IN pDate datetime)
MODIFIES SQL DATA
BEGIN
  DECLARE vMinSubState int;
  DECLARE vMinPhotoState int;

  IF pDate IS NULL THEN
    SET pDate = NOW();
  END IF;

  IF pSubOrder != '' THEN
    -- fix suborder
    -- forvard suborder pgs state
    UPDATE print_group pg
    SET pg.state = 450,
        pg.state_date = pDate
    WHERE pg.order_id = pOrder
    AND pg.sub_id = pSubOrder
    AND pg.state < 450
    ORDER BY pg.id;

    -- set suborder state
    UPDATE suborders s
    SET s.state = 450,
        s.state_date = pDate
    WHERE s.order_id = pOrder
    AND s.sub_id = pSubOrder
    AND s.state < 450;

    -- fix suborder extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      VALUES (pOrder, pSubOrder, 450, pDate, pDate)
    ON DUPLICATE KEY UPDATE state_date = pDate;

  END IF;

  -- calc min state by photo pgs 
  SELECT MIN(pg.state) INTO vMinPhotoState
    FROM print_group pg
    WHERE pg.order_id = pOrder
      AND pg.is_reprint = 0
      AND pg.book_type = 0;

  -- calc min state by suborders
  SELECT MIN(so.state) INTO vMinSubState
    FROM suborders so
    WHERE so.order_id = pOrder;

  -- attempt to forvard order state
  IF (vMinPhotoState IS NULL
    OR vMinPhotoState >= 450)
    AND (vMinSubState IS NULL
    OR vMinSubState >= 450) THEN
    -- no photo or photo pgs pass OTK and no suborders or all suborders pass otk

    -- forvard pgs state
    UPDATE print_group pg
    SET pg.state = 450,
        pg.state_date = pDate
    WHERE pg.order_id = pOrder
    AND pg.sub_id = ''
    AND pg.state < 450
    ORDER BY pg.id;

    -- set order state
    UPDATE orders o
    SET o.state = 450,
        o.state_date = pDate
    WHERE o.id = pOrder
    AND o.state < 450;

    -- fix extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      VALUES (pOrder, pSubOrder, 450, pDate, pDate)
    ON DUPLICATE KEY UPDATE state_date = pDate;

    -- stop all started order extra states
    UPDATE order_extra_state es
    SET es.state_date = pDate
    WHERE es.id = pOrder
    -- AND es.sub_id = ''
    AND es.state < 450
    AND es.state_date IS NULL
    ORDER BY es.sub_id, es.state;

  END IF;

END
$$

DELIMITER ;

DELIMITER $$

CREATE
PROCEDURE extraStateSetOTKByPG (IN pPrintGroup varchar(50), IN pDate datetime)
MODIFIES SQL DATA
COMMENT 'works only vs photo print groups'
BEGIN
  DECLARE vOrder varchar(50);

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vOrder = NULL;

  SELECT pg.order_id INTO vOrder
    FROM print_group pg
    WHERE pg.id = pPrintGroup
      AND pg.sub_id = ''
      AND pg.book_type = 0;

  IF vOrder IS NOT NULL THEN

    IF pDate IS NULL THEN
      SET pDate = NOW();
    END IF;

    UPDATE print_group pg
    SET pg.state = 450,
        pg.state_date = pDate
    WHERE pg.id = pPrintGroup;

    CALL extraStateSetOTK(vOrder, '', pDate);
  END IF;

END
$$

DELIMITER ;

CREATE OR REPLACE 
VIEW suborderotkv
AS
	SELECT `es`.`id` AS `order_id`, `es`.`sub_id` AS `sub_id`, `es`.`state` AS `state`, `es`.`start_date` AS `state_date`, 
	  COUNT(DISTINCT `tl`.`sheet`) AS `books_done`, 
	  IFNULL(`s`.`prt_qty`, IFNULL((SELECT MAX(`pg`.`book_num`) FROM `print_group` `pg` WHERE ((`pg`.`order_id` = `es`.`id`) AND (`pg`.`sub_id` = `es`.`sub_id`))), 0)) AS `prt_qty`,
	   ((SELECT MAX(`pg`.book_type) FROM `print_group` `pg` WHERE ((`pg`.`order_id` = `es`.`id`) AND (`pg`.`sub_id` = `es`.`sub_id`)))) AS proj_type
	  FROM ((((`order_extra_state` `es`
	    LEFT JOIN `orders` `o` ON ((`o`.`id` = `es`.`id`)))
	    LEFT JOIN `suborders` `s` ON (((`es`.`id` = `s`.`order_id`) AND (`es`.`sub_id` = `s`.`sub_id`))))
	    LEFT JOIN `tech_point` `tp` ON ((`tp`.`tech_type` = `es`.`state`)))
	    LEFT JOIN `tech_log` `tl` ON (((`tl`.`src_id` = `tp`.`id`) AND (`tl`.`order_id` = `es`.`id`) AND (`tl`.`sub_id` = `es`
	      .`sub_id`) AND (`tl`.`sheet` <> 0))))
	  WHERE ((`es`.`state` = 450) AND ISNULL(`es`.`state_date`))
	  GROUP BY `es`.`id`, `es`.`sub_id`
	  ORDER BY `es`.`start_date`;
	  
UPDATE orders o
  SET o.state=450
  WHERE o.state >450 AND o.state <460;

DELETE FROM package
  WHERE state >450 AND state <460;
 
UPDATE tech_point tp SET tp.tech_type=460 WHERE tp.tech_type=455;
   
  DELETE FROM order_state
  WHERE id >450 AND id <460;

UPDATE order_state
  SET name='Упаковка'
  WHERE id=460;
  
 