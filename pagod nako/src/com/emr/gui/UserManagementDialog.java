package com.emr.gui;

import com.emr.manager.DataManager;
import com.emr.models.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UserManagementDialog extends JDialog {
    private DataManager dataManager;
    private JTable usersTable;
    private JButton refreshButton;
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton toggleActiveButton;
    
    public UserManagementDialog(JFrame parent, DataManager dataManager) {
        super(parent, "User Management", true);
        this.dataManager = dataManager;
        initializeUI();
        loadUsersData();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header
        JLabel headerLabel = new JLabel("User Management", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Table
        String[] columns = {"ID", "Username", "Role", "Email", "Active", "Created"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        usersTable = new JTable(model);
        usersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usersTable.setRowHeight(25);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(usersTable);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        addUserButton = new JButton("Add User");
        editUserButton = new JButton("Edit User");
        deleteUserButton = new JButton("Delete User");
        toggleActiveButton = new JButton("Toggle Active");
        refreshButton = new JButton("Refresh");
        
        // Style buttons
        addUserButton.setBackground(new Color(40, 167, 69));
        addUserButton.setForeground(Color.WHITE);
        editUserButton.setBackground(new Color(255, 193, 7));
        editUserButton.setForeground(Color.BLACK);
        deleteUserButton.setBackground(new Color(220, 53, 69));
        deleteUserButton.setForeground(Color.WHITE);
        toggleActiveButton.setBackground(new Color(108, 117, 125));
        toggleActiveButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(0, 123, 255));
        refreshButton.setForeground(Color.WHITE);
        
        buttonPanel.add(addUserButton);
        buttonPanel.add(editUserButton);
        buttonPanel.add(deleteUserButton);
        buttonPanel.add(toggleActiveButton);
        buttonPanel.add(refreshButton);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Event listeners
        addUserButton.addActionListener(e -> addUser());
        editUserButton.addActionListener(e -> editUser());
        deleteUserButton.addActionListener(e -> deleteUser());
        toggleActiveButton.addActionListener(e -> toggleActive());
        refreshButton.addActionListener(e -> loadUsersData());
    }
    
    private void loadUsersData() {
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        model.setRowCount(0);
        
        List<User> users = dataManager.getAllUsers();
        for (User user : users) {
            model.addRow(new Object[]{
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getEmail(),
                user.isActive() ? "Yes" : "No",
                user.getCreatedAt()
            });
        }
    }
    
    private void addUser() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"ADMIN", "DOCTOR", "NURSE", "PATIENT"});
        JTextField emailField = new JTextField();
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleComboBox);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();
            String email = emailField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password are required.");
                return;
            }
            
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(User.UserRole.valueOf(role));
            newUser.setEmail(email);
            newUser.setActive(true);
            
            if (dataManager.createUser(newUser)) {
                JOptionPane.showMessageDialog(this, "User created successfully!");
                loadUsersData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create user. Username may already exist.");
            }
        }
    }
    
    private void editUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }
        
        int userId = (Integer) usersTable.getValueAt(selectedRow, 0);
        String username = (String) usersTable.getValueAt(selectedRow, 1);
        
        // For now, show a simple edit dialog
        JTextField emailField = new JTextField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"ADMIN", "DOCTOR", "NURSE", "PATIENT"});
        JCheckBox activeCheckBox = new JCheckBox("Active");
        
        // Set current values
        String currentRole = (String) usersTable.getValueAt(selectedRow, 2);
        String currentEmail = (String) usersTable.getValueAt(selectedRow, 3);
        boolean currentActive = ((String) usersTable.getValueAt(selectedRow, 4)).equals("Yes");
        
        emailField.setText(currentEmail);
        roleComboBox.setSelectedItem(currentRole);
        activeCheckBox.setSelected(currentActive);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        panel.add(new JLabel(username)); // Username cannot be changed
        panel.add(new JLabel("Role:"));
        panel.add(roleComboBox);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Active:"));
        panel.add(activeCheckBox);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User: " + username, 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(this, 
                "User edit functionality would be implemented here.\n" +
                "User: " + username + "\n" +
                "New Role: " + roleComboBox.getSelectedItem() + "\n" +
                "New Email: " + emailField.getText(),
                "Edit User", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }
        
        int userId = (Integer) usersTable.getValueAt(selectedRow, 0);
        String username = (String) usersTable.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete user: " + username + "?\n\n" +
            "This action cannot be undone!",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "User deletion would be implemented here.\n" +
                "User: " + username + " would be deleted.",
                "Delete User",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void toggleActive() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }
        
        int userId = (Integer) usersTable.getValueAt(selectedRow, 0);
        String username = (String) usersTable.getValueAt(selectedRow, 1);
        boolean currentlyActive = ((String) usersTable.getValueAt(selectedRow, 4)).equals("Yes");
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to " + (currentlyActive ? "deactivate" : "activate") + " user: " + username + "?",
            "Confirm Status Change",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "User status would be changed here.\n" +
                "User: " + username + " would be " + (currentlyActive ? "deactivated" : "activated"),
                "Change User Status",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}