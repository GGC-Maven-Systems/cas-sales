/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.model;

import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Model class for the Sales Quotation FollowUp transaction.
 *
 * <p>
 * A record is written to this table every time a Sales Quotation Version is
 * followed up. Each record is identified by the combination of
 * {@code sTransNox} (the version's transaction number) and
 * {@code nEntryNox} (the follow-up entry number), and captures the
 * reference number, follow-up date, next follow-up date, who performed the
 * follow-up, the follow-up type, and remarks.
 * </p>
 *
 * <p>
 * Detail records do not generate their own transaction number; the
 * {@code sTransNox} value is inherited from the parent
 * {@code Sales_Quotation_Version_Master} record, and {@code nEntryNox} is
 * expected to be assigned by the caller (e.g. the next available follow-up
 * entry number for the given transaction).
 * </p>
 *
 * @author TEEJEI DE CELIS
 * @date September 2x, 2026
 * @module Sales Quotation FollowUp
 * @version 1.0
 */
public class Model_Sales_Quotations_FollowUp extends Model {

    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(
                    System.getProperty("sys.default.path.metadata") + XML,
                    getTable()
            );

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            // assign default values
            poEntity.updateObject("dFollowUp", poGRider.getServerDate());
            poEntity.updateNull("dNextFlup");
            poEntity.updateNull("dModified");
            // end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setEntryNo(Integer entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Integer getEntryNo() {
        return (Integer) getValue("nEntryNox");
    }

    public JSONObject setReferenceNo(String referenceNo) {
        return setValue("sReferNox", referenceNo);
    }

    public String getReferenceNo() {
        return (String) getValue("sReferNox");
    }

    public JSONObject setFollowUpDate(Date followUpDate) {
        return setValue("dFollowUp", followUpDate);
    }

    public Date getFollowUpDate() {
        return (Date) getValue("dFollowUp");
    }

    public JSONObject setNextFollowUpDate(Date nextFollowUpDate) {
        return setValue("dNextFlup", nextFollowUpDate);
    }

    public Date getNextFollowUpDate() {
        return (Date) getValue("dNextFlup");
    }

    public JSONObject setFollowUpBy(String followUpBy) {
        return setValue("sFllwUpby", followUpBy);
    }

    public String getFollowUpBy() {
        return (String) getValue("sFllwUpby");
    }

    public JSONObject setFollowUpType(String followUpType) {
        return setValue("cFllwUpTp", followUpType);
    }

    public String getFollowUpType() {
        return (String) getValue("cFllwUpTp");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    public Timestamp getTimeStamp() {
        return (Timestamp) getValue("dTimeStmp");
    }
}
