/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales.services;

import ph.com.guanzongroup.cas.sales.model.*;
import org.guanzon.appdriver.base.GRiderCAS;

/**
 *
 * @author MIS-PC
 */
public class SalesModels {
    
    public SalesModels(GRiderCAS applicationDriver){
        poGRider = applicationDriver;
    }
    
    public Model_Sales_Inquiry_Master SalesInquiryMaster(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesInquiryMaster: Application driver is not set.");
            return null;
        }
        
        if (poSalesInquiryMaster == null){
            poSalesInquiryMaster = new Model_Sales_Inquiry_Master();
            poSalesInquiryMaster.setApplicationDriver(poGRider);
            poSalesInquiryMaster.setXML("Model_Sales_Inquiry_Master");
            poSalesInquiryMaster.setTableName("Sales_Inquiry_Master");
            poSalesInquiryMaster.initialize();
        }

        return poSalesInquiryMaster;
    }
    
    public Model_Sales_Inquiry_Detail SalesInquiryDetails(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesInquiryDetails: Application driver is not set.");
            return null;
        }
        
        if (poSalesInquiryDetail == null){
            poSalesInquiryDetail = new Model_Sales_Inquiry_Detail();
            poSalesInquiryDetail.setApplicationDriver(poGRider);
            poSalesInquiryDetail.setXML("Model_Sales_Inquiry_Detail");
            poSalesInquiryDetail.setTableName("Sales_Inquiry_Detail");
            poSalesInquiryDetail.initialize();
        }

        return poSalesInquiryDetail;
    }
    
    public Model_Sales_Inquiry_Sources SalesInquirySources(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesInquirySources: Application driver is not set.");
            return null;
        }
        
        if (poSalesInquirySources == null){
            poSalesInquirySources = new Model_Sales_Inquiry_Sources();
            poSalesInquirySources.setApplicationDriver(poGRider);
            poSalesInquirySources.setXML("Model_Sales_Inquiry_Sources");
            poSalesInquirySources.setTableName("Sales_Inquiry_Sources");
            poSalesInquirySources.initialize();
        }

        return poSalesInquirySources;
    }
    
    public Model_Salesman Salesman(){
        if (poGRider == null){
            System.err.println("SalesModels.Salesman: Application driver is not set.");
            return null;
        }
        
        if (poSalesman == null){
            poSalesman = new Model_Salesman();
            poSalesman.setApplicationDriver(poGRider);
            poSalesman.setXML("Model_Salesman");
            poSalesman.setTableName("Salesman");
            poSalesman.initialize();
        }

        return poSalesman;
    }
    
    public Model_Sales_Agent SalesAgent(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesAgent: Application driver is not set.");
            return null;
        }
        
        if (poSalesAgent == null){
            poSalesAgent = new Model_Sales_Agent();
            poSalesAgent.setApplicationDriver(poGRider);
            poSalesAgent.setXML("Model_Sales_Agent");
            poSalesAgent.setTableName("Sales_Agent");
            poSalesAgent.initialize();
        }

        return poSalesAgent;
    }

    public Model_Sales_Giveaways_Master SalesGiveawaysMaster(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesGiveawaysMaster: Application driver is not set.");
            return null;
        }

        if (poSalesGiveawaysMaster == null){
            poSalesGiveawaysMaster = new Model_Sales_Giveaways_Master();
            poSalesGiveawaysMaster.setApplicationDriver(poGRider);
            poSalesGiveawaysMaster.setXML("Model_Sales_Giveaways_Master");
            poSalesGiveawaysMaster.setTableName("Sales_Giveaways_Master");
            poSalesGiveawaysMaster.initialize();
        }

        return poSalesGiveawaysMaster;
    }

    public Model_Sales_Giveaways_Item SalesGiveawaysItem(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesGiveawaysItem: Application driver is not set.");
            return null;
        }

        if (poSalesGiveawaysItem == null){
            poSalesGiveawaysItem = new Model_Sales_Giveaways_Item();
            poSalesGiveawaysItem.setApplicationDriver(poGRider);
            poSalesGiveawaysItem.setXML("Model_Sales_Giveaways_Item");
            poSalesGiveawaysItem.setTableName("Sales_Giveaways_Item");
            poSalesGiveawaysItem.initialize();
        }

        return poSalesGiveawaysItem;
    }

    public Model_Sales_Inquiry_Requirements SalesInquiryRequirements(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesInquiryRequirements: Application driver is not set.");
            return null;
        }
        
        if (poSalesInquiryRequirements == null){
            poSalesInquiryRequirements = new Model_Sales_Inquiry_Requirements();
            poSalesInquiryRequirements.setApplicationDriver(poGRider);
            poSalesInquiryRequirements.setXML("Model_Sales_Inquiry_Requirements");
            poSalesInquiryRequirements.setTableName("Sales_Inquiry_Requirements");
            poSalesInquiryRequirements.initialize();
        }

        return poSalesInquiryRequirements;
    }
    
    public Model_Bank_Application BankApplication(){
        if (poGRider == null){
            System.err.println("SalesModels.BankApplication: Application driver is not set.");
            return null;
        }
        
        if (poBankApplication == null){
            poBankApplication = new Model_Bank_Application();
            poBankApplication.setApplicationDriver(poGRider);
            poBankApplication.setXML("Model_Bank_Application");
            poBankApplication.setTableName("Bank_Application");
            poBankApplication.initialize();
        }

        return poBankApplication;
    }
    
    public Model_Requirement_Source RequirementSource(){
        if (poGRider == null){
            System.err.println("SalesModels.RequirementSource: Application driver is not set.");
            return null;
        }
        
        if (poRequirementSource == null){
            poRequirementSource = new Model_Requirement_Source();
            poRequirementSource.setApplicationDriver(poGRider);
            poRequirementSource.setXML("Model_Requirement_Source");
            poRequirementSource.setTableName("Requirement_Source");
            poRequirementSource.initialize();
        }

        return poRequirementSource;
    }
    
    public Model_Requirement_Source_PerGroup RequirementSourcePerGroup(){
        if (poGRider == null){
            System.err.println("SalesModels.RequirementSourcePerGroup: Application driver is not set.");
            return null;
        }
        
        if (poRequirementSourcePerGroup == null){
            poRequirementSourcePerGroup = new Model_Requirement_Source_PerGroup();
            poRequirementSourcePerGroup.setApplicationDriver(poGRider);
            poRequirementSourcePerGroup.setXML("Model_Requirement_Source_PerGroup");
            poRequirementSourcePerGroup.setTableName("Requirement_Source_PerGroup");
            poRequirementSourcePerGroup.initialize();
        }

        return poRequirementSourcePerGroup;
    }

    public Model_Customer_Inquiry_FollowUp CustomerInquiryFollowUp(){
        if (poGRider == null){
            System.err.println("SalesModels.CustomerInquiryFollowUp: Application driver is not set.");
            return null;
        }

        if (poSalesInquiryFollowUp == null){
            poSalesInquiryFollowUp = new Model_Customer_Inquiry_FollowUp();
            poSalesInquiryFollowUp.setApplicationDriver(poGRider);
            poSalesInquiryFollowUp.setXML("Model_Customer_Inquiry_FollowUp");
            poSalesInquiryFollowUp.setTableName("Customer_Inquiry_FollowUp");
            poSalesInquiryFollowUp.initialize();
        }

        return poSalesInquiryFollowUp;
    }

    public Model_Sales_Commitment_Master SalesCommitmentMaster(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesCommitmentMaster: Application driver is not set.");
            return null;
        }
        
        if (poSalesCommitmentMaster == null){
            poSalesCommitmentMaster = new Model_Sales_Commitment_Master();
            poSalesCommitmentMaster.setApplicationDriver(poGRider);
            poSalesCommitmentMaster.setXML("Model_Sales_Commitment_Master");
            poSalesCommitmentMaster.setTableName("Sales_Commitment_Master");
            poSalesCommitmentMaster.initialize();
        }

        return poSalesCommitmentMaster;
    }
    
    public Model_Sales_Commitment_Detail SalesCommitmentDetail(){
        if (poGRider == null){
            System.err.println("SalesModels.SalesCommitmentDetail: Application driver is not set.");
            return null;
        }
        
        if (poSalesCommitmentDetail == null){
            poSalesCommitmentDetail = new Model_Sales_Commitment_Detail();
            poSalesCommitmentDetail.setApplicationDriver(poGRider);
            poSalesCommitmentDetail.setXML("Model_Sales_Commitment_Detail");
            poSalesCommitmentDetail.setTableName("Sales_Commitment_Detail");
            poSalesCommitmentDetail.initialize();
        }

        return poSalesCommitmentDetail;
    }

    public Model_Vehicle_Release_Master VehicleReleaseMaster(){
        if (poGRider == null){
            System.err.println("SalesModels.VehicleReleaseMaster: Application driver is not set.");
            return null;
        }

        if (poVehicleReleaseMaster == null){
            poVehicleReleaseMaster = new Model_Vehicle_Release_Master();
            poVehicleReleaseMaster.setApplicationDriver(poGRider);
            poVehicleReleaseMaster.setXML("Model_Vehicle_Release_Master");
            poVehicleReleaseMaster.setTableName("Vehicle_Release_Master");
            poVehicleReleaseMaster.initialize();
        }

        return poVehicleReleaseMaster;
    }
    public Model_Vsp_Master VspMaster(){
        if (poGRider == null){
            System.err.println("SalesModels.Vsp: Application driver is not set.");
            return null;
        }

        if (poVspMaster == null){
            poVspMaster = new Model_Vsp_Master();
            poVspMaster.setApplicationDriver(poGRider);
            poVspMaster.setXML("Model_Vsp_Master");
            poVspMaster.setTableName("Vsp_Master");
            poVspMaster.initialize();
        }

        return poVspMaster;
    }

    public Model_Sales_Reservation_Master Sales_Reservation_Master(){
        if (poGRider == null){
            System.err.println("CashflowModels.Sales Reservation Master: Application driver is not set.");
            return null;
        }

        if (poSalesReservationMaster == null){
            poSalesReservationMaster = new Model_Sales_Reservation_Master();
            poSalesReservationMaster.setApplicationDriver(poGRider);
            poSalesReservationMaster.setXML("Model_Sales_Reservation_Master");
            poSalesReservationMaster.setTableName("Sales_Reservation_Master");
            poSalesReservationMaster.initialize();
        }

        return poSalesReservationMaster;
    }

    public Model_Sales_Reservation_Detail Sales_Reservation_Detail(){
        if (poGRider == null){
            System.err.println("CashflowModels.Sales Reservation Detail: Application driver is not set.");
            return null;
        }

        if (poSalesReservationDetail == null){
            poSalesReservationDetail = new Model_Sales_Reservation_Detail();
            poSalesReservationDetail.setApplicationDriver(poGRider);
            poSalesReservationDetail.setXML("Model_Sales_Reservation_Detail");
            poSalesReservationDetail.setTableName("Sales_Reservation_Detail");
            poSalesReservationDetail.initialize();
        }

        return poSalesReservationDetail;
    }
    public Model_Sales_Quotation_Master SalesQuotationMaster(){
        if (poGRider == null){
            System.err.println("CashflowModels.Sales Quotation Master: Application driver is not set.");
            return null;
        }

        if (poSalesQuotationMaster == null){
            poSalesQuotationMaster = new Model_Sales_Quotation_Master();
            poSalesQuotationMaster.setApplicationDriver(poGRider);
            poSalesQuotationMaster.setXML("Model_Sales_Quotation_Master");
            poSalesQuotationMaster.setTableName("Sales_Quotation_Master");
            poSalesQuotationMaster.initialize();
        }

        return poSalesQuotationMaster;
    }
    public Model_Sales_Quotations_FollowUp SalesQuotationFollowUp(){
        if (poGRider == null){
            System.err.println("CashflowModels.Sales Quotation Follow Up: Application driver is not set.");
            return null;
        }

        if (poSalesQuotationFollowUp == null){
            poSalesQuotationFollowUp = new Model_Sales_Quotations_FollowUp();
            poSalesQuotationFollowUp.setApplicationDriver(poGRider);
            poSalesQuotationFollowUp.setXML("Model_Sales_Quotations_FollowUp");
            poSalesQuotationFollowUp.setTableName("Sales_Quotation_FollowUp");
            poSalesQuotationMaster.initialize();
        }

        return poSalesQuotationFollowUp;
    }

    public Model_Sales_Quotations_Version_Master SalesQuotationVersionMaster(){
        if (poGRider == null){
            System.err.println("CashflowModels.Sales Quotation Version Master: Application driver is not set.");
            return null;
        }

        if (poSalesQuotationVersionMaster == null){
            poSalesQuotationVersionMaster = new Model_Sales_Quotations_Version_Master();
            poSalesQuotationVersionMaster.setApplicationDriver(poGRider);
            poSalesQuotationVersionMaster.setXML("Model_Sales_Quotations_Version_Master");
            poSalesQuotationVersionMaster.setTableName("Sales_Quotation_Version_Master");
            poSalesQuotationVersionMaster.initialize();
        }

        return poSalesQuotationVersionMaster;
    }

    public Model_Sales_Quotations_Version_Detail SalesQuotationVersionDetail(){
        if (poGRider == null){
            System.err.println("CashflowModels.Sales Quotation Version Detail: Application driver is not set.");
            return null;
        }

        if (poSalesQuotationVersionDetail == null){
            poSalesQuotationVersionDetail = new Model_Sales_Quotations_Version_Detail();
            poSalesQuotationVersionDetail.setApplicationDriver(poGRider);
            poSalesQuotationVersionDetail.setXML("Model_Sales_Quotations_Version_Detail");
            poSalesQuotationVersionDetail.setTableName("Sales_Quotation_Version_Detail");
            poSalesQuotationVersionDetail.initialize();
        }

        return poSalesQuotationVersionDetail;
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {                    
            poSalesInquiryMaster = null;
            poSalesInquiryDetail = null;
            poSalesInquirySources = null;
            poSalesman = null;
            poSalesAgent = null;
            poSalesInquiryRequirements = null;
            poSalesGiveawaysMaster = null;
            poSalesGiveawaysItem = null;
            poRequirementSource = null;
            poRequirementSourcePerGroup = null;
            poSalesInquiryFollowUp = null;
            poSalesCommitmentMaster = null;
            poSalesCommitmentDetail = null;
            poVehicleReleaseMaster = null;
            poVspMaster = null;
            poSalesReservationMaster = null;
            poSalesReservationDetail = null;
            poSalesQuotationMaster = null;
            poSalesQuotationFollowUp = null;
            poSalesQuotationVersionMaster = null;
            poSalesQuotationVersionDetail = null;

            poGRider = null;
        } finally {
            super.finalize();
        }
    }
    
    private GRiderCAS poGRider;

    private Model_Sales_Inquiry_Master poSalesInquiryMaster;
    private Model_Sales_Inquiry_Detail poSalesInquiryDetail;
    private Model_Sales_Inquiry_Sources poSalesInquirySources;
    private Model_Salesman poSalesman;
    private Model_Sales_Agent poSalesAgent;
    private Model_Sales_Inquiry_Requirements poSalesInquiryRequirements;
    private Model_Sales_Giveaways_Master poSalesGiveawaysMaster;
    private Model_Sales_Giveaways_Item poSalesGiveawaysItem;
    private Model_Bank_Application poBankApplication;
    private Model_Requirement_Source poRequirementSource;
    private Model_Requirement_Source_PerGroup poRequirementSourcePerGroup;
    private Model_Customer_Inquiry_FollowUp poSalesInquiryFollowUp;
    private Model_Sales_Commitment_Master poSalesCommitmentMaster;
    private Model_Sales_Commitment_Detail poSalesCommitmentDetail;
    private Model_Vehicle_Release_Master poVehicleReleaseMaster;
    private Model_Vsp_Master poVspMaster;
    private Model_Sales_Reservation_Master poSalesReservationMaster;
    private Model_Sales_Reservation_Detail poSalesReservationDetail;
    private Model_Sales_Quotation_Master poSalesQuotationMaster;
    private Model_Sales_Quotations_FollowUp poSalesQuotationFollowUp;
    private Model_Sales_Quotations_Version_Master poSalesQuotationVersionMaster;
    private Model_Sales_Quotations_Version_Detail poSalesQuotationVersionDetail;
}
