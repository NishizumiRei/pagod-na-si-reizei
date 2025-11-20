package com.emr;

import com.emr.gui.LoginDialog;
import javax.swing.*;

public class EMR {
    public static void main(String[] args) {
        
        // Run on EDT
        SwingUtilities.invokeLater(() -> {
            // LoginDialog is a JDialog, so we don't need to convert it to JFrame
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.setVisible(true);
        });
    }
}