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
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Vehicle;
import ph.com.guanzongroup.cas.sales.model.Model_Validity_Period_Master;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

/**
 *
 * @author Arsiela 
 */
public class VehicleQuotationValidator implements GValidator{
    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;
    
    Model_Sales_Quotation_Master poMaster;
    Model_Sales_Quotation_Vehicle poDetail;
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
        poMaster = (Model_Sales_Quotation_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setQuotationVehicle(Object value) {
        poDetail = (Model_Sales_Quotation_Vehicle) value;
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
        Date loTransactionDate = poMaster.getTransactionDate();
        if (poMaster.getTransactionNo() == null || "".equals(poMaster.getTransactionNo())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction no must not be empty.");
            return poJSON;
        } 
        
        if (poMaster.getIndustryId() == null || "".equals(poMaster.getIndustryId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Industry must not be empty.");
            return poJSON;
        } 
        
        if (poMaster.getCategoryCode() == null || "".equals(poMaster.getCategoryCode())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Category must not be empty.");
            return poJSON;
        } 
        
        if (loTransactionDate == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid transaction date.");
            return poJSON;
        }

        if ("1900-01-01".equals(xsDateShort(loTransactionDate))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid transaction date.");
            return poJSON;
        }
        
        if (poMaster.getClientId() == null || "".equals(poMaster.getClientId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Client is not set.");
            return poJSON;
        }
        
        //Validation Quotation Vehicle
        Date loExpectedDate = poDetail.getExpectedDate();
        Date loValidThruDate = poDetail.getValidThruDate();
        
        if (loExpectedDate == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid expected date.");
            return poJSON;
        }

        if ("1900-01-01".equals(xsDateShort(loExpectedDate))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid expected date.");
            return poJSON;
        }
        
        if (loValidThruDate == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid valid thru date.");
            return poJSON;
        }

        if ("1900-01-01".equals(xsDateShort(loValidThruDate))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid valid thru date.");
            return poJSON;
        }
        
        LocalDate lldFromDate = strToDate(xsDateShort(loExpectedDate));
        LocalDate lldToDate = strToDate(xsDateShort(loValidThruDate));
        if (lldToDate.isAfter(lldFromDate)) {
            poJSON.put("result", "error");
            poJSON.put("message", "Expected date cannot be after the valid thru date.");
            return poJSON;
        }
        
        if (poDetail.getBranchCode() == null || "".equals(poDetail.getBranchCode())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch is not set.");
            return poJSON;
        }
        
        if (poDetail.getVariantId() == null || "".equals(poDetail.getVariantId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Vehicle is not set.");
            return poJSON;
        }
        
        if (poDetail.getSourceNo() == null || "".equals(poDetail.getSourceNo())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source no is not set.");
            return poJSON;
        }
        
        if (poDetail.getSourceCode() == null || "".equals(poDetail.getSourceCode())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Source code is not set.");
            return poJSON;
        }
        
        if (poDetail.getPaymentForm() == null || "".equals(poDetail.getPaymentForm())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Mode of payment is not set.");
            return poJSON;
        }
        
        if (poDetail.getSRPAmount() <= 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "SRP amount is not set.");
            return poJSON;
        }
        
        if (poDetail.getDownPayment() <= 0.0000) {
            poJSON.put("result", "error");
            poJSON.put("message", "SRP amount is not set.");
            return poJSON;
        }
        
        //Validate financing if not cash
        if (!"0".equals(poDetail.getPaymentForm())) { //Financing / PO
            if (poDetail.getDownPayment() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "SRP amount is not set.");
                return poJSON;
            }
            
            if (poDetail.getQuotationRate() == null || "".equals(poDetail.getQuotationRate())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Bank is not set.");
                return poJSON;
            }
            if (poDetail.getFinancingAmount() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Finance amount is not set.");
                return poJSON;
            }
            if (poDetail.getMonthlyAmount() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Net monthly installment is not set.");
                return poJSON;
            }
            if (poDetail.getGrossMonthlyInstallment() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Gross monthly installment is not set.");
                return poJSON;
            }
            if (poDetail.getPromissoryNoteAmt() <= 0.0000) {
                poJSON.put("result", "error");
                poJSON.put("message", "Promissory note amount is not set.");
                return poJSON;
            }
        }
        
        poJSON.put("result", "success");
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
