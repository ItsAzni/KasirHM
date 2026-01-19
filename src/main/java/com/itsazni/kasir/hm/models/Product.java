package com.itsazni.kasir.hm.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product model class representing items in the store inventory
 */
public class Product {
    private int id;
    private String barcode;
    private String name;
    private String category;
    private BigDecimal price;
    private int stock;
    private int minStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {
        this.price = BigDecimal.ZERO;
        this.stock = 0;
        this.minStock = 5;
    }

    public Product(String barcode, String name, String category, BigDecimal price, int stock, int minStock) {
        this.barcode = barcode;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.minStock = minStock;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Check if stock is below minimum threshold
     */
    public boolean isLowStock() {
        return stock <= minStock;
    }

    @Override
    public String toString() {
        return name + " - Rp " + price.toString();
    }
}
