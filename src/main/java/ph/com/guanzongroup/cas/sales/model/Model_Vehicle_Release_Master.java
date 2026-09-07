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
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.SalesInquiryStatic;

import java.sql.SQLException;
import java.util.Date;

/**
 * Model class for the Vehicle Release Master transaction.
 *
 * <p>
 * This class represents the master record of a Vehicle Release transaction
 * within the CAS Sales module. It encapsulates the transaction data,
 * initializes default values, provides access to database fields through
 * getter and setter methods, and manages related reference models used
 * throughout the transaction lifecycle.
 * </p>
 *
 * <p>
 * In addition to maintaining the Vehicle Release record, this model provides
 * convenient access to its associated reference objects, including:
 * </p>
 * <ul>
 *     <li>Client Master</li>
 *     <li>Client Address</li>
 *     <li>Client Mobile</li>
 *     <li>Client Social Media</li>
 *     <li>Salesperson</li>
 * </ul>
 *
 * <p>
 * Upon initialization, the model loads its metadata, assigns default values,
 * initializes its reference objects, and prepares the entity for create,
 * retrieve, update, and save operations.
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date August 7, 2026
 * @module Vehicle Release
 * @since 1.0
 */
public class Model_Vehicle_Release_Master extends Model {

    String psClientType = "";

    //reference objects
//    Model_Branch poBranch;
//    Model_Industry poIndustry;
//    Model_Company poCompany;
    Model_Client_Master poClient;
    Model_Client_Address poClientAddress;
    Model_Client_Social_Media poClientSocMed;
    Model_Client_Mobile poClientMobile;
    //    Model_Client_Master poAgent;
    Model_Salesman poSalesPerson;
//    Model_Sales_Inquiry_Sources poSource;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
//            poEntity.updateObject("dTransact", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
//            poEntity.updateObject("dModified", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
//            poEntity.updateObject("dTargetxx", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));
//            poEntity.updateObject("dFollowUp", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));

            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateNull("dModified");
            poEntity.updateNull("dTargetxx");
            poEntity.updateNull("dFollowUp");

            poEntity.updateString("cProcessd", "0");
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateString("cInqrStat", SalesInquiryStatic.OPEN);
            poEntity.updateString("cTranStat", SalesInquiryStatic.OPEN);
            psClientType = "0";
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            //initialize reference objects
//            ParamModels model = new ParamModels(poGRider);
//            poBranch = model.Branch();
//            poIndustry = model.Industry();
//            poCompany = model.Company();
//
            ClientModels clientModel = new ClientModels(poGRider);
            poClient = clientModel.ClientMaster();
            poClientAddress = clientModel.ClientAddress();
            poClientMobile = clientModel.ClientMobile();
            poClientSocMed = clientModel.ClientSocMed();
//
            SalesModels sales = new SalesModels(poGRider);
            poSalesPerson = sales.Salesman();

//            end - initialize reference objects

//            pnEditMode = EditMode.UNKNOWN;
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

    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setReferenceNo(String referenceNo) {
        return setValue("sReferNox", referenceNo);
    }

