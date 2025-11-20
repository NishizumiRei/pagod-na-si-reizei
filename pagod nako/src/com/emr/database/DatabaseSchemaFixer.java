package com.emr.database;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseSchemaFixer {
    
    public static void fixDatabaseSchema() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Check if all required tables exist
            String[] requiredTables = {"users", "patients", "medical_records", "medical_staff", "staff_schedules"};
            StringBuilder missingTables = new StringBuilder();
            StringBuilder corruptedTables = new StringBuilder();
            
            for (String table : requiredTables) {
                ResultSet tables = metaData.getTables(null, null, table, null);
                if (!tables.next()) {
                    missingTables.append("- ").append(table).append("\n");
                } else {
                    // Check if table has basic structure
                    if (!isTableValid(conn, table)) {
                        corruptedTables.append("- ").append(table).append("\n");
                    }
                }
                tables.close();
            }
            
            if (missingTables.length() > 0 || corruptedTables.length() > 0) {
                StringBuilder message = new StringBuilder();
                
                if (missingTables.length() > 0) {
                    message.append("Missing Tables:\n").append(missingTables).append("\n");
                }
                
                if (corruptedTables.length() > 0) {
                    message.append("Corrupted Tables:\n").append(corruptedTables).append("\n");
                }
                
                message.append("\nWould you like to repair the database now?");
                
                int choice = JOptionPane.showConfirmDialog(null,
                    message.toString(),
                    "Database Schema Issues Detected",
                    JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    repairDatabase(conn, missingTables.toString(), corruptedTables.toString());
                }
            } else {
                JOptionPane.showMessageDialog(null,
                    "Database schema appears to be correct. All required tables are present and valid.",
                    "Schema Check Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error checking database schema: " + e.getMessage(),
                "Schema Check Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static boolean isTableValid(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return true; // If we can count rows, table is probably valid
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void repairDatabase(Connection conn, String missingTables, String corruptedTables) {
        try (Statement stmt = conn.createStatement()) {
            // For SQLite, the easiest repair is to recreate the database
            int choice = JOptionPane.showConfirmDialog(null,
                "The recommended repair is to reset the database to its default state.\n\n" +
                "This will preserve your database file but recreate all tables with default data.\n\n" +
                "Continue with database reset?",
                "Database Repair",
                JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                DatabaseResetUtility.resetDatabase();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error repairing database: " + e.getMessage(),
                "Repair Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}