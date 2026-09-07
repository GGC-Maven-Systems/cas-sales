/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
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
import ph.com.guanzongroup.cas.sales.constant.Sales_Reservation_Static;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author User
 */
public class Model_Sales_Reservation_Master extends Model {

    Model_Branch poBranch;
    Model_Company poCompany;
    Model_Industry poIndustry;
    
    Model_Client_Master poClientMaster;
    Model_Client_Address poClientAddress;
    Model_Client_Mobile poClientMobile;
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();
            
            Date dTransact = SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE);
            poEntity.updateObject("dTransact", dTransact);


            Calendar cal = Calendar.getInstance();
            cal.setTime(dTransact);
            cal.add(Calendar.MONTH, 1);
            
            MiscUtil.initRowSet(poEntity);
            poEntity.updateObject("dTransact", SQLUtil.toDate(xsDateShort(poGRider.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("dExpected", SQLUtil.toDate(xsDateShort(cal.getTime()), SQLUtil.FORMAT_SHORT_DATE));
            poEntity.updateObject("nEntryNox", Sales_Reservation_Static.DefaultValues.default_zero_integer);
            poEntity.updateObject("nTranTotl", Sales_Reservation_Static.DefaultValues.default_zero_amount_double);            
            poEntity.updateObject("nRefundxx", Sales_Reservation_Static.DefaultValues.default_zero_amount_double);
            poEntity.updateObject("nAmtPaidx", Sales_Reservation_Static.DefaultValues.default_zero_amount_double);
            poEntity.updateObject("nAppliedx", Sales_Reservation_Static.DefaultValues.default_zero_amount_double);
            poEntity.updateString("cTranStat", Sales_Reservation_Static.OPEN);
            
            poEntity.updateNull("dEntryDte");

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);
            ID = "sTransNox";

            ClientModels modelClient = new ClientModels(poGRider);
            poClientMaster = modelClient.ClientMaster();
            poClientAddress = modelClient.ClientAddress();
//            poAPClient = model.ClientMaster();
            
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poCompany = model.Company();
            poIndustry = model.Industry();

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setIndustryID(String industryID) {
        return setValue("sIndstCdx", industryID);
    }

    public String getIndustryID() {
        return (String) getValue("sIndstCdx");
    }
    
    public JSONObject setCompanyID(String companyID) {
        return setValue("sCompnyID", companyID);
    }

    public String getCompanyID() {
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
    
    public JSONObject setExpectedDate(Date transactionDate) {
        return setValue("dExpected", transactionDate);
    }

    public Date getExpectedDate() {
        return (Date) getValue("dExpected");
    }
    
    public JSONObject setClientID(String clientID) {
        return setValue("sClientID", clientID);
    }

    public String getClientID() {
        return (String) getValue("sClientID");
    }
        
    public JSONObject setAddressID(String addressID) {
        return setValue("sAddrssID", addressID);
    }

    public String getAddressID() {
        return (String) getValue("sAddrssID");
    }
        
    public JSONObject setContactID(String contactID) {
        return setValue("sContctID", contactID);
    }

    public String getContactID() {
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
    
    public JSONObject setTransactionTotal(double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public double getTransactionTotal() {
        return Double.parseDouble(String.valueOf(getValue("nTranTotl")));
    }
      
    public JSONObject setAmountPaid(double amountPaid) {
        return setValue("nAmtPaidx", amountPaid);
    }

    public double getAmountPaid() {
        return Double.parseDouble(String.valueOf(getValue("nAmtPaidx")));
    }
          
    public JSONObject setApplied(double applied) {
        return setValue("nAppliedx", applied);
    }

    public double getApplied() {
        return Double.parseDouble(String.valueOf(getValue("nAppliedx")));
    }
          
    public JSONObject setRefund(double refund) {
        return setValue("nRefundxx", refund);
    }

    public double getRefund() {
        return Double.parseDouble(String.valueOf(getValue("nRefundxx")));
    }
    
    public JSONObject isPreOrder(boolean isPreOrder) {
        return setValue("cPreOrder", isPreOrder ? "1" : "0");
    }

    public boolean isPreOrder() {
        return ((String) getValue("cPreOrder")).equals("1");
    }
    
            
    public JSONObject setPayload(JSONObject payload) {
        return setValue("sPayLoadx", payload);
    }

    public JSONObject getPayload() {
        return (JSONObject) getValue("sPayLoadx");
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
    
    public JSONObject setPaymentType(String paymentType) {
        return setValue("cPaymType", paymentType);
    }

    public String getPaymentType() {
        return (String) getValue("cPaymType");
    } 
    
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }
    
    public JSONObject setEntryBy(String entryBy) {
        return setValue("sEntryByx", entryBy);
    }

    public String getEntryBy() {
        return (String) getValue("sEntryByx");
    }

    public JSONObject setEntryDate(Date entryDate) {
        return setValue("dEntryDte", entryDate);
    }

    public Date getEntryDate() {
        return (Date) getValue("dEntryDte");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    public JSONObject setModifyingId(String modifyingId) {
        return setValue("sModified", modifyingId);
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


    public Model_Branch Branch() throws GuanzonException, SQLException {
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

    public Model_Company Company() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sCompnyID"))) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals((String) getValue("sCompnyID"))) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord((String) getValue("sCompnyID"));

                if ("success".equals((String) poJSON.get("result"))) {
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

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sIndstCdx"))) {
            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals((String) getValue("sIndstCdx"))) {
                return poIndustry;
            } else {
                poJSON = poIndustry.openRecord((String) getValue("sIndstCdx"));

                if ("success".equals((String) poJSON.get("result"))) {
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

    public Model_Client_Master Client_Master() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poClientMaster.getEditMode() == EditMode.READY
                    && poClientMaster.getClientId().equals((String) getValue("sClientID"))) {
                return poClientMaster;
            } else {
                poJSON = poClientMaster.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poClientMaster;
                } else {
                    poClientMaster.initialize();
                    return poClientMaster;
                }
            }
        } else {
            poClientMaster.initialize();
            return poClientMaster;
        }
    }
    public Model_Client_Address Client_Address() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poClientAddress.getEditMode() == EditMode.READY
                    && poClientAddress.getClientId().equals((String) getValue("sAddressID"))) {
                return poClientAddress;
            } else {
                poJSON = poClientAddress.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result"))) {
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

    

}
