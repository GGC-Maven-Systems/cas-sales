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
public class Model_Sales_Quotations_Master extends Model {

    //reference objects
    Model_Industry poIndustry;
    Model_Client_Master poClient;
    Model_Client_Address poClientAddress;
    Model_Client_Mobile poClientMobile;

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
            poEntity.updateNull("dModified");

            poEntity.updateString("cTranStat", "");
            poEntity.updateInt("nVersionx", 0);
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

            ParamModels paramModel = new ParamModels(poGRider);
            poIndustry = paramModel.Industry();
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

    public JSONObject setVersion(Integer version) {
        return setValue("nVersionx", version);
    }

    public Integer getVersion() {
        return (Integer) getValue("nVersionx");
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
     * Retrieves the client associated with the current Sales Quotation
     * transaction.
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
     * Sales Quotation transaction.
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
     * Retrieves the mobile contact information associated with the current
     * Sales Quotation transaction.
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
     * Retrieves the industry associated with the current Sales Quotation
     * transaction.
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
     * @return the {@link Model_Industry} associated with the current Sales
     *         Quotation transaction, or an initialized industry model when no
     *         valid industry record is available.
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

    // end - reference object models
}