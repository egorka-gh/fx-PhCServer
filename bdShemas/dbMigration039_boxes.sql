-- main    
-- cycle rep    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

ALTER TABLE sources 
  ADD COLUMN has_boxes TINYINT(1) DEFAULT 0;
  
ALTER TABLE rack_orders 
  ADD COLUMN is_box TINYINT(1) DEFAULT 0;  

ALTER TABLE rack_orders 
   DROP FOREIGN KEY FK_rack_orders_orders_id;

CREATE TABLE package_new (
  source int(7) NOT NULL,
  id int(11) NOT NULL,
  client_id int(11) DEFAULT 0,
  created datetime DEFAULT NULL,
  attempt tinyint(4) DEFAULT 0,
  PRIMARY KEY (source, id)
)
ENGINE = INNODB,
AVG_ROW_LENGTH = 353,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

ALTER TABLE rack_space 
  ADD COLUMN box_id VARCHAR(50) DEFAULT '';
  
UPDATE order_state SET name = 'ОТК комплектация' WHERE id = 449;  

CREATE TABLE package_box (
  source int(7) DEFAULT NULL,
  package_id int(11) DEFAULT NULL,
  box_id varchar(50) NOT NULL DEFAULT '',
  box_num int(11) DEFAULT NULL,
  barcode varchar(50) DEFAULT '',
  price decimal(10, 2) DEFAULT 0.00,
  weight int(7) DEFAULT NULL,
  state int(5) DEFAULT 0,
  state_date datetime DEFAULT NULL,
  PRIMARY KEY (box_id)
)
ENGINE = INNODB,
AVG_ROW_LENGTH = 97,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

--
-- Создать внешний ключ
--
ALTER TABLE package_box
ADD CONSTRAINT FK_package_box FOREIGN KEY (source, package_id)
REFERENCES package (source, id) ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE package_box_item (
  box_id varchar(50) NOT NULL,
  order_id varchar(50) NOT NULL DEFAULT '',
  alias varchar(100) NOT NULL DEFAULT '',
  item_from int(5) DEFAULT 0,
  item_to int(5) DEFAULT 0,
  type varchar(50) DEFAULT '',
  PRIMARY KEY (box_id, order_id, alias)
)
ENGINE = INNODB,
AVG_ROW_LENGTH = 97,
CHARACTER SET utf8,
COLLATE utf8_general_ci;

--
-- Создать внешний ключ
--
ALTER TABLE package_box_item
ADD CONSTRAINT FK_package_box_item_box_id FOREIGN KEY (box_id)
REFERENCES package_box (box_id) ON DELETE CASCADE ON UPDATE CASCADE;

DROP PROCEDURE IF EXISTS syncSource;

DELIMITER $$

CREATE 
PROCEDURE syncSource (IN pSourceId int)
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
          WHERE t.source = pSourceId)
    THEN
      LEAVE main;
    END IF;


    -- fix sync iteration in transaction
    START TRANSACTION;
      -- get next sync
      SELECT ss.sync
      INTO vSync
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
      INNER JOIN tmp_orders t ON o.id = t.id
        SET o.sync = vSync,
            o.group_id = t.group_id,
            o.client_id = t.client_id,
            o.src_date = IF(t.src_date = '1970-01-01 03:00', NOW(), t.src_date)
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
        SET to1.is_new = IFNULL(
        (SELECT 0
            FROM orders o
            WHERE o.id = to1.id), 1)
        WHERE to1.source = pSourceId;
      -- insert new
      INSERT INTO orders (id, source, src_id, src_date, state, state_date, ftp_folder, fotos_num, sync, is_preload, data_ts, group_id, client_id)
        SELECT id,
          source,
          src_id,
          IF(src_date = '1970-01-01 03:00', NOW(), src_date),
          state,
          state_date,
          ftp_folder,
          fotos_num,
          sync,
          is_preload,
          data_ts,
          group_id,
          client_id
          FROM tmp_orders to1
          WHERE to1.source = pSourceId
            AND to1.is_new = 1;

      -- add new groups to get boxes
      INSERT IGNORE INTO package_new (source, id, client_id, created, attempt)
        SELECT DISTINCT source, group_id, client_id, NOW(), 0
          FROM tmp_orders to1
          WHERE to1.source = pSourceId
            AND to1.is_new = 1
            AND NOT EXISTS
            (SELECT 1
                FROM package p
                WHERE p.source = to1.source
                  AND p.id = to1.group_id);

      -- remove new
      DELETE
        FROM tmp_orders
      WHERE source = pSourceId
        AND is_new = 1;

      -- check/process preload for compo
      UPDATE tmp_orders to1
        SET to1.is_new = 1
        WHERE to1.source = pSourceId AND to1.is_preload = 0
          AND EXISTS
          (SELECT 1
              FROM print_group pg
              WHERE pg.order_id = to1.id
                AND pg.state = 199
                AND pg.compo_type = 1);
      IF ROW_COUNT() > 0
      THEN
        -- update books
        UPDATE order_books ob
        INNER JOIN print_group pg ON ob.pg_id = pg.id
        INNER JOIN tmp_orders t ON t.id = pg.order_id
          SET ob.state = IF(pg.compo_type = 1, 185, 200),
              ob.state_date = NOW()
          WHERE t.source = pSourceId AND t.is_new = 1;
        -- update printgroup 
        UPDATE print_group
          SET state = IF(compo_type = 1, 185, 200),
              state_date = NOW()
          WHERE order_id IN
            (SELECT t.id
                FROM tmp_orders t
                WHERE t.source = pSourceId
                  AND t.is_new = 1);
        -- suborders
        UPDATE suborders s
        INNER JOIN tmp_orders t ON t.id = s.order_id
          SET s.state = 185,
              s.state_date = NOW()
          WHERE s.state = 199 AND t.source = pSourceId AND t.is_new = 1;
        -- orders 
        UPDATE orders o
        INNER JOIN tmp_orders t ON o.id = t.id
          SET o.state = 185
          WHERE o.state = 199 AND t.source = pSourceId AND t.is_new = 1;
      END IF;

      -- check/process other preload
      -- update books
      UPDATE order_books ob
      INNER JOIN print_group pg ON ob.pg_id = pg.id
        AND pg.state = 199
      INNER JOIN tmp_orders t ON t.id = pg.order_id
        SET ob.state = 200,
            ob.state_date = NOW()
        WHERE t.source = pSourceId AND t.is_preload = 0;

      -- update printgroup 
      UPDATE print_group
        SET state = 200,
            state_date = NOW()
        WHERE state = 199 AND order_id IN
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

      -- set extra state
      INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
        SELECT t.id,
          '',
          200,
          NOW(),
          NOW()
          FROM tmp_orders t
            INNER JOIN orders o ON o.id = t.id
              AND o.state = 199
              AND o.forward_state = 0
          WHERE t.source = pSourceId
            AND t.is_preload = 0
      ON DUPLICATE KEY UPDATE state_date = NOW();

      -- orders
      UPDATE orders o
        SET state = IF(o.forward_state = 0, 200, o.forward_state),
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
        SET state = 505,
            state_date = NOW()
        WHERE order_id IN
          (SELECT id
              FROM orders o
              WHERE o.source = pSourceId
                AND o.state BETWEEN 100 AND 200
                AND o.sync != vSync);
      -- cancel suborders
      UPDATE suborders s
        SET s.state = 505,
            s.state_date = NOW()
        WHERE s.order_id IN
          (SELECT id
              FROM orders o
              WHERE o.source = pSourceId
                AND o.state BETWEEN 100 AND 200
                AND o.sync != vSync);
      -- cancel orders
      UPDATE orders o
        SET state = 505,
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
                AND o.state BETWEEN 120 AND 200);

      -- finde reload candidate vs sync cancel (state=505)
      UPDATE tmp_orders t
        SET t.reload = 1
        WHERE t.source = pSourceId
          AND EXISTS
          (SELECT 1
              FROM orders o
              WHERE o.id = t.id
                AND o.state = 505);

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
            o.state_date = NOW(),
            o.resume_load = 0,
            o.is_preload = IFNULL(
            (SELECT tt.is_preload
                FROM tmp_orders tt
                WHERE tt.id = o.id), 0)
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

      UPDATE sources_sync
        SET sync = vSync,
            sync_date = NOW(),
            sync_state = 1
        WHERE id = pSourceId;
    COMMIT;
  END
  $$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageGetBoxSpace;

