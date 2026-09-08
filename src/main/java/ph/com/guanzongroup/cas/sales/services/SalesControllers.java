/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.services;

import ph.com.guanzongroup.cas.sales.*;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;

/**
 *
 * @author MIS-PC
 */
public class SalesControllers {
    
    public SalesControllers(GRiderCAS applicationDriver, LogWrapper logWrapper) {
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }
    
    public SalesInquiry SalesInquiry() {
        if (poGRider == null) {
            poLogWrapper.severe("SalesControllers.SalesInquiry: Application driver is not set.");
            return null;
        }

        if (poSalesInquiry != null) {
            return poSalesInquiry;
        }

        poSalesInquiry = new SalesInquiry();
        poSalesInquiry.setApplicationDriver(poGRider);
        poSalesInquiry.setBranchCode(poGRider.getBranchCode());
        poSalesInquiry.setVerifyEntryNo(true);
        poSalesInquiry.setWithParent(false);
        poSalesInquiry.setLogWrapper(poLogWrapper);
        return poSalesInquiry;
    }
    
    public SalesInquirySources SalesInquirySources() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("SalesControllers.SalesInquirySources: Application driver is not set.");
            return null;
        }

        if (poSalesInquirySources != null) {
            return poSalesInquirySources;
        }

        poSalesInquirySources = new SalesInquirySources();
        poSalesInquirySources.setApplicationDriver(poGRider);
        poSalesInquirySources.setWithParentClass(false);
        poSalesInquirySources.setLogWrapper(poLogWrapper);
        poSalesInquirySources.initialize();
        return poSalesInquirySources;
    }
    
    public Salesman Salesman() {
        try {
            if (poGRider == null) {
                poLogWrapper.severe("SalesControllers.Salesman: Application driver is not set.");
                return null;
            }
            if (poSalesman != null){ 
                return poSalesman;
            }
            poSalesman = new Salesman();
            poSalesman.setApplicationDriver(poGRider);
            poSalesman.setWithParentClass(false);
            poSalesman.setLogWrapper(poLogWrapper);
            poSalesman.initialize();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        
        return poSalesman;
    }
    
    public SalesAgent SalesAgent(){
        try {
            if (poGRider == null) {
                poLogWrapper.severe("SalesControllers.SalesAgent: Application driver is not set.");
                return null;
            }
            if (poSalesAgent != null){ 
                return poSalesAgent;
            }
            poSalesAgent = new SalesAgent();
            poSalesAgent.setApplicationDriver(poGRider);
            poSalesAgent.setWithParentClass(false);
            poSalesAgent.setLogWrapper(poLogWrapper);
            poSalesAgent.initialize();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        
        return poSalesAgent;
    }

    public SalesGiveaways SalesGiveaways(){
        if (poGRider == null) {
            poLogWrapper.severe("SalesControllers.SalesGiveaways: Application driver is not set.");
            return null;
        }

        if (poSalesGiveaways != null){
            return poSalesGiveaways;
        }

        poSalesGiveaways = new SalesGiveaways();
        poSalesGiveaways.setApplicationDriver(poGRider);
        poSalesGiveaways.setBranchCode(poGRider.getBranchCode());
        poSalesGiveaways.setVerifyEntryNo(true);
        poSalesGiveaways.setWithParent(false);
        poSalesGiveaways.setLogWrapper(poLogWrapper);

        return poSalesGiveaways;
    }

    public RequirementsSource RequirementsSource(){
        try {
            if (poGRider == null) {
                poLogWrapper.severe("SalesControllers.RequirementsSource: Application driver is not set.");
                return null;
            }
            if (poRequirementSource != null){ 
                return poRequirementSource;
            }
            poRequirementSource = new RequirementsSource();
            poRequirementSource.setApplicationDriver(poGRider);
            poRequirementSource.setWithParentClass(false);
            poRequirementSource.setLogWrapper(poLogWrapper);
            poRequirementSource.initialize();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        
        return poRequirementSource;
    }
    
    public RequirementsSourcePerGroup RequirementsSourcePerGroup(){
        try {
            if (poGRider == null) {
                poLogWrapper.severe("SalesControllers.RequirementsSourcePerGroup: Application driver is not set.");
                return null;
            }
            if (poRequirementSourcePerGroup != null){ 
                return poRequirementSourcePerGroup;
            }
            poRequirementSourcePerGroup = new RequirementsSourcePerGroup();
            poRequirementSourcePerGroup.setApplicationDriver(poGRider);
            poRequirementSourcePerGroup.setWithParentClass(false);
            poRequirementSourcePerGroup.setLogWrapper(poLogWrapper);
            poRequirementSourcePerGroup.initialize();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        
        return poRequirementSourcePerGroup;
    }

    public SalesBankApplication SalesBankApplication(){
        if (poGRider == null) {
            poLogWrapper.severe("SalesControllers.SalesBankApplication: Application driver is not set.");
            return null;
        }

        if (poSalesBankApp != null){
            return poSalesBankApp;
        }

        poSalesBankApp = new SalesBankApplication();
        poSalesBankApp.setApplicationDriver(poGRider);
        poSalesBankApp.setBranchCode(poGRider.getBranchCode());
        poSalesBankApp.setVerifyEntryNo(false);
        poSalesBankApp.setWithParent(false);
        poSalesBankApp.setLogWrapper(poLogWrapper);

        return poSalesBankApp;
    }
    public CustomerInquiryFollowUp CustomerInquiryFollowUp(){
        try {
            if (poGRider == null) {
                poLogWrapper.severe("SalesControllers.CustomerInquiryFollowUp: Application driver is not set.");
                return null;
            }
            if (poCustomerInquiryFollowUp != null){
                return poCustomerInquiryFollowUp;
            }
            poCustomerInquiryFollowUp = new CustomerInquiryFollowUp();
            poCustomerInquiryFollowUp.setApplicationDriver(poGRider);
            poCustomerInquiryFollowUp.setWithParentClass(false);
            poCustomerInquiryFollowUp.setLogWrapper(poLogWrapper);
            poCustomerInquiryFollowUp.initialize();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

        return poCustomerInquiryFollowUp;
    }
    
    public SalesCommitment SalesCommitment(){
        if (poGRider == null) {
            poLogWrapper.severe("SalesControllers.SalesCommitment: Application driver is not set.");
            return null;
        }

        if (poSalesCommitment != null){
            return poSalesCommitment;
        }

        poSalesCommitment = new SalesCommitment();
        poSalesCommitment.setApplicationDriver(poGRider);
        poSalesCommitment.setBranchCode(poGRider.getBranchCode());
        poSalesCommitment.setVerifyEntryNo(false);
        poSalesCommitment.setWithParent(false);
        poSalesCommitment.setLogWrapper(poLogWrapper);

        return poSalesCommitment;
    }

    public VSP VSP(){
        if (poGRider == null) {
            poLogWrapper.severe("SalesControllers.VSP: Application driver is not set.");
            return null;
        }

        if (poVSP != null){
            return poVSP;
        }

        poVSP = new VSP();
        poVSP.setApplicationDriver(poGRider);
        poVSP.setBranchCode(poGRider.getBranchCode());
        poVSP.setVerifyEntryNo(false);
        poVSP.setWithParent(false);
        poVSP.setLogWrapper(poLogWrapper);

        return poVSP;
    }
    public SalesReservation SalesReservation() throws SQLException, GuanzonException {
        if (poGRider == null) {
            poLogWrapper.severe("GLControllers.Disbursement: Application driver is not set.");
            return null;
        }

        if (poSalesReservation != null) {
            return poSalesReservation;
        }

        poSalesReservation = new SalesReservation();
        poSalesReservation.setApplicationDriver(poGRider);
        poSalesReservation.setBranchCode(poGRider.getBranchCode());
        poSalesReservation.setLogWrapper(poLogWrapper);
        poSalesReservation.setVerifyEntryNo(true);
        poSalesReservation.setWithParent(false);
        return poSalesReservation;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            poSalesInquiry = null;
            poSalesInquirySources = null;
            poSalesman = null;
            poSalesAgent = null;
            poSalesGiveaways = null;
            poRequirementSource = null;
            poRequirementSourcePerGroup = null;
            poCustomerInquiryFollowUp = null;
            poSalesCommitment = null;
            poVSP = null;
            poSalesReservation = null;

            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }

    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;

    private SalesInquiry poSalesInquiry;
    private SalesInquirySources poSalesInquirySources;
    private Salesman poSalesman;
    private SalesAgent poSalesAgent;
    private SalesGiveaways poSalesGiveaways;
    private RequirementsSource poRequirementSource;
    private RequirementsSourcePerGroup poRequirementSourcePerGroup;
    private SalesBankApplication poSalesBankApp;
    private CustomerInquiryFollowUp poCustomerInquiryFollowUp;
    private SalesCommitment poSalesCommitment;
    private VSP poVSP;
    private SalesReservation poSalesReservation;
}
