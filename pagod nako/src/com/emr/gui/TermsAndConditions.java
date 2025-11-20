package com.emr.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class TermsAndConditions extends JDialog {
    private boolean agreed = false;
    private JButton agreeButton;
    private JButton disagreeButton;
    
    public TermsAndConditions(JDialog parent) {
        super(parent, "Terms and Conditions", true);
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JLabel headerLabel = new JLabel("Terms and Conditions", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Terms text
        JTextArea termsText = new JTextArea();
        termsText.setText(getTermsContent());
        termsText.setEditable(false);
        termsText.setLineWrap(true);
        termsText.setWrapStyleWord(true);
        termsText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(termsText);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Please read carefully"));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        agreeButton = new JButton("I Agree");
        agreeButton.setBackground(new Color(40, 167, 69));
        agreeButton.setForeground(Color.WHITE);
        
        disagreeButton = new JButton("I Disagree");
        disagreeButton.setBackground(new Color(220, 53, 69));
        disagreeButton.setForeground(Color.WHITE);
        
        buttonPanel.add(disagreeButton);
        buttonPanel.add(agreeButton);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Event listeners
        agreeButton.addActionListener(e -> {
            agreed = true;
            dispose();
        });
        
        disagreeButton.addActionListener(e -> {
            agreed = false;
            dispose();
        });
    }
    
    private String getTermsContent() {
        return "ELECTRONIC MEDICAL RECORDS (EMR) SYSTEM - TERMS OF USE\n\n" +
               "1. ACCEPTANCE OF TERMS\n" +
               "By accessing and using this EMR System, you agree to be bound by these Terms and Conditions.\n\n" +
               "2. MEDICAL DISCLAIMER\n" +
               "This system is intended for healthcare professionals and authorized personnel only. \n" +
               "The information contained herein is for medical record-keeping purposes and should \n" +
               "not be used as a substitute for professional medical advice.\n\n" +
               "3. DATA PRIVACY AND CONFIDENTIALITY\n" +
               "- All patient data is strictly confidential\n" +
               "- You are obligated to maintain patient privacy under HIPAA regulations\n" +
               "- Unauthorized access or disclosure is prohibited\n" +
               "- All access is logged and monitored\n\n" +
               "4. USER RESPONSIBILITIES\n" +
               "- Maintain the security of your login credentials\n" +
               "- Report any security breaches immediately\n" +
               "- Use the system only for authorized purposes\n" +
               "- Ensure accurate data entry\n\n" +
               "5. SYSTEM USAGE\n" +
               "- Only authorized healthcare personnel may access patient records\n" +
               "- Data should be entered promptly and accurately\n" +
               "- System should not be used for personal purposes\n\n" +
               "6. COMPLIANCE\n" +
               "Users must comply with all applicable healthcare regulations including HIPAA, \n" +
               "and institutional policies regarding electronic medical records.\n\n" +
               "By clicking \"I Agree\", you acknowledge that you have read, understood, \n" +
               "and agree to be bound by these Terms and Conditions.";
    }
    
    public boolean isAgreed() {
        return agreed;
    }
}