DELIMITER $$

CREATE
PROCEDURE packageGetBoxSpace (IN pSource int, IN pPackage int, IN pBox varchar(50), IN pTechPoint int)
BEGIN
  DECLARE vSpace int DEFAULT (0);

  DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN
    SET vSpace = 0;
  END;

  SELECT rs.id
  INTO vSpace
    FROM rack_space rs
    WHERE rs.package_source = pSource
      AND rs.package_id = pPackage
      AND rs.box_id = pBox;

  IF vSpace = 0
  THEN
    -- look4 empty space
    SELECT MIN(rs.id)
    INTO vSpace
      FROM rack_space rs
        INNER JOIN rack_tech_point rtp ON rs.rack = rtp.rack AND rtp.tech_point = pTechPoint
      WHERE rs.package_source = 0
        AND rs.package_id = 0;
    IF vSpace > 0
    THEN
      UPDATE rack_space
        SET package_source = pSource,
            package_id = pPackage,
            box_id = pBox
        WHERE id = vSpace;
    END IF;
  END IF;

  SELECT rs.*, r.name rack_name
    FROM rack_space rs
      INNER JOIN rack r ON rs.rack = r.id
    WHERE rs.id = vSpace;
END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageBoxStartOTK;

DELIMITER $$

CREATE 
PROCEDURE packageBoxStartOTK (IN pBoxID varchar(50), IN pPgID varchar(50))
BEGIN
  DECLARE vSource int;
  DECLARE vGroupID int;
  DECLARE vOrderID varchar(50);
  DECLARE vSubID varchar(50);
  DECLARE vAlias varchar(100);

  SELECT o.source, o.group_id, o.id, pg.sub_id, pg.alias
  INTO vSource, vGroupID, vOrderID, vSubID, vAlias
    FROM print_group pg
      INNER JOIN orders o ON pg.order_id = o.id
    WHERE pg.id = pPgID;

  UPDATE package p
    SET p.state = 449,
        p.state_date = NOW()
    WHERE p.source = vSource AND p.id = vGroupID AND p.state < 449;

  UPDATE package_box pb
    SET pb.state = 449,
        pb.state_date = NOW()
    WHERE pb.box_id = pBoxID AND pb.state < 449;

  CALL extraStateStart(vOrderID, vSubID, 450, NOW());

END
$$

DELIMITER ;

-- 22.12.2020 test cycle

ALTER TABLE package_box_item 
  ADD COLUMN state INT(5) NOT NULL DEFAULT 200;

ALTER TABLE package_box_item 
  ADD COLUMN state_date DATETIME DEFAULT NULL;

UPDATE order_state SET name = 'ОТК комплект' WHERE id = 450;

DROP PROCEDURE IF EXISTS packageBoxCreate;
DROP PROCEDURE IF EXISTS packageBoxStartOTK;
DROP PROCEDURE IF EXISTS packageGetBoxSpace;
DROP PROCEDURE IF EXISTS packageGetOrderSpace;
DROP PROCEDURE IF EXISTS packageGetSpaces;
DROP PROCEDURE IF EXISTS packageSetBoxItemOTK;
DROP PROCEDURE IF EXISTS packageSetBoxOTK;
DROP PROCEDURE IF EXISTS packageSetBoxPacked;
DROP PROCEDURE IF EXISTS packageSetOrderSpace;


DELIMITER $$

