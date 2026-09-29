/*
 * Copyright (c) 2026 Guanzon Group of Companies.
 * All rights reserved.
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 */
package ph.com.guanzongroup.cas.sales;

import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Giveaways;
import ph.com.guanzongroup.cas.sales.services.SalesModels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the giveaways of a Sales Quotation version
 * (table Sales_Quotation_Version_Giveaways, keyed by sTransNox + nEntryNox).
 *
 * <p>
 * A version can have many giveaways, so besides the single {@link #getModel()}
 * required by {@link Parameter}, this class keeps a list of giveaway models.
 * The list is driven by {@link SalesQoutation}: it loads it when a version is
 * opened, switches it to update mode, and saves it (numbering the rows and
 * removing deleted ones) together with the quotation.
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 * @module Sales Quotation
 * @since 1.0
 */
public class SalesQoutationVersionGiveaways extends Parameter {

    /** Model required by the {@link Parameter} base class (used for table name and browsing). */
    Model_Sales_Quotation_Version_Giveaways poModel;

    /** Giveaway rows of the current version. */
    List<Model_Sales_Quotation_Version_Giveaways> paGiveaways = new ArrayList<>();

    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = RecordStatus.ACTIVE;
        poModel = new SalesModels(poGRider).SalesQuotationVersionGiveaways();
        paGiveaways = new ArrayList<>();
        super.initialize();
    }

    @Override
    public Model_Sales_Quotation_Version_Giveaways getModel() {
        return poModel;
    }

    // ------------------------------------------------------------------
    // list of giveaways
    // ------------------------------------------------------------------

    /** Number of giveaway rows. */
    public int getGiveawayCount() {
        return paGiveaways.size();
    }

    /** Giveaway row (0-based). */
    public Model_Sales_Quotation_Version_Giveaways Giveaway(int row) {
        return paGiveaways.get(row);
    }

    /** All giveaway rows. */
    public List<Model_Sales_Quotation_Version_Giveaways> Giveaways() {
        return paGiveaways;
    }

    /** Removes every row from memory (nothing is deleted from the database). */
    public void clearGiveaways() {
        paGiveaways.clear();
    }

    /** Adds a blank giveaway row. */
    public JSONObject addGiveaway() throws SQLException, GuanzonException {
        Model_Sales_Quotation_Version_Giveaways loModel = new SalesModels(poGRider).SalesQuotationVersionGiveaways();
        poJSON = loModel.newRecord();
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        paGiveaways.add(loModel);

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /** Removes a giveaway row (0-based); it is deleted from the database on save. */
    public JSONObject removeGiveaway(int row) {
        poJSON = new JSONObject();
        if (row < 0 || row >= paGiveaways.size()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid row number.");
            return poJSON;
        }
        paGiveaways.remove(row);
        poJSON.put("result", "success");
        return poJSON;
    }

    /** Loads all giveaways of a version. An empty result is not an error. */
    public JSONObject loadGiveaways(String versionNo) throws SQLException, GuanzonException {
        poGRider.ensureConnected();
        paGiveaways.clear();

        String lsSQL = "SELECT nEntryNox FROM " + poModel.getTable()
                + " WHERE sTransNox = " + SQLUtil.toSQL(versionNo)
                + " ORDER BY nEntryNox";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            while (loRS.next()) {
                Model_Sales_Quotation_Version_Giveaways loModel = new SalesModels(poGRider).SalesQuotationVersionGiveaways();
                poJSON = loModel.openRecord(versionNo, loRS.getInt("nEntryNox"));
                if (!"success".equals((String) poJSON.get("result"))) {
                    paGiveaways.clear();
                    poJSON.put("message", "Unable to open giveaway record.");
                    return poJSON;
                }
                paGiveaways.add(loModel);
            }
        } finally {
            MiscUtil.close(loRS);
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /** Puts the loaded giveaway rows in update mode. */
    public JSONObject updateGiveaways() {
        for (Model_Sales_Quotation_Version_Giveaways loModel : paGiveaways) {
            poJSON = loModel.updateRecord();
            if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        }
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Saves the giveaway rows under the given version: drops rows without an
     * item, numbers the rest from 1, saves them and deletes any old rows
     * beyond the new count. Must run inside the parent's transaction.
     */
    public JSONObject saveGiveaways(String versionNo) throws SQLException, GuanzonException, CloneNotSupportedException {
        // drop rows without an item
        paGiveaways.removeIf(m -> m.getStockId() == null || m.getStockId().isEmpty());

        for (int lnCtr = 0; lnCtr < paGiveaways.size(); lnCtr++) {
            Integer lnQty = paGiveaways.get(lnCtr).getQuantity();
            if (lnQty == null || lnQty <= 0) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid giveaway quantity at row " + (lnCtr + 1) + ".");
                return poJSON;
            }
        }

        for (int lnCtr = 0; lnCtr < paGiveaways.size(); lnCtr++) {
            Model_Sales_Quotation_Version_Giveaways loModel = paGiveaways.get(lnCtr);
            loModel.setTransactionNo(versionNo);
            loModel.setEntryNo(lnCtr + 1);
            loModel.setModifiedDate(poGRider.getServerDate());

            poJSON = loModel.saveRecord();
            if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        }

        // remove old rows that are no longer in the list
        String lsWhere = " WHERE sTransNox = " + SQLUtil.toSQL(versionNo)
                + " AND nEntryNox > " + paGiveaways.size();
        boolean lbHasExtra;
        ResultSet loRS = poGRider.executeQuery("SELECT nEntryNox FROM " + poModel.getTable() + lsWhere);
        try {
            lbHasExtra = loRS.next();
        } finally {
            MiscUtil.close(loRS);
        }
        if (lbHasExtra) {
            String lsSQL = "DELETE FROM " + poModel.getTable() + lsWhere;
            if (poGRider.executeQuery(lsSQL, poModel.getTable(), poGRider.getBranchCode(), "", "") <= 0L) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to remove old giveaway records.");
                return poJSON;
            }
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    // ------------------------------------------------------------------
    // Parameter overrides
    // ------------------------------------------------------------------

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();
        poModel.setModifiedDate(poGRider.getServerDate());
        poJSON.put("result", "success");
        return poJSON;
    }

    /** Browses giveaway records and opens the selected one into {@link #getModel()}. */
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = MiscUtil.makeSelect(getModel());

        System.out.println("SEARCH RECORD : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Transaction No.»Entry No.",
                "sTransNox»nEntryNox",
                "a.sTransNox»a.nEntryNox",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord(
                    String.valueOf(poJSON.get("sTransNox")),
                    String.valueOf(poJSON.get("nEntryNox")));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
}