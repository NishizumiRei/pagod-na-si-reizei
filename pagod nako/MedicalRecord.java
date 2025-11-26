package com.emr.models;

import java.sql.Timestamp;
import java.sql.Date;

public class MedicalRecord {
    private int id;
    private int patientId;
    private int doctorId;
    private Date recordDate;
    private String diagnosis;
    private String treatment;
    private String medications;
    private String createdAt;
    private Timestamp visitDate;
    private String notes;
    private String bloodPressure;
    private Integer heartRate;
    private Double temperature;
    private Double height;
    private Double weight;
    
    
    // Constructors, Getters and Setters
    public MedicalRecord() {}
    
    public MedicalRecord(int patientId, int doctorId, String diagnosis, String treatment) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
    }
    
    // ... (Getters and Setters for all fields)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    
    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }
    
    public Timestamp getVisitDate() { return visitDate; }
    public void setVisitDate(Timestamp visitDate) { this.visitDate = visitDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Date getRecordDate() {
        return recordDate;
    }
    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}