CREATE
PROCEDURE packageSetOrderSpace (IN pOrderId varchar(50), IN pSpace int)
BEGIN
  DECLARE vSource int;
  DECLARE vPackage int;
  DECLARE vOrderWeight float DEFAULT (0);
  DECLARE vResult int DEFAULT (0);

  DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN
    SET vPackage = NULL;
    SET vResult = 0;
  END;

  -- get order data
  SELECT o.source, o.group_id, IFNULL(oei.weight, 0) INTO vSource, vPackage, vOrderWeight
  FROM orders o
    LEFT OUTER JOIN order_extra_info oei ON o.id = oei.id
    AND oei.sub_id = ''
  WHERE o.id = pOrderId;

  IF vPackage IS NOT NULL THEN
    -- check if space has different package
    SELECT IFNULL(MIN(-1), 1) INTO vResult
    FROM rack_orders ro
      INNER JOIN orders o ON ro.order_id = o.id
    WHERE ro.space = pSpace
      AND (o.source != vSource OR o.group_id != vPackage);
    IF vResult > 0 THEN
      -- check wieght
      SELECT IF(rs.weight < ROUND(((IFNULL(SUM(oei.weight), 0) + vOrderWeight) / 1000), 1), -2, 1) INTO vResult
      FROM rack_space rs
        LEFT OUTER JOIN rack_orders ro ON rs.id = ro.space
        LEFT OUTER JOIN order_extra_info oei ON ro.order_id = oei.id
        AND oei.sub_id = ''
      WHERE rs.id = pSpace
        AND ro.order_id != pOrderId;
    END IF;
    IF vResult > 0 THEN
      -- set order space
      INSERT IGNORE INTO rack_orders (order_id, space)
        VALUES (pOrderId, pSpace);
    END IF;
  END IF;

  -- return result ?
  SELECT vResult AS value;
END
$$

CREATE
PROCEDURE packageSetBoxPacked (IN pBox varchar(50))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vGroup integer;
  DECLARE vSource integer;

  -- set box state
  UPDATE package_box pb
  SET pb.state = 460,
      pb.state_date = NOW()
  WHERE pb.box_id = pBox;

  SELECT pb.package_id, pb.source INTO vGroup, vSource
  FROM package_box pb
  WHERE pb.box_id = pBox;

  -- clean rack
  UPDATE rack_space
  SET package_source = 0,
      package_id = 0,
      box_id = ''
  WHERE package_source = vSource
    AND package_id = vGroup
    AND box_id = pBox;


  -- check boxes & set group state
  SELECT MIN(pb.state) INTO vState
  FROM package_box pb
  WHERE pb.source = vSource
    AND pb.package_id = vGroup;

  IF vState >= 460 THEN
    UPDATE package p
    SET p.state = 460,
        p.state_date = NOW()
    WHERE p.source = vSource
      AND p.id = vGroup
      AND p.state < 460;
    UPDATE orders o
    SET o.state = 460,
        o.state_date = NOW()
    WHERE o.source = vSource
      AND o.group_id = vGroup
      AND o.state < 460;
  END IF;
  -- TODO fix order && extra states

  SELECT 'package_state' field, vState value;

END
$$

CREATE
PROCEDURE packageSetBoxOTK (IN pBox varchar(50))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  -- check if all box items over OTK state
  -- get state by box items 
  SELECT MIN(bi.state) state INTO vState
  FROM package_box_item bi
  WHERE bi.box_id = pBox;
  IF vState >= 450 THEN
    -- set box state
    UPDATE package_box pb
    SET pb.state = 450,
        pb.state_date = NOW()
    WHERE pb.box_id = pBox;
  -- TODO fix order && extra_state states
  END IF;

  SELECT 'state' field, vState value;


END
$$

CREATE
PROCEDURE packageSetBoxItemOTK (IN pBox varchar(50), IN pOrder varchar(50), IN pAlias varchar(100))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vPgID varchar(50);
  -- check if all books over OTK state, checks printgroup state if photo
    -- get by box item 
    SELECT MIN(IFNULL(ob.state, 450)) state INTO vState
    FROM package_box_item bi
      INNER JOIN print_group pg ON pg.order_id = bi.order_id
      AND bi.alias = IFNULL(pg.alias, pg.path)
      AND pg.is_reprint = 0
      LEFT OUTER JOIN order_books ob ON pg.id = ob.pg_id
      AND ob.book BETWEEN bi.item_from AND bi.item_to
    WHERE bi.box_id = pBox
      AND bi.order_id = pOrder
      AND bi.alias = pAlias;
    IF vState >= 450 THEN
      -- set item state
      UPDATE package_box_item pbi
      SET pbi.state = 450,
          pbi.state_date = NOW()
      WHERE pbi.box_id = pBox
        AND pbi.order_id = pOrder
        AND pbi.alias = pAlias
        AND pbi.state < 450;
    END IF;

  SELECT 'state' field, vState value;
END
$$

