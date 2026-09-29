package ph.com.guanzongroup.cas.sales.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.model.Model_Client_Mobile;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.json.simple.JSONObject;

import ph.com.guanzongroup.cas.sales.status.SalesQoutationStatic;

public class Model_Sales_Quotation_Master extends Model {

    // reference objects
    private Model_Industry poIndustry;
    private Model_Client_Master poClient;
    private Model_Client_Address poClientAddress;
    private Model_Client_Mobile poClientMobile;
    private Model_Sales_Quotation_Version_Master poSalesQuotationVersionMaster;

    private String psClientType = "";

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
            poEntity.updateString("cTranStat", SalesQoutationStatic.OPEN);
            poEntity.updateObject("nVersionx", 0);
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = "sTransNox";

            // Reference objects are intentionally NOT constructed here.
            // They are initialized lazily in Client(), ClientAddress(),
            // ClientMobile(), Industry(), and SalesQuotationVersionMaster().

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

    public void setClientType(String clientType) {
        psClientType = clientType;
    }

    public String getClientType() {
        return psClientType;
    }

    // ============================================================
    // Reference Object Models
    // ============================================================

    public Model_Client_Master Client() throws SQLException, GuanzonException {

        if (poClient == null) {
            poClient = new Model_Client_Master();
            poClient.setApplicationDriver(poGRider);
            poClient.setXML("Model_Client_Master");
            poClient.setTableName("Client_Master");
            poClient.initialize();
        }

        String clientId = (String) (
                getValue("sClientID") == null
                        ? ""
                        : getValue("sClientID")
        );

        if (!"".equals(clientId)) {

            if (poClient.getEditMode() == EditMode.READY
                    && poClient.getClientId().equals(clientId)) {
                return poClient;
            }

            if (ReferenceCache.tryLoad("Client_Master", clientId, poClient)) {
                return poClient;
            }

            poJSON = poClient.openRecord(clientId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store("Client_Master", clientId, poClient);
                return poClient;
            } else {
                poClient.initialize();
                return poClient;
            }

        } else {
            poClient.initialize();
            return poClient;
        }
    }

    public Model_Client_Address ClientAddress()
            throws SQLException, GuanzonException {

        if (poClientAddress == null) {
            poClientAddress = new Model_Client_Address();
            poClientAddress.setApplicationDriver(poGRider);
            poClientAddress.setXML("Model_Client_Address");
            poClientAddress.setTableName("Client_Address");
            poClientAddress.initialize();
        }

        String clientId = (String) (
                getValue("sClientID") == null
                        ? ""
                        : getValue("sClientID")
        );

        if (!"".equals(clientId)) {

            if (poClientAddress.getEditMode() == EditMode.READY
                    && poClientAddress.getClientId().equals(clientId)) {
                return poClientAddress;
            }

            if (ReferenceCache.tryLoad(
                    "Client_Address",
                    clientId,
                    poClientAddress)) {
                return poClientAddress;
            }

            poJSON = poClientAddress.openRecord(clientId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Client_Address",
                        clientId,
                        poClientAddress
                );
                return poClientAddress;
            } else {
                poClientAddress.initialize();
                return poClientAddress;
            }

        } else {
            poClientAddress.initialize();
            return poClientAddress;
        }
    }

    public Model_Client_Mobile ClientMobile()
            throws SQLException, GuanzonException {

        if (poClientMobile == null) {
            poClientMobile = new Model_Client_Mobile();
            poClientMobile.setApplicationDriver(poGRider);
            poClientMobile.setXML("Model_Client_Mobile");
            poClientMobile.setTableName("Client_Mobile");
            poClientMobile.initialize();
        }

        String contactId = (String) (
                getValue("sContctID") == null
                        ? ""
                        : getValue("sContctID")
        );

        if (!"".equals(contactId)) {

            if (poClientMobile.getEditMode() == EditMode.READY
                    && poClientMobile.getClientId().equals(contactId)) {
                return poClientMobile;
            }

            if (ReferenceCache.tryLoad(
                    "Client_Mobile",
                    contactId,
                    poClientMobile)) {
                return poClientMobile;
            }

            poJSON = poClientMobile.openRecord(contactId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Client_Mobile",
                        contactId,
                        poClientMobile
                );
                return poClientMobile;
            } else {
                poClientMobile.initialize();
                return poClientMobile;
            }

        } else {
            poClientMobile.initialize();
            return poClientMobile;
        }
    }

    public Model_Industry Industry()
            throws SQLException, GuanzonException {

        if (poIndustry == null) {
            poIndustry = new Model_Industry();
            poIndustry.setApplicationDriver(poGRider);
            poIndustry.setXML("Model_Industry");
            poIndustry.setTableName("Industry");
            poIndustry.initialize();
        }

        String industryCode = (String) (
                getValue("sIndstCdx") == null
                        ? ""
                        : getValue("sIndstCdx")
        );

        if (!"".equals(industryCode)) {

            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals(industryCode)) {
                return poIndustry;
            }

            if (ReferenceCache.tryLoad(
                    "Industry",
                    industryCode,
                    poIndustry)) {
                return poIndustry;
            }

            poJSON = poIndustry.openRecord(industryCode);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Industry",
                        industryCode,
                        poIndustry
                );
                return poIndustry;
            } else {
                poIndustry.initialize();
                return poIndustry;
            }

        } else {
            poIndustry.initialize();
            return poIndustry;
        }
    }

    public Model_Sales_Quotation_Version_Master SalesQuotationVersionMaster()
            throws SQLException, GuanzonException {

        if (poSalesQuotationVersionMaster == null) {
            poSalesQuotationVersionMaster =
                    new Model_Sales_Quotation_Version_Master();

            poSalesQuotationVersionMaster.setApplicationDriver(poGRider);
            poSalesQuotationVersionMaster.setXML(
                    "Model_Sales_Quotation_Version_Master"
            );
            poSalesQuotationVersionMaster.setTableName(
                    "Sales_Quotation_Version_Master"
            );
            poSalesQuotationVersionMaster.initialize();
        }

        String transactionNo = (String) (
                getValue("sTransNox") == null
                        ? ""
                        : getValue("sTransNox")
        );

        if ("".equals(transactionNo)) {
            poSalesQuotationVersionMaster.initialize();
            return poSalesQuotationVersionMaster;
        }

        if (poSalesQuotationVersionMaster.getEditMode() == EditMode.READY
                && transactionNo.equals(
                poSalesQuotationVersionMaster.getParentId())) {
            return poSalesQuotationVersionMaster;
        }

        String versionNo = "";

        String sql = "SELECT sTransNox FROM "
                + poSalesQuotationVersionMaster.getTable()
                + " WHERE sParentID = "
                + SQLUtil.toSQL(transactionNo)
                + " ORDER BY sTransNox DESC LIMIT 1";

        ResultSet rs = poGRider.executeQuery(sql);

        try {
            if (rs.next()) {
                versionNo = rs.getString("sTransNox");
            }
        } finally {
            MiscUtil.close(rs);
        }

        if (!"".equals(versionNo)) {

            poJSON = poSalesQuotationVersionMaster.openRecord(versionNo);

            if ("success".equals((String) poJSON.get("result"))) {
                return poSalesQuotationVersionMaster;
            }
        }

        poSalesQuotationVersionMaster.initialize();

        return poSalesQuotationVersionMaster;
    }

    // ============================================================
    // Next Code
    // ============================================================

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(
                getTable(),
                ID,
                true,
                poGRider.getGConnection().getConnection(),
                poGRider.getBranchCode()
        );
    }
}