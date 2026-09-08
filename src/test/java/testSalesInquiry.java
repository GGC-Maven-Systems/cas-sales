/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.sales.SalesInquiry;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
/**
 *
 * @author Arsiela
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testSalesInquiry {
    
    static GRiderCAS instance;
    static SalesInquiry poSalesInquiryController;
    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poSalesInquiryController = new SalesControllers(instance, null).SalesInquiry();
    }

    @Test
    public void testNewTransaction() {
        String branchCd = instance.getBranchCode();
        String industryId = "02";
        String companyId = "0002";
        String categoryId = "0008";
        String remarks = "this is a test Class 4.";

        JSONObject loJSON;

        try {

            loJSON = poSalesInquiryController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSalesInquiryController.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            try {
                poSalesInquiryController.setIndustryId(industryId);
                poSalesInquiryController.setCompanyId(companyId);
                poSalesInquiryController.setCategoryId(categoryId);

                poSalesInquiryController.initFields();
                poSalesInquiryController.Master().setIndustryId(industryId); 
                Assert.assertEquals(poSalesInquiryController.Master().getIndustryId(), industryId);
                poSalesInquiryController.Master().setCompanyId(companyId); 
                Assert.assertEquals(poSalesInquiryController.Master().getCompanyId(), companyId);
                poSalesInquiryController.Master().setCategoryCode(categoryId); 
                Assert.assertEquals(poSalesInquiryController.Master().getCategoryCode(), categoryId);
                poSalesInquiryController.Master().setPurchaseType("0"); 
                Assert.assertEquals(poSalesInquiryController.Master().getPurchaseType(), "0");
                poSalesInquiryController.Master().setTransactionDate(instance.getServerDate()); 
                Assert.assertEquals(poSalesInquiryController.Master().getTransactionDate(), instance.getServerDate());
                poSalesInquiryController.Master().setBranchCode(branchCd); 
                Assert.assertEquals(poSalesInquiryController.Master().getBranchCode(), branchCd);
                poSalesInquiryController.Master().setSourceCode("0"); 
                Assert.assertEquals(poSalesInquiryController.Master().getSourceCode(), "0");
                poSalesInquiryController.Master().setClientId("GCO126000001");
                Assert.assertEquals(poSalesInquiryController.Master().getClientId(), "GCO126000001");

                poSalesInquiryController.Master().setRemarks(remarks);
                Assert.assertEquals(poSalesInquiryController.Master().getRemarks(), remarks);

                poSalesInquiryController.Detail(0).setStockId("P0W125000002");
                poSalesInquiryController.AddDetail();
//                poSalesInquiryController.Detail(1).setStockId("P0W125000001");

                System.out.println("Industry ID : " + instance.getIndustry());
                System.out.println("Industry : " + poSalesInquiryController.Master().Industry().getDescription());
                System.out.println("Category Code : " + poSalesInquiryController.Master().getCategoryCode());
                System.out.println("TransNox : " + poSalesInquiryController.Master().getTransactionNo());
                
//                poSalesInquiryController.getRequirements("2");
//                
//                poSalesInquiryController.addBankApplication();
//                poSalesInquiryController.BankApplicationsList(0).setApplicationNo("00011");
//                poSalesInquiryController.BankApplicationsList(0).setPaymentMode(poSalesInquiryController.Master().getPurchaseType());
//                poSalesInquiryController.BankApplicationsList(0).setBankId("M00125001");
//                poSalesInquiryController.BankApplicationsList(0).setAppliedDate(instance.getServerDate());
//                poSalesInquiryController.BankApplicationsList(0).setRemarks("Test Bank Application");
                
                
//                poSalesInquiryController.addBankApplication();
//                poSalesInquiryController.BankApplicationsList(1).setApplicationNo("0002");
//                poSalesInquiryController.BankApplicationsList(1).setPaymentMode(poSalesInquiryController.Master().getPurchaseType());
//                poSalesInquiryController.BankApplicationsList(1).setBankId("M00125001");
//                poSalesInquiryController.BankApplicationsList(1).setAppliedDate(instance.getServerDate());
//                poSalesInquiryController.BankApplicationsList(1).setRemarks("Test Bank Application");
                
                loJSON = poSalesInquiryController.SaveTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ExceptionInInitializerError e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Test
    public void testUpdateTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poSalesInquiryController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSalesInquiryController.OpenTransaction("A00125000055");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poSalesInquiryController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            poSalesInquiryController.loadRequirements();
            for(int lnCtr = 0; lnCtr <= poSalesInquiryController.getSalesInquiryRequirementsCount() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getColumn(lnCol) + " ->> " + poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getValue(lnCol));
                }
            }
            
//            String lsCustomerGroup = "0"; //For multiple requirements
            String lsCustomerGroup = "3"; //For single requirements
            poSalesInquiryController.getRequirements(lsCustomerGroup);
            for(int lnCtr = 0; lnCtr <= poSalesInquiryController.getSalesInquiryRequirementsCount() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getColumn(lnCol) + " ->> " + poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getValue(lnCol));
                }
            }
            
            poSalesInquiryController.SalesInquiryRequimentsList(0).isSubmitted(true);
            poSalesInquiryController.SalesInquiryRequimentsList(0).setReceivedBy(instance.getUserID());
            poSalesInquiryController.SalesInquiryRequimentsList(0).setReceivedDate(instance.getServerDate());
            
            poSalesInquiryController.loadBankApplications();
            for(int lnCtr = 0; lnCtr <= poSalesInquiryController.getBankApplicationsCount()- 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poSalesInquiryController.BankApplicationsList(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poSalesInquiryController.BankApplicationsList(lnCtr).getColumn(lnCol) + " ->> " + poSalesInquiryController.BankApplicationsList(lnCtr).getValue(lnCol));
                }
            }
            
            loJSON = poSalesInquiryController.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
        } catch (CloneNotSupportedException | SQLException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (GuanzonException ex) {
            Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }
    
//    @Test
    public void testLoadSalesInquiry() {
        String industryId = "01";
        String companyId = "";
        JSONObject loJSON;
        loJSON = poSalesInquiryController.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        poSalesInquiryController.setIndustryId(industryId);
        loJSON = poSalesInquiryController.loadSalesInquiry(industryId,"","");
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        //retreiving using column index
        for (int lnCtr = 0; lnCtr <= poSalesInquiryController.getSalesInquiryCount()- 1; lnCtr++) {
            try {
                System.out.println("Row No ->> " + lnCtr);
                System.out.println("Transaction No ->> " + poSalesInquiryController.SalesInquiryList(lnCtr).getTransactionNo());
                System.out.println("Transaction Date ->> " + poSalesInquiryController.SalesInquiryList(lnCtr).getTransactionDate());
                System.out.println("Client ->> " + poSalesInquiryController.SalesInquiryList(lnCtr).Client().getCompanyName());
                System.out.println("----------------------------------------------------------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
//    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poSalesInquiryController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poSalesInquiryController.OpenTransaction("A00125000055");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poSalesInquiryController.Master().getColumnCount(); lnCol++){
                System.out.println(poSalesInquiryController.Master().getColumn(lnCol) + " ->> " + poSalesInquiryController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poSalesInquiryController.Master().Branch().getBranchName());
            System.out.println(poSalesInquiryController.Master().Industry().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poSalesInquiryController.Detail().size() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poSalesInquiryController.Detail(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poSalesInquiryController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSalesInquiryController.Detail(lnCtr).getValue(lnCol));
                }
                System.out.println("Category Description " +  poSalesInquiryController.Detail(lnCtr).Category2().getDescription());
                System.out.println("Brand Description " +  poSalesInquiryController.Detail(lnCtr).Brand().getDescription());
            }
            
            poSalesInquiryController.loadRequirements();
            System.out.println("Customer Group " + poSalesInquiryController.getCustomerGroup());
            for(int lnCtr = 0; lnCtr <= poSalesInquiryController.getSalesInquiryRequirementsCount() - 1; lnCtr++){
                System.out.println("------------------------------------------------------------------------------------------------- ");
                System.out.println("Requirements : " + poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).RequirementSource().getDescription());
                System.out.println("Received By : " + poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).SalesPerson().getFullName());
                System.out.println("Received Date : " + poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getReceivedDate());
                System.out.println("------------------------------------------------------------------------------------------------- ");
                for (int lnCol = 1; lnCol <= poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getColumn(lnCol) + " ->> " + poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getValue(lnCol));
                }
                
            }
                
            poSalesInquiryController.loadBankApplications();
            for(int lnCtr = 0; lnCtr <= poSalesInquiryController.getBankApplicationsCount()- 1; lnCtr++){
                System.out.println("Bank : " + poSalesInquiryController.BankApplicationsList(lnCtr).Bank().getBankName());
                for (int lnCol = 1; lnCol <= poSalesInquiryController.BankApplicationsList(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poSalesInquiryController.BankApplicationsList(lnCtr).getColumn(lnCol) + " ->> " + poSalesInquiryController.BankApplicationsList(lnCtr).getValue(lnCol));
                }
            }
            
//            try {
//                poSalesInquiryController.CancelBankApplication("", 0);
//            } catch (ParseException ex) {
//                Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, null, ex);
//            }
            
            System.out.println("Bank : " + (String) loJSON.get("message"));
            
        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }      
    
//    @Test
    public void testConfirmTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poSalesInquiryController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poSalesInquiryController.OpenTransaction("M00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poSalesInquiryController.Master().getColumnCount(); lnCol++){
                System.out.println(poSalesInquiryController.Master().getColumn(lnCol) + " ->> " + poSalesInquiryController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poSalesInquiryController.Master().Branch().getBranchName());
            System.out.println(poSalesInquiryController.Master().Industry().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poSalesInquiryController.Detail().size() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poSalesInquiryController.Detail(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poSalesInquiryController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poSalesInquiryController.Detail(lnCtr).getValue(lnCol));
                }
            }
            
            loJSON = poSalesInquiryController.ConfirmTransaction("test confirm");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
    
//    @Test
    public void loadRequirements(){
        JSONObject loJSON;
        
        try {
            loJSON = poSalesInquiryController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poSalesInquiryController.OpenTransaction("M00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poSalesInquiryController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            poSalesInquiryController.getRequirements("0");
            for(int lnCtr = 0; lnCtr <= poSalesInquiryController.getSalesInquiryRequirementsCount() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getColumn(lnCol) + " ->> " + poSalesInquiryController.SalesInquiryRequimentsList(lnCtr).getValue(lnCol));
                }
            }
        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testSalesInquiry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