CREATE
PROCEDURE packageGetSpaces (IN pOrderId varchar(50), IN pTechPoint int)
READS SQL DATA
BEGIN
  DECLARE vSource int;
  DECLARE vPackage int;

  DECLARE vOrderWeight float DEFAULT (0);
  DECLARE vOrderSpace int;

  DECLARE vFullWeight float DEFAULT (0);
  DECLARE vDoneWeight float DEFAULT (0);

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vPackage = NULL;

  SELECT o.source, o.group_id, IFNULL(oei.weight, 0), IFNULL(ro.space, -1) INTO vSource, vPackage, vOrderWeight, vOrderSpace
  FROM orders o
    LEFT OUTER JOIN order_extra_info oei ON o.id = oei.id
    AND oei.sub_id = ''
    LEFT OUTER JOIN rack_orders ro ON ro.order_id = o.id
  WHERE o.id = pOrderId;

  IF vPackage IS NOT NULL THEN
    IF vOrderSpace > 0 THEN
      -- order has space
      SELECT r.name rack_name, rs.*, 0 unused_weight, -1 rating
      FROM rack_space rs
        INNER JOIN rack r ON r.id = rs.rack
      WHERE rs.id = vOrderSpace;
    ELSE
      -- build space list

      -- calc package weights
      SELECT IFNULL(SUM(oei.weight), 0), IFNULL(SUM(IF(ro.space IS NOT NULL, oei.weight, 0)), 0) INTO vFullWeight, vDoneWeight
      FROM orders o
        LEFT OUTER JOIN order_extra_info oei ON o.id = oei.id
        AND oei.sub_id = ''
        LEFT OUTER JOIN rack_orders ro ON ro.order_id = o.id
      WHERE o.source = vSource
        AND o.group_id = vPackage;

      SELECT t.*
      FROM (
        -- package spaces
        SELECT r.name rack_name, rs.*, CAST((rs.weight - IFNULL(SUM(oei.weight), 0) / 1000) AS decimal(4, 1)) unused_weight, -1 rating
        FROM orders o
          INNER JOIN rack_orders ro ON ro.order_id = o.id
          INNER JOIN rack_space rs ON rs.id = ro.space
          INNER JOIN rack r ON r.id = rs.rack
          LEFT OUTER JOIN order_extra_info oei ON o.id = oei.id
          AND oei.sub_id = ''
        WHERE o.source = vSource
          AND o.group_id = vPackage
        GROUP BY r.id,
          rs.id
        HAVING (rs.weight - IFNULL(SUM(oei.weight), 0) / 1000) >= (vOrderWeight /
          1000)
        UNION ALL
        -- free spaces
        SELECT r.name rack_name, rs.*, rs.weight unused_weight, CAST((rs.weight - (vFullWeight - vDoneWeight) / 1000) AS decimal(4, 1)) rating
        FROM rack_space rs
          INNER JOIN rack r ON r.id = rs.rack
        WHERE NOT EXISTS (SELECT 1
            FROM rack_orders ro
            WHERE ro.space = rs.id)
          AND (pTechPoint = 0 OR EXISTS (SELECT 1
            FROM rack_tech_point rtp
            WHERE r.id = rtp.rack
              AND rtp.tech_point = pTechPoint))
          AND rs.weight >= ((vFullWeight - vDoneWeight) / 1000)) t
      ORDER BY t.rating;

    END IF;
  ELSE
    -- empty responce
    SELECT r.name rack_name, rs.*, 0 unused_weight, -1 rating
    FROM rack_space rs
      INNER JOIN rack r ON r.id = rs.rack
    WHERE rs.id = NULL;
  END IF;

END
$$

CREATE
PROCEDURE packageGetOrderSpace (IN pOrderId varchar(50))
READS SQL DATA
BEGIN

  SELECT r.name rack_name, rs.name, IFNULL(SUM(IF(o.id = oei.id, oei.weight, 0)), 0) / 1000 weight, IFNULL(SUM(oei.weight), 0) / 1000 unused_weight
  FROM orders o
    INNER JOIN orders o1 ON o1.source = o.source
    AND o1.group_id = o.group_id
    LEFT OUTER JOIN rack_orders ro ON ro.order_id = o.id
    LEFT OUTER JOIN rack_space rs ON rs.id = ro.space
    LEFT OUTER JOIN rack r ON r.id = rs.rack
    LEFT OUTER JOIN order_extra_info oei ON o1.id = oei.id
    AND oei.sub_id = ''
  WHERE o.id = pOrderId;

END
$$

CREATE
PROCEDURE packageGetBoxSpace (IN pSource int, IN pPackage int, IN pBox varchar(50), IN pTechPoint int)
BEGIN
  DECLARE vSpace int DEFAULT (0);

  DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN
    SET vSpace = 0;
  END;

  SELECT rs.id INTO vSpace
  FROM rack_space rs
  WHERE rs.package_source = pSource
    AND rs.package_id = pPackage
    AND rs.box_id = pBox;

  IF vSpace = 0 THEN
    -- look4 empty space
    SELECT MIN(rs.id) INTO vSpace
    FROM rack_space rs
      INNER JOIN rack_tech_point rtp ON rs.rack = rtp.rack
      AND rtp.tech_point = pTechPoint
    WHERE rs.package_source = 0
      AND rs.package_id = 0;
    IF vSpace > 0 THEN
      UPDATE rack_space
      SET package_source = pSource,
          package_id = pPackage,
          box_id = pBox
      WHERE id = vSpace;
    END IF;
  END IF;

  SELECT rs.*, r.name rack_name
  FROM rack_space rs
    INNER JOIN rack r ON rs.rack = r.id
  WHERE rs.id = vSpace;
END
$$

CREATE
PROCEDURE packageBoxStartOTK (IN pBoxID varchar(50), IN pPgID varchar(50))
BEGIN
  DECLARE vSource int;
  DECLARE vGroupID int;
  DECLARE vOrderID varchar(50);
  DECLARE vSubID varchar(50);
  DECLARE vAlias varchar(100);

  SELECT o.source, o.group_id, o.id, pg.sub_id, pg.alias INTO vSource, vGroupID, vOrderID, vSubID, vAlias
  FROM print_group pg
    INNER JOIN orders o ON pg.order_id = o.id
  WHERE pg.id = pPgID;

  UPDATE package p
  SET p.state = 449,
      p.state_date = NOW()
  WHERE p.source = vSource
    AND p.id = vGroupID
    AND p.state < 449;

  IF pBoxID = '' THEN
    -- no box, box items generated by printgroups
    UPDATE print_group pg
    SET state = 449,
        state_date = NOW()
    WHERE pg.order_id = vOrderID
      AND pg.state < 449;
  ELSE
    -- real box
    UPDATE package_box pb
    SET pb.state = 449,
        pb.state_date = NOW()
    WHERE pb.box_id = pBoxID
      AND pb.state < 449;

    UPDATE package_box_item pbi
    SET pbi.state = 449,
        pbi.state_date = NOW()
    WHERE pbi.box_id = pBoxID
      AND pbi.state < 449;
  END IF;

  CALL extraStateStart(vOrderID, vSubID, 450, NOW());

END
$$

