package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inv_Master;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Master;

public class Model_Sales_Detail extends Model {

    private Model_Inventory poInventory;
    private Model_Inv_Serial poInventorySerial;
    private Model_Inv_Master poInventoryMaster;
    private Model_Sales_Promotion_Master poSalesPromo;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata")
                    + "/Model_Sales_Detail.xml", getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            poEntity.updateDouble("nUnitCost", 0.0d);
            poEntity.updateDouble("nUnitPrce", 0.0d);
            poEntity.updateDouble("nDiscount", 0.0d);
            poEntity.updateDouble("nAddDiscx", 0.0d);
            poEntity.updateDouble("nFreightx", 0.0d);
            poEntity.updateDouble("nInsurAdd", 0.0d);
            poEntity.updateDouble("nInsAmtxx", 0.0d);
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateString("cNewStock", "1");
            poEntity.updateString("cWithVATx", "1");
            poEntity.updateNull("sPromoIDx");

            ID = "sTransNox";
            ID2 = "nEntryNox";

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransNo(String value) {
        return setValue("sTransNox", value);
    }

    public String getTransNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setEntryNo(int value) {
        return setValue("nEntryNox", value);
    }

    public int getEntryNo() {
        Object v = getValue("nEntryNox");
        return v == null ? 0 : ((Number) v).intValue();
    }

    public JSONObject setStockID(String value) {
        return setValue("sStockIDx", value);
    }

    public String getStockID() {
        return (String) getValue("sStockIDx");
    }

    public JSONObject setQuantity(int value) {
        return setValue("nQuantity", value);
    }

    public int getQuantity() {
        Object v = getValue("nQuantity");
        return v == null ? 0 : ((Number) v).intValue();
    }

    public JSONObject setIssued(int value) {
        return setValue("nIssuedxx", value);
    }

    public int getIssued() {
        Object v = getValue("nIssuedxx");
        return v == null ? 0 : ((Number) v).intValue();
    }

    public JSONObject setUnitCost(double value) {
        return setValue("nUnitCost", value);
    }

    public double getUnitCost() {
        Object v = getValue("nUnitCost");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setUnitPrice(double value) {
        return setValue("nUnitPrce", value);
    }

    public double getUnitPrice() {
        Object v = getValue("nUnitPrce");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setDiscount(double value) {
        return setValue("nDiscount", value);
    }

    public double getDiscount() {
        Object v = getValue("nDiscount");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setAddDiscount(double value) {
        return setValue("nAddDiscx", value);
    }

    public double getAddDiscount() {
        Object v = getValue("nAddDiscx");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setFreight(double value) {
        return setValue("nFreightx", value);
    }

    public double getFreight() {
        Object v = getValue("nFreightx");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setInsuranceAdd(double value) {
        return setValue("nInsurAdd", value);
    }

    public double getInsuranceAdd() {
        Object v = getValue("nInsurAdd");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setInsuranceAmount(double value) {
        return setValue("nInsAmtxx", value);
    }

    public double getInsuranceAmount() {
        Object v = getValue("nInsAmtxx");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setSerialID(String value) {
        return setValue("sSerialID", value);
    }

    public String getSerialID() {
        return (String) getValue("sSerialID");
    }

    public JSONObject setNewStock(String value) {
        return setValue("cNewStock", value);
    }

    public String getNewStock() {
        return (String) getValue("cNewStock");
    }

    public JSONObject setWarrantyNo(String value) {
        return setValue("sWarrntNo", value);
    }

    public String getWarrantyNo() {
        return (String) getValue("sWarrntNo");
    }

    public JSONObject setKeyCode(String value) {
        return setValue("sKeyCodeX", value);
    }

    public String getKeyCode() {
        return (String) getValue("sKeyCodeX");
    }

    public JSONObject setNotes(String value) {
        return setValue("sNotesxxx", value);
    }

    public String getNotes() {
        return (String) getValue("sNotesxxx");
    }

    public JSONObject setPromoID(String value) {
        return setValue("sPromoIDx", value);
    }

    public String getPromoID() {
        return (String) getValue("sPromoIDx");
    }

    public JSONObject setWithVAT(String value) {
        return setValue("cWithVATx", value);
    }

    public String getWithVAT() {
        return (String) getValue("cWithVATx");
    }

    public JSONObject setModifiedDate(Date value) {
        return setValue("dModified", value);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return ""; // detail rows take their key from the parent transaction
    }

    public Model_Sales_Promotion_Master PromotionMaster()
            throws SQLException, GuanzonException {

        if (poSalesPromo == null) {
            poSalesPromo = new Model_Sales_Promotion_Master();
            poSalesPromo.setApplicationDriver(poGRider);
            poSalesPromo.setXML("Model_Sales_Promotion_Master");
            poSalesPromo.setTableName("Sales_Promotion_Master");
            poSalesPromo.initialize();
        }

        String modelPrimary = (String) (getValue("sPromIDxx") == null
                ? "" : getValue("sPromIDxx"));

        if (!"".equals(modelPrimary)) {

            if (poSalesPromo.getEditMode() == EditMode.READY
                    && poSalesPromo.getPromoID().equals(modelPrimary)) {
                return poSalesPromo;
            }

            if (ReferenceCache.tryLoad(
                    "Sales_Promotion_Master",
                    modelPrimary,
                    poSalesPromo)) {
                return poSalesPromo;
            }

            poJSON = poSalesPromo.openRecord(modelPrimary);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Sales_Promotion_Master",
                        modelPrimary,
                        poSalesPromo);
                return poSalesPromo;
            } else {
                poSalesPromo.initialize();
                return poSalesPromo;
            }

        } else {
            poSalesPromo.initialize();
            return poSalesPromo;
        }
    }

    public Model_Inv_Serial InventorySerial() throws SQLException, GuanzonException {
        if (poInventorySerial == null) {
            poInventorySerial = new InvModels(poGRider).InventorySerial();
        }

        if (!"".equals((String) getValue("sSerialID"))) {
            if (poInventorySerial.getEditMode() == EditMode.READY
                    && poInventorySerial.getStockId().equals((String) getValue("sSerialID"))) {
                return poInventorySerial;
            } else {
                poJSON = poInventorySerial.openRecord((String) getValue("sSerialID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poInventorySerial;
                } else {
                    poInventorySerial.initialize();
                    return poInventorySerial;
                }
            }
        } else {
            poInventorySerial.initialize();
            return poInventorySerial;
        }
    }

    public Model_Inventory Inventory() throws SQLException, GuanzonException {
        if (poInventory == null) {
            poInventory = new InvModels(poGRider).Inventory();
        }

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
        if (poInventoryMaster == null) {
            poInventoryMaster = new InvModels(poGRider).InventoryMaster();
        }

        if (!"".equals((String) getValue("sStockIDx"))) {
            if (poInventoryMaster.getEditMode() == EditMode.READY
                    && poInventoryMaster.getStockId().equals((String) getValue("sStockIDx"))) {
                return poInventoryMaster;
            } else {
                poJSON = poInventoryMaster.openRecord((String) getValue("sStockIDx"),
                         poGRider.getIndustry(), poGRider.getBranchCode());

                if ("success".equals((String) poJSON.get("result"))) {
                    return poInventoryMaster;
                } else {
                    poInventoryMaster.initialize();
                    return poInventoryMaster;
                }
            }
        } else {
            poInventoryMaster.initialize();
            return poInventoryMaster;
        }
    }

}
