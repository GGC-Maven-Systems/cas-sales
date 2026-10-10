DROP TABLE IF EXISTS `Sales_Registration`;

CREATE TABLE `Sales_Registration` (
  `sTransNox` varchar(12) NOT NULL,
  `nEntryNox` tinyint(3) NOT NULL,
  `sSerialID` varchar(12) DEFAULT NULL,
  `sInsTypID` varchar(4) NOT NULL,
  `nInsAmtxx` decimal(9,2) DEFAULT NULL,
  `cRegisTyp` char(1) DEFAULT NULL,
  `cChargeTo` char(1) DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`nEntryNox`,`sInsTypID`),
  KEY `SerialID` (`sSerialID`),
  KEY `InsTypeID` (`sInsTypID`),
  KEY `RegistrationType` (`cRegisTyp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
