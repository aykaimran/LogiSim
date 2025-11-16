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
    private int signal; // 0 or 1

    public Connector(String name, Color color, Point source, Point sink) {
        this.name = name;
        this.color = color;
        this.source = source;
        this.sink = sink;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public Point getSource() { return source; }
    public void setSource(Point source) { this.source = source; }

    public Point getSink() { return sink; }
    public void setSink(Point sink) { this.sink = sink; }

    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }

    public ComponentBase getFromGate() { return fromGate; }
    public void setFromGate(ComponentBase fromGate) { this.fromGate = fromGate; }

    public ComponentBase getToGate() { return toGate; }
    public void setToGate(ComponentBase toGate) { this.toGate = toGate; }

    public int getFromPort() { return fromPort; }
    public void setFromPort(int fromPort) { this.fromPort = fromPort; }

    public int getToPort() { return toPort; }
    public void setToPort(int toPort) { this.toPort = toPort; }

    // Placeholder for processing signal
    public void process() { }
    
    // Getters and setters for signal
     public int getSignal() { return signal; }
    public void setSignal(int signal) { this.signal = signal; }
}