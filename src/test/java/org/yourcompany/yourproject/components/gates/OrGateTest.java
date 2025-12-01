package org.yourcompany.yourproject.components.gates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

class OrGateTest {

    private OrGate gate;

    @BeforeEach
    void setUp() {
        gate = new OrGate();
    }

    @Test
    void testInitialOutput() {
        // Initially both inputs are false, so output should be false
        assertFalse(gate.getOutput());
    }

    @Test
    void testComputeOutput() {
        // Test all input combinations
        gate.setInputValue(0, false);
        gate.setInputValue(1, false);
        gate.computeOutput();
        assertFalse(gate.getOutput());

        gate.setInputValue(0, true);
        gate.setInputValue(1, false);
        gate.computeOutput();
        assertTrue(gate.getOutput());

        gate.setInputValue(0, false);
        gate.setInputValue(1, true);
        gate.computeOutput();
        assertTrue(gate.getOutput());

        gate.setInputValue(0, true);
        gate.setInputValue(1, true);
        gate.computeOutput();
        assertTrue(gate.getOutput());
    }

    @Test
    void testUpdateMethod() {
        gate.setInputValue(0, false);
        gate.setInputValue(1, true);
        gate.update();
        assertTrue(gate.getOutput());

        gate.setInputValue(0, false);
        gate.setInputValue(1, false);
        gate.update();
        assertFalse(gate.getOutput());
    }

    @Test
    void testCopyMethod() {
        gate.setPosition(50, 100);
        gate.setInputValue(0, true);
        gate.setInputValue(1, false);

        ComponentBase copy = gate.copy();
        assertNotNull(copy);
        assertEquals(gate.getPosition(), copy.getPosition());
        assertEquals(gate.getInputs(), copy.getInputs());
        assertEquals(gate.getOutputs(), copy.getOutputs());
        assertNotEquals(gate.getName(), copy.getName()); // Name should have "_Copy"
    }

    @Test
    void testConstructorWithOtherGate() {
        gate.setInputValue(0, true);
        gate.setInputValue(1, false);

        OrGate clonedGate = new OrGate(gate);
        assertEquals(gate.getName(), clonedGate.getName());
        assertEquals(gate.getInputs(), clonedGate.getInputs());
        assertEquals(gate.getOutputs(), clonedGate.getOutputs());
        assertEquals(gate.getInputValue(0), clonedGate.getInputValue(0));
        assertEquals(gate.getInputValue(1), clonedGate.getInputValue(1));
    }

    @Test
    void testEvaluateMethod() {
        gate.setInputValue(0, false);
        gate.setInputValue(1, true);
        gate.evaluate();
        assertTrue(gate.getOutput());

        gate.setInputValue(0, false);
        gate.setInputValue(1, false);
        gate.evaluate();
        assertFalse(gate.getOutput());
    }
}
