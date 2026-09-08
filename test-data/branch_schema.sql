DROP TABLE IF EXISTS `Branch`;

CREATE TABLE `Branch` (
  `sBranchCd` varchar(4) NOT NULL,
  `sBranchNm` varchar(50) DEFAULT NULL,
  `sDescript` varchar(50) DEFAULT NULL,
  `sCompnyID` varchar(4) DEFAULT NULL,
  `sIndstCdx` varchar(64) DEFAULT NULL,
  `sAddressx` varchar(50) DEFAULT NULL,
  `sTownIDxx` varchar(4) DEFAULT NULL,
  `sManagerx` varchar(12) DEFAULT NULL,
  `sSellCode` varchar(2) DEFAULT NULL,
  `cWareHous` char(1) DEFAULT NULL,
  `sTelNumbr` varchar(50) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT NULL,
  `sContactx` varchar(50) DEFAULT NULL,
  `sEMailAdd` varchar(50) DEFAULT NULL,
  `dExportxx` datetime DEFAULT NULL,
  `cSrvcCntr` char(1) DEFAULT NULL,
  `cAutomate` char(1) DEFAULT NULL,
  `cMainOffc` char(1) DEFAULT NULL,
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sBranchCd`),
  KEY `sTownIDxx` (`sTownIDxx`),
  KEY `sManagerx` (`sManagerx`),
  KEY `sBranchNm` (`sBranchNm`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
