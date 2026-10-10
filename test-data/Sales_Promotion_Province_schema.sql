DROP TABLE IF EXISTS `Sales_Promotion_Province`;

CREATE TABLE `Sales_Promotion_Province` (
  `sPromIDxx` varchar(12) NOT NULL,
  `nEntryNox` tinyint(3) unsigned NOT NULL,
  `sProvIDxx` char(4) DEFAULT NULL,
  `nFreightx` decimal(8,2) DEFAULT NULL,
  `cWithIncx` char(1) DEFAULT NULL,
  `nAmountxx` decimal(10,2) DEFAULT NULL,
  `cInsurFOC` char(1) DEFAULT NULL,
  `cRegisFOC` char(1) DEFAULT NULL,
  `cActivexx` char(1) DEFAULT '1',
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sPromIDxx`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
