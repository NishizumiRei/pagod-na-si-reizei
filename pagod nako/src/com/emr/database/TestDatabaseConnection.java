package com.emr.database;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.io.File;

public class TestDatabaseConnection {
    
    public static void testConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Get database file info
            String dbUrl = conn.getMetaData().getURL();
            File dbFile = new File(dbUrl.replace("jdbc:sqlite:", ""));
            
            String message = String.format(
                "SQLite Database Connection Successful!\n\n" +
                "Database File: %s\n" +
                "File Size: %s\n" +
                "Readable: %s\n" +
                "Writable: %s\n" +
                "SQLite Version: %s",
                dbFile.getAbsolutePath(),
                formatFileSize(dbFile.length()),
                dbFile.canRead() ? "Yes" : "No",
                dbFile.canWrite() ? "Yes" : "No",
                metaData.getDatabaseProductVersion()
            );
            
            JOptionPane.showMessageDialog(null, message, "SQLite Connection Test", 
                                        JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "SQLite Database Connection Failed!\n\nError: " + e.getMessage(), 
                "Connection Test Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        else return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
}