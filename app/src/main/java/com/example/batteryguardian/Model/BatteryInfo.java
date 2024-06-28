package com.example.batteryguardian.Model;

public class BatteryInfo {
    private float batteryPct;
    private String status;
    private int capacity;
    private float tempCelsius;
    String technology;
    String voltage;
    String health;

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public BatteryInfo(float batteryPct, String status, int capacity, float tempCelsius) {
        this.batteryPct = batteryPct;
        this.status = status;
        this.capacity = capacity;
        this.tempCelsius = tempCelsius;
    }

    public float getBatteryPct() {
        return batteryPct;
    }

    public String getStatus() {
        return status;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setBatteryPct(float batteryPct) {
        this.batteryPct = batteryPct;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public float getTempCelsius() {
        return tempCelsius;
    }

    public void setTempCelsius(float tempCelsius) {
        this.tempCelsius = tempCelsius;
    }
}
