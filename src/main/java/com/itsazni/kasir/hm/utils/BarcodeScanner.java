package com.itsazni.kasir.hm.utils;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Barcode scanner using webcam and ZXing library
 */
public class BarcodeScanner {
    
    private Webcam webcam;
    private Thread scanThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final MultiFormatReader reader;
    
    /**
     * Create scanner with default webcam
     */
    public BarcodeScanner() {
        reader = new MultiFormatReader();
    }
    
    /**
     * Create scanner with specific webcam
     */
    public BarcodeScanner(Webcam webcam) {
        this.webcam = webcam;
        reader = new MultiFormatReader();
    }
    
    /**
     * Initialize with default webcam
     */
    public boolean initialize() {
        try {
            webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(webcam.getViewSizes()[webcam.getViewSizes().length - 1]);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize webcam: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get the webcam instance
     */
    public Webcam getWebcam() {
        return webcam;
    }
    
    /**
     * Set webcam to use
     */
    public void setWebcam(Webcam webcam) {
        this.webcam = webcam;
    }
    
    /**
     * Start scanning for barcodes
     */
    public void startScanning(Consumer<String> onBarcodeFound) {
        if (webcam == null || running.get()) return;
        
        if (!webcam.isOpen()) {
            webcam.open();
        }
        
        running.set(true);
        
        scanThread = new Thread(() -> {
            while (running.get()) {
                try {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        String barcode = decodeBarcode(image);
                        if (barcode != null) {
                            onBarcodeFound.accept(barcode);
                            Thread.sleep(2000);
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Ignore frame capture errors
                }
            }
        });
        scanThread.setDaemon(true);
        scanThread.start();
    }
    
    /**
     * Stop scanning
     */
    public void stopScanning() {
        running.set(false);
        if (scanThread != null) {
            scanThread.interrupt();
        }
    }
    
    /**
     * Close webcam
     */
    public void close() {
        stopScanning();
        // Don't close webcam here - let the dialog manage it
    }
    
    /**
     * Decode barcode from image
     */
    private String decodeBarcode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = reader.decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            // No barcode found in image
            return null;
        }
    }
    
    /**
     * Decode barcode from image (static method)
     */
    public static String decode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        }
    }
    
    /**
     * Check if webcam is available
     */
    public static boolean isWebcamAvailable() {
        try {
            return Webcam.getDefault() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get list of all available webcams
     */
    public static java.util.List<Webcam> getAvailableWebcams() {
        try {
            return Webcam.getWebcams();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
