
package org.yourcompany.yourproject.GUI2.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

public class DraggableGate extends JPanel {
    private ComponentBase gate;
    private static final int GATE_WIDTH = 100;
    private static final int GATE_HEIGHT = 70;
    private static final int PORT_SIZE = 18; // Increased to make ports bigger and easier to connect
    private static final Color FILL_COLOR = new Color(249, 252, 255);
    private static final Color OUTLINE_COLOR = new Color(46, 95, 139);

    public DraggableGate(ComponentBase gate) {
        this.gate = gate;
        setSize(GATE_WIDTH, GATE_HEIGHT);
        setBackground(new Color(240, 248, 255)); // Alice blue
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        setLayout(null);

        // Update gate position when component is moved
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent e) {
                if (gate != null) {
                    Point location = getLocation();
                    gate.setPosition(location.x, location.y);
                    // Notify parent to update wire connections
                    repaintParent();
                }
            }
        });
    }

    private void repaintParent() {
        Component parent = getParent();
        if (parent != null) {
            parent.repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGateBody(g2d);

        // Draw input ports (on the left side)
        if (gate != null) {
            int inputs = gate.getInputs();
            if (inputs > 0) {
                int spacing = GATE_HEIGHT / (inputs + 1);
                for (int i = 0; i < inputs; i++) {
                    int y = spacing * (i + 1);
                    // Draw port circle
                    g2d.setColor(getInputPortColor(i));
                    g2d.fillOval(-PORT_SIZE / 2, y - PORT_SIZE / 2, PORT_SIZE, PORT_SIZE);
                    g2d.setColor(OUTLINE_COLOR);
                    g2d.drawOval(-PORT_SIZE / 2, y - PORT_SIZE / 2, PORT_SIZE, PORT_SIZE);
                }
            }

            // Draw output ports (on the right side)
            int outputs = gate.getOutputs();
            if (outputs > 0) {
                int spacing = GATE_HEIGHT / (outputs + 1);
                for (int i = 0; i < outputs; i++) {
                    int y = spacing * (i + 1);
                    // Draw port circle
                    g2d.setColor(getOutputPortColor(i));
                    g2d.fillOval(GATE_WIDTH - PORT_SIZE / 2, y - PORT_SIZE / 2, PORT_SIZE, PORT_SIZE);
                    g2d.setColor(Color.BLACK);
                    g2d.drawOval(GATE_WIDTH - PORT_SIZE / 2, y - PORT_SIZE / 2, PORT_SIZE, PORT_SIZE);
                }
            }
        }
        g2d.dispose();
    }

    private void drawGateBody(Graphics2D g2d) {
        if (gate == null) {
            drawDefaultBody(g2d);
            return;
        }

        if (gate instanceof OrGate) {
            drawOrShape(g2d);
        } else if (gate instanceof AndGate) {
            drawAndShape(g2d);
        } else if (gate instanceof NotGate) {
            drawNotShape(g2d);
        } else {
            drawDefaultBody(g2d);
        }
    }

    private void drawDefaultBody(Graphics2D g2d) {
        g2d.setColor(FILL_COLOR);
        g2d.fillRoundRect(5, 5, GATE_WIDTH - 10, GATE_HEIGHT - 10, 20, 20);
        g2d.setColor(OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(5, 5, GATE_WIDTH - 10, GATE_HEIGHT - 10, 20, 20);
    }

    private void drawAndShape(Graphics2D g2d) {
        int inset = 6;
        int width = GATE_WIDTH - inset * 2;
        int height = GATE_HEIGHT - inset * 2;
        int x = inset;
        int y = inset;

        GeneralPath path = new GeneralPath();
        path.moveTo(x, y);
        path.lineTo(x + width / 2.0, y);
        path.quadTo(x + width, y + height / 2.0, x + width / 2.0, y + height);
        path.lineTo(x, y + height);
        path.closePath();

        g2d.setColor(FILL_COLOR);
        g2d.fill(path);
        g2d.setColor(OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(path);
    }

    private void drawOrShape(Graphics2D g2d) {
        int inset = 8;
        double width = GATE_WIDTH - inset * 2;
        double height = GATE_HEIGHT - inset * 2;
        double x = inset;
        double y = inset;

        double leftStartX = x - width * 0.35;

        GeneralPath outer = new GeneralPath();
        outer.moveTo(leftStartX, y);
        outer.quadTo(x - width * 0.05, y + height / 2.0, leftStartX, y + height);
        outer.curveTo(x + width * 0.3, y + height, x + width * 0.9, y + height * 0.75, x + width, y + height / 2.0);
        outer.curveTo(x + width * 0.9, y + height * 0.25, x + width * 0.3, y, leftStartX, y);
        outer.closePath();

        GeneralPath inner = new GeneralPath();
        inner.moveTo(x, y);
        inner.quadTo(x + width * 0.45, y + height / 2.0, x, y + height);

        g2d.setColor(FILL_COLOR);
        g2d.fill(outer);
        g2d.setColor(OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(outer);
        g2d.draw(inner);
    }

    private void drawNotShape(Graphics2D g2d) {
        int inset = 8;
        int width = GATE_WIDTH - inset * 2;
        int height = GATE_HEIGHT - inset * 2;
        int x = inset;
        int y = inset;

        GeneralPath triangle = new GeneralPath();
        triangle.moveTo(x, y);
        triangle.lineTo(x + width, y + height / 2.0);
        triangle.lineTo(x, y + height);
        triangle.closePath();

        g2d.setColor(FILL_COLOR);
        g2d.fill(triangle);
        g2d.setColor(OUTLINE_COLOR);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(triangle);

        int bubbleDiameter = 12;
        int bubbleX = x + width;
        int bubbleY = y + height / 2 - bubbleDiameter / 2;
        g2d.setColor(Color.WHITE);
        g2d.fillOval(bubbleX, bubbleY, bubbleDiameter, bubbleDiameter);
        g2d.setColor(OUTLINE_COLOR);
        g2d.drawOval(bubbleX, bubbleY, bubbleDiameter, bubbleDiameter);
    }

    private Color getInputPortColor(int portIndex) {
        if (gate == null)
            return Color.GRAY;
        Boolean value = gate.getInputValue(portIndex);
        return value ? Color.GREEN : Color.RED;
    }

    private Color getOutputPortColor(int portIndex) {
        if (gate == null)
            return Color.GRAY;
        Boolean value = gate.getOutputValue(portIndex);
        return value ? Color.GREEN : Color.RED;
    }

    // Check if a point is on an input port (using circular hit detection for better
    // accuracy)
    public int getInputPortAt(Point p) {
        if (gate == null)
            return -1;
        int inputs = gate.getInputs();
        if (inputs == 0)
            return -1;

        int spacing = GATE_HEIGHT / (inputs + 1);
        for (int i = 0; i < inputs; i++) {
            int y = spacing * (i + 1);
            // Input port center is at x=0 (ports are drawn at -PORT_SIZE/2, so center is at
            // 0)
            int centerX = 0;
            int centerY = y;
            // Use circular hit detection with a slightly larger tolerance for easier
            // clicking
            double distance = Math.sqrt(Math.pow(p.x - centerX, 2) + Math.pow(p.y - centerY, 2));
            // Add 2 pixels tolerance for easier clicking
            if (distance <= (PORT_SIZE / 2) + 2) {
                return i;
            }
        }
        return -1;
    }

    // Check if a point is on an output port (using circular hit detection for
    // better accuracy)
    public int getOutputPortAt(Point p) {
        if (gate == null)
            return -1;
        int outputs = gate.getOutputs();
        if (outputs == 0)
            return -1;

        int spacing = GATE_HEIGHT / (outputs + 1);
        for (int i = 0; i < outputs; i++) {
            int y = spacing * (i + 1);
            // Output port center is at x=GATE_WIDTH (ports are drawn at GATE_WIDTH -
            // PORT_SIZE/2, so center is at GATE_WIDTH)
            int centerX = GATE_WIDTH;
            int centerY = y;
            // Use circular hit detection with a slightly larger tolerance for easier
            // clicking
            double distance = Math.sqrt(Math.pow(p.x - centerX, 2) + Math.pow(p.y - centerY, 2));
            // Add 2 pixels tolerance for easier clicking
            if (distance <= (PORT_SIZE / 2) + 2) {
                return i;
            }
        }
        return -1;
    }

    // Get absolute position of an input port
    public Point getInputPortAbsolutePosition(int portIndex) {
        Point location = getLocation();
        if (gate == null)
            return location;

        int inputs = gate.getInputs();
        if (inputs == 0 || portIndex < 0 || portIndex >= inputs)
            return location;

        int spacing = GATE_HEIGHT / (inputs + 1);
        int y = spacing * (portIndex + 1);
        return new Point(location.x, location.y + y);
    }

    // Get absolute position of an output port
    public Point getOutputPortAbsolutePosition(int portIndex) {
        Point location = getLocation();
        if (gate == null)
            return location;

        int outputs = gate.getOutputs();
        if (outputs == 0 || portIndex < 0 || portIndex >= outputs)
            return location;

        int spacing = GATE_HEIGHT / (outputs + 1);
        int y = spacing * (portIndex + 1);
        return new Point(location.x + GATE_WIDTH, location.y + y);
    }

    public ComponentBase getGate() {
        return gate;
    }

    public void setGate(ComponentBase gate) {
        this.gate = gate;
        if (gate != null) {
            Point location = getLocation();
            gate.setPosition(location.x, location.y);
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(GATE_WIDTH, GATE_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(GATE_WIDTH, GATE_HEIGHT);
    }
}
