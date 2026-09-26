/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;

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
 * expected to be assigned by the caller (e.g. the next available line
 * number for the given transaction).
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 * @module Sales Quotation Version Detail
 * @version 1.0
 */
public class Model_Sales_Quotations_Version_Detail extends Model {

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
            poEntity.updateNull("dModified");
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

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

    public JSONObject setEntryNo(Integer entryNo) {
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

    public Double getUnitPrice() {
        return (Double) getValue("nUnitPrce");
    }

    public JSONObject setDiscount(Double discount) {
        return setValue("nDiscount", discount);
    }

    public Double getDiscount() {
        return (Double) getValue("nDiscount");
    }

    public JSONObject setAdditionalDiscount(Double additionalDiscount) {
        return setValue("nAddDiscx", additionalDiscount);
    }

    public Double getAdditionalDiscount() {
        return (Double) getValue("nAddDiscx");
    }

    public JSONObject setFreight(Double freight) {
        return setValue("nFreightx", freight);
    }

    public Double getFreight() {
        return (Double) getValue("nFreightx");
    }

    public JSONObject setRegistrationAmount(Double registrationAmount) {
        return setValue("nRegisAmt", registrationAmount);
    }

    public Double getRegistrationAmount() {
        return (Double) getValue("nRegisAmt");
    }

    public JSONObject setInsuranceAmount(Double insuranceAmount) {
        return setValue("nInsAmtxx", insuranceAmount);
    }

    public Double getInsuranceAmount() {
        return (Double) getValue("nInsAmtxx");
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
}
