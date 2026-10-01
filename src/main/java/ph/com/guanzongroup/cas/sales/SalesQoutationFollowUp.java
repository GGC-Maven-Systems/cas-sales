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
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_FollowUp;
import ph.com.guanzongroup.cas.sales.services.SalesModels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the follow-ups of a Sales Quotation version
 * (table Sales_Quotation_FollowUp, keyed by sTransNox + nEntryNox).
 *
 * <p>
 * Every time a version is followed up, a new record is added with the next
 * entry number, so a version has a history of follow-ups. This class works on
 * one follow-up at a time; {@link #getFollowUps(String)} returns the history
 * and {@link #getNextEntryNo(String)} gives the entry number for a new one.
 * {@link SalesQoutation} sets the quotation number (sTransNox), the version number
 * (sReferNox) and the entry number when the follow-up is saved.
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 * @module Sales Quotation
 * @since 1.0
 */
public class SalesQoutationFollowUp extends Parameter {

    /** Follow-up model that stores the current record. */
    Model_Sales_Quotation_FollowUp poModel;

    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = RecordStatus.ACTIVE;
        poModel = new SalesModels(poGRider).SalesQuotationFollowUp();
        super.initialize();
    }

    @Override
    public Model_Sales_Quotation_FollowUp getModel() {
        return poModel;
    }

    /** Next entry number for a quotation's follow-ups (1 when it has none); sTransNox is the quotation no. */
    public int getNextEntryNo(String quotationNo) throws SQLException {
        String lsSQL = "SELECT IFNULL(MAX(nEntryNox), 0) + 1 nNextEntr FROM " + poModel.getTable()
                + " WHERE sTransNox = " + SQLUtil.toSQL(quotationNo);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            return loRS.next() ? loRS.getInt("nNextEntr") : 1;
        } finally {
            MiscUtil.close(loRS);
        }
    }

    /**
     * Follow-up history of a version, newest first. Each entry has
     * sTransNox, nEntryNox, sReferNox, dFollowUp, dNextFlup, sFllwUpby,
     * cFllwUpTp and sRemarksx.
     */
    public List<JSONObject> getFollowUps(String versionNo) throws SQLException {
        List<JSONObject> laFollowUps = new ArrayList<>();
        String lsSQL = "SELECT sTransNox, nEntryNox, sReferNox, dFollowUp, dNextFlup, sFllwUpby, cFllwUpTp, sRemarksx"
                + " FROM " + poModel.getTable()
                + " WHERE sReferNox = " + SQLUtil.toSQL(versionNo)
                + " ORDER BY nEntryNox DESC";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            while (loRS.next()) {
                JSONObject loJSON = new JSONObject();
                for (String lsCol : new String[]{"sTransNox", "nEntryNox", "sReferNox", "dFollowUp",
                        "dNextFlup", "sFllwUpby", "cFllwUpTp", "sRemarksx"}) {
                    loJSON.put(lsCol, loRS.getString(lsCol));
                }
                laFollowUps.add(loJSON);
            }
        } finally {
            MiscUtil.close(loRS);
        }
        return laFollowUps;
    }

    /**
     * Validates the follow-up before it is saved: Transaction No. (the
     * quotation's number), Reference No. (the version's number; both filled in
     * by {@link SalesQoutation}), Follow-up Date
     * and Follow-up Type are required.
     */
    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

        if (poModel.getTransactionNo() == null || poModel.getTransactionNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction No. must not be empty.");
            return poJSON;
        }

        if (poModel.getReferenceNo() == null || poModel.getReferenceNo().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Reference No. (version) must not be empty.");
            return poJSON;
        }

        if (poModel.getFollowUpDate() == null) {
            poJSON.put("result", "error");
            poJSON.put("message", "Follow-up Date must not be empty.");
            return poJSON;
        }

        if (poModel.getFollowUpType() == null || poModel.getFollowUpType().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Follow-up Type must not be empty.");
            return poJSON;
        }

        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    /** Browses follow-up records and opens the selected one. */
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

    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL = " SELECT b.sCompnyNm from xxxSysUser a "
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
}