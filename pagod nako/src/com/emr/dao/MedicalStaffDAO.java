package com.emr.dao;

import com.emr.database.DatabaseConnection;
import com.emr.models.MedicalStaff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalStaffDAO {
    
    public List<MedicalStaff> getAllMedicalStaff() {
        List<MedicalStaff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM medical_staff ORDER BY name ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                staffList.add(mapResultSetToMedicalStaff(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medical staff: " + e.getMessage());
            e.printStackTrace(); // Add this for debugging
        }
        return staffList;
    }
    
    public MedicalStaff getStaffByUserId(int userId) {
        String sql = "SELECT * FROM medical_staff WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMedicalStaff(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching staff: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    private MedicalStaff mapResultSetToMedicalStaff(ResultSet rs) throws SQLException {
        MedicalStaff staff = new MedicalStaff();
        staff.setId(rs.getInt("id"));
        staff.setUserId(rs.getInt("user_id"));
        staff.setName(rs.getString("name"));
        staff.setSpecialization(rs.getString("specialization"));
        staff.setDepartment(rs.getString("department"));
        staff.setLicenseNumber(rs.getString("license_number"));
        
        // Handle date parsing safely
        try {
            Date hireDate = rs.getDate("hire_date");
            if (hireDate != null) {
                staff.setHireDate(hireDate);
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not parse hire_date for staff ID " + rs.getInt("id"));
            // Continue without the hire date
        }
        
        return staff;
    }
}