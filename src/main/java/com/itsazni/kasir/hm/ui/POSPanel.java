package com.itsazni.kasir.hm.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.itsazni.kasir.hm.dao.ProductDAO;
import com.itsazni.kasir.hm.dao.TransactionDAO;
import com.itsazni.kasir.hm.models.Product;
import com.itsazni.kasir.hm.models.Transaction;
import com.itsazni.kasir.hm.models.TransactionItem;
import com.itsazni.kasir.hm.models.User;
import com.itsazni.kasir.hm.utils.BarcodeScanner;
import com.itsazni.kasir.hm.utils.CurrencyUtils;
import com.itsazni.kasir.hm.utils.ReceiptPrinter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

import static com.itsazni.kasir.hm.ui.UIConstants.*;

/**
 * Point of Sale panel for processing transactions - Modern design with inline scanner
 */
public class POSPanel extends JPanel {
    
    private final User currentUser;
    private final ProductDAO productDAO;
    private final TransactionDAO transactionDAO;
    private Transaction currentTransaction;
    
    // Cart table
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    
    // Product search
    private JTextField txtSearch;
    private JList<Product> productList;
    private DefaultListModel<Product> productListModel;
    
    // Totals
    private JLabel lblSubtotal;
    private JLabel lblDiscount;
    private JLabel lblTotal;
    private JTextField txtDiscount;
    private JTextField txtPayment;
    private JLabel lblChange;
    
    // Inline scanner components
    private JPanel scannerPanel;
    private JPanel cameraContainer;
    private WebcamPanel webcamPanel;
    private BarcodeScanner scanner;
    private Webcam currentWebcam;
    private JComboBox<WebcamItem> cmbCamera;
    private JLabel lblScanStatus;
    private JLabel lblLastScanned;
    private boolean isScannerActive = false;
    
