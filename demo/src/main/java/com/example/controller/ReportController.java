package com.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;
import java.util.ResourceBundle;

import com.example.*;
import com.example.model.*;

public class ReportController implements Initializable {
    @FXML private Button goHomeButton;

    @FXML private HBox monthPickerHBox;

    @FXML private DatePicker monthPicker;

    @FXML private TableView<PayStatementTableRowData> payStatementReportTable;

    @FXML private TableColumn<PayStatementTableRowData, Integer> payStatementReportTable_empIdCol;
    @FXML private TableColumn<PayStatementTableRowData, String> payStatementReportTable_nameCol;
    @FXML private TableColumn<PayStatementTableRowData, String> payStatementReportTable_payDateCol;
    @FXML private TableColumn<PayStatementTableRowData, Double> payStatementReportTable_earningsCol;

    @FXML private TableView<JobTitleTableRowData> jobTitleReportTable;

    @FXML private TableColumn<JobTitleTableRowData, String> jobTitleReportTable_jobTitleCol;
    @FXML private TableColumn<JobTitleTableRowData, Double> jobTitleReportTable_totalPayCol;

    @FXML private TableView<DivisionTableRowData> divisionReportTable;

    @FXML private TableColumn<DivisionTableRowData, String> divisionReportTable_divisionCol;
    @FXML private TableColumn<DivisionTableRowData, Double> divisionReportTable_totalPayCol;

    private int empId;
    private boolean isAdmin;
    private ReportTypes reportType;

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setReportType(ReportTypes reportType) {
        this.reportType = reportType;

        if (reportType == ReportTypes.EmployeePayStatement
            || reportType == ReportTypes.AdminPayStatement) {
            monthPickerHBox.setVisible(false);
            monthPickerHBox.setManaged(false);
            
            jobTitleReportTable.setVisible(false);
            jobTitleReportTable.setManaged(false);

            divisionReportTable.setVisible(false);
            divisionReportTable.setManaged(false);
        }
        else if (reportType == ReportTypes.AdminJobTitlePayment) {
            payStatementReportTable.setVisible(false);
            payStatementReportTable.setManaged(false);

            divisionReportTable.setVisible(false);
            divisionReportTable.setManaged(false);
        }
        else if (reportType == ReportTypes.AdminDivisionPayment) {
            payStatementReportTable.setVisible(false);
            payStatementReportTable.setManaged(false);

            jobTitleReportTable.setVisible(false);
            jobTitleReportTable.setManaged(false);
        }        
    }

    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    public ReportController() {
        
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                DB_URL = prop.getProperty("db.url");
                DB_USER = prop.getProperty("db.user");
                DB_PASSWORD = prop.getProperty("db.password");
            } else {
                throw new RuntimeException("db.properties file not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        payStatementReportTable_empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        payStatementReportTable_nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        payStatementReportTable_payDateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
        payStatementReportTable_earningsCol.setCellValueFactory(new PropertyValueFactory<>("earnings"));

        jobTitleReportTable_jobTitleCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        jobTitleReportTable_totalPayCol.setCellValueFactory(new PropertyValueFactory<>("totalPay"));

        divisionReportTable_divisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
        divisionReportTable_totalPayCol.setCellValueFactory(new PropertyValueFactory<>("totalPay"));
    }

    @FXML 
    private void handleViewReport(){
        try {
            if (reportType == ReportTypes.EmployeePayStatement
                || reportType == ReportTypes.AdminPayStatement)
                handleViewPayStatementReport(empId, isAdmin);
            else {
                LocalDate selectedMonth = monthPicker.getValue();
                if (selectedMonth == null){
                    showAlert("Error", "Please select a date");
                    return;
                }

                if (reportType == ReportTypes.AdminJobTitlePayment)
                    handleViewAdminJobTitlePaymentReport(selectedMonth);
                else if (reportType == ReportTypes.AdminDivisionPayment)
                    handleViewAdminDivisionPaymentReport(selectedMonth);
                else
                    throw new Exception("Unsupported report type");
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleViewAdminDivisionPaymentReport(LocalDate selectedMonth) {
        String query = """
            SELECT d.Name AS division, SUM(p.earnings) AS total_pay
            FROM employees e
            JOIN employee_division ed ON e.empId = ed.empId
            JOIN division d ON ed.div_ID = d.ID
            JOIN payroll p ON e.empId = p.empId
            WHERE MONTH(p.pay_date) = ? AND YEAR(p.pay_date) = ?
            GROUP BY d.Name
            ORDER BY d.Name
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedMonth.getMonthValue());
            stmt.setInt(2, selectedMonth.getYear());

            ResultSet rs = stmt.executeQuery();

            ObservableList<DivisionTableRowData> data = FXCollections.observableArrayList();
            
            while (rs.next()) {
                data.add(new DivisionTableRowData(
                    rs.getString("division"),
                    rs.getDouble("total_pay")
                ));
            }

            divisionReportTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleViewAdminJobTitlePaymentReport(LocalDate selectedMonth) {
        String query = """
            SELECT jt.job_title, SUM(p.earnings) AS total_pay
            FROM employees e
            JOIN employee_job_titles ejt ON e.empId = ejt.empId
            JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
            JOIN payroll p ON e.empId = p.empId
            WHERE MONTH(p.pay_date) = ? AND YEAR(p.pay_date) = ?
            GROUP BY jt.job_title
            ORDER BY jt.job_title
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selectedMonth.getMonthValue());
            stmt.setInt(2, selectedMonth.getYear());

            ResultSet rs = stmt.executeQuery();

            ObservableList<JobTitleTableRowData> data = FXCollections.observableArrayList();

            while (rs.next()) {
                data.add(new JobTitleTableRowData(
                    rs.getString("job_title"),
                    rs.getDouble("total_pay")
                ));
            }

            jobTitleReportTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleViewPayStatementReport(int empId, boolean isAdmin) {
        String query = """
            SELECT e.empId, e.Fname, e.Lname, p.pay_date, p.earnings, p.fed_tax, p.state_tax, p.retire_401k, p.health_care
            FROM employees e
            JOIN payroll p ON e.empId = p.empId
            WHERE 1 = 1
            """ + (isAdmin ? "" : " AND e.empId = ?") + """
            ORDER BY e.empId, p.pay_date
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (!isAdmin) {
                stmt.setInt(1, empId);
            }

            ResultSet rs = stmt.executeQuery();

            ObservableList<PayStatementTableRowData> data = FXCollections.observableArrayList();

            while (rs.next()) {
                data.add(new PayStatementTableRowData(
                        rs.getInt("empId"),
                        rs.getString("Fname") + " " + rs.getString("Lname"),
                        rs.getDate("pay_date").toString(),
                        rs.getDouble("earnings")
                ));
            }

            payStatementReportTable.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML 
    private void handleGoHome(){
        try {
            String home;
            String homeTitle;

            if (isAdmin) {
                home = "/admin_dashboard.fxml";
                homeTitle = "Admin Dashboard";
            }
            else {
                home = "/employee.fxml";
                homeTitle = "Employee Dashboard";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(home));
            Parent root = loader.load();

            Stage stage = (Stage) goHomeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
             stage.setTitle(homeTitle);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
