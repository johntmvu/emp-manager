package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditEmployeeController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField salaryField;

    @FXML
    private TextField ssnField;

    @FXML
    private TextField jobTitleField;

    @FXML
    private TextField divisionField;

    private int employeeId;

    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    private AdminDashboardController adminDashboardController;

    public EditEmployeeController() {
        try (var input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            var prop = new java.util.Properties();
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

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
        loadEmployeeData();
    }

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

    private void loadEmployeeData() {
        String query = """
            SELECT e.fname, e.lname, e.email, e.salary, e.ssn,
                   jt.job_title, d.Name AS division
            FROM employees e
            LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid
            LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
            LEFT JOIN employee_division ed ON e.empid = ed.empid
            LEFT JOIN division d ON ed.div_ID = d.ID
            WHERE e.empid = ?
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                firstNameField.setText(rs.getString("fname"));
                lastNameField.setText(rs.getString("lname"));
                emailField.setText(rs.getString("email"));
                salaryField.setText(String.valueOf(rs.getDouble("salary")));
                ssnField.setText(rs.getString("ssn"));
                jobTitleField.setText(rs.getString("job_title"));
                divisionField.setText(rs.getString("division"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load employee data: " + e.getMessage());
        }
    }

    @FXML
    public void handleSaveChanges() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String salary = salaryField.getText();
        String ssn = ssnField.getText();
        String jobTitle = jobTitleField.getText();
        String division = divisionField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || salary.isEmpty() || ssn.isEmpty() || jobTitle.isEmpty() || division.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        String updateEmployeeQuery = "UPDATE employees SET fname = ?, lname = ?, email = ?, salary = ?, ssn = ? WHERE empid = ?";
        String updateJobTitleQuery = "UPDATE employee_job_titles SET job_title_id = (SELECT job_title_id FROM job_titles WHERE job_title = ?) WHERE empid = ?";
        String updateDivisionQuery = "UPDATE employee_division SET div_ID = (SELECT ID FROM division WHERE Name = ?) WHERE empid = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Update employees table
            try (PreparedStatement stmt = conn.prepareStatement(updateEmployeeQuery)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setDouble(4, Double.parseDouble(salary));
                stmt.setString(5, ssn);
                stmt.setInt(6, employeeId);
                stmt.executeUpdate();
            }

            // Update employee_job_titles table
            try (PreparedStatement stmt = conn.prepareStatement(updateJobTitleQuery)) {
                stmt.setString(1, jobTitle);
                stmt.setInt(2, employeeId);
                stmt.executeUpdate();
            }

            // Update employee_division table
            try (PreparedStatement stmt = conn.prepareStatement(updateDivisionQuery)) {
                stmt.setString(1, division);
                stmt.setInt(2, employeeId);
                stmt.executeUpdate();
            }

            showAlert("Success", "Employee information updated successfully.");

            // Notify AdminDashboardController to refresh the employee list
            if (adminDashboardController != null) {
                adminDashboardController.reloadEmployeeList();
            }

            // Close the edit window
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update employee data: " + e.getMessage());
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