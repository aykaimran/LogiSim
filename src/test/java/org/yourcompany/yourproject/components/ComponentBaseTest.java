package org.yourcompany.yourproject.components;

import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComponentBase using a simple TestGate implementation.
 */
public class ComponentBaseTest {

    /**
     * Simple concrete implementation of ComponentBase for testing.
     * Acts like a NOT gate with 1 input and 1 output.
     */
    static class TestGate extends ComponentBase {

        public TestGate(String name) {
            super(name, 1, 1);
        }

        @Override
        protected void computeOutput() {
            Boolean in = getInputValue(0);
            setOutputValueDirect(0, !in); // direct to avoid recursive propagation
        }

        @Override
        public void evaluate() {
            computeOutput();
        }

        @Override
        public void update() {
            computeOutput();
        }

        @Override
        public ComponentBase copy() {
            TestGate g = new TestGate(getName());
            g.setPosition(getPosition());
            g.setInputValueDirect(0, getInputValue(0));
            g.setOutputValueDirect(0, getOutputValue(0));
            return g;
        }
    }

    @Test
    public void testInitialValues() {
        TestGate gate = new TestGate("G1");

        assertFalse(gate.getInputValue(0), "Initial input should be false");
        assertFalse(gate.getOutputValue(0), "Initial output should be false");
    }

    @Test
    public void testSetInputTriggersComputeOutput() {
        TestGate gate = new TestGate("G1");

        // initial: input = false → output = false (NOT false = true)
        gate.setInputValue(0, false);
        gate.evaluate();
        assertTrue(gate.getOutputValue(0));

        // input = true → output = false
        gate.setInputValue(0, true);
        gate.evaluate();
        assertFalse(gate.getOutputValue(0));
    }

    @Test
    public void testDirectSetInputDoesNotTriggerCompute() {
        TestGate gate = new TestGate("G1");

        gate.setInputValueDirect(0, true);
        // No computeOutput automatically
        assertFalse(gate.getOutputValue(0), "Output should remain unchanged without evaluate()");
    }

    @Test
    public void testOutputPropagation() {
        TestGate source = new TestGate("SRC");
        TestGate target = new TestGate("DEST");

        Connector wire = new Connector(source, 0, target, 0);

        // connect properly
        source.connectOutput(0, wire);
        target.connectInput(0, wire);

        // set input on source
        source.setInputValue(0, true);
        source.evaluate();

        // propagate output
        wire.propagateSignal();

        // target should now see source output (NOT true = false)
        assertFalse(target.getInputValue(0));
    }

    @Test
    public void testCopyComponent() {
        TestGate gate = new TestGate("G1");
        gate.setInputValueDirect(0, true);
        gate.setOutputValueDirect(0, false);
        gate.setPosition(50, 100);

        TestGate copy = (TestGate) gate.copy();

        assertNotNull(copy);
        assertEquals(gate.getName(), copy.getName());
        assertEquals(gate.getPosition(), copy.getPosition());
        assertEquals(gate.getInputValue(0), copy.getInputValue(0));
        assertEquals(gate.getOutputValue(0), copy.getOutputValue(0));

        // Ensure deep copy (different object)
        assertNotSame(gate, copy);
    }

    @Test
    public void testPortPositions() {
        TestGate gate = new TestGate("G1");
        gate.setPosition(10, 20);

        // Input port position
        var inPos = gate.getInputPortPosition(0);
        assertEquals(10, inPos.x);
        assertTrue(inPos.y > 20);

        // Output port position
        var outPos = gate.getOutputPortPosition(0);
        assertEquals(110, outPos.x); // x + gateWidth (100)
        assertTrue(outPos.y > 20);
    }
}
