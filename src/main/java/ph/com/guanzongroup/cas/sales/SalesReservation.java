package ph.com.guanzongroup.cas.sales;

import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.services.CashflowModels;
import ph.com.guanzongroup.cas.sales.constant.Sales_Reservation_Static;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Inquiry_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Inquiry_Master;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Reservation_Detail;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Reservation_Master;
import ph.com.guanzongroup.cas.sales.services.SalesControllers;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.validator.Sales_Reservation_Validator_Factory;

import javax.sql.rowset.CachedRowSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SalesReservation extends Transaction {

    List<Model_Sales_Reservation_Master> poSalesReservationMaster;

    List<Model> paDetailRemoved;
    SalesInquiry salesInquiry;
    Model_Sales_Inquiry_Master modelSalesInquiryMaster;

    public JSONObject InitTransaction() throws SQLException, GuanzonException {
        SOURCE_CODE = "srsv";

        poMaster = new SalesModels(poGRider).Sales_Reservation_Master();
        poDetail = new SalesModels(poGRider).Sales_Reservation_Detail();
        salesInquiry = new SalesControllers(poGRider, logwrapr).SalesInquiry();
        SalesModels model = new SalesModels(poGRider);
        modelSalesInquiryMaster = model.SalesInquiryMaster();
        paDetail = new ArrayList<>();
        return initialize();
    }
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryCd = "";

    @Override
    public JSONObject initFields() {
        //Put initial model values here/
        poJSON = new JSONObject();
        try {
            poJSON = new JSONObject();
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryID(psIndustryId);
            Master().setCompanyID(psCompanyId);
            Master().setCategoryCode(psCategoryCd);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(Sales_Reservation_Static.OPEN);

        } catch (SQLException ex) {
            Logger.getLogger(SalesReservation.class
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public void setIndustryID(String industryID) {
        psIndustryId = industryID;
    }

    public void setCompanyID(String companyID) {
        psCompanyId = companyID;
    }

    public void setCategoryCd(String categoryCD) {
        psCategoryCd = categoryCD;
    }

    public JSONObject NewTransaction() throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        resetMaster();
        resetOthers();
        Detail().clear();
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }

//    public JSONObject CancelTransaction(String remarks) throws ParseException, SQLException, CloneNotSupportedException, GuanzonException {
//        poJSON = new JSONObject();
//
//        String lsStatus = Sales_Reservation_Static.CANCELLED;
//        boolean lbConfirm = true;
//
//        if (getEditMode() != EditMode.READY) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No transacton was loaded.");
//            return poJSON;
//        }
//
//        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already cancelled.");
//            return poJSON;
//        }
//
//        //validator
//        poJSON = isEntryOkay(Sales_Reservation_Static.CANCELLED);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
//            poJSON = ShowDialogFX.getUserApproval(poGRider);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            } else {
//                if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
//                    poJSON.put("result", "error");
//                    poJSON.put("message", "User is not an authorized approving officer..");
//                    return poJSON;
//                }
//                setApproving((String) poJSON.get("sUserIDxx"));
//            }
//        }
////        poJSON = setValueToOthers(lsStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//        //check  the user level again then if he/she allow to approve
//        poGRider.beginTrans("UPDATE STATUS", "CancelTransaction", SOURCE_CODE, Master().getTransactionNo());
//
//        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
////        poJSON = saveUpdates(PurchaseOrderStatus.CONFIRMED);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            poGRider.rollbackTrans();
////            return poJSON;
////        }
//
//        poGRider.commitTrans();
//
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//
//        if (lbConfirm) {
//            poJSON.put("message", "Transaction cancelled successfully.");
//        } else {
//            poJSON.put("message", "Transaction cancelled request submitted successfully.");
//        }
//
//        return poJSON;
//    }

    public JSONObject VoidTransaction(String remarks) throws ParseException, SQLException, CloneNotSupportedException, GuanzonException {
        poJSON = new JSONObject();

        String lsStatus = Sales_Reservation_Static.VOID;
        boolean lbConfirm = true;
        String source = Master().getSourceNo();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(Sales_Reservation_Static.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            } else {
                if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "User is not an authorized approving officer..");
                    return poJSON;
                }
                setApproving((String) poJSON.get("sUserIDxx"));
            }
        }
//        if (!source.isEmpty()) {
//            poJSON = setProcessSource(Sales_Reservation_Static.Source.source_inquiry, Master().getSourceNo());
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//        }

        //check  the user level again then if he/she allow to approve
        poGRider.beginTrans("UPDATE STATUS", "VoidTransaction", SOURCE_CODE, Master().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }
        if (!source.isEmpty()) {
//            poJSON = setProcessSource(Sales_Reservation_Static.Source.source_inquiry, Sales_Reservation_Static.CONFIRMED);
            poJSON = setProcessSource(Sales_Reservation_Static.Source.source_inquiry, Master().getSourceNo());
            if (!"success".equals((String) poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
        }

        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancelled request submitted successfully.");
        }

        return poJSON;
    }

    public JSONObject ConfirmTransaction(String remarks) throws ParseException, SQLException, CloneNotSupportedException, GuanzonException {
        poJSON = new JSONObject();

        String lsStatus = Sales_Reservation_Static.CONFIRMED;
        boolean lbConfirm = true;
        String source = Master().getSourceNo();

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(Sales_Reservation_Static.CONFIRMED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (pbWithUI){
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer..");
                        return poJSON;
                    }
                    setApproving((String) poJSON.get("sUserIDxx"));
                }
            }
        }
