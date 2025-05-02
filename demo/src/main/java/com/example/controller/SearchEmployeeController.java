package com.example.controller;

import com.example.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SearchEmployeeController {

    @FXML
    private TextField empIdField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField dobField;

    @FXML
    private TextField ssnField;

    private AdminDashboardController adminDashboardController;

    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    public SearchEmployeeController() {
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

    public void setAdminDashboardController(AdminDashboardController adminDashboardController) {
        this.adminDashboardController = adminDashboardController;
    }

    @FXML
    public void handleSearch() {
        String empId = empIdField.getText().trim();
        String name = nameField.getText().trim();
        String dob = dobField.getText().trim();
        String ssn = ssnField.getText().trim();
    
        if (empId.isEmpty() && name.isEmpty() && dob.isEmpty() && ssn.isEmpty()) {
            showAlert("Error", "Please fill in at least one field to search.");
            return;
        }
    
        System.out.println("Search Parameters:");
        System.out.println("empId: " + empId);
        System.out.println("name: " + name);
        System.out.println("dob: " + dob);
        System.out.println("ssn: " + ssn);
    
        ObservableList<Employee> searchResults = FXCollections.observableArrayList();
    
        String query = """
            SELECT e.empid, CONCAT(e.Fname, ' ', e.Lname) AS name, e.email,
                   jt.job_title, d.Name AS division, e.Salary
            FROM employees e
            LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid
            LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id
            LEFT JOIN employee_division ed ON e.empid = ed.empid
            LEFT JOIN division d ON ed.div_ID = d.ID
            LEFT JOIN address a ON e.empid = a.empid
            WHERE (? IS NULL OR e.empid = ?)
              AND (? IS NULL OR CONCAT(e.Fname, ' ', e.Lname) LIKE ?)
              AND (? IS NULL OR a.DOB = ?)
              AND (? IS NULL OR e.SSN = ?)
        """;
    
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setString(1, empId.isEmpty() ? null : empId);
            stmt.setString(2, empId.isEmpty() ? null : empId);
            stmt.setString(3, name.isEmpty() ? null : name);
            stmt.setString(4, name.isEmpty() ? null : "%" + name + "%");
            stmt.setString(5, dob.isEmpty() ? null : dob);
            stmt.setString(6, dob.isEmpty() ? null : dob);
            stmt.setString(7, ssn.isEmpty() ? null : ssn);
            stmt.setString(8, ssn.isEmpty() ? null : ssn);
    
            ResultSet rs = stmt.executeQuery();
    
            while (rs.next()) {
                searchResults.add(new Employee(
                        rs.getInt("empid"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("job_title"),
                        rs.getString("division"),
                        rs.getDouble("Salary")
                ));
            }
    
            if (adminDashboardController != null) {
                adminDashboardController.updateEmployeeTable(searchResults);
            }
    
            // Close the search window
            Stage stage = (Stage) empIdField.getScene().getWindow();
            stage.close();
    
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