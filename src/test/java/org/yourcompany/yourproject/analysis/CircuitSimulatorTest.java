package org.yourcompany.yourproject.analysis;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.businessLayer.analysis.CircuitSimulator;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

public class CircuitSimulatorTest {

    // -------------------------------
    //  test 1: single AND gate
    // -------------------------------
    @Test
    void testANDGateSimulation() {
        Circuit circuit = new Circuit("ANDCircuit");
        AndGate and = new AndGate();
        circuit.addGate(and);

        Map<String, Boolean> inputs = new HashMap<>();
        inputs.put(and.getName() + "_in0", true);
        inputs.put(and.getName() + "_in1", false);

        Map<String, Boolean> outputs = CircuitSimulator.run(circuit, inputs);

        assertEquals(1, outputs.size()); //aik output ani chhaiye
        assertFalse(outputs.get(and.getName())); // true AND false = false

        // test 1,1 → true
        inputs.put(and.getName() + "_in1", true);
        outputs = CircuitSimulator.run(circuit, inputs);
        assertTrue(outputs.get(and.getName()));
    }

    // -------------------------------
    //  test 2: single OR gate
    // -------------------------------
    @Test
    void testORGateSimulation() {
        Circuit circuit = new Circuit("ORCircuit");
        OrGate or = new OrGate();
        circuit.addGate(or);

        Map<String, Boolean> inputs = new HashMap<>();
        inputs.put(or.getName() + "_in0", false);
        inputs.put(or.getName() + "_in1", false);

        Map<String, Boolean> outputs = CircuitSimulator.run(circuit, inputs);
        assertFalse(outputs.get(or.getName()));

        inputs.put(or.getName() + "_in0", true);
        outputs = CircuitSimulator.run(circuit, inputs);
        assertTrue(outputs.get(or.getName()));
    }

    // -------------------------------
    //  test 3: single NOT gate
    // -------------------------------
    @Test
    void testNOTGateSimulation() {
        Circuit circuit = new Circuit("NOTCircuit");
        NotGate not = new NotGate();
        circuit.addGate(not);

        Map<String, Boolean> inputs = new HashMap<>();
        inputs.put(not.getName() + "_in0", false);
        Map<String, Boolean> outputs = CircuitSimulator.run(circuit, inputs);
        assertTrue(outputs.get(not.getName()));

        inputs.put(not.getName() + "_in0", true);
        outputs = CircuitSimulator.run(circuit, inputs);
        assertFalse(outputs.get(not.getName()));
    }

    // -------------------------------
    //  test 4: connected circuit: AND → NOT (NAND)
    // -------------------------------
    @Test
    void testConnectedCircuitSimulation() {
        Circuit circuit = new Circuit("NANDCircuit");

        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(and);
        circuit.addGate(not);

        // Connect AND → NOT
        circuit.connectGates(and, 0, not, 0);

        Map<String, Boolean> inputs = new HashMap<>();
        inputs.put(and.getName() + "_in0", true);
        inputs.put(and.getName() + "_in1", true);

        Map<String, Boolean> outputs = CircuitSimulator.run(circuit, inputs);

        // NAND truth table: 1 AND 1 → NOT → 0
        assertFalse(outputs.get(not.getName()));

        // 1 AND 0 → NOT → 1
        inputs.put(and.getName() + "_in1", false);
        outputs = CircuitSimulator.run(circuit, inputs);
        assertTrue(outputs.get(not.getName()));

        // 0 AND 0 → NOT → 1
        inputs.put(and.getName() + "_in0", false);
        outputs = CircuitSimulator.run(circuit, inputs);
        assertTrue(outputs.get(not.getName()));
    }

    // -------------------------------
    //  test 5: chain OR → AND → NOT
    // -------------------------------
    @Test
    void testLongerCircuitSimulation() {
        Circuit circuit = new Circuit("ComplexCircuit");

        OrGate or = new OrGate();
        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(or);
        circuit.addGate(and);
        circuit.addGate(not);

        // Connect OR → AND → NOT
        circuit.connectGates(or, 0, and, 0);
        circuit.connectGates(and, 0, not, 0);

        Map<String, Boolean> inputs = new HashMap<>();
        inputs.put(or.getName() + "_in0", true);
        inputs.put(or.getName() + "_in1", false);
        inputs.put(and.getName() + "_in1", true); // second input of AND

        Map<String, Boolean> outputs = CircuitSimulator.run(circuit, inputs);

        // OR output = true OR false = true
        // AND input = true AND true = true
        // NOT output = false
        assertFalse(outputs.get(not.getName()));
    }
}
