/*
 * Copyright (c) 2026 Guanzon Group of Companies.
 * All rights reserved.
 */
package ph.com.guanzongroup.cas.sales;

import org.guanzon.appdriver.agent.ActionAuthManager;
import org.guanzon.appdriver.agent.MatrixAuthChecker;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Recurring_Expense_Payment_Monitor;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Master;
import ph.com.guanzongroup.cas.sales.queries.SalesQoutationsMasterQueries;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationStatic;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationVersionStatic;
import ph.com.guanzongroup.cas.sales.validator.Sales_Qoutation_Validator_MC;

import javax.sql.rowset.CachedRowSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStaticData.recurring_expense_payment;

/**
 * Transaction controller for Sales Quotation Version
 * (master: Sales_Quotation_Version_Master, detail: Sales_Quotation_Version_Detail).
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 */
public class SalesQoutationVersion extends Transaction {

    public JSONObject InitTransaction() {
        // TODO: replace with the 4-character source code registered for Sales Quotation Version
        SOURCE_CODE = "SQVr";

        poMaster = new SalesModels(poGRider).SalesQuotationVersionMaster();
        poDetail = new SalesModels(poGRider).SalesQuotationVersionDetail();

        paDetail = new ArrayList<>();

        return initialize();
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return super.newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return super.saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return super.openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return super.updateTransaction();
    }

