package com.example.controller;

import com.example.ReportTypes;
import com.example.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class AdminDashboardController {

    @FXML
    private TableView<Employee> employeeTable;

    @FXML
    private TableColumn<Employee, Integer> idCol;

    @FXML
    private TableColumn<Employee, String> nameCol;

    @FXML
    private TableColumn<Employee, String> emailCol;

    @FXML
    private TableColumn<Employee, String> jobTitleCol;

    @FXML
    private TableColumn<Employee, String> divisionCol;

    @FXML
    private TableColumn<Employee, Double> salaryCol;

    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    public AdminDashboardController() {
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
    }

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        jobTitleCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        divisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        loadEmployees();
    }

    private void loadEmployees() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();

        String query = """
            SELECT e.empid, CONCAT(e.Fname, ' ', e.Lname) AS name, e.email,
                jt.job_title, d.Name AS division, e.Salary
            FROM employees e
            LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid
            LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
            LEFT JOIN employee_division ed ON e.empid = ed.empid
            LEFT JOIN division d ON ed.div_ID = d.ID
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(new Employee(
                        rs.getInt("empid"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("job_title"),
                        rs.getString("division"),
                        rs.getDouble("Salary")
                ));
            }

            employeeTable.setItems(employees);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML 
    private void handleViewPayStatementReport(){
        handleViewReport(ReportTypes.AdminPayStatement);    
    }

    @FXML 
    private void handleViewJobTitlePaymentReport(){
        handleViewReport(ReportTypes.AdminJobTitlePayment);    
    }

    @FXML 
    private void handleViewDivisionPaymentReport(){
        handleViewReport(ReportTypes.AdminDivisionPayment);    
    }

    private void handleViewReport(ReportTypes reportType){
        try {
            String reportTitle = "";
            if (reportType == ReportTypes.AdminPayStatement)
                reportTitle = "Admin Pay Statements";
            else if (reportType == ReportTypes.AdminJobTitlePayment)
                reportTitle = "Admin Job Title Payments";
            else if (reportType == ReportTypes.AdminDivisionPayment)
                reportTitle = "Admin Division Payments";

            // Load report
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/report.fxml"));
            Parent root = loader.load();

            ReportController reportController = loader.getController();
            
            reportController.setIsAdmin(true);
            reportController.setReportType(reportType);

            Stage stage = (Stage) employeeTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(reportTitle);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}