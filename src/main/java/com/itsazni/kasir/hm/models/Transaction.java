package com.itsazni.kasir.hm.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction model representing a complete sales transaction
 */
public class Transaction {
    private int id;
    private int userId;
    private LocalDateTime transactionDate;
    private BigDecimal subtotal;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private BigDecimal payment;
    private BigDecimal changeAmount;
    private List<TransactionItem> items;

    public Transaction() {
        this.transactionDate = LocalDateTime.now();
        this.subtotal = BigDecimal.ZERO;
        this.discountPercent = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.payment = BigDecimal.ZERO;
        this.changeAmount = BigDecimal.ZERO;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { 
        this.discountPercent = discountPercent;
        calculateTotals();
    }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getPayment() { return payment; }
    public void setPayment(BigDecimal payment) { 
        this.payment = payment;
        calculateChange();
    }

    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }

    public List<TransactionItem> getItems() { return items; }
    public void setItems(List<TransactionItem> items) { 
        this.items = items;
        calculateTotals();
    }

    /**
     * Add item to transaction
     */
    public void addItem(TransactionItem item) {
        // Check if product already exists in cart
        for (TransactionItem existing : items) {
            if (existing.getProductId() == item.getProductId()) {
                existing.addQuantity(item.getQuantity());
                calculateTotals();
                return;
            }
        }
        items.add(item);
        calculateTotals();
    }

    /**
     * Remove item from transaction
     */
    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            calculateTotals();
        }
    }

    /**
     * Clear all items
     */
    public void clearItems() {
        items.clear();
        calculateTotals();
    }

    /**
     * Calculate subtotal, discount, and total
     */
    public void calculateTotals() {
        // Calculate subtotal
        subtotal = BigDecimal.ZERO;
        for (TransactionItem item : items) {
            subtotal = subtotal.add(item.getSubtotal());
        }

        // Calculate discount amount
        if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = BigDecimal.ZERO;
        }

        // Calculate total
        total = subtotal.subtract(discountAmount);

        // Recalculate change if payment was already made
        if (payment.compareTo(BigDecimal.ZERO) > 0) {
            calculateChange();
        }
    }

    /**
     * Calculate change amount
     */
    public void calculateChange() {
        changeAmount = payment.subtract(total);
    }

    /**
     * Get total number of items
     */
    public int getTotalItems() {
        int count = 0;
        for (TransactionItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }
}
