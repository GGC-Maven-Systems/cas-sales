import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.h2.tools.RunScript;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;

import ph.com.guanzongroup.cas.sales.SalesReservation;
import ph.com.guanzongroup.cas.sales.constant.Sales_Reservation_Static;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;

/**
 * Integration tests for {@link SalesReservation}.
 *
 * These tests run against a real, seeded H2 database (via GRiderCAS) rather
 * than mocks, matching the style of the original SalesReservationTest scaffold.
 *
 * IMPORTANT - PLACEHOLDER VALUES:
 * Several constants below (branch code, industry/company/category codes,
 * client id, brand/model/stock ids, and pre-seeded transaction numbers for
 * each status) are placeholders. They must be replaced with values that
 * actually exist in your test-data/*.sql fixtures, or the corresponding
 * tests will fail for data reasons rather than logic reasons. Search
 * points are marked with "// TODO(fixture)".
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SalesReservationTest {

    private static GRiderCAS instance;
    private static SalesControllers poTrans;
    private static Connection conn;

    // TODO(fixture): replace with real values seeded in test-data/*.sql
    private static final String BRANCH_CODE = "GK01";
    private static final String INDUSTRY_ID = "01";
    private static final String COMPANY_ID = "0002";
    private static final String CATEGORY_CD = "0000007";
    private static final String CLIENT_ID = "GK0126000041";
    private static final String ADDRESS_ID = "GK0126000045";
    private static final String MOBILE_ID = "GK0126000023";
    private static final String BRAND_ID = "M001003";
    private static final String STOCK_ID = "W00525000860";

    // TODO(fixture): pre-seeded Sales_Reservation_Master transaction numbers
    // whose cTranStat already equals the given status. Used for the
    // "already <status>" guard-clause tests in each status-change method.
    private static final String TRANSNO_ALREADY_CONFIRMED = "GK0126000003";
    private static final String TRANSNO_ALREADY_CANCELLED = "GK0126000003";
    private static final String TRANSNO_ALREADY_VOID = "GK0126000003";
    private static final String TRANSNO_ALREADY_PAID = "GK0126000003";

    // TODO(fixture): pre-seeded Sales_Reservation_Master transaction numbers
    // that are still cTranStat = OPEN, one per status-change test below, so
    // that ConfirmTransaction/CancelTransaction/VoidTransaction/PaidTransaction
    // can each run against a fresh row without clobbering each other's state.
    private static final String TRANSNO_FOR_CONFIRM = "GK0126000003";
    private static final String TRANSNO_FOR_CANCEL = "GK0126000003";
    private static final String TRANSNO_FOR_VOID = "GK0126000003";
    private static final String TRANSNO_FOR_PAID = "GK0126000003";

    // TODO(fixture): a source code/no pair for an already-CONFIRMED Sales
    // Reservation row, used to exercise checkExistingTrans()'s error path
    // (mirrors SalesReservation_EntryCarController / ConfirmationCarController
    // calling checkExistingTrans() right before ConfirmTransaction()).
    private static final String EXISTING_SOURCE_CODE = "srsv";
    private static final String EXISTING_SOURCE_NO = "GCO126000030";

    // =========================================================
    // BEFORE ALL TESTS
    // =========================================================

    @BeforeAll
    public static void setUpClass()
            throws SQLException, GuanzonException, IOException {

        System.out.println("========================================");
        System.out.println("SalesReservationTest - setUpClass()");
        System.out.println("========================================");

        instance = new GRiderCAS();

        if (!instance.loadEnv("gRider")) {
            throw new IllegalStateException(
                    "Unable to load environment: " + instance.getMessage());
        }

        if (!instance.logUser("gRider", "M001250015")) {
            throw new IllegalStateException(
                    "Unable to login user: " + instance.getMessage());
        }

        String path;
        String tempPath;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            path = "D:/GGC_Maven_Systems";
            tempPath = "D:/temp";
        } else {
            path = "/srv/GGC_Maven_Systems";
            tempPath = "/srv/temp";
        }

        System.setProperty("sys.default.path.config", path);
        System.setProperty("sys.default.path.metadata", path + "/config/metadata/new/");
        System.setProperty("sys.default.path.temp", tempPath);

        if (!loadProperties()) {
            throw new IllegalStateException("Unable to load CAS configuration.");
        }

        System.out.println("Configuration loaded successfully.");

        poTrans = new SalesControllers(instance, null);

        loadCorePrimary();

        System.out.println("========================================");
        System.out.println("Test environment initialized.");
        System.out.println("========================================");
    }

    private static boolean loadProperties() {
        String configPath = System.getProperty("sys.default.path.config") + "/config/cas.properties";
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(configPath));

            setProperty("sys.main.industry", props);
            setProperty("sys.general.industry", props);
            setProperty("sys.dept.finance", props);
            setProperty("sys.dept.procurement", props);
            setProperty("user.selected.industry", props);
            setProperty("user.selected.category", props);
            setProperty("user.selected.company", props);

            System.setProperty("sys.default.client.token",
                    System.getProperty("sys.default.path.config") + "/client.token");
            System.setProperty("sys.default.access.token",
                    System.getProperty("sys.default.path.config") + "/access.token");

            setProperty("sys.default.path.temp.attachments", props);
            setProperty("allowed.department", props);

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static void setProperty(String name, Properties props) {
        String value = props.getProperty(name);
        if (value != null) {
            System.setProperty(name, value);
        }
    }

    @BeforeEach
    public void setUp() {
        System.out.println("Starting test...");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Test completed.");
    }

    // =========================================================
    // HELPER: obtain a fresh SalesReservation transaction object
    // =========================================================

    private SalesReservation newInitializedReservation() throws SQLException, GuanzonException {
        SalesReservation reservation = poTrans.SalesReservation();
        JSONObject result = reservation.InitTransaction();
        Assertions.assertEquals("success", result.get("result"),
                "InitTransaction should succeed: " + result.get("message"));
        return reservation;
    }

    /**
     * Creates a new (ADDNEW) reservation with default fields populated,
     * mirroring what a UI form would do before adding line items.
     */
    private SalesReservation newDraftReservation() throws Exception {
        SalesReservation reservation = newInitializedReservation();

        reservation.setIndustryID(INDUSTRY_ID);
        reservation.setCompanyID(COMPANY_ID);
        reservation.setCategoryCd(CATEGORY_CD);

        JSONObject newTxnResult = reservation.NewTransaction();
        Assertions.assertEquals("success", newTxnResult.get("result"),
                "NewTransaction should succeed: " + newTxnResult.get("message"));

        return reservation;
    }

    /**
     * `dTransact` and `dExpected` are DATE columns (no time component) in
     * Sales_Reservation_Master. H2, unlike MySQL, refuses to parse a full
     * timestamp string as a DATE literal, so any value we set here must have
     * its time-of-day zeroed out first. This mirrors what
     * Model_Sales_Reservation_Master.initialize() already does internally
     * via its private xsDateShort() helper for the same two columns - we're
     * just being consistent about it wherever the test sets these fields
     * directly, without changing the schema or production code.
     */
    private static Date toDateOnly(Date fullTimestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fullTimestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // =========================================================
    // BASIC ENVIRONMENT TESTS
    // =========================================================

    @Test
    @Order(1)
    public void testDatabaseConnection() {
        System.out.println("Running testDatabaseConnection()");

        Assertions.assertNotNull(instance, "GRider instance should not be null.");
        Assertions.assertNotNull(poTrans, "SalesControllers should not be null.");
        Assertions.assertNotNull(conn, "Database connection should not be null.");

        try {
            Assertions.assertFalse(conn.isClosed(), "Database connection should be open.");
        } catch (SQLException ex) {
            Assertions.fail("Unable to check database connection: " + ex.getMessage());
        }
    }

    // =========================================================
    // InitTransaction / NewTransaction / initFields
    // =========================================================

    @Test
    @Order(2)
    public void testInitTransaction_Succeeds() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(91)
    public void testOpenTransaction_Succeeds() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.OpenTransaction(TRANSNO_ALREADY_CONFIRMED);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }



    @Test
    @Order(92)
    public void testUpdateTransaction_Succeeds() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.OpenTransaction(TRANSNO_ALREADY_CONFIRMED);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        loJSON = reservation.validateConfirmedTransactionApproval();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        loJSON = reservation.UpdateTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        loJSON =reservation.Detail(0).setQuantity(0.00);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        reservation.Detail(0).setStockID("M00125000004");
        Assert.assertEquals(reservation.Detail(0).getStockID(), "M00125000004");
        reservation.Detail(0).setQuantity(1.00);
