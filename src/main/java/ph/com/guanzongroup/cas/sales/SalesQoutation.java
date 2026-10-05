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
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.ClientType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.ClientGUI;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.Term;
import org.guanzon.cas.parameter.model.Model_Model;
import org.guanzon.cas.parameter.model.Model_Term;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Quotation_Version_Giveaways;
import ph.com.guanzongroup.cas.sales.queries.SalesQoutationsMasterQueries;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationStatic;
import ph.com.guanzongroup.cas.sales.status.SalesQoutationVersionStatic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Sales Quotation transactions within the CAS Sales module.
 *
 * <p>
 * The quotation master ({@link Model_Sales_Quotation_Master}) is the parent.
 * New, open, update and save cascade to its children:
 * </p>
 * <ul>
 *     <li>{@link SalesQoutationVersion} - version master and detail. A quotation
 *         has many versions; only the latest can be edited.</li>
 *     <li>{@link SalesQoutationVersionGiveaways} - many giveaways per version.</li>
 *     <li>{@link SalesQoutationFollowUp} - many follow-ups per version; a new
 *         record is added every time the version is followed up.</li>
 * </ul>
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 * @module Sales Quotation
 * @since 1.0
 */
public class SalesQoutation extends Parameter {

    Model_Sales_Quotation_Master poModel;
    SalesQoutationVersion oSalesQoutationVersion;
    SalesQoutationVersionGiveaways oSalesQoutationVersionGiveaways;
    SalesQoutationFollowUp oSalesQoutationFollowUp;
    private String psSupersedeVersionNo = "";
    /** true when the opened version is the quotation's latest one (only that one may be edited). */
    boolean pbLatestVersion = true;

    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = RecordStatus.ACTIVE;
        poModel = new SalesModels(poGRider).SalesQuotationsMaster();

        oSalesQoutationVersion = new SalesControllers(poGRider, logwrapr).SalesQoutationVersion();
        oSalesQoutationVersion.setBranchCode(poGRider.getBranchCode());
        oSalesQoutationVersion.setWithParent(true);
        oSalesQoutationVersion.InitTransaction();

        oSalesQoutationVersionGiveaways = new SalesControllers(poGRider, logwrapr).SalesQoutationVersionGiveaways();
        oSalesQoutationVersionGiveaways.setWithParentClass(true);
        oSalesQoutationVersionGiveaways.initialize();

        // follow-up is created on demand, see FollowUp()
        oSalesQoutationFollowUp = null;

