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
   
    private String psCompanyId = "";
     @Override
    public void initialize() throws SQLException, GuanzonException {
      psRecdStat = FinancingRateStatus.OPEN;
      poModel = new SalesModels(poGRider).FinancingRateMaster();
      paModel = new ArrayList<>();
      paStandardRate = new ArrayList<>();
      super.initialize();
    }
    
    public void setCompanyId(String companyId) { 
        psCompanyId = companyId; 
        getModel().setCompanyId(companyId);
    }
    
    public String getStatus(String lsStatus) {
        switch (lsStatus) {
            case FinancingRateStatus.OPEN:
                return "Open";
            case FinancingRateStatus.ACTIVE:
                return "Active";
            case FinancingRateStatus.DEACTIVATE:
                return "Inactive";
            case FinancingRateStatus.VOID:
                return "Void";
            default:
                return "Unknown";
        }
    }
    

    @Override
    public Model_Financing_Rate_Master getModel() {
        return poModel;
    }
    
    public Model_Financing_Rate_Master RecordList(int row) {
        return (Model_Financing_Rate_Master) paModel.get(row);
    }
    
    public int getRecordListCount() {
        return this.paModel.size();
    }
    
    public Model_Vehicle_Financing_Rates StandardRateList(int row) {
        return (Model_Vehicle_Financing_Rates) paStandardRate.get(row);
    }
    
    public int getStandardRateListCount() {
        return this.paStandardRate.size();
    }
    
    public JSONObject ActivateRecord(String remarks)
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        
        poJSON = activateRecord();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        poJSON.put("result", "success");
        poJSON.put("message", "Record activated successfully.");
        return poJSON;
    }
    
    public JSONObject DeactivateRecord(String remarks)
              throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = FinancingRateStatus.DEACTIVATE;

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

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sRateIDxx"), remarks, lsStatus, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        poJSON = setJSON("success", "Record deactivated successfully.");
        return poJSON;
    }
    
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

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sRateIDxx"), remarks, lsStatus, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        poJSON = setJSON("success", "Record voided successfully.");
        return poJSON;
    }
  
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
    
    public JSONObject SearchBank(String value, boolean byCode, boolean isSearch)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(isSearch){
            poJSON = loadRecord(value);
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        } else {
            Banks object = new ParamControllers(poGRider, logwrapr).Banks();
            object.setRecordStatus(RecordStatus.ACTIVE);
            if(pbWithUI){
                poJSON = object.searchRecord(value, byCode);
            } else {
                object.openRecord(value);
            }
            if (isJSONSuccess(poJSON)) {
                poJSON = checkExistingBank(object.getModel().getBankID());
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                getModel().setBankId(object.getModel().getBankID());
            }

            System.out.println("Bank Name : " + getModel().Bank().getBankName());
        }
        return poJSON;
    }
    
    /**
     * Check Existing Bank
     * @param bankId
     * @param row
     * @return JSONObject success or error
     */
    private JSONObject checkExistingBank(String bankId){
        poJSON = new JSONObject();
        boolean lbExist = false;
        try {
            String lsSQL = MiscUtil.addCondition(getSQ_Browse(),
                    " a.sBankIDxx = " + SQLUtil.toSQL(bankId)
                    + " AND a.cRecdStat = " + SQLUtil.toSQL(FinancingRateStatus.VOID)
                    );
            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();
            if (MiscUtil.RecordCount(loRS) >= 0) {
                lbExist = loRS.next();
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            poJSON = setJSON("error", e.getMessage());
            return poJSON;
        }
        
        if(lbExist){
            poJSON = setJSON("error", "Found existing financing rate for the selected bank.");
            return poJSON;
        }
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
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
                case FinancingRateStatus.DEACTIVATE:
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
                        case FinancingRateStatus.DEACTIVATE:
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