//                        Assert.assertEquals( reservation.Detail(0).getQuantity(), quantity);
        reservation.Detail(0).setUnitPrice(10000.0000);
//                        Assert.assertEquals(10000.0000, reservation.Detail(0).getUnitPrice());
        reservation.Detail(0).setMinimumDown(1000.0000);
//                        Assert.assertEquals(1000.0000, reservation.Detail(0).getMinimumDown());
        reservation.Detail(0).setClassify("F");
//                        Assert.assertEquals("F", reservation.Detail(0).getClassify());
        reservation.Detail(0).setApproved(0);
//                        Assert.assertEquals(0, reservation.Detail(0).getMinimumDown());
        reservation.Detail(0).setNotes("sam");
        Assert.assertEquals(reservation.Detail(0).getNotes(), "sam");
        reservation.AddDetail();



        loJSON =reservation.SaveTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }


        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }
    // NOTE: this test used to be named testCancelTransaction_Succeeds but
    // actually invoked VoidTransaction() - renamed to match what it tests.
    // A genuine CancelTransaction() test has been added below.
    @Test
    @Order(93)
    public void testVoidTransaction_Succeeds() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.OpenTransaction(TRANSNO_FOR_VOID);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.VoidTransaction("");
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(94)
    public void testVoidTransaction_AlreadyVoided_ReturnsError() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        reservation.OpenTransaction(TRANSNO_ALREADY_VOID);

        JSONObject result = reservation.VoidTransaction("");

        Assertions.assertEquals("error", result.get("result"));
        Assertions.assertTrue(((String) result.get("message")).toLowerCase().contains("void"));
    }


    // =========================================================
    // ConfirmTransaction()
    //
    // Mirrors SalesReservation_EntryCarController's "btnSave" handler and
    // SalesReservation_ConfirmationCarController's "btnConfirm" handler,
    // both of which call checkExistingTrans(sourceCode, sourceNo) on the
    // opened transaction immediately before ConfirmTransaction().
    // =========================================================

    @Test
    @Order(98)
    public void testConfirmTransaction_Succeeds() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        reservation.OpenTransaction(TRANSNO_FOR_CONFIRM);

        JSONObject existingTransResult = reservation.checkExistingTrans(
                reservation.Master().getSourceCode(),
                reservation.Master().getSourceNo());
        Assertions.assertEquals("success", existingTransResult.get("result"),
                "checkExistingTrans should not block a fresh, unused source.");

        reservation.setWithUI(false);

        JSONObject result = reservation.ConfirmTransaction("");

        Assertions.assertEquals("success", result.get("result"),
                "ConfirmTransaction should succeed: " + result.get("message"));
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(99)
    public void testConfirmTransaction_AlreadyConfirmed_ReturnsError() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        reservation.OpenTransaction(TRANSNO_ALREADY_CONFIRMED);

        JSONObject result = reservation.ConfirmTransaction("");

        Assertions.assertEquals("error", result.get("result"));
        Assertions.assertTrue(((String) result.get("message")).toLowerCase().contains("confirm"));
    }

    // =========================================================
    // PaidTransaction()
    // =========================================================

