package ph.com.guanzongroup.cas.sales.validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.MatrixAuthChecker;
import org.guanzon.appdriver.agent.MatrixAuthManager;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import org.guanzon.cas.inv.warehouse.status.DeliveryScheduleStatus;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Master;

/**
 *
 * @author MNV
 */
public class MCSales_General implements GValidator {

    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;

    Model_Sales_Master poMaster;
    private static final String SOURCE_CD = "Sale";                 // same value as MCSales.SOURCE_CODE
    private static final String MATRIX_GROUP = "MC SALES MATRIX";
    private double pnTotalDiscount = 0;                              // peso amount, passed in from MCSales

    @Override
    public void setOthers(ArrayList<Object> value) {
        if (value != null && !value.isEmpty() && value.get(0) instanceof Number) {
            pnTotalDiscount = ((Number) value.get(0)).doubleValue();
        }
    }

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
        poMaster = (Model_Sales_Master) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDetail(ArrayList<Object> value) {
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
            Logger.getLogger(MCSales_General.class.getName()).log(Level.SEVERE, null, ex);
        }

        return poJSON;
    }

    private JSONObject validateNew() throws SQLException {
        poJSON = new JSONObject();
        boolean isRequiredApproval = false;

        if (poMaster.getTransactionDate() == null) {
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

        if (poMaster.getTransactionDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        // ---------- authorization matrix ----------
        MatrixAuthManager loMatrix = new MatrixAuthManager(poGRider, SOURCE_CD, poMaster.getTransactionNo());

        if (pnTotalDiscount > 0) {
            String lsValue = BigDecimal.valueOf(pnTotalDiscount).setScale(2, RoundingMode.HALF_UP).toPlainString();

            String lsRemarks = poGRider.getBranchCode()
                    + "/" + poMaster.getTransactionNo()
                    + ";" + SQLUtil.dateFormat(poMaster.getTransactionDate(), "yyyy-MM-dd")
                    + "; Discount: " + lsValue;

            String lsAuthCode = loMatrix.getAuthType(MATRIX_GROUP, lsValue, "");
            if (!lsAuthCode.isEmpty()) {                       // the PO validator does not guard this, keep the guard
                loMatrix.addAuthRequest(lsAuthCode, getMatrixArea(lsAuthCode), "", lsRemarks);
            }
        }

        if (loMatrix.hasAuthRequest()) {
            try {
                poJSON = loMatrix.processAuth();               // returns result + "matrix"
            } catch (GuanzonException ex) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", ex.getMessage());
                return poJSON;
            }
            // the matrix replaces the manager login prompt
            poJSON.put("isRequiredApproval", Boolean.FALSE);
            return poJSON;
        }

        // no matrix tier applies: keep the existing behavior
        boolean isRequiredApproval = poGRider.getUserLevel() <= UserRight.ENCODER;
        poJSON.put("result", "success");
        poJSON.put("isRequiredApproval", isRequiredApproval);
        return poJSON;
    }

    /**
     * Area of the branch that made the sale; blank for tiers whose approvers
     * are not set per area.
     */
    private String getMatrixArea(String authType) throws SQLException {
        String lsSQL = "SELECT IFNULL(b.sAreaCode, '') sAreaCode"
                + " FROM Branch_Others b"
                + " WHERE b.sBranchCD = LEFT(" + SQLUtil.toSQL(poMaster.getTransactionNo()) + ", 4)";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        String lsArea = "";
        if (loRS.next()) {
            lsArea = loRS.getString("sAreaCode");
        }
        MiscUtil.close(loRS);

        if (lsArea.isEmpty()) {
            return "";
        }

        lsSQL = "SELECT COUNT(*) nCount FROM xxxSysAuth_Matrix_Detail"
                + " WHERE sAuthType = " + SQLUtil.toSQL(authType)
                + " AND IFNULL(sAreaCode, '') = " + SQLUtil.toSQL(lsArea)
                + " AND cRecdStat = '1'";
        loRS = poGRider.executeQuery(lsSQL);
        boolean lbHas = loRS.next() && loRS.getInt("nCount") > 0;
        MiscUtil.close(loRS);
        return lbHas ? lsArea : "";
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
