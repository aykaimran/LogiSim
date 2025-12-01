
package org.yourcompany.yourproject.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.Point;

import org.yourcompany.yourproject.businessLayer.components.Connector;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

class ConnectorTest {

    // Simple concrete subclass of ComponentBase for testing
    static class TestGate extends ComponentBase {
        public TestGate(String name, int inputs, int outputs) {
            super(name, inputs, outputs);
        }

        @Override
        protected void computeOutput() {
            // For testing, just copy first input to first output if available
            if (getInputs() > 0 && getOutputs() > 0) {
                setOutputValueDirect(0, getInputValue(0));
            }
        }

        @Override
        public void evaluate() {
            // Not used in tests
        }

        @Override
        public ComponentBase copy() {
            return new TestGate(getName(), getInputs(), getOutputs());
        }

        @Override
        public void update() {
            // Not used
        }
    }

    private TestGate gate1;
    private TestGate gate2;
    private Connector connector;

    @BeforeEach
    void setUp() {
        gate1 = new TestGate("Gate1", 1, 1);
        gate2 = new TestGate("Gate2", 1, 1);
        connector = new Connector(gate1, 0, gate2, 0);
    }

    @Test
    void testInitialConnection() {
        assertTrue(connector.isConnected());
        assertEquals(gate1, connector.getFromGate());
        assertEquals(gate2, connector.getToGate());
        assertEquals(connector, gate1.getOutputConnector(0));
        assertEquals(connector, gate2.getInputConnector(0));
    }

    @Test
    void testSignalPropagation() {
        gate1.setOutputValue(0, true); // Set output of source gate
        connector.propagateSignal(); // Should propagate to gate2
        assertTrue(connector.getSignalValue());
        assertTrue(gate2.getInputValue(0));
    }

    @Test
    void testDisconnect() {
        connector.disconnect();
        assertFalse(connector.isConnected());
        assertNull(gate1.getOutputConnector(0));
        assertNull(gate2.getInputConnector(0));
    }

    @Test
    void testUpdateConnectionPoints() {
        gate1.setPosition(10, 20);
        gate2.setPosition(30, 40);
        connector.updateConnectionPoints();
        Point source = connector.getSource();
        Point sink = connector.getSink();
        assertNotNull(source);
        assertNotNull(sink);
        assertEquals(gate1.getOutputPortPosition(0), source);
        assertEquals(gate2.getInputPortPosition(0), sink);
    }

    @Test
    void testSettersAndGetters() {
        connector.setName("NewWire");
        connector.setColor(Color.RED);
        assertEquals("NewWire", connector.getName());
        assertEquals(Color.RED, connector.getColor());

        Point p = new Point(5, 5);
        connector.setPosition(p);
        assertEquals(p, connector.getPosition());
    }

    @Test
    void testCopyConstructor() {
        Connector copy = new Connector(connector, gate1, gate2);
        assertTrue(copy.isConnected());
        assertEquals(gate1, copy.getFromGate());
        assertEquals(gate2, copy.getToGate());
    }
}
