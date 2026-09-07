DROP TABLE IF EXISTS `Company`;

CREATE TABLE `Company` (
  `sCompnyID` char(4) NOT NULL,
  `sCompnyNm` char(64) DEFAULT NULL,
  `sCompnyCd` char(8) DEFAULT NULL,
  `sAddressx` char(128) DEFAULT NULL,
  `sTownIDxx` char(4) DEFAULT NULL,
  `sTaxIDNox` char(16) DEFAULT NULL,
  `sEmplyrNo` char(16) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `sModified` char(32) DEFAULT NULL,
  `dModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sCompnyID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
