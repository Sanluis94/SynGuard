package com.example.myapplication.models;

import java.io.Serializable;

public class CrisisData implements Serializable {

    private long timestamp;
    private long duration;

    public CrisisData() {
        // Construtor padrão necessário para o Firebase
    }

    public CrisisData(long timestamp, long duration) {
        this.timestamp = timestamp;
        this.duration = duration;
    }

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
}
