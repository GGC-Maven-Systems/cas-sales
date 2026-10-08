
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;

import org.h2.tools.RunScript;
import org.json.simple.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.FinancingRates;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;

//@Ignore("Pending schema and SQL test data setup")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FinancingRatesTest {
    static GRiderCAS instance;
    static FinancingRates poController;
    static Connection conn;
    private static String psUserId = "GCO1260011";//M001250015;
    private static String psIndustryId = "09";
    private static String psCompanyId = "M001";
    private static String psCategorCd = "0000007";
    private String psTransNo = "GK0126000001";
    private String psBankId = "00XX003";
    private String psBankId2 = "00XX017";
    private String psBankId3 = "00XX022";
    private String psBankId4 = "00XX023";

    @BeforeClass
    public static void setUpClass() throws GuanzonException, SQLException, IOException {
        instance = new GRiderCAS();

        if (!instance.loadEnv("gRider")) {
            System.err.println(instance.getMessage());
            System.exit(1);
        }

        if (!instance.logUser("gRider", "M001250015")) {
            System.err.println(instance.getMessage());
            System.exit(1);
        }

        loadCorePrimary();

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
            System.err.println("Unable to load config.");
            System.exit(1);
        }

        resetController();
    }

    @AfterClass
    public static void tearDownClass() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        System.clearProperty("sys.default.path.config");
        System.clearProperty("sys.default.path.metadata");
        System.clearProperty("sys.default.path.temp");

        System.clearProperty("sys.main.industry");
        System.clearProperty("sys.general.industry");
        System.clearProperty("sys.dept.finance");
        System.clearProperty("sys.dept.procurement");
        System.clearProperty("user.selected.industry");
        System.clearProperty("user.selected.category");
        System.clearProperty("user.selected.company");
        System.clearProperty("sys.default.client.token");
        System.clearProperty("sys.default.access.token");
        System.clearProperty("sys.default.path.temp.attachments");
        System.clearProperty("allowed.department");
    }

    private static boolean loadProperties() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(System.getProperty("sys.default.path.config") + "/config/cas.properties"));

            System.setProperty("sys.main.industry", props.getProperty("sys.main.industry"));
            System.setProperty("sys.general.industry", props.getProperty("sys.general.industry"));
            System.setProperty("sys.dept.finance", props.getProperty("sys.dept.finance"));
            System.setProperty("sys.dept.procurement", props.getProperty("sys.dept.procurement"));
            System.setProperty("user.selected.industry", props.getProperty("user.selected.industry"));
            System.setProperty("user.selected.category", props.getProperty("user.selected.category"));
            System.setProperty("user.selected.company", props.getProperty("user.selected.company"));
            System.setProperty("sys.default.client.token", System.getProperty("sys.default.path.config") + "/client.token");
            System.setProperty("sys.default.access.token", System.getProperty("sys.default.path.config") + "/access.token");
            System.setProperty("sys.default.path.temp.attachments", props.getProperty("sys.default.path.temp.attachments"));
            System.setProperty("allowed.department", props.getProperty("allowed.department"));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    
    private static void loadCorePrimary() throws IOException, SQLException {
        conn = instance.getGConnection().getConnection();

        List<String> schemaScripts = new ArrayList<>();
        List<String> dataScripts = new ArrayList<>();

        schemaScripts.add("company_schema");
        schemaScripts.add("banks_schema");
        schemaScripts.add("client_master_schema");
        schemaScripts.add("financing_rate_master_schema");
        schemaScripts.add("parameter_status_history_schema");
        schemaScripts.add("vehicle_financing_rates_schema");
        
        dataScripts.add("company_data");
        dataScripts.add("banks_data");
        dataScripts.add("client_master_data");
        dataScripts.add("financing_rate_master_data");
        dataScripts.add("parameter_status_history_data");
        dataScripts.add("vehicle_financing_rates_data");

        for (String schema : schemaScripts) {
            try (FileReader schemaReader = new FileReader("test-data/" + schema + ".sql")) {
                RunScript.execute(conn, schemaReader);
            }
        }

        for (String data : dataScripts) {
            try (FileReader dataReader = new FileReader("test-data/" + data + ".sql")) {
                RunScript.execute(conn, dataReader);
            }
        }

    }
    private static void resetController() {
        poController = new SalesControllers(instance, null).FinancingRates();
        poController.setWithUI(false);
        Assert.assertNotNull(poController);
    }

    private static void startNewTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        if (poController == null) {
            resetController();
        }
        poController.initialize();
        poController.setCompanyId(psCompanyId);

        loJSON = poController.newRecord();
        Assert.assertEquals("success", loJSON.get("result"));
    }
    
    @Test
    public void test0001(){
        try {
            JSONObject loJSON = new JSONObject();
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            poController.getModel().setCompanyId(psCompanyId);
            System.out.println("Company : " + poController.getModel().Company().getCompanyName());
            
            loJSON = poController.getModel().setRateId(psTransNo);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setBankId(psBankId3);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setDuration(48);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRate(49.10);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setDIRate(17.00);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setSIRate(1.00);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRecordStatus(FinancingRateStatus.OPEN);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.saveRecord();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            //Activate newly saved transaction
            String lsTransNo = poController.getModel().getRateId();
            loJSON = poController.openRecord(lsTransNo);
            Assume.assumeTrue("Fixture transaction not available: " + lsTransNo,
                    "success".equals(loJSON.get("result")));
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            loJSON = poController.ActivateRecord("test");
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.openRecord(lsTransNo);
            Assume.assumeTrue("Fixture transaction not available: " + lsTransNo,
                    "success".equals(loJSON.get("result")));
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            loJSON = poController.DeactivateRecord("test");
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            poController.getModel().setCompanyId(psCompanyId);
            System.out.println("Company : " + poController.getModel().Company().getCompanyName());
            
            loJSON = poController.getModel().setRateId(psTransNo);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setBankId(psBankId2);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setDuration(36);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRate(41.75);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setDIRate(17.00);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setSIRate(1.00);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRecordStatus(FinancingRateStatus.OPEN);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.saveRecord();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            //Activate newly saved transaction
            lsTransNo = poController.getModel().getRateId();
            loJSON = poController.openRecord(lsTransNo);
            Assume.assumeTrue("Fixture transaction not available: " + lsTransNo,
                    "success".equals(loJSON.get("result")));
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            loJSON = poController.VoidRecord("test");
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));

            loJSON = poController.openRecord(lsTransNo);
            Assume.assumeTrue("Fixture transaction not available: " + lsTransNo,
                    "success".equals(loJSON.get("result")));
            test004History();
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(FinancingRatesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void test004History() {
        JSONObject loJSON = new JSONObject();
        try {
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            poController.ShowStatusHistory();
            
            poController.getSysUser(psUserId);
            
            loJSON = poController.getEntryBy();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
         
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } 
    }
    
    @Test
    public void test002EntyOkay() {
        try {
            JSONObject loJSON = new JSONObject();
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            
            loJSON = poController.getModel().setRateId("");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("error", loJSON.get("result"));
            loJSON = poController.getModel().setRateId(psTransNo);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("error", loJSON.get("result"));
            loJSON = poController.getModel().setBankId(psBankId);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("error", loJSON.get("result"));
            loJSON = poController.getModel().setDuration(36);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("error", loJSON.get("result"));
            loJSON = poController.getModel().setRate(41.75);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("error", loJSON.get("result"));
            loJSON = poController.getModel().setDIRate(17.00);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("error", loJSON.get("result"));
            
            loJSON = poController.getModel().setBankId(psBankId4);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("success", loJSON.get("result"));
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } 
    }
    
    @Test
    public void test003Search() {
        JSONObject loJSON = new JSONObject();
        try {
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            poController.setRecordStatus("0123");
            loJSON = poController.searchRecord("",false);
            System.out.println("MESSAGE : " + loJSON.get("message"));
            
            loJSON = poController.SearchBank("", true, true);
            System.out.println("MESSAGE : " + loJSON.get("message"));
            
            loJSON = poController.SearchBank(psBankId, true, false);
            System.out.println("MESSAGE : " + loJSON.get("message"));
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } 
    }
    
    @Test
    public void test004Load() {
        JSONObject loJSON = new JSONObject();
        try {
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            poController.setRecordStatus("0123");
            loJSON = poController.loadRecord("");
            System.out.println("MESSAGE : " + loJSON.get("message"));
            
            System.out.println("Record List Count : " + poController.getRecordListCount());
            System.out.println("Rate ID : " + poController.RecordList(0).getRateId());
            System.out.println("Bank : " + poController.RecordList(0).Bank().getBankName());
            
            
            loJSON = poController.loadStandardRates();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            
            System.out.println("Standard Rate List Count : " + poController.getStandardRateListCount());
            System.out.println("Rate ID : " + poController.StandardRateList(0).getStandardRateId());
            System.out.println("Bank : " + poController.StandardRateList(0).getRateType());
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } 
    }
    
    @Test
    public void test0002() {
        Assert.assertNotNull(poController);

        Assert.assertEquals("Open", poController.getStatus(FinancingRateStatus.OPEN));
        Assert.assertEquals("Active", poController.getStatus(FinancingRateStatus.ACTIVE));
        Assert.assertEquals("Inactive", poController.getStatus(FinancingRateStatus.INACTIVE));
        Assert.assertEquals("Voided", poController.getStatus(FinancingRateStatus.VOID));

        Assert.assertEquals("Unknown", poController.getStatus("X"));
    }
    
}
