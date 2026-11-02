/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.Model_Vehicle_Inclusion_Master;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.InclusionStatus;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Arsiela 09172026
 */
public class Inclusion extends Parameter {
    Model_Vehicle_Inclusion_Master poModel;
    ArrayList<Model_Vehicle_Inclusion_Master> paModel;
   
    private String psCompanyId = "";
    private String psApprover = "";

    /**
     * Initializes the Inclusion controller and model.
     *
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if initialization fails
     */
     @Override
    public void initialize() throws SQLException, GuanzonException {
      psRecdStat = InclusionStatus.OPEN;
      poModel = new SalesModels(poGRider).VehicleInclusionMaster();
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
     * Returns the current Inclusion model.
     *
     * @return active Inclusion model
     */
    @Override
    public Model_Vehicle_Inclusion_Master getModel() {
        return poModel;
    }
    
    private Model_Vehicle_Inclusion_Master Detail()
            throws SQLException,
            GuanzonException {
        return new SalesModels(poGRider).VehicleInclusionMaster();
    }
    
    public Model_Vehicle_Inclusion_Master Detail(int row) {
        return (Model_Vehicle_Inclusion_Master) paModel.get(row); 
    }

    public int getDetailCount() {
        if (paModel == null) {
            paModel = new ArrayList<>();
        }

        return paModel.size();
    }
    
    public JSONObject AddDetail() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        if (getDetailCount() > 0) {
            if ((Detail(getDetailCount() - 1).getModelId() == null || "".equals(Detail(getDetailCount() - 1).getModelId()))){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }
        
        paModel.add(Detail());
        poJSON = paModel.get(getDetailCount() - 1).newRecord();
        paModel.get(getDetailCount() - 1).setDescription(getModel().getDescription());
        return poJSON;
    }
    
    public void ReloadDetail() throws CloneNotSupportedException, SQLException, GuanzonException{
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            if (Detail(lnCtr).getModelId() == null || "".equals(Detail(lnCtr).getModelId())
                ) {
                paModel.remove(lnCtr);
            } 
            lnCtr--;
        }
        
        if ((getDetailCount() - 1) >= 0) {
            if (
                (Detail(getDetailCount() - 1).getModelId() != null && !"".equals(Detail(getDetailCount() - 1).getModelId()))
                && (Detail(getDetailCount() - 1).getDescription() != null && !"".equals(Detail(getDetailCount() - 1).getDescription()))
                ) {
                AddDetail();
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
    }
    
    private JSONObject loadVehicleInclusions(String fsInclusion) throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        paModel = new ArrayList<>();
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), " a.sInclDesc = " +  SQLUtil.toSQL(fsInclusion)
                        );

        System.out.println("loadVehicleInclusions SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) >= 0) {
            while (loRS.next()) {
                // Print the result set
                System.out.println("sInclIDxx: " + loRS.getString("sInclIDxx"));
                System.out.println("------------------------------------------------------------------------------");
                paModel.add(Detail());
                paModel.get(getDetailCount() - 1).openRecord(loRS.getString("sInclIDxx"));
            }
        }
        MiscUtil.close(loRS);
        
        poJSON = setJSON("success", "success.");
        poJSON.put("continue", true);
        return poJSON;
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
         
        poJSON = setJSON("success","success");
        return poJSON;
     }
    
    
    public JSONObject OpenRecord(String fsInclusionId) throws SQLException, GuanzonException, CloneNotSupportedException{
        poJSON = new JSONObject();
        
        poJSON = openRecord(fsInclusionId);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), " sInclDesc = " + SQLUtil.toSQL(getModel().getDescription()));
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        poJSON = new JSONObject();
        if (MiscUtil.RecordCount(loRS) >= 0) {
            AddDetail();
            while(loRS.next()){
                Detail(getDetailCount() - 1).openRecord(loRS.getString("sInclIDxx"));
                ReloadDetail();
            }
        }
        MiscUtil.close(loRS);
         
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
        
        String lsStatus = InclusionStatus.ACTIVE;

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

        
        poGRider.beginTrans("UPDATE STATUS", "ActivateRecord", SOURCE_CODE, getModel().getInclusionId());
        
        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sInclIDxx"), remarks, lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();
        
//        
//        //change status
//        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sInclIDxx"), remarks, lsStatus, false);
//        if (!isJSONSuccess(poJSON)) {
//            return poJSON;
//        }
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

        String lsStatus = InclusionStatus.INACTIVE;

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
        
        poGRider.beginTrans("UPDATE STATUS", "DeactivateRecord", SOURCE_CODE, getModel().getInclusionId());

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sInclIDxx"), remarks, lsStatus, false, true);
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

        String lsStatus = InclusionStatus.VOID;

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
        
        
        poGRider.beginTrans("UPDATE STATUS", "VoidRecord", SOURCE_CODE, getModel().getInclusionId());

        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sInclIDxx"), remarks, lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();

