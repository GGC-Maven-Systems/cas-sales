/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionStatusHistory;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.Model_Validity_Period_Master;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;
import ph.com.guanzongroup.cas.sales.validator.ValidityMasterValidator;

import javax.script.ScriptException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONArray;
import ph.com.guanzongroup.cas.sales.model.Model_Vehicle_AddOn_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Vehicle_AddOn_Type;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.AddOnTypeStatus;

/**
 * Manages vehicle add on transactions including approval, voiding, and cancellation.
 * Handles master and detail records for vehicle financing pricing with validity period management.
 *
 * @author Arsiela 09182026
 */
public class VehicleAddOns extends Transaction {
    /** Company ID for filtering and transaction context */
    public String psCompanyId = "";
    /** User ID of the approving officer */
    public String psApprover = "";

    /** List of master records for batch operations */
    public List<Model_Model_Variant> paVehicleList;
    public List<Model> paVariantDetail;
    
    /**
     * Initializes the transaction with source code and loads required models.
     * Sets up master validity period and vehicle add on details.
     *
     * @return JSONObject containing initialization result with "result" and "message" fields
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "VAdn";

        poMaster = new SalesModels(poGRider).ValidityPeriodMaster();
        poDetail = new SalesModels(poGRider).VehicleAddOnMaster();

        paVehicleList = new ArrayList<Model_Model_Variant>();
        paVariantDetail = new ArrayList<>();
        psApprover = "";
        setApproving("");
        return initialize();
    }

    /**
     * Returns the transaction source code.
     *
     * @return the source code for this transaction ("VAdn" for vehicle add on)
     */
    @Override
    public String getSourceCode() { return SOURCE_CODE; }
    
    /**
     * Sets the company ID for filtering and transaction context.
     *
     * @param companyId the company ID to set
     */
    public void setCompanyId(String companyId) { psCompanyId = companyId; }
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
        
        poJSON = populateVehicleList();
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
            
            String sql = "SELECT sAddOnIDx FROM " + this.poDetail.getTable() + " WHERE sValidIDx = " + SQLUtil.toSQL(transactionNo) + " ORDER BY sAddOnIDx";
            ResultSet rs = this.poGRider.executeQuery(sql);

