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
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionStatusHistory;
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
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;
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
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;

/**
 * Manages vehicle financing price transactions including approval, voiding, and cancellation.
 * Handles master and detail records for vehicle financing pricing with validity period management.
 *
 * @author Arsiela 11032026
 */
public class VehiclePromotion extends Transaction {
    public String psCompanyId = "";
    public String psIndustryId = "";
    public String psCategoryId = "";
    public String psApprover = "";

    public List<Model_Promotion_Applicability> paApplicability;
    public List<Model_Promotion_AddOn> paAddOn;
    public List<Model_Promotion_Freebie> paFreebie;
    public List<Model_Promotion_Inclusion> paInclusion;
    public List<Model_Validity_Period_Master> paTransactionList;

    /**
     * Initializes and exposes the promotion model family used by the promotion workflow.
     *
     * @return JSONObject with success/error result
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "VPrm";
        if (poGRider == null) {
            return setJSON("error", "Application driver is not initialized.");
        }

        poMaster = new SalesModels(poGRider).ValidityPeriodMaster();
        poDetail = new SalesModels(poGRider).PromotionMaster();

        paApplicability = new ArrayList<Model_Promotion_Applicability>();
        paAddOn = new ArrayList<Model_Promotion_AddOn>();
        paFreebie = new ArrayList<Model_Promotion_Freebie>();
        paInclusion = new ArrayList<Model_Promotion_Inclusion>();
        paTransactionList = new ArrayList<Model_Validity_Period_Master>();
        psApprover = "";
        setApproving("");
        return initialize();
    }

    public void resetTransaction(){
        paDetail = new ArrayList<>();
        paApplicability = new ArrayList<Model_Promotion_Applicability>();
        paAddOn = new ArrayList<Model_Promotion_AddOn>();
        paFreebie = new ArrayList<Model_Promotion_Freebie>();
        paInclusion = new ArrayList<Model_Promotion_Inclusion>();
        psApprover = "";
        setApproving("");
    }

    /**
     * Returns the transaction source code.
     *
     * @return the source code for this transaction ("Vhpl" for Vehicle Financing Price)
     */
    @Override
    public String getSourceCode() { return SOURCE_CODE; }
    
    /**
     * Sets the company ID for filtering and transaction context.
     *
     * @param companyId the company ID to set
     */
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCategoryId(String categoryId) { psCategoryId = categoryId; }
    
