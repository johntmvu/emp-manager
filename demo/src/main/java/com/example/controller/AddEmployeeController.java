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

    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

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
        System.out.println("Add Employee button clicked!");

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String salary = salaryField.getText();
        String ssn = ssnField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || salary.isEmpty() || ssn.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        int nextEmpId = getNextEmpId();

        if (nextEmpId == -1) {
            showAlert("Error", "Failed to calculate the next employee ID.");
            return;
        }

        String query = "INSERT INTO employees (empid, fname, lname, email, hiredate, salary, ssn) VALUES (?, ?, ?, ?, CURDATE(), ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, nextEmpId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            stmt.setDouble(5, Double.parseDouble(salary));
            stmt.setString(6, ssn);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                showAlert("Success", "Employee added successfully.");
                clearFields();
            } else {
                showAlert("Error", "Failed to add employee.");
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

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        salaryField.clear();
        ssnField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}