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
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.Model_Vehicle_Financing_Rates;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;

/**
 *
 * @author Arsiela 09172026
 */
public class StandardFinancingRates extends Parameter {
    Model_Vehicle_Financing_Rates poModel;
    ArrayList<Model_Vehicle_Financing_Rates> paModel;
   
    private String psCompanyId = "";
    private String psApprover = "";

    /**
     * Initializes the standard financing rate controller and model.
     *
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if initialization fails
     */
     @Override
    public void initialize() throws SQLException, GuanzonException {
      psRecdStat = FinancingRateStatus.OPEN;
      poModel = new SalesModels(poGRider).VehicleFinancingRates();
      paModel = new ArrayList<>();
      super.initialize();
    }
    
    /**
     * Sets the company id used by this controller.
     *
     * @param companyId company identifier to assign
     */
    public void setCompanyId(String companyId) {
        psCompanyId = companyId; 
    }

    /**
     * Returns the current standard financing rate model.
     *
     * @return active vehicle financing rate model
     */
    @Override
    public Model_Vehicle_Financing_Rates getModel() {
        return poModel;
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
//            setApproving(lsUserIDxx);
            psApprover = lsUserIDxx;
        }   
        
        poJSON = setJSON("success","success");
        return poJSON;
    }
    
    public JSONObject NewRecord() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        poJSON = newRecord();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
         
        getModel().setFromDate(SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
        
        poJSON = setJSON("success","success");
        return poJSON;
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
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sStdRteID"), remarks, lsStatus, false);
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
        
        poGRider.beginTrans("UPDATE STATUS", "DeactivateRecord", SOURCE_CODE, getModel().getStandardRateId());
       
        Date loToDate = getModel().getThruDate();
        if(loToDate == null || "1900-01-01".equals(xsDateShort(loToDate))){
            Model_Vehicle_Financing_Rates loObject = new SalesModels(poGRider).VehicleFinancingRates();
            loObject.initialize();
            poJSON = loObject.openRecord(getModel().getStandardRateId());
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
            
            poJSON = loObject.updateRecord();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }

            loObject.setThruDate(SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            poJSON = loObject.saveRecord();
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sStdRteID"), remarks, lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();

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
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sStdRteID"), remarks, lsStatus, false);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }

        poJSON = setJSON("success", "Record voided successfully.");
        return poJSON;
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
     * Converts a yyyy-MM-dd string into a {@link LocalDate}.
     *
     * @param val date string to parse
     * @return parsed local date
     */
    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    
    /**
     * Validates the current standard financing rate entry.
     *
     * @return JSON result indicating whether the entry is valid
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if validation fails
     */
    @Override
    public JSONObject isEntryOkay() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        Date loFromDate = getModel().getFromDate();
        Date loToDate = getModel().getThruDate();
        
        if (poModel.getStandardRateId() == null || "".equals(poModel.getStandardRateId())) {
            poJSON = setJSON("error", "Standard Rate Id must not be empty.");
            return poJSON;
        } 
        
        if (loFromDate == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid From date.");
            return poJSON;
        }

        if ("1900-01-01".equals(xsDateShort(loFromDate))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid From date.");
            return poJSON;
        }
//        if (loToDate == null) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Invalid Due date.");
//            return poJSON;
//        }
//
//        if ("1900-01-01".equals(xsDateShort(loToDate))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Invalid To date.");
//            return poJSON;
//        }
        
        if(loToDate != null && !"1900-01-01".equals(xsDateShort(loToDate))){
            LocalDate lldFromDate = strToDate(xsDateShort(loFromDate));
            LocalDate lldToDate = strToDate(xsDateShort(loToDate));
            if (lldToDate.isBefore(lldFromDate)) {
                poJSON.put("result", "error");
                poJSON.put("message", "To date cannot be before the from date.");
                return poJSON;
            }
        }
        
        if (poModel.getRateType() == null || "".equals(poModel.getRateType())) {
            poJSON = setJSON("error", "Rate type must not be empty.");
            return poJSON;
        } 
            
        if(getEditMode() == EditMode.ADDNEW){
            poJSON = checkExistingFinancingRate();
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }
        }
        
        if(FinancingRateStatus.StandardRateType.INTEREST_RATE.equals(getModel().getRateType())){
            if (poModel.getDuration() <= 0) {
                poJSON = setJSON("error", "Invalid duration value.");
                return poJSON;
            } 
            
            poJSON = checkBankFinancingRate();
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }
        } else {
            if (poModel.getDuration() != 0) {
                poJSON = setJSON("error", "Invalid duration value.");
                return poJSON;
            } 
        }
        
        if (poModel.getRate() <= 0.00) {
            poJSON = setJSON("error", "Invalid rate value.");
            return poJSON;
        } 
        
        poModel.setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
     * Checks whether an equivalent standard financing rate already exists.
     *
     * @return JSON result indicating whether a duplicate was found
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the lookup fails
     */
    private JSONObject checkExistingFinancingRate() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), " a.sStdRteID != " +  SQLUtil.toSQL(poModel.getStandardRateId())
                        + " AND a.sRateType = " +  SQLUtil.toSQL(poModel.getRateType())
                        + " AND a.nRateValx = " +  SQLUtil.toSQL(poModel.getRate())
                        + " AND a.nDuration = " +  SQLUtil.toSQL(poModel.getDuration())
                        + " AND " +  SQLUtil.toSQL(xsDateShort(poModel.getFromDate()))
                        + "  BETWEEN dFromDate AND dThruDate "
                        );
        
        Date loToDate = getModel().getThruDate();
        if(loToDate != null && !"1900-01-01".equals(xsDateShort(loToDate))){
            lsSQL = lsSQL + " AND " +  SQLUtil.toSQL(xsDateShort(poModel.getThruDate()))
                    + "  BETWEEN dFromDate AND dThruDate ";
        } else {
            lsSQL = lsSQL + " AND ( " +  SQLUtil.toSQL(xsDateShort(poGRider.getServerDate()))
                    + "  BETWEEN dFromDate AND dThruDate "
                    + " OR dThruDate IS NULL)";
        }
        
        System.out.println("checkExistingFinancingRate SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) >= 0) {
            if (loRS.next()) {
                // Print the result set
                System.out.println("sStdRteID: " + loRS.getString("sStdRteID"));
                System.out.println("------------------------------------------------------------------------------");

                poJSON = setJSON("error", "Financing rate already exists."
                        + "\nStandard Rate ID: " + loRS.getString("sStdRteID"));
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
     * Verifies that the rate exists among active bank financing rates.
     *
     * @return JSON result indicating whether a matching bank rate exists
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the lookup fails
     */
    private JSONObject checkBankFinancingRate() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(new SalesModels(poGRider).FinancingRateMaster()),
                                                " cRecdStat = " + SQLUtil.toSQL(FinancingRateStatus.ACTIVE)
                                                + " AND nDuration = " + SQLUtil.toSQL(getModel().getDuration())
                                                + " AND nRateValx = " + SQLUtil.toSQL(getModel().getRate())
                                                );
        System.out.println("checkBankFinancingRate SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) >= 0) {
            if (!loRS.next()) {
                poJSON = setJSON("error", "No active bank financing rate matches the entered duration and rate.");
            } 
        }  else {
            poJSON = setJSON("error", "No active bank financing rate matches the entered duration and rate.");
        }
        MiscUtil.close(loRS);
        
        return poJSON;
    
    }
    
    /**
     * Searches for a standard financing rate record.
     *
     * @param value search text or code
     * @param byCode true to search by code, false to search by description
     * @return JSON result of the record search
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the search fails
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
                    "Rate ID»Rate Type»Duration»Rate»From Date»To Date",
                    "sStdRteID»sRateType»nDuration»nRateValx»dFromDate»dThruDate",
                    "a.sStdRteID»a.sRateType»a.nDuration»a.nRateValx»a.dFromDate»a.dThruDate",
                    byCode ? 0 : 1);
        } else {
            try {
                ResultSet loRS = poGRider.executeQuery(lsSQL);
                poJSON = new JSONObject();
                if (MiscUtil.RecordCount(loRS) >= 0) {
                    if(loRS.next()){
                        poJSON.put("sStdRteID", loRS.getString("sStdRteID"));
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("ERROR: " + e.getMessage());
                poJSON = setJSON("error", e.getMessage());
            }
        
        }

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sStdRteID"));
        } else {
            poJSON = setJSON("error", "No record loaded.");
            return poJSON;
        }
    }
    
    
    @Override
    public JSONObject willSave(){
        poJSON = new JSONObject();
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
                return "Voided";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Builds the browse query for standard financing rate records.
     *
     * @return SQL browse statement
     */
    @Override
    public String getSQ_Browse() {
        return  "SELECT " +
            "  a.sStdRteID " +
            ", a.sRateType " +
            ", a.nDuration " +
            ", a.nRateValx " +
            ", a.dFromDate " +
            ", a.dThruDate " +
            ", a.cRecdStat " +
            ", a.sModified " +
            ", a.dModified " +
            "FROM Vehicle_Financing_Rates a " ;
        
    }
    
    /**
     * Loads the status history of the current record into a cached row set.
     *
     * @return cached row set containing status history data
     * @throws SQLException if a database error occurs
     */
    protected CachedRowSet getStatusHistoryTest() throws SQLException {
        String lsSQL = "SELECT  a.sTableNme, a.sSourceNo, a.sRemarksx, a.cRefrStat cTranStat, IFNULL(c.sCompnyNm, '-') xModified, IFNULL(e.sCompnyNm, '-') xApproved, a.dModified, a.dApproved, a.sModified, a.sApproved " +
                    " FROM Parameter_Status_History a " +
                    "LEFT JOIN xxxSysUser b ON b.sUserIDxx = a.sModified " +
                    "LEFT JOIN Client_Master c ON b.sEmployNo = c.sClientID " +
                    "LEFT JOIN xxxSysUser d ON d.sUserIDxx = a.sApproved " +
                    "LEFT JOIN Client_Master e ON d.sEmployNo = e.sClientID " +
                    " WHERE a.sSourceNo = " + SQLUtil.toSQL(getModel().getStandardRateId()) +
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
            showStatusHistoryUI("Standard Financing Rate", (String) getModel().getValue("sStdRteID"), entryBy, entryDate, crs);
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
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sStdRteID AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(getModel().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sStdRteID =  " + SQLUtil.toSQL(getModel().getStandardRateId()));
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

