package ph.com.guanzongroup.cas.sales.mcpromo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.account.AP_Client_Master;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.inv.InventoryBrowse;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.inv.warehouse.report.ReportUtil;
import org.guanzon.cas.inv.warehouse.report.ReportUtilListener;
import org.guanzon.cas.parameter.BranchArea;
import org.guanzon.cas.parameter.Province;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.mcpromo.common.SalesPromotionStatus;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Branch_Area;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Brand;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_GiveAway_Item;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Master;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Model;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Model_Exception;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Province;
import ph.com.guanzongroup.cas.sales.mcpromo.services.PromoModels;
import ph.com.guanzongroup.cas.sales.mcpromo.validator.MCPromoSalesValidatorFactory;
import ph.com.guanzongroup.cas.sales.mcpromo.common.PromoMaintenancePrint;

public class MCPromoSales extends Transaction {

    private String psIndustryCode = "";
    private String psCompanyID = "";
    private String psCategorCD = "";
    private List<Model> paMaster;
    private List<Model> paSalesPromotionProvince;
    private List<Model> paSalesPromotionBranchArea;
    private List<Model> paSalesPromotionBrand;
    private List<Model> paSalesPromotionModel;
    private List<Model> paSalesPromotionModelException;
    private List<Model> paSalesPromotionGiveAwayItem;

    public void setIndustryID(String industryId) {
        psIndustryCode = industryId;
    }

    public void setCompanyID(String companyId) {
        psCompanyID = companyId;
    }

    public void setCategoryID(String categoryId) {
        psCategorCD = categoryId;
    }

    public Model_Sales_Promotion_Master getMaster() {
        return (Model_Sales_Promotion_Master) poMaster;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Promotion_Master> getMasterList() {
        return (List<Model_Sales_Promotion_Master>) (List<?>) paMaster;
    }

    public Model_Sales_Promotion_Master getMaster(int masterRow) {
        return (Model_Sales_Promotion_Master) paMaster.get(masterRow);

    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Promotion_Province> getSalesPromotionProvinceList() {
        return (List<Model_Sales_Promotion_Province>) (List<?>) paSalesPromotionProvince;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Promotion_Branch_Area> getSalesPromotionBranchAreaList() {
        return (List<Model_Sales_Promotion_Branch_Area>) (List<?>) paSalesPromotionBranchArea;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Promotion_Model> getSalesPromotionModelList() {
        return (List<Model_Sales_Promotion_Model>) (List<?>) paSalesPromotionModel;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Promotion_Brand> getSalesPromotionBrandList() {
        return (List<Model_Sales_Promotion_Brand>) (List<?>) paSalesPromotionBrand;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Promotion_Model_Exception> getSalesPromotionModelExceptionList() {
        return (List<Model_Sales_Promotion_Model_Exception>) (List<?>) paSalesPromotionModelException;
    }

    @SuppressWarnings("unchecked")
    public List<Model_Sales_Promotion_GiveAway_Item> getSalesPromotionGiveAwayItemList() {
        return (List<Model_Sales_Promotion_GiveAway_Item>) (List<?>) paSalesPromotionGiveAwayItem;
    }

    public int getCountSalesPromotionProvince() {
        return paSalesPromotionProvince.size();
    }

    public int getCountSalesPromotionBranchArea() {
        return paSalesPromotionBranchArea.size();
    }

    public int getCountSalesPromotionBrand() {
        return paSalesPromotionBrand.size();
    }

    public int getCountSalesPromotionModel() {
        return paSalesPromotionModel.size();
    }

    public int getCountSalesPromotionModelException() {
        return paSalesPromotionModelException.size();
    }

    public int getCountSalesPromotionGiveAwayItem() {
        return paSalesPromotionGiveAwayItem.size();
    }

    public Model_Sales_Promotion_Province getSalesPromotionProvince(int fnEntryNo) {
        if (getMaster().getPromoID().isEmpty()) {
            return null;
        }

        // Get the requested detail
        Model_Sales_Promotion_Province loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesPromotionProvince.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Promotion_Province) paSalesPromotionProvince.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                return loDetail;
            }
        }

        // No match found — create new
        loDetail = new PromoModels(poGRider).SalesPromotionProvince();
        loDetail.newRecord();
        loDetail.setPromoID(getMaster().getPromoID());
        loDetail.setEntryNo(fnEntryNo);
        paSalesPromotionProvince.add(loDetail);

        return loDetail;
    }

    public Model_Sales_Promotion_Branch_Area getSalesPromotionBranchArea(int fnEntryNo) {
        if (getMaster().getPromoID().isEmpty()) {
            return null;
        }
        // Get the requested detail
        Model_Sales_Promotion_Branch_Area loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesPromotionBranchArea.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Promotion_Branch_Area) paSalesPromotionBranchArea.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                return loDetail;
            }
        }

        // No match found — create new
        loDetail = new PromoModels(poGRider).SalesPromotionBranchArea();
        loDetail.newRecord();
        loDetail.setPromoID(getMaster().getPromoID());
        loDetail.setEntryNo(fnEntryNo);
        paSalesPromotionBranchArea.add(loDetail);

        return loDetail;
    }

