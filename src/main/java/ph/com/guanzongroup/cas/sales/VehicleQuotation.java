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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscReplUtil;
import org.guanzon.appdriver.base.WebFile;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.token.RequestAccess;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Mail;
import org.guanzon.cas.client.model.Model_Client_Mobile;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.model.Model_Company;
import org.json.simple.parser.JSONParser;
import ph.com.guanzongroup.cas.sales.status.AddOnTypeStatus;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;
import ph.com.guanzongroup.cas.sales.status.FreebieStatus;
import ph.com.guanzongroup.cas.sales.status.SalesInquiryStatic;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;
import ph.com.guanzongroup.cas.sales.validator.VehicleQuotationValidator;

/**
 * @author Arsiela
 */
public class VehicleQuotation extends Transaction {
    public String psCompanyId = "";
    public String psIndustryId = "";
    public String psCategoryId = "";
    public String psClientName = "";
    public String psApprover = "";

    Model_Company poCompany;
    public List<Model_Quotation_AddOn> paAddOn;
    public List<Model_Quotation_Freebie> paFreebies;
    public List<Model_Quotation_Inclusion> paInclusion;
    public List<Model_Quotation_Special_Request> paSpecialRequest;
    public List<Model_Quotation_Discount> paDiscount;
    List<TransactionAttachment> paAttachments;
    List<TransactionAttachment> paVehicleAttachments;
    public List<Model> paMaster;
    
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "VQOT";

        poMaster = new SalesModels(poGRider).SalesQuotationMaster();
        poDetail = new SalesModels(poGRider).SalesQuotationVehicle();

