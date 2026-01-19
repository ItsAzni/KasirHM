package com.itsazni.kasir.hm.utils;

import com.itsazni.kasir.hm.models.Transaction;
import com.itsazni.kasir.hm.models.TransactionItem;
import java.awt.print.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;

/**
 * Utility class for printing receipts
 */
public class ReceiptPrinter implements Printable {
    
    private final Transaction transaction;
    private final String cashierName;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private static final int RECEIPT_WIDTH = 280;
    private static final int LINE_HEIGHT = 14;
    
    public ReceiptPrinter(Transaction transaction, String cashierName) {
        this.transaction = transaction;
        this.cashierName = cashierName;
    }
    
    /**
     * Print the receipt
     */
    public void print() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        
        // Set page format for receipt printer
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = new Paper();
        paper.setSize(RECEIPT_WIDTH, 1000);
        paper.setImageableArea(5, 5, RECEIPT_WIDTH - 10, 990);
        pageFormat.setPaper(paper);
        
        printerJob.setPrintable(this, pageFormat);
        
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, "Error printing: " + e.getMessage(), 
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Show receipt preview in a dialog
     */
    public void showPreview(JFrame parent) {
        String receipt = generateReceiptText();
        
        JTextArea textArea = new JTextArea(receipt);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(320, 400));
        
        int option = JOptionPane.showOptionDialog(parent, scrollPane, "Receipt Preview",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Print", "Close"}, "Close");
        
        if (option == JOptionPane.YES_OPTION) {
            print();
        }
    }
    
    /**
     * Generate receipt as text
     */
    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        String divider = "================================";
        String dividerThin = "--------------------------------";
        
        // Header
        sb.append(center("KASIR HM", 32)).append("\n");
        sb.append(center("Toko Kelontong", 32)).append("\n");
        sb.append(center("JL. Contoh No. 123", 32)).append("\n");
        sb.append(divider).append("\n");
        
        // Transaction info
        sb.append(String.format("No: TRX-%05d\n", transaction.getId()));
        sb.append(String.format("Tgl: %s\n", transaction.getTransactionDate().format(DATE_FORMAT)));
        sb.append(String.format("Kasir: %s\n", cashierName));
        sb.append(dividerThin).append("\n");
        
        // Items
        for (TransactionItem item : transaction.getItems()) {
            sb.append(truncate(item.getProductName(), 22)).append("\n");
            sb.append(String.format("  %d x %s = %s\n",
                    item.getQuantity(),
                    formatCurrency(item.getPrice()),
                    formatCurrency(item.getSubtotal())));
        }
        
        sb.append(dividerThin).append("\n");
        
        // Totals
        sb.append(rightAlign("Subtotal:", formatCurrency(transaction.getSubtotal()), 32)).append("\n");
        
        if (transaction.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(rightAlign("Diskon (" + transaction.getDiscountPercent() + "%):", 
                    "-" + formatCurrency(transaction.getDiscountAmount()), 32)).append("\n");
        }
        
        sb.append(divider).append("\n");
        sb.append(rightAlign("TOTAL:", formatCurrency(transaction.getTotal()), 32)).append("\n");
        sb.append(rightAlign("Bayar:", formatCurrency(transaction.getPayment()), 32)).append("\n");
        sb.append(rightAlign("Kembali:", formatCurrency(transaction.getChangeAmount()), 32)).append("\n");
        sb.append(divider).append("\n");
        
        // Footer
        sb.append(center("Terima Kasih", 32)).append("\n");
        sb.append(center("Selamat Berbelanja Kembali", 32)).append("\n");
        
        return sb.toString();
    }
    
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        
        Font font = new Font("Monospaced", Font.PLAIN, 9);
        g2d.setFont(font);
        
        String[] lines = generateReceiptText().split("\n");
        int y = LINE_HEIGHT;
        
        for (String line : lines) {
            g2d.drawString(line, 0, y);
            y += LINE_HEIGHT;
        }
        
        return PAGE_EXISTS;
    }
    
    // Helper methods
    private String center(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
    
    private String rightAlign(String label, String value, int width) {
        int space = width - label.length() - value.length();
        if (space < 1) space = 1;
        return label + " ".repeat(space) + value;
    }
    
    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }
    
    private String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount).replace("Rp", "Rp ");
    }
}
