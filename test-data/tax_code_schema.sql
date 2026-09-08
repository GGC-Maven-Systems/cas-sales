DROP TABLE IF EXISTS `Tax_Code`;

CREATE TABLE `Tax_Code` (
  `sTaxCodex` char(5) NOT NULL,
  `sRegRatex` decimal(5,2) DEFAULT NULL,
  `sGovtRate` decimal(5,2) DEFAULT NULL,
  `dLastUpdt` date DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT NULL,
  `sModified` char(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTaxCodex`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
