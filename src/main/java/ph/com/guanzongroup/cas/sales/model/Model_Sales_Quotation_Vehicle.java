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
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.VehicleQuotationStatus;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.cas.client.model.Model_Client_Mail;

/**
 *
 * @author Arsiela
 */
public class Model_Sales_Quotation_Vehicle extends Model {
    
    //reference objects
    Model_Branch poBranch;
    Model_Industry poIndustry;
    Model_Client_Master poClient;
    Model_Client_Address poClientAddress;
    Model_Client_Mobile poClientMobile;
    Model_Client_Mail poClientEmail;
    Model_Sales_Inquiry_Master poSalesInquiry;
    Model_Company poCompany;


    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateNull("dTransact");
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
            poEntity.updateString("cTranStat", VehicleQuotationStatus.OPEN);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

//            //initialize reference objects
//            ParamModels model = new ParamModels(poGRider);
//            poBranch = model.Branch();
//            poIndustry = model.Industry();
//            poCompany = model.Company();
//
//            ClientModels clientModel = new ClientModels(poGRider);
//            poClient = clientModel.ClientMaster();
//            poClientAddress = clientModel.ClientAddress();
//            poClientMobile = clientModel.ClientMobile();
//
//            SalesModels salesModel = new SalesModels(poGRider);
//            poSalesInquiry = salesModel.SalesInquiryMaster();
            
//            end - initialize reference objects

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

//    public JSONObject setPayForm(String payForm) {
//        return setValue("cPaymForm", payForm);
//    }
//
//    public String getPayForm() {
//        return (String) getValue("cPaymForm");
//    }
//
//    public JSONObject setTermId(String termId) {
//        return setValue("sTermIDxx", termId);
//    }
//
//    public String getTermId() {
//        return (String) getValue("sTermIDxx");
//    }