        super.initialize();
    }

    @Override
    public Model_Sales_Quotation_Master getModel() {
        return poModel;
    }

    /** Accessor for the version controller. */
    public SalesQoutationVersion Version() {
        return oSalesQoutationVersion;
    }

    /** Accessor for the giveaways of the current version. */
    public SalesQoutationVersionGiveaways Giveaways() {
        return oSalesQoutationVersionGiveaways;
    }

    /** Follow-up controller, created the first time it is requested. */
    public SalesQoutationFollowUp FollowUp() throws SQLException, GuanzonException {
        if (oSalesQoutationFollowUp == null) {
            oSalesQoutationFollowUp = new SalesControllers(poGRider, logwrapr).SalesQoutationFollowUp();
            oSalesQoutationFollowUp.setWithParentClass(true);   // the factory already initialized it
        }
        return oSalesQoutationFollowUp;
    }

    // ------------------------------------------------------------------
    // follow-up (history per version)
    // ------------------------------------------------------------------

    /** Follow-up history of the currently opened version, newest first. */
    public List<JSONObject> getFollowUps() throws SQLException, GuanzonException {
        return FollowUp().getFollowUps(oSalesQoutationVersion.Master().getTransactionNo());
    }

    /**
     * Starts a new follow-up for the current version; its entry no. is
     * assigned on save and "follow-up by" defaults to the current user.
     */
    public JSONObject newFollowUp() throws SQLException, GuanzonException {
        poJSON = FollowUp().newRecord();
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        FollowUp().getModel().setFollowUpBy(poGRider.getUserID());

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /** Opens an existing follow-up (entry no.) of the current version for viewing. */
    public JSONObject openFollowUp(int entryNo) throws SQLException, GuanzonException {
        return FollowUp().openRecord(poModel.getTransactionNo(), String.valueOf(entryNo));
    }

    /**
     * Saves the pending follow-up on its own, without editing the quotation.
     * Only the latest version can be followed up.
     */
    public JSONObject saveFollowUp() throws SQLException, GuanzonException, CloneNotSupportedException {
        if (!isPending(oSalesQoutationFollowUp)) {
            return setError("No follow-up to save.");
        }
        if (!pbLatestVersion) {
            return setError("Only the latest version can be followed up.");
        }

        keyFollowUp(poModel.getTransactionNo(), oSalesQoutationVersion.Master().getTransactionNo());

        oSalesQoutationFollowUp.setWithParentClass(false);   // own transaction
        try {
            poJSON = oSalesQoutationFollowUp.saveRecord();
        } finally {
            oSalesQoutationFollowUp.setWithParentClass(true);
        }
        return poJSON;
    }

    /**
     * true only when the follow-up controller exists and holds a record that
     * is being added or edited. A controller that was merely created (no
     * newFollowUp()) or a record that was only opened must not be saved.
     */
    private boolean isPending(SalesQoutationFollowUp followUp) {
        if (followUp == null) return false;
        int lnMode = followUp.getEditMode();
        return lnMode == EditMode.ADDNEW || lnMode == EditMode.UPDATE;
    }

    /**
     * For a new follow-up sets the quotation no. (sTransNox), the version no.
     * (sReferNox) and the next entry no. (counted per quotation, because
     * sTransNox + nEntryNox is the key).
     */
    private void keyFollowUp(String quotationNo, String versionNo) throws SQLException {
        if (oSalesQoutationFollowUp.getEditMode() == EditMode.ADDNEW) {
            oSalesQoutationFollowUp.getModel().setTransactionNo(quotationNo);
            oSalesQoutationFollowUp.getModel().setReferenceNo(versionNo);
            oSalesQoutationFollowUp.getModel().setEntryNo(oSalesQoutationFollowUp.getNextEntryNo(quotationNo));
        }
    }

    // ------------------------------------------------------------------
    // new / open / update / save
    // ------------------------------------------------------------------

    @Override
    public JSONObject newRecord() throws SQLException, GuanzonException {
        poJSON = super.newRecord();   // master record + poEvent "ADD NEW"
        if ("error".equals((String) poJSON.get("result"))) return poJSON;
        System.out.println("category code : " + poModel.getCategoryCode());
        // drop any follow-up / giveaways left over from a previous record
        oSalesQoutationFollowUp = null;
        oSalesQoutationVersionGiveaways.clearGiveaways();
        pbLatestVersion = true;
        psSupersedeVersionNo = "";

        try {
            poJSON = oSalesQoutationVersion.NewTransaction();
            if ("error".equals((String) poJSON.get("result"))) return poJSON;
            oSalesQoutationVersion.setBranchCode(poGRider.getBranchCode());
        } catch (CloneNotSupportedException e) {
            return setError(e.getMessage());
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /** A new quotation starts at version 1 (the model defaults nVersionx to 0). */
    @Override
    protected JSONObject initFields() throws SQLException, GuanzonException {
        poModel.setVersion(1);

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Opens the quotation with its <b>latest</b> version.
     *
     * @param Id quotation transaction no.
     */
    @Override
    public JSONObject openRecord(String Id) throws SQLException, GuanzonException {
        return openRecord(Id, "");
    }

    /**
     * Opens the quotation together with one of its versions and that
     * version's giveaways. Used by the tree table: selecting the quotation row
     * opens the latest version, selecting an older version row opens that
     * version. Follow-ups are read through {@link #getFollowUps()}.
     *
     * @param Id  quotation transaction no.
     * @param Id2 version transaction no.; null or empty opens the latest version
     */
    @Override
    public JSONObject openRecord(String Id, String Id2) throws SQLException, GuanzonException {
        // clear anything left over from a previously opened record
        oSalesQoutationFollowUp = null;
        oSalesQoutationVersionGiveaways.clearGiveaways();
        pbLatestVersion = true;
        psSupersedeVersionNo = "";

        poJSON = super.openRecord(Id);                       // quotation master
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        try {
            String lsLatestNo = getVersionNo(Id, "");
            if (lsLatestNo.isEmpty()) {
                return setError("No version found for quotation " + Id + ".");
            }

            String lsVersionNo = lsLatestNo;
            if (Id2 != null && !Id2.isEmpty()) {
                lsVersionNo = getVersionNo(Id, Id2);         // must belong to this quotation
                if (lsVersionNo.isEmpty()) {
                    return setError("Version " + Id2 + " does not belong to quotation " + Id + ".");
                }
            }
            pbLatestVersion = lsVersionNo.equals(lsLatestNo);

            // version master + detail
            poJSON = oSalesQoutationVersion.OpenTransaction(lsVersionNo);
            if (!"success".equals((String) poJSON.get("result"))) return poJSON;

            // giveaways of the version (none is fine)
            poJSON = oSalesQoutationVersionGiveaways.loadGiveaways(lsVersionNo);
            if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        } catch (CloneNotSupportedException e) {
            return setError(e.getMessage());
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /** true if the currently opened version is the quotation's latest one. */
    public boolean isLatestVersion() {
        return pbLatestVersion;
    }

    /**
     * Versions of a quotation, newest first, for the child rows of the tree
     * table. Each entry has {@code sTransNox}, {@code dTransact},
     * {@code cTranStat}, {@code xStatus} (description) and {@code bLatest}.
     */
    public List<JSONObject> getVersions(String quotationNo) throws SQLException {
        List<JSONObject> laVersions = new ArrayList<>();
        String lsSQL = "SELECT sTransNox, dTransact, cTranStat FROM "
                + oSalesQoutationVersion.Master().getTable()
                + " WHERE sParentID = " + SQLUtil.toSQL(quotationNo)
                + " ORDER BY sTransNox DESC";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            while (loRS.next()) {
                JSONObject loJSON = new JSONObject();
                loJSON.put("sTransNox", loRS.getString("sTransNox"));
                loJSON.put("dTransact", loRS.getString("dTransact"));
                loJSON.put("cTranStat", loRS.getString("cTranStat"));
                loJSON.put("xStatus", oSalesQoutationVersion.getStatus(loRS.getString("cTranStat")));
                loJSON.put("bLatest", laVersions.isEmpty());   // first row = newest
                laVersions.add(loJSON);
            }
        } finally {
            MiscUtil.close(loRS);
        }
        return laVersions;
    }

    // ------------------------------------------------------------------
    // list for the tree table (quotations with their versions)
    // ------------------------------------------------------------------

    /**
     * Retrieves quotations together with all their versions for the tree
     * table, using two queries (quotations, then every version of those
     * quotations) instead of one query per quotation.
     *
     * <p>Every entry is a quotation with {@code sTransNox}, {@code dTransact},
     * {@code cTranStat}, {@code xStatus}, {@code sClientID}, {@code sCompnyNm}
     * and {@code aVersions}, a {@code List<JSONObject>} (newest first) whose
     * entries have {@code sTransNox}, {@code sParentID}, {@code dTransact},
     * {@code cTranStat}, {@code xStatus}, {@code nTranTotl}, {@code dValdThru}
     * and {@code bLatest}.</p>
     *
     * @param industryCode industry filter, null or empty for all
     * @param categoryCode category filter, null or empty for all
     * @param transNo      partial quotation no., null or empty for all
     * @param customer     partial customer name, null or empty for all
     * @param status       quotation status code(s), null or empty for all
     */
    public List<JSONObject> getQuotationList(String industryCode, String categoryCode,
                                             String transNo, String customer, String status) throws SQLException {
        poGRider.ensureConnected();

        // conditions shared by both queries (a = quotation, b = client)
        String lsCondition = "";
        if (industryCode != null && !industryCode.isEmpty()) {
            lsCondition = andCondition(lsCondition, "a.sIndstCdx = " + SQLUtil.toSQL(industryCode));
        }
        if (categoryCode != null && !categoryCode.isEmpty()) {
            lsCondition = andCondition(lsCondition, "a.sCategrCd = " + SQLUtil.toSQL(categoryCode));
        }
        if (transNo != null && !transNo.trim().isEmpty()) {
            lsCondition = andCondition(lsCondition, "a.sTransNox LIKE " + SQLUtil.toSQL("%" + transNo.trim() + "%"));
        }
        if (customer != null && !customer.trim().isEmpty()) {
            lsCondition = andCondition(lsCondition, "b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + customer.trim() + "%"));
        }
        if (status != null && !status.isEmpty()) {
            StringBuilder lsIn = new StringBuilder();
            for (int lnCtr = 0; lnCtr < status.length(); lnCtr++) {
                if (lnCtr > 0) lsIn.append(", ");
                lsIn.append(SQLUtil.toSQL(Character.toString(status.charAt(lnCtr))));
            }
            lsCondition = andCondition(lsCondition, "a.cTranStat IN (" + lsIn + ")");
        }

        // 1. quotations (parents)
        Map<String, JSONObject> loQuotations = new LinkedHashMap<>();
        String lsSQL = SalesQoutationsMasterQueries.SQL_QuotationList();
        if (!lsCondition.isEmpty()) lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        lsSQL = lsSQL + " ORDER BY a.sTransNox DESC";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            while (loRS.next()) {
                JSONObject loJSON = new JSONObject();
                loJSON.put("sTransNox", nvl(loRS.getString("sTransNox")));
                loJSON.put("dTransact", nvl(loRS.getString("dTransact")));
                loJSON.put("cTranStat", nvl(loRS.getString("cTranStat")));
                loJSON.put("xStatus", oSalesQoutationVersion.getStatus(nvl(loRS.getString("cTranStat"))));
                loJSON.put("sClientID", nvl(loRS.getString("sClientID")));
                loJSON.put("sCompnyNm", nvl(loRS.getString("sCompnyNm")));
                loJSON.put("aVersions", new ArrayList<JSONObject>());
                loQuotations.put((String) loJSON.get("sTransNox"), loJSON);
            }
        } finally {
            MiscUtil.close(loRS);
        }
        if (loQuotations.isEmpty()) return new ArrayList<>();

        // 2. versions of those quotations, newest first inside each quotation
        lsSQL = SalesQoutationsMasterQueries.SQL_QuotationVersionList();
        if (!lsCondition.isEmpty()) lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        lsSQL = lsSQL + " ORDER BY v.sParentID, v.sTransNox DESC";
        System.out.println("Executing SQL: " + lsSQL);
        loRS = poGRider.executeQuery(lsSQL);
        try {
            while (loRS.next()) {
                JSONObject loParent = loQuotations.get(loRS.getString("sParentID"));
                if (loParent == null) continue;

                @SuppressWarnings("unchecked")
                List<JSONObject> laVersions = (List<JSONObject>) loParent.get("aVersions");

                JSONObject loJSON = new JSONObject();
                loJSON.put("sTransNox", nvl(loRS.getString("sTransNox")));
                loJSON.put("sParentID", nvl(loRS.getString("sParentID")));
                loJSON.put("dTransact", nvl(loRS.getString("dTransact")));
                loJSON.put("cTranStat", nvl(loRS.getString("cTranStat")));
                loJSON.put("xStatus", oSalesQoutationVersion.getStatus(nvl(loRS.getString("cTranStat"))));
                loJSON.put("nTranTotl", nvl(loRS.getString("nTranTotl")));
                loJSON.put("dValdThru", nvl(loRS.getString("dValdThru")));
                loJSON.put("bLatest", laVersions.isEmpty());   // first row = newest
                laVersions.add(loJSON);
            }
        } finally {
            MiscUtil.close(loRS);
        }

        return new ArrayList<>(loQuotations.values());
    }

    /** Joins conditions with AND (no WHERE; MiscUtil.addCondition adds it). */
    private String andCondition(String where, String condition) {
        return where.isEmpty() ? condition : where + " AND " + condition;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }


    /**
     * Puts the quotation, its version and the giveaways in update mode. A
     * follow-up is not touched; it is added through {@link #newFollowUp()}.
     * Only the latest version can be modified.
     */
    @Override
    public JSONObject updateRecord() {
        if (!pbLatestVersion) {
            return setError("Only the latest version can be modified.");
        }

        poJSON = super.updateRecord();                       // quotation master
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        poJSON = oSalesQoutationVersion.UpdateTransaction(); // version master + detail
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        poJSON = oSalesQoutationVersionGiveaways.updateGiveaways();
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Called by {@link Parameter#saveRecord()} after the quotation master is
     * saved. Saves version (master + detail), giveaways and, if pending, the
     * follow-up. Any failure is returned as an error so the parent rolls back
     * everything.
     */
    @Override
    protected JSONObject saveOthers() throws SQLException, GuanzonException {
        try {
            String lsQuotationNo = poModel.getTransactionNo();

            // version: master points to the quotation, detail rows are keyed in its willSave()
            oSalesQoutationVersion.Master().setParentId(lsQuotationNo);
            poJSON = oSalesQoutationVersion.SaveTransaction();
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            String lsVersionNo = oSalesQoutationVersion.Master().getTransactionNo();

            // giveaways: numbered and keyed by the version's number
            poJSON = oSalesQoutationVersionGiveaways.saveGiveaways(lsVersionNo);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            if (!psSupersedeVersionNo.isEmpty()) {
                String lsNewVersionNo = oSalesQoutationVersion.Master().getTransactionNo();

                poJSON = oSalesQoutationVersion.OpenTransaction(psSupersedeVersionNo);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }

                poJSON = oSalesQoutationVersion.SupersedeTransaction("Superseded by a new version.");
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }

                poJSON = oSalesQoutationVersion.OpenTransaction(lsNewVersionNo);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
                psSupersedeVersionNo = "";
            }

            // follow-up: only if one is being added or edited
            if (isPending(oSalesQoutationFollowUp)) {
                keyFollowUp(lsQuotationNo, lsVersionNo);
                poJSON = oSalesQoutationFollowUp.saveRecord();
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }
            }

            poJSON = new JSONObject();
            poJSON.put("result", "success");
            return poJSON;
        } catch (CloneNotSupportedException | SQLException | GuanzonException |  ParseException e) {
            return setError(e.getMessage());
        }
    }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();

//        if (poGRider.getUserLevel() < UserRight.BRANCH_MANAGER) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "User is not allowed to save record.");
//            return poJSON;
//        }

        if (poModel.getClientId() == null || poModel.getClientId().isEmpty()) {
            return setError("Client must not be empty.");
        }

        if (poModel.getIndustryCode() == null || poModel.getIndustryCode().isEmpty()) {
            return setError("Industry Code must not be empty.");
        }

        if (poModel.getCategoryCode() == null || poModel.getCategoryCode().isEmpty()) {
            return setError("Category Code must not be empty.");
        }

        poJSON = oSalesQoutationVersionGiveaways.validateGiveaways();
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        poJSON = new JSONObject();

        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());

        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Browses Sales Quotations and opens the selected one (latest version).
     *
     * @param value  search text
     * @param byCode true to search by transaction number, false by client name
     */
    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsSQL = MiscUtil.addCondition(SalesQoutationsMasterQueries.SQL_QuotationList(),
                "a.cTranStat = " + SQLUtil.toSQL(SalesQoutationStatic.OPEN));

        System.out.println("SEARCH RECORD : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Transaction No.»Date»Client»Version No.",
                "sTransNox»dTransact»sCompnyNm»sVersnNox",
                "a.sTransNox»a.dTransact»IFNULL(b.sCompnyNm, '')»v.sTransNox",
                byCode ? 0 : 2);

        if (poJSON != null) {
            return openRecord((String) poJSON.get("sTransNox"));
        } else {
            return setError("No record loaded.");
        }
    }

    /**
     * Version no. of a quotation.
     *
     * @param versionNo a specific version to verify, or "" for the latest one
     * @return the version no., or "" if not found
     */
    private String getVersionNo(String quotationNo, String versionNo) throws SQLException {
        String lsSQL = "SELECT sTransNox FROM " + oSalesQoutationVersion.Master().getTable()
                + " WHERE sParentID = " + SQLUtil.toSQL(quotationNo)
                + (versionNo.isEmpty() ? "" : " AND sTransNox = " + SQLUtil.toSQL(versionNo))
                + " ORDER BY sTransNox DESC LIMIT 1";
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            return loRS.next() ? loRS.getString("sTransNox") : "";
        } finally {
            MiscUtil.close(loRS);
        }
    }
    /** Version number (1, 2, 3...) of a version = how many versions of the quotation are at or below it. */
    public int getVersionNumber(String quotationNo, String versionNo) throws SQLException {
        String lsSQL = "SELECT COUNT(*) nVersionNo FROM " + oSalesQoutationVersion.Master().getTable()
                + " WHERE sParentID = " + SQLUtil.toSQL(quotationNo)
                + " AND sTransNox <= " + SQLUtil.toSQL(versionNo);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            return loRS.next() ? loRS.getInt("nVersionNo") : 0;
        } finally {
            MiscUtil.close(loRS);
        }
    }
    public boolean isNewVersionPending() {
        return !psSupersedeVersionNo.isEmpty();
    }
    // ------------------------------------------------------------------
    // confirm
    // ------------------------------------------------------------------

    public JSONObject confirmVersion(String remarks)
            throws SQLException, GuanzonException, CloneNotSupportedException, java.text.ParseException {
        if (poModel.getEditMode() != EditMode.READY) {
            return setError("No quotation was loaded, or it is still being added/edited.");
        }
        if (!pbLatestVersion) {
            return setError("Only the latest version can be confirmed.");
        }
        if (SalesQoutationVersionStatic.CONFIRMED.equals(oSalesQoutationVersion.Master().getTransactionStatus())) {
            return setError("Version was already confirmed.");
        }

        return oSalesQoutationVersion.ConfirmTransaction(remarks);

    }

    /**
     * Confirms the opened quotation and its (latest) version together. Both
     * status updates run in one transaction: if either fails, both are rolled
     * back. The quotation must be open (not being added or edited) and the
     * opened version must be the latest one.
     *
     * @param remarks confirmation remarks (stored in the version's status history)
     */
    public JSONObject confirmRecord(String remarks)
            throws SQLException, GuanzonException, CloneNotSupportedException, java.text.ParseException {
        if (poModel.getEditMode() != EditMode.READY) {
            return setError("No quotation was loaded, or it is still being added/edited.");
        }
        if (!pbLatestVersion) {
            return setError("Only the latest version can be confirmed.");
        }
        if (SalesQoutationStatic.CONFIRMED.equals(poModel.getTransactionStatus())) {
            return setError("Quotation was already confirmed.");
        }
        if (!SalesQoutationStatic.OPEN.equals(poModel.getTransactionStatus())) {
            return setError("Only an open quotation can be confirmed.");
        }

        String lsQuotationNo = poModel.getTransactionNo();
        String lsVersionNo = oSalesQoutationVersion.Master().getTransactionNo();

        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, SalesQoutationStatic.CONFIRMED, false, pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        // 2. version (runs under this transaction, see setWithParent(true))
        poJSON = oSalesQoutationVersion.ConfirmTransaction(remarks);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        // reload so the screen shows the new status of both records
        poJSON = openRecord(lsQuotationNo, lsVersionNo);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        return setResult("success", "Quotation and version confirmed successfully.");
    }

    public JSONObject lostRecord(String remarks)
            throws SQLException, GuanzonException, CloneNotSupportedException, java.text.ParseException {
        if (poModel.getEditMode() != EditMode.READY) {
            return setError("No quotation was loaded, or it is still being added/edited.");
        }

        String lsQuotationNo = poModel.getTransactionNo();
        String lsVersionNo = oSalesQoutationVersion.Master().getTransactionNo();

        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, SalesQoutationStatic.LOST, false, pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        // 2. version (runs under this transaction, see setWithParent(true))
        poJSON = oSalesQoutationVersion.LostTransaction(remarks);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

        poGRider.commitTrans();

        // reload so the screen shows the new status of both records
        poJSON = openRecord(lsQuotationNo, lsVersionNo);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        return setResult("success", "Quotation and version marked as lost successfully.");
    }

    public JSONObject voidRecord(String remarks)
            throws SQLException, GuanzonException, CloneNotSupportedException, java.text.ParseException {
        if (poModel.getEditMode() != EditMode.READY) {
            return setError("No quotation was loaded, or it is still being added/edited.");
        }
        String lsQuotationNo = poModel.getTransactionNo();
        String lsVersionNo = oSalesQoutationVersion.Master().getTransactionNo();

        poJSON = statusChange(poModel.getTable(), (String) poModel.getValue("sTransNox"), remarks, SalesQoutationStatic.VOID, false, pbWthParent);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        // 2. version (runs under this transaction, see setWithParent(true))
        poJSON = oSalesQoutationVersion.VoidTransaction(remarks);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        poGRider.commitTrans();

        // reload so the screen shows the new status of both records
        poJSON = openRecord(lsQuotationNo, lsVersionNo);
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;

        return setResult("success", "Quotation and version successfully voided.");
    }

    private JSONObject setError(String message) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "error");
        loJSON.put("message", message);
        return loJSON;
    }

    private JSONObject setResult(String result, String message) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", result);
        loJSON.put("message", message);
        return loJSON;
    }


    /*Search Master References*/
    /**
     * Search for Inquiring customer
     * @param value
     * @param byCode
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException
     */
    public JSONObject SearchClient(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType(poModel.getClientType());
        poJSON = object.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {


            poModel.setClientId(object.Master().getModel().getClientId());
            System.out.println("Get Address " + poModel.ClientAddress().getAddressId());
            poModel.setAddressId(poModel.ClientAddress().getAddressId()); //TODO
            poModel.setContactId(poModel.ClientMobile().getMobileId()); //TODO
        }

        System.out.println("Client ID : " + poModel.getClientId());
        System.out.println("Address ID : " + poModel.getAddressId());
        System.out.println("Contact ID : " + poModel.getContactId());

        return poJSON;
    }
    public JSONObject addClient() throws SQLException, GuanzonException, Exception {
        JSONObject loResult = new JSONObject();
        String lsClientId = poModel.getClientId();
        lsClientId = (lsClientId == null || lsClientId.isEmpty()) ? "" : lsClientId;

        ClientGUI loClient = new ClientGUI();

        loClient.setGRider(poGRider);
        loClient.setLogWrapper(null);

        loClient.setClientType(ClientType.INDIVIDUAL);
        if(!ClientType.INDIVIDUAL.equals(poModel.getClientType())){
            loClient.setCategoryCode(poModel.getCategoryCode());
        }
        loClient.setByCode(false);
        loClient.setClientId(lsClientId);
        CommonUtils.showModal(loClient);

        if (!loClient.isCancelled()) {
            lsClientId = loClient.getClient().getModel().getClientId();
            poModel.setClientId(lsClientId != null ? lsClientId : "");
            poModel.setAddressId(poModel.ClientAddress().getAddressId());
            poModel.setContactId(poModel.ClientMobile().getMobileId());
        }
        loResult.put("result", "success");
        return loResult;
    }

    public JSONObject SearchTerm(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Term object = new ParamControllers(poGRider, logwrapr).Term();
        object.setRecordStatus("1");

        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            oSalesQoutationVersion.Master().setTermId((object.getModel().getTermId()));
            System.out.println("Term ID : " + oSalesQoutationVersion.Master().getTermId());
        }

        return poJSON;
    }

    public JSONObject SearchBranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            oSalesQoutationVersion.Master().setBranchCode((object.getModel().getBranchCode()));
            System.out.println("Branch ID : " + oSalesQoutationVersion.Master().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject SearchDetailItem(String value, int MCITemRow, String Category, int byCode) throws SQLException, GuanzonException {
        if (MCITemRow < 0 || MCITemRow >= oSalesQoutationVersion.getDetailCount()) {
            return setError("Select an item row first.");
        }

        String lsSQL = MiscUtil.addCondition(SalesQoutationsMasterQueries.SQL_MCItem(),
                "a.sIndstCdx = " + SQLUtil.toSQL(poModel.getIndustryCode()) + " AND a.sCategCd1 = " + SQLUtil.toSQL(Category));
        System.out.println("Executing SQL: " + lsSQL);

        JSONObject  loBrowse = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Stock ID»Brand»Model»Variant»Color",
                "sStockIDx»xBrandNme»xModelNme»xVrntName»xColorNme",
                "a.sStockIDx»c.sDescript»b.sDescript»d.sDescript»e.sDescript",
                byCode);

        if (loBrowse == null || loBrowse.get("sStockIDx") == null) {
            return setError("No record loaded.");
        }
        System.out.println("Stock ID : " + loBrowse.get("sStockIDx"));
        System.out.println("Unit Price : " + loBrowse.get("nUnitPrce"));
        String lsStockId = (String) loBrowse.get("sStockIDx");

        double lnUnitPrice = 0.00;
        Object loPrice = loBrowse.get("nUnitPrce");
        if (loPrice != null && !loPrice.toString().trim().isEmpty()) {
            try {
                lnUnitPrice = Double.parseDouble(loPrice.toString().replace(",", "").trim());
            } catch (NumberFormatException e) {
                lnUnitPrice = 0.00;
            }
        }
        // duplicate check: same stock id on any other row
        for (int lnCtr = 0; lnCtr < oSalesQoutationVersion.getDetailCount(); lnCtr++) {
            if (lnCtr == MCITemRow) continue;

            String lsExisting = oSalesQoutationVersion.Detail(lnCtr).getStockId();
            if (lsExisting != null && lsExisting.equals(lsStockId)) {
                return setError("Item " + lsStockId + " is already added in row " + (lnCtr + 1) + ".");
            }
        }
        oSalesQoutationVersion.Detail(MCITemRow).setStockId(lsStockId);
        oSalesQoutationVersion.Detail(MCITemRow).setUnitPrice(lnUnitPrice);

        JSONObject loResult = new JSONObject();
        loResult.put("result", "success");
        return loResult;
    }

    public JSONObject SearchGawayItem(String value, int GawayITemRow, String Category, int byCode) throws SQLException, GuanzonException {
        if (GawayITemRow < 0 || GawayITemRow >= Giveaways().getGiveawayCount()) {
            return setError("Select an item row first.");
        }

        String lsSQL = MiscUtil.addCondition(SalesQoutationsMasterQueries.SQL_GawayItem(),
                "a.sIndstCdx = " + SQLUtil.toSQL(poModel.getIndustryCode()));
        System.out.println("Executing SQL: " + lsSQL);

        if (Category != null && !Category.isEmpty()) {
            lsSQL = lsSQL + " AND a.sCategCd1 = " + SQLUtil.toSQL(Category);
        }
        JSONObject  loBrowse = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Stock ID»Barcode»Description»Color",
                "sStockIDx»sBarCodex»xStockDesc»xColorNme",
                "a.sStockIDx»a.sBarCodex»a.sDescript»e.sDescript",
                byCode);

        if (loBrowse == null || loBrowse.get("sStockIDx") == null) {
            return setError("No record loaded.");
        }
        System.out.println("Stock ID : " + loBrowse.get("sStockIDx"));
        System.out.println("Unit Price : " + loBrowse.get("nUnitPrce"));
        String lsStockId = (String) loBrowse.get("sStockIDx");


        // duplicate check: same stock id on any other row
        for (int lnCtr = 0; lnCtr < oSalesQoutationVersion.getDetailCount(); lnCtr++) {
            if (lnCtr == GawayITemRow) continue;

            String lsExisting = oSalesQoutationVersion.Detail(lnCtr).getStockId();
            if (lsExisting != null && lsExisting.equals(lsStockId)) {
                return setError("Item " + lsStockId + " is already added in row " + (lnCtr + 1) + ".");
            }
        }
        Giveaways().Giveaway(GawayITemRow).setStockId(lsStockId);
        JSONObject loResult = new JSONObject();
        loResult.put("result", "success");
        return loResult;
    }

    public JSONObject SearchMCItemPromo(String value, int MCItemRow, int byCode) throws SQLException, GuanzonException {
        if (MCItemRow < 0 || MCItemRow >= oSalesQoutationVersion.getDetailCount()) {
            return setError("Select an item row first.");
        }

        String lsSQL = MiscUtil.addCondition(SalesQoutationsMasterQueries.SQL_MCItemPromo(),
                "a.sIndstCdx = " + SQLUtil.toSQL(poModel.getIndustryCode())
                        + " AND b.sModelIDx = " + SQLUtil.toSQL(oSalesQoutationVersion.Detail(MCItemRow).Inventory().getModelId()));
        System.out.println("Executing SQL: " + lsSQL);

        JSONObject  loBrowse = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Promo ID»Description»Promo From»Promo To",
                "sPromIDxx»sPromDesc»dFromDate»dThruDate",
                "a.sPromIDxx»a.sPromDesc»a.dFromDate»a.dThruDate",
                byCode);

        if (loBrowse == null || loBrowse.get("sPromIDxx") == null) {
            return setError("No record loaded.");
        }
        System.out.println("Promo ID : " + loBrowse.get("sPromIDxx"));
        System.out.println("Promo ID : " + loBrowse.get("nDiscRate"));
        System.out.println("Promo ID : " + loBrowse.get("nDiscAmtx"));
        System.out.println("Promo ID : " + loBrowse.get("nFreightx"));
        oSalesQoutationVersion.Detail(MCItemRow).setPromoCode((String) loBrowse.get("sPromIDxx"));
        oSalesQoutationVersion.Detail(MCItemRow).setDiscount(Double.parseDouble((String)loBrowse.get("nDiscRate")));
        oSalesQoutationVersion.Detail(MCItemRow).setAdditionalDiscount(Double.parseDouble((String)loBrowse.get("nDiscAmtx")));
        oSalesQoutationVersion.Detail(MCItemRow).setFreight(Double.parseDouble((String)loBrowse.get("nFreightx")));

        JSONObject loResult = new JSONObject();
        loResult.put("PromoDesc", (String) loBrowse.get("sPromDesc"));
        loResult.put("result", "success");
        return loResult;
    }

    public double computeMCItemDetail(
            double srp,
            int qty,
            double discount,           // percentage
            double additionaldiscount, // fixed amount
            double freight,
            double registration,
            double insurance,
            String vatType
    ) {
        // ============================================
        // TRANSACTION TOTAL
        // ============================================
        double grossUnitSRP = srp * qty;
        // ============================================
        // SALES COMPUTATION
        // ============================================
        // Discount is percentage
        double discountAmount =
                grossUnitSRP * (discount / 100.0);
        // Net sales after percentage discount
        double netSales =
                grossUnitSRP - (discountAmount + additionaldiscount);
        if (netSales < 0) {
            netSales = 0;
        }
        double totalfreight = freight * qty;
        double totalregistration = registration * qty;
        double totalinsurance = insurance * qty;
        // ============================================
        // TOTAL AMOUNT
        // ============================================
        double totalAmount;
        // No VAT
        if (vatType == null || vatType.trim().isEmpty()) {

            totalAmount =
                    netSales
                            + totalfreight
                            + totalregistration
                            + totalinsurance;
        }
        // VAT Inclusive
        else if (SalesQoutationVersionStatic.VatType.VAT_INCLUSIVE.equalsIgnoreCase(vatType.trim())) {
            // Net sales already includes VAT
            double vatableSales = netSales / 1.12;
            double vatAmount = netSales - vatableSales;

            totalAmount =
                    netSales
                            + totalfreight
                            + totalregistration
                            + totalinsurance;
        }
        // VAT Exclusive
        else if (SalesQoutationVersionStatic.VatType.VAT_EXCLUSIVE.equalsIgnoreCase(vatType.trim())) {

            double vatAmount = netSales * 0.12;

            totalAmount =
                    netSales
                            + vatAmount
                            + totalfreight
                            + totalregistration
                            + totalinsurance;
        }
        // Unknown VAT type
        else {

            totalAmount =
                    netSales
                            + totalfreight
                            + totalregistration
                            + totalinsurance;
        }
        return totalAmount;
    }


    /** Plain holder for one detail row, so the old values survive NewTransaction(). */
    private static class DetailCopy {
        String stockId;
        Integer quantity;
        Double unitPrice;
        Double discount;
        Double additionalDiscount;
        Double freight;
        Double registrationAmount;
        Double insuranceAmount;
        String promoCode;
    }

    /**
     * Starts a new version of the opened quotation: quotation nVersionx + 1, and the
     * latest version (master, detail, giveaways) copied into a new, unsaved version.
     * The old version becomes SUPERCEDED when this is saved (see saveOthers()).
     */
    public JSONObject createFromVersion() throws SQLException, GuanzonException, CloneNotSupportedException {
        if (poModel.getEditMode() != EditMode.READY) {
            return setError("No quotation was loaded, or it is still being added/edited.");
        }
        if (!pbLatestVersion) {
            return setError("Only the latest version can be used to create a new version.");
        }
//        if (!SalesQoutationStatic.OPEN.equals(poModel.getTransactionStatus())) {
//            return setError("Only an open quotation can have a new version.");
//        }

        String lsOldVersionNo = oSalesQoutationVersion.Master().getTransactionNo();

        // 1. keep the old values BEFORE the version controller is reset
        String lsTitle = oSalesQoutationVersion.Master().getTitleName();
        java.util.Date ldExpected = oSalesQoutationVersion.Master().getExpectedDate();
        java.util.Date ldValidThru = oSalesQoutationVersion.Master().getValidThruDate();
        String lsReasons = oSalesQoutationVersion.Master().getReasons();
        String lsDeliveryType = oSalesQoutationVersion.Master().getDeliveryType();
        String lsDeliverTo = oSalesQoutationVersion.Master().getDeliverTo();
        String lsBranch = oSalesQoutationVersion.Master().getBranchCode();
        String lsPaymentForm = oSalesQoutationVersion.Master().getPaymentForm();
        String lsTermId = oSalesQoutationVersion.Master().getTermId();
        String lsRemarks = oSalesQoutationVersion.Master().getRemarks();
        String lsRemarks1 = oSalesQoutationVersion.Master().getRemarks1();
        String lsRemarks2 = oSalesQoutationVersion.Master().getRemarks2();

        List<DetailCopy> laOldDetail = new ArrayList<>();
        for (int lnCtr = 0; lnCtr < oSalesQoutationVersion.getDetailCount(); lnCtr++) {
            Model_Sales_Quotation_Version_Detail loRow = oSalesQoutationVersion.Detail(lnCtr);
            if (loRow.getStockId() == null || loRow.getStockId().isEmpty()) continue;   // blank trailing row

            DetailCopy loCopy = new DetailCopy();
            loCopy.stockId = loRow.getStockId();
            loCopy.quantity = loRow.getQuantity();
            loCopy.unitPrice = loRow.getUnitPrice();
            loCopy.discount = loRow.getDiscount();
            loCopy.additionalDiscount = loRow.getAdditionalDiscount();
            loCopy.freight = loRow.getFreight();
            loCopy.registrationAmount = loRow.getRegistrationAmount();
            loCopy.insuranceAmount = loRow.getInsuranceAmount();
            loCopy.promoCode = loRow.getPromoCode();
            laOldDetail.add(loCopy);
        }

        // giveaway rows: the old model objects stay valid after clearGiveaways(), so just keep them
        List<Model_Sales_Quotation_Version_Giveaways> laOldGiveaway =
                new ArrayList<>(oSalesQoutationVersionGiveaways.Giveaways());

        // 2. quotation master: edit mode, version + 1
        poJSON = super.updateRecord();
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        poModel.setVersion(poModel.getVersion() + 1);

        // 3. new version (fresh transaction no., status OPEN) with the same content
        poJSON = oSalesQoutationVersion.NewTransaction();
        if (!"success".equals((String) poJSON.get("result"))) return poJSON;
        oSalesQoutationVersion.setBranchCode(poGRider.getBranchCode());

        oSalesQoutationVersion.Master().setTitleName(lsTitle);
        oSalesQoutationVersion.Master().setExpectedDate(ldExpected);
        oSalesQoutationVersion.Master().setValidThruDate(ldValidThru);
        oSalesQoutationVersion.Master().setReasons(lsReasons);
        oSalesQoutationVersion.Master().setDeliveryType(lsDeliveryType);
        oSalesQoutationVersion.Master().setDeliverTo(lsDeliverTo);
        oSalesQoutationVersion.Master().setBranchCode(lsBranch);
        oSalesQoutationVersion.Master().setPaymentForm(lsPaymentForm);
        oSalesQoutationVersion.Master().setTermId(lsTermId);
        oSalesQoutationVersion.Master().setRemarks(lsRemarks);
        oSalesQoutationVersion.Master().setRemarks1(lsRemarks1);
        oSalesQoutationVersion.Master().setRemarks2(lsRemarks2);

        for (int lnCtr = 0; lnCtr < laOldDetail.size(); lnCtr++) {
            if (lnCtr >= oSalesQoutationVersion.getDetailCount()) {
                poJSON = oSalesQoutationVersion.AddDetail();
                if (!"success".equals((String) poJSON.get("result"))) return poJSON;
            }
            DetailCopy loCopy = laOldDetail.get(lnCtr);
            Model_Sales_Quotation_Version_Detail loNew = oSalesQoutationVersion.Detail(lnCtr);
            loNew.setStockId(loCopy.stockId);
            loNew.setQuantity(loCopy.quantity);
            loNew.setUnitPrice(loCopy.unitPrice);
            loNew.setDiscount(loCopy.discount);
            loNew.setAdditionalDiscount(loCopy.additionalDiscount);
            loNew.setFreight(loCopy.freight);
            loNew.setRegistrationAmount(loCopy.registrationAmount);
            loNew.setInsuranceAmount(loCopy.insuranceAmount);
            loNew.setPromoCode(loCopy.promoCode);
        }

        // 4. giveaways: new rows, numbered and keyed in saveGiveaways()
        oSalesQoutationVersionGiveaways.clearGiveaways();
        for (Model_Sales_Quotation_Version_Giveaways loOld : laOldGiveaway) {
            poJSON = oSalesQoutationVersionGiveaways.addGiveaway();
            if (!"success".equals((String) poJSON.get("result"))) return poJSON;

            Model_Sales_Quotation_Version_Giveaways loNew = oSalesQoutationVersionGiveaways
                    .Giveaway(oSalesQoutationVersionGiveaways.getGiveawayCount() - 1);
            loNew.setStockId(loOld.getStockId());
            loNew.setQuantity(loOld.getQuantity());
            loNew.setRemarks(loOld.getRemarks());
        }

        oSalesQoutationFollowUp = null;     // follow-ups stay with the old version
        pbLatestVersion = true;
        psSupersedeVersionNo = lsOldVersionNo;

        return setResult("success", "New version has been created successfully. Please review the details before saving.");
    }
}