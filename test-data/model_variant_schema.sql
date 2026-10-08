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
/*Table structure for table `model_variant` */

DROP TABLE IF EXISTS `model_variant`;

CREATE TABLE `model_variant` (
  `sVrntIDxx` varchar(5) NOT NULL,
  `sDescript` varchar(64) NOT NULL,
  `nSelPrice` decimal(11,2) DEFAULT NULL,
  `nYearMdlx` smallint(6) DEFAULT NULL,
  `sPayloadx` varchar(2048) DEFAULT NULL,
  `sModelIDx` varchar(9) NOT NULL,
  `sColorIDx` varchar(7) NOT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `cEndOfLfe` char(1) DEFAULT NULL,
  `sModified` char(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sVrntIDxx`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
