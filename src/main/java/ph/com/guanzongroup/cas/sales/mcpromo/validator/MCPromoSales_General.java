package ph.com.guanzongroup.cas.sales.mcpromo.validator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import org.guanzon.cas.inv.warehouse.status.DeliveryScheduleStatus;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Master;

/**
 *
 * @author MNV t4
 */
public class MCPromoSales_General implements GValidator {

    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;

    Model_Sales_Promotion_Master poMaster;

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
        poMaster = (Model_Sales_Promotion_Master) value;
    }

    @SuppressWarnings("unchecked")
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
            switch (psTranStat) {
                case DeliveryScheduleStatus.OPEN:
                    return validateNew();
                case DeliveryScheduleStatus.CONFIRMED:
                    return validateConfirmed();
                case DeliveryScheduleStatus.POSTED:
                    return validatePosted();
                case DeliveryScheduleStatus.CANCELLED:
                    return validateCancelled();
                case DeliveryScheduleStatus.VOID:
                    return validateVoid();
                default:
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", "unsupported function");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MCPromoSales_General.class.getName()).log(Level.SEVERE, null, ex);
        }

        return poJSON;
    }

    private JSONObject validateNew() throws SQLException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;

        if (poMaster.getDate()== null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);

        return poJSON;
    }

    private JSONObject validateConfirmed() throws SQLException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;

        if (poMaster.getDate()== null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }
        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);

        return poJSON;
    }

    private JSONObject validatePosted() {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }
        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

    private JSONObject validateCancelled() throws SQLException {
        boolean isRequiredApproval = false;
        poJSON = new JSONObject();

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            isRequiredApproval = true;
        }
        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

    private JSONObject validateVoid() throws SQLException {
//        boolean isRequiredApproval = false;
        poJSON = new JSONObject();
//
//        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
//            isRequiredApproval = true;
//        }
        poJSON.put("result", "success");
//        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

}
