package com.itsazni.kasir.hm.ui;

import com.itsazni.kasir.hm.dao.TransactionDAO;
import com.itsazni.kasir.hm.models.Transaction;
import com.itsazni.kasir.hm.models.TransactionItem;
import com.itsazni.kasir.hm.utils.CurrencyUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.itsazni.kasir.hm.ui.UIConstants.*;

/**
 * Transaction history panel with date filtering and charts - Modern design
 */
public class TransactionHistoryPanel extends JPanel {
    
    private final TransactionDAO transactionDAO;
    
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JSpinner dateFrom;
    private JSpinner dateTo;
    private ChartPanel chartPanel;
    
    // Detail panel
    private JPanel detailPanel;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    
    public TransactionHistoryPanel() {
        this.transactionDAO = new TransactionDAO();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        
        JLabel lblTitle = createLabel("📋 Riwayat Transaksi", FONT_TITLE, TEXT_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(BG_COLOR);
        
        JLabel lblFrom = createLabel("Dari:", FONT_BODY, TEXT_SECONDARY);
        filterPanel.add(lblFrom);
        
        dateFrom = createDateSpinner();
        dateFrom.setValue(java.sql.Date.valueOf(LocalDate.now().minusDays(7)));
        filterPanel.add(dateFrom);
        
        JLabel lblTo = createLabel("Sampai:", FONT_BODY, TEXT_SECONDARY);
        filterPanel.add(lblTo);
        
        dateTo = createDateSpinner();
        dateTo.setValue(java.sql.Date.valueOf(LocalDate.now()));
        filterPanel.add(dateTo);
        
        JButton btnFilter = createPrimaryButton("🔍 Filter");
        btnFilter.addActionListener(e -> filterTransactions());
        filterPanel.add(btnFilter);
        
        JButton btnExport = createSuccessButton("📥 Export CSV");
        btnExport.addActionListener(e -> exportToCSV());
        filterPanel.add(btnExport);
        
        headerPanel.add(filterPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Split pane for table and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        splitPane.setBackground(BG_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        
        // Left side - Transaction list
        JPanel leftPanel = createCard();
        leftPanel.setLayout(new BorderLayout(0, 12));
        
        JLabel lblTableTitle = createLabel("📝 Daftar Transaksi", FONT_BODY_BOLD, TEXT_COLOR);
        leftPanel.add(lblTableTitle, BorderLayout.NORTH);
        
        // Transaction table
        String[] columns = {"ID", "Tanggal", "Total Item", "Subtotal", "Diskon", "Total", "Bayar", "Kembali"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        styleTable(transactionTable);
        
        // Hide ID column
        transactionTable.getColumnModel().getColumn(0).setMinWidth(0);
        transactionTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        transactionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showTransactionDetail();
            }
        });
        
        JScrollPane tableScrollPane = createScrollPane(transactionTable);
        
        // Center panel for table and detail
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setBackground(CARD_BG);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Detail panel
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(CARD_BG);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, INPUT_BORDER),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));
        detailPanel.setPreferredSize(new Dimension(0, 180));
        
        JLabel lblDetailTitle = createLabel("📦 Detail Item", FONT_BODY_BOLD, TEXT_COLOR);
        detailPanel.add(lblDetailTitle, BorderLayout.NORTH);
        
        String[] itemColumns = {"Produk", "Harga", "Qty", "Subtotal"};
        itemsTableModel = new DefaultTableModel(itemColumns, 0);
        itemsTable = new JTable(itemsTableModel);
        styleTable(itemsTable);
        
        JScrollPane itemsScrollPane = createScrollPane(itemsTable);
        itemsScrollPane.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        detailPanel.add(itemsScrollPane, BorderLayout.CENTER);
        
        centerPanel.add(detailPanel, BorderLayout.SOUTH);
        leftPanel.add(centerPanel, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(leftPanel);
        
        // Right side - Chart
        JPanel rightPanel = createCard();
        rightPanel.setLayout(new BorderLayout(0, 12));
        
        JLabel lblChartTitle = createLabel("📈 Grafik Penjualan Harian", FONT_BODY_BOLD, TEXT_COLOR);
        rightPanel.add(lblChartTitle, BorderLayout.NORTH);
        
        // Create empty chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = createLineChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(CARD_BG);
        rightPanel.add(chartPanel, BorderLayout.CENTER);
        
        // Summary panel
        JPanel summaryPanel = createSummaryPanel();
        rightPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        refresh();
    }
    
    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setPreferredSize(new Dimension(120, BUTTON_HEIGHT));
        spinner.setFont(FONT_BODY);
        
        // Style the spinner
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(INPUT_BG);
            tf.setForeground(TEXT_COLOR);
            tf.setCaretColor(TEXT_COLOR);
        }
        
        return spinner;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, INPUT_BORDER),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));
        
        return panel;
    }
    
    private JFreeChart createLineChart(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createLineChart(
                null, null, "Total (Rp)", dataset,
                PlotOrientation.VERTICAL, false, true, false
        );
        
        chart.setBackgroundPaint(CARD_BG);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(INPUT_BG);
        plot.setDomainGridlinePaint(INPUT_BORDER);
        plot.setRangeGridlinePaint(INPUT_BORDER);
        
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, SUCCESS_COLOR);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        
        plot.getDomainAxis().setTickLabelPaint(TEXT_COLOR);
        plot.getDomainAxis().setTickLabelFont(FONT_SMALL);
        plot.getRangeAxis().setTickLabelPaint(TEXT_COLOR);
        plot.getRangeAxis().setTickLabelFont(FONT_SMALL);
        
        return chart;
    }
    
    public void refresh() {
        filterTransactions();
    }
    
    private void filterTransactions() {
        java.util.Date fromDate = (java.util.Date) dateFrom.getValue();
        java.util.Date toDate = (java.util.Date) dateTo.getValue();
        
        LocalDate from = new java.sql.Date(fromDate.getTime()).toLocalDate();
        LocalDate to = new java.sql.Date(toDate.getTime()).toLocalDate();
        
        List<Transaction> transactions = transactionDAO.findByDateRange(from, to);
        loadTransactions(transactions);
        
        // Update chart
        updateChart(from, to);
    }
    
    private void loadTransactions(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                t.getId(),
                t.getTransactionDate().format(formatter),
                t.getTotalItems(),
                CurrencyUtils.format(t.getSubtotal()),
                CurrencyUtils.format(t.getDiscountAmount()),
                CurrencyUtils.format(t.getTotal()),
                CurrencyUtils.format(t.getPayment()),
                CurrencyUtils.format(t.getChangeAmount())
            });
        }
    }
    
    private void showTransactionDetail() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            int transactionId = (int) tableModel.getValueAt(selectedRow, 0);
            Transaction transaction = transactionDAO.findById(transactionId);
            
            if (transaction != null) {
                itemsTableModel.setRowCount(0);
                for (TransactionItem item : transaction.getItems()) {
                    itemsTableModel.addRow(new Object[]{
                        item.getProductName(),
                        CurrencyUtils.format(item.getPrice()),
                        item.getQuantity(),
                        CurrencyUtils.format(item.getSubtotal())
                    });
                }
            }
        }
    }
    
    private void updateChart(LocalDate from, LocalDate to) {
        // Calculate days between
        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        Map<LocalDate, BigDecimal> dailySales = transactionDAO.getDailySales((int) days);
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        LocalDate current = from;
        while (!current.isAfter(to)) {
            BigDecimal sales = dailySales.getOrDefault(current, BigDecimal.ZERO);
            dataset.addValue(sales.doubleValue(), "Penjualan", current.format(formatter));
            current = current.plusDays(1);
        }
        
        JFreeChart chart = createLineChart(dataset);
        chartPanel.setChart(chart);
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export to CSV");
        fileChooser.setSelectedFile(new java.io.File("transactions_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                // Header
                writer.println("ID,Tanggal,Total Item,Subtotal,Diskon,Total,Bayar,Kembali");
                
                // Data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    StringBuilder line = new StringBuilder();
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        if (j > 0) line.append(",");
                        Object value = tableModel.getValueAt(i, j);
                        line.append("\"").append(value != null ? value.toString() : "").append("\"");
                    }
                    writer.println(line.toString());
                }
                
                JOptionPane.showMessageDialog(this, "Export berhasil!", 
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal export: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
