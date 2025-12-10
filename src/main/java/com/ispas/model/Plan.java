package com.ispas.model;

public class Plan {
    private int id;
    private String name;
    private double monthlyFee;
    private double ratePerMb;

    public Plan() {}

    public Plan(String name, double monthlyFee, double ratePerMb) {
        this.name = name;
        this.monthlyFee = monthlyFee;
        this.ratePerMb = ratePerMb;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(double monthlyFee) { this.monthlyFee = monthlyFee; }
    public double getRatePerMb() { return ratePerMb; }
    public void setRatePerMb(double ratePerMb) { this.ratePerMb = ratePerMb; }
}
