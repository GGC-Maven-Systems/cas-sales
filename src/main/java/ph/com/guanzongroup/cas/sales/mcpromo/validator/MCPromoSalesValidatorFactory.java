/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.mcpromo.validator;

import org.guanzon.appdriver.iface.GValidator;

public class MCPromoSalesValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            //no other rules for general purposes
            default: //Main Office
                return new MCPromoSales_General();
        }
    }

}
