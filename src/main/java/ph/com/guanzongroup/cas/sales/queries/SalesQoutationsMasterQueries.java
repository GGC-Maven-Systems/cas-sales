package ph.com.guanzongroup.cas.sales.queries;

import org.guanzon.appdriver.base.SQLUtil;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationVersionStatic;
import ph.com.guanzongroup.cas.sales.status.SalesVehicleReleaseStatic;

/**
 * Provides centralized SQL statements for the Sales Vehicle Release module.
 *
 * <p>
 * This class contains SQL queries used to retrieve salesman information,
 * vehicle release transactions, giveaway transactions, and vehicle
 * add-on information such as labor and parts.
 * </p>
 *
 * <p>
 * The class serves only as a centralized repository for SQL statements.
 * It does not execute queries, perform database operations, or manage
 * database connections. Query execution is handled by the appropriate
 * data-access or business-logic classes.
 * </p>
 *
 * <p>
 * <strong>Queries provided:</strong>
 * </p>
 *
 * <ul>
 *     <li>
 *         {@link #getSQL_Browse()} -
 *         Retrieves salesman records for browsing.
 *     </li>
 *     <li>
 *         {@link #getSQL_Salesman()} -
 *         Retrieves detailed salesman information, including employee,
 *         client, branch, department, and position details.
 *     </li>
 *     <li>
 *         {@link #getSQL_ForRelease()} -
 *         Retrieves VSP transactions available for vehicle release,
 *         including customer and sales person information.
 *     </li>
 *     <li>
 *         {@link #getSQL_Giveaways()} -
 *         Retrieves giveaway transactions together with their
 *         corresponding inventory information.
 *     </li>
 *     <li>
 *         {@link #getSQL_AddOns()} -
 *         Retrieves and combines giveaway, labor, and parts
 *         add-on transactions into a single result set.
 *     </li>
 * </ul>
 *
 * <hr>
 *
 * <p>
 * <strong>Author:</strong> TEEJEI DE CELIS<br>
 * <strong>Date:</strong> August 20, 2026<br>
 * <strong>Module:</strong> Vehicle Release<br>
 * <strong>Since:</strong> 1.0
 * </p>
 */