    public String getReferenceNo() {
        return (String) getValue("sReferNox");
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

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCD", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCD");
    }

    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
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

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    //reference object models
//    public Model_Branch Branch() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sBranchCd"))) {
//            if (poBranch.getEditMode() == EditMode.READY
//                    && poBranch.getBranchCode().equals((String) getValue("sBranchCd"))) {
//                return poBranch;
//            } else {
//                poJSON = poBranch.openRecord((String) getValue("sBranchCd"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poBranch;
//                } else {
//                    poBranch.initialize();
//                    return poBranch;
//                }
//            }
//        } else {
//            poBranch.initialize();
//            return poBranch;
//        }
//    }
//
//    public Model_Industry Industry() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sIndstCdx"))) {
//            if (poIndustry.getEditMode() == EditMode.READY
//                    && poIndustry.getIndustryId().equals((String) getValue("sIndstCdx"))) {
//                return poIndustry;
//            } else {
//                poJSON = poIndustry.openRecord((String) getValue("sIndstCdx"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poIndustry;
//                } else {
//                    poIndustry.initialize();
//                    return poIndustry;
//                }
//            }
//        } else {
//            poIndustry.initialize();
//            return poIndustry;
//        }
//    }
//
//    public Model_Company Company() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sCompnyID"))) {
//            if (poCompany.getEditMode() == EditMode.READY
//                    && poCompany.getCompanyId().equals((String) getValue("sCompnyID"))) {
//                return poCompany;
//            } else {
//                poJSON = poCompany.openRecord((String) getValue("sCompnyID"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poCompany;
//                } else {
//                    poCompany.initialize();
//                    return poCompany;
//                }
//            }
//        } else {
//            poCompany.initialize();
//            return poCompany;
//        }
//    }

    /**
     * Retrieves the client associated with the current Vehicle Release transaction.
     *
     * <p>
     * If a Client ID has been assigned, this method attempts to return the
     * currently loaded client model. If the model is not loaded or does not
     * match the current Client ID, the corresponding client record is retrieved
     * from the database. If the client cannot be found, an initialized empty
     * client model is returned.
     * </p>
     *
     * @return the {@link Model_Client_Master} associated with the current
     *         transaction; otherwise, an initialized empty client model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the client record.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the client information.
     *
     * @author TEEJEI DE CELIS
     * @date August 7, 2026
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
     * Vehicle Release transaction.
     *
     * <p>
     * If a Client ID has been assigned, this method loads the corresponding
     * client address record. When the address has already been loaded and
     * matches the current Client ID, the cached instance is returned.
     * Otherwise, the address is retrieved from the database. If no address
     * exists, an initialized empty address model is returned.
     * </p>
     *
     * @return the {@link Model_Client_Address} associated with the current
     *         client; otherwise, an initialized empty address model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the client address.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the client address.
     *
     * @author TEEJEI DE CELIS
     * @date August 7, 2026
     */
    public Model_Client_Address ClientAddress() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poClientAddress.getEditMode() == EditMode.READY
                    && poClientAddress.getClientId().equals((String) getValue("sClientID"))) {
                return poClientAddress;
            } else {
                poJSON = poClientAddress.openRecord((String) getValue("sClientID")); //sAddrssID

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
     * current Vehicle Release transaction.
     *
     * <p>
     * If a Client ID has been assigned, this method returns the corresponding
     * social media record. When the record is already loaded and matches the
     * current Client ID, the cached instance is returned. Otherwise, the record
     * is retrieved from the database. If no social media information exists,
     * an initialized empty model is returned.
     * </p>
     *
     * @return the {@link Model_Client_Social_Media} associated with the current
     *         client; otherwise, an initialized empty social media model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the client's social media information.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the social media information.
     *
     * @author TEEJEI DE CELIS
     * @date August 7, 2026
     */
    public Model_Client_Social_Media ClientSocMed() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poClientSocMed.getEditMode() == EditMode.READY
                    && poClientSocMed.getClientId().equals((String) getValue("sClientID"))) {
                return poClientSocMed;
            } else {
                poJSON = poClientSocMed.openRecord((String) getValue("sClientID")); //sAddrssID

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
     * Vehicle Release transaction.
     *
     * <p>
     * If a Contact ID has been assigned, this method returns the corresponding
     * mobile record. When the record is already loaded and matches the current
     * Contact ID, the cached instance is returned. Otherwise, the mobile record
     * is retrieved from the database. If no mobile information exists, an
     * initialized empty model is returned.
     * </p>
     *
     * @return the {@link Model_Client_Mobile} associated with the current
     *         transaction; otherwise, an initialized empty mobile model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the mobile contact information.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the mobile contact information.
     *
     * @author TEEJEI DE CELIS
     * @date August 7, 2026
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
     * Retrieves the salesperson associated with the current Vehicle Release
     * transaction.
     *
     * <p>
     * If a Salesperson ID has been assigned, this method returns the
     * corresponding salesperson record. When the record is already loaded and
     * matches the current Salesperson ID, the cached instance is returned.
     * Otherwise, the salesperson record is retrieved from the database. If the
     * salesperson cannot be found, an initialized empty model is returned.
     * </p>
     *
     * @return the {@link Model_Salesman} associated with the current
     *         transaction; otherwise, an initialized empty salesperson model.
     *
     * @throws SQLException if a database access error occurs while retrieving
     *         the salesperson record.
     * @throws GuanzonException if an application-specific error occurs while
     *         loading the salesperson information.
     *
     * @author TEEJEI DE CELIS
     * @date August 7, 2026
     */
    public Model_Salesman SalesPerson() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sSalesman"))) {
            if (poSalesPerson.getEditMode() == EditMode.READY
                    && poSalesPerson.getEmployeeId().equals((String) getValue("sSalesman"))) {
                return poSalesPerson;
            } else {
                poJSON = poSalesPerson.openRecord((String) getValue("sSalesman"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poSalesPerson;
                } else {
                    poSalesPerson.initialize();
                    return poSalesPerson;
                }
            }
        } else {
            poSalesPerson.initialize();
            return poSalesPerson;
        }
    }

//    public Model_Client_Master ReferralAgent() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sAgentIDx"))) {
//            if (poAgent.getEditMode() == EditMode.READY
//                    && poAgent.getClientId().equals((String) getValue("sAgentIDx"))) {
//                return poAgent;
//            } else {
//                poJSON = poAgent.openRecord((String) getValue("sAgentIDx"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poAgent;
//                } else {
//                    poAgent.initialize();
//                    return poAgent;
//                }
//            }
//        } else {
//            poAgent.initialize();
//            return poAgent;
//        }
//    }


    //end - reference object models

}
