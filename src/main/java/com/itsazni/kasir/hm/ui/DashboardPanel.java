package com.itsazni.kasir.hm.ui;

import com.itsazni.kasir.hm.dao.ProductDAO;
import com.itsazni.kasir.hm.dao.TransactionDAO;
import com.itsazni.kasir.hm.models.Product;
import com.itsazni.kasir.hm.models.User;
import com.itsazni.kasir.hm.utils.CurrencyUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.itsazni.kasir.hm.ui.UIConstants.*;

/**
 * Dashboard panel showing sales summary and low stock alerts - Modern design
 */
public class DashboardPanel extends JPanel {
    
    private final User currentUser;
    private final TransactionDAO transactionDAO;
    private final ProductDAO productDAO;
    
    // Stats labels
    private JLabel lblTodaySales;
    private JLabel lblTodayTransactions;
    private JLabel lblTotalProducts;
    private JLabel lblLowStockCount;
    
    // Low stock table
    private JTable lowStockTable;
    
    // Chart panel
    private ChartPanel chartPanel;
    
    public DashboardPanel(User user) {
        this.currentUser = user;
        this.transactionDAO = new TransactionDAO();
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
        
        JLabel lblTitle = createLabel("Dashboard", FONT_TITLE, TEXT_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        JLabel lblDate = createLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")), FONT_BODY, TEXT_SECONDARY);
        headerPanel.add(lblDate, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BG_COLOR);
        
        // Stats cards
        JPanel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        
        // Bottom section with chart and low stock
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.setBackground(BG_COLOR);
        
        // Sales chart
        JPanel chartCard = createChartPanel();
        bottomPanel.add(chartCard);
        
        // Low stock alert
        JPanel lowStockCard = createLowStockPanel();
        bottomPanel.add(lowStockCard);
        
        contentPanel.add(bottomPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
        
        refresh();
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(BG_COLOR);
        panel.setPreferredSize(new Dimension(0, 130));
        
        // Today's sales
        lblTodaySales = createLabel("Rp 0", FONT_TITLE, TEXT_COLOR);
        panel.add(createStatCard("💰 Penjualan Hari Ini", lblTodaySales, SUCCESS_COLOR));
        
        // Today's transactions
        lblTodayTransactions = createLabel("0", FONT_TITLE, TEXT_COLOR);
        panel.add(createStatCard("🧾 Transaksi Hari Ini", lblTodayTransactions, PRIMARY_COLOR));
        
        // Total products
        lblTotalProducts = createLabel("0", FONT_TITLE, TEXT_COLOR);
        panel.add(createStatCard("📦 Total Produk", lblTotalProducts, new Color(168, 85, 247)));
        
        // Low stock count
        lblLowStockCount = createLabel("0", FONT_TITLE, TEXT_COLOR);
        panel.add(createStatCard("⚠️ Stok Menipis", lblLowStockCount, WARNING_COLOR));
        
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = createAccentCard(accentColor);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        
        JLabel lblTitle = createLabel(title, FONT_SMALL, TEXT_SECONDARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTitle);
        
        card.add(Box.createVerticalStrut(12));
        
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valueLabel);
        
        return card;
    }
    
    private JPanel createChartPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 12));
        
        JLabel lblTitle = createLabel("📈 Penjualan 7 Hari Terakhir", FONT_BODY_BOLD, TEXT_COLOR);
        card.add(lblTitle, BorderLayout.NORTH);
        
        // Create empty chart initially
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = createBarChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(CARD_BG);
        card.add(chartPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JFreeChart createBarChart(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                null, null, "Total (Rp)", dataset,
                PlotOrientation.VERTICAL, false, true, false
        );
        
        chart.setBackgroundPaint(CARD_BG);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(INPUT_BG);
        plot.setDomainGridlinePaint(INPUT_BORDER);
        plot.setRangeGridlinePaint(INPUT_BORDER);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR);
        
        plot.getDomainAxis().setTickLabelPaint(TEXT_COLOR);
        plot.getRangeAxis().setTickLabelPaint(TEXT_COLOR);
        
        return chart;
    }
    
    private JPanel createLowStockPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 12));
        
        JLabel lblTitle = createLabel("⚠️ Peringatan Stok Menipis", FONT_BODY_BOLD, TEXT_COLOR);
        card.add(lblTitle, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Produk", "Stok", "Min"};
        Object[][] data = {};
        lowStockTable = new JTable(data, columns);
        styleTable(lowStockTable);
        
        JScrollPane scrollPane = createScrollPane(lowStockTable);
        card.add(scrollPane, BorderLayout.CENTER);
        
        return card;
    }
    
    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private BigDecimal todaySales;
            private int todayTransactions;
            private int totalProducts;
            private List<Product> lowStockProducts;
            private Map<LocalDate, BigDecimal> dailySales;
            
            @Override
            protected Void doInBackground() {
                todaySales = transactionDAO.getTodaySales();
                todayTransactions = transactionDAO.getTodayTransactionCount();
                totalProducts = productDAO.findAll().size();
                lowStockProducts = productDAO.findLowStock();
                dailySales = transactionDAO.getDailySales(7);
                return null;
            }
            
            @Override
            protected void done() {
                // Update stats
                lblTodaySales.setText(CurrencyUtils.format(todaySales));
                lblTodayTransactions.setText(String.valueOf(todayTransactions));
                lblTotalProducts.setText(String.valueOf(totalProducts));
                lblLowStockCount.setText(String.valueOf(lowStockProducts.size()));
                
                // Update low stock color
                if (lowStockProducts.size() > 0) {
                    lblLowStockCount.setForeground(DANGER_COLOR);
                } else {
                    lblLowStockCount.setForeground(TEXT_COLOR);
                }
                
                // Update low stock table
                String[] columns = {"Produk", "Stok", "Min"};
                Object[][] data = new Object[lowStockProducts.size()][3];
                for (int i = 0; i < lowStockProducts.size(); i++) {
                    Product p = lowStockProducts.get(i);
                    data[i] = new Object[]{p.getName(), p.getStock(), p.getMinStock()};
                }
                lowStockTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
                
                // Update chart
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                
                // Fill in missing days with 0
                LocalDate today = LocalDate.now();
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    BigDecimal sales = dailySales.getOrDefault(date, BigDecimal.ZERO);
                    dataset.addValue(sales.doubleValue(), "Penjualan", date.format(formatter));
                }
                
                JFreeChart chart = createBarChart(dataset);
                chartPanel.setChart(chart);
            }
        };
        worker.execute();
    }
}
