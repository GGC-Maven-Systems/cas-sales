/*
 * Copyright (c) 2026 Guanzon Group of Companies.
 * All rights reserved.
 */
package ph.com.guanzongroup.cas.sales;

import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Master;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationVersionStatic;

import javax.sql.rowset.CachedRowSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;

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

    @Override
    protected JSONObject willSave()
            throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());
        // Transaction.saveTransaction() only sets pdModified for new records; without this an update writes a null dModified
        pdModified = poGRider.getServerDate();

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
            // key the detail to this version's master
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
        }

        Master().setEntryNo(getDetailCount());

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
        System.out.println("getParentId. " + Master().getParentId());
        if (Master().getParentId() == null || "".equals(Master().getParentId())) {
            return setJSON("error", "Parent quotation must not be empty.");
        }
        // TODO: add more validations (branch, dates, payment form, totals) when the UI needs them

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

    private JSONObject setJSON(String fsResult, String fsMessage) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", fsResult);
        loJSON.put("message", fsMessage);
        return loJSON;
    }

    public boolean isJSONSuccess(JSONObject foJSON) {
        return ("success".equals((String) foJSON.get("result")) || !"error".equals((String) foJSON.get("result")));
    }
}