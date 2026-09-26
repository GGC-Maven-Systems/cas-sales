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
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.model.Model_Client_Mobile;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Model class for the Sales Quotation Master transaction.
 *
 * <p>
 * This class represents the master record of a Sales Quotation transaction
 * within the CAS Sales module. It encapsulates transaction information
 * including transaction number, industry, category, transaction date,
 * client, address, contact, version, and transaction status.
 * </p>
 *
 * <p>
 * In addition to maintaining the Sales Quotation Master record, this model
 * provides convenient access to its associated reference objects, including:
 * </p>
 * <ul>
 *     <li>Industry</li>
 *     <li>Client Master</li>
 *     <li>Client Address</li>
 *     <li>Client Mobile</li>
 * </ul>
 *
 * <p>
 * Upon initialization, the model loads its metadata, assigns default values,
 * initializes its reference objects, and prepares the entity for create,
 * retrieve, update, and save operations.
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 * @module Sales Quotations Master
 * @version 1.0
 */
public class Model_Sales_Quotations_Version_Master extends Model {

    //reference objects
    Model_Branch poBranch;

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
            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateNull("dExpected");
            poEntity.updateNull("dValdThru");
            poEntity.updateNull("dModified");

            poEntity.updateString("cTranStat", "");
            poEntity.updateInt("nEntryNox", 0);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            // initialize reference objects
            ParamModels paramModel = new ParamModels(poGRider);
            poBranch = paramModel.Branch();
            // end - initialize reference objects

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

    public JSONObject setParentId(String parentId) {
        return setValue("sParentID", parentId);
    }

    public String getParentId() {
        return (String) getValue("sParentID");
    }

    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setExpectedDate(Date expectedDate) {
        return setValue("dExpected", expectedDate);
    }

    public Date getExpectedDate() {
        return (Date) getValue("dExpected");
    }

    public JSONObject setDeliveryType(String deliveryType) {
        return setValue("cDelivrTp", deliveryType);
    }

    public String getDeliveryType() {
        return (String) getValue("cDelivrTp");
    }

    public JSONObject setDeliverTo(String deliverTo) {
        return setValue("sDelivrTo", deliverTo);
    }

    public String getDeliverTo() {
        return (String) getValue("sDelivrTo");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setValidThruDate(Date validThruDate) {
        return setValue("dValdThru", validThruDate);
    }

    public Date getValidThruDate() {
        return (Date) getValue("dValdThru");
    }

    public JSONObject setTitleName(String titleName) {
        return setValue("sTitleNme", titleName);
    }

    public String getTitleName() {
        return (String) getValue("sTitleNme");
    }

    public JSONObject setRemarks1(String remarks1) {
        return setValue("sRemarks1", remarks1);
    }

    public String getRemarks1() {
        return (String) getValue("sRemarks1");
    }

    public JSONObject setRemarks2(String remarks2) {
        return setValue("sRemarks2", remarks2);
    }

    public String getRemarks2() {
        return (String) getValue("sRemarks2");
    }

    public JSONObject setReasons(String reasons) {
        return setValue("sReasonsx", reasons);
    }

    public String getReasons() {
        return (String) getValue("sReasonsx");
    }

    public JSONObject setPaymentForm(String paymentForm) {
        return setValue("cPaymForm", paymentForm);
    }

    public String getPaymentForm() {
        return (String) getValue("cPaymForm");
    }

    public JSONObject setTermId(String termId) {
        return setValue("sTermIdxx", termId);
    }

    public String getTermId() {
        return (String) getValue("sTermIdxx");
    }

    public JSONObject setTransactionTotal(Double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Double getTransactionTotal() {
        return (Double) getValue("nTranTotl");
    }

    public JSONObject setDiscountAmount(Double discountAmount) {
        return setValue("nDiscAmtx", discountAmount);
    }

    public Double getDiscountAmount() {
        return (Double) getValue("nDiscAmtx");
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

    public JSONObject setVatSales(Double vatSales) {
        return setValue("nVATSales", vatSales);
    }

    public Double getVatSales() {
        return (Double) getValue("nVATSales");
    }

    public JSONObject setVatAmount(Double vatAmount) {
        return setValue("nVATAmtxx", vatAmount);
    }

    public Double getVatAmount() {
        return (Double) getValue("nVATAmtxx");
    }

    public JSONObject setNonVatSales(Double nonVatSales) {
        return setValue("nNonVATSl", nonVatSales);
    }

    public Double getNonVatSales() {
        return (Double) getValue("nNonVATSl");
    }

    public JSONObject setApproverCode(String approverCode) {
        return setValue("sAPprCode", approverCode);
    }

    public String getApproverCode() {
        return (String) getValue("sAPprCode");
    }

    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setEntryNo(Integer entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Integer getEntryNo() {
        return (Integer) getValue("nEntryNox");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    public JSONObject setModifyingId(String modifiedBy) {
        return setValue("sModified", modifiedBy);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
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

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(
                this.getTable(),
                ID,
                true,
                poGRider.getGConnection().getConnection(),
                poGRider.getBranchCode()
        );
    }

    // reference object models

    /**
     * Retrieves the branch associated with the current Sales Quotation
     * Version transaction.
     *
     * <p>
     * If a branch code is assigned to the current transaction, this method
     * checks whether the existing branch model is already loaded for the same
     * branch. If it is ready and matches the current branch code, the existing
     * model is returned. Otherwise, the branch record is opened using the
     * current branch code.
     * </p>
     *
     * <p>
     * If the branch record cannot be opened successfully, the branch model
     * is reinitialized and returned. When no branch code is assigned to the
     * current transaction, the branch model is also reinitialized.
     * </p>
     *
     * @return the {@link Model_Branch} associated with the current Sales
     *         Quotation Version transaction, or an initialized branch model
     *         when no valid branch record is available.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the branch record.
     * @throws GuanzonException if an application-specific error occurs while
     *         processing the branch record.
     */
    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sBranchCd"))) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals((String) getValue("sBranchCd"))) {

                return poBranch;

            } else {
                poJSON = poBranch.openRecord((String) getValue("sBranchCd"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poBranch;
                } else {
                    poBranch.initialize();
                    return poBranch;
                }
            }
        } else {
            poBranch.initialize();
            return poBranch;
        }
    }

    // end - reference object models
}