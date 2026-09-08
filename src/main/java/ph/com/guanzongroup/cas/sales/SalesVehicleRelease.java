/*
 * Copyright (c) 2026 Guanzon Group of Companies.
 * All rights reserved.
 *
 * @author TEEJEI DE CELIS
 * @date August 15, 2026
 */
package ph.com.guanzongroup.cas.sales;

import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Vehicle_Release_Master;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.SalesVehicleReleaseQueries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles Vehicle Release transactions within the CAS Sales module.
 *
 * <p>
 * This class provides the business logic for initializing, validating,
 * searching, retrieving, and opening Vehicle Release transactions. It
 * manages the transaction lifecycle and coordinates interactions between
 * the Vehicle Release master model, salesman records, VSP transactions,
 * and the database.
 * </p>
 *
 * <p>
 * The class supports employee and salesman searches, branch-specific
 * salesman searches, retrieval of VSP transactions available for release,
 * and opening of existing VSP transactions for Vehicle Release processing.
 * </p>
 *
 * <p>
 * The transaction uses {@link Model_Vehicle_Release_Master} as its primary
 * data model and inherits common transaction behaviors from the
 * {@link Parameter} base class.
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date August 15, 2026
 * @module Vehicle Release
 * @since 1.0
 */
public class SalesVehicleRelease extends Parameter {
    /**
     * Vehicle Release master model that stores the current transaction data.
     */
    Model_Vehicle_Release_Master poModel;
    /**
     * Initializes the Vehicle Release transaction controller.
     *
     * <p>
     * This method performs the initial setup required before any transaction
     * processing can occur. It:
     * </p>
     * <ul>
     *     <li>Sets the default record status to {@link RecordStatus#ACTIVE}.</li>
     *     <li>Creates a new instance of {@link Model_Vehicle_Release_Master}
     *         using the Sales Models factory.</li>
     *     <li>Invokes the parent class initialization to complete the setup
     *         of common transaction resources.</li>
     * </ul>
     *
     * @throws SQLException if a database access error occurs during
     *         initialization.
     * @throws GuanzonException if an application-specific error occurs while
     *         initializing the transaction.
     *
     * @author TEEJEI DE CELIS
     * @date August 7, 2026
     */
    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = RecordStatus.ACTIVE;
        poModel = new SalesModels(poGRider).VehicleReleaseMaster();
        super.initialize();
    }

    /**
     * Returns the current Vehicle Release master model associated with this
     * transaction.
     *
     * <p>
     * The returned model contains the transaction data that is created,
     * retrieved, updated, or saved by this controller.
     * </p>
     *
     * @return the current {@link Model_Vehicle_Release_Master} instance.
     *
     * @author TEEJEI DE CELIS
     * @date August 7, 2026
     */
    @Override
    public Model_Vehicle_Release_Master getModel() {
        return poModel;
    }

    /**
     * Validates the current Vehicle Release transaction before it is saved.
     *
     * <p>
     * This method performs the following validations:
     * </p>
     * <ul>
     *     <li>Verifies that the logged-in user has at least
     *         {@link UserRight#BRANCH_MANAGER} access rights.</li>
     *     <li>Ensures that a Client has been selected.</li>
     *     <li>Ensures that the Source Code is specified.</li>
     *     <li>Ensures that the Source Number is specified.</li>
     * </ul>
     *
     * <p>
     * If all validations pass, the method automatically updates the audit
     * information by setting the modifying user and the server modification
     * timestamp before allowing the transaction to proceed.
     * </p>
     *
     * @return a {@link JSONObject} containing the validation result:
     * <ul>
     *     <li><b>result</b> = {@code "success"} if the transaction is valid.</li>
     *     <li><b>result</b> = {@code "error"} together with a descriptive
     *         <b>message</b> if validation fails.</li>
     * </ul>
     *
     * @throws SQLException if a database access error occurs while performing
     *         validation or retrieving server-related information.
     *
     * @author TEEJEI DE CELIS
     * @date August 7, 2026
     */

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() < UserRight.BRANCH_MANAGER) {
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        }

        if (poModel.getClientId().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Client must not be empty.");
            return poJSON;
        }

        if (poModel.getSourceCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source Code must not be empty.");
            return poJSON;
        }

        if (poModel.getSourceNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source No. must not be empty.");
            return poJSON;
        }

        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Searches for an employee/salesman record based on the specified search value
     * and search mode, while applying the configured record status filter.
     *
     * <p>The method builds a record-status condition based on the value of
     * {@code psRecdStat}. If multiple record-status values are configured, the
     * values are used in an SQL {@code IN} clause. If only one value is configured,
     * an SQL equality condition is used.</p>
     *
     * <p>The resulting condition is appended to the SQL statement returned by
     * {@link #getSQ_Browse()}. The method then displays a browse dialog containing
     * the employee ID and salesman name. The selected employee record is loaded
     * through the model using the employee ID returned by the browse dialog.</p>
     *
     * <p>The search behavior is determined by the {@code byCode} parameter:
     * <ul>
     *     <li>{@code true} - searches/selects using the Employee ID column.</li>
     *     <li>{@code false} - searches/selects using the Salesman name column.</li>
     * </ul>
     * </p>
     *
     * <p>If no record is selected from the browse dialog, a JSON object containing
     * an error result and message is returned.</p>
     *
     * @param value
     *        the value to be used when searching for an employee or salesman
     *        record
     * @param byCode
     *        indicates whether the search is performed by employee ID
     *        ({@code true}) or by salesman name ({@code false})
     *
     * @return a {@link JSONObject} containing the selected employee record when a
     *         record is successfully selected; otherwise, a JSON object containing
     *         an error result and message
     *
     * @throws SQLException
     *         if a database access error occurs while executing the search or
     *         loading the selected record
     * @throws GuanzonException
     *         if an application-specific error occurs while processing the
     *         employee record
     */
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsCondition = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsCondition = " a.cRecdStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = " a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }

        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsCondition);

        System.out.println("SEARCH RECORD : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Salesman",
                "sEmployID»sFullName",
                "a.sEmployID»concat(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sEmployID"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    /**
     * Searches for an employee/salesman record based on the specified search
     * value, search mode, and branch code.
     *
     * <p>The search is restricted to the specified branch. If a record status
     * filter is configured through {@code psRecdStat}, the search is further
     * restricted based on the configured record status values. Multiple record
     * status values are handled using an SQL {@code IN} condition, while a single
     * record status value is handled using an SQL equality condition.</p>
     *
     * <p>The method displays a browse dialog containing the Employee ID and
     * Salesman name. The search column is determined by the {@code byCode}
     * parameter:</p>
     *
     * <ul>
     *     <li>{@code true} - searches by Employee ID.</li>
     *     <li>{@code false} - searches by Salesman name.</li>
     * </ul>
     *
     * <p>If a record is selected, the employee record is loaded through the
     * underlying model using the selected employee ID. If no record is selected,
     * an error JSON object is returned indicating that no record was loaded.</p>
     *
     * @param value
     *        the value to be used when searching for an employee or salesman
     * @param byCode
     *        indicates whether the search is performed by employee ID
     *        ({@code true}) or salesman name ({@code false})
     * @param branch
     *        the branch code used to restrict the employee/salesman search
     *
     * @return a {@link JSONObject} containing the selected employee record if a
     *         record is successfully selected; otherwise, a JSON object
     *         containing an error result and message
     *
     * @throws SQLException
     *         if a database access error occurs while executing the search or
     *         loading the selected record
     * @throws GuanzonException
     *         if an application-specific error occurs while processing the
     *         employee record
     */
    public JSONObject searchRecord(String value, boolean byCode, String branch) throws SQLException, GuanzonException {
        String lsCondition = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsCondition = " AND a.cRecdStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = " AND a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }

        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), "a.sBranchCd = " + SQLUtil.toSQL(branch));
        if(lsCondition != null && !"".equals(lsCondition)){
            lsSQL = lsSQL + lsCondition;
        }

        System.out.println("SEARCH RECORD : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Salesman",
                "sEmployID»sFullName",
                "a.sEmployID»concat(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sEmployID"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    /**
     * Searches for a salesman record based on the specified search value and
     * search mode.
     *
     * <p>The search query is generated using {@link SalesVehicleReleaseQueries#getSQL_Salesman()} and is
     * optionally filtered by the configured record status in {@code psRecdStat}.
     * If multiple record status values are configured, an SQL {@code IN}
     * condition is used; otherwise, a single equality condition is applied.</p>
     *
     * <p>The method displays a browse dialog containing the Employee ID and
     * Salesman name. The search column is determined by the {@code byCode}
     * parameter:</p>
     *
     * <ul>
     *     <li>{@code true} - searches by Employee ID.</li>
     *     <li>{@code false} - searches by Salesman name.</li>
     * </ul>
     *
     * <p>If a salesman is selected, the corresponding employee record is loaded
     * through the model using the selected employee ID. If no record is selected,
     * an error JSON object is returned indicating that no record was loaded.</p>
     *
     * @param value
     *        the value to be used when searching for a salesman
     * @param byCode
     *        indicates whether the search is performed by Employee ID
     *        ({@code true}) or Salesman name ({@code false})
     *
     * @return a {@link JSONObject} containing the selected salesman record if a
     *         record is successfully selected; otherwise, a JSON object
     *         containing an error result and message
     *
     * @throws SQLException
     *         if a database access error occurs while executing the search or
     *         loading the selected record
     * @throws GuanzonException
     *         if an application-specific error occurs while processing the
     *         salesman record
     */
    public JSONObject searchSalesMan(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsCondition = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsCondition = " a.cRecdStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = " a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }
        String lsSQL = MiscUtil.addCondition(SalesVehicleReleaseQueries.getSQL_Salesman(), lsCondition);
        System.out.println("SEARCH SALESMAN: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Salesman",
                "sEmployID»sFullName",
                "a.sEmployID»concat(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName)",
                byCode ? 0 : 1);
        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sEmployID"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    /**
     * Retrieves VSP records that are available for release, optionally filtered
     * by customer name and/or sales person.
     *
     * <p>The base SQL statement is obtained from {@link SalesVehicleReleaseQueries#getSQL_ForRelease()}.
     * When a customer name is provided, the result is filtered using a partial
     * match against the customer's company name. When a sales person is
     * provided, the result is filtered using a partial match against the sales
     * person's company name.</p>
     *
     * <p>If both filters are provided, the conditions are combined using an
     * SQL {@code AND} operator. The resulting records are sorted by transaction
     * date in ascending order.</p>
     *
     * <p>Each retrieved record is converted into a {@link JSONObject} containing
     * the transaction number, customer name, transaction date, and sales person.
     * The resulting records are returned as a {@link JSONArray} under the
     * {@code payload} property.</p>
     *
     * <p>If matching records are found, the returned JSON object contains a
     * {@code success} result. If no records are found, an {@code error} result
     * is returned with {@code continue} set to {@code true} and an empty
     * payload.</p>
     *
     * @param customerName
     *        the customer name to use as an optional partial-match filter;
     *        {@code null} or blank values are ignored
     * @param salesPerson
     *        the sales person name to use as an optional partial-match filter;
     *        {@code null} or blank values are ignored
     *
     * @return a {@link JSONObject} containing the search result, status message,
     *         and retrieved records in the {@code payload} property
     *
     * @throws SQLException
     *         if a database access error occurs while executing the query or
     *         retrieving the records
     * @throws GuanzonException
     *         if an application-specific error occurs while processing the
     *         request
     */
    public JSONObject RetreiveForReleaseVSP(String customerName,String salesPerson)
            throws SQLException, GuanzonException {

        JSONObject loJSON = new JSONObject();
        JSONArray loArray = new JSONArray();

        String lsSQL = SalesVehicleReleaseQueries.getSQL_ForRelease();

        List<String> loCondition = new ArrayList<>();

        if (customerName != null && !customerName.trim().isEmpty()) {
            loCondition.add(
                    "b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + customerName.trim() + "%")
            );
        }

        if (salesPerson != null && !salesPerson.trim().isEmpty()) {
            loCondition.add(
                    "e.sCompnyNm LIKE " + SQLUtil.toSQL("%" + salesPerson.trim() + "%")
            );
        }

        if (!loCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(
                    lsSQL,
                    String.join(" AND ", loCondition)
            );
        }

        lsSQL += " ORDER BY a.dTransact ASC ";
        System.out.println("RETRIEVE FOR RELEASE VSP :");
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            while (loRS.next()) {
                JSONObject loData = new JSONObject();
                loData.put("sTransNox", loRS.getString("sTransNox"));
                loData.put("CustomerName", loRS.getString("CustomerName"));
                loData.put("dTransact", loRS.getString("dTransact"));
                loData.put("SalesPerson", loRS.getString("SalesPerson"));
                loArray.add(loData);
            }
        } finally {
            MiscUtil.close(loRS);
        }

        if (!loArray.isEmpty()) {
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
            loJSON.put("payload", loArray);
        } else {
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found.");
            loJSON.put("payload", new JSONArray());
        }

        return loJSON;
    }
    /**
     * Retrieves add-on records associated with the specified VSP transaction.
     *
     * <p>
     * The query retrieves Giveaway, Labor, and Parts records associated with
     * the specified transaction number. The records are returned in the order
     * of Giveaway, Labor, and Parts.
     * </p>
     *
     * <p>
     * Each retrieved record is converted into a {@link JSONObject} containing
     * the add-on number, add-on type, transaction number, barcode, description,
     * quantity, and status. Fields that are not applicable to Labor or Parts
     * are returned as {@code "-"}.
     * </p>
     *
     * <p>
     * If matching records are found, the returned JSON object contains a
     * {@code success} result. If no records are found, an {@code error}
     * result is returned with {@code continue} set to {@code true} and an
     * empty payload.
     * </p>
     *
     * @param transNo
     *        the VSP transaction number used to retrieve the associated
     *        add-on records
     *
     * @return a {@link JSONObject} containing the search result, status
     *         message, and retrieved records in the {@code payload} property
     *
     * @throws SQLException
     *         if a database access error occurs while executing the query
     *         or retrieving the records
     * @throws GuanzonException
     *         if an application-specific error occurs while processing
     *         the request
     */
    public JSONObject RetreiveForReleaseVSPAddOns(String transNo)
            throws SQLException, GuanzonException {

        JSONObject loJSON = new JSONObject();
        JSONArray loArray = new JSONArray();

        String lsSQL = SalesVehicleReleaseQueries.getSQL_AddOns();

        lsSQL = lsSQL.replace(
                ":sTransNox",
                SQLUtil.toSQL(transNo.trim())
        );

        System.out.println("RETRIEVE VSP ADD ONS :");
        System.out.println(lsSQL);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            while (loRS.next()) {
                JSONObject loData = new JSONObject();
                loData.put("AddOnType", loRS.getString("AddOnType"));
                loData.put("TransactionNo", loRS.getString("TransactionNo"));
                loData.put("Barcode", loRS.getString("Barcode"));
                loData.put("Description", loRS.getString("Description"));
                loData.put("Qty", loRS.getBigDecimal("Qty"));
                loData.put("Status", loRS.getString("Status"));

                loArray.add(loData);
            }
        } finally {
            MiscUtil.close(loRS);
        }

        if (!loArray.isEmpty()) {
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
            loJSON.put("payload", loArray);
        } else {
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found.");
            loJSON.put("payload", new JSONArray());
        }

        return loJSON;
    }
    /**
     * Opens an existing VSP transaction and transfers the relevant customer and
     * transaction information to the current model.
     *
     * <p>The method initializes a new {@link VSP} transaction controller through
     * {@link SalesControllers}, then attempts to open the transaction identified
     * by the specified transaction number. If the transaction cannot be opened,
     * the error result and message returned by the VSP controller are propagated
     * to the caller.</p>
     *
     * <p>When the transaction is successfully opened, the method retrieves the
     * client ID, address ID, contact ID, and transaction number from the VSP
     * master record and sets them in the current model using the corresponding
     * model setter methods.</p>
     *
     * <p>The returned JSON object contains a {@code success} result when the
     * transaction is successfully opened and its related information is loaded.</p>
     *
     * @param fsTransNox
     *        the unique transaction number identifying the VSP transaction to
     *        open
     *
     * @return a {@link JSONObject} containing the result of the operation and,
     *         when successful, the related transaction information; otherwise,
     *         an error result and message
     *
     * @throws CloneNotSupportedException
     *         if an error occurs while cloning an object required by the VSP
     *         transaction processing
     * @throws SQLException
     *         if a database access error occurs while initializing or opening
     *         the VSP transaction
     * @throws GuanzonException
     *         if an application-specific error occurs during VSP transaction
     *         processing
     */
    public JSONObject OpenVSPTransaction(String fsTransNox) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        VSP poVSP;
        poVSP = new SalesControllers(poGRider, logwrapr).VSP();
        poJSON = poVSP.InitTransaction();
        poJSON = poVSP.OpenTransaction(fsTransNox);
        if ("error".equals(poJSON.get("result"))) {
            poJSON.put("result", "error");
            poJSON.put("message", (String) poJSON.get("message"));
            return poJSON;
        }

        poJSON = poModel.setClientId(poVSP.Master().getClientId());
        poJSON = poModel.setAddressId(poVSP.Master().getAddressId());
        poJSON = poModel.setContactId(poVSP.Master().getContactId());
        poJSON = poModel.setSourceNo(poVSP.Master().getTransactionNo());
