/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebFile;
import org.guanzon.appdriver.constant.ClientType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.ClientGUI;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.CategoryLevel2;
import org.guanzon.cas.parameter.Color;
import org.guanzon.cas.parameter.ModelVariant;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.Model_Bank_Application;
import ph.com.guanzongroup.cas.sales.model.Model_Customer_Inquiry_FollowUp;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Commitment_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Inquiry_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Inquiry_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Inquiry_Requirements;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.BankApplicationStatus;
import ph.com.guanzongroup.cas.sales.status.SalesInquiryStatic;
import ph.com.guanzongroup.cas.sales.validator.BankApplication;
import ph.com.guanzongroup.cas.sales.validator.SalesInquiryRequirements;
import ph.com.guanzongroup.cas.sales.validator.SalesInquiryValidatorFactory;

/**
 *
 * @author Arsiela
 */
public class SalesInquiry extends Transaction {

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";
    public String sourceCode = "";
    List<Model_Sales_Inquiry_Master> paMasterList;
    List<Model> paDetailRemoved;
    List<Model_Sales_Inquiry_Requirements> paRequirements;
    List<Model_Sales_Inquiry_Requirements> paRequirementsRemoved;
    List<Model_Sales_Commitment_Master> paBankApplications;
    List<Model_Customer_Inquiry_FollowUp> paFollowUpHistory;
    public List<TransactionAttachment> paAttachments;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "SInq";

        sourceCode = SOURCE_CODE;
        poMaster = new SalesModels(poGRider).SalesInquiryMaster();
        poDetail = new SalesModels(poGRider).SalesInquiryDetails();

        paMasterList = new ArrayList<>();
        paDetail = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        paRequirements = new ArrayList<>();
        paRequirementsRemoved = new ArrayList<>();
        paBankApplications = new ArrayList<>();
        paFollowUpHistory = new ArrayList<>();
        paAttachments = new ArrayList<>();

        return initialize();
    }

    public JSONObject NewTransaction()
            throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        //Clear data
        resetMaster();
        resetOthers();
        Detail().clear();
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

    public String getInquiryStatus(String lsStatus) {
        switch (lsStatus) {
            case SalesInquiryStatic.LOST:
                return "Lost Sale";
            case SalesInquiryStatic.CONFIRMED:
                return "Confirmed";
            case SalesInquiryStatic.QUOTED:
                return "Quoted";
            case SalesInquiryStatic.SALE:
                return "Sale";
            case SalesInquiryStatic.OPEN:
                return "Open";
            case SalesInquiryStatic.VOID:
                return "Void";
            case SalesInquiryStatic.CANCELLED:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    /**
     * Call Approval for encoder
     *
     * @return
     */
    public JSONObject callApproval() {
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON.put("result", "error");
                poJSON.put("message", "User is not an authorized approving officer.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    /**
     * Confirm the transaction
     *
     * @param remarks
     * @return JSONObject success or error
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     */
    public JSONObject ConfirmTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SalesInquiryStatic.CONFIRMED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.CAR) //            || Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.MOTORCYCLE)
                ) {

            poJSON = checkRequirements();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poJSON = checkBankApplication();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        //Require approval when user is not equal to sales man and user is not supervisor
        if (!Master().getSalesMan().equals(getSysUser(poGRider.getUserID(), true))) {
            poJSON = callApproval();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction confirmed successfully.");
        return poJSON;
    }

    /**
     * Quote the transaction
     *
     * @param remarks
     * @return JSONObject success or error
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     */
    public JSONObject QuoteTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SalesInquiryStatic.QUOTED;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }
//Sales Inquiry can have multiple quotation
//        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already quoted.");
//            return poJSON;
//        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction quoted successfully.");
        return poJSON;
    }

    /**
     * Sale the transaction
     *
     * @param remarks
     * @return JSONObject success or error
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     */
    public JSONObject SaleTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SalesInquiryStatic.SALE;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already converted to sale.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction converted to sale successfully.");
        return poJSON;
    }

    /**
     * Lost the transaction
     *
     * @param remarks
     * @return JSONObject success or error
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     */
    public JSONObject LostTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SalesInquiryStatic.LOST;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already lost/declined.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

