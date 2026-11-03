/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.sales;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Parameter;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.model.Model_Salesman;
import ph.com.guanzongroup.cas.sales.services.SalesModels;

/**
 *
 * @author Arsiela
 */
public class Salesman extends Parameter {
    Model_Salesman poModel;
    List<Model_Salesman> paModelList;

    @Override
    public void initialize() throws SQLException, GuanzonException {
        psRecdStat = RecordStatus.ACTIVE;
        poModel = new SalesModels(poGRider).Salesman();
        paModelList = new ArrayList<>();
        super.initialize();
    }

    @Override
    public Model_Salesman getModel() {
        return poModel;
    }

    public void resetModel() {
        poModel = new SalesModels(poGRider).Salesman();
    }

    @Override
    public JSONObject isEntryOkay() throws SQLException {
        poJSON = new JSONObject();
        if (poGRider.getUserLevel() < UserRight.BRANCH_MANAGER) {
            poJSON.put("result", "error");
            poJSON.put("message", "User is not allowed to save record.");
            return poJSON;
        }
        if (poModel.getEmployeeId().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Employee ID must not be empty.");
            return poJSON;
        }
        if (poModel.getBranchCode().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Branch must not be empty.");
            return poJSON;
        }
        if (poModel.getLastName().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "Last name must not be empty.");
            return poJSON;
        }
        if (poModel.getFirstName().isEmpty()) {
            poJSON.put("result", "error");
            poJSON.put("message", "First name must not be empty.");
            return poJSON;
        }
        //Middle name is not required
//        if (poModel.getMiddleName().isEmpty()) {
//            poJSON.put("result", "error");
//            poJSON.put("message", "Middle name must not be empty.");
//            return poJSON;
//        }

