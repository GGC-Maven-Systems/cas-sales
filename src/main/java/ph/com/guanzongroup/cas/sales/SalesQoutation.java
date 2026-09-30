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
import ph.com.guanzongroup.cas.sales.queries.SalesQoutationsMasterQueries;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.services.SalesModels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            if (!"success".equals((String) poJSON.get("result"))) return poJSON;

            String lsVersionNo = oSalesQoutationVersion.Master().getTransactionNo();

            // giveaways: numbered and keyed by the version's number
            poJSON = oSalesQoutationVersionGiveaways.saveGiveaways(lsVersionNo);
            if (!"success".equals((String) poJSON.get("result"))) return poJSON;

            // follow-up: only if one is being added or edited
            if (isPending(oSalesQoutationFollowUp)) {
                keyFollowUp(lsQuotationNo, lsVersionNo);
                poJSON = oSalesQoutationFollowUp.saveRecord();
                if (!"success".equals((String) poJSON.get("result"))) return poJSON;
            }

            poJSON = new JSONObject();
            poJSON.put("result", "success");
            return poJSON;
        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
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
        String lsSQL = "SELECT "
                + "  a.sTransNox, "
                + "  a.dTransact, "
                + "  a.sClientID, "
                + "  a.cTranStat, "
                + "  IFNULL(b.sCompnyNm, '') sCompnyNm "
                + " FROM Sales_Quotation_Master a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sClientID ";

        System.out.println("SEARCH RECORD : " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Transaction No.»Date»Client",
                "sTransNox»dTransact»sCompnyNm",
                "a.sTransNox»a.dTransact»IFNULL(b.sCompnyNm, '')",
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

    private JSONObject setError(String message) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", "error");
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

    public JSONObject SearchMcItem(String value, int MCITemRow, int byCode) throws SQLException, GuanzonException {
        if (MCITemRow < 0 || MCITemRow >= oSalesQoutationVersion.getDetailCount()) {
            return setError("Select an item row first.");
        }

        String lsSQL = SalesQoutationsMasterQueries.SQL_MCItem();
        System.out.println("Executing SQL: " + lsSQL);

        JSONObject loBrowse = ShowDialogFX.Browse(poGRider,
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



}