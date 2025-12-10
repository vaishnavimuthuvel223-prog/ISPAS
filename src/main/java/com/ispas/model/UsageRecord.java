package com.ispas.model;

public class UsageRecord {
    private int id;
    private int customerId;
    private String deviceName;
    private String dateTime;
    private double mbUsed;

    public UsageRecord() {}

    public UsageRecord(int customerId, String deviceName, String dateTime, double mbUsed) {
        this.customerId = customerId;
        this.deviceName = deviceName;
        this.dateTime = dateTime;
        this.mbUsed = mbUsed;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public double getMbUsed() { return mbUsed; }
    public void setMbUsed(double mbUsed) { this.mbUsed = mbUsed; }
}