    public Model_Sales_Promotion_Model getSalesPromotionModel(int fnEntryNo) {
        if (getMaster().getPromoID().isEmpty()) {
            return null;
        }

        // Get the requested detail
        Model_Sales_Promotion_Model loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesPromotionModel.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Promotion_Model) paSalesPromotionModel.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                return loDetail;
            }
        }

        // No match found — create new
        loDetail = new PromoModels(poGRider).SalesPromotionModel();
        loDetail.newRecord();
        loDetail.setPromoID(getMaster().getPromoID());
        loDetail.setEntryNo(fnEntryNo);
        paSalesPromotionModel.add(loDetail);

        return loDetail;
    }

    public Model_Sales_Promotion_Model_Exception getSalesPromotionModelException(int fnEntryNo) {
        if (getMaster().getPromoID().isEmpty()) {
            return null;
        }

        // Get the requested detail
        Model_Sales_Promotion_Model_Exception loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesPromotionModelException.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Promotion_Model_Exception) paSalesPromotionModelException.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                return loDetail;
            }
        }
        loDetail = new PromoModels(poGRider).SalesPromotionModelException();
        loDetail.newRecord();
        loDetail.setPromoID(getMaster().getPromoID());
        loDetail.setEntryNo(fnEntryNo);
        paSalesPromotionModelException.add(loDetail);

        return loDetail;
    }

    public Model_Sales_Promotion_Brand getSalesPromotionBrand(int fnEntryNo) {
        if (getMaster().getPromoID().isEmpty()) {
            return null;
        }
        // Get the requested detail
        Model_Sales_Promotion_Brand loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesPromotionBrand.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Promotion_Brand) paSalesPromotionBrand.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                return loDetail;
            }
        }
        loDetail = new PromoModels(poGRider).SalesPromotionBrand();
        loDetail.newRecord();
        loDetail.setPromoID(getMaster().getPromoID());
        loDetail.setEntryNo(fnEntryNo);
        paSalesPromotionBrand.add(loDetail);

        return loDetail;
    }

    public Model_Sales_Promotion_GiveAway_Item getSalesPromotionGiveAway(int fnEntryNo) {
        if (getMaster().getPromoID().isEmpty()) {
            return null;
        }
        // Get the requested detail
        Model_Sales_Promotion_GiveAway_Item loDetail;

        //find the detail record
        for (int lnCtr = 0; lnCtr <= paSalesPromotionGiveAwayItem.size() - 1; lnCtr++) {
            loDetail = (Model_Sales_Promotion_GiveAway_Item) paSalesPromotionGiveAwayItem.get(lnCtr);

            if (loDetail.getEntryNo() == fnEntryNo) {
                return loDetail;
            }

        }

        // No match found — create new
        loDetail = new PromoModels(poGRider).SalesPromotionGiveAwayItem();
        loDetail.newRecord();
        loDetail.setPromoID(getMaster().getPromoID());
        loDetail.setEntryNo(fnEntryNo);
        paSalesPromotionGiveAwayItem.add(loDetail);

        return loDetail;
    }

    public JSONObject initTransaction() throws GuanzonException, SQLException {
        SOURCE_CODE = "MCSp";

        poMaster = new PromoModels(poGRider).SalesPromotionMaster();

        //Dummy
        poDetail = new PromoModels(poGRider).SalesPromotionMaster();

        //Other Detail
        paSalesPromotionProvince = new ArrayList<Model>();
        paSalesPromotionBranchArea = new ArrayList<Model>();
        paSalesPromotionBrand = new ArrayList<Model>();
        paSalesPromotionModel = new ArrayList<Model>();
        paSalesPromotionModelException = new ArrayList<Model>();
        paSalesPromotionGiveAwayItem = new ArrayList<Model>();

        pbWithUI = true; //allow input

        return super.initialize();
    }

    public JSONObject searchTransaction(String value, boolean byCode, boolean byExact) {
        try {
            String lsSQL = SQL_BROWSE;
            String lsCondition = "";
            if (psTranStat != null) {
                if (this.psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                        lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                    }
                    lsCondition = "cTranStat IN (" + lsCondition.substring(2) + ")";
                } else {
                    lsCondition = "cTranStat = " + SQLUtil.toSQL(this.psTranStat);
                }
                lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
            }

            if (!psCategorCD.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sCategrCd = " + SQLUtil.toSQL(psCategorCD));
            }
            poJSON = ShowDialogFX.Search(poGRider,
                    lsSQL,
                    value,
                    "Promo ID»sPromDesc»Date»From Date»Thru Date",
                    "sPromIDxx»sPromDesc»dTransact»dFromDate»dThruDate",
                    "sPromIDxx»sPromDesc»dTransact»dFromDate»dThruDate",
                    byExact ? (byCode ? 0 : 1) : 2);

            if (poJSON != null) {
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    return openTransaction((String) poJSON.get("sPromIDxx"));
                }
            } else {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "No record loaded.");
                return poJSON;

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(MCPromoSales.class
                    .getName()).log(Level.SEVERE, null, ex);
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject OpenTransaction(String promoID) throws CloneNotSupportedException, SQLException, GuanzonException {
        return openTransaction(promoID);
    }

    public JSONObject openTransaction(String promoID) throws CloneNotSupportedException, SQLException, GuanzonException {
        if (promoID.isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction must not empty.");
            return poJSON;
        }

        poJSON = poMaster.openRecord(promoID);

        if ("error".equals((String) poJSON.get("result"))) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Open Transaction Record.");
            return poJSON;
        }
        paSalesPromotionProvince.clear();
        paSalesPromotionBranchArea.clear();
        paSalesPromotionBrand.clear();
        paSalesPromotionModel.clear();
        paSalesPromotionModelException.clear();
        paSalesPromotionGiveAwayItem.clear();
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Province
        String lsSQL = "SELECT * FROM Sales_Promotion_Province"
                + " WHERE sPromIDxx = " + SQLUtil.toSQL(promoID);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Promotion_Province loDetail = new PromoModels(poGRider).SalesPromotionProvince();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(promoID, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open sales promotion province record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesPromotionProvince.add(loDetail);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Branch_Area
        lsSQL = "SELECT * FROM Sales_Promotion_Branch_Area"
                + " WHERE sPromIDxx = " + SQLUtil.toSQL(promoID);

        loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Promotion_Branch_Area loDetail = new PromoModels(poGRider).SalesPromotionBranchArea();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(promoID, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open sales promotion branch area record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesPromotionBranchArea.add(loDetail);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Brand
        lsSQL = "SELECT * FROM Sales_Promotion_Brand"
                + " WHERE sPromIDxx = " + SQLUtil.toSQL(promoID);

        loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Promotion_Brand loDetail = new PromoModels(poGRider).SalesPromotionBrand();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(promoID, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open sales promotion brand record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesPromotionBrand.add(loDetail);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Model
        lsSQL = "SELECT * FROM Sales_Promotion_Model"
                + " WHERE sPromIDxx = " + SQLUtil.toSQL(promoID);

        loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Promotion_Model loDetail = new PromoModels(poGRider).SalesPromotionModel();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(promoID, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open sales promotion model record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesPromotionModel.add(loDetail);
            }
        }
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open Model Exception
        lsSQL = "SELECT * FROM Sales_Promotion_Model_Exception"
                + " WHERE sPromIDxx = " + SQLUtil.toSQL(promoID);

        loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Promotion_Model_Exception loDetail = new PromoModels(poGRider).SalesPromotionModelException();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(promoID, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open sales promotion model exception record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesPromotionModelException.add(loDetail);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Open GiveAway_Item
        lsSQL = "SELECT * FROM Sales_Promotion_GiveAway_Item"
                + " WHERE sPromIDxx = " + SQLUtil.toSQL(promoID);

        loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0) {
            while (loRS.next()) {
                Model_Sales_Promotion_GiveAway_Item loDetail = new PromoModels(poGRider).SalesPromotionGiveAwayItem();
                loDetail.initialize();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(promoID, loRS.getString("nEntryNox"));

                if ("error".equals((String) poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open sales promotion Give Away Item record.");
                    clear();
                    return poJSON;
                }
                loDetail.updateRecord();

                paSalesPromotionGiveAwayItem.add(loDetail);
            }
        }///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        poEvent = new JSONObject();

        poEvent.put("event", "READY");

        pnEditMode = EditMode.READY;
        pbRecordExist = true;

        poJSON = new JSONObject();

        poJSON.put("result", "success");
        poJSON.put("result", "Transaction loaded successfully");
        return poJSON;
    }

    @Override
    public JSONObject newTransaction() throws CloneNotSupportedException {
        if (!pbInitTran) {
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        poMaster.initialize();
        poMaster.newRecord();

        //clear Data
        paSalesPromotionProvince.clear();
        paSalesPromotionBranchArea.clear();
        paSalesPromotionBrand.clear();
        paSalesPromotionModel.clear();
        paSalesPromotionModelException.clear();
        paSalesPromotionGiveAwayItem.clear();

        poJSON = initFields();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        getMaster().setIndustryCode(psIndustryCode);
        getMaster().setCategory(psCategorCD);

        pnEditMode = EditMode.ADDNEW;

        poEvent = new JSONObject();
        poEvent.put("event", "ADD NEW");

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject willSave() {
        poJSON = new JSONObject();

        poJSON = isEntryOkay(SalesPromotionStatus.OPEN);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        removeOther();
        if (paSalesPromotionProvince.size() > 0) {
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }
        if (paSalesPromotionBranchArea.size() > 0) {

            isEntryOkay(1);
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }
        if (paSalesPromotionBrand.size() > 0) {
            isEntryOkay(2);
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }
        if (paSalesPromotionModel.size() > 0) {

            isEntryOkay(3);
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }
        if (paSalesPromotionModelException.size() > 0) {
            isEntryOkay(4);
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }
        if (paSalesPromotionGiveAwayItem.size() > 0) {
            isEntryOkay(5);
            poJSON = new JSONObject();
            poJSON = isEntryOkay(0);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

        }

        //assign values needed
        poJSON.put("result", "success");
        return poJSON;

    }

    public void removeOther() {
        //Province
        int lnDetailCount = 0;
        for (int lnCtr = paSalesPromotionProvince.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Promotion_Province loDetail = (Model_Sales_Promotion_Province) paSalesPromotionProvince.get(lnCtr);
            if (loDetail.getProvinceID() == null || loDetail.getProvinceID().isEmpty()) {
                paSalesPromotionProvince.remove(lnCtr);
                continue;
            }
        }
        for (int lnCtr = 0; lnCtr < paSalesPromotionProvince.size(); lnCtr++) {
            Model_Sales_Promotion_Province loDetail = (Model_Sales_Promotion_Province) paSalesPromotionProvince.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        //BranchArea
        lnDetailCount = 0;
        for (int lnCtr = paSalesPromotionBranchArea.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Promotion_Branch_Area loDetail = (Model_Sales_Promotion_Branch_Area) paSalesPromotionBranchArea.get(lnCtr);
            if (loDetail.getAreaCode() == null || loDetail.getAreaCode().isEmpty()) {
                paSalesPromotionBranchArea.remove(lnCtr);
                continue;
            }
        }

        for (int lnCtr = 0; lnCtr < paSalesPromotionBranchArea.size(); lnCtr++) {
            Model_Sales_Promotion_Branch_Area loDetail = (Model_Sales_Promotion_Branch_Area) paSalesPromotionBranchArea.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }

        //Brand
        lnDetailCount = 0;
        for (int lnCtr = paSalesPromotionBrand.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Promotion_Brand loDetail = (Model_Sales_Promotion_Brand) paSalesPromotionBrand.get(lnCtr);
            if (loDetail.getBrandID() == null || loDetail.getBrandID().isEmpty()) {
                paSalesPromotionBrand.remove(lnCtr);
                continue;
            }
        }

        for (int lnCtr = 0; lnCtr < paSalesPromotionBrand.size(); lnCtr++) {
            Model_Sales_Promotion_Brand loDetail = (Model_Sales_Promotion_Brand) paSalesPromotionBrand.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        //Model
        lnDetailCount = 0;
        for (int lnCtr = paSalesPromotionModel.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Promotion_Model loDetail = (Model_Sales_Promotion_Model) paSalesPromotionModel.get(lnCtr);
            if (loDetail.getModelID() == null || loDetail.getModelID().isEmpty()) {
                paSalesPromotionModel.remove(lnCtr);
                continue;
            }
        }
        for (int lnCtr = 0; lnCtr < paSalesPromotionModel.size(); lnCtr++) {
            Model_Sales_Promotion_Model loDetail = (Model_Sales_Promotion_Model) paSalesPromotionModel.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        //Model Exception
        lnDetailCount = 0;
        for (int lnCtr = paSalesPromotionModelException.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Promotion_Model_Exception loDetail = (Model_Sales_Promotion_Model_Exception) paSalesPromotionModelException.get(lnCtr);
            if (loDetail.getModelID() == null || loDetail.getModelID().isEmpty()) {
                paSalesPromotionModelException.remove(lnCtr);
                continue;
            }
        }
        for (int lnCtr = 0; lnCtr < paSalesPromotionModelException.size(); lnCtr++) {
            Model_Sales_Promotion_Model_Exception loDetail = (Model_Sales_Promotion_Model_Exception) paSalesPromotionModelException.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        //Give Away Item
        lnDetailCount = 0;
        for (int lnCtr = paSalesPromotionGiveAwayItem.size() - 1; lnCtr >= 0; lnCtr--) {
            Model_Sales_Promotion_GiveAway_Item loDetail = (Model_Sales_Promotion_GiveAway_Item) paSalesPromotionGiveAwayItem.get(lnCtr);
            if (loDetail.getStockID() == null || loDetail.getStockID().isEmpty()) {
                paSalesPromotionGiveAwayItem.remove(lnCtr);
                continue;
            }
        }
        for (int lnCtr = 0; lnCtr < paSalesPromotionGiveAwayItem.size(); lnCtr++) {
            Model_Sales_Promotion_GiveAway_Item loDetail = (Model_Sales_Promotion_GiveAway_Item) paSalesPromotionGiveAwayItem.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
    }

    @Override
    public JSONObject saveTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (!pbInitTran) {
            poJSON.put("result", "error");
            poJSON.put("message", "Object is not initialized.");
            return poJSON;
        }

        if (pnEditMode == EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Saving of unmodified transaction is not allowed.");
            return poJSON;
        }
        poJSON = willSave();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (getEditMode() == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            pdModified = poGRider.getServerDate();
            poMaster.setValue("sModified", poGRider.Encrypt(poGRider.getUserID()));
        }

        poJSON = save();

        if (!pbWthParent) {
            poGRider.beginTrans((String) poEvent.get("event"),
                    poMaster.getTable(),
                    SOURCE_CODE,
                    String.valueOf(poMaster.getValue(1)));
        }

        if ("success".equals((String) poJSON.get("result"))) {
            //save master and detail
            if (pbVerifyEntryNo) {
                poMaster.setValue("nEntryNox", paDetail.size());
            }

            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                poMaster.setValue("dModified", pdModified);
                poJSON = poMaster.saveRecord();

                if ("error".equals((String) poJSON.get("result"))) {
                    if (!pbWthParent) {
                        poGRider.rollbackTrans();
                    }
                    return poJSON;
                }
                //Province Save
                for (int lnCtr = 0; lnCtr <= paSalesPromotionProvince.size() - 1; lnCtr++) {
                    if (paSalesPromotionProvince.get(lnCtr).getValue("sPromIDxx") == null) {
                        continue;
                    }
                    paSalesPromotionProvince.get(lnCtr).setValue("sPromIDxx", String.valueOf(poMaster.getValue(1)));
                    poJSON = paSalesPromotionProvince.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }
                //BranchArea Save
                for (int lnCtr = 0; lnCtr <= paSalesPromotionBranchArea.size() - 1; lnCtr++) {
                    if (paSalesPromotionBranchArea.get(lnCtr).getValue("sPromIDxx") == null) {
                        continue;
                    }
                    paSalesPromotionBranchArea.get(lnCtr).setValue("sPromIDxx", String.valueOf(poMaster.getValue(1)));
                    poJSON = paSalesPromotionBranchArea.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }

                //Brand Save
                for (int lnCtr = 0; lnCtr <= paSalesPromotionBrand.size() - 1; lnCtr++) {
                    if (paSalesPromotionBrand.get(lnCtr).getValue("sPromIDxx") == null) {
                        continue;
                    }
                    paSalesPromotionBrand.get(lnCtr).setValue("sPromIDxx", String.valueOf(poMaster.getValue(1)));
                    poJSON = paSalesPromotionBrand.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }//Model Save
                for (int lnCtr = 0; lnCtr <= paSalesPromotionModel.size() - 1; lnCtr++) {
                    if (paSalesPromotionModel.get(lnCtr).getValue("sPromIDxx") == null) {
                        continue;
                    }
                    paSalesPromotionModel.get(lnCtr).setValue("sPromIDxx", String.valueOf(poMaster.getValue(1)));
                    poJSON = paSalesPromotionModel.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }//Model ExceptionSave
                for (int lnCtr = 0; lnCtr <= paSalesPromotionModelException.size() - 1; lnCtr++) {
                    if (paSalesPromotionModelException.get(lnCtr).getValue("sPromIDxx") == null) {
                        continue;
                    }
                    paSalesPromotionModelException.get(lnCtr).setValue("sPromIDxx", String.valueOf(poMaster.getValue(1)));
                    poJSON = paSalesPromotionModelException.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }//GiveAway Save
                for (int lnCtr = 0; lnCtr <= paSalesPromotionGiveAwayItem.size() - 1; lnCtr++) {
                    if (paSalesPromotionGiveAwayItem.get(lnCtr).getValue("sPromIDxx") == null) {
                        continue;
                    }
                    paSalesPromotionGiveAwayItem.get(lnCtr).setValue("sPromIDxx", String.valueOf(poMaster.getValue(1)));
                    poJSON = paSalesPromotionGiveAwayItem.get(lnCtr).saveRecord();
                    System.out.println(poJSON.get("message"));
                    if ("error".equals((String) poJSON.get("result"))) {
                        if (!pbWthParent) {
                            poGRider.rollbackTrans();
                        }
                        return poJSON;
                    }
                }

            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Edit mode is not allowed to save transaction.");
                return poJSON;
            }
        } else {
            if (!pbWthParent) {
                poGRider.rollbackTrans();
            }

            return poJSON;
        }

        if (!pbWthParent) {
            poGRider.commitTrans();
        }

        pnEditMode = EditMode.UNKNOWN;
        pbRecordExist = true;
//        paMaster = new ArrayList<Model>();
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction saved successfully.");
        openTransaction(getMaster().getPromoID());
        return loJSON;
    }

    public JSONObject UpdateTransaction() {
        poJSON = new JSONObject();
        if (SalesPromotionStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }
        if (SalesPromotionStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }
        if (SalesPromotionStatus.CANCELLED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }
        if (SalesPromotionStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        poJSON = updateTransaction();
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = MCPromoSalesValidatorFactory.make(getMaster().getIndustryCode());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);

        poJSON = loValidator.validate();
        if (poJSON.containsKey("isRequiredApproval") && Boolean.TRUE.equals(poJSON.get("isRequiredApproval"))) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                }
            }
        }

        return poJSON;
    }

    protected JSONObject isEntryOkay(int otherEntry) {
        JSONObject loJSON;
        switch (otherEntry) {
            case 0://Province
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            case 1://Branch Area
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            case 2://Brand
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            case 3://Model 
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            case 4://Model Exception
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            case 5://GiveAway
                loJSON = new JSONObject();
                loJSON.put("result", "success");
                break;
            default:
                loJSON = new JSONObject();
                loJSON.put("result", "error");
                loJSON.put("message", "Key Validator is not configure. Please Inform MIS SEG.");
                break;

        }

        return loJSON;
    }

    public JSONObject CloseTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (SalesPromotionStatus.CONFIRMED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesPromotionStatus.CONFIRMED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, getMaster().getPromoID());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sPromIDxx"),
                "",
                SalesPromotionStatus.CONFIRMED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction confirmed successfully.");

        openTransaction(getMaster().getPromoID());
        return loJSON;
    }

    public JSONObject CancelTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode");
            return poJSON;
        }

        if (SalesPromotionStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesPromotionStatus.CANCELLED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, getMaster().getPromoID());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sPromIDxx"),
                "",
                SalesPromotionStatus.CANCELLED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction cancelled successfully.");

        openTransaction(getMaster().getPromoID());
        return loJSON;
    }

    public JSONObject VoidTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }
        if (SalesPromotionStatus.VOID.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesPromotionStatus.VOID);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, getMaster().getPromoID());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sPromIDxx"),
                "VoidTransaction",
                SalesPromotionStatus.VOID,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction voided successfully.");

        openTransaction(getMaster().getPromoID());
        return loJSON;
    }

    public JSONObject loadTransactionList(String value)
            throws SQLException, GuanzonException, CloneNotSupportedException {
//        poJSON = new JSONObject();

        paMaster.clear();
        String lsSQL = SQL_BROWSE;
        String lsCondition = "";
        if (psTranStat != null) {
            if (this.psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= this.psTranStat.length() - 1; lnCtr++) {
                    lsCondition = lsCondition + ", " + SQLUtil.toSQL(Character.toString(this.psTranStat.charAt(lnCtr)));
                }
                lsCondition = "cTranStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = "cTranStat = " + SQLUtil.toSQL(this.psTranStat);
            }
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        }

        if (!psIndustryCode.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryCode));
        }
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS)
                <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No record found.");
            return poJSON;
        }

        while (loRS.next()) {
            Model_Sales_Promotion_Master loSalesPromotion = new PromoModels(poGRider).SalesPromotionMaster();

            poJSON = loSalesPromotion.openRecord(loRS.getString("sPromIDxx"));

            if ("success".equals((String) poJSON.get("result"))) {
                paMaster.add((Model) loSalesPromotion);
            } else {
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put(
                "result", "success");
        return poJSON;
    }

    public JSONObject PostTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.UPDATE
                && getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid Edit Mode.");
            return poJSON;
        }

        if (SalesPromotionStatus.POSTED.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(SalesPromotionStatus.POSTED);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poGRider.beginTrans("UPDATE STATUS", "PostTransaction", SOURCE_CODE, getMaster().getPromoID());

        poJSON = statusChange(poMaster.getTable(),
                (String) poMaster.getValue("sPromIDxx"),
                "PostTransaction",
                SalesPromotionStatus.POSTED,
                false, true);
        if ("error".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "success");
        loJSON.put("message", "Transaction posted successfully.");
        openTransaction(getMaster().getPromoID());
        return loJSON;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception {
        CachedRowSet crs = getStatusHistory();

        crs.beforeFirst();

        while (crs.next()) {
            switch (crs.getString("cRefrStat")) {
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case SalesPromotionStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case SalesPromotionStatus.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case SalesPromotionStatus.POSTED:
                    crs.updateString("cRefrStat", "POSTED");
                    break;
                case SalesPromotionStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case SalesPromotionStatus.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;

                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat) {
                        case SalesPromotionStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case SalesPromotionStatus.CONFIRMED:
                            crs.updateString("cRefrStat", "CONFIRMED");
                            break;
                        case SalesPromotionStatus.POSTED:
                            crs.updateString("cRefrStat", "POSTED");
                            break;
                        case SalesPromotionStatus.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                        case SalesPromotionStatus.VOID:
                            crs.updateString("cRefrStat", "VOID");
                            break;

                    }
            }
            crs.updateRow();
        }

        JSONObject loJSON = getEntryBy();
        String entryBy = "";
        String entryDate = "";

        if ("success".equals((String) loJSON.get("result"))) {
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }

        showStatusHistoryUI("Sales_Promotion_Master", (String) poMaster.getValue("sPromIDxx"), entryBy, entryDate, crs);
    }

    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL = " SELECT b.sModified, b.dModified "
                + " FROM Sales_Promotion_Master a "
                + " LEFT JOIN xxxAuditLogMaster b ON"
                + " b.sSourceNo = a.sPromIDxx AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(getMaster().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sPromIDxx =  " + SQLUtil.toSQL(getMaster().getPromoID()));
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if (loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))) {
                        if (loRS.getString("sModified").length() > 10) {
                            lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified")));
                        } else {
                            lsEntry = getSysUser(loRS.getString("sModified"));

                        }
                        // Get the LocalDateTime from your result set
                        LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class
                        );
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                        lsEntryDate = dModified.format(formatter);
                    }
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
            return poJSON;
        }

        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }

    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL = " SELECT IFNULL(b.sCompnyNm,'') sCompnyNm FROM xxxSysUser a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId));
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    lsEntry = loRS.getString("sCompnyNm");
                }
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return lsEntry;
    }

    //Searching of Master
    public JSONObject searchPromotionBySupplier(String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        AP_Client_Master object = new ClientControllers(poGRider, logwrapr).APClientMaster();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            getMaster().setClientID(object.getModel().getClientId());
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    //Searching of Promotion 
    public JSONObject searchPromotionByProvince(String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        Province loBrowse = new ParamControllers(poGRider, logwrapr).Province();
        loBrowse.initialize();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        loBrowse.setWithParentClass(true);

        poJSON = loBrowse.searchRecord(value, byCode);
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paSalesPromotionProvince.size() - 1; lnExisting++) {
                Model_Sales_Promotion_Province loExisting = (Model_Sales_Promotion_Province) paSalesPromotionProvince.get(lnExisting);
                if (loExisting.getProvinceID() != null) {
                    if (loExisting.getProvinceID().equals(loBrowse.getModel().getProvinceId())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Province is already exist!");
                        return poJSON;

                    }
                }
            }

            // keep adding into detail
            getSalesPromotionProvince(
                    getCountSalesPromotionProvince() + 1)
                    .setProvinceID(
                            loBrowse.getModel().getProvinceId());

            poJSON = new JSONObject();
            poJSON.put("result", "success");
        }
        return poJSON;
    }

    public JSONObject removeSalesPromotionProvince(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesPromotionProvince.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        paSalesPromotionProvince.remove(entryNo - 1);

        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesPromotionProvince.size(); lnCtr++) {
            Model_Sales_Promotion_Province loDetail = (Model_Sales_Promotion_Province) paSalesPromotionProvince.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }

    public JSONObject searchPromotionByBranchArea(String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        BranchArea loBrowse = new ParamControllers(poGRider, logwrapr).BranchArea();
        loBrowse.initialize();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        loBrowse.setWithParentClass(true);

        poJSON = loBrowse.searchRecord(value, byCode);
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paSalesPromotionBranchArea.size() - 1; lnExisting++) {
                Model_Sales_Promotion_Branch_Area loExisting = (Model_Sales_Promotion_Branch_Area) paSalesPromotionBranchArea.get(lnExisting);
                if (loExisting.getAreaCode() != null) {
                    if (loExisting.getAreaCode().equals(loBrowse.getModel().getAreaCode())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Branch Area is already exist!");
                        return poJSON;

                    }
                }
            }

            // keep adding into detail
            getSalesPromotionBranchArea(
                    getCountSalesPromotionBranchArea() + 1)
                    .setAreaCode(
                            loBrowse.getModel().getAreaCode());

            poJSON = new JSONObject();
            poJSON.put("result", "success");
        }
        return poJSON;
    }

    public JSONObject removeSalesPromotionBranchArea(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesPromotionBranchArea.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        paSalesPromotionBranchArea.remove(entryNo - 1);

        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesPromotionBranchArea.size(); lnCtr++) {
            Model_Sales_Promotion_Branch_Area loDetail = (Model_Sales_Promotion_Branch_Area) paSalesPromotionBranchArea.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }

    public JSONObject searchPromotionByModel(int lnRow, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        org.guanzon.cas.parameter.Model loBrowse = new ParamControllers(poGRider, logwrapr).Model();
        loBrowse.initialize();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        loBrowse.setWithParentClass(true);
        String lsCondition = null;

        if (paSalesPromotionBrand.size() > 0) {
            lsCondition = "";
            for (Object loModel : paSalesPromotionBrand) {
                Model_Sales_Promotion_Brand lsValue = (Model_Sales_Promotion_Brand) loModel;
                lsCondition += ", " + SQLUtil.toSQL(lsValue.Brand().getBrandId());
            }
        }
        if (lsCondition != null) {
            poJSON = loBrowse.searchRecordbyIndustryWCondition(value, byCode, psIndustryCode, "b.sBrandIDx IN(" + lsCondition.substring(2) + ")");
        } else {
            poJSON = loBrowse.searchRecordbyIndustryWCondition(value, byCode, psIndustryCode, null);

        }
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paSalesPromotionModel.size() - 1; lnExisting++) {
                Model_Sales_Promotion_Model loExisting = (Model_Sales_Promotion_Model) paSalesPromotionModel.get(lnExisting);
                if (loExisting.getModelID() != null) {
                    if (loExisting.getModelID().equals(loBrowse.getModel().getModelId())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Model is already exist!");
                        return poJSON;

                    }
                }
            }
            for (int lnExisting = 0; lnExisting <= paSalesPromotionModelException.size() - 1; lnExisting++) {
                Model_Sales_Promotion_Model_Exception loExisting = (Model_Sales_Promotion_Model_Exception) paSalesPromotionModelException.get(lnExisting);
                if (loExisting.getModelID() != null) {
                    if (loExisting.getModelID().equals(loBrowse.getModel().getModelId())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Model is already exist in Model Exception!");
                        return poJSON;

                    }
                }
            }
            if (lnRow > 0) {
                if (getSalesPromotionModel(lnRow).getEditMode() == EditMode.ADDNEW) {
                    getSalesPromotionModel(lnRow)
                            .setModelID(
                                    loBrowse.getModel().getModelId());
                } else {
                    getSalesPromotionModel(
                            getCountSalesPromotionModel() + 1)
                            .setModelID(loBrowse.getModel().getModelId()
                            );
                }
            } else { // keep adding into detail
                getSalesPromotionModel(
                        getCountSalesPromotionModel() + 1)
                        .setModelID(loBrowse.getModel().getModelId()
                        );
            }
            poJSON = new JSONObject();
            poJSON.put("result", "success");
        }
        return poJSON;
    }

    public JSONObject removeSalesPromotionByModel(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesPromotionModel.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        paSalesPromotionModel.remove(entryNo - 1);

        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesPromotionModel.size(); lnCtr++) {
            Model_Sales_Promotion_Model loDetail = (Model_Sales_Promotion_Model) paSalesPromotionModel.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }

    public JSONObject searchPromotionByModelException(int lnRow, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        org.guanzon.cas.parameter.Model loBrowse = new ParamControllers(poGRider, logwrapr).Model();
        loBrowse.initialize();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        loBrowse.setWithParentClass(true);

        String lsCondition = null;
        if (paSalesPromotionBrand.size() > 0) {
            lsCondition = "";
            for (Object loModel : paSalesPromotionBrand) {
                Model_Sales_Promotion_Brand lsValue = (Model_Sales_Promotion_Brand) loModel;
                lsCondition += ", " + SQLUtil.toSQL(lsValue.Brand().getBrandId());
            }
        }

        if (lsCondition != null) {
            poJSON = loBrowse.searchRecordbyIndustryWCondition(value, byCode, psIndustryCode, "b.sBrandIDx IN(" + lsCondition.substring(2) + ")");
        } else {
            poJSON = loBrowse.searchRecordbyIndustryWCondition(value, byCode, psIndustryCode, null);

        }
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paSalesPromotionModelException.size() - 1; lnExisting++) {
                Model_Sales_Promotion_Model_Exception loExisting = (Model_Sales_Promotion_Model_Exception) paSalesPromotionModelException.get(lnExisting);
                if (loExisting.getModelID() != null) {
                    if (loExisting.getModelID().equals(loBrowse.getModel().getModelId())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Model Exception is already exist!");
                        return poJSON;

                    }
                }

            }
            for (int lnExisting = 0; lnExisting <= paSalesPromotionModel.size() - 1; lnExisting++) {
                Model_Sales_Promotion_Model loExisting = (Model_Sales_Promotion_Model) paSalesPromotionModel.get(lnExisting);
                if (loExisting.getModelID() != null) {
                    if (loExisting.getModelID().equals(loBrowse.getModel().getModelId())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Model is already exist in Model");
                        return poJSON;

                    }
                }
            }

            if (lnRow > 0) {//replace
                if (getSalesPromotionModelException(lnRow).getEditMode() == EditMode.ADDNEW) {
                    getSalesPromotionModelException(lnRow)
                            .setModelID(
                                    loBrowse.getModel().getModelId());
                } else {
                    getSalesPromotionModelException(
                            getCountSalesPromotionModelException() + 1)
                            .setModelID(loBrowse.getModel().getModelId()
                            );

                }
            } else { // keep adding into detail
                getSalesPromotionModelException(
                        getCountSalesPromotionModelException() + 1)
                        .setModelID(loBrowse.getModel().getModelId()
                        );
            }
            poJSON = new JSONObject();
            poJSON.put("result", "success");
        }
        return poJSON;
    }

    public JSONObject removeSalesPromotionByModelException(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesPromotionModelException.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        paSalesPromotionModelException.remove(entryNo - 1);

        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesPromotionModelException.size(); lnCtr++) {
            Model_Sales_Promotion_Model_Exception loDetail = (Model_Sales_Promotion_Model_Exception) paSalesPromotionModelException.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }

    public JSONObject searchPromotionByBrand(int lnRow, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        org.guanzon.cas.parameter.Brand loBrowse = new ParamControllers(poGRider, logwrapr).Brand();
        loBrowse.initialize();
        loBrowse.setRecordStatus(RecordStatus.ACTIVE);
        loBrowse.setWithParentClass(true);

        poJSON = loBrowse.searchRecord(value, byCode, psIndustryCode);
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paSalesPromotionBrand.size() - 1; lnExisting++) {
                Model_Sales_Promotion_Brand loExisting = (Model_Sales_Promotion_Brand) paSalesPromotionBrand.get(lnExisting);
                if (loExisting.getBrandID() != null) {
                    if (loExisting.getBrandID().equals(loBrowse.getModel().getBrandId())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Brand is already exist!");
                        return poJSON;

                    }
                }

            }
            for (int lnExisting = 0; lnExisting <= paSalesPromotionBrand.size() - 1; lnExisting++) {
                Model_Sales_Promotion_Brand loExisting = (Model_Sales_Promotion_Brand) paSalesPromotionBrand.get(lnExisting);
                if (loExisting.getBrandID() != null) {
                    if (loExisting.getBrandID().equals(loBrowse.getModel().getBrandCode())) {
                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Brand is already exist!");
                        return poJSON;

                    }
                }
            }

            if (lnRow > 0) {//replace
                if (getSalesPromotionBrand(lnRow).getEditMode() == EditMode.ADDNEW) {
                    getSalesPromotionBrand(lnRow)
                            .setBrandID(
                                    loBrowse.getModel().getBrandId());
                } else {
                    getSalesPromotionBrand(
                            getCountSalesPromotionBrand() + 1)
                            .setBrandID(loBrowse.getModel().getBrandId()
                            );

                }
            } else { // keep adding into detail
                getSalesPromotionBrand(
                        getCountSalesPromotionBrand() + 1)
                        .setBrandID(loBrowse.getModel().getBrandId()
                        );
            }
            poJSON = new JSONObject();
            poJSON.put("result", "success");
        }
        return poJSON;
    }

    public JSONObject removeSalesPromotionByBrand(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesPromotionBrand.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        String lsBrand = getSalesPromotionBrand(entryNo).getBrandID();

        //remove also other brandthat excempted
        for (int lnCtr = paSalesPromotionModelException.size() - 1; lnCtr >= 0; lnCtr--) {
            try {
                Model_Sales_Promotion_Model_Exception loDetail
                        = (Model_Sales_Promotion_Model_Exception) paSalesPromotionModelException.get(lnCtr);
                if (loDetail.Model().getBrandId().equals(lsBrand)) {
                    paSalesPromotionModelException.remove(lnCtr);
                }
            } catch (SQLException | GuanzonException ex) {
                poJSON.put("result", "error");
                poJSON.put("message", ex.getMessage());
                return poJSON;
            }
        }
        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesPromotionModelException.size(); lnCtr++) {
            Model_Sales_Promotion_Model_Exception loDetail = (Model_Sales_Promotion_Model_Exception) paSalesPromotionModelException.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        paSalesPromotionBrand.remove(entryNo - 1);
        lnDetailCount = 0;
        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesPromotionBrand.size(); lnCtr++) {
            Model_Sales_Promotion_Brand loDetail = (Model_Sales_Promotion_Brand) paSalesPromotionBrand.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }

    public JSONObject searchPromotionByGiveAway(int lnRow, String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        InventoryBrowse loBrowse = new InventoryBrowse(poGRider, logwrapr);
        loBrowse.initTransaction();
        if (!psIndustryCode.isEmpty()) {
            loBrowse.setIndustry(psIndustryCode);
        }
        loBrowse.setCategoryFilters(psCategorCD);
        loBrowse.setBranch(poGRider.getBranchCode());
        //allow negative quantity NEW BR 07-2026
        loBrowse.isWithQuantityStock(false);

        poJSON = new JSONObject();

        poJSON = loBrowse.searchInventory(value, byCode);
        System.out.println("result " + (String) poJSON.get("result"));
        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnExisting = 0; lnExisting <= paSalesPromotionGiveAwayItem.size() - 1; lnExisting++) {
                Model_Sales_Promotion_GiveAway_Item loExisting = (Model_Sales_Promotion_GiveAway_Item) paSalesPromotionGiveAwayItem.get(lnExisting);
                if (loExisting.Inventory().getStockId() != null) {
                    if (loExisting.Inventory().getStockId().equals(loBrowse.getModelInventory().getStockId())) {

                        poJSON = new JSONObject();
                        poJSON.put("result", "error");
                        poJSON.put("message", "Selected Give Away Item is already exist!");
                        return poJSON;

                    }
                }
            }

            if (lnRow > 0) {//replace
                if (getSalesPromotionGiveAway(lnRow).getEditMode() == EditMode.ADDNEW) {
                    getSalesPromotionGiveAway(lnRow)
                            .setStockID(
                                    loBrowse.getModelInventory().getStockId());
                } else {
                    getSalesPromotionGiveAway(
                            getCountSalesPromotionGiveAwayItem() + 1)
                            .setStockID(
                                    loBrowse.getModelInventory().getStockId());
                }

            } else { // keep adding into detail
                getSalesPromotionGiveAway(
                        getCountSalesPromotionGiveAwayItem() + 1)
                        .setStockID(
                                loBrowse.getModelInventory().getStockId());
            }
            poJSON = new JSONObject();
            poJSON.put("result", "success");
        }

        return poJSON;

    }

    public JSONObject removeSalesPromotionByGiveAway(int entryNo) {
        poJSON = new JSONObject();
        int lnDetailCount = 0;
        if (paSalesPromotionGiveAwayItem.size() < entryNo) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }
        paSalesPromotionGiveAwayItem.remove(entryNo - 1);

        //realign entry no
        for (int lnCtr = 0; lnCtr < paSalesPromotionGiveAwayItem.size(); lnCtr++) {
            Model_Sales_Promotion_GiveAway_Item loDetail = (Model_Sales_Promotion_GiveAway_Item) paSalesPromotionGiveAwayItem.get(lnCtr);
            lnDetailCount++;
            loDetail.setPromoID(getMaster().getPromoID());
            loDetail.setEntryNo(lnDetailCount);
        }
        return poJSON;

    }

    public JSONObject searchPromotionByShop(String value, boolean byCode)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        String lsSQL = "SELECT * FROM ("
                + "    SELECT "
                + "        IFNULL(cShopType,'') cShopType,"
                + "        CASE cShopType"
                + "            WHEN 0 THEN '3S Shop'"
                + "            WHEN 1 THEN 'Multi Brand'"
                + "            WHEN 2 THEN 'Big Bike'"
                + "            ELSE 'Other Shop'"
                + "        END `xShopType` "
                + "    FROM Branch_Others"
                + "    WHERE sIndstCdx = '01' "
                + ") `Shop` WHERE 1=1 GROUP BY cShopType";

        //default
        String lscolHeader = "Shop Type»Description";
        String lscolName = "cShopType»xShopType»";
        String lscolCriteria = "ShopType»xShopType";

        System.out.println("Search Dialog Query : " + lsSQL);
        this.poJSON = ShowDialogFX.Search(
                poGRider,
                lsSQL,
                value,
                lscolHeader,
                lscolName,
                lscolCriteria,
                byCode ? 0 : 1);

        if (this.poJSON == null) {
            this.poJSON = new JSONObject();
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "No record loaded.");
            return this.poJSON;
        }
        if (poJSON.get("cShopType") == null) {
            this.poJSON.put("result", "error");
            this.poJSON.put("message", "No record loaded.");
            return this.poJSON;
        }
        String lsSelected = (String) this.poJSON.get("cShopType");

        // Build array of existing shop types
        String lsExisting = getMaster().getShopType();
        String[] laShopType = (lsExisting == null || lsExisting.trim().isEmpty())
                ? new String[0]
                : lsExisting.split("»");

        // Check for duplicate before appending
        for (String lsItem : laShopType) {
            if (lsItem.trim().equalsIgnoreCase(lsSelected.trim())) {
                this.poJSON = new JSONObject();
                this.poJSON.put("result", "error");
                this.poJSON.put("message", "Selected Shop Type is already exist!");
                return this.poJSON;
            }
        }

        // Append safely (no leading » on first entry)
        getMaster().setShopType(
                (lsExisting == null || lsExisting.trim().isEmpty())
                ? lsSelected.isEmpty() ? "x" : lsSelected
                : lsExisting + "»" + (lsSelected.isEmpty() ? "x" : lsSelected)
        );

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        return poJSON;
    }

    public JSONObject removeSalesPromotionByShop(int arrayNo) {
        poJSON = new JSONObject();

        // Build array of existing shop types
        String lsExisting = getMaster().getShopType();
        String[] laShopType = (lsExisting == null || lsExisting.trim().isEmpty())
                ? new String[0]
                : lsExisting.split("»");

        // Validate index (arrayNo should be 0-based; check both bounds)
        if (arrayNo < 0 || arrayNo >= laShopType.length) {
            poJSON.put("result", "error");
            poJSON.put("message", "Unable to Detect Entry No");
            return poJSON;
        }

        // Rebuild the delimited string without the removed entry
        StringBuilder lsRebuilt = new StringBuilder();
        for (int i = 0; i < laShopType.length; i++) {
            if (i == arrayNo) {
                continue; // skip the one being removed
            }
            if (lsRebuilt.length() > 0) {
                lsRebuilt.append("»");
            }
            lsRebuilt.append(laShopType[i]);
        }

        getMaster().setShopType(lsRebuilt.toString());

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject DuplicateTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.READY && getEditMode() != EditMode.UPDATE) {
            poJSON.put("result", "error");
            poJSON.put("message", "Please load a transaction before duplicating.");
            return poJSON;
        }

        poJSON = convertCurrentTransactionToNewCopy();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Transaction duplicated. Review and save to create the new record.");
        return poJSON;
    }

    /**
     * "Save As New" — while editing an existing transaction (UPDATE mode),
     * saves the CURRENT in-memory state (including whatever edits were made
     * this session) as a brand-new transaction, leaving the original record on
     * file untouched.
     */
    public JSONObject SaveAsNewTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();

        if (getEditMode() != EditMode.UPDATE) {
            poJSON.put("result", "error");
            poJSON.put("message", "Save As New is only available while editing an existing transaction.");
            return poJSON;
        }

        poJSON = convertCurrentTransactionToNewCopy();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        // Now in ADDNEW mode with promoID cleared — persist as a new record
        return saveTransaction();
    }

    private JSONObject convertCurrentTransactionToNewCopy() throws SQLException, GuanzonException, CloneNotSupportedException {
        poJSON = new JSONObject();

        // ---- Snapshot master fields ----
        String lsClientID = getMaster().getClientID();
        java.util.Date ldDate = getMaster().getDate();
        java.util.Date ldFromDate = getMaster().getFromDate();
        java.util.Date ldThruDate = getMaster().getThruDate();
        double lnAmountFrom = getMaster().getAmountFrom();
        double lnAmountTo = getMaster().getAmountTo();
        String lsPromoSource = getMaster().getPromoSource();
        String lsPromoType = getMaster().getPromoType();
        String lsTransactionType = getMaster().getTransactionType();
        String lsPromoDescription = getMaster().getPromoDescription();
        String lsRemarks = getMaster().getRemarks();
        String lsShopType = getMaster().getShopType();
        String lsReferNo = getMaster().getReferNo();

        // ---- Snapshot detail lists (copy references before newTransaction() clears the working lists) ----
        List<Model_Sales_Promotion_Province> loOldProvince = new ArrayList<>(getSalesPromotionProvinceList());
        List<Model_Sales_Promotion_Branch_Area> loOldBranchArea = new ArrayList<>(getSalesPromotionBranchAreaList());
        List<Model_Sales_Promotion_Brand> loOldBrand = new ArrayList<>(getSalesPromotionBrandList());
        List<Model_Sales_Promotion_Model> loOldModel = new ArrayList<>(getSalesPromotionModelList());
        List<Model_Sales_Promotion_Model_Exception> loOldModelException = new ArrayList<>(getSalesPromotionModelExceptionList());
        List<Model_Sales_Promotion_GiveAway_Item> loOldGiveAway = new ArrayList<>(getSalesPromotionGiveAwayItemList());

        // ---- Reset to a blank new transaction (clears promoID, sets ADDNEW, clears detail lists) ----
        poJSON = newTransaction();
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        // ---- Restore master fields (promo ID / transaction number stays blank) ----
        getMaster().setClientID(lsClientID);
        getMaster().setDate(ldDate);
        getMaster().setFromDate(ldFromDate);
        getMaster().setThruDate(ldThruDate);
        getMaster().setAmountFrom(lnAmountFrom);
        getMaster().setAmountTo(lnAmountTo);
        getMaster().setPromoSource(lsPromoSource);
        getMaster().setPromoType(lsPromoType);
        getMaster().setTransactionType(lsTransactionType);
        getMaster().setPromoDescription(lsPromoDescription);
        getMaster().setRemarks(lsRemarks);
        getMaster().setShopType(lsShopType);
        getMaster().setReferNo(lsReferNo);

        // ---- Re-add Province as fresh entries ----
        for (Model_Sales_Promotion_Province loItem : loOldProvince) {
            getSalesPromotionProvince(getCountSalesPromotionProvince() + 1)
                    .setProvinceID(loItem.getProvinceID());
        }

        // ---- Re-add Branch Area as fresh entries ----
        for (Model_Sales_Promotion_Branch_Area loItem : loOldBranchArea) {
            getSalesPromotionBranchArea(getCountSalesPromotionBranchArea() + 1)
                    .setAreaCode(loItem.getAreaCode());
        }

        // ---- Re-add Brand as fresh entries ----
        for (Model_Sales_Promotion_Brand loItem : loOldBrand) {
            Model_Sales_Promotion_Brand loNew = getSalesPromotionBrand(getCountSalesPromotionBrand() + 1);
            loNew.setBrandID(loItem.getBrandID());
            loNew.setTotalAmount(loItem.getTotalAmount());
            loNew.setDiscountRate(loItem.getDiscountRate());
            loNew.setDiscAmount(loItem.getDiscAmount());
            loNew.setFreight(loItem.getFreight());
            loNew.setAmount(loItem.getAmount());
            loNew.isInsuranceFree(loItem.isInsuranceFree());
            loNew.isRegistrationFree(loItem.isRegistrationFree());
            loNew.isWithIncentive(loItem.isWithIncentive());
            loNew.setRemarks(loItem.getRemarks());
        }

        // ---- Re-add Model as fresh entries ----
        for (Model_Sales_Promotion_Model loItem : loOldModel) {
            Model_Sales_Promotion_Model loNew = getSalesPromotionModel(getCountSalesPromotionModel() + 1);
            loNew.setModelID(loItem.getModelID());
            loNew.setTotalAmount(loItem.getTotalAmount());
            loNew.setDiscountRate(loItem.getDiscountRate());
            loNew.setDiscAmount(loItem.getDiscAmount());
            loNew.setFreight(loItem.getFreight());
            loNew.setAmount(loItem.getAmount());
            loNew.isInsuranceFree(loItem.isInsuranceFree());
            loNew.isRegistrationFree(loItem.isRegistrationFree());
            loNew.isWithIncentive(loItem.isWithIncentive());
            loNew.setRemarks(loItem.getRemarks());
        }

        // ---- Re-add Model Exception as fresh entries ----
        for (Model_Sales_Promotion_Model_Exception loItem : loOldModelException) {
            Model_Sales_Promotion_Model_Exception loNew = getSalesPromotionModelException(getCountSalesPromotionModelException() + 1);
            loNew.setModelID(loItem.getModelID());
        }

        // ---- Re-add GiveAway Item as fresh entries ----
        for (Model_Sales_Promotion_GiveAway_Item loItem : loOldGiveAway) {
            Model_Sales_Promotion_GiveAway_Item loNew = getSalesPromotionGiveAway(getCountSalesPromotionGiveAwayItem() + 1);
            loNew.setStockID(loItem.getStockID());
            loNew.setQuantity(loItem.getQuantity());
            loNew.setRemarks(loItem.getRemarks());
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject printRecord()
            throws SQLException, JRException, CloneNotSupportedException, GuanzonException {

        poJSON = new JSONObject();

        if (getMaster().getPromoID() == null
                || getMaster().getPromoID().isEmpty()) {

            poJSON.put("result", "error");
            poJSON.put("message", "No record is selected.");
            return poJSON;
        }

        // =====================================================
        // REPORT ROWS
        // =====================================================
        List<Map<String, ?>> brandRows = new ArrayList<>();
        List<Map<String, ?>> modelRows = new ArrayList<>();
        List<Map<String, ?>> exceptionRows = new ArrayList<>();
        List<Map<String, ?>> giveAwayRows = new ArrayList<>();

        // =====================================================
        // BRAND ROWS
        // =====================================================
        for (Model item : paSalesPromotionBrand) {

            Model_Sales_Promotion_Brand loBrand
                    = (Model_Sales_Promotion_Brand) item;

            String brandDesc;

            try {
                brandDesc = loBrand.Brand().getDescription();
            } catch (SQLException | GuanzonException ex) {
                brandDesc = loBrand.getBrandID();
            }

            double srp = loBrand.getTotalAmount();
            double discRate = loBrand.getDiscountRate();
            double discAmt = loBrand.getDiscAmount();
            double freight = loBrand.getFreight();
            double incentiveAmount = loBrand.getAmount();

            String remarks = loBrand.getRemarks();

            // Build complete promotion description
            String promoDetail = buildPromoDetail(
                    srp,
                    discRate,
                    discAmt,
                    loBrand.isRegistrationFree(),
                    loBrand.isInsuranceFree(),
                    freight,
                    incentiveAmount,
                    remarks
            );

            Map<String, Object> row = new HashMap<>();

            row.put("brand", brandDesc);
            row.put("promoDetail", promoDetail);

            brandRows.add(row);
        }

        // =====================================================
        // MODEL ROWS
        // =====================================================
        for (Model item : paSalesPromotionModel) {

            Model_Sales_Promotion_Model loModel
                    = (Model_Sales_Promotion_Model) item;

            String brandDesc;

            try {
                brandDesc = loModel.Model()
                        .Brand()
                        .getDescription();

            } catch (SQLException | GuanzonException ex) {

                brandDesc = "";
            }

            String modelDesc;

            try {
                modelDesc = loModel.Model()
                        .getDescription();

            } catch (SQLException | GuanzonException ex) {

                modelDesc = loModel.getModelID();
            }

            double srp = loModel.getTotalAmount();
            double discRate = loModel.getDiscountRate();
            double discAmt = loModel.getDiscAmount();
            double freight = loModel.getFreight();
            double incentiveAmount = loModel.getAmount();

            String remarks = loModel.getRemarks();

            // Build complete promotion description
            String promoDetail = buildPromoDetail(
                    srp,
                    discRate,
                    discAmt,
                    loModel.isRegistrationFree(),
                    loModel.isInsuranceFree(),
                    freight,
                    incentiveAmount,
                    remarks
            );

            Map<String, Object> row = new HashMap<>();

            row.put("brand", brandDesc);
            row.put("model", modelDesc);
            row.put("promoDetail", promoDetail);

            modelRows.add(row);
        }

        // =====================================================
        // MODEL EXCEPTION ROWS
        // =====================================================
        for (Model item : paSalesPromotionModelException) {

            Model_Sales_Promotion_Model_Exception loException
                    = (Model_Sales_Promotion_Model_Exception) item;

            String brandDesc;

            try {
                brandDesc = loException.Model()
                        .Brand()
                        .getDescription();

            } catch (SQLException | GuanzonException ex) {

                brandDesc = "";
            }

            String modelDesc;

            try {
                modelDesc = loException.Model()
                        .getDescription();

            } catch (SQLException | GuanzonException ex) {

                modelDesc = loException.getModelID();
            }

            Map<String, Object> row = new HashMap<>();

            row.put("brand", brandDesc);
            row.put("model", modelDesc);

            exceptionRows.add(row);
        }

        // =====================================================
        // GIVE AWAY ROWS
        // =====================================================
        for (Model item : paSalesPromotionGiveAwayItem) {

            Model_Sales_Promotion_GiveAway_Item loGiveAway
                    = (Model_Sales_Promotion_GiveAway_Item) item;

            String barcode;

            try {
                barcode = loGiveAway.Inventory()
                        .getBarCode();

            } catch (SQLException | GuanzonException ex) {

                barcode = "";
            }

            String description;

            try {
                description = loGiveAway.Inventory()
                        .getDescription();

            } catch (SQLException | GuanzonException ex) {

                description = "";
            }

            Map<String, Object> row = new HashMap<>();

            row.put("barcode", barcode);
            row.put("description", description);
            row.put("quantity", loGiveAway.getQuantity());
            row.put(
                    "note",
                    loGiveAway.getRemarks() == null
                    ? ""
                    : loGiveAway.getRemarks()
            );

            giveAwayRows.add(row);
        }

        // =====================================================
        // REPORT UTIL
        // =====================================================
        ReportUtil poReportJasper = new ReportUtil(poGRider);

        poReportJasper.setReportListener(new ReportUtilListener() {

            @Override
            public void onReportOpen() {
                System.out.println("Report opened.");
            }

            @Override
            public void onReportClose() {
                System.out.println("Report closed.");
            }

            @Override
            public void onReportPrint() {

                System.out.println("Report printing...");

                poReportJasper.CloseReportUtil();
            }

            @Override
            public void onReportExport() {
                // Optional
            }

            @Override
            public void onReportExportPDF() {

                System.out.println(
                        "Report exported to PDF."
                );
            }
        });

        // =====================================================
        // BUILD "IMPLEMENTED TO" SUMMARY: shop types + provinces + branch areas
        // =====================================================
        List<String> implementToParts = new ArrayList<>();

        // --- Shop Types (delimited string on master, decode codes to labels) ---
        String lsShopType = getMaster().getShopType();
        if (lsShopType != null && !lsShopType.trim().isEmpty()) {
            String[] laShopType = lsShopType.split("»");
            for (String lsCode : laShopType) {
                String lsCode2 = lsCode.trim();
                if (lsCode2.isEmpty()) {
                    continue;
                }
                String lsLabel;
                switch (lsCode2) {
                    case "0":
                        lsLabel = "3S Shop";
                        break;
                    case "1":
                        lsLabel = "Multi Brand";
                        break;
                    case "2":
                        lsLabel = "Big Bike";
                        break;
                    default:
                        lsLabel = "Other Shop";
                        break;
                }
                implementToParts.add(lsLabel);
            }
        }

        // --- Provinces ---
        for (Model item : paSalesPromotionProvince) {
            Model_Sales_Promotion_Province loProvince = (Model_Sales_Promotion_Province) item;
            try {
                String lsDesc = loProvince.Province().getDescription();
                if (lsDesc != null && !lsDesc.trim().isEmpty()) {
                    implementToParts.add(lsDesc.trim());
                }
            } catch (SQLException | GuanzonException ex) {
                // skip on lookup failure
            }
        }

        // --- Branch Areas ---
        for (Model item : paSalesPromotionBranchArea) {
            Model_Sales_Promotion_Branch_Area loArea = (Model_Sales_Promotion_Branch_Area) item;
            try {
                String lsDesc = loArea.BranchArea().getAreaDescription();
                if (lsDesc != null && !lsDesc.trim().isEmpty()) {
                    implementToParts.add(lsDesc.trim());
                }
            } catch (SQLException | GuanzonException ex) {
                // skip on lookup failure
            }
        }

        String lsImplementTo = implementToParts.isEmpty()
                ? "ALL Branches"
                : String.join(", ", implementToParts);
        // =====================================================
        // MASTER PARAMETERS
        // =====================================================
        poReportJasper.addParameter(
                "ImplentTo",
                lsImplementTo);

        poReportJasper.addParameter(
                "PromoID",
                getMaster().getPromoID());

        poReportJasper.addParameter(
                "PromoDescription",
                getMaster().getPromoDescription());

        poReportJasper.addParameter(
                "ReferNo",
                getMaster().getReferNo());

        poReportJasper.addParameter(
                "TransactionDate",
                SQLUtil.dateFormat(
                        getMaster().getDate(),
                        SQLUtil.FORMAT_LONG_DATE));

        poReportJasper.addParameter(
                "PromoStart",
                SQLUtil.dateFormat(
                        getMaster().getFromDate(),
                        SQLUtil.FORMAT_LONG_DATE));

        poReportJasper.addParameter(
                "PromoEnd", (getMaster().getThruDate() == null ? ""
                : SQLUtil.dateFormat(
                        getMaster().getThruDate(),
                        SQLUtil.FORMAT_LONG_DATE))
        );

        poReportJasper.addParameter(
                "Remarks",
                getMaster().getRemarks());

        poReportJasper.addParameter(
                "BranchName", poGRider.getBranchName());

        poReportJasper.addParameter(
                "DatePrinted",
                SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_TIMESTAMP));

        // =====================================================
        // BRAND DATASOURCE
        // =====================================================
        poReportJasper.addParameter(
                "BrandDataSource",
                new JRMapCollectionDataSource(brandRows));

        // =====================================================
        // MODEL DATASOURCE
        // =====================================================
        poReportJasper.addParameter(
                "ModelDataSource",
                new JRMapCollectionDataSource(modelRows));

        // =====================================================
        // MODEL EXCEPTION DATASOURCE
        // =====================================================
        poReportJasper.addParameter(
                "ModelExceptionDataSource",
                new JRMapCollectionDataSource(exceptionRows));

        // =====================================================
        // GIVE AWAY DATASOURCE
        // =====================================================
        poReportJasper.addParameter(
                "GiveAwayDataSource",
                new JRMapCollectionDataSource(giveAwayRows));

        // =====================================================
        // VISIBILITY
        // =====================================================
        poReportJasper.addParameter(
                "HasBrand",
                !brandRows.isEmpty()
        );

        poReportJasper.addParameter(
                "HasModel",
                !modelRows.isEmpty()
        );

        poReportJasper.addParameter(
                "HasModelException",
                !exceptionRows.isEmpty()
        );

        poReportJasper.addParameter(
                "HasGiveAway",
                !giveAwayRows.isEmpty()
        );

        // =====================================================
        // APPROVAL REMARKS
        // =====================================================
        if (getMaster().getTransactionStatus().equals(SalesPromotionStatus.CONFIRMED)) {
            poReportJasper.addParameter("watermarkImagePath", poGRider.getReportPath() + "images\\blank.png");
        }
        JSONObject loJSON = getEntryBy();
        String entryBy = "";
        String entryDate = "";

        if ("success".equals((String) loJSON.get("result"))) {
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }
        String lsPreparedBy = entryBy;
        String lsPreparedByDate = entryDate;
        String lsConfirmedBy = "";
        String lsConfirmedDate = "";

        String lsSQL = " SELECT sModified, dModified "
                + " FROM Transaction_Status_History "
                + " WHERE sTableNme ='Sales_Promotion_Master' "
                + " AND cRefrStat = '1' AND cTranStat = '1' ORDER BY dModified DESC";
        lsSQL = MiscUtil.addCondition(lsSQL, " sSourceNo =  " + SQLUtil.toSQL(getMaster().getPromoID()));
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                if (loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))) {
                    lsConfirmedBy = loRS.getString("sModified") == null ? "" : getSysUser(poGRider.Decrypt(loRS.getString("sModified")));
                    LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                    lsConfirmedDate = dModified.format(formatter);
                }
            }
        }

        MiscUtil.close(loRS);

        poReportJasper.addParameter("PrepNme", lsPreparedBy + " - " + lsPreparedByDate);
        poReportJasper.addParameter("ConfirmNme", lsConfirmedBy + " - " + lsConfirmedDate);
        poReportJasper.addParameter("ReceivrNme", "");
        // =====================================================
        // MAIN REPORT DATASOURCE
        // =====================================================
        List<Map<String, ?>> masterRows = new ArrayList<>();

        masterRows.add(
                new HashMap<String, Object>());

        poReportJasper.setJRBeanCollectionDataSource(
                new JRMapCollectionDataSource(masterRows));

        // =====================================================
        // REPORT
        // =====================================================
        poReportJasper.setJasperPath(
                PromoMaintenancePrint.getJasperReport(
                        psIndustryCode
                )
        );

        poReportJasper.isAlwaysTop(false);
        poReportJasper.isWithUI(true);
        poReportJasper.isWithExport(true);
        poReportJasper.isWithExportPDF(true);
        poReportJasper.willExport(false);

        return poReportJasper.generateReport();
    }

    private String buildPromoDetail(
            double srp,
            double discRate,
            double discAmt,
            boolean registrationFree,
            boolean insuranceFree,
            double freight,
            double incentiveAmount,
            String remarks
    ) {
        List<String> details = new ArrayList<>();

        // SRP
        if (srp != 0.0) {
            details.add(String.format("SRP OF %,.2f", srp));
        }

        // Discount Rate
        if (discRate != 0.0) {
            details.add(String.format(
                    "LESS DISCOUNT RATE OF %.2f%%",
                    discRate
            ));
        }

        // Discount Amount
        if (discAmt != 0.0) {
            details.add(String.format(
                    "DISCOUNT AMOUNT OF %,.2f",
                    discAmt
            ));
        }

        // Free Registration
        if (registrationFree) {
            details.add("FREE REGISTRATION");
        }

        // Free Insurance
        if (insuranceFree) {
            details.add("FREE INSURANCE");
        }

        // Freight
        if (freight != 0.0) {
            details.add(String.format(
                    "FREIGHT OF %,.2f",
                    freight
            ));
        }

        // Incentive
        if (incentiveAmount != 0.0) {
            details.add(String.format(
                    "INCENTIVE OF %,.2f",
                    incentiveAmount
            ));
        }

        // Remarks / Note
        if (remarks != null && !remarks.trim().isEmpty()) {
            details.add(remarks.trim());
        }

        return String.join(", ", details);
    }
}
