package ph.com.guanzongroup.cas.sales.validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.MatrixAuthManager;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.constant.SalesTransactionStatic;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Giveaways;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Insurance;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Registration;

/**
 *
 * @author MNV
 */
public class MCSales_General implements GValidator {

    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;

    Model_Sales_Master poMaster;
    ArrayList<Model_Sales_Detail> paDetail;
    ArrayList<Model_Sales_Giveaways> paGiveAway;
    ArrayList<Model_Sales_Registration> paRegistration;
    ArrayList<Model_Sales_Insurance> paInsurance;
    private static final String SOURCE_CD = "Sale";                 // same value as MCSales.SOURCE_CODE
    private static final String MATRIX_GROUP = "MC SALES MATRIX";
    private double pnTotalDiscount = 0;                              // peso amount, passed in from MCSales

    @Override
    public void setOthers(ArrayList<Object> value) {
        pnTotalDiscount = 0;
        paGiveAway = new ArrayList<>();
        paRegistration = new ArrayList<>();
        paInsurance = new ArrayList<>();

        if (value == null) {
            return;
        }
        for (Object loItem : value) {
            addOther(loItem);
        }
    }

    private void addOther(Object foItem) {
        if (foItem == null) {
            return;
        }
        if (foItem instanceof Number) {
            pnTotalDiscount = ((Number) foItem).doubleValue();
        } else if (foItem instanceof Model_Sales_Giveaways) {
            paGiveAway.add((Model_Sales_Giveaways) foItem);
        } else if (foItem instanceof Model_Sales_Registration) {
            paRegistration.add((Model_Sales_Registration) foItem);
        } else if (foItem instanceof Model_Sales_Insurance) {
            paInsurance.add((Model_Sales_Insurance) foItem);
        } else if (foItem instanceof Collection<?>) {
            for (Object loChild : (Collection<?>) foItem) {
                addOther(loChild);
            }
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
        paDetail = (ArrayList<Model_Sales_Detail>) (ArrayList<?>) value;
    }

    public void setGiveAway(ArrayList<Object> value) {
        paGiveAway = (ArrayList<Model_Sales_Giveaways>) (ArrayList<?>) value;
    }

    public void setRegistration(ArrayList<Object> value) {
        paRegistration = (ArrayList<Model_Sales_Registration>) (ArrayList<?>) value;
    }

    public void setInsurance(ArrayList<Object> value) {
        paInsurance = (ArrayList<Model_Sales_Insurance>) (ArrayList<?>) value;
    }

    @Override
    public JSONObject validate() {
        try {
            switch (psTranStat) {
                case SalesTransactionStatic.OPEN:
                    return validateNew();
                case SalesTransactionStatic.CONFIRMED:
                    return validateConfirmed();
                case SalesTransactionStatic.PAID:
                    return validatePosted();
                case SalesTransactionStatic.CANCELLED:
                    return validateCancelled();
                case SalesTransactionStatic.REPLACED:
                    return validateConfirmed();
                case SalesTransactionStatic.VOID:
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

        if (poMaster.getClientID() == null || poMaster.getClientID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Empty Customer Name Detected.");
            return poJSON;
        }

        if (poMaster.getSalesman() == null || poMaster.getSalesman().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Empty Salesman Detected.");
            return poJSON;
        }

        int lnDetailCount = 0;
        for (int lnCtr = 0; lnCtr < paDetail.size(); lnCtr++) {
            if (paDetail.get(lnCtr).getStockID() != null
                    && !paDetail.get(lnCtr).getStockID().isEmpty()) {
                System.out.println("SERIAL : " + paDetail.get(lnCtr).getSerialID());
                if (paDetail.get(lnCtr).getSerialID() == null
                        || paDetail.get(lnCtr).getSerialID().isEmpty()) {

                    poJSON.put("result", "error");
                    poJSON.put("message", "Serial is not set. Row = " + (lnCtr + 1));
                    return poJSON;

                }
                lnDetailCount++;
            }
        }
        if (lnDetailCount <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Detail is not set.");
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

        if (poMaster.getClientID() == null || poMaster.getClientID().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Empty Customer Name Detected.");
            return poJSON;
        }

        if (poMaster.getSalesman() == null || poMaster.getSalesman().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Empty Customer Name Detected.");
            return poJSON;
        }

        for (int lnCtr = 0; lnCtr < paGiveAway.size(); lnCtr++) {
            if (paGiveAway.get(lnCtr).getStockID() != null
                    && !paGiveAway.get(lnCtr).getStockID().isEmpty()) {

                if (paGiveAway.get(lnCtr).getQuantity() <= 0) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Giveaway quantity is not set. Row = " + (lnCtr + 1));
                    return poJSON;
                }
            }
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
//        boolean isRequiredApproval = poGRider.getUserLevel() <= UserRight.ENCODER;
        poJSON.put("result", "success");
//        poJSON.put("isRequiredApproval", isRequiredApproval);
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