        paAddOn = new ArrayList<>();
        paFreebies = new ArrayList<>();
        paInclusion = new ArrayList<>();
        paSpecialRequest = new ArrayList<>();
        paDiscount = new ArrayList<>();
        paMaster = new ArrayList<>();
        paAttachments = new ArrayList<>();
        paVehicleAttachments = new ArrayList<>();
        psApprover = "";
        setApproving("");
        return initialize();
    }

    @Override
    public String getSourceCode() { return SOURCE_CODE; }
    
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCategoryId(String categoryId) { psCategoryId = categoryId; }
    public void setClientName(String clientName) { psClientName = clientName; }
    public String getClientName() { return psClientName;}
    
    public Model_Company Company() throws SQLException, GuanzonException {
        if (poCompany == null) {
            poCompany = new ParamModels(poGRider).Company();
        }

        String id = poGRider.getCompnyId();

        if (!"".equals(id)) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals(id)) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poCompany;
                } else {
                    poCompany.initialize();
                    return poCompany;
                }
            }
        } else {
            poCompany.initialize();
            return poCompany;
        }
    }

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
    
    public void ResetTransaction() throws SQLException, GuanzonException {
        paDetail = new ArrayList<>();
        paAddOn = new ArrayList<>();
        paFreebies = new ArrayList<>();
        paInclusion = new ArrayList<>();
        paSpecialRequest = new ArrayList<>();
        paDiscount = new ArrayList<>();
        paMaster = new ArrayList<>();
        paAttachments = new ArrayList<>();
        paVehicleAttachments = new ArrayList<>();
        psApprover = "";
        setApproving("");
    }
    
    public JSONObject NewTransaction()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        if(!poGRider.getDepartment().equals(System.getProperty("sys.dept.sales"))
            && pbWithUI){ //BR: Authorized users from the Sales
            poJSON.put("result", "error" );
            poJSON.put("message", "User is not authorized to create the transaction." );
            return poJSON;
        }
        
        ResetTransaction();
        poJSON = newTransaction();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        ReloadDetail();
        ReloadAddOnList();
        ReloadFreebiesList();
        ReloadSpecialRequestList();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException {
        poJSON = new JSONObject();
        
        poJSON = openTransaction(transactionNo);
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        //load quotation addon
        poJSON = loadQuotationAddOn();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        //load quotation freebies
        poJSON = loadQuotationFreebies();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        //load quotation inclusion
        poJSON = loadQuotationInclusion();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        //load quotation discount
        poJSON = loadQuotationSpecialRequest();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        //load quotation discount
        poJSON = loadQuotationDiscount();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        //load quotation attachment
        if(VehicleQuotationStatus.APPROVED.equals(Master().getTransactionStatus())){
            poJSON = loadAttachments();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
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
            
            String sql = "SELECT sTransNox FROM " + this.poDetail.getTable() + " WHERE sTransNox = " + SQLUtil.toSQL(transactionNo) + " ORDER BY sTransNox";
            System.out.println("OPEN DETAIL : " + sql );
            ResultSet rs = this.poGRider.executeQuery(sql);

            while(rs.next()) {
                Model loDetail = (Model)this.poDetail.clone();
                loDetail.newRecord();
                this.poJSON = loDetail.openRecord( transactionNo);
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
        
        for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
            if(AddOnList(lnCtr).getEditMode() == EditMode.READY){
                poJSON = AddOnList(lnCtr).updateRecord();
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            }
        }
        for(int lnCtr = 0;lnCtr < getFreebiesListCount() ;lnCtr++){
            if(FreebiesList(lnCtr).getEditMode() == EditMode.READY){
                poJSON = FreebiesList(lnCtr).updateRecord();
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            }
        }
        for(int lnCtr = 0;lnCtr < getInclusionListCount();lnCtr++){
            if(InclusionList(lnCtr).getEditMode() == EditMode.READY){
                poJSON = InclusionList(lnCtr).updateRecord();
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            }
        }
        for(int lnCtr = 0;lnCtr < getSpecialRequestListCount();lnCtr++){
            if(SpecialRequestList(lnCtr).getEditMode() == EditMode.READY){
                poJSON = SpecialRequestList(lnCtr).updateRecord();
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            }
        }
        for(int lnCtr = 0;lnCtr < getDiscountListCount();lnCtr++){
            if(DiscountList(lnCtr).getEditMode() == EditMode.READY){
                poJSON = DiscountList(lnCtr).updateRecord();
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            }
        }
        
        if(VehicleQuotationStatus.APPROVED.equals(Master().getTransactionStatus())){
            poJSON = loadAttachments();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }
        
        ReloadAddOnList();
        ReloadFreebiesList();
        ReloadInclusionList();
        ReloadSpecialRequestList();
        ReloadDiscountList();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }
    
    @Override
    protected JSONObject saveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
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
//                                Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
//                                if(((Model)this.paDetail.get(lnCtr)).getEditMode() == EditMode.ADDNEW){
//                                    ((Model)this.paDetail.get(lnCtr)).setValue("sQotPayID", Detail(lnCtr).getNextCode());
//                                }
                                try {
                                    this.poJSON = ((Model)this.paDetail.get(lnCtr)).saveRecord();
                                } catch (SQLException | GuanzonException ex) {
                                    Logger.getLogger(VehicleQuotation.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                    if (!this.pbWthParent) {
                                        this.poGRider.rollbackTrans();
                                    }
                                    this.poJSON.put("result", "error");
                                    this.poJSON.put("message", MiscUtil.getException(ex));
                                    return this.poJSON;
                                }
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
            if(validateStandardDetails()){ //require approval when result is true
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
    
    public JSONObject ConfirmTransaction() throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        String lsStatus = VehicleQuotationStatus.CONFIRMED;
        
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
            poJSON = setJSON("error", "Transaction was already confirmed.");
            return poJSON;
        }
        
        boolean lbHasAttachment = false;
        for(int lnCtr = 0; lnCtr < getTransactionAttachmentCount();lnCtr++){
            if(!lbHasAttachment){
                lbHasAttachment = RecordStatus.ACTIVE.equals(TransactionAttachmentList(lnCtr).getModel().getRecordStatus());
            }
        }
        
        if (!lbHasAttachment) {
            poJSON = setJSON("error", "Client confirmation attachment is required to confirm the quotation.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if(!pbWthParent){
            psApprover = poGRider.getUserID();
            if(validateStandardDetails()){ //require approval when result is true
                poJSON = callApproval();
                if (!isJSONSuccess(poJSON)) {
                    return poJSON;
                }
            }
        }
        
        //change status
        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, Master().getTransactionNo());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), "", lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();
        
        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction confirmed successfully.");
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
    
    public JSONObject validateSRPPrice(String fsVariantId){
        poJSON = new JSONObject();
        try {
            Date ldServerDate = SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE);
            //Check existing price list
            String lsSQL = MiscUtil.addCondition(PriceList_SQL()," sCompnyID =  " + SQLUtil.toSQL(psCompanyId)
                    + " AND a.cRecdStat =  " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                    + " AND (a.dThruDate IS NULL OR a.dThruDate >= " + SQLUtil.toSQL(xsDateShort(ldServerDate))
                    + " ) "
                            + " AND a.dFromDate <= " + SQLUtil.toSQL(xsDateShort(ldServerDate))
                    + " AND b.sVrntIDxx = " + SQLUtil.toSQL(fsVariantId)
            );
            lsSQL = lsSQL + "GROUP BY a.sValidIDx";
            System.out.println("updatePriceList SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(Detail(0).getSRPAmount() > 0.00){
                    Double ldblSRPAmt = 0.00;
                    while(loRS.next()) {
                        ldblSRPAmt = loRS.getDouble("nSRPAmntx");
                        if(!Objects.equals(ldblSRPAmt, Detail(0).getSRPAmount())){
                            return setJSON("error", "Vehicle Quotation SRP Amount is outdated.");
                        }
                    }
                }
            } else {
                return setJSON("error", "No approved vehicle price list."
                        + "\nConfigure vehicle pricing before adding vehicle to quotation.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON = setJSON("error", MiscUtil.getException(ex));
            return poJSON;
        }
            
        poJSON = new JSONObject();
        poJSON = setJSON("success", "success");
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
                + (pbWithUI ? " AND (b.dValdThru IS NULL OR b.dValdThru = '' OR b.dValdThru >=  " + SQLUtil.toSQL(xsDateShort( poGRider.getServerDate()))
                    : " AND (b.dValdThru IS NULL OR b.dValdThru >=  " + SQLUtil.toSQL(xsDateShort( poGRider.getServerDate()))
                )
                + " ) " 
            );
       
       
        //If current user is an ordinary user load only its inquiries
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            lsSQL = MiscUtil.addCondition(lsSQL,
                    " d.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
        }
        
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
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
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
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " b.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        if(psTranStat.equals(VehicleQuotationStatus.OPEN)){
            lsSQL = lsSQL   + " AND (b.dValdThru IS NULL OR b.dValdThru = '' OR b.dValdThru >=  " + SQLUtil.toSQL(xsDateShort(poGRider.getServerDate()))
                            + " ) "; //Select all not expires quotation
        }
        
        //If current user is an ordinary user load only its inquiries
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            lsSQL = MiscUtil.addCondition(lsSQL,
                    " d.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
        }
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox ";
        System.out.println("Executing SQL: " + lsSQL);
        if(pbWithUI){

            poJSON = ShowDialogFX.Browse(poGRider,
                    lsSQL,
                    "",
                    "Transaction No»Client»Transaction Date",
                    "sTransNox»sCompnyNm»dTransact",
                    "a.sTransNox»c.sCompnyNm»a.dTransact" ,
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
    
    public JSONObject SearchTransaction(String fsClient, String fsTransactionNo, boolean fbByCode) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " b.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                + " AND c.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsClient + "%")
                + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + fsTransactionNo + "%")
            );
        
        //If current user is an ordinary user load only its inquiries
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            lsSQL = MiscUtil.addCondition(lsSQL,
                    " d.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
        }
        
        lsSQL = lsSQL + " GROUP BY a.sTransNox ";
        System.out.println("Executing SQL: " + lsSQL);
        if(pbWithUI){
            poJSON = ShowDialogFX.Browse(poGRider,
                    lsSQL,
                    "",
                    "Transaction No»Client»Transaction Date",
                    "sTransNox»sCompnyNm»dTransact",
                    "a.sTransNox»c.sCompnyNm»a.dTransact" ,
                   fbByCode ? 0 : 1);
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
            String lsSQL = 
                    " SELECT "
                    + " a.sTransNox "
                    + " , a.dTransact "
                    + " , a.cTranStat "
                    + " , a.sClientID "
                    + " , b.sCompnyNm AS sClientNm "
                    + " , concat(c.sLastName,', ',c.sFrstName, ' ',c.sMiddName) AS sSalePrsn "
                    + " FROM Sales_Inquiry_Master a "
                    + " LEFT JOIN Client_Master b ON b.sClientID = a.sClientID "
                    + " LEFT JOIN Salesman c ON c.sEmployID = a.sSalesman " ;
                    
                lsSQL = MiscUtil.addCondition(lsSQL,
                    " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                    + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                    + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategoryId)
                    + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                    + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + value)
                    + " AND a.cTranStat = " + SQLUtil.toSQL(SalesInquiryStatic.OPEN)
                    );

            //If current user is an ordinary user load only its inquiries
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                lsSQL = MiscUtil.addCondition(lsSQL,
                        " a.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
            }
            lsSQL = lsSQL + "  GROUP BY a.sClientID ";
            System.out.println("Search Client SQL: " + lsSQL);
            poJSON = ShowDialogFX.Browse(poGRider,
                    lsSQL,
                    value,
                    "Transaction Date»Transaction No»Client»Sales Person",
                    "dTransact»sTransNox»sClientNm»sSalePrsn",
                    "a.dTransact»a.sTransNox»b.sCompnyNm»concat(c.sLastName,', ',c.sFrstName, ' ',c.sMiddName)",
                    checkEmpty(value) ? 0 : 2);

            if (poJSON != null) {
                object.OpenTransaction((String) poJSON.get("sTransNox"));
            } else {
                poJSON = new JSONObject();
                poJSON = setJSON("error", "No Client selected");
            }
        } else {
            poJSON = object.OpenTransaction(value);
        }
        if (isJSONSuccess(poJSON)) {
            Master().setClientId(object.Master().getClientId());
            String lsAddressId = getAddress(object.Master().getClientId(),true, true);
            String lsMobileId = getContact(object.Master().getClientId(),true, true);
            Master().setAddressId(lsAddressId);
            Master().setContactId(lsMobileId);
            Detail(0).setSourceCode(object.getSourceCode());
            Detail(0).setSourceNo(object.Master().getTransactionNo());
        }
        
        return poJSON;
    }
    
    private String getAddress(String fsClientId,boolean isReturnBycode, boolean isSearch) throws SQLException, CloneNotSupportedException, GuanzonException{
        ClientModels clientModel = new ClientModels(poGRider);
        Model_Client_Address loClientAddress = clientModel.ClientAddress();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loClientAddress), 
                                    " sClientID = " + SQLUtil.toSQL(fsClientId)
                                    + " AND cPrimaryx = " + SQLUtil.toSQL(Logical.YES)
                                    );
        System.out.println("loClientAddress SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        String lsAddressId = "";
        ReloadAddOnList();
        if (MiscUtil.RecordCount(loRS) > 0) {
            if(loRS.next()){
                if(isReturnBycode){
                    lsAddressId = loRS.getString("sAddrssID");
                } else {
                    //Todo
                }
            }
        }
        
        return lsAddressId;
    }
    private String getContact(String fsClientId,boolean isReturnBycode, boolean isSearch) throws SQLException, CloneNotSupportedException, GuanzonException{
        ClientModels clientModel = new ClientModels(poGRider);
        Model_Client_Mobile loClientMobile = clientModel.ClientMobile();

        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loClientMobile), 
                                    " sClientID = " + SQLUtil.toSQL(fsClientId)
                                    + " AND cPrimaryx = " + SQLUtil.toSQL(Logical.YES)
                                    );
        System.out.println("loClientMobile SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        String lsMobileId = "";
        ReloadAddOnList();
        if (MiscUtil.RecordCount(loRS) > 0) {
            if(loRS.next()){
                if(isReturnBycode){
                    lsMobileId = loRS.getString("sMobileID");
                } else {
                    lsMobileId = loRS.getString("sMobileNo");
                }
            }
        }
        
        return lsMobileId;
    }
    
    private String getEmail(String fsClientId,boolean isReturnBycode, boolean isSearch) throws SQLException, CloneNotSupportedException, GuanzonException{
        ClientModels clientModel = new ClientModels(poGRider);
        Model_Client_Mail loClientEmail = clientModel.ClientMail();

        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loClientEmail), 
                                    " sClientID = " + SQLUtil.toSQL(fsClientId)
                                    + " AND cPrimaryx = " + SQLUtil.toSQL(Logical.YES)
                                    );
        System.out.println("loClientEmail SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        String lsEmail = "";
        ReloadAddOnList();
        if (MiscUtil.RecordCount(loRS) > 0) {
            if(loRS.next()){
                if(isReturnBycode){
                    lsEmail = loRS.getString("sEmailIDx");
                } else {
                    lsEmail = loRS.getString("sEMailAdd");
                }
            }
        }
        
        return lsEmail;
    }
    
    
    public JSONObject SearchClient(String value)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.Master().searchRecord(value, false);
        if (isJSONSuccess(poJSON)) {
            setClientName(object.Master().getModel().getCompanyName());
        }
        
        return poJSON;
    }
        
    public String getFullAddress() throws GuanzonException, SQLException{
        String lsAddress = Master().ClientAddress().getAddress();
        if(checkNotEmpty(lsAddress)){
            if(checkNotEmpty(Master().ClientAddress().Barangay().getBarangayName())){
                lsAddress = lsAddress + " " + Master().ClientAddress().Barangay().getBarangayName() ;
            }
            if(checkNotEmpty(Master().ClientAddress().Town().getDescription() )){
                lsAddress = lsAddress + " " + Master().ClientAddress().Town().getDescription()  ;
            }
            if(checkNotEmpty(Master().ClientAddress().Town().Province().getDescription())){
                lsAddress = lsAddress + ", " + Master().ClientAddress().Town().Province().getDescription();
            }
        }
        if(lsAddress == null){
            lsAddress = "";
        }
        return lsAddress;
    }
    
    public JSONObject Searchbank(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(checkEmpty(Detail(row).getSourceNo())){
            return setJSON("error", "Inquiry cannot be empty.");
        }
        
        if(checkEmpty(Detail(row).getVariantId())){
            return setJSON("error", "Unit cannot be empty.");
        }
        
        if("0".equals(Detail(row).getPaymentForm())){
            return setJSON("error", "Mode of payment must be Financing or PO.");
        }
        
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.initialize();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setWithUI(pbWithUI);
        if(pbWithUI){
            poJSON = object.searchRecord(value, byCode);
        } else {
            poJSON = object.openRecord(value);
        }
        if (isJSONSuccess(poJSON)) {
            Detail(row).setBankId(object.getModel().getBankID());
        }
        
//        StandardFinancingRates object = new SalesControllers(poGRider, logwrapr).StandardFinancingRates();
//        object.initialize();
//        object.setRecordStatus(FinancingRateStatus.ACTIVE);
//        object.setWithUI(pbWithUI);
//        if(pbWithUI){
//            poJSON = object.searchRecord(value, byCode);
//        } else {
//            poJSON = object.openRecord(value);
//        }
//        if (isJSONSuccess(poJSON)) {
//            Detail(row).setQuotationRate(object.getModel().getStandardRateId());
//            Detail(row).setDuration(object.getModel().getDuration());
//            Detail(row).setRateValue(object.getModel().getRate());
//        }
        
//        
//        FinancingRates object = new SalesControllers(poGRider, logwrapr).FinancingRates();
//        object.initialize();
//        object.setRecordStatus(RecordStatus.ACTIVE);
//        object.setWithUI(pbWithUI);
//        if(pbWithUI){
//            poJSON = object.searchRecord(value, byCode);
//        } else {
//            poJSON = object.openRecord(value);
//        }
//        if (isJSONSuccess(poJSON)) {
//            Detail(row).setQuotationRate(object.getModel().getRateId());
//            Detail(row).setDuration(object.getModel().getDuration());
//            Detail(row).setRateValue(object.getModel().getRate());
//        }
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
        
        VehicleDescription object = new SalesControllers(poGRider, logwrapr).VehicleDescription();
        object.initialize();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setIndustryId(psIndustryId);
        if(pbWithUI){
            poJSON = object.searchRecordByModel(value, byCode, Detail(row).getModelId());
        } else {
            poJSON = object.openRecord(value);
        }
        
        if(object.getModel().getSellingPrice() <= 0.00){
            return setJSON("error", "No updated selling price."
                                        + "\nConfigure pricelist before adding vehicle to quotation.");
        }
        if (isJSONSuccess(poJSON)) {
            Detail(row).setVariantId(object.getModel().getVariantId()); 
            Detail(row).setSRPAmount(object.getModel().getSellingPrice()); 

            poJSON = AutoPopulateVehicleAddOn(object.getModel().getVariantId());
            if(!isJSONSuccess(poJSON)){
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
        lsSQL = MiscUtil.addCondition(lsSQL, "(( " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate )"
                                                + " OR (a.dFromDate <= " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate()))
                                                + " AND a.dThruDate IS NULL " 
                                                + ")) "
                                                + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                                + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                                                );
        System.out.println("Executing SQL: " + lsSQL);
        if(pbWithUI){
            poJSON = ShowDialogFX.Browse(poGRider,
            lsSQL,
            value,
            "Financing ID»Description»Downpayment Rate",
            "sVhclFIDx»xDescription»nDPRatexx",
            "b.sVhclFIDx»CONCAT(f.sDescript,' ', e.sDescript, ' ', c.sDescript, ' ', g.sDescript, ' ', c.nYearMdlx, ' ',d.sTransmss )»a.nDPRatexx",
            byCode ? 0 : 1);
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "success");
            poJSON.put("sVrntIDxx", value);
            poJSON.put("nDPRatexx", 15.00); //for testing only
            poJSON.put("nRsrvAmtx", 10000.00); //for testing only
            poJSON.put("nSelPrice", 10000.00); //for testing only
        }
        if (poJSON != null) {
            String lsVariantId = (String) poJSON.get("sVrntIDxx");
            Double ldblDPRate = Double.valueOf((String) poJSON.get("nDPRatexx"));
            Double ldblRSVAmt =  Double.valueOf((String) poJSON.get("nRsrvAmtx"));
            Double ldblSRPAmt =  Double.valueOf((String) poJSON.get("nSelPrice"));
            Detail(row).setVariantId(lsVariantId);
            Detail(row).setDownPaymentPercent(ldblDPRate);
            Detail(row).setReservationAmount(ldblRSVAmt);
            Detail(row).setDownPayment(ldblSRPAmt*(ldblDPRate/100));
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
    
    public JSONObject SearchDiscountType(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(checkEmpty(Detail(0).getVariantId())){
            return setJSON("error", "Vehicle cannot be empty.");
        }
        
        DiscountType object = new SalesControllers(poGRider, logwrapr).DiscountType();
        object.initialize();
        object.setWithUI(pbWithUI);
        object.setRecordStatus(AddOnTypeStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            DiscountList(row).setDiscountType(object.getModel().getDiscountType());
        }
        return poJSON;
    }
    
    public JSONObject AutoPopulateVehicleAddOn(String fsVariantId) throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        Detail(0).setPromptPaymentDiscount(0.00);
        Detail(0).setStandardRateId("");
        Detail(0).setBankId("");
        paDiscount = new ArrayList<>();
        paAddOn = new ArrayList<>();
        String lsSQL = VehicleAddOn_SQL();
        lsSQL = MiscUtil.addCondition(lsSQL, SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate "
                                    + " AND b.sVrntIDxx = " + SQLUtil.toSQL(fsVariantId)
                                    + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                    + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                                    );
        System.out.println("AutoLoadVehicleAddOn SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        ReloadAddOnList();
        if (MiscUtil.RecordCount(loRS) > 0) {
            while(loRS.next()){
                String lsAddOnId = loRS.getString("sAddOnIDx");
                String lsAddOnName = loRS.getString("sAddTypNm");
                Double ldblAmount = loRS.getDouble("nAmountxx");
                int lnRow = getAddOnListCount() - 1;
                if(checkNotEmpty(lsAddOnName)){
                    lsAddOnName = lsAddOnName.toUpperCase();
                    if(lsAddOnName.contains(VehicleQuotationStatus.AddOnType.LTO)) {
                        Detail(0).setLTOAmount(ldblAmount);
                    } else if(lsAddOnName.contains(VehicleQuotationStatus.AddOnType.TPL)){
                        Detail(0).setTPLAmount(ldblAmount);
                    }else if(lsAddOnName.contains(VehicleQuotationStatus.AddOnType.COMPREHENSIVE)){
                        Detail(0).setComprehensiveAmount(ldblAmount);
                    }

                    //Add to Add On Detail
                    poJSON = checkExistingAddOn(lsAddOnId, lsAddOnName, lnRow);
                    if(isJSONSuccess(poJSON)){
                        lnRow = (int) poJSON.get("row");
                        AddOnList(lnRow).setAddOnId(lsAddOnId);
                        AddOnList(lnRow).setAmount(ldblAmount);
                        AddOnList(lnRow).setQuantity(1);
                        ReloadAddOnList();
                    }
                }
            }
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject loadQuotationAddOn() throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        paAddOn = new ArrayList<>();
        
        String sql = "SELECT sQotAddID FROM Quotation_AddOn " 
                                                + " WHERE sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()) 
                                                + " ORDER BY sQotAddID ASC";
        ResultSet rs = this.poGRider.executeQuery(sql);
        while(rs.next()) {
            Model_Quotation_AddOn loDetail = new SalesModels(poGRider).QuotationAddOn();
            loDetail.initialize();
            poJSON = loDetail.openRecord( rs.getString("sQotAddID"),Master().getTransactionNo());
            if (!"success".equals((String)this.poJSON.get("result"))) {
                poJSON.put("message", "Unable to open quotation add on record.");
                return poJSON;
            }

            paAddOn.add(loDetail);
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject loadQuotationFreebies() throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        paFreebies = new ArrayList<>();
        
        String sql = "SELECT sQotFreID FROM Quotation_Freebie " 
                                                + " WHERE sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()) 
                                                + " ORDER BY sQotFreID ASC";
        ResultSet rs = this.poGRider.executeQuery(sql);
        while(rs.next()) {
            Model_Quotation_Freebie loDetail = new SalesModels(poGRider).QuotationFreebie();
            loDetail.initialize();
            poJSON = loDetail.openRecord( rs.getString("sQotFreID"),Master().getTransactionNo());
            if (!"success".equals((String)this.poJSON.get("result"))) {
                poJSON.put("message", "Unable to open quotation freebie record.");
                return poJSON;
            }

            paFreebies.add(loDetail);
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject loadQuotationInclusion() throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        paInclusion = new ArrayList<>();
        
        String sql = "SELECT sQotIncID FROM Quotation_Inclusion " 
                                                + " WHERE sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()) 
                                                + " ORDER BY sQotIncID ASC";
        ResultSet rs = this.poGRider.executeQuery(sql);
        while(rs.next()) {
            Model_Quotation_Inclusion loDetail = new SalesModels(poGRider).QuotationInclusion();
            loDetail.initialize();
            poJSON = loDetail.openRecord( rs.getString("sQotIncID"),Master().getTransactionNo());
            if (!"success".equals((String)this.poJSON.get("result"))) {
                poJSON.put("message", "Unable to open quotation inclusion record.");
                return poJSON;
            }

            paInclusion.add(loDetail);
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject loadQuotationSpecialRequest() throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        paSpecialRequest = new ArrayList<>();
        
        String sql = "SELECT nEntryNox FROM Quotation_Special_Request " 
                                                + " WHERE sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()) 
                                                + " ORDER BY nEntryNox ASC";
        ResultSet rs = this.poGRider.executeQuery(sql);
        while(rs.next()) {
            Model_Quotation_Special_Request loDetail = new SalesModels(poGRider).QuotationSpecialRequest();
            loDetail.initialize();
            poJSON = loDetail.openRecord( Master().getTransactionNo(),rs.getString("nEntryNox"));
            if (!"success".equals((String)this.poJSON.get("result"))) {
                poJSON.put("message", "Unable to open quotation special request record.");
                return poJSON;
            }

            paSpecialRequest.add(loDetail);
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject loadQuotationDiscount() throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        paDiscount = new ArrayList<>();
        
        String sql = "SELECT nEntryNox FROM Quotation_Discount " 
                                                + " WHERE sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()) 
                                                + " ORDER BY nEntryNox ASC";
        ResultSet rs = this.poGRider.executeQuery(sql);
        while(rs.next()) {
            Model_Quotation_Discount loDetail = new SalesModels(poGRider).QuotationDiscount();
            loDetail.initialize();
            poJSON = loDetail.openRecord( Master().getTransactionNo(),rs.getString("nEntryNox"));
            if (!"success".equals((String)this.poJSON.get("result"))) {
                poJSON.put("message", "Unable to open quotation discount record.");
                return poJSON;
            }

            paDiscount.add(loDetail);
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public JSONObject SearchVehicleAddOn(String value, boolean byCode, int row, boolean isAddOn)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        
        String lsSQL = VehicleAddOn_SQL();
        lsSQL = MiscUtil.addCondition(lsSQL, SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate "
                                    + " AND b.sVrntIDxx = " + SQLUtil.toSQL(Detail(0).getVariantId())
                                    + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                    + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                                    );
        if(isAddOn){
            lsSQL = lsSQL + " GROUP BY c.sAddTypNm ";
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
                String lsAddOnName = (String) poJSON.get("sAddTypNm");
                Double lsAmount = Double.valueOf((String) poJSON.get("nAmountxx"));
                poJSON = checkExistingAddOn(lsAddOnId,lsAddOnName, row);
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
        } else {
            poJSON = SearchFreebies(value, byCode, row, false);
        }
        
        poJSON = setJSON("success", "success");
        poJSON.put("row", row);
        return poJSON;
    }
    
    private JSONObject checkExistingAddOn(String fsId, String fsName, int row) throws SQLException, GuanzonException{
        poJSON.put("row", row);
        for(int lnCtr = 0;lnCtr < getAddOnListCount(); lnCtr++){
            if(fsId.equals(AddOnList(lnCtr).getAddOnId())){
                if(row != lnCtr){
                    if(!AddOnList(lnCtr).getRecordStatus()){
                        AddOnList(lnCtr).setRecordStatus(true);
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    } else {
                        poJSON = setJSON("error", "Selected addon <"+AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName()+"> already exist.");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }
                }
            } else {
                //check name
                if(fsName.equals(AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName())){
                    AddOnList(lnCtr).setAddOnId(fsId);
                    poJSON.put("row", lnCtr);
                    return poJSON;
                }
            }
        }
            
        poJSON = setJSON("success", "success");
        poJSON.put("row", row);
        return poJSON;
    }
    
    public JSONObject SearchFreebies(String value, boolean byCode, int row, boolean includeAddOn)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        
        if(checkEmpty(Detail(0).getVariantId())){
            return setJSON("error", "Unit cannot be empty.");
        }
        
        String lsSQL = VehicleFreebieInclusion_SQL(includeAddOn);
        lsSQL = MiscUtil.addCondition(lsSQL, " x.cRecdStat = " + SQLUtil.toSQL(FreebieStatus.ACTIVE));
        System.out.println("Executing SQL: " + lsSQL);

        poJSON = ShowDialogFX.Browse(poGRider,
        lsSQL,
        value,
        "Description",
        "sDescxxxx",
        "x.sDescxxxx",
        0);
        if (poJSON != null) {
            String lsId = (String) poJSON.get("sIDxxxxxx");
            int lnType = Integer.valueOf((String) poJSON.get("sTypexxxx"));
            
            poJSON = checkExistingFreebies(lsId,lnType, row, !includeAddOn);
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }
            if(row != (int) poJSON.get("row")){
                return poJSON;
            }
            
            row = (int) poJSON.get("row");
            switch(lnType){
                case 0: //add on
                    ReloadAddOnList();
                    row = getAddOnListCount()-1;
                    AddOnList(row).setAddOnId(lsId);
                    AddOnList(row).setQuantity(1);
                    AddOnList(row).setRecordStatus(true);
                break;
                case 1: //freebie
                    ReloadFreebiesList();
                    row = getFreebiesListCount()-1;
                    FreebiesList(row).setFreebieId(lsId);
                    FreebiesList(row).setIsFree(includeAddOn);
                    FreebiesList(row).setQuantity(1);
                    FreebiesList(row).setRecordStatus(true);
                break;
                case 2: //inclusion
                    ReloadInclusionList();
                    row = getInclusionListCount()-1;
                    InclusionList(row).setInclusionId(lsId);
                    InclusionList(row).setIsFree(includeAddOn);
                    InclusionList(row).setQuantity(1);
                    InclusionList(row).setRecordStatus(true);
                break;
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
        poJSON = setJSON("success", "success");
        poJSON.put("row", row);
        return poJSON;
    }

    private JSONObject checkExistingFreebies(String fsId, int fnType, int row, boolean isAddOn) throws SQLException, GuanzonException{
        poJSON.put("row", row);
        String lsType = "";
        switch(fnType){
            case 0: //addon
                for(int lnCtr = 0;lnCtr < getAddOnListCount(); lnCtr++){
                    if(fsId.equals(AddOnList(lnCtr).getAddOnId())){
                        if(!AddOnList(lnCtr).getRecordStatus()){
                            AddOnList(lnCtr).setRecordStatus(true);
                            AddOnList(lnCtr).setQuantity(1);
                            AddOnList(lnCtr).setAmount(AddOnList(lnCtr).AddOnMaster().getAmount());
                            if(!isAddOn){
                                AddOnList(lnCtr).setDiscountAmount(AddOnList(lnCtr).AddOnMaster().getAmount());
                            }
                            poJSON.put("row", lnCtr);
                            return poJSON;
                        } else {
                            lsType = "add on";
                            if(AddOnList(lnCtr).getTotalAmount() <= 0.00){
                                lsType = "freebie";
                            }
                            poJSON = setJSON("error", "Selected item <"+AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName()+"> already exists in "+lsType+" table.");
                            poJSON.put("row", lnCtr);
                            return poJSON;
                        }
                    }
                }
            break;
            case 1: //freebie
                for(int lnCtr = 0;lnCtr < getFreebiesListCount(); lnCtr++){
                    if(fsId.equals(FreebiesList(lnCtr).getFreebieId())){
                        if(!FreebiesList(lnCtr).getRecordStatus()){
                            FreebiesList(lnCtr).setRecordStatus(true);
                            poJSON.put("row", lnCtr);
                            return poJSON;
                        } else {
                            lsType = "add on";
                            if(FreebiesList(lnCtr).getTotalAmount() <= 0.00
                                && FreebiesList(lnCtr).getIsFree()){
                                lsType = "freebie";
                            }
                            poJSON = setJSON("error", "Selected item <"+FreebiesList(lnCtr).Freebie().getDescription()+"> already exists in "+lsType+" table.");
                            poJSON.put("row", lnCtr);
                            return poJSON;
                        }
                    }
                }
            break;
            case 2: //inclusion
                for(int lnCtr = 0;lnCtr < getInclusionListCount(); lnCtr++){
                    if(fsId.equals(InclusionList(lnCtr).getInclusionId())){
                        if(!InclusionList(lnCtr).getRecordStatus()){
                            InclusionList(lnCtr).setRecordStatus(true);
                            poJSON.put("row", lnCtr);
                            return poJSON;
                        } else {
                            lsType = "add on";
                            if(InclusionList(lnCtr).getTotalAmount() <= 0.00
                                    && InclusionList(lnCtr).getIsFree()){
                                lsType = "freebie";
                            }
                            poJSON = setJSON("error", "Selected item <"+InclusionList(lnCtr).Inclusion().getDescription()+"> already exists in "+lsType+" table.");
                            poJSON.put("row", lnCtr);
                            return poJSON;
                        }
                    }
                }
            break;
        
        }
        
        poJSON.put("row", row);
        return poJSON;
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
    
    public JSONObject AddAddOn() throws CloneNotSupportedException {
        if (getAddOnListCount() > 0) {
            if ((AddOnList(getAddOnListCount() - 1).getAddOnId() == null || "".equals(AddOnList(getAddOnListCount() - 1).getAddOnId()))
//                || (AddOnList(getAddOnListCount() - 1).getAddOnDescription()== null || "".equals(AddOnList(getAddOnListCount() - 1).getAddOnDescription()))
                ){
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
    
    public List<Model_Quotation_AddOn> AddOnList() {
        return paAddOn;
    }
    
    public void ReloadAddOnList() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getAddOnListCount() - 1;
        while (lnCtr >= 0) {
            if ((AddOnList(lnCtr).getAddOnId() == null || "".equals(AddOnList(lnCtr).getAddOnId()))
//                && (AddOnList(lnCtr).getAddOnDescription() == null || "".equals(AddOnList(lnCtr).getAddOnDescription()))
                ) {
                paAddOn.remove(lnCtr);
            } 
            lnCtr--;
        }

        if ((getAddOnListCount() - 1) >= 0) {
            if (
                (AddOnList(getAddOnListCount() - 1).getAddOnId() != null && !"".equals(AddOnList(getAddOnListCount() - 1).getAddOnId()))
//                || (AddOnList(getAddOnListCount() - 1).getAddOnDescription() != null && !"".equals(AddOnList(getAddOnListCount() - 1).getAddOnDescription()))
                ) {
                AddAddOn();
            }
        }

        if ((getAddOnListCount() - 1) < 0) {
            AddAddOn();
        }
    }
    
    public JSONObject AddFreebies() throws CloneNotSupportedException {
        if (getFreebiesListCount() > 0) {
            if ((FreebiesList(getFreebiesListCount() - 1).getFreebieId() == null || "".equals(FreebiesList(getFreebiesListCount() - 1).getFreebieId()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }
        
        Model_Quotation_Freebie loModel = new SalesModels(poGRider).QuotationFreebie();
        loModel.newRecord();
        paFreebies.add(loModel);
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public Model_Quotation_Freebie FreebiesList(int row) {
        return (Model_Quotation_Freebie) paFreebies.get(row);
    }

    public int getFreebiesListCount() {
        if(paFreebies == null){
            return 0;
        }
        return paFreebies.size();
    }
    
    public List<Model_Quotation_Freebie> FreebiesList() {
        return paFreebies;
    }
    
    public void ReloadFreebiesList() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getFreebiesListCount() - 1;
        while (lnCtr >= 0) {
            if (FreebiesList(lnCtr).getFreebieId() == null || "".equals(FreebiesList(lnCtr).getFreebieId())) {
                paFreebies.remove(lnCtr);
            } 
            lnCtr--;
        }

        if ((getFreebiesListCount() - 1) >= 0) {
            if (
                (FreebiesList(getFreebiesListCount() - 1).getFreebieId() != null && !"".equals(FreebiesList(getFreebiesListCount() - 1).getFreebieId()))
                ){
                AddFreebies();
            }
        }

        if ((getFreebiesListCount() - 1) < 0) {
            AddFreebies();
        }
    }
    
    public JSONObject AddInclusion() throws CloneNotSupportedException {
        if (getInclusionListCount() > 0) {
            if ((InclusionList(getInclusionListCount() - 1).getInclusionId() == null || "".equals(InclusionList(getInclusionListCount() - 1).getInclusionId()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }
        
        Model_Quotation_Inclusion loModel = new SalesModels(poGRider).QuotationInclusion();
        loModel.newRecord();
        paInclusion.add(loModel);
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public Model_Quotation_Inclusion InclusionList(int row) {
        return (Model_Quotation_Inclusion) paInclusion.get(row);
    }

    public int getInclusionListCount() {
        if(paInclusion == null){
            return 0;
        }
        return paInclusion.size();
    }
    
    public List<Model_Quotation_Inclusion> InclusionList() {
        return paInclusion;
    }
    
    public void ReloadInclusionList() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getInclusionListCount() - 1;
        while (lnCtr >= 0) {
            if (InclusionList(lnCtr).getInclusionId() == null || "".equals(InclusionList(lnCtr).getInclusionId())) {
                paInclusion.remove(lnCtr);
            } 
            lnCtr--;
        }

        if ((getInclusionListCount() - 1) >= 0) {
            if (
                (InclusionList(getInclusionListCount() - 1).getInclusionId() != null && !"".equals(InclusionList(getInclusionListCount() - 1).getInclusionId()))
                ){
                AddInclusion();
            }
        }

        if ((getInclusionListCount() - 1) < 0) {
            AddInclusion();
        }
    }
   
    public JSONObject AddSpecialRequest() throws CloneNotSupportedException {
        if (getSpecialRequestListCount() > 0) {
            if ((SpecialRequestList(getSpecialRequestListCount() - 1).getRequestDescription() == null || "".equals(SpecialRequestList(getSpecialRequestListCount() - 1).getRequestDescription()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }
        
        Model_Quotation_Special_Request loModel = new SalesModels(poGRider).QuotationSpecialRequest();
        loModel.newRecord();
        paSpecialRequest.add(loModel);
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public Model_Quotation_Special_Request SpecialRequestList(int row) {
        return (Model_Quotation_Special_Request) paSpecialRequest.get(row);
    }

    public int getSpecialRequestListCount() {
        if(paSpecialRequest == null){
            return 0;
        }
        return paSpecialRequest.size();
    }
    
    public List<Model_Quotation_Special_Request> SpecialRequestList() {
        return paSpecialRequest;
    }
    
    public void ReloadSpecialRequestList() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getSpecialRequestListCount() - 1;
        while (lnCtr >= 0) {
            if (SpecialRequestList(lnCtr).getRequestDescription() == null || "".equals(SpecialRequestList(lnCtr).getRequestDescription())) {
                paSpecialRequest.remove(lnCtr);
            } 
            lnCtr--;
        }

        if ((getSpecialRequestListCount() - 1) >= 0) {
            if (
                (SpecialRequestList(getSpecialRequestListCount() - 1).getRequestDescription() != null && !"".equals(SpecialRequestList(getSpecialRequestListCount() - 1).getRequestDescription()))
                ){
                AddSpecialRequest();
            }
        }

        if ((getSpecialRequestListCount() - 1) < 0) {
            AddSpecialRequest();
        }
    }
    
    public JSONObject AddDiscount() throws CloneNotSupportedException {
        if (getDiscountListCount() > 0) {
            if (DiscountList(getDiscountListCount() - 1).getDiscountAmount() <= 0.00){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }
        
        Model_Quotation_Discount loModel = new SalesModels(poGRider).QuotationDiscount();
        loModel.newRecord();
        paDiscount.add(loModel);
        poJSON.put("result", "success");
        return poJSON;
    }
    
    public Model_Quotation_Discount DiscountList(int row) {
        return (Model_Quotation_Discount) paDiscount.get(row);
    }

    public int getDiscountListCount() {
        if(paDiscount == null){
            return 0;
        }
        return paDiscount.size();
    }
    
    public List<Model_Quotation_Discount> DiscountList() {
        return paDiscount;
    }
    
    public void ReloadDiscountList() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getDiscountListCount() - 1;
        while (lnCtr >= 0) {
            if (DiscountList(lnCtr).getDiscountType() == null || "".equals(DiscountList(lnCtr).getDiscountType())) {
                paDiscount.remove(lnCtr);
            } 
            lnCtr--;
        }

        if ((getDiscountListCount() - 1) >= 0) {
            if (DiscountList(getDiscountListCount() - 1).getDiscountAmount() > 0.00){
                AddDiscount();
            }
        }

        if ((getDiscountListCount() - 1) < 0) {
            AddDiscount();
        }
    }

//    public JSONObject remake(String fsTransNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException, ScriptException, ParseException{
//        poJSON = new JSONObject();
//        
//        VehicleQuotation loRemake = new SalesControllers(poGRider, logwrapr).VehicleQuotation();
//        loRemake.InitTransaction();
//        loRemake.setWithUI(false);
//        
//        VehicleQuotation loObj = new SalesControllers(poGRider, logwrapr).VehicleQuotation();
//        loObj.InitTransaction();
//        poJSON = loObj.OpenTransaction(fsTransNo);
//        if(!isJSONSuccess(poJSON)){
//            return poJSON;
//        }
//        
//        poJSON = loRemake.NewTransaction();
//        if(!isJSONSuccess(poJSON)){
//            return poJSON;
//        }
//        //Populate Master
//        Model_Sales_Quotation_Master loMasterSource = loObj.Master();
//        for (int lnCol = 1; lnCol <= loMasterSource.getColumnCount(); lnCol++) {
//            String lsCol = loMasterSource.getColumn(lnCol);
//            switch (lsCol) {
//                case "sTransNox":
//                case "dTransact":
//                case "cTranStat":
//                case "sModified":
//                case "dModified":
//                    break;
//                default:
//                    loRemake.Master().setValue(lsCol, loMasterSource.getValue(lsCol));
//            }
//        }
//        loRemake.Master().setTransactionNo(loRemake.Master().getNextCode());
//       
//        //Populate Detail
//        loRemake.paDetail = new ArrayList<>();
//        for (int lnCtr = 0; lnCtr < loObj.getDetailCount(); lnCtr++) {
//            Model_Sales_Quotation_Vehicle loSource = loObj.Detail(lnCtr);
//            if (!loSource.getRecordStatus()) continue;
//
//            Model_Sales_Quotation_Vehicle loNew = new SalesModels(poGRider).SalesQuotationVehicle();
//            loNew.newRecord();
//            for (int lnCol = 1; lnCol <= loSource.getColumnCount(); lnCol++) {
//                String lsCol = loSource.getColumn(lnCol);
//                switch (lsCol) {
//                    case "sTransNox":
//                    case "sQotPayID":
//                    case "dExpected":
//                    case "dValdThru":
//                    case "nSRPAmntx":
//                    case "sModified":
//                    case "dModified":
//                        break;
//                    default:
//                        loNew.setValue(lsCol, loSource.getValue(lsCol));
//                }
//            }
//            Date ldServerDate = poGRider.getServerDate();
//            Calendar loFromDate = Calendar.getInstance();
//            Calendar loToDate = Calendar.getInstance();
//            loFromDate.setTime(ldServerDate);
//            loToDate.setTime(ldServerDate);
//
//            // Set to the last day of the current month
//            loFromDate.set(Calendar.DAY_OF_MONTH,loFromDate.getActualMinimum(Calendar.DAY_OF_MONTH));
//            loToDate.set(Calendar.DAY_OF_MONTH,loToDate.getActualMaximum(Calendar.DAY_OF_MONTH));
//            
//            loNew.setTransactionNo(loRemake.Master().getTransactionNo());
//            loNew.setExpectedDate(SQLUtil.toDate(xsDateShort(poGRider.getServerDate()),SQLUtil.FORMAT_SHORT_DATE));
//            loNew.setValidThruDate(SQLUtil.toDate(xsDateShort(loToDate.getTime()),SQLUtil.FORMAT_SHORT_DATE));
//            loNew.setSRPAmount(loNew.ModelVariant().getSellingPrice()); //Set the latest SRP Amount
//            loRemake.paDetail.add(loNew);
//        }
//        
//        ReloadAddOnList();
//        for (int lnCtr = 0; lnCtr < loObj.getAddOnListCount(); lnCtr++) {
//            Model_Quotation_AddOn loSource = loObj.AddOnList(lnCtr);
//            if (!loSource.getRecordStatus()) continue;
//
//            Model_Quotation_AddOn loNew = new SalesModels(poGRider).QuotationAddOn();
//            loNew.newRecord();
//            for (int lnCol = 1; lnCol <= loSource.getColumnCount(); lnCol++) {
//                String lsCol = loSource.getColumn(lnCol);
//                switch (lsCol) {
//                    case "sTransNox":
//                    case "sQotAddID":
//                    case "sModified":
//                    case "dModified":
//                        break;
//                    default:
//                        loNew.setValue(lsCol, loSource.getValue(lsCol));
//                }
//            }
//
//            loNew.setTransactionNo(loRemake.Master().getTransactionNo());
//            loRemake.AddOnList().add(loNew);
//        }
//        
//        ReloadFreebiesList();
//        for (int lnCtr = 0; lnCtr < loObj.getFreebiesListCount(); lnCtr++) {
//            Model_Quotation_Freebie loSource = loObj.FreebiesList(lnCtr);
//            if (!loSource.getRecordStatus()) continue;
//
//            Model_Quotation_Freebie loNew = new SalesModels(poGRider).QuotationFreebie();
//            loNew.newRecord();
//            for (int lnCol = 1; lnCol <= loSource.getColumnCount(); lnCol++) {
//                String lsCol = loSource.getColumn(lnCol);
//                switch (lsCol) {
//                    case "sTransNox":
//                    case "sQotFreID":
//                    case "sModified":
//                    case "dModified":
//                        break;
//                    default:
//                        loNew.setValue(lsCol, loSource.getValue(lsCol));
//                }
//            }
//
//            loNew.setTransactionNo(loRemake.Master().getTransactionNo());
//            loRemake.FreebiesList().add(loNew);
//        }
//        
//        ReloadInclusionList();
//        for (int lnCtr = 0; lnCtr < loObj.getInclusionListCount(); lnCtr++) {
//            Model_Quotation_Inclusion loSource = loObj.InclusionList(lnCtr);
//            if (!loSource.getRecordStatus()) continue;
//
//            Model_Quotation_Inclusion loNew = new SalesModels(poGRider).QuotationInclusion();
//            loNew.newRecord();
//            for (int lnCol = 1; lnCol <= loSource.getColumnCount(); lnCol++) {
//                String lsCol = loSource.getColumn(lnCol);
//                switch (lsCol) {
//                    case "sTransNox":
//                    case "sQotIncID":
//                    case "sModified":
//                    case "dModified":
//                        break;
//                    default:
//                        loNew.setValue(lsCol, loSource.getValue(lsCol));
//                }
//            }
//
//            loNew.setTransactionNo(loRemake.Master().getTransactionNo());
//            loRemake.InclusionList().add(loNew);
//        }
//        //AUTO SAVE
////        poJSON = loRemake.SaveTransaction();
////        if(!isJSONSuccess(poJSON)){
////            return poJSON;
////        }
////        
////        poJSON = OpenTransaction(loRemake.Master().getTransactionNo());
////        if(!isJSONSuccess(poJSON)){
////            return poJSON;
////        }
//        
//        return setJSON("success", "Transaction Successfully remake.");
//    }
    
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
        //Populate Master
        Model_Sales_Quotation_Master loMasterSource = loObj.Master();
        for (int lnCol = 1; lnCol <= loMasterSource.getColumnCount(); lnCol++) {
            String lsCol = loMasterSource.getColumn(lnCol);
            switch (lsCol) {
                case "sTransNox":
                case "dTransact":
                case "cTranStat":
                case "sModified":
                case "dModified":
                    break;
                default:
                    Master().setValue(lsCol, loMasterSource.getValue(lsCol));
            }
        }
        Master().setTransactionNo(Master().getNextCode());
       
        //Populate Detail
        paDetail = new ArrayList<>();
        for (int lnCtr = 0; lnCtr < loObj.getDetailCount(); lnCtr++) {
            Model_Sales_Quotation_Vehicle loSource = loObj.Detail(lnCtr);
            if (!loSource.getRecordStatus()) continue;

            Model_Sales_Quotation_Vehicle loNew = new SalesModels(poGRider).SalesQuotationVehicle();
            loNew.newRecord();
            for (int lnCol = 1; lnCol <= loSource.getColumnCount(); lnCol++) {
                String lsCol = loSource.getColumn(lnCol);
                switch (lsCol) {
                    case "sTransNox":
                    case "dExpected":
                    case "dValdThru":
                    case "nSRPAmntx":
                    case "sModified":
                    case "dModified":
                        break;
                    default:
                        loNew.setValue(lsCol, loSource.getValue(lsCol));
                }
            }
            Date ldServerDate = poGRider.getServerDate();
            Calendar loFromDate = Calendar.getInstance();
            Calendar loToDate = Calendar.getInstance();
            loFromDate.setTime(ldServerDate);
            loToDate.setTime(ldServerDate);

            // Set to the last day of the current month
            loFromDate.set(Calendar.DAY_OF_MONTH,loFromDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            loToDate.set(Calendar.DAY_OF_MONTH,loToDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            
            loNew.setTransactionNo(Master().getTransactionNo());
            loNew.setExpectedDate(SQLUtil.toDate(xsDateShort(poGRider.getServerDate()),SQLUtil.FORMAT_SHORT_DATE));
            loNew.setValidThruDate(SQLUtil.toDate(xsDateShort(loToDate.getTime()),SQLUtil.FORMAT_SHORT_DATE));
            loNew.setSRPAmount(loNew.ModelVariant().getSellingPrice()); //Set the latest SRP Amount
            paDetail.add(loNew);
        }
        
        ReloadAddOnList();
        for (int lnCtr = 0; lnCtr < loObj.getAddOnListCount(); lnCtr++) {
            Model_Quotation_AddOn loSource = loObj.AddOnList(lnCtr);
            if (!loSource.getRecordStatus()) continue;

            Model_Quotation_AddOn loNew = new SalesModels(poGRider).QuotationAddOn();
            loNew.newRecord();
            for (int lnCol = 1; lnCol <= loSource.getColumnCount(); lnCol++) {
                String lsCol = loSource.getColumn(lnCol);
                switch (lsCol) {
                    case "sTransNox":
                    case "sQotAddID":
                    case "sModified":
                    case "dModified":
                        break;
                    default:
                        loNew.setValue(lsCol, loSource.getValue(lsCol));
                }
            }

            loNew.setTransactionNo(Master().getTransactionNo());
            AddOnList().add(loNew);
        }
        
        ReloadFreebiesList();
        for (int lnCtr = 0; lnCtr < loObj.getFreebiesListCount(); lnCtr++) {
            Model_Quotation_Freebie loSource = loObj.FreebiesList(lnCtr);
            if (!loSource.getRecordStatus()) continue;

            Model_Quotation_Freebie loNew = new SalesModels(poGRider).QuotationFreebie();
            loNew.newRecord();
            for (int lnCol = 1; lnCol <= loSource.getColumnCount(); lnCol++) {
                String lsCol = loSource.getColumn(lnCol);
                switch (lsCol) {
                    case "sTransNox":
                    case "sQotFreID":
                    case "sModified":
                    case "dModified":
                        break;
                    default:
                        loNew.setValue(lsCol, loSource.getValue(lsCol));
                }
            }

            loNew.setTransactionNo(Master().getTransactionNo());
            FreebiesList().add(loNew);
        }
        
        ReloadInclusionList();
        for (int lnCtr = 0; lnCtr < loObj.getInclusionListCount(); lnCtr++) {
            Model_Quotation_Inclusion loSource = loObj.InclusionList(lnCtr);
            if (!loSource.getRecordStatus()) continue;

            Model_Quotation_Inclusion loNew = new SalesModels(poGRider).QuotationInclusion();
            loNew.newRecord();
            for (int lnCol = 1; lnCol <= loSource.getColumnCount(); lnCol++) {
                String lsCol = loSource.getColumn(lnCol);
                switch (lsCol) {
                    case "sTransNox":
                    case "sQotIncID":
                    case "sModified":
                    case "dModified":
                        break;
                    default:
                        loNew.setValue(lsCol, loSource.getValue(lsCol));
                }
            }

            loNew.setTransactionNo(Master().getTransactionNo());
            InclusionList().add(loNew);
        }
        
        return setJSON("success", "Transaction Successfully remake.");
    }
    
    public void computeFields() throws SQLException, GuanzonException{
        if(getDetailCount() <= 0){
            return;
        }
        String lsDecimalFormat = "###0.00";
        DecimalFormat format = new DecimalFormat(lsDecimalFormat);
        
        Double ldblDownpaymentAmount = 0.00;
        Double ldblBalance = 0.00;
        Double ldblMontlyAmortizationAmt = 0.00;
        Double ldblGrossMonthlyInstallment = 0.00;
        Double ldblFinancingAmount = 0.00;
        Double ldblPromissoryNoteAmt = 0.00;
        int lnDuration = Detail(0).getDuration();
        Double ldblInterestRate = Detail(0).getRateValue();
        Double ldblSRPAmt = Detail(0).getSRPAmount();
        Double ldblDPAmt = Detail(0).getDownPayment();
        Double ldblRSVAmt = Detail(0).getReservationAmount();
        Double ldblCHMOAmt = Detail(0).getCHMOAmount();
        Double ldblTotalAddOn = 0.0000;
        Double ldblTranTotal = 0.0000;
        Double ldblTotalDisc = 0.0000;
        Double ldblDownpaymentPercentage = 0.0000;
        Double ldblQuotationDiscount = 0.0000;
        Model_Sales_Quotation_Vehicle loDetail = Detail(0);
        
        if(ldblSRPAmt > 0.0000){
            ldblDownpaymentPercentage = (ldblDPAmt / ldblSRPAmt) * 100;
            ldblDownpaymentPercentage = Double.parseDouble(format.format(ldblDownpaymentPercentage));
            String lsAddOnType = "";
            Model_Quotation_AddOn loAddOn;
            
            //Compute Add-On
            for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
                loAddOn = AddOnList(lnCtr);
                if(checkEmpty(loAddOn.getAddOnId())){
                    continue;
                }
                
                lsAddOnType = loAddOn.AddOnMaster().AddOnType().getAddTypeName();
                if(checkEmpty(lsAddOnType)){
                    continue;
                }
                lsAddOnType = lsAddOnType.toUpperCase();
                if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
                    loAddOn.setTotalAmount(0.00);
                    
                    if(loAddOn.getRecordStatus()){
                        if(loAddOn.AddOnMaster().getAmount() > loAddOn.getAmount()){
                            loAddOn.setDiscountAmount(loAddOn.AddOnMaster().getAmount() - loAddOn.getAmount());
                        }
                        loAddOn.setTotalAmount(loAddOn.getAmount() * loAddOn.getQuantity());
                    }
                    
                    if(lsAddOnType.contains(VehicleQuotationStatus.AddOnType.TPL)){
                        if(loAddOn.getQuantity() <= 0 || loAddOn.getTotalAmount()<= 0.00){
                            loDetail.setTPLAmount(0.00);
                        } else {
                            loDetail.setTPLAmount(loAddOn.getAmount());
                        }
                    } else if(lsAddOnType.contains(VehicleQuotationStatus.AddOnType.LTO)){
                        if(loAddOn.getQuantity() <= 0 || loAddOn.getTotalAmount() <= 0.00){
                            loDetail.setLTOAmount(0.00);
                        } else {
                            loDetail.setLTOAmount(loAddOn.getAmount());
                        }
                    } else if(lsAddOnType.contains(VehicleQuotationStatus.AddOnType.COMPREHENSIVE)){
                        if(loAddOn.getQuantity() <= 0 || loAddOn.getTotalAmount() <= 0.00){
                            loDetail.setComprehensiveAmount(0.00);
                        } else {
                            loDetail.setComprehensiveAmount(loAddOn.getAmount());
                        }
                    }
                }
            }
           
            Double ldblTPLAmt = loDetail.getTPLAmount();
            Double ldblCompAmt = loDetail.getComprehensiveAmount();
            Double ldblLTOAmt = loDetail.getLTOAmount();
            ldblTotalAddOn = getOtherAddOnTotal();
            ldblTranTotal = ldblDPAmt + ldblRSVAmt + ldblTPLAmt + ldblCompAmt + ldblLTOAmt + ldblCHMOAmt + ldblTotalAddOn;
            //if("0".equals(Detail(0).getPaymentForm())){
                ldblTranTotal = ldblTranTotal + ldblSRPAmt;
            //}
            
            //Compute Discount Total
            ldblTotalDisc = getTotalDiscount();

            //Financing Computation
            /**
             * SRP x Downpayment Rate = Downpayment Amount
             * SRP - Downpayment Amount = Balance
             * Balance x Interest Rate / Duration = Monthly Amortization Amount
             */
            switch(loDetail.getPaymentForm()){
                case VehicleQuotationStatus.PaymentForm.FINANCING: //Financing 
                    if(ldblInterestRate > 0 && lnDuration > 0){
                        ldblDownpaymentAmount = ldblSRPAmt * (Detail(0).getDownPaymentPercent()/100);
                        ldblBalance = ldblSRPAmt - ldblDownpaymentAmount;
                        ldblInterestRate = ((ldblInterestRate/100)+1);
                        ldblMontlyAmortizationAmt = ((ldblBalance * ldblInterestRate) / lnDuration);//((ldblBalance * ldblInterestRate) / lnDuration)+2 = (double) NaN
                        ldblMontlyAmortizationAmt = Double.parseDouble(format.format(ldblMontlyAmortizationAmt));
                        ldblFinancingAmount = ldblSRPAmt-ldblDPAmt;
                        ldblGrossMonthlyInstallment = ldblMontlyAmortizationAmt-Detail(0).getPromptPaymentDiscount();
                        ldblPromissoryNoteAmt = lnDuration * ldblGrossMonthlyInstallment;
                    }
                break;
                case VehicleQuotationStatus.PaymentForm.PO: //PO 
                    ldblFinancingAmount = ldblSRPAmt-(ldblDPAmt+ldblTotalDisc);
                    loDetail.setDuration(0);
                    loDetail.setRateValue(0.00);
                    loDetail.setPromptPaymentDiscount(0.0000);
                break;
                default: //Cash
                    if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
                        loDetail.setDuration(0);
                        loDetail.setRateValue(0.00);
                        loDetail.setPromptPaymentDiscount(0.0000);
                        loDetail.setStandardRateId("");
                        loDetail.setBankId("");
                    }
                break;
            }
            
        } else {
            paAddOn = new ArrayList<>();
            paDiscount = new ArrayList<>();
            loDetail.setDownPayment(0.00);
            loDetail.setDownPaymentPercent(0.00);
            loDetail.setReservationAmount(0.00);
            loDetail.setCHMOAmount(0.00);
            loDetail.setTPLAmount(0.00);
            loDetail.setLTOAmount(0.00);
            loDetail.setComprehensiveAmount(0.00);
            loDetail.setPromptPaymentDiscount(0.00);
            loDetail.setStandardRateId("");
            loDetail.setBankId("");
            loDetail.setRateValue(0.00);
            loDetail.setDuration(0);
        }
        if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
            Detail(0).setDownPaymentPercent(ldblDownpaymentPercentage);
            Detail(0).setFinancingAmount(ldblFinancingAmount);
            Detail(0).setMonthlyAmount(ldblMontlyAmortizationAmt);
            Detail(0).setGrossMonthlyInstallment(ldblGrossMonthlyInstallment);
            Detail(0).setPromissoryNoteAmt(ldblPromissoryNoteAmt);
            Detail(0).setTransactionTotal(ldblTranTotal);
//            Detail(0).setDiscountAmount(ldblTotalDisc);
            Detail(0).setTotalAmount(ldblTranTotal - ldblTotalDisc);
        }
    }
    
    public JSONObject validatePromptPaymentDiscount(Double fdblDiscount){
        poJSON = new JSONObject();
        if(fdblDiscount > Detail(0).getFinancingAmount()){
            return setJSON("error", "Discount cannot be greater than fincance amount.");
        }
        if(fdblDiscount > Detail(0).getMonthlyAmount()){
            return setJSON("error", "Discount cannot be greater than monthly amortization.");
        }
        if(fdblDiscount > Detail(0).getTransactionTotal()){
            return setJSON("error", "Discount cannot be greater than transaction total.");
        }
        if(fdblDiscount > Detail(0).getTotalAmount()){
            return setJSON("error", "Discount cannot be greater than total amount.");
        }
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    public JSONObject validateDiscount(Double fdblDiscount){
        poJSON = new JSONObject();
        fdblDiscount = fdblDiscount + getTotalDiscount();
        if(fdblDiscount > Detail(0).getSRPAmount()){
            return setJSON("error", "Discount cannot be greater than SRP amount.");
        }
        if(fdblDiscount > Detail(0).getTransactionTotal()){
            return setJSON("error", "Discount cannot be greater than transaction total.");
        }
        if(fdblDiscount > Detail(0).getTotalAmount()){
            return setJSON("error", "Discount cannot be greater than total amount.");
        }
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    public Double getOtherAddOnTotal(){
        Double ldblTotalAddOn = 0.00;
        
        try {
            //Compute Add-On
            String lsAddOnType = "";
            Model_Quotation_AddOn loAddOn;
            for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
                loAddOn = AddOnList(lnCtr);
                if(checkEmpty(loAddOn.getAddOnId())
//                    || checkEmpty(loAddOn.getAddOnDescription())
                    ){
                    continue;
                }

                lsAddOnType = loAddOn.AddOnMaster().AddOnType().getAddTypeName();
                if(checkEmpty(lsAddOnType)){
//                    if(checkEmpty(loAddOn.getAddOnDescription())
//                    ){
                        continue;
//                    }
                }
                lsAddOnType = lsAddOnType.toUpperCase();
                if(lsAddOnType.contains(VehicleQuotationStatus.AddOnType.TPL)
                    || lsAddOnType.contains(VehicleQuotationStatus.AddOnType.LTO)
                    || (lsAddOnType.contains(VehicleQuotationStatus.AddOnType.COMPREHENSIVE))){
                    continue;
                }
                
                //compute other add on amount
                if(loAddOn.getRecordStatus()){
                    ldblTotalAddOn = ldblTotalAddOn + loAddOn.getTotalAmount();
                }
            }

            //Compute Quotation Freebie
            Model_Quotation_Freebie loFreebie;
            for(int lnCtr = 0;lnCtr < getFreebiesListCount();lnCtr++){
                loFreebie = FreebiesList(lnCtr);
                if(loFreebie.getRecordStatus() && loFreebie.getAmount() > 0.00){
                    if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
                        loFreebie.setTotalAmount(loFreebie.getAmount() * loFreebie.getQuantity());
                    }
                    ldblTotalAddOn = ldblTotalAddOn + loFreebie.getTotalAmount();
                }
            }

            //Compute Quotation Inclusion
            Model_Quotation_Inclusion loInclusion;
            for(int lnCtr = 0;lnCtr < getInclusionListCount();lnCtr++){
                loInclusion = InclusionList(lnCtr);
                if(loInclusion.getRecordStatus() && loInclusion.getAmount() > 0.00){
                    if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
                        loInclusion.setTotalAmount(loInclusion.getAmount() * loInclusion.getQuantity());
                    }
                    ldblTotalAddOn = ldblTotalAddOn + loInclusion.getTotalAmount();
                }
            }
            
            //Compute Special Request
            for(int lnCtr =0;lnCtr < getSpecialRequestListCount();lnCtr++){
                Model_Quotation_Special_Request loSReq = SpecialRequestList(lnCtr);
                if(loSReq.getRecordStatus() && checkNotEmpty(loSReq.getRequestDescription())){
                    if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
                        loSReq.setTotalAmount(loSReq.getQuantity() * loSReq.getAmount());
                    }
                    switch(loSReq.getChargeTo()){
                        case VehicleQuotationStatus.ChargeTo.CLIENT:
                        default:
                            ldblTotalAddOn = ldblTotalAddOn + loSReq.getTotalAmount();
                        break;
                    }
                }
            }
        
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return ldblTotalAddOn;
    }
    
    public JSONObject ValidateDuplicateSpecialRequest(String fsDescription, int row){
        poJSON = new JSONObject();
        if(checkNotEmpty(fsDescription)){
            fsDescription = fsDescription.replace(" ", "").toUpperCase();
            for(int lnCtr =0;lnCtr < getSpecialRequestListCount();lnCtr++){
                Model_Quotation_Special_Request loSReq = SpecialRequestList(lnCtr);
                if(checkEmpty(loSReq.getRequestDescription())){
                    continue;
                }
                if(row == lnCtr){
                    continue;
                }
                if(fsDescription.equals(loSReq.getRequestDescription().replace(" ", "").toUpperCase())){
                    if(loSReq.getRecordStatus()){
                        poJSON = setJSON("error", loSReq.getRequestDescription() + " already exist.");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    } else {
                        loSReq.setRecordStatus(true);
                        poJSON = setJSON("success", "success");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }
                }
            }
        }
        poJSON = setJSON("success", "success");
        poJSON.put("row", row);
        return poJSON;
    }
    
    public Double getTotalDiscount(){
        Double ldblTotalDisc = 0.00;
        for(int lnCtr = 0;lnCtr < getDiscountListCount();lnCtr++){
            if(DiscountList(lnCtr).getRecordStatus()){
                ldblTotalDisc = ldblTotalDisc + DiscountList(lnCtr).getDiscountAmount();
            }
        }
        
        return ldblTotalDisc;
    }
    
    private JSONObject setAmount(Double fdblAmount, String fsType){
        poJSON = new JSONObject();
        try {
            Model_Quotation_AddOn loAddOn;
            String lsAddOnType = "";
            for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
                lsAddOnType = "";
                loAddOn = AddOnList(lnCtr);
                if(checkEmpty(loAddOn.getAddOnId())){
                    continue;
                }

                lsAddOnType = loAddOn.AddOnMaster().AddOnType().getAddTypeName().toUpperCase();
                if(checkEmpty(lsAddOnType)){
                    continue;
                }
                if(lsAddOnType.contains(fsType) ){
                    if(fdblAmount <= 0.00 ){
                        if(loAddOn.getEditMode() == EditMode.ADDNEW){
                            AddOnList().remove(loAddOn);
                            break;
                        } else {
                            poJSON = loAddOn.setQuantity(0);
                            poJSON = loAddOn.setRecordStatus(false);
                        }
                        fdblAmount = loAddOn.AddOnMaster().getAmount();
                        poJSON = loAddOn.setDiscountAmount(loAddOn.AddOnMaster().getAmount());
                    } 
                    poJSON = loAddOn.setAmount(fdblAmount);
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return poJSON;
    }
    
    public boolean isDisable(String fsType){
        if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
        } else {
            return true;
        }
        
        try {
            Model_Quotation_AddOn loAddOn;
            String lsAddOnType = "";
            for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
                lsAddOnType = "";
                loAddOn = AddOnList(lnCtr);
                if(checkEmpty(loAddOn.getAddOnId())){
                    continue;
                }

                lsAddOnType = loAddOn.AddOnMaster().AddOnType().getAddTypeName().toUpperCase();
                if(checkEmpty(lsAddOnType)){
                    continue;
                }
                if(lsAddOnType.contains(fsType) ){
                    return true;
                }
            }
            
            boolean lbHasStndrd = findStandardAddOn( fsType);
            if(lbHasStndrd){
                return true;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return false;
    }
    
    public boolean findStandardAddOn(String fsType){
        try {
            poJSON = new JSONObject();
            String lsSQL = VehicleAddOn_SQL();
            lsSQL = MiscUtil.addCondition(lsSQL, SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate "
                    + " AND b.sVrntIDxx = " + SQLUtil.toSQL(Detail(0).getVariantId())
                    + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                    + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
            );
            System.out.println("AutoLoadVehicleAddOn SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            ReloadAddOnList();
            if (MiscUtil.RecordCount(loRS) > 0) {
                while(loRS.next()){
                    String lsAddOnId = loRS.getString("sAddOnIDx");
                    String lsAddOnName = loRS.getString("sAddTypNm");
                    Double ldblAmount = loRS.getDouble("nAmountxx");
                    int lnRow = getAddOnListCount() - 1;
                    if(checkNotEmpty(lsAddOnName)){
                        lsAddOnName = lsAddOnName.toUpperCase();
                        if(lsAddOnName.contains(fsType)) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException | CloneNotSupportedException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    
        return false;
    }
    
    @Override
    public JSONObject initFields() {
        poJSON = new JSONObject();
        
        try {
            //Put initial model values here/
            Master().setIndustryId(psIndustryId);
            Master().setCategoryCode(psCategoryId);
            
            Date ldServerDate = poGRider.getServerDate();
            Master().setTransactionDate(SQLUtil.toDate(xsDateShort(ldServerDate), SQLUtil.FORMAT_SHORT_DATE));
            Detail(0).setExpectedDate(SQLUtil.toDate(xsDateShort(ldServerDate), SQLUtil.FORMAT_SHORT_DATE));

            Calendar loThruDate = Calendar.getInstance();
            loThruDate.setTime(ldServerDate);
            loThruDate.add(Calendar.MONTH, 1);

            Detail(0).setValidThruDate(SQLUtil.toDate(xsDateShort(loThruDate.getTime()), SQLUtil.FORMAT_SHORT_DATE));
            Detail(0).setBranchCode(poGRider.getBranchCode());
            if(checkNotEmpty(Detail(0).BranchOthers().getBrandID())){
                Detail(0).setBrandId(Detail(0).BranchOthers().getBrandID());
            }
            Detail(0).setPaymentForm("0");//default value
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        VehicleQuotationValidator loValidator = new VehicleQuotationValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        loValidator.setQuotationVehicle(Detail(0));
        poJSON = loValidator.validate();
        return poJSON;
    }
    
    private boolean validateStandardDetails(){
        boolean lbValidate = false;
        try {
            String lsSQL = "";
            ResultSet loRS;
            
            //Check Standard Financing Rate for Duration and Rate Value
            if(!"0".equals(Detail(0).getPaymentForm())){
//                if(checkNotEmpty(Detail(0).getBankId())){ //if bank is not empty
//                    Model_Financing_Rate_Master loRate = new SalesModels(poGRider).FinancingRateMaster();
//                    lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(loRate),
//                                                            " cRecdStat = " + SQLUtil.toSQL(FinancingRateStatus.ACTIVE)
//                                                            + " AND nDuration = " + SQLUtil.toSQL(Detail(0).getDuration())
//                                                            + " AND nRateValx = " + SQLUtil.toSQL(Detail(0).getRateValue())
//                                                            + " AND sRateIDxx = " + SQLUtil.toSQL(Detail(0).getQuotationRate())
//                                                            );
//                    System.out.println("Validate Financing Rate SQL: " + lsSQL);
//                    loRS = poGRider.executeQuery(lsSQL);
//                    if (MiscUtil.RecordCount(loRS) > 0) {
//                    } else {
//                        lbValidate = true;
//                    }
//                }
                
//                if(!lbValidate){   
//                    Model_Vehicle_Financing_Rates lObj = new SalesModels(poGRider).VehicleFinancingRates();
//                    lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(lObj),
//                                                            " cRecdStat = " + SQLUtil.toSQL(FinancingRateStatus.ACTIVE)
//                                                            + " AND nDuration = " + SQLUtil.toSQL(Detail(0).getDuration())
//                                                            + " AND nRateValx = " + SQLUtil.toSQL(Detail(0).getRateValue())
//                                                            + " AND (( " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN dFromDate AND dThruDate )"
//                                                            + " OR (dFromDate <= " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate()))
//                                                            + " AND dThruDate IS NULL " 
//                                                            + ")) "
//    //                                                        + " AND " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) +  " BETWEEN dFromDate AND dThruDate " 
//                                                            );
//                    System.out.println("Validate Standard Financing Rate SQL: " + lsSQL);
//                    loRS = poGRider.executeQuery(lsSQL);
//                    if (MiscUtil.RecordCount(loRS) > 0) {
//                    } else {
//                        lbValidate = true;
//                    }
//                }
                
                //Validate when quotrate is empty
                lbValidate = checkEmpty(Detail(0).getStandardRateId());
                
                //Check Standard Downpayment rate
                if(!lbValidate){
                    lsSQL = MiscUtil.addCondition(VehicleFinancing_SQL(), "(( " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate )"
                                                            + " OR (a.dFromDate <= " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate()))
                                                            + " AND a.dThruDate IS NULL " 
                                                            + ")) "
                                                            + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                                            + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                                                            + " AND b.sVrntIDxx = " + SQLUtil.toSQL(Detail(0).getVariantId())
                                                            + " AND b.nDPRatexx =  " + SQLUtil.toSQL(Detail(0).getDownPaymentPercent())
                                                            );
                    System.out.println("Validate Standard Downpayment Rate SQL: " + lsSQL);
                    loRS = poGRider.executeQuery(lsSQL);
                    if (MiscUtil.RecordCount(loRS) > 0) {
                    } else {
                        lbValidate = true;
                    }
                }
            
                if(!lbValidate){
                    if(Detail(0).getPromptPaymentDiscount() > 0.00){
                        lbValidate = true;
                    }
                }
            }
            
            //Check Standard Add Ons
            if(!lbValidate){
                for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
                    if(!lbValidate){
                        if(checkEmpty(AddOnList(lnCtr).getAddOnId())){
                            continue;
                        }
                        lsSQL = MiscUtil.addCondition(VehicleAddOn_SQL(), SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN a.dFromDate AND a.dThruDate "
                                                                + " AND a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                                                + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                                                                + " AND b.sVrntIDxx = " + SQLUtil.toSQL(Detail(0).getVariantId())
                                                                + " AND b.sAddOnIDx = " + SQLUtil.toSQL(AddOnList(lnCtr).getAddOnId())
                                                                );
                        System.out.println("Validate Standard Vehicle Add Ons SQL: " + lsSQL);
                        loRS = poGRider.executeQuery(lsSQL);
                        if (MiscUtil.RecordCount(loRS) > 0) {
                            if(loRS.next()){
                                Double ldblAmount = loRS.getDouble("nAmountxx");
                                if(!Objects.equals(ldblAmount, AddOnList(lnCtr).getAmount())){
                                   lbValidate = true;
                                }
                            }
                        } else {
                            lbValidate = true;
                            break;
                        }
                    }
                }
            }
            
            //validate if found discount
            if(!lbValidate){
                if(getTotalDiscount() > 0.00){
                    lbValidate = true;
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } 
            
        return lbValidate;
    }
    
    private String getStandardRateId() throws SQLException{
        String lsStdRateId = "";
        Model_Vehicle_Financing_Rates lObj = new SalesModels(poGRider).VehicleFinancingRates();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(lObj),
                                                " cRecdStat = " + SQLUtil.toSQL(FinancingRateStatus.ACTIVE)
                                                + " AND nDuration = " + SQLUtil.toSQL(Detail(0).getDuration())
                                                + " AND nRateValx = " + SQLUtil.toSQL(Detail(0).getRateValue())
                                                + " AND (( " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN dFromDate AND dThruDate )"
                                                + " OR (dFromDate <= " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate()))
                                                + " AND dThruDate IS NULL " 
                                                + ")) "
                                                );
        System.out.println("Validate Standard Financing Rate SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0) {
            if(loRS.next()){
                lsStdRateId = loRS.getString("sStdRteID");
            }
        } 
        
        MiscUtil.close(loRS);
        
        return lsStdRateId;
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
        String lsStdRateId = getStandardRateId();
        if(checkNotEmpty(lsStdRateId)){
            Detail(0).setStandardRateId(lsStdRateId);
        }

        poJSON = isEntryOkay(Master().getTransactionStatus());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        if (getTotalDiscount() > Detail(0).getTransactionTotal()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Discount amount cannot be greater than the transaction total.");
            return poJSON;
        }
        
//        if(pbWithUI){
//            if(validateStandardDetails()){ //if true require approve upon saving
//                poJSON = callApproval();
//                if(!isJSONSuccess(poJSON)){
//                    return poJSON;
//                }
//            }
//        }
        
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sVrntIDxx");
            if ((lsDetail == null || "".equals(lsDetail))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    detail.remove(); // Correctly remove the item
                } 
            } 
        }

        if (getDetailCount() <= 0) {
            poJSON = setJSON("error", "No record detail to be save.");
            return poJSON;
        }
        
        for (int lnCtr = 0; lnCtr < getDetailCount(); lnCtr++) {
            if(Detail(lnCtr).getSRPAmount() == 0.0000){
                return setJSON("error", "Invalid SRP amount.");
            }
            
//            System.out.println("Record Status : " + Detail(lnCtr).getRecordStatus());
//            if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
//                Detail(lnCtr).setQuotationPaymentId(Detail(lnCtr).getNextCode());
//            }
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        //Vehicle Quotation Add ON
        Iterator<Model_Quotation_AddOn> addon = paAddOn.iterator();
        while (addon.hasNext()) {
            Model item = addon.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sAddOnIDx");
            int lnQty = (int) item.getValue("nQuantity");
            if ((lsDetail == null || "".equals(lsDetail))
                || lnQty <= 0){
                if(item.getEditMode() == EditMode.ADDNEW){
                    addon.remove(); // Correctly remove the item
                } 
            }
        }

        for (int lnCtr = 0; lnCtr < getAddOnListCount(); lnCtr++) {
            if(checkNotEmpty(AddOnList(lnCtr).getAddOnId())){
                AddOnList(lnCtr).setTransactionNo(Master().getTransactionNo());
                AddOnList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
                AddOnList(lnCtr).setModifiedDate(poGRider.getServerDate());
            }
        }
        
        //Vehicle Quotation Freebies
        Iterator<Model_Quotation_Freebie> freebies = paFreebies.iterator();
        while (detail.hasNext()) {
            Model item = freebies.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sFreebIDx");
            int lnQty = (int) item.getValue("nQuantity");
            if ((lsDetail == null || "".equals(lsDetail))
                || lnQty <= 0){
                if(item.getEditMode() == EditMode.ADDNEW){
                    freebies.remove(); // Correctly remove the item
                } 
            }
        }

        for (int lnCtr = 0; lnCtr < getFreebiesListCount(); lnCtr++) {
            if(checkNotEmpty(FreebiesList(lnCtr).getFreebieId())){
                if(!FreebiesList(lnCtr).getIsFree()){
                    if(FreebiesList(lnCtr).getAmount() <= 0.00){
                        return setJSON("error", "Invalid Add-On amount for "+(FreebiesList(lnCtr).Freebie().getDescription()));
                    }
                }
                FreebiesList(lnCtr).setTransactionNo(Master().getTransactionNo());
                FreebiesList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
                FreebiesList(lnCtr).setModifiedDate(poGRider.getServerDate());
            }
        }
        
        //Vehicle Quotation Inclusion
        Iterator<Model_Quotation_Inclusion> inclusion = paInclusion.iterator();
        while (inclusion.hasNext()) {
            Model item = inclusion.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sInclIDxx");
            int lnQty = (int) item.getValue("nQuantity");
            if ((lsDetail == null || "".equals(lsDetail))
                || lnQty <= 0){
                if(item.getEditMode() == EditMode.ADDNEW){
                    inclusion.remove(); // Correctly remove the item
                } 
            }
        }

        for (int lnCtr = 0; lnCtr < getInclusionListCount(); lnCtr++) {
            if(checkNotEmpty(InclusionList(lnCtr).getInclusionId())){
                if(!InclusionList(lnCtr).getIsFree()){
                    if(InclusionList(lnCtr).getAmount() <= 0.00){
                        return setJSON("error", "Invalid Add-On amount for "+(InclusionList(lnCtr).Inclusion().getDescription()));
                    }
                }
                
                InclusionList(lnCtr).setTransactionNo(Master().getTransactionNo());
                InclusionList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
                InclusionList(lnCtr).setModifiedDate(poGRider.getServerDate());
            }
        }
        
        //Vehicle Quotation Special Request
        Iterator<Model_Quotation_Special_Request> specialrequest = paSpecialRequest.iterator();
        while (specialrequest.hasNext()) {
            Model item = specialrequest.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sReqstDsc");
            int lnQty = (int) item.getValue("nQuantity");
            if ((lsDetail == null || "".equals(lsDetail))
                || lnQty <= 0){
                if(item.getEditMode() == EditMode.ADDNEW){
                    specialrequest.remove(); // Correctly remove the item
                } 
            }
        }

        for (int lnCtr = 0; lnCtr < getSpecialRequestListCount(); lnCtr++) {
            if(checkNotEmpty(SpecialRequestList(lnCtr).getRequestDescription())){
                if(VehicleQuotationStatus.ChargeTo.CLIENT.equals(SpecialRequestList(lnCtr).getChargeTo())){
                    if(SpecialRequestList(lnCtr).getAmount() <= 0.00){
                        poJSON = setJSON("error", "Amount is not set for special request : " + SpecialRequestList(lnCtr).getRequestDescription() );
                        return poJSON;
                    }
                }
                
                SpecialRequestList(lnCtr).setTransactionNo(Master().getTransactionNo());
                SpecialRequestList(lnCtr).setEntryNo(lnCtr+1);
                SpecialRequestList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
                SpecialRequestList(lnCtr).setModifiedDate(poGRider.getServerDate());
            }
        }
        
        //Vehicle Quotation Discount
        Iterator<Model_Quotation_Discount> discount = paDiscount.iterator();
        while (discount.hasNext()) {
            Model item = discount.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sDiscType");
            Double ldblDetail = Double.valueOf(String.valueOf(item.getValue("nDiscAmnt")));
            if (checkEmpty(lsDetail) || ldblDetail <= 0.00){
                if(item.getEditMode() == EditMode.ADDNEW){
                    discount.remove(); // Correctly remove the item
                } 
            }
        }

        for (int lnCtr = 0; lnCtr < getDiscountListCount(); lnCtr++) {
            if(checkEmpty(DiscountList(lnCtr).getDiscountType())){
                return setJSON("error", "Discount type at row "+(lnCtr+1)+" cannot be empty.");
            }
            DiscountList(lnCtr).setEntryNo(lnCtr+1);
            DiscountList(lnCtr).setTransactionNo(Master().getTransactionNo());
            DiscountList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            DiscountList(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        //assign other info on attachment
        for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount()- 1; lnCtr++) {
            TransactionAttachmentList(lnCtr).getModel().setSourceNo(Master().getTransactionNo());
            TransactionAttachmentList(lnCtr).getModel().setSourceCode(SOURCE_CODE);
            TransactionAttachmentList(lnCtr).getModel().setBranchCode(poGRider.getBranchCode());
            TransactionAttachmentList(lnCtr).getModel().setImagePath(System.getProperty("sys.default.path.temp.attachments"));
            
            String lsOriginalFileName = TransactionAttachmentList(lnCtr).getModel().getFileName();
            //Check existing file name in database
            if(EditMode.ADDNEW == TransactionAttachmentList(lnCtr).getModel().getEditMode()
                && "0".equals(TransactionAttachmentList(lnCtr).getModel().getSendStatus())){
                int lnCopies = 0;
                String fsFilePath = TransactionAttachmentList(lnCtr).getModel().getImagePath() + "/" + TransactionAttachmentList(lnCtr).getModel().getFileName();
                String lsNewFileName = TransactionAttachmentList(lnCtr).getModel().getFileName();
                while ("error".equals((String)checkExistingFileName(lsNewFileName).get("result"))) {
                    lnCopies++;
                    //Rename the file
                    int dotIndex = TransactionAttachmentList(lnCtr).getModel().getFileName().lastIndexOf(".");
                    if (dotIndex == -1) {
                        lsNewFileName = TransactionAttachmentList(lnCtr).getModel().getFileName() +"_"+lnCopies;
                    } else {
                        lsNewFileName = TransactionAttachmentList(lnCtr).getModel().getFileName().substring(0, dotIndex) +"_"+ lnCopies +TransactionAttachmentList(lnCtr).getModel().getFileName().substring(dotIndex);
                    }
                }

                if(lnCopies > 0){
                    Path source = Paths.get(fsFilePath);
                    try {
                        // Copy file into the target directory with a new name
                        Path target = Paths.get(System.getProperty("sys.default.path.temp.attachments")).resolve(lsNewFileName);
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        //check if file is existing
                        int lnChecker = 0;
                        File file = new File(TransactionAttachmentList(lnCtr).getModel().getImagePath() + "/" + lsNewFileName);
                        while(!file.exists() && lnChecker < 5){
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
                if("0".equals(TransactionAttachmentList(lnCtr).getModel().getSendStatus())){
                    poJSON = uploadCASAttachments(poGRider, System.getProperty("sys.default.access.token"), lnCtr,lsOriginalFileName);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }
            
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
            //Save Add Ons
            for (int lnCtr = 0; lnCtr <= getAddOnListCount() - 1; lnCtr++) {
                if(checkNotEmpty(AddOnList(lnCtr).getAddOnId())){
                    if(AddOnList(lnCtr).getEditMode() == EditMode.ADDNEW
                        && !AddOnList(lnCtr).getRecordStatus()){
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
            }
            
            //Save Freebies
             for (int lnCtr = 0; lnCtr <= getFreebiesListCount() - 1; lnCtr++) {
                if(checkNotEmpty(FreebiesList(lnCtr).getFreebieId())){
                    if(FreebiesList(lnCtr).getEditMode() == EditMode.ADDNEW
                        && !FreebiesList(lnCtr).getRecordStatus()){
                        continue;
                    }
                    if(FreebiesList(lnCtr).getEditMode() == EditMode.ADDNEW || FreebiesList(lnCtr).getEditMode() == EditMode.UPDATE){
                        if(FreebiesList(lnCtr).getEditMode() == EditMode.ADDNEW){
                            FreebiesList(lnCtr).setQuotationFreebieId(FreebiesList(lnCtr).getNextCode());
                        }
                        poJSON = FreebiesList(lnCtr).saveRecord();
                        if(!isJSONSuccess(poJSON)){
                            return poJSON;
                        }
                    }
                }
            }
             
            //Save Inclusion
             for (int lnCtr = 0; lnCtr <= getInclusionListCount() - 1; lnCtr++) {
                if(checkNotEmpty(InclusionList(lnCtr).getInclusionId())){
                    if(InclusionList(lnCtr).getEditMode() == EditMode.ADDNEW
                        && !InclusionList(lnCtr).getRecordStatus()){
                        continue;
                    }
                    if(InclusionList(lnCtr).getEditMode() == EditMode.ADDNEW || InclusionList(lnCtr).getEditMode() == EditMode.UPDATE){
                        if(InclusionList(lnCtr).getEditMode() == EditMode.ADDNEW){
                            InclusionList(lnCtr).setQuotationInclusionId(InclusionList(lnCtr).getNextCode());
                        }
                        poJSON = InclusionList(lnCtr).saveRecord();
                        if(!isJSONSuccess(poJSON)){
                            return poJSON;
                        }
                    }
                }
            }
             
            //Save Special Request
             for (int lnCtr = 0; lnCtr < getSpecialRequestListCount(); lnCtr++) {
                if(checkNotEmpty(SpecialRequestList(lnCtr).getRequestDescription())){
                    System.out.println("RECORD STATUS : " + SpecialRequestList(lnCtr).getRecordStatus());
                    System.out.println("Request : " + SpecialRequestList(lnCtr).getRequestDescription());
                    if(SpecialRequestList(lnCtr).getEditMode() == EditMode.ADDNEW
                        && !SpecialRequestList(lnCtr).getRecordStatus()){
                        continue;
                    }
                    if(SpecialRequestList(lnCtr).getEditMode() == EditMode.ADDNEW || SpecialRequestList(lnCtr).getEditMode() == EditMode.UPDATE){
                        if(SpecialRequestList(lnCtr).getEditMode() == EditMode.ADDNEW){
                            SpecialRequestList(lnCtr).setEntryNo(lnCtr+1);
                        }
                        poJSON = SpecialRequestList(lnCtr).saveRecord();
                        if(!isJSONSuccess(poJSON)){
                            return poJSON;
                        }
                    }
                }
            }
             
             //Save Discount
             for (int lnCtr = 0; lnCtr <= getDiscountListCount() - 1; lnCtr++) {
                if(DiscountList(lnCtr).getEditMode() == EditMode.ADDNEW
                    && !DiscountList(lnCtr).getRecordStatus()){
                    continue;
                }
                if(DiscountList(lnCtr).getEditMode() == EditMode.ADDNEW || DiscountList(lnCtr).getEditMode() == EditMode.UPDATE){
                    poJSON = DiscountList(lnCtr).saveRecord();
                    if(!isJSONSuccess(poJSON)){
                        return poJSON;
                    }
                }
            }
            
            //Save Attachments
            for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
                if (paAttachments.get(lnCtr).getEditMode() == EditMode.ADDNEW || paAttachments.get(lnCtr).getEditMode() == EditMode.UPDATE) {
                    paAttachments.get(lnCtr).getModel().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
                    paAttachments.get(lnCtr).getModel().setModifiedDate(poGRider.getServerDate());
                    paAttachments.get(lnCtr).setWithParentClass(true);
                    System.out.println("CHECK ATTACHMENT RECORD STAT : " + paAttachments.get(lnCtr).getModel().getRecordStatus());
                    poJSON = paAttachments.get(lnCtr).saveRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
            
        } catch (ExceptionInInitializerError | SQLException | GuanzonException | CloneNotSupportedException ex) {
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

                lsCondition = "a.cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "a.cTranStat = " + SQLUtil.toSQL(psTranStat);
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
                " c.sCompnyNm, " +
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
                "LEFT JOIN Client_Master c ON c.sClientID = a.sClientID "+
                "LEFT JOIN Sales_Inquiry_Master d ON d.sTransNox = b.sSourceNo "; 
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
            + " , c.nSelPrice            "
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
    
    private String VehicleFreebieInclusion_SQL(boolean includeAddOn){
        return      "SELECT * " 
                    + "FROM ( " 
                    + "    SELECT " 
                    + "        a.sFreebIDx AS sIDxxxxxx, " 
                    + "        a.sFreebDsc AS sDescxxxx, " 
                    + "        1 AS sTypexxxx, "  //0 = freebie
                    + "        a.cRecdStat AS cRecdStat " 
                    + "    FROM Freebie_Master a " 
                    + " " 
                    + "    UNION ALL " 
                    + " " 
                    + "    SELECT " 
                    + "        b.sInclIDxx AS sIDxxxxxx, " 
                    + "        b.sInclDesc AS sDescxxxx, " 
                    + "        2 AS sTypexxxx, "  //1 = inclusion
                    + "        b.cRecdStat AS cRecdStat " 
                    + "    FROM Vehicle_Inclusion_Master b " 
                    + (includeAddOn ? 
                    " " 
                        + "    UNION ALL " 
                        + " " 
                        + "   SELECT      " 
                        + "  d.sAddOnIDx AS sIDxxxxxx "
                        + " , e.sAddTypNm AS sDescxxxx "
                        + " , 0 AS sTypexxxx "
                        + " , c.cRecdStat AS cRecdStat " 
                        + " FROM Validity_Period_Master c " 
                        + " INNER JOIN Vehicle_AddOn_Master d ON d.sValidIDx = c.sValidIDx "
                                                            + " AND d.sVrntIDxx = " + SQLUtil.toSQL(Detail(0).getVariantId())
                                                            + " AND " + SQLUtil.toSQL(xsDateShort(Master().getTransactionDate())) + " BETWEEN c.dFromDate AND c.dThruDate "
                                                            + " AND c.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                        + " LEFT JOIN Vehicle_AddOn_Type e ON e.sAddTypex = d.sAddTypex    "
                    : "") 
                    + ") AS x";
        
    }
    
    private String PriceList_SQL() {
        return "SELECT " +
                "  a.sValidIDx, " +
                "  a.sCompnyID, " +
                "  a.dFromDate, " +
                "  a.dThruDate, " +
                "  a.sValidDsc, " +
                "  a.cRecdStat, " +
                "  a.dModified, " +
                "     CASE  " +
                "         WHEN a.cRecdStat = '0'  " +
                "         THEN 'OPEN'  " +
                "         WHEN a.cRecdStat = '1'  " +
                "         THEN 'APPROVED'  " +
                "         WHEN a.cRecdStat = '2'  " +
                "         THEN 'CANCELLED'  " +
                "         WHEN a.cRecdStat = '3'  " +
                "         THEN 'VOIDED'  " +
                "         ELSE 'UNKNOWN' " +
                "     END AS xRecdStat, " +
                "  b.sVrntIDxx, " +
                "  b.nPriceYrx, " +
                "  b.nSRPAmntx, " +
                "  b.sModified," +
                "  b.dModified " +
                "FROM Validity_Period_Master a " +
                "INNER JOIN Vehicle_Price_Master b ON b.sValidIDx = a.sValidIDx";
    }
    
    private TransactionAttachment TransactionAttachment() throws SQLException, GuanzonException {
        return new SysTableContollers(poGRider, null).TransactionAttachment();
    }

    public TransactionAttachment TransactionAttachmentList(int row) {
        return (TransactionAttachment) paAttachments.get(row);
    }

    public int getTransactionAttachmentCount() {
        if (paAttachments == null) {
            paAttachments = new ArrayList<>();
        }

        return paAttachments.size();
    }
    
    /**
     * Adds a new blank transaction attachment entry.
     *
     * @return JSON result containing status and message.
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If attachment model operations fail.
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
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add transaction attachment.");
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Removes or deactivates an attachment entry by row index.
     *
     * @param fnRow Attachment row index.
     * @return JSON result containing status and message.
     * @throws GuanzonException If attachment model operations fail.
     * @throws SQLException If a database access error occurs.
     */
    public JSONObject removeAttachment(int fnRow) throws GuanzonException, SQLException{
        poJSON = new JSONObject();
        if(getTransactionAttachmentCount() <= 0){
            poJSON.put("result", "error");
            poJSON.put("message", "No transaction attachment to be removed.");
            return poJSON;
        }
        
        if(paAttachments.get(fnRow).getEditMode() == EditMode.ADDNEW){
            paAttachments.remove(fnRow);
            System.out.println("Attachment :"+ fnRow+" Removed");
        } else {
            paAttachments.get(fnRow).getModel().setRecordStatus(RecordStatus.INACTIVE);
            System.out.println("Attachment :"+ fnRow+" Deactivate");
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Adds or reactivates an attachment record for the provided file name.
     *
     * @param fFileName Attachment file name.
     * @return Row index of the active attachment entry.
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If attachment model operations fail.
     */
    public int addAttachment(String fFileName) throws SQLException, GuanzonException{
        for(int lnCtr = 0;lnCtr <= getTransactionAttachmentCount() - 1;lnCtr++){
            if(fFileName.equals(paAttachments.get(lnCtr).getModel().getFileName())
                && RecordStatus.INACTIVE.equals(paAttachments.get(lnCtr).getModel().getRecordStatus())){
                paAttachments.get(lnCtr).getModel().setRecordStatus(RecordStatus.ACTIVE);
                System.out.println("Attachment :"+ lnCtr+" Activate");
                return lnCtr;
            }
        }
        
        addAttachment();
        paAttachments.get(getTransactionAttachmentCount() - 1).getModel().setFileName(fFileName);
        paAttachments.get(getTransactionAttachmentCount() - 1).getModel().setSourceNo(Master().getTransactionNo());
        paAttachments.get(getTransactionAttachmentCount() - 1).getModel().setRecordStatus(RecordStatus.ACTIVE);
        return getTransactionAttachmentCount() - 1;
    }
    
    /**
     * Copies a local file into the temporary attachment directory.
     *
     * @param fsPath Source file path.
     */
    public void copyFile(String fsPath){
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

    /**
     * Loads and downloads existing attachments of the current transaction.
     *
     * @return JSON result containing status and message.
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If attachment model operations fail.
     */
    public JSONObject loadAttachments()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paAttachments = new ArrayList<>();

        TransactionAttachment loAttachment = new SysTableContollers(poGRider, null).TransactionAttachment();
        List loList = loAttachment.getAttachments(SOURCE_CODE, Master().getTransactionNo());
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
            if ("success".equals((String) poJSON.get("result"))) {
                if(getEditMode() == EditMode.UPDATE){
                   poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).updateRecord();
                }
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getTransactionNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceCode());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName());
            }
            
            //Download Attachments
            poJSON = WebFile.DownloadFile(WebFile.getAccessToken(System.getProperty("sys.default.access.token"))
                    , "0032" //Constant
                    , "" //Empty
                    , paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName()
                    , SOURCE_CODE
                    , paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo()
                    , "");
            if ("success".equals((String) poJSON.get("result"))) {
                
                poJSON = (JSONObject) poJSON.get("payload");
                if(WebFile.Base64ToFile((String) poJSON.get("data")
                        , (String) poJSON.get("hash")
                        , System.getProperty("sys.default.path.temp.attachments") + "/"
                        , (String) poJSON.get("filename"))){
                    System.out.println("poJSON success: " +  poJSON.toJSONString());
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
        return poJSON;
    }
    
    /**
     * Checks whether a file name already exists in attachment records.
     *
     * @param fsFileName File name to validate.
     * @return JSON result indicating duplicate detection.
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If attachment model operations fail.
     */
    public JSONObject checkExistingFileName(String fsFileName) throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(TransactionAttachment().getModel()), 
                                                                    " sFileName = " + SQLUtil.toSQL(fsFileName)
                                                                    );
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(loRS.next()){
                    if(loRS.getString("sFileName") != null && !"".equals(loRS.getString("sFileName"))){
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
    
    /**
     * Upload Attachment
     * @param instance
     * @param access 
     * @param fnRow 
     * @return  
     * @throws java.lang.Exception 
     */
    public JSONObject uploadCASAttachments(GRiderCAS instance, String access, int fnRow, String fsOriginalFileName) throws Exception{       
        poJSON = new JSONObject();
        System.out.println("Uploading... : fsOriginalFileName : " + fsOriginalFileName);
        System.out.println("New File Name... : " + paAttachments.get(fnRow).getModel().getFileName());
        String hash;
        String lsFile = paAttachments.get(fnRow).getModel().getFileName();
        
        //check if new file is existing
        File file = new File(paAttachments.get(fnRow).getModel().getImagePath() + "/" + lsFile);
        if(!file.exists()){
            //check if original file is existing
            lsFile = fsOriginalFileName;
            file = new File(paAttachments.get(fnRow).getModel().getImagePath() + "/" + lsFile);
            if(!file.exists()){
                poJSON.put("result", "error");
                poJSON.put("message", "Cannot locate file in " + paAttachments.get(fnRow).getModel().getImagePath() + "/" + lsFile
                                        + ".\nContact system administrator for assistance.");
                return poJSON;  
            }
        }

        //check if file hash is not empty
        hash = paAttachments.get(fnRow).getModel().getMD5Hash();
        if(paAttachments.get(fnRow).getModel().getMD5Hash() == null || "".equals(paAttachments.get(fnRow).getModel().getMD5Hash())){
            hash = MiscReplUtil.md5Hash(paAttachments.get(fnRow).getModel().getImagePath() + "/" + lsFile);
        }

        JSONObject result = WebFile.UploadFile(getAccessToken(access)
                                , "0032"
                                , ""
                                , paAttachments.get(fnRow).getModel().getFileName()
                                , instance.getBranchCode()
                                , hash
                                , encodeFileToBase64Binary(file)
                                , paAttachments.get(fnRow).getModel().getSourceCode()
                                , paAttachments.get(fnRow).getModel().getSourceNo()
                                , "");

        if("error".equalsIgnoreCase((String) result.get("result"))){
            System.out.println("Upload Error : " + result.toJSONString());
            System.out.println("Upload Error : " + paAttachments.get(fnRow).getModel().getFileName());
            poJSON.put("result", "error");
            poJSON.put("message", "System error while uploading file "+ paAttachments.get(fnRow).getModel().getFileName()
                                    + ".\nContact system administrator for assistance.");
            return poJSON;
        }
        paAttachments.get(fnRow).getModel().setMD5Hash(hash);
        paAttachments.get(fnRow).getModel().setSendStatus("1");
        System.out.println("Upload Success : " + paAttachments.get(fnRow).getModel().getFileName());
        poJSON.put("result", "success");
        return poJSON;
    }
    
    /**
     * Encodes a file into Base64 string content for upload.
     *
     * @param file File to encode.
     * @return Base64-encoded content.
     * @throws Exception If file reading fails.
     */
    private static String encodeFileToBase64Binary(File file) throws Exception{
         FileInputStream fileInputStreamReader = new FileInputStream(file);
         byte[] bytes = new byte[(int)file.length()];
         fileInputStreamReader.read(bytes);
         return new String(org.apache.commons.codec.binary.Base64.encodeBase64(bytes), "UTF-8");
    } 
    
    private static JSONObject token = null;
    /**
     * Loads and refreshes API access token from a local token file.
     *
     * @param access Token file path.
     * @return Access key value, or {@code null} when unavailable.
     */
    private static String getAccessToken(String access){
        try {
            JSONParser oParser = new JSONParser();
            if(token == null){
                token = (JSONObject)oParser.parse(new FileReader(access));
            }
            
            Calendar current_date = Calendar.getInstance();
            current_date.add(Calendar.MINUTE, -25);
            Calendar date_created = Calendar.getInstance();
            date_created.setTime(SQLUtil.toDate((String) token.get("created") , SQLUtil.FORMAT_TIMESTAMP));
            
            //Check if token is still valid within the time frame
            //Request new access token if not in the current period range
            if(current_date.after(date_created)){
                String[] xargs = new String[] {(String) token.get("parent"), access};
                RequestAccess.main(xargs);
                token = (JSONObject)oParser.parse(new FileReader(access));
            }
            
            return (String)token.get("access_key");
        } catch (IOException ex) {
            return null;
        } catch (ParseException ex) {
            return null;
        }
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
    
    private TransactionAttachment VehicleAttachment() throws SQLException, GuanzonException {
        return new SysTableContollers(poGRider, null).TransactionAttachment();
    }

    public TransactionAttachment VehicleAttachmentList(int row) {
        return (TransactionAttachment) paVehicleAttachments.get(row);
    }

    public int getVehicleAttachmentCount() {
        if (paVehicleAttachments == null) {
            paVehicleAttachments = new ArrayList<>();
        }

        return paVehicleAttachments.size();
    }
    
    /**
     * Loads and downloads existing attachments of the vehicle description
     *
     * @return JSON result containing status and message.
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If attachment model operations fail.
     */
    public JSONObject loadVehicleAttachments()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paVehicleAttachments = new ArrayList<>();
        String lsSourceCode = "Vrnt"; 
        TransactionAttachment loAttachment = new SysTableContollers(poGRider, null).TransactionAttachment();
        List loList = loAttachment.getAttachments(lsSourceCode, Detail(0).getVariantId());
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paVehicleAttachments.add(VehicleAttachment());
            poJSON = paVehicleAttachments.get(getVehicleAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
            if ("success".equals((String) poJSON.get("result"))) {
                System.out.println(paVehicleAttachments.get(getVehicleAttachmentCount() - 1).getModel().getTransactionNo());
                System.out.println(paVehicleAttachments.get(getVehicleAttachmentCount() - 1).getModel().getSourceNo());
                System.out.println(paVehicleAttachments.get(getVehicleAttachmentCount() - 1).getModel().getSourceCode());
                System.out.println(paVehicleAttachments.get(getVehicleAttachmentCount() - 1).getModel().getFileName());
            }
            
            //Download Attachments
            poJSON = WebFile.DownloadFile(WebFile.getAccessToken(System.getProperty("sys.default.access.token"))
                    , "0032" //Constant
                    , "" //Empty
                    , paVehicleAttachments.get(getVehicleAttachmentCount() - 1).getModel().getFileName()
                    , lsSourceCode
                    , paVehicleAttachments.get(getVehicleAttachmentCount() - 1).getModel().getSourceNo()
                    , "");
            if ("success".equals((String) poJSON.get("result"))) {
                
                poJSON = (JSONObject) poJSON.get("payload");
                if(WebFile.Base64ToFile((String) poJSON.get("data")
                        , (String) poJSON.get("hash")
                        , System.getProperty("sys.default.path.temp.attachments") + "/"
                        , (String) poJSON.get("filename"))){
                    System.out.println("poJSON success: " +  poJSON.toJSONString());
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
        return poJSON;
    }
    
    public JSONObject printTransaction()
        throws CloneNotSupportedException, SQLException, GuanzonException {

    poJSON = new JSONObject();
    pbIsPrinted = false;
    
    if(!poGRider.getDepartment().equals(System.getProperty("sys.dept.sales"))
            && pbWithUI){ //BR: Authorized users from the Sales
        poJSON.put("result", "error" );
        poJSON.put("message", "User is not authorized to print the transaction." );
        ShowMessageFX.Warning(
                   null,
                   "Computerized Accounting System",
                   "User is not authorized to print the transaction."
           );
        return poJSON;
    }
    
    try {

        String jrxmlPath =
                System.getProperty("sys.default.path.config")
                + "/Reports/VehicleSalesQuotation.jrxml";

        JasperReport jasperReport =
                JasperCompileManager.compileReport(jrxmlPath);
        String lsWatermarkPath = System.getProperty("sys.default.path.config") + "/Reports/images/";
        String lsBrand = BranchBrand();
        
        if(lsBrand == null || "".equals(lsBrand)){
            lsWatermarkPath = lsWatermarkPath + "anyauto.png" ;
        } else {
            if(lsBrand.toLowerCase().contains("honda")){
                lsWatermarkPath = lsWatermarkPath + "honda.png" ;
            } else if(lsBrand.toLowerCase().contains("nissan")){
                lsWatermarkPath = lsWatermarkPath + "nissan.png" ;
            } else if(lsBrand.toLowerCase().contains("geely")){
                lsWatermarkPath = lsWatermarkPath + "geely.png" ;
            }  else if(lsBrand.toLowerCase().contains("gac")){
                lsWatermarkPath = lsWatermarkPath + "gacmotor.png" ;
            } else {
                lsWatermarkPath = lsWatermarkPath + "anyauto.png" ;
            }
        }
        File file = new File(lsWatermarkPath);
        if(!file.exists() ){
            poJSON.put("result", "error");
            poJSON.put("message", "Cannot locate file in " + lsWatermarkPath
                                    + ".\nContact system administrator for assistance.");
            ShowMessageFX.Warning(
                    null,
                    "Computerized Accounting System",
                    "Cannot locate file in " + lsWatermarkPath
                                    + ".\nContact system administrator for assistance."
            );

            return poJSON;
        }
        
        
        //get CAR Image
        String lsCarImage = "";
        poJSON = loadVehicleAttachments();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        for(int lnCtr = 0;lnCtr < getVehicleAttachmentCount(); lnCtr++){
            if(RecordStatus.ACTIVE.equals(VehicleAttachmentList(lnCtr).getModel().getRecordStatus())){
                lsCarImage = VehicleAttachmentList(lnCtr).getModel().getImagePath() + "/" + VehicleAttachmentList(lnCtr).getModel().getFileName();
            }
        }
        
        file = new File(lsCarImage);
        if(!file.exists() || checkEmpty(lsCarImage)){
            lsCarImage = System.getProperty("sys.default.path.config") + "/Reports/images/none.png";
        }
        
        /*
         * Create the final JasperPrint.
         * The first rate will initialize it.
         */
        JasperPrint finalPrint = null;

        // ---------------------------------------------
        // Parameters
        // ---------------------------------------------
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("sCarImage", lsCarImage);
        parameters.put("watermarkImagePath", lsWatermarkPath);
        parameters.put("sValidity",Master().getTransactionNo()+ "-"+ poGRider.getServerDate());
        parameters.put("sCompany",Company().getCompanyName().toUpperCase());
        String lsAddress = Company().getCompanyAddress();
        if (Company().TownCity().getDescription() != null && !"".equals(Company().TownCity().getDescription())) {
            lsAddress = lsAddress + " " + Company().TownCity().getDescription();
        }

        if (Company().TownCity().Province().getDescription() != null
                && !"".equals(Company().TownCity().Province().getDescription())) {
            lsAddress = lsAddress+ ", "+ Company().TownCity().Province().getDescription();
        }

        parameters.put("sAddress",lsAddress.toUpperCase());

        Model_Branch loModel = Branch();
        String lsBranchName = loModel.getBranchName();
        String lsBranchDesc = loModel.getDescription();
        String lsBranchAddress = loModel.getAddress();
        String lsEmail = BranchEmail();
        if (loModel.TownCity().getDescription() != null && !"".equals(loModel.TownCity().getDescription())) {
            lsBranchAddress = lsBranchAddress + " " + loModel.TownCity().getDescription();
        }

        if (loModel.TownCity().Province().getDescription() != null && !"".equals(loModel.TownCity().Province().getDescription())) {
            lsBranchAddress = lsBranchAddress+ ", "+ loModel.TownCity().Province().getDescription();
        }
        String lsContact = loModel.getMobile();
        String lsLandLine = loModel.getLandLine();
        if(lsBranchName == null) { lsBranchName = "";}
        if(lsBranchDesc == null) { lsBranchDesc = "";}
        if(lsBranchAddress == null) { lsBranchAddress = "";}
        if(lsContact == null) { 
            lsContact = "";
        } else {
            if(!lsContact.isEmpty()){
                lsContact = "Mobile : " + lsContact;
            }
        }
        if(lsLandLine == null) {
            lsLandLine = "";
        } else {
            if(!lsLandLine.isEmpty()){
                lsContact = lsContact 
                            + "\nTel No : " + lsLandLine;
            }
        }
        if(lsEmail == null) {
            lsEmail = "";
        } else {
            if(!lsEmail.isEmpty()){
                lsContact = lsContact + "\nEmail Address : " + lsEmail;
            } 
        }

        parameters.put("sBranch",lsBranchName.toUpperCase());
        parameters.put("sBrandBranch",lsBranchName.toUpperCase());
        parameters.put("sBranchAddress",lsBranchAddress.toUpperCase());
        parameters.put("sBranchDesc",lsBranchDesc.toUpperCase());
        parameters.put("sContact",lsContact);
        parameters.put("sCarModel",Detail(0).ModelVariant().Model().Brand().getDescription().toUpperCase() + " " + Detail(0).Model().getDescription().toUpperCase());
        parameters.put("sColorPreference", Detail(0).ModelVariant().Color().getDescription().toUpperCase());
        parameters.put("validFrom",SQLUtil.dateFormat(Master().getTransactionDate(),SQLUtil.FORMAT_LONG_DATE));
        String lsToDate = "";
        if(Detail(0).getValidThruDate() == null){
            lsToDate = "Present";
        } else {
            lsToDate = SQLUtil.dateFormat(Detail(0).getValidThruDate(),SQLUtil.FORMAT_LONG_DATE);
        }
        parameters.put("validTo",lsToDate);
        parameters.put("nCoverageYear", Detail(0).getInsuranceYears());
        parameters.put("sTransactionNo",Master().getTransactionNo());
        parameters.put("sCustomerName",Master().Client().getCompanyName());
        parameters.put("sAddress", getFullAddress());
        parameters.put("sCustomerContact", Master().ClientMobile().getMobileNo());
        parameters.put("sEmail", Master().ClientEmail().getMailAddress());
        parameters.put("sReceivingMethod", Detail(0).getReceivingMethod());
        parameters.put("sLHBankRate", Detail(0).getRateValue());
        parameters.put("sRemarks", Detail(0).getRemarks());
        parameters.put("dDeliverySchedule", Detail(0).getExpectedDate());
        parameters.put("sConsultantPhone", getContact(Detail(0).SalesInquiry().getSalesMan(), false, false));
        parameters.put("sConsultantEmail", getEmail(Detail(0).SalesInquiry().getSalesMan(), false, false));
        parameters.put("sPreparedBySignature", "");
        parameters.put("sPreparedBy", Detail(0).SalesInquiry().SalesPerson().getFullName().toUpperCase());
        parameters.put("sApprovedBySignature", "");
//        poJSON = getUpdateStatusBy(Master().getTransactionStatus());
        poJSON = getSalesSupervisor();
        if(!isJSONSuccess(poJSON)){
            ShowMessageFX.Warning(
                   null,
                   "Computerized Accounting System",
                   (String) poJSON.get("message")
           );
            return poJSON;
        }
        parameters.put("sApprovedBy", ((String) poJSON.get("sUpdateByx")).toUpperCase() );
        
        Double ldblTotalAccessories = getOtherAddOnTotal();
        Double ldblTotalAddOns = ldblTotalAccessories + Detail(0).getLTOAmount() + Detail(0).getTPLAmount() + Detail(0).getComprehensiveAmount();
        Double ldblTotalDiscount = getTotalDiscount();
        parameters.put("nCashDiscount", ldblTotalDiscount); 
        parameters.put("nPODiscount", ldblTotalDiscount); 
        parameters.put("nIHDiscount", ldblTotalDiscount);
        
        parameters.put("nTotalDiscount",     ldblTotalDiscount);       // TODO
        parameters.put("bShowTotalDiscount", ldblTotalDiscount > 0.00);   // TODO
        parameters.put("nInclusionTotal",0.00);
        parameters.put("nVehiclePrice",Detail(0).getSRPAmount());
        parameters.put("nAddOnTotal",ldblTotalAddOns);
        parameters.put("nAccessories",ldblTotalAccessories);
        parameters.put("nInsurance",Detail(0).getComprehensiveAmount());
        parameters.put("nTPL",Detail(0).getTPLAmount());
        parameters.put("nLTO",Detail(0).getLTOAmount());
        parameters.put("nTotalPackagePrice",Detail(0).getTotalAmount());
        
        //Initial value
        parameters.put("nCashUnitPrice",0.00);
        parameters.put("nCashInsurance",0.00);
        parameters.put("nCashTPL",0.00);
        parameters.put("nCashLTO",0.00);
        parameters.put("nCashTotal",0.00);
        parameters.put("nPOUnitPrice",0.00);
        parameters.put("nPOPromoDiscount",0.00);
        parameters.put("nPOGuanzonGoodwillDiscount",0.00);
        parameters.put("nPOReferralSales",0.00);
        parameters.put("nPOTPL",0.00);
        parameters.put("nPOLTO",0.00);
        parameters.put("nPOTotal",0.00);
        parameters.put("nIHUUnitPrice",0.00);
        parameters.put("nIHUDownpayment",0.00);
        parameters.put("nIHUMonthly",0.00);
        parameters.put("nIHBankRate",0.00);
        parameters.put("nIHYears",0.00);
        parameters.put("bShowInHouse",       false);
        parameters.put("bShowCash",          false);
        parameters.put("bShowPurchaseOrder", false);
        
        switch(Detail(0).getPaymentForm()){
            case VehicleQuotationStatus.PaymentForm.FINANCING: //Financing 
                parameters.put("bShowInHouse",       true);
                parameters.put("sMOP","Financing");
                parameters.put("nIHUnitPrice",Detail(0).getSRPAmount());
                parameters.put("nIHDownpayment",Detail(0).getDownPayment());
                parameters.put("nIHYears",Detail(0).getDuration() / 12);
                parameters.put("nIHMonthly",Detail(0).getGrossMonthlyInstallment());
                parameters.put("nIHBankRate",Detail(0).getRateValue());
            break;
            case VehicleQuotationStatus.PaymentForm.PO: //PO
                parameters.put("bShowPurchaseOrder", true);
                parameters.put("sMOP","PO");
                parameters.put("nPOUnitPrice",Detail(0).getSRPAmount());
//                parameters.put("nPOPromoDiscount",0.00);
//                parameters.put("nPOGuanzonGoodwillDiscount",0.00);
//                parameters.put("nPOReferralSales",Detail(0).getComprehensiveAmount());
                parameters.put("nPOTPL",Detail(0).getTPLAmount());
                parameters.put("nPOLTO",Detail(0).getLTOAmount());
                parameters.put("nPOTotal",Detail(0).getTotalAmount());
            break;
            default: //Cash
                parameters.put("bShowCash",          true);
                parameters.put("sMOP","Cash");
                parameters.put("nCashUnitPrice",Detail(0).getSRPAmount());
                parameters.put("nCashInsurance",Detail(0).getComprehensiveAmount()); //TODO
                parameters.put("nCashTPL",Detail(0).getTPLAmount());
                parameters.put("nCashLTO",Detail(0).getLTOAmount());
                parameters.put("nCashTotal",Detail(0).getTotalAmount());
            break;
        
        }
       
        // ---------------------------------------------
        // Build data
        // ---------------------------------------------
        List<Map<String, Object>> laVehicleInfo = buildQuotationVehicleInfo();
        List<Map<String, ?>> vehicleInfo = new ArrayList<>(laVehicleInfo);
        List<Map<String, Object>> lAddOnList = buildQuotationAddOns(false);
        List<Map<String, ?>> addons = new ArrayList<>(lAddOnList);
        List<Map<String, Object>> lFreebiesList = buildQuotationFreebies();
        List<Map<String, ?>> freebies = new ArrayList<>(lFreebiesList);
        List<Map<String, Object>> lLockInInsuranceList = buildQuotationLockInInsurance();
        List<Map<String, ?>> lockIn = new ArrayList<>(lLockInInsuranceList);
        
        // --- hand the rows to the report ---
        parameters.put("VehicleDataSource",    new JRMapCollectionDataSource((Collection) vehicleInfo));
        parameters.put("AddOnDataSource",    new JRMapCollectionDataSource((Collection) addons));
        parameters.put("InclusionDataSource", new JRMapCollectionDataSource((Collection) freebies));
        parameters.put("LockInDataSource", new JRMapCollectionDataSource((Collection) lockIn));
        
        // ---------------------------------------------
        // Generate JasperPrint
        // ---------------------------------------------

        JasperPrint currentPrint =
                JasperFillManager.fillReport(
                        jasperReport,
                        parameters,
                        new JREmptyDataSource(1)
                );
        
        // ---------------------------------------------
        // Combine reports
        // ---------------------------------------------

        if (finalPrint == null) {
            finalPrint = currentPrint;
        } else {

            finalPrint.getPages().addAll(
                    currentPrint.getPages()
            );
        }

        // ---------------------------------------------
        // Display final report
        // ---------------------------------------------

        if (finalPrint != null) {
            CustomJasperViewer viewer = new CustomJasperViewer(finalPrint);
            viewer.setVisible(true);
            viewer.addWindowListener(
                    new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            proceedAfterViewerClosed();
                        }
                    }
            );
        }

    } catch (JRException | SQLException | GuanzonException ex) {
        poJSON.put("result", "error");
        poJSON.put("message","Transaction print aborted!");
        Logger.getLogger(getClass().getName())
                .log(Level.SEVERE, MiscUtil.getException(ex), ex);
    }

    return poJSON;
}
    
    private void proceedAfterViewerClosed() {
        Platform.runLater(() -> {
            System.out.println("SHOWED!!!!!!!!!!!");
            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, "Computerized Accounting System",
                        (String) poJSON.get("message"));
            } else {
                if (pbIsPrinted) {
                    ShowMessageFX.Information(null, "Computerized Accounting System",
                            "Vehicle Price List Printed Successfully");
                } else {
                    ShowMessageFX.Warning(null, "Computerized Accounting System",
                            "Printing was canceled by the user.");
                }
            }
        });
    }

    private boolean pbIsPrinted = false;
    public class CustomJasperViewer extends JasperViewer {

        public CustomJasperViewer(final JasperPrint jasperPrint) {
            super(jasperPrint, false);
            customizePrintButton(jasperPrint);
        }

        /* ---- toolbar patch ------------------------------------------ */
        private JSONObject customizePrintButton(final JasperPrint jasperPrint) {

            try {
                JRViewer viewer = findJRViewer(this);
                if (viewer == null) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "JRViewer not found!");
                    return poJSON;
                }
                for (int i = 0; i < viewer.getComponentCount(); i++) {
                    if (viewer.getComponent(i) instanceof JRViewerToolbar) {

                        JRViewerToolbar toolbar = (JRViewerToolbar) viewer.getComponent(i);

                        for (int j = 0; j < toolbar.getComponentCount(); j++) {
                            if (toolbar.getComponent(j) instanceof JButton) {

                                final JButton button = (JButton) toolbar.getComponent(j);

                                if ("Print".equals(button.getToolTipText())) {
                                    /* remove existing handlers */
                                    ActionListener[] old = button.getActionListeners();
                                    for (int k = 0; k < old.length; k++) {
                                        button.removeActionListener(old[k]);
                                    }

                                    /* add our own (anonymous inner‑class, not lambda) */
                                    button.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            try {
                                                pbIsPrinted = JasperPrintManager.printReport(jasperPrint, true);
                                                if (pbIsPrinted) {
                                                    CustomJasperViewer.this.dispose();
                                                } else {
                                                    poJSON.put("result", "error");
                                                    poJSON.put("message",  "Printing was canceled by the user.");
                                                }
                                            } catch (JRException ex) {
                                                poJSON.put("result", "error");
                                                poJSON.put("message",  "Print Failed: " + ex.getMessage());
                                                Logger.getLogger(getClass().getName()).log(Level.SEVERE,  ex.getMessage(), ex);
                                            }
                                        }
                                    });
                                } else {
                                    // Disable all other buttons
//                                    button.setEnabled(false);
//                                    poJSON.put("result", "error");
//                                    poJSON.put("message",  "Transaction print aborted!");
                                }
                            }
                        }
                        toolbar.revalidate();
                        toolbar.repaint();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error customizing print button: " + e.getMessage());
                poJSON.put("result", "error");
                poJSON.put("message", "Error customizing print button: " + e.getMessage());
            }

            return poJSON;
        }

        private JRViewer findJRViewer(Component parent) {
            if (parent instanceof JRViewer) {
                return (JRViewer) parent;
            }
            if (parent instanceof Container) {
                Component[] comps = ((Container) parent).getComponents();
                for (int i = 0; i < comps.length; i++) {
                    JRViewer v = findJRViewer(comps[i]);
                    if (v != null) {
                        return v;
                    }
                }
            }
            return null;
        }
    }
    
    private List<Map<String, Object>> buildQuotationAddOns(boolean isInclusions) {
        List<Map<String, Object>> rows = new ArrayList<>();

        try{
            for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
                if(checkEmpty(AddOnList(lnCtr).getAddOnId())
                    || AddOnList(lnCtr).getAmount() <= 0.00
                    || !AddOnList(lnCtr).getRecordStatus()
                    ){
                    continue;
                }
                String lsType = "Add On";
                String lsDescription = AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName();
                addAddOnList(rows
                        , lsType
                        , lsDescription
                        , AddOnList(lnCtr).getAmount()
                        , AddOnList(lnCtr).getQuantity()
                        , AddOnList(lnCtr).getTotalAmount()
                );
            }
            
            for(int lnCtr = 0;lnCtr < getFreebiesListCount();lnCtr++){
                if(checkEmpty(FreebiesList(lnCtr).getFreebieId())
                    || FreebiesList(lnCtr).getAmount() <= 0.00
                    || !FreebiesList(lnCtr).getRecordStatus()    
                    ){
                    continue;
                }
                String lsType = "Accessory";
                String lsDescription = FreebiesList(lnCtr).Freebie().getDescription();
                addAddOnList(rows
                        , lsType
                        , lsDescription
                        , FreebiesList(lnCtr).getAmount()
                        , FreebiesList(lnCtr).getQuantity()
                        , FreebiesList(lnCtr).getTotalAmount()
                );
            }
            
            for(int lnCtr = 0;lnCtr < getInclusionListCount();lnCtr++){
                if(checkEmpty(InclusionList(lnCtr).getInclusionId())
                    || InclusionList(lnCtr).getAmount() <= 0.00
                    || !InclusionList(lnCtr).getRecordStatus()
                    ){
                    continue;
                }
                String lsType = "Accessory";
                String lsDescription = InclusionList(lnCtr).Inclusion().getDescription();
                addAddOnList(rows
                        , lsType
                        , lsDescription
                        , InclusionList(lnCtr).getAmount()
                        , InclusionList(lnCtr).getQuantity()
                        , InclusionList(lnCtr).getTotalAmount()
                );
            }
            
            for(int lnCtr = 0;lnCtr < getSpecialRequestListCount();lnCtr++){
                if(checkEmpty(SpecialRequestList(lnCtr).getRequestDescription())
                    || SpecialRequestList(lnCtr).getAmount()<= 0.00
                    || !SpecialRequestList(lnCtr).getRecordStatus()
                    ){
                    continue;
                }
                String lsType = "Accessory";
                String lsDescription = SpecialRequestList(lnCtr).getRequestDescription();
                addAddOnList(rows
                        , lsType
                        , lsDescription
                        , SpecialRequestList(lnCtr).getAmount()
                        , SpecialRequestList(lnCtr).getQuantity()
                        , SpecialRequestList(lnCtr).getTotalAmount()
                );
            }
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehicleQuotation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
 
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addAddOnList(List<Map<String, Object>> rows
                                , String type
                                , String description
                                , Double amount
                                , int quantity
                                , Double total
                        ) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(type == null){ type = "";}
        if(description == null){ description = "";}
        row.put("sType", type);
        row.put("sDescription", description.toUpperCase());
        row.put("nAmount", amount);
        row.put("nQuantity", quantity);
        row.put("nTotal", total);
        rows.add(row);
    }
    
    private List<Map<String, Object>> buildQuotationVehicleInfo() {
        List<Map<String, Object>> rows = new ArrayList<>();

        try{
             addVehicleInfo(rows
                        , Detail(0).ModelVariant().Model().Brand().getDescription() + " " + Detail(0).ModelVariant().Model().getDescription()
                        , Detail(0).ModelVariant().getDescription()
                        , Detail(0).ModelVariant().Color().getDescription()
                        , Detail(0).ModelVariant().VariantInsurance().getTransmission()
                        , ""
                        , Detail(0).ModelVariant().getYearModel()
                        , Detail(0).getSRPAmount()
                );
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehicleQuotation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
 
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addVehicleInfo(List<Map<String, Object>> rows
                                , String sModel
                                , String sVariant
                                , String sColor
                                , String sTransmission
                                , String sFuelType
                                , int nYear
                                , Double nPrice
                        ) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(sModel == null){ sModel = "";}
        if(sVariant == null){ sVariant = "";}
        if(sTransmission == null){ sTransmission = "";}
        if(sFuelType == null){ sFuelType = "";}
        row.put("sModel", sModel.toUpperCase());
        row.put("sVariant", sVariant.toUpperCase());
        row.put("sColor", sColor.toUpperCase());
        row.put("sTransmission", sTransmission.toUpperCase());
        row.put("sFuelType", sFuelType.toUpperCase());
        row.put("nYear", nYear);
        row.put("nPrice", nPrice);
        rows.add(row);
    }
    
    private List<Map<String, Object>> buildQuotationFreebies() {
        List<Map<String, Object>> rows = new ArrayList<>();

        try{
            for(int lnCtr = 0;lnCtr < getAddOnListCount();lnCtr++){
                if(checkEmpty(AddOnList(lnCtr).getAddOnId()) 
                    || AddOnList(lnCtr).getAmount() > 0.00
                    || !AddOnList(lnCtr).getRecordStatus()){
                    continue;
                }
                
                addQuotationFreebies(rows
                        , AddOnList(lnCtr).AddOnMaster().AddOnType().getAddTypeName()
                        , AddOnList(lnCtr).getQuantity()
                );
            }
            for(int lnCtr = 0;lnCtr < getFreebiesListCount();lnCtr++){
                if(checkEmpty(FreebiesList(lnCtr).getFreebieId()) 
                    || FreebiesList(lnCtr).getAmount() > 0.00
                    || !FreebiesList(lnCtr).getRecordStatus()){
                    continue;
                }
                
                addQuotationFreebies(rows
                        , FreebiesList(lnCtr).Freebie().getDescription()
                        , FreebiesList(lnCtr).getQuantity()
                );
            }
            for(int lnCtr = 0;lnCtr < getInclusionListCount();lnCtr++){
                if(checkEmpty(InclusionList(lnCtr).getInclusionId()) 
                    || InclusionList(lnCtr).getAmount() > 0.00
                    || !InclusionList(lnCtr).getRecordStatus()    
                    ){
                    continue;
                }
                
                addQuotationFreebies(rows
                        , InclusionList(lnCtr).Inclusion().getDescription()
                        , InclusionList(lnCtr).getQuantity()
                );
            }
            
            for(int lnCtr = 0;lnCtr < getSpecialRequestListCount();lnCtr++){
                if(checkEmpty(SpecialRequestList(lnCtr).getRequestDescription())
                    || SpecialRequestList(lnCtr).getAmount() > 0.00
                    || !SpecialRequestList(lnCtr).getRecordStatus()
                    ){
                    continue;
                }
                
                addQuotationFreebies(rows
                        , SpecialRequestList(lnCtr).getRequestDescription()
                        , SpecialRequestList(lnCtr).getQuantity()
                );
            }
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehicleQuotation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
 
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addQuotationFreebies(List<Map<String, Object>> rows
                                , String description
                                , int quantity
                        ) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(description == null){ description = "";}
        row.put("sDescription", description.toUpperCase());
        row.put("nQuantity", quantity);
        rows.add(row);
    }
    
    private List<Map<String, Object>> buildQuotationLockInInsurance() {
        List<Map<String, Object>> rows = new ArrayList<>();
        if(!Detail(0).isInsuranceLocked()){
            return rows;
        }
        int lnLockInYear = Detail(0).getInsuranceYears();
        Double ldblFirstYear = 0.00;
        Double ldblSecondYear = 0.00;
        Double ldblThirdYear = 0.00;
        Double ldblFourthYear = 0.00;
        Double ldblFifthYear = 0.00;
        try{
            String lsSQL = "SELECT " +
                    "  a.sValidIDx, " +
                    "  a.sCompnyID, " +
                    "  a.dFromDate, " +
                    "  a.dThruDate, " +
                    "  b.sVrntIDxx, " +
                    "  b.sInsureID, " +
                    "  b.nYearNoxx, " +
                    "  b.nAmountxx " +
                    "FROM Validity_Period_Master a " +
                    "INNER JOIN Vehicle_Insurance_Master b ON b.sValidIDx = a.sValidIDx";
            
            lsSQL = MiscUtil.addCondition(lsSQL, 
                                            " a.cRecdStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                                            + " AND a.sCompnyID = " + SQLUtil.toSQL(psCompanyId)
                                            + " AND b.sVrntIDxx = " + SQLUtil.toSQL(Detail(0).getVariantId())
                                            );
            lsSQL = lsSQL + " ORDER BY b.nYearNoxx ASC";
            System.out.println("buildQuotationLockInInsurance : " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0L) {
                while (loRS.next()) {
                    switch(loRS.getInt("nYearNoxx")){
                        case 1:
                            if(lnLockInYear >= 1){
                                ldblFirstYear = loRS.getDouble("nAmountxx");
                            }
                        break;
                        case 2:
                            if(lnLockInYear >= 2){
                                ldblSecondYear = loRS.getDouble("nAmountxx");
                            }
                        break;
                        case 3:
                            if(lnLockInYear >= 3){
                                ldblThirdYear = loRS.getDouble("nAmountxx");
                            }
                        break;
                        case 4:
                            if(lnLockInYear >= 4){
                                ldblFourthYear = loRS.getDouble("nAmountxx");
                            }
                        break;
                        case 5:
                            if(lnLockInYear >= 5){
                                ldblFifthYear = loRS.getDouble("nAmountxx");
                            }
                        break;
                    }
                } 
            }
            
            MiscUtil.close(loRS);
             
            addLockInInsurance(rows
                       , Detail(0).ModelVariant().Model().Brand().getDescription() + " " + Detail(0).ModelVariant().Model().getDescription()
                       , Detail(0).ModelVariant().getDescription()
                       , Detail(0).ModelVariant().Color().getDescription()
                       , Detail(0).ModelVariant().VariantInsurance().getTransmission()
                       , Detail(0).ModelVariant().getYearModel()
                       ,ldblFirstYear
                       ,ldblSecondYear
                       ,ldblThirdYear
                       ,ldblFourthYear
                       ,ldblFifthYear
               );
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehicleQuotation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }  
  
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addLockInInsurance(List<Map<String, Object>> rows
                                , String sModel
                                , String sVariant
                                , String sColor
                                , String sTransmission
                                , int nYearModel
                                , Double nFirstYear
                                , Double nSecondYear
                                , Double nThirdYear
                                , Double nFourthYear
                                , Double nFifthYear
                        ) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(sModel == null){ sModel = "";}
        if(sVariant == null){ sVariant = "";}
        if(sColor == null){ sColor = "";}
        if(sTransmission == null){ sTransmission = "";}
        String sDescription = sModel + " " + sVariant + " " + sTransmission + " " + sColor + " " + String.valueOf(nYearModel)  ;
        
        row.put("sDescription", sDescription.toUpperCase());
        row.put("nFirstYear", nFirstYear);
        row.put("nSecondYear", nSecondYear);
        row.put("nThirdYear", nThirdYear);
        row.put("nFourthYear", nFourthYear);
        row.put("nFifthYear", nFifthYear);
        rows.add(row);
    }
    
    public JSONObject getSalesSupervisor() throws SQLException, GuanzonException {
        
        String lsUpdateBy = "";
        String lsSQL =  " SELECT " 
                        + " a.sEmployID "
                        + " , b.sCompnyNm"
                        + " , c.sDeptName"
                        + " , d.sPositnNm"
                        + " , e.sBranchNm"
                        + " FROM Employee_Master001 a"
                        + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployID"
                        + " LEFT JOIN Department c ON c.sDeptIDxx = a.sDeptIDxx"
                        + " LEFT JOIN POSITION d ON d.sPositnID = a.sPositnID"
                        + " LEFT JOIN Branch e ON e.sBranchCd = a.sBranchCd"
                        + " WHERE "
                        + " a.sBranchCd = " + SQLUtil.toSQL(Detail(0).getBranchCode())
                        + " AND a.sDeptIDxx = "+ SQLUtil.toSQL(System.getProperty("sys.dept.sales"))
                        + " AND d.sPositnNm LIKE " + SQLUtil.toSQL("%Sales%Supervisor%");
        System.out.println("Execute SQL getSalesSupervisor : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                if(loRS.getString("sEmployID") != null && !"".equals(loRS.getString("sEmployID"))){
                    lsUpdateBy = loRS.getString("sCompnyNm");
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
        return poJSON;
    }
    
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
                case VehicleQuotationStatus.CONFIRMED:
                    crs.updateString("cRefrStat", "CLIENT CONFIRMED");
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
                        case VehicleQuotationStatus.CONFIRMED:
                            crs.updateString("cRefrStat", "CLIENT CONFIRMED");
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
