package com.emr.config;

import com.emr.models.User;
import javax.swing.*;
import java.awt.*;
import static java.lang.reflect.Array.get;
import static java.nio.file.Paths.get;
import java.util.prefs.Preferences;
import static javax.swing.UIManager.get;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;

public class ThemeManager {
    private static final String THEME_PREF = "app_theme";
    private static final String FONT_PREF = "app_font";
    private static final String FONT_SIZE_PREF = "app_font_size";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    
    public enum Theme {
        SYSTEM("System Default"),
        CROSS_PLATFORM("Java Cross-Platform"), 
        NIMBUS("Nimbus"),
        METAL("Metal"),
        WINDOWS("Windows"),
        FLAT_LIGHT("Flat Light"),
        FLAT_DARK("Flat Dark"),
        FLAT_DARCULA("Flat Darcula");
        
        private final String displayName;
        
        Theme(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public static void applyTheme(Theme theme) {
        try {
            switch (theme) {
                case SYSTEM:
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
                case CROSS_PLATFORM:
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    break;
                case NIMBUS:
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                    break;
                case METAL:
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    break;
                case WINDOWS:
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    break;
                case FLAT_LIGHT:
                    applyFlatLafTheme("com.formdev.flatlaf.FlatLightLaf");
                    break;
                case FLAT_DARK:
                    applyFlatLafTheme("com.formdev.flatlaf.FlatDarkLaf");
                    break;
                case FLAT_DARCULA:
                    applyFlatLafTheme("com.formdev.flatlaf.FlatDarculaLaf");
                    break;
                default:
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            saveThemePreference(theme);
            System.out.println("Applied theme: " + theme.getDisplayName());
        } catch (Exception e) {
            System.err.println("Failed to apply theme: " + theme.getDisplayName() + " - " + e.getMessage());
            applyFallbackTheme();
        }
    }
    
    private static void applyFlatLafTheme(String flatLafClassName) {
        try {
            // Try to load FlatLaf class dynamically
            Class<?> lafClass = Class.forName(flatLafClassName);
            LookAndFeel laf = (LookAndFeel) lafClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(laf);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("FlatLaf not found in classpath. Please add flatlaf-3.2.1.jar to your libraries.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize FlatLaf: " + e.getMessage(), e);
        }
    }
    
    private static void applyFallbackTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to apply fallback theme");
        }
    }
    
    public static Theme getCurrentTheme() {
        String themeName = prefs.get(THEME_PREF, Theme.SYSTEM.name());
        try {
            return Theme.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            return Theme.SYSTEM;
        }
    }
    
    private static void saveThemePreference(Theme theme) {
        prefs.put(THEME_PREF, theme.name());
    }
    
    public static void initializeApplicationTheme() {
        // Check if FlatLaf is available
        if (!isFlatLafAvailable()) {
            System.out.println("FlatLaf not available, using built-in themes only");
        }
        applyTheme(getCurrentTheme());
    }
    
    private static boolean isFlatLafAvailable() {
        try {
            Class.forName("com.formdev.flatlaf.FlatLightLaf");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    public static Theme[] getAvailableThemes() {
        if (isFlatLafAvailable()) {
            return Theme.values(); // All themes including FlatLaf
        } else {
            // Return only built-in themes
            return new Theme[]{
                Theme.SYSTEM,
                Theme.CROSS_PLATFORM, 
                Theme.NIMBUS,
                Theme.METAL,
                isWindows() ? Theme.WINDOWS : Theme.SYSTEM
            };
        }
    }
    
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    
    public static void updateAllUIComponents(Component component) {
        SwingUtilities.updateComponentTreeUI(component);
        if (component instanceof Window) {
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        }
    }
    
    public static void Optimization(){
        UIManager.put("Table.rowHeight", 28); // Better for medical data
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        
        // High contrast for critical medical information
        UIManager.put("Panel.background", new Color(248, 250, 252));
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.selectionBackground", new Color(0, 102, 204));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        
        // Emergency/priority color coding support
        UIManager.put("Medical.highPriority", new Color(220, 53, 69));
        UIManager.put("Medical.mediumPriority", new Color(255, 193, 7));
        UIManager.put("Medical.lowPriority", new Color(40, 167, 69)); 
    }
    public static void setThemeForMedicalRole(Theme theme, User.UserRole role) {
        applyTheme(theme);
        Optimization();
        
        // Role-specific optimizations
        switch (role) {
            case DOCTOR:
                // Optimize for clinical decision-making
                UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 12));
                break;
            case NURSE:
                // Optimize for quick data entry and monitoring
                UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
                break;
            case ADMIN:
                // Standard settings
                break;
            case PATIENT:
                // Larger fonts for patient portal
                UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
                break;
        }
    }
    
    public static Color getForegroundColor(){
        return UIManager.getColor("Label.foreground");
    }
    public static Color getBackgroundColor(){
        return UIManager.getColor("Panel.background");
    }
    public static Color getTextColor(){
        return UIManager.getColor("Label.foreground");
    }
    public static Color getBorderColor(){
        return UIManager.getColor("Component.borderColor");
    }
    