    public String getStatus(String fsStatus) {
        switch (fsStatus) {
            case SalesQoutationVersionStatic.OPEN:
                return SalesQoutationVersionStatic.STATUS_DESCRIPTION.OPEN;
            case SalesQoutationVersionStatic.CONFIRMED:
                return SalesQoutationVersionStatic.STATUS_DESCRIPTION.CONFIRMED;
            case SalesQoutationVersionStatic.SALES:
                return SalesQoutationVersionStatic.STATUS_DESCRIPTION.SALES;
            case SalesQoutationVersionStatic.REJECTED:
                return SalesQoutationVersionStatic.STATUS_DESCRIPTION.REJECTED;
            case SalesQoutationVersionStatic.VOID:
                return SalesQoutationVersionStatic.STATUS_DESCRIPTION.VOID;
            case SalesQoutationVersionStatic.SUPERCEDED:
                return SalesQoutationVersionStatic.STATUS_DESCRIPTION.SUPERCEDED;
            case SalesQoutationVersionStatic.EXPIRED:
                return SalesQoutationVersionStatic.STATUS_DESCRIPTION.EXPIRED;
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Requests approval if the current user lacks sufficient rights and
     * validates the approving officer.
     */
    public JSONObject callApproval() {
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON = setJSON("error", "User is not an authorized approving officer.");
                return poJSON;
            }
            setApproving((String) poJSON.get("sUserIDxx"));
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    /**
     * Browse Sales Quotation Versions and open the selected one.
     */
    public JSONObject searchRecord(String value, boolean byCode)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        String lsCondition = "";
        if (psTranStat != null && !psTranStat.isEmpty()) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsCondition = " a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = " a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        initSQL();
        String lsSQL = SQL_BROWSE;
        if (!lsCondition.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Transaction No.»Quotation No.»Date»Branch",
                "sTransNox»sParentID»dTransact»sBranchNm",
                "a.sTransNox»a.sParentID»a.dTransact»IFNULL(b.sBranchNm, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = setJSON("error", "No record loaded.");
            return poJSON;
        }
    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Sales_Quotation_Version_Master Master() {
        return (Model_Sales_Quotation_Version_Master) poMaster;
    }

    @Override
    public Model_Sales_Quotation_Version_Detail Detail(int row) {
        return (Model_Sales_Quotation_Version_Detail) paDetail.get(row);
    }

    public Model_Sales_Quotation_Version_Detail getDetail() {
        return (Model_Sales_Quotation_Version_Detail) poDetail;
    }

    @Override
    public int getDetailCount() {
        if (paDetail == null) {
            paDetail = new ArrayList<>();
        }
        return paDetail.size();
    }


    public JSONObject AddDetail() throws CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getDetailCount() > 0) {
            String lsStock = Detail(getDetailCount() - 1).getStockId();
            if (lsStock == null || lsStock.isEmpty()) {
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }

        return addDetail();
    }

    public void resetMaster() {
        poMaster = new SalesModels(poGRider).SalesQuotationVersionMaster();
    }

    /**
     * Reload detail for adding or deleting rows: removes empty rows and makes
     * sure there is one blank row at the end.
     */
    public void ReloadDetail() throws CloneNotSupportedException, SQLException {
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            String lsStock = Detail(lnCtr).getStockId();
            if (lsStock == null || "".equals(lsStock)) {
                Detail().remove(lnCtr);
            }
            lnCtr--;
        }

        if ((getDetailCount() - 1) >= 0) {
            Model_Sales_Quotation_Version_Detail loLast = Detail(getDetailCount() - 1);
            if (loLast.getStockId() != null && !"".equals(loLast.getStockId())
                    && loLast.getQuantity() != null && loLast.getQuantity() > 0) {
                AddDetail();
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
    }
    /** VAT rate used to split a VAT-inclusive total into VAT sales and VAT amount. */
    private static final double VAT_RATE = 0.12;

    private double nz(Number fnValue) {
        return fnValue == null ? 0.00 : fnValue.doubleValue();
    }

    private double round2(double fnValue) {
        return Math.round(fnValue * 100.0) / 100.0;
    }

    /** ASSUMPTION: adjust to your VAT type codes; until then every version is treated as VAT-able. */
    private boolean isVatable() {
        return true;
    }

    public void computeMasterTotals() {
        double lnTotal = 0.00;
        double lnDiscount = 0.00;
        double lnAddDiscount = 0.00;
        double lnFreight = 0.00;

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Model_Sales_Quotation_Version_Detail loRow = Detail(lnCtr);

            double lnQty     = nz(loRow.getQuantity());
            double lnPrice   = nz(loRow.getUnitPrice());
            double lnDiscAmt = lnPrice * nz(loRow.getDiscount()) / 100.0;   // discount is a percentage
            double lnAddDisc = nz(loRow.getAdditionalDiscount());
            double lnFrght   = nz(loRow.getFreight());
            double lnReg     = nz(loRow.getRegistrationAmount());
            double lnIns     = nz(loRow.getInsuranceAmount());

            lnDiscount    += lnDiscAmt * lnQty;
            lnAddDiscount += lnAddDisc;
            lnFreight     += lnFrght * lnQty;
            lnTotal       += (lnPrice - lnDiscAmt - lnAddDisc + lnFrght + lnReg + lnIns) * lnQty;
        }

        double lnVatSales = 0.00, lnVatAmount = 0.00, lnNonVatSales = 0.00;
        if (isVatable()) {
            lnVatSales  = lnTotal / (1.0 + VAT_RATE);
            lnVatAmount = lnTotal - lnVatSales;
        } else {
            lnNonVatSales = lnTotal;
        }

        Master().setTransactionTotal(round2(lnTotal));
        Master().setDiscountAmount(round2(lnDiscount));
        Master().setAdditionalDiscount(round2(lnAddDiscount));
        Master().setFreight(round2(lnFreight));
        Master().setVatSales(round2(lnVatSales));
        Master().setVatAmount(round2(lnVatAmount));
        Master().setNonVatSales(round2(lnNonVatSales));
    }

    @Override
    protected JSONObject willSave()
            throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();


        Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());
        // Transaction.saveTransaction() only sets pdModified for new records; without this an update writes a null dModified

        if (Master().getValidThruDate() != null
                && Master().getValidThruDate().before(poGRider.getServerDate())) {
            poJSON.put("result", "error");
            poJSON.put("message",  "This version expired on "
                    + SQLUtil.dateFormat(Master().getValidThruDate(), SQLUtil.FORMAT_SHORT_DATE)
                    + ". Create a new version to continue.");
            return poJSON;

        }

