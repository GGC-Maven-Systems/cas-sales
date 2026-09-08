/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.model.Model_Inv_Type;
import org.guanzon.cas.parameter.model.Model_Model;
import org.guanzon.cas.parameter.model.Model_Tax_Code;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.constant.Sales_Reservation_Static;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author User
 */
public class Model_Sales_Reservation_Detail extends Model {
    Model_Brand poBrand;
    Model_Model poModel;
    Model_Inventory poInventory;
    Model_Inv_Type poInvType;
    Model_Tax_Code poTaxCode;
    String InvType = "";

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", Sales_Reservation_Static.DefaultValues.default_zero_integer);
            poEntity.updateObject("nQuantity", Sales_Reservation_Static.DefaultValues.default_zero_quantity_double);
            poEntity.updateObject("nUnitPrce", Sales_Reservation_Static.DefaultValues.default_zero_amount_double);
            poEntity.updateObject("nMinDownx", Sales_Reservation_Static.DefaultValues.default_zero_amount_double);
            poEntity.updateObject("cClassify", Sales_Reservation_Static.DefaultValues.default_classify_string);
            poEntity.updateObject("nApproved", Sales_Reservation_Static.DefaultValues.default_zero_quantity_double);
            poEntity.updateObject("nIssuedxx", Sales_Reservation_Static.DefaultValues.default_zero_quantity_double);
            poEntity.updateObject("nCancelld", Sales_Reservation_Static.DefaultValues.default_zero_quantity_double);
            poEntity.updateObject("cReversed", Sales_Reservation_Static.DefaultValues. default_reversed_plus);

            //end - assign default values
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            InvModels inv = new InvModels(poGRider);
            poInventory = inv.Inventory();
            
            ParamModels model = new ParamModels(poGRider);
            poTaxCode = model.TaxCode();
            poInvType = model.InventoryType();
            poBrand = model.Brand();
            poModel= model.Model();

            //end - initialize reference objects
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public String getNextCode() {
        return "";
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
        return (int) getValue("nEntryNox");
    }

    public JSONObject setStockID(String stockID) {
        return setValue("sStockIDx", stockID);
    }

    public String getStockID() {
        return (String) getValue("sStockIDx");
    }
    
    public JSONObject setQuantity(double quantity) {
        return setValue("nQuantity", quantity);
    }

    public double getQuantity() {
        return Double.parseDouble(String.valueOf(getValue("nQuantity")));
    }

    public JSONObject setUnitPrice(double unitPrice) {
        return setValue("nUnitPrce", unitPrice);
    }

    public double getUnitPrice() {
        return Double.parseDouble(String.valueOf(getValue("nUnitPrce")));
    }

    public JSONObject setMinimumDown(double minimumDown) {
        return setValue("nMinDownx", minimumDown);
    }

    public double getMinimumDown() {
        return Double.parseDouble(String.valueOf(getValue("nMinDownx")));
    }

    public JSONObject setClassify(String classify) {
        return setValue("cClassify", classify);
    }

    public String getClassify() {
        return (String) getValue("cClassify");
    }
    
    public JSONObject setApproved(double approved) {
        return setValue("nApproved", approved);
    }

    public double getApproved() {
        return Double.parseDouble(String.valueOf(getValue("nApproved")));
    }
    
    public JSONObject setIssued(double issued) {
        return setValue("nIssuedxx", issued);
    }

    public double getIssued() {
        return Double.parseDouble(String.valueOf(getValue("nIssuedxx")));
    }
    
    public JSONObject setCancelled(double cancelled) {
        return setValue("nCancelld", cancelled);
    }

    public double getCancelled() {
        return Double.parseDouble(String.valueOf(getValue("nCancelld")));
    }
    
    public JSONObject setNotes(String notes) {
        return setValue("sNotesxxx", notes);
    }

    public String getNotes() {
        return (String) getValue("sNotesxxx");
    }
        
    
    public JSONObject isReversed(boolean isProcessed) {
        return setValue("cReversed", isProcessed ? "+" : "-");
    }

    public boolean isReversed() {
        return ((String) getValue("cReversed")).equals("+");
    }
    
    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    public JSONObject setBrandId(String brandId) {
        return poBrand.setBrandId(brandId);
    }

    public String getBrandId() {
        return poBrand.getBrandId();
    }
    
    public JSONObject setModelID(String modelID) {
        return poModel.setModelId(modelID);
    }

    public String getModelID() {
        return poModel.getModelId();
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

    public Model_Tax_Code TaxCode() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sTaxCodex"))) {
            if (poTaxCode.getEditMode() == EditMode.READY
                    && poTaxCode.getTaxCode().equals((String) getValue("sTaxCodex"))) {
                return poTaxCode;
            } else {
                poJSON = poTaxCode.openRecord((String) getValue("sTaxCodex"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poTaxCode;
                } else {
                    poTaxCode.initialize();
                    return poTaxCode;
                }
            }
        } else {
            poTaxCode.initialize();
            return poTaxCode;
        }
    }

    public Model_Inv_Type InvType() throws SQLException, GuanzonException {
        if (!"".equals(InvType)) {
            if (poInvType.getEditMode() == EditMode.READY
                    && poInvType.getInventoryTypeId().equals(InvType)) {
                return poInvType;
            } else {
                poJSON = poInvType.openRecord(InvType);

                if ("success".equals((String) poJSON.get("result"))) {
                    return poInvType;
                } else {
                    poInvType.initialize();
                    return poInvType;
                }
            }
        } else {
            poInvType.initialize();
            return poInvType;
        }
    }
    
    public Model_Brand Brand() throws GuanzonException, SQLException {
        if (!"".equals(getBrandId())) {
            poJSON = poBrand.openRecord(getBrandId());
            if ("success".equals((String) poJSON.get("result"))) {
                return poBrand;
            } else {
                poBrand.initialize();
                return poBrand;
            }
        } else {
            poBrand.initialize();
            return poBrand;
        }
    }
        
    public Model_Model Model() throws GuanzonException, SQLException {
        if (!"".equals(getModelID())) {
            poJSON = poModel.openRecord(getBrandId());
            if ("success".equals((String) poJSON.get("result"))) {
                return poModel;
            } else {
                poModel.initialize();
                return poModel;
            }

        } else {
            poModel.initialize();
            return poModel;
        }
    }
}
