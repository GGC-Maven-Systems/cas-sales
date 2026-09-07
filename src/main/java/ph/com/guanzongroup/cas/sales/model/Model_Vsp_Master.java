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
import org.guanzon.cas.client.model.Model_Client_Social_Media;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.Date;

/**
 * Model class for the VSP Master transaction.
 *
 * <p>
 * This class represents the master record of a VSP transaction
 * within the CAS Sales module. It encapsulates transaction information
 * including transaction details, branch, industry, company, client,
 * vehicle, pricing, discounts, payment information, and transaction status.
 * </p>
 *
 * <p>
 * In addition to maintaining the VSP Master record, this model provides
 * convenient access to its associated reference objects, including:
 * </p>
 * <ul>
 *     <li>Branch</li>
 *     <li>Industry</li>
 *     <li>Company</li>
 *     <li>Client Master</li>
 *     <li>Client Address</li>
 *     <li>Client Mobile</li>
 *     <li>Client Social Media</li>
 * </ul>
 *
 * <p>
 * The model also maintains vehicle information, transaction pricing,
 * freight charges, promotional and fleet discounts, supplier and dealer
 * amounts, reservation and payment amounts, sales commission information,
 * printing status, transaction status, and audit information.
 * </p>
 *
 * <p>
 * Upon initialization, the model loads its metadata, assigns default values,
 * initializes its reference objects, and prepares the entity for create,
 * retrieve, update, and save operations.
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date August 14, 2026
 * @module VSP Master
 * @since 1.0
 */
public class Model_Vsp_Master extends Model {

    //reference objects
    Model_Branch poBranch;
    Model_Industry poIndustry;
    Model_Company poCompany;
    Model_Client_Master poClient;
    Model_Client_Address poClientAddress;
    Model_Client_Social_Media poClientSocMed;
    Model_Client_Mobile poClientMobile;
    //    Model_Client_Master poAgent;

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
            poEntity.updateNull("dDelvryDt");
            poEntity.updateNull("dPrintxxx");
            poEntity.updateNull("dModified");

            poEntity.updateString("cTranStat", "");
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            // initialize reference objects
            ClientModels clientModel = new ClientModels(poGRider);
            poClient = clientModel.ClientMaster();
            poClientAddress = clientModel.ClientAddress();
            poClientMobile = clientModel.ClientMobile();
            poClientSocMed = clientModel.ClientSocMed();

            ParamModels paramModel = new ParamModels(poGRider);
            poBranch = paramModel.Branch();
            poIndustry = paramModel.Industry();
            poCompany = paramModel.Company();


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

    public JSONObject setIndustryCode(String industryCode) {
        return setValue("sIndstCdx", industryCode);
    }

