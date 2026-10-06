/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Arsiela
 */
public class Model_Vehicle_AddOn_Type extends Model {

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateNull("dModified");
            poEntity.updateObject("cSourceTx", Logical.NO);
            poEntity.updateString("cRecdStat", Logical.NO);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sAddTypex";
            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setAddTypeCode(String addTypeCode) {
        return setValue("sAddTypex", addTypeCode);
    }

    public String getAddTypeCode() {
        return (String) getValue("sAddTypex");
    }

    public JSONObject setAddTypeName(String addTypeName) {
        return setValue("sAddTypNm", addTypeName);
    }

    public String getAddTypeName() {
        return (String) getValue("sAddTypNm");
    }

    public JSONObject setSource(String source) {
        return setValue("cSourceTx", source);
    }

    public String getSource() {
        return (String) getValue("cSourceTx");
    }
    
    public JSONObject setRecordStatus(boolean recordStatus) {
        return setValue("cRecdStat", recordStatus ? "1" : "0");
    }

    public String getRecordStatus() {
        return (String) getValue("cRecdStat");
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

    public JSONObject setTimestamp(Date timestamp) {
        return setValue("dTimeStmp", timestamp);
    }

    public Date getTimestamp() {
        return (Date) getValue("dTimeStmp");
    }

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }
}
