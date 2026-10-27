
package ph.com.guanzongroup.cas.sales.validator;
import org.guanzon.appdriver.iface.GValidator;

public class MCSalesValidatorFactory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            //no other rules for general purposes
            default: //Main Office
                return new MCSales_General();
        }
    }

}
