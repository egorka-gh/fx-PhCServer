-- main 2015-04-03   
-- moskva 2015-04-10
 
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
  
  
DROP VIEW suborderotkv CASCADE;

CREATE
VIEW suborderOtkV
AS
SELECT `es`.`id` AS `order_id`, `es`.`sub_id` AS `sub_id`, `es`.`state` AS `state`, `es`.`start_date` AS `state_date`, COUNT(DISTINCT `tl`.`sheet`) AS `books_done`, IFNULL(`s`.`prt_qty`, IFNULL((SELECT MAX(`pg`.`book_num`) FROM `print_group` `pg` WHERE ((`pg`.`order_id` = `es`.`id`) AND (`pg`.`sub_id` = `es`.`sub_id`))), 0)) AS `prt_qty`, (SELECT IFNULL(MAX(`pg`.`book_type`), 0) FROM `print_group` `pg` WHERE ((`pg`.`order_id` = `es`.`id`) AND (`pg`.`sub_id` = `es`.`sub_id`) AND (`pg`.`is_reprint` = 0) AND (`pg`.`state` < 450))) AS `proj_type`
  FROM ((((`order_extra_state` `es`
    LEFT JOIN `orders` `o` ON ((`o`.`id` = `es`.`id`)))
    LEFT JOIN `suborders` `s` ON (((`es`.`id` = `s`.`order_id`) AND (`es`.`sub_id` = `s`.`sub_id`))))
    LEFT JOIN `tech_point` `tp` ON ((`tp`.`tech_type` = `es`.`state`)))
    LEFT JOIN `tech_log` `tl` ON (((`tl`.`src_id` = `tp`.`id`) AND (`tl`.`order_id` = `es`.`id`) AND (`tl`.`sub_id` = `es`
      .`sub_id`) AND (`tl`.`sheet` <> 0))))
  WHERE ((`es`.`state` = 450) AND ISNULL(`es`.`state_date`))
  GROUP BY `es`.`id`, `es`.`sub_id`
  ORDER BY `es`.`start_date`;
  

DELIMITER $$

DROP PROCEDURE IF EXISTS findeSubOrderByOrder$$

CREATE 
PROCEDURE findeSubOrderByOrder(IN pOrderId varchar(50), IN pSrcCode char(1))
BEGIN

  SELECT o.id order_id, '' sub_id, sr.name source_name, sr.code source_code, os.name state_name, o.state, o.state_date,
    (SELECT MAX(pg.book_num) FROM print_group pg WHERE o.id = pg.order_id AND pg.sub_id = '') prt_qty,
    (SELECT IFNULL(MAX(pg.book_type), 0) FROM print_group pg WHERE o.id = pg.order_id AND pg.sub_id = '' AND pg.is_reprint = 0 AND pg.state < 450) proj_type
  FROM orders o
    INNER JOIN sources sr ON o.source = sr.id
    INNER JOIN order_state os ON os.id = o.state
  WHERE o.id LIKE pOrderId
  AND sr.code = IFNULL(pSrcCode, sr.code)
  AND NOT EXISTS
  (SELECT 1
    FROM suborders s
    WHERE s.order_id = o.id)
  UNION ALL
  SELECT s.order_id, s.sub_id, sr.name source_name, sr.code source_code, os.name state_name, s.state, s.state_date, s.prt_qty, s.proj_type
  FROM suborders s
    INNER JOIN orders o ON s.order_id = o.id
    INNER JOIN sources sr ON o.source = sr.id
    INNER JOIN order_state os ON os.id = s.state
  WHERE s.order_id LIKE pOrderId
  AND sr.code = IFNULL(pSrcCode, sr.code);

END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS syncSource$$

