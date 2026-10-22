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
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.services.SalesModels;

/**
 *
 * @author Arsiela
 */
public class Model_Quotation_AddOn extends Model {

    // reference objects
    Model_Quotation_Payment poQuotationPayment;
    Model_Vehicle_AddOn_Master poAddOnMaster;
    Model_Inventory poInventory;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateObject("nAmountxx", 0.00);
            poEntity.updateObject("nQtyxxxxx", 0);
            poEntity.updateObject("nTotAmntx", 0.00);
            poEntity.updateObject("nDiscAmnt", 0.00);
            poEntity.updateObject("nDiscPctx", 0.00);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            poEntity.updateNull("sModified");
            poEntity.updateNull("dModified");
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sQotAddID";
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

    public JSONObject setQuotationAddOnId(String quotationAddOnId) {
        return setValue("sQotAddID", quotationAddOnId);
    }

    public String getQuotationAddOnId() {
        return (String) getValue("sQotAddID");
    }

//    public JSONObject setQuotationPaymentId(String quotationPaymentId) {
//        return setValue("sQotPayID", quotationPaymentId);
//    }
//
//    public String getQuotationPaymentId() {
//        return (String) getValue("sQotPayID");
//    }

    public JSONObject setAddOnId(String addOnId) {
        return setValue("sAddOnIDx", addOnId);
    }

    public String getAddOnId() {
        return (String) getValue("sAddOnIDx");
    }

//    public JSONObject setStockId(String stockId) {
//        return setValue("sStockIDx", stockId);
//    }
//
//    public String getStockId() {
//        return (String) getValue("sStockIDx");
//    }

    public JSONObject setAmount(Double amount) {
        return setValue("nAmountxx", amount);
    }

    public Double getAmount() {
        if (getValue("nAmountxx") == null || "".equals(getValue("nAmountxx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nAmountxx").toString());
    }

    public JSONObject setQuantity(int quantity) {
        return setValue("nQtyxxxxx", quantity);
    }

    public int getQuantity() {
        if (getValue("nQtyxxxxx") == null || "".equals(getValue("nQtyxxxxx"))) {
            return 1;
        }
        return (int) getValue("nQtyxxxxx");
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

    public JSONObject setDiscountAmount(Double discountAmount) {
        return setValue("nDiscAmnt", discountAmount);
    }

    public Double getDiscountAmount() {
        if (getValue("nDiscAmnt") == null || "".equals(getValue("nDiscAmnt"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nDiscAmnt").toString());
    }

    public JSONObject setDiscountPercent(Double discountPercent) {
        return setValue("nDiscPctx", discountPercent);
    }

    public Double getDiscountPercent() {
        if (getValue("nDiscPctx") == null || "".equals(getValue("nDiscPctx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nDiscPctx").toString());
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

    public Date getTimeStamp() {
        return (Date) getValue("dTimeStmp");
    }

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, false, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    // reference object models
//    public Model_Quotation_Payment QuotationPayment() throws SQLException, GuanzonException {
//        if (poQuotationPayment == null) {
//            poQuotationPayment = new Model_Quotation_Payment();
//            poQuotationPayment.setApplicationDriver(poGRider);
//            poQuotationPayment.setXML("Model_Quotation_Payment");
//            poQuotationPayment.setTableName("Quotation_Payment");
//            poQuotationPayment.initialize();
//        }
//
//        String id = (String) (getValue("sQotPayID") == null ? "" : getValue("sQotPayID"));
//
//        if (!"".equals(id)) {
//            if (poQuotationPayment.getEditMode() == EditMode.READY
//                    && poQuotationPayment.getQuotationPaymentId().equals(id)) {
//                return poQuotationPayment;
//            } else {
//                poJSON = poQuotationPayment.openRecord(id);
//
//                if ("success".equals(poJSON.get("result"))) {
//                    return poQuotationPayment;
//                } else {
//                    poQuotationPayment.initialize();
//                    return poQuotationPayment;
//                }
//            }
//        } else {
//            poQuotationPayment.initialize();
//            return poQuotationPayment;
//        }
//    }

    public Model_Vehicle_AddOn_Master AddOnMaster() throws SQLException, GuanzonException {
        if (poAddOnMaster == null) {
            poAddOnMaster = new SalesModels(poGRider).VehicleAddOnMaster();
        }

        String id = (String) (getValue("sAddOnIDx") == null ? "" : getValue("sAddOnIDx"));

        if (!"".equals(id)) {
            if (poAddOnMaster.getEditMode() == EditMode.READY
                    && poAddOnMaster.getAddOnId().equals(id)) {
                return poAddOnMaster;
            } else {
                poJSON = poAddOnMaster.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poAddOnMaster;
                } else {
                    poAddOnMaster.initialize();
                    return poAddOnMaster;
                }
            }
        } else {
            poAddOnMaster.initialize();
            return poAddOnMaster;
        }
    }

    public Model_Inventory Inventory() throws SQLException, GuanzonException {
        if (poInventory == null) {
            poInventory = new InvModels(poGRider).Inventory();
        }

        String id = (String) (getValue("sStockIDx") == null ? "" : getValue("sStockIDx"));

        if (!"".equals(id)) {
            if (poInventory.getEditMode() == EditMode.READY
                    && poInventory.getStockId().equals(id)) {
                return poInventory;
            } else {
                poJSON = poInventory.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poInventory;
                } else {
                    poInventory.initialize();
                    return poInventory;
                }
            }
        } else {
            poInventory.initialize();
            return poInventory;
        }
    }
    // end - reference object models
}
