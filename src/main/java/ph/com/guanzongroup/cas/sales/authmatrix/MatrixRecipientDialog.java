package ph.com.guanzongroup.cas.sales.authmatrix;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * View-only dialog listing the approval-matrix recipients of a transaction and
 * their status, styled to match the main screen (matrix-recipient.css, placed
 * next to this class in the resources folder). Java 8 compatible.
 *
 * Usage from a button handler: MatrixRecipientDialog.show(((Node)
 * event.getSource()).getScene().getWindow(), "Approval Status", loJSON);
 */
public final class MatrixRecipientDialog {

    private static final double ROW_HEIGHT = 28;
    private static final double HEADER_HEIGHT = 30;

    private MatrixRecipientDialog() {
    }

    public static class Row {

        public final String sAuthDesc;
        public final String sName;
        public final String sLevel;
        public final String sSent;
        public final String sStatus;

        Row(String authDesc, String name, String level, String sent, String status) {
            this.sAuthDesc = authDesc;
            this.sName = name;
            this.sLevel = level;
            this.sSent = sent;
            this.sStatus = status;
        }
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
        JSONArray loArray = (JSONArray) data.get("recipients");

        List<Row> laRows = new ArrayList<>();
        // auth type -> "<desc>: approved a / required r"
        Map<String, String> loSummary = new LinkedHashMap<>();

        if (loArray != null) {
            for (Object loItem : loArray) {
                JSONObject o = (JSONObject) loItem;

                String lsDesc = String.valueOf(o.get("sAuthDesc"));
                laRows.add(new Row(
                        lsDesc,
                        String.valueOf(o.get("sName")),
                        String.valueOf(o.get("nAuthLevl")),
                        String.valueOf(o.get("sSentStat")),
                        String.valueOf(o.get("sApprStat"))));

                loSummary.put(String.valueOf(o.get("sAuthType")),
                        lsDesc + ":  approved " + o.get("nApproved") + " / required " + o.get("nAuthRqrd"));
            }
        }

        // ---------------------------------------------------------- dialog
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle(title);
        if (owner != null) {
            dlg.initOwner(owner);                       // centers over the main window
            dlg.initModality(Modality.WINDOW_MODAL);
        }

        StringBuilder loHeader = new StringBuilder();
        for (String lsLine : loSummary.values()) {
            if (loHeader.length() > 0) {
                loHeader.append("\n");
            }
            loHeader.append(lsLine);
        }
        dlg.setHeaderText(loHeader.length() == 0 ? title : loHeader.toString());
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // ----------------------------------------------------------- style
        dlg.getDialogPane().getStyleClass().add("matrix-dialog");
        URL loCss = MatrixRecipientDialog.class.getResource("matrix-recipient.css");
        if (loCss != null) {
            dlg.getDialogPane().getStylesheets().add(loCss.toExternalForm());
        } else {
            dlg.getDialogPane().setStyle("-fx-background-color: #f4f4f4;");   // css file missing: plain fallback
        }

        // ------------------------------------------------- request status
        String lsRequest = String.valueOf(data.get("sRequestStatus"));
        Label lblCaption = new Label("Request Status");
        lblCaption.getStyleClass().add("caption");

        Label lblBadge = new Label(lsRequest.toUpperCase());
        lblBadge.getStyleClass().add("status-badge");
        if (lsRequest.startsWith("Approved")) {
            lblBadge.getStyleClass().add("status-approved");
        } else if (lsRequest.startsWith("Used")) {
            lblBadge.getStyleClass().add("status-used");
        } else {
            lblBadge.getStyleClass().add("status-pending");
        }

        HBox loStatusBar = new HBox(10, lblCaption, lblBadge);
        loStatusBar.setAlignment(Pos.CENTER_LEFT);

        // ------------------------------------------------------------ table
        TableView<Row> tbl = new TableView<>();
        tbl.setEditable(false);
        tbl.setPlaceholder(new Label("No recipients."));
        tbl.setFixedCellSize(ROW_HEIGHT);
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Row, String> colAuth = new TableColumn<>("Approval Type");
        colAuth.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sAuthDesc));

        TableColumn<Row, String> colName = new TableColumn<>("Approver");
        colName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sName));

        TableColumn<Row, String> colLevel = new TableColumn<>("Level");
        colLevel.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sLevel));
        colLevel.setMaxWidth(70);
        colLevel.setStyle("-fx-alignment: CENTER;");

        TableColumn<Row, String> colSent = new TableColumn<>("Code Sent");
        colSent.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sSent));
        colSent.setMaxWidth(100);
        colSent.setStyle("-fx-alignment: CENTER;");

        TableColumn<Row, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sStatus));
        colStatus.setMaxWidth(110);
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(col -> new TableCell<Row, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("cell-approved", "cell-pending");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add("Approved".equals(item) ? "cell-approved" : "cell-pending");
            }
        });

        tbl.getColumns().add(colAuth);
        tbl.getColumns().add(colName);
        tbl.getColumns().add(colLevel);
        tbl.getColumns().add(colSent);
        tbl.getColumns().add(colStatus);
        tbl.getItems().addAll(laRows);

        // ------------------------------------------- size to the main screen
        double lnMaxW = 820;
        double lnMaxH = 360;
        if (owner != null) {
            lnMaxW = Math.min(lnMaxW, owner.getWidth() * 0.6);
            lnMaxH = Math.min(lnMaxH, owner.getHeight() * 0.45);
        }
        double lnWidth = Math.max(560, lnMaxW);
        double lnHeight = Math.min(lnMaxH, HEADER_HEIGHT + ROW_HEIGHT * Math.max(laRows.size(), 3) + 4);

        tbl.setPrefSize(lnWidth, lnHeight);

        VBox loContent = new VBox(10, loStatusBar, tbl);
        loContent.setPadding(new Insets(6, 0, 0, 0));
        dlg.getDialogPane().setContent(loContent);

        dlg.showAndWait();
    }
}
