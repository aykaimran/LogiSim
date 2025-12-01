package org.yourcompany.yourproject.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.businessLayer.analysis.BooleanExpressionGenerator;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

public class BooleanExpressionGeneratorTest {

    // -------------------------------
    //  test 1: single AND gate
    // -------------------------------
    @Test
    void testSingleANDGateExpression() {
        Circuit circuit = new Circuit("ANDCircuit");
        AndGate and = new AndGate();
        circuit.addGate(and);

        BooleanExpressionGenerator generator = new BooleanExpressionGenerator();
        String expr = generator.getExpression(and, circuit);

        // AND gate inputs are treated as external inputs
        assertEquals("(AND2_IN0 · AND2_IN1)", expr);
    }

    // -------------------------------
    //  test 2: single OR gate
    // -------------------------------
    @Test
    void testSingleORGateExpression() {
        Circuit circuit = new Circuit("ORCircuit");
        OrGate or = new OrGate();
        circuit.addGate(or);

        BooleanExpressionGenerator generator = new BooleanExpressionGenerator();
        String expr = generator.getExpression(or, circuit);

        assertEquals("(Or2_IN0 + Or2_IN1)".replace("·", "+"), expr); // OR logic uses "+"
    }

    // -------------------------------
    //  test 3: single NOT gate
    // -------------------------------
    @Test
    void testSingleNOTGateExpression() {
        Circuit circuit = new Circuit("NOTCircuit");
        NotGate not = new NotGate();
        circuit.addGate(not);

        BooleanExpressionGenerator generator = new BooleanExpressionGenerator();
        String expr = generator.getExpression(not, circuit);

        assertEquals("¬NOT3_IN0", expr);
    }

    // -------------------------------
    //  test 4: connected AND → NOT (NAND)
    // -------------------------------
    @Test
    void testConnectedANDNOTExpression() {
        Circuit circuit = new Circuit("NANDCircuit");

        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(and);
        circuit.addGate(not);

        // Connect AND → NOT
        circuit.connectGates(and, 0, not, 0);

        BooleanExpressionGenerator generator = new BooleanExpressionGenerator();
        String expr = generator.getExpression(not, circuit);

        // Expect: NOT(AND inputs) → ¬(AND1 · AND1_IN1)
        assertEquals("¬(AND3_IN0 · AND3_IN1)", expr);
    }

    // -------------------------------
    //  test 5: OR → AND → NOT
    // -------------------------------
    @Test
    void testLongerCircuitExpression() {
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

        BooleanExpressionGenerator generator = new BooleanExpressionGenerator();
        String expr = generator.getExpression(not, circuit);

        // Expected: ¬((OR1 + OR1_IN1) · AND1_IN1)
        assertEquals("¬((Or1_IN0 + Or1_IN1) · AND1_IN1)", expr);
    }
}
