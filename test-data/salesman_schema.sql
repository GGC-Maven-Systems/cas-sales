DROP TABLE IF EXISTS `Salesman`;

CREATE TABLE `Salesman` (
  `sEmployID` char(12) NOT NULL,
  `sBranchCd` char(4) DEFAULT NULL,
  `sLastName` varchar(20) DEFAULT NULL,
  `sFrstName` varchar(20) DEFAULT NULL,
  `sMiddName` varchar(20) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `sModified` varchar(12) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sEmployID`),
  KEY `cRecdStat` (`cRecdStat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
