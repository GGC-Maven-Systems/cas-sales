/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Arsiela
 */
public class Model_Vehicle_AddOn_Master extends Model {

    Model_Model_Variant poModelVariant;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateNull("dModified");
            poEntity.updateObject("nAmountxx", 0.00);
            poEntity.updateObject("nSRPAmntx",0.00);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);

            // end - assign default values
            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID2 = "sValidIDx";
            ID = "sAddOnIDx";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poModelVariant = model.ModelVariant();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setValidityId(String validityId) {
        return setValue("sValidIDx", validityId);
    }

    public String getValidityId() {
        return (String) getValue("sValidIDx");
    }

    public JSONObject setAddOnId(String addOnId) {
        return setValue("sAddOnIDx", addOnId);
    }

    public String getAddOnId() {
        return (String) getValue("sAddOnIDx");
    }

    public JSONObject setVariantId(String variantId) {
        return setValue("sVrntIDxx", variantId);
    }

    public String getVariantId() {
        return (String) getValue("sVrntIDxx");
    }

    public JSONObject setAddOnType(String addOnType) {
        return setValue("sAddTypex", addOnType);
    }

    public String getAddOnType() {
        return (String) getValue("sAddTypex");
    }

    public JSONObject setAmount(Double amount) {
        return setValue("nAmountxx", amount);
    }

    public Double getAmount() {
        if (getValue("nAmountxx") == null || "".equals(getValue("nAmountxx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nAmountxx").toString());
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

    public JSONObject setTimestamp(Date timestamp) {
        return setValue("dTimeStmp", timestamp);
    }

    public Date getTimestamp() {
        return (Date) getValue("dTimeStmp");
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
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    public Model_Model_Variant ModelVariant()
            throws SQLException,
            GuanzonException {
        if (!"".equals((String) getValue("sVrntIDxx"))) {
            if (poModelVariant.getEditMode() == EditMode.READY
                    && poModelVariant.getVariantId().equals((String) getValue("sVrntIDxx"))) {
                return poModelVariant;
            } else {
                poJSON = poModelVariant.openRecord((String) getValue("sVrntIDxx"));

                if ("success".equals((String) poJSON.get("result"))) {
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
}