CREATE
PROCEDURE packageBoxCreate (IN pSource int, IN pGroup int)
BEGIN
  -- box
  INSERT IGNORE INTO package_box (source
  , package_id
  , box_id
  , box_num
  , barcode
  , price
  , weight
  , state
  , state_date)
    SELECT p.source, p.id, CONCAT_WS('-', p.source, p.id), 0, IFNULL((SELECT pb.barcode FROM package_barcode pb WHERE pb.source = p.source AND pb.id = p.id AND pb.barcode IS NOT NULL AND pb.barcode != '' LIMIT 1), ''), 0, 0, 449, NOW()
    FROM package p
    WHERE p.source = pSource
      AND p.id = pGroup;

  -- items
  INSERT IGNORE INTO package_box_item (box_id
  , order_id
  , alias
  , item_from
  , item_to
  , type
  , state
  , state_date)
    SELECT CONCAT_WS('-', pSource, pGroup), pg.order_id, IFNULL(pg.alias, pg.path) alias, 1 item_from, MAX(pg.book_num) item_to, bt.name book_type_name, 449, NOW()
    FROM orders o
      INNER JOIN print_group pg ON o.id = pg.order_id
      INNER JOIN book_type bt ON pg.book_type = bt.id
    WHERE o.source = pSource
      AND o.group_id = pGroup
    GROUP BY pg.order_id, bt.name, pg.alias;
END
$$

DELIMITER ;

-- 2020-04-17 test cycle

DROP PROCEDURE IF EXISTS packageSetBoxSend;

DELIMITER $$

CREATE
PROCEDURE packageSetBoxSend (IN pBox varchar(50))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vGroup integer;
  DECLARE vSource integer;

  -- set box state
  UPDATE package_box pb
  SET pb.state = 465,
      pb.state_date = NOW()
  WHERE pb.box_id = pBox
    AND pb.state < 465;

  SELECT pb.package_id, pb.source INTO vGroup, vSource
  FROM package_box pb
  WHERE pb.box_id = pBox;

  -- check boxes & set group state
  SELECT MIN(pb.state) INTO vState
  FROM package_box pb
  WHERE pb.source = vSource
    AND pb.package_id = vGroup;

  IF vState >= 465 THEN
    UPDATE package p
    SET p.state = 465,
        p.state_date = NOW()
    WHERE p.source = vSource
      AND p.id = vGroup
      AND p.state < 465;

    -- set orders state
    UPDATE orders o
    SET o.state = 465,
        o.state_date = NOW()
    WHERE o.source = vSource
      AND o.group_id = vGroup
      AND o.state < 465;

    -- fix order extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      SELECT o.id, '', 465, NOW(), NOW()
      FROM orders o
      WHERE o.source = vSource
        AND o.group_id = vGroup
    ON DUPLICATE KEY UPDATE state_date = NOW();

    -- stop all started order extra states
    UPDATE order_extra_state es
    INNER JOIN orders o
      ON es.id = o.id
    SET es.state_date = NOW()
    WHERE o.source = vSource
      AND o.group_id = vGroup
      AND es.state < 465
      AND es.state_date IS NULL
    ORDER BY es.id, es.sub_id, es.state;

  END IF;

  SELECT 'package_state' field, vState value;

END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageSetBoxItemOTK;

DELIMITER $$

CREATE
PROCEDURE packageSetBoxItemOTK (IN pBox varchar(50), IN pOrder varchar(50), IN pAlias varchar(100))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vPgID varchar(50);
  -- check if all books over OTK state, checks printgroup state if photo
  SELECT MIN(IFNULL(ob.state, 450)) state INTO vState
  FROM package_box_item bi
    INNER JOIN print_group pg ON pg.order_id = bi.order_id
    AND bi.alias = IFNULL(pg.alias, pg.path)
    AND pg.is_reprint = 0
    LEFT OUTER JOIN order_books ob ON pg.id = ob.pg_id
    AND ob.book BETWEEN bi.item_from AND bi.item_to
  WHERE bi.box_id = pBox
    AND bi.order_id = pOrder
    AND bi.alias = pAlias;
  IF vState >= 450 THEN
    -- set item state
    UPDATE package_box_item pbi
    SET pbi.state = 450,
        pbi.state_date = NOW()
    WHERE pbi.box_id = pBox
      AND pbi.order_id = pOrder
      AND pbi.alias = pAlias
      AND pbi.state < 450;
  END IF;

  -- check if order complited (in all boxes)
  SELECT MIN(ob.state) state INTO vState
  FROM package_box_item bi
  WHERE bi.order_id = pOrder;
  IF vState >= 450 THEN
    -- set order state
    UPDATE orders o
    SET o.state = 450,
        o.state_date = NOW()
    WHERE o.id = pOrder;
  END IF;
  -- TODO close extra state
  SELECT 'state' field, vState value;
END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageSetBoxPacked;

DELIMITER $$

CREATE
PROCEDURE packageSetBoxPacked (IN pBox varchar(50))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vGroup integer;
  DECLARE vSource integer;

  DECLARE vOrder varchar(50);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vCur CURSOR FOR
  SELECT pbi.order_id
  FROM package_box pb
    INNER JOIN package_box_item pbi ON pb.box_id = pbi.box_id
  WHERE pb.source = vSource
    AND pb.package_id = vGroup
  GROUP BY pbi.order_id
  HAVING MIN(pbi.state) >= 460;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  -- set box state
  UPDATE package_box pb
  SET pb.state = 460,
      pb.state_date = NOW()
  WHERE pb.box_id = pBox;

  SELECT pb.package_id, pb.source INTO vGroup, vSource
  FROM package_box pb
  WHERE pb.box_id = pBox;

  -- clean rack
  UPDATE rack_space
  SET package_source = 0,
      package_id = 0,
      box_id = ''
  WHERE package_source = vSource
    AND package_id = vGroup
    AND box_id = pBox;

  -- check completed orders 
  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vOrder;
    IF vIsEnd THEN
      LEAVE wet;
    END IF;
    -- update order
    UPDATE orders o
    SET o.state = 460,
        o.state_date = NOW()
    WHERE o.id = vOrder;
  END LOOP wet;
  CLOSE vCur;

  -- check boxes & set group state
  SELECT MIN(pb.state) INTO vState
  FROM package_box pb
  WHERE pb.source = vSource
    AND pb.package_id = vGroup;

  IF vState >= 460 THEN
    UPDATE package p
    SET p.state = 460,
        p.state_date = NOW()
    WHERE p.source = vSource
      AND p.id = vGroup
      AND p.state < 460;
    UPDATE orders o
    SET o.state = 460,
        o.state_date = NOW()
    WHERE o.source = vSource
      AND o.group_id = vGroup
      AND o.state < 460;
  END IF;
  -- TODO fix extra states

  SELECT 'package_state' field, vState value;

