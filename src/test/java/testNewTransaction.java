import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testNewTransaction {

    static GRiderCAS poApp;
    static SalesControllers poSalesReservation;

    @BeforeClass
    public static void setUpClass() {
        try {
            System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

            poApp = MiscUtil.Connect();

            poSalesReservation = new SalesControllers(poApp, null);
            poSalesReservation.SalesReservation().setTransactionStatus("0");

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testUnifiedSourceList.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
        public void testNewTransaction() throws CloneNotSupportedException {
            try {
                String branchCd = poApp.getBranchCode();
                String industryId = "01";
                String companyId = "0002";
                String categoryId = "0005";
                String remarks = "Test New Transaciotn";
                String ClientID = "M00125000002";
                String AddressID = "A00124000001";
                String MobileID = "M00125000001";
                String sourceCode = "SI";
                String sourceNo = "M00125000001";
                String stockId = "C0W725000011";
                double quantity = 1.00;
                
                JSONObject loJSON;
                loJSON = poSalesReservation.SalesReservation().InitTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }
                
                loJSON = poSalesReservation.SalesReservation().NewTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }
                    poSalesReservation.SalesReservation().Master().setIndustryID(industryId);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getIndustryID(), industryId);
                    poSalesReservation.SalesReservation().Master().setCompanyID(companyId);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getCompanyID(), companyId);
                    poSalesReservation.SalesReservation().Master().setCategoryCode(categoryId);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getCategoryCode(), categoryId);
                    poSalesReservation.SalesReservation().Master().setBranchCode(branchCd);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getBranchCode(), branchCd);
                    poSalesReservation.SalesReservation().Master().setTransactionDate(poApp.getServerDate()); 
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getTransactionDate(), poApp.getServerDate());
                    poSalesReservation.SalesReservation().Master().setExpectedDate(poApp.getServerDate()); 
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getExpectedDate(), poApp.getServerDate());
                    poSalesReservation.SalesReservation().Master().setClientID(ClientID);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getClientID(), ClientID);
                    poSalesReservation.SalesReservation().Master().setAddressID(AddressID);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getAddressID(), AddressID);
                    poSalesReservation.SalesReservation().Master().setContactID(MobileID);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getContactID(), MobileID);
                    poSalesReservation.SalesReservation().Master().setRemarks(remarks);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getRemarks(), remarks);
                    poSalesReservation.SalesReservation().Master().setEntryDate(poApp.getServerDate()); 
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getEntryDate(), poApp.getServerDate());
                    poSalesReservation.SalesReservation().Master().setSourceCode(sourceCode);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getSourceCode(), sourceCode);
                    poSalesReservation.SalesReservation().Master().setSourceNo(sourceNo);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Master().getSourceNo(), sourceNo);
                    poSalesReservation.SalesReservation().Master().setEntryNo(1);
                        Assert.assertEquals(1, poSalesReservation.SalesReservation().Master().getEntryNo()); 
                    
                    
                    poSalesReservation.SalesReservation().Detail(0).setStockID(stockId);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Detail(0).getStockID(), stockId); 
                    poSalesReservation.SalesReservation().Detail(0).setQuantity(quantity);
//                        Assert.assertEquals( poSalesReservation.SalesReservation().Detail(0).getQuantity(), quantity); 
                    poSalesReservation.SalesReservation().Detail(0).setUnitPrice(10000.0000);
//                        Assert.assertEquals(10000.0000, poSalesReservation.SalesReservation().Detail(0).getUnitPrice()); 
                    poSalesReservation.SalesReservation().Detail(0).setMinimumDown(1000.0000);
//                        Assert.assertEquals(1000.0000, poSalesReservation.SalesReservation().Detail(0).getMinimumDown()); 
                    poSalesReservation.SalesReservation().Detail(0).setClassify("F");
//                        Assert.assertEquals("F", poSalesReservation.SalesReservation().Detail(0).getClassify()); 
                    poSalesReservation.SalesReservation().Detail(0).setApproved(0);
//                        Assert.assertEquals(0, poSalesReservation.SalesReservation().Detail(0).getMinimumDown()); 
                    poSalesReservation.SalesReservation().Detail(0).setNotes(remarks);
                        Assert.assertEquals(poSalesReservation.SalesReservation().Detail(0).getNotes(), remarks);
                        
                        
                    poSalesReservation.SalesReservation().AddDetail();
                    
                    
                    loJSON = poSalesReservation.SalesReservation().SaveTransaction();
                    if (!"success".equals((String) loJSON.get("result"))) {
                        System.err.println((String) loJSON.get("message"));
                        Assert.fail();
                    }
                    
                    

            } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(testNewTransaction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
