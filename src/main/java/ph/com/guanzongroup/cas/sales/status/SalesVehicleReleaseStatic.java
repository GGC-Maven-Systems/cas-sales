/*
 * -----------------------------------------------------------------------------
 * Project       : CAS Sales
 * Module        : Vehicle Release
 * Class Name    : SalesVehicleReleaseStatic
 *
 * Description   :
 * Contains static constants used by the Vehicle Release module,
 * including transaction status and vehicle type identifiers.
 *
 * Author        : TEEJEI DE CELIS
 * Date Created  : August 7, 2026
 * -----------------------------------------------------------------------------
 */
package ph.com.guanzongroup.cas.sales.status;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Static constants for the Vehicle Release module.
 *
 * <p>
 * This class contains predefined constant values used throughout the
 * Vehicle Release transactions to eliminate the use of hard-coded values
 * in the application.
 * </p>
 *
 * <ul>
 *     <li>{@link #OPEN} - Vehicle release transaction is open.</li>
 *     <li>{@link #CONFIRMED} - Vehicle release transaction has been confirmed.</li>
 *     <li>{@link #APPROVED} - Vehicle release transaction has been approved.</li>
 *     <li>{@link #CANCELLED} - Vehicle release transaction has been cancelled.</li>
 * </ul>
 *
 * <p>
 * Vehicle Type Constants:
 * </p>
 * <ul>
 *     <li>{@link UnitType#BRAND_NEW} - Brand New vehicle.</li>
 *     <li>{@link UnitType#PRE_OWNED} - Pre-Owned vehicle.</li>
 * </ul>
 *
 * @author TEEJEI DE CELIS
 * @date August 7, 2026
 * @module Vehicle Release
 * @since 1.0
 */
public final class SalesVehicleReleaseStatic {
    /**
     * Prevents instantiation of this utility class.
     */
    private SalesVehicleReleaseStatic() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Indicates that the vehicle release transaction is open and awaiting processing.
     */
    public static final String OPEN = "0";

    /**
     * Indicates that the vehicle release transaction has been confirmed.
     */
    public static final String CONFIRMED = "1";

    /**
     * Indicates that the vehicle release transaction has been approved.
     */
    public static final String APPROVED = "2";

    /**
     * Indicates that the vehicle release transaction has been cancelled.
     */
    public static final String CANCELLED = "3";


    /**
     * Vehicle type identifiers used by the Vehicle Release module.
     */
    public static final class UnitType {

        /**
         * Prevents instantiation of this utility class.
         */
        private UnitType() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated.");
        }

        /**
         * Brand New vehicle.
         */
        public static final String BRAND_NEW = "0";

        /**
         * Pre-Owned vehicle.
         */
        public static final String PRE_OWNED = "1";
    }

    /**
     * Vehicle type descriptions used in JavaFX ComboBox controls.
     */
    public static final ObservableList<String> UNIT_TYPE_DESCRIPTION =
            FXCollections.observableArrayList(
                    "Brand New",
                    "Pre-Owned",
                    ""
            );

    /**
     * Vehicle type codes corresponding to {@link UnitType}.
     */
    public static final String[] UNIT_TYPE_CODE = {
            "0",
            "1",
            null
    };
    /**
     * Add-on type identifiers used by the Vehicle Release module.
     */
        public static final class AddOnType {

            /**
             * Prevents instantiation of this utility class.
             */
            private AddOnType() {
                throw new UnsupportedOperationException(
                        "Utility class cannot be instantiated."
                );
            }

            /**
             * Identifies a giveaway add-on.
             */
            public static final String ADD_ON_TYPE_GIVEAWAYS = "0";

            /**
             * Identifies a labor add-on.
             */
            public static final String ADD_ON_TYPE_LABOR = "1";

            /**
             * Identifies a parts add-on.
             */
            public static final String ADD_ON_TYPE_PARTS = "2";

            /**
             * Display description for giveaway add-ons.
             */
            public static final String ADD_ON_DESC_GIVEAWAYS = "Giveaway";

            /**
             * Display description for labor add-ons.
             */
            public static final String ADD_ON_DESC_LABOR = "Labor";

            /**
             * Display description for parts add-ons.
             */
            public static final String ADD_ON_DESC_PARTS = "Parts";
        }
}