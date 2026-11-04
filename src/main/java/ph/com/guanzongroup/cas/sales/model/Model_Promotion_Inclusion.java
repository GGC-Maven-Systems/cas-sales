/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Arsiela
 */
public class Model_Promotion_Inclusion extends Model {

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateNull("dModified");
            poEntity.updateObject("cRecdStat", RecordStatus.ACTIVE);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sPrmIncID";
            ID2 = "sPromCode";
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setPromoInclusionId(String promoInclusionId) {
        return setValue("sPrmIncID", promoInclusionId);
    }

    public String getPromoInclusionId() {
        return (String) getValue("sPrmIncID");
    }

    public JSONObject setPromotionCode(String promotionCode) {
        return setValue("sPromCode", promotionCode);
    }

    public String getPromotionCode() {
        return (String) getValue("sPromCode");
    }

    public JSONObject setInclusionId(String inclusionId) {
        return setValue("sInclIDxx", inclusionId);
    }

    public String getInclusionId() {
        return (String) getValue("sInclIDxx");
    }

    public JSONObject setQuantity(int quantity) {
        return setValue("nQuantity", quantity);
    }

    public int getQuantity() {
        Object value = getValue("nQuantity");
        return value == null ? 0 : ((Number) value).intValue();
    }

    public JSONObject setRecordStatus(boolean recordStatus) {
        return setValue("cRecdStat", recordStatus ? "1" : "0");
    }

    public boolean getRecordStatus() {
        return ((String) getValue("cRecdStat")).equals("1");
    }

    public JSONObject setModifiedBy(String modifiedBy) {
        return setValue("sModified", modifiedBy);
    }

    public String getModifiedBy() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }
}
