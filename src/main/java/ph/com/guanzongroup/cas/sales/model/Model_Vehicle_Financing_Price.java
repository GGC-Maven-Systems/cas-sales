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
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_Vehicle_Financing_Price extends Model {

    
    Model_Model_Variant poModelVariant;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateNull("dModified");
            poEntity.updateObject("nSRPAmntx",0.00);
            poEntity.updateObject("nDPRatexx",0.00);
            poEntity.updateObject("nRsrvAmtx",0.00);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sVhclFIDx";
            ID2 = "sValidIDx";
            
            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poModelVariant = model.ModelVariant();
            
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

    public JSONObject setVehicleFinancingId(String vehicleFinancingId) {
        return setValue("sVhclFIDx", vehicleFinancingId);
    }

    public String getVehicleFinancingId() {
        return (String) getValue("sVhclFIDx");
    }

    public JSONObject setVariantId(String variantId) {
        return setValue("sVrntIDxx", variantId);
    }

    public String getVariantId() {
        return (String) getValue("sVrntIDxx");
    }

    public JSONObject setSRPAmount(Double srpAmount) {
        return setValue("nSRPAmntx", srpAmount);
    }

    public Double getSRPAmount() {
        if (getValue("nSRPAmntx") == null || "".equals(getValue("nSRPAmntx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nSRPAmntx").toString());
    }

    public JSONObject setDownPaymentRate(Double dpRate) {
        return setValue("nDPRatexx", dpRate);
    }

    public Double getDownPaymentRate() {
        if (getValue("nDPRatexx") == null || "".equals(getValue("nDPRatexx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nDPRatexx").toString());
    }

    public JSONObject setReservationAmount(Double reservationAmount) {
        return setValue("nRsrvAmtx", reservationAmount);
    }

    public Double getReservationAmount() {
        if (getValue("nRsrvAmtx") == null || "".equals(getValue("nRsrvAmtx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nRsrvAmtx").toString());
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
    
    public Model_Model_Variant ModelVariant() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sVrntIDxx"))) {
            if (poModelVariant.getEditMode() == EditMode.READY
                    && poModelVariant.getVariantId().equals((String) getValue("sVrntIDxx"))) {
                return poModelVariant;
            } else {
                poJSON = poModelVariant.openRecord((String) getValue("sVrntIDxx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poModelVariant;
                } else {
                    poModelVariant.initialize();
                    return poModelVariant;
                }
            }
        } else {
            poModelVariant.initialize();
            return poModelVariant;
        }
    }
}

