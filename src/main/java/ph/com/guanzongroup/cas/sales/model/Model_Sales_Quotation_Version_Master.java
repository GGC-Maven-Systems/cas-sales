package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Term;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationVersionStatic;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class Model_Sales_Quotation_Version_Master extends Model {

    // reference objects
    private Model_Branch poBranch;
    private Model_Term poTerm;

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
            poEntity.updateObject("dExpected", poGRider.getServerDate());
            poEntity.updateObject("dValdThru", poGRider.getServerDate());
            poEntity.updateObject("dModified", poGRider.getServerDate());

            poEntity.updateString(
                    "cTranStat",
                    SalesQoutationVersionStatic.OPEN
            );

            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nTranTotl", 0.00);
            poEntity.updateObject("nDiscAmtx", 0.00);
            poEntity.updateObject("nAddDiscx", 0.00);
            poEntity.updateObject("nFreightx", 0.00);
            poEntity.updateObject("nVATSales", 0.00);
            poEntity.updateObject("nVATAmtxx", 0.00);
            poEntity.updateObject("nNonVATSl", 0.00);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";

            // poBranch/poTerm are intentionally NOT constructed here.
            // They are initialized lazily in Branch()/Terms().

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
        return setValue("sTermIDxx", termId);
    }

    public String getTermId() {
        return (String) getValue("sTermIDxx");
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

    // ============================================================
    // Reference Object Models
    // ============================================================

    public Model_Branch Branch() throws SQLException, GuanzonException {

        if (poBranch == null) {
            poBranch = new Model_Branch();
            poBranch.setApplicationDriver(poGRider);
            poBranch.setXML("Model_Branch");
            poBranch.setTableName("Branch");
            poBranch.initialize();
        }

        String branchCode = (String) (
                getValue("sBranchCd") == null
                        ? ""
                        : getValue("sBranchCd")
        );

        if (!"".equals(branchCode)) {

            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals(branchCode)) {
                return poBranch;
            }

            if (ReferenceCache.tryLoad(
                    "Branch",
                    branchCode,
                    poBranch)) {
                return poBranch;
            }

            poJSON = poBranch.openRecord(branchCode);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Branch",
                        branchCode,
                        poBranch
                );
                return poBranch;
            } else {
                poBranch.initialize();
                return poBranch;
            }

        } else {
            poBranch.initialize();
            return poBranch;
        }
    }

    public Model_Term Terms() throws SQLException, GuanzonException {

        if (poTerm == null) {
            poTerm = new Model_Term();
            poTerm.setApplicationDriver(poGRider);
            poTerm.setXML("Model_Term");
            poTerm.setTableName("Term");
            poTerm.initialize();
        }

        String termId = (String) (
                getValue("sTermIDxx") == null
                        ? ""
                        : getValue("sTermIDxx")
        );

        if (!"".equals(termId)) {

            if (poTerm.getEditMode() == EditMode.READY
                    && poTerm.getTermId().equals(termId)) {
                return poTerm;
            }

            if (ReferenceCache.tryLoad(
                    "Term",
                    termId,
                    poTerm)) {
                return poTerm;
            }

            poJSON = poTerm.openRecord(termId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Term",
                        termId,
                        poTerm
                );
                return poTerm;
            } else {
                poTerm.initialize();
                return poTerm;
            }

        } else {
            poTerm.initialize();
            return poTerm;
        }
    }

    // end - reference object models
}