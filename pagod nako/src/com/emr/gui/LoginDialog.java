package com.emr.gui;

import com.emr.manager.DataManager;
import com.emr.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signUpButton;
    private DataManager dataManager;
    
    public LoginDialog() {
        // Call JDialog constructor with no owner (standalone dialog)
        super((JFrame) null, "EMR System - Login", true);
        dataManager = new DataManager();
        initializeUI();
    }
    
    private void initializeUI() {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JLabel headerLabel = new JLabel("EMR System Login", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 102, 204));
        headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(100, 35));
        
        signUpButton = new JButton("Sign Up");
        signUpButton.setBackground(new Color(108, 117, 125));
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFocusPainted(false);
        signUpButton.setPreferredSize(new Dimension(100, 35));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        
        // Add components to main panel
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        setupEventListeners();
    }
    
    private void setupEventListeners() {
        loginButton.addActionListener(e -> performLogin());
        
        signUpButton.addActionListener(e -> {
            SignUpDialog signUpDialog = new SignUpDialog(this); // Pass 'this' JDialog as parent
            signUpDialog.setVisible(true);
        });
        
        // Enter key support
        passwordField.addActionListener(e -> performLogin());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password.", 
                "Login Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        User user = dataManager.authenticateUser(username, password);
        if (user != null) {
            dispose(); // Close login dialog
            MainFrame mainFrame = new MainFrame(user, dataManager);
            mainFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password.", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}