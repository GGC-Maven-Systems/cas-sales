DROP TABLE IF EXISTS `Sales_Quotation_Master`;

CREATE TABLE `Sales_Quotation_Master` (
  `sTransNox` varchar(12) NOT NULL,
  `sIndstCdx` varchar(2) DEFAULT NULL,
  `sCategrCd` varchar(7) DEFAULT NULL,
  `dTransact` date DEFAULT NULL,
  `sClientID` varchar(12) DEFAULT NULL,
  `sAddrssID` varchar(12) DEFAULT NULL,
  `sContctID` varchar(12) DEFAULT NULL,
  `nVersionx` tinyint(4) unsigned DEFAULT NULL,
  `cTranStat` char(1) DEFAULT NULL,
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
