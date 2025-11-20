package com.emr.database;

import com.emr.dao.*;
import com.emr.models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class CheckDatabase {
    
    public static void displayDatabaseContents() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Database Contents");
        dialog.setModal(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Users tab
        tabbedPane.addTab("Users", createUsersTable());
        
        // Patients tab
        tabbedPane.addTab("Patients", createPatientsTable());
        
        // Medical Staff tab
        tabbedPane.addTab("Medical Staff", createMedicalStaffTable());
        
        // Schedules tab
        tabbedPane.addTab("Schedules", createSchedulesTable());
        
        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }
    
    private static JScrollPane createUsersTable() {
        String[] columns = {"ID", "Username", "Role", "Email", "Active", "Created"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        try {
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.getAllUsers();
            
            for (User user : users) {
                model.addRow(new Object[]{
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getEmail(),
                    user.isActive() ? "Yes" : "No",
                    user.getCreatedAt()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTable table = new JTable(model);
        return new JScrollPane(table);
    }
    
    private static JScrollPane createPatientsTable() {
        String[] columns = {"ID", "Name", "Age", "Gender", "Priority", "Visited", "Last Visit"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        try {
            PatientDAO patientDAO = new PatientDAO();
            List<Patient> patients = patientDAO.getAllPatients();
            
            for (Patient patient : patients) {
                model.addRow(new Object[]{
                    patient.getId(),
                    patient.getName(),
                    patient.getAge(),
                    patient.getGender(),
                    patient.getPriorityLevel(),
                    patient.isVisited() ? "Yes" : "No",
                    patient.getLastVisitDate()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTable table = new JTable(model);
        return new JScrollPane(table);
    }
    
    private static JScrollPane createMedicalStaffTable() {
        String[] columns = {"ID", "Name", "Specialization", "Department", "License"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        try {
            MedicalStaffDAO staffDAO = new MedicalStaffDAO();
            List<MedicalStaff> staffList = staffDAO.getAllMedicalStaff();
            
            for (MedicalStaff staff : staffList) {
                model.addRow(new Object[]{
                    staff.getId(),
                    staff.getName(),
                    staff.getSpecialization(),
                    staff.getDepartment(),
                    staff.getLicenseNumber()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTable table = new JTable(model);
        return new JScrollPane(table);
    }
    
    private static JScrollPane createSchedulesTable() {
        String[] columns = {"ID", "Staff ID", "Date", "Activity", "Patient ID", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        try {
            StaffScheduleDAO scheduleDAO = new StaffScheduleDAO();
            List<StaffSchedule> schedules = scheduleDAO.getAllSchedules();
            
            for (StaffSchedule schedule : schedules) {
                model.addRow(new Object[]{
                    schedule.getId(),
                    schedule.getStaffId(),
                    schedule.getScheduleDate(),
                    schedule.getActivityType(),
                    schedule.getPatientId(),
                    schedule.getStatus()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTable table = new JTable(model);
        return new JScrollPane(table);
        
    }
}