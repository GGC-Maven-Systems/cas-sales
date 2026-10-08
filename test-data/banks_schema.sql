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

/*Table structure for table `banks` */



DROP TABLE IF EXISTS `banks`;



CREATE TABLE `banks` (
  `sBankIDxx` varchar(9) NOT NULL,
  `sBRSTNxxx` varchar(9) DEFAULT NULL,
  `sBankName` varchar(30) NOT NULL,
  `sBankCode` varchar(10) NOT NULL,
  `cFinTypex` char(1) DEFAULT NULL,
  `cTerminal` char(1) DEFAULT NULL,
  `cRecdStat` char(1) DEFAULT '1',
  `sModified` varchar(32) DEFAULT NULL,
  `dModified` datetime DEFAULT NULL,
  `dTimeStmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sBankIDxx`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;

/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

