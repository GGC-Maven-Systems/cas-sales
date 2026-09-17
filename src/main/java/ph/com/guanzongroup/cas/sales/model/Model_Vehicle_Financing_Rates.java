/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_Vehicle_Financing_Rates extends Model {

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateNull("dModified");
            poEntity.updateNull("dFromDate");
            poEntity.updateNull("dThruDate");
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sStdRteID";
//            ID = "sRateType";
//            ID2 = "nDuration";
//            ID3 = "nRateValx";
//            ID4 = "dFromDate";
//            ID5 = "dThruDate";

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setStandardRateId(String validityId) {
        return setValue("sStdRteID", validityId);
    }

    public String getStandardRateId() {
        return (String) getValue("sStdRteID");
    }

    public JSONObject setRateType(String rateType) {
        return setValue("sRateType", rateType);
    }

    public String getRateType() {
        return (String) getValue("sRateType");
    }

    public JSONObject setDuration(int entryNo) {
        return setValue("nDuration", entryNo);
    }

    public int getDuration() {
        if (getValue("nDuration") == null || "".equals(getValue("nDuration"))) {
            return 0;
        }
        return (int) getValue("nDuration");
    }

    public JSONObject setRate(Double siRate) {
        return setValue("nRateValx", siRate);
    }

    public Double getRate() {
        if (getValue("nRateValx") == null || "".equals(getValue("nRateValx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nRateValx").toString());
    }

    public JSONObject setFromDate(Date fromDate) {
        return setValue("dFromDate", fromDate);
    }

    public Date getFromDate() {
        return (Date) getValue("dFromDate");
    }

    public JSONObject setThruDate(Date thruDate) {
        return setValue("dThruDate", thruDate);
    }

    public Date getThruDate() {
        return (Date) getValue("dThruDate");
    }

    public JSONObject setRecordStatus(String recordStatus) {
        return setValue("cRecdStat", recordStatus);
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
    
    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, false, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }
}

