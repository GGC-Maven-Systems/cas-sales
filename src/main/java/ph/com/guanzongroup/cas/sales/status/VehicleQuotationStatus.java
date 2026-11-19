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
public class VehicleQuotationStatus {
    public static final String OPEN = "0";
    public static final String APPROVED = "1";
    public static final String CANCELLED = "2";
    public static final String VOID = "3";
    public static final String CONFIRMED = "4"; //Client Confirmation
    
    public static class AddOnType  {
        public static final  String LTO = "LTO"; 
        public static final  String TPL = "TPL"; 
        public static final  String COMPREHENSIVE = "COMPREHENSIVE";  
    }
    public static class PaymentForm  {
        public static final  String FINANCING = "1"; 
        public static final  String PO = "2"; 
        public static final  String CASH = "0";  
    }
    public static class ChargeTo  {
        //0=Client, 1=Dealer, 2=Bank, 3=Plant, 4=Free
        public static final  String CLIENT = "0"; 
        public static final  String DEALER = "1"; 
        public static final  String BANK = "2";  
        public static final  String PLANT = "3"; 
        public static final  String FREE = "4"; 
    }
}
