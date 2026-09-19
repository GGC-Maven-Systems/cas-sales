/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
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
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.Model_Validity_Period_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Vehicle_Financing_Price;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;
import ph.com.guanzongroup.cas.sales.validator.ValidityMasterValidator;

/**
 * Manages vehicle financing price transactions including approval, voiding, and cancellation.
 * Handles master and detail records for vehicle financing pricing with validity period management.
 *
 * @author Arsiela 09182026
 */
public class VehicleFinancingPrice extends Transaction {
    /** Company ID for filtering and transaction context */
    public String psCompanyId = "";
    /** User ID of the approving officer */
    public String psApprover = "";

    /** List of master records for batch operations */
    public List<Model> paMaster;
    
    /**
     * Initializes the transaction with source code and loads required models.
     * Sets up master validity period and vehicle financing price details.
     *
     * @return JSONObject containing initialization result with "result" and "message" fields
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     */
    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "Vhpm";

        poMaster = new SalesModels(poGRider).ValidityPeriodMaster();
        poDetail = new SalesModels(poGRider).VehicleFinancingPrice();

        paMaster = new ArrayList<Model>();
        psApprover = "";
        setApproving("");
        return initialize();
    }

    /**
     * Returns the transaction source code.
     *
     * @return the source code for this transaction ("Vhpm" for Vehicle Financing Price)
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
        return openTransaction(transactionNo);
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
            
            String sql = "SELECT sVhclFIDx FROM " + this.poDetail.getTable() + " WHERE sValidIDx = " + SQLUtil.toSQL(transactionNo) + " ORDER BY sVhclFIDx";
            ResultSet rs = this.poGRider.executeQuery(sql);

            while(rs.next()) {
                Model loDetail = (Model)this.poDetail.clone();
                loDetail.newRecord();
                this.poJSON = loDetail.openRecord(transactionNo, rs.getString("sVhclFIDx"));
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
        return updateTransaction();
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
                    if (this.pbVerifyEntryNo) {
                        this.poMaster.setValue("nEntryNox", this.paDetail.size());
                    }

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
    
    /**
     * Populates detail rows with available vehicle variants and financing rates.
     * Loads standard interest and downpayment rates, then creates detail entries
     * for each vehicle variant with applicable rates.
     *
     * @return JSONObject with "result" and optional error "message"
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws CloneNotSupportedException if detail cloning fails
     */
    public JSONObject populateVehicleList() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
        paDetail = new ArrayList<>();
        JSONArray laStandardInterestRate = loadStandardInterestRates();
        ArrayList<Double> laStandardDownpaymentRate = loadStandardDownpaymentRates();
        
        if(laStandardInterestRate.size() <= 0){
            poJSON = setJSON("error", "No active standard interest rate.");
            return poJSON;
        }
        
        if(laStandardDownpaymentRate.size() <= 0){
            poJSON = setJSON("error", "No active standard downpayment rate.");
            return poJSON;
        }
        
        String lsSQL = " SELECT   " 
                       + " c.sVrntIDxx " 
                       + " , a.sIndstCdx " 
                       + " , c.nSelPrice " 
                       + " FROM Brand a " 
                       + " LEFT JOIN Model b On b.sBrandIDx = a.sBrandIDx " 
                       + " LEFT JOIN Model_Variant c ON c.sModelIDx = b.sModelIDx " ;
        lsSQL = MiscUtil.addCondition(lsSQL," a.sIndstCdx =  " + SQLUtil.toSQL(poGRider.getIndustry())
                       + " AND c.cEndOfLfe =  " + SQLUtil.toSQL(Logical.YES)
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
        
        while (loRS.next()) {
            for(int lnCtr = 0; lnCtr < laStandardDownpaymentRate.size();lnCtr++){
                Detail(getDetailCount() - 1).setVariantId(loRS.getString("sVrntIDxx"));
                Detail(getDetailCount() - 1).setSRPAmount(loRS.getDouble("nSelPrice"));
                Detail(getDetailCount() - 1).setDownPaymentRate(laStandardDownpaymentRate.get(lnCtr));
                ReloadDetail();
                if(!pbWithUI){
                    break;
                }
            }
            
            if(!pbWithUI){
                break;
            }
        }
        MiscUtil.close(loRS);
        return poJSON;
    }
    
    /**
     * Refines and validates the transaction detail list.
     * 
     * This method prunes invalid rows (those with empty particulars or zero amounts for new records) 
     * and automatically appends a new detail row if the list is empty or the last entry is valid.
     * 
     * @throws CloneNotSupportedException If an error occurs while adding a new detail row.
     */
    public void ReloadDetail() throws CloneNotSupportedException{
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            if ((Detail(lnCtr).getVariantId() == null || "".equals(Detail(lnCtr).getVariantId()))) {
                deleteDetail(lnCtr);
            } 
            lnCtr--;
        }
            
        if ((getDetailCount() - 1) >= 0) {
            if (
                (Detail(getDetailCount() - 1).getVariantId() != null && !"".equals(Detail(getDetailCount() - 1).getVariantId()))
                && Detail(getDetailCount() - 1).getSRPAmount() > 0.0000
                && Detail(getDetailCount() - 1).getDownPaymentRate() > 0.00
                ) {
                AddDetail();
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
    }
    
    /**
     * Loads all active standard vehicle financing rates.
     *
     * @return JSON result of the load operation
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if record loading fails
     */
    public ArrayList loadStandardDownpaymentRates() throws SQLException, GuanzonException {
        ArrayList<Double> laStandardRate = new ArrayList<>();
        try {
            String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(new SalesModels(poGRider).VehicleFinancingRates()),
                                                    " cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                                    + " AND sRateType = " + SQLUtil.toSQL(FinancingRateStatus.StandardRateType.DOWNPAYMENT_RATE)
                                                    + " AND " +  SQLUtil.toSQL(xsDateShort(Master().getFromDate()))
                                                    + " BETWEEN dFromDate AND dThruDate "
                                                    );
            
            Date loToDate = Master().getThruDate();
            if(loToDate != null && !"1900-01-01".equals(xsDateShort(loToDate))){
                lsSQL = lsSQL + " AND " +  SQLUtil.toSQL(xsDateShort(Master().getThruDate()))
                        + "  BETWEEN dFromDate AND dThruDate ";
            } else {
                lsSQL = lsSQL + " AND ( " +  SQLUtil.toSQL(xsDateShort(poGRider.getServerDate()))
                        + "  BETWEEN dFromDate AND dThruDate "
                        + " OR dThruDate IS NULL)";
            }
            
            lsSQL = lsSQL + " ORDER BY nRateValx ASC ";
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while(loRS.next()){
                    laStandardRate.add(loRS.getDouble("nRateValx"));
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        
        return laStandardRate;
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
     * Loads all active standard vehicle financing rates.
     *
     * @return JSON result of the load operation
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if record loading fails
     */
    public JSONArray loadStandardInterestRates() throws SQLException, GuanzonException {
        JSONArray loJSONArray = new JSONArray();
        JSONObject loJSON = new JSONObject();
        try {
            String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(new SalesModels(poGRider).VehicleFinancingRates()),
                                                    " cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                                    + " AND sRateType = " + SQLUtil.toSQL(FinancingRateStatus.StandardRateType.INTEREST_RATE)
                                                    + " AND " +  SQLUtil.toSQL(xsDateShort(Master().getFromDate()))
                                                    + " BETWEEN dFromDate AND dThruDate "
                                                    );
            
            Date loToDate = Master().getThruDate();
            if(loToDate != null && !"1900-01-01".equals(xsDateShort(loToDate))){
                lsSQL = lsSQL + " AND " +  SQLUtil.toSQL(xsDateShort(Master().getThruDate()))
                        + "  BETWEEN dFromDate AND dThruDate ";
            } else {
                lsSQL = lsSQL + " AND ( " +  SQLUtil.toSQL(xsDateShort(poGRider.getServerDate()))
                        + "  BETWEEN dFromDate AND dThruDate "
                        + " OR dThruDate IS NULL)";
            }
            
            lsSQL = lsSQL + " ORDER BY nDuration, nRateValx ASC ";
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while(loRS.next()){
                    loJSON = new JSONObject();
                    loJSON.put("nDuration", loRS.getInt("nDuration"));
                    loJSON.put("nRateValx", loRS.getDouble("nRateValx"));
                    loJSONArray.add(loJSON);
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        
        return loJSONArray;
    }
    
    /**
     * Calculates the monthly amortization amount based on vehicle financing details.
     * Formula: (SRP - DownPayment) × (InterestRate / 100) / Duration
     *
     * @param fnRow the detail row index
     * @param fnDuration the loan duration in months
     * @param fdblInterestRate the annual interest rate percentage
     * @return calculated monthly amortization amount formatted to 2 decimal places
     */
    public Double getMontlyAmortizationAmount(int fnRow, int fnDuration, Double fdblInterestRate) {
        Double ldblDownpaymentAmount = 0.00;
        Double ldblBalance = 0.00;
        Double ldblMontlyAmortizationAmt = 0.00;
        /**
         * SRP x Downpayment Rate = Downpayment Amount
         * SRP - Downpayment Amount = Balance
         * Balance x Interest Rate / Duration = Monthly Amortization Amount
         */
        ldblDownpaymentAmount = Detail(fnRow).getSRPAmount() * (Detail(fnRow).getDownPaymentRate()/100);
        ldblBalance = Detail(fnRow).getSRPAmount() - ldblDownpaymentAmount;
        ldblMontlyAmortizationAmt = (ldblBalance * (fdblInterestRate / 100)) / fnDuration;
        
        String lsDecimalFormat = "###0.00";
        DecimalFormat format = new DecimalFormat(lsDecimalFormat);
        ldblMontlyAmortizationAmt = Double.parseDouble(format.format(ldblMontlyAmortizationAmt));
         
        return ldblMontlyAmortizationAmt;
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
     * Gets a detail record by row index as Model_Vehicle_Financing_Price.
     *
     * @param row the index of the detail record to retrieve
     * @return the detail record at the specified row cast to Model_Vehicle_Financing_Price
     */
    @Override
    public Model_Vehicle_Financing_Price Detail(int row) {
        return (Model_Vehicle_Financing_Price) paDetail.get(row); 
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
            Master().setFromDate(SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            
        } catch (SQLException ex) {
            Logger.getLogger(VehicleFinancingPrice.class.getName()).log(Level.SEVERE, null, ex);
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
        
        Iterator<Model> detail = Detail().iterator();
        int lnDetailRow = 1;
        while (detail.hasNext()) {
            Model item = detail.next(); // Store the item before checking conditions
            String lsDetail = (String) item.getValue("sVrntIDxx");
            double ldblSRP = Double.parseDouble(String.valueOf(item.getValue("nSRPAmntx")));
            double ldblRSVAmt = Double.parseDouble(String.valueOf(item.getValue("nRsrvAmtx")));
            
            if ((ldblSRP == 0.0000 || (lsDetail == null || "".equals(lsDetail)))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    detail.remove(); // Correctly remove the item
                } 
            } else {
                if(ldblRSVAmt <= 0.00){
                    poJSON = setJSON("error", "Reservation amount cannot be zero at row "+lnDetailRow+".");
                    return poJSON;
                }
                lnDetailRow++;
            }
        }

        if (getDetailCount() <= 0) {
            poJSON = setJSON("error", "No record detail to be save.");
            return poJSON;
        }

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setValidityId(Master().getValidityId());
            if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                Detail(lnCtr).setVehicleFinancingId(Detail(lnCtr).getNextCode());
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
                "  a.sModified " +
                "FROM Validity_Period_Master a " +
                "INNER JOIN Vehicle_Financing_Price b ON b.sValidIDx = a.sValidIDx";
        if(lsCondition != null && !"".equals(lsCondition)){
            SQL_BROWSE = MiscUtil.addCondition(SQL_BROWSE, lsCondition);
        }
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
            showStatusHistoryUI("Vehicle Financing Promo", (String) poMaster.getValue("sValidIDx"), entryBy, entryDate, crs);
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
