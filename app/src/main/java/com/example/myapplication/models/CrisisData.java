package com.example.myapplication.models;

public class CrisisData {
    private int crisisCount;       // Número de crises
    private long averageTime;      // Tempo médio das crises
    private long lastCrisisTime;   // Duração da última crise

    public CrisisData() {
        // Construtor vazio necessário para Firebase
    }

    public CrisisData(int crisisCount, long averageTime, long lastCrisisTime) {
        this.crisisCount = crisisCount;
        this.averageTime = averageTime;
        this.lastCrisisTime = lastCrisisTime;
    }

    // Getters e setters
    public int getCrisisCount() {
        return crisisCount;
    }

    public void setCrisisCount(int crisisCount) {
        this.crisisCount = crisisCount;
    }

    public long getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(long averageTime) {
        this.averageTime = averageTime;
    }

    public long getLastCrisisTime() {
        return lastCrisisTime;
    }

    public void setLastCrisisTime(long lastCrisisTime) {
        this.lastCrisisTime = lastCrisisTime;
    }
}