//        if (SalesInquiryStatic.CONFIRMED.equals(Master().getTransactionStatus())) {
//            poJSON = callApproval();
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//        }
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction tagged as lost/declined successfully.");
        return poJSON;
    }

    /**
     * Cancel the transaction
     *
     * @param remarks
     * @return JSONObject success or error
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     */
    public JSONObject CancelTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SalesInquiryStatic.CANCELLED;

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
        poJSON = isEntryOkay(SalesInquiryStatic.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (SalesInquiryStatic.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (!Master().getSalesMan().equals(poGRider.getUserID())) {
                poJSON = callApproval();
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction cancelled successfully.");
        return poJSON;
    }

    /**
     * Void the transaction
     *
     * @param remarks
     * @return JSONObject success or error
     * @throws ParseException
     * @throws SQLException
     * @throws GuanzonException
     * @throws CloneNotSupportedException
     */
    public JSONObject VoidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = SalesInquiryStatic.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesInquiryStatic.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (SalesInquiryStatic.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (!Master().getSalesMan().equals(poGRider.getUserID())) {
                poJSON = callApproval();
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, false);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction voided successfully.");
        return poJSON;
    }

    /**
     * Check for inquiry requirements
     *
     * @return
     */
    private JSONObject checkRequirements() {
        poJSON = new JSONObject();
        boolean lbIsWithRequirements = false;
        for (int lnCtr = 0; lnCtr <= getSalesInquiryRequirementsCount() - 1; lnCtr++) {
            if (SalesInquiryRequimentsList(lnCtr).isSubmitted()) {
                lbIsWithRequirements = true;
                break;
            }
        }

        if (!lbIsWithRequirements) {
            poJSON.put("result", "error");
            poJSON.put("message", "Client must submit requirement.");
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Check Bank Application for PO or Financing Inquiry
     *
     * @return
     */
    private JSONObject checkBankApplication() {
        poJSON = new JSONObject();
        boolean lbIsWithBankApp = false;

        if (Master().getPurchaseType().equals(SalesInquiryStatic.PurchaseType.PO)
                || Master().getPurchaseType().equals(SalesInquiryStatic.PurchaseType.FINANCING)) {
            for (int lnCtr = 0; lnCtr <= getBankApplicationsCount() - 1; lnCtr++) {
                if (BankApplicationsList(lnCtr).getTransactionStatus().equals(BankApplicationStatus.APPROVED)) {
                    lbIsWithBankApp = true;
                    break;
                }
            }

            if (!lbIsWithBankApp) {
                poJSON.put("result", "error");
                poJSON.put("message", "Client must have approved bank application.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Search Transaction
     *
     * @return
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        //If current user is an ordinary user load only its inquiries
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            lsSQL = MiscUtil.addCondition(lsSQL,
                    " a.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
        }

        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Client»Sales Person",
                "dTransact»sTransNox»sClientNm»sSalePrsn",
                "a.dTransact»a.sTransNox»b.sCompnyNm»concat(c.sLastName,', ',c.sFrstName, ' ',c.sMiddName)",
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

    /**
     * Search transaction
     *
     * @param fsClient via Client Name
     * @param fsTransNo via transaction no
     * @return JSONObject success or error
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject searchTransaction(String fsClient, String fsTransNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsClient)
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransNo));

        //If current user is an ordinary user load only its inquiries
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            lsSQL = MiscUtil.addCondition(lsSQL,
                    " a.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
        }

        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Client»Sales Person",
                "dTransact»sTransNox»sClientNm»sSalePrsn",
                "a.dTransact»a.sTransNox»b.sCompnyNm»concat(c.sLastName,', ',c.sFrstName, ' ',c.sMiddName)",
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

    /**
     * Search Transaction
     *
     * @param fsIndustry filter by Industry
     * @param fsCompany filter by Company
     * @param fsCategory filter by Category
     * @param fsClient filter by Client
     * @param fsTransNo filter by Transaction NO
     * @return JSONObject success or error
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject searchTransaction(String fsIndustry, String fsCompany, String fsCategory, String fsClient, String fsTransNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sIndstCdx = " + SQLUtil.toSQL(fsIndustry)
                + " AND a.sCompnyID = " + SQLUtil.toSQL(fsCompany)
                + " AND a.sCategrCd = " + SQLUtil.toSQL(fsCategory)
                + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsClient)
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransNo));

        //If current user is an ordinary user load only its inquiries
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            lsSQL = MiscUtil.addCondition(lsSQL,
                    " a.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
        }

        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Client»Sales Person",
                "dTransact»sTransNox»sClientNm»sSalePrsn",
                "a.dTransact»a.sTransNox»b.sCompnyNm»concat(c.sLastName,', ',c.sFrstName, ' ',c.sMiddName)",
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

    /*Search Master References*/
    /**
     * Search for Inquiring customer
     *
     * @param value
     * @param byCode
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchClient(String value, boolean byCode)
            throws SQLException,
            GuanzonException,
            Exception {
        poJSON = new JSONObject();
//
//        Client object = new ClientControllers(poGRider, logwrapr).Client();
//        object.Master().setRecordStatus(RecordStatus.ACTIVE);
//        object.Master().setClientType(Master().getClientType());
//        poJSON = object.Master().searchRecord(value, byCode);
//        if ("success".equals((String) poJSON.get("result"))) {
//            
//            poJSON = checkPendingInquiry(object.Master().getModel().getClientId());
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }

        JSONObject loResult = new JSONObject();

        String lsClientId = Master().getClientId();
        lsClientId = (lsClientId == null || lsClientId.isEmpty()) ? "" : lsClientId;

        //initialize Client GUI
        ClientGUI loClient = new ClientGUI();

        loClient.setGRider(poGRider);
        loClient.setLogWrapper(null);

        loClient.setClientType(Master().getClientType());
        //filter client type
        if (!ClientType.INDIVIDUAL.equals(Master().getClientType())) { // For institution entry
            loClient.setCategoryCode(Master().getCategoryCode());
        }

        //searchRecord(fsValue,fbByCode) will run make sure to set client and bycode
        //bycode true client id
        //bycode false company
        //set search by code
        loClient.setByCode(false);
        poJSON = loClient.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            loClient.setClientId((String) poJSON.get("clientId"));
            //Load Client of Existing 

            //load record
            CommonUtils.showModal(loClient);
        } else {
            poJSON = new JSONObject();
            //Maynard 2026-10-07
            poJSON = addClient();
            if ("success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }

        //load if button
        if (!loClient.isCancelled()) {
            Master().setClientId(loClient.getClientId());
            System.out.println("Get Address " + Master().ClientAddress().getAddressId());
            Master().setAddressId(Master().ClientAddress().getAddressId()); //TODO
            Master().setContactId(Master().ClientMobile().getMobileId()); //TODO
        }

        System.out.println("Client ID : " + Master().getClientId());
        System.out.println("Address ID : " + Master().getAddressId());
        System.out.println("Contact ID : " + Master().getContactId());

        return poJSON;
    }

    /**
     * Search Referral Agent
     *
     * @param value
     * @param byCode
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchReferralAgent(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        SalesAgent object = new SalesControllers(poGRider, logwrapr).SalesAgent();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setAgentId(object.getModel().getClientId());
        }

        return poJSON;
    }

    /**
     * Search Sales Person
     *
     * @param value
     * @param byCode
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchSalesPerson(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Salesman object = new SalesControllers(poGRider, logwrapr).Salesman();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode, Master().getBranchCode());
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setSalesMan(object.getModel().getEmployeeId());
        }

        return poJSON;
    }

    /**
     * Search Source
     *
     * @param value
     * @param byCode
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchSource(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        SalesInquirySources object = new SalesControllers(poGRider, logwrapr).SalesInquirySources();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setSourceCode(object.getModel().getSourceId());
        }

        return poJSON;
    }

    /**
     * Search Brand
     *
     * @param value
     * @param byCode
     * @param row
     * @return JSONObject success or error
     * @throws ExceptionInInitializerError
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchBrand(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (Master().getClientId() == null || "".equals(Master().getClientId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Client is not set.");
            return poJSON;
        }

        poJSON = checkMaximumInqDetail();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        Model_Brand object = new ParamModels(poGRider).Brand();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(object), "sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryId()));
        System.out.println("Execute SQL : " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "ID»Description",
                "sBrandIDx»sDescript",
                "sBrandIDx»sDescript",
                byCode ? 0 : 1);

        if (poJSON != null) {
            String lsBrandId = (String) poJSON.get("sBrandIDx");
            if (lsBrandId != null && !"".equals(lsBrandId)) {
                if (!lsBrandId.equals(Detail(row).getBrandId())) {
                    Detail(row).setModelId("");
                    Detail(row).setModelVarianId("");
                    Detail(row).setColorId("");
                    Detail(row).setStockId("");
                }

                Detail(row).setBrandId(lsBrandId);
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Search Model
     *
     * @param value
     * @param byCode
     * @param row
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchModel(String value, boolean byCode, int row)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);

        if (Detail(row).getBrandId() == null || "".equals(Detail(row).getBrandId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Brand is not set.");
            return poJSON;
        }

        ModelVariant object = new ParamControllers(poGRider, logwrapr).ModelVariant();
        object.setRecordStatus(RecordStatus.ACTIVE);
        System.out.println("Brand ID : " + Detail(row).getBrandId());
        poJSON = object.searchRecordByModel(value, byCode, Detail(row).getBrandId());
        poJSON.put("row", row);
        if ("success".equals((String) poJSON.get("result"))) {
            poJSON = checkExistingDetail(row,
                    Detail(row).getBrandId(),
                    object.getModel().getModelId(),
                    object.getModel().getVariantId(),
                    "",
                    "");
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            if (!object.getModel().getModelId().equals(Detail(row).getModelId())) {
                Detail(row).setColorId("");
                Detail(row).setStockId("");
            }

            Detail(row).setModelId(object.getModel().getModelId());
            Detail(row).setModelVarianId(object.getModel().getVariantId());
            Detail(row).setCategory("");

            System.out.println("MODEL : " + Detail(row).Model().getDescription());
            System.out.println("MODEL Variant :" + Detail(row).ModelVariant().getDescription());
        }

        return poJSON;
    }

    /**
     * Search Color
     *
     * @param value
     * @param byCode
     * @param row
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchColor(String value, boolean byCode, int row)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);

        if (Detail(row).getModelId() == null || "".equals(Detail(row).getModelId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Model is not set.");
            return poJSON;
        }

        Color object = new ParamControllers(poGRider, logwrapr).Color();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        poJSON.put("row", row);
        if ("success".equals((String) poJSON.get("result"))) {

            System.out.println("Category Type : " + Master().getCategoryType());
            System.out.println("Category Code : " + Master().getCategoryCode());
            System.out.println("Brand : " + Detail(row).getBrandId());
            System.out.println("Model : " + Detail(row).getModelId());
            System.out.println("Variant : " + Detail(row).getModelVarianId());
            System.out.println("Color : " + object.getModel().getColorId());

            //Set stock ID
            String lsStockId = "";
            String lsCategoryId = "";
            Inventory inventory = new InvControllers(poGRider, logwrapr).Inventory();
            inventory.setRecordStatus(RecordStatus.ACTIVE); //Master().getCategoryType()
            poJSON = inventory.searchRecord(Master().getCategoryCode(), Detail(row).getBrandId(), Detail(row).getModelId(), Detail(row).getModelVarianId(), object.getModel().getColorId());
            if (!"error".equals((String) poJSON.get("result"))) {
                lsStockId = inventory.getModel().getStockId();
                lsCategoryId = inventory.getModel().getCategoryFirstLevelId();

//                if(inventory.getModel().Variant().getSellingPrice() != 0.0000){ //Error TODO
//                    Detail(row).setSellPrice(inventory.getModel().Variant().getSellingPrice());
//                } else {
                if (inventory.getModel().getSellingPrice() != null) {
                    Detail(row).setSellPrice(inventory.getModel().getSellingPrice().doubleValue());
                } else {
                    Detail(row).setSellPrice(0.0000);
                }
//                }
            }
            poJSON = checkExistingDetail(row,
                    Detail(row).getBrandId(),
                    Detail(row).getModelId(),
                    Detail(row).getModelVarianId(),
                    object.getModel().getColorId(),
                    lsStockId);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            Detail(row).setColorId(object.getModel().getColorId());
            Detail(row).setStockId(lsStockId);
            Detail(row).setCategory(lsCategoryId);

        }

//        System.out.println("Barcode : " + Detail(row).Inventory().getBarCode());
//        System.out.println("Description : " + Detail(row).Inventory().getDescription());
//        System.out.println("Category : " + Detail(row).Category2().getDescription());
//        System.out.println("Brand : " + Detail(row).Brand().getDescription());
//        System.out.println("Model : " + Detail(row).Model().getDescription());
//        System.out.println("Variant : " + Detail(row).ModelVariant().getDescription());
//        System.out.println("Color : " + Detail(row).Color().getDescription());
        return poJSON;
    }

    /**
     * Search Category
     *
     * @param value
     * @param byCode
     * @param row
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchCategory(String value, boolean byCode, int row) throws SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (Master().getClientId() == null || "".equals(Master().getClientId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Client is not set.");
            return poJSON;
        }

        CategoryLevel2 object = new ParamControllers(poGRider, logwrapr).CategoryLevel2();
        String lsSQL = MiscUtil.addCondition(object.getSQ_Browse(), "cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                + " AND sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryId()));

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Category ID»Description",
                "sCategrCd»sDescript",
                "sCategrCd»sDescript",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = object.getModel().openRecord((String) poJSON.get("sCategrCd"));
            if ("success".equals((String) poJSON.get("result"))) {

                if (!Detail(row).getCategory().equals(object.getModel().getCategoryId())) {
                    Detail(row).setBrandId("");
                }

                Detail(row).setCategory(object.getModel().getCategoryId());
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }

        return poJSON;
    }

    /**
     * Search Inventory
     *
     * @param value
     * @param byCode
     * @param row
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchInventory(String value, boolean byCode, int row) throws SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (Master().getClientId() == null || "".equals(Master().getClientId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Client is not set.");
            return poJSON;
        }

        if (Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.APPLIANCES)
                || Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.MOBILEPHONE)) {
            if (Detail(row).getCategory() == null || "".equals(Detail(row).getCategory())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Category is not set.");
                return poJSON;
            }
        }
        String lsBrand = Detail(row).getBrandId() != null && !"".equals(Detail(row).getBrandId())
                ? " AND a.sBrandIDx = " + SQLUtil.toSQL(Detail(row).getBrandId())
                : "";
        String lsCategory2 = Detail(row).getCategory() != null && !"".equals(Detail(row).getCategory())
                ? " AND a.sCategCd2 = " + SQLUtil.toSQL(Detail(row).getCategory())
                : "";
        Inventory object = new InvControllers(poGRider, logwrapr).Inventory();
        String lsSQL = MiscUtil.addCondition(object.getSQ_Browse(),
                // " a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                " a.sCategCd1 = " + SQLUtil.toSQL(Master().getCategoryCode())
                + " AND a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryId())
                + lsBrand
                + lsCategory2
        );

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Barcode»Description»Brand»Model»Variant»UOM",
                "sBarCodex»sDescript»xBrandNme»xModelNme»xVrntName»xMeasurNm",
                "a.sBarCodex»a.sDescript»IFNULL(b.sDescript, '')»IFNULL(c.sDescript, '')»IFNULL(f.sDescript, '')»IFNULL(e.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = object.getModel().openRecord((String) poJSON.get("sStockIDx"));
            if ("success".equals((String) poJSON.get("result"))) {
                poJSON = checkExistingDetail(row,
                        Detail(row).getBrandId(),
                        object.getModel().getModelId(),
                        object.getModel().getVariantId(),
                        object.getModel().getColorId(),
                        object.getModel().getStockId()
                );
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }

                Detail(row).setStockId(object.getModel().getStockId());
                Detail(row).setModelId(object.getModel().getModelId());
                Detail(row).setModelVarianId(object.getModel().getVariantId());
                Detail(row).setColorId(object.getModel().getColorId());

//                if(object.getModel().Variant().getSellingPrice() != 0.0000){  //Error TODO
//                    Detail(row).setSellPrice(object.getModel().Variant().getSellingPrice());
//                } else {
                if (object.getModel().getSellingPrice() != null) {
                    Detail(row).setSellPrice(object.getModel().getSellingPrice().doubleValue());
                } else {
                    Detail(row).setSellPrice(0.0000);
                }
//                }
            }

            System.out.println("Barcode : " + Detail(row).Inventory().getBarCode());
            System.out.println("Description : " + Detail(row).Inventory().getDescription());
            System.out.println("Category : " + Detail(row).Category2().getDescription());
            System.out.println("Brand : " + Detail(row).Brand().getDescription());
            System.out.println("Model : " + Detail(row).Model().getDescription());
            System.out.println("Variant : " + Detail(row).ModelVariant().getDescription());
            System.out.println("Color : " + Detail(row).Color().getDescription());

        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }

        return poJSON;
    }

//    public JSONObject SearchCategory(String value, boolean byCode, int row)
//            throws SQLException,
//            GuanzonException {
//        poJSON = new JSONObject();
//        poJSON.put("row", row);
//        
//        if(Master().getClientId()== null || "".equals(Master().getClientId())){
//            poJSON.put("result", "error");
//            poJSON.put("message", "Client is not set.");
//            return poJSON;
//        }
//        
//        CategoryLevel2 object = new ParamControllers(poGRider, logwrapr).CategoryLevel2();
//        object.setRecordStatus(RecordStatus.ACTIVE);
//        
//        poJSON = object.searchRecord(value, byCode);
////        poJSON = object.searchRecord(value, byCode, Master().getIndustryId()); TODO
//        
//        poJSON.put("row", row);
//        System.out.println("result" + (String) poJSON.get("result"));
//        if ("success".equals((String) poJSON.get("result"))) {
//            Detail(row).setCategory(object.getModel().getCategoryId());
//            Detail(row).setStockId("");
//            Detail(row).setModelId("");
//            Detail(row).setModelVarianId("");
//            Detail(row).setColorId("");
//        }
//        
//        System.out.println("Category ID : " + Detail(row).getCategory());
//        System.out.println("Description  : " + Detail(row).Category2().getDescription());
//        return poJSON;
//    }
//    public JSONObject SearchInventory(String value, boolean byCode, int row)
//            throws SQLException,
//            GuanzonException {
//        
//        poJSON = new JSONObject();
//        poJSON.put("row", row);
//        
//        if(Master().getClientId()== null || "".equals(Master().getClientId())){
//            poJSON.put("result", "error");
//            poJSON.put("message", "Client is not set.");
//            return poJSON;
//        }
//        
//        Inventory object = new InvControllers(poGRider, logwrapr).Inventory();
//        object.setRecordStatus(RecordStatus.ACTIVE);
//        
//        if(Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.APPLIANCES)
//            || Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.MOBILEPHONE)){
//            if(Detail(row).getCategory()== null || "".equals(Detail(row).getCategory())){
//                poJSON.put("result", "error");
//                poJSON.put("message", "Category is not set.");
//                return poJSON;
//            }
//    //        poJSON = object.searchRecord(value, byCode, 
//    //                null,
//    //                "".equals(Detail(row).getBrandId()) ?  null : Detail(row).getBrandId(), 
//    //                Master().getIndustryId(),  
//    //                Master().getCategoryCode(),
//    //                        Detail(row).getCategory());
//        } else {
//        
//            poJSON = object.searchRecord(value, byCode, 
//                    null,
//                    "".equals(Detail(row).getBrandId()) ?  null : Detail(row).getBrandId(), 
//                    Master().getIndustryId(),  
//                    Master().getCategoryCode());
//        }
//        
//        poJSON.put("row", row);
//        System.out.println("result" + (String) poJSON.get("result"));
//        if ("success".equals((String) poJSON.get("result"))) {
//            poJSON = checkExistingDetail(row,
//                    Detail(row).getBrandId(),
//                    object.getModel().getModelId(),
//                    object.getModel().getVariantId(),
//                    object.getModel().getColorId(),
//                    object.getModel().getStockId() 
//                    );
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//
//            Detail(row).setStockId(object.getModel().getStockId());
//            Detail(row).setModelId(object.getModel().getModelId());
//            Detail(row).setModelVarianId(object.getModel().getVariantId());
//            Detail(row).setColorId(object.getModel().getColorId());
//            if(object.getModel().Variant().getSellingPrice() == 0.0000){
//                Detail(row).setSellPrice(object.getModel().Variant().getSellingPrice());
//            } else {
//                if(object.getModel().getSellingPrice() != null){
//                    Detail(row).setSellPrice(object.getModel().getSellingPrice().doubleValue());
//                } else {
//                    Detail(row).setSellPrice(null);
//                }
//            }
//        }
//        
//        System.out.println("StockID : " + Detail(row).Inventory().getStockId());
//        System.out.println("Description  : " + Detail(row).Inventory().getDescription());
//        return poJSON;
//    }
    /**
     * Check Existing product in detail
     *
     * @param row
     * @param brandId
     * @param modelId
     * @param modelVariantId
     * @param colorId
     * @param stockId
     * @return
     */
    private JSONObject checkExistingDetail(int row, String brandId, String modelId, String modelVariantId, String colorId, String stockId) {
        poJSON = new JSONObject();
        poJSON.put("row", row);

        brandId = (brandId == null) ? "" : brandId;
        modelId = (modelId == null) ? "" : modelId;
        modelVariantId = (modelVariantId == null) ? "" : modelVariantId;
        colorId = (colorId == null) ? "" : colorId;
        stockId = (stockId == null) ? "" : stockId;

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            if (lnCtr != row) {
                //Check Existing Stock ID
                if (!"".equals(stockId)) {
                    if (stockId.equals(Detail(lnCtr).getStockId())) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Item Description already exists in the transaction detail at row " + Detail(lnCtr).getPriority() + ".");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }
                }

                //Check Existing Brand and Model
                if (brandId.equals(Detail(lnCtr).getBrandId())
                        && modelId.equals(Detail(lnCtr).getModelId())
                        && modelVariantId.equals(Detail(lnCtr).getModelVarianId())) {

                    //Check if there is brand and model without color Id
                    if (Detail(lnCtr).getColorId() == null || "".equals(Detail(lnCtr).getColorId())) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Brand, model and variant already exists without color at row " + Detail(lnCtr).getPriority() + ".");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }

                    //Check if brand, model and color already exists in the transaction detail
                    if (brandId.equals(Detail(lnCtr).getBrandId())
                            && modelId.equals(Detail(lnCtr).getModelId())
                            && modelVariantId.equals(Detail(lnCtr).getModelVarianId())
                            && colorId.equals(Detail(lnCtr).getColorId())) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Item Description already exists in the transaction detail at row " + Detail(lnCtr).getPriority() + ".");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }
                }

            }
        }

        return poJSON;
    }

    /**
     * Validate maximum product to inquire
     *
     * @return
     */
    public JSONObject checkMaximumInqDetail() {
        poJSON = new JSONObject();
        //Check if client type is corporate allow only 5 inquiry detail
        switch (Master().getCategoryCode()) {
            case SalesInquiryStatic.CategoryCode.CAR:
            case SalesInquiryStatic.CategoryCode.MOTORCYCLE:
            case SalesInquiryStatic.CategoryCode.APPLIANCES:
            case SalesInquiryStatic.CategoryCode.MOBILEPHONE:
                //Corporate
                if (Master().getClientType().equals(SalesInquiryStatic.ClientType.CORPORATE)) {
                    if (getDetailCount() > 5) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "You can only inquire up to 5 items for corporate client.");
                        return poJSON;
                    }
                }
                break;
        }
//        if(Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.CAR)
//                || Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.MOTORCYCLE)
//                || Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.APPLIANCES)
//                || Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.MOBILEPHONE)){
//            
//            //Corporate
//            if(Master().getClientType().equals(SalesInquiryStatic.ClientType.CORPORATE)){
//                if(getDetailCount() > 5){
//                    poJSON.put("result", "error");
//                    poJSON.put("message", "You can only inquire up to 5 items for corporate client.");
//                    return poJSON;
//                }
//            }
//        }
        return poJSON;
    }

    /**
     * Search Receiving Sales Man
     *
     * @param value
     * @param byCode
     * @param row
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchReceivedBy(String value, boolean byCode, int row)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Salesman object = new SalesControllers(poGRider, logwrapr).Salesman();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode, Master().getBranchCode());
        if ("success".equals((String) poJSON.get("result"))) {
            SalesInquiryRequimentsList(row).setReceivedBy(object.getModel().getEmployeeId());
        }

        return poJSON;
    }

    /**
     * Search Bank
     *
     * @param value
     * @param byCode
     * @param row
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchBank(String value, boolean byCode, int row)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);

        if (!(Master().getPurchaseType().equals(SalesInquiryStatic.PurchaseType.PO)
                || Master().getPurchaseType().equals(SalesInquiryStatic.PurchaseType.FINANCING))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Sales Inquiry purchase type must be PO or Financing.");
            return poJSON;
        }

        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        poJSON.put("row", row);
        if ("success".equals((String) poJSON.get("result"))) {
            poJSON = checkExistingBank(object.getModel().getBankID(), row);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            BankApplicationsList(row).setBankId(object.getModel().getBankID());
            BankApplicationsList(row).setAppliedDate(poGRider.getServerDate());
        }

        System.out.println("Bank Name : " + BankApplicationsList(row).Bank().getBankName());

        poJSON.put("row", row);
        return poJSON;
    }

    /**
     * Check Existing Bank
     *
     * @param bankId
     * @param row
     * @return JSONObject success or error
     */
    private JSONObject checkExistingBank(String bankId, int row) {
        poJSON = new JSONObject();

        for (int lnCtr = 0; lnCtr <= getBankApplicationsCount() - 1; lnCtr++) {
            if (lnCtr != row) {
                if (BankApplicationsList(lnCtr).getTransactionStatus().equals(BankApplicationStatus.OPEN)
                        || BankApplicationsList(lnCtr).getTransactionStatus().equals(BankApplicationStatus.APPROVED)) {

                    if (bankId.equals(BankApplicationsList(lnCtr).getBankId())) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Bank already exists in the table at row " + (lnCtr + 1) + ".");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }

                }
            }
        }

        return poJSON;
    }

    /**
     * Load Requirements
     *
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject loadRequirements()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paRequirements = new ArrayList<>();

        List loList = getSalesInquiryRequirements();
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paRequirements.add(SalesInquiryRequirement());
            poJSON = paRequirements.get(getSalesInquiryRequirementsCount() - 1).openRecord((String) loList.get(lnCtr), lnCtr + 1);
            if ("success".equals((String) poJSON.get("result"))) {
                if (Master().getEditMode() == EditMode.UPDATE) {
                    poJSON = paRequirements.get(getSalesInquiryRequirementsCount() - 1).updateRecord();
                }
            }

            System.out.println("Requirements Code : " + paRequirements.get(getSalesInquiryRequirementsCount() - 1).getRequirementCode());
        }
        return poJSON;
    }

    /**
     * Load Sales Inquiry Requirements
     *
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    private List getSalesInquiryRequirements() throws SQLException, GuanzonException {
        String lsSQL = salesInquiryRequirementSQL();
        lsSQL = MiscUtil.addCondition(lsSQL, "sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()));
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        List<String> loList = new ArrayList();
        while (loRS.next()) {
            loList.add(loRS.getString("sTransNox"));
        }
        return loList;
    }

    /**
     * Load Requirements per group
     *
     * @param customerGroup
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject getRequirements(String customerGroup)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paRequirements == null) {
            paRequirements = new ArrayList<>();
        }

        if (Master().getEditMode() == EditMode.UPDATE || Master().getEditMode() == EditMode.ADDNEW) {
        } else {
            return poJSON;
        }

        try {
            String lsSQL = MiscUtil.addCondition(requirementPerGroupSQL(),
                    " a.cCustGrpx = " + SQLUtil.toSQL(customerGroup)
                    + " AND a.cPayModex = " + SQLUtil.toSQL(Master().getPurchaseType())
                    + " AND a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
            ) + " ORDER BY b.sDescript ASC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);

            int lnctr = 0;
            if (MiscUtil.RecordCount(loRS) > 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sRqrmtIDx: " + loRS.getString("sRqrmtIDx"));
                    System.out.println("sRqrmtCde: " + loRS.getString("sRqrmtCde"));
                    System.out.println("cCustGrpx: " + loRS.getString("cCustGrpx"));
                    System.out.println("sDescript: " + loRS.getString("sDescript"));
                    System.out.println("------------------------------------------------------------------------------");

                    poJSON = populateRequirements(loRS.getString("sRqrmtCde"), customerGroup);
                    if ("error".equals((String) poJSON.get("result"))) {
                        break;
                    }
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");

            } else {
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No requirements found.\nPlease contact the System Administrator for assistance.\n\nCustomer group will revert to < Any >.");
            }

            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
            return poJSON;
        }

        removeRequirements(customerGroup);
        return poJSON;
    }

    private Model_Sales_Inquiry_Requirements SalesInquiryRequirement() {
        return new SalesModels(poGRider).SalesInquiryRequirements();
    }

    public int getSalesInquiryRequirementsCount() {
        if (paRequirements == null) {
            paRequirements = new ArrayList<>();
        }

        return paRequirements.size();
    }

    public Model_Sales_Inquiry_Requirements SalesInquiryRequimentsList(int row) {
        return (Model_Sales_Inquiry_Requirements) paRequirements.get(row);
    }

    public List<Model_Sales_Inquiry_Requirements> SalesInquiryRequimentsList() {
        return paRequirements;
    }

    public String getCustomerGroup() throws SQLException, GuanzonException {
        if (getSalesInquiryRequirementsCount() == 0) {
            return "0"; //Default falue
        }
        return paRequirements.get(0).RequirementSourcePerGroup(Master().getPurchaseType()).getCustomerGroup();
    }

    private JSONObject populateRequirements(String requirementCode, String customerGroup) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        boolean lbExist = false;
        int lnRow = 0;
        for (int lnCtr = 0; lnCtr <= getSalesInquiryRequirementsCount() - 1; lnCtr++) {
            if (requirementCode.equals(SalesInquiryRequimentsList(lnCtr).getRequirementCode())) {
                lbExist = true;
                lnRow = lnCtr;
                break;
            }
        }

        if (!lbExist) {
            paRequirements.add(SalesInquiryRequirement());
            lnRow = getSalesInquiryRequirementsCount() - 1;
            paRequirements.get(lnRow).newRecord();
        }

        SalesInquiryRequimentsList(lnRow).setRequirementCode(requirementCode);
        SalesInquiryRequimentsList(lnRow).isRequired(true);
        SalesInquiryRequimentsList(lnRow).setCustomerGroup(customerGroup);

        poJSON.put("result", "success");
        return poJSON;
    }

    private void removeRequirements(String customerGroup) {
        Iterator<Model_Sales_Inquiry_Requirements> requirements = paRequirements.iterator();
        while (requirements.hasNext()) {
            Model_Sales_Inquiry_Requirements item = requirements.next();
            if (!customerGroup.equals(item.getCustomerGroup())) {
                System.out.println("Remove Customer Group : " + customerGroup);
                System.out.println("Remove Requirements Code : " + item.getRequirementCode());
                if (item.getEditMode() == EditMode.UPDATE) {
                    paRequirementsRemoved.add(item);
                }

                requirements.remove();
            }
        }
    }

//    private JSONObject populateRequirements(String requirementCode, String customerGroup) throws SQLException, GuanzonException{
//        poJSON = new JSONObject ();
//        for(int lnCtr = 0; lnCtr <= getSalesInquiryRequirementsCount() - 1; lnCtr++){
//            if(requirementCode.equals(paRequirements.get(lnCtr).getRequirementCode())){
//                poJSON.put("result", "success");  
//                return poJSON;
//            }
//        }
//        
//        paRequirements.add(SalesInquiryRequirement());
//        paRequirements.get(getSalesInquiryRequirementsCount() - 1).newRecord();
//        paRequirements.get(getSalesInquiryRequirementsCount() - 1).setRequirementCode(requirementCode); //TODO
//        paRequirements.get(getSalesInquiryRequirementsCount() - 1).isRequired(true);
//        paRequirements.get(getSalesInquiryRequirementsCount() - 1).setCustomerGroup(customerGroup);
//
//        poJSON.put("result", "success");  
//        return poJSON;
//    }
//    private JSONObject populateRequirements(String requirementCode) throws SQLException, GuanzonException{
//        poJSON = new JSONObject ();
////        Model_Requirement_Source_PerGroup object = new SalesModels(poGRider).RequirementSourcePerGroup();
////        poJSON = object.openRecord(requirementCode, Master().getPurchaseType());
////        if ("error".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//        
//        for(int lnCtr = 0; lnCtr <= getSalesInquiryRequirementsCount() - 1; lnCtr++){
////            if(paRequirements.get(lnCtr).getRequirementCode().equals(requirementId)){
////                if(Master().getEditMode() == EditMode.UPDATE){
////                    if(paRequirements.get(lnCtr).getEditMode() != EditMode.ADDNEW 
////                            && paRequirements.get(lnCtr).getEditMode() != EditMode.UPDATE){
////                        poJSON = paRequirements.get(lnCtr).updateRecord();
////                    }
////                }
////                return poJSON;
////            }
//            //Update Editmode
//            if(Master().getEditMode() == EditMode.UPDATE){
//                if(paRequirements.get(lnCtr).getEditMode() != EditMode.ADDNEW 
//                        && paRequirements.get(lnCtr).getEditMode() != EditMode.UPDATE){
//                    poJSON = paRequirements.get(lnCtr).updateRecord();
//                }
//            }
//            
////            if(paRequirements.get(lnCtr).getRequirementCode().equals(requirementCode)){
////                return poJSON;
////            }
//            
////            if(!paRequirements.get(lnCtr).getRequirementCode().equals(requirementId)
////                    && paRequirements.get(lnCtr).RequirementSourcePerGroup().getRequirementCode().equals(object.getRequirementCode())){
////                //Update requirement ID
////                paRequirements.get(getSalesInquiryRequirementsCount() - 1).setRequirementCode(requirementId); 
////                paRequirements.get(getSalesInquiryRequirementsCount() - 1).isRequired(object.isRequired());
////                return poJSON;
////            }
//        }
//        
//        paRequirements.add(SalesInquiryRequirement());
//        paRequirements.get(getSalesInquiryRequirementsCount() - 1).newRecord();
//        paRequirements.get(getSalesInquiryRequirementsCount() - 1).setRequirementCode(requirementCode); //TODO
//        paRequirements.get(getSalesInquiryRequirementsCount() - 1).isRequired(true);
//
//        poJSON.put("result", "success");  
//        return poJSON;
//    }
    public JSONObject removeRequirements() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Iterator<Model_Sales_Inquiry_Requirements> requirements = SalesInquiryRequimentsList().iterator();
        while (requirements.hasNext()) {
            Model_Sales_Inquiry_Requirements item = requirements.next();
            if (item.getEditMode() == EditMode.UPDATE) {
                paRequirementsRemoved.add(item);
            }

            requirements.remove();
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    public JSONObject removeBankApplications() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Iterator<Model_Sales_Commitment_Master> object = BankApplicationsList().iterator();
        while (object.hasNext()) {
            Model_Sales_Commitment_Master item = object.next();
            if (item.getEditMode() == EditMode.ADDNEW) {
                object.remove();
            }
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

//    public JSONObject removeRequirements(String customerGroup, String paymentMode) throws SQLException, GuanzonException {
//        poJSON = new JSONObject();
//        Iterator<Model_Sales_Inquiry_Requirements> requirements = SalesInquiryRequimentsList().iterator();
//        while (requirements.hasNext()) {
//            Model_Sales_Inquiry_Requirements item = requirements.next();
//            if ( !customerGroup.equals(item.RequirementSourcePerGroup().getCustomerGroup())
//                    &&  !paymentMode.equals(item.RequirementSourcePerGroup().getPaymentMode())){
//                if (item.getEditMode() == EditMode.UPDATE) {
//                    paRequirementsRemoved.add(item);
//                }
//                
//                requirements.remove();
//            }
//            
//        }
//
//        poJSON.put("result", "success");
//        poJSON.put("message", "success");
//        return poJSON;
//    }
    public int getRequirementsRemovedCount() {
        if (paRequirementsRemoved == null) {
            paRequirementsRemoved = new ArrayList<>();
        }

        return paRequirementsRemoved.size();
    }

    public Model_Sales_Inquiry_Requirements RequirementsRemove(int row) {
        return (Model_Sales_Inquiry_Requirements) paRequirementsRemoved.get(row);
    }

    public void RequirementsDetail(Model_Sales_Inquiry_Requirements item) {
        if (paRequirementsRemoved == null) {
            paRequirementsRemoved = new ArrayList<>();
        }

        paRequirementsRemoved.add(item);
    }

    public JSONObject addBankApplication()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paBankApplications == null) {
            paBankApplications = new ArrayList<>();
        }

        if (paBankApplications.isEmpty()) {
            paBankApplications.add(BankApplication());
            poJSON = paBankApplications.get(getBankApplicationsCount() - 1).newRecord();
        } else {
            if ((paBankApplications.get(paBankApplications.size() - 1).getPONumber() != null && !"".equals(paBankApplications.get(paBankApplications.size() - 1).getPONumber()))
                    && (paBankApplications.get(paBankApplications.size() - 1).getBankId() != null && !"".equals(paBankApplications.get(paBankApplications.size() - 1).getBankId()))) {
                paBankApplications.add(BankApplication());
                poJSON = paBankApplications.get(getBankApplicationsCount() - 1).newRecord();
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add bank application.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;

    }

    public JSONObject loadBankApplications()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paBankApplications = null;
        List loList = getBankApplications();
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            if (paBankApplications == null) {
                paBankApplications = new ArrayList<>();
            }
            paBankApplications.add(BankApplication());
            poJSON = paBankApplications.get(getBankApplicationsCount() - 1).openRecord((String) loList.get(lnCtr));

        }
        return poJSON;
    }

//    public void loadBankApplicationList() 
//            throws CloneNotSupportedException, 
//            SQLException, 
//            GuanzonException{     
//
//         int lnRow = getBankApplicationsCount() - 1;
//         while (lnRow >= 0) {
//             if ((paBankApplications.get(lnRow).getApplicationNo() == null || "".equals(paBankApplications.get(lnRow).getApplicationNo()))
//                     && (paBankApplications.get(lnRow).getBankId()== null || "".equals(paBankApplications.get(lnRow).getBankId()))) {
//                 paBankApplications.remove(lnRow);
//             }
//             lnRow--;
//         }
//
//         if ((getBankApplicationsCount()- 1) >= 0) {
//             if ((paBankApplications.get(getBankApplicationsCount() - 1).getApplicationNo() != null
//                     && !"".equals(paBankApplications.get(getBankApplicationsCount() - 1).getApplicationNo()))
//                 || (paBankApplications.get(getDetailCount() - 1).getBankId()!= null
//                     && !"".equals(paBankApplications.get(getBankApplicationsCount() - 1).getBankId()))) {
//                 addBankApplication();
//             }
//         }
//
//         if ((getBankApplicationsCount() - 1) < 0) {
//             addBankApplication();
//         }
//    }
    public void loadBankApplicationList()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {

//        if(getEditMode() == EditMode.ADDNEW || getEditMode() == EditMode.UPDATE){
//        } else {
//            return;
//        }
//        String lsBankApplicationNo = "";
//        int lnRow = getBankApplicationsCount() - 1;
//        while (lnRow >= 0) {
//            if (paBankApplications.get(lnRow).getBankId()== null || "".equals(paBankApplications.get(lnRow).getBankId())) {
//                if (paBankApplications.get(lnRow).getApplicationNo()!= null
//                    && !"".equals(paBankApplications.get(lnRow).getApplicationNo())) {
//                    lsBankApplicationNo = paBankApplications.get(lnRow).getApplicationNo();
//                }
//                paBankApplications.remove(lnRow);
//            }
//            lnRow--;
//        }
//
//        if ((getBankApplicationsCount()- 1) >= 0) {
//            if (paBankApplications.get(getBankApplicationsCount() - 1).getBankId()!= null
//                    && !"".equals(paBankApplications.get(getBankApplicationsCount() - 1).getBankId())) {
//                addBankApplication();
//            }
//        }
//
//        if ((getBankApplicationsCount() - 1) < 0) {
//            addBankApplication();
//        }
//        
//        if (!lsBankApplicationNo.isEmpty()) {
//            paBankApplications.get(getBankApplicationsCount() - 1).setApplicationNo(lsBankApplicationNo);
//        }
//        
//        paBankApplications.get(getBankApplicationsCount() - 1).setPaymentMode(Master().getPurchaseType());
    }

    private List getBankApplications() throws SQLException, GuanzonException {
        String lsSQL = bankApplicationSQL();
        lsSQL = MiscUtil.addCondition(lsSQL, " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCd = " + SQLUtil.toSQL(getSourceCode())
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        List<String> loList = new ArrayList();
        while (loRS.next()) {
            loList.add(loRS.getString("sTransNox"));
        }
        return loList;
    }

    private Model_Sales_Commitment_Master BankApplication() {
        return new SalesModels(poGRider).SalesCommitmentMaster();
    }

    public int getBankApplicationsCount() {
        if (paBankApplications == null) {
            paBankApplications = new ArrayList<>();
        }

        return paBankApplications.size();
    }

    public Model_Sales_Commitment_Master BankApplicationsList(int row) {
        return (Model_Sales_Commitment_Master) paBankApplications.get(row);
    }

    public List<Model_Sales_Commitment_Master> BankApplicationsList() {
        return paBankApplications;
    }

    public JSONObject checkPendingBankApplication() {
        for (int lnRow = 0; lnRow <= getBankApplicationsCount() - 1; lnRow++) {
            if (BankApplicationsList(lnRow).getEditMode() == EditMode.UPDATE) {
                if (BankApplicationsList(lnRow).getTransactionStatus().equals(BankApplicationStatus.OPEN)) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "You have a pending bank application. Update the status before changing the purchase type.");
                    return poJSON;
                }
            }
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject ApproveBankApplication(String remarks, int row)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {

        poJSON = new JSONObject();
        poJSON.put("result", "error");
        poJSON.put("message", "Not supported yet.");

//        String lsStatus = SalesInquiryStatic.APPROVED;
//        boolean lbStatus = true;
//        
//        if (paBankApplications.get(row).getEditMode() != EditMode.READY) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No bank application loaded.");
//            return poJSON;
//        }
//
//        if (lsStatus.equals((String) paBankApplications.get(row).getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Bank Application was already approved.");
//            return poJSON;
//        }
//
//        //validator
//        paBankApplications.get(row).setApprovedDate(poGRider.getServerDate());
//        poJSON = isEntryOkay_BankApplication(lsStatus, paBankApplications.get(row));
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        
//        //Require approval when user is not equal to sales man and user is not supervisor
//        if(!Master().getSalesMan().equals(poGRider.getUserID())){
//            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
//                poJSON = ShowDialogFX.getUserApproval(poGRider);
//                if (!"success".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                } else {
//                    if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "User is not an authorized approving officer.");
//                        return poJSON;
//                    }
//                }
//            }
//        }
//        
//        //change status
////        poJSON = statusChange(paBankApplications.get(row).getTable(), (String) paBankApplications.get(row).getValue("sTransNox"), remarks, lsStatus, !lbStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//
//        poJSON = paBankApplications.get(row).updateRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        
//        paBankApplications.get(row).setTransactionStatus(SalesInquiryStatic.APPROVED);
//        poJSON = paBankApplications.get(row).saveRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//        if (lbStatus) {
//            poJSON.put("message", "Bank Application approved successfully.");
//        } else {
//            poJSON.put("message", "Bank Application approval request submitted successfully.");
//        }
        return poJSON;
    }

    public JSONObject DisApproveBankApplication(String remarks, int row)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        poJSON.put("result", "error");
        poJSON.put("message", "Not supported yet.");

//        String lsStatus = SalesInquiryStatic.DISAPPROVED;
//        boolean lbStatus = true;
//
//        if (paBankApplications.get(row).getEditMode() != EditMode.READY) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No bank application loaded.");
//            return poJSON;
//        }
//
//        if (lsStatus.equals((String) paBankApplications.get(row).getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Bank Application was already dis-approved.");
//            return poJSON;
//        }
//
//        //validator
////        poJSON = isEntryOkay(lsStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//        
//        //Require approval when user is not equal to sales man and user is not supervisor
//        if(!Master().getSalesMan().equals(poGRider.getUserID())){
//            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
//                poJSON = ShowDialogFX.getUserApproval(poGRider);
//                if (!"success".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                } else {
//                    if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "User is not an authorized approving officer.");
//                        return poJSON;
//                    }
//                }
//            }
//        }
//        
//        //change status
////        poJSON = statusChange(paBankApplications.get(row).getTable(), (String) paBankApplications.get(row).getValue("sTransNox"), remarks, lsStatus, !lbStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//
//        poJSON = paBankApplications.get(row).updateRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        
//        paBankApplications.get(row).setTransactionStatus(SalesInquiryStatic.DISAPPROVED);
//        poJSON = paBankApplications.get(row).saveRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//        if (lbStatus) {
//            poJSON.put("message", "Bank Application dis-approve successfully.");
//        } else {
//            poJSON.put("message", "Bank Application dis-approval request submitted successfully.");
//        }
        return poJSON;
    }

    public JSONObject CancelBankApplication(String remarks, int row)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        poJSON.put("result", "error");
        poJSON.put("message", "Not supported yet.");

//        String lsStatus = SalesInquiryStatic.CANCELLED;
//        boolean lbStatus = true;
//        poJSON = paBankApplications.get(row).openRecord(paBankApplications.get(row).getTransactionNo(), row+1);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        
//        
//        if (paBankApplications.get(row).getEditMode() != EditMode.READY) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No bank application loaded.");
//            return poJSON;
//        }
//
//        if (lsStatus.equals((String) paBankApplications.get(row).getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Bank Application was already cancelled.");
//            return poJSON;
//        }
//
//        //validator
////        poJSON = isEntryOkay(lsStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//        
//        //Require approval when user is not equal to sales man and user is not supervisor
//        if(!Master().getSalesMan().equals(poGRider.getUserID())){
//            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
//                poJSON = ShowDialogFX.getUserApproval(poGRider);
//                if (!"success".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                } else {
//                    if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "User is not an authorized approving officer.");
//                        return poJSON;
//                    }
//                }
//            }
//        }
//        
//        //change status
////        poJSON = statusChange(paBankApplications.get(row).getTable(), (String) paBankApplications.get(row).getValue("sTransNox"), remarks, lsStatus, !lbStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//        poJSON = paBankApplications.get(row).updateRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        paBankApplications.get(row).setTransactionStatus(SalesInquiryStatic.CANCELLED);
//        System.out.println("edit mode : " + paBankApplications.get(row).getEditMode());
//        poJSON = paBankApplications.get(row).saveRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//        if (lbStatus) {
//            poJSON.put("message", "Bank Application cancelled successfully.");
//        } else {
//            poJSON.put("message", "Bank Application cancelation request submitted successfully.");
//        }
        return poJSON;
    }

//    public JSONObject CancelBankApplication(String remarks, int row)
//            throws ParseException,
//            SQLException,
//            GuanzonException,
//            CloneNotSupportedException {
//        poJSON = new JSONObject();
//
//        String lsStatus = SalesInquiryStatic.CANCELLED;
//        boolean lbStatus = true;
//        Model_Bank_Application object = new SalesModels(poGRider).BankApplication();
//        object.initialize();
//        poJSON = object.openRecord(paBankApplications.get(row).getTransactionNo(), row+1, paBankApplications.get(row).getApplicationNo());
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        
//        
//        if (object.getEditMode() != EditMode.READY) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No bank application loaded.");
//            return poJSON;
//        }
//
//        if (lsStatus.equals((String) object.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Bank Application was already cancelled.");
//            return poJSON;
//        }
//
//        //validator
////        poJSON = isEntryOkay(lsStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//        
//        //Require approval when user is not equal to sales man and user is not supervisor
//        if(!Master().getSalesMan().equals(poGRider.getUserID())){
//            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
//                poJSON = ShowDialogFX.getUserApproval(poGRider);
//                if (!"success".equals((String) poJSON.get("result"))) {
//                    return poJSON;
//                } else {
//                    if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "User is not an authorized approving officer.");
//                        return poJSON;
//                    }
//                }
//            }
//        }
//        
//        //change status
////        poJSON = statusChange(paBankApplications.get(row).getTable(), (String) paBankApplications.get(row).getValue("sTransNox"), remarks, lsStatus, !lbStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//        poJSON = object.updateRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//        object.setTransactionStatus(SalesInquiryStatic.CANCELLED);
//        System.out.println("edit mode : " + object.getEditMode());
//        poJSON = object.saveRecord();
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//        if (lbStatus) {
//            poJSON.put("message", "Bank Application cancelled successfully.");
//        } else {
//            poJSON.put("message", "Bank Application cancelation request submitted successfully.");
//        }
//
//        return poJSON;
//    }
    public JSONObject loadFollowUpHistory()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paFollowUpHistory = null;
        List loList = getFollowUpHistory();
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            if (paFollowUpHistory == null) {
                paFollowUpHistory = new ArrayList<>();
            }
            paFollowUpHistory.add(FollowUpHistory());
            poJSON = paFollowUpHistory.get(getFollowUpHistoryCount() - 1).openRecord((String) loList.get(lnCtr));

        }
        return poJSON;
    }

    private List getFollowUpHistory() throws SQLException, GuanzonException {
        Model_Customer_Inquiry_FollowUp loModel = new SalesModels(poGRider).CustomerInquiryFollowUp();
        loModel.initialize();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loModel), " sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo())
                + " AND sSourceCd = " + SQLUtil.toSQL(getSourceCode())
        );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        List<String> loList = new ArrayList();
        while (loRS.next()) {
            loList.add(loRS.getString("sTransNox"));
        }
        return loList;
    }

    private Model_Customer_Inquiry_FollowUp FollowUpHistory() {
        return new SalesModels(poGRider).CustomerInquiryFollowUp();
    }

    public int getFollowUpHistoryCount() {
        if (paFollowUpHistory == null) {
            paFollowUpHistory = new ArrayList<>();
        }

        return paFollowUpHistory.size();
    }

    public Model_Customer_Inquiry_FollowUp FollowUpHistoryList(int row) {
        return (Model_Customer_Inquiry_FollowUp) paFollowUpHistory.get(row);
    }

    public List<Model_Customer_Inquiry_FollowUp> FollowUpHistoryList() {
        return paFollowUpHistory;
    }

    List<String> paAttachmentsSource;

    /**
     * Loads and downloads all attachments for transaction details.
     *
     * @return JSONObject containing load result and message
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if a business logic error occurs
     */
    public JSONObject loadAttachments()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paAttachments = new ArrayList<>();
        paAttachmentsSource = new ArrayList<>();

        if (getFollowUpHistoryCount() <= 0) {
            poJSON.put("result", "success");
            poJSON.put("message", "success");
            return poJSON;
        }

        String lsSourceNo = "";
        String lsSourceCode = "";
        TransactionAttachment loAttachment = new SysTableContollers(poGRider, null).TransactionAttachment();
        for (int lnRow = 0; lnRow <= getFollowUpHistoryCount() - 1; lnRow++) {
            lsSourceNo = FollowUpHistoryList(lnRow).getTransactionNo();
            lsSourceCode = "CIFu";

            List loList = loAttachment.getAttachments(lsSourceCode, lsSourceNo);
            for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
                paAttachmentsSource.add(FollowUpHistoryList(lnRow).getTransactionNo() + "-" + xsDateShort(FollowUpHistoryList(lnRow).getTransactionDate()));
                paAttachments.add(TransactionAttachment());
                poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
                if ("success".equals((String) poJSON.get("result"))) {
                    if (Master().getEditMode() == EditMode.UPDATE) {
                        poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).updateRecord();
                    }
                    System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getTransactionNo());
                    System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo());
                    System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceCode());
                    System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName());

                    //Download Attachments
                    poJSON = WebFile.DownloadFile(WebFile.getAccessToken(System.getProperty("sys.default.access.token")),
                            "0032" //Constant
                            ,
                             "" //Empty
                            ,
                             paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName(),
                            paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceCode(),
                            paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo(),
                            "");
                    if ("success".equals((String) poJSON.get("result"))) {

                        poJSON = (JSONObject) poJSON.get("payload");
                        if (WebFile.Base64ToFile((String) poJSON.get("data"),
                                (String) poJSON.get("hash"),
                                System.getProperty("sys.default.path.temp.attachments") + "/",
                                (String) poJSON.get("filename"))) {
                            System.out.println("poJSON success: " + poJSON.toJSONString());
                            System.out.println("File downloaded succesfully.");
                        } else {
                            System.out.println("poJSON error: " + poJSON.toJSONString());
                            poJSON.put("result", "error");
                            poJSON.put("message", "Unable to download file.");
                        }

                    } else {
                        poJSON = (JSONObject) poJSON.get("error");
                        poJSON.put("result", "error");
                        System.out.println("ERROR WebFile.DownloadFile: " + poJSON.get("message"));
                        System.out.println("poJSON error WebFile.DownloadFile: " + poJSON.toJSONString());
                        System.out.println("SKIP THE FILE TO LOAD");
                    }
                }
            }
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    /**
     * Creates a new TransactionAttachment instance.
     *
     * @return new TransactionAttachment object
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if a business logic error occurs
     */
    private TransactionAttachment TransactionAttachment()
            throws SQLException,
            GuanzonException {
        return new SysTableContollers(poGRider, null).TransactionAttachment();
    }

    /**
     * Gets a transaction attachment by row index.
     *
     * @param row index of the attachment
     * @return TransactionAttachment object
     */
    public TransactionAttachment TransactionAttachmentList(int row) {
        return (TransactionAttachment) paAttachments.get(row);
    }

    /**
     * Gets the source description of a transaction attachment.
     *
     * @param row index of the attachment
     * @return source description string
     */
    public String TransactionAttachmentSource(int row) {
        return (String) paAttachmentsSource.get(row);
    }

    /**
     * Gets the total number of transaction attachments.
     *
     * @return attachment count
     */
    public int getTransactionAttachmentCount() {
        if (paAttachments == null) {
            paAttachments = new ArrayList<>();
        }

        return paAttachments.size();
    }

    public JSONObject addClient() throws SQLException, GuanzonException, Exception {
        //initialize new json for result
        JSONObject loResult = new JSONObject();

        String lsClientId = Master().getClientId();
        lsClientId = (lsClientId == null || lsClientId.isEmpty()) ? "" : lsClientId;

        //initialize Client GUI
        ClientGUI loClient = new ClientGUI();

        loClient.setGRider(poGRider);
        loClient.setLogWrapper(null);

        loClient.setClientType(Master().getClientType());
        //filter client type
        if (!ClientType.INDIVIDUAL.equals(Master().getClientType())) { // For institution entry
            loClient.setCategoryCode(Master().getCategoryCode());
        }

        //searchRecord(fsValue,fbByCode) will run make sure to set client and bycode
        //bycode true client id
        //bycode false company
        //set search by code
        loClient.setByCode(false);

        //set cilent empty, to create a new record
        //Arsiela - 05-22-2026 - Load Client of Create new Client
        loClient.setClientId(lsClientId);

        //load record
        CommonUtils.showModal(loClient);

        //load if button
        if (!loClient.isCancelled()) {
            lsClientId = loClient.getClient().getModel().getClientId();
//            //check existing record of client id to other supplier accreditation records
//            loResult = checkDuplicateAgent(lsClientId); //Moved script to method by Arsiela 05-23-2026 09:19 AM
//            if ("error".equals((String) loResult.get("result"))) {
//                return loResult;
//            }

            //set company id for supplier accreditation
            Master().setClientId(lsClientId != null ? lsClientId : "");
            Master().setAddressId(Master().ClientAddress().getAddressId()); //TODO
            Master().setContactId(Master().ClientMobile().getMobileId()); //TODO

        }
        loResult.put("result", "success");
        return loResult;
    }

    public JSONObject loadSalesInquiry(String industryId, String client, String referenceNo) {
        try {
            if (industryId == null) {
                industryId = psIndustryId;
            }
            if (client == null) {
                client = "";
            }
            if (referenceNo == null) {
                referenceNo = "";
            }

            String lsTransStat = "";
            if (psTranStat != null) {
                if (psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                        lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                    }
                    lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
                } else {
                    lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
                }
            }

            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(industryId)
                    + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                    + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                    + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + client)
                    + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + referenceNo)
                    + " AND a.cProcessd = '0' "
            //                    + " AND ( a.cTranStat = "  + SQLUtil.toSQL(SalesInquiryStatic.CONFIRMED)
            //                    + " OR a.cTranStat = "  + SQLUtil.toSQL(SalesInquiryStatic.OPEN)
            //                    + " ) "
            );

            //If current user is an ordinary user load only its inquiries
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                lsSQL = MiscUtil.addCondition(lsSQL,
                        " a.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
            }

            if (lsTransStat != null && !"".equals(lsTransStat)) {
                lsSQL = lsSQL + lsTransStat;
            }

            lsSQL = lsSQL + " ORDER BY a.dTransact DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMasterList = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sCompnyNm: " + loRS.getString("sClientNm"));
                    System.out.println("------------------------------------------------------------------------------");

                    paMasterList.add(SalesInquiryMaster());
                    paMasterList.get(paMasterList.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paMasterList = new ArrayList<>();
                paMasterList.add(SalesInquiryMaster());
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        return poJSON;
    }

    private Model_Sales_Inquiry_Master SalesInquiryMaster() {
        return new SalesModels(poGRider).SalesInquiryMaster();
    }

    public int getSalesInquiryCount() {
        if (paMasterList == null) {
            paMasterList = new ArrayList<>();
        }

        return paMasterList.size();
    }

    public Model_Sales_Inquiry_Master SalesInquiryList(int row) {
        return (Model_Sales_Inquiry_Master) paMasterList.get(row);
    }

    public void sortPriority() {
        Detail().sort((item1, item2) -> {
            Integer lnPriority1 = (Integer) item1.getValue("nPriority");
            Integer lnPriority2 = (Integer) item2.getValue("nPriority");

            if (lnPriority1 == 0 && lnPriority2 != 0) {
                return 1;
            } else if (lnPriority2 == 0 && lnPriority1 != 0) {
                return -1;
            } else {
                return lnPriority1.compareTo(lnPriority2);
            }
        });

        //Update priority no
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setPriority(lnCtr + 1);
        }

    }

    public void sortEntryNo() {
        Detail().sort((item1, item2) -> {
            Integer lnEntry1 = (Integer) item1.getValue("nEntryNox");
            Integer lnEntry2 = (Integer) item2.getValue("nEntryNox");

            if (lnEntry1 == 0 && lnEntry2 != 0) {
                return 1;
            } else if (lnEntry2 == 0 && lnEntry1 != 0) {
                return -1;
            } else {
                return lnEntry1.compareTo(lnEntry2);
            }
        });

    }

    /*Convert Date to String*/
    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }

    public void setIndustryId(String industryId) {
        psIndustryId = industryId;
    }

    public void setCompanyId(String companyId) {
        psCompanyId = companyId;
    }

    public void setCategoryId(String categoryId) {
        psCategorCd = categoryId;
    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Sales_Inquiry_Master Master() {
        return (Model_Sales_Inquiry_Master) poMaster;
    }

    @Override
    public Model_Sales_Inquiry_Detail Detail(int row) {
        return (Model_Sales_Inquiry_Detail) paDetail.get(row);
    }

    public Model_Sales_Inquiry_Detail getDetail() {
        return (Model_Sales_Inquiry_Detail) poDetail;
    }

    @Override
    public int getDetailCount() {
        if (paDetail == null) {
            paDetail = new ArrayList<>();
        }

        return paDetail.size();
    }

    public JSONObject AddDetail()
            throws CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getDetailCount() > 0) {
            if (Detail(getDetailCount() - 1).getModelId() != null) {
                if (Detail(getDetailCount() - 1).getModelId().isEmpty()) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Last row has empty item.");
                    return poJSON;
                }
            }
        }

        return addDetail();
    }

    public void resetOthers() {
        paRequirements = new ArrayList<>();
        paBankApplications = new ArrayList<>();
        paRequirementsRemoved = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        paFollowUpHistory = new ArrayList<>();
        paAttachments = new ArrayList<>();
        paAttachmentsSource = new ArrayList<>();
    }

    public void resetMaster() {
        poMaster = new SalesModels(poGRider).SalesInquiryMaster();
    }

    public JSONObject removeDetails() {
        poJSON = new JSONObject();
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            if (item.getEditMode() == EditMode.UPDATE) {
                paDetailRemoved.add(item);
            }

            detail.remove();
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    public int getDetailRemovedCount() {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        return paDetailRemoved.size();
    }

    public Model_Sales_Inquiry_Detail DetailRemove(int row) {
        return (Model_Sales_Inquiry_Detail) paDetailRemoved.get(row);
    }

    public void removeDetail(Model_Sales_Inquiry_Detail item) {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        paDetailRemoved.add(item);
    }

    public void loadDetail() throws CloneNotSupportedException {
        String lsBrandId = "";
        String lsCategory = "";
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            System.out.println("Brand : " + Detail(lnCtr).getBrandId());
            System.out.println("Category : " + Detail(lnCtr).getCategory());
            System.out.println("Model : " + Detail(lnCtr).getModelId());
            System.out.println("Model Variant : " + Detail(lnCtr).getModelVarianId());
            System.out.println("Color : " + Detail(lnCtr).getColorId());
            if ((Detail(lnCtr).getStockId() == null || "".equals(Detail(lnCtr).getStockId()))
                    && (Detail(lnCtr).getModelId() == null || "".equals(Detail(lnCtr).getModelId()))) {
                if (Detail(lnCtr).getBrandId() != null
                        && !"".equals(Detail(lnCtr).getBrandId())) {
                    lsBrandId = Detail(lnCtr).getBrandId();
                }

                if (Detail(lnCtr).getCategory() != null
                        && !"".equals(Detail(lnCtr).getCategory())) {
                    lsCategory = Detail(lnCtr).getCategory();
                }

                if (Detail(lnCtr).getEditMode() == EditMode.UPDATE) {
                    removeDetail(Detail(lnCtr));
                }
                deleteDetail(lnCtr);
                //Detail().remove(lnCtr);
            }
            lnCtr--;
        }

        if ((getDetailCount() - 1) >= 0) {
            if ((Detail(getDetailCount() - 1).getStockId() != null
                    && !"".equals(Detail(getDetailCount() - 1).getStockId()))
                    || (Detail(getDetailCount() - 1).getModelId() != null
                    && !"".equals(Detail(getDetailCount() - 1).getModelId()))) {
                AddDetail();
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }

        //Set brand Id to last row
        if (!lsBrandId.isEmpty()) {
            Detail(getDetailCount() - 1).setBrandId(lsBrandId);
        }
        if (!lsCategory.isEmpty()) {
            Detail(getDetailCount() - 1).setCategory(lsCategory);
        }
    }

    private JSONObject checkPendingInquiry(String clientId) {
        try {
            poJSON = new JSONObject();
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                    " a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryId())
                    + " AND a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyId())
                    + " AND a.sCategrCd = " + SQLUtil.toSQL(Master().getCategoryCode())
                    + " AND a.sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode())
                    + " AND a.sClientID = " + SQLUtil.toSQL(clientId)
                    + " AND a.sTransNox <> " + SQLUtil.toSQL(Master().getTransactionNo())
                    + " AND ( a.cTranStat = " + SQLUtil.toSQL(SalesInquiryStatic.OPEN)
                    + " OR a.cTranStat = " + SQLUtil.toSQL(SalesInquiryStatic.QUOTED)
                    + " OR a.cTranStat = " + SQLUtil.toSQL(SalesInquiryStatic.CONFIRMED)
                    + " ) ");
            lsSQL = lsSQL + " ORDER BY a.dTransact ASC ";
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sCompnyNm: " + loRS.getString("sClientNm"));
                    System.out.println("------------------------------------------------------------------------------");

                    poJSON.put("result", "error");
                    poJSON.put("message", "There is an ongoing sales inquiry for client " + loRS.getString("sClientNm").toUpperCase()
                            + "\nfrom sales person " + loRS.getString("sSalePrsn").toUpperCase() + ".\n\n"
                            + "Transaction No. : " + loRS.getString("sTransNox") + "\n"
                            + "Inquiry Date : " + loRS.getDate("dTransact"));
                    return poJSON;
                }
            } else {
                poJSON.put("result", "success");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;

    }

    @Override
    public JSONObject willSave()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();

        if (SalesInquiryStatic.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (!Master().getSalesMan().equals(getSysUser(poGRider.getUserID(), true))) {
                if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                    poJSON = ShowDialogFX.getUserApproval(poGRider);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    } else {
                        if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "User is not an authorized approving officer.");
                            return poJSON;
                        }
                    }
                }
            }

            if (Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.CAR)
                    || Master().getCategoryCode().equals(SalesInquiryStatic.CategoryCode.MOTORCYCLE)) {

                poJSON = checkRequirements();
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }

                poJSON = checkBankApplication();
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }
        }

        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        if (paRequirementsRemoved == null) {
            paRequirementsRemoved = new ArrayList<>();
        }

        if (Master().getEditMode() == EditMode.ADDNEW) {
            System.out.println("Will Save : " + Master().getNextCode());
            Master().setTransactionNo(Master().getNextCode());
        }

        //Set Original Client
        if (Master().getEditMode() == EditMode.UPDATE) {
            SalesInquiry object = new SalesControllers(poGRider, logwrapr).SalesInquiry();
            object.InitTransaction();
            object.OpenTransaction(Master().getTransactionNo());
            Master().setClientId(object.Master().getClientId());
            Master().setAddressId(object.Master().getAddressId());
            Master().setContactId(object.Master().getContactId());
        }

        Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());

        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            if ((item.getValue("sModelIDx") == null || "".equals(item.getValue("sModelIDx")))
                    && (item.getValue("sStockIDx") == null || "".equals(item.getValue("sStockIDx")))) {
                detail.remove();

                if (item.getEditMode() == EditMode.UPDATE) {
                    paDetailRemoved.add(item);
                }

            }
        }

        //remove bank application without details
