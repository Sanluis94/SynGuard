package com.example.myapplication.models;

public class CrisisData {
    private String caregiverId;
    private long duration;
    private int crisisCount;
    private long averageTime;
    private long lastCrisisTime;

    // Construtor sem argumentos (necessário para o Firebase)
    public CrisisData() {
    }

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

    public void setCaregiverId(String caregiverId) {
        this.caregiverId = caregiverId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

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

    public String getTimestamp() {
        return String.valueOf(System.currentTimeMillis()); // Substitua com lógica real
    }
}
