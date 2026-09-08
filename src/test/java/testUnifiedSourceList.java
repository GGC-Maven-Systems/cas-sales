import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONArray;
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
public class testUnifiedSourceList {

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
    public void testGetUnifiedPayments() {
        try {
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

            poSalesReservation.SalesReservation().Master().setIndustryID("02");
            poSalesReservation.SalesReservation().Master().setCompanyID("0002");

            // Call the method
            JSONObject result = poSalesReservation.SalesReservation().getUnifiedSource("");

            // Check for null or "no records found"
            if (result == null) {
                System.out.println("No record found (result is null).");
                return; // Exit the test gracefully
            }

            String resultStatus = (String) result.get("result");
            String message = (String) result.get("message");

            if ("error".equals(resultStatus) && "No records found.".equalsIgnoreCase(message)) {
                System.out.println("No record found.");
                return; // Exit the test gracefully
            }

            // Basic assertions for success result
            Assert.assertEquals("Expected result to be success", "success", resultStatus);

            JSONArray dataArray = (JSONArray) result.get("data");

            Assert.assertNotNull("Data array should not be null", dataArray);
            Assert.assertTrue("Data array should not be empty", dataArray.size() > 0);

            System.out.println("Test Passed: " + message);
            int count = 1;
            for (Object item : dataArray) {
                JSONObject record = (JSONObject) item;
                System.out.println("Record #" + count);
                System.out.println("    sTransNox: " + record.get("sTransNox"));
                System.out.println("    dTransact: " + record.get("dTransact"));
                System.out.println("    TransactionType: " + record.get("TransactionType"));
                System.out.println(" -----------------------------------------------------------------");
                count++;
            }

            System.out.println("");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception occurred during test: " + e.getMessage());
        }
    }
}
