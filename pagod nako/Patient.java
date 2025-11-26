package com.emr.models;

import java.sql.Timestamp;

public class Patient {
    private int id;
    private Integer userId;
    private String name;
    private int age;
    private String DateOfBirth;
    private Gender gender;
    private String contactInfo;
    private String address;
    private String emergencyContact;
    private String insuranceInfo;
    private int priorityLevel;
    private boolean visited;
    private Timestamp lastVisitDate;
    private Timestamp createdAt;
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    // Constructors
    public Patient() {}
    
    public Patient(String name, int age, Gender gender, String contactInfo) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contactInfo = contactInfo;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getDateOfBirth(){
        return DateOfBirth;
    }
    public void setDateOfBirth(){
        this.DateOfBirth = DateOfBirth;
    }
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    
    public String getInsuranceInfo() { return insuranceInfo; }
    public void setInsuranceInfo(String insuranceInfo) { this.insuranceInfo = insuranceInfo; }
    
    public int getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }
    
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    
    public Timestamp getLastVisitDate() { return lastVisitDate; }
    public void setLastVisitDate(Timestamp lastVisitDate) { this.lastVisitDate = lastVisitDate; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}