/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import java.sql.ResultSet;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.model.Model_Client_Mobile;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.*;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.VehicleQuotationStatus;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.model.Model_Client_Mail;

/**
 *
 * @author Arsiela
 */
public class Model_Sales_Quotation_Vehicle extends Model {
    
    //reference objects
    Model_Branch poBranch;
    Model_Sales_Inquiry_Master poSalesInquiry;
    Model_Model_Variant poModelVariant;
    Model_Brand poBrand;
    Model_Model poModel;
    Model_Financing_Rate_Master poFinancingRate;

    private String psBrand, psModel;


    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateNull("dExpected");
            poEntity.updateNull("dValdThru");
            poEntity.updateObject("nTranTotl", 0.00);
            poEntity.updateObject("nDiscount", 0.00);
            poEntity.updateObject("nAddDiscx", 0.00);
            poEntity.updateObject("nFreightx", 0.00);
            poEntity.updateObject("nVATSales", 0.00);
            poEntity.updateObject("nVATAmtxx", 0.00);
            poEntity.updateObject("nNonVATSl", 0.00);
            poEntity.updateObject("nTPLAmntx", 0.00);
            poEntity.updateObject("nCompInsx", 0.00);
            poEntity.updateObject("nLTOAmntx", 0.00);
            poEntity.updateObject("nDocStmpx", 0.00);
            poEntity.updateObject("nRsrvAmtx", 0.00);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sQotPayID";
            ID2 = "sTransNox";

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
    public JSONObject setQuotationPaymentId(String quotationPaymentId) {
        return setValue("sQotPayID", quotationPaymentId);
    }

    public String getQuotationPaymentId() {
        return (String) getValue("sQotPayID");
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCdx", industryId);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyID", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyID");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setCategoryCode(String categoryCode) {
        return setValue("sCategrCd", categoryCode);
    }

