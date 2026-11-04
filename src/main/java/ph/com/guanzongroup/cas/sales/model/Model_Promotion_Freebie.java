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
public class Model_Promotion_Freebie extends Model {

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

            ID = "sPrmFreID";
            ID2 = "sPromCode";
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setPromoFreeId(String promoFreeId) {
        return setValue("sPrmFreID", promoFreeId);
    }

    public String getPromoFreeId() {
        return (String) getValue("sPrmFreID");
    }

    public JSONObject setPromotionCode(String promotionCode) {
        return setValue("sPromCode", promotionCode);
    }

    public String getPromotionCode() {
        return (String) getValue("sPromCode");
    }

    public JSONObject setFreebieId(String freebieId) {
        return setValue("sFreebIDx", freebieId);
    }

    public String getFreebieId() {
        return (String) getValue("sFreebIDx");
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
