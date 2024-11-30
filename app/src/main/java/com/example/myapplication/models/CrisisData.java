package com.example.myapplication.models;

public class CrisisData {
    private String caregiverId;
    private long duration;
    private int crisisCount;
    private long averageTime;
    private long lastCrisisTime;

    // Construtor completo
    public CrisisData(String caregiverId, long duration, int crisisCount, long averageTime, long lastCrisisTime) {
        this.caregiverId = caregiverId;
        this.duration = duration;
        this.crisisCount = crisisCount;
        this.averageTime = averageTime;
        this.lastCrisisTime = lastCrisisTime;
    }

    // Getters e setters
    public String getCaregiverId() {
        return caregiverId;
    }

    public long getDuration() {
        return duration;
    }

    public int getCrisisCount() {
        return crisisCount;
    }

    public long getAverageTime() {
        return averageTime;
    }

    public long getLastCrisisTime() {
        return lastCrisisTime;
    }
    public String getTimestamp() {
        return String.valueOf(System.currentTimeMillis()); // Substitua com lógica real
    }

}
