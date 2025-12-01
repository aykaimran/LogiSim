package org.yourcompany.yourproject.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.CircuitComponent;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

/**
 * Tests for CircuitComponent
 */
public class CircuitComponentTest {

    /**
     * Simple 1-input 1-output NOT gate used inside subcircuits
     */
    static class TestGate extends ComponentBase {

        public TestGate(String name) {
            super(name, 1, 1);
        }

        @Override
        protected void computeOutput() {
            Boolean in = getInputValue(0);
            setOutputValueDirect(0, !in);
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
            g.setInputValueDirect(0, getInputValue(0));
            g.setOutputValueDirect(0, getOutputValue(0));
            return g;
        }
    }

    /**
     * Fake circuit for testing
     * Takes 1 input → feeds into NOT gate → 1 output
     */
    static class TestCircuit extends Circuit {

        private ComponentBase inGate;
        private ComponentBase outGate;

        public TestCircuit(String name) {
            super(name);

            inGate = new TestGate("INPUT_GATE");
            outGate = new TestGate("OUTPUT_GATE");

            addComponent(inGate);
            addComponent(outGate);
        }

        @Override
        public java.util.List<ComponentBase> identifyInputNodes() {
            return java.util.List.of(inGate);
        }

        @Override
        public java.util.List<ComponentBase> identifyOutputNodes() {
            return java.util.List.of(outGate);
        }

        @Override
        public int countCircuitInputs() {
            return 1;
        }

        @Override
        public int countCircuitOutputs() {
            return 1;
        }

        @Override
        public void evaluate() {
            inGate.evaluate();
            outGate.setInputValueDirect(0, inGate.getOutputValue(0));
            outGate.evaluate();
        }

        @Override
        public Circuit createCopy(String suffix) {
            return new TestCircuit(getName() + suffix);
        }
    }


    // ----------------------------------------------------------
    // TESTS START HERE
    // ----------------------------------------------------------

    @Test
    public void testCircuitComponentInputOutputMapping() {
        TestCircuit circuit = new TestCircuit("C1");

        CircuitComponent comp = new CircuitComponent(circuit, "WRAPPER");
        assertEquals(1, comp.getInputs());
        assertEquals(1, comp.getOutputs());

        // Set input of wrapper circuit
        comp.setInputValue(0, true);
        comp.evaluate();

        assertTrue(comp.getOutputValue(0));
    }

    @Test
    public void testCircuitComponentEvaluation() {
        TestCircuit circuit = new TestCircuit("C2");
        CircuitComponent comp = new CircuitComponent(circuit, "WRAPPER");

        comp.setInputValue(0, false);
        comp.update();
        assertFalse(comp.getOutputValue(0));
    }

    @Test
    public void testCircuitComponentCopy() {
        TestCircuit circuit = new TestCircuit("COP");
        CircuitComponent comp = new CircuitComponent(circuit, "WRAPPER");

        comp.setInputValueDirect(0, true);
        comp.evaluate();

        CircuitComponent copy = (CircuitComponent) comp.copy();

        assertNotNull(copy);
        assertNotSame(comp, copy);
        assertEquals(comp.getCircuitDisplayName(), copy.getCircuitDisplayName());
        assertEquals(comp.getOutputs(), copy.getOutputs());
        assertEquals(comp.getInputs(), copy.getInputs());
    }

    @Test
    public void testExceptionDoesNotCrash() {
        Circuit broken = new TestCircuit("BROKEN") {
            @Override
            public void evaluate() {
                throw new RuntimeException("Forced fail");
            }
        };

        CircuitComponent comp = new CircuitComponent(broken, "FAILSAFE");

        comp.setInputValue(0, true);
        comp.evaluate(); // Should not crash

        // Fallback sets outputs to false
        assertFalse(comp.getOutputValue(0));
    }
}
