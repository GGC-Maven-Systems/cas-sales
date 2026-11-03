package ph.com.guanzongroup.cas.sales.authmatrix;

import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.json.simple.JSONObject;

/**
 * View-only dialog listing the approval-matrix recipients of a transaction and
 * their status. The layout lives in MatrixRecipient.fxml and its logic in
 * MatrixRecipientController; this class only loads them and wraps them in a
 * Dialog. Styled by matrix-recipient.css (same folder). Java 8 compatible.
 *
 * Usage from a button handler: MatrixRecipientDialog.show(((Node)
 * event.getSource()).getScene().getWindow(), "Approval Status", loJSON);
 */
public final class MatrixRecipientDialog {

    private MatrixRecipientDialog() {
    }

    /**
     * Without an owner the dialog is centered on the screen.
     */
    public static void show(final String title, final JSONObject data) {
        show(null, title, data);
    }

    /**
     * Pass the main window as owner so the dialog opens centered over it and is
     * sized relative to it. Safe to call from any thread.
     */
    public static void show(final Window owner, final String title, final JSONObject data) {
        if (Platform.isFxApplicationThread()) {
            build(owner, title, data);
        } else {
            Platform.runLater(() -> build(owner, title, data));
        }
    }

    private static void build(Window owner, String title, JSONObject data) {
        final MatrixRecipientController loController;
        final javafx.scene.Parent loRoot;
        try {
            URL loFxml = MatrixRecipientDialog.class.getResource(
                    "/ph/com/guanzongroup/cas/sales/authmatrix/view/MatrixRecipient.fxml");
            if (loFxml == null) {
                throw new IllegalStateException("MatrixRecipient.fxml not found on the classpath. "
                        + "Put it in the resources folder under ph/com/guanzongroup/cas/sales/authmatrix/ and rebuild the jar.");
            }
            FXMLLoader loLoader = new FXMLLoader(loFxml);
            loRoot = loLoader.load();
            loController = loLoader.getController();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load MatrixRecipient.fxml", ex);
        }

        String lsHeader = loController.setData(data, owner);

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle(title);
        if (owner != null) {
            dlg.initOwner(owner);                       // centers over the main window
            dlg.initModality(Modality.WINDOW_MODAL);
        }
        dlg.setHeaderText(lsHeader.isEmpty() ? title : lsHeader);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // style: matrix-recipient.css targets the dialog pane
        dlg.getDialogPane().getStyleClass().add("/ph/com/guanzongroup/cas/sales/authmatrix/view/css/matrix-dialog");
        URL loCss = MatrixRecipientDialog.class.getResource("matrix-recipient.css");
        if (loCss != null) {
            dlg.getDialogPane().getStylesheets().add(loCss.toExternalForm());
        } else {
            dlg.getDialogPane().setStyle("-fx-background-color: #f4f4f4;");   // css file missing: plain fallback
        }

        dlg.getDialogPane().setContent(loRoot);
        dlg.showAndWait();
    }
}
