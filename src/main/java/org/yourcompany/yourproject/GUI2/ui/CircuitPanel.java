package org.yourcompany.yourproject.GUI2.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.yourcompany.yourproject.GUI2.Controller.CanvasController;
import org.yourcompany.yourproject.GUI2.service.CanvasViewService;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.CircuitComponent;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;

public class CircuitPanel extends JPanel {

    private JLabel label;
    private List<DraggableGate> gateUIs = new ArrayList<>();
    private List<ConnectorUI> wireUIs = new ArrayList<>();
    private static CircuitPanel instance;
    private Circuit currentCircuit;

    // Controller and View Service
    private CanvasController controller;
    private CanvasViewService viewService;

    // Connection mode variables (kept for backward compatibility)
    private DraggableGate sourceGate;
    private int sourcePort;
    private boolean isConnecting = false;
    private Point tempWireEnd; // For drawing temporary wire during connection

    public CircuitPanel() {
        setLayout(null);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Circuit Design Area"));

        label = new JLabel("No circuit selected");
        label.setBounds(20, 20, 300, 30);
        add(label);
        instance = this;
        // Initialize controller and view service
        viewService = new CanvasViewService(this);
        controller = new CanvasController(this, viewService);

        // Add mouse listener for connection handling (backward compatibility)
        addMouseListener(new ConnectionMouseAdapter());
        addMouseMotionListener(new ConnectionMouseAdapter());
    }

    public void showCircuit(String name) {
        label.setText("Editing circuit: " + name);
        repaint();
    }
    
