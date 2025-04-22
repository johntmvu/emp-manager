package com.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    public LoginController() {
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
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.toLowerCase().endsWith("@admin.com")) {
            handleAdminLogin(email, password);
        } else {
            handleEmployeeLogin(email, password);
        }
        
    }

    private void handleAdminLogin(String email, String password) {
        String query = "SELECT * FROM employees WHERE email = ? AND empid = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password); // Assuming password is the employee ID for admin login

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Load admin dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Admin Dashboard");
                stage.show();
            } else {
                showAlert("Login Failed", "Invalid admin credentials. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    private void handleEmployeeLogin(String email, String empIdInput) {
        String query = "SELECT * FROM employees WHERE email = ? AND empid = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, empIdInput);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int empId = rs.getInt("empid");
                // Load employee screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/employee.fxml"));
                Parent root = loader.load();

                // Pass empId to controller
                EmployeeController controller = loader.getController();
                controller.setEmployeeId(empId);

                // Switch to employee dashboard
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Employee Dashboard");
                stage.show();

            } else {
                showAlert("Login Failed", "Invalid employee credentials. Please try again.");
            }

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
