/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import javafx.application.Platform;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
import net.sf.jasperreports.view.JasperViewer;
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
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.*;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.VehicleQuotationStatus;
import ph.com.guanzongroup.cas.sales.validator.ValidityMasterValidator;

import javax.script.ScriptException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;
import ph.com.guanzongroup.cas.sales.status.SalesInquiryStatic;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

/**
 * @author Arsiela
 */
public class VehicleQuotation extends Transaction {
    public String psCompanyId = "";
    public String psIndustryId = "";
    public String psCategoryId = "";
    public String psApprover = "";

    public List<Model_Quotation_AddOn> paAddOn;
    public List<Model> paMaster;
    
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "VQOT";

        poMaster = new SalesModels(poGRider).SalesQuotationMaster();
        poDetail = new SalesModels(poGRider).SalesQuotationVehicle();

        paAddOn = new ArrayList<>();
        paMaster = new ArrayList<>();
        psApprover = "";
        setApproving("");
        return initialize();
    }

    @Override
    public String getSourceCode() { return SOURCE_CODE; }
    
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCategoryId(String categoryId) { psCategoryId = categoryId; }
    
    private JSONObject setJSON(String fsResult, String fsMessage) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", fsResult);
        loJSON.put("message", fsMessage);
        return loJSON;
    }
    private boolean checkEmpty(String fsValue){
        return fsValue == null || "".equals(fsValue);
    }
    
    private boolean checkNotEmpty(String fsValue){
        return fsValue != null && !"".equals(fsValue);
    }

    public boolean isJSONSuccess(JSONObject foJSON) {
        return ("success".equals((String) foJSON.get("result")) || !"error".equals((String) foJSON.get("result")));
    }

    public JSONObject NewTransaction()
            throws CloneNotSupportedException, SQLException, GuanzonException {
         poJSON = new JSONObject();
        
        poJSON = newTransaction();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        ReloadDetail();
        
        return poJSON;
    }
    
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException {
        poJSON = new JSONObject();
        
        poJSON = openTransaction(transactionNo);
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }

        return poJSON;
    }
    @Override
    protected JSONObject openTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        this.poGRider.ensureConnected();
        this.poJSON = this.poMaster.openRecord(transactionNo);
        if (!"success".equals((String)this.poJSON.get("result"))) {
            this.poJSON.put("message", "Unable to open transaction master record.");
            this.clear();
            return this.poJSON;
        } else {
            this.paDetail.clear();
            
            String sql = "SELECT sQotPayID FROM " + this.poDetail.getTable() + " WHERE sTransNox = " + SQLUtil.toSQL(transactionNo) + " ORDER BY sQotPayID";
            ResultSet rs = this.poGRider.executeQuery(sql);

            while(rs.next()) {
                Model loDetail = (Model)this.poDetail.clone();
                loDetail.newRecord();
                this.poJSON = loDetail.openRecord( rs.getString("sQotPayID"),transactionNo);
                if (!"success".equals((String)this.poJSON.get("result"))) {
                    this.poJSON.put("message", "Unable to open transaction detail record.");
                    this.clear();
                    return this.poJSON;
                }

                this.paDetail.add(loDetail);
            }

            this.pnEditMode = 1;
            this.pbRecordExist = true;
            this.poJSON = new JSONObject();
            this.poJSON.put("result", "success");
            return this.poJSON;
        }
    }
    
    public JSONObject UpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        
        poJSON = updateTransaction();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        return poJSON;
    }
    
    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }
    
    @Override
    protected JSONObject saveTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        this.poJSON = new JSONObject();
        if (!this.pbInitTran) {
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "Object is not initialized.");
            return this.poJSON;
        } else if (this.pnEditMode == 1) {
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "Saving of unmodified transaction is not allowed.");
            return this.poJSON;
        } else {
            this.poGRider.ensureConnected();
            this.poJSON = this.willSave();
            if ("error".equals((String)this.poJSON.get("result"))) {
                return this.poJSON;
            } else {
                if (this.getEditMode() == 0) {
                    this.pdModified = this.poGRider.getServerDate();
                    this.poMaster.setValue("sModified", this.poGRider.Encrypt(this.poGRider.getUserID()));
                }

                if (!this.pbWthParent) {
                    this.poGRider.beginTrans((String)this.poEvent.get("event"), this.poMaster.getTable(), this.SOURCE_CODE, String.valueOf(this.poMaster.getValue(1)));
                }

                this.poJSON = this.save();
                if ("success".equals((String)this.poJSON.get("result"))) {

                    if (this.pnEditMode != 0 && this.pnEditMode != 2) {
                        if (!this.pbWthParent) {
                            this.poGRider.rollbackTrans();
                        }

                        this.poJSON.put("result", "error");
                        this.poJSON.put("message", "Edit mode is not allowed to save transaction.");
                        return this.poJSON;
                    } else {
                        this.poMaster.setValue("dModified", this.pdModified);
                        this.poJSON = this.poMaster.saveRecord();
                        if ("error".equals((String)this.poJSON.get("result"))) {
                            if (!this.pbWthParent) {
                                this.poGRider.rollbackTrans();
                            }

                            return this.poJSON;
                        } else {
                            for(int lnCtr = 0; lnCtr <= this.paDetail.size() - 1; ++lnCtr) {
                                ((Model)this.paDetail.get(lnCtr)).setValue("dModified", this.pdModified);
                                Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
                                if(((Model)this.paDetail.get(lnCtr)).getEditMode() == EditMode.ADDNEW){
                                    ((Model)this.paDetail.get(lnCtr)).setValue("sQotPayID", Detail(lnCtr).getNextCode());
                                }
                                this.poJSON = ((Model)this.paDetail.get(lnCtr)).saveRecord();
                                if ("error".equals((String)this.poJSON.get("result"))) {
                                    if (!this.pbWthParent) {
                                        this.poGRider.rollbackTrans();
                                    }

                                    return this.poJSON;
                                }
                            }

                            this.poJSON = this.saveOthers();
                            if ("error".equals((String)this.poJSON.get("result"))) {
                                if (!this.pbWthParent) {
                                    this.poGRider.rollbackTrans();
                                }

                                return this.poJSON;
                            } else {
                                if (!this.pbWthParent) {
                                    this.poGRider.commitTrans();
                                }

                                this.saveComplete();
                                this.pnEditMode = -1;
                                this.pbRecordExist = true;
                                this.poJSON = new JSONObject();
                                this.poJSON.put("result", "success");
                                this.poJSON.put("message", "Transaction saved successfully.");
                                return this.poJSON;
                            }
                        }
                    }
                } else {
                    if (!this.pbWthParent) {
                        this.poGRider.rollbackTrans();
                    }

                    return this.poJSON;
                }
            }
        }
    }
    
    public JSONObject callApproval(){
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
            String lsUserIDxx = poJSON.get("sUserIDxx").toString();
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON = setJSON("error", "User is not an authorized approving officer.");
                return poJSON;
            }
            setApproving(lsUserIDxx);
            psApprover = lsUserIDxx;
        }   
        
        poJSON = setJSON("success","success");
        return poJSON;
    }
    
    public String getStatus(String lsStatus) {
        switch (lsStatus) {
            case VehicleQuotationStatus.VOID:
                return "Voided";
            case VehicleQuotationStatus.CANCELLED:
                return "Cancelled";
            case VehicleQuotationStatus.APPROVED:
                return "Approved";
            case VehicleQuotationStatus.OPEN:
                return "Open";
            default:
                return "Unknown";
        }
    }
    
    public JSONObject ApproveTransaction() throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        String lsStatus = VehicleQuotationStatus.APPROVED;
        
        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No transacton was loaded.");
            return poJSON;
        }
        
        Model_Sales_Quotation_Master loObject = new SalesModels(poGRider).SalesQuotationMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load transaction. " + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (lsStatus.equals(loObject.getTransactionStatus())) {
            poJSON = setJSON("error", "Transaction was already approved.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(!pbWthParent){
            psApprover = poGRider.getUserID();
            if(validateStandardDetails(0)){ //require approval when result is true
                poJSON = callApproval();
                if (!isJSONSuccess(poJSON)) {
                    return poJSON;
                }
            }
        }
        
        //change status
        poGRider.beginTrans("UPDATE STATUS", "ApproveTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), "", lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();
        
        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction approved successfully.");
        return poJSON;
    }
    
    public JSONObject VoidTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException,
            ScriptException {
        poJSON = new JSONObject();

        String lsStatus = VehicleQuotationStatus.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }
        
        Model_Sales_Quotation_Master loObject = new SalesModels(poGRider).SalesQuotationMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load transaction. " + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (lsStatus.equals(loObject.getTransactionStatus())) {
            poJSON = setJSON("error", "Transaction was already voided.");
            return poJSON;
        }
        
        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sTransNox"),"", lsStatus, false, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction voided successfully.");
        return poJSON;
    }
    
    public JSONObject CancelTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException,
            ScriptException {
        poJSON = new JSONObject();

        String lsStatus = VehicleQuotationStatus.CANCELLED;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }
        
        Model_Sales_Quotation_Master loObject = new SalesModels(poGRider).SalesQuotationMaster();
        poJSON = loObject.openRecord(Master().getTransactionNo());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load transaction. " + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (lsStatus.equals(loObject.getTransactionStatus())) {
            poJSON = setJSON("error", "Transaction was already cancelled.");
            return poJSON;
        }
        
        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        if(!pbWthParent){
            psApprover = poGRider.getUserID();
            poJSON = callApproval();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }
        
        //change status
        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), "", lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();
        
        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction cancelled successfully.");
        return poJSON;
    }
    
    public JSONObject loadTransactionList(String fsClient, String fsTransactionNo) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        paMaster = new ArrayList<>();
        
        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategoryId)
                + " AND b.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND c.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsClient + "%")
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransactionNo + "%")
            );
        
       
        //If current user is an ordinary user load only its inquiries
