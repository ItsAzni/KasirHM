package com.itsazni.kasir.hm.models;

import java.math.BigDecimal;

/**
 * TransactionItem model representing a single item in a transaction
 */
public class TransactionItem {
    private int id;
    private int transactionId;
    private int productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public TransactionItem() {
        this.quantity = 1;
        this.price = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public TransactionItem(Product product, int quantity) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.quantity = quantity;
        this.price = product.getPrice();
        calculateSubtotal();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        calculateSubtotal();
    }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { 
        this.price = price;
        calculateSubtotal();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    /**
     * Calculate subtotal based on quantity and price
     */
    public void calculateSubtotal() {
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Add quantity to existing item
     */
    public void addQuantity(int qty) {
        this.quantity += qty;
        calculateSubtotal();
    }
}
