/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.sales.model.Model_Bank_Application;
import ph.com.guanzongroup.cas.sales.model.Model_Sales_Inquiry_Master;
import ph.com.guanzongroup.cas.sales.services.SalesModels;
import ph.com.guanzongroup.cas.sales.status.BankApplicationStatus;
import ph.com.guanzongroup.cas.sales.status.SalesInquiryStatic;
import ph.com.guanzongroup.cas.sales.validator.BankApplication;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionStatusHistory;

/**
 *
 * @author Arsiela 07-20-2026
 */
public class SalesBankApplication extends Transaction{
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";

    List<Model_Sales_Inquiry_Master> paMasterList;

    public JSONObject InitTransaction() {
        SOURCE_CODE = "Bnkp";

        poMaster = new SalesModels(poGRider).SalesInquiryMaster();
        poDetail = new SalesModels(poGRider).BankApplication();

        paMasterList = new ArrayList<>();
        paDetail = new ArrayList<>();

        return initialize();
    }

    public void setCompanyId(String companyId) {
        psCompanyId = companyId;
    }
    public void setIndustryId(String industryId) {
        psIndustryId = industryId;
    }
    public void setCategoryId(String categoryId) {
        psCategorCd = categoryId;
    }

    public JSONObject NewTransaction()
            throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        return openTransaction(transactionNo);
    }

    /**
     * Overide openTransaction base class to open detail for bank application
     * @param transactionNo
     * @return
     * @throws CloneNotSupportedException
     * @throws SQLException
     * @throws GuanzonException
     */
    protected JSONObject openTransaction(String transactionNo) throws CloneNotSupportedException, SQLException, GuanzonException {
        poJSON = poMaster.openRecord(transactionNo);
        if (!"success".equals((String)poJSON.get("result"))) {
            poJSON.put("message", "Unable to open transaction master record.");
            clear();
            return poJSON;
        } else {
            paDetail.clear();
            String sql = "SELECT sTransNox FROM " + poDetail.getTable()
                    + " WHERE sSourceNo = " + SQLUtil.toSQL(transactionNo)
                    + " AND sSourceCd = " + SQLUtil.toSQL("SInq")
                    + " ORDER BY sTransNox";
            ResultSet rs = poGRider.executeQuery(sql);

            while(rs.next()) {
                Model loDetail = (Model)poDetail.clone();
                loDetail.newRecord();
                poJSON = loDetail.openRecord(rs.getString("sTransNox"));
                if (!"success".equals((String)poJSON.get("result"))) {
                    poJSON.put("message", "Unable to open transaction detail record.");
                    clear();
                    return poJSON;
                }

                paDetail.add(loDetail);
            }

            pnEditMode = 1;
            pbRecordExist = true;
            poJSON = new JSONObject();
            poJSON.put("result", "success");
            return poJSON;
        }
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }
    
    public JSONObject ApproveBankApplication(String remarks, List<Model_Bank_Application> faModel)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException,
            ScriptException {
        
        poJSON = new JSONObject();

        String lsStatus = BankApplicationStatus.APPROVED;
        for(int lnCtr = 0;lnCtr < faModel.size();lnCtr++){
            if (faModel.get(lnCtr).getEditMode() != EditMode.READY) {
                poJSON.put("result", "error");
                poJSON.put("message", "No bank application loaded.");
                return poJSON;
            }

            if (lsStatus.equals(faModel.get(lnCtr).getTransactionStatus())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Bank Application was already approved.");
                return poJSON;
            }

            if (!BankApplicationStatus.OPEN.equals(faModel.get(lnCtr).getTransactionStatus())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Bank Application was already "+getStatus(faModel.get(lnCtr).getTransactionStatus()).toLowerCase()+".");
                return poJSON;
            }

            poJSON = isEntryOkay(lsStatus, faModel.get(lnCtr));
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }
        
        poJSON = callApproval();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = updateStatus(faModel, lsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Bank Application approved successfully.");
        return poJSON;
    }
    
    public JSONObject DisapproveBankApplication(String remarks, List<Model_Bank_Application> faModel)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException,
            ScriptException {
        
        poJSON = new JSONObject();

        String lsStatus = BankApplicationStatus.DISAPPROVED;
        for(int lnCtr = 0;lnCtr < faModel.size();lnCtr++){
            if (faModel.get(lnCtr).getEditMode() != EditMode.READY) {
                poJSON.put("result", "error");
                poJSON.put("message", "No bank application loaded.");
                return poJSON;
            }

            if (lsStatus.equals(faModel.get(lnCtr).getTransactionStatus())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Bank Application was already disapprove.");
                return poJSON;
            }

            if (!BankApplicationStatus.OPEN.equals(faModel.get(lnCtr).getTransactionStatus())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Bank Application was already "+getStatus(faModel.get(lnCtr).getTransactionStatus()).toLowerCase()+".");
                return poJSON;
            }

            poJSON = isEntryOkay(lsStatus, faModel.get(lnCtr));
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }
        
        poJSON = callApproval();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = updateStatus(faModel, lsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Bank Application disapproved successfully.");
        return poJSON;
    }
    
    public JSONObject CancelBankApplication(String remarks, List<Model_Bank_Application> faModel)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException,
            ScriptException {
        
        poJSON = new JSONObject();

        String lsStatus = BankApplicationStatus.CANCELLED;
        for(int lnCtr = 0;lnCtr < faModel.size();lnCtr++){
            if (faModel.get(lnCtr).getEditMode() != EditMode.READY) {
                poJSON.put("result", "error");
                poJSON.put("message", "No bank application loaded.");
                return poJSON;
            }

            if (lsStatus.equals(faModel.get(lnCtr).getTransactionStatus())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Bank Application was already cancelled.");
                return poJSON;
            }

            if (!BankApplicationStatus.OPEN.equals(faModel.get(lnCtr).getTransactionStatus())) {
                poJSON.put("result", "error");
                poJSON.put("message", "Bank Application was already "+getStatus(faModel.get(lnCtr).getTransactionStatus()).toLowerCase()+".");
                return poJSON;
            }

            poJSON = isEntryOkay(lsStatus, faModel.get(lnCtr));
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
        }
        
        poJSON = callApproval();
        if (!isJSONSuccess(poJSON)) {
            return poJSON;
        }
        
        poJSON = updateStatus(faModel, lsStatus);
        if ("error".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "Bank Application cancelled successfully.");
        return poJSON;
    }
    
    private JSONObject updateStatus(List<Model_Bank_Application> faObject, String fsStatus)
            throws ParseException, SQLException, GuanzonException, CloneNotSupportedException, ScriptException {
        poJSON = new JSONObject();

        String lsRemarks = "";
        if (pbWithUI){
            try {
                lsRemarks = ShowDialogFX.getStatusChangeNotes();
            } catch (Exception e) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", e.getMessage());
                return poJSON;
            }
        }

        for(int lnCtr = 0; lnCtr < faObject.size(); lnCtr++){
            Model_Bank_Application faItem = faObject.get(lnCtr);
            
            //Save to parameter status history
            poGRider.beginTrans("UPDATE STATUS", lsRemarks, "TSHx",faItem.getTransactionNo() );
            TransactionStatusHistory loStatus = (new SysTableContollers(poGRider, logwrapr)).TransactionStatusHistory();
            loStatus.setWithParentClass(true);
            poJSON = loStatus.newRecord();
            if (!"success".equals(poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
            loStatus.getModel().setTransactionTable(faItem.getTable());
            loStatus.getModel().setSourceNo(faItem.getTransactionNo());
            loStatus.getModel().setRemarks(lsRemarks);
            loStatus.getModel().setStatusRequest(fsStatus);
            loStatus.getModel().setTransactionStatus("1");
            loStatus.getModel().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
            poJSON = loStatus.saveRecord();
            if (!"success".equals(poJSON.get("result"))) {
                poGRider.rollbackTrans();
                return poJSON;
            }
            
            
            if(BankApplicationStatus.APPROVED.equals(fsStatus)){
                Model_Bank_Application loObject = new SalesModels(poGRider).BankApplication();
                loObject.initialize();

                poJSON = loObject.openRecord(faItem.getTransactionNo());
                if (!isJSONSuccess(poJSON)) {
                    return poJSON;
                }
                if(loObject.getApprovedDate() == null){
                    poJSON = loObject.updateRecord();
                    if (!isJSONSuccess(poJSON)) {
                        return poJSON;
                    }

                    loObject.setApprovedDate(faItem.getApprovedDate());
                    poJSON = loObject.saveRecord();
                    if (!isJSONSuccess(poJSON)) {
                        return poJSON;
                    }
                }
            }
            
            String lsSQL = "UPDATE " + faItem.getTable() + " SET   cTranStat = " + SQLUtil.toSQL(fsStatus);
            String lsCondition = ("sTransNox = " + SQLUtil.toSQL(faItem.getTransactionNo()));
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
            if (poGRider.executeQuery(lsSQL, faItem.getTable(), poGRider.getBranchCode(), "", "") <= 0L) {
                poJSON = new JSONObject();
                poJSON.put("result", "error");
                poJSON.put("message", "Error updating the transaction status.");
                poGRider.rollbackTrans();
                return poJSON;
            }
            poGRider.commitTrans();
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    public JSONObject checkPendingBankApplication(){
        for(int lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++){
            if(Detail(lnRow).getEditMode() == EditMode.UPDATE){
                if (Detail(lnRow).getTransactionStatus().equals(BankApplicationStatus.OPEN)) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "You have a pending bank application. Update the status before changing the purchase type.");
                    return poJSON;
                } 
            }
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }


    public JSONObject loadTransactionList(String client, String referenceNo) {
        try {
            if (client == null) {
                client = "";
            }
            if (referenceNo == null) {
                referenceNo = "";
            }

            String lsTransStat = "";
            if (psTranStat != null) {
                if (psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                        lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                    }
                    lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
                } else {
                    lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
                }
            }

            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
//                            + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                            + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode())
                            + " AND b.sCompnyNm LIKE " + SQLUtil.toSQL("%" + client)
                            + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + referenceNo)
//                            + " AND a.cProcessd = '0' "
            );

            //If current user is an ordinary user load only its inquiries
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                lsSQL = MiscUtil.addCondition(lsSQL,
                        " a.sSalesman = " + SQLUtil.toSQL(getSysUser(poGRider.getUserID(), true)));
            }

            if (lsTransStat != null && !"".equals(lsTransStat)) {
                lsSQL = lsSQL + lsTransStat;
            }

            lsSQL = lsSQL + " ORDER BY a.dTransact DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMasterList = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sCompnyNm: " + loRS.getString("sClientNm"));
                    System.out.println("------------------------------------------------------------------------------");

                    paMasterList.add(SalesInquiryMaster());
                    paMasterList.get(paMasterList.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paMasterList = new ArrayList<>();
                paMasterList.add(SalesInquiryMaster());
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        return poJSON;
    }

    private Model_Sales_Inquiry_Master SalesInquiryMaster() {
        return new SalesModels(poGRider).SalesInquiryMaster();
    }
    public int getSalesInquiryCount() {
        if (paMasterList == null) {
            paMasterList = new ArrayList<>();
        }

        return paMasterList.size();
    }

    public Model_Sales_Inquiry_Master SalesInquiryList(int row) {
        return (Model_Sales_Inquiry_Master) paMasterList.get(row);
    }


    /**
     * Search Bank
     * @param value
     * @param byCode
     * @param row
     * @return JSONObject success or error
     * @throws SQLException
     * @throws GuanzonException 
     */
    public JSONObject SearchBank(String value, boolean byCode, int row)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);
        
        if(!(Master().getPurchaseType().equals(SalesInquiryStatic.PurchaseType.PO)
            || Master().getPurchaseType().equals(SalesInquiryStatic.PurchaseType.FINANCING))){
            poJSON.put("result", "error");
            poJSON.put("message", "Sales Inquiry purchase type must be PO or Financing.");
            return poJSON;
        }

        Banks object = new ParamControllers(poGRider, logwrapr).Banks();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        poJSON.put("row", row);
        if ("success".equals((String) poJSON.get("result"))) {
            poJSON = checkExistingBank(object.getModel().getBankID(), row);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            System.out.println("DETAIL ROW COUNT : " + getDetailCount());
            Detail(row).setBankId(object.getModel().getBankID());
            Detail(row).setAppliedDate(poGRider.getServerDate());
        }
        
        System.out.println("Bank Name : " + Detail(row).Bank().getBankName());
        
        poJSON.put("row", row);
        return poJSON;
    }
    /**
     * Check Existing Bank
     * @param bankId
     * @param row
     * @return JSONObject success or error
     */
    private JSONObject checkExistingBank(String bankId, int row){
        poJSON = new JSONObject();
        
        for(int lnCtr = 0;lnCtr <= getDetailCount() - 1; lnCtr++){
            if(lnCtr != row){
                if(Detail(lnCtr).getTransactionStatus().equals(BankApplicationStatus.OPEN)
                    || Detail(lnCtr).getTransactionStatus().equals(BankApplicationStatus.APPROVED)){
                    
                    if(bankId.equals(Detail(lnCtr).getBankId())){
                        poJSON.put("result", "error");
                        poJSON.put("message", "Bank already exists in the table at row " + (lnCtr+1) + ".");
                        poJSON.put("row", lnCtr);
                        return poJSON;
                    }
                    
                }
            }
        }
        
        return poJSON;
    }
    
    /**
    * Requests user approval for the current transaction.
    *
    * @return JSONObject containing approval result and message
    */
    public JSONObject callApproval(){
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!isJSONSuccess(poJSON)) {
                return poJSON;
            }
            String lsUserIDxx = poJSON.get("sUserIDxx").toString();
            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                poJSON = setJSON("error", "User is not an authorized approving officer.");
                return poJSON;
            }
            setApproving(lsUserIDxx);
        }   
        
        poJSON = setJSON("success","success");
        return poJSON;
    }
    
    /**
    * Converts a numeric or short-code transaction status into a human-readable string.
    * 
    * @param lsStatus the status code to be converted.
    * @return the descriptive name of the status (e.g., "Voided", "Confirmed"), 
    *         or "Unknown" if the code is not recognized.
    */
    public String getStatus(String lsStatus) {
        switch (lsStatus) {
            case BankApplicationStatus.DISAPPROVED:
                return "Disapproved";
            case BankApplicationStatus.CANCELLED:
                return "Cancelled";
            case BankApplicationStatus.APPROVED:
                return "Approved";
            case BankApplicationStatus.OPEN:
                return "Open";
            default:
                return "Unknown";
        }
    }
    
    public String getInquiryStatus(String lsStatus) {
        switch (lsStatus) {
            case SalesInquiryStatic.LOST:
                return "Lost Sale";
            case SalesInquiryStatic.CONFIRMED:
                return "Confirmed";
            case SalesInquiryStatic.QUOTED:
                return "Quoted";
            case SalesInquiryStatic.SALE:
                return "Sale";
            case SalesInquiryStatic.OPEN:
                return "Open";
            case SalesInquiryStatic.VOID:
                return "Void";
            case SalesInquiryStatic.CANCELLED:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }
    /**
    * Returns the master record model for the current cash disbursement transaction.
    * 
    * @return The {@link Model_Sales_Inquiry_Master} instance representing the transaction header.
    */
    @Override
    public Model_Sales_Inquiry_Master Master() { 
        return (Model_Sales_Inquiry_Master) poMaster; 
    }
    /**
    * Returns the detail record model at the specified row index.
    * 
    * @param row The index of the detail row to retrieve.
    * @return The {@link Model_Bank_Application} instance for the given row.
    */
    @Override
    public Model_Bank_Application Detail(int row) {
        return (Model_Bank_Application) paDetail.get(row); 
    }
    
    /**
    * Adds a new detail row to the transaction list after validating the current entries.
    * 
    * This method ensures that a new row can only be added if the preceding row has a 
    * valid "Particular" item selected. If the last row is incomplete, an error is returned.
    * 
    * @return A JSONObject indicating the result of the operation.
    * @throws CloneNotSupportedException If an error occurs while creating the new detail model.
    */
    public JSONObject AddDetail() throws CloneNotSupportedException {
        if (getDetailCount() > 0) {
            if ((Detail(getDetailCount() - 1).getBankId() == null || "".equals(Detail(getDetailCount() - 1).getBankId()))
                && Detail(getDetailCount() - 1).getApplicationNo() == null || "".equals(Detail(getDetailCount() - 1).getApplicationNo())){
                poJSON = new JSONObject();
                poJSON = setJSON("error", "Last row has empty item.");
                return poJSON;
            }
        }

        return addDetail();
    }
   
    /**
     * Refines and validates the transaction detail list.
     * 
     * This method prunes invalid rows (those with empty particulars or zero amounts for new records) 
     * and automatically appends a new detail row if the list is empty or the last entry is valid.
     * 
     * @throws CloneNotSupportedException If an error occurs while adding a new detail row.
     */
    public void ReloadDetail() throws CloneNotSupportedException{
        try {
            int lnCtr = getDetailCount() - 1;
            while (lnCtr >= 0) {
                if ((Detail(lnCtr).getBankId() == null || "".equals(Detail(lnCtr).getBankId()))
                        && (Detail(lnCtr).getApplicationNo() == null || "".equals(Detail(lnCtr).getApplicationNo()))) {
                    deleteDetail(lnCtr);
                }
                lnCtr--;
            }
            
            if ((getDetailCount() - 1) >= 0) {
                if (
                        (Detail(getDetailCount() - 1).getBankId() != null && !"".equals(Detail(getDetailCount() - 1).getBankId()))
                        && (Detail(getDetailCount() - 1).getApplicationNo() != null && !"".equals(Detail(getDetailCount() - 1).getApplicationNo()))
                        ) {
                    AddDetail();
                }
            }
            
            if ((getDetailCount() - 1) < 0) {
                AddDetail();
            }
            
            Detail(getDetailCount() - 1).setAppliedDate(poGRider.getServerDate());
            Detail(getDetailCount() - 1).setPaymentMode(Master().getPurchaseType());
            Detail(getDetailCount() - 1).setSourceCode("SInq");
            Detail(getDetailCount() - 1).setSourceNo(Master().getTransactionNo());
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    
    @Override
    public JSONObject willSave()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        
        Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());

        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            if ((item.getValue("sBankIDxx") == null || "".equals(item.getValue("sBankIDxx")))) {
                detail.remove();
            }
        }
        //Validate detail after removing all zero qty and empty stock Id
        if (getDetailCount() <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transaction detail to be save.");
            return poJSON;
        }
        
        for(int lnRow = 0; lnRow <= getDetailCount() - 1; lnRow++){
            Detail(lnRow).setEntryNo(lnRow+1);
            
            if(Detail(lnRow).getEditMode() == EditMode.ADDNEW){
                Detail(lnRow).setTransactionNo(Detail(lnRow).getNextCode());
            }
            
            poJSON = isEntryOkay(Detail(lnRow).getTransactionStatus(), Detail(lnRow));
            if (!"success".equals((String) poJSON.get("result"))) {
                poJSON.put("result", "error");
                return poJSON;
            } 
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public JSONObject save() throws CloneNotSupportedException, SQLException, GuanzonException {
        /*Put saving business rules here*/
        return isEntryOkay(SalesInquiryStatic.OPEN);
    }
    
    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }
    
    @Override
    public JSONObject saveOthers() {
        /*Only modify this if there are other tables to modify except the master and detail tables*/
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    

    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            System.out.println("Dept ID : " + poGRider.getDepartment());
            System.out.println("Current User : " + poGRider.getUserID());
            
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryId(psIndustryId);
            Master().setCompanyId(psCompanyId);
            Master().setCategoryCode(psCategorCd);
            Master().setTransactionDate(poGRider.getServerDate());

        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject isEntryOkay(String status, Model_Bank_Application master) {
        GValidator loValidator = new BankApplication();

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(master);

        poJSON = loValidator.validate();

        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE =  " SELECT "
                + " a.sTransNox "
                + " , a.dTransact "
                + " , a.cTranStat "
                + " , a.sClientID "
                + " , b.sCompnyNm AS sClientNm "
                + " , concat(c.sLastName,', ',c.sFrstName, ' ',c.sMiddName) AS sSalePrsn "
                + " , d.sCompnyNm AS sAgentNme "
                + " , e.sBranchNm "
                + " , f.sCompnyNm "
                + " , g.sDescript "
                + " FROM sales_inquiry_master a "
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID "
                + " LEFT JOIN salesman c ON c.sEmployID = a.sSalesman "
                + " LEFT JOIN client_master d ON d.sClientID = a.sAgentIDx "
                + " LEFT JOIN branch e ON e.sBranchCd = a.sBranchCd "
                + " LEFT JOIN company f ON f.sCompnyID = a.sCompnyID "
                + " LEFT JOIN industry g ON g.sIndstCdx = a.sIndstCdx " ;

    }
    
    /**
    * Creates a JSONObject with "result" and "message" fields.
    *
    * @param fsResult  The result value (e.g., "success", "error")
    * @param fsMessage The message describing the result
    * @return JSONObject containing the result and message
    */
    private JSONObject setJSON(String fsResult, String fsMessage) {
        JSONObject loJSON = new JSONObject();
        loJSON.put("result", fsResult);
        loJSON.put("message", fsMessage);
        return loJSON;
    }

    /**
     * Checks whether a JSONObject indicates a successful result.
     *
     * Returns true if the "result" field equals "success" or is not "error".
     *
     * @param foJSON The JSONObject to check
     * @return true if successful, false otherwise
     */
    public boolean isJSONSuccess(JSONObject foJSON) {
        return ("success".equals((String) foJSON.get("result")) || !"error".equals((String) foJSON.get("result")));
    }

    protected CachedRowSet getStatusHistory(int fnRow) throws SQLException {
        String lsSQL = "SELECT  a.sTableNme, a.sSourceNo, a.sRemarksx, a.cRefrStat cTranStat, IFNULL(c.sCompnyNm, '-') xModified, IFNULL(e.sCompnyNm, '-') xApproved, a.dModified, a.dApproved, a.sModified, a.sApproved " +
                " FROM GCASys_DBF.Transaction_Status_History a " +
                "LEFT JOIN GCASys_DBF.xxxSysUser b ON b.sUserIDxx = AES_DECRYPT(UNHEX(a.sModified), '08220326') " +
                "LEFT JOIN GGC_ISysDBF.Client_Master c ON b.sEmployNo = c.sClientID " +
                "LEFT JOIN GCASys_DBF.xxxSysUser d ON d.sUserIDxx = AES_DECRYPT(UNHEX(a.sApproved), '08220326') " +
                "LEFT JOIN GGC_ISysDBF.Client_Master e ON d.sEmployNo = e.sClientID " +
                " WHERE a.sSourceNo = " + SQLUtil.toSQL(Detail(fnRow).getTransactionNo()) +
                " AND a.sTableNme = " + SQLUtil.toSQL(Detail(fnRow).getTable()) + " ORDER BY a.dModified";
        System.out.println("STATUS HISTORY : " + lsSQL);
        ResultSet loRS = this.poGRider.executeQuery(lsSQL);
        RowSetFactory factory = RowSetProvider.newFactory();
        CachedRowSet rowset = factory.createCachedRowSet();
        rowset.populate(loRS);
        MiscUtil.close(loRS);
        return rowset;
    }


    /**
     * Loads status history, maps status codes to captions, and displays the status-history dialog.
     *
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If model operations fail.
     * @throws Exception If UI rendering fails.
     */
    public void ShowStatusHistory(int fnRow) throws SQLException, GuanzonException, Exception{
        CachedRowSet crs = getStatusHistory(fnRow);

        crs.beforeFirst();
        while(crs.next()){
            switch (crs.getString("cRefrStat")){
                case "":
                    crs.updateString("cRefrStat", "-");
                    break;
                case BankApplicationStatus.OPEN:
                    crs.updateString("cRefrStat", "OPEN");
                    break;
                case BankApplicationStatus.APPROVED:
                    crs.updateString("cRefrStat", "APPROVED");
                    break;
                case BankApplicationStatus.DISAPPROVED:
                    crs.updateString("cRefrStat", "DISAPPROVED");
                    break;
                case BankApplicationStatus.CANCELLED:
                    crs.updateString("cRefrStat", "CANCELLED");
                    break;
                default:
                    char ch = crs.getString("cRefrStat").charAt(0);
                    String stat = String.valueOf((int) ch - 64);

                    switch (stat){
                        case BankApplicationStatus.OPEN:
                            crs.updateString("cRefrStat", "OPEN");
                            break;
                        case BankApplicationStatus.APPROVED:
                            crs.updateString("cRefrStat", "APPROVED");
                            break;
                        case BankApplicationStatus.DISAPPROVED:
                            crs.updateString("cRefrStat", "DISAPPROVED");
                            break;
                        case BankApplicationStatus.CANCELLED:
                            crs.updateString("cRefrStat", "CANCELLED");
                            break;
                    }
            }
            crs.updateRow();
        }

        JSONObject loJSON  = getEntryBy(fnRow);
        String entryBy = "";
        String entryDate = "";

        if ("success".equals((String) loJSON.get("result"))){
            entryBy = (String) loJSON.get("sCompnyNm");
            entryDate = (String) loJSON.get("sEntryDte");
        }
        showStatusHistoryUI("Bank Application", Detail(fnRow).getTransactionNo(), entryBy, entryDate, crs);
    }

    /**
     * Resolves encoder name and entry timestamp from audit logs for the current transaction.
     *
     * @return JSON result containing entry metadata.
     * @throws SQLException If a database access error occurs.
     * @throws GuanzonException If user lookup operations fail.
     */
    public JSONObject getEntryBy(int fnRow) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        String lsEntry = "";
        String lsEntryDate = "";
        String lsSQL =  "SELECT " +
                " b.sModified, b.dModified " +
                " FROM "+Detail(fnRow).getTable()+" a " +
                "  INNER JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sSourceNo  " +
                "  INNER JOIN xxxAuditLogDetail c ON c.sSourceNo = b.sTransNox  " +
                " WHERE a.sTransNox = "+SQLUtil.toSQL(Detail(fnRow).getTransactionNo()) +
                " AND b.sSourceNo = "+SQLUtil.toSQL(Master().getTransactionNo()) +
                " AND b.sRemarksx = "+SQLUtil.toSQL(Master().getTable()) +
                " AND c.sQryTypex = 'INSERT'  " +
                " AND sTableNme = "+SQLUtil.toSQL(Detail(fnRow).getTable()) +
                " AND c.sPayloadx LIKE " + SQLUtil.toSQL("%"+Detail(fnRow).getTransactionNo()+"%"+Detail(fnRow).getEntryNo()+",%") +
                " ORDER BY b.dModified DESC ";
//        String lsSQL =  " SELECT b.sModified, b.dModified "
//                + " FROM "+Detail(fnRow).getTable()+" a "
//                + " LEFT JOIN xxxAuditLogMaster b ON b.sSourceNo = a.sTransNox AND b.sEventNme LIKE 'ADD%NEW' AND b.sRemarksx = " + SQLUtil.toSQL(Detail(fnRow).getTable());
//        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox =  " + SQLUtil.toSQL(Detail(fnRow).getTransactionNo()));
//        lsSQL = lsSQL + " ORDER BY b.dModified DESC ";
        System.out.println("Execute SQL : " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if(loRS.getString("sModified") != null && !"".equals(loRS.getString("sModified"))){
                        if(loRS.getString("sModified").length() > 10){
                            lsEntry = getSysUser(poGRider.Decrypt(loRS.getString("sModified")), false);
                        } else {
                            lsEntry = getSysUser(loRS.getString("sModified"), false);
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

    public String getSysUser(String fsId, boolean fbIsID) throws SQLException, GuanzonException {
        String lsEntry = "";
        String lsSQL =   " SELECT b.sCompnyNm, a.sEmployNo from xxxSysUser a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployNo ";
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sUserIDxx =  " + SQLUtil.toSQL(fsId)) ;
        System.out.println("SQL " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (MiscUtil.RecordCount(loRS) > 0L) {
                if (loRS.next()) {
                    if(fbIsID) {
                        lsEntry = loRS.getString("sEmployNo");
                    } else {
                        lsEntry = loRS.getString("sCompnyNm");
                    }
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