        if (Master().getExpectedDate() != null
                && Master().getExpectedDate().before(poGRider.getServerDate())) {
            poJSON.put("result", "error");
            poJSON.put("message",  "The expected date ("
                    + SQLUtil.dateFormat(Master().getExpectedDate(), SQLUtil.FORMAT_SHORT_DATE)
                    + ") has already passed. Update the expected date to continue.");
            return poJSON;
        }
        // drop rows without an item
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            Object lsStock = item.getValue("sStockIDx");
            if (lsStock == null || "".equals(lsStock)) {
                detail.remove();
            }
        }

        if (getDetailCount() <= 0) {
            return setJSON("error", "No transaction detail to be saved.");
        }

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Integer lnQty = Detail(lnCtr).getQuantity();

            if (lnQty == null || lnQty <= 0) {
                return setJSON("error", "Invalid quantity at row " + (lnCtr + 1) + ".");
            }
            if (Detail(lnCtr).getPromoCode() != null && !Detail(lnCtr).getPromoCode().isEmpty()) {
                String lsExpiry = SearchMCItemPromoExpirey(Detail(lnCtr).Inventory().getModelId(), Detail(lnCtr).getPromoCode());
                if (lsExpiry == null || lsExpiry.isEmpty()) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Invalid promo code at row " + (lnCtr + 1) + ".");
                    return poJSON;
                }
                if (SQLUtil.toDate(lsExpiry, SQLUtil.FORMAT_SHORT_DATE).before(poGRider.getServerDate())) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Promo code at row " + (lnCtr + 1) + " has already expired.");
                    return poJSON;
                }
            }
            // key the detail to this version's master
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }
        Master().setEntryNo(getDetailCount());
        computeMasterTotals();

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /* Put saving business rules here */
        return isEntryOkay(SalesQoutationVersionStatic.OPEN);
    }

    @Override
    public void saveComplete() {
        System.out.println("Record saved successfully.");
    }

    @Override
    public JSONObject initFields() {
        poJSON = new JSONObject();
        Master().setTransactionStatus(SalesQoutationVersionStatic.OPEN);
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        poJSON = new JSONObject();

        if (Master().getTransactionNo() == null || "".equals(Master().getTransactionNo())) {
            return setJSON("error", "Transaction No. must not be empty.");
        }
        if (Master().getParentId() == null || "".equals(Master().getParentId())) {
            return setJSON("error", "Parent quotation must not be empty.");
        }

//        Sales_Qoutation_Validator_MC loValidator = new Sales_Qoutation_Validator_MC();
//        loValidator.setApplicationDriver(poGRider);
//        loValidator.setTransactionStatus(status);
//        loValidator.setMaster(Master());
//        loValidator.setDetail(new ArrayList<Object>(paDetail));
//        return loValidator.validate();
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + "  a.sTransNox, "
                + "  a.sParentID, "
                + "  a.dTransact, "
                + "  a.sBranchCd, "
                + "  a.cTranStat, "
                + "  b.sBranchNm "
                + " FROM Sales_Quotation_Version_Master a "
                + " LEFT JOIN Branch b ON b.sBranchCd = a.sBranchCd ";
    }

    /**
     * Displays the status history of the current Sales Quotation Version.
     */
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception {
        CachedRowSet crs = getStatusHistory();

        crs.beforeFirst();
        while (crs.next()) {
            String lsStat = crs.getString("cTranStat");
            if (lsStat == null || lsStat.isEmpty()) {
                crs.updateString("cTranStat", "-");
            } else {
                String lsCode = lsStat;
                if (lsStat.length() == 1 && Character.isLetter(lsStat.charAt(0))) {
                    // letter-coded status (A = 1, B = 2 ...) used by some legacy rows
                    lsCode = String.valueOf((int) lsStat.charAt(0) - 64);
                }
                crs.updateString("cTranStat", getStatus(lsCode));
            }
            crs.updateRow();
        }

        JSONObject loJSON = getEntryBy();
        String entryBy = "";
        String entryDate = "";

        if (isJSONSuccess(loJSON)) {
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }

        showStatusHistoryUI("Sales Quotation Version", (String) poMaster.getValue("sTransNox"), entryBy, entryDate, crs);
    }

    /**
     * Retrieves the user and timestamp of who created the current transaction.
     */
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM " + Master().getTable() + " a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Master().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()));
        lsSQL = lsSQL + " ORDER BY b.dModified DESC ";
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if (loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))) {
                        if (loRS.getString("sModified").length() > 10) {
                            lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified")));
                        } else {
                            lsEntry = getSysUser(loRS.getString("sModified"));
                        }
                        LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                        lsEntryDate = dModified.format(formatter);
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON = setJSON("error", e.getMessage());
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }

    /**
     * Name of the user who confirmed the loaded version, for the printed
     * "Approved By" line. Returns "" when it cannot be found.
     *
     * ASSUMPTION: confirm runs beginTrans("UPDATE STATUS", "Confirm", ...) (see
     * ConfirmTransaction), so the audit log row is found by event name
     * "UPDATE STATUS" and remarks "Confirm". If your audit log stores the
     * status change differently, adjust the two conditions below.
     */
    public String getConfirmedBy() throws SQLException, GuanzonException {
        String lsConfirmed = "";
        String lsSQL = " SELECT b.sModified "
                + " FROM " + Master().getTable() + " a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme = 'UPDATE STATUS' AND b.sRemarksx = 'Confirm' ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()));
        lsSQL = lsSQL + " ORDER BY b.dModified DESC LIMIT 1 ";
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (loRS.next()) {
                String lsUser = loRS.getString("sModified");
                if (lsUser != null && !lsUser.isEmpty()) {
                    lsConfirmed = getSysUser(lsUser.length() > 10 ? poGRider.Decrypt(lsUser) : lsUser);
                }
            }
        } finally {
            MiscUtil.close(loRS);
        }
        return lsConfirmed == null ? "" : lsConfirmed;
    }

    /**
     * Retrieves the company name of a system user based on user ID.
     */
    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL = " SELECT b.sCompnyNm from xxxSysUser a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId));
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    lsEntry = loRS.getString("sCompnyNm");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON = setJSON("error", e.getMessage());
        }
        return lsEntry;
    }

    /**
     * Confirms the loaded version. When the version runs under a parent
     * ({@link #setWithParent(boolean)}), the parent owns the database
     * transaction, so none is started or committed here.
     *
     * @param remarks confirmation remarks
     */
    public JSONObject ConfirmTransaction(String remarks)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        String lsStatus = SalesQoutationVersionStatic.CONFIRMED;

        if (getEditMode() != EditMode.READY) {
            return setJSON("error", "No version was loaded.");
        }
        if (lsStatus.equals(Master().getTransactionStatus())) {
            return setJSON("error", "Version was already confirmed.");
        }
        if (!SalesQoutationVersionStatic.OPEN.equals(Master().getTransactionStatus())) {
            return setJSON("error", "Only an open version can be confirmed.");
        }
        if (poGRider.getServerDate().equals(Master().getValidThruDate())) {
            return setJSON("error", "Version was already expired.");
        }

