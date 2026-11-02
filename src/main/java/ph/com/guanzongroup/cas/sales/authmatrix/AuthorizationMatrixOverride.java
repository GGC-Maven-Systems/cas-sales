package ph.com.guanzongroup.cas.sales.authmatrix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.guanzon.appdriver.agent.MatrixAuthChecker;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class AuthorizationMatrixOverride {

    /**
     * A person who can approve a request.
     */
    public static class Recipient {

        public final String sEmployID;
        public final String sUserIDxx;
        public final int nAuthEqlx;        // approval weight of this person
        public String sName;               // display name (falls back to employee id)
        public boolean bExisting = false;  // a recipient row already exists for this request
        public boolean bApproved = false;  // and that recipient already approved

        public Recipient(String employID, String userID, int authEqlx) {
            this.sEmployID = employID;
            this.sUserIDxx = userID;
            this.nAuthEqlx = authEqlx;
            this.sName = employID;
        }
    }

    /**
     * Chooses who to request approval from. Return ONLY the newly chosen
     * recipients (not the ones with bExisting = true), an empty list if nobody
     * new is needed, or null if the user cancelled.
     *
     * adHoc = true means the matrix has NO approving officer configured for
     * this auth type, so the candidates are general officers and every chosen
     * officer alone satisfies the required level.
     */
    @FunctionalInterface
    public interface RecipientSelector {

        List<Recipient> select(String authType, String authDesc, int authRequired,
                int existingLevel, boolean adHoc, List<Recipient> candidates);
    }

    private static class DetailEntry {

        String psAuthType;
        String psAuthDesc = "";
        String psAreaCode;
        String psDeptIDxx;
        String pcAllowSys = "";
        int pnAuthLevl = 0;
        int pnAuthTotl = 0;
        String psRemarksx;

        DetailEntry(String authType, String areaCode, String deptID, String remarks) {
            this.psAuthType = authType;
            this.psAreaCode = areaCode;
            this.psDeptIDxx = deptID;
            this.psRemarksx = remarks;
        }
    }

    // ----------------------------------------------------------------- fields
    private final GRiderCAS poGRider;
    private final String psSourceCD;
    private final String psSourceNo;
    private final String psBranchCD;

    private final List<DetailEntry> paDetailEntry = new ArrayList<>();
    private RecipientSelector poSelector = null;     // null = JavaFX dialog

    private MatrixAuthChecker poChecker = null;
    private String psApprover = "";
    private boolean pbRequired = false;

    // ------------------------------------------------------------ constructors
    public AuthorizationMatrixOverride(GRiderCAS grider, String sourceCode, String sourceNo) {
        this(grider, sourceCode, sourceNo, grider.getBranchCode());
    }

    public AuthorizationMatrixOverride(GRiderCAS grider, String sourceCode, String sourceNo, String branchCode) {
        this.poGRider = grider;
        this.psSourceCD = sourceCode;
        this.psSourceNo = sourceNo;
        this.psBranchCD = branchCode;
    }

    /**
     * Replace the JavaFX dialog with your own selector (or autoAll() for the
     * old behavior).
     */
    public void setRecipientSelector(RecipientSelector selector) {
        this.poSelector = selector;
    }

    /**
     * Old MatrixAuthManager behavior: request everybody not yet requested, no
     * UI. (Not meaningful for adHoc, where the list is every officer.)
     */
    public static RecipientSelector autoAll() {
        return new RecipientSelector() {
            @Override
            public List<Recipient> select(String authType, String authDesc, int authRequired,
                    int existingLevel, boolean adHoc, List<Recipient> candidates) {
                List<Recipient> loList = new ArrayList<>();
                if (adHoc) {
                    return loList;
                }
                for (Recipient r : candidates) {
                    if (!r.bExisting) {
                        loList.add(r);
                    }
                }
                return loList;
            }
        };
    }

    // ------------------------------------------- MatrixAuthManager-style API
    public String getAuthType(String authgroup, String value, String indstry) throws SQLException {
        String lsSQL = "SELECT sAuthType FROM xxxSysAuth_Matrix_Master"
                + " WHERE sAuthGrpx = " + SQLUtil.toSQL(authgroup)
                + " AND " + SQLUtil.toSQL(value) + " BETWEEN nValueFrm AND IFNULL(nValueTox, " + value + ")"
                + " AND cRecdStat = '1'"
                + " AND sIndstCdx = " + SQLUtil.toSQL(indstry);
        System.out.println("getAuthType SQL : \n" + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        String lsRet = "";
        if (loRS.next()) {
            lsRet = loRS.getString("sAuthType");
        }
        MiscUtil.close(loRS);
        return lsRet;
    }

    public boolean hasAuthRequest() {
        return !paDetailEntry.isEmpty();
    }

    public void addAuthRequest(String authcode, String areacode, String deptid, String remarks) {
        for (DetailEntry loExisting : paDetailEntry) {
            if (loExisting.psAuthType.equalsIgnoreCase(authcode)) {
                return;
            }
        }
        paDetailEntry.add(new DetailEntry(authcode, areacode, deptid, remarks));
    }

    /**
     * Same result shape as MatrixAuthManager.processAuth(), but the recipients
     * are chosen by the user. When the matrix has no approving officer
     * configured for the auth type, the user is shown a searchable list of
     * officers to pick from instead of getting an error.
     *
     * The dialog is shown BEFORE any database transaction is opened.
     */
    public JSONObject processAuth() throws SQLException, GuanzonException {
        JSONObject loJson = new JSONObject();
        RecipientSelector loSelector = poSelector != null ? poSelector : dialogSelector();

        // ---- phase 1: load candidates and let the user choose (read only) ----
        List<List<Recipient>> laChosen = new ArrayList<>();

        for (DetailEntry loDetail : paDetailEntry) {

            // 1. the matrix definition itself (description, required level, system approval flag)
            String lsSQL = "SELECT sAuthDesc, nAuthLevl, cAllowSys FROM xxxSysAuth_Matrix_Master"
                    + " WHERE sAuthType = " + SQLUtil.toSQL(loDetail.psAuthType);
            System.out.println("Authorization Master SQL =\n" + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);

            if (!loRS.next()) {
                MiscUtil.close(loRS);
                throw new GuanzonException(5203, "Authorization type " + loDetail.psAuthType
                        + " is not defined in the authorization matrix.");
            }
            loDetail.psAuthDesc = loRS.getString("sAuthDesc");
            loDetail.pnAuthLevl = loRS.getInt("nAuthLevl");
            loDetail.pcAllowSys = loRS.getString("cAllowSys");
            MiscUtil.close(loRS);

            // 2. approvers configured in the matrix for this auth type / department / area
            lsSQL = "SELECT IFNULL(sEmployID, '') sEmployID, IFNULL(sUserIDxx, '') sUserIDxx, nAuthEqlx"
                    + " FROM xxxSysAuth_Matrix_Detail"
                    + " WHERE sAuthType = " + SQLUtil.toSQL(loDetail.psAuthType)
                    + " AND IFNULL(sDeptIDxx, '') = " + SQLUtil.toSQL(loDetail.psDeptIDxx)
                    + " AND IFNULL(sAreaCode, '') = " + SQLUtil.toSQL(loDetail.psAreaCode)
                    + " AND cRecdStat = '1'";
            System.out.println("Authorization Detail SQL =\n" + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);

            List<Recipient> loCandidates = new ArrayList<>();
            while (loRS.next()) {
                if (loRS.getString("sEmployID").isEmpty()) {
                    continue;   // a row without an employee cannot be tracked
                }
                Recipient r = new Recipient(
                        loRS.getString("sEmployID"),
                        loRS.getString("sUserIDxx"),
                        loRS.getInt("nAuthEqlx"));
                r.sName = lookupName(r.sEmployID);
                loCandidates.add(r);
            }
            MiscUtil.close(loRS);

            // 3. nobody configured: fall back to the encoding form (pick any officer)
            boolean lbAdHoc = loCandidates.isEmpty();
            if (lbAdHoc) {
                System.out.println("No approver configured in matrix for " + loDetail.psAuthType + ", showing officer list.");
                loCandidates = loadOfficers(loDetail.pnAuthLevl);
                if (loCandidates.isEmpty()) {
                    throw new GuanzonException(5203, "No approving officer is configured for "
                            + loDetail.psAuthType + " and no eligible officer was found.");
                }
            }

            // 4. who was already requested / already approved for this transaction
            Map<String, int[]> loExisting = loadExistingRecipients(loDetail.psAuthType);
            int lnExistingLevel = 0;
            int lnApprovedLevel = 0;
            for (int[] v : loExisting.values()) {
                lnExistingLevel += v[0];
                if (v[1] == 1) {
                    lnApprovedLevel += v[0];
                }
            }
            for (Recipient r : loCandidates) {
                int[] v = loExisting.get(r.sEmployID);
                if (v != null) {
                    r.bExisting = true;
                    r.bApproved = v[1] == 1;
                }
            }
            loDetail.pnAuthTotl = lnApprovedLevel;

            // 5. ask the user
            List<Recipient> loSelected = loSelector.select(loDetail.psAuthType, loDetail.psAuthDesc,
                    loDetail.pnAuthLevl, lnExistingLevel, lbAdHoc, loCandidates);

            if (loSelected == null) {
                loJson.put("result", "error");
                loJson.put("message", "Approval request cancelled.");
                return loJson;
            }

            int lnTotal = lnExistingLevel;
            for (Recipient r : loSelected) {
                if (!r.bExisting) {
                    lnTotal += r.nAuthEqlx;
                }
            }
            if (lnTotal < loDetail.pnAuthLevl) {
                loJson.put("result", "error");
                loJson.put("message", "Selected approvers cannot satisfy the required authority level ("
                        + loDetail.pnAuthLevl + ").");
                return loJson;
            }

            laChosen.add(loSelected);
        }

        // ---- phase 2: write the request ----
        boolean lbIsAutoCommit = poGRider.getGConnection().getConnection().getAutoCommit();
        boolean lbHasUpdate = false;
        if (lbIsAutoCommit) {
            poGRider.beginTrans("NEW", "Create Approval Request from Matrix Record", psSourceCD, psSourceNo);
        }

        try {
            for (int lnCtr = 0; lnCtr < paDetailEntry.size(); lnCtr++) {
                DetailEntry loDetail = paDetailEntry.get(lnCtr);

                for (Recipient r : laChosen.get(lnCtr)) {
                    if (r.bExisting) {
                        continue;
                    }

                    ResultSet loAuth = poGRider.executeQuery(authLookupSQL(loDetail.psAuthType, r.sEmployID));

                    if (!loAuth.next()) {
                        lbHasUpdate = true;
                        String lsTransNox = createAuthMaster();
                        lsTransNox = createAuthDetail(loDetail, lsTransNox);
                        createAuthRecipient(r, lsTransNox);
                    } else if (loAuth.getString("sDetlNoxx").isEmpty()) {
                        lbHasUpdate = true;
                        String lsTransNox = createAuthDetail(loDetail, loAuth.getString("sTransNox"));
                        createAuthRecipient(r, lsTransNox);
                    } else if (loAuth.getString("sRcptNoxx").isEmpty()) {
                        lbHasUpdate = true;
                        createAuthRecipient(r, loAuth.getString("sDetlNoxx"));
                    }
                    MiscUtil.close(loAuth);
                }
            }

            if (lbIsAutoCommit) {
                if (lbHasUpdate) {
                    poGRider.commitTrans();
                } else {
                    poGRider.rollbackTrans();
                }
            }
        } catch (SQLException | GuanzonException | RuntimeException ex) {
            if (lbIsAutoCommit) {
                poGRider.rollbackTrans();
            }
            throw ex;
        }

        JSONArray loArray = new JSONArray();
        for (DetailEntry loDetail : paDetailEntry) {
            JSONObject obj = new JSONObject();
            obj.put("sAuthType", loDetail.psAuthType);
            obj.put("sAuthDesc", loDetail.psAuthDesc);
            obj.put("sAreaCode", loDetail.psAreaCode);
            obj.put("sDeptIDxx", loDetail.psDeptIDxx);
            obj.put("nAuthLevl", Integer.valueOf(loDetail.pnAuthLevl));
            obj.put("nAuthTotl", Integer.valueOf(loDetail.pnAuthTotl));
            obj.put("sRemarksx", loDetail.psRemarksx);
            obj.put("cAllowSys", loDetail.pcAllowSys);
            loArray.add(obj);
        }
        loJson.put("result", "success");
        loJson.put("matrix", loArray);
        return loJson;
    }

    // ------------------------------------- high-level flow (confirm-type use)
    /**
     * True when a matrix row applied to the last check().
     */
    public boolean isRequired() {
        return pbRequired;
    }

    /**
     * User id of the current or on-the-spot approver, or "" if none was
     * captured.
     */
    public String getApprover() {
        return psApprover;
    }

    public JSONObject check(String authGroup, double value, String industry, String remarks)
            throws SQLException, GuanzonException {
        String lsValue = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
        return check(authGroup, lsValue, industry, remarks);
    }

    /**
     * 1. no matrix row covers the value -> success 2. matrix applies, no
     * request yet -> approver picker, request created, returns error with
     * requested = true 3. request exists -> current user / on-the-spot approver
     * approves, success only when all required levels are approved
     */
    public JSONObject check(String authGroup, String value, String industry, String remarks)
            throws SQLException, GuanzonException {
        poChecker = null;
        psApprover = "";
        pbRequired = false;

        String lsAuthType = getAuthType(authGroup, value, industry == null ? "" : industry);
        if (lsAuthType.isEmpty()) {
            return result("success", "No matrix approval required.");
        }
        pbRequired = true;

        MatrixAuthChecker loCheck = new MatrixAuthChecker(poGRider, psSourceCD, psSourceNo, psBranchCD);
        JSONObject loJSON = loCheck.loadAuth();

        // no request yet: choose approvers and create it
        if (!"success".equals((String) loJSON.get("result"))) {
            paDetailEntry.clear();
            addAuthRequest(lsAuthType, "", "", remarks == null ? "" : remarks);

            loJSON = processAuth();
            if (!"success".equals((String) loJSON.get("result"))) {
                return loJSON;
            }
            loJSON = result("error", "Approval request created. Once the selected approver(s) approve it, try again.");
            loJSON.put("requested", Boolean.TRUE);
            return loJSON;
        }

        // request exists: collect approval
        if (!loCheck.isAuthOkay()) {
            if (!loCheck.isAllowSys()) {
                loJSON = loCheck.authTrans(lsAuthType, poGRider.getUserID());

                if (!"success".equals((String) loJSON.get("result"))) {
                    loJSON = ShowDialogFX.getUserApproval(poGRider);
                    if ("error".equals((String) loJSON.get("result"))) {
                        return loJSON;
                    }
                    String lsUserIDxx = loJSON.get("sUserIDxx").toString();

                    loJSON = loCheck.authTrans(lsAuthType, lsUserIDxx);
                    if (!"success".equals((String) loJSON.get("result"))) {
                        return loJSON;
                    }
                    psApprover = lsUserIDxx;
                } else {
                    psApprover = poGRider.getUserID();
                }
            }

            if (!loCheck.isAuthOkay()) {
                return result("error", "Transaction is not yet fully approved in the matrix.");
            }
        }

        poChecker = loCheck;
        return result("success", "Matrix approval completed.");
    }

    /**
     * Marks the approved request as used. Call inside the caller's own
     * transaction, after the status change and before commit. No-op when
     * nothing was approved.
     */
    public JSONObject postAuth() throws SQLException, GuanzonException {
        if (poChecker == null) {
            return result("success", "Nothing to post.");
        }
        return poChecker.postAuth();
    }

    // ---------------------------------------------------------------- helpers
    private String authLookupSQL(String authType, String employID) {
        return "SELECT a.sTransNox, IFNULL(b.sTransNox, '') sDetlNoxx, IFNULL(c.sTransNox, '') sRcptNoxx"
                + ", b.nAuthRqrd, b.nAuthTotl, b.cAllowSys, c.sEmployID, c.sUserIDxx, c.nAuthLevl"
                + ", c.cSentStat, c.cTranStat"
                + " FROM Transaction_Authorization_Master a"
                + " LEFT JOIN Transaction_Authorization_Detail b ON a.sTransNox = b.sSourceNo"
                + " AND b.sAuthType = " + SQLUtil.toSQL(authType)
                + " LEFT JOIN Transaction_Authorization_Recipient c ON b.sTransNox = c.sSourceNo"
                + " AND c.sEmployID = " + SQLUtil.toSQL(employID)
                + " WHERE a.sSourceCD = " + SQLUtil.toSQL(psSourceCD)
                + " AND a.sSourceNo = " + SQLUtil.toSQL(psSourceNo);
    }

    /**
     * employID -> {level, approved(1/0)} for recipients already created for
     * this transaction and auth type.
     */
    private Map<String, int[]> loadExistingRecipients(String authType) throws SQLException {
        Map<String, int[]> loMap = new HashMap<>();
        String lsSQL = "SELECT c.sEmployID, c.nAuthLevl, c.cTranStat"
                + " FROM Transaction_Authorization_Master a"
                + " INNER JOIN Transaction_Authorization_Detail b ON a.sTransNox = b.sSourceNo"
                + " AND b.sAuthType = " + SQLUtil.toSQL(authType)
                + " INNER JOIN Transaction_Authorization_Recipient c ON b.sTransNox = c.sSourceNo"
                + " WHERE a.sSourceCD = " + SQLUtil.toSQL(psSourceCD)
                + " AND a.sSourceNo = " + SQLUtil.toSQL(psSourceNo);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        while (loRS.next()) {
            loMap.put(loRS.getString("sEmployID"),
                    new int[]{loRS.getInt("nAuthLevl"), "1".equals(loRS.getString("cTranStat")) ? 1 : 0});
        }
        MiscUtil.close(loRS);
        return loMap;
    }

    /**
     * Officers offered when the matrix has no approver for the auth type. Each
     * one gets the full required level as weight, so one approval is enough.
     *
     * ADJUST THIS QUERY to your user table: it assumes xxxSysUser has
     * sUserIDxx, sEmployNo (= Client_Master.sClientID) and nUserLevl, and it
     * lists users above the encoder level, excluding the current user.
     */
    private List<Recipient> loadOfficers(int weight) throws SQLException {
        String lsSQL = "SELECT a.sUserIDxx, a.sEmployNo, IFNULL(b.sCompnyNm, '') sCompnyNm"
                + " FROM xxxSysUser a"
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo"
                + " WHERE IFNULL(a.sEmployNo, '') <> ''"
                + " AND a.nUserLevl > " + UserRight.ENCODER
                + " AND a.sUserIDxx <> " + SQLUtil.toSQL(poGRider.getUserID())
                + " ORDER BY b.sCompnyNm";
        System.out.println("Officer list SQL =\n" + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        List<Recipient> loList = new ArrayList<>();
        while (loRS.next()) {
            Recipient r = new Recipient(loRS.getString("sEmployNo"), loRS.getString("sUserIDxx"), weight);
            r.sName = loRS.getString("sCompnyNm").isEmpty() ? loRS.getString("sUserIDxx") : loRS.getString("sCompnyNm");
            loList.add(r);
        }
        MiscUtil.close(loRS);
        return loList;
    }

    /**
     * Assumes the matrix sEmployID is the Client_Master.sClientID. Falls back
     * to the id.
     */
    private String lookupName(String employID) {
        String lsName = employID;
        try {
            ResultSet loRS = poGRider.executeQuery(
                    "SELECT IFNULL(sCompnyNm, '') sCompnyNm FROM Client_Master WHERE sClientID = "
                    + SQLUtil.toSQL(employID));
            if (loRS.next() && !loRS.getString("sCompnyNm").isEmpty()) {
                lsName = loRS.getString("sCompnyNm");
            }
            MiscUtil.close(loRS);
        } catch (SQLException ex) {
            // keep the id
        }
        return lsName;
    }

    private String createAuthMaster() throws SQLException, GuanzonException {
        String lsTransNox = MiscUtil.getNextCode("Transaction_Authorization_Master", "sTransNox", true,
                poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
        String lsSQL = "INSERT INTO Transaction_Authorization_Master SET"
                + " sTransNox = " + SQLUtil.toSQL(lsTransNox)
                + ", dTransact = " + SQLUtil.toSQL(poGRider.getServerDate())
                + ", sSourceCD = " + SQLUtil.toSQL(psSourceCD)
                + ", sSourceNo = " + SQLUtil.toSQL(psSourceNo)
                + ", cDisplayx = " + SQLUtil.toSQL("0")
                + ", cTranStat = " + SQLUtil.toSQL("0")
                + ", sModified = " + SQLUtil.toSQL(poGRider.getUserID())
                + ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());
        poGRider.executeQuery(lsSQL, "Transaction_Authorization_Master", psBranchCD, "", "");
        return lsTransNox;
    }

    private String createAuthDetail(DetailEntry loDetail, String fsSourceNo) throws SQLException, GuanzonException {
        String lsTransNox = MiscUtil.getNextCode("Transaction_Authorization_Detail", "sTransNox", true,
                poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
        String lsSQL = "INSERT INTO Transaction_Authorization_Detail SET"
                + " sTransNox = " + SQLUtil.toSQL(lsTransNox)
                + ", sSourceNo = " + SQLUtil.toSQL(fsSourceNo)
                + ", sAuthType = " + SQLUtil.toSQL(loDetail.psAuthType)
                + ", sSysPayLx = " + SQLUtil.toSQL("")
                + ", sRqstPayL = " + SQLUtil.toSQL("")
                + ", nAuthRqrd = " + SQLUtil.toSQL(loDetail.pnAuthLevl)
                + ", nAuthTotl = " + SQLUtil.toSQL(0)
                + ", sRemarksx = " + SQLUtil.toSQL(loDetail.psRemarksx)
                + ", cAllowSys = " + SQLUtil.toSQL(loDetail.pcAllowSys)
                + ", cTranStat = " + SQLUtil.toSQL("0")
                + ", sModified = " + SQLUtil.toSQL(poGRider.getUserID())
                + ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());
        poGRider.executeQuery(lsSQL, "Transaction_Authorization_Detail", psBranchCD, "", "");
        return lsTransNox;
    }

    private void createAuthRecipient(Recipient r, String fsSourceNo) throws SQLException, GuanzonException {
        String lsTransNox = MiscUtil.getNextCode("Transaction_Authorization_Recipient", "sTransNox", true,
                poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
        String lsSQL = "INSERT INTO Transaction_Authorization_Recipient SET"
                + " sTransNox = " + SQLUtil.toSQL(lsTransNox)
                + ", sSourceNo = " + SQLUtil.toSQL(fsSourceNo)
                + ", sEmployID = " + SQLUtil.toSQL(r.sEmployID)
                + ", sUserIDxx = " + SQLUtil.toSQL(r.sUserIDxx)
                + ", nAuthLevl = " + SQLUtil.toSQL(r.nAuthEqlx)
                + ", sRemarksx = " + SQLUtil.toSQL("")
                + ", cSentStat = " + SQLUtil.toSQL("0")
                + ", cTranStat = " + SQLUtil.toSQL("0")
                + ", sModified = " + SQLUtil.toSQL(poGRider.getUserID())
                + ", dModified = " + SQLUtil.toSQL(poGRider.getServerDate());
        poGRider.executeQuery(lsSQL, "Transaction_Authorization_Recipient", psBranchCD, "", "");
    }

    private static JSONObject result(String result, String message) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", result);
        loJSON.put("message", message);
        return loJSON;
    }

    // ------------------------------------------------------------- JavaFX UI
    private static RecipientSelector dialogSelector() {
        return new RecipientSelector() {
            @Override
            public List<Recipient> select(String authType, String authDesc, int authRequired,
                    int existingLevel, boolean adHoc, List<Recipient> candidates) {
                return showDialog(authDesc, authRequired, existingLevel, adHoc, candidates);
            }
        };
    }

    /**
     * Safe from any thread: off the FX thread it runs via Platform.runLater and
     * waits.
     */
    private static List<Recipient> showDialog(final String authDesc, final int required,
            final int existingLevel, final boolean adHoc, final List<Recipient> candidates) {
        if (Platform.isFxApplicationThread()) {
            return buildDialog(authDesc, required, existingLevel, adHoc, candidates);
        }

        FutureTask<List<Recipient>> task = new FutureTask<>(
                () -> buildDialog(authDesc, required, existingLevel, adHoc, candidates));
        Platform.runLater(task);
        try {
            return task.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private static List<Recipient> buildDialog(String authDesc, final int required,
            final int existingLevel, final boolean adHoc, List<Recipient> candidates) {
        final Dialog<List<Recipient>> dlg = new Dialog<>();
        dlg.setTitle("Request Approval");
        dlg.setHeaderText(authDesc + "\nRequired authority level: " + required
                + (adHoc
                        ? "\nNo approving officer is set up in the matrix for this.\nSelect the officer(s) to request an approval code from."
                        : "\nSelect the employee(s) to request an approval code from."));

        final ButtonType okType = new ButtonType("Send Request", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        final Node okButton = dlg.getDialogPane().lookupButton(okType);
        final Label lblTotal = new Label();
        final Map<CheckBox, Recipient> map = new LinkedHashMap<>();

        final Runnable refresh = () -> {
            int total = existingLevel;
            int selected = 0;
            for (Map.Entry<CheckBox, Recipient> e : map.entrySet()) {
                if (!e.getValue().bExisting && e.getKey().isSelected()) {
                    total += e.getValue().nAuthEqlx;
                    selected++;
                }
            }
            lblTotal.setText(adHoc
                    ? "Selected officer(s): " + selected
                    : "Requested level: " + total + " / " + required);
            okButton.setDisable(total < required);
        };

        VBox list = new VBox(6);
        for (Recipient r : candidates) {
            String lsText = r.sName + (adHoc ? "" : " (level " + r.nAuthEqlx + ")")
                    + (r.bApproved ? " - approved" : r.bExisting ? " - already requested" : "");
            CheckBox cb = new CheckBox(lsText);
            if (r.bExisting) {
                cb.setSelected(true);
                cb.setDisable(true);
            } else {
                cb.selectedProperty().addListener((obs, oldVal, newVal) -> refresh.run());
            }
            map.put(cb, r);
            list.getChildren().add(cb);
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(Math.min(300, 30 * Math.max(candidates.size(), 3)));

        VBox content = new VBox(10);

        // search box for long lists (always for the officer list)
        if (adHoc || candidates.size() > 8) {
            final TextField txtFilter = new TextField();
            txtFilter.setPromptText("Search name");
            txtFilter.textProperty().addListener((obs, oldVal, newVal) -> {
                String lsFilter = newVal == null ? "" : newVal.trim().toLowerCase();
                for (Map.Entry<CheckBox, Recipient> e : map.entrySet()) {
                    boolean lbShow = lsFilter.isEmpty()
                            || e.getValue().sName.toLowerCase().contains(lsFilter);
                    e.getKey().setVisible(lbShow);
                    e.getKey().setManaged(lbShow);
                }
            });
            content.getChildren().add(txtFilter);
        }

        content.getChildren().addAll(scroll, lblTotal);
        dlg.getDialogPane().setContent(content);
        refresh.run();

        dlg.setResultConverter(bt -> {
            if (bt != okType) {
                return null;                       // cancelled
            }
            List<Recipient> selected = new ArrayList<>();
            for (Map.Entry<CheckBox, Recipient> e : map.entrySet()) {
                if (!e.getValue().bExisting && e.getKey().isSelected()) {
                    selected.add(e.getValue());
                }
            }
            return selected;
        });

        return dlg.showAndWait().orElse(null);
    }
}
