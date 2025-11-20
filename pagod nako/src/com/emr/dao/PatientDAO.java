package com.emr.dao;

import com.emr.database.DatabaseConnection;
import com.emr.models.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    
    public boolean createPatient(Patient patient) {
        String sql = "INSERT INTO patients (user_id, name, age, gender, contact_info, address, " +
                    "emergency_contact, insurance_info, priority_level, visited) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setPatientParameters(stmt, patient);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    patient.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating patient: " + e.getMessage());
        }
        return false;
    }
    
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY priority_level DESC, name ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patients: " + e.getMessage());
        }
        return patients;
    }
    
    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, contact_info = ?, " +
                    "address = ?, emergency_contact = ?, insurance_info = ?, priority_level = ?, " +
                    "visited = ?, last_visit_date = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setPatientParameters(stmt, patient);
            stmt.setTimestamp(10, patient.getLastVisitDate());
            stmt.setInt(11, patient.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deletePatient(int patientId) {
    String sql = "DELETE FROM patients WHERE id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, patientId);
        int affectedRows = stmt.executeUpdate();
        return affectedRows > 0;
        
    } catch (SQLException e) {
        System.err.println("Error deleting patient: " + e.getMessage());
        return false;
    }
}
    public Patient getPatientById(int id) {
        String sql = "SELECT * FROM patients WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPatient(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient: " + e.getMessage());
        }
        return null;
    }
    
    private void setPatientParameters(PreparedStatement stmt, Patient patient) throws SQLException {
        if (patient.getUserId() != null) {
            stmt.setInt(1, patient.getUserId());
        } else {
            stmt.setNull(1, Types.INTEGER);
        }
        stmt.setString(2, patient.getName());
        stmt.setInt(3, patient.getAge());
        stmt.setString(4, patient.getGender().name());
        stmt.setString(5, patient.getContactInfo());
        stmt.setString(6, patient.getAddress());
        stmt.setString(7, patient.getEmergencyContact());
        stmt.setString(8, patient.getInsuranceInfo());
        stmt.setInt(9, patient.getPriorityLevel());
        stmt.setBoolean(10, patient.isVisited());
    }
    
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getInt("id"));
        patient.setUserId(rs.getInt("user_id"));
        patient.setName(rs.getString("name"));
        patient.setAge(rs.getInt("age"));
        patient.setGender(Patient.Gender.valueOf(rs.getString("gender")));
        patient.setContactInfo(rs.getString("contact_info"));
        patient.setAddress(rs.getString("address"));
        patient.setEmergencyContact(rs.getString("emergency_contact"));
        patient.setInsuranceInfo(rs.getString("insurance_info"));
        patient.setPriorityLevel(rs.getInt("priority_level"));
        patient.setVisited(rs.getBoolean("visited"));
        patient.setLastVisitDate(rs.getTimestamp("last_visit_date"));
        patient.setCreatedAt(rs.getTimestamp("created_at"));
        return patient;
    }
}