public class SalesQoutationsMasterQueries {
    /**
     * Returns the SQL statement used to retrieve salesman records for browsing.
     *
     * <p>
     * The query retrieves the employee ID, branch code, employee name
     * components, record status, and concatenated full name from the
     * {@code Salesman} table.
     * </p>
     *
     * @return the SQL query used for the salesman browse operation
     */
    public static String getSQL_Browse() {
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

    public static String SQL_MCItem() {
        String lsSQL = "SELECT "
                // Inventory
                + "  a.sStockIDx"
                + ", a.sBarCodex"
                + ", a.sDescript AS xStockDesc"
                + ", a.cRecdStat AS xStockStat"
                + ", a.nUnitPrce"
                + ", a.sIndstCdx"
                + ", a.sCategCd1"

                // Model
                + ", b.sModelIDx"
                + ", b.sModelCde"
                + ", b.sDescript AS xModelNme"
                + ", b.cRecdStat AS xModelStat"

                // Brand
                + ", c.sBrandIDx"
                + ", c.sBrandCde"
                + ", c.sDescript AS xBrandNme"
                + ", c.cRecdStat AS xBrandStat"

                // Model Variant
                + ", d.sVrntIDxx"
                + ", d.sDescript AS xVrntName"
                + ", d.cRecdStat AS xVrntStat"

                // Color
                + ", e.sColorIDx"
                + ", e.sColorCde"
                + ", e.sDescript AS xColorNme"
                + ", e.cRecdStat AS xColorStat"

                + " FROM Inventory a"
                + " LEFT JOIN Model b ON a.sModelIDx = b.sModelIDx"
                + " LEFT JOIN Brand c ON b.sBrandIDx = c.sBrandIDx"
                + " LEFT JOIN Model_Variant d ON a.sVrntIDxx = d.sVrntIDxx"
                + " LEFT JOIN Color e ON a.sColorIDx = e.sColorIDx";

        return lsSQL;
    }
    public static String SQL_GawayItem() {
        String lsSQL = "SELECT "
                // Inventory
                + "  a.sStockIDx"
                + ", a.sBarCodex"
                + ", a.sDescript AS xStockDesc"
                + ", a.cRecdStat AS xStockStat"
                + ", a.nUnitPrce"
                + ", a.sIndstCdx"
                + ", a.sCategCd1"

                // Brand
                + ", c.sBrandIDx"
                + ", c.sBrandCde"
                + ", c.sDescript AS xBrandNme"
                + ", c.cRecdStat AS xBrandStat"

                // Color
                + ", e.sColorIDx"
                + ", e.sColorCde"
                + ", e.sDescript AS xColorNme"
                + ", e.cRecdStat AS xColorStat"

                + " FROM Inventory a"
                + " LEFT JOIN Brand c ON a.sBrandIDx = c.sBrandIDx"
                + " LEFT JOIN Color e ON a.sColorIDx = e.sColorIDx";

        return lsSQL;
    }
    /**
     * Returns the SQL statement used to retrieve detailed salesman information.
     *
     * <p>
     * The query retrieves salesman information together with the associated
     * employee, client, branch, department, and position details. The related
     * records are joined using {@code LEFT JOIN} so that the salesman record
     * remains available even when one or more related records are missing.
     * </p>
     *
     * <p>
     * The returned query includes the following information:
     * </p>
     *
     * <ul>
     *     <li>Employee ID</li>
     *     <li>Branch code and branch name</li>
     *     <li>Salesman full name</li>
     *     <li>Record status</li>
     *     <li>Client/company name</li>
     *     <li>Department name</li>
     *     <li>Position name</li>
     * </ul>
     *
     * @return the SQL query used to retrieve detailed salesman information
     */
    public static String SQL_MCItemPromo() {
        return " SELECT "
                + "    a.sPromIDxx "
                + "  , a.sPromDesc "
                + "  , a.dFromDate "
                + "  , a.dThruDate "
                + "  , b.sModelIDx "
                + "  , b.nTotalAmt "
                + "  , b.nDiscRate "
                + "  , b.nDiscAmtx "
                + "  , b.nFreightx "
                + "  , br.sBrandIDx "
                + "  , pv.sProvIDxx "
                + "  , ar.sAreaCode "
                + "  , bk.sBankIDxx "
                + " FROM Sales_Promotion_Master a "
                + " JOIN Sales_Promotion_Model b "
                + "      ON b.sPromIDxx = a.sPromIDxx "
                + "     AND b.cActivexx = '1' "
                + " LEFT JOIN Sales_Promotion_Model_Exception e "
                + "      ON e.sPromIDxx = a.sPromIDxx "
                + "     AND e.sModelIDx = b.sModelIDx "
                + "     AND e.cActivexx = '1' "
                + " LEFT JOIN Sales_Promotion_Brand br "
                + "      ON br.sPromIDxx = a.sPromIDxx "
                + "     AND br.cActivexx = '1' "
                + " LEFT JOIN Sales_Promotion_Province pv "
                + "      ON pv.sPromIDxx = a.sPromIDxx "
                + "     AND pv.cActivexx = '1' "
                + " LEFT JOIN Sales_Promotion_Branch_Area ar "
                + "      ON ar.sPromIDxx = a.sPromIDxx "
                + "     AND ar.cActivexx = '1' "
                + " LEFT JOIN Sales_Promotion_Bank bk "
                + "      ON bk.sPromIDxx = a.sPromIDxx ";
    }
    /**
     * Returns the SQL statement used to retrieve VSP transactions that are
     * available for release.
     *
     * <p>
     * The query retrieves the VSP transaction number and transaction date,
     * together with the associated customer and sales person information.
     * </p>
     *
     * <p>
     * The query joins the VSP master record with the client master,
     * sales inquiry master, salesman, and client master tables to obtain
     * the customer and sales person names.
     * </p>
     *
     * @return the SQL query used to retrieve VSP transactions for release
     */
    public static String getSQL_ForRelease() {
        return " SELECT "
                + "    a.sTransNox "
                + "  , b.sCompnyNm AS CustomerName "
                + "  , a.dTransact "
                + "  , e.sCompnyNm AS SalesPerson "
                + " FROM Vsp_Master a "
                + " LEFT JOIN Client_Master b "
                + "        ON b.sClientID = a.sClientID "
                + " LEFT JOIN Sales_Inquiry_Master c "
                + "        ON c.sTransNox = a.sInqryIDx "
                + " LEFT JOIN Salesman d "
                + "        ON d.sEmployID = c.sSalesman "
                + " LEFT JOIN Client_Master e "
                + "        ON e.sClientID = d.sEmployID ";
    }
    /**
     * Returns the SQL statement used to retrieve giveaway transactions
     * together with their corresponding inventory information.
     *
     * <p>
     * The query retrieves the transaction number, entry number, stock ID,
     * giveaway quantity, issued quantity, giveaway status, source code,
     * and source number from the {@code Sales_Giveaways} table.
     * </p>
     *
     * <p>
     * The query also joins the {@code Inventory} table to retrieve the
     * barcode and description of the corresponding giveaway item.
     * </p>
     *
     * @return the SQL query used to retrieve giveaway transactions and
     *         their associated inventory information
     */
    public static String getSQL_Giveaways() {
        return " SELECT "
                + "    a.sTransNox "
                + "  , a.nEntryNox "
                + "  , a.sStockIDx "
                + "  , a.nGivenxxx "
                + "  , a.nIssuedxx "
                + "  , a.cGAwyStat AS GiveawayStatus "
                + "  , a.sSourceCD "
                + "  , a.sSourceNo "
                + "  , b.sBarCodex "
                + "  , b.sDescript "
                + " FROM Sales_Giveaways a "
                + " LEFT JOIN Inventory b "
                + "        ON b.sStockIDx = a.sStockIDx ";
    }
    /**
     * Returns the SQL statement used to retrieve add-on transactions
     * together with their corresponding inventory and labor information.
     *
     * <p>
     * The query combines Giveaway, Labor, and Parts transactions into
     * a single result set. The records are ordered by add-on type using
     * the add-on type identifiers defined in
     * {@link SalesVehicleReleaseStatic.AddOnType}.
     * </p>
     *
     * <p>
     * Giveaway records are retrieved from the {@code Sales_Giveaways}
     * table and joined with the {@code Inventory} table to retrieve the
     * barcode and description of the corresponding item. The add-on type
     * is displayed using the Giveaway description defined in
     * {@link SalesVehicleReleaseStatic.AddOnType#ADD_ON_DESC_GIVEAWAYS}.
     * </p>
     *
     * <p>
     * Labor records are retrieved from the
     * {@code Joborder_Estimate_Labor} table and joined with the
     * {@code Labor} table to retrieve the labor description. Since
     * labor records do not have a barcode or status, their values are
     * returned as {@code "-"}. The add-on type is displayed using the
     * Labor description defined in
     * {@link SalesVehicleReleaseStatic.AddOnType#ADD_ON_DESC_LABOR}.
     * </p>
     *
     * <p>
     * Parts records are retrieved from the
     * {@code Joborder_Estimate_Parts} table and joined with the
     * {@code Inventory} table to retrieve the barcode and description
     * of the corresponding item. Since parts records do not have a
     * status, the status value is returned as {@code "-"}. The add-on
     * type is displayed using the Parts description defined in
     * {@link SalesVehicleReleaseStatic.AddOnType#ADD_ON_DESC_PARTS}.
     * </p>
     *
     * <p>
     * The query uses the {@code :sTransNox} named parameter to filter
     * records belonging to the specified transaction. A sequential row
     * number is generated for each result based on the add-on type order
     * and transaction number.
     * </p>
     *
     * @return the SQL query used to retrieve Giveaway, Labor, and Parts
     *         add-on transactions and their associated information
     */
    public static String getSQL_AddOns() {
        return " SELECT "
//                + "    ROW_NUMBER() OVER ( "
//                + "        ORDER BY AddOnOrder, TransactionNo "
//                + "    ) AS No "
                + "  , AddOnType "
                + "  , TransactionNo "
                + "  , Barcode "
                + "  , Description "
                + "  , Qty "
                + "  , Status "
                + " FROM ( "
                + "    /* GIVEAWAY */ "
                + "    SELECT "
                + "        " + SalesVehicleReleaseStatic.AddOnType.ADD_ON_TYPE_GIVEAWAYS + " AS AddOnOrder "
                + "      , '" + SalesVehicleReleaseStatic.AddOnType.ADD_ON_DESC_GIVEAWAYS + "' AS AddOnType "
                + "      , a.sTransNox AS TransactionNo "
                + "      , b.sBarCodex AS Barcode "
                + "      , b.sDescript AS Description "
                + "      , a.nGivenxxx AS Qty "
                + "      , a.cGAwyStat AS Status "
                + "    FROM Sales_Giveaways a "
                + "    LEFT JOIN Inventory b "
                + "        ON b.sStockIDx = a.sStockIDx "
                + "    WHERE a.sSourceNo = :sTransNox "
                + " "
                + "    UNION ALL "
                + " "
                + "    /* LABOR */ "
                + "    SELECT "
                + "        " + SalesVehicleReleaseStatic.AddOnType.ADD_ON_TYPE_LABOR + " AS AddOnOrder "
                + "      , '" + SalesVehicleReleaseStatic.AddOnType.ADD_ON_DESC_LABOR + "' AS AddOnType "
                + "      , a.sTransNox AS TransactionNo "
                + "      , '-' AS Barcode "
                + "      , e.sLaborNme AS Description "
                + "      , a.nQuantity AS Qty "
                + "      , '-' AS Status "
                + "    FROM Joborder_Estimate_Labor a "
                + "    LEFT JOIN Joborder_Estimate_Master m "
                + "        ON m.sTransNox = a.sTransNox "
                + "    LEFT JOIN Labor e "
                + "        ON e.sLaborIDx = a.sLaborCde "
                + "    WHERE m.sSourceNo = :sTransNox "
                + " "
                + "    UNION ALL "
                + " "
                + "    /* PARTS */ "
                + "    SELECT "
                + "        " + SalesVehicleReleaseStatic.AddOnType.ADD_ON_TYPE_PARTS + " AS AddOnOrder "
                + "      , '" + SalesVehicleReleaseStatic.AddOnType.ADD_ON_DESC_PARTS + "' AS AddOnType "
                + "      , p.sTransNox AS TransactionNo "
                + "      , b.sBarCodex AS Barcode "
                + "      , b.sDescript AS Description "
                + "      , p.nQuantity AS Qty "
                + "      , '-' AS Status "
                + "    FROM Joborder_Estimate_Parts p "
                + "    LEFT JOIN Joborder_Estimate_Master m "
                + "        ON m.sTransNox = p.sTransNox "
                + "    LEFT JOIN Inventory b "
                + "        ON b.sStockIDx = p.sStockIDx "
                + "    WHERE m.sSourceNo = :sTransNox "
                + " ) x "
                + " ORDER BY "
                + "    AddOnOrder "
                + "  , TransactionNo ";
    }

    /**
     * Returns the SQL statement used to list Sales Quotations for the
     * quotation list (tree table parent rows).
     *
     * <p>
     * Retrieves the quotation number, date, status, client and client name
     * from {@code Sales_Quotation_Master} joined with {@code Client_Master}.
     * No condition or ordering is included; the caller adds them.
     * </p>
     *
     * @return the SQL query used to list Sales Quotations
     */
//    public static String SQL_QuotationList() {
//        return " SELECT "
//                + "    a.sTransNox "
//                + "  , a.dTransact "
//                + "  , a.cTranStat "
//                + "  , a.nVersionx "
//                + "  , a.sClientID "
//                + "  , IFNULL(b.sCompnyNm, '') AS sCompnyNm "
//                + "  , v.sTransNox AS sVersnNox "
//                + "  , v.cTranStat AS cVersStat "
//                + " FROM Sales_Quotation_Master a "
//                + " LEFT JOIN Client_Master b "
//                + "        ON b.sClientID = a.sClientID "
//                + " INNER JOIN Sales_Quotation_Version_Master v "
//                + "        ON v.sParentID = a.sTransNox "
//                + "       AND v.sTransNox = ( SELECT MAX(v2.sTransNox) "
//                + "                             FROM Sales_Quotation_Version_Master v2 "
//                + "                            WHERE v2.sParentID = a.sTransNox ) ";
//    }

    public static String SQL_QuotationList() {
        return " SELECT "
                + "    a.sTransNox "
                + "  , a.dTransact "
                + "  , a.cTranStat "
                + "  , a.nVersionx "
                + "  , a.sClientID "
                + "  , IFNULL(b.sCompnyNm, '') AS sCompnyNm "
                + "  , v.sTransNox AS sVersnNox "
                + "  , v.cTranStat AS cVersStat "
                + "  , CASE WHEN v.cTranStat = " + SQLUtil.toSQL(SalesQoutationVersionStatic.CONFIRMED)
                + "         THEN ( SELECT MAX(h.dModified) "
                + "                  FROM Transaction_Status_History h "
                + "                 WHERE h.sSourceNo = v.sTransNox "
                + "                   AND h.sTableNme = 'Sales_Quotation_Version_Master' "
                + "                   AND h.cRefrStat = '1' ) "
                + "         ELSE NULL END AS dConfirmd "
                + " FROM Sales_Quotation_Master a "
                + " LEFT JOIN Client_Master b "
                + "        ON b.sClientID = a.sClientID "
                + " INNER JOIN Sales_Quotation_Version_Master v "
                + "        ON v.sParentID = a.sTransNox "
                + "       AND v.sTransNox = ( SELECT MAX(v2.sTransNox) "
                + "                             FROM Sales_Quotation_Version_Master v2 "
                + "                            WHERE v2.sParentID = a.sTransNox ) ";
    }

    /**
     * Returns the SQL statement used to list the versions of Sales
     * Quotations for the quotation list (tree table child rows).
     *
     * <p>
     * Retrieves each version's number, parent quotation number, date,
     * status, total amount and valid-through date from
     * {@code Sales_Quotation_Version_Master}. The parent quotation and its
     * client are joined as {@code a} and {@code b}, so the same conditions
     * used with {@link #SQL_QuotationList()} can be applied. No condition or
     * ordering is included; the caller adds them.
     * </p>
     *
     * @return the SQL query used to list Sales Quotation versions
     */
    public static String SQL_QuotationVersionList() {
        return " SELECT "
                + "    v.sTransNox "
                + "  , v.sParentID "
                + "  , v.dTransact "
                + "  , v.cTranStat "
                + "  , v.nTranTotl "
                + "  , v.dValdThru "
                + "  , CASE WHEN v.cTranStat = " + SQLUtil.toSQL(SalesQoutationVersionStatic.CONFIRMED)
                + "         THEN ( SELECT MAX(h.dModified) "
                + "                  FROM Transaction_Status_History h "
                + "                 WHERE h.sSourceNo = v.sTransNox "
                + "                   AND h.sTableNme = 'Sales_Quotation_Version_Master' "
                + "                   AND h.cRefrStat = '1' ) "
                + "         ELSE NULL END AS dConfirmd "
                + " FROM Sales_Quotation_Version_Master v "
                + " INNER JOIN Sales_Quotation_Master a "
                + "        ON a.sTransNox = v.sParentID "
                + " LEFT JOIN Client_Master b "
                + "        ON b.sClientID = a.sClientID ";
    }
}