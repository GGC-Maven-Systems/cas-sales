//import java.io.FileInputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//import org.guanzon.appdriver.base.GRiderCAS;
//import org.guanzon.appdriver.base.GuanzonException;
//import org.h2.tools.RunScript;
//import org.json.simple.JSONObject;
//import org.junit.AfterClass;
//import org.junit.Assert;
//import org.junit.BeforeClass;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runners.MethodSorters;
//import ph.com.guanzongroup.cas.sales.SalesGiveaways;
//import ph.com.guanzongroup.cas.sales.services.SalesControllers;
//import org.guanzon.appdriver.agent.services.Transaction;
//import org.guanzon.appdriver.constant.EditMode;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import ph.com.guanzongroup.cas.sales.SalesCommitment;
//import ph.com.guanzongroup.cas.sales.status.BankApplicationStatus;
//
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class SalesCommitmentTest {
//    static GRiderCAS instance;
//    static SalesCommitment poController;
//    static Connection conn;
//
//    private static String psIndustryId = "02";
//    private static String psCompanyId = "M001";
//    private static String psCategorCd = "0000005";
//    private String psSalesCommitmentNo = "GCO126000002";
//    private String psTransNo = "GK0126000001";
//    private String psStockId = "GK0123000010";
//    private String psBankId = "M00120139";
//
//    @BeforeClass
//    public static void setUpClass() throws GuanzonException, SQLException, IOException {
//        instance = new GRiderCAS();
//
//        if (!instance.loadEnv("gRider")) {
//            System.err.println(instance.getMessage());
//            System.exit(1);
//        }
//
//        if (!instance.logUser("gRider", "M001250015")) {
//            System.err.println(instance.getMessage());
//            System.exit(1);
//        }
//
//        loadCorePrimary();
//
//        String path;
//        String tempPath;
//        if (System.getProperty("os.name").toLowerCase().contains("win")) {
//            path = "D:/GGC_Maven_Systems";
//            tempPath = "D:/temp";
//        } else {
//            path = "/srv/GGC_Maven_Systems";
//            tempPath = "/srv/temp";
//        }
//
//        System.setProperty("sys.default.path.config", path);
//        System.setProperty("sys.default.path.metadata", path + "/config/metadata/new/");
//        System.setProperty("sys.default.path.temp", tempPath);
//
//        if (!loadProperties()) {
//            System.err.println("Unable to load config.");
//            System.exit(1);
//        }
//
//        resetController();
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//        if (conn != null) {
//            try {
//                conn.close();
//            } catch (SQLException e) {
//                System.err.println(e.getMessage());
//            }
//        }
//
//        System.clearProperty("sys.default.path.config");
//        System.clearProperty("sys.default.path.metadata");
//        System.clearProperty("sys.default.path.temp");
//
//        System.clearProperty("sys.main.industry");
//        System.clearProperty("sys.general.industry");
//        System.clearProperty("sys.dept.finance");
//        System.clearProperty("sys.dept.procurement");
//        System.clearProperty("user.selected.industry");
//        System.clearProperty("user.selected.category");
//        System.clearProperty("user.selected.company");
//        System.clearProperty("sys.default.client.token");
//        System.clearProperty("sys.default.access.token");
//        System.clearProperty("sys.default.path.temp.attachments");
//        System.clearProperty("allowed.department");
//    }
//
//    @Test
//    public void test01InitTransaction() throws SQLException, GuanzonException {
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        Assert.assertEquals("Slcm", poController.getSourceCode());
//        Assert.assertNotNull(poController.Master());
//        Assert.assertNotNull(poController.Detail());
//    }
//
//    @Test
//    public void test02NewTransaction() throws CloneNotSupportedException, SQLException, GuanzonException{
//        if (poController == null) {
//            resetController();
//        }
//        Assert.assertNotNull(poController);
//
//        resetController();
//
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//
//        loJSON = poController.NewTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        Assert.assertEquals(psIndustryId, poController.Master().getIndustryId());
//        Assert.assertEquals(psCategorCd, poController.Master().getCategoryCode());
//        Assert.assertEquals(psCompanyId, poController.Master().getCompanyId());
//        Assert.assertEquals(BankApplicationStatus.OPEN, poController.Master().getTransactionStatus());
//    }
//
//    @Test
//    public void test03AddDetailValidationLastRowEmpty() throws CloneNotSupportedException, SQLException, GuanzonException {
//        startNewTransaction();
//
//        if (poController.getDetailCount() == 0) {
//            JSONObject loJSON = poController.AddDetail();
//            Assert.assertEquals("success", loJSON.get("result"));
//        }
//
//        int lastRow = poController.getDetailCount() - 1;
//        poController.Detail(lastRow).setStockId("");
//
//        JSONObject loJSON = poController.AddDetail();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Last row has empty item.", loJSON.get("message"));
//    }
//    
//    @Test
//    public void test06StatusMethodsRequireLoadedTransaction() throws Exception {
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        // Load existing transaction master directly, then force transaction into READY mode.
//        loJSON = poController.OpenTransaction(psSalesCommitmentNo);
//        Assert.assertEquals("success", loJSON.get("result"));
//        setTransactionEditMode(poController);
//
//        // Trigger early status validation branches after loading an existing record.
////        poController.Master().setApprovedDate(instance.getServerDate());
////        poController.Master().setDueDate(instance.getServerDate());
////        poController.Master().setTransactionStatus(BankApplicationStatus.APPROVED);
////        loJSON = poController.ApproveTransaction();
////        Assert.assertEquals("error", loJSON.get("result"));
////        Assert.assertEquals("Record was already approved.", loJSON.get("message"));
//
//        loJSON = poController.DisapproveTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        loJSON = poController.CancelTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//
//    @Test
//    public void test07GetStatusMappings() {
//        Assert.assertEquals("OPEN", poController.getStatus(BankApplicationStatus.OPEN).toUpperCase());
//        Assert.assertEquals("DISAPPROVED", poController.getStatus(BankApplicationStatus.DISAPPROVED).toUpperCase());
//        Assert.assertEquals("APPROVED", poController.getStatus(BankApplicationStatus.APPROVED).toUpperCase());
//        Assert.assertEquals("CANCELLED", poController.getStatus(BankApplicationStatus.CANCELLED).toUpperCase());
//        Assert.assertEquals("UNKNOWN", poController.getStatus("-").toUpperCase());
//    }
//
//    @Test
//    public void test08InitFieldsSetsDefaultValues() throws SQLException, GuanzonException {
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//
//        loJSON = poController.initFields();
//        Assert.assertEquals("success", loJSON.get("result"));
//        Assert.assertEquals(psIndustryId, poController.Master().getIndustryId());
//        Assert.assertEquals(psCategorCd, poController.Master().getCategoryCode());
//        Assert.assertEquals(psCompanyId, poController.Master().getCompanyId());
//        Assert.assertEquals(BankApplicationStatus.OPEN, poController.Master().getTransactionStatus());
//        Assert.assertNotNull(poController.Master().getTransactionDate());
//        Assert.assertNotNull(poController.Master().getAppliedDate());
//    }
//
//    @Test
//    public void test09ReloadDetailKeepsSingleBlankRow() throws Exception {
//        startNewTransaction();
//        Assert.assertEquals(1, poController.getDetailCount());
//
//        poController.ReloadDetail();
//        Assert.assertEquals(1, poController.getDetailCount());
//        Assert.assertTrue(poController.Detail(0).getStockId() == null || "".equals(poController.Detail(0).getStockId()));
//    }
//
//    @Test
//    public void test10WillSaveAssignsDetailLinkage() throws Exception {
//        startNewTransaction();
//
//        poController.populateDetail(psTransNo);
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId(psBankId);
//        poController.Master().setRemarks("TEST Save");
//        poController.Master().setTransactionTotal(110000.00);
//        poController.Master().setSalesAmount(110000.00);
//        poController.Master().setTermCode("M0W1003");
//
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//        poController.Detail(0).setUnitPrice(10000.00);
//
//        JSONObject loJSON = poController.willSave();
//        Assert.assertEquals("success", loJSON.get("result"));
//        Assert.assertEquals(poController.Master().getTransactionNo(), poController.Detail(0).getTransactionNo());
//        Assert.assertEquals(1, poController.Detail(0).getEntryNo());
//    }
//    
//
//    @Test
//    public void test04SaveValidationNoDetail() throws Exception {
//        startNewTransaction();
//        
//        poController.populateDetail(psTransNo);
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId(psBankId);
//        poController.Master().setTermCode("M0W1003");
//        poController.Master().setRemarks("TEST Save");
//        poController.Master().setSourceCode("SInq");
//        poController.Master().setSourceNo(psTransNo);
//        poController.Master().setTransactionTotal(110000.00);
//        poController.Master().setSalesAmount(110000.00);
//        
//        for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
//            poController.Detail(lnCtr).setStockId("");
//        }
//
//        JSONObject loJSON = poController.SaveTransaction();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("No transaction detail to be save.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test05SaveValidationMissingTransactionNo() throws Exception {
//        startNewTransaction();
//
//        poController.populateDetail(psTransNo);
//        if (poController.getDetailCount() == 0) {
//            JSONObject loJSON = poController.AddDetail();
//            Assert.assertEquals("success", loJSON.get("result"));
//        }
//        
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//        poController.Detail(0).setUnitPrice(100000.00);
//
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId(psBankId);
//        poController.Master().setRemarks("TEST Save");
//        poController.Master().setTransactionNo("");
//        poController.Master().setTransactionTotal(110000.00);
//        poController.Master().setSalesAmount(110000.00);
//        poController.Master().setTermCode("M0W1003");
//        // Use business-rule validation directly to avoid hitting DB constraints first.
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Transaction no is not set.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test12SaveValidationMissingDuedate() throws Exception {
//        startNewTransaction();
//
//        poController.populateDetail(psTransNo);
//        
//        poController.Master().setDueDate(null);
//        poController.Master().setRemarks("TEST Save");
//
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        poController.Master().setTermCode("M0W1003");
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Invalid Due Date.", loJSON.get("message"));
//    }
//    
//    @Test
//    public void test12SaveValidationMissingPONumber() throws Exception {
//        startNewTransaction();
//
//        poController.populateDetail(psTransNo);
//        
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("");
//        poController.Master().setRemarks("TEST Save");
//
//        poController.Master().setTermCode("M0W1003");
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("PO Number is not set.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test12SaveValidationMissingPaymentMode() throws Exception {
//        startNewTransaction();
//
//        poController.populateDetail(psTransNo);
//        
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("");
//        poController.Master().setBankId("");
//        poController.Master().setRemarks("TEST Save");
//
//        poController.Master().setTermCode("M0W1003");
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Payment Mode is not set.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test12SaveValidationMissingIssuer() throws Exception {
//        startNewTransaction();
//
//        poController.populateDetail(psTransNo);
//        
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId("");
//        poController.Master().setRemarks("TEST Save");
//
//        poController.Master().setTermCode("M0W1003");
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Bank is not set.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test12SaveValidationMissingSourceCode() throws Exception {
//        startNewTransaction();
//        
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId(psBankId);
//        poController.Master().setRemarks("TEST Save");
//
//        poController.Master().setTermCode("M0W1003");
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Source Code is not set.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test12SaveValidationMissingSourceNo() throws Exception {
//        startNewTransaction();
//        
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId(psBankId);
//        poController.Master().setRemarks("TEST Save");
//        poController.Master().setSourceCode("SInq");
//
//        poController.Master().setTermCode("M0W1003");
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Source No is not set.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test12SaveValidationMissingClient() throws Exception {
//        startNewTransaction();
//        
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId(psBankId);
//        poController.Master().setRemarks("TEST Save");
//        poController.Master().setSourceCode("SInq");
//        poController.Master().setSourceNo(psTransNo);
//
//        poController.Master().setTermCode("M0W1003");
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Client is not set.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test12SaveValidationMissingSalesAmount() throws Exception {
//        startNewTransaction();
//        
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId(psBankId);
//        poController.Master().setRemarks("TEST Save");
//        poController.populateDetail(psTransNo);
//
//        poController.Master().setTermCode("M0W1003");
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Invalid sales amount.", loJSON.get("message"));
//    }
//
//    @Test
//    public void test12SaveValidationMissingTransactionTotal() throws Exception {
//        startNewTransaction();
//        
//        poController.Master().setDueDate(instance.getServerDate());
//        poController.Master().setPONumber("Test");
//        poController.Master().setPaymentMode("0");
//        poController.Master().setBankId(psBankId);
//        poController.Master().setRemarks("TEST Save");
//        poController.populateDetail(psTransNo);
//        poController.Master().setSalesAmount(110000.00);
//
//        poController.Master().setTermCode("M0W1003");
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//
//        JSONObject loJSON = poController.save();
//        Assert.assertEquals("error", loJSON.get("result"));
//        Assert.assertEquals("Invalid transaction total.", loJSON.get("message"));
//    }
//    
//    @Test
//    public void test15IsJSONSuccess() {
//        JSONObject loJSON = new JSONObject();
//        loJSON.put("result", "success");
//        Assert.assertTrue(poController.isJSONSuccess(loJSON));
//
//        loJSON.put("result", "error");
//        Assert.assertFalse(poController.isJSONSuccess(loJSON));
//
//        loJSON.put("result", "warning");
//        Assert.assertTrue(poController.isJSONSuccess(loJSON));
//    }
//
//    @Test
//    public void test16OpenTransactionFromSampleData() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        loJSON = poController.OpenTransaction(psSalesCommitmentNo);
//        Assert.assertEquals("success", loJSON.get("result"));
//        Assert.assertEquals(psSalesCommitmentNo, poController.Master().getTransactionNo());
//    }
//    
//    @Test
//    public void test17LoadSalesInquiry() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        loJSON = poController.loadSalesInquiryList("");
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        if(poController.getSalesInquiryCount() > 0){
//            poController.SalesInquiryList(0);
//        }
//    }
//
//    @Test
//    public void test18GetPriorityUnit() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        loJSON = poController.NewTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        poController.populateDetail(psTransNo);
//        String lsPriority = poController.getPriorityUnit();
//        Assert.assertEquals("", "");
//    }
//
//    @Test
//    public void test19initSQL() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        poController.setTransactionStatus(BankApplicationStatus.OPEN);
//        poController.initSQL();
//        
//        poController.setTransactionStatus("0123");
//        poController.initSQL();
//    }
//    
//    @Test
//    public void test20LoadTransactionList() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        loJSON = poController.loadTransactionList("","");
//        if("error".equals(loJSON.get("result"))){
//            Assert.assertEquals("error", loJSON.get("result"));
//        } else {
//            Assert.assertEquals("success", loJSON.get("result"));
//        }
//        
//        if(poController.getTransactionListCount() > 0){
//            poController.TransactionList(0);
//        }
//    }
//    
//    @Test
//    public void test21SetVatRate() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        loJSON = poController.setVatRate("0.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        loJSON = poController.setVatRate("10.00");
//        Assert.assertEquals("You're not allowed to enter vat rate, no transaction total amount.", loJSON.get("message"));
//        
//        loJSON = poController.Master().setTransactionTotal(2000.00);
//        loJSON = poController.setVatRate("1000.00");
//        Assert.assertEquals("Invalid vat rate. Must be between 0.00 and 100.00", loJSON.get("message"));
//        loJSON = poController.setVatRate("50.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//    
//    @Test
//    public void test22SetVatableSales() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        loJSON = poController.setVatableSales("0.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        loJSON = poController.setVatableSales("10.00");
//        Assert.assertEquals("You're not allowed to enter vatable sales, no transaction total amount.", loJSON.get("message"));
//        
//        loJSON = poController.Master().setTransactionTotal(2000.00);
//        loJSON = poController.setVatableSales("-1000.00");
//        Assert.assertEquals("Invalid vatable sales.", loJSON.get("message"));
//        
//        loJSON = poController.setVatableSales("11000.00");
//        Assert.assertEquals("Vatable sales cannot be greater than transaction total.", loJSON.get("message"));
//        loJSON = poController.setVatableSales("100.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//    
//    @Test
//    public void test23SetVatAmounts() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        loJSON = poController.setVatableAmount("0.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        loJSON = poController.setVatableAmount("10.00");
//        Assert.assertEquals("You're not allowed to enter vat amount, no transaction total amount.", loJSON.get("message"));
//        
//        loJSON = poController.Master().setTransactionTotal(2000.00);
//        loJSON = poController.Master().setVATSale(1000.00);
//        loJSON = poController.setVatableAmount("-1000.00");
//        Assert.assertEquals("Invalid vat amount.", loJSON.get("message"));
//        
//        loJSON = poController.setVatableAmount("11000.00");
//        Assert.assertEquals("Vat amount cannot be greater than vatable sales or transaction total.", loJSON.get("message"));
//        loJSON = poController.setVatableAmount("10.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//    
//    @Test
//    public void test23SetWTaxRate() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        loJSON = poController.setWTaxRate("0.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        loJSON = poController.setWTaxRate("10.00");
//        Assert.assertEquals("You're not allowed to enter tax rate, no transaction total amount.", loJSON.get("message"));
//        
//        loJSON = poController.Master().setTransactionTotal(2000.00);
//        loJSON = poController.setWTaxRate("-1000.00");
//        Assert.assertEquals("Invalid tax rate. Must be between 0.00 and 100.00", loJSON.get("message"));
//        
//        loJSON = poController.setWTaxRate("10.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//
//    @Test
//    public void test23setWithholdingTax() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        loJSON = poController.setWithholdingTax("0.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        loJSON = poController.setWithholdingTax("10.00");
//        Assert.assertEquals("You're not allowed to enter withholding tax, no transaction total amount.", loJSON.get("message"));
//        
//        loJSON = poController.Master().setTransactionTotal(2000.00);
//        loJSON = poController.setWithholdingTax("-1000.00");
//        Assert.assertEquals("Invalid withholding tax.", loJSON.get("message"));
//        
//        loJSON = poController.setWithholdingTax("11000.00");
//        Assert.assertEquals("Withholding tax cannot be greater than transaction total.", loJSON.get("message"));
//        loJSON = poController.setWithholdingTax("10.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//
//    @Test
//    public void test23setVatExempt() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        loJSON = poController.setVatExempt("0.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        loJSON = poController.setVatExempt("10.00");
//        Assert.assertEquals("You're not allowed to enter vat exempt, no transaction total amount.", loJSON.get("message"));
//        
//        loJSON = poController.Master().setTransactionTotal(2000.00);
//        loJSON = poController.setVatExempt("-1000.00");
//        Assert.assertEquals("Invalid vat exempt.", loJSON.get("message"));
//        
//        loJSON = poController.setVatExempt("11000.00");
//        Assert.assertEquals("Vat exempt cannot be greater than transaction total.", loJSON.get("message"));
//        loJSON = poController.setVatExempt("10.00");
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//    
//    @Test
//    public void test24Computefields() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        loJSON = poController.NewTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        poController.populateDetail(psTransNo);
//        loJSON = poController.computeFields(false);
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//    
//    @Test
//    public void test24checkExistingDetail() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        loJSON = poController.NewTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        poController.populateDetail(psTransNo);
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//        poController.Detail(0).setUnitPrice(1000.00);
//        
//        poController.ReloadDetail();
//        loJSON = poController.checkExistingDetail(1, psStockId);
//        Assert.assertEquals("Stock Description already exists in the transaction detail at row 1.", loJSON.get("message"));
//        
//        poController.Detail(0).isReversed(false);
//        poController.ReloadDetail();
//        loJSON = poController.checkExistingDetail(1, psStockId);
//        Assert.assertEquals("error", loJSON.get("result"));
//        
//        poController.Detail(0).isReversed(true);
//        poController.ReloadDetail();
//        loJSON = poController.checkExistingDetail(1, "GK0123000011");
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//    
//    
//    @Test
//    public void test25Setter() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        loJSON = poController.NewTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//        
//        poController.populateDetail(psTransNo);
//        poController.Detail(0).setStockId(psStockId);
//        poController.Detail(0).setQuantity(1);
//        poController.Detail(0).setUnitPrice(1000.00);
//        
//        poController.setClient("test");
//        String lsVal = poController.getClient();
//        Assert.assertEquals("test", lsVal);
//        
//        poController.resetMaster();
//        
//    }
//   
//    
//    @Test
//    public void test26UpdateTransaction() throws Exception {
//        Assert.assertNotNull("No transaction sample transaction available.", psSalesCommitmentNo);
//
//        resetController();
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        loJSON = poController.OpenTransaction("GCO126000003");
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//        loJSON = poController.UpdateTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//        
//    }
//    
//    private static void resetController() {
//        poController = new SalesControllers(instance, null).SalesCommitment();
//        poController.setWithUI(false);
//        poController.setWithParent(true);
//        Assert.assertNotNull(poController);
//    }
//
//    private static void startNewTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
//        if (poController == null) {
//            resetController();
//        }
//
//        JSONObject loJSON = poController.InitTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//
//        poController.setIndustryId(psIndustryId);
//        poController.setCategoryId(psCategorCd);
//        poController.setCompanyId(psCompanyId);
//
//        loJSON = poController.NewTransaction();
//        Assert.assertEquals("success", loJSON.get("result"));
//    }
//
//    private static boolean loadProperties() {
//        try {
//            Properties props = new Properties();
//            props.load(new FileInputStream(System.getProperty("sys.default.path.config") + "/config/cas.properties"));
//
//            System.setProperty("sys.main.industry", props.getProperty("sys.main.industry"));
//            System.setProperty("sys.general.industry", props.getProperty("sys.general.industry"));
//            System.setProperty("sys.dept.finance", props.getProperty("sys.dept.finance"));
//            System.setProperty("sys.dept.procurement", props.getProperty("sys.dept.procurement"));
//            System.setProperty("user.selected.industry", props.getProperty("user.selected.industry"));
//            System.setProperty("user.selected.category", props.getProperty("user.selected.category"));
//            System.setProperty("user.selected.company", props.getProperty("user.selected.company"));
//            System.setProperty("sys.default.client.token", System.getProperty("sys.default.path.config") + "/client.token");
//            System.setProperty("sys.default.access.token", System.getProperty("sys.default.path.config") + "/access.token");
//            System.setProperty("sys.default.path.temp.attachments", props.getProperty("sys.default.path.temp.attachments"));
//            System.setProperty("allowed.department", props.getProperty("allowed.department"));
//            return true;
//        } catch (IOException ex) {
//            return false;
//        }
//    }
//
//    private static void loadCorePrimary() throws IOException, SQLException {
//        conn = instance.getGConnection().getConnection();
//
//        List<String> schemaScripts = new ArrayList<>();
//        List<String> dataScripts = new ArrayList<>();
//
//        schemaScripts.add("sales_inquiry_master_schema");
//        schemaScripts.add("sales_inquiry_detail_schema");
//        schemaScripts.add("sales_commitment_master_schema");
//        schemaScripts.add("sales_commitment_detail_schema");
//        schemaScripts.add("inventory_schema");
//        schemaScripts.add("client_master_schema");
//        schemaScripts.add("salesman_schema");
//        schemaScripts.add("company_schema");
//        schemaScripts.add("industry_schema");
//        schemaScripts.add("category_schema");
//        schemaScripts.add("brand_schema");
//        schemaScripts.add("model_schema");
//        schemaScripts.add("model_variant_schema");
//        schemaScripts.add("color_schema");
//        schemaScripts.add("transaction_status_history_schema");
//
//        dataScripts.add("sales_inquiry_master_data");
//        dataScripts.add("sales_inquiry_detail_data");
//        dataScripts.add("sales_commitment_master_data");
//        dataScripts.add("sales_commitment_detail_data");
//        dataScripts.add("inventory_data");
//        dataScripts.add("client_master_data");
//        dataScripts.add("salesman_data");
//        dataScripts.add("company_data");
//        dataScripts.add("industry_data");
//        dataScripts.add("category_data");
//        dataScripts.add("brand_data");
//        dataScripts.add("model_data");
//        dataScripts.add("model_variant_data");
//        dataScripts.add("color_data");
//        dataScripts.add("transaction_status_history_data");
//
//        for (String schema : schemaScripts) {
//            try (FileReader schemaReader = new FileReader("test-data/" + schema + ".sql")) {
//                RunScript.execute(conn, schemaReader);
//            }
//        }
//
//        for (String data : dataScripts) {
//            try (FileReader dataReader = new FileReader("test-data/" + data + ".sql")) {
//                RunScript.execute(conn, dataReader);
//            }
//        }
//
//    }
////
////    private static String resolveSampleTransactionNo() throws SQLException {
////        String giveawayCode = getFirstTransactionNo();
////        if (giveawayCode != null && !giveawayCode.isEmpty()) {
////            return giveawayCode;
////        }
////
////        // Fallback when sample data files are empty.
////        String fallbackCode = "GCO126000001";
////        String insertMaster = "INSERT INTO sales_transaction_master("
////                + "sGAWayCde, sGAWayDsc, sIndstCdx, sCategrCd, sRemarksx, dFromDate, dThruDate, cTranStat, sEntryByx, dEntryDte, sModified, dModified"
////                + ") VALUES (?, ?, ?, ?, ?, CURRENT_DATE, CURRENT_DATE, '0', ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP)";
////        try (PreparedStatement ps = conn.prepareStatement(insertMaster)) {
////            ps.setString(1, fallbackCode);
////            ps.setString(2, "Sample Giveaways");
////            ps.setString(3, testIndustryId);
////            ps.setString(4, testCategoryId);
////            ps.setString(5, "fallback sample");
////            ps.setString(6, "M001250015");
////            ps.setString(7, "M001250015");
////            ps.executeUpdate();
////        }
////
////        String insertItem = "INSERT INTO sales_transaction_item(sGAWayCde, nEntryNox, sStockIDx, nQuantity, cReversex) VALUES (?, ?, ?, ?, ?)";
////        try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
////            ps.setString(1, fallbackCode);
////            ps.setInt(2, 1);
////            ps.setString(3, testStockId);
////            ps.setInt(4, 1);
////            ps.setString(5, "0");
////            ps.executeUpdate();
////        }
////
////        return fallbackCode;
////    }
////
////    private static String getFirstTransactionNo() throws SQLException {
////        String sql = "SELECT sGAWayCde FROM sales_transaction_master ORDER BY sGAWayCde LIMIT 1";
////        try (PreparedStatement ps = conn.prepareStatement(sql);
////                ResultSet rs = ps.executeQuery()) {
////            if (rs.next()) {
////                return rs.getString("sGAWayCde");
////            }
////        }
////        return null;
////    }
//
//    private static void setTransactionEditMode(SalesCommitment controller) throws Exception {
//        Field field = Transaction.class.getDeclaredField("pnEditMode");
//        field.setAccessible(true);
//        field.setInt(controller, EditMode.READY);
//    }
//
////    @Test
////    public void test17CheckExistingDetailValidation() throws Exception {
////        startNewTransaction();
////
////        // Prepare row 0 as existing detail, then create row 1 for duplicate check.
////        poController.Detail(0).setStockId(psStockId);
////        poController.Detail(0).setQuantity(1);
////
////        JSONObject loJSON = poController.AddDetail();
////        Assert.assertEquals("success", loJSON.get("result"));
////        Assert.assertTrue(poController.getDetailCount() >= 2);
////
////        // Default cReversex is YES, so duplicate stock should return error.
////        JSONObject checkJSON = invokeCheckExistingDetail(poController, 1, psStockId);
////        Assert.assertEquals("error", checkJSON.get("result"));
////        Assert.assertEquals("Stock Description already exists in the transaction detail at row 1.", checkJSON.get("message"));
////        Assert.assertEquals(0, ((Number) checkJSON.get("row")).intValue());
////    }
//
//    private static JSONObject invokeCheckExistingDetail(SalesCommitment controller, int row, String stockId) throws Exception {
//        Method method = SalesGiveaways.class.getDeclaredMethod("checkExistingDetail", int.class, String.class);
//        method.setAccessible(true);
//        return (JSONObject) method.invoke(controller, row, stockId);
//    }
//    
//}
