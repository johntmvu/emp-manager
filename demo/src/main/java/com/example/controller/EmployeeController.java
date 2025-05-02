package com.example.controller;

import com.example.ReportTypes;
import com.example.model.PayrollRecord;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EmployeeController implements Initializable {

    @FXML private Label nameLabel, emailLabel, jobTitleLabel, divisionLabel, addressLabel, phoneLabel,
                        demographicLabel, hireDateLabel, salaryLabel;

    @FXML private TableView<PayrollRecord> payrollTable;
    @FXML private TableColumn<PayrollRecord, Date> dateCol;
    @FXML private TableColumn<PayrollRecord, Double> earningsCol, fedTaxCol, stateTaxCol, retireCol, healthCol;

    private int empId;
    private String DB_URL, DB_USER, DB_PASSWORD;

    public void setEmployeeId(int empId) {
        this.empId = empId;
        loadEmployeeInfo();
        loadPayrollData();
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
        earningsCol.setCellValueFactory(new PropertyValueFactory<>("earnings"));
        fedTaxCol.setCellValueFactory(new PropertyValueFactory<>("fedTax"));
        stateTaxCol.setCellValueFactory(new PropertyValueFactory<>("stateTax"));
        retireCol.setCellValueFactory(new PropertyValueFactory<>("retire401k"));
        healthCol.setCellValueFactory(new PropertyValueFactory<>("healthCare"));
    }

    private void loadEmployeeInfo() {
        String query = """
            SELECT e.Fname, e.Lname, e.email, e.HireDate, e.Salary,
                   jt.job_title,
                   d.Name AS division,
                   a.street, c.city_name, s.state_name, a.zip_code,
                   a.gender, a.race, a.DOB, a.phone_number
            FROM employees e
            LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid
            LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
            LEFT JOIN employee_division ed ON e.empid = ed.empid
            LEFT JOIN division d ON ed.div_ID = d.ID
            LEFT JOIN address a ON e.empid = a.empid
            LEFT JOIN city c ON a.city_id = c.city_id
            LEFT JOIN state s ON a.state_id = s.state_id
            WHERE e.empid = ?
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, empId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameLabel.setText("Name: " + rs.getString("Fname") + " " + rs.getString("Lname"));
                emailLabel.setText("Email: " + rs.getString("email"));
                jobTitleLabel.setText("Job Title: " + safe(rs.getString("job_title")));
                divisionLabel.setText("Division: " + safe(rs.getString("division")));

                String street = safe(rs.getString("street"));
                String city = safe(rs.getString("city_name"));
                String state = safe(rs.getString("state_name"));
                String zip = safe(rs.getString("zip_code"));
                addressLabel.setText("Address: " + street + ", " + city + ", " + state + " " + zip);

                phoneLabel.setText("Phone: " + safe(rs.getString("phone_number")));

                String gender = safe(rs.getString("gender"));
                String race = safe(rs.getString("race"));
                Date dob = rs.getDate("DOB");
                demographicLabel.setText("Gender: " + gender + ", Race: " + race + ", DOB: " + (dob != null ? dob : "N/A"));

                Date hireDate = rs.getDate("HireDate");
                hireDateLabel.setText("Hire Date: " + (hireDate != null ? hireDate.toString() : "N/A"));

                salaryLabel.setText("Salary: $" + rs.getDouble("Salary"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            // Get the current stage and set the login scene
            Stage stage = (Stage) payrollTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPayrollData() {
        String query = """
            SELECT pay_date, earnings, fed_tax, state_tax, retire_401k, health_care
            FROM payroll
            WHERE empid = ?
            ORDER BY pay_date DESC
        """;

        ObservableList<PayrollRecord> data = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, empId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                data.add(new PayrollRecord(
                        rs.getDate("pay_date"),
                        rs.getDouble("earnings"),
                        rs.getDouble("fed_tax"),
                        rs.getDouble("state_tax"),
                        rs.getDouble("retire_401k"),
                        rs.getDouble("health_care")
                ));
            }

            payrollTable.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Gracefully handle null values
    private String safe(String input) {
        return input != null ? input : "N/A";
    }

    @FXML 
    private void handleViewPayStatementReport(){
        try {
            // Load report
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/report.fxml"));
            Parent root = loader.load();

            ReportController reportController = loader.getController();
            
            reportController.setEmpId(empId);
            reportController.setIsAdmin(false);
            reportController.setReportType(ReportTypes.EmployeePayStatement);

            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Employee Pay Statements");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}