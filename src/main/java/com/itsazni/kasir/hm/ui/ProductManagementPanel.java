package com.itsazni.kasir.hm.ui;

import com.itsazni.kasir.hm.dao.ProductDAO;
import com.itsazni.kasir.hm.models.Product;
import com.itsazni.kasir.hm.utils.BarcodeScanner;
import com.itsazni.kasir.hm.utils.CurrencyUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

import static com.itsazni.kasir.hm.ui.UIConstants.*;

/**
 * Product management panel for CRUD operations - Modern design
 */
public class ProductManagementPanel extends JPanel {
    
    private final ProductDAO productDAO;
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbCategory;
    
    // Form fields
    private JDialog productDialog;
    private JTextField txtBarcode;
    private JTextField txtName;
    private JTextField txtPrice;
    private JTextField txtStock;
    private JTextField txtMinStock;
    private JComboBox<String> cmbFormCategory;
    private Product currentProduct;
    
    public ProductManagementPanel() {
        this.productDAO = new ProductDAO();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        
        JLabel lblTitle = createLabel("📦 Manajemen Produk", FONT_TITLE, TEXT_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        // Add button
        JButton btnAdd = createSuccessButton("+ Tambah Produk");
        btnAdd.addActionListener(e -> showProductDialog(null));
        headerPanel.add(btnAdd, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Search and filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(BG_COLOR);
        
        txtSearch = createTextField("Cari produk...");
        txtSearch.setPreferredSize(new Dimension(280, BUTTON_HEIGHT));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchProducts();
            }
        });
        filterPanel.add(txtSearch);
        
        cmbCategory = new JComboBox<>();
        cmbCategory.addItem("Semua Kategori");
        cmbCategory.setPreferredSize(new Dimension(180, BUTTON_HEIGHT));
        cmbCategory.setBackground(INPUT_BG);
        cmbCategory.setForeground(TEXT_COLOR);
        cmbCategory.setFont(FONT_BODY);
        cmbCategory.addActionListener(e -> searchProducts());
        filterPanel.add(cmbCategory);
        
        JButton btnScan = createPrimaryButton("📷 Scan Barcode");
        btnScan.addActionListener(e -> scanBarcode());
        filterPanel.add(btnScan);
        
