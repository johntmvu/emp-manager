package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UpdateSalariesController {

    @FXML
    private TextField percentageField;

    @FXML
    private TextField minSalaryField;

    @FXML
    private TextField maxSalaryField;

    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    private AdminDashboardController adminDashboardController;

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

    public UpdateSalariesController() {
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

    @FXML
    public void handleUpdateSalaries() {
        String percentageText = percentageField.getText();
        String minSalaryText = minSalaryField.getText();
        String maxSalaryText = maxSalaryField.getText();

        if (percentageText.isEmpty() || minSalaryText.isEmpty() || maxSalaryText.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        try {
            double percentageIncrease = Double.parseDouble(percentageText);
            double minSalary = Double.parseDouble(minSalaryText);
            double maxSalary = Double.parseDouble(maxSalaryText);

            String updateSalariesQuery = """
                UPDATE employees
                SET salary = salary + (salary * ? / 100)
                WHERE salary >= ? AND salary < ?
            """;

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(updateSalariesQuery)) {

                stmt.setDouble(1, percentageIncrease);
                stmt.setDouble(2, minSalary);
                stmt.setDouble(3, maxSalary);

                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    showAlert("Success", rowsUpdated + " employees' salaries updated successfully.");
                    
                    // Notify AdminDashboardController to refresh the employee list
                    if (adminDashboardController != null) {
                        adminDashboardController.reloadEmployeeList();
                    }
                } else {
                    showAlert("Info", "No employees found in the specified salary range.");
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid input. Please enter valid numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage());
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