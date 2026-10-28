package ph.com.guanzongroup.cas.sales.constant;

/**
 * Static values for the Sales Transaction (Motorcycle Sales) form: module name,
 * combo box sources, status text, messages, table placeholders, attachment
 * settings and formats.
 *
 * PLACEHOLDER VALUES: replace the combo descriptions (and add matching *_CODE
 * arrays, in the same order, like SalesQoutationStatic) with the real ones.
 * Array position = combo box index, so keep the order stable.
 */
public class SalesTransactionStatic {

    private SalesTransactionStatic() {
        // constants only
    }

    // ---------------------------------------------------------------------
    // Module / logging
    // ---------------------------------------------------------------------
    public static final String MODULE_NAME = "Sales Transaction";
    public static final String LOG_SOURCE = "CAS";
    public static final String LOG_PATH_PROPERTY = "sys.default.path.temp";
    public static final String LOG_FILE_NAME = "cas-error.log";

    // ---------------------------------------------------------------------
    // Formats
    // ---------------------------------------------------------------------
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String ATTACH_POSITION_FORMAT = "%d / %d";   // current / total

    // ---------------------------------------------------------------------
    // Combo box sources
    // ---------------------------------------------------------------------
    public static final String[] TRANSACTION_STATUS = {"OPEN", "CONFIRMED", "CANCELLED", "PAID", "DELIVERED"};
    public static final String[] CLIENT_TYPES = {"Individual", "Company"};
    public static final String[] VEHICLE_TYPES = {"Brand New", "Pre-Owned"};
    public static final String[] REGISTRATION_TYPES = {"New Registration",
                                                        "Transfer of Ownership",
                                                        "Registration Renewal",
                                                        "Transfer of Ownership and Renewal"};
    public static final String[] INSURANCE_TYPES = {"CTPL", "Comprehensive"};
    public static final String[] INSURANCE_OPTIONS = {"In-house", "Outside"};
    public static final String[] GENDER_TEXT = {"Male", "Female"};
    public static final String[] PAYMENT_TRAN_TYPES = {"Cash", "Credit"};
    public static final int PAYMENT_CREDIT = 1;   // index of "Credit" in PAYMENT_TRAN_TYPES

    public static final String[] PAYMENT_FORMS = {"Cash", "Check", "Bank Transfer", "Credit Card","Cash Balance"};
    public static final String[] CWT_RATES = {"None", "1%", "2%"};
    public static final String[] ATTACHMENT_TYPES = {"Others", "Customer ID", "Invoice","BIR 2317"};

    // ---------------------------------------------------------------------
    // Form status text
    // ---------------------------------------------------------------------
    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_UPDATING = "UPDATING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_UNKNOWN = "UNKNOWN";

    // ---------------------------------------------------------------------
    // Messages
    // ---------------------------------------------------------------------
    public static final String MSG_UNEXPECTED_VALUE = "Unexpected value: ";
    public static final String MSG_UNDER_DEVELOPMENT = "This buttons is under development.";
    public static final String MSG_TEXTFIELD_UNDER_DEVELOPMENT = "This TextField is under development.";
    public static final String MSG_ADD_CLIENT_UNDER_DEVELOPMENT = "Adding a client is under development.";
    public static final String MSG_ADD_COCLIENT_UNDER_DEVELOPMENT = "Adding a co-client is under development.";

    public static final String MSG_CONFIRM_CANCEL
            = "Do you really want to cancel this record? \nAny data collected will not be kept.";
    public static final String MSG_CONFIRM_CLOSE
            = "Do you really want to close this screen? \nAny unsaved data will not be kept.";
    public static final String MSG_CONFIRM_REMOVE_UNIT = "Do you really want to remove this unit?";
    public static final String MSG_CONFIRM_REMOVE_GIVEAWAY = "Do you really want to remove this giveaway?";
    public static final String MSG_CONFIRM_REMOVE_ATTACHMENT = "Do you really want to remove this attachment?";

    public static final String MSG_DUE_DATE_BEFORE_TRANS_DATE = "The due date cannot be earlier than the transaction date.";
    public static final String MSG_SELECT_ATTACHMENT_TYPE = "Please select an attachment type first.";
    public static final String MSG_ATTACHMENT_DUPLICATE = "This file is already attached.";
    public static final String MSG_ATTACHMENT_INVALID_IMAGE = "The selected file is not a valid image.";
    public static final String MSG_DATE_FROM_AFTER_TO = "The 'Date From' cannot be later than the 'Date To'.";
    public static final String MSG_SELECT_TRANSACTION = "Please select a transaction";
    public static final String MSG_CONFIRM_REMOVE_REGINSUR = "Do you really want to remove this registration/insurance?";

    // ---------------------------------------------------------------------
    // Table placeholders
    // ---------------------------------------------------------------------
    public static final String TBL_NO_UNITS = "No units added";
    public static final String TBL_NO_GIVEAWAYS = "No giveaways added";
    public static final String TBL_NO_INVOICES = "No invoices";
    public static final String TBL_NO_ATTACHMENTS = "No attachments";
    public static final String TBL_NO_TRANSACTIONS = "No transactions";
    public static final String TBL_NO_REGINSUR = "No registration/insurance";

    // ---------------------------------------------------------------------
    // Attachment file chooser
    // ---------------------------------------------------------------------
    public static final String ATTACH_CHOOSER_TITLE = "Select Attachment";
    public static final String ATTACH_FILTER_NAME = "Images";
    public static final String[] ATTACH_FILTER_EXTENSIONS = {"*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"};

    // ---------------------------------------------------------------------
    // UI internals (column reorder lock)
    // ---------------------------------------------------------------------
    public static final String HEADER_ROW_SELECTOR = "TableHeaderRow";
    public static final String REORDER_LOCK_KEY = "reorderLocked";

    public static final String OPEN = "0";
    public static final String CONFIRMED = "1";
    public static final String PAID = "2";
    public static final String CANCELLED = "3";
    public static final String VOID = "4";
    public static final String REPLACED = "5";
    public static final String[] STATUS = {
        "OPEN",
        "CONFIRMED",
        "PAID",
        "CANCELLED",
        "VOID",
        "REPLACED"};

    public static final String[] TRANSACTION_TYPE = {
        "SALES",
        "GIFT"};
}
