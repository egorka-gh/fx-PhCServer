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