package com.itsazni.kasir.hm.dao;

import com.itsazni.kasir.hm.models.Transaction;
import com.itsazni.kasir.hm.models.TransactionItem;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Transaction operations
 */
public class TransactionDAO {
    
    private final DatabaseConnection dbConnection;
    private final ProductDAO productDAO;
    
    public TransactionDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.productDAO = new ProductDAO();
    }
    
    /**
     * Save transaction with all items
     */
    public boolean save(Transaction transaction) {
        String transactionSql = "INSERT INTO transactions (user_id, subtotal, discount_percent, discount_amount, total, payment, change_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO transaction_items (transaction_id, product_id, product_name, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert transaction
            PreparedStatement transactionStmt = conn.prepareStatement(transactionSql, Statement.RETURN_GENERATED_KEYS);
            transactionStmt.setInt(1, transaction.getUserId());
            transactionStmt.setBigDecimal(2, transaction.getSubtotal());
            transactionStmt.setBigDecimal(3, transaction.getDiscountPercent());
            transactionStmt.setBigDecimal(4, transaction.getDiscountAmount());
            transactionStmt.setBigDecimal(5, transaction.getTotal());
            transactionStmt.setBigDecimal(6, transaction.getPayment());
            transactionStmt.setBigDecimal(7, transaction.getChangeAmount());
            
            int affected = transactionStmt.executeUpdate();
            if (affected == 0) {
                conn.rollback();
                return false;
            }
            
            ResultSet keys = transactionStmt.getGeneratedKeys();
            if (keys.next()) {
                transaction.setId(keys.getInt(1));
            }
            
            // Insert items and update stock
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            for (TransactionItem item : transaction.getItems()) {
                itemStmt.setInt(1, transaction.getId());
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setString(3, item.getProductName());
                itemStmt.setInt(4, item.getQuantity());
                itemStmt.setBigDecimal(5, item.getPrice());
                itemStmt.setBigDecimal(6, item.getSubtotal());
                itemStmt.addBatch();
                
                // Update stock
                String stockSql = "UPDATE products SET stock = stock - ? WHERE id = ?";
                PreparedStatement stockStmt = conn.prepareStatement(stockSql);
                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.executeUpdate();
            }
            
            itemStmt.executeBatch();
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto commit: " + e.getMessage());
                }
            }
        }
        return false;
    }
    
    /**
     * Find transaction by ID
     */
    public Transaction findById(int id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs);
                transaction.setItems(findItemsByTransactionId(id));
                return transaction;
            }
        } catch (SQLException e) {
            System.err.println("Error finding transaction: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get all transactions
     */
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return transactions;
    }
    
    /**
     * Get transactions by date range
     */
    public List<Transaction> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE DATE(transaction_date) BETWEEN ? AND ? ORDER BY transaction_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions by date: " + e.getMessage());
        }
        return transactions;
    }
    
    /**
     * Get transactions for today
     */
    public List<Transaction> findToday() {
        LocalDate today = LocalDate.now();
        return findByDateRange(today, today);
    }
    
    /**
     * Get today's total sales
     */
    public BigDecimal getTodaySales() {
        String sql = "SELECT COALESCE(SUM(total), 0) as total_sales FROM transactions WHERE DATE(transaction_date) = CURDATE()";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBigDecimal("total_sales");
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's sales: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Get today's transaction count
     */
    public int getTodayTransactionCount() {
        String sql = "SELECT COUNT(*) as count FROM transactions WHERE DATE(transaction_date) = CURDATE()";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get daily sales for the past n days
     */
    public Map<LocalDate, BigDecimal> getDailySales(int days) {
        Map<LocalDate, BigDecimal> dailySales = new HashMap<>();
        String sql = "SELECT DATE(transaction_date) as sale_date, SUM(total) as daily_total " +
                     "FROM transactions " +
                     "WHERE transaction_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "GROUP BY DATE(transaction_date) " +
                     "ORDER BY sale_date";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, days);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                LocalDate date = rs.getDate("sale_date").toLocalDate();
                BigDecimal total = rs.getBigDecimal("daily_total");
                dailySales.put(date, total);
            }
        } catch (SQLException e) {
            System.err.println("Error getting daily sales: " + e.getMessage());
        }
        return dailySales;
    }
    
    /**
     * Get items for a transaction
     */
    public List<TransactionItem> findItemsByTransactionId(int transactionId) {
        List<TransactionItem> items = new ArrayList<>();
        String sql = "SELECT * FROM transaction_items WHERE transaction_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                TransactionItem item = new TransactionItem();
                item.setId(rs.getInt("id"));
                item.setTransactionId(rs.getInt("transaction_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setSubtotal(rs.getBigDecimal("subtotal"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transaction items: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Map ResultSet to Transaction object
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setUserId(rs.getInt("user_id"));
        
        Timestamp date = rs.getTimestamp("transaction_date");
        if (date != null) {
            transaction.setTransactionDate(date.toLocalDateTime());
        }
        
        transaction.setSubtotal(rs.getBigDecimal("subtotal"));
        transaction.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        transaction.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        transaction.setTotal(rs.getBigDecimal("total"));
        transaction.setPayment(rs.getBigDecimal("payment"));
        transaction.setChangeAmount(rs.getBigDecimal("change_amount"));
        
        return transaction;
    }
}
