/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.cas.inv.model.Model_Inv_Master;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.model.Model_Category_Level2;
import org.guanzon.cas.parameter.model.Model_Color;
import org.guanzon.cas.parameter.model.Model_Model;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_Sales_Inquiry_Detail extends Model {
    
    String psBrandId = "";
    String psCategory = "";
    
    //reference objects
    Model_Brand poBrand;
    Model_Model poModel;
    Model_Model_Variant poModelVariant;
    Model_Color poColor;
    Model_Inventory poInventory;
    Model_Inv_Master poInvMaster;
    Model_Category_Level2 poCategory;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("dModified", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nPriority", 0);
            poEntity.updateObject("nSelPrice", 0.0000);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            //initialize reference objects
//            ParamModels model = new ParamModels(poGRider);
            poBrand = new ParamModels(poGRider).Brand();
            poModel = new ParamModels(poGRider).Model();
            poColor = new ParamModels(poGRider).Color();
            poModelVariant = new ParamModels(poGRider).ModelVariant();
            poCategory = new ParamModels(poGRider).Category2();
            
            InvModels invModel = new InvModels(poGRider); 
            poInventory = invModel.Inventory();
            //end - initialize reference objects

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        if (getValue("nEntryNox") == null || "".equals(getValue("nEntryNox"))) {
            return 0;
        }
        return (int) getValue("nEntryNox");
    }

    public JSONObject setPriority(int priority) {
        return setValue("nPriority", priority);
    }

    public int getPriority() {
        if (getValue("nPriority") == null || "".equals(getValue("nPriority"))) {
            return 0;
        }
        return (int) getValue("nPriority");
    }

    public JSONObject setStockId(String stockId) {
        return setValue("sStockIDx", stockId);
    }

    public String getStockId() {
        return (String) getValue("sStockIDx");
    }

    public JSONObject setModelId(String modelId) {
        return setValue("sModelIDx", modelId);
    }

    public String getModelId() {
        return (String) getValue("sModelIDx");
    }

    public JSONObject setModelVarianId(String varianId) {
        return setValue("sVrntIDxx", varianId);
    }

    public String getModelVarianId() {
        return (String) getValue("sVrntIDxx");
    }

    public JSONObject setColorId(String colorId) {
        return setValue("sColorIDx", colorId);
    }

    public String getColorId() {
        return (String) getValue("sColorIDx");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }
    
    public void setBrandId(String brandId){
        psBrandId = brandId;
    }
    
    public String getBrandId(){
        return psBrandId;
    }

    public JSONObject setCategory(String category) {
        return setValue("sCategrCd", category);
    }

    public String getCategory() {
        return (String) getValue("sCategrCd");
    }
    
    public JSONObject setSellPrice(Double sellPrice) {
        return setValue("nSelPrice", sellPrice);
    }

    public Double getSellPrice() {
        if (getValue("nSelPrice") == null || "".equals(getValue("nSelPrice"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nSelPrice").toString());
    }

    @Override
    public String getNextCode() {
        return "";
    }

    //reference object models
    public Model_Brand Brand() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sModelIDx")) && (String) getValue("sModelIDx") != null) {
            psBrandId = Model().getBrandId();
            setBrandId(Model().getBrandId());
        } else if (!"".equals((String) getValue("sStockIDx")) && (String) getValue("sStockIDx") != null) {
            psBrandId = Inventory().getBrandId();
            setBrandId(Inventory().getBrandId());
        }
        
        if (!"".equals(getBrandId())) {
            if (poBrand.getEditMode() == EditMode.READY
                    && getBrandId().equals(poBrand.getBrandId())) {
                return poBrand;
            } else {
                poJSON = poBrand.openRecord(getBrandId());
                if ("success".equals((String) poJSON.get("result"))) {
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
        if (!"".equals((String) getValue("sModelIDx"))) {
            if (poModel.getEditMode() == EditMode.READY
                    && poModel.getModelId().equals((String) getValue("sModelIDx"))) {
                return poModel;
            } else {
                poJSON = poModel.openRecord((String) getValue("sModelIDx"));

                if ("success".equals((String) poJSON.get("result"))) {
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
    
    public Model_Color Color() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sColorIDx"))) {
            if (poColor.getEditMode() == EditMode.READY
                    && poColor.getColorId().equals((String) getValue("sColorIDx"))) {
                return poColor;
            } else {
                poJSON = poColor.openRecord((String) getValue("sColorIDx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poColor;
                } else {
                    poColor.initialize();
                    return poColor;
                }
            }
        } else {
            poColor.initialize();
            return poColor;
        }
    }
    
    public Model_Model_Variant ModelVariant() throws SQLException, GuanzonException {
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
    
    public Model_Inventory Inventory() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sStockIDx"))) {
            if (poInventory.getEditMode() == EditMode.READY
                    && poInventory.getStockId().equals((String) getValue("sStockIDx"))) {
                return poInventory;
            } else {
                poJSON = poInventory.openRecord((String) getValue("sStockIDx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poInventory;
                } else {
                    poInventory.initialize();
                    return poInventory;
                }
            }
        } else {
            poInventory.initialize();
            return poInventory;
        }
    }
    
    public Model_Inv_Master InventoryMaster() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sStockIDx"))) {
            if (poInvMaster.getEditMode() == EditMode.READY
                    && poInvMaster.getStockId().equals((String) getValue("sStockIDx"))) {
                return poInvMaster;
            } else {
                poJSON = poInvMaster.openRecord((String) getValue("sStockIDx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poInvMaster;
                } else {
                    poInvMaster.initialize();
                    return poInvMaster;
                }
            }
        } else {
            poInvMaster.initialize();
            return poInvMaster;
        }
    }
    
    public Model_Category_Level2 Category2() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sCategrCd"))) {
            if (poCategory.getEditMode() == EditMode.READY
                    && poCategory.getCategoryId().equals((String) getValue("sCategrCd"))) {
                return poCategory;
            } else {
                poJSON = poCategory.openRecord((String) getValue("sCategrCd"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCategory;
                } else {
                    poCategory.initialize();
                    return poCategory;
                }
            }
        } else {
            poCategory.initialize();
            return poCategory;
        }
    }
    //end reference object models
}

