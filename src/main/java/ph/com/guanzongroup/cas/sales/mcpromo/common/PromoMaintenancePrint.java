package ph.com.guanzongroup.cas.sales.mcpromo.common;

import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;

/**
 *
 * @author Maynard
 */
public class PromoMaintenancePrint {

    public static final String MOBILE_PHONE_REPORT = "PromoBulletin";
    public static final String MOTORCYCLE_REPORT = "PromoBulletin";
    public static final String CAR_REPORT = "PromoBulletin";
    public static final String HOSPITALITY_REPORT = "PromoBulletin";
    public static final String LOS_PEDRITOS_REPORT = "PromoBulletin";
    public static final String GENERAL_REPORT = "PromoBulletin";
    public static final String APPLIANCE_REPORT = "PromoBulletin";

    public static String getJasperReport(String psIndustryCode) {
        switch (psIndustryCode) {
            case "01":
                return MOBILE_PHONE_REPORT;
            case "02":
                return MOTORCYCLE_REPORT;
            case "03":
                return CAR_REPORT;
            case "04":
                return HOSPITALITY_REPORT;
            case "05":
                return LOS_PEDRITOS_REPORT;
            case "06":
                return GENERAL_REPORT;
            case "07":
                return APPLIANCE_REPORT;
            default:
                return GENERAL_REPORT;
        }
    }

}