    /**
    * Creates a JSONObject with "result" and "message" fields.
    *
    * @param fsResult  The result value (e.g., "success", "error")
    * @param fsMessage The message describing the result
    * @return JSONObject containing the result and message
    */
    private JSONObject setJSON(String fsResult, String fsMessage) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", fsResult);
        loJSON.put("message", fsMessage);
        return loJSON;
    }

    /**
     * Checks whether a JSONObject indicates a successful result.
     *
     * Returns true if the "result" field equals "success" or is not "error".
     *
     * @param foJSON The JSONObject to check
     * @return true if successful, false otherwise
     */
    public boolean isJSONSuccess(JSONObject foJSON) {
        return ("success".equals((String) foJSON.get("result")) || !"error".equals((String) foJSON.get("result")));
    }

    private boolean checkEmpty(String fsValue){
        return fsValue == null || "".equals(fsValue);
    }
    private boolean checkNotEmpty(String fsValue){
        return fsValue != null && !"".equals(fsValue);
    }
    
    /**
     * Creates a new transaction in memory.
     * Delegates to the parent class newTransaction() method.
     *
     * @return JSONObject containing the new transaction result
     * @throws CloneNotSupportedException if detail cloning fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public JSONObject NewTransaction()
            throws CloneNotSupportedException, SQLException, GuanzonException {
        return newTransaction();
    }
    
    /**
     * Opens an existing transaction by its transaction number.
     * Delegates to the parent class openTransaction() method.
     *
     * @param transactionNo the validity ID/transaction number to open
     * @return JSONObject containing the operation result
     * @throws CloneNotSupportedException if detail cloning fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws ScriptException if script processing fails
     */
    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException {
        poJSON = new JSONObject();
        
        poJSON = openTransaction(transactionNo);
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        sortDetail();
        
        return poJSON;
    }
    /**
     * Opens an existing transaction and loads all associated detail records.
     * Sets edit mode to EDIT (mode 1) when successfully opened.
     *
     * @param transactionNo the validity ID to load
     * @return JSONObject with "result" and optional "message" fields
     * @throws CloneNotSupportedException if detail cloning fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
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
            
            String sql = "SELECT sPromCode FROM " + this.poDetail.getTable() + " WHERE sValidIDx = " + SQLUtil.toSQL(transactionNo) + " ORDER BY sPromCode";
            ResultSet rs = this.poGRider.executeQuery(sql);

            while(rs.next()) {
                Model loDetail = (Model)this.poDetail.clone();
                loDetail.newRecord();
//                this.poJSON = loDetail.openRecord(transactionNo, rs.getString("sPromCode"));
                this.poJSON = loDetail.openRecord( rs.getString("sPromCode"),transactionNo);
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
    
    /**
     * Updates the current transaction.
     * Delegates to the parent class updateTransaction() method.
     *
     * @return JSONObject containing the update result
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if detail cloning fails
     * @throws ScriptException if script processing fails
     */
    public JSONObject UpdateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        
        poJSON = updateTransaction();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        return poJSON;
    }
    
    /**
     * Saves the current transaction to the database.
     * Delegates to the parent class saveTransaction() method.
     *
     * @return JSONObject containing save result with success or error message
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if detail cloning fails
     */
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
                                Detail(lnCtr).setValidityId(Master().getValidityId());
                                if(((Model)this.paDetail.get(lnCtr)).getEditMode() == EditMode.ADDNEW){
                                    ((Model)this.paDetail.get(lnCtr)).setValue("sPromCode", Detail(lnCtr).getNextCode());
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
    
    /**
    * Requests user approval for the current transaction.
    *
    * @return JSONObject containing approval result and message
    */
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
    
    /**
     * Converts status code to human-readable status string.
     *
     * @param lsStatus the status code from ValidityPeriodStatus constants
     * @return readable status string ("Approved", "Voided", "Cancelled", "Open", or "Unknown")
     */
    public String getStatus(String lsStatus) {
        switch (lsStatus) {
            case ValidityPeriodStatus.VOID:
                return "Voided";
            case ValidityPeriodStatus.CANCELLED:
                return "Cancelled";
            case ValidityPeriodStatus.APPROVED:
                return "Approved";
            case ValidityPeriodStatus.OPEN:
                return "Open";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Approves the current transaction after validation.
     * Validates entry, requests approval if necessary, and updates transaction status to APPROVED.
     *
     * @return JSONObject with "result" and "message" indicating success or failure
     * @throws ParseException if JSON parsing fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if detail cloning fails
     * @throws ScriptException if script processing fails
     */
    public JSONObject ApproveTransaction() throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();
        String lsStatus = ValidityPeriodStatus.APPROVED;
        
        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No transacton was loaded.");
            return poJSON;
        }
        
        Model_Validity_Period_Master loObject = new SalesModels(poGRider).ValidityPeriodMaster();
        poJSON = loObject.openRecord(Master().getValidityId());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load transaction. " + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (lsStatus.equals(loObject.getRecordStatus())) {
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
            poJSON = callApproval();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }
        
        //change status
        poGRider.beginTrans("UPDATE STATUS", "ApproveTransaction", SOURCE_CODE, Master().getValidityId());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sValidIDx"), "", lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();
        
        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction approved successfully.");
        return poJSON;
    }

    /**
     * Voids the current transaction.
     * Validates that transaction is in appropriate state and updates status to VOID.
     *
     * @return JSONObject with "result" and "message" indicating success or failure
     * @throws ParseException if JSON parsing fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if detail cloning fails
     * @throws ScriptException if script processing fails
     */
    public JSONObject VoidTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException,
            ScriptException {
        poJSON = new JSONObject();

        String lsStatus = ValidityPeriodStatus.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }
        
        Model_Validity_Period_Master loObject = new SalesModels(poGRider).ValidityPeriodMaster();
        poJSON = loObject.openRecord(Master().getValidityId());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load transaction. " + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (lsStatus.equals(loObject.getRecordStatus())) {
            poJSON = setJSON("error", "Transaction was already voided.");
            return poJSON;
        }
        
        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sValidIDx"),"", lsStatus, false, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction voided successfully.");
        return poJSON;
    }
    
    /**
     * Cancels the current transaction.
     * Validates transaction state, requests approval, and updates status to CANCELLED.
     *
     * @return JSONObject with "result" and "message" indicating success or failure
     * @throws ParseException if JSON parsing fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if detail cloning fails
     * @throws ScriptException if script processing fails
     */
    public JSONObject CancelTransaction()
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException,
            ScriptException {
        poJSON = new JSONObject();

        String lsStatus = ValidityPeriodStatus.CANCELLED;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error", "No record was loaded.");
            return poJSON;
        }
        
        Model_Validity_Period_Master loObject = new SalesModels(poGRider).ValidityPeriodMaster();
        poJSON = loObject.openRecord(Master().getValidityId());
        if (!isJSONSuccess(poJSON)) {
            poJSON = setJSON((String) poJSON.get("result"), "Unable to load transaction. " + (String) poJSON.get("message"));
            return poJSON;
        }
        
        if (lsStatus.equals(loObject.getRecordStatus())) {
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
        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, Master().getValidityId());

        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sValidIDx"), "", lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();
        
        poJSON = new JSONObject();
        poJSON = setJSON("success", "Transaction cancelled successfully.");
        return poJSON;
    }
    
    /**
     * Handles status change logic with transaction history tracking.
     * Records status changes in Transaction_Status_History and optionally updates master status.
     *
     * @param tableName the name of the table being updated
     * @param sourceNo the transaction source number
     * @param remarks optional remarks for the status change
     * @param statusRequest the new status code to apply
     * @param needConfirmation whether status change requires confirmation before application
     * @param withParent whether transaction is managed by parent class
     * @return JSONObject with "result", "message", and optional "notes" fields
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if record cloning fails
     */
    @Override
     protected JSONObject statusChange(String tableName, String sourceNo, String remarks, String statusRequest, boolean needConfirmation, boolean withParent) throws SQLException, GuanzonException, CloneNotSupportedException {
        this.poGRider.ensureConnected();
        if (remarks.isEmpty() && this.pbWithUI) {
            try {
                remarks = ShowDialogFX.getStatusChangeNotes();
            } catch (Exception e) {
                this.poJSON = new JSONObject();
                this.poJSON.put("result", "error");
                this.poJSON.put("message", e.getMessage());
                return this.poJSON;
            }
        }

        if (!withParent) {
            this.poGRider.beginTrans("UPDATE STATUS", remarks, "TSHx", sourceNo);
        }

        TransactionStatusHistory loStatus = (new SysTableContollers(this.poGRider, this.logwrapr)).TransactionStatusHistory();
        loStatus.setWithParentClass(true);
        this.poJSON = loStatus.newRecord();
        if (!"success".equals((String)this.poJSON.get("result"))) {
            if (!withParent) {
                this.poGRider.rollbackTrans();
            }

            return this.poJSON;
        } else {
            String lsApproved;
            if (this.psApproved != null && !this.psApproved.isEmpty()) {
                lsApproved = this.poGRider.Encrypt(this.psApproved);
                this.psApproved = "";
            } else {
                lsApproved = this.poGRider.Encrypt(this.poGRider.getUserID());
            }

            loStatus.getModel().setTransactionTable(tableName);
            loStatus.getModel().setSourceNo(sourceNo);
            loStatus.getModel().setRemarks(remarks);
            loStatus.getModel().setStatusRequest(statusRequest);
            loStatus.getModel().setTransactionStatus(needConfirmation ? "0" : "1");
            loStatus.getModel().setModifyingId(lsApproved);
            this.poJSON = loStatus.saveRecord();
            if (!"success".equals((String)this.poJSON.get("result"))) {
                if (!withParent) {
                    this.poGRider.rollbackTrans();
                }

                return this.poJSON;
            } else {
                if (!needConfirmation) {
                    this.poJSON = this.updateMasterStatus(statusRequest);
                    if (!"success".equals((String)this.poJSON.get("result"))) {
                        if (!withParent) {
                            this.poGRider.rollbackTrans();
                        }

                        return this.poJSON;
                    }
                }

                if (!withParent) {
                    this.poGRider.commitTrans();
                }

                this.poJSON = new JSONObject();
                this.poJSON.put("result", "success");
                this.poJSON.put("notes", remarks);
                return this.poJSON;
            }
        }
    }
    
    /**
     * Updates the master transaction record status in the database.
     *
     * @param statusRequest the new status code to set
     * @return JSONObject with "result" and optional "message" fields
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    private JSONObject updateMasterStatus(String statusRequest) throws SQLException, GuanzonException {
        String lsSQL = "UPDATE " + this.poMaster.getTable() + " SET cRecdStat = " + SQLUtil.toSQL(statusRequest) + " WHERE sValidIDx = " + SQLUtil.toSQL((String)this.poMaster.getValue("sValidIDx"));
        if (this.poGRider.executeQuery(lsSQL, this.poMaster.getTable(), this.psBranchCode, this.psDestination, "") <= 0L) {
            this.poJSON = new JSONObject();
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "Error updating the transaction status.");
            return this.poJSON;
        } else {
            return this.poJSON;
        }
    }
    
    public JSONObject loadTransactionList() {
        poJSON = new JSONObject();
        paTransactionList = new ArrayList<>();
        try {
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                                    " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId));

            lsSQL = lsSQL + " ORDER BY a.dFromDate DESC ";
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (loRS == null) {
                poJSON.put("result", "error");
                poJSON.put("message", "Query execution failed.");
                return poJSON;
            }

            int lnctr = 0;
            while (loRS.next()) {
                String lsId = loRS.getString("sValidIDx");
                if(checkNotEmpty(lsId)){
                    paTransactionList.add(new SalesModels(poGRider).ValidityPeriodMaster());
                    poJSON = paTransactionList.get(paTransactionList.size() - 1).openRecord(lsId);
                    if(!isJSONSuccess(poJSON)){
                        poJSON = setJSON("errpr", "Error while loading promotion list.\n"
                                                 + (String) poJSON.get("message"));
                        return poJSON;
                    }
                }
                lnctr++;
            }
            MiscUtil.close(loRS);

            if (lnctr > 0) {
                poJSON.put("result", "success");
                poJSON.put("message", "Record(s) loaded successfully.");
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "No records found.");
            }
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(VehiclePriceList.class.getName()).log(Level.SEVERE, null, ex);
        }

        return poJSON;
    }
    
    /**
     * Searches for and opens a transaction based on criteria.
     * Displays browse dialog with filtered validity records or loads record by code directly.
     *
     * @param fsValue search value to filter results
     * @param fbByCode if true, search by code; otherwise search by description
     * @return JSONObject containing opened transaction data or error message
     * @throws CloneNotSupportedException if detail cloning fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws ScriptException if script processing fails
     */
    public JSONObject SearchTransaction(String fsValue, boolean fbByCode) throws CloneNotSupportedException, SQLException, GuanzonException, ScriptException{
        poJSON = new JSONObject();

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                " a.sCompnyID = " + SQLUtil.toSQL(psCompanyId));
        
        lsSQL = lsSQL + " GROUP BY a.sValidIDx ";
        System.out.println("Executing SQL: " + lsSQL);
        if(pbWithUI){

            poJSON = ShowDialogFX.Browse(poGRider,
                    lsSQL,
                    "",
                    "Validity ID»Description»From Date»Thru Date»Status",
                    "sValidIDx»sValidDsc»dFromDate»dThruDate»xRecdStat",
                    "a.sValidIDx»a.sValidDsc»a.dFromDate»a.dThruDate»" + 
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
                "     END AS xRecdStat",
                    fbByCode ? 0 : 1);
        } else {
            poJSON.put("sValidIDx", fsValue);
        }
        if (poJSON != null) {

            return OpenTransaction((String) poJSON.get("sValidIDx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject SearchAddOn(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);
        
        AddOnType object = new SalesControllers(poGRider, logwrapr).AddOnType();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setWithUI(pbWithUI);
        
        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            for(int lnCtr = 0;lnCtr < getPromotionAddOnCount(); lnCtr++){
                if(row != lnCtr && checkNotEmpty(PromotionAddOnList(lnCtr).getAddOnId())){
                    if(PromotionAddOnList(lnCtr).getAddOnId().equals(object.getModel().getAddTypeCode())){
                        row = lnCtr;
                        if(!PromotionAddOnList(lnCtr).getRecordStatus()){
                            PromotionAddOnList(lnCtr).setRecordStatus(true);
                            break;
                        } else {
                            poJSON = setJSON("error", "Selected Add-On already exists at row "+ (lnCtr+1));
                            poJSON.put("row", row);
                            return poJSON;
                        }
                    }
                }
            }
            
            PromotionAddOnList(row).setAddOnId(object.getModel().getAddTypeCode());
        }
        
        poJSON.put("row", row);
        return poJSON;
    }

    public JSONObject SearchFreebie(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        Freebie object = new SalesControllers(poGRider, logwrapr).Freebie();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setWithUI(pbWithUI);

        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            //Validate
            for(int lnCtr = 0;lnCtr < getPromotionFreebieCount(); lnCtr++){
                if(row != lnCtr && checkNotEmpty(PromotionFreebieList(lnCtr).getFreebieId())){
                    if(PromotionFreebieList(lnCtr).getFreebieId().equals(object.getModel().getFreebieId())){
                        row = lnCtr;
                        if(!PromotionFreebieList(lnCtr).getRecordStatus()){
                            PromotionFreebieList(lnCtr).setRecordStatus(true);
                            break;
                        } else {
                            poJSON = setJSON("error", "Selected Freebie already exists at row "+ (lnCtr+1));
                            poJSON.put("row", row);
                            return poJSON;
                        }
                    }
                }
            }
            
            PromotionFreebieList(row).setFreebieId(object.getModel().getFreebieId());
        }
        
        return poJSON;
    }

    public JSONObject SearchInclusion(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        Inclusion object = new SalesControllers(poGRider, logwrapr).Inclusion();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setWithUI(pbWithUI);

        poJSON = object.searchRecord(value, byCode);
        if (isJSONSuccess(poJSON)) {
            
            //Validate
            for(int lnCtr = 0;lnCtr < getPromotionInclusionCount(); lnCtr++){
                if(row != lnCtr && checkNotEmpty(PromotionInclusionList(lnCtr).getInclusionId())){
                    if(PromotionInclusionList(lnCtr).getInclusionId().equals(object.getModel().getInclusionId())){
                        row = lnCtr;
                        if(!PromotionInclusionList(lnCtr).getRecordStatus()){
                            PromotionInclusionList(lnCtr).setRecordStatus(true);
                            break;
                        } else {
                            poJSON = setJSON("error", "Selected Inclusion already exists at row "+ (lnCtr+1));
                            poJSON.put("row", row);
                            return poJSON;
                        }
                    }
                }
            }
            
            PromotionInclusionList(row).setInclusionId(object.getModel().getInclusionId());
        }
        
        return poJSON;
    }
    
    /**
     * Validate Applicability
     * @param value
     * @param row
     * @param type
     * @return 
     */
