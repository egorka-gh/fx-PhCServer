SET NAMES 'utf8';
SET SESSION sql_mode='NO_AUTO_VALUE_ON_ZERO';

INSERT INTO xrep_report(id, src_type, name, rep_group, hidden) VALUES ('reprintLog', 1, 'Перепечатка', 0, 0);
INSERT INTO xrep_report_params(report, parameter) VALUES ('reprintLog', 'period');