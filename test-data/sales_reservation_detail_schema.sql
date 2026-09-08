DROP TABLE IF EXISTS `Sales_Reservation_Detail`;

CREATE TABLE `Sales_Reservation_Detail` (
  `sTransNox` varchar(12) NOT NULL,
  `nEntryNox` smallint(6) NOT NULL,
  `sStockIDx` varchar(12) DEFAULT NULL,
  `nQuantity` decimal(10,4) DEFAULT NULL,
  `nUnitPrce` decimal(11,4) DEFAULT NULL,
  `nMinDownx` decimal(10,4) DEFAULT NULL,
  `cClassify` char(1) DEFAULT NULL,
  `nApproved` decimal(10,4) DEFAULT NULL,
  `nIssuedxx` decimal(10,4) DEFAULT NULL,
  `nCancelld` decimal(10,4) DEFAULT NULL,
  `sNotesxxx` varchar(128) DEFAULT NULL,
  `cReversed` char(1) DEFAULT '+',
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
