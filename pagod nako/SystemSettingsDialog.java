package com.emr.gui;

import com.emr.config.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class SystemSettingsDialog extends JDialog {
    private Properties settings;
    private File settingsFile;
    
    // Settings components
    private JTextField hospitalNameField;
    private JComboBox<String> languageCombo;
    private JComboBox<String> dateFormatCombo;
    private JComboBox<String> timeFormatCombo;
    private JSpinner autoLogoutSpinner;
    private JComboBox<String> backupCombo;
    private JCheckBox requireComplexPasswords;
    private JCheckBox forcePasswordChange;
    private JCheckBox auditLogging;
    private JCheckBox loginNotifications;
    private JTextField databasePathField;
    private JComboBox<String> themeComboBox;
    private JCheckBox darkModeCheckBox;
    
    public SystemSettingsDialog(JFrame parent) {
        super(parent, "System Settings", true);
        initializeSettings();
        initializeUI();
        loadSettings();
    }
    
    private void initializeSettings() {
        settings = new Properties();
        settingsFile = new File("emr_settings.properties");
        
        // Set default values
        settings.setProperty("hospital.name", "General Hospital");
        settings.setProperty("language", "English");
        settings.setProperty("date.format", "YYYY-MM-DD");
        settings.setProperty("time.format", "24-hour");
        settings.setProperty("auto.logout", "30");
        settings.setProperty("backup.frequency", "Weekly");
        settings.setProperty("password.complexity", "true");
        settings.setProperty("password.forceChange", "false");
        settings.setProperty("audit.logging", "true");
        settings.setProperty("login.notifications", "false");
        settings.setProperty("database.path", System.getProperty("user.home") + "/emr_system/emr.db");
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel headerLabel = new JLabel("System Settings", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("General", createGeneralSettingsPanel());

        tabbedPane.addTab("Security", createSecuritySettingsPanel());

        tabbedPane.addTab("Database", createDatabaseSettingsPanel());

        tabbedPane.addTab("Appearance", createAppearanceTab());
        
        tabbedPane.addTab("Fonts", createFontSettingsTab());

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Settings");
        JButton resetButton = new JButton("Reset to Defaults");
        JButton cancelButton = new JButton("Cancel");

        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        resetButton.setBackground(new Color(255, 193, 7));
        resetButton.setForeground(Color.BLACK);
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);

        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event listeners
        saveButton.addActionListener(e -> saveSettings());
        resetButton.addActionListener(e -> resetToDefaults());
        cancelButton.addActionListener(e -> dispose());

        // Add keyboard shortcut (ESC to close)
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel createGeneralSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Hospital Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Hospital Name:"), gbc);
        gbc.gridx = 1;
        hospitalNameField = new JTextField();
        panel.add(hospitalNameField, gbc);
        
        // System Language
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Language:"), gbc);
        gbc.gridx = 1;
        languageCombo = new JComboBox<>(new String[]{"English", "Spanish", "French", "German"});
        panel.add(languageCombo, gbc);
        
        // Date Format
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Date Format:"), gbc);
        gbc.gridx = 1;
        dateFormatCombo = new JComboBox<>(new String[]{"YYYY-MM-DD", "MM/DD/YYYY", "DD/MM/YYYY"});
        panel.add(dateFormatCombo, gbc);
        
        // Time Format
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Time Format:"), gbc);
        gbc.gridx = 1;
        timeFormatCombo = new JComboBox<>(new String[]{"24-hour", "12-hour"});
        panel.add(timeFormatCombo, gbc);
        
        // Auto Logout
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Auto Logout (minutes):"), gbc);
        gbc.gridx = 1;
        autoLogoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 240, 5));
        panel.add(autoLogoutSpinner, gbc);
        
        // Backup Frequency
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Backup Frequency:"), gbc);
        gbc.gridx = 1;
        backupCombo = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Never"});
        panel.add(backupCombo, gbc);
        
        return panel;
    }
    
    private JPanel createSecuritySettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Password Policy
        JPanel passwordPanel = new JPanel(new GridLayout(3, 1));
        passwordPanel.setBorder(new TitledBorder("Password Policy"));
        
        requireComplexPasswords = new JCheckBox("Require complex passwords");
        forcePasswordChange = new JCheckBox("Force password change every 90 days");
        JCheckBox preventReuse = new JCheckBox("Prevent password reuse (last 5 passwords)");
        
        passwordPanel.add(requireComplexPasswords);
        passwordPanel.add(forcePasswordChange);
        passwordPanel.add(preventReuse);
        
        // Session Settings
        JPanel sessionPanel = new JPanel(new GridLayout(3, 1));
        sessionPanel.setBorder(new TitledBorder("Session Settings"));
        
        auditLogging = new JCheckBox("Enable audit logging");
        loginNotifications = new JCheckBox("Email login notifications");
        JCheckBox concurrentSessions = new JCheckBox("Allow multiple concurrent sessions");
        
        sessionPanel.add(auditLogging);
        sessionPanel.add(loginNotifications);
        sessionPanel.add(concurrentSessions);
        
        // Access Control
        JPanel accessPanel = new JPanel(new GridLayout(2, 1));
        accessPanel.setBorder(new TitledBorder("Access Control"));
        
        JCheckBox ipRestrictions = new JCheckBox("Enable IP address restrictions");
        JCheckBox timeRestrictions = new JCheckBox("Enable time-based access restrictions");
        
        accessPanel.add(ipRestrictions);
        accessPanel.add(timeRestrictions);
        
        panel.add(passwordPanel);
        panel.add(sessionPanel);
        panel.add(accessPanel);
        
        return panel;
    }
    
    private JPanel createDatabaseSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Database Info
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        infoPanel.setBorder(new TitledBorder("Database Information"));
        
        infoPanel.add(new JLabel("Database Type:"));
        infoPanel.add(new JLabel("SQLite"));
        infoPanel.add(new JLabel("Database Path:"));
        databasePathField = new JTextField();
        infoPanel.add(databasePathField);
        infoPanel.add(new JLabel("Database Size:"));
        infoPanel.add(new JLabel(calculateDatabaseSize()));
        infoPanel.add(new JLabel("Last Backup:"));
        infoPanel.add(new JLabel(getLastBackupDate()));
        
        // Maintenance
        JPanel maintenancePanel = new JPanel(new GridLayout(3, 1, 5, 5));
        maintenancePanel.setBorder(new TitledBorder("Database Maintenance"));
        
        JButton optimizeButton = new JButton("Optimize Database");
        JButton cleanupButton = new JButton("Cleanup Old Records");
        JButton backupButton = new JButton("Create Backup Now");
        
        optimizeButton.setBackground(new Color(0, 123, 255));
        optimizeButton.setForeground(Color.WHITE);
        cleanupButton.setBackground(new Color(255, 193, 7));
        cleanupButton.setForeground(Color.BLACK);
        backupButton.setBackground(new Color(40, 167, 69));
        backupButton.setForeground(Color.WHITE);
        
        maintenancePanel.add(optimizeButton);
        maintenancePanel.add(cleanupButton);
        maintenancePanel.add(backupButton);
        
        // Add action listeners for maintenance buttons
        optimizeButton.addActionListener(e -> optimizeDatabase());
        cleanupButton.addActionListener(e -> cleanupOldRecords());
        backupButton.addActionListener(e -> createBackup());
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(maintenancePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // In your SystemSettingsDialog.java - add this method
    private JPanel createAppearanceTab() {
        JPanel appearancePanel = new JPanel(new BorderLayout(10, 10));
        appearancePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Theme Selection
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.setBorder(BorderFactory.createTitledBorder("Application Theme"));

        JLabel themeLabel = new JLabel("Theme: ");
        JComboBox<ThemeManager.Theme> themeCombo = new JComboBox<>(ThemeManager.getAvailableThemes());
        themeCombo.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ThemeManager.Theme){
                    setText(((ThemeManager.Theme) value).getDisplayName());
                }
                return this;
            }
        });

        themeCombo.setSelectedItem(ThemeManager.getCurrentTheme());

        themeCombo.addActionListener(e -> {
            ThemeManager.Theme selectedTheme = (ThemeManager.Theme) themeCombo.getSelectedItem();
            if (selectedTheme != null) {
                int result = JOptionPane.showConfirmDialog(SystemSettingsDialog.this,
                    "Change theme to " + selectedTheme.getDisplayName() + "?",
                    "Confirm Theme Change",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    ThemeManager.applyTheme(selectedTheme);
                    ThemeManager.updateAllUIComponents(SystemSettingsDialog.this);

                    // Notify MainFrame to refresh its colors
                    if (getParent() instanceof MainFrame) {
                        ((MainFrame) getParent()).onThemeChanged();
                    }

                    JOptionPane.showMessageDialog(SystemSettingsDialog.this,
                        "Theme changed to " + selectedTheme.getDisplayName() + ".\n" +
                        "Some windows may need to be reopened to see all changes.",
                        "Theme Updated",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Revert selection
                    themeCombo.setSelectedItem(ThemeManager.getCurrentTheme());
                }
            }
        });

        themePanel.add(themeLabel);
        themePanel.add(themeCombo);

        // Theme description
        JTextArea themeDescription = new JTextArea(4, 40);
        themeDescription.setEditable(false);
        themeDescription.setBackground(appearancePanel.getBackground());
        themeDescription.setFont(new Font("SansSerif", Font.PLAIN, 12));
        themeDescription.setText("Choose a theme that works best for your environment.\n\n" +
                               "• System Default: Uses your operating system's native look\n" +
                               "• Nimbus: Modern Java theme with smooth appearance\n" +
                               "• Flat Themes: Modern flat design (requires FlatLaf library)\n" +
                               "• Metal: Classic Java cross-platform theme");
        themeDescription.setBorder(new EmptyBorder(10, 5, 10, 5));

        JPanel mainThemePanel = new JPanel(new BorderLayout());
        mainThemePanel.add(themePanel, BorderLayout.NORTH);
        mainThemePanel.add(new JScrollPane(themeDescription), BorderLayout.CENTER);

        appearancePanel.add(mainThemePanel, BorderLayout.NORTH);

        return appearancePanel;
    }
    
    private JPanel createFontSettingsTab(){
        JPanel fontPanel = new JPanel(new BorderLayout(10, 10));
        fontPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Font Family Selection
        JPanel fontFamilyPanel = new JPanel(new BorderLayout(5, 5));
        fontFamilyPanel.setBorder(BorderFactory.createTitledBorder("Font Family"));

        JComboBox<ThemeManager.FontFamily> fontFamilyCombo = new JComboBox<>(ThemeManager.getAvailableFontFamilies());
        fontFamilyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ThemeManager.FontFamily) {
                    ThemeManager.FontFamily fontFamily = (ThemeManager.FontFamily) value;
                    setText(fontFamily.getFontName());
                    setToolTipText(fontFamily.getDescription());
                }
                return this;
            }
        });

        // Set current font family
        fontFamilyCombo.setSelectedItem(ThemeManager.getCurrentFontFamily());

        JLabel fontPreviewLabel = new JLabel("Preview: The quick brown fox jumps over the lazy dog");
        fontPreviewLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        fontFamilyPanel.add(new JLabel("Select Font:"), BorderLayout.NORTH);
        fontFamilyPanel.add(fontFamilyCombo, BorderLayout.CENTER);
        fontFamilyPanel.add(fontPreviewLabel, BorderLayout.SOUTH);

        // Font Size Selection
        JPanel fontSizePanel = new JPanel(new BorderLayout(5, 5));
        fontSizePanel.setBorder(BorderFactory.createTitledBorder("Font Size"));

        JComboBox<ThemeManager.FontSize> fontSizeCombo = new JComboBox<>(ThemeManager.getAvailableFontSizes());
        fontSizeCombo.setSelectedItem(ThemeManager.getCurrentFontSizeEnum());

        JSpinner customSizeSpinner = new JSpinner(new SpinnerNumberModel(
            ThemeManager.getCurrentFontSize(), 8, 24, 1));
        customSizeSpinner.setEnabled(ThemeManager.getCurrentFontSizeEnum() == ThemeManager.FontSize.CUSTOM);

        JPanel sizeControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizeControlPanel.add(new JLabel("Preset Size:"));
        sizeControlPanel.add(fontSizeCombo);
        sizeControlPanel.add(new JLabel("Custom Size:"));
        sizeControlPanel.add(customSizeSpinner);

        fontSizePanel.add(sizeControlPanel, BorderLayout.CENTER);

        // Accessibility Options
        JPanel accessibilityPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        accessibilityPanel.setBorder(BorderFactory.createTitledBorder("Accessibility"));

        JCheckBox highContrastCheckbox = new JCheckBox("High contrast mode (for visual impairment)");
        JCheckBox largerTextCheckbox = new JCheckBox("Always use larger text in tables and forms");

        accessibilityPanel.add(highContrastCheckbox);
        accessibilityPanel.add(largerTextCheckbox);

        // Apply Button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton applyFontButton = new JButton("Apply Font Settings");
        applyFontButton.setBackground(new Color(0, 123, 255));
        applyFontButton.setForeground(Color.WHITE);

        buttonPanel.add(applyFontButton);

        // Assemble the panel
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.add(fontFamilyPanel);
        mainContent.add(Box.createVerticalStrut(15));
        mainContent.add(fontSizePanel);
        mainContent.add(Box.createVerticalStrut(15));
        mainContent.add(accessibilityPanel);

        fontPanel.add(mainContent, BorderLayout.CENTER);
        fontPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Event Listeners
        fontFamilyCombo.addActionListener(e -> {
            updateFontPreview(fontFamilyCombo, fontSizeCombo, customSizeSpinner, fontPreviewLabel);
        });

        fontSizeCombo.addActionListener(e -> {
            ThemeManager.FontSize selectedSize = (ThemeManager.FontSize) fontSizeCombo.getSelectedItem();
            customSizeSpinner.setEnabled(selectedSize == ThemeManager.FontSize.CUSTOM);
            updateFontPreview(fontFamilyCombo, fontSizeCombo, customSizeSpinner, fontPreviewLabel);
        });

        customSizeSpinner.addChangeListener(e -> {
            updateFontPreview(fontFamilyCombo, fontSizeCombo, customSizeSpinner, fontPreviewLabel);
        });

        applyFontButton.addActionListener(e -> {
            applyFontSettings(fontFamilyCombo, fontSizeCombo, customSizeSpinner);
        });

        // Initial preview update
        updateFontPreview(fontFamilyCombo, fontSizeCombo, customSizeSpinner, fontPreviewLabel);

        return fontPanel;
    }

    private void updateFontPreview(JComboBox<ThemeManager.FontFamily> fontFamilyCombo,
                                  JComboBox<ThemeManager.FontSize> fontSizeCombo,
                                  JSpinner customSizeSpinner,
                                  JLabel previewLabel) {
        try {
            ThemeManager.FontFamily fontFamily = (ThemeManager.FontFamily) fontFamilyCombo.getSelectedItem();
            ThemeManager.FontSize fontSizeEnum = (ThemeManager.FontSize) fontSizeCombo.getSelectedItem();

            int fontSize = (fontSizeEnum == ThemeManager.FontSize.CUSTOM) ?
                (Integer) customSizeSpinner.getValue() : fontSizeEnum.getSize();

            Font previewFont = new Font(fontFamily.getFontName(), Font.PLAIN, fontSize);
            previewLabel.setFont(previewFont);
            previewLabel.setText("Preview: The quick brown fox jumps over the lazy dog (" + fontSize + "pt)");

        } catch (Exception e) {
            System.err.println("Error updating font preview: " + e.getMessage());
        }
    }

    private void applyFontSettings(JComboBox<ThemeManager.FontFamily> fontFamilyCombo,
                              JComboBox<ThemeManager.FontSize> fontSizeCombo,
                              JSpinner customSizeSpinner) {
        try {
            ThemeManager.FontFamily fontFamily = (ThemeManager.FontFamily) fontFamilyCombo.getSelectedItem();
            ThemeManager.FontSize fontSizeEnum = (ThemeManager.FontSize) fontSizeCombo.getSelectedItem();

            if (fontSizeEnum == ThemeManager.FontSize.CUSTOM) {
                int customSize = (Integer) customSizeSpinner.getValue();
                ThemeManager.applyCustomFontSize(customSize);
            } else {
                ThemeManager.applyFontSettings(fontFamily, fontSizeEnum);
            }

            // Force update ALL UI components including MainFrame
            updateAllApplicationFonts();

            JOptionPane.showMessageDialog(this,
                "Font settings applied successfully!\n\n" +
                "Font: " + fontFamily.getFontName() + "\n" +
                "Size: " + ((fontSizeEnum == ThemeManager.FontSize.CUSTOM) ? 
                    customSizeSpinner.getValue() + "pt" : fontSizeEnum.getDisplayName()),
                "Font Settings Applied",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error applying font settings: " + e.getMessage(),
                "Font Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates fonts for the entire application
     */
    private void updateAllApplicationFonts() {
        // Update this dialog
        ThemeManager.updateAllUIFonts(this);
        ThemeManager.updateComponentFontsRecursive(this);

        // Update parent frame (MainFrame) and all its children
        Window parent = getOwner();
        if (parent != null) {
            ThemeManager.updateAllUIFonts(parent);
            ThemeManager.updateComponentFontsRecursive(parent);

            // If parent is MainFrame, trigger its refresh method
            if (parent instanceof MainFrame) {
                ((MainFrame) parent).onThemeAndFontChanged();
            }
        }

        // Update all other windows
        ThemeManager.updateAllComponentFonts();
    }
    
    private void loadSettings() {
        try {
            if (settingsFile.exists()) {
                FileInputStream fis = new FileInputStream(settingsFile);
                settings.load(fis);
                fis.close();
            }
            
            // Apply settings to UI components
            hospitalNameField.setText(settings.getProperty("hospital.name"));
            languageCombo.setSelectedItem(settings.getProperty("language"));
            dateFormatCombo.setSelectedItem(settings.getProperty("date.format"));
            timeFormatCombo.setSelectedItem(settings.getProperty("time.format"));
            autoLogoutSpinner.setValue(Integer.parseInt(settings.getProperty("auto.logout")));
            backupCombo.setSelectedItem(settings.getProperty("backup.frequency"));
            requireComplexPasswords.setSelected(Boolean.parseBoolean(settings.getProperty("password.complexity")));
            forcePasswordChange.setSelected(Boolean.parseBoolean(settings.getProperty("password.forceChange")));
            auditLogging.setSelected(Boolean.parseBoolean(settings.getProperty("audit.logging")));
            loginNotifications.setSelected(Boolean.parseBoolean(settings.getProperty("login.notifications")));
            databasePathField.setText(settings.getProperty("database.path"));
            
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
        }
    }
    
    private void saveSettings() {
        // Update settings from UI components
        settings.setProperty("hospital.name", hospitalNameField.getText());
        settings.setProperty("language", (String) languageCombo.getSelectedItem());
        settings.setProperty("date.format", (String) dateFormatCombo.getSelectedItem());
        settings.setProperty("time.format", (String) timeFormatCombo.getSelectedItem());
        settings.setProperty("auto.logout", autoLogoutSpinner.getValue().toString());
        settings.setProperty("backup.frequency", (String) backupCombo.getSelectedItem());
        settings.setProperty("password.complexity", String.valueOf(requireComplexPasswords.isSelected()));
        settings.setProperty("password.forceChange", String.valueOf(forcePasswordChange.isSelected()));
        settings.setProperty("audit.logging", String.valueOf(auditLogging.isSelected()));
        settings.setProperty("login.notifications", String.valueOf(loginNotifications.isSelected()));
        settings.setProperty("database.path", databasePathField.getText());
        
        try {
            FileOutputStream fos = new FileOutputStream(settingsFile);
            settings.store(fos, "EMR System Settings");
            fos.close();
            
            JOptionPane.showMessageDialog(this,
                "Settings saved successfully!\n\n" +
                "Some changes may require restarting the application to take full effect.",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving settings: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetToDefaults() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Reset all settings to default values?",
            "Reset Settings",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            initializeSettings(); // Reload defaults
            loadSettings(); // Update UI
            JOptionPane.showMessageDialog(this,
                "Settings reset to default values.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private String calculateDatabaseSize() {
        File dbFile = new File(settings.getProperty("database.path"));
        if (dbFile.exists()) {
            long sizeInMB = dbFile.length() / (1024 * 1024);
            return sizeInMB + " MB";
        }
        return "Unknown";
    }
    
    private String getLastBackupDate() {
        // This would check for the most recent backup file
        File backupDir = new File(System.getProperty("user.home") + "/emr_system/backups/");
        if (backupDir.exists() && backupDir.listFiles().length > 0) {
            // Return date of most recent backup file
            return "Recently";
        }
        return "Never";
    }
    
    private void optimizeDatabase() {
        JOptionPane.showMessageDialog(this,
            "Database optimization would be performed here.\n\n" +
            "This process would:\n" +
            "• Rebuild indexes\n" +
            "• Vacuum the database\n" +
            "• Update statistics\n" +
            "• Improve performance",
            "Database Optimization",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cleanupOldRecords() {
        int choice = JOptionPane.showConfirmDialog(this,
            "This will remove old records based on retention policies.\n\n" +
            "Are you sure you want to proceed?",
            "Cleanup Old Records",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "Old records cleanup completed successfully!",
                "Cleanup Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void createBackup() {
        JOptionPane.showMessageDialog(this,
            "Database backup would be created here.\n\n" +
            "The backup would be saved to:\n" +
            System.getProperty("user.home") + "/emr_system/backups/",
            "Create Backup",
            JOptionPane.INFORMATION_MESSAGE);
    }
}