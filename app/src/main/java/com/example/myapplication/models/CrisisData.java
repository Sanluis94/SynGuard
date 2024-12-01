package com.example.myapplication.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    // Método para obter o timestamp formatado (data e hora)
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Método para obter o tempo médio formatado em minutos
    public String getFormattedAverageTime() {
        long averageTimeInMinutes = averageTime / 60;
        return String.format(Locale.getDefault(), "%d min", averageTimeInMinutes);
    }

    // Método para obter o tempo da última crise formatado
    public String getFormattedLastCrisisTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(lastCrisisTime));
    }
}
