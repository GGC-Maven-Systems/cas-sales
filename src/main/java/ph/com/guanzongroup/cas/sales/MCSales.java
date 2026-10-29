package ph.com.guanzongroup.cas.sales;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.sql.rowset.CachedRowSet;
import org.apache.commons.codec.binary.Base64;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.Model_Transaction_Attachment;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscReplUtil;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebFile;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.appdriver.token.RequestAccess;
import org.guanzon.cas.client.ClientGUI;
import org.guanzon.cas.client.ClientInfo;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.inv.InventoryBrowse;
import org.guanzon.cas.parameter.InsuranceType;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.constant.SalesTransactionStatic;
import ph.com.guanzongroup.cas.sales.constant.Sales_Reservation_Static;
import ph.com.guanzongroup.cas.sales.mcpromo.MCPromoSales;
import ph.com.guanzongroup.cas.sales.mcpromo.services.PromoControllers;
import ph.com.guanzongroup.cas.sales.mcpromo.validator.MCPromoSalesValidatorFactory;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Giveaways;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Insurance;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Registration;
import static ph.com.guanzongroup.cas.sales.queries.SalesMasterQueries.getSalesClientBrowse;
import static ph.com.guanzongroup.cas.sales.queries.SalesMasterQueries.getSalesClientSource;
import static ph.com.guanzongroup.cas.sales.queries.SalesMasterQueries.getSalesMasterWithSource;
import static ph.com.guanzongroup.cas.sales.queries.SalesMasterQueries.getSalesPromotion;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.validator.MCSalesValidatorFactory;

public class MCSales extends Transaction {

    private String psIndustryCode = "";
    private String psCompanyID = "";
    private String psCategorCD = "";
    private String psClientType = "0";
    private List<Model> paMaster;
    private List<Model> paSalesGiveaways;
    private List<Model> paSalesInsurance;
    private List<Model> paSalesRegistration;
    private MCPromoSales poPromo;
    private String psApprovalUser = "";

