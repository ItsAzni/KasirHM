package com.itsazni.kasir.hm.ui;

import com.itsazni.kasir.hm.dao.UserDAO;
import com.itsazni.kasir.hm.models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static com.itsazni.kasir.hm.ui.UIConstants.*;

/**
 * Login panel for user authentication - Modern design
 */
public class LoginPanel extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblMessage;
    private JPanel cardPanelRef;
    
    private final UserDAO userDAO;
    
    public LoginPanel() {
        userDAO = new UserDAO();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Kasir Hm - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_COLOR, 0, getHeight(), BG_SECONDARY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        // Login card with rounded corners
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(INPUT_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));
        cardPanel.setPreferredSize(new Dimension(420, 540));
        cardPanelRef = cardPanel;
        
        // Logo
        JLabel lblLogo = new JLabel("🛒");
        lblLogo.setFont(new Font("SansSerif", Font.PLAIN, 60));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(lblLogo);
        
        cardPanel.add(Box.createVerticalStrut(12));
        
        // Title
        JLabel lblTitle = new JLabel("KASIR HM");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(lblTitle);
        
        JLabel lblSubtitle = new JLabel("Point of Sales System");
        lblSubtitle.setFont(FONT_SMALL);
        lblSubtitle.setForeground(TEXT_SECONDARY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(lblSubtitle);
        
        cardPanel.add(Box.createVerticalStrut(40));
        
        // Username field
        JLabel lblUsername = createLabel("Username", FONT_SMALL_BOLD, TEXT_COLOR);
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(lblUsername);
        
        cardPanel.add(Box.createVerticalStrut(10));
        
        txtUsername = createTextField("Masukkan username");
        txtUsername.setMaximumSize(new Dimension(320, 48));
        txtUsername.setPreferredSize(new Dimension(320, 48));
        txtUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(txtUsername);
        
        cardPanel.add(Box.createVerticalStrut(20));
        
        // Password field
        JLabel lblPassword = createLabel("Password", FONT_SMALL_BOLD, TEXT_COLOR);
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(lblPassword);
        
        cardPanel.add(Box.createVerticalStrut(10));
        
        txtPassword = createPasswordField("Masukkan password");
        txtPassword.setMaximumSize(new Dimension(320, 48));
        txtPassword.setPreferredSize(new Dimension(320, 48));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        cardPanel.add(txtPassword);
        
        cardPanel.add(Box.createVerticalStrut(12));
        
        // Error message label
        lblMessage = new JLabel(" ");
        lblMessage.setFont(FONT_SMALL);
        lblMessage.setForeground(DANGER_COLOR);
        lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(lblMessage);
        
        cardPanel.add(Box.createVerticalStrut(24));
        
        // Login button
        btnLogin = createPrimaryButton("Masuk");
        btnLogin.setMaximumSize(new Dimension(320, 52));
        btnLogin.setPreferredSize(new Dimension(320, 52));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setFont(FONT_BODY_BOLD);
        btnLogin.addActionListener(e -> performLogin());
        cardPanel.add(btnLogin);
        
        cardPanel.add(Box.createVerticalStrut(32));
        
        // Divider with text
        JPanel dividerPanel = new JPanel();
        dividerPanel.setLayout(new BoxLayout(dividerPanel, BoxLayout.X_AXIS));
        dividerPanel.setOpaque(false);
        dividerPanel.setMaximumSize(new Dimension(320, 20));
        dividerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JSeparator leftSep = new JSeparator();
        leftSep.setForeground(INPUT_BORDER);
        leftSep.setMaximumSize(new Dimension(120, 1));
        
        mainPanel.add(cardPanel);
        add(mainPanel);
    }
    
    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Username dan password harus diisi!");
            shakeComponent(cardPanelRef);
            return;
        }
        
        btnLogin.setEnabled(false);
        btnLogin.setText("Memuat...");
        lblMessage.setText(" ");
        
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return userDAO.authenticate(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            MainFrame mainFrame = new MainFrame(user);
                            mainFrame.setVisible(true);
                        });
                    } else {
                        lblMessage.setText("Username atau password salah!");
                        shakeComponent(cardPanelRef);
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Masuk");
                    }
                } catch (Exception e) {
                    lblMessage.setText("Error: " + e.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Masuk");
                }
            }
        };
        worker.execute();
    }
    
    private void shakeComponent(Component component) {
        Point point = component.getLocation();
        Timer timer = new Timer(50, null);
        final int[] count = {0};
        timer.addActionListener(e -> {
            if (count[0] >= 6) {
                timer.stop();
                component.setLocation(point);
                return;
            }
            int offset = (count[0] % 2 == 0) ? 5 : -5;
            component.setLocation(point.x + offset, point.y);
            count[0]++;
        });
        timer.start();
    }
}
