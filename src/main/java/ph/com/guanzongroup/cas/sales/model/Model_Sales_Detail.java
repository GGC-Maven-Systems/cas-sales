package ph.com.guanzongroup.cas.sales.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inv_Master;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Brand;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Master;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Model;

public class Model_Sales_Detail extends Model {

    private Model_Inventory poInventory;
    private Model_Inv_Serial poInventorySerial;
    private Model_Inv_Master poInventoryMaster;
    private Model_Sales_Promotion_Master poSalesPromo;
    private Model_Sales_Promotion_Model poPromoModel;
    private Model_Sales_Promotion_Brand poPromoBrand;

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
            poEntity.updateObject("nQuantity", 0);
            poEntity.updateObject("nIssuedxx", 0);
            poEntity.updateString("cNewStock", "1");
            poEntity.updateString("cWithVATx", "1");
            poEntity.updateNull("sPromoIDx");
            poEntity.updateObject("dModified", poGRider.getServerDate());

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
        return Integer.valueOf(getValue("nEntryNox").toString());
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
        return Integer.valueOf(getValue("nQuantity").toString());
    }

    public JSONObject setIssued(int value) {
        return setValue("nIssuedxx", value);
    }

    public int getIssued() {
        return Integer.valueOf(getValue("nIssuedxx").toString());
    }

    public JSONObject setUnitCost(double value) {
        return setValue("nUnitCost", value);
    }

    public double getUnitCost() {
        return Double.valueOf(getValue("nUnitCost").toString());
    }

    public JSONObject setUnitPrice(double value) {
        return setValue("nUnitPrce", value);
    }

    public double getUnitPrice() {
        return Double.valueOf(getValue("nUnitPrce").toString());
    }

    public JSONObject setDiscount(double value) {
        return setValue("nDiscount", value);
    }

    public double getDiscount() {

        return Double.valueOf(getValue("nDiscount").toString());
    }

    public JSONObject setAddDiscount(double value) {
        return setValue("nAddDiscx", value);
    }

    public double getAddDiscount() {
        return Double.valueOf(getValue("nAddDiscx").toString());
    }

    public JSONObject setFreight(double value) {
        return setValue("nFreightx", value);
    }

    public double getFreight() {
        return Double.valueOf(getValue("nFreightx").toString());
    }

    public JSONObject setInsuranceAdd(double value) {
        return setValue("nInsurAdd", value);
    }

    public double getInsuranceAdd() {
        return Double.valueOf(getValue("nInsurAdd").toString());
    }

    public JSONObject setInsuranceAmount(double value) {
        return setValue("nInsAmtxx", value);
    }

    public double getInsuranceAmount() {
        return Double.valueOf(getValue("nInsAmtxx").toString());
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

        String modelPrimary = (String) (getValue("sPromoIDx") == null
                ? "" : getValue("sPromoIDx"));

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

    public Model_Sales_Promotion_Model PromotionModel()
            throws SQLException, GuanzonException {

        if (poPromoModel == null) {
            poPromoModel = new Model_Sales_Promotion_Model();
            poPromoModel.setApplicationDriver(poGRider);
            poPromoModel.setXML("Model_Sales_Promotion_Model");
            poPromoModel.setTableName("Sales_Promotion_Model");
            poPromoModel.initialize();
        }

        String lsPromoID = getValue("sPromoIDx") == null ? "" : (String) getValue("sPromoIDx");
        String lsModelID = Inventory().getModelId() == null ? "" : Inventory().getModelId();

        if (lsPromoID.isEmpty() || lsModelID.isEmpty()) {
            poPromoModel.initialize();
            return poPromoModel;
        }

        if (poPromoModel.getEditMode() == EditMode.READY
                && lsPromoID.equals(poPromoModel.getPromoID())
                && lsModelID.equals(poPromoModel.getModelID())) {
            return poPromoModel;
        }
        String getModelEntryNo = PromoModelEntryNo(lsPromoID, lsModelID);
        String lsKey = lsPromoID + getModelEntryNo;   // composite cache key
        if (ReferenceCache.tryLoad("Sales_Promotion_Model", lsKey, poPromoModel)) {
            return poPromoModel;
        }

        poJSON = poPromoModel.openRecord(lsPromoID, getModelEntryNo);
        if ("success".equals((String) poJSON.get("result"))) {
            ReferenceCache.store("Sales_Promotion_Model", lsKey, poPromoModel);
        } else {
            poPromoModel.initialize();
        }
        return poPromoModel;
    }

    public Model_Sales_Promotion_Brand PromotionBrand()
            throws SQLException, GuanzonException {

        if (poPromoBrand == null) {
            poPromoBrand = new Model_Sales_Promotion_Brand();
            poPromoBrand.setApplicationDriver(poGRider);
            poPromoBrand.setXML("Model_Sales_Promotion_Brand");
            poPromoBrand.setTableName("Sales_Promotion_Brand");
            poPromoBrand.initialize();
        }

        String lsPromoID = getValue("sPromoIDx") == null ? "" : (String) getValue("sPromoIDx");
        String lsBrandID = Inventory().getBrandId() == null ? "" : Inventory().getBrandId();

        if (lsPromoID.isEmpty() || lsBrandID.isEmpty()) {
            poPromoBrand.initialize();
            return poPromoBrand;
        }

        if (poPromoBrand.getEditMode() == EditMode.READY
                && lsPromoID.equals(poPromoBrand.getPromoID())
                && lsBrandID.equals(poPromoBrand.getBrandID())) {
            return poPromoBrand;
        }

        String getModelEntryNo = PromoBrandEntryNo(lsPromoID, lsBrandID);
        String lsKey = lsPromoID + getModelEntryNo;   // composite cache key
        if (ReferenceCache.tryLoad("Sales_Promotion_Brand", lsKey, poPromoBrand)) {
            return poPromoBrand;
        }

        poJSON = poPromoBrand.openRecord(lsPromoID, getModelEntryNo);
        if ("success".equals((String) poJSON.get("result"))) {
            ReferenceCache.store("Sales_Promotion_Brand", lsKey, poPromoBrand);
        } else {
            poPromoBrand.initialize();
        }
        return poPromoBrand;
    }

    public boolean HasPromo() throws SQLException, GuanzonException {
        String lsPromoID = getValue("sPromoIDx") == null ? "" : (String) getValue("sPromoIDx");
        return !lsPromoID.isEmpty();

    }

    public boolean HasModelPromo() throws SQLException, GuanzonException {
        String lsPromoID = getValue("sPromoIDx") == null ? "" : (String) getValue("sPromoIDx");
        String lsModelID = Inventory().getModelId() == null ? "" : Inventory().getModelId();

        return !PromoModelEntryNo(lsPromoID, lsModelID).isEmpty();

    }

    public String PromoModelEntryNo(String promo, String model) throws SQLException {
        String lsSQL = "SELECT * FROM Sales_Promotion_Model"
                + "              WHERE sPromIDxx = " + SQLUtil.toSQL(promo);
        lsSQL = MiscUtil.addCondition(lsSQL, "sModelIDx = " + SQLUtil.toSQL(model));
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        System.out.println("Load Transaction list query is " + lsSQL);

        if (MiscUtil.RecordCount(loRS) <= 0) {
            return "";
        }

        loRS.beforeFirst();
        if (loRS.next()) {
            return loRS.getObject("nEntryNox").toString();
        }
        return "";
    }

    public String PromoBrandEntryNo(String promo, String brand) throws SQLException {
        String lsSQL = "SELECT * FROM Sales_Promotion_Brand"
                + "              WHERE sPromIDxx = " + SQLUtil.toSQL(promo);
        lsSQL = MiscUtil.addCondition(lsSQL, "sBrandIDx = " + SQLUtil.toSQL(brand));
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        System.out.println("Load Transaction list query is " + lsSQL);

        if (MiscUtil.RecordCount(loRS) <= 0) {
            return "";
        }

        loRS.beforeFirst();
        if (loRS.next()) {
            return loRS.getObject("nEntryNox").toString();
        }
        return "";
    }
}
