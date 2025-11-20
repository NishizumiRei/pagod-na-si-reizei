package com.emr.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SystemSettingsDialog extends JDialog {
    
    public SystemSettingsDialog(JFrame parent) {
        super(parent, "System Settings", true);
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel headerLabel = new JLabel("System Settings", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Settings panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // General Settings Tab
        tabbedPane.addTab("General", createGeneralSettingsPanel());
        
        // Security Settings Tab
        tabbedPane.addTab("Security", createSecuritySettingsPanel());
        
        // Database Settings Tab
        tabbedPane.addTab("Database", createDatabaseSettingsPanel());
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Settings");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Event listeners
        saveButton.addActionListener(e -> saveSettings());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private JPanel createGeneralSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Hospital Name
        panel.add(new JLabel("Hospital Name:"));
        JTextField hospitalNameField = new JTextField("General Hospital");
        panel.add(hospitalNameField);
        
        // System Language
        panel.add(new JLabel("Language:"));
        JComboBox<String> languageCombo = new JComboBox<>(new String[]{"English", "Spanish", "French", "German"});
        panel.add(languageCombo);
        
        // Date Format
        panel.add(new JLabel("Date Format:"));
        JComboBox<String> dateFormatCombo = new JComboBox<>(new String[]{"YYYY-MM-DD", "MM/DD/YYYY", "DD/MM/YYYY"});
        panel.add(dateFormatCombo);
        
        // Auto Logout
        panel.add(new JLabel("Auto Logout (minutes):"));
        JSpinner logoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 240, 5));
        panel.add(logoutSpinner);
        
        // Backup Frequency
        panel.add(new JLabel("Backup Frequency:"));
        JComboBox<String> backupCombo = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Never"});
        panel.add(backupCombo);
        
        return panel;
    }
    
    private JPanel createSecuritySettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Password Policy
        JPanel passwordPanel = new JPanel(new GridLayout(2, 1));
        passwordPanel.setBorder(new TitledBorder("Password Policy"));
        
        JCheckBox requireComplexPasswords = new JCheckBox("Require complex passwords", true);
        JCheckBox forcePasswordChange = new JCheckBox("Force password change every 90 days");
        
        passwordPanel.add(requireComplexPasswords);
        passwordPanel.add(forcePasswordChange);
        
        // Session Settings
        JPanel sessionPanel = new JPanel(new GridLayout(2, 1));
        sessionPanel.setBorder(new TitledBorder("Session Settings"));
        
        JCheckBox auditLogging = new JCheckBox("Enable audit logging", true);
        JCheckBox loginNotifications = new JCheckBox("Email login notifications");
        
        sessionPanel.add(auditLogging);
        sessionPanel.add(loginNotifications);
        
        panel.add(passwordPanel);
        panel.add(sessionPanel);
        
        return panel;
    }
    
    private JPanel createDatabaseSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Database Info
        JPanel infoPanel = new JPanel(new GridLayout(2, 2));
        infoPanel.setBorder(new TitledBorder("Database Information"));
        
        infoPanel.add(new JLabel("Database Type:"));
        infoPanel.add(new JLabel("SQLite"));
        infoPanel.add(new JLabel("Database Path:"));
        infoPanel.add(new JLabel(System.getProperty("user.home") + "/emr_system/emr.db"));
        
        // Maintenance
        JPanel maintenancePanel = new JPanel(new GridLayout(2, 1));
        maintenancePanel.setBorder(new TitledBorder("Maintenance"));
        
        JButton optimizeButton = new JButton("Optimize Database");
        JButton cleanupButton = new JButton("Cleanup Old Records");
        
        maintenancePanel.add(optimizeButton);
        maintenancePanel.add(cleanupButton);
        
        panel.add(infoPanel);
        panel.add(maintenancePanel);
        
        return panel;
    }
    
    private void saveSettings() {
        JOptionPane.showMessageDialog(this,
            "Settings saved successfully!\n\n" +
            "Note: In a real implementation, these settings would be\n" +
            "stored in a configuration file or database.",
            "Settings Saved",
            JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public void setVisible(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}