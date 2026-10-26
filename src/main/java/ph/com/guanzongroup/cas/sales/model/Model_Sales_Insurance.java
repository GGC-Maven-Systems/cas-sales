package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Insurance_Type;
import org.json.simple.JSONObject;

public class Model_Sales_Insurance extends Model {

    private Model_Insurance_Type poInsuranceType;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata")
                    + "/Model_Sales_Insurance.xml", getTable());
            MiscUtil.initRowSet(poEntity);

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateDouble("nInsAmtxx", 0.0d);
            poEntity.updateString("cChargeTo", "1");
            poEntity.last();
            poEntity.moveToInsertRow();

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
        return setValue("sInsurIDx", value);
    }

    public String getInsuranceID() {
        return (String) getValue("sInsurIDx");
    }

    public JSONObject setInsuranceAmount(double value) {
        return setValue("nInsAmtxx", value);
    }

    public double getInsuranceAmount() {
        Object v = getValue("nInsAmtxx");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setInsuranceYear(int value) {
        return setValue("nInsurYrx", value);
    }

    public int getInsuranceYear() {
        Object v = getValue("nInsurYrx");
        return v == null ? 0 : ((Number) v).intValue();
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
}
