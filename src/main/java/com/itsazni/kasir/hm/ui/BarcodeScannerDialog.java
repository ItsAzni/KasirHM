package com.itsazni.kasir.hm.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.itsazni.kasir.hm.utils.BarcodeScanner;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dialog for scanning barcodes using webcam with camera selection support
 */
public class BarcodeScannerDialog extends JDialog {
    
    private final Consumer<String> onBarcodeDetected;
    private BarcodeScanner scanner;
    private WebcamPanel webcamPanel;
    private JLabel lblStatus;
    private JComboBox<WebcamItem> cmbCamera;
    private JPanel cameraContainer;
    private Webcam currentWebcam;
    
    private static final Color BG_COLOR = new Color(24, 24, 37);
    private static final Color CARD_BG = new Color(30, 30, 46);
    private static final Color TEXT_COLOR = new Color(205, 214, 244);
    private static final Color PRIMARY_COLOR = new Color(99, 102, 241);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    
    // Store last selected camera index
    private static int lastSelectedCameraIndex = 0;
    
    public BarcodeScannerDialog(Frame parent, Consumer<String> callback) {
        super(parent, "Scan Barcode", true);
        this.onBarcodeDetected = callback;
        initComponents();
    }
    
    private void initComponents() {
        setSize(600, 550);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(CARD_BG);
        
        // Header with camera selection
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        JLabel lblTitle = new JLabel("Scan Barcode");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_COLOR);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(lblTitle);
        
        headerPanel.add(Box.createVerticalStrut(15));
        
        // Camera selection panel - Row 1: Label + Combo
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setBackground(CARD_BG);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JLabel lblCamera = new JLabel("Pilih Kamera:");
        lblCamera.setForeground(TEXT_COLOR);
        lblCamera.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCamera.setPreferredSize(new Dimension(90, 30));
        row1.add(lblCamera, BorderLayout.WEST);
        
        cmbCamera = new JComboBox<>();
        cmbCamera.setBackground(new Color(49, 50, 68));
        cmbCamera.setForeground(TEXT_COLOR);
        cmbCamera.setFont(new Font("SansSerif", Font.PLAIN, 12));
        row1.add(cmbCamera, BorderLayout.CENTER);
        
        headerPanel.add(row1);
        headerPanel.add(Box.createVerticalStrut(10));
        
        // Camera selection panel - Row 2: Buttons
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row2.setBackground(CARD_BG);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton btnRefresh = new JButton("Refresh Kamera");
        btnRefresh.setToolTipText("Refresh daftar kamera");
        btnRefresh.setBackground(new Color(49, 50, 68));
        btnRefresh.setForeground(TEXT_COLOR);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnRefresh.addActionListener(e -> refreshCameraList());
        row2.add(btnRefresh);
        
        row2.add(Box.createHorizontalStrut(10));
        
        JButton btnSwitch = new JButton(">>> Gunakan Kamera <<<");
        btnSwitch.setBackground(SUCCESS_COLOR);
        btnSwitch.setForeground(Color.WHITE);
        btnSwitch.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSwitch.setFocusPainted(false);
        btnSwitch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSwitch.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnSwitch.addActionListener(e -> switchCamera());
        row2.add(btnSwitch);
        
        headerPanel.add(row2);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Camera panel
        cameraContainer = new JPanel(new BorderLayout());
        cameraContainer.setBackground(BG_COLOR);
        cameraContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        
        // Show loading initially
        JLabel lblLoading = new JLabel("Memuat daftar kamera...", SwingConstants.CENTER);
        lblLoading.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblLoading.setForeground(TEXT_COLOR);
        cameraContainer.add(lblLoading, BorderLayout.CENTER);
        