//        poJSON = isEntryOkay(lsStatus);
//        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        MatrixAuthChecker check = null;

        if (!pbWthParent) {
            //validator
            poJSON = isEntryOkay(lsStatus);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            Sales_Qoutation_Validator_MC loValidator = new Sales_Qoutation_Validator_MC();
            loValidator.setApplicationDriver(poGRider);
            loValidator.setTransactionStatus(lsStatus);
            loValidator.setMaster(Master());
            loValidator.setDetail(new ArrayList<Object>(paDetail));

            poJSON = loValidator.validate();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            //get the matrix return from isEntryOkey
            JSONArray loMatrix = (JSONArray) poJSON.get("matrix");

            if (loMatrix != null && !loMatrix.isEmpty()) {
                poJSON = processMatrixApproval(loMatrix, lsStatus, remarks);
                // "error" -> stop, "matrix" -> approval still pending (status already logged)
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            } else {
                // no matrix request (e.g. no discount): normal approval, then write the status
                poJSON = seekApproval();
                if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                    return poJSON;
                }

                boolean lbOwnTrans = poGRider.getGConnection().getConnection().getAutoCommit();
                if (lbOwnTrans) {
                    poGRider.beginTrans("UPDATE STATUS", "Confirm", SOURCE_CODE, Master().getTransactionNo());
                }
                poJSON = applyStatus(lsStatus, remarks);
                if (lbOwnTrans) {
                    if ("success".equals((String) poJSON.get("result"))) {
                        poGRider.commitTrans();
                    } else {
                        poGRider.rollbackTrans();
                    }
                }
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }
        poJSON.put("result", "success");
        poJSON.put("message", "Version confirmed successfully.");

        return poJSON;
    }

    /**
     * Runs the matrix approval for the request created by the validator.
     *
     * One transaction wraps the whole approval. MatrixAuthChecker.authTrans() opens a
     * transaction on a success path and never commits it, so any later beginTrans()
     * throws "Guanzon Object Execution Sequence Error". Because the connection is
     * already in a transaction here, the checker methods see auto-commit = false and
     * do not begin/commit by themselves.
     *
     * @return "success" - every required authorizer approved, status written
     *         "matrix"  - approval is still pending, status was logged
     *         "error"   - stop
     */
    private JSONObject processMatrixApproval(JSONArray loMatrix, String lsStatus, String remarks)
            throws SQLException, GuanzonException, ParseException, CloneNotSupportedException {

        // If the caller already owns a transaction, it commits/rolls back; otherwise we do.
        boolean lbOwnTrans = poGRider.getGConnection().getConnection().getAutoCommit();
        if (lbOwnTrans) {
            poGRider.beginTrans("UPDATE STATUS", "Confirm", SOURCE_CODE, Master().getTransactionNo());
        }

        try {
            JSONObject loResult = runMatrixApproval(loMatrix, lsStatus, remarks);

            if (lbOwnTrans) {
                if ("error".equals((String) loResult.get("result"))) {
                    poGRider.rollbackTrans();
                } else {
                    poGRider.commitTrans();   // "success" or "matrix"
                }
            }
            return loResult;
        } catch (Exception ex) {
            if (lbOwnTrans) {
                poGRider.rollbackTrans();
            }
            throw ex;   // precise rethrow: only the declared exceptions can reach here
        }
    }

    /** Approval logic only - no begin/commit/rollback in here. */
    private JSONObject runMatrixApproval(JSONArray loMatrix, String lsStatus, String remarks)
            throws SQLException, GuanzonException, ParseException, CloneNotSupportedException {

        MatrixAuthChecker check = new MatrixAuthChecker(poGRider, SOURCE_CODE, Master().getTransactionNo());

        JSONObject loResult = check.loadAuth();
        if (!"success".equals((String) loResult.get("result"))) {
            return loResult;
        }

        // everybody already approved -> write the confirmed status
        if (check.isAuthOkay()) {
            return applyStatus(lsStatus, remarks);
        }

        // approval by the current user / a supervising officer
        if (!check.isAllowSys()) {
            String lsAuthType = (String) ((JSONObject) loMatrix.get(0)).get("sAuthType");

            // 1st: is the logged-in user one of the authorizers?
            loResult = check.authTrans(lsAuthType, poGRider.getUserID());

            if (!"success".equalsIgnoreCase((String) loResult.get("result"))) {
                // 2nd: ask for an approving officer
                JSONObject loApprover = ShowDialogFX.getUserApproval(poGRider);
                if ("error".equals((String) loApprover.get("result"))) {
                    return loApprover;
                }

                // authorize with the APPROVING OFFICER, not the logged-in user
                String lsApproverID = loApprover.get("sUserIDxx").toString();
                loResult = check.authTrans(lsAuthType, lsApproverID);

                if (!"success".equalsIgnoreCase((String) loResult.get("result"))) {
                    return loResult;
                }
            }
        }

        // re-evaluate after the approval above -> fully approved, write the confirmed status
        if (check.isAuthOkay()) {
            return applyStatus(lsStatus, remarks);
        }

        // still waiting on other authorizers: log the status and report "matrix"
        loResult = applyStatus(lsStatus, remarks);
        if (!"success".equals((String) loResult.get("result"))) {
            return loResult;
        }

        loResult.put("result", "matrix");
        return loResult;
    }

    /** Writes the status change. Never begins/commits - the caller owns the transaction. */
    private JSONObject applyStatus(String lsStatus, String remarks)
            throws SQLException, GuanzonException, ParseException, CloneNotSupportedException {
        JSONObject loResult = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"),
                remarks, lsStatus, false, true);
        if (!"success".equals((String) loResult.get("result"))) {
            return loResult;
        }
        return setJSON("success", "");
    }
    /**
     * Seek Approval method
     *
     * @return JSON
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject seekApproval()
            throws SQLException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        //Moved only the script for seeking of approval - Arsiela 10-15-2025 - 14:11:01

        //load authorization manager that evaluates current users authority for this process
        ActionAuthManager loAuth = new ActionAuthManager(poGRider, "cas-purchasing");
        poJSON = loAuth.isAuthorized();

        //check if currenty user is authorized
        System.out.println(poGRider.getUserID());
        if (!((String) poJSON.get("result")).equalsIgnoreCase("true")) {
            //show process needs authorization
            ShowMessageFX.Warning((String) poJSON.get("warning"), "Authorization Required", null);
            //get authorization from authoried personnel
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            //check if approving officer is authorized
            String lsUserIDxx = poJSON.get("sUserIDxx").toString();
            int lnUserLevl = Integer.parseInt(poJSON.get("nUserLevl").toString());
            poJSON = loAuth.isAuthorized(lsUserIDxx, lnUserLevl);

            //if approving is not authorized then do not continue process
            if (!((String) poJSON.get("result")).equalsIgnoreCase("true")) {
                ShowMessageFX.Warning((String) poJSON.get("warning"), "Authorization Required", null);
                poJSON.put("result", "error");
                poJSON.put("message", "User is not an authorized approving officer..");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    public JSONObject LostTransaction(String remarks)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        String lsStatus = SalesQoutationVersionStatic.REJECTED;

        if (getEditMode() != EditMode.READY) {
            return setJSON("error", "No version was loaded.");
        }
        if (lsStatus.equals(Master().getTransactionStatus())) {
            return setJSON("error", "Version was already rejected/lost.");
        }

        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;


            poGRider.beginTrans("UPDATE STATUS", "Lost", SOURCE_CODE, Master().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"),
                remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) poGRider.rollbackTrans();
            return poJSON;
        }

        if (!pbWthParent) poGRider.commitTrans();

        poJSON = setJSON("success", "Version marked as lost successfully.");
        return poJSON;
    }
    public JSONObject VoidTransaction(String remarks)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        String lsStatus = SalesQoutationVersionStatic.VOID;

        if (getEditMode() != EditMode.READY) {
            return setJSON("error", "No version was loaded.");
        }
        if (lsStatus.equals(Master().getTransactionStatus())) {
            return setJSON("error", "Version was already voided.");
        }

        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;


        poGRider.beginTrans("UPDATE STATUS", "Void", SOURCE_CODE, Master().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"),
                remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            if (!pbWthParent) poGRider.rollbackTrans();
            return poJSON;
        }

        if (!pbWthParent) poGRider.commitTrans();

        poJSON = setJSON("success", "Version marked as void successfully.");
        return poJSON;
    }

    /**
     * Marks the loaded version as SUPERCEDED. Used when a new version replaces it.
     * Runs inside the parent's transaction, so none is started or committed.
     *
     * @param remarks status history remarks
     */
    public JSONObject SupersedeTransaction(String remarks)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException {
        String lsStatus = SalesQoutationVersionStatic.SUPERCEDED;

        if (getEditMode() != EditMode.READY) {
            return setJSON("error", "No version was loaded.");
        }
        if (lsStatus.equals(Master().getTransactionStatus())) {
            return setJSON("error", "Version was already superseded.");
        }

        // runs under the parent's transaction (setWithParent(true)), so none is started or committed here
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"),
                remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        return setJSON("success", "Version superseded successfully.");
    }


    private JSONObject setJSON(String fsResult, String fsMessage) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", fsResult);
        loJSON.put("message", fsMessage);
        return loJSON;
    }

    public boolean isJSONSuccess(JSONObject foJSON) {
        return ("success".equals((String) foJSON.get("result")) || !"error".equals((String) foJSON.get("result")));
    }


    public String SearchMCItemPromoExpirey(String fsModel,String fsPromoCode) throws SQLException, GuanzonException {
        String lsExpiry = "";
        String lsSQL = SalesQoutationsMasterQueries.SQL_MCItemPromo();
        lsSQL = MiscUtil.addCondition(lsSQL, " b.sModelIDx =  " + SQLUtil.toSQL(fsModel)+
                " AND a.sPromIDxx = " + SQLUtil.toSQL(fsPromoCode));
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    lsExpiry = loRS.getString("dThruDate");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON = setJSON("error", e.getMessage());
        }
        return lsExpiry;
    }

    public  JSONObject VersionStatusChange(String tableName,
                                         String sourceNo,
                                         String remarks,
                                         String statusRequest,
                                         boolean needConfirmation,
                                         boolean withParent)
            throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();


        poJSON = statusChange(tableName, sourceNo, remarks, statusRequest, needConfirmation, withParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }
}