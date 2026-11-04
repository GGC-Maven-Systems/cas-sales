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
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.model.Model_Model;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Arsiela
 */
public class Model_Promotion_Applicability extends Model {

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
            poEntity.updateNull("dModified");
            poEntity.updateObject("cExcludex", Logical.NO);
            poEntity.updateObject("cRecdStat", RecordStatus.ACTIVE);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sPrmAppID";
            ID2 = "sPromCode";
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setPromotionApplicabilityId(String promotionApplicabilityId) {
        return setValue("sPrmAppID", promotionApplicabilityId);
    }

    public String getPromotionApplicabilityId() {
        return (String) getValue("sPrmAppID");
    }

    public JSONObject setPromotionCode(String promotionCode) {
        return setValue("sPromCode", promotionCode);
    }

    public String getPromotionCode() {
        return (String) getValue("sPromCode");
    }

    public JSONObject setBrandId(String brandId) {
        return setValue("sBrandIDx", brandId);
    }

    public String getBrandId() {
        return (String) getValue("sBrandIDx");
    }

    public JSONObject setModelId(String modelId) {
        return setValue("sModelIDx", modelId);
    }

    public String getModelId() {
        return (String) getValue("sModelIDx");
    }

    public JSONObject setVariantId(String variantId) {
        return setValue("sVrntIDxx", variantId);
    }

    public String getVariantId() {
        return (String) getValue("sVrntIDxx");
    }

    public JSONObject setExcludeFlag(boolean excludeFlag) {
        return setValue("cExcludex", excludeFlag ? "1" : "0");
    }

    public String getExcludeFlag() {
        return (String) getValue("cExcludex");
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

    public Model_Brand Brand() throws SQLException, GuanzonException {
        if (poBrand == null) {
            poBrand = new ParamModels(poGRider).Brand();
        }

        String id = (String) (getValue("sBrandIDx") == null ? "" : getValue("sBrandIDx"));

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

        String id = (String) (getValue("sModelIDx") == null ? "" : getValue("sModelIDx"));

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
}
