/*
 * -----------------------------------------------------------------------------
 * Project       : CAS Sales
 * Module        : Quotation Version
 * Class Name    : SalesQoutationVersionStatic
 *
 * Description   :
 * Contains static constants used by the Quotation Version module,
 * including quotation version status identifiers.
 *
 * Author        : TEEJEI DE CELIS
 * Date Created  : September 26, 2026
 * -----------------------------------------------------------------------------
 */
package ph.com.guanzongroup.cas.sales.status;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Defines static constants used to represent the status of a Quotation Version
 * transaction.
 *
 * <p>
 * This class centralizes the status identifiers used throughout the Quotation
 * Version module to prevent hard-coded values and ensure consistent status
 * handling across the application.
 * </p>
 *
 * <p>
 * The quotation version statuses are represented by the following values:
 * </p>
 * <ul>
 *     <li>{@link #OPEN} - The quotation version is open and available for processing.</li>
 *     <li>{@link #CONFIRMED} - The quotation version has been confirmed.</li>
 *     <li>{@link #SALES} - The quotation version has been converted to a sales transaction.</li>
 *     <li>{@link #REJECTED} - The quotation version has been rejected.</li>
 *     <li>{@link #VOID} - The quotation version has been voided.</li>
 *     <li>{@link #SUPERCEDED} - The quotation version has been superseded by another version.</li>
 *     <li>{@link #EXPIRED} - The quotation version has exceeded its validity period.</li>
 * </ul>
 *
 * @author TEEJEI DE CELIS
 * @date September 26, 2026
 * @module Quotation
 * @since 1.0
 */
public final class SalesQoutationVersionStatic {

    /**
     * Prevents instantiation of this utility class.
     */
    private SalesQoutationVersionStatic() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    // -------------------------------------------------------------------------
    // Quotation Version Status
    // -------------------------------------------------------------------------

    /**
     * Indicates that the quotation version is open and available for processing.
     */
    public static final String OPEN = "0";

    /**
     * Indicates that the quotation version has been confirmed.
     */
    public static final String CONFIRMED = "1";

    /**
     * Indicates that the quotation version has been converted to a sales transaction.
     */
    public static final String SALES = "2";

    /**
     * Indicates that the quotation version has been rejected.
     */
    public static final String REJECTED = "3";

    /**
     * Indicates that the quotation version has been voided.
     */
    public static final String VOID = "4";

    /**
     * Indicates that the quotation version has been superseded by another version.
     */
    public static final String SUPERCEDED = "5";

    /**
     * Indicates that the quotation version has exceeded its validity period.
     */
    public static final String EXPIRED = "6";




// -------------------------------------------------------------------------
    // Vehicle Types
    // -------------------------------------------------------------------------

    /**
     * Payment type identifiers used by the Sales Quotation Version module.
     */
    public static final class PaymentType {

        /**
         * Prevents instantiation of this utility class.
         */
        private PaymentType() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated.");
        }

        /**
         * Cash.
         */
        public static final String CASH = "0";

        /**
         * Cash Balance.
         */
        public static final String CASH_BALANCE = "1";

        /**
         * Term.
         */
        public static final String TERM = "2";
    }

    /**
     * Payment type descriptions used in JavaFX ComboBox controls.
     */
    public static final ObservableList<String> UNIT_TYPE_DESCRIPTION =
            FXCollections.observableArrayList(
                    "Cash",
                    "Cash Balance",
                    "Term",
                    ""
            );

    /**
     * Payment type codes corresponding to {@link PaymentType}.
     */
    public static final String[] PAYMENT_TYPE_CODE = {
            "0",
            "1",
            "2",
            null
    };

    public static final class DeliveryType {

        /**
         * Prevents instantiation of this utility class.
         */
        private DeliveryType() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated.");
        }

        /**
         * DELIVERY.
         */
        public static final String DELIVERY = "0";

        /**
         * PICK_UP.
         */
        public static final String PICK_UP = "1";


    }

    /**
     * Delivery type descriptions used in JavaFX ComboBox controls.
     */
    public static final ObservableList<String> DELIVERY_TYPE_DESCRIPTION =
            FXCollections.observableArrayList(
                    "Delivery",
                    "Pick Up",
                    ""
            );

    /**
     * Delivery type codes corresponding to {@link DeliveryType}.
     */
    public static final String[] DELIVERY_TYPE_CODE = {
            "0",
            "1",
            null
    };
}