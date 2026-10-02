
        /*
         * To change this license header, choose License Headers in Project Properties.
         * To change this template file, choose Tools | Templates
         * and open the template in the editor.
         */
        package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.parameter.model.Model_Term;
import org.json.simple.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Model class for the Sales Quotation Version Detail transaction.
 *
 * <p>
 * This class represents a line item of a Sales Quotation Version. Each
 * record is identified by the combination of {@code sTransNox} (the parent
 * version's transaction number) and {@code nEntryNox} (the line entry
 * number), and captures the stocked item, quantity, pricing, discounts,
 * freight, registration/insurance amounts, and VAT flag for that line.
 * </p>
 *
 * <p>
 * Detail records do not generate their own transaction number; the
 * {@code sTransNox} value is inherited from the parent
 * {@code Sales_Quotation_Version_Master} record, and {@code nEntryNox} is
 * expected to be assigned by the caller.
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 * @module Sales Quotation Version Detail
 * @version 1.0
 */
public class Model_Sales_Quotation_Version_Detail extends Model {

    private Model_Inventory poInventory;
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(
                    System.getProperty("sys.default.path.metadata") + XML,
                    getTable()
            );

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateNull("dModified");
            poEntity.updateObject("nQuantity", 0);
            poEntity.updateDouble("nUnitPrce", 0.00);
            poEntity.updateDouble("nDiscount", 0.00);
            poEntity.updateDouble("nAddDiscx", 0.00);
            poEntity.updateDouble("nFreightx", 0.00);
            poEntity.updateDouble("nRegisAmt", 0.00);
            poEntity.updateDouble("nInsAmtxx", 0.00);
            poEntity.updateString("cWithVATx", "0");
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public String getNextCode() {
        return "";
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

    public Integer getEntryNo() {
        return (Integer) getValue("nEntryNox");
    }

    public JSONObject setPromoCode(String promoCode) {
        return setValue("sPromCode", promoCode);
    }

    public String getPromoCode() {
        return (String) getValue("sPromCode");
    }

    public JSONObject setStockId(String stockId) {
        return setValue("sStockIDx", stockId);
    }

    public String getStockId() {
        return (String) getValue("sStockIDx");
    }

    public JSONObject setQuantity(Integer quantity) {
        return setValue("nQuantity", quantity);
    }

    public Integer getQuantity() {
        return (Integer) getValue("nQuantity");
    }

    public JSONObject setUnitPrice(Double unitPrice) {
        return setValue("nUnitPrce", unitPrice);
    }

    public double getUnitPrice() {
        return Double.parseDouble(String.valueOf(getValue("nUnitPrce")));
    }

    public JSONObject setDiscount(Double discount) {
        return setValue("nDiscount", discount);
    }

    public double getDiscount() {
        return Double.parseDouble(String.valueOf(getValue("nDiscount")));
    }

    public JSONObject setAdditionalDiscount(Double additionalDiscount) {
        return setValue("nAddDiscx", additionalDiscount);
    }

    public double getAdditionalDiscount() {
        return Double.parseDouble(String.valueOf(getValue("nAddDiscx")));
    }

    public JSONObject setFreight(Double freight) {
        return setValue("nFreightx", freight);
    }

    public double getFreight() {
        return Double.parseDouble(String.valueOf(getValue("nFreightx")));
    }

    public JSONObject setRegistrationAmount(double registrationAmount) {
        return setValue("nRegisAmt", registrationAmount);
    }

    public double getRegistrationAmount() {
        return Double.parseDouble(String.valueOf(getValue("nRegisAmt")));
    }

    public JSONObject setInsuranceAmount(double insuranceAmount) {
        return setValue("nInsAmtxx", insuranceAmount);
    }

    public double getInsuranceAmount() {
        return Double.parseDouble(String.valueOf(getValue("nInsAmtxx")));
    }

    public JSONObject setWithVAT(String withVat) {
        return setValue("cWithVATx", withVat);
    }

    public String getWithVAT() {
        return (String) getValue("cWithVATx");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    public Timestamp getTimeStamp() {
        return (Timestamp) getValue("dTimeStmp");
    }

    public Model_Inventory Inventory() throws SQLException, GuanzonException {

        if (poInventory == null) {
            poInventory = new Model_Inventory();
            poInventory.setApplicationDriver(poGRider);
            poInventory.setXML("Model_Inventory");
            poInventory.setTableName("Inventory");
            poInventory.initialize();
        }

        String stockId = (String) (
                getValue("sStockIDx") == null
                        ? ""
                        : getValue("sStockIDx")
        );

        if (!"".equals(stockId)) {

            if (poInventory.getEditMode() == EditMode.READY
                    && poInventory.getStockId().equals(stockId)) {
                return poInventory;
            }

            if (ReferenceCache.tryLoad(
                    "Inventory",
                    stockId,
                    poInventory)) {
                return poInventory;
            }

            poJSON = poInventory.openRecord(stockId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Inventory",
                        stockId,
                        poInventory
                );
                return poInventory;
            } else {
                poInventory.initialize();
                return poInventory;
            }

        } else {
            poInventory.initialize();
            return poInventory;
        }
    }
}

