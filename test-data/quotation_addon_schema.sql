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
/*Table structure for table `quotation_addon` */

DROP TABLE IF EXISTS `quotation_addon`;

CREATE TABLE `quotation_addon` (
  `sTransNox` char(12) NOT NULL,
  `sQotAddID` varchar(12) NOT NULL,
  `sAddOnIDx` varchar(12) DEFAULT NULL,
  `nAmountxx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nQtyxxxxx` smallint(6) NOT NULL DEFAULT '1',
  `nTotAmntx` decimal(15,2) NOT NULL DEFAULT '0.00',
  `nDiscAmnt` decimal(15,2) DEFAULT '0.00',
  `nDiscPctx` decimal(5,2) DEFAULT '0.00',
  `cRecdStat` char(1) NOT NULL DEFAULT '1',
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`,`sQotAddID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
