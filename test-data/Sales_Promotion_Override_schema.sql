DROP TABLE IF EXISTS `Sales_Promotion_Override`;

CREATE TABLE `Sales_Promotion_Override` (
  `sPromIDxx` varchar(12) NOT NULL,
  `nEntryNox` tinyint(3) unsigned NOT NULL,
  `sOngoingx` varchar(12) DEFAULT NULL,
  `dModified` date DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sPromIDxx`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
