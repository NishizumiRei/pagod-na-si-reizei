package com.emr.gui;

import com.emr.manager.DataManager;
import com.emr.models.MedicalRecord;
import com.emr.models.Patient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class MedicalRecordDialog extends JDialog {
    private DataManager dataManager;
    private JComboBox<Patient> patientComboBox;
    private JTextArea diagnosisArea;
    private JTextArea treatmentArea;
    private JTextArea medicationsArea;
    private JTextArea notesArea;
    private JTextField bloodPressureField;
    private JTextField heartRateField;
    private JTextField temperatureField;
    private JTextField heightField;
    private JTextField weightField;
    
    public MedicalRecordDialog(JFrame parent, DataManager dataManager) {
        super(parent, "Medical Record Entry", true);
        this.dataManager = dataManager;
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel headerLabel = new JLabel("Medical Record Entry", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Form panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Basic Info Tab
        tabbedPane.addTab("Basic Information", createBasicInfoPanel());
        
        // Vital Signs Tab
        tabbedPane.addTab("Vital Signs", createVitalSignsPanel());
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Record");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(120, 35));
        
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Event listeners
        saveButton.addActionListener(e -> saveMedicalRecord());
        cancelButton.addActionListener(e -> dispose());
        
        loadPatients();
    }
    
    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Patient Selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1;
        patientComboBox = new JComboBox<>();
        panel.add(patientComboBox, gbc);
        
        // Diagnosis
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 1;
        diagnosisArea = new JTextArea(4, 30);
        diagnosisArea.setLineWrap(true);
        diagnosisArea.setWrapStyleWord(true);
        JScrollPane diagnosisScroll = new JScrollPane(diagnosisArea);
        panel.add(diagnosisScroll, gbc);
        
        // Treatment
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Treatment:"), gbc);
        gbc.gridx = 1;
        treatmentArea = new JTextArea(4, 30);
        treatmentArea.setLineWrap(true);
        treatmentArea.setWrapStyleWord(true);
        JScrollPane treatmentScroll = new JScrollPane(treatmentArea);
        panel.add(treatmentScroll, gbc);
        
        // Medications
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Medications:"), gbc);
        gbc.gridx = 1;
        medicationsArea = new JTextArea(3, 30);
        medicationsArea.setLineWrap(true);
        medicationsArea.setWrapStyleWord(true);
        JScrollPane medicationsScroll = new JScrollPane(medicationsArea);
        panel.add(medicationsScroll, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        notesArea = new JTextArea(3, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        panel.add(notesScroll, gbc);
        
        return panel;
    }
    
    private JPanel createVitalSignsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Blood Pressure
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Blood Pressure:"), gbc);
        gbc.gridx = 1;
        bloodPressureField = new JTextField(10);
        panel.add(bloodPressureField, gbc);
        
        // Heart Rate
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Heart Rate:"), gbc);
        gbc.gridx = 1;
        heartRateField = new JTextField(10);
        panel.add(heartRateField, gbc);
        
        // Temperature
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Temperature (°C):"), gbc);
        gbc.gridx = 1;
        temperatureField = new JTextField(10);
        panel.add(temperatureField, gbc);
        
        // Height
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Height (cm):"), gbc);
        gbc.gridx = 1;
        heightField = new JTextField(10);
        panel.add(heightField, gbc);
        
        // Weight
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 1;
        weightField = new JTextField(10);
        panel.add(weightField, gbc);
        
        return panel;
    }
    
    private void loadPatients() {
        List<Patient> patients = dataManager.getAllPatients();
        DefaultComboBoxModel<Patient> model = new DefaultComboBoxModel<>();
        
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
                    setText(patient.getName() + " (ID: " + patient.getId() + ")");
                }
                return this;
            }
        });
    }
    
    
    
    
    private void saveMedicalRecord() {
        Patient selectedPatient = (Patient) patientComboBox.getSelectedItem();
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (diagnosisArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a diagnosis.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            MedicalRecord record = new MedicalRecord();
            record.setPatientId(selectedPatient.getId());
            record.setDoctorId(1); // This should be the current user's ID in a real implementation
            record.setDiagnosis(diagnosisArea.getText().trim());
            record.setTreatment(treatmentArea.getText().trim());
            record.setMedications(medicationsArea.getText().trim());
            record.setNotes(notesArea.getText().trim());
            record.setBloodPressure(bloodPressureField.getText().trim());
            
            // Parse numeric fields
            if (!heartRateField.getText().trim().isEmpty()) {
                record.setHeartRate(Integer.parseInt(heartRateField.getText().trim()));
            }
            if (!temperatureField.getText().trim().isEmpty()) {
                record.setTemperature(Double.parseDouble(temperatureField.getText().trim()));
            }
            if (!heightField.getText().trim().isEmpty()) {
                record.setHeight(Double.parseDouble(heightField.getText().trim()));
            }
            if (!weightField.getText().trim().isEmpty()) {
                record.setWeight(Double.parseDouble(weightField.getText().trim()));
            }
            
            if (dataManager.createMedicalRecord(record)) {
                JOptionPane.showMessageDialog(this, "Medical record saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Update patient's visited status
                selectedPatient.setVisited(true);
                selectedPatient.setLastVisitDate(new java.sql.Timestamp(System.currentTimeMillis()));
                dataManager.updatePatient(selectedPatient);
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save medical record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for vital signs.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}