    public static void applyThemeColors(Container container) {
        applyThemeColorsRecursive(container);
    }
    
    private static void applyThemeColorsRecursive(Component component) {
        if (component instanceof JLabel) {
            component.setForeground(getForegroundColor());
        } else if (component instanceof JPanel) {
            component.setBackground(getBackgroundColor());
        } else if (component instanceof JTextArea || component instanceof JTextField) {
            component.setForeground(getForegroundColor());
            component.setBackground(getBackgroundColor());
        }
        
        // Recursively apply to children
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyThemeColorsRecursive(child);
            }
        }
    }
    
    /**
     * Creates a styled panel with theme-aware colors
     */
    public static JPanel createThemedPanel(String title, boolean useDefaultBackground) {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(getForegroundColor());
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(titleLabel, BorderLayout.NORTH);
        }
        
        if (useDefaultBackground) {
            panel.setBackground(getBackgroundColor());
        }
        
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(getBorderColor(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        return panel;
    }
    public enum FontFamily {
        SEGOE_UI("Segoe UI", "Modern Windows font"),
        SYSTEM("System", "Default system font"),
        SAN_SERIF("SansSerif", "Generic sans-serif"),
        SERIF("Serif", "Traditional serif font"),
        DIALOG("Dialog", "Standard dialog font"),
        MONOSPACED("Monospaced", "Fixed-width font"),
        ARIAL("Arial", "Universal sans-serif"),
        VERDANA("Verdana", "Highly readable sans-serif"),
        TAOMA("Tahoma", "Compact sans-serif"),
        HELVETICA("Helvetica", "Classic sans-serif");
        
        private final String fontName;
        private final String description;
        
        FontFamily(String fontName, String description) {
            this.fontName = fontName;
            this.description = description;
        }
        
        public String getFontName() {
            return fontName;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return fontName + " - " + description;
        }
    }
    public enum FontSize {
        SMALL("Small", 11),
        MEDIUM("Medium", 13),
        LARGE("Large", 15),
        EXTRA_LARGE("Extra Large", 17),
        CUSTOM("Custom", 13);
        
        private final String displayName;
        private final int size;
        
        FontSize(String displayName, int size) {
            this.displayName = displayName;
            this.size = size;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String toString() {
            return displayName + " (" + size + "pt)";
        }
    }
     public static void applyFontSettings(FontFamily fontFamily, FontSize fontSize) {
        try {
            Font baseFont = new Font(fontFamily.getFontName(), Font.PLAIN, fontSize.getSize());
            applyFontToUIManager(baseFont);
            saveFontPreference(fontFamily, fontSize);
            
            // Update all existing components
            updateAllComponentFonts();
            
            System.out.println("Applied font: " + fontFamily + ", Size: " + fontSize.getSize());
        } catch (Exception e) {
            System.err.println("Failed to apply font settings: " + e.getMessage());
            applyDefaultFont();
        }
    }
    
    /**
     * Applies custom font size
     */
    public static void applyCustomFontSize(int customSize) {
        try {
            FontFamily currentFamily = getCurrentFontFamily();
            Font baseFont = new Font(currentFamily.getFontName(), Font.PLAIN, customSize);
            applyFontToUIManager(baseFont);
            prefs.putInt(FONT_SIZE_PREF, customSize);
            
            // Update all existing components
            updateAllComponentFonts();
            
            System.out.println("Applied custom font size: " + customSize);
        } catch (Exception e) {
            System.err.println("Failed to apply custom font size: " + e.getMessage());
        }
    }
    
    private static void applyFontToUIManager(Font baseFont) {
        // Apply to common UI components
        UIManager.put("Button.font", baseFont);
        UIManager.put("Label.font", baseFont);
        UIManager.put("TextField.font", baseFont);
        UIManager.put("TextArea.font", baseFont);
        UIManager.put("EditorPane.font", baseFont);
        UIManager.put("PasswordField.font", baseFont);
        UIManager.put("ComboBox.font", baseFont);
        UIManager.put("CheckBox.font", baseFont);
        UIManager.put("RadioButton.font", baseFont);
        UIManager.put("ToggleButton.font", baseFont);
        UIManager.put("Spinner.font", baseFont);
        UIManager.put("TabbedPane.font", baseFont);
        UIManager.put("TitledBorder.font", baseFont.deriveFont(Font.BOLD));
        UIManager.put("MenuBar.font", baseFont);
        UIManager.put("Menu.font", baseFont);
        UIManager.put("MenuItem.font", baseFont);
        UIManager.put("PopupMenu.font", baseFont);
        UIManager.put("ToolBar.font", baseFont);
        
        // Table fonts
        UIManager.put("Table.font", baseFont);
        UIManager.put("TableHeader.font", baseFont.deriveFont(Font.BOLD));
        
        // Tree fonts
        UIManager.put("Tree.font", baseFont);
        
        // List fonts
        UIManager.put("List.font", baseFont);
        
        // Bold fonts for headers and important text
        UIManager.put("defaultFont", baseFont);
        UIManager.put("Panel.font", baseFont);
    }
    
    private static void applyDefaultFont() {
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
        applyFontToUIManager(defaultFont);
    }
    
    /**
     * Gets current font family
     */
    public static FontFamily getCurrentFontFamily() {
        String fontName = prefs.get(FONT_PREF, FontFamily.SEGOE_UI.name());
        try {
            return FontFamily.valueOf(fontName);
        } catch (IllegalArgumentException e) {
            return FontFamily.SEGOE_UI;
        }
    }
    
    /**
     * Gets current font size
     */
    public static int getCurrentFontSize() {
        return prefs.getInt(FONT_SIZE_PREF, FontSize.MEDIUM.getSize());
    }
    
    public static FontSize getCurrentFontSizeEnum() {
        int currentSize = getCurrentFontSize();
        for (FontSize size : FontSize.values()) {
            if (size.getSize() == currentSize && size != FontSize.CUSTOM) {
                return size;
            }
        }
        return FontSize.CUSTOM;
    }
    
    private static void saveFontPreference(FontFamily fontFamily, FontSize fontSize) {
        prefs.put(FONT_PREF, fontFamily.name());
        prefs.putInt(FONT_SIZE_PREF, fontSize.getSize());
    }
    
    /**
     * Initializes application font settings
     */
    public static void initializeApplicationFont() {
        FontFamily fontFamily = getCurrentFontFamily();
        int fontSize = getCurrentFontSize();
        applyFontSettings(fontFamily, FontSize.CUSTOM); // Use custom to apply saved size
    }
    
    /**
     * Updates font for all UI components
     */
    public static void updateAllUIFonts(Component component) {
        SwingUtilities.updateComponentTreeUI(component);
        if (component instanceof Window) {
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        }
    }
    
    /**
     * Gets available font families for current system
     */
    public static FontFamily[] getAvailableFontFamilies() {
        return FontFamily.values();
    }
    
    /**
     * Gets available font sizes
     */
    public static FontSize[] getAvailableFontSizes() {
        return FontSize.values();
    }
    
    /**
     * Applies both theme and font settings
     */
    public static void initializeApplicationThemeAndFont() {
        initializeApplicationTheme();
        initializeApplicationFont();
    }
    
    public static void updateAllComponentFonts(){
        Window[] windows = Window.getWindows();
        for (Window window : windows){
            updateComponentFontsRecursive(window);
        }
    }
    
    public static void updateComponentFontsRecursive(Component component){
        if (component == null) return;
        
        try{
        
            if(component instanceof JLabel){
                JLabel label = (JLabel) component;
                Font currentFont = label.getFont();
                if(currentFont != null) {
                    Font newFont = deriveFontForComponent(currentFont, "Label.font");
                    label.setFont(newFont);
                }
            }
            else if (component instanceof JButton) {
                JButton button = (JButton) component;
                Font currentFont = button.getFont();
                if (currentFont != null) {
                    Font newFont = deriveFontForComponent(currentFont, "Button.font");
                }
            }
            else if (component instanceof JTextField) {
                    JTextField textField = (JTextField) component;
                    Font currentFont = textField.getFont();
                    if (currentFont != null) {
                        Font newFont = deriveFontForComponent(currentFont, "TextField.font");
                        textField.setFont(newFont);
                }
            }
            else if (component instanceof JTextArea) {
                JTextArea textArea = (JTextArea) component;
                Font currentFont = textArea.getFont();
                if (currentFont != null) {
                    Font newFont = deriveFontForComponent(currentFont, "TextArea.font");
                    textArea.setFont(newFont);
                }
            }
            else if (component instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) component;
                Font currentFont = combo.getFont();
                if (currentFont != null) {
                    Font newFont = deriveFontForComponent(currentFont, "ComboBox.font");
                    combo.setFont(newFont);
                }
            }
            else if (component instanceof JTable) {
                JTable table = (JTable) component;
                Font currentFont = table.getFont();
                if (currentFont != null) {
                    Font newFont = deriveFontForComponent(currentFont, "Table.font");
                    table.setFont(newFont);
                    table.setRowHeight(getCurrentFontSize() + 8); // Adjust row height
                }

                // Update table header font
                if (table.getTableHeader() != null) {
                    JTableHeader header = table.getTableHeader();
                    Font headerFont = header.getFont();
                    if (headerFont != null) {
                        Font newHeaderFont = deriveFontForComponent(headerFont, "TableHeader.font");
                        header.setFont(newHeaderFont);
                    }
                }
            }
            else if (component instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) component;
                Font currentFont = tabbedPane.getFont();
                if (currentFont != null) {
                    Font newFont = deriveFontForComponent(currentFont, "TabbedPane.font");
                    tabbedPane.setFont(newFont);
                }
            }
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                            updateComponentFontsRecursive(child);
                        }
                    }
        } catch (Exception e) {
            System.err.println("Error updating font for component: " + e.getMessage());
        }
    }
    private static Font deriveFontForComponent(Font currentFont, String uiManagerKey) {
        Font uiFont = UIManager.getFont(uiManagerKey);
        if (uiFont != null) {
            // Preserve font style (bold, italic) but use the new font family and size
            return uiFont.deriveFont(currentFont.getStyle());
        }
        return currentFont;
    }
    
}

