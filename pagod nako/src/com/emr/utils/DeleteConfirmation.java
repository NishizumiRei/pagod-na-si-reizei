package com.emr.utils;

import javax.swing.JOptionPane;

public class DeleteConfirmation {
    
    public static boolean confirmPatientDeletion(String patientName, int patientId) {
        int result = JOptionPane.showConfirmDialog(null,
            "<html><b>Delete Patient: " + patientName + "</b><br><br>" +
            "ID: " + patientId + "<br><br>" +
            "This will permanently remove:<br>" +
            "• Patient information<br>" +
            "• All medical records<br>" +
            "• Appointment history<br><br>" +
            "<font color='red'><b>This action cannot be undone!</b></font></html>",
            "Confirm Patient Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        return result == JOptionPane.YES_OPTION;
    }
    
    public static boolean confirmMedicalRecordDeletion(String diagnosis, String visitDate) {
        int result = JOptionPane.showConfirmDialog(null,
            "<html><b>Delete Medical Record</b><br><br>" +
            "Diagnosis: " + diagnosis + "<br>" +
            "Visit Date: " + visitDate + "<br><br>" +
            "<font color='red'>This action cannot be undone!</font></html>",
            "Confirm Record Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        return result == JOptionPane.YES_OPTION;
    }
}