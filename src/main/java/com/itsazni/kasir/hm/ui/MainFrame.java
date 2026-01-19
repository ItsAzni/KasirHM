package com.itsazni.kasir.hm.ui;

import com.itsazni.kasir.hm.models.User;
import javax.swing.*;
import java.awt.*;

import static com.itsazni.kasir.hm.ui.UIConstants.*;

/**
 * Main application frame with navigation sidebar - Modern design
 */
public class MainFrame extends JFrame {
    
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    
    // Panels
    private DashboardPanel dashboardPanel;
    private ProductManagementPanel productPanel;
    private POSPanel posPanel;
    private TransactionHistoryPanel historyPanel;
    
    // Navigation buttons
    private JButton btnDashboard;
    private JButton btnProducts;
    private JButton btnPOS;
    private JButton btnHistory;
    private JButton btnLogout;
    
    private String currentPanel = "dashboard";
    
    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Kasir Hm - Point of Sales");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setMinimumSize(new Dimension(1024, 600));
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Create sidebar
        createSidebar();
        add(sidebarPanel, BorderLayout.WEST);
        
        // Create main content area with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_COLOR);
        
        // Initialize panels
        dashboardPanel = new DashboardPanel(currentUser);
        productPanel = new ProductManagementPanel();
        posPanel = new POSPanel(currentUser);
        historyPanel = new TransactionHistoryPanel();
        
        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(productPanel, "products");
        mainPanel.add(posPanel, "pos");
        mainPanel.add(historyPanel, "history");
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Show dashboard by default
        showPanel("dashboard");
    }
    
    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, INPUT_BORDER));
        
        // Top padding
        sidebarPanel.add(Box.createVerticalStrut(24));
        
        // Logo/Title - use wrapper with FlowLayout for centering
        JPanel logoWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoWrapper.setBackground(SIDEBAR_BG);
        logoWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(SIDEBAR_BG);
        
        JLabel lblLogo = new JLabel("🛒", SwingConstants.CENTER);
        lblLogo.setFont(new Font("SansSerif", Font.PLAIN, 36));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(lblLogo);
        
        JLabel lblTitle = new JLabel("KASIR HM", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(lblTitle);
        
        JLabel lblSubtitle = new JLabel("Point of Sales", SwingConstants.CENTER);
        lblSubtitle.setFont(FONT_SMALL);
        lblSubtitle.setForeground(TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(lblSubtitle);
        
        logoWrapper.add(logoPanel);
        sidebarPanel.add(logoWrapper);
        sidebarPanel.add(Box.createVerticalStrut(28));
        
        // User info card
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBackground(SIDEBAR_HOVER);
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        userPanel.setPreferredSize(new Dimension(240, 80));
        userPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblUserIcon = new JLabel("👤 " + currentUser.getFullName());
        lblUserIcon.setFont(FONT_BODY_BOLD);
        lblUserIcon.setForeground(TEXT_COLOR);
        lblUserIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblRole = new JLabel(currentUser.getRole().name());
        lblRole.setFont(FONT_CAPTION);
        lblRole.setForeground(TEXT_SECONDARY);
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        userPanel.add(lblUserIcon);
        userPanel.add(Box.createVerticalStrut(6));
        userPanel.add(lblRole);
        sidebarPanel.add(userPanel);
        
        sidebarPanel.add(Box.createVerticalStrut(28));
        
        // Navigation section
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(SIDEBAR_BG);
        navPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        navPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblMenu = createLabel("MENU", FONT_CAPTION, TEXT_MUTED);
        lblMenu.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 0));
        navPanel.add(lblMenu);
        
        // Navigation buttons
        btnDashboard = createNavButton("Dashboard", "dashboard");
        btnPOS = createNavButton("Kasir (POS)", "pos");
        btnProducts = createNavButton("Produk", "products");
        btnHistory = createNavButton("Riwayat", "history");
        
        navPanel.add(btnDashboard);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnPOS);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnProducts);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnHistory);
        
        sidebarPanel.add(navPanel);
        
        // Spacer
        sidebarPanel.add(Box.createVerticalGlue());
        
        // Logout section
        JPanel logoutPanel = new JPanel();
        logoutPanel.setLayout(new BoxLayout(logoutPanel, BoxLayout.Y_AXIS));
        logoutPanel.setBackground(SIDEBAR_BG);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
        
        btnLogout = createNavButton("🚪  Logout", "logout");
        btnLogout.setBackground(new Color(60, 30, 35));
        btnLogout.setForeground(DANGER_COLOR);
        logoutPanel.add(btnLogout);
        
        sidebarPanel.add(logoutPanel);
    }
    
    private JButton createNavButton(String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        btn.setMaximumSize(new Dimension(208, 48));
        btn.setPreferredSize(new Dimension(208, 48));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setOpaque(true);
        
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!currentPanel.equals(panelName) && !panelName.equals("logout")) {
                    btn.setBackground(SIDEBAR_HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!currentPanel.equals(panelName) && !panelName.equals("logout")) {
                    btn.setBackground(SIDEBAR_BG);
                } else if (panelName.equals("logout")) {
                    btn.setBackground(new Color(60, 30, 35));
                }
            }
        });
        
        btn.addActionListener(e -> {
            if (panelName.equals("logout")) {
                logout();
            } else {
                showPanel(panelName);
            }
        });
        
        return btn;
    }
    
    private void showPanel(String panelName) {
        currentPanel = panelName;
        cardLayout.show(mainPanel, panelName);
        
        // Update active button styling
        resetButtonStyles();
        
        switch (panelName) {
            case "dashboard":
                setActiveButton(btnDashboard);
                dashboardPanel.refresh();
                break;
            case "products":
                setActiveButton(btnProducts);
                productPanel.refresh();
                break;
            case "pos":
                setActiveButton(btnPOS);
                break;
            case "history":
                setActiveButton(btnHistory);
                historyPanel.refresh();
                break;
        }
    }
    
    private void setActiveButton(JButton btn) {
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
    }
    
    private void resetButtonStyles() {
        btnDashboard.setBackground(SIDEBAR_BG);
        btnDashboard.setForeground(TEXT_COLOR);
        btnProducts.setBackground(SIDEBAR_BG);
        btnProducts.setForeground(TEXT_COLOR);
        btnPOS.setBackground(SIDEBAR_BG);
        btnPOS.setForeground(TEXT_COLOR);
        btnHistory.setBackground(SIDEBAR_BG);
        btnHistory.setForeground(TEXT_COLOR);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin logout?", 
                "Konfirmasi Logout", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginPanel loginPanel = new LoginPanel();
                loginPanel.setVisible(true);
            });
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
}
