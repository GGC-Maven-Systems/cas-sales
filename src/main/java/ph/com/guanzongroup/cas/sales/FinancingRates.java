/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.Model_Financing_Rate_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Vehicle_Financing_Rates;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;

/**
 *
 * @author Arsiela
 */
public class FinancingRates extends Parameter {
    Model_Financing_Rate_Master poModel;
    ArrayList<Model_Financing_Rate_Master> paModel;
    ArrayList<Model_Vehicle_Financing_Rates> paStandardRate;
   
    private String psApprover = "";
    private String psCompanyId = "";
    private String psBank = "";

    /**
     * Initializes the financing rate controller and its default models.
     *
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application initialization fails
     */
     @Override
    public void initialize() throws SQLException, GuanzonException {
      psRecdStat = FinancingRateStatus.OPEN;
      poModel = new SalesModels(poGRider).FinancingRateMaster();
      paModel = new ArrayList<>();
      paStandardRate = new ArrayList<>();
      psBank = "";
      super.initialize();
    }
    
    /**
     * Sets the company id for the current financing rate record.
     *
     * @param companyId company identifier to assign
     */
    public void setCompanyId(String companyId) {
        psCompanyId = companyId; 
        getModel().setCompanyId(companyId);
    }
    public void setSearchBank(String bank) { psBank = bank; }
    public String getSearchBank() { return psBank; }
    
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
//            setApproving(lsUserIDxx);
            psApprover = lsUserIDxx;
        }   
        
        poJSON = setJSON("success","success");
        return poJSON;
    }
    
    /**
     * Converts a financing status code into its display label.
     *
     * @param lsStatus status code to evaluate
     * @return readable status text
     */
    public String getStatus(String lsStatus) {
        switch (lsStatus) {
            case FinancingRateStatus.OPEN:
                return "Open";
            case FinancingRateStatus.ACTIVE:
                return "Active";
            case FinancingRateStatus.INACTIVE:
                return "Inactive";
            case FinancingRateStatus.VOID:
                return "Void";
            default:
                return "Unknown";
        }
    }
    

    /**
     * Returns the active financing rate master model.
     *
     * @return current financing rate model
     */
    @Override
    public Model_Financing_Rate_Master getModel() {
        return poModel;
    }
    
    /**
     * Gets a financing rate record from the loaded record list.
     *
     * @param row zero-based row index
     * @return financing rate record at the given index
     */
    public Model_Financing_Rate_Master RecordList(int row) {
        return (Model_Financing_Rate_Master) paModel.get(row);
    }
    
    /**
     * Returns the number of loaded financing rate records.
     *
     * @return record list size
     */
    public int getRecordListCount() {
        return this.paModel.size();
    }
    
    /**
     * Gets a standard financing rate entry from the loaded list.
     *
     * @param row zero-based row index
     * @return standard financing rate at the given index
     */
    public Model_Vehicle_Financing_Rates StandardRateList(int row) {
        return (Model_Vehicle_Financing_Rates) paStandardRate.get(row);
    }
    
    /**
     * Returns the number of loaded standard financing rates.
     *
     * @return standard rate list size
     */
    public int getStandardRateListCount() {
        return this.paStandardRate.size();
    }
    
    /**
     * Activates the currently loaded financing rate record.
     *
     * @param remarks remarks to save with the status change
     * @return JSON result of the activation request
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the activation fails
     * @throws CloneNotSupportedException if model cloning is not supported
     */
    public JSONObject ActivateRecord(String remarks)
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        
        String lsStatus = FinancingRateStatus.ACTIVE;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error",  "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cRecdStat"))) {
            poJSON = setJSON("error", "Record was already active.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sRateIDxx"), remarks, lsStatus, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        poJSON.put("result", "success");
        poJSON.put("message", "Record activated successfully.");
        return poJSON;
    }
    
    /**
     * Deactivates the currently loaded financing rate record.
     *
     * @param remarks remarks to save with the status change
     * @return JSON result of the deactivation request
     * @throws ParseException if status history parsing fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the deactivation fails
     * @throws CloneNotSupportedException if model cloning is not supported
     */
    public JSONObject DeactivateRecord(String remarks)
              throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = FinancingRateStatus.INACTIVE;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error",  "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cRecdStat"))) {
            poJSON = setJSON("error", "Record was already deactivated.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
        if (!"success".equals((String) poJSON.get("result"))) {
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
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sRateIDxx"), remarks, lsStatus, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        poJSON = setJSON("success", "Record deactivated successfully.");
        return poJSON;
    }
    
    /**
     * Voids the currently loaded financing rate record.
     *
     * @param remarks remarks to save with the status change
     * @return JSON result of the void request
     * @throws ParseException if status history parsing fails
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the void operation fails
     * @throws CloneNotSupportedException if model cloning is not supported
     */
    public JSONObject VoidRecord(String remarks)
              throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = FinancingRateStatus.VOID;

        if (getEditMode() != EditMode.READY) {
            poJSON = setJSON("error",  "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poModel.getValue("cRecdStat"))) {
            poJSON = setJSON("error", "Record was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay();
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
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sRateIDxx"), remarks, lsStatus, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        poJSON = setJSON("success", "Record voided successfully.");
        return poJSON;
    }
  
    /**
     * Validates the current financing rate entry before saving or updating.
     *
     * @return JSON result indicating whether the entry is valid
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if validation cannot be completed
     */
    @Override
    public JSONObject isEntryOkay() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        
        if (poModel.getRateId() == null || "".equals(poModel.getRateId())) {
            poJSON = setJSON("error", "Rate Id must not be empty.");
            return poJSON;
        }
        
        if (poModel.getBankId() == null || "".equals(poModel.getBankId())) {
            poJSON = setJSON("error", "Bank Id must not be empty.");
            return poJSON;
        } 
        
        if (poModel.getDuration() <= 0) {
            poJSON = setJSON("error", "Invalid duration value.");
            return poJSON;
        } 
        
        if (poModel.getRate() <= 0.00) {
            poJSON = setJSON("error", "Invalid rate value.");
            return poJSON;
        } 
        
        if (poModel.getDIRate()<= 0.00) {
            poJSON = setJSON("error", "Invalid DI rate value.");
            return poJSON;
        } 

        if(getEditMode() == EditMode.ADDNEW){
            poJSON = checkExistingFinancingRate();
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }
        }
        poModel.setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
     * Checks whether a financing rate already exists for the selected bank.
     *
     * @return JSON result indicating whether a duplicate record was found
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if lookup processing fails
     */
    private JSONObject checkExistingFinancingRate() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), " a.sBankIDxx = " +  SQLUtil.toSQL(poModel.getBankId()));
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) >= 0) {
            if (loRS.next()) {
                // Print the result set
                System.out.println("sRateIDxx: " + loRS.getString("sRateIDxx"));
                System.out.println("sBankIDxx: " + loRS.getString("sBankIDxx"));
                System.out.println("------------------------------------------------------------------------------");

                poJSON = setJSON("error", poModel.Bank().getBankName() + " financing rate already exists."
                        + "\nRate ID: " + loRS.getString("sRateIDxx"));
                MiscUtil.close(loRS);
                return poJSON;
            }
        }
        MiscUtil.close(loRS);
        
        poJSON = setJSON("success", "success.");
        poJSON.put("continue", true);
        return poJSON;
    
    }
    
    /**
     * Searches for a bank or loads financing records filtered by bank name.
     *
     * @param value search text or bank identifier
     * @param byCode true to search by code, false to search by description
     * @param isSearch true to load financing records, false to search banks
     * @return JSON result of the search operation
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the search operation fails
     */
    public JSONObject SearchBank(String value, boolean byCode, boolean isSearch)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus(RecordStatus.ACTIVE);
        if(pbWithUI){
            poJSON = object.searchRecord(value, byCode);
        } else {
            object.openRecord(value);
        }
        if (isJSONSuccess(poJSON)) {
            if(isSearch){
                setSearchBank(object.getModel().getBankName());
            } else {
                poJSON = checkExistingBank(object.getModel().getBankID());
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                getModel().setBankId(object.getModel().getBankID());
            }
        }

        System.out.println("Bank Name : " + getModel().Bank().getBankName());
        return poJSON;
    }
    
    /**
     * Checks whether the selected bank already has a conflicting rate record.
     *
     * @param bankId bank identifier to validate
     * @return JSON result indicating whether the bank can be used
     */
    private JSONObject checkExistingBank(String bankId){
        poJSON = new JSONObject();
        boolean lbExist = false;
        String lsRateId = "";
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                    " a.sBankIDxx = " + SQLUtil.toSQL(bankId)
                    + " AND a.cRecdStat != " + SQLUtil.toSQL(FinancingRateStatus.VOID)
                    );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                if(loRS.next()){
                    lbExist = true;
                    lsRateId = loRS.getString("sRateIDxx");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            poJSON = setJSON("error", e.getMessage());
            return poJSON;
        }
        
        if(lbExist){
            poJSON = setJSON("error", "Found existing financing rate for the selected bank."
                    + "\n Rate ID : " + lsRateId);
            return poJSON;
        }
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
     * Loads financing rate records that match the provided bank name.
     *
     * @param fsBankName bank name filter
     * @return JSON result of the load operation
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if record loading fails
     */
    public JSONObject loadRecord(String fsBankName) throws SQLException, GuanzonException {
        paModel = new ArrayList<>();
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                    " b.sBankName LIKE " + SQLUtil.toSQL("%"+fsBankName+"%")
                    + " AND a.cRecdStat != " + SQLUtil.toSQL(FinancingRateStatus.VOID)
                    );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while(loRS.next()){
                    paModel.add(new SalesModels(poGRider).FinancingRateMaster());
                    paModel.get(paModel.size()-1).openRecord(loRS.getString("sRateIDxx"));
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            poJSON = setJSON("error", e.getMessage());
        }
        
        return poJSON;
    }
    
    /**
     * Loads all active standard vehicle financing rates.
     *
     * @return JSON result of the load operation
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if record loading fails
     */
    public JSONObject loadStandardRates() throws SQLException, GuanzonException {
        paStandardRate = new ArrayList<>();
        try {
            String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(new SalesModels(poGRider).VehicleFinancingRates()),
                                                    " cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                                    );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                while(loRS.next()){
                    paStandardRate.add(new SalesModels(poGRider).VehicleFinancingRates());
                    paStandardRate.get(paStandardRate.size()-1).openRecord(loRS.getString("sStdRteID"));
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            poJSON = setJSON("error", e.getMessage());
        }
        
        return poJSON;
    }
    
    /**
     * Searches for a financing rate record using the configured record status.
     *
     * @param value search text or code
     * @param byCode true to search by code, false to search by description
     * @return JSON result of the record search
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the search operation fails
     */
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsCondition = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsCondition = " a.cRecdStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = " a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }
        
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsCondition);

        System.out.println("Executing SQL: " + lsSQL);
        if(pbWithUI){
            poJSON = ShowDialogFX.Browse(poGRider,
                    lsSQL,
                    value,
                    "Rate Id»Bank»Duration»Interest Rate»DI Rate»SI Rate",
                    "sRateIDxx»sBankName»nDuration»nRateValx»nDIRatexx»nSIRatexx",
                    "a.sRateIDxx»b.sBankName»a.nDuration»a.nRateValx»a.nDIRatexx»a.nSIRatexx",
                    byCode ? 0 : 1);
        } else {
            try {
                ResultSet loRS = poGRider.executeQuery(lsSQL);
                poJSON = new JSONObject();
                if (MiscUtil.RecordCount(loRS) >= 0) {
                    if(loRS.next()){
                        poJSON.put("sRateIDxx", loRS.getString("sRateIDxx"));
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("ERROR: " + e.getMessage());
                poJSON = setJSON("error", e.getMessage());
            }
        
        }

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sRateIDxx"));
        } else {
            poJSON = setJSON("error", "No record loaded.");
            return poJSON;
        }
    }
    
    @Override
    public JSONObject willSave(){
        if(FinancingRateStatus.ACTIVE.equals(getModel().getRecordStatus())){
            if(!pbWthParent){
                psApprover = poGRider.getUserID();
                poJSON = callApproval();
                if (!isJSONSuccess(poJSON)) {
                    return poJSON;
                }
            }
        }
    
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
     * Builds the browse query for financing rate records.
     *
     * @return SQL browse statement
     */
    @Override
    public String getSQ_Browse() {
        return  "SELECT " +
            "  a.sRateIDxx " +
            ", a.sBankIDxx " +
            ", a.nDuration " +
            ", a.nRateValx " +
            ", a.nDIRatexx " +
            ", a.nSIRatexx " +
            ", a.cRecdStat " +
            ", a.sModified " +
            ", a.dModified " +
            ", b.sBankName " +
            "FROM Financing_Rate_Master a " +
            "LEFT JOIN Banks b ON b.sBankIDxx = a.sBankIDxx ";
        
    }
    
    /**
     * Loads the status history of the current record into a cached row set.
     *
     * @return cached row set containing status history rows
     * @throws SQLException if a database error occurs
     */
    protected CachedRowSet getStatusHistoryTest() throws SQLException {
        String lsSQL = "SELECT  a.sTableNme, a.sSourceNo, a.sRemarksx, a.cRefrStat cTranStat, IFNULL(c.sCompnyNm, '-') xModified, IFNULL(e.sCompnyNm, '-') xApproved, a.dModified, a.dApproved, a.sModified, a.sApproved " +
                    " FROM Parameter_Status_History a " +
                    "LEFT JOIN xxxSysUser b ON b.sUserIDxx = a.sModified " +
                    "LEFT JOIN Client_Master c ON b.sEmployNo = c.sClientID " +
                    "LEFT JOIN xxxSysUser d ON d.sUserIDxx = a.sApproved " +
                    "LEFT JOIN Client_Master e ON d.sEmployNo = e.sClientID " +
                    " WHERE a.sSourceNo = " + SQLUtil.toSQL(getModel().getRateId()) +
                    " AND a.sTableNme = " + SQLUtil.toSQL(getModel().getTable()) + " ORDER BY a.dModified";
        System.out.println("STATUS HISTORY : " + lsSQL);
        ResultSet loRS = this.poGRider.executeQuery(lsSQL);
        RowSetFactory factory = RowSetProvider.newFactory();
        CachedRowSet rowset = factory.createCachedRowSet();
        rowset.populate(loRS);
        MiscUtil.close(loRS);
        return rowset;
    }

    /**
     * Displays the status history of a Cash Advance transaction.
     *
     * Retrieves status records, maps status codes to readable text, and
     * shows them in the UI along with entry details.
     *
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
     * @throws Exception for other unexpected errors
     */
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception {
        CachedRowSet crs;
        if(pbWithUI){
            crs = getStatusHistory();
        } else {
            crs = getStatusHistoryTest();
        }

        crs.beforeFirst();

        while(crs.next()){
            switch (crs.getString("cRefrStat")){
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case FinancingRateStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case FinancingRateStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                case FinancingRateStatus.ACTIVE:
                    crs.updateString("cRefrStat", "ACTIVE");
                    break;
                case FinancingRateStatus.INACTIVE:
                    crs.updateString("cRefrStat", "INACTIVE");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat){
                        case FinancingRateStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case FinancingRateStatus.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                        case FinancingRateStatus.ACTIVE:
                            crs.updateString("cRefrStat", "ACTIVE");
                            break;
                        case FinancingRateStatus.INACTIVE:
                            crs.updateString("cRefrStat", "INACTIVE");
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
            showStatusHistoryUI("Bank Financing Rate", (String) getModel().getValue("sRateIDxx"), entryBy, entryDate, crs);
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
                + " FROM "+getModel().getTable()+" a "
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sRateIDxx AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(getModel().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sRateIDxx =  " + SQLUtil.toSQL(getModel().getRateId()));
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
     * Retrieves the company name of a system user based on user ID.
     *
     * @param fsId User ID to lookup
     * @return Company name of the user
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if application-specific error occurs
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
}

