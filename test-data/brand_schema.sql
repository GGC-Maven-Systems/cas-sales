DROP TABLE IF EXISTS `Brand`;

CREATE TABLE `Brand` (
  `sBrandIDx` varchar(7) NOT NULL,
  `sDescript` varchar(64) NOT NULL,
  `sBrandCde` varchar(25) DEFAULT NULL,
  `sIndstCdx` char(2) NOT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sBrandIDx`,`sIndstCdx`),
  KEY `sIndstCdx` (`sIndstCdx`),
  KEY `cRecdStat` (`cRecdStat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
