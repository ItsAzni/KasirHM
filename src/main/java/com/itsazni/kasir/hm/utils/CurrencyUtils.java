package com.itsazni.kasir.hm.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Currency formatting utility for Indonesian Rupiah
 */
public class CurrencyUtils {
    
    private static final Locale INDONESIA = new Locale("id", "ID");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(INDONESIA);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(INDONESIA);
    
    /**
     * Format BigDecimal as Indonesian Rupiah
     */
    public static String format(BigDecimal amount) {
        if (amount == null) return "Rp 0";
        return CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Format double as Indonesian Rupiah
     */
    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Format number without currency symbol
     */
    public static String formatNumber(BigDecimal amount) {
        if (amount == null) return "0";
        return NUMBER_FORMAT.format(amount);
    }
    
    /**
     * Parse string to BigDecimal (removes non-numeric characters)
     */
    public static BigDecimal parse(String text) {
        if (text == null || text.isEmpty()) return BigDecimal.ZERO;
        
        // Remove all non-numeric characters except decimal point
        String cleaned = text.replaceAll("[^\\d,.]", "");
        // Replace comma with dot for BigDecimal parsing
        cleaned = cleaned.replace(",", ".");
        
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
