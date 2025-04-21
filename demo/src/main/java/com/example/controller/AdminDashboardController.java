package com.example.controller;

import com.example.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    private String DB_URL = "jdbc:mysql://localhost:3306/employeedata";
    private String DB_USER = "root";
    private String DB_PASSWORD = "password";

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        jobTitleCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        divisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));

        loadEmployees();
    }

    private void loadEmployees() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();

        String query = """
            SELECT e.empid, CONCAT(e.Fname, ' ', e.Lname) AS name, e.email,
                   jt.job_title, d.Name AS division
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
                        rs.getString("division")
                ));
            }

            employeeTable.setItems(employees);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}