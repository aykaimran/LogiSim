package org.yourcompany.yourproject.components.gates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;

import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

class NotGateTest {

    private NotGate gate;

    @BeforeEach
    void setUp() {
        gate = new NotGate();
    }

    @Test
    void testInitialOutput() {
        // Initially, inputValue field defaults to false, so output should be true
        gate.setInputValue(false);
        gate.evaluate();
        assertTrue(gate.getOutput());
    }

    @Test
    void testComputeOutput() {
        // Test logical inversion
        gate.setInputValue(0, true);
        gate.computeOutput();
        assertFalse(gate.getOutput());

        gate.setInputValue(0, false);
        gate.computeOutput();
        assertTrue(gate.getOutput());
    }

    @Test
    void testUpdateMethod() {
        gate.setInputValue(0, true);
        gate.update();
        assertFalse(gate.getOutput());

        gate.setInputValue(0, false);
        gate.update();
        assertTrue(gate.getOutput());
    }

    @Test
    void testCopyMethod() {
        gate.setPosition(50, 100);
        gate.setInputValue(0, true);

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

        NotGate clonedGate = new NotGate(gate);
        assertEquals(gate.getName(), clonedGate.getName());
        assertEquals(gate.getInputs(), clonedGate.getInputs());
        assertEquals(gate.getOutputs(), clonedGate.getOutputs());
        assertEquals(gate.getInputValue(), clonedGate.getInputValue());
    }

    @Test
    void testEvaluateMethod() {
        gate.setInputValue(0, true);
        gate.evaluate();
        assertFalse(gate.getOutput());

        gate.setInputValue(0, false);
        gate.evaluate();
        assertTrue(gate.getOutput());
    }
}
