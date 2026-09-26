
/*
 * -----------------------------------------------------------------------------
 * Project       : CAS Sales
 * Module        : Quotation
 * Class Name    : SalesQoutationStatic
 *
 * Description   :
 * Contains static constants used by the Quotation module,
 * including quotation transaction status identifiers.
 *
 * Author        : TEEJEI DE CELIS
 * Date Created  : September 26, 2026
 * -----------------------------------------------------------------------------
 */
package ph.com.guanzongroup.cas.sales.status;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Defines static constants used by the Quotation module.
 *
 * <p>
 * This class centralizes the status identifiers used throughout Quotation
 * transactions to eliminate hard-coded values and ensure consistent
 * status handling across the application.
 * </p>
 *
 * <p>
 * The quotation transaction statuses are represented by the following values:
 * </p>
 * <ul>
 *     <li>{@link #OPEN} - The quotation transaction is open and available for processing.</li>
 *     <li>{@link #CONFIRMED} - The quotation transaction has been confirmed.</li>
 *     <li>{@link #CANCELLED} - The quotation transaction has been cancelled.</li>
 *     <li>{@link #VOID} - The quotation transaction has been voided.</li>
 * </ul>
 *
 * @author TEEJEI DE CELIS
 * @date September 26, 2026
 * @module Quotation
 * @since 1.0
 */
public final class SalesQoutationStatic {

    /**
     * Prevents instantiation of this utility class.
     */
    private SalesQoutationStatic() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    // -------------------------------------------------------------------------
    // Quotation Status
    // -------------------------------------------------------------------------

    /**
     * Indicates that the quotation transaction is open and available for processing.
     */
    public static final String OPEN = "0";

    /**
     * Indicates that the quotation transaction has been confirmed.
     */
    public static final String CONFIRMED = "2";

    /**
     * Indicates that the quotation transaction has been cancelled.
     */
    public static final String CANCELLED = "3";

    /**
     * Indicates that the quotation transaction has been voided.
     */
    public static final String VOID = "4";


// -------------------------------------------------------------------------
    // Vehicle Types
    // -------------------------------------------------------------------------

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
}