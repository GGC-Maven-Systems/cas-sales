package ph.com.guanzongroup.cas.sales.authmatrix;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Controller of MatrixRecipient.fxml. Java 8 compatible. MatrixRecipientDialog
 * loads the FXML, calls setData() and uses the returned summary as the dialog
 * header.
 */
public class MatrixRecipientController implements Initializable {

    private static final double ROW_HEIGHT = 28;
    private static final double HEADER_HEIGHT = 30;

    public static class Row {

        public final String sName;
        public final String sSent;
        public final String sStatus;
        public final String sApproved;

        Row(String name, String sent, String status, String approved) {
            this.sName = name;
            this.sSent = sent;
            this.sStatus = status;
            this.sApproved = approved;
        }
    }

    @FXML
    private TableColumn<Row, String> colApproved;

    @FXML
    private VBox rootBox;
    @FXML
    private Label lblCaption;
    @FXML
    private Label lblBadge;
    @FXML
    private TableView<Row> tbl;
    @FXML
    private TableColumn<Row, String> colName;
    @FXML
    private TableColumn<Row, String> colSent;
    @FXML
    private TableColumn<Row, String> colStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tbl.setPlaceholder(new Label("No recipients."));

        colName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sName));
        colSent.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sSent));
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sStatus));
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
    }

    public VBox getRoot() {
        return rootBox;
    }

    /**
     * Fills the table and the request status badge, sizes the table to the
     * owner window.
     *
     * @return the summary text for the dialog header ("" when there is none)
     */
    public String setData(JSONObject data, Window owner) {
        JSONArray loArray = (JSONArray) data.get("recipients");

        // auth type -> "<desc>: approved a / required r"
        Map<String, String> loSummary = new LinkedHashMap<>();
        tbl.getItems().clear();

        if (loArray != null) {
            for (Object loItem : loArray) {
                JSONObject o = (JSONObject) loItem;
                tbl.getItems().add(new Row(
                        String.valueOf(o.get("sName")),
                        String.valueOf(o.get("sSentStat")),
                        String.valueOf(o.get("sApprStat")),
                        String.valueOf(o.get("dApproved"))));

            }
        }

        // request status badge
        String lsRequest = String.valueOf(data.get("sRequestStatus"));
        lblBadge.setText(lsRequest.toUpperCase());
        lblBadge.getStyleClass().removeAll("status-approved", "status-used", "status-pending");
        if (lsRequest.startsWith("Approved")) {
            lblBadge.getStyleClass().add("status-approved");
        } else if (lsRequest.startsWith("Already")) {
            lblBadge.getStyleClass().add("status-used");
        } else {
            lblBadge.getStyleClass().add("status-pending");
        }

        // size to the main window
        double lnMaxW = 820;
        double lnMaxH = 360;
        if (owner != null) {
            lnMaxW = Math.min(lnMaxW, owner.getWidth() * 0.6);
            lnMaxH = Math.min(lnMaxH, owner.getHeight() * 0.45);
        }
        double lnWidth = Math.max(560, lnMaxW);
        double lnHeight = Math.min(lnMaxH, HEADER_HEIGHT + ROW_HEIGHT * Math.max(tbl.getItems().size(), 3) + 4);
        tbl.setPrefSize(lnWidth, lnHeight);

        StringBuilder loHeader = new StringBuilder();
//        for (String lsLine : loSummary.values()) {
//            if (loHeader.length() > 0) {
//                loHeader.append("\n");
//            }
//            loHeader.append(lsLine);
//        }
        return loHeader.toString();
    }
}