    private List<TransactionAttachment> paAttachments;

    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }

    public void setCompanyID(String companyId) {
        psCompanyID = companyId;
    }

    public void setCategoryID(String categoryId) {
        psCategorCD = categoryId;
    }

    public void setClientType(String clientType) {
        psClientType = clientType;
    }

    public String getClientType() {
        return psClientType;
    }

    public Model_Sales_Master getMaster() {
        return (Model_Sales_Master) poMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Master> getMasterList() {
        return (List<Model_Sales_Master>) (List<?>) paMaster;
    }

    public Model_Sales_Master getMaster(int masterRow) {
        return (Model_Sales_Master) paMaster.get(masterRow);
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Detail> getSalesDetailList() {
        computeTransaction();
        return (List<Model_Sales_Detail>) (List<?>) paDetail;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Giveaways> getSalesGiveawaysList() {
        return (List<Model_Sales_Giveaways>) (List<?>) paSalesGiveaways;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Registration> getSalesRegistrationsList() {
        computeTransaction();
        return (List<Model_Sales_Registration>) (List<?>) paSalesRegistration;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Insurance> getSalesInsurancesList() {
        computeTransaction();
        return (List<Model_Sales_Insurance>) (List<?>) paSalesInsurance;
    }

    public int getCountSalesGiveAway() {
        return paSalesGiveaways.size();
    }

    public int getCountSalesInsurance() {
        return paSalesInsurance.size();
    }

    public int getCountSalesRegistration() {
        return paSalesRegistration.size();
    }

    public Model_Sales_Detail getDetail(int fnEntryNo) {
        if (getMaster().getTransactionNo().isEmpty()) {
            return null;
        }

        // Get the requested detail
        Model_Sales_Detail loDetail;

        // keep the last detail's keys in sync (only when there is one)
        if (!paDetail.isEmpty() && (getEditMode() == EditMode.ADDNEW || getEditMode() == EditMode.UPDATE)) {
            Model_Sales_Giveaways lastDetail = (Model_Sales_Giveaways) paSalesGiveaways.get(paSalesGiveaways.size() - 1);
            lastDetail.setTransNo(getMaster().getTransactionNo());
            lastDetail.setEntryNo(getCountSalesGiveAway());
        }

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Detail) paDetail.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                computeTransaction();
                return loDetail;
            }
        }

        // No match found — create new
        loDetail = new SalesModels(poGRider).SalesDetail();
        loDetail.newRecord();
        loDetail.setTransNo(getMaster().getTransactionNo());
        loDetail.setEntryNo(fnEntryNo);
        paDetail.add(loDetail);

        return loDetail;
    }

    public Model_Sales_Giveaways getSalesGiveaways(int fnEntryNo) {
        if (getMaster().getTransactionNo() == null || getMaster().getTransactionNo().isEmpty()) {
            return null;
        }

        Model_Sales_Giveaways loDetail;

        // keep the last detail's keys in sync (only when there is one)
        if (!paSalesGiveaways.isEmpty() && getEditMode() == EditMode.ADDNEW) {
            Model_Sales_Giveaways lastDetail = (Model_Sales_Giveaways) paSalesGiveaways.get(paSalesGiveaways.size() - 1);
            lastDetail.setTransNo(getMaster().getTransactionNo());
            lastDetail.setEntryNo(getCountSalesGiveAway());
        }

        // find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesGiveaways.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Giveaways) paSalesGiveaways.get(lnCtr);
            if (loDetail.getEntryNo() == fnEntryNo) {
                computeTransaction();
                return loDetail;
            }
        }

        // no match found, create new
        loDetail = new SalesModels(poGRider).SalesGiveaways();
        loDetail.newRecord();
        loDetail.setTransNo(getMaster().getTransactionNo());
        loDetail.setEntryNo(fnEntryNo);
        paSalesGiveaways.add(loDetail);

        return loDetail;
    }

    public Model_Sales_Insurance getSalesInsurance(int fnEntryNo) {
        if (getMaster().getTransactionNo().isEmpty()) {
            return null;
        }
        // Get the requested detail
        Model_Sales_Insurance loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesInsurance.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Insurance) paSalesInsurance.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                computeTransaction();
                return loDetail;
            }
        }

        // No match found — create new
        loDetail = new SalesModels(poGRider).SalesInsurance();
        loDetail.newRecord();
        loDetail.setTransNo(getMaster().getTransactionNo());
        loDetail.setEntryNo(fnEntryNo);
        paSalesInsurance.add(loDetail);

        return loDetail;
    }

    public Model_Sales_Registration getSalesRegistration(int fnEntryNo) {
        if (getMaster().getTransactionNo().isEmpty()) {
            return null;
        }

        // Get the requested detail
        Model_Sales_Registration loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesRegistration.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Registration) paSalesRegistration.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                computeTransaction();
                return loDetail;
            }
        }

        // No match found — create new
        loDetail = new SalesModels(poGRider).SalesRegistration();
        loDetail.newRecord();
        loDetail.setTransNo(getMaster().getTransactionNo());
        loDetail.setEntryNo(fnEntryNo);
        paSalesRegistration.add(loDetail);

        return loDetail;
    }

    private List<TransactionAttachment> TransactionAttachmentList() {
        return paAttachments;
    }

    public TransactionAttachment TransactionAttachmentList(int row) {
        return (TransactionAttachment) paAttachments.get(row);
    }

    @SuppressWarnings("unchecked")
    public List<Model_Transaction_Attachment> getAttachmentList() {
        return (List<Model_Transaction_Attachment>) (List<?>) paAttachments;
    }

    public Model_Transaction_Attachment getAttachmentList(int entryRow) {
        TransactionAttachment loAttachment = (TransactionAttachment) paAttachments.get(entryRow);
        return loAttachment.getModel();
    }

    public int getTransactionAttachmentCount() {
        if (paAttachments == null) {
            paAttachments = new ArrayList<>();
        }

        return paAttachments.size();
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "Sale";

        poMaster = new SalesModels(poGRider).SalesMaster();
        poDetail = new SalesModels(poGRider).SalesDetail();

        //Other Detail
        paSalesGiveaways = new ArrayList<Model>();
        paSalesInsurance = new ArrayList<Model>();
        paSalesRegistration = new ArrayList<Model>();
        paMaster = new ArrayList<Model>();
        paDetail = new ArrayList<Model>();
        paAttachments = new ArrayList<>();

        pbWithUI = true; //allow input

        return super.initialize();
    }

    public JSONObject searchTransaction(String value, boolean byCode, boolean byExact) {
        try {
            String lsSQL = getSalesMasterWithSource(poGRider.getIndustry());
            String lsCondition = "";
            if (psTranStat != null) {
                if (this.psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                        lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                    }
                    lsCondition = "cTranStat IN (" + lsCondition.substring(2) + ")";
                } else {
                    lsCondition = "cTranStat = " + SQLUtil.toSQL(this.psTranStat);
                }
                lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
            }

            if (!psCategorCD.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sCategrCd = " + SQLUtil.toSQL(psCategorCD));
            }

            lsSQL = lsSQL + " WHERE 1=1";
            lsSQL = lsSQL + " GROUP BY sTransNox ";

            System.out.println("search Transaction Query =\n" + lsSQL);
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    value,
                    "Transaction»Date»Name»Source»Source No»Source Date",
                    "sTransNox»dTransact»xClientNm»sSource»sSourceNo»dSourceDt",
                    "sTransNox»dTransact»xClientNm»sSource»sSourceNo»adSourceDt",
                    byExact ? (byCode ? 0 : 1) : 2);

            if (poJSON != null) {
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    return openTransaction((String) poJSON.get("sTransNox"));
                }
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(MCSales.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject OpenTransaction(String transaction) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(transaction);
    }

    public JSONObject openTransaction(String transaction) throws CloneNotSupportedException, SQLException, GuanzonException {
        if (transaction.isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction must not empty.");
            return poJSON;
        }

        poJSON = poMaster.openRecord(transaction);

        if ("error".equals((String) poJSON.get("result"))) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Open Transaction Record.");
            return poJSON;
        }
        paSalesGiveaways.clear();
        paSalesInsurance.clear();
        paSalesRegistration.clear();
        paDetail.clear();
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Sales_Giveaways
        String lsSQL = "SELECT * FROM Sales_Giveaways"
                + " WHERE sTransNox = " + SQLUtil.toSQL(transaction);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Giveaways loDetail = new SalesModels(poGRider).SalesGiveaways();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(transaction, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open sales Sales Giveaways record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesGiveaways.add(loDetail);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Sales_Insurance
        lsSQL = "SELECT * FROM Sales_Insurance"
                + " WHERE sTransNox = " + SQLUtil.toSQL(transaction);

        loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Insurance loDetail = new SalesModels(poGRider).SalesInsurance();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(transaction, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open Sales Insurance record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesInsurance.add(loDetail);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Sales_Registration
        lsSQL = "SELECT * FROM Sales_Registration"
                + " WHERE sTransNox = " + SQLUtil.toSQL(transaction);

        loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Registration loDetail = new SalesModels(poGRider).SalesRegistration();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(transaction, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open Sales Registration record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesRegistration.add(loDetail);
            }
        }
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Sales_Detail
        lsSQL = "SELECT * FROM Sales_Detail"
                + " WHERE sTransNox = " + SQLUtil.toSQL(transaction);

        loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Detail loDetail = new SalesModels(poGRider).SalesDetail();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(transaction, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open sales detail record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paDetail.add(loDetail);
            }
        }

        poEvent = new JSONObject();

        poEvent.put("event", "READY");

        pnEditMode = EditMode.READY;
        pbRecordExist = true;

        poJSON = new JSONObject();

        poJSON.put("result", "success");
        poJSON.put("result", "Transaction loaded successfully");
        return poJSON;
    }

    @Override
    public JSONObject newTransaction() throws CloneNotSupportedException {
        if (!pbInitTran) {
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        paAttachments = new ArrayList<>();
        poMaster.initialize();
        poMaster.newRecord();

        //clear Data
        paSalesGiveaways.clear();
        paSalesInsurance.clear();
        paSalesRegistration.clear();

        poJSON = initFields();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        getMaster().setIndustryCode(psIndustryCode);
        getMaster().setCategory(psCategorCD);

        pnEditMode = EditMode.ADDNEW;

        poEvent = new JSONObject();
        poEvent.put("event", "ADD NEW");

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject willSave() throws SQLException, GuanzonException {
        poJSON = new JSONObject();

        poJSON = isEntryOkay(SalesTransactionStatic.OPEN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        removeOther();

        if (paSalesGiveaways.size() > 0) {
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }
        if (paSalesInsurance.size() > 0) {

            isEntryOkay(1);
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }
        if (paSalesRegistration.size() > 0) {
            isEntryOkay(2);
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }

        //assign values needed
        poJSON.put("result", "success");
        return poJSON;

    }

    @Override
    public JSONObject saveOthers() {

        //assign other info on attachment
        for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
            TransactionAttachmentList(lnCtr).getModel().setSourceNo(getMaster().getTransactionNo());
            TransactionAttachmentList(lnCtr).getModel().setSourceCode(getSourceCode());
            TransactionAttachmentList(lnCtr).getModel().setBranchCode(getMaster().getBranchCode());
            TransactionAttachmentList(lnCtr).getModel().setImagePath(System.getProperty("sys.default.path.temp.attachments"));

            String lsOriginalFileName = TransactionAttachmentList(lnCtr).getModel().getFileName();
            //Check existing file name in database
            if (EditMode.ADDNEW == TransactionAttachmentList(lnCtr).getModel().getEditMode()) {
                int lnCopies = 0;
                String fsFilePath = TransactionAttachmentList(lnCtr).getModel().getImagePath() + "/" + TransactionAttachmentList(lnCtr).getModel().getFileName();
                String lsNewFileName = TransactionAttachmentList(lnCtr).getModel().getFileName();
                try {
                    while ("error".equals((String) checkExistingFileName(lsNewFileName).get("result"))) {
                        lnCopies++;
                        //Rename the file
                        int dotIndex = TransactionAttachmentList(lnCtr).getModel().getFileName().lastIndexOf(".");
                        if (dotIndex == -1) {
                            lsNewFileName = TransactionAttachmentList(lnCtr).getModel().getFileName() + "_" + lnCopies;
                        } else {
                            lsNewFileName = TransactionAttachmentList(lnCtr).getModel().getFileName().substring(0, dotIndex) + "_" + lnCopies + TransactionAttachmentList(lnCtr).getModel().getFileName().substring(dotIndex);
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (GuanzonException ex) {
                    ex.printStackTrace();
                }

                if (lnCopies > 0) {
                    Path source = Paths.get(fsFilePath);
                    try {
                        // Copy file into the target directory with a new name
                        Path target = Paths.get(System.getProperty("sys.default.path.temp.attachments")).resolve(lsNewFileName);
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        //check if file is existing
                        int lnChecker = 0;
                        File file = new File(TransactionAttachmentList(lnCtr).getModel().getImagePath() + "/" + lsNewFileName);
                        while (!file.exists() && lnChecker < 5) {
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Re-Copying... " + lnChecker);
                            lnChecker++;
                        }
                        TransactionAttachmentList(lnCtr).getModel().setFileName(lsNewFileName);
                        System.out.println("File copied successfully as " + lsNewFileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //Upload Attachment when send status is 0
            try {
                if ("0".equals(TransactionAttachmentList(lnCtr).getModel().getSendStatus())) {
                    poJSON = uploadCASAttachments(poGRider, System.getProperty("sys.default.access.token"), lnCtr, lsOriginalFileName);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                poJSON = setJSON("error", MiscUtil.getException(ex));
                return poJSON;
            }

        }
        try {
            System.out.println("--------------------------SAVE OTHERS---------------------------------------------");
            System.out.println("Class Edit Mode : " + getEditMode());
            System.out.println("Master Edit Mode : " + getMaster().getEditMode());
            System.out.println("-----------------------------------------------------------------------");
//            for(int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++){
//            System.out.println("COUNTER : " + lnCtr);
//            System.out.println("Transaction No : " + getDetail(lnCtr).getTransactionNo());
//            System.out.println("Stock ID : " + getDetail(lnCtr).getStockId());
            System.out.println("-----------------------------------------------------------------------");
//            }

            //Save Attachments
            System.out.println("-----------------------------SAVE TRANSACTION ATTACHMENT------------------------------------------");
            for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
                if (paAttachments.get(lnCtr).getEditMode() == EditMode.ADDNEW || paAttachments.get(lnCtr).getEditMode() == EditMode.UPDATE) {
                    paAttachments.get(lnCtr).getModel().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
                    paAttachments.get(lnCtr).getModel().setModifiedDate(poGRider.getServerDate());
                    paAttachments.get(lnCtr).setWithParentClass(true);
                    // ADD THIS: only default it if the UI hasn't already chosen one
//                    if (TransactionAttachmentList(lnCtr).getModel().getDocumentType() == null
//                            || TransactionAttachmentList(lnCtr).getModel().getDocumentType().isEmpty()) {
//                        TransactionAttachmentList(lnCtr).getModel().setDocumentType(<your default doc type code>);
//                    }
                    poJSON = paAttachments.get(lnCtr).saveRecord();
                    if (!isJSONSuccess(poJSON)) {
                        return poJSON;
                    }
                }
            }
            System.out.println("-----------------------------------------------------------------------");

        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON = setJSON("error", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON = setJSON("success", "success");
        return poJSON;
    }

    public void removeOther() {
        //Giveaways
        int lnDetailCount = 0;
        for (int lnCtr = paSalesGiveaways.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Giveaways loDetail = (Model_Sales_Giveaways) paSalesGiveaways.get(lnCtr);
            if (loDetail.getStockID() == null || loDetail.getStockID().isEmpty()) {
                paSalesGiveaways.remove(lnCtr);
                continue;
            }
        }
        for (int lnCtr = 0; lnCtr < paSalesGiveaways.size(); lnCtr++) {
            Model_Sales_Giveaways loDetail = (Model_Sales_Giveaways) paSalesGiveaways.get(lnCtr);
            lnDetailCount++;
            loDetail.setTransNo(getMaster().getTransactionNo());
            loDetail.setEntryNo(lnDetailCount);
        }

        //Insurance
        lnDetailCount = 0;
        for (int lnCtr = paSalesInsurance.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Insurance loDetail = (Model_Sales_Insurance) paSalesInsurance.get(lnCtr);
            if (loDetail.getInsuranceID() == null || loDetail.getInsuranceID().isEmpty()) {
                paSalesInsurance.remove(lnCtr);
                continue;
            }
        }

        for (int lnCtr = 0; lnCtr < paSalesInsurance.size(); lnCtr++) {
            Model_Sales_Insurance loDetail = (Model_Sales_Insurance) paSalesInsurance.get(lnCtr);
            lnDetailCount++;
            loDetail.setTransNo(getMaster().getTransactionNo());
            loDetail.setEntryNo(lnDetailCount);
        }

        //Registration
        lnDetailCount = 0;
        for (int lnCtr = paSalesRegistration.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Registration loDetail = (Model_Sales_Registration) paSalesRegistration.get(lnCtr);
            if (loDetail.getInsuranceTypeID() == null || loDetail.getInsuranceTypeID().isEmpty()) {
                paSalesRegistration.remove(lnCtr);
                continue;
            }
        }

        for (int lnCtr = 0; lnCtr < paSalesRegistration.size(); lnCtr++) {
            Model_Sales_Registration loDetail = (Model_Sales_Registration) paSalesRegistration.get(lnCtr);
            lnDetailCount++;
            loDetail.setTransNo(getMaster().getTransactionNo());
            loDetail.setEntryNo(lnDetailCount);
        }

        //Detail
        lnDetailCount = 0;
        for (int lnCtr = paDetail.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Detail loDetail = (Model_Sales_Detail) paDetail.get(lnCtr);
            if (loDetail.getStockID() == null || loDetail.getStockID().isEmpty()) {
                paDetail.remove(lnCtr);
                continue;
            }
        }
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Sales_Detail loDetail = (Model_Sales_Detail) paDetail.get(lnCtr);
            lnDetailCount++;
            loDetail.setTransNo(getMaster().getTransactionNo());
            loDetail.setEntryNo(lnDetailCount);
        }

    }

    @Override
    public JSONObject saveTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (!pbInitTran) {
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        if (pnEditMode == EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Saving of unmodified transaction is not allowed.");
            return poJSON;
        }
        poJSON = willSave();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (getEditMode() == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            pdModified = poGRider.getServerDate();
            poMaster.setValue("sModified", poGRider.Encrypt(poGRider.getUserID()));
        }

        poJSON = save();

        if (!pbWthParent) {
            poGRider.beginTrans((String) poEvent.get("event"),
                    poMaster.getTable(),
                    SOURCE_CODE,
                    String.valueOf(poMaster.getValue(1)));
        }

        if ("success".equals((String) poJSON.get("result"))) {
            //save master and detail
            if (pbVerifyEntryNo) {
                poMaster.setValue("nEntryNox", paDetail.size());
            }

            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                poMaster.setValue("dModified", pdModified);
                poJSON = poMaster.saveRecord();

                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                    return poJSON;
                }
                //Giveaways Save
                for (int lnCtr = 0; lnCtr <= paSalesGiveaways.size() - 1; lnCtr++) {
                    if (paSalesGiveaways.get(lnCtr).getValue("sTransNox") == null) {
                        continue;
                    }
                    paSalesGiveaways.get(lnCtr).setValue("sTransNox", String.valueOf(poMaster.getValue(1)));
                    paSalesGiveaways.get(lnCtr).setValue("dModified", pdModified);
                    poJSON = paSalesGiveaways.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        if ("message".contains("unmodified")) {
                            continue;
                        }
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }
                //Insurance Save
                for (int lnCtr = 0; lnCtr <= paSalesInsurance.size() - 1; lnCtr++) {
                    if (paSalesInsurance.get(lnCtr).getValue("sTransNox") == null) {
                        continue;
                    }
                    paSalesInsurance.get(lnCtr).setValue("sTransNox", String.valueOf(poMaster.getValue(1)));

                    poJSON = paSalesInsurance.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        if ("message".contains("unmodified")) {
                            continue;
                        }
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }

                //Registration Save
                for (int lnCtr = 0; lnCtr <= paSalesRegistration.size() - 1; lnCtr++) {
                    if (paSalesRegistration.get(lnCtr).getValue("sTransNox") == null) {
                        continue;
                    }
                    paSalesRegistration.get(lnCtr).setValue("sTransNox", String.valueOf(poMaster.getValue(1)));
                    poJSON = paSalesRegistration.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {

                        if ("message".contains("unmodified")) {
                            continue;
                        }
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }

                //Detail Save
                for (int lnCtr = 0; lnCtr <= paDetail.size() - 1; lnCtr++) {
                    if (paDetail.get(lnCtr).getValue("sTransNox") == null) {
                        continue;
                    }
                    paDetail.get(lnCtr).setValue("sTransNox", String.valueOf(poMaster.getValue(1)));
                    paDetail.get(lnCtr).setValue("dModified", pdModified);
                    poJSON = paDetail.get(lnCtr).saveRecord();

                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {

                        if ("message".contains("unmodified")) {
                            continue;
                        }
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }

            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Edit mode is not allowed to save transaction.");
                return poJSON;
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }

            return poJSON;
        }
        poJSON = saveOthers();
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return  poJSON;
        }

        if (!pbWthParent) {
            poGRider.commitTrans();
        }

        pnEditMode = EditMode.UNKNOWN;
        pbRecordExist = true;
//        paMaster = new ArrayList<Model>();
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction saved successfully.");
        openTransaction(getMaster().getTransactionNo());
        return loJSON;
    }

    public JSONObject UpdateTransaction() {
        poJSON = new JSONObject();
        if (SalesTransactionStatic.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }
        if (SalesTransactionStatic.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }
        if (SalesTransactionStatic.REPLACED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already replaced.");
            return poJSON;
        }
        if (SalesTransactionStatic.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }
        if (SalesTransactionStatic.PAID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        poJSON = updateTransaction();
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) throws SQLException, GuanzonException {
        GValidator loValidator = MCSalesValidatorFactory.make(getMaster().getIndustryCode());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);

        poJSON = loValidator.validate();
        if (poJSON.containsKey("isRequiredApproval") && Boolean.TRUE.equals(poJSON.get("isRequiredApproval"))) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    //check if approving officer is authorized
                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }

                    String lsUserIDxx = poJSON.get("sUserIDxx").toString();
                    int lnUserLevl = Integer.parseInt(poJSON.get("nUserLevl").toString());

                    setApproving(lsUserIDxx);
                    psApprovalUser = lsUserIDxx;
                }
            }
        }

        return poJSON;
    }

    protected JSONObject isEntryOkay(int otherEntry) {
        JSONObject loJSON;
        switch (otherEntry) {
            case 0://GiveAways
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            case 1://Insurance
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            case 2://Registration
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            default:
                loJSON = new JSONObject();
                loJSON.put("result", "error");
                loJSON.put("message", "Key Validator is not configure. Please Inform MIS SEG.");
                break;

        }

        return loJSON;
    }

    public JSONObject CloseTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (SalesTransactionStatic.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        if (SalesTransactionStatic.REPLACED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already replaced.");
            return poJSON;
        }

        if (SalesTransactionStatic.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesTransactionStatic.CONFIRMED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "",
                SalesTransactionStatic.CONFIRMED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction confirmed successfully.");

        openTransaction(getMaster().getTransactionNo());
        return loJSON;
    }

    public JSONObject CancelTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode");
            return poJSON;
        }

        if (SalesTransactionStatic.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesTransactionStatic.CANCELLED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "",
                SalesTransactionStatic.CANCELLED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction cancelled successfully.");

        openTransaction(getMaster().getTransactionNo());
        return loJSON;
    }

    public JSONObject VoidTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }
        if (SalesTransactionStatic.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesTransactionStatic.VOID);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "VoidTransaction",
                SalesTransactionStatic.VOID,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction voided successfully.");

        openTransaction(getMaster().getTransactionNo());
        return loJSON;
    }

    public JSONObject loadTransactionList(String value)
            throws SQLException, GuanzonException, CloneNotSupportedException {
//        poJSON = new JSONObject();

        paMaster.clear();
        String lsSQL = SQL_BROWSE;
        String lsCondition = "";
        if (psTranStat != null) {
            if (this.psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                    lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                }
                lsCondition = "cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "cTranStat = " + SQLUtil.toSQL(this.psTranStat);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if (!psIndustryCode.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
        }
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Sales_Master loSales = new SalesModels(poGRider).SalesMaster();

            poJSON = loSales.openRecord(loRS.getString("sTransNox"));

            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loSales);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put(
                "result", "success");
        return poJSON;
    }

    public JSONObject PostTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.UPDATE
                && getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }

        if (SalesTransactionStatic.PAID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesTransactionStatic.PAID);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "PostTransaction", SOURCE_CODE, getMaster().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sTransNox"),
                "PostTransaction",
                SalesTransactionStatic.PAID,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction posted successfully.");
        openTransaction(getMaster().getTransactionNo());
        return loJSON;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception {
        CachedRowSet crs = getStatusHistory();

        crs.beforeFirst();

        while (crs.next()) {
            switch (crs.getString("cRefrStat")) {
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case SalesTransactionStatic.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case SalesTransactionStatic.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case SalesTransactionStatic.PAID:
                    crs.updateString("cRefrStat", "PAID");
                    break;
                case SalesTransactionStatic.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case SalesTransactionStatic.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;

                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat) {
                        case SalesTransactionStatic.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case SalesTransactionStatic.CONFIRMED:
                            crs.updateString("cRefrStat", "CONFIRMED");
                            break;
                        case SalesTransactionStatic.PAID:
                            crs.updateString("cRefrStat", "PAID");
                            break;
                        case SalesTransactionStatic.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case SalesTransactionStatic.VOID:
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

        showStatusHistoryUI("Sales_Master", (String) poMaster.getValue("sTransNox"), entryBy, entryDate, crs);
    }

    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM Sales_Promotion_Master a "
                + " LEFT JOIN xxxAuditLogMaster b ON"
                + " b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(getMaster().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(getMaster().getTransactionNo()));
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
                        // Get the LocalDateTime from your result set
                        LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class
                        );
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

    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL = " SELECT IFNULL(b.sCompnyNm,'') sCompnyNm FROM xxxSysUser a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId));
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    lsEntry = loRS.getString("sCompnyNm");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return lsEntry;
    }

    //Searching of Master
    public JSONObject searchClient(String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException, Exception {
        JSONObject loSourceTransaction = new JSONObject();
        poJSON = new JSONObject();
        JSONObject loJSON = new JSONObject();
        //initialize Client GUI
        ClientGUI loClient = new ClientGUI();
        loClient.setGRider(poGRider);
        loClient.setLogWrapper(null);

        loClient.setClientType(getClientType());

        String lsSQL = getSalesClientBrowse(poGRider.getIndustry());
        lsSQL = lsSQL + " WHERE cClientTp = " + SQLUtil.toSQL(getClientType());
        System.out.println("SQL Client: " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Reservation»Quotation»Credit»Employee»Name»Birth Date»Address»Client ID",
                "xHasReserv»xHasQuotat»xHasComitment»xIsEmploye»xCompnyNm»dBirthDte»sAddressx»sClientID",
                "xHasReserv»xHasQuotat»xHasComitment»xIsEmploye»xCompnyNm»dBirthDte»sAddressx»sClientID",
                byCode ? 4 : 7);

        if (poJSON == null) {
            return setJSON("error", "No record loaded.");
        }

        // a selected row has no "result" key, it only carries the column values
        boolean lbHasClient = poJSON.get("sClientID") != null
                && !poJSON.get("sClientID").toString().isEmpty();

        if (!lbHasClient) {
            // nothing selected / no match -> add a new client
            if ("error".equals((String) poJSON.get("result"))
                    && poJSON.get("message") != null
                    && !"No record to load.".equals(poJSON.get("message"))) {
                return poJSON; // real error, not just "no match"
            }
            return addClient();
        }

        // ----- existing client -----
        String lsClientID = poJSON.get("sClientID").toString();
        boolean lbHasReserv = "YES".equals(String.valueOf(poJSON.get("xHasReserv")));
        boolean lbHasQuotat = "YES".equals(String.valueOf(poJSON.get("xHasQuotat")));
        boolean lbHasCommit = "YES".equals(String.valueOf(poJSON.get("xHasComitment")));
        boolean lbIsEmploye = "YES".equals(String.valueOf(poJSON.get("xIsEmploye")));

        loClient.setClientId(lsClientID);
        CommonUtils.showModal(loClient);   // show / edit the client record

        if (loClient.isCancelled()) {
            return setJSON("error", "No record loaded.");
        }

        // ----- possible source transaction -----
        if (lbHasReserv || lbHasQuotat || lbHasCommit) {
            lsSQL = getSalesClientSource(poGRider.getIndustry());
            lsSQL = lsSQL + " WHERE sClientID = " + SQLUtil.toSQL(lsClientID);
            System.out.println("SQL Client Source: " + lsSQL);

            ResultSet loRS = poGRider.executeQuery(lsSQL);
//
            if (MiscUtil.RecordCount(loRS) > 0L) {
                loSourceTransaction = ShowDialogFX.Browse(poGRider,
                        lsSQL,
                        lsClientID,
                        "Source»Source No»Date»Name»Client ID",
                        "sSource»sTransNox»dTransact»xClientNm»sClientID",
                        "sSource»sTransNox»dTransact»xClientNm»sClientID",
                        4);

                if (loSourceTransaction != null && loSourceTransaction.get("sSource") != null) {
                    JSONObject loValidate = new JSONObject();
                    switch (loSourceTransaction.get("sSource").toString()) {
                        case "Sales Reservation":
                            loValidate = validateReservation(loSourceTransaction.get("sTransNox").toString());
                            break;
                        case "Sales Quotation":
                            loValidate = validateQuotation(loSourceTransaction.get("sTransNox").toString());
                            break;
                        default:
                            loValidate = setJSON("error", "Sales Commitment is not yet supported.");
                    }
                    if (!"success".equals((String) loValidate.get("result"))) {
                        return setJSON("error", "Unable to load Source Sales.\n\t "
                                + loValidate.get("message"));
                    }
                }
            }
        }
        // business rule for employee here
        if (lbIsEmploye) {
        }

        // ----- set master (use the selected id, not the GUI's) -----
        getMaster().setClientID(lsClientID);
        getMaster().setCompanyName(poJSON.get("xCompnyNm") == null ? "" : poJSON.get("xCompnyNm").toString());
        getMaster().setAddressID(loClient.getClient().Address(0).getAddressId());
        getMaster().setAddress(loClient.getClient().Address(0).getAddress());
        getMaster().setContactID(loClient.getClient().Mobile(0).getMobileId());
        getMaster().setMobileNo(loClient.getClient().Mobile(0).getMobileNo());

        return setJSON("success", "success");
    }

    public JSONObject addClient() throws SQLException, GuanzonException, Exception {
        //initialize new json for result
        JSONObject loResult = new JSONObject();

        String lsClientId = getMaster().getClientID();
        lsClientId = (lsClientId == null || lsClientId.isEmpty()) ? "" : lsClientId;

        //initialize Client GUI
        ClientGUI loClient = new ClientGUI();

        loClient.setGRider(poGRider);
        loClient.setLogWrapper(null);

        loClient.setClientType(getClientType());
        loClient.setByCode(false);
        loClient.setClientId(lsClientId);

        //load record
        CommonUtils.showModal(loClient);

        //load if button
        if (!loClient.isCancelled()) {
            lsClientId = loClient.getClient().getModel().getClientId();

            getMaster().setClientID(loClient.getClientId());
            getMaster().setAddressID(loClient.getClient().Address(0).getAddressId());
            getMaster().setContactID(loClient.getClient().Mobile(0).getMobileId());

            getMaster().setCompanyName(loClient.getClient().getModel().getCompanyName());
            getMaster().setAddress(loClient.getClient().Address(0).getAddress());
            getMaster().setMobileNo(loClient.getClient().Mobile(0).getMobileNo());

        }
        loResult.put("result", "success");
        return loResult;
    }

    //Validate Source's
    public JSONObject validateReservation(String sourceNo) throws SQLException, GuanzonException, CloneNotSupportedException {

        JSONObject loValidateSource = new JSONObject();
        SalesReservation loSalesSource = new SalesControllers(poGRider, null).SalesReservation();
        loSalesSource.setTransactionStatus(Sales_Reservation_Static.CONFIRMED);

        loSalesSource.setIndustryID(psIndustryCode);
        loSalesSource.setCompanyID(psCompanyID);
        loSalesSource.setCategoryCd(psCategorCD);
        loSalesSource.setBranchCode(psBranchCode);

        loValidateSource = loSalesSource.OpenTransaction(sourceNo);
        if ("success".equals((String) loValidateSource.get("result"))) {

            getMaster().setClientID(loSalesSource.Master().getClientID());
            getMaster().setAddress(loSalesSource.Master().getAddressID());
            getMaster().setMobileNo(loSalesSource.Master().getContactID());
            getMaster().setAdvances(loSalesSource.Master().getAmountPaid());
            getMaster().setSalesman(loSalesSource.Master().getEntryBy());
            getMaster().setSourceNo(loSalesSource.Master().getTransactionNo());
            getMaster().setSourceCode(loSalesSource.getSourceCode());

            //clear existing detail
            paDetail.clear();
            paDetail = new ArrayList<Model>();
            int lnRowSale = 0;
            for (int lnCtr = 0; lnCtr < loSalesSource.getDetailCount(); lnCtr++) {

                if (!loSalesSource.Detail(lnCtr).isReversed()) {
                    continue;
                }

                //Entry No Sales
                lnRowSale = lnRowSale++;
                //initialize detail to set
                getDetail(lnRowSale);
                //Sync Detail to Sales Source's
                getDetail(lnRowSale).setStockID(loSalesSource.Detail(lnCtr).getStockID());
                getDetail(lnRowSale).setQuantity(Integer.parseInt(String.valueOf(loSalesSource.Detail(lnCtr).getApproved())));
                getDetail(lnRowSale).setUnitCost(Double.parseDouble(loSalesSource.Detail(lnCtr).Inventory().getCost().toString()));
                getDetail(lnRowSale).setUnitPrice(loSalesSource.Detail(lnCtr).getUnitPrice());

            }

        } else {
            loValidateSource.put("result", "error");
            loValidateSource.put("message", "Unable to Detect Entry No");
            return loValidateSource;
        }
        loValidateSource = new JSONObject();
        loValidateSource.put("result", "success");
        return loValidateSource;
    }

    public JSONObject validateQuotation(String sourceNo) throws SQLException, GuanzonException, CloneNotSupportedException {

        JSONObject loValidateSource = new JSONObject();
        SalesQoutation loSalesSource = new SalesControllers(poGRider, null).SalesQoutation();
        loSalesSource.setRecordStatus(Sales_Reservation_Static.CONFIRMED);

        loValidateSource = loSalesSource.openRecord(sourceNo);

        if ("success".equals((String) loValidateSource.get("result"))) {

            getMaster().setClientID(loSalesSource.getModel().getClientId());
            getMaster().setAddress(loSalesSource.getModel().getAddressId());
            getMaster().setMobileNo(loSalesSource.getModel().getContactId());
            if (loSalesSource.Version().Master().getValidThruDate() != null && !loSalesSource.Version().Master().getValidThruDate().before(getMaster().getTransactionDate())) {

                getMaster().setPaymentForm(loSalesSource.Version().Master().getPaymentForm());
                getMaster().setTermCode(loSalesSource.Version().Master().getTermId());
                getMaster().setTransactionTotal(loSalesSource.Version().Master().getTransactionTotal());
                getMaster().setDiscount(loSalesSource.Version().Master().getDiscountAmount());
                getMaster().setAddDiscount(loSalesSource.Version().Master().getAdditionalDiscount());
                getMaster().setFreight(loSalesSource.Version().Master().getFreight());
                getMaster().setVATSales(loSalesSource.Version().Master().getVatSales());
                getMaster().setFreight(loSalesSource.Version().Master().getFreight());
                getMaster().setVATAmount(loSalesSource.Version().Master().getVatAmount());
                getMaster().setNonVATSales(loSalesSource.Version().Master().getNonVatSales());
                getMaster().setSourceCode(loSalesSource.Version().getSourceCode());
                getMaster().setSourceNo(loSalesSource.Version().Master().getParentId());

                //clear existing detail
                paDetail.clear();
                paDetail = new ArrayList<Model>();
                int lnRowSale = 0;
                for (int lnCtr = 0; lnCtr < loSalesSource.Version().getDetailCount(); lnCtr++) {

                    if ((loSalesSource.Version().Detail(lnCtr).getStockId()) == null || (loSalesSource.Version().Detail(lnCtr).getStockId().isEmpty())) {
                        continue;
                    }

                    //Entry No Sales
                    lnRowSale = lnRowSale++;
                    //initialize detail to set
                    getDetail(lnRowSale);
                    //Sync Detail to Sales Source's
                    getDetail(lnRowSale).setStockID(loSalesSource.Version().Detail(lnCtr).getStockId());
                    getDetail(lnRowSale).setQuantity(loSalesSource.Version().Detail(lnCtr).getQuantity());
                    getDetail(lnRowSale).setUnitCost(Double.parseDouble(loSalesSource.Version().Detail(lnCtr).Inventory().getCost().toString()));
                    getDetail(lnRowSale).setUnitPrice(loSalesSource.Version().Detail(lnCtr).getUnitPrice());
                    getDetail(lnRowSale).setPromoID(loSalesSource.Version().Detail(lnCtr).getPromoCode());
                    getDetail(lnRowSale).setDiscount(loSalesSource.Version().Detail(lnCtr).getDiscount());
                    getDetail(lnRowSale).setAddDiscount(loSalesSource.Version().Detail(lnCtr).getAdditionalDiscount());
                    getDetail(lnRowSale).setFreight(loSalesSource.Version().Detail(lnCtr).getFreight());

                }
                lnRowSale = 0;
                for (int lnCtr = 0; lnCtr < loSalesSource.Giveaways().getGiveawayCount(); lnCtr++) {

                    if ((loSalesSource.Giveaways().Giveaway(lnCtr).getStockId()) == null
                            || (loSalesSource.Giveaways().Giveaway(lnCtr).getStockId().isEmpty())) {
                        continue;
                    }

                    //Entry No Sales
                    lnRowSale = lnRowSale++;
                    //initialize detail to set
                    getDetail(lnRowSale);
                    //Sync Detail to Sales Source's
                    getSalesGiveaways(lnRowSale).setStockID(loSalesSource.Giveaways().Giveaway(lnCtr).getStockId());
                    getSalesGiveaways(lnRowSale).setQuantity(loSalesSource.Giveaways().Giveaway(lnCtr).getQuantity());
                    getSalesGiveaways(lnRowSale).setUnitCost(Double.parseDouble(loSalesSource.Giveaways().Giveaway(lnCtr).Inventory().getCost().toString()));
                    getSalesGiveaways(lnRowSale).setUnitPrice(Double.parseDouble(loSalesSource.Giveaways().Giveaway(lnCtr).Inventory().getSellingPrice().toString()));
                    getSalesGiveaways(lnRowSale).setQuantity(loSalesSource.Giveaways().Giveaway(lnCtr).getQuantity());
                    getSalesGiveaways(lnRowSale).setSourceNo(loSalesSource.Giveaways().Giveaway(lnCtr).getTransactionNo());
                    getSalesGiveaways(lnRowSale).setSourceCode(loSalesSource.Version().getSourceCode());

                }
            }
        } else {
            loValidateSource.put("result", "error");
            loValidateSource.put("message", "Unable to Detect Entry No");
            return loValidateSource;
        }
        loValidateSource = new JSONObject();

        loValidateSource.put(
                "result", "success");
        if (loSalesSource.Version()
                .Master().getValidThruDate() != null && loSalesSource.Version().Master().getValidThruDate().before(getMaster().getTransactionDate())) {
            loValidateSource.put("message", "Quotation is already Expired");
        }
        return loValidateSource;
    }

//Searching of Master Co Client
    public JSONObject searchCoClient(String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException, Exception {
        poJSON = new JSONObject();
        JSONObject loJSON = new JSONObject();
        //initialize Client GUI
        ClientGUI loClient = new ClientGUI();

        loClient.setGRider(poGRider);
        loClient.setLogWrapper(null);

        loClient.setClientType(getClientType());

        poJSON = loClient.searchRecord(value, byCode);
        if (poJSON != null) {
            if ("success".equals((String) poJSON.get("result"))) {
                loClient.setClientId((String) poJSON.get("clientId"));
                //Load Client of Existing 

                //load record
                CommonUtils.showModal(loClient);
                loJSON.put("result", "success");
            } else {
                loJSON = addCoClient();
                return loJSON;
            }
            if ("success".equals((String) loJSON.get("result"))) {

                getMaster().setCoClientID(loClient.getClientId());
                getMaster().setAddressID2(loClient.getClient().Address(0).getAddressId());

                poJSON = new JSONObject();
                poJSON.put("result", "success");
                return poJSON;
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject addCoClient() throws SQLException, GuanzonException, Exception {
        //initialize new json for result
        JSONObject loResult = new JSONObject();

        String lsClientId = getMaster().getClientID();
        lsClientId = (lsClientId == null || lsClientId.isEmpty()) ? "" : lsClientId;

        //initialize Client GUI
        ClientGUI loClient = new ClientGUI();

        loClient.setGRider(poGRider);
        loClient.setLogWrapper(null);

        loClient.setClientType(getClientType());
        loClient.setByCode(false);
        loClient.setClientId(lsClientId);

        //load record
        CommonUtils.showModal(loClient);

        //load if button
        if (!loClient.isCancelled()) {
            lsClientId = loClient.getClient().getModel().getClientId();
            getMaster().setCoClientID(loClient.getClientId());
            getMaster().setAddressID2(loClient.getClient().Address(0).getAddressId());

        }
        loResult.put("result", "success");
        return loResult;
    }

    //Searching of Master salesman/referral
    public JSONObject searchSalesMan(String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException, Exception {
        poJSON = new JSONObject();

        ClientInfo loClient = new ClientControllers(poGRider, logwrapr).ClientInfo();
        loClient.initialize();
        loClient.setWithParentClass(true);
        loClient.setWithUI(false);

        poJSON = loClient.searchRecord(value, byCode);

        if (poJSON != null) {
            if ("success".equals((String) poJSON.get("result"))) {

                getMaster().setSalesman(loClient.getModel().getClientId());

                poJSON = new JSONObject();
                poJSON.put("result", "success");
                return poJSON;
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    //Searching of Detail Promotion
    public JSONObject searchSalesDetailPromotion(int fnRow, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException, Exception {
        poJSON = new JSONObject();

        JSONObject loResult = new JSONObject();

        //validate every required before selection
        if (getDetail(fnRow).getSerialID().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Please select a Inventory Serial First.");
            return poJSON;
        }
        if (getDetail(fnRow).getStockID().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Please select a Inventory First.");
            return poJSON;
        }
        if (getDetail(fnRow).Inventory().getModelId() == null && getDetail(fnRow).Inventory().getModelId().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Model to proceed with Promo Selection");
            return poJSON;
        }

        String lsSQL = getSalesPromotion(
                getDetail(fnRow).Inventory().getModelId(),
                getDetail(fnRow).Inventory().getBrandId());
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "sPromIDxx»Description»Date»Promo Start»Promo End",
                "a.sPromIDxx»a.sPromDesc»a.dTransact»a.dFromDate»a.dThruDate",
                "a.sPromIDxx»a.sPromDesc»a.dTransact»a.dFromDate»a.dThruDate",
                byCode ? 0 : 1);
        if (poJSON != null) {
            getDetail(fnRow).setPromoID((String) poJSON.get("sPromIDxx"));
            //load Promotion Data

            MCPromoSales loObject = new PromoControllers(poGRider, null).MCPromoSales();
            loObject.initTransaction();
            loObject.setTransactionStatus("1");
            loObject.setIndustryID(psIndustryCode);
            loObject.setCompanyID(psCompanyID);
            loObject.setCategoryID(psCategorCD);

            loResult = loObject.OpenTransaction((String) poJSON.get("sPromIDxx"));
            if ("success".equals((String) loResult.get("result"))) {
                //set Promo object for valadtion usage;
                poPromo = loObject;
                if (loObject.getCountSalesPromotionGiveAwayItem() > 0) {
                    int lnRowSale = 0;
                    for (int lnCtr = 1; lnCtr <= loObject.getCountSalesPromotionGiveAwayItem(); lnCtr++) {
                        if (!loObject.getSalesPromotionGiveAway(lnCtr).isWithActive()) {
                            continue;
                        }
                        //Entry No Sales
                        lnRowSale = lnRowSale++;
                        //initialize detail to set
                        getSalesGiveaways(lnRowSale);
                        //Sync Detail to Sales Promo's
                        getSalesGiveaways(lnRowSale).setStockID(loObject.getSalesPromotionGiveAway(lnCtr).getStockID());
                        getSalesGiveaways(lnRowSale).setQuantity(loObject.getSalesPromotionGiveAway(lnCtr).getQuantity());
                        getSalesGiveaways(lnRowSale).setUnitCost(Double.parseDouble(loObject.getSalesPromotionGiveAway(lnCtr).Inventory().getCost().toString()));
                        getSalesGiveaways(lnRowSale).setUnitPrice(Double.parseDouble(loObject.getSalesPromotionGiveAway(lnCtr).Inventory().getSellingPrice().toString()));
                        getSalesGiveaways(lnRowSale).setQuantity(loObject.getSalesPromotionGiveAway(lnCtr).getQuantity());
                        getSalesGiveaways(lnRowSale).setSourceNo(loObject.getMaster().getPromoID());
                        getSalesGiveaways(lnRowSale).setSourceCode(loObject.getSourceCode());

                    }

                }

                int lnRowSale = 0;
                int lnRowSaleR = 0;
                if (loObject.getCountSalesPromotionBrand() > 0) {
                    for (int lnCtr = 1; lnCtr <= loObject.getCountSalesPromotionBrand(); lnCtr++) {
                        if (loObject.getSalesPromotionBrand(lnCtr).getBrandID().equals(getDetail(fnRow).Inventory().getBrandId())) {

                            //Entry No Sales
                            lnRowSale = getCountSalesInsurance() + 1;
                            lnRowSaleR = getCountSalesRegistration() + 1;

                            //initialize detail to set
                            getSalesInsurance(lnRowSale);

                            //Sync Detail to Sales Insurance's
                            getSalesInsurance(lnRowSale).setSerialID(getDetail(fnRow).getSerialID());
                            getSalesInsurance(lnRowSale).isChargeTo(!loObject.getSalesPromotionBrand(lnCtr).isInsuranceFree());

                            //Sync Detail to Sales Registration's
                            getSalesRegistration(lnRowSaleR).setSerialID(getDetail(fnRow).getSerialID());
                            getSalesRegistration(lnRowSaleR).isChargeTo(!loObject.getSalesPromotionBrand(lnCtr).isRegistrationFree());

                            //Sync Detail to Sales Detail's
                            getDetail(fnRow).setDiscount(loObject.getSalesPromotionBrand(lnCtr).getDiscountRate());
                            getDetail(fnRow).setAddDiscount(loObject.getSalesPromotionBrand(lnCtr).getDiscAmount());
                            getDetail(fnRow).setFreight(loObject.getSalesPromotionBrand(lnCtr).getFreight());
                            break;
                        }
                    }
                }
                if (loObject.getCountSalesPromotionModel() > 0) {
                    for (int lnCtr = 1; lnCtr <= loObject.getCountSalesPromotionModel(); lnCtr++) {
                        if (loObject.getSalesPromotionModel(lnCtr).getModelID().equals(getDetail(fnRow).Inventory().getModelId())) {

                            //Entry No Sales
                            lnRowSale = getCountSalesRegistration() + 1;
                            //initialize detail to set
                            getSalesRegistration(lnRowSale);

                            //Sync Detail to Sales Insurance's
                            getSalesInsurance(lnRowSale).setSerialID(getDetail(fnRow).getSerialID());
                            getSalesInsurance(lnRowSale).isChargeTo(!loObject.getSalesPromotionModel(lnCtr).isInsuranceFree());

                            //Sync Detail to Sales Registration's
                            getSalesRegistration(lnRowSaleR).setSerialID(getDetail(fnRow).getSerialID());
                            getSalesRegistration(lnRowSaleR).isChargeTo(!loObject.getSalesPromotionModel(lnCtr).isRegistrationFree());

                            //Sync Detail to Sales Detail's
                            getDetail(fnRow).setDiscount(loObject.getSalesPromotionModel(lnCtr).getDiscountRate());
                            getDetail(fnRow).setAddDiscount(loObject.getSalesPromotionModel(lnCtr).getDiscAmount());
                            getDetail(fnRow).setFreight(loObject.getSalesPromotionModel(lnCtr).getFreight());
                            break;
                        }
                    }
                }

            }

        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Renumbers detail rows 1..n in list order.
     */
    private void realignDetailEntryNo() {
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Sales_Detail loRow = (Model_Sales_Detail) paDetail.get(lnCtr);
            loRow.setTransNo(getMaster().getTransactionNo());
            loRow.setEntryNo(lnCtr + 1);
        }
    }

    public JSONObject searchDetailBySerial(int row, String value, int fnColumn)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        // entry numbers are 1-based and must be contiguous
        if (row < 1) {
            row = 1;
        }
        if (row > paDetail.size() + 1) {
            row = paDetail.size() + 1;
        }

        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
        loBrowse.initTransaction();
        if (!psIndustryCode.isEmpty()) {
            loBrowse.setIndustry(psIndustryCode);
        }
        loBrowse.setCategoryFilters(psCategorCD);
        loBrowse.setBranch(poGRider.getBranchCode());

        //allow negative quantity NEW BR 07-2026
        loBrowse.isWithQuantityStock(false);

        loBrowse.setCustomColHeader("Serial 01»Serial 02»Model»Color/Varian»Description»ID");
        loBrowse.setCustomColName("sSerial01»sSerial02»xModelNme»xColorNme»b.sDescript»sSerialID");
        loBrowse.setCustomColCriteria("a.sSerial01»a.sSerial02»IFNULL (d.sDescript, '')»IFNULL (e.sDescript, '')»b.sDescript»a.sSerialID");

        poJSON = loBrowse.searchInventorySerialWithStock(value, fnColumn);

        // nothing selected or error: do not touch the row
        if (poJSON == null) {
            return setJSON("error", "No record loaded.");
        }
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        String lsStockID = loBrowse.getModelInventory().getStockId();
        String lsSerialID = loBrowse.getModelInventorySerial().getSerialId(); // may be null (non-serialized)
        boolean lbWithSerial = lsSerialID != null && !lsSerialID.isEmpty();

        // ---------- duplicate check (ignore the row being edited) ----------
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Sales_Detail loExisting = (Model_Sales_Detail) paDetail.get(lnCtr);
            if (loExisting.getEntryNo() == row || loExisting.getStockID() == null
                    || !loExisting.getStockID().equals(lsStockID)) {
                continue;
            }
            boolean lbExistingSerial = loExisting.getSerialID() != null && !loExisting.getSerialID().isEmpty();

            if (lbWithSerial && lbExistingSerial && loExisting.getSerialID().equals(lsSerialID)) {
                return setJSON("error", "Selected Serial is already exist!");
            }
            if (!lbWithSerial && !lbExistingSerial && !hasSource()) {
                return setJSON("error", "Selected Inventory is already exist!");
            }
        }

        Model_Sales_Detail loDetail = getDetail(row);   // entry no = row (1-based)
        double lnUnitCost = Double.parseDouble(loBrowse.getModelInventory().getCost().toString());
        int lnRemaining = 0;

        // ---------- source handling ----------
        if (hasSource()) {
            Model_Sales_Detail loSourceRow = null;

            boolean lbRowIsSource = loDetail.getStockID() != null
                    && loDetail.getStockID().equals(lsStockID);

            if (lbRowIsSource) {
                loSourceRow = loDetail;
            } else {
                for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
                    Model_Sales_Detail loTmp = (Model_Sales_Detail) paDetail.get(lnCtr);
                    if (loTmp != loDetail
                            && lsStockID.equals(loTmp.getStockID())
                            && (loTmp.getSerialID() == null || loTmp.getSerialID().isEmpty())
                            && loTmp.getQuantity() > 0) {
                        loSourceRow = loTmp;
                        break;
                    }
                }
            }

            if (loSourceRow != null) {
                lnRemaining = loSourceRow.getQuantity() - 1; // this serial takes 1

                lnUnitCost = loSourceRow.getUnitCost();
                loDetail.setUnitPrice(loSourceRow.getUnitPrice());
                loDetail.setPromoID(loSourceRow.getPromoID());
                loDetail.setDiscount(loSourceRow.getDiscount());
                loDetail.setAddDiscount(loSourceRow.getAddDiscount());
                loDetail.setFreight(loSourceRow.getFreight());

                if (loSourceRow != loDetail) {
                    if (lnRemaining > 0) {
                        loSourceRow.setQuantity(lnRemaining);
                    } else {
                        paDetail.remove(loSourceRow);
                    }
                    lnRemaining = 0; // remainder stays in the other source row
                }
            }
        }

        // ---------- fill the selected row (one serial = qty 1) ----------
        loDetail.setStockID(lsStockID);
        if (lbWithSerial) {
            loDetail.setSerialID(lsSerialID);
        }
        loDetail.setUnitCost(lnUnitCost);
        loDetail.setQuantity(1);

        // ---------- keep entry numbers 1..n with no gaps (a source row may have been removed) ----------
        realignDetailEntryNo();

        // ---------- push remaining qty to a new row at the end ----------
        if (lnRemaining > 0) {
            Model_Sales_Detail loNew = getDetail(paDetail.size() + 1); // creates + adds row, entry no = size + 1
            loNew.setStockID(lsStockID);
            loNew.setQuantity(lnRemaining);
            loNew.setUnitCost(lnUnitCost);
            loNew.setUnitPrice(loDetail.getUnitPrice());
            loNew.setPromoID(loDetail.getPromoID());
            loNew.setDiscount(loDetail.getDiscount());
            loNew.setAddDiscount(loDetail.getAddDiscount());
            loNew.setFreight(loDetail.getFreight());
        }

        return setJSON("success", "success");
    }

    private boolean hasSource() {
        return getMaster().getSourceNo() != null && !getMaster().getSourceNo().isEmpty();
    }

    public JSONObject searchSalesGiveAway(int fsRow, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
        loBrowse.initTransaction();
        if (!psIndustryCode.isEmpty()) {
            loBrowse.setIndustry(psIndustryCode);
        }
        loBrowse.setCategoryFilters(psCategorCD);
        loBrowse.setBranch(poGRider.getBranchCode());
        //allow negative quantity NEW BR 07-2026
        loBrowse.isWithQuantityStock(false);

        poJSON = new JSONObject();

        poJSON = loBrowse.searchInventory(value, byCode);
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paSalesGiveaways.size() - 1; lnExisting++) {
                Model_Sales_Giveaways loExisting = (Model_Sales_Giveaways) paSalesGiveaways.get(lnExisting);
                if (loExisting.getStockID() != null) {
                    if (loExisting.getStockID().equals(loBrowse.getModelInventory().getStockId())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Barcode is already exist!");
                        return poJSON;

                    }
                }
            }

            getSalesGiveaways(fsRow).setStockID(
                    loBrowse.getModelInventory().getStockId());

            poJSON = new JSONObject();
            poJSON.put("result", "success");
        }
        return poJSON;
    }

    public JSONObject removeSalesGiveAway(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesGiveaways.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        paSalesGiveaways.remove(entryNo - 1);

        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesGiveaways.size(); lnCtr++) {
            Model_Sales_Giveaways loDetail = (Model_Sales_Giveaways) paSalesGiveaways.get(lnCtr);
            lnDetailCount++;
            loDetail.setTransNo(getMaster().getTransactionNo());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }

    public JSONObject searchInsuranceTypeInsurance(int fnRowDetail, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        JSONObject loJSON = new JSONObject();
        String lsSerialID = getDetail(fnRowDetail).getSerialID();
        if (lsSerialID == null || lsSerialID.isEmpty()) {
            loJSON.put("result", "error");
            loJSON.put("message", "Unable to load Insurance Type. Please select a Serial.");
            return loJSON;
        }

        InsuranceType loBrowse = new ParamControllers(poGRider, logwrapr).InsuranceType();
        loBrowse.initialize();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        loBrowse.setWithParentClass(true);
        loBrowse.setInsuranceOnly(true);
        loJSON = loBrowse.searchRecord(value, byCode);
        if (!"success".equals((String) loJSON.get("result"))) {
            return loJSON;
        }

        String lsTypeID = loBrowse.getModel().getInsuranceTypeID();

        for (int lnExisting = 0; lnExisting < paSalesInsurance.size(); lnExisting++) {
            Model_Sales_Insurance loExisting = (Model_Sales_Insurance) paSalesInsurance.get(lnExisting);
            if (lsSerialID.equals(loExisting.getSerialID())
                    && lsTypeID.equals(loExisting.getInsuranceTypeID())) {
                loJSON = new JSONObject();
                loJSON.put("result", "error");
                loJSON.put("message", "Selected Insurance Type already exists for this Serial.");
                return loJSON;
            }
        }

        // add a new insurance row for this serial
        Model_Sales_Insurance loNew = getSalesInsurance(getCountSalesInsurance() + 1); // adjust to your index base
        loNew.setInsuranceTypeID(lsTypeID);
        loNew.setSerialID(lsSerialID);

        loJSON = new JSONObject();
        loJSON.put("result", "success");
        return loJSON;
    }

    public JSONObject removeSalesInsurance(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesInsurance.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        paSalesInsurance.remove(entryNo - 1);

        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesInsurance.size(); lnCtr++) {
            Model_Sales_Insurance loDetail = (Model_Sales_Insurance) paSalesInsurance.get(lnCtr);
            lnDetailCount++;
            loDetail.setTransNo(getMaster().getTransactionNo());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }

    //Searching of Master salesman/referral
    public JSONObject searchInsuranceTypePayee(int fnrow, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException, Exception {
        poJSON = new JSONObject();

        ClientInfo loClient = new ClientControllers(poGRider, logwrapr).ClientInfo();
        loClient.initialize();
        loClient.setWithParentClass(true);
        loClient.setWithUI(false);

        poJSON = loClient.searchRecord(value, byCode);

        if (poJSON != null) {
            if ("success".equals((String) poJSON.get("result"))) {
                getSalesInsurance(fnrow).setInsuranceClientID(loClient.getModel().getClientId());
                poJSON = new JSONObject();
                poJSON.put("result", "success");
                return poJSON;
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject searchInsuranceTypeRegistration(int fnRowDetail, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        JSONObject loJSON = new JSONObject();
        String lsSerialID = getDetail(fnRowDetail).getSerialID();
        if (lsSerialID == null || lsSerialID.isEmpty()) {
            loJSON.put("result", "error");
            loJSON.put("message", "Unable to load Insurance Type. Please select a Serial.");
            return loJSON;
        }

        InsuranceType loBrowse = new ParamControllers(poGRider, logwrapr).InsuranceType();
        loBrowse.initialize();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        loBrowse.setWithParentClass(true);
        loJSON = loBrowse.searchRecord(value, byCode);
        if (!"success".equals((String) loJSON.get("result"))) {
            return loJSON;
        }

        String lsTypeID = loBrowse.getModel().getInsuranceTypeID();

        for (int lnExisting = 0; lnExisting < paSalesRegistration.size(); lnExisting++) {
            Model_Sales_Registration loExisting
                    = (Model_Sales_Registration) paSalesRegistration.get(lnExisting);
            if (lsSerialID.equals(loExisting.getSerialID())
                    && lsTypeID.equals(loExisting.getInsuranceTypeID())) {
                loJSON = new JSONObject();
                loJSON.put("result", "error");
                loJSON.put("message", "Selected Insurance Type already exists for this Serial.");
                return loJSON;
            }
        }

        // add a new registration row for this serial
        Model_Sales_Registration loNew
                = getSalesRegistration(getCountSalesRegistration() + 1);
        loNew.setInsuranceTypeID(lsTypeID);
        loNew.setRegistrationType(loBrowse.getModel().getRegistrationType());
        loNew.setSerialID(lsSerialID);

        loJSON = new JSONObject();
        loJSON.put("result", "success");
        return loJSON;
    }

    public JSONObject removeSalesRegistration(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesRegistration.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        paSalesRegistration.remove(entryNo - 1);

        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesRegistration.size(); lnCtr++) {
            Model_Sales_Registration loDetail = (Model_Sales_Registration) paSalesRegistration.get(lnCtr);
            lnDetailCount++;
            loDetail.setTransNo(getMaster().getTransactionNo());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }
//    public JSONObject printRecord()
//            throws SQLException, JRException, CloneNotSupportedException, GuanzonException {
//
//        poJSON = new JSONObject();
//
//        if (getMaster().getTransactionNo() == null
//                || getMaster().getTransactionNo().isEmpty()) {
//
//            poJSON.put("result", "error");
//            poJSON.put("message", "No record is selected.");
//            return poJSON;
//        }
//
//        // =====================================================
//        // REPORT ROWS
//        // =====================================================
//        List<Map<String, ?>> brandRows = new ArrayList<>();
//        List<Map<String, ?>> modelRows = new ArrayList<>();
//        List<Map<String, ?>> exceptionRows = new ArrayList<>();
//        List<Map<String, ?>> giveAwayRows = new ArrayList<>();
//
//        // =====================================================
//        // BRAND ROWS
//        // =====================================================
//        for (Model item : paSalesPromotionBrand) {
//
//            Model_Sales_Promotion_Brand loBrand
//                    = (Model_Sales_Promotion_Brand) item;
//            if (!loBrand.isWithActive()) {
//                continue;
//            }
//            String brandDesc;
//
//            try {
//                brandDesc = loBrand.Brand().getDescription();
//            } catch (SQLException | GuanzonException ex) {
//                brandDesc = loBrand.getBrandID();
//            }
//
//            double srp = loBrand.getTotalAmount();
//            double discRate = loBrand.getDiscountRate();
//            double discAmt = loBrand.getDiscAmount();
//            double freight = loBrand.getFreight();
//            double incentiveAmount = loBrand.getAmount();
//
//            String remarks = loBrand.getRemarks();
//
//            // Build complete promotion description
//            String promoDetail = buildPromoDetail(
//                    srp,
//                    discRate,
//                    discAmt,
//                    loBrand.isRegistrationFree(),
//                    loBrand.isInsuranceFree(),
//                    freight,
//                    incentiveAmount,
//                    remarks
//            );
//
//            Map<String, Object> row = new HashMap<>();
//
//            row.put("brand", brandDesc == null ? "" : brandDesc);
//            row.put("promoDetail", promoDetail);
//
//            brandRows.add(row);
//        }
//
//        // =====================================================
//        // MODEL ROWS
//        // =====================================================
//        for (Model item : paSalesPromotionModel) {
//
//            Model_Sales_Promotion_Model loModel
//                    = (Model_Sales_Promotion_Model) item;
//            if (!loModel.isWithActive()) {
//                continue;
//            }
//            String brandDesc;
//
//            try {
//                brandDesc = loModel.Model()
//                        .Brand()
//                        .getDescription();
//
//            } catch (SQLException | GuanzonException ex) {
//
//                brandDesc = "";
//            }
//
//            String modelDesc;
//
//            try {
//                modelDesc = loModel.Model()
//                        .getDescription();
//
//            } catch (SQLException | GuanzonException ex) {
//
//                modelDesc = loModel.getModelID();
//            }
//
//            double srp = loModel.getTotalAmount();
//            double discRate = loModel.getDiscountRate();
//            double discAmt = loModel.getDiscAmount();
//            double freight = loModel.getFreight();
//            double incentiveAmount = loModel.getAmount();
//
//            String remarks = loModel.getRemarks();
//
//            // Build complete promotion description
//            String promoDetail = buildPromoDetail(
//                    srp,
//                    discRate,
//                    discAmt,
//                    loModel.isRegistrationFree(),
//                    loModel.isInsuranceFree(),
//                    freight,
//                    incentiveAmount,
//                    remarks
//            );
//
//            Map<String, Object> row = new HashMap<>();
//
//            row.put("brand", brandDesc == null ? "" : brandDesc);
//            row.put("model", modelDesc == null ? "" : modelDesc);
//            row.put("promoDetail", promoDetail);
//
//            modelRows.add(row);
//        }
//
//        // =====================================================
//        // MODEL EXCEPTION ROWS
//        // =====================================================
//        for (Model item : paSalesPromotionModelException) {
//
//            Model_Sales_Promotion_Model_Exception loException
//                    = (Model_Sales_Promotion_Model_Exception) item;
//            if (!loException.isWithActive()) {
//                continue;
//            }
//            String brandDesc;
//
//            try {
//                brandDesc = loException.Model()
//                        .Brand()
//                        .getDescription();
//
//            } catch (SQLException | GuanzonException ex) {
//
//                brandDesc = "";
//            }
//
//            String modelDesc;
//
//            try {
//                modelDesc = loException.Model()
//                        .getDescription();
//
//            } catch (SQLException | GuanzonException ex) {
//
//                modelDesc = loException.getModelID();
//            }
//
//            Map<String, Object> row = new HashMap<>();
//
//            row.put("brand", brandDesc == null ? "" : brandDesc);
//            row.put("model", modelDesc == null ? "" : modelDesc);
//
//            exceptionRows.add(row);
//        }
//
//        // =====================================================
//        // GIVE AWAY ROWS
//        // =====================================================
//        for (Model item : paSalesPromotionGiveAwayItem) {
//
//            Model_Sales_Promotion_GiveAway_Item loGiveAway
//                    = (Model_Sales_Promotion_GiveAway_Item) item;
//
//            if (!loGiveAway.isWithActive()) {
//                continue;
//            }
//            String barcode;
//
//            try {
//                barcode = loGiveAway.Inventory()
//                        .getBarCode();
//
//            } catch (SQLException | GuanzonException ex) {
//
//                barcode = "";
//            }
//
//            String description;
//
//            try {
//                description = loGiveAway.Inventory()
//                        .getDescription();
//
//            } catch (SQLException | GuanzonException ex) {
//
//                description = "";
//            }
//
//            Map<String, Object> row = new HashMap<>();
//
//            row.put("barcode", barcode);
//            row.put("description", description);
//            row.put("quantity", loGiveAway.getQuantity());
//            row.put(
//                    "note",
//                    loGiveAway.getRemarks() == null
//                    ? ""
//                    : loGiveAway.getRemarks()
//            );
//
//            giveAwayRows.add(row);
//        }
//
//        // =====================================================
//        // REPORT UTIL
//        // =====================================================
//        ReportUtil poReportJasper = new ReportUtil(poGRider);
//
//        poReportJasper.setReportListener(new ReportUtilListener() {
//
//            @Override
//            public void onReportOpen() {
//                System.out.println("Report opened.");
//            }
//
//            @Override
//            public void onReportClose() {
//                System.out.println("Report closed.");
//            }
//
//            @Override
//            public void onReportPrint() {
//
//                System.out.println("Report printing...");
//
//                poReportJasper.CloseReportUtil();
//            }
//
//            @Override
//            public void onReportExport() {
//                // Optional
//            }
//
//            @Override
//            public void onReportExportPDF() {
//
//                System.out.println(
//                        "Report exported to PDF."
//                );
//            }
//        });
//
//        // =====================================================
//        // BUILD "IMPLEMENTED TO" SUMMARY: shop types + provinces + branch areas
//        // =====================================================
//        List<String> implementToParts = new ArrayList<>();
//
//        // --- Shop Types (delimited string on master, decode codes to labels) ---
//        String lsShopType = getMaster().getShopType();
//        if (lsShopType != null && !lsShopType.trim().isEmpty()) {
//            String[] laShopType = lsShopType.split("»");
//            for (String lsCode : laShopType) {
//                String lsCode2 = lsCode.trim();
//                if (lsCode2.isEmpty()) {
//                    continue;
//                }
//                String lsLabel;
//                switch (lsCode2) {
//                    case "0":
//                        lsLabel = "3S Shop";
//                        break;
//                    case "1":
//                        lsLabel = "Multi Brand";
//                        break;
//                    case "2":
//                        lsLabel = "Big Bike";
//                        break;
//                    default:
//                        lsLabel = "Other Shop";
//                        break;
//                }
//                implementToParts.add(lsLabel);
//            }
//        }
//
//        // --- Provinces ---
//        for (Model item : paSalesPromotionProvince) {
//            Model_Sales_Promotion_Province loProvince = (Model_Sales_Promotion_Province) item;
//            try {
//                String lsDesc = loProvince.Province().getDescription();
//                if (lsDesc != null && !lsDesc.trim().isEmpty()) {
//                    implementToParts.add(lsDesc.trim());
//                }
//            } catch (SQLException | GuanzonException ex) {
//                // skip on lookup failure
//            }
//        }
//
//        // --- Branch Areas ---
//        for (Model item : paSalesPromotionBranchArea) {
//            Model_Sales_Promotion_Branch_Area loArea = (Model_Sales_Promotion_Branch_Area) item;
//            try {
//                String lsDesc = loArea.BranchArea().getAreaDescription();
//                if (lsDesc != null && !lsDesc.trim().isEmpty()) {
//                    implementToParts.add(lsDesc.trim());
//                }
//            } catch (SQLException | GuanzonException ex) {
//                // skip on lookup failure
//            }
//        }
//
//        String lsImplementTo = implementToParts.isEmpty()
//                ? "ALL Branches"
//                : String.join(", ", implementToParts);
//        // =====================================================
//        // MASTER PARAMETERS
//        // =====================================================
//        poReportJasper.addParameter(
//                "ImplentTo",
//                lsImplementTo);
//
//        poReportJasper.addParameter(
//                "PromoID",
//                getMaster().getTransactionNo());
//
//        poReportJasper.addParameter(
//                "PromoDescription",
//                getMaster().getPromoDescription());
//
//        poReportJasper.addParameter(
//                "ReferNo",
//                getMaster().getReferNo());
//
//        poReportJasper.addParameter(
//                "TransactionDate",
//                SQLUtil.dateFormat(
//                        getMaster().getDate(),
//                        SQLUtil.FORMAT_LONG_DATE));
//
//        poReportJasper.addParameter(
//                "PromoStart",
//                SQLUtil.dateFormat(
//                        getMaster().getFromDate(),
//                        SQLUtil.FORMAT_LONG_DATE));
//
//        poReportJasper.addParameter(
//                "PromoEnd", (getMaster().getThruDate() == null ? ""
//                : SQLUtil.dateFormat(
//                        getMaster().getThruDate(),
//                        SQLUtil.FORMAT_LONG_DATE))
//        );
//
//        poReportJasper.addParameter(
//                "Remarks",
//                getMaster().getRemarks());
//
//        poReportJasper.addParameter(
//                "BranchName", poGRider.getBranchName());
//
//        poReportJasper.addParameter(
//                "DatePrinted",
//                SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_TIMESTAMP));
//
//        // =====================================================
//        // BRAND DATASOURCE
//        // =====================================================
//        poReportJasper.addParameter(
//                "BrandDataSource",
//                new JRMapCollectionDataSource(brandRows));
//
//        // =====================================================
//        // MODEL DATASOURCE
//        // =====================================================
//        poReportJasper.addParameter(
//                "ModelDataSource",
//                new JRMapCollectionDataSource(modelRows));
//
//        // =====================================================
//        // MODEL EXCEPTION DATASOURCE
//        // =====================================================
//        poReportJasper.addParameter(
//                "ModelExceptionDataSource",
//                new JRMapCollectionDataSource(exceptionRows));
//
//        // =====================================================
//        // GIVE AWAY DATASOURCE
//        // =====================================================
//        poReportJasper.addParameter(
//                "GiveAwayDataSource",
//                new JRMapCollectionDataSource(giveAwayRows));
//
//        // =====================================================
//        // VISIBILITY
//        // =====================================================
//        poReportJasper.addParameter(
//                "HasBrand",
//                !brandRows.isEmpty()
//        );
//
//        poReportJasper.addParameter(
//                "HasModel",
//                !modelRows.isEmpty()
//        );
//
//        poReportJasper.addParameter(
//                "HasModelException",
//                !exceptionRows.isEmpty()
//        );
//
//        poReportJasper.addParameter(
//                "HasGiveAway",
//                !giveAwayRows.isEmpty()
//        );
//
//        // =====================================================
//        // APPROVAL REMARKS
//        // =====================================================
//        if (getMaster().getTransactionStatus().equals(SalesTransactionStatic.CONFIRMED)) {
//            poReportJasper.addParameter("watermarkImagePath", poGRider.getReportPath() + "images\\blank.png");
//        }
//        JSONObject loJSON = getEntryBy();
//        String entryBy = "";
//        String entryDate = "";
//
//        if ("success".equals((String) loJSON.get("result"))) {
//            entryBy = (String) loJSON.get("sCompnyNm");
//            entryDate = (String) loJSON.get("sEntryDte");
//        }
//        String lsPreparedBy = entryBy;
//        String lsPreparedByDate = entryDate;
//        String lsConfirmedBy = "";
//        String lsConfirmedDate = "";
//
//        String lsSQL = " SELECT sModified, dModified "
//                + " FROM Transaction_Status_History "
//                + " WHERE sTableNme ='Sales_Promotion_Master' "
//                + " AND cRefrStat = '1' AND cTranStat = '1' ORDER BY dModified DESC";
//        lsSQL = MiscUtil.addCondition(lsSQL, " sSourceNo =  " + SQLUtil.toSQL(getMaster().getTransactionNo()));
//        System.out.println("Execute SQL : " + lsSQL);
//        ResultSet loRS = poGRider.executeQuery(lsSQL);
//
//        if (MiscUtil.RecordCount(loRS) > 0L) {
//            if (loRS.next()) {
//                if (loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))) {
//                    lsConfirmedBy = loRS.getString("sModified") == null ? "" : getSysUser(poGRider.Decrypt(loRS.getString("sModified")));
//                    LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
//                    lsConfirmedDate = dModified.format(formatter);
//                }
//            }
//        }
//
//        MiscUtil.close(loRS);
//
//        poReportJasper.addParameter("PrepNme", lsPreparedBy + " - " + lsPreparedByDate);
//        poReportJasper.addParameter("ConfirmNme", lsConfirmedBy + " - " + lsConfirmedDate);
//        poReportJasper.addParameter("ReceivrNme", "");
//        // =====================================================
//        // MAIN REPORT DATASOURCE
//        // =====================================================
//        List<Map<String, ?>> masterRows = new ArrayList<>();
//
//        masterRows.add(
//                new HashMap<String, Object>());
//
//        poReportJasper.setJRBeanCollectionDataSource(
//                new JRMapCollectionDataSource(masterRows));
//
//        // =====================================================
//        // REPORT
//        // =====================================================
//        poReportJasper.setJasperPath(
//                PromoMaintenancePrint.getJasperReport(
//                        psIndustryCode
//                )
//        );
//
//        poReportJasper.isAlwaysTop(false);
//        poReportJasper.isWithUI(true);
//        poReportJasper.isWithExport(false);
//        poReportJasper.isWithExportPDF(true);
//        poReportJasper.willExport(false);
//
//        return poReportJasper.generateReport();
//    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT a.* FROM Sales_Master a";

    }

    public JSONObject loadTransactionList()
            throws SQLException, GuanzonException, CloneNotSupportedException {

//        if (psIndustryCode.isEmpty()) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "I is not set");
//            return poJSON;
//        }
        paMaster.clear();
        initSQL();
        String lsSQL = SQL_BROWSE;
        String lsCondition = "";

        if (!psIndustryCode.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
        }
        if (!psCategorCD.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sCategrCd = " + SQLUtil.toSQL(psCategorCD));
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox ";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        System.out.println("Load Transaction list query is " + lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Sales_Master loObject = new SalesModels(poGRider).SalesMaster();

            for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); ++lnCtr) {
                String lsColumnName = loRS.getMetaData().getColumnName(lnCtr);
                loObject.setValue(lsColumnName, loRS.getObject(lnCtr));
            }
            paMaster.add((Model) loObject);
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject ReplaceTransaction(String fsTransaction) throws SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (fsTransaction == null || fsTransaction.isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "No Transaction to replace.");
            return poJSON;
        }

        String lsSQL = "UPDATE Sales_Master SET"
                + "  cTranStat = " + SQLUtil.toSQL(SalesTransactionStatic.REPLACED)
                + ", sModified = " + SQLUtil.toSQL(poGRider.Encrypt(poGRider.getUserID()))
                + ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate())
                + " WHERE sTransNox= " + SQLUtil.toSQL(fsTransaction);

        poGRider.beginTrans("UPDATE STATUS", "ReplaceTransaction", SOURCE_CODE, fsTransaction);

        if (poGRider.executeQuery(lsSQL, "Sales_Promotion_Master", poGRider.getBranchCode(), "", "") <= 0) {
            poGRider.rollbackTrans();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to replace conflicting promo(s).");
            return poJSON;
        }

        poGRider.commitTrans();

        poJSON.put("result", "success");
        poJSON.put("message", "Transaction replaced successfully.");
        return poJSON;
    }

    //------------------------ATTACHMENT CODE HERE-------------------------------------------------------------------------------------
    private TransactionAttachment TransactionAttachment()
            throws SQLException,
            GuanzonException {
        return new SysTableContollers(poGRider, null).TransactionAttachment();
    }

    public JSONObject loadAttachments()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paAttachments = new ArrayList<>();

        TransactionAttachment loAttachment = new SysTableContollers(poGRider, null).TransactionAttachment();
        List loList = loAttachment.getAttachments(SOURCE_CODE, getMaster().getTransactionNo());
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
            if ("success".equals((String) poJSON.get("result"))) {
                if (getMaster().getEditMode() == EditMode.UPDATE) {
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
                        SOURCE_CODE,
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
                        poJSON = (JSONObject) poJSON.get("error");
                        poJSON.put("result", "error");
                        System.out.println("ERROR WebFile.DownloadFile: " + poJSON.get("message"));
                        System.out.println("poJSON error WebFile.DownloadFile: " + poJSON.toJSONString());
                    }

                } else {
                    System.out.println("poJSON error WebFile.DownloadFile: " + poJSON.toJSONString());
                }
            }
        }
        return poJSON;
    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    private JSONObject setJSON(String fsResult, String fsMessage) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", fsResult);
        loJSON.put("message", fsMessage);
        return loJSON;
    }

    /**
     * Adds an empty attachment row. FIX: the second and later rows were created WITHOUT newRecord(), so they stayed
     * in an unusable edit mode and were never saved. Both branches now call newRecord().
     */
    public JSONObject addAttachment()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paAttachments.isEmpty()) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).newRecord();
        } else {
            if (!paAttachments.get(paAttachments.size() - 1).getModel().getTransactionNo().isEmpty()) {
                paAttachments.add(TransactionAttachment());
                poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).newRecord();
            } else {
                poJSON = setJSON("error", "Unable to add transaction attachment.");
                return poJSON;
            }
        }
        poJSON = setJSON("success", "success");
        return poJSON;
    }

    public JSONObject removeAttachment(int fnRow) throws GuanzonException, SQLException {
        poJSON = new JSONObject();
        if (getTransactionAttachmentCount() <= 0) {
            poJSON = setJSON("error", "No transaction attachment to be removed.");
            return poJSON;
        }

        if (paAttachments.get(fnRow).getEditMode() == EditMode.ADDNEW) {
            paAttachments.remove(fnRow);
            System.out.println("Attachment :" + fnRow + " Removed");
        } else {
            paAttachments.get(fnRow).getModel().setRecordStatus(RecordStatus.INACTIVE);
            System.out.println("Attachment :" + fnRow + " Deactivate");
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public int addAttachment(String fFileName) throws SQLException, GuanzonException {
        for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
            if (fFileName.equals(paAttachments.get(lnCtr).getModel().getFileName())
                    && RecordStatus.INACTIVE.equals(paAttachments.get(lnCtr).getModel().getRecordStatus())) {
                paAttachments.get(lnCtr).getModel().setRecordStatus(RecordStatus.ACTIVE);
                System.out.println("Attachment :" + lnCtr + " Activate");
                return lnCtr;
            }
        }

        addAttachment();
        paAttachments.get(getTransactionAttachmentCount() - 1).getModel().setFileName(fFileName);
        paAttachments.get(getTransactionAttachmentCount() - 1).getModel().setSourceNo(getMaster().getTransactionNo());
        paAttachments.get(getTransactionAttachmentCount() - 1).getModel().setRecordStatus(RecordStatus.ACTIVE);
        return getTransactionAttachmentCount() - 1;
    }

    public void copyFile(String fsPath) {
        Path source = Paths.get(fsPath);
        Path targetDir = Paths.get(System.getProperty("sys.default.path.temp.attachments"));

        try {
            // Ensure target directory exists
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Copy file into the target directory
            Files.copy(source, targetDir.resolve(source.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File copied successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject checkExistingFileName(String fsValue) throws SQLException, GuanzonException {
        poJSON = new JSONObject();

        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(TransactionAttachment().getModel()), " sFileName = " + SQLUtil.toSQL(fsValue));
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0) {
                if (loRS.next()) {
                    if (loRS.getString("sFileName") != null && !"".equals(loRS.getString("sFileName"))) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "File name already exist in database.\nTry changing the file name to upload.");
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("No record loaded.");
        }
        return poJSON;
    }

    public JSONObject uploadCASAttachments(GRiderCAS instance, String access, int fnRow, String fsOriginalFileName) throws Exception {
        poJSON = new JSONObject();
        System.out.println("Uploading... : fsOriginalFileName : " + fsOriginalFileName);
        System.out.println("New File Name... : " + paAttachments.get(fnRow).getModel().getFileName());
        String hash;
        String lsFile = paAttachments.get(fnRow).getModel().getFileName();

        //check if new file is existing
        File file = new File(paAttachments.get(fnRow).getModel().getImagePath() + "/" + lsFile);
        if (!file.exists()) {
            //check if original file is existing
            lsFile = fsOriginalFileName;
            file = new File(paAttachments.get(fnRow).getModel().getImagePath() + "/" + lsFile);
            if (!file.exists()) {
                poJSON = setJSON("error", "Cannot locate file in " + paAttachments.get(fnRow).getModel().getImagePath() + "/" + lsFile
                        + ".\nContact system administrator for assistance.");
                return poJSON;
            }
        }

        //check if file hash is not empty
        hash = paAttachments.get(fnRow).getModel().getMD5Hash();
        if (paAttachments.get(fnRow).getModel().getMD5Hash() == null || "".equals(paAttachments.get(fnRow).getModel().getMD5Hash())) {
            hash = MiscReplUtil.md5Hash(paAttachments.get(fnRow).getModel().getImagePath() + "/" + lsFile);
        }

        JSONObject result = WebFile.UploadFile(getAccessToken(access),
                "0032",
                "",
                paAttachments.get(fnRow).getModel().getFileName(),
                instance.getBranchCode(),
                hash,
                encodeFileToBase64Binary(file),
                paAttachments.get(fnRow).getModel().getSourceCode(),
                paAttachments.get(fnRow).getModel().getSourceNo(),
                "");

        if ("error".equalsIgnoreCase((String) result.get("result"))) {
            System.out.println("Upload Error : " + result.toJSONString());
            System.out.println("Upload Error : " + paAttachments.get(fnRow).getModel().getFileName());
            poJSON = setJSON("error", "System error while uploading file " + paAttachments.get(fnRow).getModel().getFileName()
                    + ".\nContact system administrator for assistance.");
            return poJSON;
        }
        paAttachments.get(fnRow).getModel().setMD5Hash(hash);
        paAttachments.get(fnRow).getModel().setSendStatus("1");
        System.out.println("Upload Success : " + paAttachments.get(fnRow).getModel().getFileName());
        poJSON.put("result", "success");
        return poJSON;
    }

    private static String encodeFileToBase64Binary(File file) throws Exception {
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);
        return new String(Base64.encodeBase64(bytes), "UTF-8");
    }

    private static JSONObject token = null;

    private static String getAccessToken(String access) {
        try {
            JSONParser oParser = new JSONParser();
            if (token == null) {
                token = (JSONObject) oParser.parse(new FileReader(access));
            }

            Calendar current_date = Calendar.getInstance();
            current_date.add(Calendar.MINUTE, -25);
            Calendar date_created = Calendar.getInstance();
            date_created.setTime(SQLUtil.toDate((String) token.get("created"), SQLUtil.FORMAT_TIMESTAMP));

            //Check if token is still valid within the time frame
            //Request new access token if not in the current period range
            if (current_date.after(date_created)) {
                String[] xargs = new String[]{(String) token.get("parent"), access};
                RequestAccess.main(xargs);
                token = (JSONObject) oParser.parse(new FileReader(access));
            }

            return (String) token.get("access_key");
        } catch (IOException ex) {
            return null;
        } catch (ParseException ex) {
            return null;
        }
    }

    public boolean isJSONSuccess(JSONObject foJSON) {
        return ("success".equals((String) foJSON.get("result")) || !"error".equals((String) foJSON.get("result")));
    }

    private boolean isJSONSuccess(JSONObject loJSON, String module, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            Platform.runLater(() -> {
                if (message != null) {
                    ShowMessageFX.Warning(null, module, fsModule + ": " + message);
                }
            });
            return false;
        }
        String message = (String) loJSON.get("message");

        Platform.runLater(() -> {
            if (message != null) {
                ShowMessageFX.Information(null, module, fsModule + ": " + message);
            }
        });
        return true;

    }
    // imports: java.math.BigDecimal, java.math.RoundingMode
    private static final double VAT_RATE = 0.12;   // PH VAT

    private static double round2(double fnValue) {
        return BigDecimal.valueOf(fnValue).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public void computeTransaction() {
        if (getEditMode() != EditMode.ADDNEW && getEditMode() != EditMode.UPDATE) {
            return;
        }
        double lnTranTotal = 0, lnDiscount = 0, lnAddDisc = 0, lnFreight = 0;
        double lnInsAdd = 0, lnRegIns = 0;
        double lnVatableBase = 0, lnNonVatBase = 0;

        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            Model_Sales_Detail loDetail = (Model_Sales_Detail) paDetail.get(lnCtr);

            // ---- line amounts ----
            double lnGross = round2(loDetail.getUnitPrice() * loDetail.getQuantity());
            double lnDiscAmt = round2(lnGross * loDetail.getDiscount() / 100.0); // nDiscount is a rate (%)
            double lnAddAmt = loDetail.getAddDiscount();
            double lnFrtAmt = loDetail.getFreight();

            // ---- charged registration + insurance for this serial ----
            double lnCharge = 0;
            String lsSerial = loDetail.getSerialID();
            if (lsSerial != null && !lsSerial.isEmpty()) {
                for (int lnIns = 0; lnIns < paSalesInsurance.size(); lnIns++) {
                    Model_Sales_Insurance loIns = (Model_Sales_Insurance) paSalesInsurance.get(lnIns);
                    if (lsSerial.equals(loIns.getSerialID()) && loIns.isChargeTo()) {
                        lnCharge += loIns.getInsuranceAmount();
                    }
                }
                for (int lnReg = 0; lnReg < paSalesRegistration.size(); lnReg++) {
                    Model_Sales_Registration loReg = (Model_Sales_Registration) paSalesRegistration.get(lnReg);
                    if (lsSerial.equals(loReg.getSerialID()) && loReg.isChargeTo()) {
                        lnCharge += loReg.getInsuranceAmount();
                    }
                }
            }
            loDetail.setInsuranceAmount(round2(lnCharge));

            // ---- master accumulators ----
            lnTranTotal += lnGross;
            lnDiscount += lnDiscAmt;
            lnAddDisc += lnAddAmt;
            lnFreight += lnFrtAmt;
            lnInsAdd += loDetail.getInsuranceAdd();
            lnRegIns += loDetail.getInsuranceAmount();

            // net selling amount of the line (basis for VAT / net sales)
            double lnLineNet = lnGross - lnDiscAmt - lnAddAmt + lnFrtAmt;
            if ("1".equals(loDetail.getWithVAT())) {
                lnVatableBase += lnLineNet;
            } else {
                lnNonVatBase += lnLineNet;
            }
        }

        lnTranTotal = round2(lnTranTotal);
        lnDiscount = round2(lnDiscount);
        lnAddDisc = round2(lnAddDisc);
        lnFreight = round2(lnFreight);
        lnInsAdd = round2(lnInsAdd);
        lnRegIns = round2(lnRegIns);

        Model_Sales_Master loMaster = getMaster();
        loMaster.setTransactionTotal(lnTranTotal);   // no discount
        loMaster.setDiscount(lnDiscount);            // amount, from rate x line gross
        loMaster.setAddDiscount(lnAddDisc);
        loMaster.setFreight(lnFreight);
        loMaster.setInsuranceAdd(lnInsAdd);
        loMaster.setRegistrationAmount(lnRegIns);
        loMaster.setZeroVATSales(0.00);              // not applicable to MC sales

        // ---- VAT ----
        double lnVatSales, lnVatAmount, lnNonVat;
        if (loMaster.getCWTAmount() > 0) {
            // CWT: no VAT, everything goes to non-VAT sales
            lnVatSales = 0.00;
            lnVatAmount = 0.00;
            lnNonVat = round2(lnVatableBase + lnNonVatBase);
        } else {
            lnVatSales = round2(lnVatableBase / (1 + VAT_RATE));
            lnVatAmount = round2(round2(lnVatableBase) - lnVatSales);   // tallies to the gross
            lnNonVat = round2(lnNonVatBase);
        }
        loMaster.setVATSales(lnVatSales);
        loMaster.setVATAmount(lnVatAmount);
        loMaster.setNonVATSales(lnNonVat);

        // nAmtPaidx stays 0, nAdvances is set by the reservation source
        double lnAmountDue = lnTranTotal - lnDiscount - lnAddDisc + lnFreight
                + lnInsAdd + lnRegIns - loMaster.getAdvances();
        loMaster.setAmountDue(round2(lnAmountDue));
    }

}
