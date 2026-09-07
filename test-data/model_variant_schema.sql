DROP TABLE IF EXISTS `Model_Variant`;

CREATE TABLE `Model_Variant` (
  `sVrntIDxx` varchar(5) NOT NULL,
  `sDescript` varchar(64) NOT NULL,
  `nSelPrice` decimal(11,2) DEFAULT NULL,
  `nYearMdlx` smallint(6) DEFAULT NULL,
  `sPayloadx` varchar(2048) DEFAULT NULL,
  `sModelIDx` varchar(9) NOT NULL,
  `sColorIDx` varchar(7) NOT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `sModified` char(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sVrntIDxx`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
