package ph.com.guanzongroup.cas.sales.mcpromo.model;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Model;
import org.json.simple.JSONObject;

public class Model_Sales_Promotion_Model_Exception extends Model {

    private Model_Model poModel;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", 1);
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

    public JSONObject setModelID(String modelID) {
        return setValue("sModelIDx", modelID);
    }

    public String getModelID() {
        return (String) getValue("sModelIDx");
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

    public Model_Model Model() throws SQLException, GuanzonException {
        if (poModel == null) {
            poModel = new Model_Model();
            poModel.setApplicationDriver(poGRider);
            poModel.setXML("Model_Model");
            poModel.setTableName("Model");
            poModel.initialize();
        }

        String modelID = getValue("sModelIDx") == null ? "" : (String) getValue("sModelIDx");

        if (!"".equals(modelID)) {
            if (poModel.getEditMode() == EditMode.READY
                    && poModel.getModelId().equals(modelID)) {
                return poModel;
            } else {
                if (ReferenceCache.tryLoad("Model", modelID, poModel)) {
                    return poModel;
                }

                poJSON = poModel.openRecord(modelID);

                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store("Model", modelID, poModel);
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

}
