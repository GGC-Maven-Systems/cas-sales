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
import org.guanzon.appdriver.constant.Logical;

/**
 *
 * @author Arsiela
 */
public class Model_Quotation_Special_Request extends Model {

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nQuantity", 1);
            poEntity.updateObject("nAmountxx", 0.00);
            poEntity.updateObject("nTotAmntx", 0.00);
            poEntity.updateString("cChargeTo", Logical.NO);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            poEntity.updateNull("sModified");
            poEntity.updateNull("dModified");
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

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

    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        if (getValue("nEntryNox") == null || "".equals(getValue("nEntryNox"))) {
            return 0;
        }
        return Integer.parseInt(getValue("nEntryNox").toString());
    }

    public JSONObject setRequestDescription(String requestDescription) {
        return setValue("sReqstDsc", requestDescription);
    }

    public String getRequestDescription() {
        return (String) getValue("sReqstDsc");
    }

    public JSONObject setQuantity(int quantity) {
        return setValue("nQuantity", quantity);
    }

    public int getQuantity() {
        if (getValue("nQuantity") == null || "".equals(getValue("nQuantity"))) {
            return 1;
        }
        return Integer.parseInt(getValue("nQuantity").toString());
    }

    public JSONObject setAmount(double amount) {
        return setValue("nAmountxx", amount);
    }

    public double getAmount() {
        if (getValue("nAmountxx") == null || "".equals(getValue("nAmountxx"))) {
            return 0.00;
        }
        return Double.parseDouble(getValue("nAmountxx").toString());
    }

    public JSONObject setTotalAmount(double totalAmount) {
        return setValue("nTotAmntx", totalAmount);
    }

    public double getTotalAmount() {
        if (getValue("nTotAmntx") == null || "".equals(getValue("nTotAmntx"))) {
            return 0.00;
        }
        return Double.parseDouble(getValue("nTotAmntx").toString());
    }

    public JSONObject setChargeTo(String chargeTo) {
        return setValue("cChargeTo", chargeTo);
    }

    public String getChargeTo() {
        return (String) getValue("cChargeTo");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
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
        return "";
    }
}
