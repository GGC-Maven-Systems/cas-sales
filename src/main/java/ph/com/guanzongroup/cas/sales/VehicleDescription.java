package ph.com.guanzongroup.cas.sales;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.codec.binary.Base64;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscReplUtil;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.base.WebFile;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.token.RequestAccess;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.Color;
import org.guanzon.cas.parameter.Model;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.model.Model_Model_Variant_Insurance;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class VehicleDescription extends Parameter {
    Model_Model_Variant poModel;
    Model_Model_Variant_Insurance poModelVariantInsurance;
    public String psIndustryId = "";
    public String psCategoryId = "";
    public String psBrandId = "";
    public String psApprover = "";
    public String psSourceCode = "Vrnt"; //For Attachment, to retrieve specific attachment for the model variant;
    List<TransactionAttachment> paAttachments;
    
    @Override
    public void initialize() throws SQLException, GuanzonException{
        psRecdStat = Logical.YES;
        
        poModel = new ParamModels(poGRider).ModelVariant();
        poModelVariantInsurance = new ParamModels(poGRider).ModelVariantInsurance();
        paAttachments = new ArrayList<>();
        psBrandId = "";
        super.initialize();
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
    
    //Set value for private strings used in searching / filtering data
    public void setIndustryId(String industryId) { psIndustryId = industryId; }
    public void setCategoryId(String categoryId) { psCategoryId = categoryId; }
    public void setBrandIdId(String brandId) { psBrandId = brandId; }
    public String getBrand() throws SQLException, GuanzonException { 
        if(getModel().getModelId() != null && !"".equals(getModel().getModelId())){
            psBrandId = poModel.Model().getBrandId();
            return poModel.Model().Brand().getDescription();
        } else {
            if(psBrandId != null && !"".equals(psBrandId)){
                Model_Brand loObj = new ParamModels(poGRider).Brand();
                loObj.initialize();
                poJSON = loObj.openRecord(psBrandId);
                if(isJSONSuccess(poJSON)){
                    return loObj.getDescription();
                }
            } 
        }
        
        return "";
    }
    
    public JSONObject NewRecord() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        poJSON = newRecord();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = poModelVariantInsurance.newRecord();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = setJSON("success","success");
        return poJSON;
    }
    
    public JSONObject OpenRecord(String fsVariantId) throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        poJSON = openRecord(fsVariantId);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = poModelVariantInsurance.openRecord(fsVariantId);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = setJSON("success","success");
        return poJSON;
    }
    
    public JSONObject UpdateRecord() throws SQLException, GuanzonException{
        poJSON = new JSONObject();
        
        poJSON = updateRecord();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = poModelVariantInsurance.updateRecord();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
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
        
        String lsStatus = RecordStatus.ACTIVE;

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

        poGRider.beginTrans("UPDATE STATUS", "ActivateRecord", SOURCE_CODE, poModel.getVariantId());
        
        poJSON = generateInventory(lsStatus);
        if (!isJSONSuccess(poJSON)){
            return poJSON;
        }
        
        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sVrntIDxx"), remarks, lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();
        
        poJSON = setJSON("success", "Record activated successfully.");
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

        String lsStatus = RecordStatus.INACTIVE;

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
        
        poGRider.beginTrans("UPDATE STATUS", "DeactivateRecord", SOURCE_CODE, poModel.getVariantId());
        
//        poJSON = generateInventory(lsStatus); 
//        if (!isJSONSuccess(poJSON)){
//            return poJSON;
//        }
        
        //change status
        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sVrntIDxx"), remarks, lsStatus, false, true);
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poGRider.commitTrans();

        poJSON = setJSON("success", "Record deactivated successfully.");
        return poJSON;
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
    
    private boolean checkEmpty(String fsValue){
        return fsValue == null || "".equals(fsValue);
    }
    
    private boolean checkNotEmpty(String fsValue){
        return fsValue != null && !"".equals(fsValue);
    }
    
    @Override
    public JSONObject isEntryOkay() throws SQLException{
        poJSON = new JSONObject();
        poModelVariantInsurance.setVariantId(poModel.getVariantId());
        
        if (checkEmpty(poModel.getDescription())){
            poJSON.put("result", "error");
            poJSON.put("message", "Description must not be empty.");
            return poJSON;
        }

        if (poModel.getYearModel() == 0){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid year model.");
            return poJSON;
        }

        if (checkEmpty(poModel.getModelId())){
            poJSON.put("result", "error");
            poJSON.put("message", "Model must not be empty.");
            return poJSON;
        }

        if (checkEmpty(poModel.getColorId())){
            poJSON.put("result", "error");
            poJSON.put("message", "Color must not be empty.");
            return poJSON;
        }
        
        if (checkEmpty(poModelVariantInsurance.getVariantId())){
            poJSON.put("result", "error");
            poJSON.put("message", "Variant must not be empty.");
            return poJSON;
        }

        if (checkEmpty(poModelVariantInsurance.getTransmission())){
            poJSON.put("result", "error");
            poJSON.put("message", "Transmission must not be empty.");
            return poJSON;
        }

        if (checkEmpty(poModelVariantInsurance.getVehicleType())){
            poJSON.put("result", "error");
            poJSON.put("message", "Vehicle type must not be empty.");
            return poJSON;
        }
        
        if (checkEmpty(poModelVariantInsurance.getBodyType())){
            poJSON.put("result", "error");
            poJSON.put("message", "Body type must not be empty.");
            return poJSON;
        }

        if (poModelVariantInsurance.getAuthCapx() <= 0 || poModelVariantInsurance.getAuthCapx() > 20){
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid authorize capacity.");
            return poJSON;
        }
        
        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public Model_Model_Variant getModel() {
        return poModel;
    }
    
    public Model_Model_Variant_Insurance getModelVariantInsurance() {
        return poModelVariantInsurance;
    }
    
    
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException{
        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), "d.sIndstCdx = " + SQLUtil.toSQL(psIndustryId));
        
        System.out.println("Search Record by industry id SQL : " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Model Name»Variant»Transmission»Year Model»Color",
                "xModelNme»sDescript»sTransmss»nYearMdlx»xColorNme",
                "IFNULL(b.sDescript, '')»a.sDescript»IFNULL(e.sTransmss,'')»a.nYearMdlx»IFNULL(c.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return OpenRecord((String) poJSON.get("sVrntIDxx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }   
    
    public JSONObject searchRecordByModel(String value, boolean byCode, String modelId) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        
        if (modelId != null){
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sModelIDx = " + SQLUtil.toSQL(modelId));
        }
        
        System.out.println("Search Record by model id SQL : " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Model Name»Variant»Transmission»Year Model»Color",
                "xModelNme»sDescript»sTransmss»nYearMdlx»xColorNme",
                "IFNULL(b.sDescript, '')»a.sDescript»IFNULL(e.sTransmss,'')»a.nYearMdlx»IFNULL(c.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return OpenRecord((String) poJSON.get("sVrntIDxx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
        
    }    
    
    public JSONObject searchRecordByBrand(String value, boolean byCode, String brandId) throws SQLException, GuanzonException{
        String lsSQL = getSQ_Browse();
        
        if (brandId != null){
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sBrandIDx = " + SQLUtil.toSQL(brandId));
        }
        System.out.println("Search Record By brand id SQL : " + lsSQL);
        poJSON = ShowDialogFX.Search(poGRider,
                lsSQL,
                value,
                "Model Name»Variant»Transmission»Year Model»Color",
                "xModelNme»sDescript»sTransmss»nYearMdlx»xColorNme",
                "IFNULL(b.sDescript, '')»a.sDescript»IFNULL(e.sTransmss,'')»a.nYearMdlx»IFNULL(c.sDescript, '')",
                byCode ? 0 : 1);
        
        if (poJSON != null) {
            return OpenRecord((String) poJSON.get("sVrntIDxx"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }    
    
    public JSONObject SearchBrand(String value, boolean byCode)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        Brand object = new ParamControllers(poGRider, logwrapr).Brand();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode, psIndustryId);
        if ("success".equals((String) poJSON.get("result"))) {
            setBrandIdId(object.getModel().getBrandId());
        }
        return poJSON;
    }
    
    public JSONObject SearchModel(String value, boolean byCode)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(checkEmpty(psBrandId)){
            return setJSON("error", "Brand cannot be empty.");
        }
        
        Model object = new ParamControllers(poGRider, logwrapr).Model();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode, psBrandId);
        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setModelId(object.getModel().getModelId());
            psBrandId = poModel.Model().getBrandId();
        }
        return poJSON;
    }
    
    public JSONObject SearchColor(String value, boolean byCode)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        Color object = new ParamControllers(poGRider, logwrapr).Color();
        object.setRecordStatus(RecordStatus.ACTIVE);
        object.setIndustryId(psIndustryId);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setColorId(object.getModel().getColorId());
        }
        return poJSON;
    }
    
    private JSONObject generateInventory(String fsRecordStatus)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
       
        Model_Inventory loObj = new InvModels(poGRider).Inventory();
        loObj.initialize();
        
        if(getEditMode() == EditMode.ADDNEW){
            poJSON = loObj.newRecord();
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }

            loObj.setIndustryCode(psIndustryId);
            loObj.setCategoryFirstLevelId(psCategoryId);
