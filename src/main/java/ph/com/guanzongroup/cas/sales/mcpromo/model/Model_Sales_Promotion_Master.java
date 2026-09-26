package ph.com.guanzongroup.cas.sales.mcpromo.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.json.simple.JSONObject;

public class Model_Sales_Promotion_Master extends Model {

    private Model_Industry poIndustry;
    private Model_Client_Master poClient;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            
            //assign default values
            poEntity.updateObject("dTransact", poGRider.getServerDate());
            poEntity.updateString("sPromType", RecordStatus.INACTIVE);
            poEntity.updateString("cTranType", RecordStatus.INACTIVE);
            poEntity.updateString("cPromoSrc", RecordStatus.INACTIVE);
            poEntity.updateString("cAllBrand", RecordStatus.INACTIVE);
            poEntity.updateString("cAllModel", RecordStatus.INACTIVE);
            poEntity.updateString("cAllProvx", RecordStatus.INACTIVE);
            poEntity.updateString("cAllAreax", RecordStatus.INACTIVE);
            poEntity.updateString("cAllBanks", RecordStatus.INACTIVE);
            poEntity.updateString("cTranStat", TransactionStatus.STATE_OPEN);
            poEntity.updateDouble("nAmtFromx", 0.00d);
            poEntity.updateDouble("nAmtToxxx", 0.00d);
            poEntity.updateString("cGiveAway", RecordStatus.INACTIVE);
            poEntity.updateString("cCredtCrd", RecordStatus.INACTIVE);
            poEntity.updateString("cInstlmnt", RecordStatus.INACTIVE);
            poEntity.updateString("cPreOrder", RecordStatus.INACTIVE);
            poEntity.updateNull("sClientID");
            poEntity.updateObject("dFromDate", poGRider.getServerDate());
            poEntity.updateObject("dThruDate", poGRider.getServerDate());
            poEntity.updateObject("dModified", poGRider.getServerDate());
            
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }
//        sPromIDxx/
//        sPromDesc/
//        sIndstCdx/
//        dTransact/
//        sCategrCd/
//        sPromType/
//        cTranType/
//        cPromoSrc/
//        sReferNox/
//        sClientID/
//        sRemarksx/
//        dFromDate/
//        dThruDate/
//        cAllBrand/
//        cAllModel/
//        cAllAreax/
//        cAllProvx/
//        cAllBanks/
//        sShopType/
//        nAmtFromx/
//        nAmtToxxx/
//        cGiveAway/
//        cCredtCrd/
//        cInstlmnt/
//        cPreOrder/
//        cTranStat/
//        sModified/
//        dModified/

    public JSONObject setPromoID(String promoID) {
        return setValue("sPromIDxx", promoID);
    }

    public String getPromoID() {
        return (String) getValue("sPromIDxx");
    }

    public JSONObject setPromoDescription(String promoDescriptionn) {
        return setValue("sPromDesc", promoDescriptionn);
    }

    public String getPromoDescription() {
        return (String) getValue("sPromDesc");
    }

    public JSONObject setIndustryCode(String industryCode) {
        return setValue("sIndstCdx", industryCode);
    }

    public String getIndustryCode() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setDate(Date Date) {
        return setValue("dTransact", Date);
    }

    public Date getDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setCategory(String category) {
        return setValue("sCategrCd", category);
    }

    public String getCategory() {
        return (String) getValue("sCategrCd");
    }

    public JSONObject setPromoType(String promoType) {
        return setValue("sPromType", promoType);
    }

    public String getPromoType() {
        return (String) getValue("sPromType");
    }

    public JSONObject setTransactionType(String transactionType) {
        return setValue("cTranType", transactionType);
    }

    public String getTransactionType() {
        return (String) getValue("cTranType");
    }

    public JSONObject setPromoSource(String promoSource) {
        return setValue("cPromoSrc", promoSource);
    }

    public String getPromoSource() {
        return (String) getValue("cPromoSrc");
    }

    public JSONObject setReferNo(String referNo) {
        return setValue("sReferNox", referNo);
    }

    public String getReferNo() {
        return (String) getValue("sReferNox");
    }

    public JSONObject setClientID(String referNo) {
        return setValue("sClientID", referNo);
    }

    public String getClientID() {
        return (String) getValue("sClientID");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setFromDate(Date fromDate) {
        return setValue("dFromDate", fromDate);
    }

    public Date getFromDate() {
        return (Date) getValue("dFromDate");
    }

    public JSONObject setThruDate(Date thruDate) {
        return setValue("dThruDate", thruDate);
    }

    public Date getThruDate() {
        return (Date) getValue("dThruDate");
    }

    public JSONObject setAllBrand(String allBrand) {
        return setValue("cAllBrand", allBrand);
    }

    public String getAllBrand() {
        return (String) getValue("cAllBrand");
    }

    public JSONObject setAllModel(String allModel) {
        return setValue("cAllModel", allModel);
    }

    public String getAllModel() {
        return (String) getValue("cAllModel");
    }

    public JSONObject setAllArea(String allAreax) {
        return setValue("cAllAreax", allAreax);
    }

    public String getAllArea() {
        return (String) getValue("cAllAreax");
    }

    public JSONObject setAllProvince(String allProvx) {
        return setValue("cAllProvx", allProvx);
    }

    public String getAllProvince() {
        return (String) getValue("cAllProvx");
    }

    public JSONObject setAllBanks(String allBanks) {
        return setValue("cAllBanks", allBanks);
    }

    public String getAllBanks() {
        return (String) getValue("cAllBanks");
    }

    public JSONObject isAllBrand(boolean isAllBrand) {
        return setValue("cAllBrand", isAllBrand == true ? "1" : "0");
    }

    public boolean isAllBrand() {
        return "1".equals((String) getValue("cAllBrand"));
    }

    public JSONObject isAllModel(boolean isAllModel) {
        return setValue("cAllModel", isAllModel == true ? "1" : "0");
    }

    public boolean isAllModel() {
        return "1".equals((String) getValue("cAllModel"));
    }

    public JSONObject isAllArea(boolean isAllArea) {
        return setValue("cAllAreax", isAllArea == true ? "1" : "0");
    }

    public boolean isAllArea() {
        return "1".equals((String) getValue("cAllAreax"));
    }

    public JSONObject isAllProvince(boolean isAllProvince) {
        return setValue("cAllProvx", isAllProvince == true ? "1" : "0");
    }

    public boolean isAllProvince() {
        return "1".equals((String) getValue("cAllProvx"));
    }

    public JSONObject isAllBanks(boolean isAllBanks) {
        return setValue("cAllBanks", isAllBanks == true ? "1" : "0");
    }

    public boolean isAllBanks() {
        return "1".equals((String) getValue("cAllBanks"));
    }

    public JSONObject setShopType(String shopType) {
        return setValue("sShopType", shopType);
    }

    public String getShopType() {
        return (String) getValue("sShopType");
    }

    public JSONObject setAmountFrom(Double amountFrom) {
        return setValue("nAmtFromx", amountFrom);
    }

    public Double getAmountFrom() {
        return Double.valueOf(getValue("nAmtFromx").toString());
    }

    public JSONObject setAmountTo(Double amountTo) {
        return setValue("nAmtToxxx", amountTo);
    }

    public Double getAmountTo() {
        return Double.valueOf(getValue("nAmtToxxx").toString());
    }

    public JSONObject setGiveAway(String giveAway) {
        return setValue("cGiveAway", giveAway);
    }

    public String getGiveAway() {
        return (String) getValue("cGiveAway");
    }

    public JSONObject setCreditCard(String creditCard) {
        return setValue("cCredtCrd", creditCard);
    }

    public String getCreditCard() {
        return (String) getValue("cCredtCrd");
    }

    public JSONObject setInstallment(String installment) {
        return setValue("cInstlmnt", installment);
    }

    public String getInstallment() {
        return (String) getValue("cInstlmnt");
    }

    public JSONObject setPreOrder(String preOrder) {
        return setValue("cPreOrder", preOrder);
    }

    public String getPreOrder() {
        return (String) getValue("cPreOrder");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }

//        cGiveAway/
//        /
//        /
//        /
    public JSONObject isGiveAway(boolean isGiveAway) {
        return setValue("cGiveAway", isGiveAway == true ? "1" : "0");
    }

    public boolean isGiveAway() {
        return "1".equals((String) getValue("cGiveAway"));
    }

    public JSONObject isCreditCard(boolean isCreditCard) {
        return setValue("cCredtCrd", isCreditCard == true ? "1" : "0");
    }

    public boolean isCreditCard() {
        return "1".equals((String) getValue("cCredtCrd"));
    }

    public JSONObject isInstallment(boolean isInstallment) {
        return setValue("cInstlmnt", isInstallment == true ? "1" : "0");
    }

    public boolean isInstallment() {
        return "1".equals((String) getValue("cInstlmnt"));
    }

    public JSONObject isPreOrder(boolean isInstallment) {
        return setValue("cPreOrder", isInstallment == true ? "1" : "0");
    }

    public boolean isPreOrder() {
        return "1".equals((String) getValue("cPreOrder"));
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

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (poIndustry == null) {
            poIndustry = new Model_Industry();
            poIndustry.setApplicationDriver(poGRider);
            poIndustry.setXML("Model_Industry");
            poIndustry.setTableName("Industry");
            poIndustry.initialize();
        }

        String industryId = (String) getValue("sIndstCdx");

        if (!"".equals(industryId)) {
            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals(industryId)) {
                return poIndustry;
            } else {
                if (ReferenceCache.tryLoad("Industry", industryId, poIndustry)) {
                    return poIndustry;
                }

                poJSON = poIndustry.openRecord(industryId);

                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store("Industry", industryId, poIndustry);
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
    
    public Model_Client_Master ClientMaster() throws SQLException, GuanzonException {
        if (poClient == null) {
            poClient = new Model_Client_Master();
            poClient.setApplicationDriver(poGRider);
            poClient.setXML("Model_Client_Master");
            poClient.setTableName("Client_Master");
            poClient.initialize();
        }

        String clientId = getValue("sClientID") == null ? "" : (String) getValue("sClientID");

        if (!"".equals(clientId)) {
            if (poClient.getEditMode() == EditMode.READY
                    && poClient.getClientId().equals(clientId)) {
                return poClient;
            } else {
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
            }
        } else {
            poClient.initialize();
            return poClient;
        }
    }
}
