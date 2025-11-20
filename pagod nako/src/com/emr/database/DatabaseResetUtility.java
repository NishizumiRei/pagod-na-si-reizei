package com.emr.database;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseResetUtility {
    
    public static void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(null,
            "<html><b>⚠️ DATABASE RESET WARNING ⚠️</b><br><br>" +
            "This will <font color='red'><b>PERMANENTLY DELETE</b></font> all data including:<br>" +
            "• All patient records<br>" +
            "• All medical records<br>" +
            "• All staff schedules<br>" +
            "• All user accounts (except default admin)<br><br>" +
            "<b>This action cannot be undone!</b><br><br>" +
            "Are you absolutely sure you want to continue?</html>",
            "Confirm Database Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Second confirmation for safety
        int finalConfirm = JOptionPane.showConfirmDialog(null,
            "<html><b>FINAL WARNING</b><br><br>" +
            "You are about to <font color='red'><b>ERASE ALL DATA</b></font> from the system.<br>" +
            "This will restore the database to factory settings.<br><br>" +
            "Type <b>RESET</b> to confirm:</html>",
            "Final Confirmation",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);
        
        if (finalConfirm != JOptionPane.OK_OPTION) {
            return;
        }
        
        String userInput = JOptionPane.showInputDialog(null,
            "Please type RESET to confirm database reset:",
            "Confirm Reset",
            JOptionPane.WARNING_MESSAGE);
        
        if (userInput == null || !userInput.equalsIgnoreCase("RESET")) {
            JOptionPane.showMessageDialog(null,
                "Database reset cancelled.",
                "Reset Cancelled",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Perform the reset
        performDatabaseReset();
    }
    
    private static void performDatabaseReset() {
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Resetting database...");
        
        JDialog progressDialog = new JDialog();
        progressDialog.setTitle("Database Reset Progress");
        progressDialog.setModal(true);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(null);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.add(progressBar);
        
        // Run reset in a separate thread to keep UI responsive
        new Thread(() -> {
            try {
                progressDialog.setVisible(true);
            } catch (Exception e) {
                // Ignore UI thread issues
            }
        }).start();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            progressBar.setValue(10);
            progressBar.setString("Disabling foreign keys...");
            
            // Disable foreign key checks
            stmt.execute("PRAGMA foreign_keys = OFF");
            
            progressBar.setValue(20);
            progressBar.setString("Dropping tables...");
            
            // Drop all tables in correct order to avoid constraint violations
            String[] tables = {"staff_schedules", "medical_records", "medical_staff", "patients", "users"};
            
            for (String table : tables) {
                try {
                    stmt.execute("DROP TABLE IF EXISTS " + table);
                    Thread.sleep(100); // Small delay for UI update
                } catch (Exception e) {
                    System.err.println("Error dropping table " + table + ": " + e.getMessage());
                }
            }
            
            progressBar.setValue(60);
            progressBar.setString("Re-enabling foreign keys...");
            
            // Re-enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");
            
            progressBar.setValue(70);
            progressBar.setString("Recreating database schema...");
            
            // Recreate tables using DatabaseConnection's initialization
            recreateDatabaseSchema(stmt);
            
            progressBar.setValue(90);
            progressBar.setString("Inserting default data...");
            
            // Insert default data
            insertDefaultData(stmt);
            
            progressBar.setValue(100);
            progressBar.setString("Reset complete!");
            
            Thread.sleep(500); // Show completion for a moment
            
            SwingUtilities.invokeLater(() -> {
                progressDialog.dispose();
                JOptionPane.showMessageDialog(null,
                    "<html><b>✅ Database reset successful!</b><br><br>" +
                    "The database has been restored to factory settings.<br>" +
                    "Default accounts have been recreated:<br>" +
                    "• admin / admin123 (Administrator)<br>" +
                    "• doctor1 / doc123 (Doctor)<br>" +
                    "• nurse1 / nurse123 (Nurse)<br><br>" +
                    "Sample patient data has been added.</html>",
                    "Reset Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            });
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                progressDialog.dispose();
                JOptionPane.showMessageDialog(null,
                    "<html><b>❌ Database reset failed!</b><br><br>" +
                    "Error: " + e.getMessage() + "<br><br>" +
                    "Please check the console for details.</html>",
                    "Reset Failed",
                    JOptionPane.ERROR_MESSAGE);
            });
            e.printStackTrace();
        }
    }
    
    private static void recreateDatabaseSchema(Statement stmt) throws Exception {
        // Recreate all tables
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "role TEXT NOT NULL CHECK(role IN ('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT'))," +
                    "email TEXT," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "is_active BOOLEAN DEFAULT TRUE)");
        
        stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "name TEXT NOT NULL," +
                    "age INTEGER NOT NULL," +
                    "gender TEXT NOT NULL CHECK(gender IN ('MALE', 'FEMALE', 'OTHER'))," +
                    "contact_info TEXT," +
                    "address TEXT," +
                    "emergency_contact TEXT," +
                    "insurance_info TEXT," +
                    "priority_level INTEGER DEFAULT 1 CHECK(priority_level BETWEEN 1 AND 5)," +
                    "visited BOOLEAN DEFAULT FALSE," +
                    "last_visit_date DATETIME," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
        
        stmt.execute("CREATE TABLE IF NOT EXISTS medical_staff (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "specialization TEXT," +
                    "department TEXT," +
                    "license_number TEXT," +
                    "hire_date DATE," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
        
        stmt.execute("CREATE TABLE IF NOT EXISTS medical_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "patient_id INTEGER NOT NULL," +
                    "doctor_id INTEGER NOT NULL," +
                    "diagnosis TEXT," +
                    "treatment TEXT," +
                    "medications TEXT," +
                    "visit_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "notes TEXT," +
                    "blood_pressure TEXT," +
                    "heart_rate INTEGER," +
                    "temperature REAL," +
                    "height REAL," +
                    "weight REAL," +
                    "FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (doctor_id) REFERENCES users(id))");
        
        stmt.execute("CREATE TABLE IF NOT EXISTS staff_schedules (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "staff_id INTEGER NOT NULL," +
                    "schedule_date DATE NOT NULL," +
                    "start_time TIME NOT NULL," +
                    "end_time TIME NOT NULL," +
                    "activity_type TEXT NOT NULL CHECK(activity_type IN ('CONSULTATION', 'SURGERY', 'ROUNDS', 'BREAK', 'EMERGENCY'))," +
                    "patient_id INTEGER," +
                    "description TEXT," +
                    "status TEXT DEFAULT 'SCHEDULED' CHECK(status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))," +
                    "FOREIGN KEY (staff_id) REFERENCES medical_staff(id)," +
                    "FOREIGN KEY (patient_id) REFERENCES patients(id))");
    }
    
    private static void insertDefaultData(Statement stmt) throws Exception {
        // Insert default users
        stmt.execute("INSERT INTO users (username, password, role, email) VALUES " +
                    "('admin', 'admin123', 'ADMIN', 'admin@hospital.com')," +
                    "('doctor1', 'doc123', 'DOCTOR', 'doctor1@hospital.com')," +
                    "('nurse1', 'nurse123', 'NURSE', 'nurse1@hospital.com')");
        
        // Insert medical staff
        stmt.execute("INSERT INTO medical_staff (user_id, name, specialization, department, license_number, hire_date) VALUES " +
                    "(2, 'Dr. Sarah Wilson', 'Cardiology', 'Heart Center', 'MED123456', '2020-01-15')," +
                    "(3, 'Nurse Jane Smith', 'General', 'Emergency', 'NUR654321', '2021-03-20')");
        
        // Insert sample patients with more realistic data
        stmt.execute("INSERT INTO patients (name, age, gender, contact_info, address, emergency_contact, priority_level, visited) VALUES " +
                    "('John Smith', 45, 'MALE', '555-0101', '123 Main St, City', 'Mary Smith - 555-0102', 3, TRUE)," +
                    "('Maria Garcia', 32, 'FEMALE', '555-0202', '456 Oak Ave, Town', 'Carlos Garcia - 555-0203', 1, FALSE)," +
                    "('Robert Johnson', 68, 'MALE', '555-0303', '789 Pine Rd, Village', 'Lisa Johnson - 555-0304', 5, TRUE)," +
                    "('Sarah Chen', 29, 'FEMALE', '555-0404', '321 Elm St, City', 'David Chen - 555-0405', 2, FALSE)," +
                    "('James Wilson', 55, 'MALE', '555-0505', '654 Maple Dr, Town', 'Susan Wilson - 555-0506', 4, TRUE)");
        
        // Insert sample medical records
        stmt.execute("INSERT INTO medical_records (patient_id, doctor_id, diagnosis, treatment, medications, notes, blood_pressure, heart_rate, temperature) VALUES " +
                    "(1, 2, 'Hypertension', 'Lifestyle modification and medication', 'Lisinopril 10mg daily', 'Patient responding well to treatment', '120/80', 72, 36.8)," +
                    "(3, 2, 'Diabetes Type 2', 'Diet control and medication', 'Metformin 500mg twice daily', 'Blood sugar levels improving', '130/85', 68, 36.9)");
        
        // Insert sample schedules
        stmt.execute("INSERT INTO staff_schedules (staff_id, schedule_date, start_time, end_time, activity_type, description) VALUES " +
                    "(1, date('now'), '09:00', '10:00', 'CONSULTATION', 'Regular patient consultations')," +
                    "(1, date('now'), '11:00', '12:00', 'SURGERY', 'Heart surgery - Operating Room 2')," +
                    "(2, date('now'), '08:00', '16:00', 'ROUNDS', 'Daily patient rounds')," +
                    "(1, date('now', '+1 day'), '10:00', '11:00', 'CONSULTATION', 'Follow-up appointments')");
    }
    
    // Method to reset only specific tables (for advanced use)
    public static void resetSpecificTables(String[] tablesToReset) {
        int confirm = JOptionPane.showConfirmDialog(null,
            "Reset only selected tables? This may cause data inconsistency.",
            "Partial Reset",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = OFF");
            
            for (String table : tablesToReset) {
                try {
                    stmt.execute("DELETE FROM " + table);
                    System.out.println("Cleared table: " + table);
                } catch (Exception e) {
                    System.err.println("Error clearing table " + table + ": " + e.getMessage());
                }
            }
            
            stmt.execute("PRAGMA foreign_keys = ON");
            
            JOptionPane.showMessageDialog(null,
                "Selected tables have been reset.",
                "Partial Reset Complete",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error during partial reset: " + e.getMessage(),
                "Reset Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Method to backup database before reset (safety feature)
    public static boolean backupDatabase() {
        try {
            String dbPath = DatabaseConnection.getConnection().getMetaData().getURL().replace("jdbc:sqlite:", "");
            String backupPath = dbPath + ".backup_" + System.currentTimeMillis();
            
            // In a real implementation, you would copy the database file
            // For SQLite, this can be done with file operations
            
            JOptionPane.showMessageDialog(null,
                "Backup would be created at: " + backupPath + "\n" +
                "(File copy implementation required)",
                "Backup Info",
                JOptionPane.INFORMATION_MESSAGE);
            
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Backup failed: " + e.getMessage(),
                "Backup Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}