END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageSetBoxItemOTK;

DELIMITER $$

--
-- Создать процедуру `packageSetBoxItemOTK`
--
CREATE
PROCEDURE packageSetBoxItemOTK (IN pBox varchar(50), IN pOrder varchar(50), IN pAlias varchar(100))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vPgID varchar(50);
  -- check if all books over OTK state, checks printgroup state if photo
  SELECT MIN(IFNULL(ob.state, 450)) state INTO vState
  FROM package_box_item bi
    INNER JOIN print_group pg ON pg.order_id = bi.order_id
    AND bi.alias = IFNULL(pg.alias, pg.path)
    AND pg.is_reprint = 0
    LEFT OUTER JOIN order_books ob ON pg.id = ob.pg_id
    AND ob.book BETWEEN bi.item_from AND bi.item_to
  WHERE bi.box_id = pBox
    AND bi.order_id = pOrder
    AND bi.alias = pAlias;
  IF vState >= 450 THEN
    -- set item state
    UPDATE package_box_item pbi
    SET pbi.state = 450,
        pbi.state_date = NOW()
    WHERE pbi.box_id = pBox
      AND pbi.order_id = pOrder
      AND pbi.alias = pAlias
      AND pbi.state < 450;
  END IF;

  -- check if order complited (in all boxes)
  SELECT MIN(bi.state) state INTO vState
  FROM package_box_item bi
  WHERE bi.order_id = pOrder;
  IF vState >= 450 THEN
    -- set order state
    UPDATE orders o
    SET o.state = 450,
        o.state_date = NOW()
    WHERE o.id = pOrder;
  END IF;
  -- TODO close extra state
  SELECT 'state' field, vState value;
END
$$

DELIMITER ;

-- 2020-05-05 test cycle

INSERT INTO order_state (id, name, runtime, extra, tech, book_part) VALUES
(445, 'Производство', 0, 0, 0, 0);

DROP PROCEDURE IF EXISTS packageBoxCreate;

DELIMITER $$

CREATE
PROCEDURE packageBoxCreate (IN pSource int, IN pGroup int)
BEGIN
  -- box
  INSERT IGNORE INTO package_box (source
  , package_id
  , box_id
  , box_num
  , barcode
  , price
  , weight
  , state
  , state_date)
    SELECT p.source, p.id, CONCAT_WS('-', p.source, p.id), 0, IFNULL((SELECT pb.barcode FROM package_barcode pb WHERE pb.source = p.source AND pb.id = p.id AND pb.barcode IS NOT NULL AND pb.barcode != '' LIMIT 1), ''), 0, 0, 445, NOW()
    FROM package p
    WHERE p.source = pSource
      AND p.id = pGroup;

  -- items
  INSERT IGNORE INTO package_box_item (box_id
  , order_id
  , alias
  , item_from
  , item_to
  , type
  , state
  , state_date)
    SELECT CONCAT_WS('-', pSource, pGroup), pg.order_id, IFNULL(pg.alias, pg.path) alias, 1 item_from, MAX(pg.book_num) item_to, bt.name book_type_name, 445, NOW()
    FROM orders o
      INNER JOIN print_group pg ON o.id = pg.order_id
      INNER JOIN book_type bt ON pg.book_type = bt.id
    WHERE o.source = pSource
      AND o.group_id = pGroup
    GROUP BY pg.order_id, bt.name, pg.alias;

END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageBoxStartOTK;

DELIMITER $$

CREATE
PROCEDURE packageBoxStartOTK (IN pBoxID varchar(50), IN pPgID varchar(50))
BEGIN
  DECLARE vSource int;
  DECLARE vGroupID int;
  DECLARE vOrderID varchar(50);
  DECLARE vSubID varchar(50);
  DECLARE vAlias varchar(100);

  SELECT o.source, o.group_id, o.id, pg.sub_id, pg.alias INTO vSource, vGroupID, vOrderID, vSubID, vAlias
  FROM print_group pg
    INNER JOIN orders o ON pg.order_id = o.id
  WHERE pg.id = pPgID;

  -- package state
  UPDATE package p
  SET p.state = 449,
      p.state_date = NOW()
  WHERE p.source = vSource
    AND p.id = vGroupID
    AND p.state < 449;

  -- box state
  UPDATE package_box pb
  SET pb.state = 449,
      pb.state_date = NOW()
  WHERE pb.box_id = pBoxID
    AND pb.state < 449;

  -- item state
  UPDATE package_box_item pbi
  SET pbi.state = 449,
      pbi.state_date = NOW()
  WHERE pbi.box_id = pBoxID
    AND pbi.state < 449;

  -- start order exta state
  CALL extraStateStart(vOrderID, vSubID, 450, NOW());

END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageSetBoxItemOTK;

DELIMITER $$