//        Iterator<Model_Bank_Application> bankApplication = paBankApplications.iterator();
//        while (bankApplication.hasNext()) {
//            Model_Bank_Application item = bankApplication.next();
//            if (item.getBankId() == null || "".equals(item.getBankId())){
//                bankApplication.remove();
//            }
//        }
        //remove bank application without details
        Iterator<Model_Sales_Inquiry_Requirements> requirements = paRequirements.iterator();
        while (requirements.hasNext()) {
            Model_Sales_Inquiry_Requirements item = requirements.next();
            if (item.getRequirementCode() == null || "".equals(item.getRequirementCode())) {
                requirements.remove();
            }
        }

        //Validate detail after removing all zero qty and empty stock Id
        if (getDetailCount() <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transaction detail to be save.");
            return poJSON;
        }

        //assign other info on detail
        int lnEntryNo = 1;
        int lnRow = 0;
        System.out.println("Total Detail : " + getDetailCount());
        sortEntryNo();
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            System.out.println("Ctr " + lnCtr + " Entry No : " + Detail(lnCtr).getEntryNo());

            lnEntryNo = 1;
            //Update entry no if equal to 0
            if (Detail(lnCtr).getEntryNo() == 0) {
                for (lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                    if (Detail(lnRow).getEntryNo() == lnEntryNo) {
                        lnEntryNo++;
                    }
                }

                System.out.println("Ctr " + lnCtr + " SET Entry No : " + lnEntryNo);
                Detail(lnCtr).setEntryNo(lnEntryNo);
            } else {
                //Update entry no if more than the detail count
                if (Detail(lnCtr).getEntryNo() > getDetailCount()) {
                    for (lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                        if (Detail(lnRow).getEntryNo() == lnEntryNo) {
                            lnEntryNo++;
                        }
                    }

                    System.out.println("Ctr " + lnCtr + " SET Entry No : " + lnEntryNo);
                    Detail(lnCtr).setEntryNo(lnEntryNo);
                }
            }
        }

        for (lnRow = 0; lnRow <= getSalesInquiryRequirementsCount() - 1; lnRow++) {
            SalesInquiryRequimentsList(lnRow).setTransactionNo(Master().getTransactionNo());
            SalesInquiryRequimentsList(lnRow).setEntryNo(lnRow + 1);

            poJSON = isEntryOkay_SalesInquiryRequirements(SalesInquiryRequimentsList(lnRow));
            if (!"success".equals((String) poJSON.get("result"))) {
                poJSON.put("result", "error");
                return poJSON;
            }
        }

