package com.example.model;

public class DivisionTableRowData {
    private final String division;
    private final double totalPay;

    public DivisionTableRowData(String division, double totalPay) {
        this.division = division;
        this.totalPay = totalPay;
    }

    public String getDivision() { return division; }
    public double getTotalPay() { return totalPay; }
}