package com.emr;

import com.emr.config.IconManager;
import com.emr.config.SettingsManager;
import com.emr.config.ThemeManager;
import com.emr.gui.LoginDialog;
import java.awt.Color;
import java.awt.Image;
import javax.swing.*;

public class EMR {
    static {
        // Initialize theme before any UI components are created
        ThemeManager.initializeApplicationThemeAndFont();
    }
    
    public static void main(String[] args) {
        // Set up exception handling
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            JOptionPane.showMessageDialog(null,
                "An unexpected error occurred: " + throwable.getMessage(),
                "Application Error",
                JOptionPane.ERROR_MESSAGE);
            throwable.printStackTrace();
        });
        
        // Run on EDT
        SwingUtilities.invokeLater(() -> {
            // Call setLookAndFeel before creating any UI
            setLookAndFeel();
            
            // LoginDialog is a JDialog, so we don't need to convert it to JFrame
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.setVisible(true);
        });
    }
    
    private static void setLookAndFeel(){
        // Remove the old implementation and replace with:
        ThemeManager.initializeApplicationThemeAndFont();
        setupGlobalUI();
    }
    
    private static void setupGlobalUI(){
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // UI manager tweaks for better appearance
        UIManager.put("TabbedPane.selected", new Color(0, 102, 204));
        UIManager.put("TabbedPane.selectHighlight", new Color(0, 102, 204));

        // Table improvements
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", true);
        UIManager.put("Table.gridColor", new Color(220, 220, 220));

        // Button improvements
        UIManager.put("Button.focus", new Color(0, 102, 204, 50));
    }
    
    private static void initializeIconSystem() {
        try {
            // Set application-wide icons for message dialogs
            setupDialogIcons();
            
            // Test icon system availability
            testIconSystem();
            
        } catch (Exception e) {
            System.err.println("Warning: Icon system initialization failed: " + e.getMessage());
            // Continue without icons - system should still function
        }
    }
    
    /**
     * Set up global icons for Swing dialogs and components
     */
    private static void setupDialogIcons() {
        try {
            // Set custom icons for option panes
            UIManager.put("OptionPane.informationIcon", IconManager.getIcon("status/info.png", 32, 32));
            UIManager.put("OptionPane.warningIcon", IconManager.getIcon("status/warning.png", 32, 32));
            UIManager.put("OptionPane.errorIcon", IconManager.getIcon("status/error.png", 32, 32));
            UIManager.put("OptionPane.questionIcon", IconManager.getIcon("status/info.png", 32, 32));
            
            // Set application icon for all frames (will be applied when frames are created)
            UIManager.put("Frame.iconImage", IconManager.getIcon("app_icon.png", 32, 32).getImage());
            
            // Set internal frame icon if using MDI
            UIManager.put("InternalFrame.iconImage", IconManager.getIcon("app_icon.png", 16, 16).getImage());
            
        } catch (Exception e) {
            System.err.println("Failed to set dialog icons: " + e.getMessage());
            // Use default icons if custom ones fail
        }
    }
    
    /**
     * Test that the icon system is working properly
     */
    private static void testIconSystem() {
        try {
            // Test loading a basic icon
            ImageIcon testIcon = IconManager.getIcon("status/info.png");
            if (testIcon == null || testIcon.getIconWidth() <= 0) {
                System.err.println("Warning: Icon system test failed - icons may not display properly");
            } else {
                System.out.println("Icon system initialized successfully");
            }
        } catch (Exception e) {
            System.err.println("Icon system test failed: " + e.getMessage());
        }
    }
    
    /**
     * Utility method to get the application icon for use in frames
     */
    public static Image getApplicationIcon() {
        try {
            return IconManager.getIcon("app_icon.png", 32, 32).getImage();
        } catch (Exception e) {
            // Return a default blank icon if custom icon fails
            System.err.println("Failed to load application icon: " + e.getMessage());
            return new ImageIcon().getImage();
        }
    }
    
    /**
     * Utility method to set frame icon for any JFrame in the application
     */
    public static void setFrameIcon(JFrame frame) {
        try {
            Image iconImage = getApplicationIcon();
            if (iconImage != null) {
                frame.setIconImage(iconImage);
            }
        } catch (Exception e) {
            System.err.println("Failed to set frame icon: " + e.getMessage());
        }
    }
    
    /**
     * Utility method to set dialog icon for any JDialog in the application
     */
    public static void setDialogIcon(JDialog dialog) {
        try {
            Image iconImage = getApplicationIcon();
            if (iconImage != null) {
                dialog.setIconImage(iconImage);
            }
        } catch (Exception e) {
            System.err.println("Failed to set dialog icon: " + e.getMessage());
        }
    }    
}