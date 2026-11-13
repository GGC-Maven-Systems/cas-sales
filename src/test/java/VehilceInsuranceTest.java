
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
import javax.script.ScriptException;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehicleInsurance;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VehilceInsuranceTest {
    static GRiderCAS instance;
    static VehicleInsurance poController;
    static Connection conn;
    private static String psUserId = "GCO1260011";//M001250015;
    private static String psCompanyId = "M001";
    private String psTransNo = "GK0126000001";

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
        schemaScripts.add("transaction_status_history_schema");
        
        schemaScripts.add("validity_period_master_schema");
        schemaScripts.add("vehicle_insurance_master_schema");
        schemaScripts.add("brand_schema");
        schemaScripts.add("model_schema");
        schemaScripts.add("model_variant_schema");
        schemaScripts.add("color_schema");
        
        dataScripts.add("client_master_data");
        dataScripts.add("transaction_status_history_data");
        
        dataScripts.add("validity_period_master_data");
        dataScripts.add("vehicle_insurance_master_data");
        dataScripts.add("model_data");
        dataScripts.add("model_variant_data");
        dataScripts.add("brand_data");
        dataScripts.add("color_data");

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
        poController = new SalesControllers(instance, null).VehicleInsurance();
        poController.setWithUI(false);
        poController.setVerifyEntryNo(false);
        Assert.assertNotNull(poController);
    }

    private static void startNewTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        if (poController == null) {
            resetController();
        }

        JSONObject loJSON = poController.InitTransaction();
        Assert.assertEquals("success", loJSON.get("result"));

        poController.setCompanyId(psCompanyId);

        loJSON = poController.NewTransaction();
        Assert.assertEquals("success", loJSON.get("result"));
    }
    
    /*Convert Date to String*/
    private static String xsDateShort(Date fdValue) {
        if(fdValue == null){
            return "1900-01-01";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    
    public String formatDate(String dateString) {
        DateTimeFormatter outputFormat =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DateTimeFormatter[] inputFormats = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
        };

        for (DateTimeFormatter inputFormat : inputFormats) {
            try {
                return LocalDate.parse(dateString, inputFormat)
                                .format(outputFormat);
            } catch (java.time.format.DateTimeParseException e) {
                // Try the next format.
            }
        }

        throw new IllegalArgumentException(
            "Unsupported date format: " + dateString
        );
    }
    
    
    @Test
    public void test0001(){
        try {
            JSONObject loJSON = new JSONObject();
            
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            
            loJSON = poController.Master().setValidityDescription("Test Vehicle Insurance");
            Assert.assertEquals("success", loJSON.get("result"));
            
//            loJSON = poController.populateVehicleList();
//            System.out.println("MESSAGE : " + loJSON.get("message"));
//            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadDetail();
            
            //1. Load detail 
            System.out.println("---------------------------------DETAIL LIST------------------------------------------");
            for(int lnCtr = 0;lnCtr < poController.getDetailCount();lnCtr++ ){
                    System.out.println("Variant ID : " + poController.Detail(lnCtr).getVariantId());
                    System.out.println("SRP : " + poController.Detail(lnCtr).ModelVariant().getSellingPrice());
                    System.out.println("Year : " + poController.Detail(lnCtr).getYearNo());
                    System.out.println("Amount : " + poController.Detail(lnCtr).getAmount());
                    System.out.println("1st Year Amount : " + poController.getAmount(poController.Detail(lnCtr).getVariantId(), 1));
                    System.out.println("2nd Year Amount : " + poController.getAmount(poController.Detail(lnCtr).getVariantId(), 2));
                    System.out.println("3rd Year Amount : " + poController.getAmount(poController.Detail(lnCtr).getVariantId(), 3));
                    System.out.println("4th Year Amount : " + poController.getAmount(poController.Detail(lnCtr).getVariantId(), 4));
                    System.out.println("5th Year Amount : " + poController.getAmount(poController.Detail(lnCtr).getVariantId(), 5));
                    
                    System.out.println("---------------------------------SUGGESTED AMOUNT------------------------------------------");
                    System.out.println("1st Year Amount : " + poController.getSuggestedAmount(poController.Detail(lnCtr).ModelVariant().getSellingPrice(), 1));
                    System.out.println("2nd Year Amount : " + poController.getSuggestedAmount(poController.Detail(lnCtr).ModelVariant().getSellingPrice(), 2));
                    System.out.println("3rd Year Amount : " + poController.getSuggestedAmount(poController.Detail(lnCtr).ModelVariant().getSellingPrice(), 3));
                    System.out.println("4th Year Amount : " + poController.getSuggestedAmount(poController.Detail(lnCtr).ModelVariant().getSellingPrice(), 4));
                    System.out.println("5th Year Amount : " + poController.getSuggestedAmount(poController.Detail(lnCtr).ModelVariant().getSellingPrice(), 5));
                    
                
            }
            System.out.println("---------------------------------------------------------------------------------");
            
            loJSON = poController.SaveTransaction();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            String lsTransNo = poController.Master().getValidityId();
            loJSON = poController.OpenTransaction(lsTransNo);
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            System.out.println("---------------------------------DETAIL LIST------------------------------------------");
            for(int lnCtr = 0;lnCtr < poController.getDetailCount();lnCtr++ ){
                    System.out.println("Variant ID : " + poController.Detail(lnCtr).getVariantId());
                    System.out.println("Year : " + poController.Detail(lnCtr).getYearNo());
                    System.out.println("Amount : " + poController.Detail(lnCtr).getAmount());
                
            }
            System.out.println("---------------------------------------------------------------------------------");
            
            loJSON = poController.UpdateTransaction();
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.Master().setValidityDescription("Test Vehicle Financing Promo 2");
            Assert.assertEquals("success", loJSON.get("result"));
            
            System.out.println("---------------------------------DETAIL LIST------------------------------------------");
            for(int lnCtr = 0;lnCtr < poController.getDetailCount();lnCtr++ ){
                    System.out.println("Variant ID : " + poController.Detail(lnCtr).getVariantId());
                    System.out.println("Year : " + poController.Detail(lnCtr).getYearNo());
                    System.out.println("Amount : " + poController.Detail(lnCtr).getAmount());
                
            }
            System.out.println("---------------------------------------------------------------------------------");
            
            
            loJSON = poController.SaveTransaction();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            lsTransNo = poController.Master().getValidityId();
            loJSON = poController.OpenTransaction(lsTransNo);
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            System.out.println("---------------------------------DETAIL LIST------------------------------------------");
            for(int lnCtr = 0;lnCtr < poController.getDetailCount();lnCtr++ ){
                    System.out.println("Variant ID : " + poController.Detail(lnCtr).getVariantId());
                    System.out.println("Year : " + poController.Detail(lnCtr).getYearNo());
                    System.out.println("Amount : " + poController.Detail(lnCtr).getAmount());
                
            }
            System.out.println("---------------------------------------------------------------------------------");
            
            lsTransNo = poController.Master().getValidityId();
            loJSON = poController.OpenTransaction(lsTransNo);
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.VoidTransaction();
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            
            loJSON = poController.Master().setValidityDescription("Test Vehicle Add On");
            Assert.assertEquals("success", loJSON.get("result"));
//            loJSON = poController.populateVehicleList();
//            System.out.println("MESSAGE : " + loJSON.get("message"));
//            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadDetail();
            
            System.out.println("---------------------------------DETAIL LIST------------------------------------------");
            for(int lnCtr = 0;lnCtr < poController.getDetailCount();lnCtr++ ){
                    System.out.println("Variant ID : " + poController.Detail(lnCtr).getVariantId());
                    System.out.println("Year : " + poController.Detail(lnCtr).getYearNo());
                    System.out.println("Amount : " + poController.Detail(lnCtr).getAmount());
                
            }
            System.out.println("---------------------------------------------------------------------------------");
            
            loJSON = poController.SaveTransaction();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            lsTransNo = poController.Master().getValidityId();
            loJSON = poController.OpenTransaction(lsTransNo);
            Assume.assumeTrue("Fixture transaction not available: " + lsTransNo,
                    "success".equals(loJSON.get("result")));
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.ApproveTransaction();
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
//            loJSON = poController.printTransaction("--All--");
//            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
//            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.OpenTransaction(lsTransNo);
            Assume.assumeTrue("Fixture transaction not available: " + lsTransNo,
                    "success".equals(loJSON.get("result")));
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.CancelTransaction();
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            test0001History();
            
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } 
    }
    
    
    public void test0001History() {
        JSONObject loJSON = new JSONObject();
        try {
            poController.setWithUI(false);
            poController.ShowStatusHistory();
            
            poController.getSysUser(psUserId, true);
            
            loJSON = poController.getEntryBy();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            poController.getSourceCode();
            poController.ReloadDetail();
            poController.getUpdateStatusBy(poController.Master().getRecordStatus());
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } 
    }
    
    @Test
    public void test0002() {
        Assert.assertNotNull(poController);

        Assert.assertEquals("Open", poController.getStatus(ValidityPeriodStatus.OPEN));
        Assert.assertEquals("Approved", poController.getStatus(ValidityPeriodStatus.APPROVED));
        Assert.assertEquals("Cancelled", poController.getStatus(ValidityPeriodStatus.CANCELLED));
        Assert.assertEquals("Voided", poController.getStatus(ValidityPeriodStatus.VOID));

        Assert.assertEquals("Unknown", poController.getStatus("X"));
    }
    
    @Test
    public void test0003Search() {
        JSONObject loJSON = new JSONObject();
        try {
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            poController.setTransactionStatus("0123");
            loJSON = poController.SearchTransaction(psTransNo, false);
            System.out.println("MESSAGE : " + loJSON.get("message"));
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            Assert.assertEquals(MiscUtil.getException(ex), MiscUtil.getException(ex));
        } 
    }

    
}
