package ph.com.guanzongroup.cas.sales.validator;

import org.guanzon.appdriver.iface.GValidator;

public class Sales_Reservation_Validator_Factory {

    public static GValidator make(String industryId) {
        switch (industryId) {
            case "01": //Mobile Phone
                return new Sales_Reservation_Validator_MP();
            case "02": //Motorcycle
                return new Sales_Reservation_Validator_MC();
            case "03": //Vehicle
                return new Sales_Reservation_Validator_Vehicle();
            case "04": //Hospitality
                return new Sales_Reservation_Validator_Hospitality();
            case "05": //Los Pedritos
                return new Sales_Reservation_Validator_LP();
        }
        return null;
    }
}
