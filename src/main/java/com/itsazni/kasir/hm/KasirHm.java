package com.itsazni.kasir.hm;

import com.formdev.flatlaf.FlatDarkLaf;
import com.itsazni.kasir.hm.dao.DatabaseConnection;
import com.itsazni.kasir.hm.ui.LoginPanel;
import javax.swing.*;

/**
 * Main entry point for Kasir Hm POS Application
 * 
 * @author itsazni
 */
public class KasirHm {

    public static void main(String[] args) {
        // Set FlatLaf Dark Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            
            // Customize UI defaults
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
        }
        
        // Test database connection
        SwingUtilities.invokeLater(() -> {
            // Show splash/loading
            JDialog splash = createSplashScreen();
            splash.setVisible(true);
            
            // Test connection in background
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return DatabaseConnection.getInstance().testConnection();
                }
                
                @Override
                protected void done() {
                    splash.dispose();
                    try {
                        if (get()) {
                            // Show login panel
                            LoginPanel loginPanel = new LoginPanel();
                            loginPanel.setVisible(true);
                        } else {
                            // Show error dialog
                            JOptionPane.showMessageDialog(null,
                                    "Tidak dapat terhubung ke database!\n\n" +
                                    "Pastikan:\n" +
                                    "1. MySQL server sudah berjalan\n" +
                                    "2. Database 'kasir_hm' sudah dibuat\n" +
                                    "3. Jalankan script database_schema.sql\n\n" +
                                    "Aplikasi akan keluar.",
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE);
                            System.exit(1);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null,
                                "Error: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                }
            };
            worker.execute();
        });
    }
    
    private static JDialog createSplashScreen() {
        JDialog splash = new JDialog();
        splash.setUndecorated(true);
        splash.setSize(300, 150);
        splash.setLocationRelativeTo(null);
        splash.setModal(false);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new java.awt.Color(30, 30, 46));
        panel.setBorder(BorderFactory.createLineBorder(new java.awt.Color(99, 102, 241), 2));
        
        panel.add(Box.createVerticalGlue());
        
        JLabel lblTitle = new JLabel("KASIR HM");
        lblTitle.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        lblTitle.setForeground(new java.awt.Color(99, 102, 241));
        lblTitle.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(lblTitle);
        
        panel.add(Box.createVerticalStrut(10));
        
        JLabel lblLoading = new JLabel("Connecting to database...");
        lblLoading.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        lblLoading.setForeground(new java.awt.Color(166, 173, 200));
        lblLoading.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(lblLoading);
        
        panel.add(Box.createVerticalStrut(15));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new java.awt.Dimension(200, 5));
        progressBar.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(progressBar);
        
        panel.add(Box.createVerticalGlue());
        
        splash.add(panel);
        return splash;
    }
}
