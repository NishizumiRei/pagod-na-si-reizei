package com.emr.gui;

import com.emr.EMR;
import com.emr.config.SettingsManager;
import com.emr.config.ThemeManager;
import com.emr.dao.PatientDAO;
import com.emr.database.DatabaseResetUtility;
import com.emr.database.DatabaseRepairUtility;
import com.emr.database.DatabaseSchemaFixer;
import com.emr.manager.DataManager;
import com.emr.models.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private DataManager dataManager;
    private MedicalStaff currentStaff;
    private SettingsManager settingsManager;
    
    // Dashboard components
    private JLabel totalPatientsLabel;
    private JLabel visitedPatientsLabel;
    private JLabel highPriorityLabel;
    private JPanel demographicsChartPanel;
    private JPanel priorityChartPanel;
    private JTable patientsTable;
    private JTable schedulesTable;
    
    // Tabbed pane
    private JTabbedPane tabbedPane;
    
    public MainFrame(User user, DataManager dataManager) {
        
        ThemeManager.initializeApplicationThemeAndFont();
        
        this.currentUser = user;
        this.dataManager = dataManager;
        
        // If user is staff, get their staff record
        if (user.getRole() == User.UserRole.DOCTOR || user.getRole() == User.UserRole.NURSE) {
            this.currentStaff = dataManager.getStaffByUserId(user.getId());
        }
        
        initializeUI();
        loadDashboardData();
    }
    
    private void initializeUI() {
        setTitle("CipherChart - Your Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setLocationRelativeTo(null);
        
        createMenuBar();
        createMainPanel();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        JMenuItem logoutItem = new JMenuItem("Log out");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        refreshItem.addActionListener(e -> refreshData());
        logoutItem.addActionListener(e -> performLogout());
        exitItem.addActionListener(e -> System.exit(0));
    
        fileMenu.add(refreshItem);
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        if (currentUser.getRole() == User.UserRole.ADMIN){
            JMenu adminMenu = new JMenu("Admin Tools");
            // User Management
            JMenu userManagementMenu = new JMenu("User Management");
            JMenuItem userManagementItem = new JMenuItem("Manage Users");
            userManagementItem.addActionListener(e -> showUserManagement());
            userManagementMenu.add(userManagementItem);

            // System Configuration
            JMenu systemMenu = new JMenu("System Configuration");
            JMenuItem systemSettingsItem = new JMenuItem("System Settings");
            JMenuItem reportsItem = new JMenuItem("Generate Reports");
            systemSettingsItem.addActionListener(e -> showSystemSettings());
            reportsItem.addActionListener(e -> showSystemReports());
            systemMenu.add(systemSettingsItem);
            systemMenu.add(reportsItem);

            // Database Tools
            JMenu databaseMenu = new JMenu("Database Tools");
            JMenuItem testConnectionItem = new JMenuItem("Test Connection");
            JMenuItem checkContentsItem = new JMenuItem("Check Contents");
            JMenuItem repairDbItem = new JMenuItem("Repair Database");
            JMenuItem resetDatabaseItem = new JMenuItem("Reset Database");

            testConnectionItem.addActionListener(e -> testDatabaseConnection());
            checkContentsItem.addActionListener(e -> checkDatabaseContents());
            repairDbItem.addActionListener(e -> repairDatabaseDates());
            resetDatabaseItem.addActionListener(e -> resetDatabase());

            databaseMenu.add(testConnectionItem);
            databaseMenu.add(checkContentsItem);
            databaseMenu.addSeparator();
            databaseMenu.add(repairDbItem);
            databaseMenu.add(resetDatabaseItem);

            // Add all submenus to admin menu
            adminMenu.add(userManagementMenu);
            adminMenu.add(systemMenu);
            adminMenu.add(databaseMenu);

            menuBar.add(adminMenu);
        }
        if (currentUser.getRole() != User.UserRole.PATIENT) {
            JMenu toolsMenu = new JMenu("Tools");
            JMenuItem patientInfoItem = new JMenuItem("Patient Information");

            patientInfoItem.addActionListener(e -> showPatientInformationDialog());

            toolsMenu.add(patientInfoItem);

            // Medical records only for doctors and admin
            if (currentUser.getRole() == User.UserRole.DOCTOR || currentUser.getRole() == User.UserRole.ADMIN) {
                JMenuItem medicalRecordItem = new JMenuItem("Medical Records");
                medicalRecordItem.addActionListener(e -> showMedicalRecordsDialog());
                toolsMenu.add(medicalRecordItem);
            }

            menuBar.add(toolsMenu);
        }
        // Help Menu (Available to all)
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }
    
    private JPanel createWelcomeTab() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        welcomePanel.setBackground(ThemeManager.getBackgroundColor()); // Theme-aware

        // Header Section
        JPanel headerPanel = createWelcomeHeader();

        // Main Content based on user role
        JPanel contentPanel = createRoleBasedContent();

        // Footer with system info
        JPanel footerPanel = createWelcomeFooter();

        welcomePanel.add(headerPanel, BorderLayout.NORTH);
        welcomePanel.add(contentPanel, BorderLayout.CENTER);
        welcomePanel.add(footerPanel, BorderLayout.SOUTH);

        return welcomePanel;
    }

    private JPanel createWelcomeHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.getBackgroundColor()); // Theme-aware
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel welcomeTitle = new JLabel("CipherChart", JLabel.CENTER);
        welcomeTitle.setForeground(ThemeManager.getForegroundColor()); // Theme-aware

        String roleDisplay = currentUser.getRole().toString();
        String greeting = "Welcome, " + currentUser.getUsername() + "!";

        JLabel userGreeting = new JLabel("<html><center>" + greeting + "<br>" +
                                       "<small>Role: " + roleDisplay + "</small></center></html>");
        userGreeting.setForeground(ThemeManager.getForegroundColor()); // Theme-aware
        userGreeting.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(welcomeTitle, BorderLayout.NORTH);
        headerPanel.add(userGreeting, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createRoleBasedContent() {
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        contentPanel.setBackground(Color.WHITE);

        switch (currentUser.getRole()) {
            case ADMIN:
                contentPanel.add(createAdminWelcomePanel());
                contentPanel.add(createSystemOverviewPanel());
                break;
            case DOCTOR:
                contentPanel.add(createDoctorWelcomePanel());
                contentPanel.add(createPatientQuickViewPanel());
                break;
            case NURSE:
                contentPanel.add(createNurseWelcomePanel());
                contentPanel.add(createPatientQuickViewPanel());
                break;
            case PATIENT:
                contentPanel.add(createPatientWelcomePanel());
                contentPanel.add(createPatientPersonalInfoPanel());
                break;
            default:
                contentPanel.add(createDefaultWelcomePanel());
        }

        return contentPanel;
    }

    private JPanel createAdminWelcomePanel() {
        JPanel panel = ThemeManager.createThemedPanel("Administrator Dashboard", true);
        panel.setLayout(new BorderLayout());

        JTextArea adminInfo = new JTextArea();
        adminInfo.setText("ADMINISTRATOR ACCESS\n\n" +
                         "• Full system access\n" +
                         "• User management\n" +
                         "• Database administration\n" +
                         "• System configuration\n" +
                         "• Audit logs access\n\n" +
                         "Use the Admin menu above to access all system features.");
        adminInfo.setEditable(false);
        adminInfo.setBackground(ThemeManager.getBackgroundColor()); 
        adminInfo.setForeground(ThemeManager.getForegroundColor()); 

        // Remove the Admin Tools buttons since they're in the menu
        JLabel menuNote = new JLabel("💡 Use the 'Admin' menu in the menu bar above for all administrative functions.");
        menuNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        menuNote.setForeground(new Color(0, 0, 0));
        menuNote.setBorder(new EmptyBorder(10, 0, 0, 0));

        panel.add(new JScrollPane(adminInfo), BorderLayout.CENTER);
        panel.add(menuNote, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDoctorWelcomePanel() {
        JPanel panel = createStyledPanel("Doctor Dashboard", new Color(230, 245, 255));
        panel.setLayout(new BorderLayout());

        JTextArea doctorInfo = new JTextArea();
        doctorInfo.setText("DOCTOR ACCESS\n\n" +
                          "• Patient medical records\n" +
                          "• Diagnosis and treatment\n" +
                          "• Prescription management\n" +
                          "• Patient consultations\n" +
                          "• Medical history review\n\n" +
                          "You have access to patient care features.");
        doctorInfo.setEditable(false);
        doctorInfo.setBackground(panel.getBackground());

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.add(createQuickActionButton("Patient Records", "👥", new Color(40, 167, 69), 
            e -> tabbedPane.setSelectedIndex(2)));
        buttonPanel.add(createQuickActionButton("Medical Records", "🏥", new Color(0, 123, 255), 
            e -> showMedicalRecordsDialog()));
        buttonPanel.add(createQuickActionButton("My Schedule", "📅", new Color(255, 193, 7), 
            e -> tabbedPane.setSelectedIndex(3)));

        panel.add(new JScrollPane(doctorInfo), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createNurseWelcomePanel() {
        JPanel panel = createStyledPanel("Nurse Dashboard", new Color(230, 255, 245));
        panel.setLayout(new BorderLayout());

        JTextArea nurseInfo = new JTextArea();
        nurseInfo.setText("NURSE ACCESS\n\n" +
                         "• Patient vital signs\n" +
                         "• Medication administration\n" +
                         "• Patient monitoring\n" +
                         "• Basic patient care\n" +
                         "• Schedule management\n\n" +
                         "You have access to nursing care features.");
        nurseInfo.setEditable(false);
        nurseInfo.setBackground(panel.getBackground());

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.add(createQuickActionButton("Patient List", "👥", new Color(40, 167, 69), 
            e -> tabbedPane.setSelectedIndex(2)));
        buttonPanel.add(createQuickActionButton("Vital Signs", "❤️", new Color(0, 123, 255), 
            e -> JOptionPane.showMessageDialog(this, "Vital Signs feature")));
        buttonPanel.add(createQuickActionButton("Nurse Schedule", "📅", new Color(255, 193, 7), 
            e -> tabbedPane.setSelectedIndex(3)));

        panel.add(new JScrollPane(nurseInfo), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPatientWelcomePanel() {
        JPanel panel = createStyledPanel("Patient Portal", new Color(255, 245, 230));
        panel.setLayout(new BorderLayout());

        JTextArea patientInfo = new JTextArea();
        patientInfo.setText("PATIENT ACCESS\n\n" +
                           "• View personal information\n" +
                           "• Check appointment schedules\n" +
                           "• View medical history (read-only)\n" +
                           "• Update contact information\n" +
                           "• Message healthcare providers\n\n" +
                           "Your access is limited to personal health information.");
        patientInfo.setEditable(false);
        patientInfo.setBackground(panel.getBackground());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.add(createQuickActionButton("My Information", "👤", new Color(40, 167, 69), 
            e -> showPatientPersonalInfo()));
        buttonPanel.add(createQuickActionButton("Appointments", "📅", new Color(0, 123, 255), 
            e -> JOptionPane.showMessageDialog(this, "Appointment viewing feature")));

        panel.add(new JScrollPane(patientInfo), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPatientPersonalInfoPanel() {
        JPanel panel = createStyledPanel("My Information", new Color(255, 250, 240));

        // For patients, show their own information
        try {
            // Find patient record for current user
            Patient patient = findPatientForCurrentUser();
            if (patient != null) {
                JTextArea infoArea = new JTextArea();
                infoArea.setText("Personal Information:\n\n" +
                               "Name: " + patient.getName() + "\n" +
                               "Age: " + patient.getAge() + "\n" +
                               "Gender: " + patient.getGender() + "\n" +
                               "Contact: " + patient.getContactInfo() + "\n" +
                               "Priority Level: " + patient.getPriorityLevel() + "\n" +
                               "Last Visit: " + (patient.getLastVisitDate() != null ? 
                                   patient.getLastVisitDate().toString() : "Never"));
                infoArea.setEditable(false);
                infoArea.setBackground(panel.getBackground());

                panel.add(new JScrollPane(infoArea));
            } else {
                panel.add(new JLabel("No patient information found."));
            }
        } catch (Exception e) {
            panel.add(new JLabel("Error loading patient information."));
        }

        return panel;
    }

    private JPanel createSystemOverviewPanel() {
        JPanel panel = createStyledPanel("System Overview", new Color(245, 245, 245));
        panel.setLayout(new BorderLayout());

        // Basic system stats (safe for all roles)
        int totalPatients = dataManager.getTotalPatients();
        int totalStaff = dataManager.getAllMedicalStaff().size();

        JTextArea statsArea = new JTextArea();
        statsArea.setText("System Statistics:\n\n" +
                         "Total Patients: " + totalPatients + "\n" +
                         "Medical Staff: " + totalStaff + "\n" +
                         "System Status: Online\n" +
                         "Last Update: " + new java.util.Date());
        statsArea.setEditable(false);
        statsArea.setBackground(panel.getBackground());

        panel.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        // Add role-specific additional info
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            JLabel adminNote = new JLabel("✓ Full administrative privileges active");
            adminNote.setForeground(new Color(40, 167, 69));
            adminNote.setBorder(new EmptyBorder(10, 0, 0, 0));
            panel.add(adminNote, BorderLayout.SOUTH);
        }

        return panel;
    }

    private JPanel createPatientQuickViewPanel() {
        JPanel panel = createStyledPanel("Patient Quick View", new Color(240, 248, 255));

        // For medical staff, show patient summary
        try {
            List<Patient> patients = dataManager.getAllPatients();
            JTextArea patientSummary = new JTextArea();
            patientSummary.setText("Patient Summary:\n\n" +
                                 "Total Patients: " + patients.size() + "\n" +
                                 "High Priority: " + dataManager.getHighPriorityPatientsCount() + "\n" +
                                 "Visited Today: " + dataManager.getVisitedPatientsCount() + "\n\n" +
                                 "Use the Patient Records tab for detailed information.");
            patientSummary.setEditable(false);
            patientSummary.setBackground(panel.getBackground());

            panel.add(new JScrollPane(patientSummary));
        } catch (Exception e) {
            panel.add(new JLabel("Unable to load patient summary."));
        }

        return panel;
    }

    private JPanel createWelcomeFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel footerLabel = new JLabel(
            "<html><center><small>EMR System v1.0 | CipherChart | " +
            "Logged in as: " + currentUser.getRole() + " | " +
            new java.util.Date() + "</small></center></html>", 
            JLabel.CENTER
        );
        footerLabel.setForeground(new Color(150, 150, 150));

        footerPanel.add(footerLabel, BorderLayout.CENTER);
        return footerPanel;
    }

    private JButton createQuickActionButton(String text, String emoji, Color color, java.awt.event.ActionListener action) {
        JButton button = new JButton("<html><center>" + emoji + "<br>" + text + "</center></html>");
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
    
        // Hover effect
            button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private Patient findPatientForCurrentUser() {
    // For patient users, find their patient record
        if (currentUser.getRole() == User.UserRole.PATIENT) {
            List<Patient> patients = dataManager.getAllPatients();
            for (Patient patient : patients) {
                if (patient.getUserId() != null && patient.getUserId() == currentUser.getId()) {
                    return patient;
                }
            }
        }
    return null;
    }

    private void showPatientPersonalInfo() {
    Patient patient = findPatientForCurrentUser();
    if (patient != null) {
        JOptionPane.showMessageDialog(this,
            "Your Information:\n\n" +
            "Name: " + patient.getName() + "\n" +
            "Age: " + patient.getAge() + "\n" +
            "Gender: " + patient.getGender() + "\n" +
            "Contact: " + patient.getContactInfo() + "\n" +
            "Insurance: " + (patient.getInsuranceInfo() != null ? patient.getInsuranceInfo() : "Not provided"),
            "My Information",
            JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this,
            "No patient information found for your account.",
            "Information Not Found",
            JOptionPane.WARNING_MESSAGE);
        }   
    }

    private void showAdminTools() {
    JOptionPane.showMessageDialog(this,
        "Admin Tools:\n\n" +
        "• Database Management\n" +
        "• User Account Control\n" +
        "• System Configuration\n" +
        "• Audit Logs\n" +
        "• Backup & Restore\n\n" +
        "Access these features from the Admin menu.",
        "Administrative Tools",
        JOptionPane.INFORMATION_MESSAGE);
    }
    private void createMainPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Welcome Tab (First tab - Homepage)
        tabbedPane.addTab("🏠 Welcome", createWelcomeTab());

        // Dashboard Tab (Statistics - Limited access for patients)
        if (currentUser.getRole() != User.UserRole.PATIENT) {
            tabbedPane.addTab("📊 Dashboard", createDashboardTab());
        }

        // Patient Records Tab (Medical staff and admin only)
        if (currentUser.getRole() != User.UserRole.PATIENT) {
            tabbedPane.addTab("👥 Patient Records", createPatientsTab());
        }

        // Medical Records Tab (Doctors and admin only)
        if (currentUser.getRole() == User.UserRole.DOCTOR || currentUser.getRole() == User.UserRole.ADMIN) {
            tabbedPane.addTab("🏥 Medical Records", createMedicalRecordsTab());
        }

        // Staff Schedules Tab (Medical staff and admin only)
        if (currentUser.getRole() != User.UserRole.PATIENT) {
            tabbedPane.addTab("📅 Staff Schedules", createSchedulesTab());
        }

        add(tabbedPane);
    }
    
    private JPanel createDashboardTab() {
        JPanel dashboardPanel = new JPanel(new BorderLayout(10, 10));
        dashboardPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        dashboardPanel.setBackground(ThemeManager.getBackgroundColor()); // Theme-aware

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.getBackgroundColor()); // Theme-aware

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setForeground(ThemeManager.getForegroundColor()); // Theme-aware

        JLabel titleLabel = new JLabel("EMR Statistics Overview", JLabel.CENTER);
        titleLabel.setForeground(ThemeManager.getForegroundColor()); // Theme-aware
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Key Metrics Panel
        JPanel metricsPanel = createMetricsPanel();
        
        // Charts Panel
        JPanel chartsPanel = createChartsPanel();
        
        // Recent Activity Panel
        JPanel activityPanel = createRecentActivityPanel();
        
        // Layout
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(metricsPanel);
        topPanel.add(activityPanel);
        
        dashboardPanel.add(headerPanel, BorderLayout.NORTH);
        dashboardPanel.add(topPanel, BorderLayout.CENTER);
        dashboardPanel.add(chartsPanel, BorderLayout.SOUTH);
        
        return dashboardPanel;
    }
    
    private void performLogout(){
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if(confirm == JOptionPane.YES_OPTION) {
            dispose();
            
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog();
                loginDialog.setVisible(true);
            });
        }
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = createStyledPanel("Key Metrics", new Color(248, 249, 250));
        panel.setLayout(new GridLayout(4, 1, 10, 10));

        // These should be JPanel components, not JLabel
        JPanel totalPatientsCard = createMetricCard("Total Patients", "0", new Color(0, 123, 255));
        JPanel visitedPatientsCard = createMetricCard("Visited Patients", "0", new Color(40, 167, 69));
        JPanel highPriorityCard = createMetricCard("High Priority", "0", new Color(220, 53, 69));
        JPanel staffCard = createMetricCard("Medical Staff", String.valueOf(dataManager.getAllMedicalStaff().size()), 
                                          new Color(255, 193, 7));

        panel.add(totalPatientsCard);
        panel.add(visitedPatientsCard);
        panel.add(highPriorityCard);
        panel.add(staffCard);

        // Store references to update later
        totalPatientsLabel = findLabelInPanel(totalPatientsCard);
        visitedPatientsLabel = findLabelInPanel(visitedPatientsCard);
        highPriorityLabel = findLabelInPanel(highPriorityCard);

        return panel;
    }

// Helper method to find the value label in the metric card
    private JLabel findLabelInPanel(JPanel card) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                // Return the first JLabel we find (should be the value label)
                return label;
            }
        }
        return new JLabel("0"); // Fallback
    }
    
    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color.darker(), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setForeground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        demographicsChartPanel = createStyledPanel("Patient Demographics", Color.WHITE);
        demographicsChartPanel.setLayout(new BorderLayout());
        
        priorityChartPanel = createStyledPanel("Priority Distribution", Color.WHITE);
        priorityChartPanel.setLayout(new BorderLayout());
        
        panel.add(demographicsChartPanel);
        panel.add(priorityChartPanel);
        
        return panel;
    }
    
    private JPanel createRecentActivityPanel() {
        JPanel panel = createStyledPanel("Recent Patient Activity", new Color(248, 249, 250));
        panel.setLayout(new BorderLayout());
        
        String[] columns = {"Patient", "Priority", "Visit Status", "Last Visit"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        
        // Load recent patients
        List<Patient> patients = dataManager.getAllPatients();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        
        for (Patient patient : patients) {
            String visitStatus = patient.isVisited() ? "✅ Visited" : "❌ Not Visited";
            String lastVisit = patient.getLastVisitDate() != null ? 
                sdf.format(patient.getLastVisitDate()) : "Never";
            
            model.addRow(new Object[]{
                patient.getName(),
                "Level " + patient.getPriorityLevel(),
                visitStatus,
                lastVisit
            });
        }
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStyledPanel(String title, Color backgroundColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.getBackgroundColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.getBorderColor(), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        // Use UIManager font instead of hardcoded font
        titleLabel.setForeground(ThemeManager.getForegroundColor());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }
    
    private JPanel createPatientsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header with buttons
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Patient Management");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Patient");
        JButton refreshButton = new JButton("Refresh");
        JButton editButton = new JButton("Edit Patient");
        JButton deleteButton = new JButton("Delete Patient");
        
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(0, 123, 255));
        refreshButton.setForeground(Color.WHITE);
        editButton.setBackground(new Color(255, 193, 7));
        editButton.setForeground(Color.BLACK);
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Patients table
        String[] columns = {"ID", "Name", "Age", "Gender", "Contact", "Priority", "Visited", "Last Visit"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        patientsTable = new JTable(model);
        patientsTable.setRowHeight(25);
        patientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(patientsTable);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Event listeners
        addButton.addActionListener(e -> showPatientInformationDialog());
        refreshButton.addActionListener(e -> loadPatientsData());
        editButton.addActionListener(e -> editSelectedPatient());
        deleteButton.addActionListener(e -> deleteSelectedPatient());
        
        loadPatientsData();
        return panel;
    }
    
        private JPanel createMedicalRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Medical Records Management");

        // Patient selection panel
        JPanel patientSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        patientSelectionPanel.add(new JLabel("Select Patient:"));

        JComboBox<Patient> patientComboBox = new JComboBox<>();
        loadPatientsIntoComboBox(patientComboBox);
        patientSelectionPanel.add(patientComboBox);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(0, 123, 255));
        refreshButton.setForeground(Color.WHITE);
        patientSelectionPanel.add(refreshButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(patientSelectionPanel, BorderLayout.EAST);

        // Medical records table
        String[] columns = {"ID", "Date", "Diagnosis", "Treatment", "Medications", "Doctor", "Created"};
        DefaultTableModel recordsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable recordsTable = new JTable(recordsModel);
        recordsTable.setRowHeight(25);
        recordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScrollPane = new JScrollPane(recordsTable);

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRecordButton = new JButton("Add New Record");
        JButton viewRecordButton = new JButton("View Record");
        JButton editRecordButton = new JButton("Edit Record");
        JButton deleteRecordButton = new JButton("Delete Record");
        JButton printRecordButton = new JButton("Print Record");

        addRecordButton.setBackground(new Color(40, 167, 69));
        addRecordButton.setForeground(Color.WHITE);
        viewRecordButton.setBackground(new Color(0, 123, 255));
        viewRecordButton.setForeground(Color.WHITE);
        editRecordButton.setBackground(new Color(255, 193, 7));
        editRecordButton.setForeground(Color.BLACK);
        deleteRecordButton.setBackground(new Color(220, 53, 69));
        deleteRecordButton.setForeground(Color.WHITE);
        printRecordButton.setBackground(new Color(108, 117, 125));
        printRecordButton.setForeground(Color.WHITE);

        actionPanel.add(addRecordButton);
        actionPanel.add(viewRecordButton);
        actionPanel.add(editRecordButton);
        actionPanel.add(deleteRecordButton);
        actionPanel.add(printRecordButton);

        // Patient info panel
        JPanel patientInfoPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        patientInfoPanel.setBorder(new TitledBorder("Patient Information"));

        JTextArea patientInfoArea = new JTextArea();
        patientInfoArea.setEditable(false);
        patientInfoArea.setBackground(patientInfoPanel.getBackground());

        JTextArea medicalSummaryArea = new JTextArea();
        medicalSummaryArea.setEditable(false);
        medicalSummaryArea.setBackground(patientInfoPanel.getBackground());

        patientInfoPanel.add(new JScrollPane(patientInfoArea));
        patientInfoPanel.add(new JScrollPane(medicalSummaryArea));

        // Assemble the main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        panel.add(patientInfoPanel, BorderLayout.SOUTH);

        // Adjust layout to include both action panel and patient info
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(patientInfoPanel, BorderLayout.CENTER);
        southPanel.add(actionPanel, BorderLayout.SOUTH);

        panel.remove(actionPanel);
        panel.remove(patientInfoPanel);
        panel.add(southPanel, BorderLayout.SOUTH);

        // Event listeners
        patientComboBox.addActionListener(e -> {
            Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
            if (selectedPatient != null) {
                loadPatientMedicalRecords(selectedPatient.getId(), recordsModel);
                updatePatientInfo(selectedPatient, patientInfoArea, medicalSummaryArea);
            } else {
                recordsModel.setRowCount(0);
                patientInfoArea.setText("");
                medicalSummaryArea.setText("");
            }
        });

        refreshButton.addActionListener(e -> {
            loadPatientsIntoComboBox(patientComboBox);
            Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
            if (selectedPatient != null) {
                loadPatientMedicalRecords(selectedPatient.getId(), recordsModel);
                updatePatientInfo(selectedPatient, patientInfoArea, medicalSummaryArea);
            }
        });

        addRecordButton.addActionListener(e -> {
            Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
            if (selectedPatient != null) {
                addNewMedicalRecord(selectedPatient);
            } else {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a patient first.", 
                    "No Patient Selected", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        viewRecordButton.addActionListener(e -> {
            int selectedRow = recordsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int recordId = (int) recordsModel.getValueAt(selectedRow, 0);
                viewMedicalRecord(recordId);
            } else {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a medical record to view.", 
                    "No Record Selected", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        editRecordButton.addActionListener(e -> {
            int selectedRow = recordsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int recordId = (int) recordsModel.getValueAt(selectedRow, 0);
                editMedicalRecord(recordId);
            } else {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a medical record to edit.", 
                    "No Record Selected", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteRecordButton.addActionListener(e -> {
            int selectedRow = recordsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int recordId = (int) recordsModel.getValueAt(selectedRow, 0);
                deleteMedicalRecord(recordId, (Patient) patientComboBox.getSelectedItem(), recordsModel);
            } else {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a medical record to delete.", 
                    "No Record Selected", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        printRecordButton.addActionListener(e -> {
            int selectedRow = recordsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int recordId = (int) recordsModel.getValueAt(selectedRow, 0);
                printMedicalRecord(recordId);
            } else {
                JOptionPane.showMessageDialog(panel, 
                    "Please select a medical record to print.", 
                    "No Record Selected", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        // Load initial data if a patient is selected
        if (patientComboBox.getItemCount() > 0) {
            patientComboBox.setSelectedIndex(0);
        }

        return panel;
    }

    // Helper methods
    private void loadPatientsIntoComboBox(JComboBox<Patient> comboBox) {
        comboBox.removeAllItems();
        List<Patient> patients = dataManager.getAllPatients();

        DefaultComboBoxModel<Patient> model = new DefaultComboBoxModel<>();
        for (Patient patient : patients) {
            model.addElement(patient);
        }

        comboBox.setModel(model);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Patient) {
                    Patient patient = (Patient) value;
                    setText(patient.getName() + " (ID: " + patient.getId() + ", Age: " + patient.getAge() + ")");
                }
                return this;
            }
        });
    }

    private void loadPatientMedicalRecords(int patientId, DefaultTableModel model) {
        model.setRowCount(0);
        List<MedicalRecord> records = dataManager.getPatientRecords(patientId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

        for (MedicalRecord record : records) {
            String doctorName = "Unknown";
            if (record.getDoctorId() != 0) {
                MedicalStaff doctor = dataManager.getStaffByUserId(record.getDoctorId());
                if (doctor != null) {
                    doctorName = doctor.getName();
                }
            }

            model.addRow(new Object[]{
                record.getId(),
                record.getRecordDate() != null ? dateFormat.format(record.getRecordDate()) : "N/A",
                truncateText(record.getDiagnosis(), 30),
                truncateText(record.getTreatment(), 30),
                truncateText(record.getMedications(), 30),
                doctorName,
                record.getCreatedAt() != null ? datetimeFormat.format(record.getCreatedAt()) : "N/A"
            });
        }
    }

    private void updatePatientInfo(Patient patient, JTextArea infoArea, JTextArea summaryArea) {
        // Basic patient information
        StringBuilder info = new StringBuilder();
        info.append("Name: ").append(patient.getName()).append("\n");
        info.append("Age: ").append(patient.getAge()).append("\n");
        info.append("Gender: ").append(patient.getGender()).append("\n");
        info.append("Priority Level: ").append(patient.getPriorityLevel()).append("\n");
        info.append("Last Visit: ").append(patient.getLastVisitDate() != null ? 
            new SimpleDateFormat("yyyy-MM-dd").format(patient.getLastVisitDate()) : "Never").append("\n");
        info.append("Insurance: ").append(patient.getInsuranceInfo() != null ? patient.getInsuranceInfo() : "Not provided");

        infoArea.setText(info.toString());

        // Medical summary
        List<MedicalRecord> records = dataManager.getPatientRecords(patient.getId());
        StringBuilder summary = new StringBuilder();
        summary.append("Total Records: ").append(records.size()).append("\n\n");

        if (!records.isEmpty()) {
            summary.append("Recent Diagnoses:\n");
            Set<String> recentDiagnoses = new HashSet<>();
            for (int i = 0; i < Math.min(3, records.size()); i++) {
                MedicalRecord record = records.get(i);
                if (record.getDiagnosis() != null && !record.getDiagnosis().trim().isEmpty()) {
                    recentDiagnoses.add(record.getDiagnosis());
                }
            }
            for (String diagnosis : recentDiagnoses) {
                summary.append("• ").append(truncateText(diagnosis, 40)).append("\n");
            }
        } else {
            summary.append("No medical records found.");
        }

        summaryArea.setText(summary.toString());
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "N/A";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    // Action methods (to be implemented)
    private void addNewMedicalRecord(Patient patient) {
        if (patient == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a patient first.",
                "No Patient Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            MedicalRecordDialog dialog = new MedicalRecordDialog(this, dataManager);
            dialog.setVisible(true);

            refreshMedicalRecordsForCurrentPatient();


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error creating medical record: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void viewMedicalRecord(int recordId) {
        try {
            MedicalRecord record = dataManager.getMedicalRecordById(recordId);
            if (record != null) {
                // Create a view-only dialog
                JDialog viewDialog = new JDialog(this, "View Medical Record", true);
                viewDialog.setSize(600, 500);
                viewDialog.setLocationRelativeTo(this);

                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

                // Header
                JLabel titleLabel = new JLabel("Medical Record Details", JLabel.CENTER);

                // Record details in a text area
                JTextArea recordDetails = new JTextArea();
                recordDetails.setEditable(false);
                recordDetails.setText(formatMedicalRecordForDisplay(record));

                JScrollPane scrollPane = new JScrollPane(recordDetails);

                // Close button
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> viewDialog.dispose());

                JPanel buttonPanel = new JPanel(new FlowLayout());
                buttonPanel.add(closeButton);

                mainPanel.add(titleLabel, BorderLayout.NORTH);
                mainPanel.add(scrollPane, BorderLayout.CENTER);
                mainPanel.add(buttonPanel, BorderLayout.SOUTH);

                viewDialog.add(mainPanel);
                viewDialog.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(this,
                    "Medical record not found!",
                    "Record Not Found",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error viewing medical record: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void editMedicalRecord(int recordId) {
        try {
            MedicalRecord record = dataManager.getMedicalRecordById(recordId);
            if (record != null) {
                MedicalRecordDialog dialog = new MedicalRecordDialog(this, dataManager);
                dialog.setVisible(true);
                
                refreshMedicalRecordsForCurrentPatient();
                
            } else {
                JOptionPane.showMessageDialog(this,
                    "Medical record not found!",
                    "Record Not Found",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error editing medical record: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Helper methods
    private void refreshMedicalRecordsForCurrentPatient() {
        // This method should refresh the medical records table
        // You'll need to implement this based on your UI structure
        Component centerComponent = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) centerComponent;
            Component medicalRecordsTab = tabbedPane.getComponentAt(2); // Adjust index as needed

            // You'll need to traverse the component hierarchy to find your table
            // This is a simplified approach - you might need to adjust it
            refreshMedicalRecordsTable();
        }
    }
    
    private void showMedicalRecordViewDialog(MedicalRecord record) {
        JDialog viewDialog = new JDialog(this, "View Medical Record", true);
        viewDialog.setSize(500, 400);
        viewDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JLabel titleLabel = new JLabel("Medical Record Details", JLabel.CENTER);

        // Content
        JTextArea contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setText(formatMedicalRecordForDisplay(record));

        JScrollPane scrollPane = new JScrollPane(contentArea);

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> viewDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        viewDialog.add(mainPanel);
        viewDialog.setVisible(true);
    }

    private void refreshMedicalRecordsTable() {
        // Implement this to refresh your medical records table
        // You'll need access to your table model
        SwingUtilities.invokeLater(() -> {
            // Force refresh of the medical records tab
            tabbedPane.setSelectedIndex(2); // Switch to medical records tab
            // Additional refresh logic here
        });
    }

    private String formatMedicalRecordForDisplay(MedicalRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("MEDICAL RECORD DETAILS\n");
        sb.append("=====================\n\n");

        sb.append("Record ID: ").append(record.getId()).append("\n");
        sb.append("Patient ID: ").append(record.getPatientId()).append("\n");

        if (record.getRecordDate() != null) {
            sb.append("Record Date: ").append(new SimpleDateFormat("yyyy-MM-dd").format(record.getRecordDate())).append("\n");
        }

        sb.append("\nDIAGNOSIS:\n");
        sb.append("----------\n");
        sb.append(record.getDiagnosis() != null ? record.getDiagnosis() : "Not specified").append("\n");

        sb.append("\nTREATMENT:\n");
        sb.append("----------\n");
        sb.append(record.getTreatment() != null ? record.getTreatment() : "Not specified").append("\n");

        sb.append("\nMEDICATIONS:\n");
        sb.append("------------\n");
        sb.append(record.getMedications() != null ? record.getMedications() : "Not specified").append("\n");

        sb.append("\nNOTES:\n");
        sb.append("------\n");
        sb.append(record.getNotes() != null ? record.getNotes() : "No additional notes").append("\n");

        if (record.getCreatedAt() != null) {
            sb.append("\nCreated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(record.getCreatedAt())).append("\n");
        }

        return sb.toString();
    }

    private void printMedicalRecord(int recordId) {
        JOptionPane.showMessageDialog(this,
            "Print functionality for medical record ID: " + recordId + "\n\n" +
            "This would generate a printable report of the medical record.",
            "Print Medical Record",
            JOptionPane.INFORMATION_MESSAGE);
    }

        private JPanel createSchedulesTab() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));

            // Header
            JPanel headerPanel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel("Medical Staff Schedules");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton addScheduleButton = new JButton("Add Schedule");
            JButton refreshButton = new JButton("Refresh");

            addScheduleButton.setBackground(new Color(40, 167, 69));
            addScheduleButton.setForeground(Color.WHITE);
            refreshButton.setBackground(new Color(0, 123, 255));
            refreshButton.setForeground(Color.WHITE);

            buttonPanel.add(addScheduleButton);
            buttonPanel.add(refreshButton);

            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(buttonPanel, BorderLayout.EAST);

            // Schedules table
            String[] columns = {"Staff", "Date", "Start", "End", "Activity", "Patient", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            schedulesTable = new JTable(model);
            schedulesTable.setRowHeight(25);

            JScrollPane scrollPane = new JScrollPane(schedulesTable);

            panel.add(headerPanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);

            // Event listeners
            refreshButton.addActionListener(e -> loadSchedulesData());
            addScheduleButton.addActionListener(e -> showAddScheduleDialog());

            loadSchedulesData();
            return panel;
        }
    
    private void loadDashboardData() {
        // Update metrics
        int totalPatients = dataManager.getTotalPatients();
        long visitedPatients = dataManager.getVisitedPatientsCount();
        long highPriority = dataManager.getHighPriorityPatientsCount();
        
        // Update labels through the card panels (this would need refactoring for direct label access)
        // For now, we'll repaint the charts
        demographicsChartPanel.removeAll();
        priorityChartPanel.removeAll();
        
        // Add chart components
        demographicsChartPanel.add(new StatisticsChart("Gender Distribution", dataManager.getGenderDistribution()));
        priorityChartPanel.add(new StatisticsChart("Priority Levels", dataManager.getPriorityDistribution()));
        
        demographicsChartPanel.revalidate();
        demographicsChartPanel.repaint();
        priorityChartPanel.revalidate();
        priorityChartPanel.repaint();
        
        loadPatientsData();
        loadSchedulesData();
    }
    
    private void loadPatientsData() {
        DefaultTableModel model = (DefaultTableModel) patientsTable.getModel();
        model.setRowCount(0);
        
        List<Patient> patients = dataManager.getAllPatients();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        
        for (Patient patient : patients) {
            String lastVisit = patient.getLastVisitDate() != null ? 
                sdf.format(patient.getLastVisitDate()) : "Never";
            
            model.addRow(new Object[]{
                patient.getId(),
                patient.getName(),
                patient.getAge(),
                patient.getGender(),
                patient.getContactInfo(),
                "Level " + patient.getPriorityLevel(),
                patient.isVisited() ? "Yes" : "No",
                lastVisit
            });
        }
    }
    
    private void loadSchedulesData() {
        DefaultTableModel model = (DefaultTableModel) schedulesTable.getModel();
        model.setRowCount(0);
        
        List<StaffSchedule> schedules = dataManager.getAllSchedules();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        for (StaffSchedule schedule : schedules) {
            model.addRow(new Object[]{
                "Staff ID: " + schedule.getStaffId(),
                dateFormat.format(schedule.getScheduleDate()),
                timeFormat.format(schedule.getStartTime()),
                timeFormat.format(schedule.getEndTime()),
                schedule.getActivityType(),
                schedule.getPatientId() != null ? "Patient ID: " + schedule.getPatientId() : "N/A",
                schedule.getStatus()
            });
        }
    }
    
    private void populateSchedulesTable(List<StaffSchedule> schedules) {
        DefaultTableModel model = (DefaultTableModel) schedulesTable.getModel();
        model.setRowCount(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        for (StaffSchedule schedule : schedules) {
            // Get staff name
            String staffName = "Unknown Staff";
            MedicalStaff staff = dataManager.getStaffByUserId(schedule.getStaffId());
            if (staff != null) {
                staffName = staff.getName();
            }

            // Get patient name if exists
            String patientName = "N/A";
            if (schedule.getPatientId() != null) {
                Patient patient = dataManager.getPatientById(schedule.getPatientId());
                if (patient != null) {
                    patientName = patient.getName();
                }
            }

            model.addRow(new Object[]{
                schedule.getId(),
                staffName,
                dateFormat.format(schedule.getScheduleDate()),
                timeFormat.format(schedule.getStartTime()),
                timeFormat.format(schedule.getEndTime()),
                schedule.getActivityType(),
                patientName,
                schedule.getStatus(),
                schedule.getDescription() != null ? schedule.getDescription() : ""
            });
        }

        // Show message if no schedules
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "No schedules found. Click 'Add Schedule' to create new schedules.",
                "No Schedules", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void refreshData() {
        loadDashboardData();
        JOptionPane.showMessageDialog(this, "Data refreshed successfully!", "Refresh", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Database utility methods
    private void testDatabaseConnection() {
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        JDialog progDiag = new JDialog(this, "Testing Connection", true);
        progDiag.setSize(300, 100);
        progDiag.setLocationRelativeTo(this);
        progDiag.add(progressBar);
        
        new Thread(() -> {
            try{
                progDiag.setVisible(true);
            }catch(Exception e){
                
            }
        }).start();
        
        new Thread(() -> {
           try{
               Thread.sleep(1500);
               
               boolean success = false;
               String details = "";
               
               try{
                   var conn = com.emr.database.DatabaseConnection.getConnection();
                   var metaData = conn.getMetaData();
                   
                   success = true;
                   details = String.format("Database Connection Successful!\n\n" +
                           "Database: %s\n" +
                           "Version: %s\n"+
                           "Driver: %s\n"+
                           "URL: %s\n"+
                           "All database operations are functioning correctly.",
                   metaData.getDatabaseProductName(),
                   metaData.getDatabaseProductVersion(),
                   metaData.getDriverName(),
                   metaData.getURL()
                           );
                   conn.close();
               } catch(Exception e){
                   success = false;
                   details = String.format("❌ Database Connection Failed!\n\n" +
                    "Error: %s\n\n" +
                    "Possible issues:\n" +
                    "• Database file is corrupted\n" +
                    "• File permissions issue\n" +
                    "• SQLite driver not available\n\n" +
                    "Try using 'Repair Database Dates' or 'Reset Database'.",
                    e.getMessage()
                );
               }
               
               final boolean finalSuccess = success;
               final String finalDetails = details;
               
               SwingUtilities.invokeLater(() -> {
                   progDiag.dispose();
                   
                   JTextArea textArea = new JTextArea(finalDetails);
                   textArea.setEditable(false);
                   textArea.setBackground(finalSuccess ? new Color(230, 255, 230) : new Color(255, 230, 230));
                   
                   JScrollPane scrPan = new JScrollPane(textArea);
                   scrPan.setPreferredSize(new Dimension(500, 300));
                   
                   JOptionPane.showMessageDialog(this, scrPan,
                           "Database Connection Test",
                           finalSuccess ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
               });
           } catch(Exception e){
               SwingUtilities.invokeLater(() -> {
                progDiag.dispose();
                JOptionPane.showMessageDialog(this, 
                    "Error during connection test: " + e.getMessage(), 
                    "Test Error", 
                    JOptionPane.ERROR_MESSAGE);
               });
           }
        }).start();
        
    }
    
    private void checkDatabaseContents() {
        try{
            DataManager datMan = new DataManager();
            StringBuilder report = new StringBuilder();
            
            report.append("Database Contents Report: \n");
            report.append("Generated: ").append(new java.util.Date()).append("\n");
            report.append("=").append("=".repeat(50)).append("\n\n");
            
            var users = datMan.getAllUsers();
            report.append("USERS ( ").append(users.size()).append("):\n");
            for (User user : users) {
                report.append(String.format("  %-10s %-15s %-10s %s\n", 
                user.getId(), user.getUsername(), user.getRole(), 
                user.isActive() ? "Active" : "Inactive"));
            }
            
            var patients = datMan.getAllPatients();
            report.append("\nPATIENTS (").append(patients.size()).append("):\n");
            for(Patient patient : patients){
                report.append(String.format("  %-3s %-20s %-3s %-6s Priority: %s %s\n", 
                patient.getId(), 
                patient.getName().length() > 20 ? patient.getName().substring(0, 17) + "..." : patient.getName(),
                patient.getAge(), 
                patient.getGender(),
                patient.getPriorityLevel(),
                patient.isVisited() ? "✅ Visited" : "❌ Not Visited"));
            }
            
            var staff = datMan.getAllMedicalStaff();
            report.append("\nMedicalStaff");
            for (MedicalStaff ms : staff) {
            report.append(String.format("  %-3s %-20s %-15s %s\n", 
                ms.getId(), ms.getName(), ms.getSpecialization(), ms.getDepartment()));
            
            }
            var schedules = datMan.getAllSchedules();
            report.append("\nSCHEDULES (").append(schedules.size()).append("):\n");
            for (StaffSchedule schedule : schedules) {
            report.append(String.format("  %-3s Staff:%-3s %-12s %s-%s %-15s %s\n", 
                schedule.getId(), schedule.getStaffId(),
                new SimpleDateFormat("yyyy-MM-dd").format(schedule.getScheduleDate()),
                new SimpleDateFormat("HH:mm").format(schedule.getStartTime()),
                new SimpleDateFormat("HH:mm").format(schedule.getEndTime()),
                schedule.getActivityType(),
                schedule.getStatus()));
            }
            // Medical Records
            int totalRecords = 0;
            for (Patient patient : patients) {
            totalRecords += datMan.getPatientRecords(patient.getId()).size();
            }
            report.append("\nMEDICAL RECORDS: ").append(totalRecords).append(" total records\n");
        
            // Summary
            report.append("\nSUMMARY:\n");
            report.append(String.format("  Total Users: %d\n", users.size()));
            report.append(String.format("  Total Patients: %d\n", patients.size()));
            report.append(String.format("  Total Staff: %d\n", staff.size()));
            report.append(String.format("  Total Schedules: %d\n", schedules.size()));
            report.append(String.format("  Total Medical Records: %d\n", totalRecords));
        
            // Display the report
            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
        
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(700, 500));
        
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Database Contents Report", 
                JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e){
        JOptionPane.showMessageDialog(this,
                "Error checking database contents: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }    
    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to reset the database? This action cannot be undone.",
            "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Final confirmation with password for critical operation
            String password = JOptionPane.showInputDialog(this,
                "<html><b>Final Confirmation Required</b><br><br>" +
                "Please type 'RESET' to confirm database reset:<br>" +
                "<small>(This is case-sensitive)</small></html>",
                "Final Confirmation",
                JOptionPane.WARNING_MESSAGE);

            if ("RESET".equals(password)) {
                // Show progress dialog
                JDialog progressDialog = new JDialog(this, "Resetting Database", true);
                progressDialog.setLayout(new BorderLayout());
                progressDialog.setSize(400, 120);
                progressDialog.setLocationRelativeTo(this);

                JLabel progressLabel = new JLabel("Resetting database... This may take a moment.", JLabel.CENTER);
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);

                progressDialog.add(progressLabel, BorderLayout.CENTER);
                progressDialog.add(progressBar, BorderLayout.SOUTH);

                // Run the reset in a background thread
                new Thread(() -> {
                    try {
                        progressDialog.setVisible(true);
                    } catch (Exception e) {
                        // Ignore visibility exceptions
                    }
                }).start();

                new Thread(() -> {
                    boolean success = false;
                    String resultMessage = "";


                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();

                        JOptionPane.showMessageDialog(this,
                            resultMessage,
                            "Reset Operation Complete",
                            success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

                        if (success) {
                            // Restart the application or refresh all data
                            int restartChoice = JOptionPane.showConfirmDialog(this,
                                "<html><b>Restart Required</b><br><br>" +
                                "The database has been reset successfully.<br>" +
                                "Do you want to restart the application now?</html>",
                                "Restart Application",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                            if (restartChoice == JOptionPane.YES_OPTION) {
                                // Close current application and restart
                                dispose();
                                new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            // Restart the EMR application
                                            try {
                                                String javaBin = System.getProperty("java.home") + "/bin/java";
                                                String classpath = System.getProperty("java.class.path");
                                                String className = EMR.class.getName();

                                                ProcessBuilder builder = new ProcessBuilder(
                                                    javaBin, "-cp", classpath, className
                                                );
                                                builder.start();
                                            } catch (Exception ex) {
                                                // Fallback: just show message
                                                JOptionPane.showMessageDialog(null,
                                                    "Please manually restart the application.",
                                                    "Restart Required",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                            }
                                            System.exit(0);
                                        }
                                    },
                                    1000
                                );
                            } else {
                                // Just refresh all data
                                refreshData();
                                JOptionPane.showMessageDialog(this,
                                    "Please note: Some features may not work correctly until the application is restarted.",
                                    "Manual Restart Recommended",
                                    JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    });
                }).start();

            } else {
                if (password != null) { // User didn't click cancel
                    JOptionPane.showMessageDialog(this,
                        "<html>Reset operation cancelled.<br>" +
                        "The confirmation text did not match 'RESET'.</html>",
                        "Reset Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
    
    private void fixDatabaseSchema() {
    int choice = JOptionPane.showConfirmDialog(this,
        "This will check and fix any database schema issues.\n\n" +
        "This process will:\n" +
        "• Add missing tables if they don't exist\n" +
        "• Add missing columns to existing tables\n" +
        "• Fix data type inconsistencies\n" +
        "• Update table constraints\n\n" +
        "Do you want to continue?",
        "Fix Database Schema",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    
        if (choice == JOptionPane.YES_OPTION) {
            // Show progress dialog
            JDialog progressDialog = new JDialog(this, "Fixing Database Schema", true);
            progressDialog.setLayout(new BorderLayout());
            progressDialog.setSize(400, 120);
            progressDialog.setLocationRelativeTo(this);

            JLabel progressLabel = new JLabel("Checking and fixing database schema...", JLabel.CENTER);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);

            progressDialog.add(progressLabel, BorderLayout.CENTER);
            progressDialog.add(progressBar, BorderLayout.SOUTH);

            // Run the schema fix in a background thread
            new Thread(() -> {
                try {
                    progressDialog.setVisible(true);
                } catch (Exception e) {
                    // Ignore visibility exceptions
                }
            }).start();

            new Thread(() -> {

                    // Use your DatabaseSchemaFixer class
                    DatabaseSchemaFixer schemaFixer = new DatabaseSchemaFixer();
                    schemaFixer.fixDatabaseSchema();


                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(this,
                            "Error fixing database schema:\n",
                            "Schema Fix Error",
                            JOptionPane.ERROR_MESSAGE);
                    });
                });
        }
    }
    
    private void showPatientInformationDialog() {
        PatientInformationDialog dialog = new PatientInformationDialog(this, dataManager);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadPatientsData();
            loadDashboardData();
        }
    }
    
    private void showMedicalRecordsDialog() {
        MedicalRecordDialog dialog = new MedicalRecordDialog(this, dataManager);
        dialog.setVisible(true);
    }
    
    private void showAddScheduleDialog() {
        StaffScheduleDialog dialog = new StaffScheduleDialog(this, dataManager, null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadSchedulesData();
            JOptionPane.showMessageDialog(this, 
                "Schedule created successfully!\n\nThe schedules list has been updated.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }    
    }
    
    private void editSelectedPatient() {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient to edit.");
            return;
        }
        
        try{
        
            int patientId = (int) patientsTable.getValueAt(selectedRow, 0);

            PatientDAO patientDAO = new PatientDAO();
            Patient patient = patientDAO.getPatientById(patientId);

            if(patient != null) {
                PatientEditDialog editDialog = new PatientEditDialog(this, patient);
                editDialog.setVisible(true);

                if(editDialog.isSaved()){
                    refreshPatientsTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Patient not found!");
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(this, "Error editing patient: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteSelectedPatient() {
        if (currentUser.getRole() == User.UserRole.PATIENT) {
        JOptionPane.showMessageDialog(this,
            "You do not have permission to delete patients.",
            "Permission Denied",
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    if (currentUser.getRole() == User.UserRole.NURSE) {
        JOptionPane.showMessageDialog(this,
            "Nurses cannot delete patient records. Please contact a doctor or administrator.",
            "Permission Denied",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
        int selectedRow = patientsTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Please select a patient to delete.", 
            "No Selection", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    try {
        // Get patient details from the table
        int patientId = (Integer) patientsTable.getValueAt(selectedRow, 0);
        String patientName = (String) patientsTable.getValueAt(selectedRow, 1);
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>Are you sure you want to delete this patient?</b><br><br>" +
            "Patient: " + patientName + "<br>" +
            "ID: " + patientId + "<br><br>" +
            "<font color='red'>This action cannot be undone!</font></html>",
            "Confirm Patient Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Check if patient has medical records first
            if (hasMedicalRecords(patientId)) {
                int confirmWithRecords = JOptionPane.showConfirmDialog(this,
                    "<html><b>Warning: This patient has medical records!</b><br><br>" +
                    "Deleting this patient will also remove all their medical history.<br>" +
                    "Are you absolutely sure you want to continue?</html>",
                    "Patient Has Medical Records",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
                
                if (confirmWithRecords != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Perform deletion
            boolean success = dataManager.deletePatient(patientId);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Patient '" + patientName + "' has been successfully deleted.",
                    "Deletion Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh the table
                loadPatientsData();
                loadDashboardData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete patient. Please try again.",
                    "Deletion Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
            } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Error deleting patient: " + e.getMessage(),
            "Deletion Error",
            JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasMedicalRecords(int patientId) {
        try {
            List<MedicalRecord> records = dataManager.getPatientRecords(patientId);
                return !records.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private JPanel createDefaultWelcomePanel() {
        JPanel panel = createStyledPanel("Welcome to EMR System", new Color(248, 249, 250));
        panel.setLayout(new BorderLayout());

        JTextArea welcomeText = new JTextArea();
        welcomeText.setText("Welcome to CipherChart™!\n\n" +
                           "This system provides comprehensive healthcare management\n" +
                           "tools for medical professionals and administrators.\n\n" +
                           "Features Available:\n" +
                           "• Patient record management\n" +
                           "• Medical staff scheduling\n" +
                           "• Secure data access\n" +
                           "• Reporting and analytics\n\n" +
                           "Please use the navigation menu to access different features\n" +
                           "based on your role permissions.");
        welcomeText.setEditable(false);
        welcomeText.setBackground(panel.getBackground());
        welcomeText.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Center the text
        welcomeText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(panel.getBackground());
        centerPanel.add(welcomeText, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void showAboutDialog() {
        JDialog aboutDialog = new JDialog(this, "About CipherChart", true);
        aboutDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        aboutDialog.setSize(500, 600);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header with icon and title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel iconLabel = new JLabel("🏥", JLabel.CENTER);
        iconLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("CipherChart™", JLabel.CENTER);
        titleLabel.setForeground(new Color(0, 102, 204));

        JLabel versionLabel = new JLabel("Version 1.0", JLabel.CENTER);
        versionLabel.setForeground(new Color(100, 100, 100));

        headerPanel.add(iconLabel, BorderLayout.NORTH);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(versionLabel, BorderLayout.SOUTH);

        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(15, 0, 15, 0));

        // System information
        String[] infoLines = {
            "CipherChart EMR - Professional Healthcare Management",
            "",
            "Developed for: Healthcare Professionals",
            "License: Proprietary",
            "Database: SQLite",
            "",
            "Features:",
            "• Patient Record Management",
            "• Medical Staff Scheduling",
            "• Medical Records System",
            "• Role-based Access Control",
            "• Statistics and Reporting",
            "",
            "Security: HIPAA Compliant Design",
            "Data Protection: AES-256 Encryption"
        };

        for (String line : infoLines) {
            JLabel infoLabel = new JLabel(line);
            infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (line.startsWith("•")) {
                infoLabel.setBorder(new EmptyBorder(2, 20, 2, 0));
            } else if (line.isEmpty()) {
                infoLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            } else {
                infoLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
            }
            contentPanel.add(infoLabel);
        }

        // Footer with copyright and system info
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel copyrightLabel = new JLabel("© 2024 CipherChart Healthcare Solutions", JLabel.CENTER);
        copyrightLabel.setForeground(new Color(150, 150, 150));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel systemLabel = new JLabel("Java " + System.getProperty("java.version") + 
                                       " | " + System.getProperty("os.name"), JLabel.CENTER);
        systemLabel.setForeground(new Color(180, 180, 180));
        systemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        footerPanel.add(copyrightLabel);
        footerPanel.add(Box.createVerticalStrut(5));
        footerPanel.add(systemLabel);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(0, 123, 255));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(100, 30));

        closeButton.addActionListener(e -> aboutDialog.dispose());

        buttonPanel.add(closeButton);

        // Assemble the dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Adjust layout to include both footer and button panel
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(Color.WHITE);
        southPanel.add(footerPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.remove(footerPanel);
        mainPanel.remove(buttonPanel);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        aboutDialog.add(mainPanel);
        aboutDialog.setVisible(true);
    }
    
    private void fixDatabaseIssues() {
    if (currentUser.getRole() == User.UserRole.ADMIN) {
        int choice = JOptionPane.showConfirmDialog(this,
            "Database date format issues detected.\n\n" +
            "Would you like to reset the database to fix these issues?",
            "Database Repair",
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            DatabaseResetUtility.resetDatabase();
        }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please contact an administrator to fix database date format issues.",
                    "Database Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void repairDatabaseDates() {
        DatabaseRepairUtility.quickFix();
    }

    private void showUserManagement() {
        UserManagementDialog dialog = new UserManagementDialog(this, dataManager);
        dialog.setVisible(true);    
    }

    private void showSystemSettings() {
        SystemSettingsDialog dialog = new SystemSettingsDialog(this);
        dialog.setVisible(true);
    }
    
    private void showSystemReports() {
        ReportsDialog dialog = new ReportsDialog(this, dataManager);
        dialog.setVisible(true);
    }

    private void refreshPatientsTable() {
        try{
            PatientDAO patientDAO = new PatientDAO();
            List<Patient> patients = patientDAO.getAllPatients();
            
            DefaultTableModel model = (DefaultTableModel) patientsTable.getModel();
            model.setRowCount(0);
            
            for(Patient patient : patients){
                model.addRow(new Object []{
                    patient.getId(),
                    patient.getName(),
                    patient.getGender(),
                    patient.getContactInfo(),
                    
                });
            }
            patientsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } catch (Exception e){
            JOptionPane.showMessageDialog(this, "Error refreshing patient data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteMedicalRecord(int recordId, Patient patient, DefaultTableModel recordsModel) {
    // Check user permissions
        if (currentUser.getRole() == User.UserRole.PATIENT) {
            JOptionPane.showMessageDialog(this,
                "Patients cannot delete medical records.",
                "Permission Denied",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentUser.getRole() == User.UserRole.NURSE) {
            JOptionPane.showMessageDialog(this,
                "Nurses cannot delete medical records. Please contact a doctor or administrator.",
                "Permission Denied",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get record details for better confirmation
        MedicalRecord record = dataManager.getMedicalRecordById(recordId);
        if (record == null) {
            JOptionPane.showMessageDialog(this,
                "Medical record not found! It may have already been deleted.",
                "Record Not Found",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder confirmMessage = new StringBuilder();
        confirmMessage.append("<html><b>⚠️ DELETE MEDICAL RECORD</b><br><br>");
        confirmMessage.append("<b>Record Details:</b><br>");
        confirmMessage.append("• ID: ").append(record.getId()).append("<br>");

        if (record.getRecordDate() != null) {
            confirmMessage.append("• Date: ").append(record.getRecordDate()).append("<br>");
        }

        if (record.getDiagnosis() != null && !record.getDiagnosis().isEmpty()) {
            String diagnosis = record.getDiagnosis();
            if (diagnosis.length() > 60) {
                diagnosis = diagnosis.substring(0, 57) + "...";
            }
            confirmMessage.append("• Diagnosis: ").append(diagnosis).append("<br>");
        }

        if (patient != null) {
            confirmMessage.append("• Patient: ").append(patient.getName()).append(" (ID: ").append(patient.getId()).append(")<br>");
        }

        confirmMessage.append("<br><font color='red' size='+1'><b>🚨 WARNING: This action is irreversible!</b></font><br><br>");
        confirmMessage.append("Are you absolutely sure you want to delete this medical record?</html>");

        int confirm = JOptionPane.showConfirmDialog(this,
            confirmMessage.toString(),
            "Confirm Medical Record Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Final confirmation for critical operation
            String finalConfirm = JOptionPane.showInputDialog(this,
                "<html><b>Final Confirmation Required</b><br><br>" +
                "Type 'DELETE' to confirm medical record deletion:<br>" +
                "<small>(This is case-sensitive)</small></html>",
                "Final Confirmation",
                JOptionPane.WARNING_MESSAGE);

            if ("DELETE".equals(finalConfirm)) {
                try {
                    boolean success = dataManager.deleteMedicalRecord(recordId);

                    if (success) {
                        JOptionPane.showMessageDialog(this,
                            "<html><b>✅ Medical Record Deleted</b><br><br>" +
                            "The medical record has been permanently deleted from the system.</html>",
                            "Deletion Successful",
                            JOptionPane.INFORMATION_MESSAGE);

                        // Refresh the records table
                        if (patient != null && recordsModel != null) {
                            loadPatientMedicalRecords(patient.getId(), recordsModel);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to delete medical record. Please try again or contact system administrator.",
                            "Deletion Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting medical record:\n" + e.getMessage(),
                        "Deletion Error",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            } else if (finalConfirm != null) {
                JOptionPane.showMessageDialog(this,
                    "Deletion cancelled. The confirmation text did not match 'DELETE'.",
                    "Deletion Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    public void refreshThemeColors(){
        ThemeManager.applyThemeColors(getContentPane());
        
        // Also update colors in all tabs
        if (tabbedPane != null) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component tab = tabbedPane.getComponentAt(i);
                if (tab instanceof Container) {
                    ThemeManager.applyThemeColors((Container) tab);
                }
            }
        }    
    }
    
    /**
     * Call this when theme changes from SystemSettingsDialog
     */
    public void onThemeChanged() {
        refreshThemeColors();
        if (tabbedPane != null) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component tab = tabbedPane.getComponentAt(i);
                if (tab instanceof Container) {
                    ThemeManager.applyThemeColors((Container) tab);
                }
            }
        }
    }

    void onThemeAndFontChanged() {
        refreshThemeColors();
        
        // Manually update fonts for all tab content
        updateAllTabFonts();
        
        // Force UI update
        revalidate();
        repaint();
    }

    private void updateAllTabFonts() {
        if (tabbedPane != null) {
            // Update the tabbed pane itself
            ThemeManager.updateComponentFontsRecursive(tabbedPane);
            
            // Update each tab's content
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component tabComponent = tabbedPane.getComponentAt(i);
                if (tabComponent != null) {
                    ThemeManager.updateComponentFontsRecursive(tabComponent);
                }
            }
        }
        
        // Update all other components in the frame
        ThemeManager.updateComponentFontsRecursive(getContentPane());
    }
} 