//    @Test
//    @Order(100)
//    public void testPaidTransaction_Succeeds() throws Exception {
//        SalesReservation reservation = newInitializedReservation();
//        reservation.OpenTransaction(TRANSNO_FOR_PAID);
//
//        JSONObject result = reservation.PaidTransaction("");
//
//        Assertions.assertEquals("success", result.get("result"),
//                "PaidTransaction should succeed: " + result.get("message"));
//        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
//    }

//    @Test
//    @Order(101)
//    public void testPaidTransaction_AlreadyPaid_ReturnsError() throws Exception {
//        SalesReservation reservation = newInitializedReservation();
//        reservation.OpenTransaction(TRANSNO_ALREADY_PAID);
//
//        JSONObject result = reservation.PaidTransaction("");
//
//        // NOTE: PaidTransaction() currently reuses the "already confirmed"
//        // message text for this guard clause (see SalesReservation.java) -
//        // this test asserts today's actual behavior rather than the message
//        // that was probably intended, so it will flag the copy/paste bug if
//        // the message text is ever fixed and someone forgets to update this.
//        Assertions.assertEquals("error", result.get("result"));
//        Assertions.assertTrue(((String) result.get("message")).toLowerCase().contains("already"));
//    }

    // =========================================================
    // checkExistingTrans() - blocks re-confirming a source already in use
    // =========================================================

