package ph.com.guanzongroup.cas.sales.status;

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
public class SalesQoutationsFollowUpQueries {
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
    public static String getSQL_Salesman() {
        return " SELECT "
                + "    a.sEmployID "
                + "  , a.sBranchCd "
                + "  , CONCAT(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName) AS sFullName "
                + "  , a.cRecdStat "
                + "  , c.sCompnyNm "
                + "  , e.sDeptName "
                + "  , d.sBranchNm "
                + "  , f.sPositnNm "
                + " FROM Salesman a "
                + " LEFT JOIN Employee_Master001 b "
                + "        ON a.sEmployID = b.sEmployID "
                + " LEFT JOIN Client_Master c "
                + "        ON a.sEmployID = c.sClientID "
                + " LEFT JOIN Branch d "
                + "        ON d.sBranchCd = a.sBranchCd "
                + " LEFT JOIN Department e "
                + "        ON b.sDeptIDxx = e.sDeptIDxx "
                + " LEFT JOIN Position f "
                + "        ON b.sPositnID = f.sPositnID ";
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
}