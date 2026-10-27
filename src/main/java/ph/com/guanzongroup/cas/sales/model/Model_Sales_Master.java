package ph.com.guanzongroup.cas.sales.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.ClientInfo;
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Mail;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.model.Model_Client_Mobile;
import org.guanzon.cas.client.model.Model_Client_Social_Media;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.model.Model_Term;
import org.guanzon.cas.parameter.model.Model_xxxTransactionSource;
import org.json.simple.JSONObject;

public class Model_Sales_Master extends Model {

    private Model_Term poTerm;
    private Model_Branch poBranch;
    private Model_Company poCompany;
    private Model_Category poCategory;
    private Model_Industry poIndustry;
    private Model_Client_Master poClient;
    private Model_Client_Address poClientAddress;
    private Model_Client_Mobile poClientMobile;
    private Model_Client_Master poCoClient;
    private Model_Client_Address poCoClientAddress;
    private Model_Client_Master poSalesmanClient;
    private Model_Client_Mail poClientMail;
    private Model_Client_Mail poCoClientMail;
    private Model_xxxTransactionSource poTransactionSource;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata")
                    + "/Model_Sales_Master.xml", getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            poEntity.updateDouble("nTranTotl", 0.0d);
            poEntity.updateDouble("nAdvances", 0.0d);
            poEntity.updateDouble("nDiscount", 0.0d);
            poEntity.updateDouble("nAddDiscx", 0.0d);
            poEntity.updateDouble("nFreightx", 0.0d);
            poEntity.updateDouble("nInsurAdd", 0.0d);
            poEntity.updateDouble("nRegAmtxx", 0.0d);
            poEntity.updateDouble("nVATSales", 0.0d);
            poEntity.updateDouble("nVATAmtxx", 0.0d);
            poEntity.updateDouble("nNonVATSl", 0.0d);
            poEntity.updateDouble("nZroVATSl", 0.0d);
            poEntity.updateDouble("nCWTAmtxx", 0.0d);
            poEntity.updateDouble("nAmtPaidx", 0.0d);
            poEntity.updateInt("nEntryNox", 0);
            poEntity.updateString("cTranType", "0");
            poEntity.updateString("cPaymForm", "0");
            poEntity.updateString("cHasPromo", "0");
            poEntity.updateString("cTranStat", "0");

