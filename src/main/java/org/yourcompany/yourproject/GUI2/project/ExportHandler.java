
package org.yourcompany.yourproject.GUI2.project;

import org.yourcompany.yourproject.GUI2.ui.CircuitPanel;
import org.yourcompany.yourproject.GUI2.ui.MainUI;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

public class ExportHandler {
    
    public static void exportCircuit(String circuitName, CircuitPanel circuitPanel) {
        if (circuitPanel == null) {
            JOptionPane.showMessageDialog(null, "Circuit panel not provided!");
            return;
        }
        
        // Check if there's a circuit currently displayed
        if (circuitPanel.getCurrentCircuit() == null) {
            JOptionPane.showMessageDialog(null, 
                "No circuit is currently open in the design area!\n" +
                "Please open a circuit first.", 
                "No Circuit", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Circuit as Image - " + circuitName);
        
        // Set default file name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultFileName = circuitName + "_" + timestamp;
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        // Filter for image formats
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "PNG Images (*.png)", "png"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "JPEG Images (*.jpg)", "jpg"));
        
        // Set PNG as default (faster than JPEG for circuit diagrams)
        fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[0]);
        
        int userSelection = fileChooser.showSaveDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String format = getFormatFromFileFilter(fileChooser.getFileFilter());
            
            // Ensure file extension
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith("." + format.toLowerCase())) {
                fileToSave = new File(filePath + "." + format);
            }
            
            // Check if file exists
            if (fileToSave.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(null,
                    "File already exists. Overwrite?",
                    "File Exists",
                    JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) return;
            }
            
            // Perform export in background thread
            performExportInBackground(circuitPanel, fileToSave, format, circuitName);
        }
    }
    
    private static void performExportInBackground(CircuitPanel circuitPanel, File file, String format, String circuitName) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return captureCircuitPanelOptimized(circuitPanel, file, format);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(null,
                            "Circuit '" + circuitName + "' exported successfully!\n" +
                            "Location: " + file.getAbsolutePath(),
                            "Export Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                            "Failed to export circuit!",
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                        "Error during export: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private static boolean captureCircuitPanelOptimized(CircuitPanel circuitPanel, File file, String format) {
        try {
            // OPTIMIZATION 1: Calculate the actual bounds of circuit components
            Rectangle contentBounds = calculateContentBounds(circuitPanel);
            
            // Add some padding around the content
            int padding = 50;
            int width = Math.max(contentBounds.width + padding * 2, 800);
            int height = Math.max(contentBounds.height + padding * 2, 600);
            
            // OPTIMIZATION 2: Create image with optimal size
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // OPTIMIZATION 3: Set rendering hints for speed vs quality balance
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            
            // White background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            
            // OPTIMIZATION 4: Translate to center the content with padding
            g2d.translate(padding - contentBounds.x, padding - contentBounds.y);
            
            // OPTIMIZATION 5: Paint only the circuit content (not the entire panel)
            paintCircuitContent(circuitPanel, g2d);
            
            g2d.dispose();
            
            // OPTIMIZATION 6: Use faster compression for JPEG
            if ("jpg".equals(format)) {
                return ImageIO.write(image, "JPEG", file);
            } else {
                return ImageIO.write(image, format.toUpperCase(), file);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Calculate the bounds of actual circuit content (gates and wires)
     */
    private static Rectangle calculateContentBounds(CircuitPanel circuitPanel) {
        Rectangle bounds = new Rectangle();
        
        // Get all gate components from the circuit panel
        java.awt.Component[] components = circuitPanel.getComponents();
        boolean first = true;
        
        for (java.awt.Component comp : components) {
            if (comp.isVisible() && comp.getBounds() != null && !comp.getBounds().isEmpty()) {
                Rectangle compBounds = comp.getBounds();
                if (first) {
                    bounds.setBounds(compBounds);
                    first = false;
                } else {
                    bounds.add(compBounds);
                }
            }
        }
        
        // If no components found, use default size
        if (first) {
            bounds.setBounds(0, 0, 800, 600);
        }
        
        // Ensure minimum size
        if (bounds.width < 400) bounds.width = 400;
        if (bounds.height < 300) bounds.height = 300;
        
        return bounds;
    }
    
    /**
     * Paint only the circuit content, not the entire panel background
     */
    private static void paintCircuitContent(CircuitPanel circuitPanel, Graphics2D g2d) {
        // Paint grid background
        paintGridBackground(g2d, circuitPanel.getWidth(), circuitPanel.getHeight());
        
        // Paint all components
        circuitPanel.paint(g2d);
    }
    
    /**
     * Paint a simplified grid background
     */
    private static void paintGridBackground(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(240, 240, 240)); // Light gray grid
        
        int gridSize = 20;
        for (int x = 0; x < width; x += gridSize) {
            g2d.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += gridSize) {
            g2d.drawLine(0, y, width, y);
        }
    }
    
    private static String getFormatFromFileFilter(javax.swing.filechooser.FileFilter fileFilter) {
        String description = fileFilter.getDescription().toLowerCase();
        if (description.contains("jpeg") || description.contains("jpg")) return "jpg";
        if (description.contains("png")) return "png";
        return "png"; // default
    }
}