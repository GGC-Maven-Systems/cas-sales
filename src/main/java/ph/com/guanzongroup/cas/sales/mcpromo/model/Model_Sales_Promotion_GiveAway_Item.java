package ph.com.guanzongroup.cas.sales.mcpromo.model;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.parameter.model.Model_Model;
import org.json.simple.JSONObject;

public class Model_Sales_Promotion_GiveAway_Item extends Model {

    private Model_Inventory poInventory;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", 1);
            poEntity.updateString("cCashSale", RecordStatus.INACTIVE);
            poEntity.updateString("cLoanSale", RecordStatus.INACTIVE);
            poEntity.updateInt("nQuantity", 0);
            poEntity.updateDouble("nDiscRate", 0.00d);
            poEntity.updateDouble("nDiscAmtx", 0.00d);
            poEntity.updateString("cActivexx", RecordStatus.ACTIVE);

            //end - assign default values
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);
            ID2 = poEntity.getMetaData().getColumnLabel(2);

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setPromoID(String promoID) {
        return setValue("sPromIDxx", promoID);
    }

    public String getPromoID() {
        return (String) getValue("sPromIDxx");
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

    public JSONObject setQuantity(int quantity) {
        return setValue("nQuantity", quantity);
    }

    public int getQuantity() {
        return Integer.parseInt(getValue("nQuantity").toString());
    }

    public JSONObject getDiscountRate(String discountRate) {
        return setValue("nDiscRate", discountRate);
    }

    public Double getDiscountRate() {
        return Double.valueOf(getValue("nDiscRate").toString());
    }

    public JSONObject setDiscAmount(String discAmount) {
        return setValue("nDiscAmtx", discAmount);
    }

    public Double getDiscAmount() {
        return Double.valueOf(getValue("nDiscAmtx").toString());
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setCashSale(String cashSale) {
        return setValue("cCashSale", cashSale);
    }

    public String getCashSale() {
        return (String) getValue("cCashSale");
    }

    public JSONObject setLoanSale(String loanSale) {
        return setValue("cLoanSale", loanSale);
    }

    public String getLoanSale() {
        return (String) getValue("cLoanSale");
    }

    public JSONObject isCashSale(boolean isCashSale) {
        return setValue("cCashSale", isCashSale == true ? "1" : "0");
    }

    public boolean isCashSale() {
        return "1".equals((String) getValue("cCashSale"));
    }

    public JSONObject isLoanSale(boolean isLoanSale) {
        return setValue("cLoanSale", isLoanSale == true ? "1" : "0");
    }

    public boolean isLoanSale() {
        return "1".equals((String) getValue("cLoanSale"));
    }

    public JSONObject setActive(String active) {
        return setValue("cActivexx", active);
    }

    public String getActive() {
        return (String) getValue("cActivexx");
    }

    public JSONObject isWithActive(boolean isWithActive) {
        return setValue("cActivexx", isWithActive == true ? "1" : "0");
    }

    public boolean isWithActive() {
        return "1".equals((String) getValue("cActivexx"));
    }

    @Override
    public String getNextCode() {
        return "";
    }

    public Model_Inventory Inventory() throws SQLException, GuanzonException {
        if (poInventory == null) {
            poInventory = new Model_Inventory();
            poInventory.setApplicationDriver(poGRider);
            poInventory.setXML("Model_Inventory");
            poInventory.setTableName("Inventory");
            poInventory.initialize();
        }

        String stockID = getValue("sStockIDx") == null ? "" : (String) getValue("sStockIDx");

        if (!"".equals(stockID)) {
            if (poInventory.getEditMode() == EditMode.READY
                    && poInventory.getStockId().equals(stockID)) {
                return poInventory;
            } else {
                if (ReferenceCache.tryLoad("Inventory", stockID, poInventory)) {
                    return poInventory;
                }

                poJSON = poInventory.openRecord(stockID);

                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store("Inventory", stockID, poInventory);
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

}
