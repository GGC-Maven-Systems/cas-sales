package ph.com.guanzongroup.cas.sales.constant;

import java.util.LinkedHashMap;
import java.util.Map;

public class Model_Shop_Type  {

    // Master lookup, mirrors the SQL CASE in searchPromotionByShop
    public static final Map<Integer, String> SHOP_TYPE = new LinkedHashMap<>();
    static {
        SHOP_TYPE.put(0, "3S Shop");
        SHOP_TYPE.put(1, "Multi Brand");
        SHOP_TYPE.put(2, "Big Bike");
    }

    private int entryNo;   // position in the delimited string (used for removal)
    private String code;   

    public Model_Shop_Type(int entryNo, String code) {
        this.entryNo = entryNo;
        this.code = code;
    }

    public int getEntryNo() {
        return entryNo;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        try {
            return SHOP_TYPE.getOrDefault(Integer.parseInt(code), "Other Shop");
        } catch (NumberFormatException e) {
            return "Other Shop";
        }
    }
}
