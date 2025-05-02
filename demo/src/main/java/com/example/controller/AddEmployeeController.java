package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class AddEmployeeController {

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

    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    private AdminDashboardController adminDashboardController;

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

    public AddEmployeeController() {
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
    public void handleAddEmployee() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String salary = salaryField.getText();
        String ssn = ssnField.getText();
        String jobTitle = jobTitleField.getText();
        String divisionName = divisionField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || salary.isEmpty() || ssn.isEmpty() || jobTitle.isEmpty() || divisionName.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        int nextEmpId = getNextEmpId();

        if (nextEmpId == -1) {
            showAlert("Error", "Failed to calculate the next employee ID.");
            return;
        }

        int divisionId = getDivisionId(divisionName);

        if (divisionId == -1) {
            showAlert("Error", "Division not found. Please ensure the division exists.");
            return;
        }

        String insertEmployeeQuery = "INSERT INTO employees (empid, fname, lname, email, hiredate, salary, ssn) VALUES (?, ?, ?, ?, CURDATE(), ?, ?)";
        String insertJobTitleQuery = "INSERT INTO employee_job_titles (empid, job_title_id) VALUES (?, (SELECT job_title_id FROM job_titles WHERE job_title = ?))";
        String insertDivisionQuery = "INSERT INTO employee_division (empid, div_ID) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Insert into employees table
            try (PreparedStatement stmt = conn.prepareStatement(insertEmployeeQuery)) {
                stmt.setInt(1, nextEmpId);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, email);
                stmt.setDouble(5, Double.parseDouble(salary));
                stmt.setString(6, ssn);

                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    // Insert into employee_job_titles table
                    try (PreparedStatement jobTitleStmt = conn.prepareStatement(insertJobTitleQuery)) {
                        jobTitleStmt.setInt(1, nextEmpId);
                        jobTitleStmt.setString(2, jobTitle);
                        jobTitleStmt.executeUpdate();
                    }

                    // Insert into employee_division table
                    try (PreparedStatement divisionStmt = conn.prepareStatement(insertDivisionQuery)) {
                        divisionStmt.setInt(1, nextEmpId);
                        divisionStmt.setInt(2, divisionId);
                        divisionStmt.executeUpdate();
                    }

                    showAlert("Success", "Employee added successfully.");
                    clearFields();

                    // Notify AdminDashboardController to refresh the employee list
                    if (adminDashboardController != null) {
                        adminDashboardController.reloadEmployeeList();
                    }
                } else {
                    showAlert("Error", "Failed to add employee.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    private int getNextEmpId() {
        String query = "SELECT MAX(empid) AS max_id FROM employees";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("max_id") + 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to retrieve the next employee ID: " + e.getMessage());
        }

        return -1; // Return -1 if there is an error
    }

    private int getDivisionId(String divisionName) {
        String query = "SELECT ID FROM division WHERE Name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, divisionName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("ID");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to retrieve division ID: " + e.getMessage());
        }

        return -1; // Return -1 if division not found
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        salaryField.clear();
        ssnField.clear();
        jobTitleField.clear();
        divisionField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}