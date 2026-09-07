DROP TABLE IF EXISTS `Client_Address`;

CREATE TABLE `Client_Address` (
  `sAddrssID` char(12) NOT NULL,
  `sClientID` char(12) DEFAULT NULL,
  `sHouseNox` varchar(5) DEFAULT NULL,
  `sAddressx` varchar(128) DEFAULT NULL,
  `sBrgyIDxx` varchar(7) DEFAULT NULL,
  `sTownIDxx` varchar(5) DEFAULT NULL,
  `nLatitude` decimal(15,11) DEFAULT NULL,
  `nLongitud` decimal(15,11) DEFAULT NULL,
  `cPrimaryx` char(1) DEFAULT '0',
  `cOfficexx` char(1) DEFAULT '0',
  `cProvince` char(1) DEFAULT '0',
  `cBillingx` char(1) DEFAULT '0',
  `cShipping` char(1) DEFAULT '0',
  `cCurrentx` char(1) DEFAULT '0',
  `cLTMSAddx` char(1) DEFAULT '0',
  `sSourceCd` varchar(4) DEFAULT NULL,
  `sReferNox` varchar(12) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sAddrssID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