            while(rs.next()) {
                Model loDetail = (Model)this.poDetail.clone();
                loDetail.newRecord();
                this.poJSON = loDetail.openRecord( rs.getString("sAddOnIDx"),transactionNo);
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
        
        poJSON = populateVehicleList();
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
                                    ((Model)this.paDetail.get(lnCtr)).setValue("sAddOnIDx", Detail(lnCtr).getNextCode());
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
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sValidIDx"),"", lsStatus, false, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
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
        poJSON = statusChange(Master().getTable(), (String) Master().getValue("sValidIDx"),"", lsStatus, false, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
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
    
    /**
     * Populates detail rows with available vehicle variants.
     * for each vehicle variant with applicable rates.
     *
     * @return JSONObject with "result" and optional error "message"
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if detail cfloning fails
     */
    public JSONObject loadVehicleList() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        String lsSQL = " SELECT   " 
                       + " c.sVrntIDxx " 
                       + " , a.sIndstCdx " 
                       + " , c.nSelPrice " 
                       + " FROM Brand a " 
                       + " LEFT JOIN Model b On b.sBrandIDx = a.sBrandIDx " 
                       + " LEFT JOIN Model_Variant c ON c.sModelIDx = b.sModelIDx " ;
        lsSQL = MiscUtil.addCondition(lsSQL," a.sIndstCdx =  " + SQLUtil.toSQL(poGRider.getIndustry())
                       + " AND c.cEndOfLfe =  " + SQLUtil.toSQL(Logical.NO)
                       + " AND c.nSelPrice > 0.00 "
                    );
        
        lsSQL = lsSQL + " ORDER BY b.sDescript, c.sDescript ASC ";
        System.out.println("loadVehicleList SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON = setJSON("error", "No vehicle model variant available.");
            return poJSON;
        }
        
        ReloadDetail();
        String lsVariantId = "";
        while (loRS.next()) {
            lsVariantId = loRS.getString("sVrntIDxx");
            if(lsVariantId != null && !"".equals(lsVariantId)){
                paVehicleList.add(ModelVariant());
                poJSON = paVehicleList.get(paVehicleList.size()-1).openRecord(lsVariantId);
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            }
        }
        MiscUtil.close(loRS);
        
        sortDetail();
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private Model_Model_Variant ModelVariant() {
        return new ParamModels(poGRider).ModelVariant();
    }
    public int getVehicleListCount() {
        if (paVehicleList == null) {
            paVehicleList = new ArrayList<>();
        }

        return paVehicleList.size();
    }
    
    public Model_Model_Variant VehicleList(int row) {
        return (Model_Model_Variant) paVehicleList.get(row);
    }
    
//    public ArrayList<String> loadUniqueAddOnTypeId(String fsAsOfDate) {
//        ArrayList<String> laType = new ArrayList<>();
//        
//        try {
//            initSQL();
//            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
//                    " a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.VOID)
//                    + " AND a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.CANCELLED)
//            );
//    //          lsSQL = lsSQL
//    //                    + " AND ((a.dFromDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dFromDate <= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")"
//    //                    + " AND ((a.dThruDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dThruDate IS NULL OR a.dThruDate >= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")";
//            lsSQL = lsSQL + " GROUP BY b.sAddTypex ORDER BY b.sAddTypex DESC";
//            System.out.println("loadUniqueAddOnTypeId SQL: " + lsSQL);
//            ResultSet loRS = poGRider.executeQuery(lsSQL);
//            if (MiscUtil.RecordCount(loRS) <= 0) {
//                return laType;
//            }
//            String lsAddOnType = "";
//            while (loRS.next()) {
//                lsAddOnType = loRS.getString("sAddTypex");
//                if(lsAddOnType != null && !"".equals(lsAddOnType)){
//                    if(laType.isEmpty()){
//                        laType.add(lsAddOnType);
//                    } else {
//                        if(!laType.contains(lsAddOnType)){
//                            laType.add(lsAddOnType);
//                        }
//                    }
//                }
//            }   
//        } catch (SQLException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//            return laType;
//        }
//        
//        return laType;
//    }
    
    public JSONArray loadUniqueAddOnTypeId(String fsAsOfDate) throws SQLException, GuanzonException {
        JSONArray loJSONArray = new JSONArray();
        JSONObject loJSON = new JSONObject();
        try {
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                    " a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.VOID)
                    + " AND a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.CANCELLED)
            );
    //          lsSQL = lsSQL
    //                    + " AND ((a.dFromDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dFromDate <= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")"
    //                    + " AND ((a.dThruDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dThruDate IS NULL OR a.dThruDate >= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")";
            lsSQL = lsSQL + " GROUP BY b.sAddTypex ORDER BY b.sAddTypex DESC";
            System.out.println("loadUniqueAddOnTypeId SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while(loRS.next()){
                    loJSON = new JSONObject();
                    loJSON.put("sAddTypex", loRS.getString("sAddTypex"));
                    loJSON.put("sAddTypNm", loRS.getString("sAddTypNm"));
                    loJSONArray.add(loJSON);
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        
        return loJSONArray;
    }
    
    
    public String getAddOnTypeDescription(String fsAddOnTypeID){
        poJSON = new JSONObject();
        try {
            Model_Vehicle_AddOn_Type loObj = new SalesModels(poGRider).VehicleAddOnType();
            loObj.initialize();
            poJSON = loObj.openRecord(fsAddOnTypeID);
            if(isJSONSuccess(poJSON)){
                return loObj.getAddTypeName();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return "";
        }
        
        return "";
    }
    
    public Double getAmount(String fsVariantId, String fsTypeId, String fsAsOfDate){
        try {
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                    " a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.VOID)
                    + " AND a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.CANCELLED)
                    + " AND b.sVrntIDxx = " + SQLUtil.toSQL(fsVariantId)
                    + " AND b.sAddTypex = " + SQLUtil.toSQL(fsTypeId)
            );
    //          lsSQL = lsSQL
    //                    + " AND ((a.dFromDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dFromDate <= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")"
    //                    + " AND ((a.dThruDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dThruDate IS NULL OR a.dThruDate >= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")";

            System.out.println("getAmount SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) <= 0) {
                return 0.0000;
            }
            if (loRS.next()) {
                return loRS.getDouble("nAmountxx");
            }   
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return 0.0000;
        }
        return 0.0000;
    }
    
    public JSONObject OpenAddOn(String fsVariantId, String fsTypeId){
         try {
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                    " a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.VOID)
                    + " AND a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.CANCELLED)
                    + " AND b.sVrntIDxx = " + SQLUtil.toSQL(fsVariantId)
                    + " AND b.sAddTypex = " + SQLUtil.toSQL(fsTypeId)
            );
    //          lsSQL = lsSQL
    //                    + " AND ((a.dFromDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dFromDate <= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")"
    //                    + " AND ((a.dThruDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dThruDate IS NULL OR a.dThruDate >= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")";

            System.out.println("OpenAddOn SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) <= 0) {
            } else {
                if (loRS.next()) {
                    if(loRS.getString("sValidIDx") != null && !"".equals(loRS.getString("sValidIDx"))){
                        return OpenTransaction(loRS.getString("sValidIDx"));    
                    }
                } 
            }
            
            poJSON = NewTransaction();
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }
            
            Master().setCompanyId(psCompanyId);
            
            ReloadDetail();
            Detail(getDetailCount() -1).setVariantId(fsVariantId);
            Detail(getDetailCount() -1).setAddOnType(fsTypeId);
            
        } catch (SQLException | CloneNotSupportedException | GuanzonException | ScriptException  ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return poJSON;
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
                    "Validity ID»Description»From Date»Thru Date",
                    "sValidIDx»sValidDsc»dFromDate»dThruDate",
                    "a.sValidIDx»a.sValidDsc»a.dFromDate»a.dThruDate",
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
    
    public JSONObject SearchAddOnType(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        AddOnType object = new SalesControllers(poGRider, logwrapr).AddOnType();
        object.setRecordStatus(AddOnTypeStatus.ACTIVE);
        object.setWithUI(pbWithUI);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setValidityDescription(object.getModel().getAddTypeName());
            
            for(int lnCtr = 0;lnCtr < getDetailCount();lnCtr++){
                Detail(lnCtr).setAddOnType(object.getModel().getAddTypeCode());
                System.out.println("Variant : " + Detail(lnCtr).getVariantId());
                System.out.println("Add On Type: " + Detail(lnCtr).getAddOnType());
            }
        }
        
        return poJSON;
    }
    
    /**
     * Populates detail rows with available vehicle variants.
     * for each vehicle variant with applicable rates.
     *
     * @return JSONObject with "result" and optional error "message"
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if detail cloning fails
     */
    public JSONObject populateVehicleList() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        
        String lsSQL = " SELECT   " 
                       + " c.sVrntIDxx " 
                       + " , a.sIndstCdx " 
                       + " , c.nSelPrice " 
                       + " FROM Brand a " 
                       + " LEFT JOIN Model b On b.sBrandIDx = a.sBrandIDx " 
                       + " LEFT JOIN Model_Variant c ON c.sModelIDx = b.sModelIDx " ;
        lsSQL = MiscUtil.addCondition(lsSQL," a.sIndstCdx =  " + SQLUtil.toSQL(poGRider.getIndustry())
                       + " AND c.cEndOfLfe =  " + SQLUtil.toSQL(Logical.NO)
                       + " AND c.nSelPrice > 0.00 "
                    );
        
        lsSQL = lsSQL + " ORDER BY b.sDescript, c.sDescript ASC ";
        System.out.println("populateVehicleList SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            poJSON = setJSON("error", "No vehicle model variant available.");
            return poJSON;
        }
        
        ReloadDetail();
        boolean lbExist = false;
        String lsVariantId = "";
        double ldblSelPrice = 0.00;
        int lnRow = 0;
        String lsAddOnType = "";
        while (loRS.next()) {
            lbExist = false;
            lsVariantId = loRS.getString("sVrntIDxx");
            ldblSelPrice = loRS.getDouble("nSelPrice");
            if(lsVariantId != null && !"".equals(lsVariantId)){
                //Check variant if it exist in detail 
                lnRow = 0;
                for(lnRow = 0; lnRow < getDetailCount(); lnRow++){
                    if(Detail(lnRow).getAddOnType() != null && !"".equals(Detail(lnRow).getAddOnType())){
                        lsAddOnType = Detail(lnRow).getAddOnType();
                    }
                    if(lsVariantId.equals(Detail(lnRow).getVariantId())){
                        lbExist = true;
                        break;
                    }
                }
                
                if(getEditMode() == EditMode.ADDNEW || getEditMode() == EditMode.UPDATE){
                    if(!lbExist){
                        Detail(getDetailCount() - 1).setVariantId(lsVariantId);
                        Detail(getDetailCount() - 1).setSRPAmount(ldblSelPrice);
                        Detail(getDetailCount() - 1).setAddOnType(lsAddOnType);
                    }
                    
                    ReloadDetail();

                }
            }
        }
        MiscUtil.close(loRS);
        
        sortDetail();
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /**
     * Refines and validates the transaction detail list.This method prunes invalid rows (those with empty particulars or zero amounts for new records)
        and automatically appends a new detail row if the list is empty or the last entry is valid.
    * 
    *
    * @throws CloneNotSupportedException If an error occurs while adding a new detail row.
    */
    public void ReloadDetail() throws CloneNotSupportedException{
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            if ((Detail(lnCtr).getVariantId() == null || "".equals(Detail(lnCtr).getVariantId()))
//                && (Detail(lnCtr).getAddOnType() == null || "".equals(Detail(lnCtr).getAddOnType()))
                ) {
                deleteDetail(lnCtr);
            } 
            lnCtr--;
        }
            
        if ((getDetailCount() - 1) >= 0) {
            if (
                (Detail(getDetailCount() - 1).getVariantId() != null && !"".equals(Detail(getDetailCount() - 1).getVariantId()))
//                && (Detail(getDetailCount() - 1).getAddOnType() != null && !"".equals(Detail(getDetailCount() - 1).getAddOnType()))
                ) {
                AddDetail();
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
        
    }
    
    public void removeDetail(String fsType){
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            if (fsType.equals(Detail(lnCtr).getAddOnType())) {
                if(EditMode.ADDNEW == Detail(lnCtr).getEditMode()){
                    deleteDetail(lnCtr);
                } else {
                    Detail(lnCtr).setRecordStatus(false);
                }
            } 
            lnCtr--;
        }
    }
    
    public void sortDetail(){
        //Sort paDetail by brand, model, variant;
        paDetail.sort(
            Comparator.comparing(
                o -> getBrandDescription((Model_Vehicle_AddOn_Master) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ).thenComparing(
                o -> getModelDescription((Model_Vehicle_AddOn_Master) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ).thenComparing(
                o -> getModelVariantDescription((Model_Vehicle_AddOn_Master) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ).thenComparing(
                o -> getAddOnType((Model_Vehicle_AddOn_Master) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                )
        );
    }
    
    private String getBrandDescription(Model_Vehicle_AddOn_Master poDetail) {
        try {
            return poDetail.ModelVariant()
                    .Model()
                    .Brand()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }

    private String getModelDescription(Model_Vehicle_AddOn_Master poDetail) {
        try {
            return poDetail.ModelVariant()
                    .Model()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }

    private String getModelVariantDescription(Model_Vehicle_AddOn_Master poDetail) {
        try {
            return poDetail.ModelVariant()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }

    private String getAddOnType(Model_Vehicle_AddOn_Master poDetail) {
        return poDetail.getAddOnType();
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
    
    /**
     * Gets the master record as Model_Validity_Period_Master.
     *
     * @return the current master record cast to Model_Validity_Period_Master
     */
    @Override
    public Model_Validity_Period_Master Master() { 
        return (Model_Validity_Period_Master) poMaster; 
    }
    
    /**
     * Gets a detail record by row index as Model_Vehicle_AddOn_Master.
     *
     * @param row the index of the detail record to retrieve
     * @return the detail record at the specified row cast to Model_Vehicle_AddOn_Master
     */
    @Override
    public Model_Vehicle_AddOn_Master Detail(int row) {
        return (Model_Vehicle_AddOn_Master) paDetail.get(row); 
    }
    public Model_Vehicle_AddOn_Master VariantDetail(int row) {
        return (Model_Vehicle_AddOn_Master) paVariantDetail.get(row); 
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
            if ((Detail(getDetailCount() - 1).getVariantId() == null || "".equals(Detail(getDetailCount() - 1).getVariantId()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }

        return addDetail();
    }
    
    public JSONObject AddVariantDetail() throws CloneNotSupportedException {
        if (getVariantDetailCount() > 0) {
            if ((VariantDetail(getVariantDetailCount() - 1).getVariantId() == null || "".equals(VariantDetail(getVariantDetailCount() - 1).getVariantId()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        if (!pbInitTran) {
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        } else {
            this.paVariantDetail.add(new SalesModels(poGRider).VehicleAddOnMaster());
            this.poJSON.put("result", "success");
            return this.poJSON;
        }
    }
    
    public int getVariantDetailCount() {
        if(paVariantDetail == null){
            return -1;
        }
        return paVariantDetail.size();
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
        } catch (SQLException ex) {
            Logger.getLogger(VehicleAddOns.class.getName()).log(Level.SEVERE, null, ex);
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
    
    private JSONObject checkExistingVehicleAddOns(){
        try {
            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE,
                                                    " a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.VOID)
                                                    + " AND a.cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.CANCELLED)
                                                    + " AND a.sCompnyID != " + SQLUtil.toSQL(psCompanyId)
                                                    + " AND a.sValidIDx != " + SQLUtil.toSQL(Master().getValidityId())
                                                    );
          lsSQL = lsSQL 
                    + " AND ((a.dFromDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dFromDate <= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")"
                    + " AND ((a.dThruDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR a.dThruDate IS NULL OR a.dThruDate >= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")";
      
            System.out.println("checkExistingVehicleAddOns SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(loRS.next()){    
                    poJSON.put("result", "error");
                    poJSON.put("message", "A Vehicle Add Ons already exists for the selected validity period.");
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
        
        poJSON = isEntryOkay(Master().getRecordStatus());
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = checkExistingVehicleAddOns();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        Iterator<Model> detail = Detail().iterator();
        int lnDetailRow = 1;
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sVrntIDxx");
            String lsDetailType = (String) item.getValue("sAddTypex");
            double ldblAmount = Double.parseDouble(String.valueOf(item.getValue("nAmountxx")));
            double ldblSRP = Double.parseDouble(String.valueOf(item.getValue("nSRPAmntx")));
            
            if (ldblSRP <= 0.00 || (lsDetailType == null || "".equals(lsDetailType)) || (lsDetail == null || "".equals(lsDetail))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    detail.remove(); // Correctly remove the item
                } 
            } else {
                if(ldblAmount <= 0.00){
                    poJSON = setJSON("error", "Amount cannot be zero at row "+lnDetailRow+".");
                    return poJSON;
                }
                lnDetailRow++;
            }
        }

        if (getDetailCount() <= 0) {
            poJSON = setJSON("error", "No record detail to be save.");
            return poJSON;
        }

        sortDetail();
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setValidityId(Master().getValidityId());
            if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                Detail(lnCtr).setAddOnId(Detail(lnCtr).getNextCode());
            }
            Detail(lnCtr).setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
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
                "  a.sModified, " +
                "  b.sAddTypex, " +
                "  c.sAddTypNm " +
                "FROM Validity_Period_Master a " +
                "INNER JOIN Vehicle_AddOn_Master b ON b.sValidIDx = a.sValidIDx" +
                "LEFT JOIN Vehicle_AddOn_Type c ON c.sAddTypex = b.sAddTypex"
                
                ;
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
//                + "/Reports/VehicleAddOn.jrxml";
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
//        parameters.put("sValidity",Master().getValidityId()+ "-"+ poGRider.getServerDate());
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
//        parameters.put("sValidityDesc",Master().getValidityDescription().toUpperCase());
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
//            } else {
//                lsContact = "Tel No : " + lsLandLine;
//            }
//        }
//        if(lsEmail == null) {
//            lsEmail = "";
//        } else {
//            if(!lsEmail.isEmpty()){
//                lsContact = lsContact + "\nEmail Address : " + lsEmail;
//            } else {
//                lsContact = "Email Address : " + lsEmail;
//            }
//        }
//
//        parameters.put("sBranch",lsBranchName.toUpperCase());
//        parameters.put("sBranchAddress",lsBranchAddress.toUpperCase());
//        parameters.put("sBranchDesc",lsBranchDesc.toUpperCase());
//        parameters.put("sContact",lsContact);
//        // Current DP rate
////        parameters.put("nDPRatePct",ldblDPRate);
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
//        // Display final report
//        // ---------------------------------------------
//
//        if (finalPrint != null) {
//
//            CustomJasperViewer viewer =
//                    new CustomJasperViewer(currentPrint);
//
//            viewer.setVisible(true);
//
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
//
//        poJSON.put("result", "error");
//        poJSON.put(
//                "message",
//                "Transaction print aborted!"
//        );
//
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
//                            "Vehicle Financing Promo Printed Successfully");
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
//    /**
//     * Sample data. Notice Civic RS has FOUR terms (36/48/60/72) while every
//     * other variant only has three - that's deliberate, to demonstrate that the
//     * crosstab prints exactly as many term columns as the data supports, with
//     * no template change required.
//     */
//    private List<Map<String, Object>> buildSampleData() {
//        List<Map<String, Object>> rows = new ArrayList<>();
//        sortDetail();
//        ArrayList<String> faArray = loadUniqueAddOnType();
//        try{
//            //Group by dp rate 
//            for(int lnCtr = 0;lnCtr < getVariantDetailCount();lnCtr++){
//                if(VariantDetail(lnCtr).getRecordStatus()){
//                    for(int lnRow = 0;lnRow < faArray.size();lnRow++){
//                        System.out.println("Add On Type : " + faArray.get(lnRow));
//                        System.out.println("Amount : " + getAmount(VariantDetail(lnRow).getVariantId(),faArray.get(lnRow)));
//                        String lsVariant = Detail(lnCtr).ModelVariant().getDescription();
//                        if(Detail(lnCtr).ModelVariant().Color().getDescription() != null && !"".equals(Detail(lnCtr).ModelVariant().Color().getDescription())){
//                            lsVariant = lsVariant + " " + Detail(lnCtr).ModelVariant().Color().getDescription(); 
//                        }
//                        addRow(rows
//                                , VariantDetail(lnCtr).ModelVariant().Model().Brand().getDescription()
//                                , VariantDetail(lnCtr).ModelVariant().Model().getDescription()
//                                , lsVariant
//                                , VariantDetail(lnCtr).getSRPAmount()
//                                , faArray.get(lnRow)
//                                , getAmount(VariantDetail(lnRow).getVariantId(),faArray.get(lnRow)));
//                    }
//                    
//                }
//            }
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(VehicleFinancingPrice.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return rows;
//    }
// 
//    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
//    private void addRow(List<Map<String, Object>> rows,
//                                String brand, String model, String variant,
//                                double srpAmt,
//                                String addonType,double amount) {
//        Map<String, Object> row = new LinkedHashMap<>();
//        if(brand == null){ brand = "";}
//        if(model == null){ model = "";}
//        if(variant == null){ variant = "";}
//        row.put("sBrand", brand.toUpperCase());
//        row.put("sModel", model.toUpperCase());
//        row.put("sVariant", variant.toUpperCase());
//        row.put("nSRP", srpAmt);
//        row.put("nTermMonths", addonType);
//        row.put("nMonthlyAmort", amount);
//        rows.add(row);
//    }
    
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
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sValidIDx = " + SQLUtil.toSQL(Master().getValidityId())) ;
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
//    
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
            showStatusHistoryUI("Vehicle Add On", (String) poMaster.getValue("sValidIDx"), entryBy, entryDate, crs);
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
