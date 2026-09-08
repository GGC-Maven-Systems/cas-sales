DROP TABLE IF EXISTS `Sales_Inquiry_Requirements`;

CREATE TABLE `Sales_Inquiry_Requirements` (
  `sTransNox` char(12) NOT NULL,
  `nEntryNox` smallint(6) NOT NULL,
  `cCustGrpx` char(1) DEFAULT NULL,
  `sRqrmtCde` char(4) DEFAULT NULL,
  `cRequired` char(1) DEFAULT NULL,
  `cSubmittd` char(1) DEFAULT NULL,
  `sReceived` char(12) DEFAULT NULL,
  `dReceived` datetime DEFAULT NULL,
  `sModified` char(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
