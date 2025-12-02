package org.yourcompany.yourproject.GUI2.Controller;



import javax.swing.*;

import org.yourcompany.yourproject.GUI2.service.CanvasViewService;
import org.yourcompany.yourproject.GUI2.ui.DraggableGate;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Controller for managing canvas interactions
 * Handles gate selection, dragging, connections, inputs, and outputs
 */
public class CanvasController {
    
    private CanvasViewService viewService;
    private Circuit currentCircuit;
    private JPanel canvas;
    
    // Drag state
    private Point dragStartPoint;
    private DraggableGate draggedGate;
    private boolean isDragging;
    
    public CanvasController(JPanel canvas, CanvasViewService viewService) {
        this.canvas = canvas;
        this.viewService = viewService;
        this.isDragging = false;
        setupMouseListeners();
    }
    
    /**
     * Set the current circuit being edited
     */
    public void setCurrentCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
        if (circuit != null) {
            loadCircuitToView(circuit);
        }
    }
    
    /**
     * Get the current circuit
     */
    public Circuit getCurrentCircuit() {
        return currentCircuit;
    }
    
    /**
     * Load circuit components into the view
     */
    private void loadCircuitToView(Circuit circuit) {
        viewService.clearCanvas();
        
        // Add gates
        for (ComponentBase gate : circuit.getGates()) {
            Point pos = gate.getPosition();
            int x = (pos.x == 0 && pos.y == 0) ? 50 : pos.x;
            int y = (pos.x == 0 && pos.y == 0) ? 50 : pos.y;
            DraggableGate gateView = viewService.addGateView(gate, x, y);
            if (gateView != null) {
                setupGateListeners(gateView);
            }
        }
        
        // Update wires
        updateWireViews();
    }
    
    /**
     * Setup mouse listeners for the canvas
     */
    private void setupMouseListeners() {
        // Canvas mouse listener for connection handling
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleCanvasMousePressed(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleCanvasMouseReleased(e);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCanvasMouseClicked(e);
            }
        });
        
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleCanvasMouseMoved(e);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                handleCanvasMouseDragged(e);
            }
        });
        
        // Also add mouse motion listener for connection tracking
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (viewService.isConnecting()) {
                    viewService.updateConnectionEnd(e.getPoint());
                }
            }
        });
    }
    
    /**
     * Setup listeners for a gate view
     */
    private void setupGateListeners(DraggableGate gateView) {
        gateView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleGateMousePressed(gateView, e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleGateMouseReleased(gateView, e);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                handleGateMouseClicked(gateView, e);
            }
        });
        
        gateView.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleGateMouseDragged(gateView, e);
            }
        });
    }
    
    /**
     * Handle mouse press on canvas
     */
    private void handleCanvasMousePressed(MouseEvent e) {
        // Cancel connection if clicking empty space with double click
        if (viewService.isConnecting() && e.getClickCount() == 2) {
            viewService.endConnection();
            viewService.selectGate(null);
        }
    }
    
    /**
     * Handle mouse release on canvas
     */
    private void handleCanvasMouseReleased(MouseEvent e) {
        // Check if we're ending a connection by releasing on canvas
        if (viewService.isConnecting()) {
            // Try to find a gate at the release point
            Point releasePoint = e.getPoint();
            DraggableGate gateAtPoint = viewService.getGateAt(releasePoint);
            
            if (gateAtPoint != null && viewService.getSourceGateForConnection() != null) {
                // Convert canvas point to gate local coordinates
                Point localPoint = SwingUtilities.convertPoint(canvas, releasePoint, gateAtPoint);
                int inputPort = gateAtPoint.getInputPortAt(localPoint);
                
                if (inputPort >= 0 && gateAtPoint != viewService.getSourceGateForConnection()) {
                    // Create connection
                    createConnection(
                        viewService.getSourceGateForConnection(),
                        viewService.getSourcePortForConnection(),
                        gateAtPoint,
                        inputPort
                    );
                }
            }
            
            viewService.endConnection();
        }
        
        if (isDragging && draggedGate != null) {
            // Update gate position in circuit
            Point location = draggedGate.getLocation();
            draggedGate.getGate().setPosition(location.x, location.y);
            updateWireViews();
            isDragging = false;
            draggedGate = null;
        }
    }
    
    /**
     * Handle mouse click on canvas
     */
    private void handleCanvasMouseClicked(MouseEvent e) {
        // Deselect if clicking empty space
        if (e.getClickCount() == 1) {
            Point clickPoint = e.getPoint();
            DraggableGate gateAtPoint = viewService.getGateAt(clickPoint);
            if (gateAtPoint == null) {
                if (viewService.isConnecting()) {
                    viewService.endConnection();
                }
                viewService.selectGate(null);
            }
        }
    }
    
    /**
     * Handle mouse move on canvas
     */
    private void handleCanvasMouseMoved(MouseEvent e) {
        if (viewService.isConnecting()) {
            viewService.updateConnectionEnd(e.getPoint());
        }
    }
    
    /**
     * Handle mouse drag on canvas
     */
    private void handleCanvasMouseDragged(MouseEvent e) {
        if (viewService.isConnecting()) {
            viewService.updateConnectionEnd(e.getPoint());
        }
    }
    
    /**
     * Handle mouse press on gate
     */
    private void handleGateMousePressed(DraggableGate gateView, MouseEvent e) {
        // Start dragging (connection handled on click)
        isDragging = true;
        draggedGate = gateView;
        dragStartPoint = SwingUtilities.convertPoint(gateView, e.getPoint(), canvas);
        if (!viewService.isConnecting()) {
            viewService.selectGate(gateView);
        }
    }
    
    /**
     * Handle mouse release on gate
     */
    private void handleGateMouseReleased(DraggableGate gateView, MouseEvent e) {
        if (isDragging && draggedGate == gateView) {
            // Update gate position
            Point location = gateView.getLocation();
            gateView.getGate().setPosition(location.x, location.y);
            updateWireViews();
            isDragging = false;
            draggedGate = null;
        }
    }
    
    /**
     * Handle mouse click on gate
     */
    private void handleGateMouseClicked(DraggableGate gateView, MouseEvent e) {
        if (e.getClickCount() == 1) {
            handleSingleClickConnection(gateView);
        } else if (e.getClickCount() == 2) {
            // Double click - focus gate and update toolbar via selection listener
            if (!viewService.isConnecting()) {
                viewService.selectGate(gateView);
            }
        }
    }
    
    /**
     * Handle mouse drag on gate
     */
    private void handleGateMouseDragged(DraggableGate gateView, MouseEvent e) {
        if (isDragging && draggedGate == gateView) {
            Point now = SwingUtilities.convertPoint(gateView, e.getPoint(), canvas);
            int dx = now.x - dragStartPoint.x;
            int dy = now.y - dragStartPoint.y;
            
            Rectangle bounds = gateView.getBounds();
            bounds.translate(dx, dy);
            
            // Keep inside parent
            int pw = canvas.getWidth();
            int ph = canvas.getHeight();
            if (bounds.x < 0) bounds.x = 0;
            if (bounds.y < 0) bounds.y = 0;
            if (bounds.x + bounds.width > pw) bounds.x = Math.max(0, pw - bounds.width);
            if (bounds.y + bounds.height > ph) bounds.y = Math.max(0, ph - bounds.height);
            
            gateView.setBounds(bounds);
            dragStartPoint = now;
            canvas.repaint();
        } else if (viewService.isConnecting()) {
            // Update connection end
            Point absPoint = SwingUtilities.convertPoint(gateView, e.getPoint(), canvas);
            viewService.updateConnectionEnd(absPoint);
        }
    }

    /**
     * Handle single click logic for creating connections
     */
    private void handleSingleClickConnection(DraggableGate gateView) {
        ComponentBase gate = gateView.getGate();
        if (gate == null) {
            return;
        }

        if (!viewService.isConnecting()) {
            int availableOutput = findAvailableOutputPort(gate);
            if (availableOutput < 0) {
                JOptionPane.showMessageDialog(canvas,
                    "All output ports on this gate are already connected.",
                    "No Available Output",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            viewService.selectGate(gateView);
            Point startPoint = gateView.getOutputPortAbsolutePosition(availableOutput);
            viewService.startConnection(gateView, availableOutput, startPoint);
        } else {
            DraggableGate sourceGate = viewService.getSourceGateForConnection();
            if (sourceGate == null) {
                viewService.endConnection();
                return;
            }

            if (gateView == sourceGate) {
                // Clicking the same gate cancels the connection
                viewService.endConnection();
                return;
            }

            int availableInput = findAvailableInputPort(gate);
            if (availableInput < 0) {
                JOptionPane.showMessageDialog(canvas,
                    "All input ports on the selected gate are already connected.",
                    "No Available Input",
                    JOptionPane.INFORMATION_MESSAGE);
                viewService.endConnection();
                return;
            }

            createConnection(
                sourceGate,
                viewService.getSourcePortForConnection(),
                gateView,
                availableInput
            );
            viewService.endConnection();
            viewService.selectGate(gateView);
        }
    }

    /**
     * Find the first available output port on a gate
     */
    private int findAvailableOutputPort(ComponentBase gate) {
        if (gate == null) return -1;
        for (int i = 0; i < gate.getOutputs(); i++) {
            if (gate.getOutputConnector(i) == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find the first available input port on a gate
     */
    private int findAvailableInputPort(ComponentBase gate) {
        if (gate == null) return -1;
        for (int i = 0; i < gate.getInputs(); i++) {
            if (gate.getInputConnector(i) == null) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Create a connection between two gates
     */
    private void createConnection(DraggableGate fromGate, int fromPort, 
                                  DraggableGate toGate, int toPort) {
        if (currentCircuit == null || fromGate == null || toGate == null) {
            System.out.println("Connection failed: circuit or gates null");
            return;
        }
        
        ComponentBase fromComponent = fromGate.getGate();
        ComponentBase toComponent = toGate.getGate();
        
        if (fromComponent == null || toComponent == null) {
            System.out.println("Connection failed: gate components null");
            return;
        }
        
        System.out.println("Attempting to connect: " + fromComponent.getName() + " port " + fromPort + 
                          " to " + toComponent.getName() + " port " + toPort);
        
        // Create connection through circuit
        Connector wire = currentCircuit.connectGates(
            fromComponent, fromPort,
            toComponent, toPort
        );
        
        if (wire != null) {
            System.out.println("Connection created successfully!");
            updateWireViews();
            // Propagate signal
            propagateSignals();
            canvas.repaint();
        } else {
            System.out.println("Connection creation returned null - port may already be connected");
        }
    }
    
    /**
     * Add a gate to the canvas
     */
    public boolean addGate(ComponentBase gate, int x, int y) {
        if (currentCircuit == null) {
            JOptionPane.showMessageDialog(canvas,
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
        if (y < 50) y = 50;
        
        // Add to circuit
        gate.setPosition(x, y);
        currentCircuit.addGate(gate);
        
        // Add to view
        DraggableGate gateView = viewService.addGateView(gate, x, y);
        if (gateView != null) {
            setupGateListeners(gateView);
            updateWireViews();
            return true;
        }
        
        return false;
    }
    
    /**
     * Remove a gate from the canvas
     */
    public void removeGate(DraggableGate gateView) {
        if (gateView == null || currentCircuit == null) return;
        
        ComponentBase gate = gateView.getGate();
        if (gate != null) {
            currentCircuit.removeGate(gate);
            viewService.removeGateView(gateView);
            updateWireViews();
        }
    }
    
    /**
     * Update wire views from circuit
     */
    private void updateWireViews() {
        if (currentCircuit != null) {
            currentCircuit.updateWirePositions();
            viewService.updateWireViews(currentCircuit.getWires());
        }
    }
    
    /**
     * Propagate signals through the circuit
     */
    public void propagateSignals() {
        if (currentCircuit != null) {
            currentCircuit.propagateSignals();
            canvas.repaint();
        }
    }
    
    /**
     * Set input value for a gate
     */
    public void setGateInput(ComponentBase gate, int inputIndex, boolean value) {
        if (gate != null) {
            gate.setInputValue(inputIndex, value);
            propagateSignals();
            canvas.repaint();
        }
    }
    
    /**
     * Get output value from a gate
     */
    public boolean getGateOutput(ComponentBase gate, int outputIndex) {
        if (gate != null) {
            return gate.getOutputValue(outputIndex);
        }
        return false;
    }
    
    /**
     * Clear the canvas
     */
    public void clearCanvas() {
        viewService.clearCanvas();
        if (currentCircuit != null) {
            currentCircuit.clear();
        }
    }
    
    /**
     * Get selected gate
     */
    public DraggableGate getSelectedGate() {
        return viewService.getSelectedGate();
    }
    
    /**
     * Paint temporary connection (called from canvas paintComponent)
     */
    public void paintTemporaryConnection(Graphics g) {
        viewService.paintTemporaryConnection(g);
    }
}

