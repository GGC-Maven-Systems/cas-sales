DROP TABLE IF EXISTS `Sales_Quotation_Detail`;

CREATE TABLE `Sales_Quotation_Detail` (
  `sTransNox` char(12) NOT NULL,
  `nEntryNox` smallint(6) NOT NULL,
  `sPromoCde` char(8) DEFAULT NULL,
  `sStockIDx` char(12) DEFAULT NULL,
  `nQuantity` decimal(8,2) DEFAULT NULL,
  `nUnitPrce` decimal(10,2) DEFAULT NULL,
  `nDiscount` decimal(5,2) DEFAULT NULL,
  `nAddDiscx` decimal(8,2) DEFAULT NULL,
  `nDownPaym` decimal(8,2) DEFAULT NULL,
  `cWithVATx` char(1) DEFAULT '0',
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
