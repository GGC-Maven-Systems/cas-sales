/*
SQLyog Ultimate v8.55 
MySQL - 5.7.44-log : Database - gcasys_dbf
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `parameter_status_history` */

DROP TABLE IF EXISTS `parameter_status_history`;

CREATE TABLE `parameter_status_history` (
  `sTransNox` char(12) NOT NULL,
  `sTableNme` char(64) NOT NULL,
  `sSourceNo` char(25) DEFAULT NULL,
  `sPayloadx` json DEFAULT NULL,
  `sRemarksx` char(128) DEFAULT NULL,
  `sApproved` varchar(12) DEFAULT NULL,
  `dApproved` datetime DEFAULT NULL,
  `cRefrStat` char(1) DEFAULT NULL,
  `cTranStat` char(1) NOT NULL,
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sTransNox`),
  KEY `sTableNme` (`sTableNme`),
  KEY `sSourceNo` (`sSourceNo`),
  KEY `cTranStat` (`cTranStat`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
