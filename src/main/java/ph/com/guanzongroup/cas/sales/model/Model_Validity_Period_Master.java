/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

/**
 *
 * @author Arsiela
 */
public class Model_Validity_Period_Master extends Model {

    Model_Company poCompany;
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateNull("dFromDate");
            poEntity.updateNull("dThruDate");
            poEntity.updateNull("dModified");
            poEntity.updateString("cRecdStat", ValidityPeriodStatus.APPROVED);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sValidIDx";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poCompany = model.Company();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setValidityId(String validityId) {
        return setValue("sValidIDx", validityId);
    }

    public String getValidityId() {
        return (String) getValue("sValidIDx");
    }

    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyID", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyID");
    }

    public JSONObject setValidityDescription(String description) {
        return setValue("sValidDsc", description);
    }

    public String getValidityDescription() {
        return (String) getValue("sValidDsc");
    }

    public JSONObject setFromDate(Date fromDate) {
        JSONObject loJSON = new JSONObject();
        if(fromDate == null){
            try {
                poEntity.updateNull("dFromDate");
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }
        } else {
            return setValue("dFromDate", fromDate);
        }
        
        loJSON.put("result", "success");
        loJSON.put("message", "success");
        return loJSON;
        
    }

    public Date getFromDate() {
        return (Date) getValue("dFromDate");
    }

    public JSONObject setThruDate(Date thruDate) {
        JSONObject loJSON = new JSONObject();
        if(thruDate == null){
            try {
                poEntity.updateNull("dThruDate");
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            }
        } else {
            return setValue("dThruDate", thruDate);
        }
        
        loJSON.put("result", "success");
        loJSON.put("message", "success");
        return loJSON;
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
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }
    
    public Model_Company Company() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sCompnyID"))) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals((String) getValue("sCompnyID"))) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord((String) getValue("sCompnyID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCompany;
                } else {
                    poCompany.initialize();
                    return poCompany;
                }
            }
        } else {
            poCompany.initialize();
            return poCompany;
        }
    }
}

