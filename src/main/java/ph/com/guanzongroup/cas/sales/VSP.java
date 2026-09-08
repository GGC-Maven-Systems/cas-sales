package ph.com.guanzongroup.cas.sales;

import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.Model_Vsp_Master;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.VSPStatic;
import ph.com.guanzongroup.cas.sales.validator.VSPValidator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VSP extends Transaction {

    List<Model_Vsp_Master> poVspMaster;

    List<Model> paDetailRemoved;
    SalesInquiry salesInquiry;

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "srsv";

        poMaster = new SalesModels(poGRider).VspMaster();
//        poDetail = new SalesModels(poGRider).();
        salesInquiry = new SalesControllers(poGRider, logwrapr).SalesInquiry();
        paDetail = new ArrayList<>();
        return initialize();
    }
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryCd = "";

    @Override
    public JSONObject initFields() {
        //Put initial model values here/
        poJSON = new JSONObject();
        try {
            poJSON = new JSONObject();
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryCode(psIndustryId);
            Master().setCompanyId(psCompanyId);
            Master().setTransactionDate(poGRider.getServerDate());

        } catch (SQLException ex) {
            Logger.getLogger(VSP.class
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public void setIndustryID(String industryID) {
        psIndustryId = industryID;
    }

    public void setCompanyID(String companyID) {
        psCompanyId = companyID;
    }

    public void setCategoryCd(String categoryCD) {
        psCategoryCd = categoryCD;
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, CloneNotSupportedException, GuanzonException {
        poJSON = new JSONObject();

        String lsStatus = VSPStatic.CANCELLED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(VSPStatic.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            } else {
                if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "User is not an authorized approving officer..");
                    return poJSON;
                }
                setApproving((String) poJSON.get("sUserIDxx"));
            }
        }
//        poJSON = setValueToOthers(lsStatus);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
        //check  the user level again then if he/she allow to approve
        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, Master().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
//        poJSON = saveUpdates(PurchaseOrderStatus.CONFIRMED);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancelled request submitted successfully.");
        }

        return poJSON;
    }






    /*Search Master References*/
    public JSONObject SearchBranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setBranchCode(object.getModel().getBranchCode());
        }

        return poJSON;
    }


    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox, "
                + " a.dTransact, "
                + " c.sBranchNm, "
                + " a.sClientID, "
                + " d.sCompnyNm "
                + " FROM Sales_Reservation_Master a "
                + " LEFT JOIN Branch c ON LEFT(a.sTransNox, 4) = c.sBranchCd "
                + " LEFT JOIN Client_Master d ON a.sClientID = d.sClientID "
                + " LEFT JOIN Client_Address e ON d.sClientID = e.sClientID "
                + " LEFT JOIN Client_Mobile f ON d.sClientID = f.sClientID "
                + ", Sales_Reservation_Detail b ";
    }

    public JSONObject SearchTransaction(String fsValue) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        String lsBranch = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryCode()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyId()));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        if (!fsValue.isEmpty()) {
            if (Master().getClientId() == null) {
                lsSQL = lsSQL + " AND d.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%");
            } else {
                lsSQL = lsSQL + " AND a.sClientID = " + SQLUtil.toSQL(Master().getClientId());
            }
        } else {
            lsSQL = lsSQL + " AND d.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%");
        }

        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        if (!poGRider.isMainOffice() || !poGRider.isWarehouse()) {
            lsSQL = lsSQL + " AND a.sBranchCd LIKE " + SQLUtil.toSQL(poGRider.getBranchCode());
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction Date»Transaction No»Customer Name»Branch",
                "a.dTransact»a.sTransNox»d.sCompnyNm»c.sBranchNm",
                "a.dTransact»a.sTransNox»d.sCompnyNm»ecsBranchNm",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject SearchTransactionbyFilter(String fsValue, boolean fsByCode) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        String lsBranch = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryCode()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyId()));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        if (fsByCode) {
            if (!fsValue.isEmpty()) {
                lsSQL = lsSQL + " AND a.sTransNox = " + SQLUtil.toSQL(fsValue);
            } else {
                lsSQL = lsSQL + "  AND a.sTransNox LIKE '%' ";
            }
        } else {
            if (!fsValue.isEmpty()) {
                lsSQL = lsSQL + " AND d.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue);
            } else {
                lsSQL = lsSQL + " AND d.sCompnyNm LIKE '%'";
            }
        }

        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        if (!poGRider.isMainOffice() || !poGRider.isWarehouse()) {
            lsSQL = lsSQL + " AND a.sBranchCd LIKE " + SQLUtil.toSQL(poGRider.getBranchCode());
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction Date»Transaction No»Customer Name»Branch",
                "a.dTransact»a.sTransNox»d.sCompnyNm»c.sBranchNm",
                "a.dTransact»a.sTransNox»d.sCompnyNm»ecsBranchNm",
                fsByCode ? 1 : 2);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    /*End - Search Master References*/
    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Vsp_Master Master() {
        return (Model_Vsp_Master) poMaster;
    }

//    @Override
//    public Model_Sales_Reservation_Detail Detail(int row) {
//        return (Model_Sales_Reservation_Detail) paDetail.get(row);
//    }

    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {

//        if (paDetailRemoved == null) {
//            paDetailRemoved = new ArrayList<>();
//        }
//
//        Iterator<Model> detail = Detail().iterator();
//        while (detail.hasNext()) {
//            Model item = detail.next();
//            Object quantityObj = item.getValue("nQuantity");
//
//            if (quantityObj != null) {
//                double quantity = ((Number) quantityObj).doubleValue();
//                if (quantity <= 0.00) {
//                    switch (getEditMode()) {
//                        case EditMode.ADDNEW:
//                            detail.remove();
//                            break;
//                        case EditMode.UPDATE:
//                            paDetailRemoved.add(item);
//                            item.setValue("cReversex", "0");
//                            break;
//                        default:
//                            throw new AssertionError();
//                    }
//                }
//            } else {
//                paDetailRemoved.add(item); // track removed
//                detail.remove();
//            }
//        }
//
//        // Re-number remaining details
//        for (int lnCtr = 0; lnCtr < getDetailCount(); lnCtr++) {
//            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
//            Detail(lnCtr).setEntryNo(lnCtr + 1);
//            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
//        }

        Master().setModifiedDate(poGRider.getServerDate());
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(RecordStatus.INACTIVE);
    }

    @Override
    public JSONObject saveOthers() {
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }



    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = (GValidator) new VSPValidator();

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());

        poJSON = loValidator.validate();
        return poJSON;
    }

    public JSONObject callapproval() {
        JSONObject loJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            loJSON = ShowDialogFX.getUserApproval(poGRider);

            if (!"success".equalsIgnoreCase((String) loJSON.get("result"))) {
                return loJSON; // Already contains result/message
            }

            int approvingUserLevel = Integer.parseInt(loJSON.get("nUserLevl").toString());
            if (approvingUserLevel <= UserRight.ENCODER) {
                loJSON.put("result", "error");
                loJSON.put("message", "User is not an authorized approving officer.");
                return loJSON;
            }
            setApproving((String) poJSON.get("sUserIDxx"));
        }
        loJSON.put("result", "success");
        return loJSON;
    }
}
