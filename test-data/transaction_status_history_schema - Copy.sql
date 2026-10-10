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
/*Table structure for table `transaction_status_history` */

DROP TABLE IF EXISTS `transaction_status_history`;

CREATE TABLE `transaction_status_history` (
  `sTransNox` VARCHAR(12) NOT NULL,
  `sTableNme` VARCHAR(64) NOT NULL,
  `sSourceNo` VARCHAR(12) DEFAULT NULL,
  `sPayloadx` json DEFAULT NULL,
  `sRemarksx` VARCHAR(128) DEFAULT NULL,
  `sApproved` VARCHAR(12) DEFAULT NULL,
  `dApproved` DATETIME DEFAULT NULL,
  `cRefrStat` CHAR(1) DEFAULT NULL,
  `cTranStat` CHAR(1) NOT NULL,
  `sModified` VARCHAR(32) DEFAULT NULL,
  `dModified` DATETIME DEFAULT NULL,
  `dTimeStmp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
