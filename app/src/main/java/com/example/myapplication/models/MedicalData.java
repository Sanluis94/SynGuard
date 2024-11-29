package com.example.myapplication.models;

public class MedicalData {

    private String monthYear;
    private int totalCrises;
    private long averageDuration;

    public MedicalData() {
    }

    public MedicalData(String monthYear, int totalCrises, long averageDuration) {
        this.monthYear = monthYear;
        this.totalCrises = totalCrises;
        this.averageDuration = averageDuration;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public int getTotalCrises() {
        return totalCrises;
    }

    public void setTotalCrises(int totalCrises) {
        this.totalCrises = totalCrises;
    }

    public long getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(long averageDuration) {
        this.averageDuration = averageDuration;
    }
}
