package com.emr.gui;

import com.emr.manager.DataManager;
import com.emr.models.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.List;

public class StaffScheduleDialog extends JDialog {
    private DataManager dataManager;
    private StaffSchedule schedule;
    private boolean success = false;
    
    private JComboBox<MedicalStaff> staffComboBox;
    private JDateChooser dateChooser;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    private JComboBox<String> activityTypeComboBox;
    private JComboBox<Patient> patientComboBox;
    private JTextArea descriptionArea;
    private JComboBox<String> statusComboBox;
    
    public StaffScheduleDialog(JFrame parent, DataManager dataManager, StaffSchedule schedule) {
        super(parent, schedule == null ? "Add New Schedule" : "Edit Schedule", true);
        this.dataManager = dataManager;
        this.schedule = schedule;
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel headerLabel = new JLabel(schedule == null ? "Add New Schedule" : "Edit Schedule", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Staff Selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Medical Staff:*"), gbc);
        gbc.gridx = 1;
        staffComboBox = new JComboBox<>();
        loadMedicalStaff();
        formPanel.add(staffComboBox, gbc);
        
        // Date
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Date:*"), gbc);
        gbc.gridx = 1;
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        formPanel.add(dateChooser, gbc);
        
        // Start Time
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Start Time:*"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel startModel = new SpinnerDateModel();
        startTimeSpinner = new JSpinner(startModel);
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
        startTimeSpinner.setEditor(startEditor);
        formPanel.add(startTimeSpinner, gbc);
        
        // End Time
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("End Time:*"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel endModel = new SpinnerDateModel();
        endTimeSpinner = new JSpinner(endModel);
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
        endTimeSpinner.setEditor(endEditor);
        formPanel.add(endTimeSpinner, gbc);
        
        // Activity Type
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Activity Type:*"), gbc);
        gbc.gridx = 1;
        activityTypeComboBox = new JComboBox<>(new String[]{
            "CONSULTATION", "SURGERY", "ROUNDS", "BREAK", "EMERGENCY"
        });
        formPanel.add(activityTypeComboBox, gbc);
        
        // Patient Selection
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Patient (Optional):"), gbc);
        gbc.gridx = 1;
        patientComboBox = new JComboBox<>();
        loadPatients();
        formPanel.add(patientComboBox, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Status:*"), gbc);
        gbc.gridx = 1;
        statusComboBox = new JComboBox<>(new String[]{
            "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
        });
        formPanel.add(statusComboBox, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        formPanel.add(descriptionScroll, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton(schedule == null ? "Create Schedule" : "Update Schedule");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(140, 35));
        
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Load data if editing
        if (schedule != null) {
            loadScheduleData();
        } else {
            // Set default values for new schedule
            dateChooser.setDate(new java.util.Date());
            statusComboBox.setSelectedItem("SCHEDULED");
            
            // Set default times
            try {
                java.util.Date defaultStart = new SimpleDateFormat("HH:mm").parse("09:00");
                java.util.Date defaultEnd = new SimpleDateFormat("HH:mm").parse("10:00");
                startTimeSpinner.setValue(defaultStart);
                endTimeSpinner.setValue(defaultEnd);
            } catch (Exception e) {
                // Use current time as fallback
                startTimeSpinner.setValue(new java.util.Date());
                endTimeSpinner.setValue(new java.util.Date(System.currentTimeMillis() + 3600000)); // +1 hour
            }
        }
        
        // Event listeners
        saveButton.addActionListener(e -> saveSchedule());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void loadMedicalStaff() {
        List<MedicalStaff> staffList = dataManager.getAllMedicalStaff();
        DefaultComboBoxModel<MedicalStaff> model = new DefaultComboBoxModel<>();
        
        for (MedicalStaff staff : staffList) {
            model.addElement(staff);
        }
        
        staffComboBox.setModel(model);
        staffComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MedicalStaff) {
                    MedicalStaff staff = (MedicalStaff) value;
                    setText(staff.getName() + " - " + staff.getSpecialization() + " (" + staff.getDepartment() + ")");
                }
                return this;
            }
        });
        
        // Select first staff member by default
        if (model.getSize() > 0) {
            staffComboBox.setSelectedIndex(0);
        }
    }
    
    private void loadPatients() {
        List<Patient> patients = dataManager.getAllPatients();
        DefaultComboBoxModel<Patient> model = new DefaultComboBoxModel<>();
        model.addElement(null); // Add empty option
        
        for (Patient patient : patients) {
            model.addElement(patient);
        }
        
        patientComboBox.setModel(model);
        patientComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Patient) {
                    Patient patient = (Patient) value;
                    setText(patient.getName() + " (ID: " + patient.getId() + ", Age: " + patient.getAge() + ")");
                } else if (value == null) {
                    setText("-- No patient selected --");
                }
                return this;
            }
        });
    }
    
    private void loadScheduleData() {
        if (schedule != null) {
            // Find and select the staff member
            List<MedicalStaff> staffList = dataManager.getAllMedicalStaff();
            for (MedicalStaff staff : staffList) {
                if (staff.getId() == schedule.getStaffId()) {
                    staffComboBox.setSelectedItem(staff);
                    break;
                }
            }
            
            // Set date
            if (schedule.getScheduleDate() != null) {
                dateChooser.setDate(new java.util.Date(schedule.getScheduleDate().getTime()));
            }
            
            // Set times
            if (schedule.getStartTime() != null) {
                java.util.Date startDate = new java.util.Date(schedule.getStartTime().getTime());
                startTimeSpinner.setValue(startDate);
            }
            
            if (schedule.getEndTime() != null) {
                java.util.Date endDate = new java.util.Date(schedule.getEndTime().getTime());
                endTimeSpinner.setValue(endDate);
            }
            
            // Set activity type
            activityTypeComboBox.setSelectedItem(schedule.getActivityType().name());
            
            // Find and select patient
            if (schedule.getPatientId() != null) {
                List<Patient> patients = dataManager.getAllPatients();
                for (Patient patient : patients) {
                    if (patient.getId() == schedule.getPatientId()) {
                        patientComboBox.setSelectedItem(patient);
                        break;
                    }
                }
            }
            
            // Set status and description
            statusComboBox.setSelectedItem(schedule.getStatus().name());
            descriptionArea.setText(schedule.getDescription() != null ? schedule.getDescription() : "");
        }
    }
    
    private void saveSchedule() {
        try {
            // Validation
            if (staffComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select medical staff.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (dateChooser.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Please select a date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            MedicalStaff selectedStaff = (MedicalStaff) staffComboBox.getSelectedItem();
            java.util.Date startTime = (java.util.Date) startTimeSpinner.getValue();
            java.util.Date endTime = (java.util.Date) endTimeSpinner.getValue();
            
            if (endTime.before(startTime)) {
                JOptionPane.showMessageDialog(this, "End time cannot be before start time.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Check for time conflicts (basic check)
            if (hasScheduleConflict(selectedStaff.getId(), dateChooser.getDate(), startTime, endTime)) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "This staff member already has schedules during this time.\n" +
                    "Do you want to proceed anyway?",
                    "Schedule Conflict",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Create or update schedule
            StaffSchedule newSchedule = new StaffSchedule();
            newSchedule.setStaffId(selectedStaff.getId());
            newSchedule.setScheduleDate(new Date(dateChooser.getDate().getTime()));
            newSchedule.setStartTime(new Time(startTime.getTime()));
            newSchedule.setEndTime(new Time(endTime.getTime()));
            newSchedule.setActivityType(StaffSchedule.ActivityType.valueOf((String) activityTypeComboBox.getSelectedItem()));
            
            Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
            if (selectedPatient != null) {
                newSchedule.setPatientId(selectedPatient.getId());
            }
            
            newSchedule.setStatus(StaffSchedule.ScheduleStatus.valueOf((String) statusComboBox.getSelectedItem()));
            newSchedule.setDescription(descriptionArea.getText().trim());
            
            boolean success;
            String actionMessage;
            
            if (schedule == null) {
                success = dataManager.createSchedule(newSchedule);
                actionMessage = "created";
            } else {
                newSchedule.setId(schedule.getId());
                success = dataManager.updateSchedule(newSchedule);
                actionMessage = "updated";
            }
            
            if (success) {
                this.success = true;
                JOptionPane.showMessageDialog(this, 
                    "Schedule " + actionMessage + " successfully!\n\n" +
                    "Staff: " + selectedStaff.getName() + "\n" +
                    "Date: " + new SimpleDateFormat("yyyy-MM-dd").format(dateChooser.getDate()) + "\n" +
                    "Time: " + new SimpleDateFormat("HH:mm").format(startTime) + " - " + 
                    new SimpleDateFormat("HH:mm").format(endTime) + "\n" +
                    "Activity: " + activityTypeComboBox.getSelectedItem(),
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to save schedule. Please try again.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving schedule: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private boolean hasScheduleConflict(int staffId, java.util.Date date, java.util.Date startTime, java.util.Date endTime) {
        try {
            // Convert to SQL date
            Date sqlDate = new Date(date.getTime());
            
            // Get existing schedules for this staff member on this date
            List<StaffSchedule> existingSchedules = dataManager.getSchedulesByDate(sqlDate);
            
            for (StaffSchedule existing : existingSchedules) {
                // Skip the current schedule if we're editing
                if (schedule != null && existing.getId() == schedule.getId()) {
                    continue;
                }
                
                // Check for time overlap
                java.util.Date existingStart = new java.util.Date(existing.getStartTime().getTime());
                java.util.Date existingEnd = new java.util.Date(existing.getEndTime().getTime());
                
                if (staffId == existing.getStaffId() && 
                    startTime.before(existingEnd) && 
                    endTime.after(existingStart)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking schedule conflicts: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean isSuccess() {
        return success;
    }
}

// JDateChooser Implementation
class JDateChooser extends JPanel {
    private JSpinner dateSpinner;
    
    public JDateChooser() {
        setLayout(new BorderLayout());
        SpinnerDateModel model = new SpinnerDateModel();
        dateSpinner = new JSpinner(model);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        add(dateSpinner, BorderLayout.CENTER);
    }
    
    public java.util.Date getDate() {
        return (java.util.Date) dateSpinner.getValue();
    }
    
    public void setDate(java.util.Date date) {
        dateSpinner.setValue(date);
    }
    
    public void setDateFormatString(String format) {
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, format));
    }
}