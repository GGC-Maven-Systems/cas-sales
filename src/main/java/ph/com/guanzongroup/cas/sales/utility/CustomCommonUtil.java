/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.cas.sales.utility;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.regex.Pattern;

/**
 *
 * @author
 */
/**
 * Utility class providing various common methods for date formatting, text
 * manipulation, and other utilities.
 */
public class CustomCommonUtil {

    /* DATE FORMATTER UTILITY SECTION */
    /**
     * Converts a string representing a date in "yyyy-MM-dd" format to a
     * {@link LocalDate} object.
     *
     * This method takes a date in string format (e.g., "2024-10-01") and
     * converts it to a {@link LocalDate}. It expects the input string to follow
     * the "yyyy-MM-dd" format. If the input cannot be parsed, a
     * {@link DateTimeParseException} will be thrown.
     *
     * @param fsDateValue
     * @param fsPattern The date string in "yyyy-MM-dd" format.
     * @return A {@link LocalDate} object representing the date.
     *
     * <b>Example:</b>
     * <pre>{@code
     * String dateStr = "2024-10-01";
     * LocalDate date = parseDateStringToLocalDate(dateStr, "yyyy-MM-dd");
     * System.out.println(date); // Outputs: 2024-10-01
     * }</pre>
     */
    public static LocalDate parseDateStringToLocalDate(String fsDateValue, String fsPattern) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(fsPattern);
        return LocalDate.parse(fsDateValue, dateFormatter);
    }

    /**
     * Converts a {@link Date} object to a string in "yyyy-MM-dd" format.
     *
     * This method formats a {@link Date} object (e.g., from a timestamp) to a
     * string in the "yyyy-MM-dd" format, commonly used for database entries or
     * display.
     *
     * @param foDateValue The {@link Date} object to be formatted.
     * @return A string representing the date in "yyyy-MM-dd" format.
     *
     * <b>Example:</b>
     * <pre>{@code
     * Date now = new Date();
     * String formattedDate = formatDateToShortString(now);
     * System.out.println(formattedDate); // Outputs: Current date in yyyy-MM-dd format
     * }</pre>
     */
    public static String formatDateToShortString(Date foDateValue) {
        if(foDateValue == null){
            return "1900-01-01";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(foDateValue);
    }

    /**
     * Converts a {@link Date} object to a string in "yyyy-MM-dd" format.
     *
     * This method formats a {@link Date} object (e.g., from a timestamp) to a
     * string in the "yyyy-MM-dd" format, commonly used for database entries or
     * display.
     *
     * @param foLocalDate The {@link Date} object to be formatted.
     * @return A string representing the date in "yyyy-MM-dd" format.
     *
     * <b>Example:</b>
     * <pre>{@code
     * LocalDate now = new LocalDate();
     * String formattedDate = formatLocalDateToShortString(now);
     * System.out.println(formattedDate); // Outputs: Current date in yyyy-MM-dd format
     * }</pre>
     */
    public static String formatLocalDateToShortString(LocalDate foLocalDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return foLocalDate.format(formatter);
    }

    /**
     * Converts a date string from the "MMMM dd, yyyy" format to "yyyy-MM-dd".
     *
     * This method takes a string formatted with the month name (e.g., "October
     * 02, 2024") and converts it into the standard "yyyy-MM-dd" format. If the
     * input string cannot be parsed, it throws a {@link ParseException}.
     *
     * @param fsLongDateString The date string in "MMMM dd, yyyy" format.
     * @return A string representing the date in "yyyy-MM-dd" format.
     * @throws ParseException If the input string cannot be parsed into a valid
     * date.
     *
     * <b>Example:</b>
     * <pre>{@code
     * String dateStr = "October 02, 2024";
     * String formattedDate = convertLongDateStringToShort(dateStr);
     * System.out.println(formattedDate); // Outputs: 2024-10-02
     * }</pre>
     */
    public static String convertLongDateStringToShort(String fsLongDateString) throws ParseException {
        SimpleDateFormat fromUser = new SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        return myFormat.format(fromUser.parse(fsLongDateString));
    }

    /**
     * Formats a {@link Date} object into a string with the month name and day
     * in the "MMMM dd, yyyy" format.
     *
     * This method converts a {@link Date} object into a more human-readable
     * format, where the month name is displayed in full, such as "October 02,
     * 2024".
     *
     * @param foDateValue The {@link Date} object to be formatted.
     * @return A string representing the date in "MMMM dd, yyyy" format.
     *
     * <b>Example:</b>
     * <pre>{@code
     * Date now = new Date();
     * String formattedDate = formatDateWithMonthName(now);
     * System.out.println(formattedDate); // Outputs: October 02, 2024 (or current date)
     * }</pre>
     */
    public static String formatDateWithMonthName(Date foDateValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        return sdf.format(foDateValue);
    }

    /**
     * Adds a text limiter to a {@link TextField} to restrict its length to a
     * specified maximum number of characters.
     *
     * @param foTextField The {@link TextField} to which the limiter will be
     * applied.
     * @param fnMaxLength The maximum length of text allowed.
     */
    @SuppressWarnings("unchecked")
    public static void setTextFieldValueLimit(TextField foTextField, int fnMaxLength) {
        if (foTextField.getProperties().get("textLimiter") != null) {
            foTextField.textProperty().removeListener((ChangeListener<String>) foTextField.getProperties().get("textLimiter"));
        }

        final boolean[] isUpdating = {false};

        ChangeListener<String> textLimiter = (observable, oldValue, newValue) -> {
            if (isUpdating[0]) {
                return;
            }

            if (newValue.length() > fnMaxLength) {
                isUpdating[0] = true;
                foTextField.setText(oldValue);
                isUpdating[0] = false;
            }
        };

        foTextField.textProperty().addListener(textLimiter);
        foTextField.getProperties().put("textLimiter", textLimiter);
    }

    /**
     * Extracts the first initial from a given full name and appends the last
     * name.
     *
     * This method takes a full name in the format "FirstName LastName" and
     * returns a formatted string containing the first letter of the first name
     * followed by a period and the last name.
     *
     * <p>
     * Example:
     * <pre>{@code
     * String formattedName = formatInitialAndLastName("John Doe");
     * System.out.println(formattedName); // Outputs: J. Doe
     * }</pre>
     *
     * @param fsFullName The full name in "FirstName LastName" format.
     * @return A formatted string with the first initial and last name.
     * @throws IllegalArgumentException If the input is null, empty, or does not
     * contain at least two words.
     */
    public static String formatInitialAndLastName(String fsFullName) {
        if (fsFullName == null || fsFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty.");
        }

        String[] nameParts = fsFullName.trim().split("\\s+"); // Handles multiple spaces
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("Full name must contain at least first and last name.");
        }

        String firstNameInitial = nameParts[0].substring(0, 1);
        String lastName = nameParts[nameParts.length - 1];

        return firstNameInitial + ". " + lastName;
    }

    /**
     * @param foTxtFields The {@link TextField} to apply the behavior to.
     *
     * <b>Example:</b>
     * <pre>{@code
     * TextField textField1 = new TextField();
     * TextField textField2 = new TextField();
     * inputDecimalOnly(textField1,textField2);
     * }</pre>
     */
    public static void inputDecimalOnly(TextField... foTxtFields) {
        Pattern pattern = Pattern.compile("[0-9,.]*");
        for (TextField txtField : foTxtFields) {
            if (txtField != null) {
                txtField.setTextFormatter(new TextFormaterUtil(pattern));
            }
        }
    }

    /**
     * @param foTxtFields The {@link TextField} to apply the behavior to.
     *
     * <b>Example:</b>
     * <pre>{@code
     * TextField textField1 = new TextField();
     * TextField textField2 = new TextField();
     * inputIntegersOnly(textField1,textField2);
     * }</pre>
     */
    public static void inputIntegersOnly(TextField... foTxtFields) {
        Pattern pattern = Pattern.compile("[0-9,]*");
        for (TextField txtField : foTxtFields) {
            if (txtField != null) {
                txtField.setTextFormatter(new TextFormaterUtil(pattern));
            }
        }
    }

    /**
     *
     * @param foTxtFields The {@link TextField} to apply the behavior to.
     *
     * <b>Example:</b>
     * <pre>{@code
     * TextField textField1 = new TextField();
     * TextField textField2 = new TextField();
     * inputLettersOnly(textField1,textField2);
     * }</pre>
     */
    public static void inputLettersOnly(TextField... foTxtFields) {
        Pattern pattern = Pattern.compile("[a-zA-Z]*");
        for (TextField txtField : foTxtFields) {
            if (txtField != null) {
                txtField.setTextFormatter(new TextFormaterUtil(pattern));
            }
        }
    }

    /**
     *
     * @param foTxtFields The {@link TextField} to apply the behavior to.
     *
     * <b>Example:</b>
     * <pre>{@code
     * TextField textField1 = new TextField();
     * TextField textField2 = new TextField();
     * inputLettersAndNumbersOnly(textField1,textField2);
     * }</pre>
     */
    public static void inputLettersAndNumbersOnly(TextField... foTxtFields) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
        for (TextField txtField : foTxtFields) {
            if (txtField != null) {
                txtField.setTextFormatter(new TextFormaterUtil(pattern));
            }
        }
    }

    /**
     * Formats an integer or numeric value into a decimal format with two
     * decimal places.
     *
     * <p>
     * This method takes an input object, attempts to convert it to a numeric
     * value, and formats it to a string representation with thousands
     * separators and two decimal places.</p>
     *
     * <p>
     * If the input is invalid (e.g., non-numeric), it returns "0.00" as a
     * default value.</p>
     *
     * @param foObject The input object containing a numeric value (Integer,
     * Double, String, etc.).
     * @return A formatted string representation of the number (e.g.,
     * "1,000.00"). Returns "0.00" if the input is invalid or null.
     *
     * <b>Example Usage:</b>
     * <pre>{@code
     * System.out.println(setIntegerValueToDecimalFormat(1000));      // Outputs: "1,000.00"
     * System.out.println(setIntegerValueToDecimalFormat(1234567));  // Outputs: "1,234,567.00"
     * System.out.println(setIntegerValueToDecimalFormat("5000"));   // Outputs: "5,000.00"
     * System.out.println(setIntegerValueToDecimalFormat("abc"));    // Outputs: "0.00" (Invalid input)
     * System.out.println(setIntegerValueToDecimalFormat(null));     // Outputs: "0.00" (Null input)
     * }</pre>
     */
    public static String setIntegerValueToDecimalFormat(Object foObject) {
        DecimalFormat format = new DecimalFormat("#,##0.00");
        try {
            if (foObject != null) {
                return format.format(Double.parseDouble(String.valueOf(foObject)));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for input - " + foObject);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
        return "0.00";
    }

    public static String setIntegerValueToDecimalFormat(double fnValue) {
        DecimalFormat format = new DecimalFormat("#,##0.00");
        try {
            return format.format(Double.parseDouble(String.valueOf(fnValue)));
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for input - " + fnValue);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
        return "0.00";
    }

    /**
     * Formats a given numeric object into a string using either a 2-decimal or
     * 4-decimal format.
     *
     * <p>
     * This method attempts to parse the given object to a {@code double} and
     * format it using {@link DecimalFormat}. If the object is
     * {@code null} or parsing fails, it returns a default formatted value
     * ("0.00" or "0.0000" depending on the flag).</p>
     *
     * @param foObject the numeric object to be formatted (e.g., {@code Double},
     * {@code Integer}, {@code String} that can be parsed as a number)
     * @param fbIs4Decimal if {@code true}, formats using 4 decimal places
     * ("#,##0.0000"); if {@code false}, formats using 2 decimal places
     * ("#,##0.00")
     * @return the formatted string representation of the number, or
     * "0.0000"/"0.00" if input is null or invalid
     */
    public static String setIntegerValueToDecimalFormat(Object foObject, boolean fbIs4Decimal) {
        String lsDecimalFormat = fbIs4Decimal ? "#,##0.0000" : "#,##0.00";
        DecimalFormat format = new DecimalFormat(lsDecimalFormat);
        try {
            if (foObject != null) {
                return format.format(Double.parseDouble(String.valueOf(foObject)));
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for input - " + foObject);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
        return fbIs4Decimal ? "0.0000" : "0.00";
    }

    /**
     * Formats a given numeric object into a string using either a 2-decimal or
     * 4-decimal format.
     *
     * <p>
     * This method attempts to parse the given object to a {@code double} and
     * format it using {@link DecimalFormat}. If the object is
     * {@code null} or parsing fails, it returns a default formatted value
     * ("0.00" or "0.0000" depending on the flag).</p>
     *
     * @param fnValue
     * @param fbIs4Decimal if {@code true}, formats using 4 decimal places
     * ("#,##0.0000"); if {@code false}, formats using 2 decimal places
     * ("#,##0.00")
     * @return the formatted string representation of the number, or
     * "0.0000"/"0.00" if input is null or invalid
     */
    public static String setIntegerValueToDecimalFormat(double fnValue, boolean fbIs4Decimal) {
        String lsDecimalFormat = fbIs4Decimal ? "#,##0.0000" : "#,##0.00";
        DecimalFormat format = new DecimalFormat(lsDecimalFormat);
        try {
            return format.format(Double.parseDouble(String.valueOf(fnValue)));
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for input - " + fnValue);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
        return fbIs4Decimal ? "0.0000" : "0.00";
    }

    public static String setDecimalValueToIntegerFormat(double fnValue) {
        DecimalFormat format = new DecimalFormat("#,##0");
        try {
            return format.format(Math.round(fnValue)); // You can use Math.round() if you want to round the number
            // return format.format((int) fnValue); // Use this if you want to just drop decimals without rounding
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format for input - " + fnValue);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
        return "0";
    }

    /* DATE FORMATTER UTILITY SECTION */
    /**
     * Converts a string representing a date in "yyyy-MM-dd" format to a
     * {@link LocalDate} object.
     *
     * This method takes a date in string format (e.g., "2024-10-01") and
     * converts it to a {@link LocalDate}. It expects the input string to follow
     * the "yyyy-MM-dd" format. If the input cannot be parsed, a
     * {@link DateTimeParseException} will be thrown.
     *
     * @param fsDateValue
     * @return A {@link LocalDate} object representing the date.
     *
     * <b>Example:</b>
     * <pre>{@code
     * String dateStr = "2024-10-01";
     * LocalDate date = parseDateStringToLocalDate(dateStr, "yyyy-MM-dd");
     * System.out.println(date); // Outputs: 2024-10-01
     * }</pre>
     */
    public static LocalDate parseDateStringToLocalDate(String fsDateValue) {
        if (fsDateValue == null || fsDateValue.trim().isEmpty()) {
            return null;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return LocalDate.parse(fsDateValue.trim(), dateFormatter);
        } catch (DateTimeParseException e) {
            return null; // or log the error if needed
        }
    }

    /**
     * Sets the disabled state for the given nodes.
     *
     * <p>
     * For example, to disable multiple nodes:</p>
     * <pre>
     * {@code
     * Button button = new Button("Submit");
     * TextField textField = new TextField();
     * setDisable(true, button, textField);  // This will disable both the button and the text field
     * }
     * </pre>
     *
     * @param disable if {@code true}, disables the nodes; otherwise, enables
     * them.
     * @param nodes the nodes to be disabled or enabled.
     */
    public static void setDisable(boolean disable, Node... nodes) {
        for (Node node : nodes) {
            if (node != null) {
                node.setDisable(disable);
            }
        }
    }

    /**
     * Sets the visibility state for the given nodes.
     *
     * <p>
     * For example, to hide or show multiple nodes:</p>
     * <pre>
     * {@code
     * Label label = new Label("Hidden Label");
     * Button button = new Button("Click Me");
     * setVisible(false, label, button);  // This will hide both the label and the button
     * }
     * </pre>
     *
     * @param visible if {@code true}, makes the nodes visible; otherwise, hides
     * them.
     * @param nodes the nodes to be shown or hidden.
     */
    public static void setVisible(boolean visible, Node... nodes) {
        for (Node node : nodes) {
            if (node != null) {
                node.setVisible(visible);
            }
        }
    }

    /**
     * Sets the managed state for the given nodes.A managed node is included in
     * the layout calculations, so setting it to {@code false} will exclude it
     * from layout, but the node will still exist in the scene graph.<p>
     * For example, to remove nodes from layout calculations:</p>
     * <pre>
     * {
     *
     * @param managed if {@code true}, includes the nodes in layout; otherwise,
     * excludes them.
     * @param nodes the nodes to be included or excluded from the layout.
     * TextField"); Label label = new Label("Managed Label"); setManaged(false,
     * textField, label); // These nodes will no longer be part of the layout }
     * </pre>
     *
     *
     */
    public static void setManaged(boolean managed, Node... nodes) {
        for (Node node : nodes) {
            if (node != null) {
                node.setManaged(managed);
            }
        }
    }

    /**
     * Sets the provided text value to multiple {@link TextField} elements.
     *
     * @param fsValue the text value to be set for each TextField
     * @param txtFields one or more TextField objects to set the text for
     *
     * Example usage:      <pre>
     * {@code
     * TextField txtField1 = new TextField();
     * TextField txtField2 = new TextField();
     * setText("Sample Text", txtField1, txtField2);
     * }
     * </pre>
     */
    public static void setText(String fsValue, TextField... txtFields) {
        for (TextField txtField : txtFields) {
            if (txtField != null) {
                txtField.setText(fsValue);
            }
        }
    }

    /**
     * Sets the provided text value to multiple {@link TextArea} elements.
     *
     * @param fsValue the text value to be set for each TextArea
     * @param txtAreas one or more TextArea objects to set the text for
     *
     * Example usage:      <pre>
     * {@code
     * TextArea txtAreas1 = new TextArea();
     * TextArea txtAreas2 = new TextArea();
     * setText("Sample Text", txtAreas1, txtAreas2);
     * }
     * </pre>
     */
    public static void setText(String fsValue, TextArea... txtAreas) {
        for (TextArea txtArea : txtAreas) {
            if (txtArea != null) {
                txtArea.setText(fsValue);
            }
        }
    }

    /**
     * Sets the provided value to multiple {@link ComboBox} elements.
     *
     * @param fsValue the value to be set for each ComboBox
     * @param comboBoxes one or more ComboBox objects to set the value for
     *
     * Example usage:      <pre>
     * {@code
     * ComboBox<String> comboBox1 = new ComboBox<>();
     * ComboBox<String> comboBox2 = new ComboBox<>();
     * setValue("Option1", comboBox1, comboBox2);
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static void setValue(String fsValue, ComboBox... comboBoxes) {
        for (ComboBox comboBox : comboBoxes) {
            if (comboBox != null) {
                comboBox.setValue(fsValue);
            }
        }
    }

    /**
     * Sets the provided text value to multiple {@link Label} elements.
     *
     * @param fsValue the text value to be set for each Label
     * @param labels one or more Label objects to set the text for
     *
     * Example usage:      <pre>
     * {@code
     * Label label1 = new Label();
     * Label label2 = new Label();
     * setText("Sample Text", label1, label2);
     * }
     * </pre>
     */
    public static void setText(String fsValue, Label... labels) {
        for (Label label : labels) {
            if (label != null) {
                label.setText(fsValue);
            }
        }
    }

    /**
     * Sets the selected state for a variable number of CheckBox components.
     *
     * @param fbValue The value to set for the selected state (true to select,
     * false to deselect).
     * @param checkBoxes The CheckBox components to modify.
     *
     * <p>
     * Example usage:</p>
     * <pre>
     * CheckBox checkBox1 = new CheckBox("Option 1");
     * CheckBox checkBox2 = new CheckBox("Option 2");
     *
     * // Select all checkboxes
     * UIUtils.setSelected(true, checkBox1, checkBox2);
     *
     * // Deselect all checkboxes
     * UIUtils.setSelected(false, checkBox1, checkBox2);
     * </pre>
     */
    public static void setSelected(boolean fbValue, CheckBox... checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox != null) {
                checkBox.setSelected(fbValue);
            }
        }
    }

    /**
     * Sets the selected state for a variable number of RadioButton components.
     *
     * @param fbValue The value to set for the selected state (true to select,
     * false to deselect).
     * @param radioButtons The RadioButton components to modify.
     *
     * <p>
     * Example usage:</p>
     * <pre>
     * RadioButton radioBtn1 = new RadioButton("Option A");
     * RadioButton radioBtn2 = new RadioButton("Option B");
     *
     * // Select the first radio button
     * UIUtils.setSelected(true, radioBtn1);
     *
     * // Deselect the first radio button
     * UIUtils.setSelected(false, radioBtn1);
     * </pre>
     */
    public static void setSelected(boolean fbValue, RadioButton... radioButtons) {
        for (RadioButton radioBtn : radioButtons) {
            if (radioBtn != null) {
                radioBtn.setSelected(fbValue);
            }
        }
    }

    public static void setDropShadow(AnchorPane anchorPane, StackPane stackPane) {
        // Set the AnchorPane background to transparent
        anchorPane.setStyle("-fx-background-color: transparent;");

        // Set the StackPane background to white
        stackPane.setStyle("-fx-background-color: white;");

        // Configure and apply DropShadow to StackPane
        DropShadow dropShadow = new DropShadow();
        dropShadow.setWidth(23.45);
        dropShadow.setHeight(21.01);
        dropShadow.setRadius(10.62);
        dropShadow.setSpread(0.4);
        dropShadow.setColor(Color.color(0, 0, 0, 0.36)); // Black with 36% opacity

        stackPane.setEffect(dropShadow);

        // Add constraints for StackPane: all sides 10
        AnchorPane.setTopAnchor(stackPane, 10.0);
        AnchorPane.setBottomAnchor(stackPane, 10.0);
        AnchorPane.setLeftAnchor(stackPane, 10.0);
        AnchorPane.setRightAnchor(stackPane, 10.0);
    }

    /**
     *
     *
     * @param foTab the tab from parent tabpane
     * @param foTabPane this is the parent of tab<pre>
     * Example usage:{@code
     * switchToTab(tab1 , TabPane);
     * }
     * </pre>
     */
    public static void switchToTab(Tab foTab, TabPane foTabPane) {
        foTabPane.getSelectionModel().select(foTab);
    }
    //test

}
