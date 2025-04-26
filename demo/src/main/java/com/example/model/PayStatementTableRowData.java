package com.example.model;

public class PayStatementTableRowData {
    private final int empId;
    private final String name;
    private final String payDate;
    private final double earnings;

    public PayStatementTableRowData(int empId, String name, String payDate, double earnings) {
        this.empId = empId;
        this.name = name;
        this.payDate = payDate;
        this.earnings = earnings;
    }

    public int getEmpId() { return empId; }
    public String getName() { return name; }
    public String getPayDate() { return payDate; }
    public double getEarnings() { return earnings; }
}