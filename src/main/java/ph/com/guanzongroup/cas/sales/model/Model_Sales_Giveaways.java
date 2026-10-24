package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.json.simple.JSONObject;

public class Model_Sales_Giveaways extends Model {

    private Model_Inventory poInventory;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata")
                    + "/Model_Sales_Giveaways.xml", getTable());

            poEntity.updateInt("nEntryNox", 0);
            poEntity.updateInt("nQuantity", 0);
            poEntity.updateInt("nGivenxxx", 0);
            poEntity.updateInt("nIssuedxx", 0);
            poEntity.updateDouble("nUnitCost", 0.0d);
            poEntity.updateDouble("nUnitPrce", 0.0d);
            poEntity.updateString("cGAwyStat", "1");
            poEntity.updateNull("sSourceCD");
            poEntity.updateNull("sSourceNo");
            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

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

    public JSONObject setGiven(int value) {
        return setValue("nGivenxxx", value);
    }

    public int getGiven() {
        Object v = getValue("nGivenxxx");
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

    public JSONObject setGiveAwayStatus(String value) {
        return setValue("cGAwyStat", value);
    }

    public String getGiveAwayStatus() {
        return (String) getValue("cGAwyStat");
    }

    public JSONObject setSourceCode(String value) {
        return setValue("sSourceCD", value);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCD");
    }

    public JSONObject setSourceNo(String value) {
        return setValue("sSourceNo", value);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setModifiedDate(String value) {
        return setValue("dModified", value);
    }

    public String getModifiedDate() {
        return (String) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return ""; // detail rows take their key from the parent transaction
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
}
