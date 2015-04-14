-- main 2015-04-13
-- moskva 
-- valichek

SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';


DELIMITER $$


DROP PROCEDURE IF EXISTS packageGetOrderSpace$$

CREATE 
PROCEDURE packageGetOrderSpace (IN pOrderId varchar(50))
READS SQL DATA
BEGIN

  SELECT r.name rack_name, rs.name, IFNULL(SUM(IF(o.id = oei.id, oei.weight, 0)), 0) / 1000 weight, IFNULL(SUM(oei.weight), 0) / 1000 unused_weight
    FROM orders o
      INNER JOIN orders o1 ON o1.source = o.source AND o1.group_id = o.group_id
      LEFT OUTER JOIN rack_orders ro ON ro.order_id = o.id
      LEFT OUTER JOIN rack_space rs ON rs.id = ro.space
      LEFT OUTER JOIN rack r ON r.id = rs.rack
      LEFT OUTER JOIN order_extra_info oei ON o1.id = oei.id AND oei.sub_id = ''
    WHERE o.id = pOrderId;

END
$$

DELIMITER ;

ALTER TABLE book_synonym
  ADD COLUMN has_backprint TINYINT(1) DEFAULT 1 AFTER synonym_type;