
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.StandardFinancingRates;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.FinancingRateStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StandardFinancingRatesTest {
    static GRiderCAS instance;
    static StandardFinancingRates poController;
    static Connection conn;
    private static String psUserId = "GCO1260011";//M001250015;
    private static String psIndustryId = "09";
    private static String psCompanyId = "M001";
    private static String psCategorCd = "0000007";
    private String psTransNo = "GK0100001";

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

        schemaScripts.add("client_master_schema");
        schemaScripts.add("financing_rate_master_schema");
        schemaScripts.add("vehicle_financing_rates_schema");
        schemaScripts.add("parameter_status_history_schema");
        
        dataScripts.add("client_master_data");
        dataScripts.add("financing_rate_master_data");
        dataScripts.add("vehicle_financing_rates_data");
        dataScripts.add("parameter_status_history_data");

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
        poController = new SalesControllers(instance, null).StandardFinancingRates();
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

        loJSON = poController.NewRecord();
        Assert.assertEquals("success", loJSON.get("result"));
    }
    
    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }
    
    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    
    @Test
    public void test0001(){
        try {
            JSONObject loJSON = new JSONObject();
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            
            loJSON = poController.getModel().setStandardRateId(psTransNo);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setDuration(36);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRate(41.00);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRateType("0");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRecordStatus("1");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setFromDate(SQLUtil.toDate(xsDateShort(instance.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.saveRecord();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("error", loJSON.get("result"));
            
            
            loJSON = poController.getModel().setDuration(36);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRate(41.75);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.saveRecord();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            
            //Activate newly saved transaction
            String lsTransNo = poController.getModel().getStandardRateId();
            loJSON = poController.openRecord(lsTransNo);
            Assume.assumeTrue("Fixture transaction not available: " + lsTransNo,
                    "success".equals(loJSON.get("result")));
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            loJSON = poController.DeactivateRecord("test");
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            test004History();
             
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            
            loJSON = poController.getModel().setStandardRateId(psTransNo);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setDuration(0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRate(99.00);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRateType("1");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setRecordStatus("1");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setFromDate(SQLUtil.toDate(xsDateShort(instance.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.getModel().setThruDate(SQLUtil.toDate(xsDateShort(instance.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.saveRecord();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.openRecord(lsTransNo);
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
           
            loJSON = poController.VoidRecord("");
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.ActivateRecord("");
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
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
            
            loJSON = poController.getModel().setStandardRateId("");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("error", loJSON.get("result"));
            loJSON = poController.getModel().setStandardRateId(psTransNo);
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.getModel().setFromDate(SQLUtil.toDate(xsDateShort(instance.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.isEntryOkay();
            Assert.assertEquals("error", loJSON.get("result"));
            
            loJSON = poController.getModel().setThruDate(SQLUtil.toDate(xsDateShort(instance.getServerDate()), SQLUtil.FORMAT_SHORT_DATE));
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
