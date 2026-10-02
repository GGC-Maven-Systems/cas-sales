package ph.com.guanzongroup.cas.sales.mcpromo.model;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Province;
import org.json.simple.JSONObject;

public class Model_Sales_Promotion_Province extends Model {

    private Model_Province poProvince;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", 1);
            poEntity.updateString("cWithIncx", RecordStatus.INACTIVE);
            poEntity.updateString("cInsurFOC", RecordStatus.INACTIVE);
            poEntity.updateString("cRegisFOC", RecordStatus.INACTIVE);
            poEntity.updateDouble("nFreightx", 0.00d);
            poEntity.updateDouble("nAmountxx", 0.00d);
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

    public JSONObject setProvinceID(String provinceID) {
        return setValue("sProvIDxx", provinceID);
    }

    public String getProvinceID() {
        return (String) getValue("sProvIDxx");
    }

    public JSONObject setFreight(Double freight) {
        return setValue("nFreightx", freight);
    }

    public Double getFreight() {
        return Double.valueOf(getValue("nFreightx").toString());
    }

    public JSONObject setWithIncentive(String withIncentive) {
        return setValue("cTranType", withIncentive);
    }

    public String getWithIncentive() {
        return (String) getValue("cWithIncx");
    }

    public Double getAmount() {
        return Double.valueOf(getValue("nAmountxx").toString());
    }

    public JSONObject setAmount(String amount) {
        return setValue("nAmountxx", amount);
    }

    public JSONObject setInsuranceFree(String insuranceFree) {
        return setValue("cInsurFOC", insuranceFree);
    }

    public String getInsuranceFree() {
        return (String) getValue("cInsurFOC");
    }

    public JSONObject setRegistrationFree(String registrationFree) {
        return setValue("cRegisFOC", registrationFree);
    }

    public String getRegistrationFree() {
        return (String) getValue("cRegisFOC");
    }

    public JSONObject isWithIncentive(boolean isWithIncentive) {
        return setValue("cWithIncx", isWithIncentive == true ? "1" : "0");
    }

    public boolean isWithIncentive() {
        return "1".equals((String) getValue("cWithIncx"));
    }

    public JSONObject isInsuranceFree(boolean isInsuranceFree) {
        return setValue("cInsurFOC", isInsuranceFree == true ? "1" : "0");
    }

    public boolean isInsuranceFree() {
        return "1".equals((String) getValue("cInsurFOC"));
    }

    public JSONObject isRegistrationFree(boolean isRegistrationFree) {
        return setValue("cRegisFOC", isRegistrationFree == true ? "1" : "0");
    }

    public boolean isRegistrationFree() {
        return "1".equals((String) getValue("cRegisFOC"));
    }

    public JSONObject isAllProvince(boolean isAllProvince) {
        return setValue("cAllProvx", isAllProvince == true ? "1" : "0");
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

    public Model_Province Province() throws SQLException, GuanzonException {
        if (poProvince == null) {
            poProvince = new Model_Province();
            poProvince.setApplicationDriver(poGRider);
            poProvince.setXML("Model_Province");
            poProvince.setTableName("Province");
            poProvince.initialize();
        }

        String provinceID = getValue("sProvIDxx") == null ? "" : (String) getValue("sProvIDxx");

        if (!"".equals(provinceID)) {
            if (poProvince.getEditMode() == EditMode.READY
                    && poProvince.getProvinceId().equals(provinceID)) {
                return poProvince;
            } else {
                if (ReferenceCache.tryLoad("Province", provinceID, poProvince)) {
                    return poProvince;
                }

                poJSON = poProvince.openRecord(provinceID);

                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store("Province", provinceID, poProvince);
                    return poProvince;
                } else {
                    poProvince.initialize();
                    return poProvince;
                }
            }
        } else {
            poProvince.initialize();
            return poProvince;
        }
    }

}
