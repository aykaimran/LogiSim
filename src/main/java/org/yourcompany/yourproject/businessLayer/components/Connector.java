package org.yourcompany.yourproject.businessLayer.components;
import java.awt.Color;
import java.awt.Point;

public class Connector {
    private String name;
    private Color color;
    private Point source;
    private Point sink;
    private Point position;

    private ComponentBase fromGate;
    private ComponentBase toGate;
    private int fromPort;
    private int toPort;
    private Boolean signalValue; // Current signal value on this wire
    private String id; // Unique identifier for the wire

    public Connector(String name, Color color, Point source, Point sink) {
        this.name = name;
        this.color = color != null ? color : Color.BLACK;
        this.source = source;
        this.sink = sink;
        this.signalValue = false;
        this.id = generateId();
    }
    public Connector(Connector other, ComponentBase newFrom, ComponentBase newTo) {
    this("Wire", other.getColor(), null, null);
    this.fromGate = newFrom;
    this.fromPort = other.getFromPort();
    this.toGate = newTo;
    this.toPort = other.getToPort();
    updateConnectionPoints();

    if (newFrom != null) newFrom.connectOutput(fromPort, this);
    if (newTo != null) newTo.connectInput(toPort, this);
}

    public Connector(ComponentBase fromGate, int fromPort, ComponentBase toGate, int toPort) {
        this("Wire", Color.BLACK, null, null);
        this.fromGate = fromGate;
        this.fromPort = fromPort;
        this.toGate = toGate;
        this.toPort = toPort;
        updateConnectionPoints();
        // Connect the wire to the gates
        if (fromGate != null) {
            fromGate.connectOutput(fromPort, this);
        }
        if (toGate != null) {
            toGate.connectInput(toPort, this);
        }
    }

    private String generateId() {
        return "Wire_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Update source and sink points based on connected gates
    public void updateConnectionPoints() {
        if (fromGate != null && fromPort >= 0) {
            this.source = fromGate.getOutputPortPosition(fromPort);
        }
        if (toGate != null && toPort >= 0) {
            this.sink = toGate.getInputPortPosition(toPort);
        }
    }

    // Propagate signal from source gate to destination gate
    public void propagateSignal() {
        if (fromGate != null && toGate != null) {
            Boolean value = fromGate.getOutputValue(fromPort);
            this.signalValue = value;
            toGate.setInputValue(toPort, value);
        }
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color != null ? color : Color.BLACK; }

    public Point getSource() { 
        updateConnectionPoints(); // Ensure points are up to date
        return source; 
    }
    public void setSource(Point source) { this.source = source; }

    public Point getSink() { 
        updateConnectionPoints(); // Ensure points are up to date
        return sink; 
    }
    public void setSink(Point sink) { this.sink = sink; }

    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }

    public ComponentBase getFromGate() { return fromGate; }
    public void setFromGate(ComponentBase fromGate) { 
        this.fromGate = fromGate; 
        updateConnectionPoints();
        if (fromGate != null && fromPort >= 0) {
            fromGate.connectOutput(fromPort, this);
        }
    }

    public ComponentBase getToGate() { return toGate; }
    public void setToGate(ComponentBase toGate) { 
        this.toGate = toGate; 
        updateConnectionPoints();
        if (toGate != null && toPort >= 0) {
            toGate.connectInput(toPort, this);
        }
    }

    public int getFromPort() { return fromPort; }
    public void setFromPort(int fromPort) { 
        this.fromPort = fromPort; 
        updateConnectionPoints();
        if (fromGate != null) {
            fromGate.connectOutput(fromPort, this);
        }
    }

    public int getToPort() { return toPort; }
    public void setToPort(int toPort) { 
        this.toPort = toPort; 
        updateConnectionPoints();
        if (toGate != null) {
            toGate.connectInput(toPort, this);
        }
    }

    public Boolean getSignalValue() { return signalValue; }
    public void setSignalValue(Boolean signalValue) { this.signalValue = signalValue; }

    public String getId() { return id; }

    // Disconnect this wire from both gates
    public void disconnect() {
        if (fromGate != null && fromPort >= 0) {
            Connector currentConnector = fromGate.getOutputConnector(fromPort);
            if (currentConnector == this) {
                fromGate.connectOutput(fromPort, null);
            }
        }
        if (toGate != null && toPort >= 0) {
            Connector currentConnector = toGate.getInputConnector(toPort);
            if (currentConnector == this) {
                toGate.connectInput(toPort, null);
            }
        }
        fromGate = null;
        toGate = null;
    }

    // Check if wire is properly connected
    public boolean isConnected() {
        return fromGate != null && toGate != null;
    }
    
}
