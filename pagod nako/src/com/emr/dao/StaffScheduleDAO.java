package com.emr.dao;

import com.emr.database.DatabaseConnection;
import com.emr.models.StaffSchedule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffScheduleDAO {
    
    public boolean createSchedule(StaffSchedule schedule) {
        String sql = "INSERT INTO staff_schedules (staff_id, schedule_date, start_time, end_time, " +
                    "activity_type, patient_id, description, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setScheduleParameters(stmt, schedule);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    schedule.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating schedule: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateSchedule(StaffSchedule schedule) {
        String sql = "UPDATE staff_schedules SET staff_id = ?, schedule_date = ?, start_time = ?, " +
                    "end_time = ?, activity_type = ?, patient_id = ?, description = ?, status = ? " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setScheduleParameters(stmt, schedule);
            stmt.setInt(9, schedule.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating schedule: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteSchedule(int scheduleId) {
        String sql = "DELETE FROM staff_schedules WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, scheduleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting schedule: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public StaffSchedule getScheduleById(int id) {
        String sql = "SELECT ss.*, ms.name as staff_name, p.name as patient_name " +
                    "FROM staff_schedules ss " +
                    "JOIN medical_staff ms ON ss.staff_id = ms.id " +
                    "LEFT JOIN patients p ON ss.patient_id = p.id " +
                    "WHERE ss.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSchedule(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching schedule: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<StaffSchedule> getSchedulesByStaffId(int staffId) {
        List<StaffSchedule> schedules = new ArrayList<>();
        String sql = "SELECT ss.*, ms.name as staff_name, p.name as patient_name " +
                    "FROM staff_schedules ss " +
                    "JOIN medical_staff ms ON ss.staff_id = ms.id " +
                    "LEFT JOIN patients p ON ss.patient_id = p.id " +
                    "WHERE ss.staff_id = ? ORDER BY ss.schedule_date, ss.start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, staffId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                schedules.add(mapResultSetToSchedule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching schedules: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }
    
    public List<StaffSchedule> getSchedulesByDate(Date date) {
        List<StaffSchedule> schedules = new ArrayList<>();
        String sql = "SELECT ss.*, ms.name as staff_name, p.name as patient_name " +
                    "FROM staff_schedules ss " +
                    "JOIN medical_staff ms ON ss.staff_id = ms.id " +
                    "LEFT JOIN patients p ON ss.patient_id = p.id " +
                    "WHERE ss.schedule_date = ? ORDER BY ss.start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, date);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                schedules.add(mapResultSetToSchedule(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching schedules by date: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }
    
    public List<StaffSchedule> getAllSchedules() {
        List<StaffSchedule> schedules = new ArrayList<>();
        String sql = "SELECT ss.*, ms.name as staff_name, p.name as patient_name " +
                    "FROM staff_schedules ss " +
                    "JOIN medical_staff ms ON ss.staff_id = ms.id " +
                    "LEFT JOIN patients p ON ss.patient_id = p.id " +
                    "ORDER BY ss.schedule_date, ss.start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                try {
                    schedules.add(mapResultSetToSchedule(rs));
                } catch (SQLException e) {
                    System.err.println("Warning: Skipping invalid schedule record: " + e.getMessage());
                    // Continue processing other records
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all schedules: " + e.getMessage());
            e.printStackTrace();
        }
        return schedules;
    }
    
    private void setScheduleParameters(PreparedStatement stmt, StaffSchedule schedule) throws SQLException {
        stmt.setInt(1, schedule.getStaffId());
        stmt.setDate(2, schedule.getScheduleDate());
        stmt.setTime(3, schedule.getStartTime());
        stmt.setTime(4, schedule.getEndTime());
        stmt.setString(5, schedule.getActivityType().name());
        if (schedule.getPatientId() != null) {
            stmt.setInt(6, schedule.getPatientId());
        } else {
            stmt.setNull(6, Types.INTEGER);
        }
        stmt.setString(7, schedule.getDescription());
        stmt.setString(8, schedule.getStatus().name());
    }
    
    private StaffSchedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
        StaffSchedule schedule = new StaffSchedule();
        schedule.setId(rs.getInt("id"));
        schedule.setStaffId(rs.getInt("staff_id"));
        
        // Handle date parsing safely
        try {
            Date scheduleDate = rs.getDate("schedule_date");
            if (scheduleDate != null) {
                schedule.setScheduleDate(scheduleDate);
            } else {
                throw new SQLException("schedule_date is null");
            }
        } catch (SQLException e) {
            System.err.println("Error parsing schedule_date for schedule ID " + rs.getInt("id"));
            throw e; // Re-throw to skip this record
        }
        
        // Handle time parsing safely
        try {
            Time startTime = rs.getTime("start_time");
            if (startTime != null) {
                schedule.setStartTime(startTime);
            } else {
                throw new SQLException("start_time is null");
            }
        } catch (SQLException e) {
            System.err.println("Error parsing start_time for schedule ID " + rs.getInt("id"));
            throw e;
        }
        
        try {
            Time endTime = rs.getTime("end_time");
            if (endTime != null) {
                schedule.setEndTime(endTime);
            } else {
                throw new SQLException("end_time is null");
            }
        } catch (SQLException e) {
            System.err.println("Error parsing end_time for schedule ID " + rs.getInt("id"));
            throw e;
        }
        
        schedule.setActivityType(StaffSchedule.ActivityType.valueOf(rs.getString("activity_type")));
        schedule.setPatientId(rs.getInt("patient_id"));
        if (rs.wasNull()) schedule.setPatientId(null);
        schedule.setDescription(rs.getString("description"));
        schedule.setStatus(StaffSchedule.ScheduleStatus.valueOf(rs.getString("status")));
        
        return schedule;
    }
}