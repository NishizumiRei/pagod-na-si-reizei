package com.emr.dao;

import com.emr.database.DatabaseConnection;
import com.emr.models.MedicalRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAO {
    
    public boolean createMedicalRecord(MedicalRecord record) throws SQLException {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, diagnosis, treatment, " +
                    "medications, notes, blood_pressure, heart_rate, temperature, height, weight) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setMedicalRecordParameters(stmt, record);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    record.setId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }
    
    public List<MedicalRecord> getRecordsByPatientId(int patientId) throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = "SELECT mr.*, u.username as doctor_name FROM medical_records mr " +
                    "JOIN users u ON mr.doctor_id = u.id WHERE patient_id = ? ORDER BY visit_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                records.add(mapResultSetToMedicalRecord(rs));
            }
        }
        return records;
    }
    
    private void setMedicalRecordParameters(PreparedStatement stmt, MedicalRecord record) throws SQLException {
        stmt.setInt(1, record.getPatientId());
        stmt.setInt(2, record.getDoctorId());
        stmt.setString(3, record.getDiagnosis());
        stmt.setString(4, record.getTreatment());
        stmt.setString(5, record.getMedications());
        stmt.setString(6, record.getNotes());
        stmt.setString(7, record.getBloodPressure());
        stmt.setObject(8, record.getHeartRate(), Types.INTEGER);
        stmt.setObject(9, record.getTemperature(), Types.DECIMAL);
        stmt.setObject(10, record.getHeight(), Types.DECIMAL);
        stmt.setObject(11, record.getWeight(), Types.DECIMAL);
    }
    
    private MedicalRecord mapResultSetToMedicalRecord(ResultSet rs) throws SQLException {
        MedicalRecord record = new MedicalRecord();
        record.setId(rs.getInt("id"));
        record.setPatientId(rs.getInt("patient_id"));
        record.setDoctorId(rs.getInt("doctor_id"));
        record.setDiagnosis(rs.getString("diagnosis"));
        record.setTreatment(rs.getString("treatment"));
        record.setMedications(rs.getString("medications"));
        record.setVisitDate(rs.getTimestamp("visit_date"));
        record.setNotes(rs.getString("notes"));
        record.setBloodPressure(rs.getString("blood_pressure"));
        record.setHeartRate(rs.getInt("heart_rate"));
        if (rs.wasNull()) record.setHeartRate(null);
        record.setTemperature(rs.getDouble("temperature"));
        if (rs.wasNull()) record.setTemperature(null);
        record.setHeight(rs.getDouble("height"));
        if (rs.wasNull()) record.setHeight(null);
        record.setWeight(rs.getDouble("weight"));
        if (rs.wasNull()) record.setWeight(null);
        return record;
    }
        
    public boolean deleteMedicalRecord(int recordId) {
    String sql = "DELETE FROM medical_records WHERE id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, recordId);
        int affectedRows = stmt.executeUpdate();
        return affectedRows > 0;
        
    } catch (SQLException e) {
        System.err.println("Error deleting medical record: " + e.getMessage());
        return false;
    }
}

    public boolean deleteAllPatientRecords(int patientId) {
        String sql = "DELETE FROM medical_records WHERE patient_id = ?";
    
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
        
            stmt.setInt(1, patientId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        
        } catch (SQLException e) {
        System.err.println("Error deleting patient records: " + e.getMessage());
        return false;
        }
    }
    
}