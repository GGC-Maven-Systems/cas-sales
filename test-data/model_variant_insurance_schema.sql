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
/*Table structure for table `model_variant_insurance` */

DROP TABLE IF EXISTS `model_variant_insurance`;

CREATE TABLE `model_variant_insurance` (
  `sVrntIDxx` varchar(5) NOT NULL,
  `sVhclType` varchar(12) NOT NULL,
  `sBodyType` varchar(12) NOT NULL,
  `nAuthCapx` smallint(6) DEFAULT '0',
  `sTransmss` varchar(12) NOT NULL,
  PRIMARY KEY (`sVrntIDxx`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
