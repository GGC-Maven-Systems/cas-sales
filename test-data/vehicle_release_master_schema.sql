DROP TABLE IF EXISTS `vehicle_release_master`;

CREATE TABLE `vehicle_release_master` (
  `sTransNox` char(12) NOT NULL,
  `dTransact` date DEFAULT NULL,
  `sReferNox` varchar(12) DEFAULT NULL,
  `sClientID` varchar(12) DEFAULT NULL,
  `sAddrssID` varchar(12) DEFAULT NULL,
  `sContctID` varchar(12) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `sSourceCD` char(4) DEFAULT NULL,
  `sSourceNo` char(12) DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `sModified` varchar(12) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`),
  KEY `ReferNo` (`sReferNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
