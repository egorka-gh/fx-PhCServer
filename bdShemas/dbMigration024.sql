-- main 2015-08-18 
-- moskva 
-- reserv 
-- valichek
-- poligon

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO order_state(id, name, runtime, extra, tech, book_part) VALUES (139, 'Ожидание цветокоррекции', 0, 0, 0, 0);

ALTER TABLE suborders ADD COLUMN color_corr TINYINT(1) DEFAULT 0 AFTER alias;

DELIMITER $$
CREATE 
PROCEDURE forwardSubOrderState (IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pDate datetime)
BEGIN
  DECLARE vMinState int;
  IF pSubOrder != ''
  THEN
    UPDATE suborders s
    SET s.state = pState,
        s.state_date = pDate
    WHERE s.order_id = pOrder
    AND s.sub_id = pSubOrder
    AND s.state < pState;

    SELECT IFNULL(MIN(so.state), 0) state
    INTO vMinState
      FROM suborders so
      WHERE so.order_id = pOrder;

    IF vMinState >= pState
    THEN
      UPDATE orders o
      SET o.state = vMinState,
          o.state_date = pDate
      WHERE o.id = pOrder
      AND o.state < vMinState;
    END IF;

  END IF;
END
$$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS orderCleanUp$$

CREATE
PROCEDURE orderCleanUp (IN pId varchar(50), IN pState int)
MODIFIES SQL DATA
BEGIN
  DECLARE vDate datetime DEFAULT NOW();

  DELETE
    FROM print_group
  WHERE order_id = pId;

  IF pState = 100
  THEN
    DELETE
      FROM order_extra_info
    WHERE id = pId;
    DELETE
      FROM suborders
    WHERE order_id = pId;
  END IF;

  UPDATE orders
  SET state = pState,
      state_date = vDate,
      resume_load = 0
  WHERE id = pId;

  UPDATE suborders
  SET state = pState,
      state_date = vDate
  WHERE order_id = pId;

  INSERT INTO state_log (order_id, state, state_date, comment)
    VALUES (pId, pState, vDate, 'reset');
END
$$

DELIMITER ;

-- main
