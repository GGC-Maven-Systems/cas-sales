DROP TABLE IF EXISTS `Sales_Quotation_Followup`;

CREATE TABLE `Sales_Quotation_Followup` (
  `sTransNox` varchar(12) NOT NULL,
  `nEntryNox` tinyint(4) unsigned NOT NULL,
  `sReferNox` varchar(12) DEFAULT NULL,
  `dFollowUp` date DEFAULT NULL,
  `dNextFlup` date DEFAULT NULL,
  `sFllwUpby` varchar(32) DEFAULT NULL,
  `cFllwUpTp` char(1) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
