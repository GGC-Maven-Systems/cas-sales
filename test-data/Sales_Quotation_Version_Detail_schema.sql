DROP TABLE IF EXISTS `Sales_Quotation_Version_Detail`;

CREATE TABLE `Sales_Quotation_Version_Detail` (
  `sTransNox` varchar(12) NOT NULL,
  `nEntryNox` tinyint(4) unsigned NOT NULL,
  `sPromCode` varchar(12) DEFAULT NULL,
  `sStockIDx` varchar(12) DEFAULT NULL,
  `nQuantity` smallint(6) DEFAULT NULL,
  `nUnitPrce` decimal(10,2) DEFAULT NULL,
  `nDiscount` decimal(8,2) DEFAULT NULL,
  `nAddDiscx` decimal(8,2) DEFAULT NULL,
  `nFreightx` decimal(8,2) DEFAULT NULL,
  `nRegisAmt` decimal(8,2) DEFAULT NULL,
  `nInsAmtxx` decimal(8,2) DEFAULT NULL,
  `cWithVATx` char(1) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
