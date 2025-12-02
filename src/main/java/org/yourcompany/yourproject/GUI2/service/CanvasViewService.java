package org.yourcompany.yourproject.GUI2.service;

import javax.swing.*;

import org.yourcompany.yourproject.GUI2.ui.ConnectorUI;
import org.yourcompany.yourproject.GUI2.ui.DraggableGate;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.yourcompany.yourproject.GUI2.ui.ConnectorUI;
import org.yourcompany.yourproject.GUI2.ui.DraggableGate;

/**
 * View Service for managing the canvas view and visual representation
 * Handles rendering, gate selection, visual feedback, and input/output display
 */
public class CanvasViewService {

    private JPanel canvas;
    private List<DraggableGate> gateViews;
    private List<ConnectorUI> wireViews;
    private DraggableGate selectedGate;
    private DraggableGate sourceGateForConnection;
    private int sourcePortForConnection;
    private Point tempWireEnd;
    private boolean isConnecting;
    private GateSelectionListener gateSelectionListener;

    // UI colors matching the current scheme
    private static final Color CANVAS_BG = new Color(245, 250, 255); // Very light blue-white
    private static final Color BORDER_COLOR = new Color(70, 130, 180); // Steel blue
    private static final Color SELECTION_COLOR = new Color(255, 215, 0); // Gold for selection

    public CanvasViewService(JPanel canvas) {
        this.canvas = canvas;
        this.gateViews = new ArrayList<>();
        this.wireViews = new ArrayList<>();
        this.selectedGate = null;
        this.isConnecting = false;
        initializeCanvas();
    }

