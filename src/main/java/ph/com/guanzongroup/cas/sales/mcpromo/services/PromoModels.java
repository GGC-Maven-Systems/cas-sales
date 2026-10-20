package ph.com.guanzongroup.cas.sales.mcpromo.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Master;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Adjustment;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Count_Detail;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Count_Master;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Branch_Area;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Brand;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_GiveAway_Item;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Master;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Model;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Model_Exception;
import ph.com.guanzongroup.cas.sales.mcpromo.model.Model_Sales_Promotion_Province;

public class PromoModels {

    public PromoModels(GRiderCAS applicationDriver) {
        poGRider = applicationDriver;
    }

    public Model_Sales_Promotion_Master SalesPromotionMaster() {
        if (poGRider == null) {
            System.err.println("PromoModels.SalesPromotionMaster: Application driver is not set.");
            return null;
        }

        if (poSalesPromotionMaster == null) {
            poSalesPromotionMaster = new Model_Sales_Promotion_Master();
            poSalesPromotionMaster.setApplicationDriver(poGRider);
            poSalesPromotionMaster.setXML("Model_Sales_Promotion_Master");
            poSalesPromotionMaster.setTableName("Sales_Promotion_Master");
            poSalesPromotionMaster.initialize();
        }

        return poSalesPromotionMaster;
    }

    public Model_Sales_Promotion_Province SalesPromotionProvince() {
        if (poGRider == null) {
            System.err.println("PromoModels.SalesPromotionProvince: Application driver is not set.");
            return null;
        }

        if (poSalesPromotionProvince == null) {
            poSalesPromotionProvince = new Model_Sales_Promotion_Province();
            poSalesPromotionProvince.setApplicationDriver(poGRider);
            poSalesPromotionProvince.setXML("Model_Sales_Promotion_Province");
            poSalesPromotionProvince.setTableName("Sales_Promotion_Province");
            poSalesPromotionProvince.initialize();
        }

        return poSalesPromotionProvince;
    }

    public Model_Sales_Promotion_Branch_Area SalesPromotionBranchArea() {
        if (poGRider == null) {
            System.err.println("PromoModels.SalesPromotionBranchArea: Application driver is not set.");
            return null;
        }

        if (poSalesPromotionBranchArea == null) {
            poSalesPromotionBranchArea = new Model_Sales_Promotion_Branch_Area();
            poSalesPromotionBranchArea.setApplicationDriver(poGRider);
            poSalesPromotionBranchArea.setXML("Model_Sales_Promotion_Branch_Area");
            poSalesPromotionBranchArea.setTableName("Sales_Promotion_Branch_Area");
            poSalesPromotionBranchArea.initialize();
        }

        return poSalesPromotionBranchArea;
    }

    public Model_Sales_Promotion_Brand SalesPromotionBrand() {
        if (poGRider == null) {
            System.err.println("PromoModels.SalesPromotionBrand: Application driver is not set.");
            return null;
        }

        if (poSalesPromotionBrand == null) {
            poSalesPromotionBrand = new Model_Sales_Promotion_Brand();
            poSalesPromotionBrand.setApplicationDriver(poGRider);
            poSalesPromotionBrand.setXML("Model_Sales_Promotion_Brand");
            poSalesPromotionBrand.setTableName("Sales_Promotion_Brand");
            poSalesPromotionBrand.initialize();
        }

        return poSalesPromotionBrand;
    }

    public Model_Sales_Promotion_Model SalesPromotionModel() {
        if (poGRider == null) {
            System.err.println("PromoModels.SalesPromotionModel: Application driver is not set.");
            return null;
        }

        if (poSalesPromotionModel == null) {
            poSalesPromotionModel = new Model_Sales_Promotion_Model();
            poSalesPromotionModel.setApplicationDriver(poGRider);
            poSalesPromotionModel.setXML("Model_Sales_Promotion_Model");
            poSalesPromotionModel.setTableName("Sales_Promotion_Model");
            poSalesPromotionModel.initialize();
        }

        return poSalesPromotionModel;
    }
    
    
    public Model_Sales_Promotion_Model_Exception SalesPromotionModelException() {
        if (poGRider == null) {
            System.err.println("PromoModels.SalesPromotionModelException: Application driver is not set.");
            return null;
        }

        if (poSalesPromotionModelException == null) {
            poSalesPromotionModelException = new Model_Sales_Promotion_Model_Exception();
            poSalesPromotionModelException.setApplicationDriver(poGRider);
            poSalesPromotionModelException.setXML("Model_Sales_Promotion_Model_Exception");
            poSalesPromotionModelException.setTableName("Sales_Promotion_Model_Exception");
            poSalesPromotionModelException.initialize();
        }

        return poSalesPromotionModelException;
    }
    
    
    
    
    public Model_Sales_Promotion_GiveAway_Item SalesPromotionGiveAwayItem() {
        if (poGRider == null) {
            System.err.println("PromoModels.SalesPromotionGiveAwayItem: Application driver is not set.");
            return null;
        }

        if (poModelSalesPromotionGiveAwayItem == null) {
            poModelSalesPromotionGiveAwayItem = new Model_Sales_Promotion_GiveAway_Item();
            poModelSalesPromotionGiveAwayItem.setApplicationDriver(poGRider);
            poModelSalesPromotionGiveAwayItem.setXML("Model_Sales_Promotion_GiveAway_Item");
            poModelSalesPromotionGiveAwayItem.setTableName("Sales_Promotion_GiveAway_Item");
            poModelSalesPromotionGiveAwayItem.initialize();
        }

        return poModelSalesPromotionGiveAwayItem;
    }

    private final GRiderCAS poGRider;

    private Model_Sales_Promotion_Master poSalesPromotionMaster;
    private Model_Sales_Promotion_Province poSalesPromotionProvince;
    private Model_Sales_Promotion_Branch_Area poSalesPromotionBranchArea;
    private Model_Sales_Promotion_Brand poSalesPromotionBrand;
    private Model_Sales_Promotion_Model poSalesPromotionModel;
    private Model_Sales_Promotion_Model_Exception poSalesPromotionModelException;
    private Model_Sales_Promotion_GiveAway_Item poModelSalesPromotionGiveAwayItem;
}