            poEntity.updateNull("sSourceCd");
            poEntity.updateNull("sSourceNo");
            poEntity.updateNull("dDueDatex");
            poEntity.updateObject("dDueDatex", poGRider.getServerDate());
            poEntity.updateObject("dTransact", poGRider.getServerDate());

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String value) {
        return setValue("sTransNox", value);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setIndustryCode(String value) {
        return setValue("sIndstCdx", value);
    }

    public String getIndustryCode() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setCategory(String value) {
        return setValue("sCategrCd", value);
    }

    public String getCategory() {
        return (String) getValue("sCategrCd");
    }

    public JSONObject setCompanyID(String value) {
        return setValue("sCompnyID", value);
    }

    public String getCompanyID() {
        return (String) getValue("sCompnyID");
    }

    public JSONObject setBranchCode(String value) {
        return setValue("sBranchCd", value);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setTransactionDate(Date value) {
        return setValue("dTransact", value);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setClientID(String value) {
        return setValue("sClientID", value);
    }

    public String getClientID() {
        return (String) getValue("sClientID");
    }

    public JSONObject setAddressID(String value) {
        return setValue("sAddrssID", value);
    }

    public String getAddressID() {
        return (String) getValue("sAddrssID");
    }

    public JSONObject setContactID(String value) {
        return setValue("sContctID", value);
    }

    public String getContactID() {
        return (String) getValue("sContctID");
    }

    public JSONObject setCompanyName(String value) {
        return setValue("sCompnyNm", value);
    }

    public String getCompanyName() {
        return (String) getValue("sCompnyNm");
    }

    public JSONObject setAddress(String value) {
        return setValue("sAddressx", value);
    }

    public String getAddress() {
        return (String) getValue("sAddressx");
    }

    public JSONObject setMobileNo(String value) {
        return setValue("sMobileNo", value);
    }

    public String getMobileNo() {
        return (String) getValue("sMobileNo");
    }

    public JSONObject setCoClientID(String value) {
        return setValue("sCoCltID1", value);
    }

    public String getCoClientID() {
        return (String) getValue("sCoCltID1");
    }

    public JSONObject setAddressID2(String value) {
        return setValue("sAddress2", value);
    }

    public String getAddressID2() {
        return (String) getValue("sAddress2");
    }

    public JSONObject setRemarks(String value) {
        return setValue("sRemarksx", value);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setSalesman(String value) {
        return setValue("sSalesman", value);
    }

    public String getSalesman() {
        return (String) getValue("sSalesman");
    }

    public JSONObject setTransactionType(String value) {
        return setValue("cTranType", value);
    }

    public String getTransactionType() {
        return (String) getValue("cTranType");
    }

    public JSONObject setPaymentForm(String value) {
        return setValue("cPaymForm", value);
    }

    public String getPaymentForm() {
        return (String) getValue("cPaymForm");
    }

    public JSONObject setTermCode(String value) {
        return setValue("sTermCode", value);
    }

    public String getTermCode() {
        return (String) getValue("sTermCode");
    }

    public JSONObject setTransactionTotal(double value) {
        return setValue("nTranTotl", value);
    }

    public double getTransactionTotal() {
        return Double.valueOf(getValue("nTranTotl").toString());
    }

    public JSONObject setAdvances(double value) {
        return setValue("nAdvances", value);
    }

    public double getAdvances() {
        return Double.valueOf(getValue("nAdvances").toString());
    }

    public JSONObject setDiscount(double value) {
        return setValue("nDiscount", value);
    }

    public double getDiscount() {
        return Double.valueOf(getValue("nDiscount").toString());
    }

    public JSONObject setAddDiscount(double value) {
        return setValue("nAddDiscx", value);
    }

    public double getAddDiscount() {
        return Double.valueOf(getValue("nAddDiscx").toString());
    }

    public JSONObject setFreight(double value) {
        return setValue("nFreightx", value);
    }

    public double getFreight() {
        return Double.valueOf(getValue("nFreightx").toString());
    }

    public JSONObject setInsuranceAdd(double value) {
        return setValue("nInsurAdd", value);
    }

    public double getInsuranceAdd() {
        return Double.valueOf(getValue("nInsurAdd").toString());
    }

    public JSONObject setRegistrationAmount(double value) {
        return setValue("nRegAmtxx", value);
    }

    public double getRegistrationAmount() {
        return Double.valueOf(getValue("nRegAmtxx").toString());
    }

    public JSONObject setVATSales(double value) {
        return setValue("nVATSales", value);
    }

    public double getVATSales() {
        return Double.valueOf(getValue("nVATSales").toString());
    }

    public JSONObject setVATAmount(double value) {
        return setValue("nVATAmtxx", value);
    }

    public double getVATAmount() {

        return Double.valueOf(getValue("nVATAmtxx").toString());
    }

    public JSONObject setNonVATSales(double value) {
        return setValue("nNonVATSl", value);
    }

    public double getNonVATSales() {
        return Double.valueOf(getValue("nNonVATSl").toString());
    }

    public JSONObject setZeroVATSales(double value) {
        return setValue("nZroVATSl", value);
    }

    public double getZeroVATSales() {

        return Double.valueOf(getValue("nZroVATSl").toString());
    }

    public JSONObject setCWTAmount(double value) {
        return setValue("nCWTAmtxx", value);
    }

    public double getCWTAmount() {
        return Double.valueOf(getValue("nCWTAmtxx").toString());
    }

    public JSONObject setAmountPaid(double value) {
        return setValue("nAmtPaidx", value);
    }

    public double getAmountPaid() {
        return Double.valueOf(getValue("nAmtPaidx").toString());
    }

    public JSONObject setDueDate(Date value) {
        return setValue("dDueDatex", value);
    }

    public Date getDueDate() {
        return (Date) getValue("dDueDatex");
    }

    public JSONObject setSourceCode(String value) {
        return setValue("sSourceCd", value);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    public JSONObject setSourceNo(String value) {
        return setValue("sSourceNo", value);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setHasPromo(String value) {
        return setValue("cHasPromo", value);
    }

    public String getHasPromo() {
        return (String) getValue("cHasPromo");
    }

    public JSONObject setEntryNo(int value) {
        return setValue("nEntryNox", value);
    }

    public int getEntryNo() {
        return Integer.valueOf(getValue("nEntryNox").toString());
    }

    public JSONObject setReleased(String value) {
        return setValue("cReleased", value);
    }

    public String getReleased() {
        return (String) getValue("cReleased");
    }

    public boolean isReleased() {
        return "1".equals((String) getValue("cReleased"));
    }

    public JSONObject setTransactionStatus(String value) {
        return setValue("cTranStat", value);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

    public JSONObject setModifiedBy(String value) {
        return setValue("sModified", value);
    }

    public String getModifiedBy() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date value) {
        return setValue("dModified", value);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return MiscUtil.getNextCode(getTable(), ID, true,
                poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    public Model_Client_Master Client() throws SQLException, GuanzonException {

        if (poClient == null) {
            poClient = new Model_Client_Master();
            poClient.setApplicationDriver(poGRider);
            poClient.setXML("Model_Client_Master");
            poClient.setTableName("Client_Master");
            poClient.initialize();
        }

        String clientId = (String) (getValue("sClientID") == null
                ? ""
                : getValue("sClientID"));

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

        String clientId = (String) (getValue("sAddrssID") == null
                ? ""
                : getValue("sAddrssID"));

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

        String contactId = (String) (getValue("sContctID") == null
                ? ""
                : getValue("sContctID"));

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

        String industryCode = (String) (getValue("sIndstCdx") == null
                ? "" : getValue("sIndstCdx"));

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

    public Model_Category Category()
            throws SQLException, GuanzonException {

        if (poCategory == null) {
            poCategory = new Model_Category();
            poCategory.setApplicationDriver(poGRider);
            poCategory.setXML("Model_Category");
            poCategory.setTableName("Category");
            poCategory.initialize();
        }

        String modelPrimary = (String) (getValue("sCategrCd") == null
                ? "" : getValue("sCategrCd"));

        if (!"".equals(modelPrimary)) {

            if (poCategory.getEditMode() == EditMode.READY
                    && poCategory.getCategoryId().equals(modelPrimary)) {
                return poCategory;
            }

            if (ReferenceCache.tryLoad(
                    "Category",
                    modelPrimary,
                    poCategory)) {
                return poCategory;
            }

            poJSON = poCategory.openRecord(modelPrimary);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Category",
                        modelPrimary,
                        poCategory);
                return poCategory;
            } else {
                poCategory.initialize();
                return poCategory;
            }

        } else {
            poCategory.initialize();
            return poCategory;
        }
    }

    public Model_Company Company()
            throws SQLException, GuanzonException {

        if (poCompany == null) {
            poCompany = new Model_Company();
            poCompany.setApplicationDriver(poGRider);
            poCompany.setXML("Model_Company");
            poCompany.setTableName("Company");
            poCompany.initialize();
        }

        String modelPrimary = (String) (getValue("sCompnyID") == null
                ? "" : getValue("sCompnyID"));

        if (!"".equals(modelPrimary)) {

            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals(modelPrimary)) {
                return poCompany;
            }

            if (ReferenceCache.tryLoad(
                    "Company",
                    modelPrimary,
                    poCompany)) {
                return poCompany;
            }

            poJSON = poCompany.openRecord(modelPrimary);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Company",
                        modelPrimary,
                        poCompany);
                return poCompany;
            } else {
                poCompany.initialize();
                return poCompany;
            }

        } else {
            poCompany.initialize();
            return poCompany;
        }
    }

    public Model_Branch Branch()
            throws SQLException, GuanzonException {

        if (poBranch == null) {
            poBranch = new Model_Branch();
            poBranch.setApplicationDriver(poGRider);
            poBranch.setXML("Model_Branch");
            poBranch.setTableName("Branch");
            poBranch.initialize();
        }

        String modelPrimary = (String) (getValue("sBranchCd") == null
                ? "" : getValue("sBranchCd"));

        if (!"".equals(modelPrimary)) {

            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals(modelPrimary)) {
                return poBranch;
            }

            if (ReferenceCache.tryLoad(
                    "Branch",
                    modelPrimary,
                    poBranch)) {
                return poBranch;
            }

            poJSON = poBranch.openRecord(modelPrimary);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Branch",
                        modelPrimary,
                        poBranch);
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

    public Model_Client_Master CoClient() throws SQLException, GuanzonException {

        if (poCoClient == null) {
            poCoClient = new Model_Client_Master();
            poCoClient.setApplicationDriver(poGRider);
            poCoClient.setXML("Model_Client_Master");
            poCoClient.setTableName("Client_Master");
            poCoClient.initialize();
        }

        String clientId = (String) (getValue("sCoCltID1") == null
                ? ""
                : getValue("sCoCltID1"));

        if (!"".equals(clientId)) {

            if (poCoClient.getEditMode() == EditMode.READY
                    && poCoClient.getClientId().equals(clientId)) {
                return poCoClient;
            }

            if (ReferenceCache.tryLoad("Client_Master", clientId, poCoClient)) {
                return poCoClient;
            }

            poJSON = poCoClient.openRecord(clientId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store("Client_Master", clientId, poCoClient);
                return poCoClient;
            } else {
                poCoClient.initialize();
                return poCoClient;
            }

        } else {
            poCoClient.initialize();
            return poCoClient;
        }
    }

    public Model_Client_Address CoClientAddress()
            throws SQLException, GuanzonException {

        if (poCoClientAddress == null) {
            poCoClientAddress = new Model_Client_Address();
            poCoClientAddress.setApplicationDriver(poGRider);
            poCoClientAddress.setXML("Model_Client_Address");
            poCoClientAddress.setTableName("Client_Address");
            poCoClientAddress.initialize();
        }

        String clientId = (String) (getValue("sAddress2") == null
                ? ""
                : getValue("sAddress2"));

        if (!"".equals(clientId)) {

            if (poCoClientAddress.getEditMode() == EditMode.READY
                    && poCoClientAddress.getClientId().equals(clientId)) {
                return poCoClientAddress;
            }

            if (ReferenceCache.tryLoad(
                    "Client_Address",
                    clientId,
                    poCoClientAddress)) {
                return poCoClientAddress;
            }

            poJSON = poCoClientAddress.openRecord(clientId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Client_Address",
                        clientId,
                        poCoClientAddress
                );
                return poCoClientAddress;
            } else {
                poCoClientAddress.initialize();
                return poCoClientAddress;
            }

        } else {
            poCoClientAddress.initialize();
            return poCoClientAddress;
        }
    }

    public Model_Client_Master SalesmanClient() throws SQLException, GuanzonException {

        if (poSalesmanClient == null) {
            poSalesmanClient = new Model_Client_Master();
            poSalesmanClient.setApplicationDriver(poGRider);
            poSalesmanClient.setXML("Model_Client_Master");
            poSalesmanClient.setTableName("Client_Master");
            poSalesmanClient.initialize();
        }

        String clientId = (String) (getValue("sSalesman") == null
                ? ""
                : getValue("sSalesman"));

        if (!"".equals(clientId)) {

            if (poSalesmanClient.getEditMode() == EditMode.READY
                    && poSalesmanClient.getClientId().equals(clientId)) {
                return poSalesmanClient;
            }

            if (ReferenceCache.tryLoad("Client_Master", clientId, poSalesmanClient)) {
                return poSalesmanClient;
            }

            poJSON = poSalesmanClient.openRecord(clientId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store("Client_Master", clientId, poSalesmanClient);
                return poSalesmanClient;
            } else {
                poSalesmanClient.initialize();
                return poSalesmanClient;
            }

        } else {
            poSalesmanClient.initialize();
            return poSalesmanClient;
        }
    }

    public Model_Term Term()
            throws SQLException, GuanzonException {

        if (poTerm == null) {
            poTerm = new Model_Term();
            poTerm.setApplicationDriver(poGRider);
            poTerm.setXML("Model_Term");
            poTerm.setTableName("Term");
            poTerm.initialize();
        }

        String modelPrimary = (String) (getValue("sTermCode") == null
                ? "" : getValue("sTermCode"));

        if (!"".equals(modelPrimary)) {

            if (poTerm.getEditMode() == EditMode.READY
                    && poTerm.getTermId().equals(modelPrimary)) {
                return poTerm;
            }

            if (ReferenceCache.tryLoad(
                    "Term",
                    modelPrimary,
                    poTerm)) {
                return poTerm;
            }

            poJSON = poTerm.openRecord(modelPrimary);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Term",
                        modelPrimary,
                        poTerm);
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

    public Model_Client_Mail ClientMail()
            throws SQLException, GuanzonException {

        if (poClientMail == null) {
            poClientMail = new Model_Client_Mail();
            poClientMail.setApplicationDriver(poGRider);
            poClientMail.setXML("Model_Client_Mail");
            poClientMail.setTableName("Client_eMail_Address");
            poClientMail.initialize();
        }
        String modelClient = (String) (getValue("sClientID") == null
                ? "" : getValue("sClientID"));

        if ("".equals(modelClient)) {
            poClientMail.initialize();
            return poClientMail;
        }

        //load email addresses
        String lsSQL = "SELECT * FROM Client_eMail_Address"
                + " WHERE sClientID = " + SQLUtil.toSQL(modelClient)
                + " ORDER BY sEmailIDx";

        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0L) {

            String modelPrimary = loRS.getString("sEmailIDx") == null
                    ? "" : loRS.getString("sEmailIDx");

            if (!"".equals(modelPrimary)) {

                if (poClientMail.getEditMode() == EditMode.READY
                        && poClientMail.getEmailId().equals(modelPrimary)) {
                    return poClientMail;
                }

                if (ReferenceCache.tryLoad(
                        "Client_eMail_Address",
                        modelPrimary,
                        poClientMail)) {
                    return poClientMail;
                }

                poJSON = poClientMail.openRecord(modelPrimary);

                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store(
                            "Client_eMail_Address",
                            modelPrimary,
                            poClientMail);
                    return poClientMail;
                } else {
                    poClientMail.initialize();
                    return poClientMail;
                }

            } else {
                poClientMail.initialize();
                return poClientMail;
            }
        } else {
            poClientMail.initialize();
            return poClientMail;
        }
    }

    public Model_Client_Mail CoClientMail()
            throws SQLException, GuanzonException {

        if (poCoClientMail == null) {
            poCoClientMail = new Model_Client_Mail();
            poCoClientMail.setApplicationDriver(poGRider);
            poCoClientMail.setXML("Model_Client_Mail");
            poCoClientMail.setTableName("Client_eMail_Address");
            poCoClientMail.initialize();
        }
        String modelClient = (String) (getValue("sCoCltID1") == null
                ? "" : getValue("sCoCltID1"));

        if ("".equals(modelClient)) {
            poCoClientMail.initialize();
            return poCoClientMail;
        }

        //load email addresses
        String lsSQL = "SELECT * FROM Client_eMail_Address"
                + " WHERE sClientID = " + SQLUtil.toSQL(modelClient)
                + " ORDER BY sEmailIDx";

        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) > 0L) {

            String modelPrimary = loRS.getString("sEmailIDx") == null
                    ? "" : loRS.getString("sEmailIDx");

            if (!"".equals(modelPrimary)) {

                if (poCoClientMail.getEditMode() == EditMode.READY
                        && poCoClientMail.getEmailId().equals(modelPrimary)) {
                    return poCoClientMail;
                }

                if (ReferenceCache.tryLoad(
                        "Client_eMail_Address",
                        modelPrimary,
                        poCoClientMail)) {
                    return poCoClientMail;
                }

                poJSON = poCoClientMail.openRecord(modelPrimary);

                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store(
                            "Client_eMail_Address",
                            modelPrimary,
                            poCoClientMail);
                    return poCoClientMail;
                } else {
                    poCoClientMail.initialize();
                    return poCoClientMail;
                }

            } else {
                poCoClientMail.initialize();
                return poCoClientMail;
            }
        } else {
            poCoClientMail.initialize();
            return poCoClientMail;
        }
    }

    public Model_xxxTransactionSource TransactionSource()
            throws SQLException, GuanzonException {

        if (poTransactionSource == null) {
            poTransactionSource = new Model_xxxTransactionSource();
            poTransactionSource.setApplicationDriver(poGRider);
            poTransactionSource.setXML("Model_xxxTransactionSource");
            poTransactionSource.setTableName("xxxTransactionSource");
            poTransactionSource.initialize();
        }
        String modelPrimary = (String) (getValue("sSourceCd") == null
                ? "" : getValue("sSourceCd"));

        if (!"".equals(modelPrimary)) {

            if (poTransactionSource.getEditMode() == EditMode.READY
                    && poTransactionSource.getSourceCode().equals(modelPrimary)) {
                return poTransactionSource;
            }

            if (ReferenceCache.tryLoad(
                    "xxxTransactionSource",
                    modelPrimary,
                    poTransactionSource)) {
                return poTransactionSource;
            }

            poJSON = poTransactionSource.openRecord(modelPrimary);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "xxxTransactionSource",
                        modelPrimary,
                        poTransactionSource);
                return poTransactionSource;
            } else {
                poTransactionSource.initialize();
                return poTransactionSource;
            }

        } else {
            poTransactionSource.initialize();
            return poTransactionSource;
        }
    }
}