    public String getCategoryCode() {
        return (String) getValue("sCategrCd");
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
    
    public JSONObject setValidThruDate(Date validThruDate) {
        return setValue("dValdThru", validThruDate);
    }

    public Date getValidThruDate() {
        return (Date) getValue("dValdThru");
    }

    public JSONObject setClientId(String clientId) {
        return setValue("sClientID", clientId);
    }

    public String getClientId() {
        return (String) getValue("sClientID");
    }

    public JSONObject setAddressId(String addressId) {
        return setValue("sAddrssID", addressId);
    }

    public String getAddressId() {
        return (String) getValue("sAddrssID");
    }

    public JSONObject setContactId(String contactId) {
        return setValue("sContctID", contactId);
    }

    public String getContactId() {
        return (String) getValue("sContctID");
    }

    public JSONObject setReferenceNo(String referenceNo) {
        return setValue("sReferNox", referenceNo);
    }

    public String getReferenceNo() {
        return (String) getValue("sReferNox");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setQuotePaymentId(String quotePaymentId) {
        return setValue("sQotPayID", quotePaymentId);
    }

    public String getQuotePaymentId() {
        return (String) getValue("sQotPayID");
    }

    public JSONObject setPaymentForm(String paymentForm) {
        return setValue("cPaymForm", paymentForm);
    }

    public String getPaymentForm() {
        return (String) getValue("cPaymForm");
    }

    public JSONObject setDuration(int duration) {
        return setValue("nDuration", duration);
    }

    public int getDuration() {
        if (getValue("nDuration") == null || "".equals(getValue("nDuration"))) {
            return 0;
        }
        return (int) getValue("nDuration");
    }

    public JSONObject setVariantId(String variantId) {
        return setValue("sVrntIDxx", variantId);
    }

    public String getVariantId() {
        return (String) getValue("sVrntIDxx");
    }

    public JSONObject setPromptPaymentDiscount(Double primaryPayment) {
        return setValue("nPrmPayDx", primaryPayment);
    }

    public Double getPromptPaymentDiscount() {
        if (getValue("nPrmPayDx") == null || "".equals(getValue("nPrmPayDx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nPrmPayDx").toString());
    }

    public JSONObject setQuotationRate(String quotationRate) {
        return setValue("sQuotRate", quotationRate);
    }

    public String getQuotationRate() {
        return (String) getValue("sQuotRate");
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

    public JSONObject setDownPayment(Double downPayment) {
        return setValue("nDownPayx", downPayment);
    }

    public Double getDownPayment() {
        if (getValue("nDownPayx") == null || "".equals(getValue("nDownPayx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nDownPayx").toString());
    }

    public JSONObject setDownPaymentPercent(Double downPaymentPercent) {
        return setValue("nDownPctx", downPaymentPercent);
    }

    public Double getDownPaymentPercent() {
        if (getValue("nDownPctx") == null || "".equals(getValue("nDownPctx"))) {
            return 0.00;
        }
        return Double.valueOf(getValue("nDownPctx").toString());
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


    public JSONObject setFinancingAmount(Double financingAmount) {
        return setValue("nFinAmntx", financingAmount);
    }

    public Double getFinancingAmount() {
        if (getValue("nFinAmntx") == null || "".equals(getValue("nFinAmntx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nFinAmntx").toString());
    }

    public JSONObject setRateValue(Double rateValue) {
        return setValue("nRateValx", rateValue);
    }

    public Double getRateValue() {
        if (getValue("nRateValx") == null || "".equals(getValue("nRateValx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nRateValx").toString());
    }

    public JSONObject setMonthlyAmount(Double monthlyAmount) {
        return setValue("nMonthlyx", monthlyAmount);
    }

    public Double getMonthlyAmount() {
        if (getValue("nMonthlyx") == null || "".equals(getValue("nMonthlyx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nMonthlyx").toString());
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
    
    public JSONObject setTransactionTotal(Double totalAmount) {
        return setValue("nTranTotl", totalAmount);
    }

    public Double getTransactionTotal() {
        if (getValue("nTranTotl") == null || "".equals(getValue("nTranTotl"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nTranTotl").toString());
    }

    public JSONObject setTPLAmount(Double totalAmount) {
        return setValue("nTPLAmntx", totalAmount);
    }

    public Double getTPLAmount() {
        if (getValue("nTPLAmntx") == null || "".equals(getValue("nTPLAmntx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nTPLAmntx").toString());
    }

    public JSONObject setComprehensiveAmount(Double totalAmount) {
        return setValue("nCompInsx", totalAmount);
    }

    public Double getComprehensiveAmount() {
        if (getValue("nCompInsx") == null || "".equals(getValue("nCompInsx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nCompInsx").toString());
    }

    public JSONObject setLTOAmount(Double totalAmount) {
        return setValue("nLTOAmntx", totalAmount);
    }

    public Double getLTOAmount() {
        if (getValue("nLTOAmntx") == null || "".equals(getValue("nLTOAmntx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nLTOAmntx").toString());
    }

    public JSONObject setCHMOAmount(Double totalAmount) {
        return setValue("nDocStmpx", totalAmount);
    }

    public Double getCHMOAmount() {
        if (getValue("nDocStmpx") == null || "".equals(getValue("nDocStmpx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nDocStmpx").toString());
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

    public JSONObject setGiveawayCode(String giveawayCode) {
        return setValue("sGAWayCde", giveawayCode);
    }

    public String getGiveawayCode() {
        return (String) getValue("sGAWayCde");
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

    public void setBrandId(String brand) {
        psBrand = brand;
    }

    public String getBrandId() {
        String id = (String) (getValue("sVrntIDxx") == null ? "" : getValue("sVrntIDxx"));

        if (!"".equals(id)) {
            try {
                psBrand = ModelVariant().Model().getBrandId();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (GuanzonException e) {
                throw new RuntimeException(e);
            }
        }

        return psBrand == null ? "" : psBrand;
    }

    public void setModelId(String model) {
        psModel = model;
    }

    public String getModelId() {
        String id = (String) (getValue("sVrntIDxx") == null ? "" : getValue("sVrntIDxx"));

        if (!"".equals(id)) {
            try {
                psModel = ModelVariant().getModelId();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (GuanzonException e) {
                throw new RuntimeException(e);
            }
        }

        return psModel == null ? "" : psModel;
    }


    //reference object models
    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (poBranch == null) {
            poBranch = new ParamModels(poGRider).Branch();
        }

        String id = (String) (getValue("sBranchCd") == null ? "" : getValue("sBranchCd"));

        if (!"".equals(id)) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals(id)) {
                return poBranch;
            } else {
                poJSON = poBranch.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
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

    public Model_Sales_Inquiry_Master SalesInquiry() throws SQLException, GuanzonException {
        if (poSalesInquiry == null) {
            poSalesInquiry = new SalesModels(poGRider).SalesInquiryMaster();
        }

        String id = (String) (getValue("sSourceNo") == null ? "" : getValue("sSourceNo"));

        if (!"".equals(id)) {
            if (poSalesInquiry.getEditMode() == EditMode.READY
                    && poSalesInquiry.getTransactionNo().equals(id)) {
                return poSalesInquiry;
            } else {
                poJSON = poSalesInquiry.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poSalesInquiry;
                } else {
                    poSalesInquiry.initialize();
                    return poSalesInquiry;
                }
            }
        } else {
            poSalesInquiry.initialize();
            return poSalesInquiry;
        }
    }


    public Model_Model_Variant ModelVariant() throws SQLException, GuanzonException {
        if (poModelVariant == null) {
            poModelVariant = new ParamModels(poGRider).ModelVariant();
        }

        String id = (String) (getValue("sVrntIDxx") == null ? "" : getValue("sVrntIDxx"));

        if (!"".equals(id)) {
            if (poModelVariant.getEditMode() == EditMode.READY
                    && poModelVariant.getVariantId().equals(id)) {
                return poModelVariant;
            } else {
                poJSON = poModelVariant.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
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

    public Model_Brand Brand() throws SQLException, GuanzonException {
        if (poBrand == null) {
            poBrand = new ParamModels(poGRider).Brand();
        }

        String id = getBrandId();

        if (!"".equals(id)) {
            if (poBrand.getEditMode() == EditMode.READY
                    && poBrand.getBrandId().equals(id)) {
                return poBrand;
            } else {
                poJSON = poBrand.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poBrand;
                } else {
                    poBrand.initialize();
                    return poBrand;
                }
            }
        } else {
            poBrand.initialize();
            return poBrand;
        }
    }
    
    public Model_Model Model() throws SQLException, GuanzonException {
        if (poModel == null) {
            poModel = new ParamModels(poGRider).Model();
        }

        String id = getModelId();

        if (!"".equals(id)) {
            if (poModel.getEditMode() == EditMode.READY
                    && poModel.getModelId().equals(id)) {
                return poModel;
            } else {
                poJSON = poModel.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poModel;
                } else {
                    poModel.initialize();
                    return poModel;
                }
            }
        } else {
            poModel.initialize();
            return poModel;
        }
    }
    
    public Model_Financing_Rate_Master FinancingRate() throws SQLException, GuanzonException {
        if (poFinancingRate == null) {
            poFinancingRate = new SalesModels(poGRider).FinancingRateMaster();
        }

        String id = (String) (getValue("sQuotRate") == null ? "" : getValue("sQuotRate"));
        
        if (!"".equals(id)) {
            if (poFinancingRate.getEditMode() == EditMode.READY
                    && poFinancingRate.getRateId().equals(id)) {
                return poFinancingRate;
            } else {
                poJSON = poFinancingRate.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poFinancingRate;
                } else {
                    poFinancingRate.initialize();
                    return poFinancingRate;
                }
            }
        } else {
            poFinancingRate.initialize();
            return poFinancingRate;
        }
    }
    
    //end - reference object models

}