//        poJSON = poModel.setSourceCode(poVSP.Master().gets());
        poJSON.put("result", "success");
        return poJSON;
    }
    /**
     * Retrieves giveaway transactions available for release.
     *
     * <p>The method retrieves giveaway transaction details together with
     * the corresponding inventory barcode and description. Optional filters
     * may be applied using the source code and source number.</p>
     *
     * <p>The retrieved records include the transaction number, entry number,
     * stock ID, given quantity, issued quantity, giveaway status, source code,
     * source number, barcode, and item description.</p>
     *
     * @param sourceCode the source code used to filter giveaway transactions;
     *                   may be {@code null} or empty to ignore this filter
     * @param sourceNo the source number used to filter giveaway transactions;
     *                 may be {@code null} or empty to ignore this filter
     * @return a {@link JSONObject} containing the result status, message,
     *         and retrieved giveaway records
     * @throws SQLException if an error occurs while executing the SQL query
     * @throws GuanzonException if a business or application error occurs
     */
    public JSONObject RetrieveForReleaseGiveaways(
            String sourceCode,
            String sourceNo
    ) throws SQLException, GuanzonException {

        JSONObject loJSON = new JSONObject();
        JSONArray loArray = new JSONArray();

        String lsSQL = SalesVehicleReleaseQueries.getSQL_Giveaways();
        List<String> loCondition = new ArrayList<>();

        if (sourceCode != null && !sourceCode.trim().isEmpty()) {
            loCondition.add(
                    "a.sSourceCD = " + SQLUtil.toSQL(sourceCode.trim())
            );
        }

        if (sourceNo != null && !sourceNo.trim().isEmpty()) {
            loCondition.add(
                    "a.sSourceNo = " + SQLUtil.toSQL(sourceNo.trim())
            );
        }

        if (!loCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(
                    lsSQL,
                    String.join(" AND ", loCondition)
            );
        }

        lsSQL += " ORDER BY a.sTransNox, a.nEntryNox ASC ";

        System.out.println("Retrieve For ReleaseGiveaways :");
        System.out.println(lsSQL);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            while (loRS.next()) {
                JSONObject loData = new JSONObject();

                loData.put("sTransNox", loRS.getString("sTransNox"));
                loData.put("nEntryNox", loRS.getInt("nEntryNox"));
                loData.put("sStockIDx", loRS.getString("sStockIDx"));
                loData.put("nGivenxxx", loRS.getBigDecimal("nGivenxxx"));
                loData.put("nIssuedxx", loRS.getBigDecimal("nIssuedxx"));
                loData.put("GiveawayStatus", loRS.getString("GiveawayStatus"));
                loData.put("sSourceCD", loRS.getString("sSourceCD"));
                loData.put("sSourceNo", loRS.getString("sSourceNo"));
                loData.put("sBarCodex", loRS.getString("sBarCodex"));
                loData.put("sDescript", loRS.getString("sDescript"));

                loArray.add(loData);
            }
        } finally {
            MiscUtil.close(loRS);
        }

        if (!loArray.isEmpty()) {
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
            loJSON.put("payload", loArray);
        } else {
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found.");
            loJSON.put("payload", new JSONArray());
        }
        return loJSON;
    }
}
