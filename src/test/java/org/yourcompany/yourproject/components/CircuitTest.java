package org.yourcompany.yourproject.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.Connector;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

public class CircuitTest {

    private Circuit circuit;

    @BeforeEach
    void setup() {
        circuit = new Circuit("TestCircuit");
    }

    // -------------------------------
    //  test 1: add & remove gates
    // -------------------------------
    @Test
    void testAddRemoveGates() {
        AndGate and = new AndGate();
        OrGate or = new OrGate();

        circuit.addGate(and);
        circuit.addGate(or);

        assertEquals(2, circuit.getGates().size());
        assertTrue(circuit.getGates().contains(and));
        assertTrue(circuit.getGates().contains(or));

        circuit.removeGate(and);
        assertEquals(1, circuit.getGates().size());
        assertFalse(circuit.getGates().contains(and));
    }
    
    @Test
    void testAddGate(){
        AndGate and = new AndGate();
        circuit.addGate(and);
        assertEquals(1, circuit.getGates().size());
        assertTrue(circuit.getGates().contains(and));
    }

    // -------------------------------
    //  test 2: connect gates
    // -------------------------------
    @Test
    void testConnectGates() {
        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(and);
        circuit.addGate(not);

        Connector conn = circuit.connectGates(and, 0, not, 0);

        assertNotNull(conn);
        assertEquals(1, circuit.getWires().size());
        assertEquals(and, conn.getFromGate());
        assertEquals(not, conn.getToGate());
    }

    // -------------------------------
    //  test 3: evaluate circuit
    // -------------------------------
    @Test
    void testEvaluateCircuit() {
        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(and);
        circuit.addGate(not);
        circuit.connectGates(and, 0, not, 0);

        // set external inputs
        circuit.setInputValue(0, true);   // AND input 0
        circuit.setInputValue(1, true);   // AND input 1

        circuit.evaluate();

        // AND output should be true
        assertTrue(and.getOutputValue(0));

        // NOT output should invert AND output
        assertFalse(not.getOutputValue(0));
    }

    // -------------------------------
    //  test 4:identify input/output nodes
    // -------------------------------
    @Test
    void testIdentifyInputOutputNodes() {
        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(and);
        circuit.addGate(not);

        // Connect AND → NOT
        circuit.connectGates(and, 0, not, 0);

        // AND is input node (unconnected input)
        assertTrue(circuit.identifyInputNodes().contains(and));

        // NOT is output node (unconnected output)
        assertTrue(circuit.identifyOutputNodes().contains(not));
    }

    // -------------------------------
    //  test 5:clear circuit
    // -------------------------------
    @Test
    void testClearCircuit() {
        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(and);
        circuit.addGate(not);
        circuit.connectGates(and, 0, not, 0);

        circuit.clear();

        assertEquals(0, circuit.getGates().size());
        assertEquals(0, circuit.getWires().size());
        assertEquals(0, circuit.getInputNodes().size());
        assertEquals(0, circuit.getOutputNodes().size());
    }

    // -------------------------------
    //  test 6:deep copy of circuit
    // -------------------------------
    @Test
    void testCreateCopy() {
        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(and);
        circuit.addGate(not);
        circuit.connectGates(and, 0, not, 0);

        Circuit copy = circuit.createCopy("CopyCircuit");

        // Original gates and wires count match
        assertEquals(circuit.getGates().size(), copy.getGates().size());
        assertEquals(circuit.getWires().size(), copy.getWires().size());

        // Copy has different object references
        assertNotSame(circuit.getGates().get(0), copy.getGates().get(0));
    }
}