    public String getIndustryCode() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCD", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCD");
    }

    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyID", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyID");
    }

    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setDeliveryDate(Date deliveryDate) {
        return setValue("dDelvryDt", deliveryDate);
    }

    public Date getDeliveryDate() {
        return (Date) getValue("dDelvryDt");
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

    public JSONObject setVehicleNew(String vehicleNew) {
        return setValue("cIsVhclNw", vehicleNew);
    }

    public String getVehicleNew() {
        return (String) getValue("cIsVhclNw");
    }

    public JSONObject setVIP(String vip) {
        return setValue("cIsVIPxxx", vip);
    }

    public String getVIP() {
        return (String) getValue("cIsVIPxxx");
    }

    public JSONObject setSerialId(String serialId) {
        return setValue("sSerialID", serialId);
    }

    public String getSerialId() {
        return (String) getValue("sSerialID");
    }

    public JSONObject setEndPlate(String endPlate) {
        return setValue("sEndPlate", endPlate);
    }

    public String getEndPlate() {
        return (String) getValue("sEndPlate");
    }

    public JSONObject setUnitPrice(Double unitPrice) {
        return setValue("nUnitPrce", unitPrice);
    }

    public Double getUnitPrice() {
        return (Double) getValue("nUnitPrce");
    }

    public JSONObject setLaborAmount(Double laborAmount) {
        return setValue("nLaborAmt", laborAmount);
    }

    public Double getLaborAmount() {
        return (Double) getValue("nLaborAmt");
    }

    public JSONObject setAccessoriesAmount(Double accessoriesAmount) {
        return setValue("nAccesAmt", accessoriesAmount);
    }

    public Double getAccessoriesAmount() {
        return (Double) getValue("nAccesAmt");
    }

    public JSONObject setPayload(String payload) {
        return setValue("sPayLoadx", payload);
    }

    public String getPayload() {
        return (String) getValue("sPayLoadx");
    }

    public JSONObject setFreightCharge(Double freightCharge) {
        return setValue("nFrgtChrg", freightCharge);
    }

    public Double getFreightCharge() {
        return (Double) getValue("nFrgtChrg");
    }

    public JSONObject setPromoDiscount(Double promoDiscount) {
        return setValue("nPromoDsc", promoDiscount);
    }

    public Double getPromoDiscount() {
        return (Double) getValue("nPromoDsc");
    }

    public JSONObject setFleetDiscount(Double fleetDiscount) {
        return setValue("nFleetDsc", fleetDiscount);
    }

    public Double getFleetDiscount() {
        return (Double) getValue("nFleetDsc");
    }

    public JSONObject setSPFleetDiscount(Double spFleetDiscount) {
        return setValue("nSPFltDsc", spFleetDiscount);
    }

    public Double getSPFleetDiscount() {
        return (Double) getValue("nSPFltDsc");
    }

    public JSONObject setBundleDiscount(Double bundleDiscount) {
        return setValue("nBndleDsc", bundleDiscount);
    }

    public Double getBundleDiscount() {
        return (Double) getValue("nBndleDsc");
    }

    public JSONObject setAdditionalDiscount(Double additionalDiscount) {
        return setValue("nAddlDscx", additionalDiscount);
    }

    public Double getAdditionalDiscount() {
        return (Double) getValue("nAddlDscx");
    }

    public JSONObject setDueToSupplier(Double dueToSupplier) {
        return setValue("nDue2Supx", dueToSupplier);
    }

    public Double getDueToSupplier() {
        return (Double) getValue("nDue2Supx");
    }

    public JSONObject setDueToDealer(Double dueToDealer) {
        return setValue("nDue2Dlrx", dueToDealer);
    }

    public Double getDueToDealer() {
        return (Double) getValue("nDue2Dlrx");
    }

    public JSONObject setSPFDueToSupplier(Double spfDueToSupplier) {
        return setValue("nSPFD2Sup", spfDueToSupplier);
    }

    public Double getSPFDueToSupplier() {
        return (Double) getValue("nSPFD2Sup");
    }

    public JSONObject setSPFDueToDealer(Double spfDueToDealer) {
        return setValue("nSPFD2Dlr", spfDueToDealer);
    }

    public Double getSPFDueToDealer() {
        return (Double) getValue("nSPFD2Dlr");
    }

    public JSONObject setPremiumDueToSupplier(Double premiumDueToSupplier) {
        return setValue("nPrmD2Sup", premiumDueToSupplier);
    }

    public Double getPremiumDueToSupplier() {
        return (Double) getValue("nPrmD2Sup");
    }

    public JSONObject setPremiumDueToDealer(Double premiumDueToDealer) {
        return setValue("nPrmD2Dlr", premiumDueToDealer);
    }

    public Double getPremiumDueToDealer() {
        return (Double) getValue("nPrmD2Dlr");
    }

    public JSONObject setReservationFee(Double reservationFee) {
        return setValue("nResrvFee", reservationFee);
    }

    public Double getReservationFee() {
        return (Double) getValue("nResrvFee");
    }

    public JSONObject setDownPayment(Double downPayment) {
        return setValue("nDownPaym", downPayment);
    }

    public Double getDownPayment() {
        return (Double) getValue("nDownPaym");
    }

    public JSONObject setFullPayment(Double fullPayment) {
        return setValue("nFullPaym", fullPayment);
    }

    public Double getFullPayment() {
        return (Double) getValue("nFullPaym");
    }

    public JSONObject setTransactionTotal(Double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Double getTransactionTotal() {
        return (Double) getValue("nTranTotl");
    }

    public JSONObject setAmountPaid(Double amountPaid) {
        return setValue("nAmtPaidx", amountPaid);
    }

    public Double getAmountPaid() {
        return (Double) getValue("nAmtPaidx");
    }

    public JSONObject setPaymentMode(String paymentMode) {
        return setValue("cPayModex", paymentMode);
    }

    public String getPaymentMode() {
        return (String) getValue("cPayModex");
    }

    public JSONObject setSalesCommissionCode(String salesCommissionCode) {
        return setValue("sSlsComCD", salesCommissionCode);
    }

    public String getSalesCommissionCode() {
        return (String) getValue("sSlsComCD");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setPrintStatus(String printStatus) {
        return setValue("cPrintxxx", printStatus);
    }

    public String getPrintStatus() {
        return (String) getValue("cPrintxxx");
    }

    public JSONObject setPrintDate(Date printDate) {
        return setValue("dPrintxxx", printDate);
    }

    public Date getPrintDate() {
        return (Date) getValue("dPrintxxx");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
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
     * Retrieves the client associated with the current VSP transaction.
     *
     * @return the {@link Model_Client_Master} associated with the current
     *         transaction; otherwise, an initialized empty client model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the client record.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the client information.
     */
    public Model_Client_Master Client() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poClient.getEditMode() == EditMode.READY
                    && poClient.getClientId().equals((String) getValue("sClientID"))) {

                return poClient;

            } else {
                poJSON = poClient.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result"))) {
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

    /**
     * Retrieves the primary address of the client associated with the current
     * VSP transaction.
     *
     * @return the {@link Model_Client_Address} associated with the current
     *         client; otherwise, an initialized empty address model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the client address.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the client address.
     */
    public Model_Client_Address ClientAddress() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poClientAddress.getEditMode() == EditMode.READY
                    && poClientAddress.getClientId().equals((String) getValue("sClientID"))) {

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

    /**
     * Retrieves the social media information of the client associated with the
     * current VSP transaction.
     *
     * @return the {@link Model_Client_Social_Media} associated with the current
     *         client; otherwise, an initialized empty social media model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the client's social media information.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the social media information.
     */
    public Model_Client_Social_Media ClientSocMed() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poClientSocMed.getEditMode() == EditMode.READY
                    && poClientSocMed.getClientId().equals((String) getValue("sClientID"))) {

                return poClientSocMed;

            } else {
                poJSON = poClientSocMed.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poClientSocMed;
                } else {
                    poClientSocMed.initialize();
                    return poClientSocMed;
                }
            }
        } else {
            poClientSocMed.initialize();
            return poClientSocMed;
        }
    }

    /**
     * Retrieves the mobile contact information associated with the current
     * VSP transaction.
     *
     * @return the {@link Model_Client_Mobile} associated with the current
     *         transaction; otherwise, an initialized empty mobile model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the mobile contact information.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the mobile information.
     */
    public Model_Client_Mobile ClientMobile() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sContctID"))) {
            if (poClientMobile.getEditMode() == EditMode.READY
                    && poClientMobile.getClientId().equals((String) getValue("sContctID"))) {

                return poClientMobile;

            } else {
                poJSON = poClientMobile.openRecord((String) getValue("sContctID"));

                if ("success".equals((String) poJSON.get("result"))) {
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
    /**
     * Retrieves the industry associated with the current VSP transaction.
     *
     * <p>
     * If an industry code is assigned to the current transaction, this method
     * checks whether the existing industry model is already loaded for the same
     * industry. If it is ready and matches the current industry code, the
     * existing model is returned. Otherwise, the industry record is opened using
     * the current industry code.
     * </p>
     *
     * <p>
     * If the industry record cannot be opened successfully, the industry model
     * is reinitialized and returned. When no industry code is assigned to the
     * current transaction, the industry model is also reinitialized.
     * </p>
     *
     * @return the {@link Model_Industry} associated with the current VSP
     *         transaction, or an initialized industry model when no valid
     *         industry record is available.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the industry record.
     * @throws GuanzonException if an application-specific error occurs while
     *         processing the industry record.
     */

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
    /**
     * Retrieves the branch associated with the current VSP transaction.
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
     * @return the {@link Model_Branch} associated with the current VSP
     *         transaction, or an initialized branch model when no valid
     *         branch record is available.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the branch record.
     * @throws GuanzonException if an application-specific error occurs while
     *         processing the branch record.
     */
    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sBranchCD"))) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals((String) getValue("sBranchCD"))) {

                return poBranch;

            } else {
                poJSON = poBranch.openRecord((String) getValue("sBranchCD"));

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
    /**
     * Retrieves the company associated with the current VSP transaction.
     *
     * <p>
     * If a company ID is assigned to the current transaction, this method
     * checks whether the existing company model is already loaded for the same
     * company. If it is ready and matches the current company ID, the existing
     * model is returned. Otherwise, the company record is opened using the
     * current company ID.
     * </p>
     *
     * <p>
     * If the company record cannot be opened successfully, the company model
     * is reinitialized and returned. When no company ID is assigned to the
     * current transaction, the company model is also reinitialized.
     * </p>
     *
     * @return the {@link Model_Company} associated with the current VSP
     *         transaction, or an initialized company model when no valid
     *         company record is available.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the company record.
     * @throws GuanzonException if an application-specific error occurs while
     *         processing the company record.
     */
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

    // end - reference object models
}