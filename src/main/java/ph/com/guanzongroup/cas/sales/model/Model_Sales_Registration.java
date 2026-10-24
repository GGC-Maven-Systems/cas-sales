package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Insurance_Type;
import org.json.simple.JSONObject;

public class Model_Sales_Registration extends Model {

    private Model_Insurance_Type poInsuranceType;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata")
                    + "/Model_Sales_Registration.xml", getTable());

            poEntity.updateInt("nEntryNox", 0);
            poEntity.updateDouble("nInsAmtxx", 0.0d);
            poEntity.updateString("cChargeTo", "1");
            poEntity.updateString("cRegisTyp", "0");
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

    public JSONObject setInsuranceAmount(double value) {
        return setValue("nInsAmtxx", value);
    }

    public double getInsuranceAmount() {
        Object v = getValue("nInsAmtxx");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setRegistrationType(String value) {
        return setValue("cRegisTyp", value);
    }

    public String getRegistrationType() {
        return (String) getValue("cRegisTyp");
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
