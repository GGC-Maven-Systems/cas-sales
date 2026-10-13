package ph.com.guanzongroup.cas.sales.mcpromo.model;

import java.sql.SQLException;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.json.simple.JSONObject;

public class Model_Sales_Promotion_Brand extends Model {

    private Model_Brand poBrand;

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateObject("nEntryNox", 1);
            poEntity.updateString("cWithIncx", RecordStatus.INACTIVE);
            poEntity.updateString("cInsurFOC", RecordStatus.INACTIVE);
            poEntity.updateString("cRegisFOC", RecordStatus.INACTIVE);
            poEntity.updateString("cCashSale", RecordStatus.INACTIVE);
            poEntity.updateString("cLoanSale", RecordStatus.INACTIVE);
            poEntity.updateDouble("nTotalAmt", 0.00d);
            poEntity.updateDouble("nDiscRate", 0.00d);
            poEntity.updateDouble("nDiscAmtx", 0.00d);
            poEntity.updateDouble("nFreightx", 0.00d);
            poEntity.updateDouble("nAmountxx", 0.00d);
            poEntity.updateString("cActivexx", RecordStatus.ACTIVE);

            //end - assign default values
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            ID = poEntity.getMetaData().getColumnLabel(1);
            ID2 = poEntity.getMetaData().getColumnLabel(2);

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setPromoID(String promoID) {
        return setValue("sPromIDxx", promoID);
    }

    public String getPromoID() {
        return (String) getValue("sPromIDxx");
    }

    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        return (int) getValue("nEntryNox");
    }

    public JSONObject setBrandID(String brandID) {
        return setValue("sBrandIDx", brandID);
    }

    public String getBrandID() {
        return (String) getValue("sBrandIDx");
    }

    public JSONObject setTotalAmount(Double totalAmount) {
        return setValue("nTotalAmt", totalAmount);
    }

    public Double getTotalAmount() {
        return Double.valueOf(getValue("nTotalAmt").toString());
    }

    public JSONObject setDiscountRate(Double discountRate) {
        return setValue("nDiscRate", discountRate);
    }

    public Double getDiscountRate() {
        return Double.valueOf(getValue("nDiscRate").toString());
    }

    public JSONObject setDiscAmount(Double discAmount) {
        return setValue("nDiscAmtx", discAmount);
    }

    public Double getDiscAmount() {
        return Double.valueOf(getValue("nDiscAmtx").toString());
    }

    public JSONObject setFreight(Double freight) {
        return setValue("nFreightx", freight);
    }

    public Double getFreight() {
        return Double.valueOf(getValue("nFreightx").toString());
    }

    public JSONObject setWithIncentive(String withIncentive) {
        return setValue("cWithIncx", withIncentive);
    }

    public String getWithIncentive() {
        return (String) getValue("cWithIncx");
    }

    public JSONObject setAmount(Double amount) {
        return setValue("nAmountxx", amount);
    }

    public Double getAmount() {
        return Double.valueOf(getValue("nAmountxx").toString());
    }

    public JSONObject setInsuranceFree(String insuranceFree) {
        return setValue("cInsurFOC", insuranceFree);
    }

    public String getInsuranceFree() {
        return (String) getValue("cInsurFOC");
    }

    public JSONObject setRegistrationFree(String registrationFree) {
        return setValue("cRegisFOC", registrationFree);
    }

    public String getRegistrationFree() {
        return (String) getValue("cRegisFOC");
    }

    public JSONObject isWithIncentive(boolean isWithIncentive) {
        return setValue("cWithIncx", isWithIncentive == true ? "1" : "0");
    }

    public boolean isWithIncentive() {
        return "1".equals((String) getValue("cWithIncx"));
    }

    public JSONObject isInsuranceFree(boolean isInsuranceFree) {
        return setValue("cInsurFOC", isInsuranceFree == true ? "1" : "0");
    }

    public boolean isInsuranceFree() {
        return "1".equals((String) getValue("cInsurFOC"));
    }

    public JSONObject isRegistrationFree(boolean isRegistrationFree) {
        return setValue("cRegisFOC", isRegistrationFree == true ? "1" : "0");
    }

    public boolean isRegistrationFree() {
        return "1".equals((String) getValue("cRegisFOC"));
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setCashSale(String cashSale) {
        return setValue("cCashSale", cashSale);
    }

    public String getCashSale() {
        return (String) getValue("cCashSale");
    }

    public JSONObject setLoanSale(String loanSale) {
        return setValue("cLoanSale", loanSale);
    }

    public String getLoanSale() {
        return (String) getValue("cLoanSale");
    }

    public JSONObject isCashSale(boolean isCashSale) {
        return setValue("cCashSale", isCashSale == true ? "1" : "0");
    }

    public boolean isCashSale() {
        return "1".equals((String) getValue("cCashSale"));
    }

    public JSONObject isLoanSale(boolean isLoanSale) {
        return setValue("cLoanSale", isLoanSale == true ? "1" : "0");
    }

    public boolean isLoanSale() {
        return "1".equals((String) getValue("cLoanSale"));
    }

    public JSONObject setActive(String active) {
        return setValue("cActivexx", active);
    }

    public String getActive() {
        return (String) getValue("cActivexx");
    }

    public JSONObject isWithActive(boolean isWithActive) {
        return setValue("cActivexx", isWithActive == true ? "1" : "0");
    }

    public boolean isWithActive() {
        return "1".equals((String) getValue("cActivexx"));
    }

    @Override
    public String getNextCode() {
        return "";
    }

    public Model_Brand Brand() throws SQLException, GuanzonException {
        if (poBrand == null) {
            poBrand = new Model_Brand();
            poBrand.setApplicationDriver(poGRider);
            poBrand.setXML("Model_Brand");
            poBrand.setTableName("Brand");
            poBrand.initialize();
        }

        String brandID = getValue("sBrandIDx") == null ? "" : (String) getValue("sBrandIDx");

        if (!"".equals(brandID)) {
            if (poBrand.getEditMode() == EditMode.READY
                    && poBrand.getBrandId().equals(brandID)) {
                return poBrand;
            } else {
                if (ReferenceCache.tryLoad("Brand", brandID, poBrand)) {
                    return poBrand;
                }

                poJSON = poBrand.openRecord(brandID);

                if ("success".equals((String) poJSON.get("result"))) {
                    ReferenceCache.store("Brand", brandID, poBrand);
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

}