//    @Test
//    @Order(102)
//    public void testCheckExistingTrans_SourceAlreadyConfirmed_ReturnsError() throws Exception {
//        SalesReservation reservation = newInitializedReservation();
//
//        JSONObject result = reservation.checkExistingTrans(EXISTING_SOURCE_CODE, EXISTING_SOURCE_NO);
//
//        Assertions.assertEquals("error", result.get("result"));
//        Assertions.assertTrue(((String) result.get("message")).contains("already in use"));
//    }

    // =========================================================
    // getReservationList() / getSalesReservationCount()
    //
    // Mirrors SalesReservation_ConfirmationCarController's "loadTableSourceList"
    // task, which calls getReservationList(...) then iterates
    // getSalesReservationCount() rows via poSalesReservationMasterList(row).
    // =========================================================

    @Test
    @Order(103)
    public void testGetReservationList_PopulatesSalesReservationCount() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        reservation.setIndustryID(INDUSTRY_ID);
        reservation.setCompanyID(COMPANY_ID);
        reservation.setCategoryCd(CATEGORY_CD);
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        JSONObject result = reservation.getReservationList("", "");

        Assertions.assertEquals("success", result.get("result"), (String) result.get("message"));
        Assertions.assertTrue(reservation.getSalesReservationCount() >= 0);
        if (reservation.getSalesReservationCount() > 0) {
            Assertions.assertNotNull(reservation.poSalesReservationMasterList(0));
        }
    }

    @Test
    @Order(3)
    public void testNewTransaction_DefaultsFieldsCorrectly() throws Exception {
        double quantity = 1.00;

        SalesReservation reservation = poTrans.SalesReservation();

        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }


        // dTransact / dExpected are DATE columns -> must be date-only.
        // dEntryDte is a DATETIME column -> keeps the full timestamp as-is.
        Date dateOnlyToday = toDateOnly(instance.getServerDate());

        reservation.Master().setIndustryID(INDUSTRY_ID);
        Assert.assertEquals(reservation.Master().getIndustryID(), INDUSTRY_ID);
        reservation.Master().setCompanyID(COMPANY_ID);
        Assert.assertEquals(reservation.Master().getCompanyID(), COMPANY_ID);
        reservation.Master().setCategoryCode(CATEGORY_CD);
        Assert.assertEquals(reservation.Master().getCategoryCode(), CATEGORY_CD);
        reservation.Master().setBranchCode(BRANCH_CODE);
        Assert.assertEquals(reservation.Master().getBranchCode(), BRANCH_CODE);
        reservation.Master().setTransactionDate(dateOnlyToday);
        Assert.assertEquals(reservation.Master().getTransactionDate(), dateOnlyToday);
        reservation.Master().setExpectedDate(dateOnlyToday);
        Assert.assertEquals(reservation.Master().getExpectedDate(), dateOnlyToday);
        reservation.Master().setClientID(CLIENT_ID);
        Assert.assertEquals(reservation.Master().getClientID(), CLIENT_ID);
        reservation.Master().setAddressID(ADDRESS_ID);
        Assert.assertEquals(reservation.Master().getAddressID(), ADDRESS_ID);
        reservation.Master().setContactID(MOBILE_ID);
        Assert.assertEquals(reservation.Master().getContactID(), MOBILE_ID);
        reservation.Master().setRemarks("remarks");
        Assert.assertEquals(reservation.Master().getRemarks(), "remarks");
        reservation.Master().setEntryDate(instance.getServerDate());
        Assert.assertEquals(reservation.Master().getEntryDate(), instance.getServerDate());
        reservation.Master().setSourceCode("SI");
        Assert.assertEquals(reservation.Master().getSourceCode(), "SI");
        reservation.Master().setSourceNo("M00125000001");
        Assert.assertEquals(reservation.Master().getSourceNo(), "M00125000001");
        reservation.Master().setEntryNo(1);
        Assert.assertEquals(1, reservation.Master().getEntryNo());

        reservation.Detail(0).setStockID(STOCK_ID);
        Assert.assertEquals(reservation.Detail(0).getStockID(), STOCK_ID);
        reservation.Detail(0).setQuantity(quantity);
        reservation.Detail(0).setUnitPrice(10000.0000);
        reservation.Detail(0).setMinimumDown(1000.0000);
        reservation.Detail(0).setClassify("F");
        reservation.Detail(0).setApproved(0);
        reservation.Detail(0).setNotes("remarks");
        Assert.assertEquals(reservation.Detail(0).getNotes(), "remarks");

        reservation.AddDetail();

        loJSON = reservation.SaveTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
    }



    // =========================================================
    // validateDetails()
    // =========================================================

    @Test
    @Order(4)
    public void testValidateDetails_NoDetailRows_ReturnsError() throws Exception {
        SalesReservation reservation = newDraftReservation();

        JSONObject result = reservation.validateDetails();

        if (reservation.getDetailCount() == 0) {
            Assertions.assertEquals("error", result.get("result"));
            Assertions.assertTrue(((String) result.get("message")).contains("add an item"));
        }
    }

    @Test
    @Order(5)
    public void testValidateDetails_SingleRow_ZeroQuantity_ReturnsError() throws Exception {
        SalesReservation reservation = newDraftReservation();

        if (reservation.getDetailCount() == 0) {
            reservation.AddDetail();
        }
        reservation.Detail(0).setQuantity(0);

        JSONObject result = reservation.validateDetails();

        Assertions.assertEquals("error", result.get("result"));
        Assertions.assertTrue(((String) result.get("message")).toLowerCase().contains("quantity"));
    }

    @Test
    @Order(6)
    public void testValidateDetails_SingleRow_NoStockNoNotes_ReturnsError() throws Exception {
        SalesReservation reservation = newDraftReservation();

        if (reservation.getDetailCount() == 0) {
            reservation.AddDetail();
        }
        reservation.Detail(0).setQuantity(1);
        reservation.Detail(0).setStockID("");
        reservation.Detail(0).setNotes("");

        JSONObject result = reservation.validateDetails();

        Assertions.assertEquals("error", result.get("result"));
        Assertions.assertTrue(((String) result.get("message")).toLowerCase().contains("notes"));
    }

    @Test
    @Order(7)
    public void testValidateDetails_SingleRow_NoStockWithNotes_ReturnsSuccess() throws Exception {
        SalesReservation reservation = newDraftReservation();

        if (reservation.getDetailCount() == 0) {
            reservation.AddDetail();
        }
        reservation.Detail(0).setQuantity(1);
        reservation.Detail(0).setStockID("");
        reservation.Detail(0).setNotes("Custom order, no stock item.");

        JSONObject result = reservation.validateDetails();

        Assertions.assertEquals("success", result.get("result"));
    }

    @Test
    @Order(8)
    public void testValidateDetails_SingleRow_WithStock_ReturnsSuccess() throws Exception {
        SalesReservation reservation = newDraftReservation();

        if (reservation.getDetailCount() == 0) {
            reservation.AddDetail();
        }
        reservation.Detail(0).setQuantity(2);
        reservation.Detail(0).setStockID(STOCK_ID);

        JSONObject result = reservation.validateDetails();

        Assertions.assertEquals("success", result.get("result"));
    }

    @Test
    @Order(9)
    public void testValidateDetails_MultipleRows_AllZeroQuantity_ReturnsError() throws Exception {
        SalesReservation reservation = newDraftReservation();

        while (reservation.getDetailCount() < 2) {
            reservation.Detail(reservation.getDetailCount() - 1).setStockID(STOCK_ID);
            reservation.AddDetail();
        }
        for (int i = 0; i < reservation.getDetailCount(); i++) {
            reservation.Detail(i).setQuantity(0);
        }

        JSONObject result = reservation.validateDetails();

        Assertions.assertEquals("error", result.get("result"));
        Assertions.assertTrue(((String) result.get("message")).toLowerCase().contains("zero quantity"));
    }

    @Test
    @Order(10)
    public void testValidateDetails_MultipleRows_AtLeastOnePositiveQuantity_ReturnsSuccess() throws Exception {
        SalesReservation reservation = newDraftReservation();

        reservation.Detail(0).setStockID(STOCK_ID);
        reservation.Detail(0).setQuantity(0);
        reservation.AddDetail();
        reservation.Detail(1).setStockID(STOCK_ID);
        reservation.Detail(1).setQuantity(3);

        JSONObject result = reservation.validateDetails();

        Assertions.assertEquals("success", result.get("result"));
    }

    // =========================================================
    // AddDetail() guard clause
    // =========================================================

    @Test
    @Order(11)
    public void testAddDetail_LastRowHasEmptyStock_ReturnsError() throws Exception {
        SalesReservation reservation = newDraftReservation();

        if (reservation.getDetailCount() == 0) {
            reservation.AddDetail();
        }
        reservation.Detail(reservation.getDetailCount() - 1).setStockID("");

        JSONObject result = reservation.AddDetail();

        Assertions.assertEquals("error", result.get("result"));
        Assertions.assertTrue(((String) result.get("message")).contains("Last row has empty item."));
    }

    @Test
    @Order(12)
    public void testAddDetail_LastRowHasStock_Succeeds() throws Exception {
        SalesReservation reservation = newDraftReservation();

        if (reservation.getDetailCount() == 0) {
            reservation.AddDetail();
        }
        int beforeCount = reservation.getDetailCount();
        reservation.Detail(beforeCount - 1).setStockID(STOCK_ID);

        JSONObject result = reservation.AddDetail();

        Assertions.assertEquals("success", result.get("result"));
        Assertions.assertEquals(beforeCount + 1, reservation.getDetailCount());
    }

    // =========================================================
    // computeStockSummaries()
    // =========================================================

    @Test
    @Order(13)
    public void testComputeStockSummaries_NetsReversedAgainstNonReversed() throws Exception {
        SalesReservation reservation = newDraftReservation();

        reservation.Detail(0).setStockID(STOCK_ID);
        reservation.Detail(0).setQuantity(5);
        reservation.Detail(0).isReversed(false);
        reservation.AddDetail();

        reservation.Detail(1).setStockID(STOCK_ID);
        reservation.Detail(1).setQuantity(2);
        reservation.Detail(1).isReversed(true);

        List<SalesReservation.StockSummary> summaries = reservation.computeStockSummaries();

        Assertions.assertEquals(1, summaries.size(),
                "Rows sharing the same stock id should collapse into a single summary.");
        SalesReservation.StockSummary summary = summaries.get(0);
        Assertions.assertEquals(STOCK_ID, summary.getStockId());
        Assertions.assertEquals(2.0 - 5.0, summary.getNetQty(), 0.0001);
    }

    // =========================================================
    // checkExistingTrans()
    // =========================================================

    @Test
    @Order(22)
    public void testCheckExistingTrans_NullOrEmptyParams_ReturnsSuccessImmediately() throws Exception {
        SalesReservation reservation = newDraftReservation();

        JSONObject resultNullCode = reservation.checkExistingTrans(null, "SRC-001");
        Assertions.assertEquals("success", resultNullCode.get("result"));

        JSONObject resultEmptyNo = reservation.checkExistingTrans("srsv", "");
        Assertions.assertEquals("success", resultEmptyNo.get("result"));

        JSONObject resultBothEmpty = reservation.checkExistingTrans("", "");
        Assertions.assertEquals("success", resultBothEmpty.get("result"));
    }

    @Test
    @Order(29)
    public void testgetUnifiedSource() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.getUnifiedSource("");
        System.err.println((String) loJSON.get("message"));
