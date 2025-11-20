package com.emr.gui;

import com.emr.dao.PatientDAO;
import com.emr.database.DatabaseResetUtility;
import com.emr.database.DatabaseRepairUtility;
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
        setTitle("EMR System - Dashboard");
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
        JMenuItem exitItem = new JMenuItem("Exit");
        
        refreshItem.addActionListener(e -> refreshData());
        exitItem.addActionListener(e -> System.exit(0));
    
        fileMenu.add(refreshItem);
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
        welcomePanel.setBackground(Color.WHITE);

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
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel welcomeTitle = new JLabel("Electronic Medical Records System", JLabel.CENTER);
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeTitle.setForeground(new Color(0, 102, 204));

        String roleDisplay = currentUser.getRole().toString();
        String greeting = "Welcome, " + currentUser.getUsername() + "!";

        JLabel userGreeting = new JLabel("<html><center>" + greeting + "<br>" +
                                       "<small>Role: " + roleDisplay + "</small></center></html>");
        userGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userGreeting.setForeground(new Color(100, 100, 100));
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
        JPanel panel = createStyledPanel("Administrator Dashboard", new Color(248, 249, 250));
        panel.setLayout(new BorderLayout());

        JTextArea adminInfo = new JTextArea();
        adminInfo.setText("ADMINISTRATOR ACCESS\n\n" +
                         "• Full system access\n" +
                         "• User management\n" +
                         "• Database administration\n" +
                         "• System configuration\n" +
                         "• Audit logs access\n\n" +
                         "Use the Admin menu above to access all system features.");
        adminInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        adminInfo.setEditable(false);
        adminInfo.setBackground(panel.getBackground());

        // Remove the Admin Tools buttons since they're in the menu
        JLabel menuNote = new JLabel("💡 Use the 'Admin' menu in the menu bar above for all administrative functions.");
        menuNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        menuNote.setForeground(new Color(100, 100, 100));
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
        doctorInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        nurseInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        patientInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
                infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
        statsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
            patientSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        dashboardPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(0, 102, 204));
        
        JLabel titleLabel = new JLabel("EMR Statistics Overview", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        
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
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
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
        patientsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
        
        JLabel titleLabel = new JLabel("Medical Records - Select a patient from the Patient Records tab");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createSchedulesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Medical Staff Schedules");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
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
        schedulesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
                   textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
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
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
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
            // Implementation would use DatabaseResetUtility class
            JOptionPane.showMessageDialog(this, "Database reset would be implemented here.");
        }
    }
    
    private void fixDatabaseSchema() {
        // Implementation would use DatabaseSchemaFixer class
        JOptionPane.showMessageDialog(this, "Database schema fix would be implemented here.");
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
            Patient patient = patientDAO.getPatientByID(patientID);

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

    private PopupMenu createDefaultWelcomePanel() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void showAboutDialog() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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

}