// SettingsManager.java
package com.emr.config;

import java.io.*;
import java.util.Properties;

public class SettingsManager {
    private static SettingsManager instance;
    private Properties settings;
    private File settingsFile;
    
    private SettingsManager() {
        settings = new Properties();
        settingsFile = new File("emr_settings.properties");
        loadSettings();
    }
    
    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }
    
    public void loadSettings() {
        try {
            if (settingsFile.exists()) {
                FileInputStream fis = new FileInputStream(settingsFile);
                settings.load(fis);
                fis.close();
            } else {
                setDefaultSettings();
            }
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            setDefaultSettings();
        }
    }
    
    private void setDefaultSettings() {
        settings.setProperty("hospital.name", "General Hospital");
        settings.setProperty("language", "English");
        settings.setProperty("date.format", "YYYY-MM-DD");
        settings.setProperty("time.format", "24-hour");
        settings.setProperty("auto.logout", "30");
        settings.setProperty("backup.frequency", "Weekly");
        settings.setProperty("password.complexity", "true");
        settings.setProperty("audit.logging", "true");
        settings.setProperty("database.path", System.getProperty("user.home") + "/emr_system/emr.db");
    }
    
    public String getSetting(String key) {
        return settings.getProperty(key);
    }
    
    public int getIntSetting(String key) {
        try {
            return Integer.parseInt(settings.getProperty(key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public boolean getBooleanSetting(String key) {
        return Boolean.parseBoolean(settings.getProperty(key));
    }
    
    public void saveSettings() {
        try {
            FileOutputStream fos = new FileOutputStream(settingsFile);
            settings.store(fos, "EMR System Settings");
            fos.close();
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    // Convenience methods for common settings
    public String getHospitalName() {
        return getSetting("hospital.name");
    }
    
    public int getAutoLogoutMinutes() {
        return getIntSetting("auto.logout");
    }
    
    public boolean isAuditLoggingEnabled() {
        return getBooleanSetting("audit.logging");
    }
}