//        if (!source.isEmpty()) {
//            poJSON = setProcessSource(Sales_Reservation_Static.Source.source_inquiry, Master().getSourceNo());
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//        }
        //check  the user level again then if he/she allow to approve
        poGRider.beginTrans("UPDATE STATUS", "ConfirmTransaction", SOURCE_CODE, Master().getTransactionNo());

        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
        if (!"success".equals((String) poJSON.get("result"))) {
            poGRider.rollbackTrans();
            return poJSON;
        }

//        if (!source.isEmpty()) {
//            salesInquiry.getEditMode();
//            poJSON = saveProcessSource(Sales_Reservation_Static.Source.source_inquiry, Sales_Reservation_Static.CONFIRMED);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                poGRider.rollbackTrans();
//                return poJSON;
//            }
//        }
        poGRider.commitTrans();

        poJSON = new JSONObject();
        poJSON.put("result", "success");

        if (lbConfirm) {
            poJSON.put("message", "Transaction confirmed successfully.");
        } else {
            poJSON.put("message", "Transaction confirmation request submitted successfully.");
        }

        return poJSON;
    }

//    public JSONObject PaidTransaction(String remarks) throws ParseException, SQLException, CloneNotSupportedException, GuanzonException {
//        poJSON = new JSONObject();
//
//        String lsStatus = Sales_Reservation_Static.PAID;
//        boolean lbConfirm = true;
//
//        if (getEditMode() != EditMode.READY) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "No transaction was loaded.");
//            return poJSON;
//        }
//
//        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Transaction was already confirmed.");
//            return poJSON;
//        }
//
//        //validator
//        poJSON = isEntryOkay(Sales_Reservation_Static.PAID);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            return poJSON;
//        }
//
//        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
//            poJSON = ShowDialogFX.getUserApproval(poGRider);
//            if (!"success".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            } else {
//                if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
//                    poJSON.put("result", "error");
//                    poJSON.put("message", "User is not an authorized approving officer..");
//                    return poJSON;
//                }
//                setApproving((String) poJSON.get("sUserIDxx"));
//            }
//        }
////        poJSON = setValueToOthers(lsStatus);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            return poJSON;
////        }
//        //check  the user level again then if he/she allow to approve
//        poGRider.beginTrans("UPDATE STATUS", "PaidTransaction", SOURCE_CODE, Master().getTransactionNo());
//
//        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm, true);
//        if (!"success".equals((String) poJSON.get("result"))) {
//            poGRider.rollbackTrans();
//            return poJSON;
//        }
////        poJSON = saveUpdates(PurchaseOrderStatus.CONFIRMED);
////        if (!"success".equals((String) poJSON.get("result"))) {
////            poGRider.rollbackTrans();
////            return poJSON;
////        }
//
//        poGRider.commitTrans();
//
//        poJSON = new JSONObject();
//        poJSON.put("result", "success");
//
//        if (lbConfirm) {
//            poJSON.put("message", "Transaction Paid successfully.");
//        } else {
//            poJSON.put("message", "Transaction Paid request submitted successfully.");
//        }
//
//        return poJSON;
//    }

    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (Detail(getDetailCount() - 1).getStockID().isEmpty()) {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "Last row has empty item.");
            return poJSON;
        }

        return addDetail();
    }


    /*Search Master References*/
    public JSONObject SearchBranch(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus("1");

        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setBranchCode(object.getModel().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject SearchClient(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (value.isEmpty()) {
            Master().setClientID(null);
        }
        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType("1");
        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = object.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setClientID(object.Master().getModel().getClientId());
            Master().setAddressID(object.ClientAddress().getModel().getAddressId());
//            Master().setContactID(object.Mobile().getModel().getClientId());
        }

        return poJSON;
    }

    public JSONObject SearchBrand(String value, boolean byCode, int row) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Brand brand = new ParamControllers(poGRider, logwrapr).Brand();
        brand.getModel().setRecordStatus(RecordStatus.ACTIVE);

        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = brand.searchRecord(value, byCode, Master().getIndustryID());

        if ("success".equals((String) poJSON.get("result"))) {
            Detail(row).setBrandId(brand.getModel().getBrandId());
        }

        return poJSON;
    }

    public JSONObject SearchModel(String value, boolean byCode, int row)
            throws SQLException, GuanzonException, NullPointerException, CloneNotSupportedException {
        Inventory object = new InvControllers(poGRider, logwrapr).Inventory();
        object.getModel().setRecordStatus(RecordStatus.ACTIVE);

        String brand = (Detail(row).getBrandId() != null && !Detail(row).getBrandId().isEmpty()) ? Detail(row).getBrandId() : null;
        String industry = Master().getIndustryID().isEmpty() ? null : Master().getIndustryID();
        String category = Master().getCategoryCode();
        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = object.searchRecord(
                value,
                byCode,
                null,
                brand,
                industry,
                category
        );

        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                if (lnRow != row) {
                    if ((Detail(lnRow).getStockID().equals("") || Detail(lnRow).getStockID() == null)
                            || (Detail(lnRow).getStockID().equals(object.getModel().getStockId()))) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Barcode: " + object.getModel().getDescription() + " already exist in table at row " + (lnRow + 1) + ".");
                        poJSON.put("tableRow", lnRow);
                        return poJSON;
                    }
                }
            }

            Detail(row).setStockID(object.getModel().getStockId());
            Detail(row).setUnitPrice(object.getModel().getCost().doubleValue());
            if (row == getDetailCount() - 1) {
                AddDetail();
            }
        }
        return poJSON;
    }

    public JSONObject SearchBarcode(String value, boolean byCode, int row)
            throws ExceptionInInitializerError, SQLException, GuanzonException, CloneNotSupportedException, NullPointerException {

        Inventory object = new InvControllers(poGRider, logwrapr).Inventory();
        object.setRecordStatus(RecordStatus.ACTIVE);

        String brand = (Detail(row).getBrandId() != null && !Detail(row).getBrandId().isEmpty()) ? Detail(row).getBrandId() : null;
        String industry = Master().getIndustryID().isEmpty() ? null : Master().getIndustryID();
        String category = Master().getCategoryCode();
        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = object.searchRecord(
                value,
                byCode,
                null,
                brand,
                industry,
                category
        );

        if ("success".equals((String) poJSON.get("result"))) {
//            for (int lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
//                if (lnRow != row) {
//                    if ((Detail(lnRow).getStockID().equals("") || Detail(lnRow).getStockID() == null)
//                            || (Detail(lnRow).getStockID().equals(object.getModel().getStockId()))) {
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "Barcode: " + object.getModel().getDescription() + " already exist in table at row " + (lnRow + 1) + ".");
//                        poJSON.put("tableRow", lnRow);
//                        return poJSON;
//                    }
//                }
//            }

            Detail(row).setStockID(object.getModel().getStockId());
            Detail(row).setUnitPrice(object.getModel().getCost().doubleValue());
            if (row == getDetailCount() - 1) {
                AddDetail();
            }
        }
        return poJSON;
    }

    public JSONObject SearchDescription(String value, boolean byCode, int row)
            throws ExceptionInInitializerError, SQLException, GuanzonException, CloneNotSupportedException, NullPointerException {

        Inventory object = new InvControllers(poGRider, logwrapr).Inventory();
        object.setRecordStatus(RecordStatus.ACTIVE);

        String brand = (Detail(row).getBrandId() != null && !Detail(row).getBrandId().isEmpty()) ? Detail(row).getBrandId() : null;
        String industry = Master().getIndustryID().isEmpty() ? null : Master().getIndustryID();
        String category = Master().getCategoryCode();
        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = object.searchRecord(
                value,
                byCode,
                null,
                brand,
                industry,
                category
        );

        if ("success".equals((String) poJSON.get("result"))) {
            for (int lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++) {
                if (lnRow != row) {
                    if ((Detail(lnRow).getStockID().equals("") || Detail(lnRow).getStockID() == null)
                            || (Detail(lnRow).getStockID().equals(object.getModel().getStockId()))) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Barcode: " + object.getModel().getDescription() + " already exist in table at row " + (lnRow + 1) + ".");
                        poJSON.put("tableRow", lnRow);
                        return poJSON;
                    }
                }
            }

            Detail(row).setStockID(object.getModel().getStockId());
            Detail(row).setUnitPrice(object.getModel().getCost().doubleValue());
            if (row == getDetailCount() - 1) {
                AddDetail();
            }
        }
        return poJSON;
    }

    public JSONObject SearchInventory(String value, int row, String Banks, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Inventory object = new InvControllers(poGRider, logwrapr).Inventory();
        object.setRecordStatus("1");

        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Detail(row).setStockID(object.getModel().getStockId());
            Detail(row).setUnitPrice(object.getModel().getCost().doubleValue());
            Detail(row).setClassify("F");

        }

        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE = "SELECT "
                + " a.sTransNox, "
                + " a.dTransact, "
                + " c.sBranchNm, "
                + " a.sClientID, "
                + " d.sCompnyNm "
                + " FROM Sales_Reservation_Master a "
                + " LEFT JOIN Branch c ON LEFT(a.sTransNox, 4) = c.sBranchCd "
                + " LEFT JOIN Client_Master d ON a.sClientID = d.sClientID "
                + " LEFT JOIN Client_Address e ON d.sClientID = e.sClientID "
                + " LEFT JOIN Client_Mobile f ON d.sClientID = f.sClientID "
                + ", Sales_Reservation_Detail b ";
    }

    public JSONObject SearchTransaction(String fsValue) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        String lsBranch = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.sCategrCd LIKE " + SQLUtil.toSQL("%" + Master().getCategoryCode()));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        if (!fsValue.isEmpty()) {
            if (Master().getClientID() == null) {
                lsSQL = lsSQL + " AND d.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%");
            } else {
                lsSQL = lsSQL + " AND a.sClientID = " + SQLUtil.toSQL(Master().getClientID());
            }
        } else {
            lsSQL = lsSQL + " AND d.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue + "%");
        }

        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        if (!poGRider.isMainOffice() || !poGRider.isWarehouse()) {
            lsSQL = lsSQL + " AND a.sBranchCd LIKE " + SQLUtil.toSQL(poGRider.getBranchCode());
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction Date»Transaction No»Customer Name»Branch",
                "a.dTransact»a.sTransNox»d.sCompnyNm»c.sBranchNm",
                "a.dTransact»a.sTransNox»d.sCompnyNm»ecsBranchNm",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject SearchTransactionbyFilter(String fsValue, boolean fsByCode) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        String lsBranch = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCompnyID = " + SQLUtil.toSQL(Master().getCompanyID()),
                " a.sCategrCd LIKE " + SQLUtil.toSQL("%" + Master().getCategoryCode()));

        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        if (fsByCode) {
            if (!fsValue.isEmpty()) {
                lsSQL = lsSQL + " AND a.sTransNox = " + SQLUtil.toSQL(fsValue);
            } else {
                lsSQL = lsSQL + "  AND a.sTransNox LIKE '%' ";
            }
        } else {
            if (!fsValue.isEmpty()) {
                lsSQL = lsSQL + " AND d.sCompnyNm LIKE " + SQLUtil.toSQL("%" + fsValue);
            } else {
                lsSQL = lsSQL + " AND d.sCompnyNm LIKE '%'";
            }
        }

        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        if (!poGRider.isMainOffice() || !poGRider.isWarehouse()) {
            lsSQL = lsSQL + " AND a.sBranchCd LIKE " + SQLUtil.toSQL(poGRider.getBranchCode());
        }

        lsSQL = lsSQL + " GROUP BY a.sTransNox";
        System.out.println("SQL EXECUTED: " + lsSQL);
        if(!pbWithUI){
            poJSON.put("result", "success");
            poJSON.put("message", "withUI");
            return poJSON;
        }
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                fsValue,
                "Transaction Date»Transaction No»Customer Name»Branch",
                "a.dTransact»a.sTransNox»d.sCompnyNm»c.sBranchNm",
                "a.dTransact»a.sTransNox»d.sCompnyNm»ecsBranchNm",
                fsByCode ? 1 : 2);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    /*End - Search Master References*/
    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public Model_Sales_Reservation_Master Master() {
        return (Model_Sales_Reservation_Master) poMaster;
    }

    @Override
    public Model_Sales_Reservation_Detail Detail(int row) {
        return (Model_Sales_Reservation_Detail) paDetail.get(row);
    }

    @Override
    public JSONObject willSave() throws SQLException, GuanzonException, CloneNotSupportedException {

        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            Object quantityObj = item.getValue("nQuantity");

            if (quantityObj != null) {
                double quantity = ((Number) quantityObj).doubleValue();
                if (quantity <= 0.00) {
                    switch (getEditMode()) {
                        case EditMode.ADDNEW:
                            detail.remove();
                            break;
                        case EditMode.UPDATE:
                            paDetailRemoved.add(item);
                            item.setValue("cReversex", "0");
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
            } else {
                paDetailRemoved.add(item); // track removed
                detail.remove();
            }
        }

        // Re-number remaining details
        for (int lnCtr = 0; lnCtr < getDetailCount(); lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr + 1);
            Detail(lnCtr).setModifiedDate(poGRider.getServerDate());
        }

        Master().setModifiedDate(poGRider.getServerDate());
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(Sales_Reservation_Static.OPEN);
    }

    @Override
    public JSONObject saveOthers() {
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }

    public JSONObject validateDetails() {
        poJSON = new JSONObject();
        int detailCount = getDetailCount();

        if (detailCount == 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "Reservation cannot be saved without any detail. Please add an item.");
            return poJSON;
        }

        boolean allZeroQty = true;
        double quantity;
        for (int i = 0; i < detailCount; i++) {
            // Directly use Detail(i) since we are inside the same class
            quantity = Detail(i).getQuantity();
            if (detailCount == 1) {

                String notes = Detail(i).getNotes();
                String stock = Detail(i).getStockID();

                // Check for invalid quantity
                if (quantity <= Sales_Reservation_Static.DefaultValues.default_zero_quantity_double) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Reservation cannot be saved. Please verify the quantity.");
                    return poJSON;
                }

                if (stock == null || stock.isEmpty()) // Check for missing notes
                {
                    if (notes == null || notes.trim().isEmpty()) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Reservation cannot be saved. Notes are required.");
                        return poJSON;
                    }
                }
            }
            if (Detail(i).getQuantity() > 0) {
                allZeroQty = false; // at least one valid quantity
            }
        }

        if (allZeroQty) {
            poJSON.put("result", "error");
            poJSON.put("message", "Reservation cannot be saved with all details having zero quantity.");
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = Sales_Reservation_Validator_Factory.make(Master().getIndustryID());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(Master());

        poJSON = loValidator.validate();
        return poJSON;
    }

    public JSONObject getUnifiedSource(String ClientID) throws SQLException, GuanzonException {
        StringBuilder lsSQL = new StringBuilder("SELECT * FROM (");
        boolean hasCondition = false;
        System.out.println("MASTER : " + psIndustryId + " " + Master().getIndustryID());

        if (hasCondition) {
            lsSQL.append(" UNION ALL ");
        }
        lsSQL.append(
                "SELECT "
                + " a.sTransNox, "
                + " a.dTransact, "
                + " 'Inquiry' AS source, "
                + " a.sIndstCdx AS Industry, "
                + " a.sCompnyID AS Company, "
                + " a.sCategrCd AS Category "
                + " FROM Sales_Inquiry_Master a "
                + " WHERE a.cTranStat = '" + Sales_Reservation_Static.CONFIRMED + "' "
                + " AND a.cProcessd = '" + Sales_Reservation_Static.OPEN + "' "
                + " AND a.sIndstCdx = '" + Master().getIndustryID() + "' "
                + " AND a.sCompnyID = '" + Master().getCompanyID() + "'"
                + " AND a.sCategrCd = '" + Master().getCategoryCode() + "'"
                + " AND a.sClientID LIKE '" + (Master().getClientID() == null || Master().getClientID().isEmpty() ? "%" : Master().getClientID()) + "'"
        );
        hasCondition = true;

        if (hasCondition) {
            lsSQL.append(" UNION ALL ");
        }
        lsSQL.append(
                "SELECT "
                + " b.sTransNox, "
                + " b.dTransact, "
                + " 'Quotation' AS source, "
                + " b.sIndstCdx AS Industry, "
                + " b.sCompnyID AS Company, "
                + " b.sCategrCd AS Category "
                + " FROM Sales_Quotation_Master b "
                + " WHERE b.cTranStat = '" + Sales_Reservation_Static.CONFIRMED + "' "
                + " AND b.sIndstCdx = '" + psIndustryId + "' "
                + " AND b.sCompnyID = '" + Master().getCompanyID() + "'"
                + " AND b.sCategrCd = '" + Master().getCategoryCode() + "'"
                + " AND b.sClientID LIKE '" + (Master().getClientID() == null || Master().getClientID().isEmpty() ? "%" : Master().getClientID()) + "'"
        );
        hasCondition = true;

        lsSQL.append(") AS CombinedResults ORDER BY dTransact ASC");

        System.out.println("Executing SQL: " + lsSQL.toString());

        ResultSet loRS = poGRider.executeQuery(lsSQL.toString());
        JSONArray dataArray = new JSONArray();
        JSONObject loJSON = new JSONObject();

        if (loRS == null) {
            loJSON.put("result", "error");
            loJSON.put("message", "Query execution failed.");
            return loJSON;
        }

        try {
            int lnctr = 0;

            while (loRS.next()) {
                JSONObject record = new JSONObject();
                record.put("sTransNox", loRS.getString("sTransNox"));
                record.put("dTransact", loRS.getDate("dTransact"));
                record.put("source", loRS.getString("source"));

                dataArray.add(record);
                lnctr++;
            }

            if (lnctr > 0) {
                loJSON.put("result", "success");
                loJSON.put("message", "Record(s) loaded successfully.");
                loJSON.put("data", dataArray);
            } else {
                loJSON.put("result", "error");
                loJSON.put("message", "No records found.");
                loJSON.put("data", new JSONArray());
            }

            MiscUtil.close(loRS);

        } catch (SQLException e) {
            loJSON.put("result", "error");
            loJSON.put("message", e.getMessage());
        }

        return loJSON;
    }

    public JSONObject addSourceToSalesRsvDetail(String transactionNo, String source)
            throws CloneNotSupportedException, SQLException, GuanzonException {

        poJSON = new JSONObject();
        int insertedCount = 0;
        int detailCount = 0;

        switch (source) {
            case Sales_Reservation_Static.Source.source_inquiry:
                SalesInquiry salesInquiry = new SalesControllers(poGRider, logwrapr).SalesInquiry();

                poJSON = salesInquiry.InitTransaction();
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                poJSON = salesInquiry.OpenTransaction(transactionNo);
                if (!"success".equals(poJSON.get("result"))) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No records found.");
                    return poJSON;
                }

                detailCount = salesInquiry.getDetailCount();
//                String currentPayeeID = salesInquiry.Master().getPayeeID();

                for (int i = 0; i < detailCount; i++) {
                    String salesStockId = salesInquiry.Detail(i).getStockId();
                    String salesSourceNo = salesInquiry.Detail(i).getTransactionNo();
                    String salesSourcecode = salesInquiry.getSourceCode();
                    String salesModel = "";
                    String salesModelVariant = "";
                    String salesModelColor = "";

                    if (salesInquiry.Detail(i).getStockId() == null || salesInquiry.Detail(i).getStockId().isEmpty()) {
//                        poJSON.put("result", "error");
//                        poJSON.put("message", "Stock ID is not yet available");
//                        return poJSON;
                        salesStockId = "";

                    }
                    for (int j = 0; j < getDetailCount(); j++) {
                        if (salesStockId.equals(Detail(j).getStockID())
                                && salesSourceNo.equals(Master().getSourceNo())
                                && salesSourcecode.equals(Master().getSourceNo())) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "Stock ID is already exist in the detail");
                            return poJSON;
                        }
                    }
                    if (!Master().getSourceNo().isEmpty()) {
                        if (!Master().getSourceNo().equals(salesInquiry.Master().getTransactionNo())) {
                            poJSON.put("ischange", "true");
                            poJSON.put("result", "error");
                            poJSON.put("message", "Existing data will be cleared when adding a new inquiry or quotation. \n"
                                    + " Do you want to proceed?");
                            return poJSON;
                        }
                    }

                    Master().setClientID(salesInquiry.Master().getClientId());
                    Master().setAddressID(salesInquiry.Master().getAddressId());
                    Master().setContactID(salesInquiry.Master().getContactId());
                    Master().setSourceNo(salesInquiry.Master().getTransactionNo());
                    Master().setSourceCode(salesInquiry.Master().getSourceCode());

                    AddDetail();
                    int newIndex = getDetailCount() - 1;
                    Detail(newIndex).setStockID(salesStockId);
                    Detail(newIndex).setUnitPrice(salesInquiry.Detail(i).Inventory().getCost().doubleValue());
                    Detail(newIndex).setMinimumDown(salesInquiry.Detail(i).Inventory().getCost().doubleValue());
                    Detail(newIndex).setClassify("F");

                    salesModel = Objects.toString(salesInquiry.Detail(i).Model().getDescription(), "");
                    salesModelVariant = Objects.toString(salesInquiry.Detail(i).ModelVariant().getDescription(), "");
                    salesModelColor = Objects.toString(salesInquiry.Detail(i).Color().getDescription(), "");

                    String notes = Stream.of(salesModel, salesModelVariant, salesModelColor)
                            .filter(s -> s != null && !s.isEmpty())
                            .collect(Collectors.joining(", "));

                    Detail(newIndex).setNotes(notes);
                    insertedCount++;
                }
                break;
            default:
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid source type.");
                return poJSON;
        }

        poJSON = new JSONObject();
        if (insertedCount == 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "The selected transaction has already been inserted.");
        } else {
            poJSON.put("result", "success");
            poJSON.put("message", insertedCount + " detail(s) added successfully.");
        }

        return poJSON;
    }

    public JSONObject getReservationList(String fsTransactionNo, String fsCustomer) throws SQLException, GuanzonException {
        JSONObject loJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat.length() > 1) {
            for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
            }
            lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
        } else {
            lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
        }

        initSQL();
        String lsFilterCondition = String.join(" AND ", "a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryID()),
                " a.sCategrCd = " + SQLUtil.toSQL(psCategoryCd),
                " a.sClientID LIKE " + SQLUtil.toSQL("%" + fsCustomer),
                " a.sTransNox  LIKE " + SQLUtil.toSQL("%" + fsTransactionNo),
                " a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, lsFilterCondition);

        lsSQL = MiscUtil.addCondition(lsSQL, lsFilterCondition);
        if (!psTranStat.isEmpty()) {
            lsSQL = lsSQL + lsTransStat;
        }
        lsSQL = lsSQL + " GROUP BY  a.sTransNox"
                + " ORDER BY dTransact ASC";
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        int lnCtr = 0;
        if (MiscUtil.RecordCount(loRS) >= 0) {
            poSalesReservationMaster = new ArrayList<>();
            while (loRS.next()) {
                // Print the result set
                System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                System.out.println("dTransact: " + loRS.getDate("dTransact"));
                System.out.println("------------------------------------------------------------------------------");

                poSalesReservationMaster.add(SalesReservationMasterList());
                poSalesReservationMaster.get(poSalesReservationMaster.size() - 1).openRecord(loRS.getString("sTransNox"));
                lnCtr++;
            }
            System.out.println("Records found: " + lnCtr);
            loJSON.put("result", "success");
            loJSON.put("message", "Record loaded successfully.");
        } else {
            poSalesReservationMaster = new ArrayList<>();
            poSalesReservationMaster.add(SalesReservationMasterList());
            loJSON.put("result", "error");
            loJSON.put("continue", true);
            loJSON.put("message", "No record found .");
        }
        MiscUtil.close(loRS);
        return loJSON;
    }

    private Model_Sales_Reservation_Master SalesReservationMasterList() {
        return new SalesModels(poGRider).Sales_Reservation_Master();
    }

    public int getSalesReservationCount() {
        return this.poSalesReservationMaster.size();
    }

    public Model_Sales_Reservation_Master poSalesReservationMasterList(int row) {
        return (Model_Sales_Reservation_Master) poSalesReservationMaster.get(row);
    }

    public void resetMaster() {
        poMaster = new SalesModels(poGRider).Sales_Reservation_Master();
        Master().setIndustryID(psIndustryId);
        Master().setCompanyID(psCompanyId);
    }

    public void resetOthers() throws SQLException, GuanzonException {
//        checkPayments = new CashflowControllers(poGRider, logwrapr).CheckPayments();
//        Payees = new CashflowControllers(poGRider, logwrapr).Payee();
//        poPaymentRequest = new ArrayList<>();
//        poApPayments = new ArrayList<>();
//        poCachePayable = new ArrayList<>();
    }

    public JSONObject validateConfirmedTransactionApproval() {
        JSONObject loJSON = new JSONObject();

        // Default success
        loJSON.put("result", "success");
        loJSON.put("message", "");

        // Only validate if transaction is confirmed
        if (!Master().getTransactionStatus().equals(Sales_Reservation_Static.CONFIRMED)) {
            return loJSON; // Not confirmed, skip approval
        }

        // Ask user if they want to proceed
        boolean proceed = ShowMessageFX.YesNo(
                "Updating a confirmed transaction requires system user approval.\n"
                + "Do you want to proceed?",
                "Computerized Accounting System",
                null
        );

        if (!proceed) {
            loJSON.put("result", "error");
            loJSON.put("message", "User cancelled update of confirmed transaction.");
            return loJSON;
        }

        // Check if current user level requires higher-level approval
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            JSONObject approvalJSON = ShowDialogFX.getUserApproval(poGRider);

            if (!"success".equalsIgnoreCase((String) approvalJSON.get("result"))) {
                return approvalJSON; // Already contains result/message
            }

            int approvingUserLevel = Integer.parseInt(approvalJSON.get("nUserLevl").toString());
            if (approvingUserLevel <= UserRight.ENCODER) {
                loJSON.put("result", "error");
                loJSON.put("message", "User is not an authorized approving officer.");
                return loJSON;
            }
            setApproving((String) poJSON.get("sUserIDxx"));
        }
        loJSON.put("result", "success");
        return loJSON; // Passed all checks
    }

    public JSONObject callapproval() {
        JSONObject loJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            loJSON = ShowDialogFX.getUserApproval(poGRider);

            if (!"success".equalsIgnoreCase((String) loJSON.get("result"))) {
                return loJSON; // Already contains result/message
            }

            int approvingUserLevel = Integer.parseInt(loJSON.get("nUserLevl").toString());
            if (approvingUserLevel <= UserRight.ENCODER) {
                loJSON.put("result", "error");
                loJSON.put("message", "User is not an authorized approving officer.");
                return loJSON;
            }
            setApproving((String) poJSON.get("sUserIDxx"));
        }
        loJSON.put("result", "success");
        return loJSON;
    }

    public JSONObject checkExistingTrans(String sourceCode, String sourceNo) throws SQLException {
        JSONObject loJSON = new JSONObject();

        // Return success immediately if either parameter is null or empty
        if (sourceCode == null || sourceCode.isEmpty() || sourceNo == null || sourceNo.isEmpty()) {
            loJSON.put("result", "success");
            return loJSON;
        }

        String lsSQL = "SELECT sTransNox FROM Sales_Reservation_Master WHERE "
                + "sBranchCd = " + SQLUtil.toSQL(Master().getBranchCode()) + " AND "
                + "sSourceNo = '" + sourceNo + "' AND "
                + "sSourceCd = '" + sourceCode + "' AND "
                + "cTranStat = '" + Sales_Reservation_Static.CONFIRMED + "' "
                + "ORDER BY sTransNox DESC LIMIT 1";

        ResultSet loRS = null;

        try {
            System.out.println("EXECUTING SQL :  " + lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            System.out.println("VALUE OF Lors : " + loRS.toString());
            if (loRS != null && loRS.next()) {
                loJSON.put("result", "error");
                loJSON.put("message", "Sales Reservation cannot be confirmed because the reservation source is already in use.");
                return loJSON;
            }

        } finally {
            MiscUtil.close(loRS);
        }
        loJSON.put("result", "success");
        return loJSON;
    }

    private JSONObject setProcessSource(String source, String transactionNo)
            throws GuanzonException,
            SQLException,
            CloneNotSupportedException {
        poJSON = new JSONObject();
        switch (source) {
            case Sales_Reservation_Static.Source.source_inquiry:
                modelSalesInquiryMaster.initialize();

                poJSON = modelSalesInquiryMaster.openRecord(transactionNo);
                if (!"success".equals(poJSON.get("result"))) {
                    String message = (String) poJSON.get("message");
                    poJSON.put("result", "error");
                    poJSON.put("message", message);
                    return poJSON;
                }

                poJSON = modelSalesInquiryMaster.updateRecord();
                if (!"success".equals(poJSON.get("result"))) {
                    String message = (String) poJSON.get("message");
                    poJSON.put("result", "error");
                    poJSON.put("message", message);
                    return poJSON;
                }
                modelSalesInquiryMaster.getEditMode();
                modelSalesInquiryMaster.isProcessed(true);

                poJSON = modelSalesInquiryMaster.saveRecord();

                if (!"success".equals(poJSON.get("result"))) {
                    String message = (String) poJSON.get("message");
                    poJSON.put("result", "error");
                    poJSON.put("message", message);
                    return poJSON;
                }
//                poJSON = salesInquiry.InitTransaction();
//                poJSON = salesInquiry.OpenTransaction(transactionNo);
//                if (!"success".equals(poJSON.get("result"))) {
//                    String message = (String) poJSON.get("message");
//                    poJSON.put("result", "error");
//                    poJSON.put("message", message);
//                    return poJSON;
//                }
//                poJSON = salesInquiry.UpdateTransaction();
//                if (!"success".equals(poJSON.get("result"))) {
//                    String message = (String) poJSON.get("message");
//                    poJSON.put("result", "error");
//                    poJSON.put("message", message);
//                    return poJSON;
//                }
//
//                salesInquiry.getEditMode();
                break;
            case Sales_Reservation_Static.Source.source_qoutation:
                ShowMessageFX.Error(
                        "This feature is currently disabled because its core object is not yet implemented.",
                        "Feature Under Development",
                        null
                );
                break;
            default:
                throw new AssertionError();
        }
        poJSON.put("result", "success");
        return poJSON;
    }

//    private JSONObject saveProcessSource(String source, String status)
//            throws GuanzonException,
//            SQLException,
//            CloneNotSupportedException {
//        poJSON = new JSONObject();
//
//        switch (source) {
//            case Sales_Reservation_Static.Source.source_inquiry:
//                switch (status) {
//                    case Sales_Reservation_Static.CONFIRMED:
//                        modelSalesInquiryMaster.isProcessed(true);
////                        salesInquiry.Master().isProcessed(true);
//                        break;
//                    case Sales_Reservation_Static.VOID:
//                        modelSalesInquiryMaster.isProcessed(false);
////                        salesInquiry.Master().isProcessed(false);
//                        break;
//                }
////                modelSalesInquiryMaster.setWithParent(true);
//                poJSON = modelSalesInquiryMaster.saveRecord();
//                if (!"success".equals(poJSON.get("result"))) {
//                    String message = (String) poJSON.get("message");
//                    poJSON.put("result", "error");
//                    poJSON.put("message", message);
//                    return poJSON;
//                }
//
//                break;
//            case Sales_Reservation_Static.Source.source_qoutation:
//                ShowMessageFX.Error(
//                        "This feature is currently disabled because its core object is not yet implemented.",
//                        "Feature Under Development",
//                        null
//                );
//                break;
//            default:
//                throw new AssertionError();
//        }
//        poJSON.put("result", "success");
//        return poJSON;
//    }

    private JSONObject ComputeAmount(double quantity, double minimumDP)
            throws GuanzonException,
            SQLException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        double TotalComputed = quantity * minimumDP;

        poJSON.put("result", "success");
        poJSON.put("totalComputed", TotalComputed);
        return poJSON;
    }

    
    /*use to display details on UI*/
    public List<StockSummary> computeStockSummaries()
        throws SQLException, GuanzonException, CloneNotSupportedException {

    List<StockSummary> result = new ArrayList<>();
    int detailCount = getDetailCount();
    boolean[] done = new boolean[detailCount];

    for (int i = 0; i < detailCount; i++) {
        if (done[i]) continue;

        String stockId = Detail(i).getStockID();
        double totalTrue = 0.0, totalFalse = 0.0;

        for (int j = 0; j < detailCount; j++) {
            if (!done[j] && stockId.equals(Detail(j).getStockID())) {
                double q = Detail(j).getQuantity();
                if (Detail(j).isReversed()) totalTrue += q;
                else totalFalse += q;
                done[j] = true;
            }
        }

        double netQty   = totalTrue - totalFalse;
        double unitCost = Double.parseDouble(
                              Detail(i).Inventory().getCost().toString());

        result.add(new StockSummary(
            stockId,
            netQty,
            unitCost,
            Detail(i).Inventory().getDescription()
        ));
    }
    return result;
}
    
    public static class StockSummary {

        private final String stockId;
        private final double netQty;
        private final double unitPrice;
        private final String description;

        public StockSummary(String stockId,
                double netQty,
                double unitPrice,
                String description) {
            this.stockId = stockId;
            this.netQty = netQty;
            this.unitPrice = unitPrice;
            this.description = description;
        }

        public String getStockId() {
            return stockId;
        }

        public double getNetQty() {
            return netQty;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public String getDescription() {
            return description;
        }
    }
    
    
    public void ShowStatusHistory() throws SQLException, GuanzonException, Exception{
        CachedRowSet crs = getStatusHistory();
        
        crs.beforeFirst(); 

	
        while(crs.next()){
            switch (crs.getString("cRefrStat")){
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case Sales_Reservation_Static.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case Sales_Reservation_Static.CONFIRMED:
                    crs.updateString("cRefrStat", "CONFIRMED");
                    break;
                case Sales_Reservation_Static.PAID:
                    crs.updateString("cRefrStat", "PAID");
                    break;
                case Sales_Reservation_Static.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                case Sales_Reservation_Static.VOID:
                    crs.updateString("cRefrStat", "VOID");
                    break;
                
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);
                    
                    switch (stat){
                    case Sales_Reservation_Static.OPEN:
                        crs.updateString("cRefrStat", "OPEN");
                        break;
                    case Sales_Reservation_Static.CONFIRMED:
                        crs.updateString("cRefrStat", "CONFIRMED");
                        break;
                    case Sales_Reservation_Static.PAID:
                        crs.updateString("cRefrStat", "PAID");
                        break;
                    case Sales_Reservation_Static.CANCELLED:
                        crs.updateString("cRefrStat", "CANCELLED");
                        break;
                    case Sales_Reservation_Static.VOID:
                        crs.updateString("cRefrStat", "VOID");
                        break;                    
                    }
            }
            crs.updateRow(); 
        }
        
        JSONObject loJSON  = getEntryBy();
        String entryBy = "";
        String entryDate = "";
        
        if ("success".equals((String) loJSON.get("result"))){
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }
        
        showStatusHistoryUI("Purchase Order", (String) poMaster.getValue("sTransNox"), entryBy, entryDate, crs);
    }
    public JSONObject getEntryBy() throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL =  " SELECT b.sModified, b.dModified " 
                        + " FROM PO_Master a "
                        + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Master().getTable());
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(Master().getTransactionNo())) ;
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                if(loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))){
                    if(loRS.getString("sModified").length() > 10){
                        lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified"))); 
                    } else {
                        lsEntry = getSysUser(loRS.getString("sModified")); 
                    }
                    // Get the LocalDateTime from your result set
                    LocalDateTime dModified = loRS.getObject("dModified", LocalDateTime.class);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                    lsEntryDate =  dModified.format(formatter);
                }
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
          return poJSON;
        } 
        
        poJSON.put("result", "success");
        poJSON.put("sCompnyNm", lsEntry);
        poJSON.put("sEntryDte", lsEntryDate);
        return poJSON;
    }
    public String getSysUser(String fsId) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL =   " SELECT b.sCompnyNm from xxxSysUser a " 
                       + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId)) ;
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
          if (MiscUtil.RecordCount(loRS) > 0L) {
            if (loRS.next()) {
                lsEntry = loRS.getString("sCompnyNm");
            } 
          }
          MiscUtil.close(loRS);
        } catch (SQLException e) {
          poJSON.put("result", "error");
          poJSON.put("message", e.getMessage());
        } 
        return lsEntry;
    }

}