    public void addCircuitComponents(Circuit circuit) {
        this.currentCircuit = circuit;

        // Use controller to load circuit (this ensures proper listener setup)
        if (controller != null) {
            controller.setCurrentCircuit(circuit);
            // Update local lists for backward compatibility
            gateUIs = viewService.getGateViews();
            updateWireConnections();
            showCircuit(circuit.getName());
            revalidate();
            repaint();
            return;
        }

        // Fallback to old method if controller not available
        int offsetX = 50;
        int offsetY = 50;

        // Clear existing components ONLY if this is a different circuit
        if (this.currentCircuit != circuit) {
            clearCircuit();
            this.currentCircuit = circuit;
        }

        // Add gates
        for (ComponentBase gate : circuit.getGates()) {
            // Check if gate already exists in UI to avoid duplicates
            boolean gateExists = false;
            for (DraggableGate existingGate : gateUIs) {
                if (existingGate.getGate() == gate) {
                    gateExists = true;
                    break;
                }
            }

            if (!gateExists) {
                DraggableGate gateUI = new DraggableGate(gate);
                Point pos = gate.getPosition();
                gateUI.setLocation(pos.x == 0 && pos.y == 0 ? offsetX : pos.x,
                        pos.x == 0 && pos.y == 0 ? offsetY : pos.y);
                gate.setPosition(gateUI.getLocation().x, gateUI.getLocation().y);
                add(gateUI);
                enableDrag(gateUI);
                enableConnection(gateUI);
                gateUIs.add(gateUI);

                offsetX += gateUI.getWidth() + 20;
                if (offsetX > getWidth() - 100) {
                    offsetX = 50;
                    offsetY += 100;
                }
            }
        }

        // Add connectors/wires
        updateWireConnections();
        showCircuit(circuit.getName());
        revalidate();
        repaint();
    }

    
public boolean addCircuitIntoCurrent(Circuit circuitToAdd) {
    if (currentCircuit == null) {
        JOptionPane.showMessageDialog(this, 
            "Please open or create a circuit first.\n" +
            "1. Select a project\n" +
            "2. Create a new circuit or open an existing one",
            "No Circuit Selected",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    if (circuitToAdd == null) {
        return false;
    }
    
    // Prevent adding a circuit to itself
    if (circuitToAdd == currentCircuit) {
        JOptionPane.showMessageDialog(this, 
            "Cannot add a circuit to itself!",
            "Invalid Operation",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    
    try {
        // Create a DEEP COPY of the entire circuit to add
        Circuit circuitCopy = deepCopyCircuit(circuitToAdd);
        if (circuitCopy == null) {
            JOptionPane.showMessageDialog(this, 
                "Failed to copy circuit components.",
                "Copy Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Calculate offset to position the new circuit's components
        int offsetX = 50;
        int offsetY = 50;
        
        // Find a good position for the new components
        if (!gateUIs.isEmpty()) {
            // Position to the right of existing gates
            int maxX = 0;
            for (DraggableGate gate : gateUIs) {
                maxX = Math.max(maxX, gate.getX() + gate.getWidth());
            }
            offsetX = maxX + 50;
        }
        
        // Apply offset to all gates in the copy
        for (ComponentBase gate : circuitCopy.getGates()) {
            Point originalPos = gate.getPosition();
            gate.setPosition(originalPos.x + offsetX, originalPos.y + offsetY);
        }
        
        // Update wire positions in the copy
        circuitCopy.updateWirePositions();
        
        // Use CONTROLLER to add the gates - this ensures proper setup
        if (controller != null) {
            // Add gates through controller for proper integration
            for (ComponentBase gate : circuitCopy.getGates()) {
                controller.addGate(gate, gate.getPosition().x, gate.getPosition().y);
            }
            
            // Add wires through controller
            for (Connector wire : circuitCopy.getWires()) {
                currentCircuit.addWire(wire);
            }
        } else {
            // Fallback: old method without controller
            for (ComponentBase gate : circuitCopy.getGates()) {
                currentCircuit.addGate(gate);
                
                DraggableGate gateUI = new DraggableGate(gate);
                gateUI.setLocation(gate.getPosition().x, gate.getPosition().y);
                add(gateUI);
                enableDrag(gateUI);
                enableConnection(gateUI);
                gateUIs.add(gateUI);
            }
            
            for (Connector wire : circuitCopy.getWires()) {
                currentCircuit.addWire(wire);
            }
        }
        
        // Update wire connections in UI
        updateWireConnections();
        
        // Refresh the view but DON'T call addCircuitComponents() as that would replace everything
        refreshCircuitLabel();
        
        revalidate();
        repaint();
        return true;
        
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Error adding circuit: " + e.getMessage(),
            "Operation Failed",
            JOptionPane.ERROR_MESSAGE);
        return false;
    }
}

/**
 * Create a proper deep copy of a circuit preserving all states
 */
private Circuit deepCopyCircuit(Circuit original) {
    if (original == null) return null;
    
    try {
        Circuit copy = new Circuit(original.getName() + "_Instance");
        
        // Create a mapping from original gates to copied gates
        Map<ComponentBase, ComponentBase> gateMap = new HashMap<>();
        
        // Deep copy gates with state preservation
        for (ComponentBase originalGate : original.getGates()) {
            ComponentBase copiedGate = originalGate.copy();
            if (copiedGate != null) {
                // Copy position
                Point originalPos = originalGate.getPosition();
                copiedGate.setPosition(originalPos.x, originalPos.y);
                
                // Copy input/output states - this is the key fix
                // You'll need to implement this based on your gate implementation
                copyGateState(originalGate, copiedGate);
                
                copy.addGate(copiedGate);
                gateMap.put(originalGate, copiedGate);
            }
        }
        
        // Deep copy wires using the gate mapping
        for (Connector originalWire : original.getWires()) {
            ComponentBase fromGate = originalWire.getFromGate();
            ComponentBase toGate = originalWire.getToGate();
            
            ComponentBase copiedFromGate = gateMap.get(fromGate);
            ComponentBase copiedToGate = gateMap.get(toGate);
            
            if (copiedFromGate != null && copiedToGate != null) {
                int fromPort = originalWire.getFromPort();
                int toPort = originalWire.getToPort();
                
                copy.connectGates(copiedFromGate, fromPort, copiedToGate, toPort);
            }
        }
        
        return copy;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

/**
 * Copy the state (input/output values) from one gate to another
 */
/**
 * Copy the state (input/output values) from one gate to another
 */
private void copyGateState(ComponentBase source, ComponentBase target) {
    try {
        // Copy input values using direct method to avoid triggering computeOutput
        for (int i = 0; i < Math.min(source.getInputs(), target.getInputs()); i++) {
            Boolean inputValue = source.getInputValue(i);
            target.setInputValueDirect(i, inputValue);
        }
        
        // Copy output values using direct method to avoid triggering propagation
        for (int i = 0; i < Math.min(source.getOutputs(), target.getOutputs()); i++) {
            Boolean outputValue = source.getOutputValue(i);
            target.setOutputValueDirect(i, outputValue);
        }
        
    } catch (Exception e) {
        System.err.println("Warning: Could not copy gate state from " + source + " to " + target);
        e.printStackTrace();
    }
}
   /**
     * Update wire connections in the UI
     */
    private void updateWireConnections() {
        // Remove old wire UIs
        for (ConnectorUI wireUI : wireUIs) {
            remove(wireUI);
        }
        wireUIs.clear();

        if (currentCircuit != null) {
            // Update wire positions
            currentCircuit.updateWirePositions();
            
            // Add new wire UIs
            for (Connector wire : currentCircuit.getWires()) {
                ConnectorUI wireUI = new ConnectorUI(wire);
                wireUI.setBounds(0, 0, getWidth(), getHeight());
                add(wireUI);
                wireUIs.add(wireUI);
            }
        }
        
        // Ensure wires are behind gates
        for (ConnectorUI wireUI : wireUIs) {
            setComponentZOrder(wireUI, 0);
        }
        for (DraggableGate gateUI : gateUIs) {
            setComponentZOrder(gateUI, 1);
        }
    }

    /**
     * Enable connection handling for a gate
     */
    private void enableConnection(DraggableGate gateUI) {
        gateUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point localPoint = e.getPoint();
                Point absPoint = SwingUtilities.convertPoint(gateUI, localPoint, CircuitPanel.this);
                
                // Only activate connection mode if clicking on output port
                // This allows dragging to work normally when clicking elsewhere
                int outputPort = gateUI.getOutputPortAt(localPoint);
                if (outputPort >= 0) {
                    isConnecting = true;
                    sourceGate = gateUI;
                    sourcePort = outputPort;
                    tempWireEnd = absPoint;
                    repaint();
                }
                // Don't consume the event - let drag listener handle it if not on port
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isConnecting && sourceGate != null) {
                    Point localPoint = e.getPoint();
                    
                    // Check if releasing on an input port
                    int inputPort = gateUI.getInputPortAt(localPoint);
                    if (inputPort >= 0 && gateUI != sourceGate) {
                        // Create connection
                        if (currentCircuit != null) {
                            Connector wire = currentCircuit.connectGates(
                                sourceGate.getGate(), sourcePort,
                                gateUI.getGate(), inputPort
                            );
                            if (wire != null) {
                                updateWireConnections();
                                repaint();
                            }
                        }
                    }
                    
                    // Reset connection state
                    isConnecting = false;
                    sourceGate = null;
                    sourcePort = -1;
                    tempWireEnd = null;
                    repaint();
                }
            }
        });
    }
    
    /**
     * Add a new gate component to the current circuit at the specified position
     */
    public boolean addGateToCircuit(ComponentBase gate, int x, int y) {
        // Use controller to add gate
        if (controller != null) {
            boolean result = controller.addGate(gate, x, y);
            if (result) {
                // Update local lists for backward compatibility
                gateUIs = viewService.getGateViews();
            }
            return result;
        }
        
        // Fallback to old implementation if controller not available
        if (currentCircuit == null) {
            JOptionPane.showMessageDialog(this, 
                "Please open or create a circuit first.\n" +
                "1. Select a project\n" +
                "2. Create a new circuit or open an existing one",
                "No Circuit Selected",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (gate == null) return false;
        
        // Ensure position is within bounds
        if (x < 0) x = 50;
        if (y < 50) y = 50; // Leave space for label

        // Set the gate position
        gate.setPosition(x, y);

        // Add to circuit
        currentCircuit.addGate(gate);

        // Create UI component
        DraggableGate gateUI = new DraggableGate(gate);
        gateUI.setLocation(x, y);
        add(gateUI);
        // Don't use old enableDrag/enableConnection - let controller handle it
        // But if controller is not available, use old methods
        if (controller == null) {
            enableDrag(gateUI);
            enableConnection(gateUI);
        }
        gateUIs.add(gateUI);

        // Update wire connections to reflect new gate
        updateWireConnections();

        revalidate();
        repaint();
        return true;
    }

    /**
     * Clears the current design area
     */
    public void clearCircuit() {
        for (DraggableGate g : gateUIs) remove(g);
        for (ConnectorUI w : wireUIs) remove(w);
        gateUIs.clear();
        wireUIs.clear();
        currentCircuit = null;
        label.setText("No circuit selected");
        if (viewService != null) {
            viewService.selectGate(null);
        }
        repaint();
    }

    /**
     * Checks if a circuit by this name is already placed
     */
    public boolean isCircuitPlaced(String circuitName) {
        for (DraggableGate g : gateUIs) {
            if (g.getGate().getName().equals(circuitName)) return true;
        }
        return false;
    }

    /**
     * Enable dragging for gates
     */
    private void enableDrag(JComponent comp) {
        DragListener dl = new DragListener();
        comp.addMouseListener(dl);
        comp.addMouseMotionListener(dl);
    }

    public void setCurrentCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
        if (controller != null) {
            controller.setCurrentCircuit(circuit);
        }
    }

    public void refreshCircuitLabel() {
        if (currentCircuit != null) {
            showCircuit(currentCircuit.getName());
        } else {
            showCircuit("No circuit selected");
        }
    }

    public Circuit getCurrentCircuit() {
        return currentCircuit;
    }

    /**
     * Get the canvas controller
     */
    public CanvasController getController() {
        return controller;
    }

    /**
     * Register a listener for gate selection changes
     */
    public void setGateSelectionListener(CanvasViewService.GateSelectionListener listener) {
        if (viewService != null) {
            viewService.setGateSelectionListener(listener);
        }
    }

     /**
     * Dragging behavior
     */
    private class DragListener extends MouseAdapter {
        private Point pressed;
        private Component target;

        @Override
        public void mousePressed(MouseEvent e) {
            target = e.getComponent();
            pressed = SwingUtilities.convertPoint(target, e.getPoint(), target.getParent());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (target == null || pressed == null) return;

            Point now = SwingUtilities.convertPoint(target, e.getPoint(), target.getParent());
            int dx = now.x - pressed.x;
            int dy = now.y - pressed.y;

            Rectangle bounds = target.getBounds();
            bounds.translate(dx, dy);

            // Keep inside parent
            int pw = target.getParent().getWidth();
            int ph = target.getParent().getHeight();
            if (bounds.x < 0) bounds.x = 0;
            if (bounds.y < 0) bounds.y = 0;
            if (bounds.x + bounds.width > pw) bounds.x = Math.max(0, pw - bounds.width);
            if (bounds.y + bounds.height > ph) bounds.y = Math.max(0, ph - bounds.height);

            target.setBounds(bounds);
            
            // Update gate position - allow dragging even if circuit check might fail
            if (target instanceof DraggableGate) {
                DraggableGate gateUI = (DraggableGate) target;
                if (gateUI.getGate() != null) {
                    gateUI.getGate().setPosition(bounds.x, bounds.y);
                    // Update wire connections only if circuit exists
                    if (currentCircuit != null) {
                        updateWireConnections();
                    }
                }
            }

            target.getParent().repaint();
            pressed = now;
        }
    }

    private class ConnectionMouseAdapter extends MouseAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            if (isConnecting && sourceGate != null) {
                tempWireEnd = e.getPoint();
                repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // Cancel connection if clicking on empty space
            if (isConnecting && e.getClickCount() == 2) {
                isConnecting = false;
                sourceGate = null;
                sourcePort = -1;
                tempWireEnd = null;
                repaint();
            }
        }
    }
    
 @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    drawGridBackground(g);
    
    // Draw temporary wire during connection using controller
    if (controller != null) {
        controller.paintTemporaryConnection(g);
    }
    
    // Also draw using old method for backward compatibility
    if (isConnecting && sourceGate != null && tempWireEnd != null) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Point sourcePoint = sourceGate.getOutputPortAbsolutePosition(sourcePort);
        
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(sourcePoint.x, sourcePoint.y, tempWireEnd.x, tempWireEnd.y);
    }
    
    // Draw circuit components
    if (currentCircuit != null) {
        for (ComponentBase gate : currentCircuit.getGates()) {
            if (gate instanceof CircuitComponent) {
                drawCircuitComponent(g, (CircuitComponent) gate);
            }
        }
    }
}

// Add this method to CircuitPanel
// Compact version with side labels
// Add this method to CircuitPanel
private void drawCircuitComponent(Graphics graphics, CircuitComponent circuitComp) {
    Point pos = circuitComp.getPosition();
    
    // Draw as a rectangle representing the circuit
    graphics.setColor(new Color(200, 230, 255)); // Light blue for circuit components
    graphics.fillRect(pos.x, pos.y, 120, 80);
    graphics.setColor(Color.BLUE);
    graphics.drawRect(pos.x, pos.y, 120, 80);
    
    // Draw circuit name
    graphics.setColor(Color.BLACK);
    graphics.setFont(new Font("Arial", Font.BOLD, 12));
    String displayName = circuitComp.getCircuitDisplayName();
    if (displayName.length() > 10) {
        displayName = displayName.substring(0, 10) + "...";
    }
    graphics.drawString(displayName, pos.x + 10, pos.y + 20);
    
    // Draw input pins on left side - use ACTUAL input count
    int inputCount = circuitComp.getInputs();
    for (int i = 0; i < inputCount; i++) {
        int pinY = pos.y + 25 + (i * 15);
        graphics.setColor(Color.RED);
        graphics.fillRect(pos.x - 5, pinY, 5, 3);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.PLAIN, 10));
        graphics.drawString("In" + i, pos.x - 25, pinY + 3);
    }
    
    // Draw output pins on right side - use ACTUAL output count
    int outputCount = circuitComp.getOutputs();
    for (int i = 0; i < outputCount; i++) {
        int pinY = pos.y + 25 + (i * 15);
        graphics.setColor(Color.GREEN);
        graphics.fillRect(pos.x + 120, pinY, 5, 3);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.PLAIN, 10));
        graphics.drawString("Out" + i, pos.x + 125, pinY + 3);
    }
}  private void drawGridBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color majorLine = new Color(210, 220, 235);
        Color minorLine = new Color(230, 236, 245);
        int minorSpacing = 20;
        int majorSpacing = minorSpacing * 5;

        for (int x = 0; x < getWidth(); x += minorSpacing) {
            g2d.setColor(x % majorSpacing == 0 ? majorLine : minorLine);
            g2d.drawLine(x, 0, x, getHeight());
        }

        for (int y = 0; y < getHeight(); y += minorSpacing) {
            g2d.setColor(y % majorSpacing == 0 ? majorLine : minorLine);
            g2d.drawLine(0, y, getWidth(), y);
        }

        g2d.dispose();
    }

    public static CircuitPanel getInstance() {
        return instance;
    }
}
