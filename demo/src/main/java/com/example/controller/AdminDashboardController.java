package com.example.controller;

import com.example.ReportTypes;
import com.example.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

    @FXML
    private TextField searchField; // Add this field

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

    @FXML
    public void handleAddEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_employee.fxml"));
            Parent root = loader.load();

            AddEmployeeController addEmployeeController = loader.getController();
            addEmployeeController.setAdminDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Employee");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadEmployeeList() {
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
    public void handleDeleteEmployee() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();

        if (selectedEmployee == null) {
            showAlert("Error", "No employee selected. Please select an employee to delete.");
            return;
        }

        String deleteDivisionQuery = "DELETE FROM employee_division WHERE empid = ?";
        String deleteJobTitleQuery = "DELETE FROM employee_job_titles WHERE empid = ?";
        String deleteEmployeeQuery = "DELETE FROM employees WHERE empid = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Start a transaction
            conn.setAutoCommit(false);

            // Delete from employee_division table
            try (PreparedStatement divisionStmt = conn.prepareStatement(deleteDivisionQuery)) {
                divisionStmt.setInt(1, selectedEmployee.getId());
                divisionStmt.executeUpdate();
            }

            // Delete from employee_job_titles table
            try (PreparedStatement jobTitleStmt = conn.prepareStatement(deleteJobTitleQuery)) {
                jobTitleStmt.setInt(1, selectedEmployee.getId());
                jobTitleStmt.executeUpdate();
            }

            // Delete from employees table
            try (PreparedStatement employeeStmt = conn.prepareStatement(deleteEmployeeQuery)) {
                employeeStmt.setInt(1, selectedEmployee.getId());
                int rowsDeleted = employeeStmt.executeUpdate();

                if (rowsDeleted > 0) {
                    conn.commit(); // Commit the transaction
                    showAlert("Success", "Employee deleted successfully.");
                    reloadEmployeeList(); // Refresh the employee list
                } else {
                    conn.rollback(); // Rollback the transaction if no rows were deleted
                    showAlert("Error", "Failed to delete the employee.");
                }
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

    @FXML
    public void handleUpdateSalaries() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/update_salaries.fxml"));
            Parent root = loader.load();

            UpdateSalariesController updateSalariesController = loader.getController();
            updateSalariesController.setAdminDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Update Salaries");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditEmployee() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
    
        if (selectedEmployee == null) {
            showAlert("Error", "No employee selected. Please select an employee to edit.");
            return;
        }
    
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_employee.fxml"));
            Parent root = loader.load();
    
            EditEmployeeController editEmployeeController = loader.getController();
            editEmployeeController.setEmployeeId(selectedEmployee.getId());
            editEmployeeController.setAdminDashboardController(this);
    
            Stage stage = new Stage();
            stage.setTitle("Edit Employee");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    public void handleOpenSearchScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/search_employee.fxml"));
            Parent root = loader.load();

            SearchEmployeeController searchEmployeeController = loader.getController();
            searchEmployeeController.setAdminDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Search Employee");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateEmployeeTable(ObservableList<Employee> searchResults) {
        employeeTable.setItems(searchResults);
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