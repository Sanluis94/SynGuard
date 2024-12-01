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

    // Construtor completo com todos os parâmetros
    public CrisisData(long timestamp, long duration, int crisisCount, long averageTime, long lastCrisisTime) {
        this.timestamp = timestamp;
        this.duration = duration;
        this.crisisCount = crisisCount;
        this.averageTime = averageTime;
        this.lastCrisisTime = lastCrisisTime;
    }

    // Novo construtor para aceitar três parâmetros (String, long, long)
    public CrisisData(String key, long duration, long averageTime) {
        this.timestamp = System.currentTimeMillis(); // Ou defina conforme necessário
        this.duration = duration;
        this.crisisCount = 1; // Atribuindo valor padrão, ou defina conforme a lógica
        this.averageTime = averageTime;
        this.lastCrisisTime = System.currentTimeMillis(); // Ou defina conforme necessário
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

    // Método para exibir a duração da crise em formato de horas, minutos e segundos
    public String getFormattedDuration() {
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
