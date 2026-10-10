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
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import java.util.ArrayList;
import java.util.HashMap;
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
//    public List<JSONObject> getQuotationList(String industryCode, String categoryCode,
//                                             String transNo, String customer, String status) throws SQLException {
//        poGRider.ensureConnected();
//
//        // conditions shared by both queries (a = quotation, b = client)
//        String lsCondition = "";
//        if (industryCode != null && !industryCode.isEmpty()) {
//            lsCondition = andCondition(lsCondition, "a.sIndstCdx = " + SQLUtil.toSQL(industryCode));
//        }
//        if (categoryCode != null && !categoryCode.isEmpty()) {
//            lsCondition = andCondition(lsCondition, "a.sCategrCd = " + SQLUtil.toSQL(categoryCode));
//        }
//        if (transNo != null && !transNo.trim().isEmpty()) {
//            lsCondition = andCondition(lsCondition, "a.sTransNox LIKE " + SQLUtil.toSQL("%" + transNo.trim() + "%"));
//        }
//        if (customer != null && !customer.trim().isEmpty()) {
//            lsCondition = andCondition(lsCondition, "b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + customer.trim() + "%"));
//        }
//        if (status != null && !status.isEmpty()) {
//            StringBuilder lsIn = new StringBuilder();
//            for (int lnCtr = 0; lnCtr < status.length(); lnCtr++) {
//                if (lnCtr > 0) lsIn.append(", ");
//                lsIn.append(SQLUtil.toSQL(Character.toString(status.charAt(lnCtr))));
//            }
//            lsCondition = andCondition(lsCondition, "a.cTranStat IN (" + lsIn + ")");
//        }
//
//        // 1. quotations (parents)
//        Map<String, JSONObject> loQuotations = new LinkedHashMap<>();
//        String lsSQL = SalesQoutationsMasterQueries.SQL_QuotationList();
//        if (!lsCondition.isEmpty()) lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
//        lsSQL = lsSQL + " ORDER BY a.sTransNox DESC";
//        System.out.println("Executing SQL: " + lsSQL);
//        ResultSet loRS = poGRider.executeQuery(lsSQL);
//        try {
//            while (loRS.next()) {
//                JSONObject loJSON = new JSONObject();
//                loJSON.put("sTransNox", nvl(loRS.getString("sTransNox")));
//                loJSON.put("dTransact", nvl(loRS.getString("dTransact")));
//                loJSON.put("cTranStat", nvl(loRS.getString("cTranStat")));
//                loJSON.put("xStatus", oSalesQoutationVersion.getStatus(nvl(loRS.getString("cTranStat"))));
//                loJSON.put("sClientID", nvl(loRS.getString("sClientID")));
//                loJSON.put("sCompnyNm", nvl(loRS.getString("sCompnyNm")));
//                loJSON.put("aVersions", new ArrayList<JSONObject>());
//                loQuotations.put((String) loJSON.get("sTransNox"), loJSON);
//            }
//        } finally {
//            MiscUtil.close(loRS);
//        }
//        if (loQuotations.isEmpty()) return new ArrayList<>();
//
//        // 2. versions of those quotations, newest first inside each quotation
//        lsSQL = SalesQoutationsMasterQueries.SQL_QuotationVersionList();
//        if (!lsCondition.isEmpty()) lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
//        lsSQL = lsSQL + " ORDER BY v.sParentID, v.sTransNox DESC";
//        System.out.println("Executing SQL: " + lsSQL);
//        loRS = poGRider.executeQuery(lsSQL);
//        try {
//            while (loRS.next()) {
//                JSONObject loParent = loQuotations.get(loRS.getString("sParentID"));
//                if (loParent == null) continue;
//
//                @SuppressWarnings("unchecked")
//                List<JSONObject> laVersions = (List<JSONObject>) loParent.get("aVersions");
//
//                JSONObject loJSON = new JSONObject();
//                loJSON.put("sTransNox", nvl(loRS.getString("sTransNox")));
//                loJSON.put("sParentID", nvl(loRS.getString("sParentID")));
//                loJSON.put("dTransact", nvl(loRS.getString("dTransact")));
//                loJSON.put("cTranStat", nvl(loRS.getString("cTranStat")));
//                loJSON.put("xStatus", oSalesQoutationVersion.getStatus(nvl(loRS.getString("cTranStat"))));
//                loJSON.put("nTranTotl", nvl(loRS.getString("nTranTotl")));
//                loJSON.put("dValdThru", nvl(loRS.getString("dValdThru")));
//                loJSON.put("bLatest", laVersions.isEmpty());   // first row = newest
//                laVersions.add(loJSON);
//            }
//        } finally {
//            MiscUtil.close(loRS);
//        }
//
//        return new ArrayList<>(loQuotations.values());
//    }
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
                loJSON.put("dConfirmd", nvl(loRS.getString("dConfirmd")));   // empty unless the quotation is confirmed
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
                loJSON.put("dConfirmd", nvl(loRS.getString("dConfirmd")));
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
        if (!SalesQoutationStatic.OPEN.equals(poModel.getTransactionStatus())) {
            if (SalesQoutationStatic.LOST.equals(poModel.getTransactionStatus())) {
                return setError("Quotation was already marked as lost.");
            }
            return setError("Only an open quotation can be marked as lost.");
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
        if (!SalesQoutationStatic.OPEN.equals(poModel.getTransactionStatus())) {
            if (SalesQoutationStatic.VOID.equals(poModel.getTransactionStatus())) {
                return setError("Quotation was already marked as void.");
            }
            return setError("Only an open quotation can be marked as void.");
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

    // ------------------------------------------------------------------
    // print (Jasper) - report: reports/SalesQoutationVersion.jrxml
    // ------------------------------------------------------------------

    /**
     * Prints the opened quotation version with {@code SalesQoutationVersion.jrxml}.
     *
     * <p>The report prints one version: header (date, customer, quotation no.,
     * version), the motorcycle unit list ({@code ModelDataSource}), the giveaway /
     * service list ({@code GiveAwayDataSource}), remarks and the signatories.
     * On Windows the Jasper viewer is shown; on other systems a PDF is written
     * to {@code sys.default.path.config/temp/}.</p>
     */
    public JSONObject printTransaction() {
        try {
            if (poModel.getEditMode() != EditMode.READY) {
                return setError("Open a quotation first. Printing is only available in view mode.");
            }

            JasperPrint loPrint = buildJasperPrint();

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                javax.swing.SwingUtilities.invokeLater(() -> net.sf.jasperreports.view.JasperViewer.viewReport(loPrint, false));
            } else {
                JasperExportManager.exportReportToPdfFile(loPrint, System.getProperty("sys.default.path.config")
                        + "/temp/" + poModel.getTransactionNo() + ".pdf");
            }

            return setResult("success", "Quotation printed successfully.");
        } catch (JRException | SQLException | GuanzonException e) {
            e.printStackTrace();
            return setError("Quotation print aborted! " + e.getMessage());
        }
    }

    /**
     * Exports the opened quotation version straight to a PDF (no preview), using
     * the same report and data as {@link #printTransaction()}, then reveals the
     * file in its folder. If an Explorer window for that folder is already open,
     * it is reused: the exported file is selected and the window is brought to
     * the front instead of opening another one.
     *
     * <p>The file is saved to {@code sys.default.path.config/temp/} as
     * {@code <customer>_<quotation no.>_V<version no.>.pdf}. If a file with that
     * name already exists, it is kept and the new export is saved as
     * {@code ..._V<version no.> (1).pdf}, {@code (2)}, and so on. On success the
     * result also has {@code path}, the full path of the PDF.</p>
     */
    public JSONObject exportTransaction() {
        try {
            if (poModel.getEditMode() != EditMode.READY) {
                return setError("Open a quotation first. Export is only available in view mode.");
            }

            JasperPrint loPrint = buildJasperPrint();

            java.io.File loFolder = new java.io.File(System.getProperty("sys.default.path.config") + "/temp/");
            if (!loFolder.exists() && !loFolder.mkdirs()) {
                return setError("Unable to create the export folder " + loFolder.getAbsolutePath() + ".");
            }

            String lsQuotationNo = poModel.getTransactionNo();
            String lsCustomerName = sanitizeFileName(poModel.Client().getCompanyName());
            int lnVersion = getVersionNumber(lsQuotationNo, oSalesQoutationVersion.Master().getTransactionNo());

            String lsBaseName = lsCustomerName + "_" + lsQuotationNo + "_V" + lnVersion;
            java.io.File loFile = getUniqueFile(loFolder, lsBaseName, ".pdf");

            JasperExportManager.exportReportToPdfFile(loPrint, loFile.getAbsolutePath());

            boolean lbOpened = openFileLocation(loFile);

            JSONObject loResult = setResult("success", "Quotation exported to:\n" + loFile.getAbsolutePath()
                    + (lbOpened ? "" : "\n(The folder could not be opened automatically.)"));
            loResult.put("path", loFile.getAbsolutePath());
            return loResult;
        } catch (JRException | SQLException | GuanzonException e) {
            e.printStackTrace();
            return setError("Quotation export aborted! " + e.getMessage());
        }
    }
    /**
     * Returns a file in the folder that does not exist yet. If {@code baseName + extension}
     * is free it is used as is; otherwise " (1)", " (2)", ... is appended to the base name.
     */
    private java.io.File getUniqueFile(java.io.File folder, String baseName, String extension) {
        java.io.File loFile = new java.io.File(folder, baseName + extension);
        int lnCounter = 1;
        while (loFile.exists()) {
            loFile = new java.io.File(folder, baseName + " (" + lnCounter + ")" + extension);
            lnCounter++;
        }
        return loFile;
    }

    /** Removes characters that are illegal in Windows file names (customer names can contain / : * ? etc.). */
    private String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Quotation";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    /**
     * Reveals a file in its folder. On Windows, an already-open Explorer window for
     * that folder is reused (file re-selected, window brought to front); otherwise a
     * new window is opened. Returns false if nothing could be opened.
     */
    private boolean openFileLocation(java.io.File file) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                if (reuseExplorerWindow(file)) {
                    return true;
                }
                new ProcessBuilder("explorer.exe", "/select,", file.getAbsolutePath()).start();
                return true;
            }
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                java.awt.Desktop.getDesktop().open(file.getParentFile());
                return true;
            }
        } catch (java.io.IOException | RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Looks for an open Explorer window showing the file's folder. If found, selects
     * the file in it and brings the window to the front.
     *
     * @return true if an existing window was reused, false if none was found (or the
     *         lookup failed) and the caller should open a new one.
     */
    private boolean reuseExplorerWindow(java.io.File file) {
        // Script is passed Base64/UTF-16LE encoded (-EncodedCommand) to avoid any quoting issues.
        // The file path is passed through an environment variable for the same reason.
        final String script =
                "$file = $env:EXPORT_FILE_PATH\n"
                        + "$folder = [System.IO.Path]::GetDirectoryName($file).TrimEnd('\\')\n"
                        + "$name = [System.IO.Path]::GetFileName($file)\n"
                        + "$shell = New-Object -ComObject Shell.Application\n"
                        + "$target = $null\n"
                        + "foreach ($w in $shell.Windows()) {\n"
                        + "  try {\n"
                        + "    $p = $w.Document.Folder.Self.Path\n"
                        + "    if ($p -and ($p.TrimEnd('\\') -ieq $folder)) { $target = $w; break }\n"
                        + "  } catch {}\n"
                        + "}\n"
                        + "if ($null -eq $target) { exit 1 }\n"
                        + "$item = $target.Document.Folder.ParseName($name)\n"
                        + "if ($item) { $target.Document.SelectItem($item, 29) }\n"   // 1=select 4=deselect others 8=ensure visible 16=focus
                        + "Add-Type -TypeDefinition @'\n"
                        + "using System;\n"
                        + "using System.Runtime.InteropServices;\n"
                        + "public class WinFront {\n"
                        + "  [DllImport(\"user32.dll\")] public static extern bool SetForegroundWindow(IntPtr h);\n"
                        + "  [DllImport(\"user32.dll\")] public static extern bool ShowWindow(IntPtr h, int c);\n"
                        + "  [DllImport(\"user32.dll\")] public static extern bool IsIconic(IntPtr h);\n"
                        + "  [DllImport(\"user32.dll\")] public static extern void keybd_event(byte k, byte s, uint f, UIntPtr e);\n"
                        + "}\n"
                        + "'@\n"
                        + "$h = [IntPtr]$target.HWND\n"
                        + "if ([WinFront]::IsIconic($h)) { [void][WinFront]::ShowWindow($h, 9) }\n"  // SW_RESTORE
                        + "[WinFront]::keybd_event(0x12, 0, 0, [UIntPtr]::Zero)\n"                 // Alt down/up: lets us take foreground
                        + "[WinFront]::keybd_event(0x12, 0, 2, [UIntPtr]::Zero)\n"
                        + "[void][WinFront]::SetForegroundWindow($h)\n"
                        + "exit 0\n";

        try {
            String lsEncoded = java.util.Base64.getEncoder()
                    .encodeToString(script.getBytes(java.nio.charset.StandardCharsets.UTF_16LE));

            ProcessBuilder loBuilder = new ProcessBuilder(
                    "powershell.exe", "-NoProfile", "-NonInteractive",
                    "-WindowStyle", "Hidden", "-EncodedCommand", lsEncoded);
            loBuilder.environment().put("EXPORT_FILE_PATH", file.getAbsolutePath());
            loBuilder.redirectErrorStream(true);

            Process loProcess = loBuilder.start();
            loProcess.getInputStream().close(); // we don't need the output

            if (!loProcess.waitFor(8, java.util.concurrent.TimeUnit.SECONDS)) {
                loProcess.destroyForcibly();
                return false;
            }
            return loProcess.exitValue() == 0;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Builds the filled report for the opened version (parameters, motorcycle
     * units and giveaways). Shared by print and export so both give the same
     * document.
     */
    private JasperPrint buildJasperPrint() throws JRException, SQLException, GuanzonException {
        String lsConfig = System.getProperty("sys.default.path.config");
        String lsImages = lsConfig + "/reports/images/";
        String lsQuotationNo = poModel.getTransactionNo();
        String lsVersionNo = oSalesQoutationVersion.Master().getTransactionNo();

        // ---------------- parameters ----------------
        Map<String, Object> parameters = new HashMap<>();

        // watermark: DRAFT until the version is confirmed
        String lsWatermark = lsImages + "draft.png";
        String lsVersionStat = oSalesQoutationVersion.Master().getTransactionStatus();
        if (SalesQoutationVersionStatic.CONFIRMED.equals(lsVersionStat)
                || SalesQoutationVersionStatic.SALES.equals(lsVersionStat)) {
            lsWatermark = lsImages + "approved.png";
        }
        parameters.put("watermarkImagePath", lsWatermark);
        parameters.put("TitlewatermarkImagePath", lsImages + "Guanzon We Believe Logo.png");
        parameters.put("HeaderDefault", lsImages + "TemplateHeader.png");
        parameters.put("FooterDefault", lsImages + "TemplateFooter.png");

        // header block
        java.util.Date ldDate = oSalesQoutationVersion.Master().getTransactionDate();
        parameters.put("QoutationDate", ldDate == null ? ""
                : new java.text.SimpleDateFormat("MMMM dd, yyyy").format(ldDate));
        parameters.put("CustomerName", safeString(poModel.Client().getCompanyName()));
        parameters.put("CustomerAddress", getCustomerAddress());
        parameters.put("QoutationTitle", safeString(oSalesQoutationVersion.Master().getTitleName()));
        parameters.put("QoutationTransactionNo", safeString(lsQuotationNo));
        parameters.put("QoutationVersion",
                String.valueOf(getVersionNumber(lsQuotationNo, lsVersionNo)));

        // summary block
//        if(oSalesQoutationVersion.Master().getDeliveryType() != null
//                && oSalesQoutationVersion.Master().getDeliveryType().equalsIgnoreCase(SalesQoutationVersionStatic.DeliveryType.DELIVERY)){
//            parameters.put("DeliveryType", "Deliver to Branch");
//        } else if(oSalesQoutationVersion.Master().getDeliveryType() != null && oSalesQoutationVersion.Master().getDeliveryType().equalsIgnoreCase("2")){
//            parameters.put("DeliveryType", "Deliver to Customer");
//        } else {
//            parameters.put("DeliveryType", "N/A");
//        }
        parameters.put("QoutationValidity", safeString(oSalesQoutationVersion.Master().getValidThruDate()));
        parameters.put("Remarks", safeString(oSalesQoutationVersion.Master().getRemarks2()));

        JSONObject loEntry = oSalesQoutationVersion.getEntryBy();
        parameters.put("PrepNme", loEntry != null && !"error".equals((String) loEntry.get("result"))
                ? safeString(loEntry.get("sCompnyNm")) : "");

        String lsConfirmedBy = "";
        if (SalesQoutationVersionStatic.CONFIRMED.equals(lsVersionStat)
                || SalesQoutationVersionStatic.SALES.equals(lsVersionStat)) {
            lsConfirmedBy = oSalesQoutationVersion.getConfirmedBy();
        }
        parameters.put("ConfirmNme", lsConfirmedBy);

        // declared by the report but not printed by its layout
        parameters.put("ReceivrNme", "");
        parameters.put("ImplentTo", "");
        parameters.put("ReferNo", lsVersionNo);

        // ---------------- motorcycle units ----------------
        List<Map<String, ?>> laModels = new ArrayList<>();
        for (int lnCtr = 0; lnCtr < oSalesQoutationVersion.getDetailCount(); lnCtr++) {
            Model_Sales_Quotation_Version_Detail loRow = oSalesQoutationVersion.Detail(lnCtr);
            if (loRow.getStockId() == null || loRow.getStockId().isEmpty()) continue;   // blank trailing row

            double lnSrp = nz(loRow.getUnitPrice());
            if (lnSrp <= 0.00) {   // fall back to the item's selling price
                Object loPrice = loRow.Inventory().getSellingPrice();   // Object: works for boxed or primitive
                if (loPrice != null) {
                    try {
                        lnSrp = Double.parseDouble(loPrice.toString().replace(",", "").trim());
                    } catch (NumberFormatException e) {
                        lnSrp = 0.00;
                    }
                }
            }
            int lnQty = (int) nz(loRow.getQuantity());
            double lnDisc = nz(loRow.getDiscount());
            double lnAddDisc = nz(loRow.getAdditionalDiscount());
            double lnFreight = nz(loRow.getFreight());
            double lnReg = nz(loRow.getRegistrationAmount());
            double lnIns = nz(loRow.getInsuranceAmount());

            // same computation as the screen's item table
            double lnTotal = computeMCItemDetail(lnSrp, lnQty, lnDisc, lnAddDisc, lnFreight, lnReg, lnIns, null);

            Map<String, Object> loMap = new HashMap<>();
            loMap.put("Brand", htmlSafe(loRow.Inventory().Brand().getDescription()));
            loMap.put("Model", htmlSafe(loRow.Inventory().Model().getDescription()));
            loMap.put("Description", htmlSafe(loRow.Inventory().getDescription()));
            loMap.put("Color", htmlSafe(loRow.Inventory().Color().getDescription()));
            loMap.put("SRP", money(lnSrp));
            loMap.put("Discount", money(lnDisc));
            loMap.put("Add_Desc", money(lnAddDisc));
            loMap.put("Freight", money(lnFreight));
            loMap.put("Reg", money(lnReg));
            loMap.put("Insur", money(lnIns));
            loMap.put("Qty", String.valueOf(lnQty));
            loMap.put("Total", money(lnTotal));
            laModels.add(loMap);
        }

        // ---------------- giveaways and services ----------------
        List<Map<String, ?>> laGiveaways = new ArrayList<>();
        for (int lnCtr = 0; lnCtr < oSalesQoutationVersionGiveaways.getGiveawayCount(); lnCtr++) {
            Model_Sales_Quotation_Version_Giveaways loRow = oSalesQoutationVersionGiveaways.Giveaway(lnCtr);
            boolean lbHasItem = loRow.getStockId() != null && !loRow.getStockId().trim().isEmpty();
            boolean lbHasQty = loRow.getQuantity() != null && loRow.getQuantity() > 0;
            boolean lbHasRemarks = loRow.getRemarks() != null && !loRow.getRemarks().trim().isEmpty();
            if (!lbHasItem && !lbHasQty && !lbHasRemarks) continue;                      // blank trailing row

            Map<String, Object> loMap = new HashMap<>();
            loMap.put("Inv_Type", lbHasItem ? "Giveaways" : "Service");                  // same rule as the screen
            loMap.put("Barrcode", lbHasItem ? htmlSafe(loRow.Inventory().getBarCode()) : "");
            loMap.put("Description", lbHasItem ? htmlSafe(loRow.Inventory().getDescription()) : "");
            loMap.put("Qty", lbHasQty ? String.valueOf(loRow.getQuantity()) : "");
            loMap.put("Remarks", htmlSafe(loRow.getRemarks()));
            laGiveaways.add(loMap);
        }

        // the list components read these; the layout shows a section only when its flag is true
        parameters.put("HasModel", !laModels.isEmpty());
        parameters.put("HasGiveAway", !laGiveaways.isEmpty());
        parameters.put("HasBrand", false);
        parameters.put("HasModelException", false);
        parameters.put("ModelDataSource", new JRMapCollectionDataSource(laModels));
        parameters.put("GiveAwayDataSource", new JRMapCollectionDataSource(laGiveaways));
        parameters.put("BrandDataSource", new JRMapCollectionDataSource(new ArrayList<Map<String, ?>>()));
        parameters.put("ModelExceptionDataSource", new JRMapCollectionDataSource(new ArrayList<Map<String, ?>>()));

        // ---------------- compile and fill ----------------
        String lsJrxml = lsConfig + "/reports/SalesQoutationVersion.jrxml";
        JasperReport loReport = JasperCompileManager.compileReport(lsJrxml);

        // the report has no query: one empty record makes the detail bands print once,
        // and the data comes from the two list datasources above
        JasperPrint loPrint = JasperFillManager.fillReport(loReport, parameters, new JREmptyDataSource(1));
        return loPrint;
    }

    /** Customer address as one line: street, barangay, town, province. */
    private String getCustomerAddress() throws SQLException, GuanzonException {
        StringBuilder loAddress = new StringBuilder();
        appendPart(loAddress, poModel.ClientAddress().getAddress());
        appendPart(loAddress, poModel.ClientAddress().Barangay().getBarangayName());
        appendPart(loAddress, poModel.ClientAddress().Town().getDescription());
        appendPart(loAddress, poModel.ClientAddress().Town().Province().getDescription());
        return loAddress.toString();
    }

    private void appendPart(StringBuilder builder, String part) {
        if (part == null || part.trim().isEmpty()) return;
        if (builder.length() > 0) builder.append(", ");
        builder.append(part.trim());
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    private double nz(Number value) {
        return value == null ? 0.00 : value.doubleValue();
    }

    private String money(double value) {
        return String.format("%,.2f", value);
    }

    /** The report's list fields use markup="html", so escape characters that would be read as tags. */
    private String htmlSafe(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}