//        //change status
//        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sInclIDxx"), remarks, lsStatus, false);
//        if (!isJSONSuccess(poJSON)) {
//            return poJSON;
//        }

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
     * Validates the current Inclusion entry.
     *
     * @return JSON result indicating whether the entry is valid
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if validation fails
     */
    @Override
    public JSONObject isEntryOkay() throws SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (poModel.getInclusionId() == null || "".equals(poModel.getInclusionId())) {
            poJSON = setJSON("error", "Inclusion Id must not be empty.");
            return poJSON;
        }

        if (poModel.getDescription() == null || "".equals(poModel.getDescription())) {
            poJSON = setJSON("error", "Description must not be empty.");
            return poJSON;
        } 
            
        poJSON = checkExistingInclusion();
        if(!isJSONSuccess(poJSON)){
            return poJSON;
        }

        poModel.setModifiedBy(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    /**
     * Checks whether an equivalent Inclusion already exists.
     *
     * @return JSON result indicating whether a duplicate was found
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if the lookup fails
     */
    private JSONObject checkExistingInclusion() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), " a.sInclIDxx != " +  SQLUtil.toSQL(poModel.getInclusionId())
                        + " AND a.sInclDesc = " +  SQLUtil.toSQL(poModel.getDescription())
                        + " AND a.cRecdStat != " +  SQLUtil.toSQL(InclusionStatus.VOID)
                        );

        System.out.println("checkExistingInclusion SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) >= 0) {
            if (loRS.next()) {
                // Print the result set
                System.out.println("sInclIDxx: " + loRS.getString("sInclIDxx"));
                System.out.println("------------------------------------------------------------------------------");

                poJSON = setJSON("error", "Inclusion already exists."
                        + "\nInclusion Id: " + loRS.getString("sInclIDxx"));
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
     * Searches for a Inclusion record.
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
                    "Inclusion ID»Description",
                    "sInclIDxx»sInclDesc",
                    "a.sInclIDxx»a.sInclDesc",
                    byCode ? 0 : 1);
        } else {
            try {
                ResultSet loRS = poGRider.executeQuery(lsSQL);
                poJSON = new JSONObject();
                if (MiscUtil.RecordCount(loRS) >= 0) {
                    if(loRS.next()){
                        poJSON.put("sInclIDxx", loRS.getString("sInclIDxx"));
                    }
                }
                MiscUtil.close(loRS);
            } catch (SQLException e) {
                System.out.println("ERROR: " + e.getMessage());
                poJSON = setJSON("error", e.getMessage());
            }
        
        }

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sInclIDxx"));
        } else {
            poJSON = setJSON("error", "No record loaded.");
            return poJSON;
        }
    }
    
    
    @Override
    public JSONObject willSave() throws SQLException{
        poJSON = new JSONObject();
        if(InclusionStatus.ACTIVE.equals(getModel().getRecordStatus())){
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
            case InclusionStatus.OPEN:
                return "Open";
            case InclusionStatus.ACTIVE:
                return "Active";
            case InclusionStatus.INACTIVE:
                return "Inactive";
            case InclusionStatus.VOID:
                return "Voided";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Builds the browse query for Inclusion records.
     *
     * @return SQL browse statement
     */
    @Override
    public String getSQ_Browse() {
        return  "SELECT " +
            "  a.sInclIDxx " +
            ", a.sInclDesc " +
//            ", a.sModelIDx " +
//            ", a.sVrntIDxx " +
            ", a.cRecdStat " +
            ", a.sModified " +
            ", a.dModified " +
            "FROM Vehicle_Inclusion_Master a " ;
        
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
                    " WHERE a.sSourceNo = " + SQLUtil.toSQL(getModel().getInclusionId()) +
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
     * Displays the status history of a record.
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
                case InclusionStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case InclusionStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                case InclusionStatus.ACTIVE:
                    crs.updateString("cRefrStat", "ACTIVE");
                    break;
                case InclusionStatus.INACTIVE:
                    crs.updateString("cRefrStat", "INACTIVE");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat){
                        case InclusionStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case InclusionStatus.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;
                        case InclusionStatus.ACTIVE:
                            crs.updateString("cRefrStat", "ACTIVE");
                            break;
                        case InclusionStatus.INACTIVE:
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
            showStatusHistoryUI("Inclusion", (String) getModel().getValue("sInclIDxx"), entryBy, entryDate, crs);
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
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sInclIDxx AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(getModel().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sInclIDxx =  " + SQLUtil.toSQL(getModel().getInclusionId()));
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

