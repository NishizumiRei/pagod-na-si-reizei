package com.emr.gui;

import com.emr.manager.DataManager;
import com.emr.models.Patient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class PatientInformationDialog extends JDialog {
    private DataManager dataManager;
    private boolean success = false;
    
    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderComboBox;
    private JTextField contactField;
    private JTextArea addressArea;
    private JTextField emergencyContactField;
    private JTextField insuranceField;
    private JSpinner prioritySpinner;
    private JCheckBox visitedCheckBox;
    
    public PatientInformationDialog(JFrame parent, DataManager dataManager) {
        super(parent, "Add New Patient", true);
        this.dataManager = dataManager;
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel headerLabel = new JLabel("Patient Information", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
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
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Age
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(5);
        formPanel.add(ageField, gbc);
        
        // Gender
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        genderComboBox = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        formPanel.add(genderComboBox, gbc);
        
        // Contact Info
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Contact Info:"), gbc);
        gbc.gridx = 1;
        contactField = new JTextField(20);
        formPanel.add(contactField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        formPanel.add(addressScroll, gbc);
        
        // Emergency Contact
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Emergency Contact:"), gbc);
        gbc.gridx = 1;
        emergencyContactField = new JTextField(20);
        formPanel.add(emergencyContactField, gbc);
        
        // Insurance Info
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Insurance Info:"), gbc);
        gbc.gridx = 1;
        insuranceField = new JTextField(20);
        formPanel.add(insuranceField, gbc);
        
        // Priority Level
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Priority Level:"), gbc);
        gbc.gridx = 1;
        SpinnerNumberModel priorityModel = new SpinnerNumberModel(1, 1, 5, 1);
        prioritySpinner = new JSpinner(priorityModel);
        formPanel.add(prioritySpinner, gbc);
        
        // Visited
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2;
        visitedCheckBox = new JCheckBox("Patient has visited");
        formPanel.add(visitedCheckBox, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Patient");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(120, 35));
        
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Event listeners
        saveButton.addActionListener(e -> savePatient());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void savePatient() {
        // Validation
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter patient name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Patient patient = new Patient();
            patient.setName(nameField.getText().trim());
            patient.setAge(Integer.parseInt(ageField.getText().trim()));
            patient.setGender(Patient.Gender.valueOf((String) genderComboBox.getSelectedItem()));
            patient.setContactInfo(contactField.getText().trim());
            patient.setAddress(addressArea.getText().trim());
            patient.setEmergencyContact(emergencyContactField.getText().trim());
            patient.setInsuranceInfo(insuranceField.getText().trim());
            patient.setPriorityLevel((Integer) prioritySpinner.getValue());
            patient.setVisited(visitedCheckBox.isSelected());
            
            if (dataManager.createPatient(patient)) {
                success = true;
                JOptionPane.showMessageDialog(this, "Patient saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid age.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public boolean isSuccess() {
        return success;
    }
}