DROP TABLE IF EXISTS `Industry`;

CREATE TABLE `Industry` (
  `sIndstCdx` char(2) NOT NULL,
  `sDescript` char(64) NOT NULL,
  `sCompnyID` char(128) NOT NULL,
  `cIsIndstr` char(1) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `sModified` char(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sIndstCdx`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