        // Table
        String[] columns = {"ID", "Barcode", "Nama Produk", "Kategori", "Harga", "Stok", "Min Stok", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        styleTable(productTable);
        
        // Hide ID column
        productTable.getColumnModel().getColumn(0).setMinWidth(0);
        productTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        // Set column widths
        productTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Barcode
        productTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Name
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Category
        productTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Price
        productTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Stock
        productTable.getColumnModel().getColumn(6).setPreferredWidth(60);  // Min Stock
        productTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Status
        
        // Context menu
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBackground(CARD_BG);
        popupMenu.setBorder(BorderFactory.createLineBorder(INPUT_BORDER));
        
        JMenuItem editItem = new JMenuItem("✏️ Edit");
        editItem.setBackground(CARD_BG);
        editItem.setForeground(TEXT_COLOR);
        editItem.setFont(FONT_BODY);
        editItem.addActionListener(e -> editSelectedProduct());
        
        JMenuItem deleteItem = new JMenuItem("🗑️ Hapus");
        deleteItem.setBackground(CARD_BG);
        deleteItem.setForeground(DANGER_COLOR);
        deleteItem.setFont(FONT_BODY);
        deleteItem.addActionListener(e -> deleteSelectedProduct());
        
        popupMenu.add(editItem);
        popupMenu.addSeparator();
        popupMenu.add(deleteItem);
        productTable.setComponentPopupMenu(popupMenu);
        
        // Double-click to edit
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedProduct();
                }
            }
        });
        
        JScrollPane scrollPane = createScrollPane(productTable);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.add(filterPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        refresh();
    }
    
    public void refresh() {
        loadProducts(productDAO.findAll());
        loadCategories();
    }
    
    private void loadProducts(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            String status = p.isLowStock() ? "⚠️ Stok Menipis" : "✅ Normal";
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getBarcode(),
                p.getName(),
                p.getCategory(),
                CurrencyUtils.format(p.getPrice()),
                p.getStock(),
                p.getMinStock(),
                status
            });
        }
    }
    
    private void loadCategories() {
        cmbCategory.removeAllItems();
        cmbCategory.addItem("Semua Kategori");
        for (String category : productDAO.getAllCategories()) {
            cmbCategory.addItem(category);
        }
    }
    
    private void searchProducts() {
        String keyword = txtSearch.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();
        
        List<Product> products;
        if (keyword.isEmpty() && (category == null || category.equals("Semua Kategori"))) {
            products = productDAO.findAll();
        } else if (!keyword.isEmpty()) {
            products = productDAO.search(keyword);
        } else {
            products = productDAO.findByCategory(category);
        }
        loadProducts(products);
    }
    
    private void showProductDialog(Product product) {
        currentProduct = product;
        boolean isEdit = product != null;
        
        productDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                isEdit ? "Edit Produk" : "Tambah Produk", true);
        productDialog.setSize(450, 550);
        productDialog.setLocationRelativeTo(this);
        productDialog.setLayout(new BorderLayout());
        productDialog.getContentPane().setBackground(CARD_BG);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        
        // Dialog header
        JLabel lblDialogTitle = createLabel(isEdit ? "✏️ Edit Produk" : "➕ Tambah Produk Baru", FONT_SUBTITLE, TEXT_COLOR);
        lblDialogTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblDialogTitle);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Barcode
        formPanel.add(createFormLabel("Barcode"));
        JPanel barcodePanel = new JPanel(new BorderLayout(8, 0));
        barcodePanel.setBackground(CARD_BG);
        barcodePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        barcodePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtBarcode = createTextField("");
        txtBarcode.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        barcodePanel.add(txtBarcode, BorderLayout.CENTER);
        
        JButton btnScanBarcode = createPrimaryButton("📷");
        btnScanBarcode.setPreferredSize(new Dimension(50, BUTTON_HEIGHT));
        btnScanBarcode.addActionListener(e -> scanBarcodeForForm());
        barcodePanel.add(btnScanBarcode, BorderLayout.EAST);
        formPanel.add(barcodePanel);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Name
        formPanel.add(createFormLabel("Nama Produk"));
        txtName = createTextField("");
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtName);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Category
        formPanel.add(createFormLabel("Kategori"));
        cmbFormCategory = new JComboBox<>();
        cmbFormCategory.setEditable(true);
        cmbFormCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        cmbFormCategory.setBackground(INPUT_BG);
        cmbFormCategory.setForeground(TEXT_COLOR);
        cmbFormCategory.setFont(FONT_BODY);
        cmbFormCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String cat : productDAO.getAllCategories()) {
            cmbFormCategory.addItem(cat);
        }
        formPanel.add(cmbFormCategory);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Price
        formPanel.add(createFormLabel("Harga (Rp)"));
        txtPrice = createTextField("");
        txtPrice.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        txtPrice.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtPrice);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Stock row
        JPanel stockRow = new JPanel(new GridLayout(1, 2, 15, 0));
        stockRow.setBackground(CARD_BG);
        stockRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        stockRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel stockPanel = new JPanel();
        stockPanel.setLayout(new BoxLayout(stockPanel, BoxLayout.Y_AXIS));
        stockPanel.setBackground(CARD_BG);
        stockPanel.add(createFormLabel("Stok"));
        txtStock = createTextField("");
        txtStock.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        stockPanel.add(txtStock);
        stockRow.add(stockPanel);
        
        JPanel minStockPanel = new JPanel();
        minStockPanel.setLayout(new BoxLayout(minStockPanel, BoxLayout.Y_AXIS));
        minStockPanel.setBackground(CARD_BG);
        minStockPanel.add(createFormLabel("Stok Minimum"));
        txtMinStock = createTextField("");
        txtMinStock.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        minStockPanel.add(txtMinStock);
        stockRow.add(minStockPanel);
        
        formPanel.add(stockRow);
        formPanel.add(Box.createVerticalStrut(25));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        
        JButton btnCancel = createSecondaryButton("Batal");
        btnCancel.addActionListener(e -> productDialog.dispose());
        
        JButton btnSave = createSuccessButton(isEdit ? "Update" : "Simpan");
        btnSave.addActionListener(e -> saveProduct());
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        formPanel.add(buttonPanel);
        
        productDialog.add(formPanel);
        
        // Fill data if editing
        if (isEdit) {
            txtBarcode.setText(product.getBarcode());
            txtName.setText(product.getName());
            cmbFormCategory.setSelectedItem(product.getCategory());
            txtPrice.setText(product.getPrice().toString());
            txtStock.setText(String.valueOf(product.getStock()));
            txtMinStock.setText(String.valueOf(product.getMinStock()));
        } else {
            txtMinStock.setText("5");
        }
        
        productDialog.setVisible(true);
    }
    
    private JLabel createFormLabel(String text) {
        JLabel label = createLabel(text, FONT_SMALL, TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return label;
    }
    
    private void saveProduct() {
        try {
            String barcode = txtBarcode.getText().trim();
            String name = txtName.getText().trim();
            String category = (String) cmbFormCategory.getSelectedItem();
            BigDecimal price = new BigDecimal(txtPrice.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            int minStock = Integer.parseInt(txtMinStock.getText().trim());
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(productDialog, "Nama produk harus diisi!", 
                        "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Product product = currentProduct != null ? currentProduct : new Product();
            product.setBarcode(barcode);
            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setStock(stock);
            product.setMinStock(minStock);
            
            boolean success;
            if (currentProduct != null) {
                success = productDAO.update(product);
            } else {
                success = productDAO.create(product);
            }
            
            if (success) {
                productDialog.dispose();
                refresh();
                JOptionPane.showMessageDialog(this, "Produk berhasil disimpan!", 
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(productDialog, "Gagal menyimpan produk!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(productDialog, "Format angka tidak valid!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            Product product = productDAO.findById(id);
            if (product != null) {
                showProductDialog(product);
            }
        }
    }
    
    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 2);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Hapus produk \"" + name + "\"?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (productDAO.delete(id)) {
                    refresh();
                    JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!", 
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus produk!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void scanBarcode() {
        BarcodeScannerDialog dialog = new BarcodeScannerDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                barcode -> {
                    Product product = productDAO.findByBarcode(barcode);
                    if (product != null) {
                        // Product found - show edit dialog
                        showProductDialog(product);
                    } else {
                        // New product - show add dialog with barcode
                        Product newProduct = new Product();
                        newProduct.setBarcode(barcode);
                        showProductDialog(null);
                        txtBarcode.setText(barcode);
                    }
                }
        );
        dialog.setVisible(true);
    }
    
    private void scanBarcodeForForm() {
        BarcodeScannerDialog dialog = new BarcodeScannerDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                barcode -> txtBarcode.setText(barcode)
        );
        dialog.setVisible(true);
    }
}
