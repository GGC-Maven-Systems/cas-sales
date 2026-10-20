package ph.com.guanzongroup.cas.sales.mcpromo.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;

import ph.com.guanzongroup.cas.sales.mcpromo.MCPromoSales;

public class PromoControllers {

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;

    private MCPromoSales poMCPromo;

    public PromoControllers(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }

    public MCPromoSales MCPromoSales() {
        if (poGRider == null) {
            poLogWrapper.severe("PromoControllers.MCPromoSales: Application driver is not set.");
            return null;
        }

        if (poMCPromo != null) {
            return poMCPromo;
        }

        poMCPromo = new MCPromoSales();
        poMCPromo.setApplicationDriver(poGRider);
        poMCPromo.setBranchCode(poGRider.getBranchCode());
        poMCPromo.setVerifyEntryNo(false);
        poMCPromo.setWithParent(false);
        poMCPromo.setLogWrapper(poLogWrapper);
        return poMCPromo;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            poMCPromo = null;

            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }

}
