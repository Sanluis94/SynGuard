package com.example.myapplication.models;

public class CrisisData {
    private long timestamp;
    private long duration;
    private int crisisCount;
    private long averageTime;
    private long lastCrisisTime;

    // Construtor vazio necessário para Firebase
    public CrisisData() {}

    // Construtor completo
    public CrisisData(long timestamp, long duration, int crisisCount, long averageTime, long lastCrisisTime) {
        this.timestamp = timestamp;
        this.duration = duration;
        this.crisisCount = crisisCount;
        this.averageTime = averageTime;
        this.lastCrisisTime = lastCrisisTime;
    }

    // Getters e setters
    public long getTimestamp() {
        return timestamp;
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

    public String getFormattedTimestamp() {
        return String.valueOf(timestamp);
    }
}
