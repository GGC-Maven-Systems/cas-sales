/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.validator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Validity_Period_Master;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

/**
 *
 * @author Arsiela 
 */
public class ValidityMasterValidator implements GValidator{
    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;
    
    Model_Validity_Period_Master poMaster;
    ArrayList<Object> paDetail;

    @Override
    public void setApplicationDriver(Object applicationDriver) {
        poGRider = (GRiderCAS) applicationDriver;
    }

    @Override
    public void setTransactionStatus(String transactionStatus) {
        psTranStat = transactionStatus;
    }

    @Override
    public void setMaster(Object value) {
        poMaster = (Model_Validity_Period_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        try {
            switch (psTranStat){
                case ValidityPeriodStatus.OPEN:
                    return validateNew();
                case ValidityPeriodStatus.APPROVED:
                    return validateApproved();
                case ValidityPeriodStatus.VOID:
                    return validateVoid();
                case ValidityPeriodStatus.CANCELLED:
                    return validateCancelled();
                default:
                    poJSON = new JSONObject();
                    poJSON.put("result", "success");
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        return poJSON;
    }
    
    private JSONObject validateNew() throws SQLException{
        poJSON = new JSONObject();
        Date loFromDate = poMaster.getFromDate();
        Date loToDate = poMaster.getThruDate();
        
        if (poMaster.getValidityId() == null || "".equals(poMaster.getValidityId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Validity Id must not be empty.");
            return poJSON;
        } 
        
        if (poMaster.getCompanyId() == null || "".equals(poMaster.getCompanyId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Company Id must not be empty.");
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
        if (loToDate == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Due date.");
            return poJSON;
        }

        if ("1900-01-01".equals(xsDateShort(loToDate))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid To date.");
            return poJSON;
        }
        
//        if (loToDate != null) {
//            if (!"1900-01-01".equals(xsDateShort(loToDate))) {
                LocalDate lldFromDate = strToDate(xsDateShort(loFromDate));
                LocalDate lldToDate = strToDate(xsDateShort(loToDate));
                if (lldToDate.isBefore(lldFromDate)) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "To date cannot be before the from date.");
                    return poJSON;
                }
//            }
//        }
        if (poMaster.getValidityDescription() == null || "".equals(poMaster.getValidityDescription())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Validity description is not set.");
            return poJSON;
        }
        
        poJSON = checkExistingVehicleFinancing();
        if("error".equals((String) poJSON.get("result"))){
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject checkExistingVehicleFinancing(){
        try {
            String lsSQL = MiscUtil.addCondition(MiscUtil.makeSelect(poMaster),
                                                    " cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.VOID)
                                                    + " AND cRecdStat != " + SQLUtil.toSQL(ValidityPeriodStatus.CANCELLED)
                                                    + " AND sValidIDx != " + SQLUtil.toSQL(poMaster.getValidityId())
                                                    );
          lsSQL = lsSQL 
                    + " AND ((dFromDate between "+ SQLUtil.toSQL(xsDateShort(poMaster.getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(poMaster.getThruDate())) +") OR dFromDate <= "+ SQLUtil.toSQL(xsDateShort(poMaster.getFromDate()))+")"
                    + " AND ((dThruDate between "+ SQLUtil.toSQL(xsDateShort(poMaster.getFromDate()))+" AND "+  SQLUtil.toSQL(xsDateShort(poMaster.getThruDate())) +") OR dThruDate IS NULL OR dThruDate >= "+ SQLUtil.toSQL(xsDateShort(poMaster.getFromDate()))+")";
      
            System.out.println("checkExistingVehicleFinancing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0) {
                if(loRS.next()){    
                    poJSON.put("result", "error");
                    poJSON.put("message", "A Vehicle Financing Promo already exists for the selected validity period.");
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
    
    private JSONObject validateApproved()throws SQLException{
        poJSON = new JSONObject();
        
        poJSON = validateNew();
        if("error".equals((String) poJSON.get("result"))){
            return poJSON;
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateVoid(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateCancelled(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
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
    
    
}
