INSERT INTO `lab_stop_type` (`id`, `name`) VALUES (1, 'Нет заказов');
INSERT INTO `lab_stop_type` (`id`, `name`) VALUES (2, 'Замена рулона');

ALTER TABLE `lab` ADD `soft_speed` INT  NULL DEFAULT 0;
UPDATE `lab` SET `is_managed`=0;