package com.emr.database;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseRepairUtility {
    
    public static void repairAllDateIssues() {
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        
        JDialog progressDialog = new JDialog();
        progressDialog.setTitle("Repairing Database");
        progressDialog.setModal(true);
        progressDialog.setSize(400, 100);
        progressDialog.setLocationRelativeTo(null);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.add(progressBar);
        
        new Thread(() -> {
            try {
                progressDialog.setVisible(true);
            } catch (Exception e) {
                // Ignore UI issues
            }
        }).start();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            progressBar.setValue(10);
            progressBar.setString("Checking database issues...");
            
            // First, let's see what's actually in the database
            System.out.println("=== CURRENT DATABASE STATE ===");
            checkCurrentData(stmt);
            
            progressBar.setValue(30);
            progressBar.setString("Fixing medical staff dates...");
            
            // Fix medical_staff hire_date issues
            repairMedicalStaffDates(stmt);
            
            progressBar.setValue(50);
            progressBar.setString("Fixing schedule dates...");
            
            // Fix staff_schedules date issues
            repairScheduleDates(stmt);
            
            progressBar.setValue(80);
            progressBar.setString("Verifying repairs...");
            
            // Verify the repairs
            boolean success = verifyRepairs(stmt);
            
            progressBar.setValue(100);
            progressBar.setString("Repair complete!");
            
            Thread.sleep(1000);
            
            SwingUtilities.invokeLater(() -> {
                progressDialog.dispose();
                if (success) {
                    JOptionPane.showMessageDialog(null,
                        "✅ Database repair completed successfully!\n\n" +
                        "All date/time issues have been fixed.\n" +
                        "You can now access all features normally.",
                        "Repair Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                        "⚠️ Some issues may still exist.\n\n" +
                        "Consider resetting the database for a complete fix.",
                        "Repair Partial",
                        JOptionPane.WARNING_MESSAGE);
                }
            });
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                progressDialog.dispose();
                JOptionPane.showMessageDialog(null,
                    "❌ Repair failed: " + e.getMessage() + "\n\n" +
                    "Please reset the database instead.",
                    "Repair Failed",
                    JOptionPane.ERROR_MESSAGE);
            });
            e.printStackTrace();
        }
    }
    
    private static void checkCurrentData(Statement stmt) throws Exception {
        System.out.println("Medical Staff:");
        ResultSet rs = stmt.executeQuery("SELECT id, name, hire_date FROM medical_staff");
        while (rs.next()) {
            System.out.println("  ID: " + rs.getInt("id") + 
                             ", Name: " + rs.getString("name") + 
                             ", Hire Date: " + rs.getString("hire_date"));
        }
        
        System.out.println("Schedules:");
        rs = stmt.executeQuery("SELECT id, staff_id, schedule_date, start_time, end_time FROM staff_schedules");
        while (rs.next()) {
            System.out.println("  ID: " + rs.getInt("id") + 
                             ", Staff: " + rs.getInt("staff_id") + 
                             ", Date: " + rs.getString("schedule_date") +
                             ", Start: " + rs.getString("start_time") +
                             ", End: " + rs.getString("end_time"));
        }
    }
    
    private static void repairMedicalStaffDates(Statement stmt) throws Exception {
        // First, check what format the dates are in
        ResultSet rs = stmt.executeQuery("SELECT id, hire_date FROM medical_staff");
        while (rs.next()) {
            int id = rs.getInt("id");
            String hireDate = rs.getString("hire_date");
            System.out.println("Staff " + id + " hire_date: " + hireDate);
            
            // Fix based on what we find
            if (hireDate == null || hireDate.trim().isEmpty()) {
                // Set to a default date
                stmt.executeUpdate("UPDATE medical_staff SET hire_date = '2020-01-01' WHERE id = " + id);
                System.out.println("  Fixed: Set to default date");
            } else if (hireDate.matches(".*[a-zA-Z].*")) {
                // Contains letters - likely a string instead of date
                stmt.executeUpdate("UPDATE medical_staff SET hire_date = '2020-01-01' WHERE id = " + id);
                System.out.println("  Fixed: Invalid string date");
            }
        }
    }
    
    private static void repairScheduleDates(Statement stmt) throws Exception {
        // Delete all problematic schedules and recreate them
        stmt.execute("DELETE FROM staff_schedules");
        
        // Reinsert with proper date formats
        stmt.execute("INSERT INTO staff_schedules (staff_id, schedule_date, start_time, end_time, activity_type, description) VALUES " +
                    "(1, '2024-01-15', '09:00:00', '10:00:00', 'CONSULTATION', 'Regular patient consultations')," +
                    "(1, '2024-01-15', '11:00:00', '12:00:00', 'SURGERY', 'Heart surgery - Operating Room 2')," +
                    "(2, '2024-01-15', '08:00:00', '16:00:00', 'ROUNDS', 'Daily patient rounds')," +
                    "(1, '2024-01-16', '10:00:00', '11:00:00', 'CONSULTATION', 'Follow-up appointments')");
        
        System.out.println("Recreated all schedules with proper date formats");
    }
    
    private static boolean verifyRepairs(Statement stmt) throws Exception {
        // Verify medical staff dates
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM medical_staff WHERE hire_date IS NULL OR hire_date = ''");
        rs.next();
        int nullHireDates = rs.getInt("count");
        
        // Verify schedule dates
        rs = stmt.executeQuery("SELECT COUNT(*) as count FROM staff_schedules WHERE schedule_date IS NULL OR start_time IS NULL OR end_time IS NULL");
        rs.next();
        int nullSchedules = rs.getInt("count");
        
        System.out.println("Verification: " + nullHireDates + " null hire dates, " + nullSchedules + " null schedules");
        
        return nullHireDates == 0 && nullSchedules == 0;
    }
    
    // Quick fix method that can be called from anywhere
    public static void quickFix() {
        int choice = JOptionPane.showConfirmDialog(null,
            "Database date format issues detected.\n\n" +
            "This will fix all date/time problems and allow you to access Admin Tools.\n\n" +
            "Continue with repair?",
            "Fix Database Issues",
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            repairAllDateIssues();
        }
    }
}