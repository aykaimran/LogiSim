package org.yourcompany.yourproject.GUI2.ui;

import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;
import javax.swing.*;
import java.awt.*;

public class ConnectorUI extends JComponent {
    private Connector connector;

    public ConnectorUI(Connector connector) {
        this.connector = connector;
        setBounds(0, 0, 1200, 800); // design area size
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (connector == null) return;

        // Use getters instead of direct access
       // Color color = connector.getColor();
        // Get source and sink points (this will update positions automatically)
        Point source = connector.getSource();
        Point sink = connector.getSink();

        if (source != null && sink != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Determine wire color based on signal value
            Color wireColor;
            Boolean signalValue = connector.getSignalValue();
            if (signalValue != null && signalValue) {
                wireColor = Color.GREEN; // High signal
            } else {
                wireColor = Color.RED; // Low signal
            }
            
            // Use connector color if signal is not set
            if (signalValue == null) {
                wireColor = connector.getColor();
            }
            
            g2d.setColor(wireColor);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(source.x, source.y, sink.x, sink.y);
        }
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
        repaint();
    }
    @Override
    public boolean contains(int x, int y) {
        // Don't intercept mouse events - let them pass through to gates
        return false;
    }
}
