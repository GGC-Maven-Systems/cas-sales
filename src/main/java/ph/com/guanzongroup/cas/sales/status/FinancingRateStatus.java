/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.status;

/**
 *
 * @author Arsiela
 */
public class FinancingRateStatus {
    public static final String OPEN = "0";
    public static final String ACTIVE = "1";
    public static final String DEACTIVATE = "2";
    public static final String VOID = "3";
    
    public static class StandardRateType  {
        public static final  String INTEREST_RATE = "0"; 
        public static final  String DOWNPAYMENT_RATE = "1";  
    }
    
}