    /**
     * Initialize the canvas with proper styling
     */
    private void initializeCanvas() {
        canvas.setLayout(null);
        canvas.setBackground(CANVAS_BG);
        canvas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                "Circuit Design Area",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13),
                BORDER_COLOR));
    }

    /**
     * Add a gate to the canvas view
     */
    public DraggableGate addGateView(ComponentBase gate, int x, int y) {
        if (gate == null)
            return null;

        DraggableGate gateView = new DraggableGate(gate);
        gateView.setLocation(x, y);
        canvas.add(gateView);
        gateViews.add(gateView);

        // Set z-order to ensure gates are above wires
        canvas.setComponentZOrder(gateView, 0);
        for (ConnectorUI wire : wireViews) {
            canvas.setComponentZOrder(wire, wireViews.size());
        }

        canvas.revalidate();
        canvas.repaint();
        return gateView;
    }

    /**
     * Remove a gate from the canvas view
     */
    public void removeGateView(DraggableGate gateView) {
        if (gateView != null) {
            canvas.remove(gateView);
            gateViews.remove(gateView);
            if (selectedGate == gateView) {
                selectedGate = null;
            }
            canvas.revalidate();
            canvas.repaint();
        }
    }

    /**
     * Select a gate (visual feedback)
     */
    public void selectGate(DraggableGate gate) {
        // Deselect previous
        if (selectedGate != null) {
            selectedGate.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 2),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }

        // Select new
        selectedGate = gate;
        if (gate != null) {
            gate.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SELECTION_COLOR, 3),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }

        if (gateSelectionListener != null) {
            ComponentBase component = gate != null ? gate.getGate() : null;
            gateSelectionListener.onGateSelected(component);
        }
        canvas.repaint();
    }

    /**
     * Get the currently selected gate
     */
    public DraggableGate getSelectedGate() {
        return selectedGate;
    }

    /**
     * Update wire connections in the view
     */
    public void updateWireViews(List<Connector> connectors) {
        // Remove old wire views
        for (ConnectorUI wireView : wireViews) {
            canvas.remove(wireView);
        }
        wireViews.clear();

        // Add new wire views
        for (Connector connector : connectors) {
            ConnectorUI wireView = new ConnectorUI(connector);
            wireView.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.add(wireView);
            wireViews.add(wireView);
        }

        // Ensure wires are behind gates
        for (ConnectorUI wireView : wireViews) {
            canvas.setComponentZOrder(wireView, wireViews.size());
        }
        for (DraggableGate gateView : gateViews) {
            canvas.setComponentZOrder(gateView, 0);
        }

        canvas.revalidate();
        canvas.repaint();
    }

    /**
     * Start connection mode (dragging from output port)
     */
    public void startConnection(DraggableGate sourceGate, int sourcePort, Point startPoint) {
        this.sourceGateForConnection = sourceGate;
        this.sourcePortForConnection = sourcePort;
        this.tempWireEnd = startPoint;
        this.isConnecting = true;
        canvas.repaint();
    }

    /**
     * Update temporary wire end during connection
     */
    public void updateConnectionEnd(Point endPoint) {
        if (isConnecting) {
            this.tempWireEnd = endPoint;
            canvas.repaint();
        }
    }

    /**
     * End connection mode
     */
    public void endConnection() {
        this.isConnecting = false;
        this.sourceGateForConnection = null;
        this.sourcePortForConnection = -1;
        this.tempWireEnd = null;
        canvas.repaint();
    }

    /**
     * Check if currently in connection mode
     */
    public boolean isConnecting() {
        return isConnecting;
    }

    /**
     * Get source gate for connection
     */
    public DraggableGate getSourceGateForConnection() {
        return sourceGateForConnection;
    }

    /**
     * Get source port for connection
     */
    public int getSourcePortForConnection() {
        return sourcePortForConnection;
    }

    /**
     * Paint temporary connection wire
     */
    public void paintTemporaryConnection(Graphics g) {
        if (isConnecting && sourceGateForConnection != null && tempWireEnd != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Point sourcePoint = sourceGateForConnection.getOutputPortAbsolutePosition(sourcePortForConnection);

            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(sourcePoint.x, sourcePoint.y, tempWireEnd.x, tempWireEnd.y);
        }
    }

    /**
     * Get gate view at a specific point
     */
    public DraggableGate getGateAt(Point point) {
        for (DraggableGate gateView : gateViews) {
            Rectangle bounds = gateView.getBounds();
            if (bounds.contains(point)) {
                return gateView;
            }
        }
        return null;
    }

    /**
     * Clear all gates and wires from the canvas
     */
    public void clearCanvas() {
        for (DraggableGate gate : gateViews) {
            canvas.remove(gate);
        }
        for (ConnectorUI wire : wireViews) {
            canvas.remove(wire);
        }
        gateViews.clear();
        wireViews.clear();
        selectedGate = null;
        canvas.revalidate();
        canvas.repaint();
    }

    /**
     * Get all gate views
     */
    public List<DraggableGate> getGateViews() {
        return new ArrayList<>(gateViews);
    }

    /**
     * Update gate position in view
     */
    public void updateGatePosition(DraggableGate gateView, int x, int y) {
        if (gateView != null) {
            gateView.setLocation(x, y);
            canvas.repaint();
        }
    }

    /**
     * Show input/output dialog for a gate
     */
    public void showInputOutputDialog(ComponentBase gate) {
        if (gate == null)
            return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(canvas),
                "Gate: " + gate.getName(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CANVAS_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Inputs section
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Inputs:"), gbc);

        List<JCheckBox> inputBoxes = new ArrayList<>();
        for (int i = 0; i < gate.getInputs(); i++) {
            final int index = i;
            gbc.gridy++;
            JLabel label = new JLabel("Input " + (index + 1) + ":");
            label.setForeground(BORDER_COLOR);
            contentPanel.add(label, gbc);

            gbc.gridx = 1;
            JCheckBox checkbox = new JCheckBox();
            checkbox.setSelected(gate.getInputValue(index));
            Connector inputConnector = gate.getInputConnector(index);
            boolean isPortConnected = inputConnector != null;
            checkbox.setEnabled(!isPortConnected);
            checkbox.addActionListener(e -> {
                gate.setInputValue(index, checkbox.isSelected());
                canvas.repaint(); // Update port colors
            });
            inputBoxes.add(checkbox);
            contentPanel.add(checkbox, gbc);

            gbc.gridx = 2;
            if (isPortConnected) {
                JLabel statusLabel = new JLabel("Port already connected");
                statusLabel.setForeground(Color.RED.darker());
                contentPanel.add(statusLabel, gbc);
            } else {
                contentPanel.add(Box.createHorizontalStrut(10), gbc);
            }

            gbc.gridx = 0;
        }

        // Outputs section
        gbc.gridy++;
        gbc.gridx = 0;
        contentPanel.add(new JLabel("Outputs:"), gbc);

        for (int i = 0; i < gate.getOutputs(); i++) {
            gbc.gridy++;
            JLabel label = new JLabel("Output " + (i + 1) + ":");
            label.setForeground(BORDER_COLOR);
            contentPanel.add(label, gbc);

            gbc.gridx = 1;
            JLabel outputLabel = new JLabel(gate.getOutputValue(i) ? "HIGH" : "LOW");
            outputLabel.setForeground(gate.getOutputValue(i) ? Color.GREEN : Color.RED);
            outputLabel.setFont(new Font("Arial", Font.BOLD, 12));
            contentPanel.add(outputLabel, gbc);
            gbc.gridx = 0;
        }

        // Close button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(173, 216, 230));
        closeButton.setForeground(BORDER_COLOR);
        closeButton.addActionListener(e -> dialog.dispose());
        contentPanel.add(closeButton, gbc);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(canvas);
        dialog.setVisible(true);
    }

    /**
     * Repaint the canvas
     */
    public void repaint() {
        canvas.repaint();
    }

    /**
     * Register listener for gate selection changes
     */
    public void setGateSelectionListener(GateSelectionListener listener) {
        this.gateSelectionListener = listener;
    }

    public interface GateSelectionListener {
        void onGateSelected(ComponentBase gate);
    }
}

