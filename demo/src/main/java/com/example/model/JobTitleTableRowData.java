package com.example.model;

public class JobTitleTableRowData {
    private final String jobTitle;
    private final double totalPay;

    public JobTitleTableRowData(String jobTitle, double totalPay) {
        this.jobTitle = jobTitle;
        this.totalPay = totalPay;
    }

    public String getJobTitle() { return jobTitle; }
    public double getTotalPay() { return totalPay; }
}