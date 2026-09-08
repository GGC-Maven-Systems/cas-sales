DROP TABLE IF EXISTS `Sales_Quotation_Detail_Installment`;

CREATE TABLE `Sales_Quotation_Detail_Installment` (
  `sTransNox` char(12) NOT NULL,
  `nEntryNox` smallint(6) NOT NULL,
  `nAcctTerm` smallint(6) NOT NULL,
  `nMonAmort` decimal(8,2) DEFAULT NULL,
  `nRebatesx` decimal(8,2) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`,`nAcctTerm`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