CREATE
PROCEDURE packageSetBoxItemOTK (IN pBox varchar(50), IN pOrder varchar(50), IN pAlias varchar(100))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vOrderState integer(5) DEFAULT (0);
  DECLARE vPgID varchar(50);
  -- check if all books over OTK state, checks printgroup state if photo
  SELECT MIN(IFNULL(ob.state, 450)) state INTO vState
  FROM package_box_item bi
    INNER JOIN print_group pg ON pg.order_id = bi.order_id
    AND bi.alias = IFNULL(pg.alias, pg.path)
    AND pg.is_reprint = 0
    LEFT OUTER JOIN order_books ob ON pg.id = ob.pg_id
    AND ob.book BETWEEN bi.item_from AND bi.item_to
  WHERE bi.box_id = pBox
    AND bi.order_id = pOrder
    AND bi.alias = pAlias;
  IF vState >= 450 THEN
    -- set item state
    UPDATE package_box_item pbi
    SET pbi.state = 450,
        pbi.state_date = NOW()
    WHERE pbi.box_id = pBox
      AND pbi.order_id = pOrder
      AND pbi.alias = pAlias
      AND pbi.state < 450;
  END IF;

  -- check if order complited (in all boxes)
  SELECT MIN(bi.state) state INTO vOrderState
  FROM package_box_item bi
  WHERE bi.order_id = pOrder;
  IF vOrderState >= 450 THEN
    -- set order state
    UPDATE orders o
    SET o.state = 450,
        o.state_date = NOW()
    WHERE o.id = pOrder;
  END IF;
  -- TODO close extra state
  SELECT 'state' field, vState value;
END
$$

DELIMITER ;

-- 2020-05-06 test cycle

INSERT INTO delivery_type (id, name, hideClient) VALUES
(18, 'До отделения Европочты', 0),
(19, 'PickPoint', 0),
(20, 'Boxberry ', 0);

-- 2020-05-25 stone cycle

DROP PROCEDURE IF EXISTS packageSetBoxOTK;

DELIMITER $$

CREATE
PROCEDURE packageSetBoxOTK (IN pBox varchar(50))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);

  DECLARE vGroup integer;
  DECLARE vSource integer;
  DECLARE vOrder varchar(50);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vCur CURSOR FOR
  SELECT pbi.order_id
  FROM package_box pb
    INNER JOIN package_box_item pbi ON pb.box_id = pbi.box_id
  WHERE pb.source = vSource
    AND pb.package_id = vGroup
  GROUP BY pbi.order_id
  HAVING MIN(pbi.state) >= 450;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  -- check if all box items over OTK state
  -- get state by box items 
  SELECT MIN(bi.state) state INTO vState
  FROM package_box_item bi
  WHERE bi.box_id = pBox;
  IF vState >= 450 THEN
    -- set box state
    UPDATE package_box pb
    SET pb.state = 450,
        pb.state_date = NOW()
    WHERE pb.box_id = pBox;
  END IF;

  SELECT pb.source, pb.package_id INTO vSource, vGroup
  FROM package_box pb
  WHERE pb.box_id = pBox;

  -- check completed orders 
  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vOrder;
    IF vIsEnd THEN
      LEAVE wet;
    END IF;

    -- update order
    UPDATE orders o
    SET o.state = 450,
        o.state_date = NOW()
    WHERE o.id = vOrder;

    -- fix order extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      VALUES (vOrder, '', 450, NOW(), NOW())
    ON DUPLICATE KEY UPDATE state_date = NOW();

    -- stop all started order extra states
    UPDATE order_extra_state es
    SET es.state_date = NOW()
    WHERE es.id = vOrder
      -- AND es.sub_id = ''
      AND es.state < 450
      AND es.state_date IS NULL
    ORDER BY es.sub_id, es.state;

  END LOOP wet;
  CLOSE vCur;

  SELECT 'state' field, vState value;


END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageSetBoxPacked;

DELIMITER $$

CREATE
PROCEDURE packageSetBoxPacked (IN pBox varchar(50))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vGroup integer;
  DECLARE vSource integer;

  DECLARE vOrder varchar(50);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vCur CURSOR FOR
  SELECT pbi.order_id
  FROM package_box pb
    INNER JOIN package_box_item pbi ON pb.box_id = pbi.box_id
  WHERE pb.source = vSource
    AND pb.package_id = vGroup
  GROUP BY pbi.order_id
  HAVING MIN(pbi.state) >= 460;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  -- set box state
  UPDATE package_box pb
  SET pb.state = 460,
      pb.state_date = NOW()
  WHERE pb.box_id = pBox;

  SELECT pb.package_id, pb.source INTO vGroup, vSource
  FROM package_box pb
  WHERE pb.box_id = pBox;

  -- clean rack
  UPDATE rack_space
  SET package_source = 0,
      package_id = 0,
      box_id = ''
  WHERE package_source = vSource
    AND package_id = vGroup
    AND box_id = pBox;

  -- check completed orders 
  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vOrder;
    IF vIsEnd THEN
      LEAVE wet;
    END IF;
    -- update order
    UPDATE orders o
    SET o.state = 460,
        o.state_date = NOW()
    WHERE o.id = vOrder;

    -- fix order extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      VALUES (vOrder, '', 460, NOW(), NOW())
    ON DUPLICATE KEY UPDATE state_date = NOW();

    -- stop all started order extra states
    UPDATE order_extra_state es
    SET es.state_date = NOW()
    WHERE es.id = vOrder
      -- AND es.sub_id = ''
      AND es.state < 460
      AND es.state_date IS NULL
    ORDER BY es.sub_id, es.state;

  END LOOP wet;
  CLOSE vCur;

  -- check boxes & set group state
  SELECT MIN(pb.state) INTO vState
  FROM package_box pb
  WHERE pb.source = vSource
    AND pb.package_id = vGroup;

  IF vState >= 460 THEN
    UPDATE package p
    SET p.state = 460,
        p.state_date = NOW()
    WHERE p.source = vSource
      AND p.id = vGroup
      AND p.state < 460;
    UPDATE orders o
    SET o.state = 460,
        o.state_date = NOW()
    WHERE o.source = vSource
      AND o.group_id = vGroup
      AND o.state < 460;
  END IF;

  SELECT 'package_state' field, vState value;

END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS packageSetBoxSend;

DELIMITER $$

