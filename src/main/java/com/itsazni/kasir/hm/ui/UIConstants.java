package com.itsazni.kasir.hm.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * UI Constants and helper methods for consistent modern styling
 */
public class UIConstants {
    
    // ==================== COLORS ====================
    // Primary Colors
    public static final Color PRIMARY_COLOR = new Color(99, 102, 241);      // Indigo
    public static final Color PRIMARY_HOVER = new Color(79, 82, 221);       // Darker indigo
    public static final Color PRIMARY_LIGHT = new Color(129, 140, 248);     // Lighter indigo
    
    // Status Colors
    public static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // Green
    public static final Color SUCCESS_HOVER = new Color(22, 163, 74);       // Darker green
    public static final Color WARNING_COLOR = new Color(250, 204, 21);      // Yellow
    public static final Color DANGER_COLOR = new Color(239, 68, 68);        // Red
    public static final Color DANGER_HOVER = new Color(220, 50, 50);        // Darker red
    public static final Color INFO_COLOR = new Color(56, 189, 248);         // Cyan
    
    // Background Colors
    public static final Color BG_COLOR = new Color(24, 24, 37);             // Dark background
    public static final Color BG_SECONDARY = new Color(17, 17, 27);         // Darker background
    public static final Color CARD_BG = new Color(30, 30, 46);              // Card background
    public static final Color SIDEBAR_BG = new Color(30, 30, 46);           // Sidebar background
    public static final Color SIDEBAR_HOVER = new Color(49, 50, 68);        // Sidebar hover
    
    // Input Colors
    public static final Color INPUT_BG = new Color(49, 50, 68);             // Input background
    public static final Color INPUT_BORDER = new Color(69, 71, 90);         // Input border
    public static final Color INPUT_FOCUS = new Color(99, 102, 241);        // Input focus border
    
    // Text Colors
    public static final Color TEXT_COLOR = new Color(205, 214, 244);        // Primary text
    public static final Color TEXT_SECONDARY = new Color(166, 173, 200);    // Secondary text
    public static final Color TEXT_MUTED = new Color(108, 112, 134);        // Muted text
    
    // ==================== DIMENSIONS ====================
    // Standard Heights
    public static final int BUTTON_HEIGHT = 40;
    public static final int INPUT_HEIGHT_INT = 44;
    public static final int ROW_HEIGHT = 40;
    
    // Button Sizes
    public static final Dimension BUTTON_SMALL = new Dimension(80, 32);
    public static final Dimension BUTTON_MEDIUM = new Dimension(120, BUTTON_HEIGHT);
    public static final Dimension BUTTON_LARGE = new Dimension(180, 48);
    public static final Dimension BUTTON_FULL = new Dimension(Integer.MAX_VALUE, 48);
    
    // Input Sizes
    public static final Dimension INPUT_HEIGHT = new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT_INT);
    public static final Dimension INPUT_SMALL = new Dimension(Integer.MAX_VALUE, 36);
    
    // ==================== FONTS ====================
    public static final Font FONT_TITLE_LARGE = new Font("SansSerif", Font.BOLD, 28);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_CAPTION = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_PRICE = new Font("SansSerif", Font.BOLD, 20);
    
    // ==================== BORDERS ====================
    public static final int BORDER_RADIUS = 8;
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 15;
    public static final int PADDING_LARGE = 20;
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Create a styled button with consistent appearance
     */
    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(fgColor);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setOpaque(true);
        return btn;
    }
    
    /**
     * Create a primary styled button
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = createButton(text, PRIMARY_COLOR, Color.WHITE);
        addHoverEffect(btn, PRIMARY_COLOR, PRIMARY_HOVER);
        return btn;
    }
    
    /**
     * Create a success styled button
     */
    public static JButton createSuccessButton(String text) {
        JButton btn = createButton(text, SUCCESS_COLOR, Color.WHITE);
        addHoverEffect(btn, SUCCESS_COLOR, SUCCESS_HOVER);
        return btn;
    }
    
    /**
     * Create a danger styled button
     */
    public static JButton createDangerButton(String text) {
        JButton btn = createButton(text, DANGER_COLOR, Color.WHITE);
        addHoverEffect(btn, DANGER_COLOR, DANGER_HOVER);
        return btn;
    }
    
    /**
     * Create a secondary styled button
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = createButton(text, INPUT_BG, TEXT_COLOR);
        addHoverEffect(btn, INPUT_BG, INPUT_BORDER);
        return btn;
    }
    
    /**
     * Create icon button (small square button)
     */
    public static JButton createIconButton(String icon, Color bgColor) {
        JButton btn = new JButton(icon);
        btn.setFont(FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setPreferredSize(new Dimension(44, 44));
        return btn;
    }
    
    /**
     * Add hover effect to button
     */
    public static void addHoverEffect(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hover);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(normal);
            }
        });
    }
    
    /**
     * Create a styled text field
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setMaximumSize(INPUT_HEIGHT);
        field.setPreferredSize(new Dimension(200, 44));
        if (placeholder != null) {
            field.putClientProperty("JTextField.placeholderText", placeholder);
        }
        return field;
    }
    
    /**
     * Create a styled password field
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT_BODY);
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setMaximumSize(INPUT_HEIGHT);
        field.setPreferredSize(new Dimension(200, 44));
        if (placeholder != null) {
            field.putClientProperty("JTextField.placeholderText", placeholder);
        }
        return field;
    }
    
    /**
     * Create a styled combo box
     */
    public static <T> JComboBox<T> createComboBox() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(FONT_BODY);
        combo.setBackground(INPUT_BG);
        combo.setForeground(TEXT_COLOR);
        combo.setPreferredSize(new Dimension(200, 44));
        combo.setMaximumSize(INPUT_HEIGHT);
        return combo;
    }
    
    /**
     * Create a card panel with border
     */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        return card;
    }
    
    /**
     * Create a card panel with top accent
     */
    public static JPanel createAccentCard(Color accentColor) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(PADDING_MEDIUM, PADDING_LARGE, PADDING_MEDIUM, PADDING_LARGE)
        ));
        return card;
    }
    
    /**
     * Create a styled label
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }
    
    /**
     * Create a styled JTable
     */
    public static void styleTable(JTable table) {
        table.setBackground(INPUT_BG);
        table.setForeground(TEXT_COLOR);
        table.setGridColor(INPUT_BORDER);
        table.setRowHeight(44);
        table.setSelectionBackground(PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        // Style header
        table.getTableHeader().setBackground(CARD_BG);
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.getTableHeader().setFont(FONT_SMALL_BOLD);
        table.getTableHeader().setPreferredSize(new Dimension(0, 44));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, INPUT_BORDER));
    }
    
    /**
     * Create a styled scroll pane
     */
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createLineBorder(INPUT_BORDER));
        scrollPane.getViewport().setBackground(INPUT_BG);
        return scrollPane;
    }
    
    /**
     * Style a dialog
     */
    public static void styleDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(CARD_BG);
    }
}
