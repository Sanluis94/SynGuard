package com.example.myapplication;

import java.util.Date;

public class MedicalRecord {

    private String patientId;
    private String caregiverId;
    private Date timestamp;
    private long duration;

    public MedicalRecord() {
    }

    public MedicalRecord(String patientId, String caregiverId, Date timestamp, long duration) {
        this.patientId = patientId;
        this.caregiverId = caregiverId;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(String caregiverId) {
        this.caregiverId = caregiverId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