//        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
//            lsSQL = MiscUtil.addCondition(lsSQL,
//                    " b.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
//        }
        
        lsSQL = lsSQL + " ORDER BY a.dTransact, c.sCompnyNm  ASC ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON = setJSON("error", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Sales_Quotation_Master loObject = new SalesModels(poGRider).SalesQuotationMaster();
            poJSON = loObject.openRecord(loRS.getString("sTransNox"));
            if (isJSONSuccess(poJSON)) {
                paMaster.add((Model) loObject);
            } else {
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    public Model_Sales_Quotation_Master TransactionList(int row) {
        return (Model_Sales_Quotation_Master) paMaster.get(row);
    }

    public int getTransactionListCount() {
        if(paMaster == null){
            return 0;
        }
        return paMaster.size();
    }
    
    public JSONObject SearchTransaction(String fsValue, boolean fbByCode) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox ";
        System.out.println("Executing SQL: " + lsSQL);
        if(pbWithUI){

            poJSON = ShowDialogFX.Browse(poGRider,
                    lsSQL,
                    "",
                    "Validity ID»Description»From Date»Thru Date»Status",
                    "sTransNox»sValidDsc»dFromDate»dThruDate»xRecdStat",
                    "a.sTransNox»a.sValidDsc»a.dFromDate»a.dThruDate»" + 
                "     CASE  " +
                "         WHEN a.cTranStat = '0'  " +
                "         THEN 'OPEN'  " +
                "         WHEN a.cTranStat = '1'  " +
                "         THEN 'APPROVED'  " +
                "         WHEN a.cTranStat = '2'  " +
                "         THEN 'CANCELLED'  " +
                "         WHEN a.cTranStat = '3'  " +
                "         THEN 'VOIDED'  " +
                "         ELSE 'UNKNOWN' " +
                "     END AS xRecdStat",
                    fbByCode ? 0 : 1);
        } else {
            poJSON.put("sTransNox", fsValue);
        }
        if (poJSON != null) {

            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject SearchTransaction(String fsYear, String fsStatus) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND YEAR(a.dFromDate) = " + SQLUtil.toSQL(fsYear)
                + " AND a.cRecdStat = " + SQLUtil.toSQL(fsStatus)
            );
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox ";
        System.out.println("Executing SQL: " + lsSQL);
        if(pbWithUI){
            poJSON = ShowDialogFX.Browse(poGRider,
                    lsSQL,
                    "",
                    "Validity ID»Description»From Date»Thru Date»Status",
                    "sTransNox»sValidDsc»dFromDate»dThruDate»xRecdStat",
                    "a.sTransNox»a.sValidDsc»a.dFromDate»a.dThruDate»" + 
                "     CASE  " +
                "         WHEN a.cTranStat = '0'  " +
                "         THEN 'OPEN'  " +
                "         WHEN a.cTranStat = '1'  " +
                "         THEN 'APPROVED'  " +
                "         WHEN a.cTranStat = '2'  " +
                "         THEN 'CANCELLED'  " +
                "         WHEN a.cTranStat = '3'  " +
                "         THEN 'VOIDED'  " +
                "         ELSE 'UNKNOWN' " +
                "     END",
                   0);
        }
        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject SearchClient(String value, boolean byCode)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        
        SalesInquiry object = new SalesControllers(poGRider, logwrapr).SalesInquiry();
        object.InitTransaction();
        object.setTransactionStatus(SalesInquiryStatic.OPEN);
        object.setWithUI(pbWithUI);
        if(pbWithUI){
            poJSON = object.searchTransaction(psIndustryId, psCompanyId, psCategoryId, value, "");
        } else {
            poJSON = object.OpenTransaction(value);
        }
        if (isJSONSuccess(poJSON)) {
            Master().setClientId(object.Master().getClientId());
            Master().setAddressId(object.Master().getAddressId());
            Master().setContactId(object.Master().getContactId());
            Detail(0).setSourceCode(object.getSourceCode());
            Detail(0).setSourceNo(object.Master().getTransactionNo());
            Detail(0).setPaymentForm(object.Master().getPurchaseType());
        }
        
        return poJSON;
    }
    
    public JSONObject Searchbank(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(checkEmpty(Detail(row).getSourceNo())){
            return setJSON("error", "Inquiry cannot be empty.");
        }
        
        FinancingRates object = new SalesControllers(poGRider, logwrapr).FinancingRates();
        object.setRecordStatus(RecordStatus.ACTIVE);
        if(pbWithUI){
            poJSON = object.searchRecord(value, byCode);
        } else {
            poJSON = object.openRecord(value);
        }
        if (isJSONSuccess(poJSON)) {
            Detail(row).setQuotationRate(object.getModel().getRateId());
            Detail(row).setDuration(object.getModel().getDuration());
            Detail(row).setRateValue(object.getModel().getRate());
        }
        return poJSON;
    }
    
    public JSONObject SearchSalesGiveAways(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        
        SalesGiveaways object = new SalesControllers(poGRider, logwrapr).SalesGiveaways();
        object.setTransactionStatus(SalesInquiryStatic.OPEN);
        
        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
//            Master().setSalesGiveAwaysCode(object.Master().getGiveawayCode());
        }
        return poJSON;
    }
    
    public JSONObject SearchBrand(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        Brand object = new ParamControllers(poGRider, logwrapr).Brand();
        object.setRecordStatus(RecordStatus.ACTIVE);
        if(pbWithUI){
            poJSON = object.searchRecord(value, byCode, psIndustryId);
        } else {
            poJSON = object.openRecord(value);
        }
        if (isJSONSuccess(poJSON)) {
            Detail(row).setBrandId(object.getModel().getBrandId());
        }
        return poJSON;
    }
    
    public JSONObject SearchModel(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(checkEmpty(Detail(row).getBrandId())){
            return setJSON("error", "Brand cannot be empty.");
        }
        
        org.guanzon.cas.parameter.Model object = new ParamControllers(poGRider, logwrapr).Model();
        object.setRecordStatus(RecordStatus.ACTIVE);
        if(pbWithUI){
            poJSON = object.searchRecord(value, byCode, Detail(row).getBrandId());
        } else {
            poJSON = object.openRecord(value);
        }
        if (isJSONSuccess(poJSON)) {
            Detail(row).setModelId(object.getModel().getModelId());
        }
        return poJSON;
    }
    
    public JSONObject SearchModelVariant(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        
        if(checkEmpty(Detail(row).getModelId())){
            poJSON =  setJSON("error", "Model cannot be empty.");
            poJSON.put("row", row);
            return poJSON;
        }
        
        if("0".equals(Detail(row).getPaymentForm())){ //Cash
            VehicleDescription object = new SalesControllers(poGRider, logwrapr).VehicleDescription();
            object.initialize();
            object.setRecordStatus(RecordStatus.ACTIVE);
            object.setIndustryId(psIndustryId);
            if(pbWithUI){
                poJSON = object.searchRecord(value, byCode, Detail(row).getModelId());
            } else {
                poJSON = object.openRecord(value);
            }
            if (isJSONSuccess(poJSON)) {
                Detail(row).setVariantId(object.getModel().getVariantId()); 
                Detail(row).setSRPAmount(object.getModel().getSellingPrice()); 
                
                poJSON = AutoPopulateVehicleAddOn(object.getModel().getVariantId());
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            }
        } else {
            String lsSQL = VehicleFinancing_SQL();
            lsSQL = MiscUtil.addCondition(lsSQL, SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate "
                                            + " AND e.sModelIDx = " + Detail(row).getModelId()
                                            + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                        );
            System.out.println("Executing SQL: " + lsSQL);
            if(pbWithUI){
                poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Financing ID»Description»Downpayment Rate",
                "sVhclFIDx»xDescription»nDPRatexx",
                "a.sVhclFIDx»CONCAT(f.sDescript,' ', e.sDescript, ' ', c.sDescript, ' ', g.sDescript, ' ', c.nYearMdlx, ' ',d.sTransmss )»a.nDPRatexx",
                byCode ? 0 : 1);
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "success");
                poJSON.put("sVrntIDxx", value);
                poJSON.put("nDPRatexx", 15.00); //for testing only
                poJSON.put("nRsrvAmtx", 10000.00); //for testing only
            }
            if (poJSON != null) {
                String lsVariantId = (String) poJSON.get("sVrntIDxx");
                Double ldblDPRate = (Double) poJSON.get("nDPRatexx");
                Double ldblRSVAmt = (Double) poJSON.get("nRsrvAmtx");
                Double ldblSRPAmt = (Double) poJSON.get("nSRPAmntx");
                
                Detail(row).setVariantId(lsVariantId);
                Detail(row).setDownPaymentPercent(ldblDPRate);
                Detail(row).setReservationAmount(ldblRSVAmt);
                Detail(row).setSRPAmount(ldblSRPAmt); 
                
                poJSON = AutoPopulateVehicleAddOn(lsVariantId);
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
                
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;
            }
        }
        return poJSON;
    }
    
    public JSONObject SearchVehicleFinancing(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        
        String lsSQL = VehicleFinancing_SQL();
        lsSQL = MiscUtil.addCondition(lsSQL, SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate "
                                                + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                                );
        System.out.println("Executing SQL: " + lsSQL);
        if(pbWithUI){
            poJSON = ShowDialogFX.Browse(poGRider,
            lsSQL,
            value,
            "Financing ID»Description»Downpayment Rate",
            "sVhclFIDx»xDescription»nDPRatexx",
            "a.sVhclFIDx»CONCAT(f.sDescript,' ', e.sDescript, ' ', c.sDescript, ' ', g.sDescript, ' ', c.nYearMdlx, ' ',d.sTransmss )»a.nDPRatexx",
            byCode ? 0 : 1);
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "success");
            poJSON.put("sVrntIDxx", value);
            poJSON.put("nDPRatexx", 15.00); //for testing only
            poJSON.put("nRsrvAmtx", 10000.00); //for testing only
        }
        if (poJSON != null) {
            String lsVariantId = (String) poJSON.get("sVrntIDxx");
            Double ldblDPRate = Double.valueOf((String) poJSON.get("nDPRatexx"));
            Double ldblRSVAmt =  Double.valueOf((String) poJSON.get("nRsrvAmtx"));
            Double ldblSRPAmt =  Double.valueOf((String) poJSON.get("nSRPAmntx"));
            Detail(row).setVariantId(lsVariantId);
            Detail(row).setDownPaymentPercent(ldblDPRate);
            Detail(row).setReservationAmount(ldblRSVAmt);
            Detail(row).setSRPAmount(ldblSRPAmt); 
            
            poJSON = AutoPopulateVehicleAddOn(lsVariantId);
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }
            
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
        return poJSON;
    }
    
    public JSONObject AutoPopulateVehicleAddOn(String fsVariantId) throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        String lsSQL = VehicleAddOn_SQL();
        lsSQL = MiscUtil.addCondition(lsSQL, SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate "
                                    + " AND b.sVrntIDxx = " + SQLUtil.toSQL(fsVariantId)
                                    + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                    );
        System.out.println("AutoLoadVehicleAddOn SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        ReloadAddOnList();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while(loRS.next()){
                String lsAddOnId = loRS.getString("sAddOnIDx");
                String lsAddOnName = loRS.getString("sAddTypNm");
                Double ldblAmount = loRS.getDouble("nAmountxx");
                
                if(lsAddOnName.contains("LTO")) {
                    Detail(0).setLTOAmount(ldblAmount);
                } else if(lsAddOnName.contains("TPL")){
                    Detail(0).setTPLAmount(ldblAmount);
                }else if(lsAddOnName.contains("Comprehensive")){
                    Detail(0).setComprehensiveAmount(ldblAmount);
                } else {
                    AddOnList(getAddOnListCount() - 1).setAddOnId(lsAddOnId);
                    AddOnList(getAddOnListCount() - 1).setAmount(ldblAmount);
                    AddOnList(getAddOnListCount() - 1).setQuantity(1);
                    ReloadAddOnList();
                }
            }
        }
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public JSONObject SearchVehicleAddOn(String value, boolean byCode, int row, boolean isAddOn)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        String lsSQL = VehicleAddOn_SQL();
        lsSQL = MiscUtil.addCondition(lsSQL, SQLUtil.toSQL(Master().getTransactionDate()) + " BETWEEN a.dThruDate AND a.dThruDate ");
        if(isAddOn){
            lsSQL = lsSQL + " AND c.cSourceTx != '0'"; //not accessories
        }
        
        System.out.println("Executing SQL: " + lsSQL);
        
        poJSON = ShowDialogFX.Browse(poGRider,
        lsSQL,
        value,
        "AddOn ID»Description»Amount",
        "sAddOnIDx»sAddTypNm»nAmountxx",
        "a.sAddOnIDx»c.sAddTypNm»a.nAmountxx",
        byCode ? 0 : 1);
        if (poJSON != null) {
            String lsAddOnId = (String) poJSON.get("sAddOnIDx");
            Double lsAmount = (Double) poJSON.get("nAmountxx");
            poJSON = checkExistingAddOn(lsAddOnId, row);
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }
            row = (int) poJSON.get("row");
            AddOnList(row).setAddOnId(lsAddOnId);
            AddOnList(row).setAmount(lsAmount);
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
        return poJSON;
    }

    private JSONObject checkExistingAddOn(String fsStockId, int row){
        poJSON.put("row", row);
        for(int lnCtr = 0;lnCtr < getAddOnListCount(); lnCtr++){
            if(fsStockId.equals(AddOnList(lnCtr).getAddOnId())
                && row != lnCtr){
                if(!AddOnList(lnCtr).getRecordStatus()){
                    AddOnList(lnCtr).setRecordStatus(true);
                    poJSON.put("row", lnCtr);
                    return poJSON;
                } else {
                    poJSON = setJSON("error", "Selected Add-On already exist.");
                    poJSON.put("row", lnCtr);
                    return poJSON;
                }
            }
        }
            
        poJSON.put("row", row);
        return poJSON;
    }

    public void ReloadAddOnList() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getAddOnListCount() - 1;
        while (lnCtr >= 0) {
            if ((AddOnList(lnCtr).getAddOnId() == null || "".equals(AddOnList(lnCtr).getAddOnId()))
                ) {
                paAddOn.remove(lnCtr);
            } 
            lnCtr--;
        }

        if ((getAddOnListCount() - 1) >= 0) {
            if (
                (AddOnList(getAddOnListCount() - 1).getAddOnId() != null && !"".equals(AddOnList(getAddOnListCount() - 1).getAddOnId()))
                && AddOnList(getAddOnListCount() - 1).getAmount() > 0.0000
                ) {
                AddAddOn();
            }
        }

        if ((getAddOnListCount() - 1) < 0) {
            AddAddOn();
        }
    }

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
    
    @Override
    public Model_Sales_Quotation_Master Master() {
        return (Model_Sales_Quotation_Master) poMaster;
    }
    
    @Override
    public Model_Sales_Quotation_Vehicle Detail(int row) {
        return (Model_Sales_Quotation_Vehicle) paDetail.get(row);
    }
    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (getDetailCount() > 0) {
            if ((Detail(getDetailCount() - 1).getVariantId() == null || "".equals(Detail(getDetailCount() - 1).getVariantId()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }

        return addDetail();
    }
    
    public JSONObject AddAddOn() throws CloneNotSupportedException {
        if (getAddOnListCount() > 0) {
            if ((AddOnList(getAddOnListCount() - 1).getAddOnId() == null || "".equals(AddOnList(getAddOnListCount() - 1).getAddOnId()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }
        
        Model_Quotation_AddOn loModel = new SalesModels(poGRider).QuotationAddOn();
        loModel.newRecord();
        paAddOn.add(loModel);
        poJSON.put("result", "success");
        return poJSON;
    }

    public Model_Quotation_AddOn AddOnList(int row) {
        return (Model_Quotation_AddOn) paAddOn.get(row);
    }

    public int getAddOnListCount() {
        if(paAddOn == null){
            return 0;
        }
        return paAddOn.size();
    }
    
    public void ReloadDetail() throws CloneNotSupportedException, SQLException, GuanzonException{
//        int lnCtr = getDetailCount() - 1;
//        while (lnCtr >= 0) {
//            if ((Detail(lnCtr).getAddOnId() == null || "".equals(Detail(lnCtr).getAddOnId()))
//                ) {
//                paAddOn.remove(lnCtr);
//            } 
//            lnCtr--;
//        }
//
//        if ((getDetailCount() - 1) >= 0) {
//            if (
//                (Detail(getDetailCount() - 1).getAddOnId() != null && !"".equals(Detail(getDetailCount() - 1).getAddOnId()))
//                && Detail(getDetailCount() - 1).getAmount() > 0.0000
//                ) {
//                AddDetail();
//            }
//        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
    }
    
    public JSONObject remake(String fsTransNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException, ScriptException, ParseException{
        poJSON = new JSONObject();
        
        VehicleQuotation loObj = new SalesControllers(poGRider, logwrapr).VehicleQuotation();
        loObj.InitTransaction();
        poJSON = loObj.OpenTransaction(fsTransNo);
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        poJSON = NewTransaction();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        for(int lnCtr = 0;lnCtr < loObj.getDetailCount(); lnCtr++){
            if(loObj.Detail(lnCtr).getRecordStatus()){
                Detail(getDetailCount()-1).setVariantId(loObj.Detail(lnCtr).getVariantId());
                Detail(getDetailCount()-1).setSRPAmount(loObj.Detail(lnCtr).getSRPAmount());
                //
            }
        }
        
        return setJSON("success", "success");
    }
    
    public void computeFields() throws SQLException, GuanzonException{
        Double ldblDownpaymentAmount = 0.00;
        Double ldblBalance = 0.00;
        int lnDuration = Detail(0).getDuration();
        Double ldblMontlyAmortizationAmt = 0.00;
        Double ldblInterestRate = Detail(0).getRateValue();
        Double ldblSRPAmt = Detail(0).getSRPAmount();
        Double ldblDPAmt = Detail(0).getDownPayment();
        Double ldblRSVAmt = Detail(0).getReservationAmount();
        Double ldblTPLAmt = Detail(0).getTPLAmount();
        Double ldblCompAmt = Detail(0).getComprehensiveAmount();
        Double ldblLTOAmt = Detail(0).getLTOAmount();
        Double ldblCHMOAmt = Detail(0).getCHMOAmount();
        Double ldblTotalAddOn = 0.0000;
        Double ldblTranTotal = 0.0000;
        Double ldblTotalDisc = 0.0000;
        
        for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
            if(checkNotEmpty(AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName())){
                if(AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName().contains("LTO")
                    || AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName().contains("TPL")
                    || AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName().contains("Comprehensive")
                ){
                    continue;
                }
                
                ldblTotalAddOn = ldblTotalAddOn + AddOnList(lnCtr).getAmount();
            }
        }
        
        ldblTranTotal = ldblDPAmt + ldblRSVAmt + ldblTPLAmt + ldblCompAmt + ldblLTOAmt + ldblCHMOAmt + ldblTotalAddOn;
        Detail(0).setTransactionTotal(ldblTranTotal);
        ldblTotalDisc = Detail(0).getDiscountAmount() + Detail(0).getAdditionalDiscountAmount();
        Detail(0).setTotalAmount(ldblTranTotal - ldblTotalDisc);
        
        //Financing Computation
        /**
         * SRP x Downpayment Rate = Downpayment Amount
         * SRP - Downpayment Amount = Balance
         * Balance x Interest Rate / Duration = Monthly Amortization Amount
         */
        if(ldblInterestRate > 0 && lnDuration > 0){
            ldblDownpaymentAmount = Detail(0).getSRPAmount() * (Detail(0).getDownPaymentPercent()/100);
            ldblBalance = Detail(0).getSRPAmount() - ldblDownpaymentAmount;
            ldblInterestRate = ((ldblInterestRate/100)+1);
            ldblMontlyAmortizationAmt = ((ldblBalance * ldblInterestRate) / lnDuration)+2;//((ldblBalance * ldblInterestRate) / lnDuration)+2 = (double) NaN

            String lsDecimalFormat = "###0.00";
            DecimalFormat format = new DecimalFormat(lsDecimalFormat);
            ldblMontlyAmortizationAmt = Double.parseDouble(format.format(ldblMontlyAmortizationAmt));
            ldblMontlyAmortizationAmt = Double.parseDouble(String.valueOf(Math.round(ldblMontlyAmortizationAmt)));

            Detail(0).setFinancingAmount(ldblSRPAmt-ldblDPAmt);
            Detail(0).setMonthlyAmount(ldblMontlyAmortizationAmt);
            Detail(0).setGrossMonthlyInstallment(ldblMontlyAmortizationAmt-Detail(0).getPromptPaymentDiscount());
            Detail(0).setPromissoryNoteAmt(Detail(0).getDuration() * ldblMontlyAmortizationAmt);
        }
    
    }
    
    @Override
    public JSONObject initFields() {
        poJSON = new JSONObject();
        
        try {
            //Put initial model values here/
//            Master().setCompanyId(psCompanyId);
//            Master().setFromDate(SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            Date ldServerDate = poGRider.getServerDate();
            Calendar loFromDate = Calendar.getInstance();
            Calendar loToDate = Calendar.getInstance();
            loFromDate.setTime(ldServerDate);
            loToDate.setTime(ldServerDate);

            // Set to the last day of the current month
            loFromDate.set(Calendar.DAY_OF_MONTH,loFromDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            loToDate.set(Calendar.DAY_OF_MONTH,loToDate.getActualMaximum(Calendar.DAY_OF_MONTH));

            Master().setTransactionDate(SQLUtil.toDate(xsDateShort(ldServerDate),SQLUtil.FORMAT_SHORT_DATE));
            Detail(0).setExpectedDate(SQLUtil.toDate(xsDateShort(loToDate.getTime()),SQLUtil.FORMAT_SHORT_DATE));
            Detail(0).setBranchCode(poGRider.getBranchCode());
        } catch (SQLException ex) {
            Logger.getLogger(VehicleQuotation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
//        GValidator loValidator = new ValidityMasterValidator();
//        loValidator.setApplicationDriver(poGRider);
//        loValidator.setTransactionStatus(status);
//        loValidator.setMaster(Master());
//        poJSON = loValidator.validate();
        return poJSON;
    }
    
    private boolean validateStandardDetails(int row){
        try {
            Model_Financing_Rate_Master lObj = new SalesModels(poGRider).FinancingRateMaster();
            String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(lObj),
                                                    " cRecdStat = " + SQLUtil.toSQL(FinancingRateStatus.ACTIVE)
//                                                    + " AND sBankIDxx = " + SQLUtil.toSQL("")
                                                    + " AND nDuration = " + SQLUtil.toSQL(Detail(row).getDuration())
                                                    + " AND nRateValx = " + SQLUtil.toSQL(Detail(row).getRateValue())
//                                                    + " AND sCompnyID = " + SQLUtil.toSQL(Detail(row).Branch().getCompanyId())
                                                    );
            System.out.println("validateStandardDetails SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(loRS.next()){
                    return false;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } 
            
        return true;
    }
    
    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        /*Put system validations and other assignments here*/
        System.out.println("Class Edit Mode : " + getEditMode());
        System.out.println("Master Edit Mode : " + Master().getEditMode());
        
        //Re-set the transaction no 
        if(getEditMode() == EditMode.ADDNEW){
            Master().setTransactionNo(Master().getNextCode());
        }

        poJSON = isEntryOkay(Master().getTransactionStatus());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(validateStandardDetails(0)){ //if true require approve upon saving
            if(pbWithUI){
                poJSON = callApproval();
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            }
        }
        
        Iterator<Model> detail = Detail().iterator();
        int lnDetailRow = 1;
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sVrntIDxx");
//            double ldblSRP = Double.parseDouble(String.valueOf(item.getValue("nSRPAmntx")));
            
            if ((lsDetail == null || "".equals(lsDetail))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    detail.remove(); // Correctly remove the item
                } 
            } else {
                lnDetailRow++;
            }
        }

        if (getDetailCount() <= 0) {
            poJSON = setJSON("error", "No record detail to be save.");
            return poJSON;
        }
        
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            if(Detail(lnCtr).getSRPAmount() == 0.0000){
                return setJSON("error", "Invalid SRP amount.");
            }
            
            System.out.println("Record Status : " + Detail(lnCtr).getRecordStatus());
            if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                Detail(lnCtr).setQuotationPaymentId(Detail(lnCtr).getNextCode());
            }
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        //Vehicle Quotation Add ON
        Iterator<Model_Quotation_AddOn> addon = paAddOn.iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sAddOnIDx");
            double ldblSRP = Double.parseDouble(String.valueOf(item.getValue("nSRPAmntx")));
            
            if ((lsDetail == null || "".equals(lsDetail))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    detail.remove(); // Correctly remove the item
                } 
            } else {
                lnDetailRow++;
            }
        }

        for (int lnCtr = 0; lnCtr <= getAddOnListCount() - 1; lnCtr++) {
            if(checkEmpty(AddOnList(lnCtr).getAddOnId())){
                continue;
            }
            if(AddOnList(lnCtr).getAmount() == 0.0000){
                return setJSON("error", "Invalid amount at row "+(lnCtr+1)+".");
            }
            
            AddOnList(lnCtr).setTransactionNo(Master().getTransactionNo());
            AddOnList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            AddOnList(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        //Recompute
        computeFields();
        
        Master().setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    @Override
    protected JSONObject saveOthers() {
        try {
            poJSON = new JSONObject();
            for (int lnCtr = 0; lnCtr <= getAddOnListCount() - 1; lnCtr++) {
                if(checkEmpty(AddOnList(lnCtr).getAddOnId())){
                    continue;
                }
                if(AddOnList(lnCtr).getEditMode() == EditMode.ADDNEW || AddOnList(lnCtr).getEditMode() == EditMode.UPDATE){
                    if(AddOnList(lnCtr).getEditMode() == EditMode.ADDNEW){
                        AddOnList(lnCtr).setQuotationAddOnId(AddOnList(lnCtr).getNextCode());
                    }
                    poJSON = AddOnList(lnCtr).saveRecord();
                    if(!isJSONSuccess(poJSON)){
                        return poJSON;
                    }
                }
            }
            
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON = setJSON("error", MiscUtil.getException(ex));
            return poJSON;
        } 
        
        return poJSON;
    }
    
    
    @Override
    public JSONObject save() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put saving business rules here*/
        return isEntryOkay(VehicleQuotationStatus.OPEN);
    }

    @Override
    public void initSQL() {
        String lsCondition = "";
        
        if(psTranStat != null && !"".equals(psTranStat)){
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }

                lsCondition = "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        SQL_BROWSE ="SELECT " +
                " a.sTransNox, " +
                " a.sIndstCdx, " +
                " a.sCategrCd, " +
                " a.dTransact, " +
                " a.sClientID, " +
                " a.sAddrssID, " +
                " a.sContctID, " +
                " a.nVersionx, " +
                " a.cTranStat, " +
                " a.sModified, " +
                " a.dModified, " +
                "     CASE  " +
                "         WHEN a.cTranStat = '0'  " +
                "         THEN 'OPEN'  " +
                "         WHEN a.cTranStat = '1'  " +
                "         THEN 'APPROVED'  " +
                "         WHEN a.cTranStat = '2'  " +
                "         THEN 'CANCELLED'  " +
                "         WHEN a.cTranStat = '3'  " +
                "         THEN 'VOIDED'  " +
                "         ELSE 'UNKNOWN' " +
                "     END AS xTranStat, " +
                "  b.sVrntIDxx, " +
                "  b.nSRPAmntx, " +
                "  b.sModified," +
                "  b.dModified " +
                "FROM Sales_Quotation_Master a " +
                "INNER JOIN Sales_Quotation_Vehicle b ON b.sTransNox = a.sTransNox " +
                "LEFT JOIN Client_Master c ON c.sClientID = a.sClientID "; 
        if(lsCondition != null && !"".equals(lsCondition)){
            SQL_BROWSE = MiscUtil.addCondition(SQL_BROWSE, lsCondition);
        }
    }
    
    private String VehicleFinancing_SQL(){
        return  " SELECT                   "
            + " a.sValidIDx              "
            + " , a.sCompnyID            "
            + " , a.dFromDate            "
            + " , a.dThruDate            "
            + " , a.sValidDsc            "
            + " , a.cRecdStat            "
            + " , b.sVhclFIDx            "
            + " , b.nDPRatexx            "
            + " , b.nSRPAmntx            "
            + " , b.nRsrvAmtx            "
            + " , b.sVrntIDxx            "
            + " , c.sDescript AS xVariant"
            + " , e.sDescript AS xModel  "
            + " , f.sDescript AS xBrand  "
            + " , g.sDescript AS xColor  "
            + " , c.nYearMdlx            "
            + " , d.sTransmss            "
            + " , d.sBodyType            "
            + " , d.sVhclType            "
            + " , d.nAuthCapx            "
            + " ,  CONCAT(f.sDescript,' ', e.sDescript, ' ', c.sDescript, ' ', g.sDescript, ' ', c.nYearMdlx, ' ',d.sTransmss ) AS xDescription "
            + " FROM Validity_Period_Master a  "
            + " INNER JOIN Vehicle_Financing_Price b ON b.sValidIDx = a.sValidIDx "
            + " LEFT JOIN Model_Variant c ON c.sVrntIDxx = b.sVrntIDxx            "
            + " LEFT JOIN Model_Variant_Insurance d ON d.sVrntIDxx = b.sVrntIDxx  "
            + " LEFT JOIN Model e ON e.sModelIDx = c.sModelIDx                    "
            + " LEFT JOIN Brand f ON f.sBrandIDx = e.sBrandIDx                    "
            + " LEFT JOIN Color g ON g.sColorIDx = c.sColorIDx                    ";
    }
    
    private String VehicleAddOn_SQL(){
        return "   SELECT      " 
                + "   a.sValidIDx " 
                + " , a.sCompnyID " 
                + " , a.dFromDate " 
                + " , a.dThruDate " 
                + " , a.sValidDsc " 
                + " , a.cRecdStat " 
                + " , b.sAddOnIDx "
                + " , b.sAddTypex "
                + " , b.nAmountxx "
                + " , b.sVrntIDxx "
                + " , c.sAddTypNm "
                + " FROM Validity_Period_Master a " 
                + " INNER JOIN Vehicle_AddOn_Master b ON b.sValidIDx = a.sValidIDx "  
                + " LEFT JOIN Vehicle_AddOn_Type c ON c.sAddTypex = b.sAddTypex    ";
    }
    
     public Model_Branch Branch() throws SQLException, GuanzonException {
        Model_Branch loModel = new ParamModels(poGRider).Branch();
        loModel.initialize();
        
        if (!"".equals(poGRider.getBranchCode())) {
            if (loModel.getEditMode() == EditMode.READY && loModel.getBranchCode().equals(poGRider.getBranchCode())) {
                return loModel;
            } else {
                poJSON = loModel.openRecord(poGRider.getBranchCode());

                if ("success".equals((String) poJSON.get("result"))) {
                    return loModel;
                } else {
                    loModel.initialize();
                    return loModel;
                }
            }
        } else {
            loModel.initialize();
            return loModel;
        }
    }
    
    public String BranchEmail() throws SQLException{
        String lsEmail = "";
        String lsSQL =   " SELECT "
                    + "     sBranchCD"
                    + " ,   sEMailAdd"
                    + " FROM Branch_Email  "
                    + " WHERE sBranchCD = " + SQLUtil.toSQL(poGRider.getBranchCode());
//                    + " AND a.cRectStat = '1' ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) >= 0) {
            while (loRS.next()) {
                lsEmail = loRS.getString("sEMailAdd");
            }
            MiscUtil.close(loRS);
        }
        
        return lsEmail;
    }
    
    public String BranchBrand() throws SQLException{
        String lsDesc = "";
        String lsSQL =   " SELECT "
                    + "     a.sBranchCD "
                    + " ,   a.sBrandIDx "
                    + " ,   b.sDescript "
                    + " FROM Branch_Others a "
                    + " LEFT JOIN Brand b ON b.sBrandIDx = a.sBrandIDx "
                    + " WHERE sBranchCD = " + SQLUtil.toSQL(poGRider.getBranchCode());
//                    + " AND a.cRectStat = '1' ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) >= 0) {
            while (loRS.next()) {
                lsDesc = loRS.getString("sDescript");
            }
            MiscUtil.close(loRS);
        }
        
        return lsDesc;
    }
//    public JSONObject printTransaction()
//        throws CloneNotSupportedException, SQLException, GuanzonException {
//
//    poJSON = new JSONObject();
//    pbIsPrinted = false;
//    
//    try {
//
//        String jrxmlPath =
//                System.getProperty("sys.default.path.config")
//                + "/Reports/VehicleSalesQuotation.jrxml";
//
//        JasperReport jasperReport =
//                JasperCompileManager.compileReport(jrxmlPath);
//        String lsWatermarkPath = System.getProperty("sys.default.path.config") + "/Reports/images/";
//        String lsBrand = BranchBrand();
//        
//        if(lsBrand == null || "".equals(lsBrand)){
//            lsWatermarkPath = lsWatermarkPath + "anyauto.png" ;
//        } else {
//            if(lsBrand.toLowerCase().contains("honda")){
//                lsWatermarkPath = lsWatermarkPath + "honda.png" ;
//            } else if(lsBrand.toLowerCase().contains("nissan")){
//                lsWatermarkPath = lsWatermarkPath + "nissan.png" ;
//            } else if(lsBrand.toLowerCase().contains("geely")){
//                lsWatermarkPath = lsWatermarkPath + "geely.png" ;
//            }  else if(lsBrand.toLowerCase().contains("gac")){
//                lsWatermarkPath = lsWatermarkPath + "gacmotor.png" ;
//            } else {
//                lsWatermarkPath = lsWatermarkPath + "anyauto.png" ;
//            }
//        }
//        
//        /*
//         * Create the final JasperPrint.
//         * The first rate will initialize it.
//         */
//        JasperPrint finalPrint = null;
//
//        // ---------------------------------------------
//        // Parameters
//        // ---------------------------------------------
//        Map<String, Object> parameters = new HashMap<>();
//
//
//        parameters.put("watermarkImagePath", lsWatermarkPath);
//        parameters.put("sValidity",Master().getTransactionNo()+ "-"+ poGRider.getServerDate());
//        parameters.put("sCompany",Master().Company().getCompanyName().toUpperCase());
//        String lsAddress = Master().Company().getCompanyAddress();
//        if (Master().Company().TownCity().getDescription() != null && !"".equals(Master().Company().TownCity().getDescription())) {
//            lsAddress = lsAddress + " " + Master().Company().TownCity().getDescription();
//        }
//
//        if (Master().Company().TownCity().Province().getDescription() != null
//                && !"".equals(Master().Company().TownCity().Province().getDescription())) {
//            lsAddress = lsAddress+ ", "+ Master().Company().TownCity().Province().getDescription();
//        }
//
//        parameters.put("sAddress",lsAddress.toUpperCase());
//
//        Model_Branch loModel = Branch();
//        String lsBranchName = loModel.getBranchName();
//        String lsBranchDesc = loModel.getDescription();
//        String lsBranchAddress = loModel.getAddress();
//        String lsEmail = BranchEmail();
//        if (loModel.TownCity().getDescription() != null && !"".equals(loModel.TownCity().getDescription())) {
//            lsBranchAddress = lsBranchAddress + " " + loModel.TownCity().getDescription();
//        }
//
//        if (loModel.TownCity().Province().getDescription() != null && !"".equals(loModel.TownCity().Province().getDescription())) {
//            lsBranchAddress = lsBranchAddress+ ", "+ loModel.TownCity().Province().getDescription();
//        }
//        String lsContact = loModel.getMobile();
//        String lsLandLine = loModel.getLandLine();
//        if(lsBranchName == null) { lsBranchName = "";}
//        if(lsBranchDesc == null) { lsBranchDesc = "";}
//        if(lsBranchAddress == null) { lsBranchAddress = "";}
//        if(lsContact == null) { 
//            lsContact = "";
//        } else {
//            if(!lsContact.isEmpty()){
//                lsContact = "Mobile : " + lsContact;
//            }
//        }
//        if(lsLandLine == null) {
//            lsLandLine = "";
//        } else {
//            if(!lsContact.isEmpty()){
//                lsContact = lsContact 
//                            + "\nTel No : " + lsLandLine;
//            }
//        }
//        if(lsEmail == null) {
//            lsEmail = "";
//        } else {
//            if(!lsEmail.isEmpty()){
//                lsContact = lsContact + "\nEmail Address : " + lsEmail;
//            } 
//        }
//
//        parameters.put("sBranch",lsBranchName.toUpperCase());
//        parameters.put("sBranchAddress",lsBranchAddress.toUpperCase());
//        parameters.put("sBranchDesc",lsBranchDesc.toUpperCase());
//        parameters.put("sContact",lsContact);
//        parameters.put("validFrom",SQLUtil.dateFormat(Master().getTransactionDate(),SQLUtil.FORMAT_LONG_DATE));
//        String lsToDate = "";
//        if(Detail(0).getValidThruDate() == null){
//            lsToDate = "Present";
//        } else {
//            lsToDate = SQLUtil.dateFormat(Detail(0).getValidThruDate(),SQLUtil.FORMAT_LONG_DATE);
//        }
//        parameters.put("validTo",lsToDate);
//        // ---------------------------------------------
//        // Build data for current DP rate
//        // ---------------------------------------------
//        List<Map<String, Object>> rows = buildSampleData();
//        List<Map<String, ?>> data = new ArrayList<>(rows);
//        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data);
//
//        // ---------------------------------------------
//        // Generate JasperPrint
//        // ---------------------------------------------
//
//        JasperPrint currentPrint =
//                JasperFillManager.fillReport(
//                        jasperReport,
//                        parameters,
//                        dataSource
//                );
//
//        // ---------------------------------------------
//        // Combine reports
//        // ---------------------------------------------
//
//        if (finalPrint == null) {
//            finalPrint = currentPrint;
//        } else {
//
//            finalPrint.getPages().addAll(
//                    currentPrint.getPages()
//            );
//        }
//
//        // ---------------------------------------------
//        // Display final report
//        // ---------------------------------------------
//
//        if (finalPrint != null) {
//            CustomJasperViewer viewer = new CustomJasperViewer(finalPrint);
//            viewer.setVisible(true);
//            viewer.addWindowListener(
//                    new WindowAdapter() {
//                        @Override
//                        public void windowClosed(WindowEvent e) {
//                            proceedAfterViewerClosed();
//                        }
//                    }
//            );
//        }
//
//    } catch (JRException | SQLException | GuanzonException ex) {
//        poJSON.put("result", "error");
//        poJSON.put("message","Transaction print aborted!");
//        Logger.getLogger(getClass().getName())
//                .log(Level.SEVERE, null, ex);
//    }
//
//    return poJSON;
//}
//    
//    private void proceedAfterViewerClosed() {
//        Platform.runLater(() -> {
//            System.out.println("SHOWED!!!!!!!!!!!");
//            if ("error".equals((String) poJSON.get("result"))) {
//                ShowMessageFX.Warning(null, "Computerized Accounting System",
//                        (String) poJSON.get("message"));
//            } else {
//                if (pbIsPrinted) {
//                    ShowMessageFX.Information(null, "Computerized Accounting System",
//                            "Vehicle Price List Printed Successfully");
//                } else {
//                    ShowMessageFX.Warning(null, "Computerized Accounting System",
//                            "Printing was canceled by the user.");
//                }
//            }
//        });
//    }
//
//    private boolean pbIsPrinted = false;
//    public class CustomJasperViewer extends JasperViewer {
//
//        public CustomJasperViewer(final JasperPrint jasperPrint) {
//            super(jasperPrint, false);
//            customizePrintButton(jasperPrint);
//        }
//
//        /* ---- toolbar patch ------------------------------------------ */
//        private JSONObject customizePrintButton(final JasperPrint jasperPrint) {
//
//            try {
//                JRViewer viewer = findJRViewer(this);
//                if (viewer == null) {
//                    poJSON.put("result", "error");
//                    poJSON.put("message", "JRViewer not found!");
//                    return poJSON;
//                }
//                for (int i = 0; i < viewer.getComponentCount(); i++) {
//                    if (viewer.getComponent(i) instanceof JRViewerToolbar) {
//
//                        JRViewerToolbar toolbar = (JRViewerToolbar) viewer.getComponent(i);
//
//                        for (int j = 0; j < toolbar.getComponentCount(); j++) {
//                            if (toolbar.getComponent(j) instanceof JButton) {
//
//                                final JButton button = (JButton) toolbar.getComponent(j);
//
//                                if ("Print".equals(button.getToolTipText())) {
//                                    /* remove existing handlers */
//                                    ActionListener[] old = button.getActionListeners();
//                                    for (int k = 0; k < old.length; k++) {
//                                        button.removeActionListener(old[k]);
//                                    }
//
//                                    /* add our own (anonymous inner‑class, not lambda) */
//                                    button.addActionListener(new ActionListener() {
//                                        @Override
//                                        public void actionPerformed(ActionEvent e) {
//                                            try {
//                                                pbIsPrinted = JasperPrintManager.printReport(jasperPrint, true);
//                                                if (pbIsPrinted) {
//                                                    CustomJasperViewer.this.dispose();
//                                                } else {
//                                                    poJSON.put("result", "error");
//                                                    poJSON.put("message",  "Printing was canceled by the user.");
//                                                }
//                                            } catch (JRException ex) {
//                                                poJSON.put("result", "error");
//                                                poJSON.put("message",  "Print Failed: " + ex.getMessage());
//                                                Logger.getLogger(getClass().getName()).log(Level.SEVERE,  ex.getMessage(), ex);
//                                            }
//                                        }
//                                    });
//                                } else {
//                                    // Disable all other buttons
////                                    button.setEnabled(false);
////                                    poJSON.put("result", "error");
////                                    poJSON.put("message",  "Transaction print aborted!");
//                                }
//                            }
//                        }
//                        toolbar.revalidate();
//                        toolbar.repaint();
//                    }
//                }
//            } catch (Exception e) {
//                System.out.println("Error customizing print button: " + e.getMessage());
//                poJSON.put("result", "error");
//                poJSON.put("message", "Error customizing print button: " + e.getMessage());
//            }
//
//            return poJSON;
//        }
//
//        private JRViewer findJRViewer(Component parent) {
//            if (parent instanceof JRViewer) {
//                return (JRViewer) parent;
//            }
//            if (parent instanceof Container) {
//                Component[] comps = ((Container) parent).getComponents();
//                for (int i = 0; i < comps.length; i++) {
//                    JRViewer v = findJRViewer(comps[i]);
//                    if (v != null) {
//                        return v;
//                    }
//                }
//            }
//            return null;
//        }
//    }
//    private List<Map<String, Object>> buildSampleData() {
//        List<Map<String, Object>> rows = new ArrayList<>();
//
//        try{
//            //Group by dp rate 
//            int lnRow = 0;
//            for(int lnCtr = 0;lnCtr < getDetailCount();lnCtr++){
//                if(Detail(lnCtr).getRecordStatus()){
//                    lnRow++;
//                    String lsVariant = Detail(lnCtr).ModelVariant().getDescription();
//                        if(Detail(lnCtr).ModelVariant().Color().getDescription() != null && !"".equals(Detail(lnCtr).ModelVariant().Color().getDescription())){
//                            lsVariant = ""; //lsVariant + " " +  Detail(lnCtr).ModelVariantInsurance().getTransmission() + " " + Detail(lnCtr).ModelVariant().Color().getDescription() ;
//                        }
//                        addRow(rows
//                                , Detail(lnCtr).ModelVariant().Model().Brand().getDescription()
//                                , Detail(lnCtr).ModelVariant().Model().getDescription()
//                                , lsVariant
//                                , Detail(lnCtr).getSRPAmount()
//                                ,lnRow
//                                , 0
//                        );
//                }
//            }
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(VehicleQuotation.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return rows;
//    }
// 
//    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
//    private void addRow(List<Map<String, Object>> rows,
//                                String brand, String model, String variant,
//                                double srpAmt,int nRow,int priceYr) {
//        Map<String, Object> row = new LinkedHashMap<>();
//        if(brand == null){ brand = "";}
//        if(model == null){ model = "";}
//        if(variant == null){ variant = "";}
//        row.put("sBrand", brand.toUpperCase());
//        row.put("sModel", model.toUpperCase());
//        row.put("sVariant", variant.toUpperCase());
//        row.put("nSRPAmount", srpAmt);
//        row.put("nRow", nRow);
//        row.put("npriceYr", priceYr);
//        rows.add(row);
//    }
//    
    public JSONObject getUpdateStatusBy(String fsStatus) throws SQLException, GuanzonException {
        String lsUpdateBy = "";
        String lsDate = "";
        String lsSQL = "SELECT b.sModified,b.dModified FROM "+Master().getTable()+" a "
                     + " LEFT JOIN Transaction_Status_History b ON b.sSourceNo = a.sTransNox AND b.sTableNme = "+ SQLUtil.toSQL(Master().getTable())
                     + " AND b.cRefrStat = "+ SQLUtil.toSQL(fsStatus) ;
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo())) ;
        lsSQL = lsSQL + " ORDER BY b.dModified DESC ";
        System.out.println("Execute SQL STATUS : "+fsStatus+" : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                if(loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))){
                    if(loRS.getString("sModified").length() > 10){
                        lsUpdateBy = getSysUser(poGRider.Decrypt(loRS.getString("sModified")),false); 
                    } else {
                        lsUpdateBy = getSysUser(loRS.getString("sModified"),false); 
                    }
                    // Get the LocalDateTime from your result set
                    LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                    lsDate =  dModified.format(formatter);
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
        poJSON.put("sUpdateByx", lsUpdateBy);
        poJSON.put("sUpdateDte", lsDate);
        return poJSON;
    }
    
    protected CachedRowSet getStatusHistoryTest() throws SQLException {
        String lsSQL = "SELECT  a.sTableNme, a.sSourceNo, a.sRemarksx, a.cRefrStat cTranStat, IFNULL(c.sCompnyNm, '-') xModified, IFNULL(e.sCompnyNm, '-') xApproved, a.dModified, a.dApproved, a.sModified, a.sApproved " +
                    " FROM Transaction_Status_History a " +
                    "LEFT JOIN xxxSysUser b ON b.sUserIDxx = a.sModified " +
                    "LEFT JOIN Client_Master c ON b.sEmployNo = c.sClientID " +
                    "LEFT JOIN xxxSysUser d ON d.sUserIDxx = a.sApproved " +
                    "LEFT JOIN Client_Master e ON d.sEmployNo = e.sClientID " +
                    " WHERE a.sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo()) +
                    " AND a.sTableNme = " + SQLUtil.toSQL(Master().getTable()) + " ORDER BY a.dModified";
        System.out.println("STATUS HISTORY : " + lsSQL);
        ResultSet loRS = this.poGRider.executeQuery(lsSQL);
        RowSetFactory factory = RowSetProvider.newFactory();
        CachedRowSet rowset = factory.createCachedRowSet();
        rowset.populate(loRS);
        MiscUtil.close(loRS);
        return rowset;
    }
   
    @Override
    protected CachedRowSet getStatusHistory() throws SQLException {
        this.poGRider.ensureConnected();
        String lsSQL = "SELECT  a.sTableNme, a.sSourceNo, a.sRemarksx, a.cRefrStat cTranStat, IFNULL(c.sCompnyNm, '-') xModified, IFNULL(e.sCompnyNm, '-') xApproved, a.dModified, a.dApproved, a.sModified, a.sApproved "
                + " FROM GCASys_DBF.Transaction_Status_History a "
                + " LEFT JOIN GCASys_DBF.xxxSysUser b ON b.sUserIDxx = AES_DECRYPT(UNHEX(a.sModified), '08220326') "
                + " LEFT JOIN GGC_ISysDBF.Client_Master c ON b.sEmployNo = c.sClientID "
                + " LEFT JOIN GCASys_DBF.xxxSysUser d ON d.sUserIDxx = AES_DECRYPT(UNHEX(a.sApproved), '08220326') "
                + " LEFT JOIN GGC_ISysDBF.Client_Master e ON d.sEmployNo = e.sClientID "
                + " WHERE a.sSourceNo = " + SQLUtil.toSQL(Master().getTransactionNo()) + " AND a.sTableNme = " + SQLUtil.toSQL(Master().getTable()) + " ORDER BY a.dModified, a.sTransNox";
        ResultSet loRS = this.poGRider.executeQuery(lsSQL);
        RowSetFactory factory = RowSetProvider.newFactory();
        CachedRowSet rowset = factory.createCachedRowSet();
        rowset.populate(loRS);
        MiscUtil.close(loRS);
        return rowset;
    }

    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception {
        CachedRowSet crs;
        if(pbWithUI){
            crs = getStatusHistory();
        } else {
            crs = getStatusHistoryTest();
        }

        crs.beforeFirst();

        while (crs.next()) {
            switch (crs.getString("cRefrStat")) {
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case VehicleQuotationStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case VehicleQuotationStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case VehicleQuotationStatus.APPROVED:
                    crs.updateString("cRefrStat", "APPROVED");
                    break;
                case VehicleQuotationStatus.VOID:
                    crs.updateString("cRefrStat", "VOIDED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat) {
                        case VehicleQuotationStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case VehicleQuotationStatus.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case VehicleQuotationStatus.APPROVED:
                            crs.updateString("cRefrStat", "APPROVED");
                            break;
                        case VehicleQuotationStatus.VOID:
                            crs.updateString("cRefrStat", "VOIDED");
                            break;
                    }
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

        if(pbWithUI){
            showStatusHistoryUI("Vehicle Sales Quotation", (String) poMaster.getValue("sTransNox"), entryBy, entryDate, crs);
        }
    }
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM "+Master().getTable()+" a "
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
                            lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified")),false);
                        } else {
                            lsEntry = getSysUser(loRS.getString("sModified"),false);
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
            poJSON = setJSON("error", e.getMessage());
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }
    
    public String getSysUser(String fsId, boolean fbIsID) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL =   " SELECT b.sCompnyNm, a.sEmployNo from xxxSysUser a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId)) ;
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if(fbIsID) {
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