        add(cameraContainer, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(CARD_BG);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        lblStatus = new JLabel("Pilih kamera dan klik 'Gunakan Kamera'");
        lblStatus.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblStatus.setForeground(new Color(166, 173, 200));
        footerPanel.add(lblStatus, BorderLayout.WEST);
        
        JButton btnCancel = new JButton("Batal");
        btnCancel.setBackground(new Color(69, 71, 90));
        btnCancel.setForeground(TEXT_COLOR);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCancel.addActionListener(e -> closeDialog());
        footerPanel.add(btnCancel, BorderLayout.EAST);
        
        add(footerPanel, BorderLayout.SOUTH);
        
        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeDialog();
            }
        });
        
        // Load camera list in background
        SwingWorker<List<Webcam>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Webcam> doInBackground() {
                return Webcam.getWebcams();
            }
            
            @Override
            protected void done() {
                try {
                    List<Webcam> webcams = get();
                    populateCameraList(webcams);
                } catch (Exception e) {
                    showError("Gagal memuat daftar kamera: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void populateCameraList(List<Webcam> webcams) {
        cmbCamera.removeAllItems();
        
        if (webcams.isEmpty()) {
            showError("Tidak ada kamera yang terdeteksi!");
            return;
        }
        
        int index = 0;
        for (Webcam webcam : webcams) {
            String name = webcam.getName();
            // Make USB/external cameras more visible
            String displayName;
            if (name.toLowerCase().contains("usb") || 
                name.toLowerCase().contains("android") ||
                name.toLowerCase().contains("droidcam") ||
                name.toLowerCase().contains("ip webcam")) {
                displayName = "📱 " + name + " (USB/External)";
            } else if (name.toLowerCase().contains("integrated") || 
                       name.toLowerCase().contains("built-in") ||
                       name.toLowerCase().contains("laptop")) {
                displayName = "💻 " + name + " (Built-in)";
            } else {
                displayName = "📷 " + name;
            }
            cmbCamera.addItem(new WebcamItem(webcam, displayName, index));
            index++;
        }
        
        // Restore last selected camera
        if (lastSelectedCameraIndex < cmbCamera.getItemCount()) {
            cmbCamera.setSelectedIndex(lastSelectedCameraIndex);
        }
        
        // Update UI
        cameraContainer.removeAll();
        JLabel lblReady = new JLabel("Kamera siap. Klik 'Gunakan Kamera' untuk mulai scan.", SwingConstants.CENTER);
        lblReady.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblReady.setForeground(TEXT_COLOR);
        cameraContainer.add(lblReady, BorderLayout.CENTER);
        cameraContainer.revalidate();
        cameraContainer.repaint();
        
        lblStatus.setText("Ditemukan " + webcams.size() + " kamera");
    }
    
    private void refreshCameraList() {
        lblStatus.setText("Memuat ulang daftar kamera...");
        cmbCamera.removeAllItems();
        
        SwingWorker<List<Webcam>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Webcam> doInBackground() {
                // Close current webcam first
                if (currentWebcam != null && currentWebcam.isOpen()) {
                    currentWebcam.close();
                }
                // Force refresh webcam list
                Webcam.resetDriver();
                return Webcam.getWebcams();
            }
            
            @Override
            protected void done() {
                try {
                    List<Webcam> webcams = get();
                    populateCameraList(webcams);
                } catch (Exception e) {
                    showError("Gagal memuat daftar kamera: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void switchCamera() {
        WebcamItem selectedItem = (WebcamItem) cmbCamera.getSelectedItem();
        if (selectedItem == null) {
            showError("Silakan pilih kamera terlebih dahulu!");
            return;
        }
        
        // Save selected index
        lastSelectedCameraIndex = selectedItem.index;
        
        // Stop current scanner if running
        if (scanner != null) {
            scanner.close();
        }
        
        // Close current webcam
        if (currentWebcam != null && currentWebcam.isOpen()) {
            currentWebcam.close();
        }
        
        lblStatus.setText("Membuka kamera...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                currentWebcam = selectedItem.webcam;
                
                // Set highest resolution available
                if (currentWebcam.getViewSizes().length > 0) {
                    currentWebcam.setViewSize(currentWebcam.getViewSizes()[currentWebcam.getViewSizes().length - 1]);
                }
                
                currentWebcam.open();
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    startCameraPreview();
                } catch (Exception e) {
                    showError("Gagal membuka kamera: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void startCameraPreview() {
        // Remove old content
        cameraContainer.removeAll();
        
        // Create webcam panel
        webcamPanel = new WebcamPanel(currentWebcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setDisplayDebugInfo(false);
        webcamPanel.setMirrored(true);
        webcamPanel.setFillArea(true);
        
        cameraContainer.add(webcamPanel, BorderLayout.CENTER);
        cameraContainer.revalidate();
        cameraContainer.repaint();
        
        // Start barcode scanning
        scanner = new BarcodeScanner(currentWebcam);
        scanner.startScanning(barcode -> {
            SwingUtilities.invokeLater(() -> {
                // Play beep sound
                Toolkit.getDefaultToolkit().beep();
                
                // Close and return barcode
                closeDialog();
                onBarcodeDetected.accept(barcode);
            });
        });
        
        lblStatus.setText("🔍 Arahkan barcode ke kamera...");
        lblStatus.setForeground(SUCCESS_COLOR);
    }
    
    private void showError(String message) {
        cameraContainer.removeAll();
        JLabel lblError = new JLabel(message, SwingConstants.CENTER);
        lblError.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblError.setForeground(new Color(239, 68, 68));
        cameraContainer.add(lblError, BorderLayout.CENTER);
        cameraContainer.revalidate();
        cameraContainer.repaint();
        
        lblStatus.setText("Error!");
        lblStatus.setForeground(new Color(239, 68, 68));
    }
    
    private void closeDialog() {
        if (scanner != null) {
            scanner.close();
        }
        if (currentWebcam != null && currentWebcam.isOpen()) {
            currentWebcam.close();
        }
        dispose();
    }
    
    /**
     * Helper class to hold webcam info in combo box
     */
    private static class WebcamItem {
        final Webcam webcam;
        final String displayName;
        final int index;
        
        WebcamItem(Webcam webcam, String displayName, int index) {
            this.webcam = webcam;
            this.displayName = displayName;
            this.index = index;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}
