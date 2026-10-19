/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.model.Model_Model;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.model.Model_Model_Variant_Insurance;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_Quotation_Payment extends Model {

    private String psBrand, psModel;

    // reference objects
    Model_Model_Variant poModelVariant;
    Model_Brand poBrand;
    Model_Model poModel;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateObject("nSRPAmntx", 0.00);
            poEntity.updateObject("nDownPayx", 0.00);
            poEntity.updateObject("nDownPctx", 0.00);
            poEntity.updateObject("nRsrvAmtx", 0.00);
            poEntity.updateObject("nFinAmntx", 0.00);
            poEntity.updateObject("nDuration", 0);
            poEntity.updateObject("nRateValx", 0.0000);
            poEntity.updateObject("nMonthlyx", 0.00);
            poEntity.updateObject("nTotAmntx", 0.00);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            poEntity.updateNull("sModified");
            poEntity.updateNull("dModified");
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sQotPayID";
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setQuotationPaymentId(String quotationPaymentId) {
        return setValue("sQotPayID", quotationPaymentId);
    }

    public String getQuotationPaymentId() {
        return (String) getValue("sQotPayID");
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setVariantId(String variantId) {
        return setValue("sVrntIDxx", variantId);
    }

    public String getVariantId() {
        return (String) getValue("sVrntIDxx");
    }

    public JSONObject setSRPAmount(Double srpAmount) {
        return setValue("nSRPAmntx", srpAmount);
    }

    public Double getSRPAmount() {
        if (getValue("nSRPAmntx") == null || "".equals(getValue("nSRPAmntx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nSRPAmntx").toString());
    }

    public JSONObject setDownPayment(Double downPayment) {
        return setValue("nDownPayx", downPayment);
    }

    public Double getDownPayment() {
        if (getValue("nDownPayx") == null || "".equals(getValue("nDownPayx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nDownPayx").toString());
    }

    public JSONObject setDownPaymentPercent(Double downPaymentPercent) {
        return setValue("nDownPctx", downPaymentPercent);
    }

    public Double getDownPaymentPercent() {
        if (getValue("nDownPctx") == null || "".equals(getValue("nDownPctx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nDownPctx").toString());
    }

    public JSONObject setReservationAmount(Double reservationAmount) {
        return setValue("nRsrvAmtx", reservationAmount);
    }

    public Double getReservationAmount() {
        if (getValue("nRsrvAmtx") == null || "".equals(getValue("nRsrvAmtx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nRsrvAmtx").toString());
    }

    public JSONObject setFinancingAmount(Double financingAmount) {
        return setValue("nFinAmntx", financingAmount);
    }

    public Double getFinancingAmount() {
        if (getValue("nFinAmntx") == null || "".equals(getValue("nFinAmntx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nFinAmntx").toString());
    }

    public JSONObject setDuration(int duration) {
        return setValue("nDuration", duration);
    }

    public int getDuration() {
        if (getValue("nDuration") == null || "".equals(getValue("nDuration"))) {
            return 0;
        }
        return (int) getValue("nDuration");
    }

    public JSONObject setRateValue(Double rateValue) {
        return setValue("nRateValx", rateValue);
    }

    public Double getRateValue() {
        if (getValue("nRateValx") == null || "".equals(getValue("nRateValx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nRateValx").toString());
    }

    public JSONObject setMonthlyAmount(Double monthlyAmount) {
        return setValue("nMonthlyx", monthlyAmount);
    }

    public Double getMonthlyAmount() {
        if (getValue("nMonthlyx") == null || "".equals(getValue("nMonthlyx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nMonthlyx").toString());
    }

    public JSONObject setTotalAmount(Double totalAmount) {
        return setValue("nTotAmntx", totalAmount);
    }

    public Double getTotalAmount() {
        if (getValue("nTotAmntx") == null || "".equals(getValue("nTotAmntx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nTotAmntx").toString());
    }

    public JSONObject setRecordStatus(boolean recordStatus) {
        return setValue("cRecdStat", recordStatus ? "1" : "0");
    }

    public boolean getRecordStatus() {
        return ((String) getValue("cRecdStat")).equals("1");
    }

    public JSONObject setModifiedBy(String modifiedBy) {
        return setValue("sModified", modifiedBy);
    }

    public String getModifiedBy() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, false, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    public void setBrandId(String brand) {
        psBrand = brand;
    }

    public String getBrandId() {
        String id = (String) (getValue("sVrntIDxx") == null ? "" : getValue("sVrntIDxx"));

        if (!"".equals(id)) {
            try {
                psBrand = ModelVariant().Model().getBrandId();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (GuanzonException e) {
                throw new RuntimeException(e);
            }
        }

        return psBrand == null ? "" : psBrand;
    }

    public void setModelId(String model) {
        psModel = model;
    }

    public String getModelId() {
        String id = (String) (getValue("sVrntIDxx") == null ? "" : getValue("sVrntIDxx"));

        if (!"".equals(id)) {
            try {
                psModel = ModelVariant().getModelId();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (GuanzonException e) {
                throw new RuntimeException(e);
            }
        }

        return psModel == null ? "" : psModel;
    }

    // reference object models
    public Model_Model_Variant ModelVariant() throws SQLException, GuanzonException {
        if (poModelVariant == null) {
            poModelVariant = new ParamModels(poGRider).ModelVariant();
        }

        String id = (String) (getValue("sVrntIDxx") == null ? "" : getValue("sVrntIDxx"));

        if (!"".equals(id)) {
            if (poModelVariant.getEditMode() == EditMode.READY
                    && poModelVariant.getVariantId().equals(id)) {
                return poModelVariant;
            } else {
                poJSON = poModelVariant.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poModelVariant;
                } else {
                    poModelVariant.initialize();
                    return poModelVariant;
                }
            }
        } else {
            poModelVariant.initialize();
            return poModelVariant;
        }
    }

    public Model_Brand Brand() throws SQLException, GuanzonException {
        if (poBrand == null) {
            poBrand = new ParamModels(poGRider).Brand();
        }

        String id = getBrandId();

        if (!"".equals(id)) {
            if (poBrand.getEditMode() == EditMode.READY
                    && poBrand.getBrandId().equals(id)) {
                return poBrand;
            } else {
                poJSON = poBrand.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poBrand;
                } else {
                    poBrand.initialize();
                    return poBrand;
                }
            }
        } else {
            poBrand.initialize();
            return poBrand;
        }
    }


    public Model_Model Model() throws SQLException, GuanzonException {
        if (poModel == null) {
            poModel = new ParamModels(poGRider).Model();
        }

        String id = getModelId();

        if (!"".equals(id)) {
            if (poModel.getEditMode() == EditMode.READY
                    && poModel.getModelId().equals(id)) {
                return poModel;
            } else {
                poJSON = poModel.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poModel;
                } else {
                    poModel.initialize();
                    return poModel;
                }
            }
        } else {
            poModel.initialize();
            return poModel;
        }
    }
    // end - reference object models
}

