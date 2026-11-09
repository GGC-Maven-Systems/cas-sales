/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.services.SalesModels;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Arsiela
 */
public class Model_Quotation_Inclusion extends Model {

    Model_Vehicle_Inclusion_Master poInclusion;
    boolean pbIsFree = true;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateObject("nQuantity", 1);
            poEntity.updateObject("nAmountxx", 0.00);
            poEntity.updateObject("nTotAmntx", 0.00);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            poEntity.updateNull("sModified");
            poEntity.updateNull("dModified");
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sQotIncID";
            ID2 = "sTransNox";

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setQuotationInclusionId(String quotationInclusionId) {
        return setValue("sQotIncID", quotationInclusionId);
    }

    public String getQuotationInclusionId() {
        return (String) getValue("sQotIncID");
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
        if (getValue("nQuantity") == null || "".equals(getValue("nQuantity"))) {
            return 1;
        }
        return (int) getValue("nQuantity");
    }

    public JSONObject setAmount(Double amount) {
        return setValue("nAmountxx", amount);
    }

    public Double getAmount() {
        if (getValue("nAmountxx") == null || "".equals(getValue("nAmountxx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nAmountxx").toString());
    }

    public JSONObject setTotalAmount(Double totalAmount) {
        return setValue("nTotAmntx", totalAmount);
    }

    public Double getTotalAmount() {
        if (getValue("nTotAmntx") == null || "".equals(getValue("nTotAmntx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nTotAmntx").toString());
    }

    public JSONObject setRecordStatus(boolean recordStatus) {
        return setValue("cRecdStat", recordStatus ? "1" : "0");
    }

    public boolean getRecordStatus() {
        Object loValue = getValue("cRecdStat");
        return loValue != null && "1".equals(String.valueOf(loValue));
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
    
    public void setIsFree(boolean fbFree){
        pbIsFree = fbFree;
    }
    
    public boolean getIsFree(){
        return pbIsFree;
    }

    public Model_Vehicle_Inclusion_Master Inclusion() throws SQLException, GuanzonException {
        if (poInclusion == null) {
            poInclusion = new SalesModels(poGRider).VehicleInclusionMaster();
        }

        String id = (String) (getValue("sInclIDxx") == null ? "" : getValue("sInclIDxx"));

        if (!"".equals(id)) {
            if (poInclusion.getEditMode() == EditMode.READY
                    && poInclusion.getInclusionId().equals(id)) {
                return poInclusion;
            } else {
                poJSON = poInclusion.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poInclusion;
                } else {
                    poInclusion.initialize();
                    return poInclusion;
                }
            }
        } else {
            poInclusion.initialize();
            return poInclusion;
        }
    }
}
