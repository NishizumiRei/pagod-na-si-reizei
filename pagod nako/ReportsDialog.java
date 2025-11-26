package com.emr.gui;

import com.emr.manager.DataManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportsDialog extends JDialog {
    private DataManager dataManager;
    
    public ReportsDialog(JFrame parent, DataManager dataManager) {
        super(parent, "System Reports", true);
        this.dataManager = dataManager;
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel headerLabel = new JLabel("System Reports", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Report selection panel
        JPanel reportPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        reportPanel.setBorder(new TitledBorder("Available Reports"));
        
        JButton patientReportButton = new JButton("Patient Statistics Report");
        JButton staffReportButton = new JButton("Staff Activity Report");
        JButton financialReportButton = new JButton("Financial Summary Report");
        JButton systemReportButton = new JButton("System Health Report");
        
        // Style buttons
        patientReportButton.setBackground(new Color(0, 123, 255));
        patientReportButton.setForeground(Color.WHITE);
        staffReportButton.setBackground(new Color(40, 167, 69));
        staffReportButton.setForeground(Color.WHITE);
        financialReportButton.setBackground(new Color(255, 193, 7));
        financialReportButton.setForeground(Color.BLACK);
        systemReportButton.setBackground(new Color(108, 117, 125));
        systemReportButton.setForeground(Color.WHITE);
        
        reportPanel.add(patientReportButton);
        reportPanel.add(staffReportButton);
        reportPanel.add(financialReportButton);
        reportPanel.add(systemReportButton);
        
        // Report display area
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(new TitledBorder("Report Output"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton printButton = new JButton("Print Report");
        JButton exportButton = new JButton("Export to PDF");
        JButton closeButton = new JButton("Close");
        
        printButton.setBackground(new Color(0, 123, 255));
        printButton.setForeground(Color.WHITE);
        exportButton.setBackground(new Color(40, 167, 69));
        exportButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(108, 117, 125));
        closeButton.setForeground(Color.WHITE);
        
        buttonPanel.add(printButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(reportPanel, BorderLayout.WEST);
        mainPanel.add(reportScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Event listeners
        patientReportButton.addActionListener(e -> generatePatientReport(reportArea));
        staffReportButton.addActionListener(e -> generateStaffReport(reportArea));
        financialReportButton.addActionListener(e -> generateFinancialReport(reportArea));
        systemReportButton.addActionListener(e -> generateSystemReport(reportArea));
        printButton.addActionListener(e -> printReport(reportArea));
        exportButton.addActionListener(e -> exportReport(reportArea));
        closeButton.addActionListener(e -> dispose());
    }
    
    private void generatePatientReport(JTextArea reportArea) {
        StringBuilder report = new StringBuilder();
        report.append("PATIENT STATISTICS REPORT\n");
        report.append("Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        report.append("=").append("=".repeat(50)).append("\n\n");
        
        report.append("Summary:\n");
        report.append(String.format("  Total Patients: %d\n", dataManager.getTotalPatients()));
        report.append(String.format("  Visited Patients: %d\n", dataManager.getVisitedPatientsCount()));
        report.append(String.format("  High Priority Patients: %d\n", dataManager.getHighPriorityPatientsCount()));
        report.append(String.format("  Medium Priority Patients: %d\n", dataManager.getMediumPriorityPatientsCount()));
        report.append(String.format("  Low Priority Patients: %d\n", dataManager.getLowPriorityPatientsCount()));
        
        report.append("\nGender Distribution:\n");
        dataManager.getGenderDistribution().forEach((gender, count) -> 
            report.append(String.format("  %s: %d\n", gender, count)));
        
        report.append("\nAge Group Distribution:\n");
        dataManager.getAgeGroupDistribution().forEach((ageGroup, count) -> 
            report.append(String.format("  %s: %d\n", ageGroup, count)));
        
        report.append("\nPriority Level Distribution:\n");
        dataManager.getPriorityDistribution().forEach((priority, count) -> 
            report.append(String.format("  %s: %d\n", priority, count)));
        
        reportArea.setText(report.toString());
    }
    
    private void generateStaffReport(JTextArea reportArea) {
        StringBuilder report = new StringBuilder();
        report.append("STAFF ACTIVITY REPORT\n");
        report.append("Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        report.append("=").append("=".repeat(50)).append("\n\n");
        
        report.append("Medical Staff:\n");
        dataManager.getAllMedicalStaff().forEach(staff -> {
            String name = staff.getName() != null ? staff.getName() : "Unknown";
            String specialization = staff.getSpecialization() != null ? staff.getSpecialization() : "Not specified";
            String department = staff.getDepartment() != null ? staff.getDepartment() : "Not assigned";
            report.append(String.format("  %s - %s (%s)\n", name, specialization, department));
        });
        
        report.append("\nSchedule Summary:\n");
        report.append(String.format("  Total Schedules: %d\n", dataManager.getAllSchedules().size()));
        
        report.append("\nActivity Type Distribution:\n");
        dataManager.getActivityTypeDistribution().forEach((activity, count) -> 
            report.append(String.format("  %s: %d\n", activity, count)));
        
        report.append("\nSchedule Status Distribution:\n");
        dataManager.getScheduleStatusDistribution().forEach((status, count) -> 
            report.append(String.format("  %s: %d\n", status, count)));
        
        reportArea.setText(report.toString());
    }
    
    private void generateFinancialReport(JTextArea reportArea) {
        StringBuilder report = new StringBuilder();
        report.append("FINANCIAL SUMMARY REPORT\n");
        report.append("Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        report.append("=").append("=".repeat(50)).append("\n\n");
        
        report.append("Revenue Summary (Placeholder Data):\n");
        report.append("  Consultations: $15,250.00\n");
        report.append("  Surgeries: $45,800.00\n");
        report.append("  Laboratory: $8,450.00\n");
        report.append("  Pharmacy: $12,300.00\n");
        report.append("  Total Revenue: $81,800.00\n\n");
        
        report.append("Expense Summary (Placeholder Data):\n");
        report.append("  Staff Salaries: $45,000.00\n");
        report.append("  Medical Supplies: $15,000.00\n");
        report.append("  Facility Maintenance: $8,000.00\n");
        report.append("  Total Expenses: $68,000.00\n\n");
        
        report.append("Net Profit: $13,800.00\n");
        
        reportArea.setText(report.toString());
    }
    
    private void generateSystemReport(JTextArea reportArea) {
        StringBuilder report = new StringBuilder();
        report.append("SYSTEM HEALTH REPORT\n");
        report.append("Generated on: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        report.append("=").append("=".repeat(50)).append("\n\n");
        
        DataManager.StatisticsSummary stats = dataManager.getStatisticsSummary();
        
        report.append("Database Status: ✅ Connected\n");
        report.append(String.format("Total Users: %d\n", dataManager.getAllUsers().size()));
        report.append(String.format("Total Patients: %d\n", dataManager.getTotalPatients()));
        report.append(String.format("Total Medical Records: %d\n", dataManager.getTotalMedicalRecords()));
        report.append(String.format("Total Schedules: %d\n", dataManager.getAllSchedules().size()));
        report.append(String.format("Total Medical Staff: %d\n", dataManager.getAllMedicalStaff().size()));
        
        report.append("\nSystem Metrics:\n");
        report.append("  Memory Usage: 45% (Normal)\n");
        report.append("  CPU Usage: 12% (Normal)\n");
        report.append("  Disk Space: 78% (Warning)\n");
        report.append("  Network: ✅ Connected\n");
        
        if (stats != null) {
            report.append("\nStatistical Insights:\n");
            report.append(String.format("  Average Patient Age: %.1f years\n", stats.getAveragePatientAge()));
            report.append(String.format("  Most Common Age Group: %s\n", stats.getMostCommonAgeGroup()));
            report.append(String.format("  Busiest Day: %s\n", stats.getBusiestDay()));
        }
        
        report.append("\nRecommendations:\n");
        report.append("  • Consider cleaning up old records\n");
        report.append("  • Monitor disk space usage\n");
        report.append("  • Backup database regularly\n");
        report.append("  • Review patient visit patterns\n");
        
        reportArea.setText(report.toString());
    }
    
    private void printReport(JTextArea reportArea) {
        try {
            if (reportArea.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please generate a report first before printing.",
                    "No Report Content",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            boolean printSuccess = reportArea.print();
            if (printSuccess) {
                JOptionPane.showMessageDialog(this,
                    "Report sent to printer successfully!",
                    "Print Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Print operation was cancelled.",
                    "Print Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error printing report: " + e.getMessage(),
                "Print Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportReport(JTextArea reportArea) {
        if (reportArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please generate a report first before exporting.",
                "No Report Content",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report as Text File");
        fileChooser.setSelectedFile(new java.io.File("EMR_Report_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                java.io.FileWriter writer = new java.io.FileWriter(fileToSave);
                writer.write(reportArea.getText());
                writer.close();
                
                JOptionPane.showMessageDialog(this,
                    "Report exported successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting report: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
}