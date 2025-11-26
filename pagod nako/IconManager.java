// src/com/emr/config/IconManager.java
package com.emr.config;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IconManager {
    private static final Map<String, ImageIcon> iconCache = new HashMap<>();
    private static final String ICON_BASE_PATH = "images/icons/";
    private static final String PHOTO_BASE_PATH = "images/photos/";
    
    public static ImageIcon getIcon(String iconName) {
        return getIcon(iconName, 16, 16);
    }
    
    public static ImageIcon getIcon(String iconName, int width, int height) {
        String key = iconName + "_" + width + "x" + height;
        
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }
        
        try {
            String path = ICON_BASE_PATH + iconName;
            File iconFile = new File(path);
            
            if (!iconFile.exists()) {
                // Fallback to classpath resource
                java.net.URL imgURL = IconManager.class.getClassLoader().getResource(path);
                if (imgURL != null) {
                    ImageIcon originalIcon = new ImageIcon(imgURL);
                    Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    iconCache.put(key, scaledIcon);
                    return scaledIcon;
                }
                System.err.println("Icon not found: " + path);
                return createPlaceholderIcon(width, height);
            }
            
            ImageIcon originalIcon = new ImageIcon(path);
            Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            iconCache.put(key, scaledIcon);
            return scaledIcon;
            
        } catch (Exception e) {
            System.err.println("Error loading icon: " + iconName + " - " + e.getMessage());
            return createPlaceholderIcon(width, height);
        }
    }
    
    public static ImageIcon getPatientPhoto(String patientId) {
        // First try to find patient-specific photo
        String photoPath = PHOTO_BASE_PATH + "patients/" + patientId + ".png";
        File photoFile = new File(photoPath);
        
        if (!photoFile.exists()) {
            // Return default patient photo
            return getIcon("../photos/default_patient.png", 64, 64);
        }
        
        try {
            return new ImageIcon(photoPath);
        } catch (Exception e) {
            return getIcon("../photos/default_patient.png", 64, 64);
        }
    }
    
    private static ImageIcon createPlaceholderIcon(int width, int height) {
        // Create a simple colored placeholder
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(0, 0, width-1, height-1);
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    public static void setFrameIcon(JFrame frame) {
        ImageIcon appIcon = getIcon("app_icon.png", 32, 32);
        if (appIcon != null) {
            frame.setIconImage(appIcon.getImage());
        }
    }
    
    public static void setDialogIcon(JDialog dialog) {
        ImageIcon appIcon = getIcon("app_icon.png", 32, 32);
        if (appIcon != null) {
            dialog.setIconImage(appIcon.getImage());
        }
    }
}