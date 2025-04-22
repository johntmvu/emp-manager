package com.example.model;

import java.sql.Date;

public class PayrollRecord {
    private Date payDate;
    private double earnings;
    private double fedTax;
    private double stateTax;
    private double retire401k;
    private double healthCare;

    public PayrollRecord(Date payDate, double earnings, double fedTax, double stateTax, double retire401k, double healthCare) {
        this.payDate = payDate;
        this.earnings = earnings;
        this.fedTax = fedTax;
        this.stateTax = stateTax;
        this.retire401k = retire401k;
        this.healthCare = healthCare;
    }

    public Date getPayDate() { return payDate; }
    public double getEarnings() { return earnings; }
    public double getFedTax() { return fedTax; }
    public double getStateTax() { return stateTax; }
    public double getRetire401k() { return retire401k; }
    public double getHealthCare() { return healthCare; }
}
