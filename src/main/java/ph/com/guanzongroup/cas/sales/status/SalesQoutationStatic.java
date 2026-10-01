
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
    public static final String SALES = "2";

    /**
     * Indicates that the quotation transaction has been cancelled.
     */
    public static final String LOST = "3";

    /**
     * Indicates that the quotation transaction has been voided.
     */
    public static final String VOID = "4";

    public static final class STATUS_DESCRIPTION {

        /**
         * Prevents instantiation of this utility class.
         */
        private STATUS_DESCRIPTION() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated.");
        }

        /**
         * DELIVERY.
         */
        public static final String OPEN = "OPEN";

        /**
         * PICK_UP.
         */
        public static final String SALES = "SALES";
        public static final String LOST = "LOST";
        public static final String VOID = "VOID";

    }

    // -------------------------------------------------------------------------
    // Delivery Types
    // -------------------------------------------------------------------------

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

        public static final String EMPTY = "";


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
     * Delivery type codes corresponding to {@link SalesQoutationVersionStatic.DeliveryType}.
     */
    public static final String[] DELIVERY_TYPE_CODE = {
            "0",
            "1",
            ""
    };

    // -------------------------------------------------------------------------
    // Payment Types
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
        /**
         * Term.
         */
        public static final String EMPTY = "";
    }

    /**
     * Payment type descriptions used in JavaFX ComboBox controls.
     */
    public static final ObservableList<String> PAYMENT_TYPE_DESCRIPTION =
            FXCollections.observableArrayList(
                    "Cash",
                    "Cash Balance",
                    "Term",
                    ""
            );

    /**
     * Payment type codes corresponding to {@link SalesQoutationVersionStatic.PaymentType}.
     */
    public static final String[] PAYMENT_TYPE_CODE = {
            "0",
            "1",
            "2",
            ""
    };

    // -------------------------------------------------------------------------
    // Insurance
    // -------------------------------------------------------------------------

    public static final class InsuranceType {

        /**
         * Prevents instantiation of this utility class.
         */
        private InsuranceType() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated.");
        }

        /**
         * YES.
         */
        public static final String YES = "0";

        /**
         * NO.
         */
        public static final String NO = "1";

        public static final String EMPTY = "";


    }

    /**
     * Delivery type descriptions used in JavaFX ComboBox controls.
     */
    public static final ObservableList<String> INSURANCE_DESCRIPTION =
            FXCollections.observableArrayList(
                    "Yes",
                    "No",
                    ""
            );

    /**
     * Delivery type codes corresponding to {@link SalesQoutationVersionStatic.DeliveryType}.
     */
    public static final String[] INSURANCE_CODE = {
            "0",
            "1",
            ""
    };

    public static final class Registration {

        /**
         * Prevents instantiation of this utility class.
         */
        private Registration() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated.");
        }

        /**
         * YES.
         */
        public static final String YES = "0";

        /**
         * NO.
         */
        public static final String NO = "1";

        public static final String EMPTY = "";


    }

    /**
     * Delivery type descriptions used in JavaFX ComboBox controls.
     */
    public static final ObservableList<String> REGISTRATION_DESCRIPTION =
            FXCollections.observableArrayList(
                    "Yes",
                    "No",
                    ""
            );

    /**
     * Delivery type codes corresponding to {@link SalesQoutationVersionStatic.DeliveryType}.
     */
    public static final String[] REGISTRATION_CODE = {
            "0",
            "1",
            ""
    };

    public static final class Category {

        /**
         * Prevents instantiation of this utility class.
         */
        private Category() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated.");
        }

        /**
         * Cash.
         */
            public static final String MC_UNIT = "0000003";

        /**
         * Cash Balance.
         */
        public static final String MC_SPAREPARTS = "0000004";

        /**
         * Term.
         */
        public static final String TERM = "2";
        /**
         * Term.
         */
        public static final String EMPTY = "";
    }
}