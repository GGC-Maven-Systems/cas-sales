/*
SQLyog Ultimate v8.55 
MySQL - 5.7.44-log : Database - gcasys_dbf
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `sales_quotation_vehicle` */

DROP TABLE IF EXISTS `sales_quotation_vehicle`;

CREATE TABLE `sales_quotation_vehicle` (
  `sTransNox` varchar(12) NOT NULL,
  `sQotPayID` varchar(12) NOT NULL,
  `sBranchCd` char(4) NOT NULL,
  `dExpected` date DEFAULT NULL,
  `sReferNox` varchar(32) DEFAULT NULL,
  `sRemarksx` varchar(256) DEFAULT NULL,
  `cPaymForm` char(1) NOT NULL,
  `nTranTotl` decimal(10,2) NOT NULL DEFAULT '0.00',
  `nDiscount` decimal(8,2) DEFAULT '0.00',
  `nAddDiscx` decimal(8,2) DEFAULT '0.00',
  `nFreightx` decimal(10,2) DEFAULT '0.00',
  `nVATSales` decimal(10,2) DEFAULT '0.00',
  `nVATAmtxx` decimal(10,2) DEFAULT '0.00',
  `nNonVATSl` decimal(10,2) DEFAULT '0.00',
  `dValdThru` date DEFAULT NULL,
  `sSourceCd` varchar(4) DEFAULT NULL,
  `sSourceNo` varchar(12) DEFAULT NULL,
  `sVrntIDxx` varchar(5) NOT NULL,
  `sRecvMeth` varchar(12) DEFAULT NULL,
  `nTPLAmntx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nCompInsx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nLTOAmntx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nDocStmpx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nRsrvAmtx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nPrmPayDx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `sQuotRate` varchar(12) DEFAULT NULL,
  `nSRPAmntx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nDownPayx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nDownPctx` decimal(5,2) NOT NULL DEFAULT '0.00',
  `nFinAmntx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nDuration` smallint(6) NOT NULL,
  `nRateValx` decimal(7,4) NOT NULL DEFAULT '0.0000',
  `nMonthlyx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nGrsMnthI` decimal(15,2) DEFAULT '0.00',
  `nPromNote` decimal(15,2) DEFAULT '0.00',
  `nTotAmntx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `cRecdStat` char(1) NOT NULL DEFAULT '1',
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sQotPayID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