//        for(lnRow = 0; lnRow <= getBankApplicationsCount()- 1; lnRow++){
//            BankApplicationsList(lnRow).setTransactionNo(Master().getTransactionNo());
//            BankApplicationsList(lnRow).setEntryNo(lnRow+1);
//            
//            poJSON = isEntryOkay_BankApplication(BankApplicationsList(lnRow).getTransactionStatus(), BankApplicationsList(lnRow));
//            if (!"success".equals((String) poJSON.get("result"))) {
//                poJSON.put("result", "error");
//                return poJSON;
//            } 
//        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(SalesInquiryStatic.OPEN);
    }

    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }

    @Override
    public JSONObject saveOthers() {
        /*Only modify this if there are other tables to modify except the master and detail tables*/
        poJSON = new JSONObject();
        try {
            //Save Sales Inquiry Requirements
            for (int lnCtr = 0; lnCtr <= getSalesInquiryRequirementsCount() - 1; lnCtr++) {
                if (paRequirements.get(lnCtr).getEditMode() == EditMode.ADDNEW || paRequirements.get(lnCtr).getEditMode() == EditMode.UPDATE) {
                    paRequirements.get(lnCtr).setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
                    paRequirements.get(lnCtr).setModifiedDate(poGRider.getServerDate());
                    poJSON = paRequirements.get(lnCtr).saveRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.out.println("Save Sales Inquiry Requirements " + (String) poJSON.get("message"));
                        poJSON.put("result", "error");
                        poJSON.put("message", (String) poJSON.get("message"));
                        return poJSON;
                    }
                }
            }

            //Save Bank Applications
//            for (int lnCtr = 0; lnCtr <= getBankApplicationsCount()- 1; lnCtr++) {
//                if (paBankApplications.get(lnCtr).getEditMode() == EditMode.ADDNEW){
//                    paBankApplications.get(lnCtr).setEntryBy(poGRider.Encrypt(poGRider.getUserID()));
//                    paBankApplications.get(lnCtr).setEntryDate(poGRider.getServerDate());
//                }
//                if (paBankApplications.get(lnCtr).getEditMode() == EditMode.ADDNEW || paBankApplications.get(lnCtr).getEditMode() == EditMode.UPDATE) {
//                    paBankApplications.get(lnCtr).setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
//                    paBankApplications.get(lnCtr).setModifiedDate(poGRider.getServerDate());
//                    poJSON = paBankApplications.get(lnCtr).saveRecord();
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        System.out.println("Save Sales Inquiry Bank Application " + (String) poJSON.get("message"));
//                        poJSON.put("result", "error");
//                        poJSON.put("message", (String) poJSON.get("message"));
//                        return poJSON;
//                    }
//                }
//            }
            //Delete Record
            for (int lnCtr = 0; lnCtr <= getRequirementsRemovedCount() - 1; lnCtr++) {
                poJSON = paRequirementsRemoved.get(lnCtr).deleteRecord();
                if ("error".equals((String) poJSON.get("result"))) {
                    System.out.println("Delete Sales Inquiry Requirements " + (String) poJSON.get("message"));
                    poJSON.put("result", "error");
                    poJSON.put("message", (String) poJSON.get("message"));
                    return poJSON;
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            System.out.println("Dept ID : " + poGRider.getDepartment());
            System.out.println("Current User : " + poGRider.getUserID());

            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryId(psIndustryId);
            Master().setCompanyId(psCompanyId);
            Master().setCategoryCode(psCategorCd);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(SalesInquiryStatic.OPEN);
            Master().setInquiryStatus(SalesInquiryStatic.OPEN);

            LocalDate currentDate = strToDate(xsDateShort(poGRider.getServerDate())).plusMonths(1);
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
            Master().setTargetDate(SQLUtil.toDate(formattedDate, SQLUtil.FORMAT_SHORT_DATE));
            Master().setSalesMan(getSysUser(poGRider.getUserID(), true));
            Master().setPurchaseType("0");
            if (SalesInquiryStatic.CategoryCode.CAR.equals(Master().getCategoryCode())
                    || SalesInquiryStatic.CategoryCode.MOTORCYCLE.equals(Master().getCategoryCode())) {
                Master().setCategoryType("0");
            }
            Master().setClientType("0");

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = SalesInquiryValidatorFactory.make(Master().getIndustryId());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
//        loValidator.setDetail(paDetail);

        poJSON = loValidator.validate();

        return poJSON;
    }

    private JSONObject isEntryOkay_BankApplication(String status, Model_Bank_Application master) {
        GValidator loValidator = new BankApplication();

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(master);

        poJSON = loValidator.validate();

        return poJSON;
    }

    private JSONObject isEntryOkay_SalesInquiryRequirements(Model_Sales_Inquiry_Requirements master) {
        GValidator loValidator = new SalesInquiryRequirements();

        loValidator.setApplicationDriver(poGRider);
        loValidator.setMaster(master);

        poJSON = loValidator.validate();

        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = " SELECT "
                + " a.sTransNox "
                + " , a.dTransact "
                + " , a.cTranStat "
                + " , a.sClientID "
                + " , b.sCompnyNm AS sClientNm "
                + " , concat(c.sLastName,', ',c.sFrstName, ' ',c.sMiddName) AS sSalePrsn "
                + " , d.sCompnyNm AS sAgentNme "
                + " , e.sBranchNm "
                + " , f.sCompnyNm "
                + " , g.sDescript "
                + " FROM Sales_Inquiry_Master a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sClientID "
                + " LEFT JOIN Salesman c ON c.sEmployID = a.sSalesman "
                + " LEFT JOIN Client_Master d ON d.sClientID = a.sAgentIDx "
                + " LEFT JOIN Branch e ON e.sBranchCd = a.sBranchCd "
                + " LEFT JOIN Company f ON f.sCompnyID = a.sCompnyID "
                + " LEFT JOIN Industry g ON g.sIndstCdx = a.sIndstCdx ";

    }

    private String requirementPerGroupSQL() {
        return " SELECT  "
                + "   a.sRqrmtIDx "
                + " , a.cPayModex "
                + " , a.cCustGrpx "
                + " , a.sRqrmtCde "
                + " , a.cRequired "
                + " , a.cRecdStat "
                + " , a.sModified "
                + " , a.dModified "
                + " , b.sDescript "
                + "  FROM Requirement_Source_PerGroup a "
                + " LEFT JOIN Requirement_Source b ON b.sRqrmtCde = a.sRqrmtCde ";
    }

    private String salesInquiryRequirementSQL() {
        return " SELECT "
                + "    sTransNox "
                + "  , nEntryNox "
                + "  , sRqrmtCde "
                + "  , cRequired "
                + "  , cSubmittd "
                + "  , sReceived "
                + "  , dReceived "
                + "  , sModified "
                + "  , dModified "
                + " FROM Sales_Inquiry_Requirements ";
    }

    private String bankApplicationSQL() {
        return " SELECT "
                + "   sTransNox "
                + " , nEntryNox "
                + " , sPONumber "
                + " , cTranStat "
                + " FROM Sales_Commitment_Master ";
    }

    /**
     * Loads status history, maps status codes to captions, and displays the
     * status-history dialog.
     *
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If model operations fail.
     * @throws Exception If UI rendering fails.
     */
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception {
        CachedRowSet crs = getStatusHistory();

        crs.beforeFirst();
        while (crs.next()) {
            switch (crs.getString("cRefrStat")) {
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case SalesInquiryStatic.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case SalesInquiryStatic.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case SalesInquiryStatic.QUOTED:
                    crs.updateString("cRefrStat", "QUOTED");
                    break;
                case SalesInquiryStatic.LOST:
                    crs.updateString("cRefrStat", "LOST SALE");
                    break;
                case SalesInquiryStatic.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case SalesInquiryStatic.SALE:
                    crs.updateString("cRefrStat", "SALE");
                    break;
                case SalesInquiryStatic.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat) {
                        case SalesInquiryStatic.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case SalesInquiryStatic.CONFIRMED:
                            crs.updateString("cRefrStat", "CONFIRMED");
                            break;
                        case SalesInquiryStatic.QUOTED:
                            crs.updateString("cRefrStat", "QUOTED");
                            break;
                        case SalesInquiryStatic.LOST:
                            crs.updateString("cRefrStat", "LOST SALE");
                            break;
                        case SalesInquiryStatic.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case SalesInquiryStatic.SALE:
                            crs.updateString("cRefrStat", "SALE");
                            break;
                        case SalesInquiryStatic.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                    }
            }
            crs.updateRow();
        }

        JSONObject loJSON = getEntryBy();
        String entryBy = "";
        String entryDate = "";

        if ("success".equals((String) loJSON.get("result"))) {
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }
        showStatusHistoryUI("Sales Inquiry", Master().getTransactionNo(), entryBy, entryDate, crs);
    }

    /**
     * Resolves encoder name and entry timestamp from audit logs for the current
     * transaction.
     *
     * @return JSON result containing entry metadata.
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If user lookup operations fail.
     */
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM " + Master().getTable() + " a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Master().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(Master().getTransactionNo()));
        lsSQL = lsSQL + " ORDER BY b.dModified DESC ";
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if (loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))) {
                        if (loRS.getString("sModified").length() > 10) {
                            lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified")), false);
                        } else {
                            lsEntry = getSysUser(loRS.getString("sModified"), false);
                        }
                        // Get the LocalDateTime from your result set
                        LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                        lsEntryDate = dModified.format(formatter);
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }

    public String getSysUser(String fsId, boolean fbIsID) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL = " SELECT b.sCompnyNm, a.sEmployNo from xxxSysUser a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId));
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if (fbIsID) {
                        lsEntry = loRS.getString("sEmployNo");
                    } else {
                        lsEntry = loRS.getString("sCompnyNm");
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return lsEntry;
    }
}