        poModel.setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        poModel.setModifiedDate(poGRider.getServerDate());
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject searchRecord(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsCondition = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsCondition = " a.cRecdStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = " a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }

        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), lsCondition);

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Salesman",
                "sEmployID»sFullName",
                "a.sEmployID»concat(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sEmployID"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }


    public JSONObject searchRecord(String value, boolean byCode, String branch) throws SQLException, GuanzonException {
        String lsCondition = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsCondition = " AND a.cRecdStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = " AND a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }

        String lsSQL = MiscUtil.addCondition(getSQ_Browse(), "a.sBranchCd = " + SQLUtil.toSQL(branch));
        if(lsCondition != null && !"".equals(lsCondition)){
            lsSQL = lsSQL + lsCondition;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Salesman",
                "sEmployID»sFullName",
                "a.sEmployID»concat(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sEmployID"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject loadModelList() {
        try {

//            String lsSQL = MiscUtil.addCondition(getSQ_Browse()," a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
            String lsSQL = getSQ_Browse();
            lsSQL = lsSQL + " ORDER BY a.sLastName DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paModelList = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sEmployID: " + loRS.getString("sEmployID"));
                    System.out.println("sFullName: " + loRS.getString("sFullName"));
                    System.out.println("------------------------------------------------------------------------------");

                    paModelList.add(SalesmanModel());
                    paModelList.get(paModelList.size() - 1).openRecord(loRS.getString("sEmployID"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paModelList = new ArrayList<>();
                paModelList.add(SalesmanModel());
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

    private Model_Salesman SalesmanModel() {
        return new SalesModels(poGRider).Salesman();
    }
    public int getModelCount() {
        if (paModelList == null) {
            paModelList = new ArrayList<>();
        }

        return paModelList.size();
    }

    public Model_Salesman ModelList(int row) {
        return (Model_Salesman) paModelList.get(row);
    }

    public JSONObject searchEmployee(String value,String branch, String Department, boolean byCode) throws SQLException, GuanzonException {
        String lsCondition = "";
//        if (psRecdStat != null) {
//            if (psRecdStat.length() > 1) {
//                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
//                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
//                }
//                lsCondition = " AND a.cRecdStat IN (" + lsCondition.substring(2) + ")";
//            } else {
//                lsCondition = " AND a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
//            }
//        }
        String lsBranchCd;
        String lsDepartment;

        boolean main = poGRider.isMainOffice() || poGRider.isWarehouse();

        System.out.println("isMainOffice: " + poGRider.isMainOffice()
                + ", isWarehouse: " + poGRider.isWarehouse());

        if (main) {
            // Main Office / Warehouse
            lsBranchCd = (branch == null || branch.trim().isEmpty())
                    ? null
                    : branch.trim();

            lsDepartment = (Department == null || Department.trim().isEmpty())
                    ? "015"
                    : Department.trim();

        } else {
            // Branch User
            lsBranchCd = poGRider.getBranchCode();
            lsDepartment = poGRider.getDepartment();
        }

        String lsFilter = "a.cRecdStat = '1'";

        if (lsBranchCd != null && !lsBranchCd.trim().isEmpty()) {
            lsFilter += " AND a.sBranchCd = " + SQLUtil.toSQL(lsBranchCd);
        }

        if (lsDepartment != null && !lsDepartment.trim().isEmpty()) {
            lsFilter += " AND a.sDeptIDxx = " + SQLUtil.toSQL(lsDepartment);
        }

        String lsSQL = MiscUtil.addCondition(getSQ_Employee(), lsFilter);

        if (lsCondition != null && !lsCondition.isEmpty()) {
            lsSQL += lsCondition;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Employee ID»Name",
                "sEmployID»sCompnyNm",
                "a.sEmployID»b.sCompnyNm",
                byCode ? 0 : 1);

        if (poJSON != null) {
            for(int lnCtr = 0; lnCtr <= getModelCount() - 1; lnCtr++){
                if(((String) poJSON.get("sEmployID")).equals(ModelList(lnCtr).getEmployeeId())){
                    poJSON.put("result", "error");
                    poJSON.put("message", "Employee already exists in the list.");
                    return poJSON;
                }
            }
            poModel.setEmployeeId((String) poJSON.get("sEmployID"));
            poModel.setLastName((String) poJSON.get("sLastName"));
            poModel.setFirstName((String) poJSON.get("sFrstName"));
            poModel.setMiddleName((String) poJSON.get("sMiddName"));
            poModel.setBranchCode((String) poJSON.get("sBranchCd"));
            poJSON.put("result", "success");
            return poJSON;
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject SearchBranch(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            poModel.setBranchCode(object.getModel().getBranchCode());
        }

        return poJSON;
    }

    public JSONObject SearchDepartment(String value, boolean byCode)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);
        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            poJSON.put("deptID", object.getModel().getDepartmentId());
            poJSON.put("department", object.getModel().getDescription());
        }

        return poJSON;
    }

    @Override
    public String getSQ_Browse() {
        return    " SELECT "
                + "    a.sEmployID "
                + "  , a.sBranchCd "
                + "  , a.sLastName "
                + "  , a.sFrstName "
                + "  , a.sMiddName "
                + "  , a.cRecdStat "
                + "  , CONCAT(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName) as sFullName "
                + " FROM salesman a ";
//                + " LEFT JOIN ggc_isysdbf.client_master b on b.sClientID = a.sEmployID ";
    }

    public String getSQ_Employee() {
        return  " SELECT "
                + "   a.sEmployID "
                + " , a.sBranchCd "
                + " , a.dFiredxxx "
                + " , a.cRecdStat "
                + " , b.sCompnyNm "
                + " , b.sLastName "
                + " , b.sFrstName "
                + " , b.sMiddName "
                + " , c.sBranchNm "
                + " FROM Employee_Master001 a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployID "
                + " LEFT JOIN Branch c ON c.sBranchCd = a.sBranchCd "
                + " LEFT JOIN Department d ON a.sDeptIDxx = d.sDeptIDxx "
                + " LEFT JOIN Position e ON a.sPositnID = e.sPositnID ";

    }
    public String getSalesman() {

        return " SELECT "
                + "    a.sEmployID "
                + "  , a.sBranchCd "
                + "   , concat(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName) as sFullName "
                + "  , a.cRecdStat "
                + "  , c.sCompnyNm "
                + "  , e.sDeptName "
                + "  , d.sBranchNm "
                + "  , f.sPositnNm "
                + " FROM Salesman a "
                + " LEFT JOIN Employee_Master001 b "
                + "        ON a.sEmployID = b.sEmployID "
                + " LEFT JOIN Client_Master c "
                + "        ON a.sEmployID = c.sClientID "
                + " LEFT JOIN Branch d "
                + "        ON d.sBranchCd = a.sBranchCd "
                + " LEFT JOIN Department e "
                + "        ON b.sDeptIDxx = e.sDeptIDxx "
                + " LEFT JOIN Position f "
                + "        ON b.sPositnID = f.sPositnID ";
    }


    public JSONObject searchSalesMan(String value, boolean byCode) throws SQLException, GuanzonException {
        String lsCondition = "";
        if (psRecdStat != null) {
            if (psRecdStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psRecdStat.length() - 1; lnCtr++) {
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(psRecdStat.charAt(lnCtr)));
                }
                lsCondition = " a.cRecdStat IN (" + lsCondition.substring(2) + ")";
            } else {
                lsCondition = " a.cRecdStat = " + SQLUtil.toSQL(psRecdStat);
            }
        }

        String lsSQL = MiscUtil.addCondition(getSalesman(), lsCondition);

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Employee ID»Salesman",
                "sEmployID»sFullName",
                "a.sEmployID»concat(a.sLastName,', ',a.sFrstName, ' ',a.sMiddName)",
                byCode ? 0 : 1);

        if (poJSON != null) {
            return poModel.openRecord((String) poJSON.get("sEmployID"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }

    public JSONObject  getOtherInfos(String fsEmployID) throws SQLException {
        JSONObject loResult = new JSONObject();

        String lsSQL = " SELECT "
                + "   a.sEmployID "
                + " , a.sBranchCd "
                + " , a.dFiredxxx "
                + " , a.cRecdStat "
                + " , b.sCompnyNm "
                + " , b.sLastName "
                + " , b.sFrstName "
                + " , b.sMiddName "
                + " , c.sBranchNm "
                + " , d.sDeptName "
                + " , e.sPositnNm "
                + " FROM Employee_Master001 a "
                + " LEFT JOIN Client_Master b ON b.sClientID = a.sEmployID "
                + " LEFT JOIN Branch c ON c.sBranchCd = a.sBranchCd "
                + " LEFT JOIN Department d ON a.sDeptIDxx = d.sDeptIDxx "
                + " LEFT JOIN Position e ON a.sPositnID = e.sPositnID ";

        lsSQL = MiscUtil.addCondition(lsSQL,
                " a.sEmployID = " + SQLUtil.toSQL(fsEmployID));
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        if (!loRS.next()) {
            loResult.put("result", "error");
            loResult.put("message", "Employee  not found.");
            MiscUtil.close(loRS);
            return loResult;
        }

        loResult.put("position", loRS.getString("sPositnNm"));
        loResult.put("department", loRS.getString("sDeptName"));
        loResult.put("branch", loRS.getString("sBranchNm"));

        loResult.put("result", "success");
        loResult.put("message", "success");

        MiscUtil.close(loRS);

        return loResult;
    }
}
