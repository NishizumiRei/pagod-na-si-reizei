package com.emr.models;

import java.sql.Date;

public class MedicalStaff {
    private int id;
    private int userId;
    private String name;
    private String specialization;
    private String department;
    private String licenseNumber;
    private Date hireDate;
    
    // Constructors, Getters and Setters
    public MedicalStaff() {}
    
    public MedicalStaff(int userId, String name, String specialization, String department) {
        this.userId = userId;
        this.name = name;
        this.specialization = specialization;
        this.department = department;
    }
    
    // ... (Getters and Setters for all fields)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    
    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }
}

