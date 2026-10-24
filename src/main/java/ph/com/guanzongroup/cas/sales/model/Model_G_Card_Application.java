package ph.com.guanzongroup.cas.sales.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;

/** Auto-generated from Model_G_Card_Application.xml (table: G_Card_Application). */
public class Model_G_Card_Application extends Model {

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata")
                    + "/Model_G_Card_Application.xml", getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            poEntity.updateString("cTranStat", "0");   // OPEN
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

    public JSONObject setTransNo(String value) {
        return setValue("sTransNox", value);
    }

    public String getTransNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setTransactionDate(Date value) {
        return setValue("dTransact", value);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setSourceCode(String value) {
        return setValue("sSourceCd", value);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
    }

    public JSONObject setModelID(String value) {
        return setValue("sModelIDx", value);
    }

    public String getModelID() {
        return (String) getValue("sModelIDx");
    }

    public JSONObject setPurchaseMode(String value) {
        return setValue("cPurcMode", value);
    }

    public String getPurchaseMode() {
        return (String) getValue("cPurcMode");
    }

    public JSONObject setAccountTerm(int value) {
        return setValue("nAcctTerm", value);
    }

    public int getAccountTerm() {
        Object v = getValue("nAcctTerm");
        return v == null ? 0 : ((Number) v).intValue();
    }

    public JSONObject setClientID(String value) {
        return setValue("sClientID", value);
    }

    public String getClientID() {
        return (String) getValue("sClientID");
    }

    public JSONObject setSourceInfo(String value) {
        return setValue("sSrceInfo", value);
    }

    public String getSourceInfo() {
        return (String) getValue("sSrceInfo");
    }

    public JSONObject setReasons(String value) {
        return setValue("sReasonsx", value);
    }

    public String getReasons() {
        return (String) getValue("sReasonsx");
    }

    public JSONObject setCardType(String value) {
        return setValue("cCardType", value);
    }

    public String getCardType() {
        return (String) getValue("cCardType");
    }

    public JSONObject setApplicationType(String value) {
        return setValue("cApplType", value);
    }

    public String getApplicationType() {
        return (String) getValue("cApplType");
    }

    public JSONObject setPreviousGCard(String value) {
        return setValue("sPrevGCrd", value);
    }

    public String getPreviousGCard() {
        return (String) getValue("sPrevGCrd");
    }

    public JSONObject setSourceNo(String value) {
        return setValue("sSourceNo", value);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setAmountPaid(double value) {
        return setValue("nAmtPaidx", value);
    }

    public double getAmountPaid() {
        Object v = getValue("nAmtPaidx");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setNameOnCard(String value) {
        return setValue("sNmOnCard", value);
    }

    public String getNameOnCard() {
        return (String) getValue("sNmOnCard");
    }

    public JSONObject setCompanyID(String value) {
        return setValue("sCompnyID", value);
    }

    public String getCompanyID() {
        return (String) getValue("sCompnyID");
    }

    public JSONObject setSoldBy(String value) {
        return setValue("sSoldByxx", value);
    }

    public String getSoldBy() {
        return (String) getValue("sSoldByxx");
    }

    public JSONObject setSerialID(String value) {
        return setValue("sSerialID", value);
    }

    public String getSerialID() {
        return (String) getValue("sSerialID");
    }

    public JSONObject setYellow(int value) {
        return setValue("nYellowxx", value);
    }

    public int getYellow() {
        Object v = getValue("nYellowxx");
        return v == null ? 0 : ((Number) v).intValue();
    }

    public JSONObject setWhite(int value) {
        return setValue("nWhitexxx", value);
    }

    public int getWhite() {
        Object v = getValue("nWhitexxx");
        return v == null ? 0 : ((Number) v).intValue();
    }

    public JSONObject setPoints(double value) {
        return setValue("nPointsxx", value);
    }

    public double getPoints() {
        Object v = getValue("nPointsxx");
        return v == null ? 0.00 : ((Number) v).doubleValue();
    }

    public JSONObject setDigital(String value) {
        return setValue("cDigitalx", value);
    }

    public String getDigital() {
        return (String) getValue("cDigitalx");
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
}