CREATE 
PROCEDURE syncSource(IN pSourceId int)
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


  -- fix sync iteration in transaction
  START TRANSACTION;
    -- get next sync
    SELECT ss.sync INTO vSync
      FROM sources_sync ss
      WHERE ss.id = pSourceId;
    SET vSync = IFNULL(vSync, 0) + 1;

    -- save sync and reset sync state
    INSERT INTO sources_sync (id, sync, sync_date, sync_state)
                      VALUES (pSourceId, vSync, NOW(), 0)
    ON DUPLICATE KEY UPDATE sync = vSync, sync_date = NOW(), sync_state = 0;
    
    -- orders vs state >200 can be locked while sync complite
    -- any updates - print and so on - will be aborted
    -- to prevent locks update orders (release row lock after COMMIT?)
    -- update sync & group_id
    UPDATE orders o
      INNER JOIN tmp_orders t ON o.id=t.id
      SET o.sync = vSync, o.group_id=t.group_id, o.client_id=t.client_id
      WHERE t.source = pSourceId;

  COMMIT;

  -- keep in transaction
  START TRANSACTION;
    -- set sync
    UPDATE tmp_orders to1
      SET to1.sync = vSync
      WHERE to1.source = pSourceId;

    -- add new
    -- search new
    UPDATE tmp_orders to1
      SET to1.is_new = IFNULL((SELECT 0 FROM orders o WHERE o.id = to1.id), 1)
      WHERE to1.source = pSourceId;
    -- insert new
    INSERT INTO orders
      (id, source, src_id, src_date, state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts, group_id, client_id)
      SELECT id, source, src_id, IF(src_date='1970-01-01 03:00',NOW(),src_date), state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts, group_id, client_id
        FROM tmp_orders to1
        WHERE to1.source = pSourceId AND to1.is_new = 1;
    -- remove new
    DELETE FROM tmp_orders
      WHERE source = pSourceId AND is_new = 1;

    -- check/process preload
    -- update printgroup 
    UPDATE print_group
      SET state = 200, state_date = NOW()
      WHERE state = 199 AND order_id IN (SELECT t.id FROM tmp_orders t WHERE t.source = pSourceId AND t.is_preload = 0);
    -- suborders
    UPDATE suborders s
      SET s.state = 200, s.state_date = NOW()
      WHERE s.state = 199 AND s.order_id IN (SELECT t.id FROM tmp_orders t WHERE t.source = pSourceId AND t.is_preload = 0);

    -- set extra state
    INSERT INTO order_extra_state
      (id, sub_id, state, start_date, state_date)
      SELECT t.id, '', 200, NOW(), NOW()
        FROM tmp_orders t
          INNER JOIN orders o ON o.id = t.id AND o.state = 199 AND o.forward_state=0
        WHERE t.source = pSourceId AND t.is_preload = 0
      ON DUPLICATE KEY UPDATE state_date= NOW();

    -- orders
    UPDATE orders o
      SET state = IF(o.forward_state=0,200,o.forward_state), state_date = NOW(), is_preload = 0
      WHERE o.state = 199 AND o.id IN (SELECT t.id FROM tmp_orders t WHERE t.source = pSourceId AND t.is_preload = 0);

    -- cancel not in sync
    -- cancel print groups
    UPDATE print_group
      SET state = 505, state_date = NOW()
      WHERE order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state BETWEEN 100 AND 200 AND o.sync != vSync);
    -- cancel suborders
    UPDATE suborders s
      SET s.state = 505, s.state_date = NOW()
      WHERE s.order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state BETWEEN 100 AND 200 AND o.sync != vSync);
    -- cancel orders
    UPDATE orders o
      SET state = 505, state_date = NOW()
      WHERE o.source = pSourceId AND o.state BETWEEN 100 AND 200 AND o.sync != vSync;

    -- finde reload candidate by project data time
    UPDATE tmp_orders t
      SET t.reload = 1
      WHERE t.source = pSourceId AND t.data_ts IS NOT NULL
        AND EXISTS (SELECT 1 FROM orders o WHERE o.id = t.id AND o.data_ts IS NOT NULL AND o.data_ts != o.data_ts AND o.state BETWEEN 199 AND 200);

    -- finde reload candidate vs sync cancel (state=505)
    UPDATE tmp_orders t
      SET t.reload = 1
      WHERE t.source = pSourceId
        AND EXISTS (SELECT 1 FROM orders o WHERE o.id = t.id AND o.state=505);

    -- clean orders 4 reload
    -- clean print_group
    DELETE FROM print_group
      WHERE order_id IN
        (SELECT id FROM tmp_orders t WHERE t.source = pSourceId AND t.reload = 1);
    -- clean extra info
    DELETE FROM order_extra_info
      WHERE id IN
        (SELECT id FROM tmp_orders t WHERE t.source = pSourceId AND t.reload = 1);
    -- clean suborder
    DELETE FROM suborders
      WHERE order_id IN
        (SELECT id FROM tmp_orders t WHERE t.source = pSourceId AND t.reload = 1);
    -- reset order state
    UPDATE orders o
      SET o.state = 100, o.state_date = NOW(), o.is_preload=IFNULL((SELECT tt.is_preload FROM tmp_orders tt WHERE tt.id = o.id),0)
      WHERE o.id IN
        (SELECT id FROM tmp_orders t WHERE t.source = pSourceId AND t.reload = 1);
    -- set project data time
    UPDATE orders o
      SET o.data_ts = (SELECT tt.data_ts FROM tmp_orders tt WHERE tt.id = o.id)
      WHERE o.source = pSourceId
        AND EXISTS
          (SELECT 1 FROM tmp_orders t WHERE t.id = o.id AND t.data_ts IS NOT NULL AND t.data_ts != IFNULL(o.data_ts, ''));

    -- forvard state to mail send (site) state (466)
    -- print groups
    UPDATE print_group
      SET state = 466, state_date = NOW()
      WHERE order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state IN (450,465) AND o.sync != vSync);
    -- suborders
    UPDATE suborders s
      SET s.state = 466, s.state_date = NOW()
      WHERE s.order_id IN (SELECT id FROM orders o WHERE o.source = pSourceId AND o.state IN (450,465) AND o.sync != vSync);
    -- orders
    UPDATE orders o
      SET state = 466, state_date = NOW()
      WHERE o.source = pSourceId AND o.state IN (450,465) AND o.sync != vSync;

    -- finalize
    DELETE FROM tmp_orders
      WHERE source = pSourceId;

    UPDATE sources_sync 
      SET sync = vSync, sync_date = NOW(), sync_state = 1
    WHERE id=pSourceId;
  COMMIT;
END
$$

DELIMITER ;