//        if (!"success".equals((String) loJSON.get("result"))) {
//            System.err.println((String) loJSON.get("message"));
//            Assert.fail();
//        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(30)
    public void testaddSourceToSalesRsvDetail() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

         loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        loJSON = reservation.addSourceToSalesRsvDetail(TRANSNO_ALREADY_CONFIRMED, "Inquiry");
        if ("success".equals(loJSON.get("result"))) {
                System.out.println("poPurchasingController no:" +reservation.Master().getTransactionNo());
                System.out.println("poPurchasingController no:" +reservation.Master().getSourceNo());
            System.out.println("user ID " + instance.getUserID());


        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    };

    @Test
    @Order(30)
    public void testgetUser() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.getSysUser( "M001250015");

        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(31)
    public void testSearchClient() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setWithUI(false);
        loJSON = reservation.SearchClient( "M001250015",true);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(32)
    public void testSearchTransaction() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        reservation.setWithUI(false);
        loJSON = reservation.SearchTransaction( "GK0126000003");
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }
    @Test
    @Order(33)
    public void testSearchTransactionByFilter() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        reservation.setWithUI(false);
        loJSON = reservation.SearchTransactionbyFilter( "GK0126000003", true);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(34)
    public void testSearchModel() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        reservation.setWithUI(false);
        loJSON = reservation.SearchModel( "M00125000004", true, 0);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }
    @Test
    @Order(34)
    public void testSearchDescription() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        reservation.setWithUI(false);
        loJSON = reservation.SearchDescription( "M00125000004", true, 0);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(35)
    public void testSearchInventory() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        reservation.setWithUI(false);
        loJSON = reservation.SearchInventory( "M00125000004",0, "M00125000004", true);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(36)
    public void testSearchBrand() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        reservation.setWithUI(false);
        loJSON = reservation.SearchBrand( "M00125000004",true, 0);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(37)
    public void testSearchBranch() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        reservation.setWithUI(false);
        loJSON = reservation.SearchBranch( "M00125000004",true);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }

    @Test
    @Order(38)
    public void testRtrieveMasterList() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN+ Sales_Reservation_Static.CONFIRMED);
        reservation.setWithUI(false);
        loJSON = reservation.getReservationList(TRANSNO_ALREADY_CONFIRMED, "Inquiry");
        if ("success".equals(loJSON.get("result"))) {
            System.out.println("poPurchasingController no:" +reservation.Master().getTransactionNo());
            System.out.println("poPurchasingController no:" +reservation.Master().getSourceNo());
            System.out.println("user ID " + instance.getUserID());


        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    };
    @Test
    @Order(39)
    public void testSearchBarcode() throws Exception {
        SalesReservation reservation = newInitializedReservation();
        JSONObject loJSON = reservation.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }

        loJSON = reservation.NewTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        reservation.setTransactionStatus(Sales_Reservation_Static.OPEN);
        reservation.setWithUI(false);
        loJSON = reservation.SearchBarcode( "M00125000004",true, 0);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        Assertions.assertNotNull(reservation.Master(), "Master model should be initialized.");
    }
    // =========================================================
    // LOAD DATABASE SCHEMA + DATA
    // =========================================================


    private static void loadCorePrimary() throws IOException, SQLException {
        conn = instance.getGConnection().getConnection();

        if (conn == null) {
            throw new SQLException("GRider returned a null database connection.");
        }

        try (Statement statement = conn.createStatement()) {
            statement.execute("SET MODE MySQL");
        }

        System.out.println("Loading database schemas and test data...");

        try (
                FileReader s1 = new FileReader("test-data/branch_schema.sql");
                FileReader s2 = new FileReader("test-data/company_schema.sql");
                FileReader s3 = new FileReader("test-data/industry_schema.sql");
                FileReader s4 = new FileReader("test-data/client_master_schema.sql");
                FileReader s5 = new FileReader("test-data/client_address_schema.sql");
                FileReader s6 = new FileReader("test-data/client_mobile_schema.sql");
                FileReader s7 = new FileReader("test-data/brand_schema.sql");
                FileReader s8 = new FileReader("test-data/model_schema.sql");
                FileReader s9 = new FileReader("test-data/inventory_schema.sql");
                FileReader s10 = new FileReader("test-data/inv_type_schema.sql");
                FileReader s11 = new FileReader("test-data/tax_code_schema.sql");
                FileReader s12 = new FileReader("test-data/sales_reservation_master_schema.sql");
                FileReader s13 = new FileReader("test-data/sales_reservation_detail_schema.sql");
                // Needed because ConfirmTransaction()/VoidTransaction()/CancelTransaction()/
                // PaidTransaction() call setProcessSource(), which opens a SalesInquiry
                // transaction (queries Sales_Inquiry_Master) whenever the reservation being
                // acted on has a non-empty Master().getSourceNo(). Without this table, that
                // path throws JdbcSQLSyntaxErrorException instead of a normal JSON error.
                FileReader s14 = new FileReader("test-data/sales_inquiry_master_schema.sql");
                FileReader s15 = new FileReader("test-data/sales_inquiry_detail_schema.sql");
                FileReader s16 = new FileReader("test-data/transaction_status_history_schema.sql");
                FileReader s17 = new FileReader("test-data/sales_quotation_master_schema.sql");
                FileReader s18 = new FileReader("test-data/sales_quotation_detail_schema.sql");
                FileReader s19 = new FileReader("test-data/sales_quotation_detail_installment_schema.sql");
                FileReader s20 = new FileReader("test-data/model_variant_schema.sql");
                FileReader s21 = new FileReader("test-data/color_schema.sql");

                FileReader d1 = new FileReader("test-data/branch_data.sql");
                FileReader d2 = new FileReader("test-data/company_data.sql");
                FileReader d3 = new FileReader("test-data/industry_data.sql");
                FileReader d4 = new FileReader("test-data/client_master_data.sql");
                FileReader d5 = new FileReader("test-data/client_address_data.sql");
                FileReader d6 = new FileReader("test-data/client_mobile_data.sql");
                FileReader d7 = new FileReader("test-data/brand_data.sql");
                FileReader d8 = new FileReader("test-data/model_data.sql");
                FileReader d9 = new FileReader("test-data/inventory_data.sql");
                FileReader d10 = new FileReader("test-data/inv_type_data.sql");
                FileReader d11 = new FileReader("test-data/tax_code_data.sql");
                FileReader d12 = new FileReader("test-data/sales_reservation_master_data.sql");
                FileReader d13 = new FileReader("test-data/sales_reservation_detail_data.sql");
                FileReader d14 = new FileReader("test-data/sales_inquiry_master_data.sql");
                FileReader d15 = new FileReader("test-data/sales_inquiry_detail_data.sql");
                FileReader d16 = new FileReader("test-data/transaction_status_history_data.sql");
                FileReader d17 = new FileReader("test-data/sales_quotation_master_data.sql");
                FileReader d18 = new FileReader("test-data/sales_quotation_detail_data.sql");
                FileReader d19 = new FileReader("test-data/sales_quotation_detail_installment_data.sql");
                FileReader d20 = new FileReader("test-data/model_variant_data.sql");
                FileReader d21 = new FileReader("test-data/color_data.sql");
                // TODO(fixture): once you have sales_inquiry_master_data.sql /
                // sales_inquiry_detail_data.sql, add FileReader d14/d15 for them here and
                // RunScript.execute(conn, d14/d15) below - that will let you seed a reservation
                // whose SourceCode/SourceNo point at a real, confirmed Sales_Inquiry_Master row,
                // so the setProcessSource() "linked to an inquiry" path can be tested directly
                // instead of only avoided.
        ) {
            RunScript.execute(conn, s1);
            RunScript.execute(conn, s2);
            RunScript.execute(conn, s3);
            RunScript.execute(conn, s4);
            RunScript.execute(conn, s5);
            RunScript.execute(conn, s6);
            RunScript.execute(conn, s7);
            RunScript.execute(conn, s8);
            RunScript.execute(conn, s9);
            RunScript.execute(conn, s10);
            RunScript.execute(conn, s11);
            RunScript.execute(conn, s12);
            RunScript.execute(conn, s13);
            RunScript.execute(conn, s14);
            RunScript.execute(conn, s15);
            RunScript.execute(conn, s16);
            RunScript.execute(conn, s17);
            RunScript.execute(conn, s18);
            RunScript.execute(conn, s19);
            RunScript.execute(conn, s20);
            RunScript.execute(conn, s21);

            RunScript.execute(conn, d1);
            RunScript.execute(conn, d2);
            RunScript.execute(conn, d3);
            RunScript.execute(conn, d4);
            RunScript.execute(conn, d5);
            RunScript.execute(conn, d6);
            RunScript.execute(conn, d7);
            RunScript.execute(conn, d8);
            RunScript.execute(conn, d9);
            RunScript.execute(conn, d10);
            RunScript.execute(conn, d11);
            RunScript.execute(conn, d12);
            RunScript.execute(conn, d13);
            RunScript.execute(conn, d14);
            RunScript.execute(conn, d15);
            RunScript.execute(conn, d16);
            RunScript.execute(conn, d17);
            RunScript.execute(conn, d18);
            RunScript.execute(conn, d20);
            RunScript.execute(conn, d21);

            System.out.println("All schemas and test data loaded successfully.");
        }
    }

    // =========================================================
    // AFTER ALL TESTS
    // =========================================================

    @AfterAll
    public static void tearDownClass() {
        System.out.println("Closing SalesReservationTest...");

        killdbcon();

        clearProperty("sys.default.path.config");
        clearProperty("sys.default.path.metadata");
        clearProperty("sys.default.path.temp");
        clearProperty("sys.main.industry");
        clearProperty("sys.general.industry");
        clearProperty("sys.dept.finance");
        clearProperty("sys.dept.procurement");
        clearProperty("user.selected.industry");
        clearProperty("user.selected.category");
        clearProperty("user.selected.company");
        clearProperty("sys.default.client.token");
        clearProperty("sys.default.access.token");
        clearProperty("sys.default.path.temp.attachments");
        clearProperty("allowed.department");

        System.out.println("System properties cleared.");
    }

    private static void clearProperty(String property) {
        System.clearProperty(property);
    }

    private static void killdbcon() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}