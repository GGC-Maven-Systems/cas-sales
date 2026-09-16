/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Banks;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;

/**
 *
 * @author Arsiela
 */
public class Model_Financing_Rate_Master extends Model {

    Model_Company poCompany;
    Model_Banks poBank;
    
    private String psCompany = "";
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateNull("dModified");
            poEntity.updateObject("nRateValx",0.00);
            poEntity.updateObject("nDIRatexx",0.00);
            poEntity.updateObject("nSIRatexx",0.00);
            poEntity.updateObject("nDuration",0);
            poEntity.updateString("cRecdStat", FinancingRateStatus.OPEN);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sRateIDxx";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poCompany = model.Company();
            poBank = model.Banks();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setRateId(String rateID) {
        return setValue("sRateIDxx", rateID);
    }

    public String getRateId() {
        return (String) getValue("sRateIDxx");
    }

    public JSONObject setBankId(String bankId) {
        return setValue("sBankIDxx", bankId);
    }

    public String getBankId() {
        return (String) getValue("sBankIDxx");
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

    public JSONObject setRate(Double rate) {
        return setValue("nRateValx", rate);
    }

    public Double getRate() {
        if (getValue("nRateValx") == null || "".equals(getValue("nRateValx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nRateValx").toString());
    }

    public JSONObject setDIRate(Double diRate) {
        return setValue("nDIRatexx", diRate);
    }

    public Double getDIRate() {
        if (getValue("nDIRatexx") == null || "".equals(getValue("nDIRatexx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nDIRatexx").toString());
    }

    public JSONObject setSIRate(Double siRate) {
        return setValue("nSIRatexx", siRate);
    }

    public Double getSIRate() {
        if (getValue("nSIRatexx") == null || "".equals(getValue("nSIRatexx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nSIRatexx").toString());
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

    public void setCompanyId(String companyId) {
        psCompany = companyId;
    }
    
    //reference object models
    public Model_Company Company() throws SQLException, GuanzonException {
        if (!"".equals(psCompany)) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals(psCompany)) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord(psCompany);

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
    
    public Model_Banks Bank() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sBankIDxx"))) {
            if (poBank.getEditMode() == EditMode.READY
                    && poBank.getBankCode().equals((String) getValue("sBankIDxx"))) {
                return poBank;
            } else {
                poJSON = poBank.openRecord((String) getValue("sBankIDxx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poBank;
                } else {
                    poBank.initialize();
                    return poBank;
                }
            }
        } else {
            poBank.initialize();
            return poBank;
        }
    }
    //end - reference object models
}

