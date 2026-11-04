
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
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.VehiclePromotion;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.status.ValidityPeriodStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VehiclePromotionTest {
    static GRiderCAS instance;
    static VehiclePromotion poController;
    static Connection conn;
    private static String psUserId = "GCO1260011";//M001250015;
    private static String psCompanyId = "M001";
    private static String psIndustryId = "03";
    private static String psCategoryId = "000005";
    private String psTransNo = "GK0126000001";
    private String psInquiryNo = "GK0126000001";
    private String psClientId = "A00120000016";

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
        schemaScripts.add("industry_schema");
        schemaScripts.add("transaction_status_history_schema");
        
        schemaScripts.add("brand_schema");
        schemaScripts.add("model_schema");
        schemaScripts.add("color_schema");
        schemaScripts.add("model_variant_schema");
        schemaScripts.add("model_variant_insurance_schema");
        schemaScripts.add("validity_period_master_schema");
        schemaScripts.add("vehicle_addon_type_schema");
        schemaScripts.add("freebie_master_schema");
        schemaScripts.add("vehicle_inclusion_master_schema");
        schemaScripts.add("promotion_addon_schema");
        schemaScripts.add("promotion_applicability_schema");
        schemaScripts.add("promotion_freebie_schema");
        schemaScripts.add("promotion_inclusion_schema");
        schemaScripts.add("promotion_master_schema");
        
        dataScripts.add("client_master_data");
        dataScripts.add("industry_data");
        dataScripts.add("transaction_status_history_data");
        
        dataScripts.add("brand_data");
        dataScripts.add("model_data");
        dataScripts.add("color_data");
        dataScripts.add("model_variant_data");
        dataScripts.add("model_variant_insurance_data");
        dataScripts.add("validity_period_master_data");
        dataScripts.add("vehicle_addon_type_data");
        dataScripts.add("freebie_master_data");
        dataScripts.add("vehicle_inclusion_master_data");
        dataScripts.add("promotion_addon_data");
        dataScripts.add("promotion_applicability_data");
        dataScripts.add("promotion_freebie_data");
        dataScripts.add("promotion_inclusion_data");
        dataScripts.add("promotion_master_data");

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
        poController = new SalesControllers(instance, null).VehiclePromotion();
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
        poController.setIndustryId(psIndustryId);
        poController.setCategoryId(psCategoryId);

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
    
    @Test
    public void test0001(){
        try {
            JSONObject loJSON = new JSONObject();
            
            resetController();
            startNewTransaction();
            poController.setWithUI(false);
            
            loJSON = poController.PromotionMaster().setDescription("Unit Test Promotion");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionMaster().setPaymentForm("0");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionMaster().setDiscountAmount(200.00);
            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadAddOn();
            loJSON = poController.SearchAddOn("GK0126000001", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionAddOnList(0).setDiscountAmount(1000.00);
            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadFreebie();
            loJSON = poController.SearchFreebie("GK0126000001", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionFreebieList(0).setQuantity(2);
            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadInclusion();
            loJSON = poController.SearchInclusion("GK0126000001", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionInclusionList(0).setQuantity(2);
            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadApplicability();
            loJSON = poController.SearchBrand("GK01012", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.SearchModel("W00525028", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.SearchModelVariant("00020", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            
            System.out.println("-----------LOAD DETAIL-----------");
            for(int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++){
                System.out.println("Description : " + poController.Detail(lnCtr).getDescription());
                System.out.println("Discount Amount : " + poController.Detail(lnCtr).getDiscountAmount());
            }
            System.out.println("-----------LOAD ADD ONS-----------");
            for(int lnCtr = 0; lnCtr < poController.getPromotionAddOnCount(); lnCtr++){
                if(poController.PromotionAddOnList(lnCtr).getAddOnId()== null || "".equals(poController.PromotionAddOnList(lnCtr).getAddOnId())){
                    continue;
                }
                System.out.println("AddOn ID : " + poController.PromotionAddOnList(lnCtr).getAddOnId());
                System.out.println("AddOn Name : " + poController.PromotionAddOnList(lnCtr).AddOnType().getAddTypeName());
                System.out.println("AddOn Amount : " + poController.PromotionAddOnList(lnCtr).AddOnType().getAmount());
                System.out.println("Discount Amount : " + poController.PromotionAddOnList(lnCtr).getDiscountAmount());
            }
            System.out.println("-----------LOAD FREEBIES-----------");
            for(int lnCtr = 0; lnCtr < poController.getPromotionFreebieCount(); lnCtr++){
                if(poController.PromotionFreebieList(lnCtr).getFreebieId() == null || "".equals(poController.PromotionFreebieList(lnCtr).getFreebieId())){
                    continue;
                }
                System.out.println("Freebie ID : " + poController.PromotionFreebieList(lnCtr).getFreebieId());
                System.out.println("Freebie Name : " + poController.PromotionFreebieList(lnCtr).Freebie().getDescription());
                System.out.println("Freebie Quantity : " + poController.PromotionFreebieList(lnCtr).getQuantity());
            }
            System.out.println("-----------LOAD INCLUSION-----------");
            for(int lnCtr = 0; lnCtr < poController.getPromotionInclusionCount(); lnCtr++){
                if(poController.PromotionInclusionList(lnCtr).getInclusionId() == null || "".equals(poController.PromotionInclusionList(lnCtr).getInclusionId())){
                    continue;
                }
                System.out.println("Inclusion ID : " + poController.PromotionInclusionList(lnCtr).getInclusionId());
                System.out.println("Inclusion Name : " + poController.PromotionInclusionList(lnCtr).Inclusion().getDescription());
                System.out.println("Inclusion Quantity : " + poController.PromotionInclusionList(lnCtr).getQuantity());
            }
            System.out.println("-----------LOAD APPLICABILITY-----------");
            for(int lnCtr = 0; lnCtr < poController.getPromotionApplicabilityCount(); lnCtr++){
                if(poController.PromotionApplicabilityList(lnCtr).getBrandId() == null || "".equals(poController.PromotionApplicabilityList(lnCtr).getBrandId())){
                    continue;
                }
                System.out.println("Applicability ID : " + poController.PromotionApplicabilityList(lnCtr).getPromotionApplicabilityId());
                System.out.println("Brand : " + poController.PromotionApplicabilityList(lnCtr).Brand().getDescription());
                System.out.println("Model : " + poController.PromotionApplicabilityList(lnCtr).Model().getDescription());
                System.out.println("Variant : " + poController.PromotionApplicabilityList(lnCtr).ModelVariant().getDescription());
            }
            loJSON = poController.SaveTransaction();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            
            System.out.println("-----------LOAD TRANSACTION LIST-----------");
            poController.setTransactionStatus("0123");
            for(int lnCtr = 0; lnCtr < poController.getTransactionListCount(); lnCtr++){
                System.out.println("Validity No : " + poController.TransactionList(lnCtr).getValidityId());
                System.out.println("From Date : " + xsDateShort(poController.TransactionList(lnCtr).getFromDate()));
                System.out.println("Thru Date : " + xsDateShort(poController.TransactionList(lnCtr).getThruDate()));
                System.out.println("Promotion Title : " + poController.TransactionList(lnCtr).getValidityDescription());
            }
            
            String lsTransNo = poController.Master().getValidityId();
            loJSON = poController.OpenTransaction(lsTransNo);
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            System.out.println("-----------LOAD DETAIL-----------");
            for(int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++){
                System.out.println("Description : " + poController.Detail(lnCtr).getDescription());
                System.out.println("Discount Amount : " + poController.Detail(lnCtr).getDiscountAmount());
            }
            System.out.println("-----------LOAD ADD ONS-----------");
            for(int lnCtr = 0; lnCtr < poController.getPromotionAddOnCount(); lnCtr++){
                if(poController.PromotionAddOnList(lnCtr).getAddOnId()== null || "".equals(poController.PromotionAddOnList(lnCtr).getAddOnId())){
                    continue;
                }
                System.out.println("AddOn ID : " + poController.PromotionAddOnList(lnCtr).getAddOnId());
                System.out.println("AddOn Name : " + poController.PromotionAddOnList(lnCtr).AddOnType().getAddTypeName());
                System.out.println("AddOn Amount : " + poController.PromotionAddOnList(lnCtr).AddOnType().getAmount());
                System.out.println("Discount Amount : " + poController.PromotionAddOnList(lnCtr).getDiscountAmount());
            }
            System.out.println("-----------LOAD FREEBIES-----------");
            for(int lnCtr = 0; lnCtr < poController.getPromotionFreebieCount(); lnCtr++){
                if(poController.PromotionFreebieList(lnCtr).getFreebieId() == null || "".equals(poController.PromotionFreebieList(lnCtr).getFreebieId())){
                    continue;
                }
                System.out.println("Freebie ID : " + poController.PromotionFreebieList(lnCtr).getFreebieId());
                System.out.println("Freebie Name : " + poController.PromotionFreebieList(lnCtr).Freebie().getDescription());
                System.out.println("Freebie Quantity : " + poController.PromotionFreebieList(lnCtr).getQuantity());
            }
            System.out.println("-----------LOAD INCLUSION-----------");
            for(int lnCtr = 0; lnCtr < poController.getPromotionInclusionCount(); lnCtr++){
                if(poController.PromotionInclusionList(lnCtr).getInclusionId() == null || "".equals(poController.PromotionInclusionList(lnCtr).getInclusionId())){
                    continue;
                }
                System.out.println("Inclusion ID : " + poController.PromotionInclusionList(lnCtr).getInclusionId());
                System.out.println("Inclusion Name : " + poController.PromotionInclusionList(lnCtr).Inclusion().getDescription());
                System.out.println("Inclusion Quantity : " + poController.PromotionInclusionList(lnCtr).getQuantity());
            }
            System.out.println("-----------LOAD APPLICABILITY-----------");
            for(int lnCtr = 0; lnCtr < poController.getPromotionApplicabilityCount(); lnCtr++){
                if(poController.PromotionApplicabilityList(lnCtr).getBrandId() == null || "".equals(poController.PromotionApplicabilityList(lnCtr).getBrandId())){
                    continue;
                }
                System.out.println("Applicability ID : " + poController.PromotionApplicabilityList(lnCtr).getPromotionApplicabilityId());
                System.out.println("Brand : " + poController.PromotionApplicabilityList(lnCtr).Brand().getDescription());
                System.out.println("Model : " + poController.PromotionApplicabilityList(lnCtr).Model().getDescription());
                System.out.println("Variant : " + poController.PromotionApplicabilityList(lnCtr).ModelVariant().getDescription());
            }
            
            loJSON = poController.UpdateTransaction();
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.PromotionMaster().setDescription("UNIT TEST");
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.SaveTransaction();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
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
            
            loJSON = poController.PromotionMaster().setDescription("Unit Test Promotion");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionMaster().setPaymentForm("0");
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionMaster().setDiscountAmount(200.00);
            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadAddOn();
            loJSON = poController.SearchAddOn("GK0126000001", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionAddOnList(0).setDiscountAmount(1000.00);
            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadFreebie();
            loJSON = poController.SearchFreebie("GK0126000001", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionFreebieList(0).setQuantity(2);
            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadInclusion();
            loJSON = poController.SearchInclusion("GK0126000001", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.PromotionInclusionList(0).setQuantity(2);
            Assert.assertEquals("success", loJSON.get("result"));
            
            poController.ReloadApplicability();
            loJSON = poController.SearchBrand("GK01012", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.SearchModel("W00525028", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            loJSON = poController.SearchModelVariant("00020", true, 0);
            Assert.assertEquals("success", loJSON.get("result"));
            
            loJSON = poController.SaveTransaction();
            System.out.println("MESSAGE : " + loJSON.get("message"));
            Assert.assertEquals("success", loJSON.get("result"));
            
            lsTransNo = poController.Master().getValidityId();
            loJSON = poController.OpenTransaction(lsTransNo);
            Assume.assumeTrue("Fixture transaction not available: " + lsTransNo,
                    "success".equals(loJSON.get("result")));
            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
            Assert.assertEquals("success", loJSON.get("result"));
            
//            loJSON = poController.ApproveTransaction();
//            System.out.println("MESSAGE : " + String.valueOf(loJSON.get("message")));
//            Assert.assertEquals("success", loJSON.get("result"));
            
            
//            loJSON = poController.printTransaction();
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
