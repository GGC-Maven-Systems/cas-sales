import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.sales.SalesQoutation;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNewRecordQoutation {

    static GRiderCAS poApp;
    static SalesQoutation poQuotation;

    // filled by test2, used by test3
    static String psQuotationNo;
    static String psVersionNo;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty(
                "sys.default.path.metadata",
                "D:/GGC_Maven_Systems/config/metadata/new/"
        );

        poApp = MiscUtil.Connect();

        System.out.println("User level : " + poApp.getUserLevel());

        // The factory already calls initialize()
        poQuotation = new SalesControllers(poApp, null).SalesQoutation();

        Assert.assertNotNull("SalesQoutation factory returned null", poQuotation);

        poQuotation.setWithUI(false);
    }

    @After
    public void tearDown() {
        // A failed save must not leave a transaction open for the next test.
        try {
            poApp.rollbackTrans();
        } catch (Exception ignored) {
        }
    }

    /** Step 1: newRecord() alone. No save. */
    @Test
    public void test1NewRecord() throws SQLException, GuanzonException {
        assertSuccess(poQuotation.newRecord());
        Assert.assertEquals("new quotation starts at version 1",
                Integer.valueOf(1), poQuotation.getModel().getVersion());
        Assert.assertEquals("no giveaway row until one is added",
                0, poQuotation.Giveaways().getGiveawayCount());
    }

    /** Step 2: fill every table, save, then verify each table. */
    @Test
    public void test2SaveRecord() throws Exception {

        String stockId = "C0W725000011";

        assertSuccess(poQuotation.newRecord());

        // Sales_Quotation_Master
        poQuotation.getModel().setClientId("M00125000002");
        poQuotation.getModel().setIndustryCode("01");
        poQuotation.getModel().setCategoryCode("0005");

        // Sales_Quotation_Version_Master (parent id is set again by saveOthers())
        poQuotation.Version().Master().setBranchCode(poApp.getBranchCode());
        System.out.println("VERSTION trans no + " + poQuotation.Version().Master().getTransactionNo());
        // Sales_Quotation_Version_Detail
        poQuotation.Version().Detail(0).setStockId(stockId);
        poQuotation.Version().Detail(0).setQuantity(1);
        poQuotation.Version().Detail(0).setUnitPrice(10000.00);

        // Sales_Quotation_Version_Giveaways (a list: add a row first)
        assertSuccess(poQuotation.Giveaways().addGiveaway());
        poQuotation.Giveaways().Giveaway(0).setStockId(stockId);
        poQuotation.Giveaways().Giveaway(0).setQuantity(1);

        displayRecordsToSave();

        assertSuccess(poQuotation.saveRecord());

        // generated numbers
        psQuotationNo = poQuotation.getModel().getTransactionNo();
        psVersionNo = poQuotation.Version().Master().getTransactionNo();

        System.out.println();
        System.out.println("==================================================");
        System.out.println("GENERATED TRANSACTION NUMBERS");
        System.out.println("==================================================");
        System.out.println("Quotation No : " + psQuotationNo);
        System.out.println("Version No   : " + psVersionNo);
        System.out.println("==================================================");
        System.out.println();

        Assert.assertNotNull("Quotation No.", psQuotationNo);
        Assert.assertNotNull("Version No.", psVersionNo);

        Assert.assertEquals("Sales_Quotation_Master", 1,
                count("Sales_Quotation_Master",
                        "sTransNox = '" + psQuotationNo + "' AND nVersionx = 1"));

        Assert.assertEquals("Sales_Quotation_Version_Master", 1,
                count("Sales_Quotation_Version_Master",
                        "sTransNox = '" + psVersionNo + "' AND sParentID = '" + psQuotationNo + "'"));

        Assert.assertEquals("Sales_Quotation_Version_Detail", 1,
                count("Sales_Quotation_Version_Detail",
                        "sTransNox = '" + psVersionNo + "' AND nEntryNox = 1"));

        Assert.assertEquals("Sales_Quotation_Version_Giveaways", 1,
                count("Sales_Quotation_Version_Giveaways",
                        "sTransNox = '" + psVersionNo + "' AND nEntryNox = 1"));
    }

    /** Step 3: reopen what test2 saved (latest version and by version no.), then update mode. */
    @Test
    public void test3OpenAndUpdate() throws Exception {
        Assert.assertNotNull("test2 must run first", psQuotationNo);

        // parent row of the tree: latest version
        assertSuccess(poQuotation.openRecord(psQuotationNo));
        Assert.assertEquals(psQuotationNo, poQuotation.getModel().getTransactionNo());
        Assert.assertEquals(psVersionNo, poQuotation.Version().Master().getTransactionNo());
        Assert.assertEquals(1, poQuotation.Version().getDetailCount());
        Assert.assertEquals(1, poQuotation.Giveaways().getGiveawayCount());
        Assert.assertTrue(poQuotation.isLatestVersion());

        // version row of the tree: a specific version
        assertSuccess(poQuotation.openRecord(psQuotationNo, psVersionNo));
        Assert.assertEquals(psVersionNo, poQuotation.Version().Master().getTransactionNo());

        // a version that is not this quotation's must be rejected
        JSONObject loBad = poQuotation.openRecord(psQuotationNo, "XXXXXXXXXXXX");
        Assert.assertEquals("error", loBad.get("result"));

        // update mode (no save)
        assertSuccess(poQuotation.openRecord(psQuotationNo, psVersionNo));
        assertSuccess(poQuotation.updateRecord());
    }

    /**
     * Step 4: follow-ups are a history per version. Each save adds a record
     * with the next entry no.; nothing else about the quotation is touched.
     */
    @Test
    public void test4FollowUpHistory() throws Exception {
        Assert.assertNotNull("test2 must run first", psQuotationNo);

        assertSuccess(poQuotation.openRecord(psQuotationNo));
        String lsVersionNo = poQuotation.Version().Master().getTransactionNo();
        String lsWhere = "sReferNox = '" + lsVersionNo + "'";

        int lnBefore = count("Sales_Quotation_FollowUp", lsWhere);
        Assert.assertEquals("history matches table before", lnBefore, poQuotation.getFollowUps().size());

        // first follow-up
        assertSuccess(poQuotation.newFollowUp());
        Assert.assertEquals("follow-up by defaults to the current user",
                poApp.getUserID(), poQuotation.FollowUp().getModel().getFollowUpBy());
        fillFollowUp("test follow-up 1");
        assertSuccess(poQuotation.saveFollowUp());
        Assert.assertEquals("after 1st follow-up", lnBefore + 1, count("Sales_Quotation_FollowUp", lsWhere));

        // second follow-up: same version, next entry no.
        assertSuccess(poQuotation.newFollowUp());
        fillFollowUp("test follow-up 2");
        assertSuccess(poQuotation.saveFollowUp());
        Assert.assertEquals("after 2nd follow-up", lnBefore + 2, count("Sales_Quotation_FollowUp", lsWhere));

        // history, newest first, entry numbers keep counting up
        List<JSONObject> laFollowUps = poQuotation.getFollowUps();
        Assert.assertEquals(lnBefore + 2, laFollowUps.size());
        int lnNewest = Integer.parseInt((String) laFollowUps.get(0).get("nEntryNox"));
        int lnPrevious = Integer.parseInt((String) laFollowUps.get(1).get("nEntryNox"));
        Assert.assertEquals("entry no. is previous + 1", lnPrevious + 1, lnNewest);
        Assert.assertEquals("test follow-up 2", laFollowUps.get(0).get("sRemarksx"));

        for (JSONObject loFollowUp : laFollowUps) {
            System.out.println("follow-up " + loFollowUp.get("nEntryNox") + " : "
                    + loFollowUp.get("dFollowUp") + " -> " + loFollowUp.get("dNextFlup")
                    + " / " + loFollowUp.get("sRemarksx"));
        }

        // open one for viewing
        assertSuccess(poQuotation.openFollowUp(lnNewest));
        Assert.assertEquals(psQuotationNo, poQuotation.FollowUp().getModel().getTransactionNo());
        Assert.assertEquals(lsVersionNo, poQuotation.FollowUp().getModel().getReferenceNo());
        Assert.assertEquals(Integer.valueOf(lnNewest), poQuotation.FollowUp().getModel().getEntryNo());
        Assert.assertEquals("test follow-up 2", poQuotation.FollowUp().getModel().getRemarks());
    }

    /**
     * Debug: why does a follow-up save report success but write no row?
     * Part A goes through saveFollowUp(); part B saves the model directly
     * inside its own transaction to tell the controller apart from the model.
     */
    @Test
    public void test4aFollowUpDebug() throws Exception {
        Assert.assertNotNull("test2 must run first", psQuotationNo);
        assertSuccess(poQuotation.openRecord(psQuotationNo));

        String lsVersionNo = poQuotation.Version().Master().getTransactionNo();
        String lsTable = poQuotation.FollowUp().getModel().getTable();
        System.out.println("follow-up table : " + lsTable);
        System.out.println("EditMode        : ADDNEW=" + EditMode.ADDNEW + " READY=" + EditMode.READY
                + " UPDATE=" + EditMode.UPDATE);

        // ---- Part A: controller path
        assertSuccess(poQuotation.newFollowUp());
        fillFollowUp("debug A");
        System.out.println("A mode after new  : " + poQuotation.FollowUp().getEditMode());
        System.out.println("A key before save : " + poQuotation.FollowUp().getModel().getTransactionNo()
                + " / " + poQuotation.FollowUp().getModel().getEntryNo());

        JSONObject loA = poQuotation.saveFollowUp();
        System.out.println("A save result     : " + loA);
        System.out.println("A key after save  : " + poQuotation.FollowUp().getModel().getTransactionNo()
                + " / " + poQuotation.FollowUp().getModel().getEntryNo());
        System.out.println("A mode after save : " + poQuotation.FollowUp().getEditMode());
        System.out.println("A rows for version: " + count(lsTable, "sReferNox = '" + lsVersionNo + "'"));

        // ---- Part B: model only, own transaction, keys set by hand
        assertSuccess(poQuotation.newFollowUp());
        fillFollowUp("debug B");
        poQuotation.FollowUp().getModel().setTransactionNo(psQuotationNo);
        poQuotation.FollowUp().getModel().setReferenceNo(lsVersionNo);
        poQuotation.FollowUp().getModel().setEntryNo(poQuotation.FollowUp().getNextEntryNo(psQuotationNo));
        poQuotation.FollowUp().getModel().setModifiedDate(poApp.getServerDate());
        System.out.println("B mode            : " + poQuotation.FollowUp().getEditMode());
        System.out.println("B key             : " + poQuotation.FollowUp().getModel().getTransactionNo()
                + " / " + poQuotation.FollowUp().getModel().getEntryNo());

        poApp.beginTrans("ADD NEW", lsTable, "PARM", lsVersionNo);
        JSONObject loB = poQuotation.FollowUp().getModel().saveRecord();
        System.out.println("B model save      : " + loB);
        poApp.commitTrans();
        System.out.println("B rows for version: " + count(lsTable, "sReferNox = '" + lsVersionNo + "'"));

        ResultSet rs = poApp.executeQuery("SELECT sTransNox, nEntryNox, cFllwUpTp, dTimeStmp FROM "
                + lsTable + " ORDER BY dTimeStmp DESC LIMIT 5");
        while (rs.next()) {
            System.out.println("row : " + rs.getString(1) + " / " + rs.getInt(2) + " / "
                    + rs.getString(3) + " / " + rs.getString(4));
        }
        MiscUtil.close(rs);
    }

    /** Step 5: a follow-up without a type is rejected and nothing is written. */
    @Test
    public void test5FollowUpValidation() throws Exception {
        Assert.assertNotNull("test2 must run first", psQuotationNo);

        assertSuccess(poQuotation.openRecord(psQuotationNo));
        String lsWhere = "sReferNox = '" + poQuotation.Version().Master().getTransactionNo() + "'";
        int lnBefore = count("Sales_Quotation_FollowUp", lsWhere);

        assertSuccess(poQuotation.newFollowUp());
        poQuotation.FollowUp().getModel().setRemarks("no type on purpose");
        // follow-up type left empty

        JSONObject loJSON = poQuotation.saveFollowUp();
        Assert.assertEquals("error", loJSON.get("result"));
        System.out.println("expected error : " + loJSON.get("message"));

        Assert.assertEquals("nothing written", lnBefore, count("Sales_Quotation_FollowUp", lsWhere));

        // nothing pending -> nothing to save
        assertSuccess(poQuotation.openRecord(psQuotationNo));
        // a controller that exists but was never started must not "save" either
        poQuotation.getFollowUps();
        Assert.assertEquals("error", poQuotation.saveFollowUp().get("result"));
    }

    /**
     * Step 6: a pending follow-up is saved together with the quotation when
     * the quotation is updated (saveOthers). This re-saves the version detail
     * and giveaways too, so it needs the detail rows to be saved by test2.
     */
    @Test
    public void test6FollowUpWithQuotationUpdate() throws Exception {
        Assert.assertNotNull("test2 must run first", psQuotationNo);

        assertSuccess(poQuotation.openRecord(psQuotationNo));
        String lsVersionNo = poQuotation.Version().Master().getTransactionNo();
        String lsWhere = "sReferNox = '" + lsVersionNo + "'";
        int lnBefore = count("Sales_Quotation_FollowUp", lsWhere);

        assertSuccess(poQuotation.updateRecord());
        assertSuccess(poQuotation.newFollowUp());
        fillFollowUp("saved with the quotation");

        assertSuccess(poQuotation.saveRecord());

        Assert.assertEquals("follow-up added by saveRecord()", lnBefore + 1, count("Sales_Quotation_FollowUp", lsWhere));
        Assert.assertEquals("still one version", 1,
                count("Sales_Quotation_Version_Master", "sParentID = '" + psQuotationNo + "'"));
    }

    /** Fills the follow-up currently started with newFollowUp(). */
    private static void fillFollowUp(String remarks) throws Exception {
        Date ldToday = poApp.getServerDate();
        Date ldNext = new Date(ldToday.getTime() + 7L * 24 * 60 * 60 * 1000);

        poQuotation.FollowUp().getModel().setFollowUpDate(ldToday);
        poQuotation.FollowUp().getModel().setNextFollowUpDate(ldNext);
        poQuotation.FollowUp().getModel().setFollowUpType("1");
        poQuotation.FollowUp().getModel().setRemarks(remarks);
    }

    private static void displayRecordsToSave() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("           RECORDS TO BE SAVED");
        System.out.println("==================================================");

        System.out.println("[Sales_Quotation_Master]");
        System.out.println("Transaction No : " + poQuotation.getModel().getTransactionNo());
        System.out.println("Client ID      : " + poQuotation.getModel().getClientId());
        System.out.println("Industry Code  : " + poQuotation.getModel().getIndustryCode());
        System.out.println("Category Code  : " + poQuotation.getModel().getCategoryCode());
        System.out.println("Version        : " + poQuotation.getModel().getVersion());

        System.out.println("[Sales_Quotation_Version_Master]");
        System.out.println("Transaction No : " + poQuotation.Version().Master().getTransactionNo());
        System.out.println("Branch Code    : " + poQuotation.Version().Master().getBranchCode());

        System.out.println("[Sales_Quotation_Version_Detail]");
        System.out.println("Stock ID       : " + poQuotation.Version().Detail(0).getStockId());
        System.out.println("Quantity       : " + poQuotation.Version().Detail(0).getQuantity());
        System.out.println("Unit Price     : " + poQuotation.Version().Detail(0).getUnitPrice());

        System.out.println("[Sales_Quotation_Version_Giveaways]");
        for (int lnCtr = 0; lnCtr < poQuotation.Giveaways().getGiveawayCount(); lnCtr++) {
            System.out.println("Row " + (lnCtr + 1) + " Stock ID : " + poQuotation.Giveaways().Giveaway(lnCtr).getStockId()
                    + " / Quantity : " + poQuotation.Giveaways().Giveaway(lnCtr).getQuantity());
        }
        System.out.println("==================================================");
    }

    private static int count(String table, String where) throws Exception {
        ResultSet rs = poApp.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + where);
        rs.next();
        int lnCount = rs.getInt(1);
        MiscUtil.close(rs);
        return lnCount;
    }

    private static void assertSuccess(JSONObject foJSON) {
        if (!"success".equals((String) foJSON.get("result"))) {
            String lsMessage = (String) foJSON.get("message");
            System.err.println(lsMessage);
            Assert.fail(lsMessage);
        }
    }
}