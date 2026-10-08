DROP TABLE IF EXISTS `financing_rate_master`;

CREATE TABLE `financing_rate_master` (
             `sRateIDxx`  VARCHAR(12) NOT NULL,
             `sBankIDxx`  VARCHAR(9)  NULL,
             `nDuration`  SMALLINT    NOT NULL,
             `nRateValx`  DECIMAL(7,2) NOT NULL DEFAULT 0.00,
             `nDIRatexx`  DECIMAL(7,2) NOT NULL DEFAULT 0.00,
             `nSIRatexx`  DECIMAL(7,2) NOT NULL DEFAULT 0.00,
             `cRecdStat`  CHAR(1)     NOT NULL DEFAULT '0',
             `sModified`  VARCHAR(32) NULL,
             `dModified`  DATETIME    NULL,
             `dTimeStmp`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
             PRIMARY KEY (`sRateIDxx`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;