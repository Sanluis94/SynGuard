package com.example.myapplication.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrisisData {
    private long timestamp;
    private long duration;
    private long crisisCount;
    private long averageTime;
    private long lastCrisisTime;
    private long totalTime;  // Novo campo para armazenar o tempo total das crises

    // Construtor vazio necessário para Firebase
    public CrisisData() {}

    // Construtor com parâmetros para dados necessários
    public CrisisData(long lastCrisisTime, long duration, long crisisCount, long totalTime) {
        this.lastCrisisTime = lastCrisisTime;
        this.duration = duration;
        this.crisisCount = crisisCount;
        this.totalTime = totalTime;  // Inicializando o tempo total
        this.averageTime = crisisCount > 0 ? totalTime / crisisCount : 0; // Calculando a média com o tempo total
        this.timestamp = System.currentTimeMillis();  // Usa o timestamp atual
    }

    // Getters e Setters
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

    public long getCrisisCount() {
        return crisisCount;
    }

    public void setCrisisCount(long crisisCount) {
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

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    // Métodos para formatar os valores como strings (opcionais)
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedAverageTime() {
        long averageTimeInMinutes = averageTime / 60;
        return String.format(Locale.getDefault(), "%d min", averageTimeInMinutes);
    }

    public String getFormattedLastCrisisTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(lastCrisisTime));
    }

    public String getFormattedDuration() {
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String getFormattedTotalTime() {
        long hours = totalTime / 3600;
        long minutes = (totalTime % 3600) / 60;
        long seconds = totalTime % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