//    private JSONObject validateApplicability(String value, int row, String type){
//    
//    
//    }

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
            PromotionApplicabilityList(row).setBrandId(object.getModel().getBrandId());
        }
        
        sortDetail();
        return poJSON;
    }
    
    public JSONObject SearchModel(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(checkEmpty(PromotionApplicabilityList(row).getBrandId())){
            return setJSON("error", "Brand cannot be empty.");
        }
        
        org.guanzon.cas.parameter.Model object = new ParamControllers(poGRider, logwrapr).Model();
        object.setRecordStatus(RecordStatus.ACTIVE);
        if(pbWithUI){
            poJSON = object.searchRecord(value, byCode, PromotionApplicabilityList(row).getBrandId());
        } else {
            poJSON = object.openRecord(value);
        }
        if (isJSONSuccess(poJSON)) {
            PromotionApplicabilityList(row).setModelId(object.getModel().getModelId());
        }
        
        sortDetail();
        return poJSON;
    }
    
    public JSONObject SearchModelVariant(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(checkEmpty(PromotionApplicabilityList(row).getModelId())){
            poJSON =  setJSON("error", "Model cannot be empty.");
            poJSON.put("row", row);
            return poJSON;
        }
        
        String lsVariantId = "";
        VehicleDescription object = new SalesControllers(poGRider, logwrapr).VehicleDescription();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setIndustryId(psIndustryId);
        if(pbWithUI){
            poJSON = object.searchRecordByModel(value, byCode, PromotionApplicabilityList(row).getModelId());
        } else {
            poJSON = object.openRecord(value);
        }
        if (isJSONSuccess(poJSON)) {
            //Check existing variant in detail
            lsVariantId = object.getModel().getVariantId();
            poJSON = checkExistingVariant(object.getModel().getVariantId(), row);
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        
            if(poJSON.get("row") != null){
                row = (Integer) poJSON.get("row");
            }
        }
        
        sortDetail();
        
        return poJSON;
    }
    
    private JSONObject checkExistingVariant(String fsVariantId, int row){
        poJSON.put("row", row);
        for(int lnCtr = 0;lnCtr < getPromotionApplicabilityCount(); lnCtr++){
            if(fsVariantId.equals(PromotionApplicabilityList(lnCtr).getVariantId())
                && row != lnCtr){
                if(!PromotionApplicabilityList(lnCtr).getRecordStatus()){
                    PromotionApplicabilityList(lnCtr).setRecordStatus(true);
                    PromotionApplicabilityList(row).setBrandId("");
                    PromotionApplicabilityList(row).setModelId("");
                    PromotionApplicabilityList(row).setVariantId("");
                    poJSON.put("row", lnCtr);
                    return poJSON;
                } else {
                    poJSON = setJSON("error", "Selected Variant already exist.");
                    poJSON.put("row", lnCtr);
                    return poJSON;
                }
            }
        }
            
        PromotionApplicabilityList(row).setVariantId(fsVariantId);
        poJSON.put("row", row);
        return poJSON;
    }
    
    public void sortDetail(){
        //Sort paDetail by brand, model, variant;
        Detail().sort(
            Comparator.comparing(
                o -> getBrandDescription((Model_Vehicle_Price_Master) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ).thenComparing(
                o -> getModelDescription((Model_Vehicle_Price_Master) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ).thenComparing(
                o -> getModelVariantDescription((Model_Vehicle_Price_Master) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            )
        );
    }
    
    private String getBrandDescription(Model_Vehicle_Price_Master poDetail) {
        try {
            return poDetail.ModelVariant()
                    .Model()
                    .Brand()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }

    private String getModelDescription(Model_Vehicle_Price_Master poDetail) {
        try {
            return poDetail.ModelVariant()
                    .Model()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }

    private String getModelVariantDescription(Model_Vehicle_Price_Master poDetail) {
        try {
            return poDetail.ModelVariant()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }
    /**
     * Formats a {@link Date} value into a yyyy-MM-dd string.
     *
     * @param fdValue date to format
     * @return formatted date string
     */
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
    
    /**
     * Gets the master record as Model_Promotion_Master.
     *
     * @return the current master record cast to Model_Promotion_Master
     */
    @Override
    public Model_Validity_Period_Master Master() {
        return (Model_Validity_Period_Master) poMaster;
    }
    
    /**
     * Gets a detail record by row index as Model_Vehicle_Price_Master.
     *
     * @param row the index of the detail record to retrieve
     * @return the detail record at the specified row cast to Model_Vehicle_Price_Master
     */
    @Override
    public Model_Promotion_Master Detail(int row) {
        return (Model_Promotion_Master) paDetail.get(row);
    }
    /**
     * Adds a new detail row with validation.
     * Checks that the last row is not empty before adding a new row.
     *
     * @return JSONObject with "result" and optional error "message"
     * @throws CloneNotSupportedException if detail cloning fails
     */
    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (getDetailCount() > 0) {
            if ((Detail(getDetailCount() - 1).getDescription() == null || "".equals(Detail(getDetailCount() - 1).getDescription()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }

        return addDetail();
    }

    public Model_Validity_Period_Master TransactionList(int row) {
        return (Model_Validity_Period_Master) paTransactionList.get(row);
    }

    public int getTransactionListCount() {
        if (paTransactionList == null) {
            paTransactionList = new ArrayList<>();
        }

        return paTransactionList.size();
    }
    
    public Model_Promotion_Master PromotionMaster() {
        return (Model_Promotion_Master) Detail(0);
    }

    private Model_Promotion_AddOn PromotionAddOn() throws SQLException, GuanzonException {
        return new SalesModels(poGRider).PromotionAddOn();
    }
    
    public List<Model_Promotion_AddOn> PromotionAddOnList() {
        return paAddOn;
    }

    public Model_Promotion_AddOn PromotionAddOnList(int row) {
        return (Model_Promotion_AddOn) paAddOn.get(row);
    }

    public int getPromotionAddOnCount() {
        if (paAddOn == null) {
            paAddOn = new ArrayList<>();
        }

        return paAddOn.size();
    }

    public JSONObject addPromotionAddOn()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paAddOn.isEmpty()) {
            paAddOn.add(PromotionAddOn());
            poJSON = paAddOn.get(getPromotionAddOnCount() - 1).newRecord();
        } else {
            if (!paAddOn.get(paAddOn.size() - 1).getAddOnId().isEmpty()) {
                paAddOn.add(PromotionAddOn());
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add promotion add-on.");
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    private Model_Promotion_Freebie PromotionFreebie() throws SQLException, GuanzonException {
        return new SalesModels(poGRider).PromotionFreebie();
    }
    
    public List<Model_Promotion_Freebie> PromotionFreebieList() {
        return paFreebie;
    }

    public Model_Promotion_Freebie PromotionFreebieList(int row) {
        return (Model_Promotion_Freebie) paFreebie.get(row);
    }

    public int getPromotionFreebieCount() {
        if (paFreebie == null) {
            paFreebie = new ArrayList<>();
        }

        return paFreebie.size();
    }

    public JSONObject addPromotionFreebie()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paFreebie.isEmpty()) {
            paFreebie.add(PromotionFreebie());
            poJSON = paFreebie.get(getPromotionFreebieCount() - 1).newRecord();
        } else {
            if (!paFreebie.get(paFreebie.size() - 1).getFreebieId().isEmpty()) {
                paFreebie.add(PromotionFreebie());
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add promotion freebie.");
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    private Model_Promotion_Inclusion PromotionInclusion() throws SQLException, GuanzonException {
        return new SalesModels(poGRider).PromotionInclusion();
    }
    
    public List<Model_Promotion_Inclusion> PromotionInclusionList() {
        return paInclusion;
    }

    public Model_Promotion_Inclusion PromotionInclusionList(int row) {
        return (Model_Promotion_Inclusion) paInclusion.get(row);
    }

    public int getPromotionInclusionCount() {
        if (paInclusion == null) {
            paInclusion = new ArrayList<>();
        }

        return paInclusion.size();
    }

    public JSONObject addPromotionInclusion()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paInclusion.isEmpty()) {
            paInclusion.add(PromotionInclusion());
            poJSON = paInclusion.get(getPromotionInclusionCount() - 1).newRecord();
        } else {
            if (!paInclusion.get(paInclusion.size() - 1).getInclusionId().isEmpty()) {
                paInclusion.add(PromotionInclusion());
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add promotion inclusion.");
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    private Model_Promotion_Applicability PromotionApplicability() throws SQLException, GuanzonException {
        return new SalesModels(poGRider).PromotionApplicability();
    }
    
    public List<Model_Promotion_Applicability> PromotionApplicabilityList() {
        return paApplicability;
    }

    public Model_Promotion_Applicability PromotionApplicabilityList(int row) {
        return (Model_Promotion_Applicability) paApplicability.get(row);
    }

    public int getPromotionApplicabilityCount() {
        if (paApplicability == null) {
            paApplicability = new ArrayList<>();
        }

        return paApplicability.size();
    }

    public JSONObject addPromotionApplicability()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paApplicability.isEmpty()) {
            paApplicability.add(PromotionApplicability());
            poJSON = paApplicability.get(getPromotionApplicabilityCount() - 1).newRecord();
        } else {
            if (!paApplicability.get(paApplicability.size() - 1).getBrandId().isEmpty()) {
                paApplicability.add(PromotionApplicability());
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add promotion applicability.");
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Refines and validates the list.
     * 
     * This method prunes invalid rows (those with empty particulars or zero amounts for new records) 
     * and automatically appends a new detail row if the list is empty or the last entry is valid.
     * 
     * @throws CloneNotSupportedException If an error occurs while adding a new detail row.
     */
    public void ReloadAddOn() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getPromotionAddOnCount() - 1;
        while (lnCtr >= 0) {
            if (PromotionAddOnList(lnCtr).getAddOnId()== null || "".equals(PromotionAddOnList(lnCtr).getAddOnId())){
                paAddOn.remove(lnCtr);
            } 
            lnCtr--;
        }
        
        if ((getPromotionAddOnCount() - 1) >= 0) {
            if (
                (PromotionAddOnList(getPromotionAddOnCount() - 1).getAddOnId() != null && !"".equals(PromotionAddOnList(getPromotionAddOnCount() - 1).getAddOnId()))
                ) {
                addPromotionAddOn();
            }

        }

        if ((getPromotionAddOnCount() - 1) < 0) {
            addPromotionAddOn();
        }
    }

    /**
     * Refines and validates the list.
     *
     * This method prunes invalid rows (those with empty particulars or zero amounts for new records)
     * and automatically appends a new detail row if the list is empty or the last entry is valid.
     *
     * @throws CloneNotSupportedException If an error occurs while adding a new detail row.
     */
    public void ReloadFreebie() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getPromotionFreebieCount() - 1;
        while (lnCtr >= 0) {
            if (PromotionFreebieList(lnCtr).getFreebieId()== null || "".equals(PromotionFreebieList(lnCtr).getFreebieId())){
                paFreebie.remove(lnCtr);
            }
            lnCtr--;
        }

        if ((getPromotionFreebieCount() - 1) >= 0) {
            if (
                    (PromotionFreebieList(getPromotionFreebieCount() - 1).getFreebieId() != null && !"".equals(PromotionFreebieList(getPromotionFreebieCount() - 1).getFreebieId()))
            ) {
                addPromotionFreebie();
            }

        }

        if ((getPromotionFreebieCount() - 1) < 0) {
            addPromotionFreebie();
        }
    }


    /**
     * Refines and validates the list.
     *
     * This method prunes invalid rows (those with empty particulars or zero amounts for new records)
     * and automatically appends a new detail row if the list is empty or the last entry is valid.
     *
     * @throws CloneNotSupportedException If an error occurs while adding a new detail row.
     */
    public void ReloadInclusion() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getPromotionInclusionCount() - 1;
        while (lnCtr >= 0) {
            if (PromotionInclusionList(lnCtr).getInclusionId()== null || "".equals(PromotionInclusionList(lnCtr).getInclusionId())){
                paInclusion.remove(lnCtr);
            }
            lnCtr--;
        }

        if ((getPromotionInclusionCount() - 1) >= 0) {
            if (
                    (PromotionInclusionList(getPromotionInclusionCount() - 1).getInclusionId() != null && !"".equals(PromotionInclusionList(getPromotionInclusionCount() - 1).getInclusionId()))
            ) {
                addPromotionInclusion();
            }

        }

        if ((getPromotionInclusionCount() - 1) < 0) {
            addPromotionInclusion();
        }
    }


    /**
     * Refines and validates the list.
     *
     * This method prunes invalid rows (those with empty particulars or zero amounts for new records)
     * and automatically appends a new detail row if the list is empty or the last entry is valid.
     *
     * @throws CloneNotSupportedException If an error occurs while adding a new detail row.
     */
    public void ReloadApplicability() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getPromotionApplicabilityCount() - 1;
        while (lnCtr >= 0) {
            if (PromotionApplicabilityList(lnCtr).getBrandId() == null || "".equals(PromotionApplicabilityList(lnCtr).getBrandId())){
                paApplicability.remove(lnCtr);
            }
            lnCtr--;
        }

        if ((getPromotionApplicabilityCount() - 1) >= 0) {
            if (
                    (PromotionApplicabilityList(getPromotionApplicabilityCount() - 1).getBrandId() != null && !"".equals(PromotionApplicabilityList(getPromotionApplicabilityCount() - 1).getBrandId()))
            ) {
                addPromotionApplicability();
            }

        }

        if ((getPromotionApplicabilityCount() - 1) < 0) {
            addPromotionApplicability();
        }
    }
    
    
    /**
     * Sets default master record values for a new transaction.
     * 
     * Configures the branch, industry, and company identifiers, sets the current 
     * server date, and initializes the status to {@link ValidityPeriodStatus#OPEN}.
     * 
     * @return A {@link JSONObject} indicating the initialization result.
     */
    @Override
    public JSONObject initFields() {
        poJSON = new JSONObject();
        
        try {
            //Put initial model values here/
            Master().setCompanyId(psCompanyId);
//            Master().setFromDate(SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            Date ldServerDate = poGRider.getServerDate();
            Calendar loFromDate = Calendar.getInstance();
            Calendar loToDate = Calendar.getInstance();
            loFromDate.setTime(ldServerDate);
            loToDate.setTime(ldServerDate);

            // Set to the last day of the current month
            loFromDate.set(Calendar.DAY_OF_MONTH,loFromDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            loToDate.set(Calendar.DAY_OF_MONTH,loToDate.getActualMaximum(Calendar.DAY_OF_MONTH));

            Master().setFromDate(SQLUtil.toDate(xsDateShort(loFromDate.getTime()),SQLUtil.FORMAT_SHORT_DATE));
            Master().setThruDate(SQLUtil.toDate(xsDateShort(loToDate.getTime()),SQLUtil.FORMAT_SHORT_DATE));
            Master().setRecordStatus(ValidityPeriodStatus.APPROVED); //Set to approve
            
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Validates transaction entries based on status.
     * Uses ValidityMasterValidator to perform comprehensive validation.
     *
     * @param status the transaction status to validate against
     * @return JSONObject with "result" and optional "message" fields
     */
    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = new ValidityMasterValidator();
        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());
        poJSON = loValidator.validate();
        return poJSON;
    }
    
    private JSONObject checkExistingPromotion(){
        try {
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                                                    " a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.VOID)
                                                    + " AND a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.CANCELLED)
                                                    + " AND a.sValidIDx != " + SQLUtil.toSQL(Master().getValidityId())
                                                    + " AND a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyId())
                                                    );
          lsSQL = lsSQL 
                    + " AND ((a.dFromDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dFromDate <= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")"
                    + " AND ((a.dThruDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dThruDate IS NULL OR a.dThruDate >= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")";
      
            System.out.println("checkExistingPromotion SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(loRS.next()){    
                    poJSON.put("result", "error");
                    poJSON.put("message", "A Vehicle Promotion already exists for the selected validity period.");
                    return poJSON;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
            
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Performs pre-save validation and assignments.
     * Generates new IDs for new records, validates all entries, removes invalid detail rows,
     * and prepares records for database persistence.
     *
     * @return JSONObject with "result" and optional "message" fields
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if record cloning fails
     */
    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        /*Put system validations and other assignments here*/
        System.out.println("Class Edit Mode : " + getEditMode());
        System.out.println("Master Edit Mode : " + Master().getEditMode());
        
        //Re-set the transaction no 
        if(getEditMode() == EditMode.ADDNEW){
            Master().setValidityId(Master().getNextCode());
        }
        
        Master().setValidityDescription(PromotionMaster().getDescription());
            
        poJSON = isEntryOkay(Master().getRecordStatus());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = checkExistingPromotion();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        Iterator<Model> detail = Detail().iterator();
        int lnDetailRow = 0;
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sPromDesc");
            
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
        
        Iterator<Model_Promotion_AddOn> laAddOn = paAddOn.iterator();
        int lnAddOnRow = 0;
        while (laAddOn.hasNext()) {
            Model_Promotion_AddOn item = laAddOn.next(); // Store the item before checking conditions
            String lsId = item.getAddOnId();
            if ((lsId == null || "".equals(lsId))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    laAddOn.remove(); // Correctly remove the item
                } 
            } else {
                lnAddOnRow++;
            }
        }
        
        Iterator<Model_Promotion_Freebie> laFreebie = paFreebie.iterator();
        int lnFreebieRow = 0;
        while (laFreebie.hasNext()) {
            Model_Promotion_Freebie item = laFreebie.next(); // Store the item before checking conditions
            String lsId = item.getFreebieId();
            if ((lsId == null || "".equals(lsId))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    laFreebie.remove(); // Correctly remove the item
                } 
            } else {
                lnFreebieRow++;
            }
        }
        
        Iterator<Model_Promotion_Inclusion> laInclusion = paInclusion.iterator();
        int lnInclusionRow = 0;
        while (laInclusion.hasNext()) {
            Model_Promotion_Inclusion item = laInclusion.next(); // Store the item before checking conditions
            String lsId = item.getInclusionId();
            if ((lsId == null || "".equals(lsId))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    laInclusion.remove(); // Correctly remove the item
                } 
            } else {
                lnInclusionRow++;
            }
        }
        
        Iterator<Model_Promotion_Applicability> laApplicability = paApplicability.iterator();
        int lnApplicability = 0;
        while (laApplicability.hasNext()) {
            Model_Promotion_Applicability item = laApplicability.next(); // Store the item before checking conditions
            String lsId = item.getBrandId();
            if ((lsId == null || "".equals(lsId))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    laApplicability.remove(); // Correctly remove the item
                } 
            } else {
                lnApplicability++;
            }
        }
        
        if(lnAddOnRow == 0 && lnFreebieRow == 0 && lnInclusionRow == 0){
            poJSON = setJSON("error", "No promotion detail to be save.");
            return poJSON;
        }
        
        if(lnApplicability == 0){
            poJSON = setJSON("error", "No promotion applicability to be save.");
            return poJSON;
        }
        
        //Set value for promotion master
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            System.out.println("Record Status : " + Detail(lnCtr).getRecordStatus());
            if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                Detail(lnCtr).setPromotionCode(Detail(lnCtr).getNextCode());
            }
            Detail(lnCtr).setValidityId(Master().getValidityId());
            Detail(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        //Set value for promotion addon
        for (int lnCtr = 0; lnCtr <= getPromotionAddOnCount() - 1; lnCtr++) {
            System.out.println("Record Status : " + PromotionAddOnList(lnCtr).getRecordStatus());
            if(PromotionAddOnList(lnCtr).getEditMode() == EditMode.ADDNEW){
                PromotionAddOnList(lnCtr).setPromoAddId(PromotionAddOnList(lnCtr).getNextCode());
                PromotionAddOnList(lnCtr).setPromotionCode(PromotionMaster().getPromotionCode());
            }
            PromotionAddOnList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            PromotionAddOnList(lnCtr).setModifiedDate(poGRider.getServerDate());
        }

        //Set value for promotion freebie
        for (int lnCtr = 0; lnCtr <= getPromotionFreebieCount()- 1; lnCtr++) {
            System.out.println("Record Status : " + PromotionFreebieList(lnCtr).getRecordStatus());
            if(PromotionFreebieList(lnCtr).getEditMode() == EditMode.ADDNEW){
                PromotionFreebieList(lnCtr).setPromoFreeId(PromotionFreebieList(lnCtr).getNextCode());
                PromotionFreebieList(lnCtr).setPromotionCode(PromotionMaster().getPromotionCode());
            }
            PromotionFreebieList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            PromotionFreebieList(lnCtr).setModifiedDate(poGRider.getServerDate());
        }

        //Set value for promotion inclusion
        for (int lnCtr = 0; lnCtr <= getPromotionInclusionCount() - 1; lnCtr++) {
            System.out.println("Record Status : " + PromotionAddOnList(lnCtr).getRecordStatus());
            if(PromotionInclusionList(lnCtr).getEditMode() == EditMode.ADDNEW){
                PromotionInclusionList(lnCtr).setPromoInclusionId(PromotionInclusionList(lnCtr).getNextCode());
                PromotionInclusionList(lnCtr).setPromotionCode(PromotionMaster().getPromotionCode());
            }
            PromotionInclusionList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            PromotionInclusionList(lnCtr).setModifiedDate(poGRider.getServerDate());
        }

        //Set value for promotion applicability
        for (int lnCtr = 0; lnCtr <= getPromotionApplicabilityCount()- 1; lnCtr++) {
            System.out.println("Record Status : " + PromotionApplicabilityList(lnCtr).getRecordStatus());
            if(PromotionApplicabilityList(lnCtr).getEditMode() == EditMode.ADDNEW){
                PromotionApplicabilityList(lnCtr).setPromotionApplicabilityId(PromotionApplicabilityList(lnCtr).getNextCode());
                PromotionApplicabilityList(lnCtr).setPromotionCode(PromotionMaster().getPromotionCode());
            }
            PromotionApplicabilityList(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            PromotionApplicabilityList(lnCtr).setModifiedDate(poGRider.getServerDate());
        }
        
        Master().setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
     * Performs final save validation on transaction.
     * Validates entry status against the OPEN status.
     *
     * @return JSONObject with "result" and optional "message" fields
     * @throws CloneNotSupportedException if record cloning fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    @Override
    public JSONObject save() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put saving business rules here*/
        return isEntryOkay(ValidityPeriodStatus.OPEN);
    }

    /**
     * Initializes the SQL browse statement with appropriate filters.
     * Builds SQL_BROWSE with status filtering based on psTranStat field.
     * Joins master and detail tables for complete transaction view.
     */
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
                "  b.sPromCode, " +
                "  b.sPromDesc, " +
                "  b.cPaymForm, " +
                "  b.sModified," +
                "  b.dModified " +
                "FROM Validity_Period_Master a " +
                "INNER JOIN Promotion_Master b ON b.sValidIDx = a.sValidIDx";
        if(lsCondition != null && !"".equals(lsCondition)){
            SQL_BROWSE = MiscUtil.addCondition(SQL_BROWSE, lsCondition);
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
    public JSONObject printTransaction()
        throws CloneNotSupportedException, SQLException, GuanzonException {

    poJSON = new JSONObject();
    pbIsPrinted = false;
    
    try {

        String jrxmlPath =
                System.getProperty("sys.default.path.config")
                + "/Reports/Promotion.jrxml";

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
        
        /*
         * Create the final JasperPrint.
         * The first rate will initialize it.
         */
        JasperPrint finalPrint = null;

        // ---------------------------------------------
        // Parameters
        // ---------------------------------------------
        Map<String, Object> parameters = new HashMap<>();


        parameters.put("watermarkImagePath", lsWatermarkPath);
        parameters.put("sValidity",Master().getValidityId()+ "-"+ poGRider.getServerDate());
        parameters.put("sCompany",Master().Company().getCompanyName().toUpperCase());
        String lsAddress = Master().Company().getCompanyAddress();
        if (Master().Company().TownCity().getDescription() != null && !"".equals(Master().Company().TownCity().getDescription())) {
            lsAddress = lsAddress + " " + Master().Company().TownCity().getDescription();
        }

        if (Master().Company().TownCity().Province().getDescription() != null
                && !"".equals(Master().Company().TownCity().Province().getDescription())) {
            lsAddress = lsAddress+ ", "+ Master().Company().TownCity().Province().getDescription();
        }

        parameters.put("sAddress",lsAddress.toUpperCase());
        parameters.put("sValidityDesc",Master().getValidityDescription().toUpperCase());

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
        if(lsLandLine != null) {
            if(!lsLandLine.isEmpty()){
                lsContact = lsContact 
                            + "\nTel No : " + lsLandLine;
            }
        }
        if(lsEmail != null) {
            if(!lsEmail.isEmpty()){
                lsContact = lsContact + "\nEmail Address : " + lsEmail;
            } 
        }

        parameters.put("sBranch",lsBranchName.toUpperCase());
        parameters.put("sBranchAddress",lsBranchAddress.toUpperCase());
        parameters.put("sBranchDesc",lsBranchDesc.toUpperCase());
        parameters.put("sContact",lsContact);
        parameters.put("validFrom",SQLUtil.dateFormat(Master().getFromDate(),SQLUtil.FORMAT_LONG_DATE));
        String lsToDate = "";
        if(Master().getThruDate() == null){
            lsToDate = "Present";
        } else {
            lsToDate = SQLUtil.dateFormat(Master().getThruDate(),SQLUtil.FORMAT_LONG_DATE);
        }
        parameters.put("validTo",lsToDate);
        
        parameters.put("nPromoDiscount",PromotionMaster().getDiscountAmount());
        String lsPayForm = "";
        switch(PromotionMaster().getPaymentForm()){
            case "0": //Cash 
                lsPayForm = "Cash";
            break;
            case "1": //Financing 
                lsPayForm = "Financing";
            break;
            case "2": //PO
                lsPayForm = "PO";
            break;
            default: //Applicable to all
                lsPayForm = "Applicable to all";
            break;
        
        }
        parameters.put("nPayForm",lsPayForm);
        
        // ---------------------------------------------
        // Build data
        // ---------------------------------------------
        List<Map<String, Object>> rows = buildPromotionAddOn();
        List<Map<String, ?>> addons = new ArrayList<>(rows);
        List<Map<String, Object>> freebieRow = buildPromotionFreebie();
        List<Map<String, ?>> freebies = new ArrayList<>(freebieRow);
        List<Map<String, Object>> inclusionRow = buildPromotionInclusion();
        List<Map<String, ?>> inclusions = new ArrayList<>(inclusionRow);
        List<Map<String, Object>> applicabilityRow = buildPromotionApplicability();
        List<Map<String, ?>> applicabilities = new ArrayList<>(applicabilityRow);
        
        
        // --- hand the rows to the report ---
        parameters.put("AddOnDataSource",    new JRMapCollectionDataSource((Collection) addons));
        parameters.put("FreebieDataSource",    new JRMapCollectionDataSource((Collection) freebies));
        parameters.put("InclusionDataSource", new JRMapCollectionDataSource((Collection) inclusions));
        parameters.put("ApplicabilityDataSource", new JRMapCollectionDataSource((Collection) applicabilities));
        

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
                .log(Level.SEVERE, null, ex);
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
    
    private List<Map<String, Object>> buildPromotionAddOn() {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        try{
            //Group by dp rate 
            int lnRow = 0;
            for(int lnCtr = 0;lnCtr < getPromotionAddOnCount();lnCtr++){
                if(PromotionAddOnList(lnCtr).getRecordStatus()){
                    lnRow++;
                    String lsType = PromotionAddOnList(lnCtr).getPromoType();
                        addAddOn(rows
                                , PromotionAddOnList(lnCtr).AddOnType().getAddTypeName()
                                , lsType
                                , PromotionAddOnList(lnCtr).AddOnType().getAmount()
                                , PromotionAddOnList(lnCtr).getDiscountAmount()
                        );
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehiclePromotion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
 
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addAddOn(List<Map<String, Object>> rows,
                                String sAddOnTypeName,
                                String sPromoType,
                                double nAmount,
                                double nDiscountAmount) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(sAddOnTypeName == null){ sAddOnTypeName = "";}
        if(sPromoType == null){ sPromoType = "";}
        row.put("sAddOnTypeName", sAddOnTypeName.toUpperCase());
        row.put("sPromoType", sPromoType.toUpperCase());
        row.put("nAmount", nAmount);
        row.put("nDiscountAmount", nDiscountAmount);
        rows.add(row);
    }
    
    private List<Map<String, Object>> buildPromotionFreebie() {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        try{
            //Group by dp rate 
            int lnRow = 0;
            for(int lnCtr = 0;lnCtr < getPromotionFreebieCount();lnCtr++){
                if(PromotionFreebieList(lnCtr).getRecordStatus()){
                    lnRow++;
                        addFreebie(rows
                                , PromotionFreebieList(lnCtr).Freebie().getDescription()
                                , PromotionFreebieList(lnCtr).getQuantity()
                        );
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehiclePromotion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
 
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addFreebie(List<Map<String, Object>> rows,
                                String sFreebie,
                                int nFreebieQty) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(sFreebie == null){ sFreebie = "";}
        row.put("sFreebie", sFreebie.toUpperCase());
        row.put("nFreebieQty", nFreebieQty);
        rows.add(row);
    }
    
    private List<Map<String, Object>> buildPromotionInclusion() {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        try{
            //Group by dp rate 
            int lnRow = 0;
            for(int lnCtr = 0;lnCtr < getPromotionInclusionCount();lnCtr++){
                if(PromotionInclusionList(lnCtr).getRecordStatus()){
                    lnRow++;
                        addInclusion(rows
                                , PromotionInclusionList(lnCtr).Inclusion().getDescription()
                                , PromotionInclusionList(lnCtr).getQuantity()
                        );
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehiclePromotion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
 
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addInclusion(List<Map<String, Object>> rows,
                                String sInclusion,
                                int nInclusionQty) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(sInclusion == null){ sInclusion = "";}
        row.put("sInclusion", sInclusion.toUpperCase());
        row.put("nInclusionQty", nInclusionQty);
        rows.add(row);
    }
    
    private List<Map<String, Object>> buildPromotionApplicability() {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        try{
            //Group by dp rate 
            int lnRow = 0;
            for(int lnCtr = 0;lnCtr < getPromotionApplicabilityCount();lnCtr++){
                if(PromotionApplicabilityList(lnCtr).getRecordStatus()){
                    lnRow++;
                    String lsDescription = PromotionApplicabilityList(lnCtr).Brand().getDescription();
                    if(checkNotEmpty(PromotionApplicabilityList(lnCtr).getModelId())){
                        lsDescription = lsDescription + " " + PromotionApplicabilityList(lnCtr).Model().getDescription();
                    }   
                    if(checkNotEmpty(PromotionApplicabilityList(lnCtr).getVariantId())){
                        lsDescription = lsDescription + " " + PromotionApplicabilityList(lnCtr).ModelVariant().getDescription();
                    }   
                    addApplicability(rows
                            , lsDescription
                            , PromotionApplicabilityList(lnCtr).getExcludeFlag()
                    );
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehiclePromotion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
 
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addApplicability(List<Map<String, Object>> rows,
                                String sDescription,
                                String sExlude) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(sDescription == null){ sDescription = "";}
        row.put("sDescription", sDescription.toUpperCase());
        row.put("sExlude", sExlude);
        rows.add(row);
    }
    
    /**
     * Retrieves the user and timestamp of the last status update.
     * Queries transaction history to find who last updated to the specified status.
     *
     * @param fsStatus the status code to find update information for
     * @return JSONObject with "result", "sUpdateByx" (user), and "sUpdateDte" (timestamp)
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public JSONObject getUpdateStatusBy(String fsStatus) throws SQLException, GuanzonException {
        String lsUpdateBy = "";
        String lsDate = "";
        String lsSQL = "SELECT b.sModified,b.dModified FROM "+Master().getTable()+" a "
                     + " LEFT JOIN Transaction_Status_History b ON b.sSourceNo = a.sValidIDx AND b.sTableNme = "+ SQLUtil.toSQL(Master().getTable())
                     + " AND b.cRefrStat = "+ SQLUtil.toSQL(fsStatus) ;
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sValidIDx =  " + SQLUtil.toSQL(Master().getValidityId()));
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
    
    /**
     * Retrieves transaction status history as a cached row set.
     * Used for testing; queries all status changes with user and approval information.
     *
     * @return CachedRowSet containing status history records ordered by modification date
     * @throws SQLException if a database error occurs
     */
    protected CachedRowSet getStatusHistoryTest() throws SQLException {
        String lsSQL = "SELECT  a.sTableNme, a.sSourceNo, a.sRemarksx, a.cRefrStat cTranStat, IFNULL(c.sCompnyNm, '-') xModified, IFNULL(e.sCompnyNm, '-') xApproved, a.dModified, a.dApproved, a.sModified, a.sApproved " +
                    " FROM Transaction_Status_History a " +
                    "LEFT JOIN xxxSysUser b ON b.sUserIDxx = a.sModified " +
                    "LEFT JOIN Client_Master c ON b.sEmployNo = c.sClientID " +
                    "LEFT JOIN xxxSysUser d ON d.sUserIDxx = a.sApproved " +
                    "LEFT JOIN Client_Master e ON d.sEmployNo = e.sClientID " +
                    " WHERE a.sSourceNo = " + SQLUtil.toSQL(Master().getValidityId()) +
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
                + " WHERE a.sSourceNo = " + SQLUtil.toSQL(Master().getValidityId()) + " AND a.sTableNme = " + SQLUtil.toSQL(Master().getTable()) + " ORDER BY a.dModified, a.sTransNox";
        ResultSet loRS = this.poGRider.executeQuery(lsSQL);
        RowSetFactory factory = RowSetProvider.newFactory();
        CachedRowSet rowset = factory.createCachedRowSet();
        rowset.populate(loRS);
        MiscUtil.close(loRS);
        return rowset;
    }

    /**
     * Displays the transaction status history with readable status values.
     * Converts status codes to labels and displays in UI if available.
     * Shows entry user and date information.
     *
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws Exception if UI display fails
     */
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
                case ValidityPeriodStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case ValidityPeriodStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case ValidityPeriodStatus.APPROVED:
                    crs.updateString("cRefrStat", "APPROVED");
                    break;
                case ValidityPeriodStatus.VOID:
                    crs.updateString("cRefrStat", "VOIDED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat) {
                        case ValidityPeriodStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case ValidityPeriodStatus.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case ValidityPeriodStatus.APPROVED:
                            crs.updateString("cRefrStat", "APPROVED");
                            break;
                        case ValidityPeriodStatus.VOID:
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
            showStatusHistoryUI("Vehicle Price", (String) poMaster.getValue("sValidIDx"), entryBy, entryDate, crs);
        }
    }
    /**
     * Retrieves the user and timestamp of who created the current transaction.
     *
     * @return JSONObject containing "sCompnyNm" (user) and "sEntryDte" (timestamp)
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM "+Master().getTable()+" a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sValidIDx AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Master().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sValidIDx =  " + SQLUtil.toSQL(Master().getValidityId()));
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
    
    /**
     * Retrieves system user information by ID.
     * Returns either employee ID or company name based on flag.
     *
     * @param fsId the system user ID to look up
     * @param fbIsID if true, return employee ID; if false, return company name
     * @return the requested user information, or empty string if not found
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
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
