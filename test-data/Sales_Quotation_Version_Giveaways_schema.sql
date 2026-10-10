DROP TABLE IF EXISTS `Sales_Quotation_Version_Giveaways`;

CREATE TABLE `Sales_Quotation_Version_Giveaways` (
  `sTransNox` varchar(12) NOT NULL,
  `nEntryNox` tinyint(4) unsigned NOT NULL,
  `sStockIDx` varchar(12) DEFAULT NULL,
  `nQuantity` smallint(6) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
