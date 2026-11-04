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
import org.guanzon.appdriver.base.GuanzonException;
import ph.com.guanzongroup.cas.sales.services.SalesModels;

/**
 *
 * @author Arsiela
 */
public class Model_Promotion_AddOn extends Model {
    Model_Vehicle_AddOn_Type poAddOnType;

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
            poEntity.updateObject("nDiscAmnt", 0.00);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sPrmAddID";
            ID2 = "sPromCode";
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setPromoAddId(String promoAddId) {
        return setValue("sPrmAddID", promoAddId);
    }

    public String getPromoAddId() {
        return (String) getValue("sPrmAddID");
    }

    public JSONObject setPromotionCode(String promotionCode) {
        return setValue("sPromCode", promotionCode);
    }

    public String getPromotionCode() {
        return (String) getValue("sPromCode");
    }

    public JSONObject setAddOnId(String addOnIndex) {
        return setValue("sAddOnIDx", addOnIndex);
    }

    public String getAddOnId() {
        return (String) getValue("sAddOnIDx");
    }

    public JSONObject setPromoType(String promoType) {
        return setValue("sPromoTyp", promoType);
    }

    public String getPromoType() {
        return (String) getValue("sPromoTyp");
    }

    public JSONObject setDiscountAmount(Double discountAmount) {
        return setValue("nDiscAmnt", discountAmount);
    }

    public Double getDiscountAmount() {
        if (getValue("nDiscAmnt") == null || "".equals(getValue("nDiscAmnt"))) {
            return 0.00;
        }
        return Double.valueOf(getValue("nDiscAmnt").toString());
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
    
    public Model_Vehicle_AddOn_Type AddOnType()
            throws SQLException,
            GuanzonException {
        
            //initialize reference objects
            SalesModels model = new SalesModels(poGRider);
            poAddOnType = model.VehicleAddOnType();
        
        if (!"".equals((String) getValue("sAddOnIDx"))) {
            if (poAddOnType.getEditMode() == EditMode.READY
                    && poAddOnType.getAddTypeCode().equals((String) getValue("sAddOnIDx"))) {
                return poAddOnType;
            } else {
                poJSON = poAddOnType.openRecord((String) getValue("sAddOnIDx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poAddOnType;
                } else {
                    poAddOnType.initialize();
                    return poAddOnType;
                }
            }
        } else {
            poAddOnType.initialize();
            return poAddOnType;
        }
    }
}
