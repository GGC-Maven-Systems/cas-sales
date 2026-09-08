DROP TABLE IF EXISTS `Inv_Type`;

CREATE TABLE `Inv_Type` (
  `sInvTypCd` char(4) NOT NULL,
  `sDescript` char(32) NOT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `sModified` char(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sInvTypCd`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
