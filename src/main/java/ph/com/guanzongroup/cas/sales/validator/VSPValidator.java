/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.validator;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Inquiry_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Inquiry_Master;
import ph.com.guanzongroup.cas.sales.status.SalesInquiryStatic;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TEEJEI DE CELIS
 * @date August 15, 2026
 * @module VSP
 * @since 1.0
 */
public class VSPValidator implements GValidator{
    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;

    Model_Sales_Inquiry_Master poMaster;
    ArrayList<Model_Sales_Inquiry_Detail> poDetail;

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
        poMaster = (Model_Sales_Inquiry_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for(int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++){
            poDetail.add((Model_Sales_Inquiry_Detail) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        try {
            switch (psTranStat){
                case SalesInquiryStatic.OPEN:
                    return validateNew();
                case SalesInquiryStatic.CONFIRMED:
                    return validateConfirmed();
                case SalesInquiryStatic.QUOTED:
                    return validateQuoted();
                case SalesInquiryStatic.SALE:
                    return validateSale();
                case SalesInquiryStatic.LOST:
                    return validateLost();
                case SalesInquiryStatic.CANCELLED:
                    return validateCancelled();
                case SalesInquiryStatic.VOID:
                    return validateVoid();
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
        Date loTargetDate = poMaster.getTargetDate();
        LocalDate serverDate = strToDate(xsDateShort(poGRider.getServerDate()));
        LocalDate oneYearAgo = serverDate.minusYears(1);

        if (loTransactionDate == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        if ("1900-01-01".equals(xsDateShort(loTransactionDate))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        if (loTargetDate == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Target Date.");
            return poJSON;
        }
        if (poMaster.getIndustryId() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Industry is not set.");
            return poJSON;
        }
        if (poMaster.getCompanyId() == null || "".equals(poMaster.getCompanyId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Company is not set.");
            return poJSON;
        }
        if (poMaster.getCategoryCode()== null || "".equals(poMaster.getCategoryCode())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Category is not set.");
            return poJSON;
        }
        if (poMaster.getBranchCode()== null || "".equals(poMaster.getBranchCode())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch is not set.");
            return poJSON;
        }
        if (poMaster.getPurchaseType()== null || "".equals(poMaster.getPurchaseType())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Purchase Type is not set.");
            return poJSON;
        }
        if(SalesInquiryStatic.CategoryCode.MOTORCYCLE.equals(poMaster.getCategoryCode())){
            if (poMaster.getCategoryType()== null || "".equals(poMaster.getCategoryType())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Unit Type is not set.");
                return poJSON;
            }
        }
        if (poMaster.getSourceCode()== null || "".equals(poMaster.getSourceCode())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Inquiry Type is not set.");
            return poJSON;
        }
        if (poMaster.getSalesMan()== null || "".equals(poMaster.getSalesMan())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Sales Person is not set.");
            return poJSON;
        }
        if (poMaster.getClientId() == null || "".equals(poMaster.getClientId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Client is not set.");
            return poJSON;
        }
//        if (poMaster.getAddressId()== null || "".equals(poMaster.getAddressId())) {
//            poJSON.put("message", "Address is not set.");
//            return poJSON;
//        }
//        if (poMaster.getContactId()== null || "".equals(poMaster.getContactId())) {
//            poJSON.put("message", "Contact Number is not set.");
//            return poJSON;
//        }
        if (poMaster.getInquiryStatus()== null || "".equals(poMaster.getInquiryStatus())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Inquiry Status is not set.");
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateConfirmed()throws SQLException{
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateProcessed(){
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateCancelled() throws SQLException{
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateVoid() throws SQLException{
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateQuoted(){
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateSale(){
        poJSON = new JSONObject();

        poJSON.put("result", "success");
        return poJSON;
    }

    private JSONObject validateLost(){
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
