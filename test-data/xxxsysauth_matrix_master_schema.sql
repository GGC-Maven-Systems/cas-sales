DROP TABLE IF EXISTS `xxxsysauth_matrix_master`;

CREATE TABLE `xxxsysauth_matrix_master` (
  `sAuthType` varchar(24) NOT NULL,
  `sAuthDesc` varchar(128) NOT NULL,
  `sAuthGrpx` varchar(32) DEFAULT NULL,
  `nValueFrm` decimal(12,4) DEFAULT '0.0000',
  `nValueTox` decimal(12,4) DEFAULT '0.0000',
  `sRemarksx` varchar(512) NOT NULL,
  `nAuthLevl` smallint(6) NOT NULL,
  `sIndstCdx` char(2) DEFAULT NULL,
  `cAllowSys` char(1) DEFAULT '0',
  `cWithSpcl` char(1) DEFAULT '0',
  `cRecdStat` char(1) DEFAULT '0',
  `sModified` char(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sAuthType`),
  KEY `sIndstCdx` (`sIndstCdx`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
