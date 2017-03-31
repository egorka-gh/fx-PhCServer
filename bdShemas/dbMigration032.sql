-- main    
-- new main (virt)    
-- moskva 
-- ua 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO form(id, name, report) VALUES(5, 'Печать ШК2', 'mpBarcodeFrm2');
INSERT INTO delivery_type_form(delivery_type, form) VALUES(0, 5);

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES(506, 'Отменен не актуален', 0, 0, 0, 0);

DELIMITER $$

DROP PROCEDURE IF EXISTS sync_4Lvalid$$
CREATE
PROCEDURE sync_4Lvalid()
  MODIFIES SQL DATA
  COMMENT 'синхронизация актуальности заказов для качалки'
BEGIN
  DECLARE vId integer(7) DEFAULT (0);
  DECLARE vIsEnd int DEFAULT (0);

  DECLARE vCur CURSOR FOR
  SELECT s.id
    FROM sources s
    WHERE s.online = 1 AND s.type=4;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET vIsEnd = 1;

  OPEN vCur;
wet:
  LOOP
    FETCH vCur INTO vId;
    IF vIsEnd THEN
      LEAVE wet;
    END IF;
    CALL syncS4Lvalid(vId);
  END LOOP wet;
  CLOSE vCur;
  UPDATE orders_load ol SET ol.state = IF(ol.ftp_folder IS NULL, 530, 529), state_date = NOW() WHERE ol.state IN(505,506);
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS syncS4Lvalid$$
CREATE
PROCEDURE syncS4Lvalid(IN pSourceId int)
  MODIFIES SQL DATA
main:
BEGIN

  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;

  IF NOT EXISTS (SELECT 1
        FROM tmp_orders t
        WHERE t.source = pSourceId)
  THEN
    LEAVE main;
  END IF;

  -- keep in transaction
  START TRANSACTION;

    -- cancel not in sync
    UPDATE orders_load o
    SET state = 506,
        state_date = NOW()
    WHERE o.source = pSourceId
    AND o.state < 465
    AND NOT EXISTS(SELECT 1
        FROM tmp_orders t
        WHERE t.source = pSourceId AND t.id=o.id);

    -- finalize
    DELETE
      FROM tmp_orders
    WHERE source = pSourceId;

  COMMIT;
END
$$

DELIMITER ;
