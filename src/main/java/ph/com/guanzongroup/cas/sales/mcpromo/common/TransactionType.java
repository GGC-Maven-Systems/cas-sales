package ph.com.guanzongroup.cas.sales.mcpromo.common;

import java.util.Arrays;
import java.util.List;

public class TransactionType {

    public static final String SALES = "0";
    public static final String JOB_ORDER = "1";
    public static final List<String> TransactionType = Arrays.asList(
            "SALES",
            "JOB ORDER"
    );
}