    public POSPanel(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();
        this.transactionDAO = new TransactionDAO();
        this.currentTransaction = new Transaction();
        currentTransaction.setUserId(user.getId());
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(12, 0));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Left panel - Product search and scanner
        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);
        
        // Center panel - Cart
        JPanel centerPanel = createCartPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Right panel - Payment
        JPanel rightPanel = createPaymentPanel();
        add(rightPanel, BorderLayout.EAST);
        
        loadProducts();
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_COLOR);
        panel.setPreferredSize(new Dimension(320, 0));
        
        // Top: Inline Scanner
        scannerPanel = createInlineScannerPanel();
        panel.add(scannerPanel, BorderLayout.NORTH);
        
        // Bottom: Product list
        JPanel productPanel = createProductPanel();
        panel.add(productPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInlineScannerPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(320, 380));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        
        JLabel lblTitle = createLabel("📷 Scanner Barcode", FONT_BODY_BOLD, TEXT_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Camera container
        cameraContainer = new JPanel(new BorderLayout());
        cameraContainer.setBackground(INPUT_BG);
        cameraContainer.setPreferredSize(new Dimension(290, 240));
        
        JLabel lblPlaceholder = createLabel("Klik 'Mulai Scan' untuk mengaktifkan kamera", FONT_SMALL, TEXT_SECONDARY);
        lblPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        cameraContainer.add(lblPlaceholder, BorderLayout.CENTER);
        
        panel.add(cameraContainer, BorderLayout.CENTER);
        
        // Controls panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(CARD_BG);
        
        // Camera selection row
        JPanel cameraRow = new JPanel(new BorderLayout(8, 0));
        cameraRow.setBackground(CARD_BG);
        cameraRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        
        cmbCamera = new JComboBox<>();
        cmbCamera.setFont(FONT_SMALL);
        cmbCamera.setBackground(INPUT_BG);
        cmbCamera.setForeground(TEXT_COLOR);
        cmbCamera.setPreferredSize(new Dimension(180, 36));
        cameraRow.add(cmbCamera, BorderLayout.CENTER);
        
        JButton btnRefreshCam = createSecondaryButton("↻");
        btnRefreshCam.setPreferredSize(new Dimension(40, 36));
        btnRefreshCam.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnRefreshCam.addActionListener(e -> loadCameraList());
        cameraRow.add(btnRefreshCam, BorderLayout.EAST);
        
        controlsPanel.add(cameraRow);
        controlsPanel.add(Box.createVerticalStrut(8));
        
        // Button row
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonRow.setBackground(CARD_BG);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JButton btnStartScan = createSuccessButton("▶ Mulai Scan");
        btnStartScan.setFont(FONT_SMALL_BOLD);
        btnStartScan.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        btnStartScan.addActionListener(e -> startScanner());
        
        JButton btnStopScan = createDangerButton("⏹ Stop");
        btnStopScan.setFont(FONT_SMALL_BOLD);
        btnStopScan.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        btnStopScan.addActionListener(e -> stopScanner());
        
        buttonRow.add(btnStartScan);
        buttonRow.add(btnStopScan);
        controlsPanel.add(buttonRow);
        controlsPanel.add(Box.createVerticalStrut(8));
        
        // Status row
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setBackground(CARD_BG);
        
        lblScanStatus = createLabel("⚪ Tidak aktif", FONT_SMALL, TEXT_MUTED);
        statusRow.add(lblScanStatus, BorderLayout.WEST);
        
        lblLastScanned = createLabel("", FONT_SMALL, SUCCESS_COLOR);
        statusRow.add(lblLastScanned, BorderLayout.EAST);
        
        controlsPanel.add(statusRow);
        
        panel.add(controlsPanel, BorderLayout.SOUTH);
        
        // Load camera list on init
        SwingUtilities.invokeLater(this::loadCameraList);
        
        return panel;
    }
    
    private JPanel createProductPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(0, 10));
        
        // Header
        JLabel lblTitle = createLabel("🛍️ Daftar Produk", FONT_BODY_BOLD, TEXT_COLOR);
        panel.add(lblTitle, BorderLayout.NORTH);
        
        // Search
        txtSearch = createTextField("Cari atau ketik barcode...");
        txtSearch.setPreferredSize(new Dimension(0, 40));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    String text = txtSearch.getText().trim();
                    Product product = productDAO.findByBarcode(text);
                    if (product != null) {
                        addToCart(product);
                        txtSearch.setText("");
                        loadProducts();
                    }
                } else {
                    searchProducts();
                }
            }
        });
        
        // Product list
        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setBackground(INPUT_BG);
        productList.setForeground(TEXT_COLOR);
        productList.setSelectionBackground(PRIMARY_COLOR);
        productList.setFixedCellHeight(56);
        productList.setCellRenderer(new ProductListCellRenderer());
        productList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Product selected = productList.getSelectedValue();
                    if (selected != null) {
                        addToCart(selected);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = createScrollPane(productList);
        
        JPanel listPanel = new JPanel(new BorderLayout(0, 10));
        listPanel.setBackground(CARD_BG);
        listPanel.add(txtSearch, BorderLayout.NORTH);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(listPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCartPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(0, 12));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        
        JLabel lblTitle = createLabel("🛒 Keranjang Belanja", FONT_BODY_BOLD, TEXT_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        JButton btnClear = createDangerButton("🗑️ Kosongkan");
        btnClear.setFont(FONT_SMALL_BOLD);
        btnClear.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btnClear.addActionListener(e -> clearCart());
        headerPanel.add(btnClear, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Cart table
        String[] columns = {"Produk", "Harga", "Qty", "Subtotal", ""};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        
        cartTable = new JTable(cartTableModel);
        styleTable(cartTable);
        cartTable.setRowHeight(48);
        
        // Column widths
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        
        // Delete button column renderer
        cartTable.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("✕");
            btn.setBackground(DANGER_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setFont(FONT_SMALL_BOLD);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return btn;
        });
        
        // Center align quantity column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        cartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = cartTable.columnAtPoint(e.getPoint());
                int row = cartTable.rowAtPoint(e.getPoint());
                if (column == 4 && row >= 0) {
                    removeFromCart(row);
                }
            }
        });
        
        // Update quantity when edited
        cartTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 2) {
                int row = e.getFirstRow();
                try {
                    int newQty = Integer.parseInt(cartTableModel.getValueAt(row, 2).toString());
                    if (newQty > 0) {
                        currentTransaction.getItems().get(row).setQuantity(newQty);
                        currentTransaction.calculateTotals();
                        refreshCartDisplay();
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }
        });
        
        JScrollPane scrollPane = createScrollPane(cartTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPaymentPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 0));
        
        // Title
        JLabel lblTitle = createLabel("💳 Pembayaran", FONT_BODY_BOLD, TEXT_COLOR);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(20));
        
        // Subtotal
        panel.add(createPaymentRow("Subtotal:", lblSubtotal = createLabel("Rp 0", FONT_BODY_BOLD, TEXT_COLOR)));
        panel.add(Box.createVerticalStrut(12));
        
        // Discount input
        JLabel lblDiscountLabel = createLabel("Diskon (%):", FONT_BODY, TEXT_SECONDARY);
        lblDiscountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblDiscountLabel);
        panel.add(Box.createVerticalStrut(6));
        
        txtDiscount = createTextField("");
        txtDiscount.setText("0");
        txtDiscount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtDiscount.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDiscount.setHorizontalAlignment(JTextField.RIGHT);
        txtDiscount.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyDiscount();
            }
        });
        panel.add(txtDiscount);
        panel.add(Box.createVerticalStrut(12));
        
        panel.add(createPaymentRow("Potongan:", lblDiscount = createLabel("- Rp 0", FONT_BODY, DANGER_COLOR)));
        panel.add(Box.createVerticalStrut(16));
        
        // Divider
        JSeparator separator = new JSeparator();
        separator.setForeground(INPUT_BORDER);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(16));
        
        // Total
        JPanel totalPanel = createPaymentRow("TOTAL:", lblTotal = createLabel("Rp 0", FONT_PRICE, SUCCESS_COLOR));
        panel.add(totalPanel);
        panel.add(Box.createVerticalStrut(24));
        
        // Payment input
        JLabel lblPaymentLabel = createLabel("Bayar (Rp):", FONT_BODY_BOLD, TEXT_COLOR);
        lblPaymentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPaymentLabel);
        panel.add(Box.createVerticalStrut(8));
        
        txtPayment = createTextField("");
        txtPayment.setText("0");
        txtPayment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtPayment.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPayment.setHorizontalAlignment(JTextField.RIGHT);
        txtPayment.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                calculateChange();
            }
        });
        panel.add(txtPayment);
        panel.add(Box.createVerticalStrut(12));
        
        // Change
        panel.add(createPaymentRow("Kembalian:", lblChange = createLabel("Rp 0", FONT_SUBTITLE, TEXT_COLOR)));
        
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(16));
        
        // Quick amount buttons
        JPanel quickPanel = new JPanel(new GridLayout(2, 3, 6, 6));
        quickPanel.setBackground(CARD_BG);
        quickPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        quickPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        int[] amounts = {10000, 20000, 50000, 100000, 200000, 500000};
        for (int amount : amounts) {
            JButton btn = createSecondaryButton(amount >= 1000 ? (amount / 1000) + "K" : String.valueOf(amount));
            btn.setFont(FONT_SMALL_BOLD);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
            final int amt = amount;
            btn.addActionListener(e -> {
                txtPayment.setText(String.valueOf(amt));
                calculateChange();
            });
            quickPanel.add(btn);
        }
        panel.add(quickPanel);
        panel.add(Box.createVerticalStrut(16));
        
        // Process button
        JButton btnProcess = createSuccessButton("✅ Proses Pembayaran");
        btnProcess.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btnProcess.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnProcess.setFont(FONT_BODY_BOLD);
        btnProcess.addActionListener(e -> processPayment());
        panel.add(btnProcess);
        
        return panel;
    }
    
    private JPanel createPaymentRow(String label, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblLabel = createLabel(label, FONT_BODY, TEXT_SECONDARY);
        panel.add(lblLabel, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    // ==================== SCANNER METHODS ====================
    
    private void loadCameraList() {
        SwingWorker<List<Webcam>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Webcam> doInBackground() {
                return Webcam.getWebcams();
            }
            
            @Override
            protected void done() {
                try {
                    List<Webcam> webcams = get();
                    cmbCamera.removeAllItems();
                    
                    if (webcams.isEmpty()) {
                        lblScanStatus.setText("⚠️ Tidak ada kamera");
                        return;
                    }
                    
                    int index = 0;
                    for (Webcam webcam : webcams) {
                        String name = webcam.getName();
                        String displayName;
                        if (name.toLowerCase().contains("usb") || 
                            name.toLowerCase().contains("android") ||
                            name.toLowerCase().contains("droidcam")) {
                            displayName = "📱 " + name;
                        } else {
                            displayName = "📷 " + name;
                        }
                        cmbCamera.addItem(new WebcamItem(webcam, displayName, index));
                        index++;
                    }
                    
                    lblScanStatus.setText("⚪ " + webcams.size() + " kamera tersedia");
                } catch (Exception e) {
                    lblScanStatus.setText("❌ Gagal memuat kamera");
                }
            }
        };
        worker.execute();
    }
    
    private void startScanner() {
        if (isScannerActive) return;
        
        WebcamItem selectedItem = (WebcamItem) cmbCamera.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Pilih kamera terlebih dahulu!");
            return;
        }
        
        lblScanStatus.setText("🔄 Membuka kamera...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                currentWebcam = selectedItem.webcam;
                if (currentWebcam.getViewSizes().length > 0) {
                    currentWebcam.setViewSize(currentWebcam.getViewSizes()[currentWebcam.getViewSizes().length - 1]);
                }
                currentWebcam.open();
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    startCameraPreview();
                    isScannerActive = true;
                    lblScanStatus.setText("🟢 Scanning aktif");
                } catch (Exception e) {
                    lblScanStatus.setText("❌ Gagal: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void startCameraPreview() {
        cameraContainer.removeAll();
        
        webcamPanel = new WebcamPanel(currentWebcam);
        webcamPanel.setFPSDisplayed(false);
        webcamPanel.setDisplayDebugInfo(false);
        webcamPanel.setMirrored(true);
        webcamPanel.setFillArea(true);
        
        cameraContainer.add(webcamPanel, BorderLayout.CENTER);
        cameraContainer.revalidate();
        cameraContainer.repaint();
        
        // Start barcode scanning - continues scanning without closing
        scanner = new BarcodeScanner(currentWebcam);
        scanner.startScanning(barcode -> {
            SwingUtilities.invokeLater(() -> {
                Toolkit.getDefaultToolkit().beep();
                
                Product product = productDAO.findByBarcode(barcode);
                if (product != null) {
                    addToCart(product);
                    lblLastScanned.setText("✓ " + product.getName());
                    
                    // Auto-clear after 2 seconds
                    Timer timer = new Timer(2000, e -> lblLastScanned.setText(""));
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    lblLastScanned.setText("✗ Tidak ditemukan");
                }
            });
        });
    }
    
    private void stopScanner() {
        isScannerActive = false;
        
        if (scanner != null) {
            try {
                scanner.close();
            } catch (Exception ignored) {}
            scanner = null;
        }
        
        if (currentWebcam != null) {
            final Webcam webcamToClose = currentWebcam;
            currentWebcam = null;
            
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    if (webcamToClose.isOpen()) {
                        webcamToClose.close();
                    }
                } catch (Exception ignored) {}
            }).start();
        }
        
        if (webcamPanel != null) {
            webcamPanel.stop();
            webcamPanel = null;
        }
        
        cameraContainer.removeAll();
        JLabel lblPlaceholder = createLabel("Klik 'Mulai Scan' untuk mengaktifkan kamera", FONT_SMALL, TEXT_SECONDARY);
        lblPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        cameraContainer.add(lblPlaceholder, BorderLayout.CENTER);
        cameraContainer.revalidate();
        cameraContainer.repaint();
        
        lblScanStatus.setText("⚪ Tidak aktif");
        lblLastScanned.setText("");
    }
    
    // ==================== PRODUCT & CART METHODS ====================
    
    private void loadProducts() {
        productListModel.clear();
        for (Product p : productDAO.findAll()) {
            if (p.getStock() > 0) {
                productListModel.addElement(p);
            }
        }
    }
    
    private void searchProducts() {
        String keyword = txtSearch.getText().trim();
        productListModel.clear();
        
        List<Product> products = keyword.isEmpty() ? 
                productDAO.findAll() : productDAO.search(keyword);
        
        for (Product p : products) {
            if (p.getStock() > 0) {
                productListModel.addElement(p);
            }
        }
    }
    
    private void addToCart(Product product) {
        if (product.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "Stok produk habis!");
            return;
        }
        
        // Check if already in cart
        int currentQty = 0;
        for (TransactionItem item : currentTransaction.getItems()) {
            if (item.getProductId() == product.getId()) {
                currentQty = item.getQuantity();
                break;
            }
        }
        
        if (currentQty >= product.getStock()) {
            JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!");
            return;
        }
        
        TransactionItem item = new TransactionItem(product, 1);
        currentTransaction.addItem(item);
        refreshCartDisplay();
    }
    
    private void removeFromCart(int index) {
        currentTransaction.removeItem(index);
        refreshCartDisplay();
    }
    
    private void clearCart() {
        currentTransaction.clearItems();
        txtDiscount.setText("0");
        txtPayment.setText("");
        refreshCartDisplay();
    }
    
    private void refreshCartDisplay() {
        cartTableModel.setRowCount(0);
        for (TransactionItem item : currentTransaction.getItems()) {
            cartTableModel.addRow(new Object[]{
                item.getProductName(),
                CurrencyUtils.format(item.getPrice()),
                item.getQuantity(),
                CurrencyUtils.format(item.getSubtotal()),
                "✕"
            });
        }
        
        lblSubtotal.setText(CurrencyUtils.format(currentTransaction.getSubtotal()));
        lblDiscount.setText("- " + CurrencyUtils.format(currentTransaction.getDiscountAmount()));
        lblTotal.setText(CurrencyUtils.format(currentTransaction.getTotal()));
        calculateChange();
    }
    
    private void applyDiscount() {
        try {
            BigDecimal discount = new BigDecimal(txtDiscount.getText().trim());
            if (discount.compareTo(BigDecimal.ZERO) >= 0 && discount.compareTo(new BigDecimal(100)) <= 0) {
                currentTransaction.setDiscountPercent(discount);
                refreshCartDisplay();
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }
    
    private void calculateChange() {
        try {
            BigDecimal payment = CurrencyUtils.parse(txtPayment.getText());
            currentTransaction.setPayment(payment);
            
            BigDecimal change = currentTransaction.getChangeAmount();
            lblChange.setText(CurrencyUtils.format(change));
            
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                lblChange.setForeground(SUCCESS_COLOR);
            } else {
                lblChange.setForeground(DANGER_COLOR);
            }
        } catch (Exception e) {
            lblChange.setText("Rp 0");
            lblChange.setForeground(TEXT_COLOR);
        }
    }
    
    private void processPayment() {
        if (currentTransaction.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!");
            return;
        }
        
        if (currentTransaction.getPayment().compareTo(currentTransaction.getTotal()) < 0) {
            JOptionPane.showMessageDialog(this, "Pembayaran kurang dari total!");
            return;
        }
        
        // Save transaction
        if (transactionDAO.save(currentTransaction)) {
            // Show receipt
            ReceiptPrinter printer = new ReceiptPrinter(currentTransaction, currentUser.getFullName());
            printer.showPreview((JFrame) SwingUtilities.getWindowAncestor(this));
            
            // Reset
            currentTransaction = new Transaction();
            currentTransaction.setUserId(currentUser.getId());
            clearCart();
            loadProducts();
            
            JOptionPane.showMessageDialog(this, "Transaksi berhasil!");
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi!");
        }
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Helper class for webcam combo box
     */
    private static class WebcamItem {
        final Webcam webcam;
        final String displayName;
        final int index;
        
        WebcamItem(Webcam webcam, String displayName, int index) {
            this.webcam = webcam;
            this.displayName = displayName;
            this.index = index;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    /**
     * Custom cell renderer for product list
     */
    private class ProductListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBackground(isSelected ? PRIMARY_COLOR : INPUT_BG);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            
            Product product = (Product) value;
            
            JLabel lblName = createLabel(product.getName(), FONT_BODY, TEXT_COLOR);
            
            JLabel lblPrice = createLabel(CurrencyUtils.format(product.getPrice()), FONT_BODY_BOLD, SUCCESS_COLOR);
            
            JLabel lblStock = createLabel("Stok: " + product.getStock(), FONT_CAPTION, TEXT_SECONDARY);
            
            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.setBackground(panel.getBackground());
            infoPanel.add(lblName, BorderLayout.NORTH);
            infoPanel.add(lblStock, BorderLayout.SOUTH);
            
            panel.add(infoPanel, BorderLayout.CENTER);
            panel.add(lblPrice, BorderLayout.EAST);
            
            return panel;
        }
    }
}