CREATE
PROCEDURE packageSetBoxSend (IN pBox varchar(50))
BEGIN
  DECLARE vState integer(5) DEFAULT (0);
  DECLARE vGroup integer;
  DECLARE vSource integer;

  -- set box state
  UPDATE package_box pb
  SET pb.state = 465,
      pb.state_date = NOW()
  WHERE pb.box_id = pBox
    AND pb.state < 465;

  SELECT pb.package_id, pb.source INTO vGroup, vSource
  FROM package_box pb
  WHERE pb.box_id = pBox;

  -- check boxes & set group state
  SELECT MIN(pb.state) INTO vState
  FROM package_box pb
  WHERE pb.source = vSource
    AND pb.package_id = vGroup;

  IF vState >= 465 THEN
    UPDATE package p
    SET p.state = 465,
        p.state_date = NOW()
    WHERE p.source = vSource
      AND p.id = vGroup
      AND p.state < 465;

    -- set orders state
    UPDATE orders o
    SET o.state = 465,
        o.state_date = NOW()
    WHERE o.source = vSource
      AND o.group_id = vGroup
      AND o.state < 465;

    -- fix order extra state
    INSERT INTO order_extra_state (id, sub_id, state, start_date, state_date)
      SELECT o.id, '', 465, NOW(), NOW()
      FROM orders o
      WHERE o.source = vSource
        AND o.group_id = vGroup
    ON DUPLICATE KEY UPDATE state_date = NOW();

    -- stop all started order extra states
    UPDATE order_extra_state es
    INNER JOIN orders o
      ON es.id = o.id
    SET es.state_date = NOW()
    WHERE o.source = vSource
      AND o.group_id = vGroup
      AND es.state < 465
      AND es.state_date IS NULL;

  END IF;

  SELECT 'package_state' field, vState value;

END
$$

DELIMITER ;

ALTER TABLE sources 
 ADD COLUMN caption VARCHAR(100) DEFAULT NULL AFTER name;
ALTER TABLE lab 
  ADD COLUMN efi TINYINT(1) DEFAULT 0;
  
INSERT INTO form_field (id, name, parametr, simplex) VALUES
(20, 'Коментарий', 'pcomment', 0);
INSERT INTO form_field_items (id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES
(18, 20, 0, 0, 0, 70, '', '', '');
INSERT INTO form (id, name, report) VALUES
(10, 'Наклейка', 'frmUniSticker');
INSERT INTO delivery_type_form (delivery_type, form) VALUES
(0, 10);
INSERT INTO form_parametr (form, form_field) VALUES
(10, 7),
(10, 1),
(10, 5),
(10, 18),
(10, 11),
(10, 20);
INSERT INTO src_type (id, loc_type, name, state, book_part) VALUES
(27, 2, 'Efi', 0, 0);

DROP PROCEDURE IF EXISTS techEfiPgPrinted;

DELIMITER $$

CREATE
PROCEDURE techEfiPgPrinted (IN pPgroup varchar(50), IN pTechPoint int)
MODIFIES SQL DATA
BEGIN
  DECLARE vDataValid int;
  DECLARE vOrderId varchar(50);
  DECLARE vSubId varchar(50);
  DECLARE vrepgroup varchar(50);
  DECLARE vState int;
  DECLARE vBooks int;
  DECLARE vPrints int;
  DECLARE vIsReprint int;
  DECLARE vDate datetime;
  DECLARE vBookPart int;
  DECLARE vMinState int;

  SET vDate = NOW();
  SET vState = 300;
  BEGIN
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET vDataValid = 0;

    SELECT pg.order_id, pg.sub_id, pg.book_num, pg.prints, pg.is_reprint, pg.book_part, 1 INTO vOrderId, vSubId, vBooks, vPrints, vIsReprint, vBookPart, vDataValid
    FROM print_group pg
    WHERE pg.id = pPgroup;
  END;

  IF vDataValid = 1 THEN
    UPDATE print_group pg
    SET pg.state = vState,
        pg.state_date = vDate,
        pg.prints_done = pg.prints
    WHERE pg.id = pPgroup
      AND pg.state >= 250
      AND pg.state < 300;

    -- set books
    UPDATE order_books b
    SET b.state = vState,
        b.state_date = vDate
    WHERE b.pg_id = pPgroup;

    -- check order pringroups
    SELECT IFNULL(MIN(pg.state), 0) INTO vMinState
    FROM print_group pg
    WHERE pg.order_id = vOrderId
      AND pg.sub_id = vSubId
      AND pg.is_reprint = 0;

    IF vMinState >= 300 THEN
      -- all pg printed end order/suborder
      CALL extraStateSet(vOrderId, vSubId, vState, vDate);
    END IF;

  END IF;

END
$$

DELIMITER ;

-- 2021-06-12 stone cycle

INSERT INTO attr_type (id, attr_fml, name, field, list, persist) VALUES
(100, 6, 'Отделение BoxBerry', 'bbPickup', 0, 1),
(101, 6, 'pickpoint город', 'ppCity', 0, 1),
(102, 6, 'pickup точка выдачи', 'ppName', 0, 1);

UPDATE form_field_items SET sequence = 3 WHERE id = 16;

INSERT INTO form_field (id, name, parametr, simplex) VALUES
(21, 'Отделение BoxBerry', 'pBBpoint', 0),
(22, 'Отделение pickpoint', 'pPPpoint', 0);

INSERT INTO form_field_items (id, form_field, sequence, is_field, child_field, attr_type, delemiter, prefix, sufix) VALUES
(20, 21, 0, 0, 0, 100, '', '-', ''),
(21, 18, 1, 1, 21, 0, '', '', ''),
(22, 22, 0, 0, 0, 101, '', '', ''),
(23, 22, 1, 0, 0, 102, ' ', '', ''),
(24, 18, 2, 1, 22, 0, ' ', '', '');

INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 100, 'address.bbPickup');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 101, 'pickpoint.city');
INSERT INTO attr_json_map(src_type, attr_type, json_key) VALUES(0, 102, 'pickpoint.pickup_name');

-- 2021-06-22 stone cycle