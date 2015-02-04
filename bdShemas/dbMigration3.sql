SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO xrep_report(id, src_type, name, rep_group, hidden) VALUES ('reprintLog', 1, 'Перепечатка', 0, 0);
INSERT INTO xrep_report_params(report, parameter) VALUES ('reprintLog', 'period');

DELETE FROM order_state WHERE id = 212;

UPDATE order_state SET  name = 'Ошибка записи.' WHERE  id = -310;
UPDATE order_state SET  name = 'Ошибка чтения.' WHERE  id = -309;
UPDATE order_state SET  name = 'В очереди размещения на печать' WHERE  id = 203;
UPDATE order_state SET  name = 'Размещен на печать' WHERE  id = 250;
