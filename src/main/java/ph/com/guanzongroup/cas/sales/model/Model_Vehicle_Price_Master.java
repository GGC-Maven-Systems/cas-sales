                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
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
public class Model_Vehicle_Price_Master extends Model {

    Model_Brand poBrand;
    Model_Model poModel;
    Model_Model_Variant poModelVariant;
    Model_Model_Variant_Insurance poModelVariantInsurance;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateNull("dModified");
            poEntity.updateObject("nSRPAmntx",0.00);
            poEntity.updateObject("nPriceYrx",0);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID2 = "sValidIDx";
            ID = "sPriceIDx";

            //initialize reference objects
            
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

    public JSONObject setPriceId(String priceId) {
        return setValue("sPriceIDx", priceId);
    }

    public String getPriceId() {
        return (String) getValue("sPriceIDx");
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

    public JSONObject setPriceYear(Integer priceYear) {
        return setValue("nPriceYrx", priceYear);
    }

    public Integer getPriceYear() {
        if (getValue("nPriceYrx") == null || "".equals(getValue("nPriceYrx"))) {
            return 0;
        }
        return Integer.valueOf(getValue("nPriceYrx").toString());
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
    
    String psBrandId = "";
    public void setBrandId(String brandId){
        psBrandId = brandId;
    }
    
    public String getBrandId(){
        return psBrandId;
    }
    
    String psModelId = "";
    public void setModelId(String modelId){
        psModelId = modelId;
    }
    
    public String getModelId(){
        return psModelId;
    }
    
    
    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
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
    
    public Model_Model_Variant_Insurance ModelVariantInsurance() throws SQLException, GuanzonException {
        if (poModelVariantInsurance == null) {
            poModelVariantInsurance = new ParamModels(poGRider).ModelVariantInsurance();
        }
        
        String id = (String) (getValue("sVrntIDxx") == null ? "" : getValue("sVrntIDxx"));
        
        if (!"".equals(id)) {
            if (poModelVariantInsurance.getEditMode() == EditMode.READY
                    && poModelVariantInsurance.getVariantId().equals(id)) {
                return poModelVariantInsurance;
            } else {
                poJSON = poModelVariantInsurance.openRecord(id);

                if ("success".equals((String) poJSON.get("result"))) {
                    return poModelVariantInsurance;
                } else {
                    poModelVariantInsurance.initialize();
                    return poModelVariantInsurance;
                }
            }
        } else {
            poModelVariantInsurance.initialize();
            return poModelVariantInsurance;
        }
        
    }
    
    public Model_Brand Brand() throws GuanzonException, SQLException {
        if (poBrand == null) {
            poBrand = new ParamModels(poGRider).Brand();
        }

        if (!"".equals((String) getValue("sVrntIDxx")) && (String) getValue("sVrntIDxx") != null) {
            setBrandId(ModelVariant().Model().getBrandId());
        }
        
        String id = (String) (getBrandId() == null ? "" : getBrandId());

        if (!"".equals(id)) {
            if (poBrand.getEditMode() == EditMode.READY
                    && poBrand.getBrandId().equals(id)) {
                return poBrand;
            } else {
                if (ReferenceCache.tryLoad("Brand", id, poBrand)) {
                    return poBrand;
                }

                poJSON = poBrand.openRecord(id);
                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store("Brand", id, poBrand);
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
    
    public Model_Model Model() throws GuanzonException, SQLException {
        if (poModel == null) {
            poModel = new ParamModels(poGRider).Model();
        }

        if (!"".equals((String) getValue("sVrntIDxx")) && (String) getValue("sVrntIDxx") != null) {
            setModelId(ModelVariant().getModelId());
        }
        
        String id = (String) (getModelId() == null ? "" : getModelId());

        if (!"".equals(id)) {
            if (poModel.getEditMode() == EditMode.READY
                    && poModel.getBrandId().equals(id)) {
                return poModel;
            } else {
                if (ReferenceCache.tryLoad("Model", id, poModel)) {
                    return poModel;
                }

                poJSON = poModel.openRecord(id);
                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store("Model", id, poModel);
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
}