//            loObj.setBarCode(poModel.getDescription().replace(" ", "")); //Replace space
            loObj.setBarCode(poModel.getModelId()+poModel.getVariantId()+psIndustryId); //Replace space
            loObj.isSerialized(true);
            
        } else {
            //Find the inventory
            String lsInvId = findInventory(loObj);
            if(!checkEmpty(lsInvId)){
                poJSON = loObj.openRecord(lsInvId);
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
                
                poJSON = loObj.updateRecord();
                if(!isJSONSuccess(poJSON)){
                    return poJSON;
                }
            } else {
                if(getEditMode() == EditMode.UPDATE){
                    poJSON = loObj.newRecord();
                    if(!isJSONSuccess(poJSON)){
                        return poJSON;
                    }

                    loObj.setIndustryCode(psIndustryId);
                    loObj.setCategoryFirstLevelId(psCategoryId);
                    loObj.setBarCode(poModel.getDescription().replace(" ", "")); //Replace space
                    loObj.isSerialized(true);
                 }
            }
            
        }
        
        if(loObj.getEditMode() == EditMode.ADDNEW || loObj.getEditMode() == EditMode.UPDATE){
            
            loObj.setBrandId(poModel.Model().getBrandId());
            loObj.setModelId(poModel.getModelId());
            loObj.setVariantId(poModel.getVariantId());
            loObj.setColorId(poModel.getColorId());
            loObj.setDescription(poModel.Model().Brand().getDescription() + " " + poModel.Model().getDescription() + " " + poModel.getDescription() + " " + poModel.Color().getDescription());
            loObj.setRecordStatus(fsRecordStatus);

            poJSON = loObj.saveRecord();
            if(!isJSONSuccess(poJSON)){
                return poJSON;
            }
        }
        
        return poJSON;
    }
    
    public String findInventory(Model_Inventory loObj) throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();
       
        String lsSQL = MiscUtil.makeSelect(loObj);
        lsSQL = MiscUtil.addCondition(lsSQL," sIndstCdx =  " + SQLUtil.toSQL(psIndustryId)
                       + " AND sVrntIDxx =  " + SQLUtil.toSQL(poModel.getVariantId())
                       + " AND sCategCd1 =  " + SQLUtil.toSQL(psCategoryId)
                    );
        
        System.out.println("findInventory SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) <= 0) {
            return "";
        }
        String lsInvId = "";
        if(loRS.next()) {
            lsInvId = loRS.getString("sStockIDx");
        }
        MiscUtil.close(loRS);
        
        return lsInvId;
    }
    
    public String checkExistingVariant() {
        String lsRecId = "";
        poJSON = new JSONObject();
        try {
            String lsSQL = "SELECT  "
                    + "  a.sVrntIDxx  "
                    + ", a.sDescript  "
                    + ", a.nSelPrice  "
                    + ", a.nYearMdlx  "
                    + ", a.sPayloadx  "
                    + ", a.sModelIDx  "
                    + ", a.sColorIDx  "
                    + ", a.cRecdStat  "
                    + ", b.sVhclType "
                    + ", b.sBodyType "
                    + ", b.sTransmss "
                    + ", b.nAuthCapx "
                    + " FROM Model_Variant a  "
                    + " LEFT JOIN Model_Variant_Insurance b ON b.sVrntIDxx = a.sVrntIDxx";
            lsSQL = MiscUtil.addCondition(lsSQL," a.sDescript =  " + SQLUtil.toSQL(poModel.getDescription())
                    + " AND a.sVrntIDxx !=  " + SQLUtil.toSQL(poModel.getVariantId())
                    + " AND a.sModelIDx =  " + SQLUtil.toSQL(poModel.getModelId())
                    + " AND a.sColorIDx =  " + SQLUtil.toSQL(poModel.getColorId())
                    + " AND b.sVhclType =  " + SQLUtil.toSQL(poModelVariantInsurance.getVehicleType())
                    + " AND b.sBodyType =  " + SQLUtil.toSQL(poModelVariantInsurance.getBodyType())
                    + " AND b.sTransmss =  " + SQLUtil.toSQL(poModelVariantInsurance.getTransmission())
            );
            
            System.out.println("checkExistingVariant SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) <= 0) {
                return "";
            }
            if(loRS.next()) {
                lsRecId = loRS.getString("sVrntIDxx");
            }
            MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            return "";
        }
        return lsRecId;
    }
    
    
    @Override
    protected JSONObject willSave()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if(getEditMode() == EditMode.ADDNEW){
            poModel.setVariantId(poModel.getNextCode());
        }
        
        if(getEditMode() == EditMode.UPDATE){
            if(!pbWthParent){
                psApprover = poGRider.getUserID();
                poJSON = callApproval();
                if (!isJSONSuccess(poJSON)) {
                    return poJSON;
                }
            }
        }
        String lsValVariant = checkExistingVariant();
        if(checkNotEmpty(lsValVariant)){
            poJSON = setJSON("error","Variant already exists.\nPlease check Variant ID: " + lsValVariant);
            return poJSON;
        }
        
        poJSON = setJSON("success", "success");
        return poJSON;
    }
    
    @Override
    protected JSONObject saveOthers() {
        try {
            poJSON = new JSONObject();
            if(poModelVariantInsurance.getEditMode() == EditMode.ADDNEW || poModelVariantInsurance.getEditMode() == EditMode.UPDATE){
                poModelVariantInsurance.setVariantId(poModel.getVariantId());
                    poJSON = poModelVariantInsurance.saveRecord();
                
                if (!isJSONSuccess(poJSON)) {
                    poJSON = setJSON("error", "Unable to save model variant parameter.\n"+(String) poJSON.get("message"));
                    return poJSON;
                }
            }
            
            poJSON = generateInventory(poModel.getRecordStatus());
            if (!isJSONSuccess(poJSON)){
                poJSON = setJSON("error", "Unable to save inventory.\n"+(String) poJSON.get("message"));
                return poJSON;
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
            
        } catch (ExceptionInInitializerError | CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON = setJSON("error", MiscUtil.getException(ex));
            return poJSON;
        } 
        
        return poJSON;
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
        paAttachments.get(getTransactionAttachmentCount() - 1).getModel().setSourceNo(getModel().getVariantId());
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
        List loList = loAttachment.getAttachments(psSourceCode, getModel().getVariantId());
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
            if ("success".equals((String) poJSON.get("result"))) {
                if(getModel().getEditMode() == EditMode.UPDATE){
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
                    , psSourceCode
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
         return new String(Base64.encodeBase64(bytes), "UTF-8");
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
    
    @Override
    public String getSQ_Browse(){
        String lsCondition = "";

        if (psRecdStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
            }

            lsCondition = "a.cRecdStat IN (" + lsCondition.substring(2) + ")";
        } else {
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
        }
        
        String lsSQL = "SELECT" +
                            "  a.sVrntIDxx" +
                            ", a.sDescript" +
                            ", a.nSelPrice" +
                            ", a.nYearMdlx" +
                            ", a.sPayloadx" +
                            ", a.sModelIDx" +
                            ", a.sColorIDx" +
                            ", a.cRecdStat" +
                            ", a.sModified" +
                            ", a.dModified" +
                            ", IFNULL(b.sModelCde, '') xModelCde" +
                            ", IFNULL(b.sDescript, '') xModelNme" +
                            ", IFNULL(c.sDescript, '') xColorNme" +
                            ", IFNULL(d.sDescript, '') xBrandNme" +
                            ", IFNULL(b.sBrandIDx, '') xBrandIDx" +
                            ", d.sIndstCdx " +
                            ", e.sTransmss " +
                        " FROM Model_Variant a" +
                            " INNER JOIN Model_Variant_Insurance e ON e.sVrntIDxx = a.sVrntIDxx" +
                            " LEFT JOIN Model b ON a.sModelIDx = b.sModelIDx" +
                            " LEFT JOIN Color c ON a.sColorIDx = c.sColorIDx" +
                            " LEFT JOIN Brand d ON b.sBrandIDx = d.sBrandIDx";
        
        return MiscUtil.addCondition(lsSQL, lsCondition);
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
                    " WHERE a.sSourceNo = " + SQLUtil.toSQL(getModel().getVariantId()) +
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
//                case VehicleDescriptionStatus.OPEN:
//                    crs.updateString("cRefrStat", "OPEN");
//                    break;
//                case VehicleDescriptionStatus.VOID:
//                    crs.updateString("cRefrStat", "VOID");
//                    break;
                case RecordStatus.ACTIVE:
                    crs.updateString("cRefrStat", "ACTIVE");
                    break;
                case RecordStatus.INACTIVE:
                    crs.updateString("cRefrStat", "INACTIVE");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat){
//                        case VehicleDescriptionStatus.OPEN:
//                            crs.updateString("cRefrStat", "OPEN");
//                            break;
//                        case VehicleDescriptionStatus.VOID:
//                            crs.updateString("cRefrStat", "VOID");
//                            break;
                        case RecordStatus.ACTIVE:
                            crs.updateString("cRefrStat", "ACTIVE");
                            break;
                        case RecordStatus.INACTIVE:
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
            showStatusHistoryUI("Vehicle Description", (String) getModel().getValue("sVrntIDxx"), entryBy, entryDate, crs);
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
                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sVrntIDxx AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(getModel().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sVrntIDxx =  " + SQLUtil.toSQL(getModel().getVariantId()));
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

}