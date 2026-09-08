DROP TABLE IF EXISTS Model;

CREATE TABLE `Model` (
  sModelIDx varchar(9) NOT NULL,
  sModelCde varchar(64) DEFAULT NULL,
  sDescript varchar(128) DEFAULT NULL,
  nMfgYearx smallint(6) DEFAULT NULL,
  sMainModl varchar(9) DEFAULT NULL,
  sBrandIDx varchar(8) DEFAULT NULL,
  sIndstCdx char(2) NOT NULL,
  cEndOfLfe char(1) DEFAULT '0',
  sPayLoadx varchar(512) DEFAULT NULL,
  cRecdStat char(1) DEFAULT '1',
  sModified varchar(32) DEFAULT NULL,
  dModified datetime DEFAULT NULL,
  dTimeStmp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (sModelIDx,sIndstCdx)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
