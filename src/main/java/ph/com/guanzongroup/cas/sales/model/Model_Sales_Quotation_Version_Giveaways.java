/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.ReferenceCache;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Model class for the Sales Quotation Version Giveaways transaction.
 *
 * <p>
 * This class represents a giveaway item attached to a Sales Quotation
 * Version. Each record is identified by the combination of
 * {@code sTransNox} (the parent version's transaction number) and
 * {@code nEntryNox} (the line entry number), and captures the stocked item
 * and quantity given away.
 * </p>
 *
 * <p>
 * Detail records do not generate their own transaction number; the
 * {@code sTransNox} value is inherited from the parent
 * {@code Sales_Quotation_Version_Master} record, and {@code nEntryNox} is
 * expected to be assigned by the caller (e.g. the next available line
 * number for the given transaction).
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 * @module Sales Quotation Version Giveaways
 * @version 1.0
 */
public class Model_Sales_Quotation_Version_Giveaways extends Model {
    private Model_Inventory poInventory;
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
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nQuantity", 0);
            poEntity.updateNull("dModified");

            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);
            ID  = "sTransNox";
            ID2 = "nEntryNox";

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setEntryNo(Integer entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Integer getEntryNo() {
        return (Integer) getValue("nEntryNox");
    }

    public JSONObject setStockId(String stockId) {
        return setValue("sStockIDx", stockId);
    }

    public String getStockId() {
        return (String) getValue("sStockIDx");
    }

    public JSONObject setQuantity(Integer quantity) {
        return setValue("nQuantity", quantity);
    }

    public Integer getQuantity() {
        return (Integer) getValue("nQuantity");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
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
        return "";   // sTransNox is inherited from the parent version
    }
    public Model_Inventory Inventory() throws SQLException, GuanzonException {

        if (poInventory == null) {
            poInventory = new Model_Inventory();
            poInventory.setApplicationDriver(poGRider);
            poInventory.setXML("Model_Inventory");
            poInventory.setTableName("Inventory");
            poInventory.initialize();
        }

        String stockId = (String) (
                getValue("sStockIDx") == null
                        ? ""
                        : getValue("sStockIDx")
        );

        if (!"".equals(stockId)) {

            if (poInventory.getEditMode() == EditMode.READY
                    && poInventory.getStockId().equals(stockId)) {
                return poInventory;
            }

            if (ReferenceCache.tryLoad(
                    "Inventory",
                    stockId,
                    poInventory)) {
                return poInventory;
            }

            poJSON = poInventory.openRecord(stockId);

            if ("success".equals((String) poJSON.get("result"))) {
                ReferenceCache.store(
                        "Inventory",
                        stockId,
                        poInventory
                );
                return poInventory;
            } else {
                poInventory.initialize();
                return poInventory;
            }

        } else {
            poInventory.initialize();
            return poInventory;
        }
    }
}