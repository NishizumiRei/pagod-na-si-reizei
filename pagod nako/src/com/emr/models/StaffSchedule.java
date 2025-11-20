package com.emr.models;

import java.sql.Date;
import java.sql.Time;

public class StaffSchedule {
    private int id;
    private int staffId;
    private Date scheduleDate;
    private Time startTime;
    private Time endTime;
    private ActivityType activityType;
    private Integer patientId;
    private String description;
    private ScheduleStatus status;
    
    public enum ActivityType {
        CONSULTATION, SURGERY, ROUNDS, BREAK, EMERGENCY
    }
    
    public enum ScheduleStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
    
    // Constructors, Getters and Setters
    public StaffSchedule() {}
    
    // ... (Getters and Setters for all fields)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }
    
    public Date getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(Date scheduleDate) { this.scheduleDate = scheduleDate; }
    
    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }
    
    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }
    
    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    
    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }
}