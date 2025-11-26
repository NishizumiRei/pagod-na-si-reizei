// In gui/PatientEditDialog.java
package com.emr.gui;

import com.emr.dao.PatientDAO;
import com.emr.models.Patient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PatientEditDialog extends JDialog {
    private Patient patient;
    private boolean saved = false;
    
    private JTextField nameField;
    private JTextField dateOfBirthField;
    private JComboBox<String> genderComboBox;
    private JTextField contactInfoField;
    private JTextArea addressArea;
    private JTextField emergencyContactField;
    private JTextField insuranceField;
    
    public PatientEditDialog(Frame parent, Patient patient) {
        super(parent, "Edit Patient", true);
        this.patient = patient;
        initializeComponents();
        loadPatientData();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Date of Birth
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dateOfBirthField = new JTextField(20);
        formPanel.add(dateOfBirthField, gbc);
        
        // Gender
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        formPanel.add(genderComboBox, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        contactInfoField = new JTextField(20);
        formPanel.add(contactInfoField, gbc);
        
        
        // Address
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        formPanel.add(new JScrollPane(addressArea), gbc);
        
        // Emergency Contact
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Emergency Contact:"), gbc);
        gbc.gridx = 1;
        emergencyContactField = new JTextField(20);
        formPanel.add(emergencyContactField, gbc);
        
        // Insurance
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Insurance:"), gbc);
        gbc.gridx = 1;
        insuranceField = new JTextField(20);
        formPanel.add(insuranceField, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePatient();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadPatientData() {
        if (patient != null) {
            nameField.setText(patient.getName());
            genderComboBox.setSelectedItem(patient.getGender());
            addressArea.setText(patient.getAddress());
            emergencyContactField.setText(patient.getEmergencyContact());
            insuranceField.setText(patient.getInsuranceInfo());
        }
    }
    
    private void savePatient() {
        try {
            // Validate required fields
            if (nameField.getText().trim().isEmpty())
                    {
                JOptionPane.showMessageDialog(this, "First name and last name are required.");
                return;
            }
            
            // Update patient object
            patient.setName(nameField.getText().trim());
            patient.setGender((Patient.Gender) genderComboBox.getSelectedItem());
            patient.setContactInfo(contactInfoField.getText().trim());
            patient.setAddress(addressArea.getText().trim());
            patient.setEmergencyContact(emergencyContactField.getText().trim());
            patient.setInsuranceInfo(insuranceField.getText().trim());
            
            // Save to database
            PatientDAO patientDAO = new PatientDAO();
            boolean success = patientDAO.updatePatient(patient);
            
            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Patient updated successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update patient.");
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving patient: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
}