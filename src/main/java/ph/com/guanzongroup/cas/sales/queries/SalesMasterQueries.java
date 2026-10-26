package ph.com.guanzongroup.cas.sales.queries;

import org.guanzon.appdriver.base.SQLUtil;
import ph.com.guanzongroup.cas.sales.mcpromo.common.SalesPromotionStatus;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationVersionStatic;
import ph.com.guanzongroup.cas.sales.status.SalesVehicleReleaseStatic;

public class SalesMasterQueries {

    public static String getSQL_Salesman() {
        return " SELECT "
                + "    a.sEmployID "
                + "  , a.sBranchCd "
                + "  , a.sLastName "
                + "  , a.sFrstName "
                + "  , a.sMiddName "
                + "  , a.cRecdStat "
                + "  , CONCAT(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName) AS sFullName "
                + " FROM Salesman a ";
    }

    public static String getSalesClientBrowse(String industrycode) {

        return "SELECT * FROM (SELECT"
                + "          IF(b.sClientID IS NULL, 'NO', 'YES') AS xHasReserv"
                + ",\n       IF(c.sClientID IS NULL, 'NO', 'YES') AS xHasQuotat"
                + ",\n       IF(d.sClientID IS NULL, 'NO', 'YES') AS xHasComitment"
                + ",\n       IF(e.sEmployID IS NULL, 'NO', 'YES') AS xIsEmploye"
                + ",\n       IFNULL(a.sCompnyNm,CONCAT(a.sLastName+', '+a.sFrstName , ' ',a.sMiddName))"
                + ",\n       a.dBirthDte "
                + ",\n       f. sAddressx"
                + ",\n       a.sClientID "
                + "     FROM Client_Master a"
                + "\n LEFT JOIN (SELECT DISTINCT sClientID FROM Sales_Reservation_Master WHERE cTranStat = 1 "
                + "\n       AND sIndstCdx =" + SQLUtil.toSQL(industrycode) + ") b ON a.sClientID = b.sClientID "
                + "\n LEFT JOIN (SELECT DISTINCT sClientID FROM Sales_Quotation_Master WHERE cTranStat = '0' "
                + "         AND sIndstCdx = " + SQLUtil.toSQL(industrycode) + " AND sTransNox IN (SELECT sParentID FROM Sales_Quotation_Version_Master WHERE cTranStat = '1')) c ON a.sClientID = c.sClientID "
                + "\n LEFT JOIN (SELECT DISTINCT sClientID FROM Sales_Commitment_Master WHERE cTranStat = '1' )   d ON a.sClientID = d.sClientID"
                + "\n LEFT JOIN Employee_Master001 e ON a.sClientID = e.sEmployID"
                + "\n LEFT JOIN (SELECT DISTINCT sClientID,sAddressx FROM Client_Address  WHERE cPrimaryx = 1) f  ON a.sClientID = f.sClientID"
                + ") ClientSource ";

    }

    public static String getSalesClientSource(String industrycode) {

        String lsName = "IF(IFNULL(b.`sCompnyNm`, '') <> '', b.`sCompnyNm`,"
                + " CONCAT(b.`sLastName`, ', ', b.`sFrstName`, ' ', IFNULL(b.`sMiddName`, '')))";

        return "SELECT * FROM ("
                + "\n   SELECT 'Sales Reservation' AS sSource"
                + ",\n          a.sTransNox"
                + ",\n          a.dTransact"
                + ",\n          " + lsName + " AS xClientNm"
                + ",\n          a.sClientID"
                + "\n     FROM Sales_Reservation_Master a"
                + "\n     JOIN Client_Master b ON a.sClientID = b.sClientID"
                + "\n    WHERE a.`cTranStat` = 1 AND a.sIndstCdx = " + SQLUtil.toSQL(industrycode)
                + "\n   UNION ALL"
                + "\n   SELECT 'Sales Quotation' AS sSource"
                + ",\n          a.sTransNox"
                + ",\n          a.dTransact"
                + ",\n          " + lsName + " AS xClientNm"
                + ",\n          a.sClientID"
                + "\n     FROM Sales_Quotation_Master a"
                + "\n     JOIN Client_Master b ON a.sClientID = b.sClientID"
                + "\n    WHERE a.`cTranStat` = 1 AND a.sIndstCdx = " + SQLUtil.toSQL(industrycode)
                + "             AND a.sTransNox IN (SELECT sParentID FROM Sales_Quotation_Version_Master WHERE cTranStat = '1')"
                + "\n   UNION ALL"
                + "\n   SELECT 'Sales Commitment' AS sSource"
                + ",\n          a.sTransNox"
                + ",\n          a.dTransact"
                + ",\n          " + lsName + " AS xClientNm"
                + ",\n          a.sClientID"
                + "\n     FROM Sales_Commitment_Master a"
                + "\n     JOIN Client_Master b ON a.sClientID = b.sClientID"
                + "\n    WHERE a.`cTranStat` = 1"
                + "\n ) x";
    }

    public static String getSalesPromotion(String model, String brand) {
        boolean lbHasModel = model != null && !model.trim().isEmpty();
        boolean lbHasBrand = brand != null && !brand.trim().isEmpty();
        
        // nothing to search by -> return no rows instead of every promo
        if (!lbHasModel && !lbHasBrand) {
            return "SELECT a.* FROM Sales_Promotion_Master a WHERE 1 = 0";
        }

        StringBuilder lsMatch = new StringBuilder();

        // master -> model
        if (lbHasModel) {
            lsMatch.append("\n       EXISTS (SELECT 1 FROM Sales_Promotion_Model m")
                    .append("\n                WHERE m.sPromIDxx = a.sPromIDxx")
                    .append("\n                  AND m.sModelIDx = ").append(SQLUtil.toSQL(model.trim())).append(")");
        }

        // master -> brand
        if (lbHasBrand) {
            if (lsMatch.length() > 0) {
                lsMatch.append("\n    OR");
            }
            lsMatch.append("\n       EXISTS (SELECT 1 FROM Sales_Promotion_Brand b")
                    .append("\n                WHERE b.sPromIDxx = a.sPromIDxx")
                    .append("\n                  AND b.sBrandIDx = ").append(SQLUtil.toSQL(brand.trim())).append(")");
        }

        String lsSQL = "SELECT DISTINCT a.* FROM Sales_Promotion_Master a"
                + "\n WHERE (" + lsMatch + "\n )";

        // exception only makes sense when a model was given
        if (lbHasModel) {
            lsSQL += "\n AND NOT EXISTS (SELECT 1 FROM Sales_Promotion_Model_Exception x"
                    + "\n                  WHERE x.sPromIDxx = a.sPromIDxx"
                    + "\n                    AND x.sModelIDx = " + SQLUtil.toSQL(model.trim()) + ")";
        }

        // only live promos
        lsSQL += "\n AND a.cTranStat NOT IN ("
                + SQLUtil.toSQL(SalesPromotionStatus.OPEN) + ", "
                + SQLUtil.toSQL(SalesPromotionStatus.VOID) + ", "
                + SQLUtil.toSQL(SalesPromotionStatus.REPLACED) + ", "
                + SQLUtil.toSQL(SalesPromotionStatus.CANCELLED) + ")"
                // only promos valid today (date part only)
                + "\n AND DATE(a.dFromDate) <= CURDATE()"
                + "\n AND (a.dThruDate IS NULL OR DATE(a.dThruDate) >= CURDATE())";

        return lsSQL;
    }
}
