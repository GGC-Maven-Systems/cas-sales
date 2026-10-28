package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.model.Model_Insurance_Type;
import org.json.simple.JSONObject;

public class Model_Sales_Insurance extends Model {

    private Model_Inv_Serial poInventorySerial;
    private Model_Insurance_Type poInsuranceType;
    private Model_Client_Master poClient;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata")
                    + "/Model_Sales_Insurance.xml", getTable());
            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateDouble("nInsAmtxx", 0.0d);
            poEntity.updateString("cChargeTo", "1");

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

    public JSONObject setInsuranceNo(String value) {
        return setValue("sInsurNox", value);
    }

    public String getInsuranceNo() {
        return (String) getValue("sInsurNox");
    }

    public JSONObject setSerialID(String value) {
        return setValue("sSerialID", value);
    }

    public String getSerialID() {
        return (String) getValue("sSerialID");
    }

    public JSONObject setInsuranceTypeID(String value) {
        return setValue("sInsTypID", value);
    }

    public String getInsuranceTypeID() {
        return (String) getValue("sInsTypID");
    }

    public JSONObject setInsuranceID(String value) {
        return setValue("sInsTypID", value);
    }

    public String getInsuranceID() {
        return (String) getValue("sInsTypID");
    }

    public JSONObject setInsuranceClientID(String value) {
        return setValue("sInsurIDx", value);
    }

    public String getInsuranceClientID() {
        return (String) getValue("sInsurIDx");
    }

    public JSONObject setInsuranceAmount(double value) {
        return setValue("nInsAmtxx", value);
    }

    public double getInsuranceAmount() {
        return Double.valueOf(getValue("nInsAmtxx").toString());
    }

    public JSONObject setInsuranceYear(int value) {
        return setValue("nInsurYrx", value);
    }

    public int getInsuranceYear() {
        return Integer.valueOf(getValue("nInsurYrx").toString());
    }

    public JSONObject setChargeTo(String value) {
        return setValue("cChargeTo", value);
    }

    public String getChargeTo() {
        return (String) getValue("cChargeTo");
    }

    public boolean isChargeTo() {
        return "1".equals((String) getValue("cChargeTo"));
    }

    public JSONObject isChargeTo(boolean isCharge) {
        return setValue("cChargeTo", isCharge ? "1" : "0");
    }

    @Override
    public String getNextCode() {
        return ""; // detail rows take their key from the parent transaction
    }

    public Model_Insurance_Type InsuranceType()
            throws SQLException, GuanzonException {

        if (poInsuranceType == null) {
            poInsuranceType = new Model_Insurance_Type();
            poInsuranceType.setApplicationDriver(poGRider);
            poInsuranceType.setXML("Model_Insurance_Type");
            poInsuranceType.setTableName("Insurance_Type");
            poInsuranceType.initialize();
        }

        String modelPrimary = (String) (getValue("sInsTypID") == null
                ? "" : getValue("sInsTypID"));

        if (!"".equals(modelPrimary)) {

            if (poInsuranceType.getEditMode() == EditMode.READY
                    && poInsuranceType.getInsuranceTypeID().equals(modelPrimary)) {
                return poInsuranceType;
            }

            if (ReferenceCache.tryLoad(
                    "Insurance_Type",
                    modelPrimary,
                    poInsuranceType)) {
                return poInsuranceType;
            }

            poJSON = poInsuranceType.openRecord(modelPrimary);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Insurance_Type",
                        modelPrimary,
                        poInsuranceType);
                return poInsuranceType;
            } else {
                poInsuranceType.initialize();
                return poInsuranceType;
            }

        } else {
            poInsuranceType.initialize();
            return poInsuranceType;
        }
    }

    public Model_Client_Master InusrancePayeeClient() throws SQLException, GuanzonException {

        if (poClient == null) {
            poClient = new Model_Client_Master();
            poClient.setApplicationDriver(poGRider);
            poClient.setXML("Model_Client_Master");
            poClient.setTableName("Client_Master");
            poClient.initialize();
        }

        String clientId = (String) (getValue("sInsurIDx") == null
                ? ""
                : getValue("sInsurIDx"));

        if (!"".equals(clientId)) {

            if (poClient.getEditMode() == EditMode.READY
                    && poClient.getClientId().equals(clientId)) {
                return poClient;
            }

            if (ReferenceCache.tryLoad("Client_Master", clientId, poClient)) {
                return poClient;
            }

            poJSON = poClient.openRecord(clientId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store("Client_Master", clientId, poClient);
                return poClient;
            } else {
                poClient.initialize();
                return poClient;
            }

        } else {
            poClient.initialize();
            return poClient;
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
}