    public JSONObject setTransactionTotal(Double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Double getTransactionTotal() {
        if (getValue("nTranTotl") == null || "".equals(getValue("nTranTotl"))) {
            return 0.0000;
        }
        return (Double) getValue("nTranTotl");
    }

    public JSONObject setDiscountRate(Double discountRate) {
        return setValue("nDiscount", discountRate);
    }

    public Double getDiscountRate() {
        if (getValue("nDiscount") == null || "".equals(getValue("nDiscount"))) {
            return 0.00;
        }
        return (Double) getValue("nDiscount");
    }

    public JSONObject setDiscount(Double discount) {
        return setValue("nAddDiscx", discount);
    }

    public Double getDiscount() {
        if (getValue("nAddDiscx") == null || "".equals(getValue("nAddDiscx"))) {
            return 0.0000;
        }
        return (Double) getValue("nAddDiscx");
    }

    public JSONObject setFreight(Double freight) {
        return setValue("nFreightx", freight);
    }

    public Double getFreight() {
        if (getValue("nFreightx") == null || "".equals(getValue("nFreightx"))) {
            return 0.00;
        }
        return (Double) getValue("nFreightx");
    }

    public JSONObject setVatSales(Double vatSales) {
        return setValue("nVATSales", vatSales);
    }

    public Double getVatSales() {
        if (getValue("nVATSales") == null || "".equals(getValue("nVATSales"))) {
            return 0.0000;
        }
        return (Double) getValue("nVATSales");
    }

    public JSONObject setVatAmount(Double vatAmount) {
        return setValue("nVATAmtxx", vatAmount);
    }

    public Double getVatAmount() {
        if (getValue("nVATAmtxx") == null || "".equals(getValue("nVATAmtxx"))) {
            return 0.0000;
        }
        return (Double) getValue("nVATAmtxx");
    }

    public JSONObject setNonVatSales(Double nonVatSales) {
        return setValue("nNonVATSl", nonVatSales);
    }

    public Double getNonVatSales() {
        if (getValue("nNonVATSl") == null || "".equals(getValue("nNonVATSl"))) {
            return 0.0000;
        }
        return (Double) getValue("nNonVATSl");
    }

    public JSONObject setTPLAmount(Double tplAmount) {
        return setValue("nTPLAmntx", tplAmount);
    }

    public Double getTPLAmount() {
        if (getValue("nTPLAmntx") == null || "".equals(getValue("nTPLAmntx"))) {
            return 0.0000;
        }
        return (Double) getValue("nTPLAmntx");
    }

    public JSONObject setComprehensiveInsurance(Double comprehensiveInsurance) {
        return setValue("nCompInsx", comprehensiveInsurance);
    }

    public Double getComprehensiveInsurance() {
        if (getValue("nCompInsx") == null || "".equals(getValue("nCompInsx"))) {
            return 0.0000;
        }
        return (Double) getValue("nCompInsx");
    }

    public JSONObject setLTOAmount(Double ltoAmount) {
        return setValue("nLTOAmntx", ltoAmount);
    }

    public Double getLTOAmount() {
        if (getValue("nLTOAmntx") == null || "".equals(getValue("nLTOAmntx"))) {
            return 0.0000;
        }
        return (Double) getValue("nLTOAmntx");
    }

    public JSONObject setDocStampAmount(Double docStampAmount) {
        return setValue("nDocStmpx", docStampAmount);
    }

    public Double getDocStampAmount() {
        if (getValue("nDocStmpx") == null || "".equals(getValue("nDocStmpx"))) {
            return 0.0000;
        }
        return (Double) getValue("nDocStmpx");
    }

    public JSONObject setReservationAmount(Double reservationAmount) {
        return setValue("nRsrvAmtx", reservationAmount);
    }

    public Double getReservationAmount() {
        if (getValue("nRsrvAmtx") == null || "".equals(getValue("nRsrvAmtx"))) {
            return 0.0000;
        }
        return (Double) getValue("nRsrvAmtx");
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

//    public JSONObject setEntryNo(int entryNo) {
//        return setValue("nEntryNox", entryNo);
//    }
//
//    public int getEntryNo() {
//        if (getValue("nEntryNox") == null || "".equals(getValue("nEntryNox"))) {
//            return 0;
//        }
//        return (int) getValue("nEntryNox");
//    }

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

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
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

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (poIndustry == null) {
            poIndustry = new ParamModels(poGRider).Industry();
        }

        String id = (String) (getValue("sIndstCdx") == null ? "" : getValue("sIndstCdx"));

        if (!"".equals(id)) {
            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals(id)) {
                return poIndustry;
            } else {
                poJSON = poIndustry.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poIndustry;
                } else {
                    poIndustry.initialize();
                    return poIndustry;
                }
            }
        } else {
            poIndustry.initialize();
            return poIndustry;
        }
    }

    public Model_Company Company() throws SQLException, GuanzonException {
        if (poCompany == null) {
            poCompany = new ParamModels(poGRider).Company();
        }

        String id = (String) (getValue("sCompnyID") == null ? "" : getValue("sCompnyID"));

        if (!"".equals(id)) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals(id)) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
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

    public Model_Client_Master Client() throws SQLException, GuanzonException {
        if (poClient == null) {
            poClient = new ClientModels(poGRider).ClientMaster();
        }

        String id = (String) (getValue("sClientID") == null ? "" : getValue("sClientID"));

        if (!"".equals(id)) {
            if (poClient.getEditMode() == EditMode.READY
                    && poClient.getClientId().equals(id)) {
                return poClient;
            } else {
                poJSON = poClient.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poClient;
                } else {
                    poClient.initialize();
                    return poClient;
                }
            }
        } else {
            poClient.initialize();
            return poClient;
        }
    }
    
    public Model_Client_Address ClientAddress() throws SQLException, GuanzonException {
        if (poClientAddress == null) {
            poClientAddress = new ClientModels(poGRider).ClientAddress();
        }

        String id = (String) (getValue("sAddrssID") == null ? "" : getValue("sAddrssID"));

        if (!"".equals(id)) {
            if (poClientAddress.getEditMode() == EditMode.READY
                    && poClientAddress.getAddressId().equals(id)) {
                return poClientAddress;
            } else {
                poJSON = poClientAddress.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poClientAddress;
                } else {
                    poClientAddress.initialize();
                    return poClientAddress;
                }
            }
        } else {
            poClientAddress.initialize();
            return poClientAddress;
        }
    }
    
    public Model_Client_Mobile ClientMobile() throws SQLException, GuanzonException {
        if (poClientMobile == null) {
            poClientMobile = new ClientModels(poGRider).ClientMobile();
        }

        String id = (String) (getValue("sContctID") == null ? "" : getValue("sContctID"));

        if (!"".equals(id)) {
            if (poClientMobile.getEditMode() == EditMode.READY
                    && poClientMobile.getClientId().equals(id)) {
                return poClientMobile;
            } else {
                poJSON = poClientMobile.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poClientMobile;
                } else {
                    poClientMobile.initialize();
                    return poClientMobile;
                }
            }
        } else {
            poClientMobile.initialize();
            return poClientMobile;
        }
    }
    
    public Model_Client_Mail ClientEmail() throws SQLException, GuanzonException {
        if (poClientEmail == null) {
            poClientEmail = new ClientModels(poGRider).ClientMail();
        }

        String id = (String) (getValue("sClientID") == null ? "" : getValue("sClientID"));

        if (!"".equals(id)) {
            if (poClientEmail.getEditMode() == EditMode.READY
                    && poClientEmail.getClientId().equals(id)) {
                return poClientEmail;
            } else {
                poJSON = poClientEmail.openRecord(id);

                if ("success".equals(poJSON.get("result"))) {
                    return poClientEmail;
                } else {
                    poClientEmail.initialize();
                    return poClientEmail;
                }
            }
        } else {
            poClientEmail.initialize();
            return poClientEmail;
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
    //end - reference object models

}
