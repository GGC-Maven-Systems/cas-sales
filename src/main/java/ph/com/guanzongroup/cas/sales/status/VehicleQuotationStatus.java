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
    
    public static class AddOnType  {
        public static final  String LTO = "LTO"; 
        public static final  String TPL = "TPL"; 
        public static final  String COMPREHENSIVE = "COMPREHENSIVE";  
    }
}
