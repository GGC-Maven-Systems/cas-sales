package ph.com.guanzongroup.cas.sales.validator;

import org.guanzon.appdriver.agent.MatrixAuthManager;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.iface.GValidator;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Master;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationVersionStatic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Validator for Sales Quotation Version.
 *
 * On CONFIRM it compares the version total against its parent quotation
 * (Sales_Quotation_Version_Master.sReferNox = Sales_Quotation_Master.sTransNox)
 * and raises a matrix authorization request when the version total is lower
 * than the parent's.
 */
public class Sales_Qoutation_Validator_MC implements GValidator {

    private static final double TOLERANCE = 0.005;

    // ASSUMPTION: matrix name registered for this source code.


    MatrixAuthManager poMatrix;
    GRiderCAS poGrider;
    String psTranStat;
    JSONObject poJSON;

    String SOURCE_CD = SalesQoutationVersionStatic.SOURCE_CODE;
    Model_Sales_Quotation_Version_Master poMaster;
    ArrayList<Model_Sales_Quotation_Version_Detail> poDetail = new ArrayList<>();

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
        poMaster = (Model_Sales_Quotation_Version_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for (int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++) {
            poDetail.add((Model_Sales_Quotation_Version_Detail) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        switch (psTranStat) {
            case SalesQoutationVersionStatic.OPEN:
                return validateNew();
            case SalesQoutationVersionStatic.CONFIRMED:
                try {
                    return validateConfirmed();
                } catch (SQLException ex) {
                    poJSON = new JSONObject();
                    poJSON.put("result", "error");
                    poJSON.put("message", ex.getMessage());
                    return poJSON;
                }
            default:
                poJSON = new JSONObject();
                poJSON.put("result", "success");
                return poJSON;
        }
    }

    private JSONObject validateNew() {
        poJSON = new JSONObject();

        if (poMaster.getTransactionNo() == null || "".equals(poMaster.getTransactionNo())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Please specify transaction no.");
            return poJSON;
        }

        if (poMaster.getParentId() == null || "".equals(poMaster.getParentId())) {
            poJSON.put("result", "error");
            poJSON.put("message", "Parent quotation must not be empty.");
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }


    private static final String DETAIL_TABLE   = "Sales_Quotation_Version_Detail";
    private static final String DETAIL_SRP_COL = "nUnitPrce";
    private static final String DETAIL_QTY_COL = "nQuantity";

    /**
     * Validates the discount of the version and, when a discount exists, creates
     * the matrix approval request. The returned JSON carries the "matrix" array
     * (from MatrixAuthManager.processAuth) when an authorization is required.
     */

    private JSONObject validateConfirmed() throws SQLException {
        poJSON = new JSONObject();
        poMatrix = new MatrixAuthManager(poGrider, SOURCE_CD, poMaster.getTransactionNo());


        // ---- 1. version master + parent ------------------------------------
        String lsSQL = "SELECT"
                + "  vm.sTransNox"
                + ", pm.sTransNox AS sParentNo"
                + ", IFNULL(vm.nDiscAmtx, 0) AS nDisAmtx"
                + ", IFNULL(vm.nAddDiscx, 0) AS nAddDiscx"
                + " FROM Sales_Quotation_Version_Master vm"
                + " LEFT JOIN Sales_Quotation_Master pm ON pm.sTransNox = vm.sParentID"
                + " WHERE vm.sTransNox = " + SQLUtil.toSQL(poMaster.getTransactionNo());

        System.out.println("Execute SQL : " + lsSQL);

        boolean lbHasParent = false;
        double lnDiscAmount = 0.00;
        double lnAddDiscount = 0.00;

        ResultSet loRS = poGrider.executeQuery(lsSQL);
        try {
            if (loRS.next()) {
                lbHasParent = loRS.getString("sParentNo") != null;
                lnDiscAmount = loRS.getDouble("nDisAmtx");
                lnAddDiscount = loRS.getDouble("nAddDiscx");
            }
        } finally {
            MiscUtil.close(loRS);
        }

        if (!lbHasParent) {
            poJSON.put("result", "error");
            poJSON.put("message", "Parent quotation was not found.");
            return poJSON;
        }

        // ---- 2. basic sanity of the discount values ------------------------
        if (lnDiscAmount < 0.00) {
            poJSON.put("result", "error");
            poJSON.put("message", "Discount amount must not be negative.");
            return poJSON;
        }

        if (lnAddDiscount < 0.00) {
            poJSON.put("result", "error");
            poJSON.put("message", "Additional discount must not be negative.");
            return poJSON;
        }

        // ---- 3. get quotation details and identify big bikes ---------------
        lsSQL = "SELECT"
                + " d.sStockIDx"
                + ", (d." + DETAIL_SRP_COL + " * d." + DETAIL_QTY_COL + ") AS nGrossAmt"
                + ", i.sCategCd2"
                + " FROM " + DETAIL_TABLE + " d"
                + " LEFT JOIN Inventory i ON i.sStockIDx = d.sStockIDx"
                + " WHERE d.sTransNox = "
                + SQLUtil.toSQL(poMaster.getTransactionNo());

        System.out.println("Execute SQL : " + lsSQL);

        loRS = poGrider.executeQuery(lsSQL);

        double lnGross = 0.00;
        double lnBigBikeGross = 0.00;
        int lnDetailCount = 0;
        int lnBigBikeCount = 0;

        try {
            while (loRS.next()) {
                lnDetailCount++;

                String lsStockID = loRS.getString("sStockIDx");
                String lsCategory = loRS.getString("sCategCd2");
                double lnDetailGross = loRS.getDouble("nGrossAmt");

                lnGross += lnDetailGross;

                if (SalesQoutationVersionStatic.BIG_BIKE.equals(lsCategory)) {
                    lnBigBikeGross += lnDetailGross;
                    lnBigBikeCount++;

                    System.out.println("Big bike detected:"
                            + " StockID=" + lsStockID
                            + ", Category=" + lsCategory
                            + ", Gross=" + lnDetailGross);
                } else {
                    System.out.println("Non-big-bike detail:"
                            + " StockID=" + lsStockID
                            + ", Category=" + lsCategory
                            + ", Gross=" + lnDetailGross);
                }
            }
        } finally {
            MiscUtil.close(loRS);
        }

        if (lnDetailCount == 0 || lnGross <= 0.00) {
            poJSON.put("result", "error");
            poJSON.put("message", "Cannot apply a discount to a quotation with no detail amount.");
            return poJSON;
        }

        System.out.println("lnDetailCount: " + lnDetailCount);
        System.out.println("lnBigBikeCount: " + lnBigBikeCount);
        System.out.println("lnGross: " + lnGross);
        System.out.println("lnBigBikeGross: " + lnBigBikeGross);
        System.out.println("lnDiscAmount: " + lnDiscAmount);
        System.out.println("lnAddDiscount: " + lnAddDiscount);

        // no discount -> nothing to authorize
        if (lnDiscAmount == 0.00 && lnAddDiscount == 0.00) {
            poJSON.put("result", "success");
            poJSON.put("message", "");
            return poJSON;
        }

        // ---- 4. total discount (amount and percent) -------------------------
        // nDiscAmtx stores the discount amount.
        double lnDiscPercent = round2((lnDiscAmount / lnGross) * 100.00);

        // nAddDiscx stores the additional discount amount.
        double lnAddDiscPct = round2((lnAddDiscount / lnGross) * 100.00);

        double lnTotalDiscAmt = round2(lnDiscAmount + lnAddDiscount);
        double lnTotalDiscPct = round2(lnDiscPercent + lnAddDiscPct);

        System.out.println("lnDiscPercent: " + lnDiscPercent);
        System.out.println("lnAddDiscPct: " + lnAddDiscPct);
        System.out.println("lnTotalDiscAmt: " + lnTotalDiscAmt);
        System.out.println("lnTotalDiscPct: " + lnTotalDiscPct);

        // ---- 5. matrix lookup ----------------------------------------------
        // ---- 5. matrix lookup ----------------------------------------------
// Use a separate authorization matrix for big bikes.
        String lsMatrixName;

        if (lnBigBikeCount > 0) {
            lsMatrixName = SalesQoutationVersionStatic.MATRIX_BIG_BIKE_NAME;
        } else {
            lsMatrixName = SalesQoutationVersionStatic.MATRIX_REGULAR_BIKE_NAME;
        }

        System.out.println("Selected Matrix: " + lsMatrixName);
        System.out.println("Total Discount Amount: " + lnTotalDiscAmt);

        String lsAuthCode = poMatrix.getAuthType(
                lsMatrixName,
                String.valueOf(lnTotalDiscAmt),
                ""
        );

        if (lsAuthCode == null || lsAuthCode.isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "No approval matrix is defined for a total discount of "
                    + lnTotalDiscAmt + " using matrix " + lsMatrixName + ".");
            return poJSON;
        }

        if (lsAuthCode == null || lsAuthCode.isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "No approval matrix is defined for a total discount of "
                    + lnTotalDiscAmt + ".");
            return poJSON;
        }

        String lsRemarks = poMaster.getBranchCode()
                + "/" + poMaster.getTransactionNo()
                + ";" + SQLUtil.dateFormat(poMaster.getTransactionDate(), "yyyy-MM-dd")
                + ";gross=" + lnGross
                + ";bigBikeGross=" + lnBigBikeGross
                + ";bigBikeCount=" + lnBigBikeCount
                + ";discAmt=" + lnDiscAmount
                + ";disc%=" + lnDiscPercent
                + ";addDisc=" + lnAddDiscount
                + ";addDisc%=" + lnAddDiscPct
                + ";totalDisc=" + lnTotalDiscAmt
                + ";totalDisc%=" + lnTotalDiscPct
                + ";" + poMaster.getRemarks();

        poMatrix.addAuthRequest(lsAuthCode, "", "", lsRemarks);

        // ---- 6. create / load the approval request --------------------------
        try {
            poJSON = poMatrix.processAuth();

            if ("error".equals(poJSON.get("result"))) {
                return poJSON;
            }
        } catch (GuanzonException ex) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());
            return poJSON;
        }

        return poJSON;
    }

    private static double round2(double fnValue) {
        return Math.round(fnValue * 100.0) / 100.0;
    }
}