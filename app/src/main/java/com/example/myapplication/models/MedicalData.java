package com.example.myapplication.models;

public class MedicalData {
    private String monthYear;
    private int crisisCount;
    private long averageTime;

    // Construtor
    public MedicalData(String monthYear, int crisisCount, long averageTime) {
        this.monthYear = monthYear;
        this.crisisCount = crisisCount;
        this.averageTime = averageTime;
    }

    // Getters
    public String getMonthYear() {
        return monthYear;
    }

    public int getCrisisCount() {
        return crisisCount;
    }

    public long getAverageTime() {
        return averageTime;
    }
}
