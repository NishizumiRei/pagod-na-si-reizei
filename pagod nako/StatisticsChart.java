package com.emr.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class StatisticsChart extends JPanel {
    private String title;
    private Map<?, Long> data;
    private Color[] colors = {
        new Color(65, 105, 225),   // Royal Blue
        new Color(220, 20, 60),    // Crimson
        new Color(50, 205, 50),    // Lime Green
        new Color(255, 165, 0),    // Orange
        new Color(148, 0, 211),    // Dark Violet
        new Color(255, 192, 203)   // Pink
    };
    
    public StatisticsChart(String title, Map<?, Long> data) {
        this.title = title;
        this.data = data;
        setPreferredSize(new Dimension(400, 300));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 25);
        
        if (data == null || data.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.drawString("No data available", width / 2 - 50, height / 2);
            return;
        }
        
        // Calculate total for percentages
        long total = data.values().stream().mapToLong(Long::longValue).sum();
        
        // Draw pie chart
        int centerX = width / 3;
        int centerY = height / 2 + 20;
        int radius = Math.min(width / 4, height / 3);
        
        int startAngle = 0;
        int colorIndex = 0;
        
        // Draw pie slices
        for (Map.Entry<?, Long> entry : data.entrySet()) {
            int arcAngle = (int) (360 * entry.getValue() / total);
            
            g2d.setColor(colors[colorIndex % colors.length]);
            g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, arcAngle);
            
            // Draw legend
            int legendX = width * 2 / 3;
            int legendY = 60 + colorIndex * 25;
            
            g2d.fillRect(legendX, legendY, 15, 15);
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String label = String.format("%s: %d (%.1f%%)", 
                entry.getKey().toString(), 
                entry.getValue(), 
                (entry.getValue() * 100.0 / total));
            g2d.drawString(label, legendX + 20, legendY + 12);
            
            startAngle += arcAngle;
            colorIndex++;
        }
        
        // Draw center circle for donut effect
        g2d.setColor(getBackground());
        g2d.fillOval(centerX - radius / 2, centerY - radius / 2, radius, radius);
    }
}