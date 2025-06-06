package com.example.model;

public class Employee {
    private int id;
    private String name;
    private String email;
    private String jobTitle;
    private String division;
    private double salary;
    
    public Employee(int id, String name, String email, String jobTitle, String division, double salary) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.jobTitle = jobTitle;
        this.division = division;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getDivision() {
        return division;
    }

    public double getSalary() {
        return salary;
    }
}