/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.script.ScriptException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
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
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.model.Model_Disbursement_Master;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.utility.CustomCommonUtil;
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
            
            String sql = "SELECT sVhclFIDx FROM " + this.poDetail.getTable() + " WHERE sValidIDx = " + SQLUtil.toSQL(transactionNo) + " ORDER BY sVhclFIDx";
            ResultSet rs = this.poGRider.executeQuery(sql);

            while(rs.next()) {
                Model loDetail = (Model)this.poDetail.clone();
                loDetail.newRecord();
//                this.poJSON = loDetail.openRecord(transactionNo, rs.getString("sVhclFIDx"));
                this.poJSON = loDetail.openRecord( rs.getString("sVhclFIDx"),transactionNo);
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
                                    ((Model)this.paDetail.get(lnCtr)).setValue("sVhclFIDx", Detail(lnCtr).getNextCode());
                                }
                                this.poJSON = ((Model)this.paDetail.get(lnCtr)).saveRecord();
                                if ("error".equals((String)this.poJSON.get("rsearesult"))) {
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
        if(pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.UPDATE){
            poJSON = new JSONObject();
            poJSON.put("result", "success");
            poJSON.put("message", "success");
            return poJSON;
        }
        
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
                       + " AND c.cRecdStat =  " + SQLUtil.toSQL(RecordStatus.ACTIVE)
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
        double ldblDownpaymentRate = 0.00;
        double ldblSelPrice= 0.00;
        while (loRS.next()) {
            lsVariantId = loRS.getString("sVrntIDxx");
            ldblSelPrice = loRS.getDouble("nSelPrice");
            if(lsVariantId != null && !"".equals(lsVariantId)){
                for(int lnCtr = 0; lnCtr < laStandardDownpaymentRate.size();lnCtr++){
                    lbExist = false;
                    ldblDownpaymentRate = laStandardDownpaymentRate.get(lnCtr);
                    for(int lnRow = 0; lnRow < getDetailCount(); lnRow++){
                        if(lsVariantId.equals(Detail(lnRow).getVariantId())
                            && ldblDownpaymentRate == Detail(lnRow).getDownPaymentRate()){
                            lbExist = true;
                            break;
                        }
                    }
                    
                    if(!lbExist){
                        Detail(getDetailCount() - 1).setVehicleFinancingId(Detail(getDetailCount() - 1).getNextCode());
                        Detail(getDetailCount() - 1).setVariantId(lsVariantId);
                        Detail(getDetailCount() - 1).setSRPAmount(ldblSelPrice);
                        Detail(getDetailCount() - 1).setDownPaymentRate(ldblDownpaymentRate);
                        ReloadDetail();
                        if(!pbWithUI){
                            break;
                        }
                    }
                }

                if(!pbWithUI){
                    break;
                }
            }
        }
        MiscUtil.close(loRS);

        sortDetail();
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    public void sortDetail(){
        //Sort paDetail by brand, model, variant;
        paDetail.sort(
            Comparator.comparing(
                o -> getBrandDescription((Model_Vehicle_Financing_Price) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ).thenComparing(
                o -> getModelDescription((Model_Vehicle_Financing_Price) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ).thenComparing(
                o -> getModelVariantDescription((Model_Vehicle_Financing_Price) o),
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ).thenComparing(
                o -> getDownpaymentRate((Model_Vehicle_Financing_Price) o),
                    Comparator.nullsLast(Comparator.naturalOrder())
                )
        );
    }
    
    private String getBrandDescription(Model_Vehicle_Financing_Price poDetail) {
        try {
            return poDetail.ModelVariant()
                    .Model()
                    .Brand()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }

    private String getModelDescription(Model_Vehicle_Financing_Price poDetail) {
        try {
            return poDetail.ModelVariant()
                    .Model()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }

    private String getModelVariantDescription(Model_Vehicle_Financing_Price poDetail) {
        try {
            return poDetail.ModelVariant()
                    .getDescription();
        } catch (SQLException | GuanzonException e) {
            return null;
        }
    }

    private Double getDownpaymentRate(Model_Vehicle_Financing_Price poDetail) {
        return poDetail.getDownPaymentRate();
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
            String lsSQL = getStandardRate(true);
            System.out.println("Downpayment Rate SQL: " + lsSQL);
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
    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
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
            String lsSQL = getStandardRate(false);
            System.out.println("Standard Rate SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while(loRS.next()){
                    loJSON = new JSONObject();
//                    Date loVFromDate = Master().getFromDate();
//                    Date loFromDate = loRS.getDate("dFromDate");
//                    if (loFromDate != null) {
//                        if (!"1900-01-01".equals(xsDateShort(loFromDate))) {
//                            LocalDate lldVFromDate = strToDate(xsDateShort(loVFromDate));
//                            LocalDate lldFromDate = strToDate(xsDateShort(loFromDate));
//                            if (lldVFromDate.isBefore(lldFromDate)) {
//                                loJSON.put("nDuration", loRS.getInt("nDuration"));
//                                loJSON.put("nRateValx", loRS.getDouble("nRateValx"));
//                                loJSONArray.add(loJSON);
//                            }
//                        }
//                    }


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
    
    private String getStandardRate(boolean isDownpaymentRate) throws SQLException{
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(new SalesModels(poGRider).VehicleFinancingRates()),
                                                    " cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                                    );
        if(isDownpaymentRate){
            lsSQL = lsSQL + " AND sRateType = " + SQLUtil.toSQL(FinancingRateStatus.StandardRateType.DOWNPAYMENT_RATE);
        } else {
            lsSQL = lsSQL + " AND sRateType = " + SQLUtil.toSQL(FinancingRateStatus.StandardRateType.INTEREST_RATE);
        }
                 
        Date loToDate = Master().getThruDate();
        Date loFromDate = Master().getFromDate();
        if(loToDate != null && !"1900-01-01".equals(xsDateShort(loToDate))){
            lsSQL = lsSQL 
                    + " AND ((dFromDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR dFromDate <= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")"
                    + " AND ((dThruDate between "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(Master().getThruDate())) +") OR dThruDate IS NULL OR dThruDate >= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")";
        } else {
            if(loFromDate != null && !"".equals(xsDateShort(loFromDate))){
                lsSQL = lsSQL 
                    + " AND ( dFromDate <= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")"
                    + " AND ( dThruDate IS NULL OR dThruDate >= "+ SQLUtil.toSQL(xsDateShort(Master().getFromDate()))+")";
            } else {
                Date ldServerDate = poGRider.getServerDate();
                Calendar locFromDate = Calendar.getInstance();
                locFromDate.setTime(ldServerDate);

                // Set to the last day of the current month
                locFromDate.set(Calendar.DAY_OF_MONTH,locFromDate.getActualMinimum(Calendar.DAY_OF_MONTH));
                lsSQL = lsSQL 
                    + " AND ( dFromDate <= "+ SQLUtil.toSQL(SQLUtil.toDate(xsDateShort(locFromDate.getTime()),SQLUtil.FORMAT_SHORT_DATE))+")"
                    + " AND ( dThruDate IS NULL OR dThruDate >= "+ SQLUtil.toSQL(SQLUtil.toDate(xsDateShort(locFromDate.getTime()),SQLUtil.FORMAT_SHORT_DATE))+")";
            }
        }

        lsSQL = lsSQL + " GROUP BY nDuration, nRateValx  ORDER BY nDuration, nRateValx ASC ";
        return lsSQL;
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
        fdblInterestRate = ((fdblInterestRate/100)+1);
        ldblMontlyAmortizationAmt = ((ldblBalance * fdblInterestRate) / fnDuration)+2;
        
        String lsDecimalFormat = "###0.00";
        DecimalFormat format = new DecimalFormat(lsDecimalFormat);
        ldblMontlyAmortizationAmt = Double.parseDouble(format.format(ldblMontlyAmortizationAmt));
        ldblMontlyAmortizationAmt = Double.parseDouble(String.valueOf(Math.round(ldblMontlyAmortizationAmt)));
        
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
            String lsRecStat = (String) item.getValue("cRecdStat");
            double ldblSRP = Double.parseDouble(String.valueOf(item.getValue("nSRPAmntx")));
            double ldblRSVAmt = Double.parseDouble(String.valueOf(item.getValue("nRsrvAmtx")));
            
            if ((ldblSRP == 0.0000 || (lsDetail == null || "".equals(lsDetail)))){
                if(item.getEditMode() == EditMode.ADDNEW){
                    detail.remove(); // Correctly remove the item
                } 
            } else {
//                if(ldblRSVAmt <= 0.00 && RecordStatus.ACTIVE.equals(lsRecStat)){
//                    poJSON = setJSON("error", "Reservation amount cannot be zero at row "+lnDetailRow+".");
//                    return poJSON;
//                }
                lnDetailRow++;
            }
        }

        if (getDetailCount() <= 0) {
            poJSON = setJSON("error", "No record detail to be save.");
            return poJSON;
        }

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            System.out.println("Record Status : " + Detail(lnCtr).getRecordStatus());
            if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                Detail(lnCtr).setVehicleFinancingId(Detail(lnCtr).getNextCode());
            }
            Detail(lnCtr).setValidityId(Master().getValidityId());
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
    
    public JSONObject getApprover() throws SQLException{
        JSONObject loJSON = new JSONObject();
        loJSON.put("sModified", "");
        loJSON.put("dModified", "");
        String lsSQL =   " SELECT "
                    + "     a.sModified"
                    + " ,   a.dModified"
                    + " FROM Transaction_Status_History a  "
                    + " WHERE a.sTableNme = " + SQLUtil.toSQL(Master().getTable())
                    + " AND a.sSourceNo = " + SQLUtil.toSQL(Master().getValidityId())
                    + " AND a.cRefrStat = " + SQLUtil.toSQL(ValidityPeriodStatus.APPROVED)
                    + " AND a.cTranStat = '1' ";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) >= 0) {
            while (loRS.next()) {
                // Get the LocalDateTime from your result set
                LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                loJSON.put("sModified", loRS.getString("sModified"));
                loJSON.put("dModified", dModified.format(formatter));
                loJSON.put("result", "success");
                return loJSON;
            }
            MiscUtil.close(loRS);
        }
        
        loJSON.put("result", "error");
        return loJSON;
    }
    
    public String getPreparedDate() throws SQLException, GuanzonException {
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(Master()), " sValidIDx =  " + SQLUtil.toSQL(Master().getValidityId())) ;
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                // Get the LocalDateTime from your result set
                LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                return dModified.format(formatter);
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
        } 
        return "";
    }
    
    
    /**
     * Prints disbursement vouchers for the provided transactions and marks first-time prints.
     *
     * @param fsSelectedDPRate
     * @return JSON result containing print status.
     * @throws CloneNotSupportedException If cloning operations fail.
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If transaction loading or validation fails.
     */
    public JSONObject printTransaction(String fsSelectedDPRate)
            throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        JasperReport jasperReport = null;
        pbIsPrinted = false;
        Double ldblSelectedDPRate = 0.00;
        if (fsSelectedDPRate == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid downpayment rate selected.");
            return poJSON;
        } else {
            if ("--All--".equalsIgnoreCase(fsSelectedDPRate.trim())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid downpayment rate selected.");
                ShowMessageFX.Warning(null, "Computerized Accounting System",
                        "Invalid downpayment rate selected.");
                return poJSON;
            }
        }

        JSONArray laStandardInterestRate = loadStandardInterestRates();
        if(laStandardInterestRate.size() <= 0){
            poJSON = setJSON("error", "No active standard interest rate.");
            return poJSON;
        }
 
        try {
            String jrxmlPath = System.getProperty("sys.default.path.config") + "/Reports/VehicleFinancingPromo_dynamic.jrxml";
            jasperReport = JasperCompileManager.compileReport(jrxmlPath);

            // 1. Prepare parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("sCompany", Master().Company().getCompanyName().toUpperCase()); 
            String lsAddress = Master().Company().getCompanyAddress() ;
            if(Master().Company().TownCity().getDescription() != null && !"".equals(Master().Company().TownCity().getDescription())){
                lsAddress = lsAddress + " " + Master().Company().TownCity().getDescription();
            }
            if(Master().Company().TownCity().Province().getDescription() != null && !"".equals(Master().Company().TownCity().Province().getDescription())){
                lsAddress = lsAddress  + ", " + Master().Company().TownCity().Province().getDescription();
            }
            
            parameters.put("sAddress", lsAddress.toUpperCase()); 
            parameters.put("sValidityDesc", Master().getValidityDescription().toUpperCase()); 
            parameters.put("nDPRatePct", ldblSelectedDPRate);
            List<Map<String, Object>> rows = buildSampleData(ldblSelectedDPRate,laStandardInterestRate);
            List<Map<String, ?>> data = new ArrayList<>(rows);
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(data);
            JasperPrint currentPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            if (currentPrint != null) {
                CustomJasperViewer viewer = new CustomJasperViewer(currentPrint);
                viewer.setVisible(true);
                viewer.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        proceedAfterViewerClosed();
                    }

                });
            }

        } catch (JRException | SQLException | GuanzonException  ex) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction print aborted!");
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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
                            "Transaction Printed Successfully");
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
    /**
     * Sample data. Notice Civic RS has FOUR terms (36/48/60/72) while every
     * other variant only has three - that's deliberate, to demonstrate that the
     * crosstab prints exactly as many term columns as the data supports, with
     * no template change required.
     */
    private List<Map<String, Object>> buildSampleData(Double fdblSelectedDPRate,JSONArray faStandardInterestRate) {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        sortDetail();
        try{
            //Group by dp rate 
            for(int lnCtr = 0;lnCtr < getDetailCount();lnCtr++){
                if(fdblSelectedDPRate.equals(Detail(lnCtr).getDownPaymentRate())){
                    for(int lnRow = 0;lnRow < faStandardInterestRate.size();lnRow++){
                        JSONObject loJSONObject = (JSONObject) faStandardInterestRate.get(lnRow);
                        int lnDuration = (int) loJSONObject.get("nDuration");
                        Double ldblRate = (Double) loJSONObject.get("nRateValx");
                        System.out.println("Duration : " + lnDuration);
                        System.out.println("Rate : " + ldblRate);
                        System.out.println("Montly Amortization Amount : " + getMontlyAmortizationAmount(lnCtr, lnDuration, ldblRate));

                        addRow(rows
                                , Detail(lnCtr).ModelVariant().Model().Brand().getDescription()
                                , Detail(lnCtr).ModelVariant().Model().getDescription()
                                , Detail(lnCtr).ModelVariant().getDescription()
                                , Detail(lnCtr).getSRPAmount()
                                , Detail(lnCtr).getReservationAmount()
                                , lnDuration
                                , ldblRate
                                , getMontlyAmortizationAmount(lnCtr, lnDuration, ldblRate));
                    }
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(VehicleFinancingPrice.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
 
    // Keys here MUST match the <field name="..."> values in the .jrxml exactly.
    private void addRow(List<Map<String, Object>> rows,
                                String brand, String model, String variant,
                                double srpAmt,
                                double cashOutDP, int termMonths,
                                double ratePct, double monthlyAmort) {
        Map<String, Object> row = new LinkedHashMap<>();
        if(brand == null){ brand = "";}
        if(model == null){ model = "";}
        if(variant == null){ variant = "";}
        row.put("sBrand", brand.toUpperCase());
        row.put("sModel", model.toUpperCase());
        row.put("sVariant", variant.toUpperCase());
        row.put("nSRPAmt", srpAmt);
        row.put("nCashOutDP", cashOutDP);
        row.put("nTermMonths", termMonths);
        row.put("nRatePct", ratePct);
        row.put("nMonthlyAmort", monthlyAmort);
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
