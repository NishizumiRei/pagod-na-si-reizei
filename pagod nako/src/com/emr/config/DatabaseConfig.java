package com.emr.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                System.out.println("Unable to find database.properties, using defaults");
                setDefaultProperties();
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            System.out.println("Error loading database.properties, using defaults");
            setDefaultProperties();
        }
    }
    
    private static void setDefaultProperties() {
        String userHome = System.getProperty("user.home");
        String dbPath = userHome + File.separator + "emr_system" + File.separator + "emr.db";
        
        File dbDir = new File(userHome + File.separator + "emr_system");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        
        properties.setProperty("db.url", "jdbc:sqlite:" + dbPath);
        properties.setProperty("db.driver", "org.sqlite.JDBC");
    }
    
    public static String getUrl() {
        return properties.getProperty("db.url");
    }
    
    public static String getDriver() {
        return properties.getProperty("db.driver");
    }
    
    public static String getUsername() { 
        return ""; 
    }
    
    public static String getPassword() { 
        return ""; 
    }
}