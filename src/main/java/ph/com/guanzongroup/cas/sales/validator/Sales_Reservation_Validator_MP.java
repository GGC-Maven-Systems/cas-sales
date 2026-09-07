package ph.com.guanzongroup.cas.sales.validator;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.constant.Sales_Reservation_Static;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Reservation_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Reservation_Master;

import java.util.ArrayList;

public class Sales_Reservation_Validator_MP implements GValidator{
    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;
    
    Model_Sales_Reservation_Master poMaster;
    ArrayList<Model_Sales_Reservation_Detail> poDetail;

    @Override
    public void setApplicationDriver(Object applicationDriver) {
        poGrider = (GRiderCAS) applicationDriver;
    }

    @Override
    public void setTransactionStatus(String transactionStatus) {
        psTranStat = transactionStatus;
    }

    @Override
    public void setMaster(Object value) {
        poMaster = (Model_Sales_Reservation_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for(int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++){
            poDetail.add((Model_Sales_Reservation_Detail) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        switch (psTranStat){
            case Sales_Reservation_Static.OPEN:
                return validateNew();
            case Sales_Reservation_Static.CONFIRMED:
                return validateConfirmed();
            case Sales_Reservation_Static.PAID:
                return validatePaid();
            case Sales_Reservation_Static.CANCELLED:
                return validateCancelled();
            default:
                poJSON = new JSONObject();
                poJSON.put("result", "success");
        }
        
        return poJSON;
    }
    
    private JSONObject validateNew(){
        poJSON = new JSONObject();
        
        if (poMaster.getIndustryID()== null || poMaster.getIndustryID().isEmpty()) {
            poJSON.put("message", "Industry is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getBranchCode()== null || poMaster.getBranchCode().isEmpty()) {
            poJSON.put("message", "Invalid Branch");
            return poJSON;
        }

        if (poMaster.getIndustryID()== null || poMaster.getIndustryID().isEmpty()) {
            poJSON.put("message", "Industry is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getCompanyID()== null || poMaster.getCompanyID().isEmpty()) {
            poJSON.put("message", "Company is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getClientID()== null || poMaster.getClientID().isEmpty()) {
            poJSON.put("message", "Client ID is missing or not set.");
            return poJSON;
        }        
        
        if (poMaster.getAddressID()== null || poMaster.getAddressID().isEmpty()) {
            poJSON.put("message", "Adress is missing or not set.");
            return poJSON;
        }
        
//        if (poMaster.getContactID()== null || poMaster.getContactID().isEmpty()) {
//            poJSON.put("message", "Contact is missing or not set.");
//            return poJSON;
//        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateConfirmed(){
        poJSON = new JSONObject();
        
        if (poMaster.getIndustryID()== null || poMaster.getIndustryID().isEmpty()) {
            poJSON.put("message", "Industry is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getBranchCode()== null || poMaster.getBranchCode().isEmpty()) {
            poJSON.put("message", "Invalid Branch");
            return poJSON;
        }

        if (poMaster.getIndustryID()== null || poMaster.getIndustryID().isEmpty()) {
            poJSON.put("message", "Industry is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getCompanyID()== null || poMaster.getCompanyID().isEmpty()) {
            poJSON.put("message", "Company is missing or not set.");
            return poJSON;
        }
        
        if (poMaster.getClientID()== null || poMaster.getClientID().isEmpty()) {
            poJSON.put("message", "Client ID is missing or not set.");
            return poJSON;
        }        
        
        if (poMaster.getAddressID()== null || poMaster.getAddressID().isEmpty()) {
            poJSON.put("message", "Adress is missing or not set.");
            return poJSON;
        }
        
//        if (poMaster.getContactID()== null || poMaster.getContactID().isEmpty()) {
//            poJSON.put("message", "Contact is missing or not set.");
//            return poJSON;
//        }
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validatePaid(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateCancelled(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
}
