package com.emr.database;

import com.emr.config.DatabaseConfig;
import java.sql.*;

public class DatabaseConnection {
    private static Connection connection = null;
    
    static {
        try {
            Class.forName(DatabaseConfig.getDriver());
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC Driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DatabaseConfig.getUrl());
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    static void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl());
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON");
            createTables(stmt);
            insertDefaultData(stmt);
        }
    }
    
    private static void createTables(Statement stmt) throws SQLException {
        // Users table
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "role TEXT NOT NULL CHECK(role IN ('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT'))," +
                    "email TEXT," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "is_active BOOLEAN DEFAULT TRUE)");
        
        // Patients table
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
        
        // Medical staff table
        stmt.execute("CREATE TABLE IF NOT EXISTS medical_staff (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "specialization TEXT," +
                    "department TEXT," +
                    "license_number TEXT," +
                    "hire_date DATE," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");
        
        // Medical records table
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
                    "FOREIGN KEY (patient_id) REFERENCES patients(id)," +
                    "FOREIGN KEY (doctor_id) REFERENCES users(id))");
        
        // Staff schedules table
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
        
        // Delete functionality etc etc...
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
    }
    
    private static void insertDefaultData(Statement stmt) throws SQLException {
    // Check if admin user already exists
    var rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'");
    if (rs.getInt(1) == 0) {
        // Insert default users
        stmt.execute("INSERT INTO users (username, password, role, email) VALUES " +
                    "('admin', 'admin123', 'ADMIN', 'admin@hospital.com')," +
                    "('doctor1', 'doc123', 'DOCTOR', 'doctor1@hospital.com')," +
                    "('nurse1', 'nurse123', 'NURSE', 'nurse1@hospital.com')");
        
        // Insert medical staff with proper date format
        stmt.execute("INSERT INTO medical_staff (user_id, name, specialization, department, license_number, hire_date) VALUES " +
                    "(2, 'Dr. Sarah Wilson', 'Cardiology', 'Heart Center', 'MED123456', '2020-01-15')," +
                    "(3, 'Nurse Jane Smith', 'General', 'Emergency', 'NUR654321', '2021-03-20')");
        
        // Insert sample patients
        stmt.execute("INSERT INTO patients (name, age, gender, contact_info, address, priority_level, visited) VALUES " +
                    "('John Smith', 45, 'MALE', '555-0101', '123 Main St, City', 3, TRUE)," +
                    "('Maria Garcia', 32, 'FEMALE', '555-0102', '456 Oak Ave, Town', 1, FALSE)," +
                    "('Robert Johnson', 68, 'MALE', '555-0103', '789 Pine Rd, Village', 5, TRUE)," +
                    "('Sarah Chen', 29, 'FEMALE', '555-0104', '321 Elm St, City', 2, FALSE)");
        
        // Insert sample schedules with proper time format
        stmt.execute("INSERT INTO staff_schedules (staff_id, schedule_date, start_time, end_time, activity_type, description) VALUES " +
                    "(1, date('now'), '09:00:00', '10:00:00', 'CONSULTATION', 'Regular patient consultations')," +
                    "(1, date('now'), '11:00:00', '12:00:00', 'SURGERY', 'Heart surgery - Operating Room 2')," +
                    "(2, date('now'), '08:00:00', '16:00:00', 'ROUNDS', 'Daily patient rounds')," +
                    "(1, date('now', '+1 day'), '10:00:00', '11:00:00', 'CONSULTATION', 'Follow-up appointments')");
        }
    }
}