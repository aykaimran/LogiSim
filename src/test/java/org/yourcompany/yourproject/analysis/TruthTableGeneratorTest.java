package org.yourcompany.yourproject.analysis;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.yourcompany.yourproject.businessLayer.analysis.TruthTableGenerator;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

public class TruthTableGeneratorTest {

    //helper to get a value from the output map
    private boolean out(Map<String, Boolean> map, String key) {
        return map.get(key);
    }

    // --------------------------------------------------------------------
    //  test 1: truth table for a single AND gate with 2 inputs
    // --------------------------------------------------------------------
    @Test
    void testTruthTableForSingleANDGate() {

        Circuit circuit = new Circuit("C1");
        AndGate g = new AndGate();
        circuit.addGate(g);

        TruthTableGenerator gen = new TruthTableGenerator();
        List<TruthTableGenerator.TruthTableRow> rows = gen.generateTruthTable(circuit);

        assertEquals(4, rows.size(), "AND gate must have 4 truth table rows");

        //row 0: A=0, B=0 → 0
        assertFalse(out(rows.get(0).getOutputs(), g.getName()));

        //row 1: 0,1 → 0
        assertFalse(out(rows.get(1).getOutputs(), g.getName()));

        //row 2: 1,0 → 0
        assertFalse(out(rows.get(2).getOutputs(), g.getName()));

        //row 3: 1,1 → 1
        assertTrue(out(rows.get(3).getOutputs(), g.getName()));
    }


    // --------------------------------------------------------------------
    //  test 2: truth table for single OR gate
    // --------------------------------------------------------------------
    @Test
    void testTruthTableForSingleORGate() {

        Circuit circuit = new Circuit("C2");
        OrGate g = new OrGate();
        circuit.addGate(g);

        TruthTableGenerator gen = new TruthTableGenerator();
        List<TruthTableGenerator.TruthTableRow> rows = gen.generateTruthTable(circuit);

        assertEquals(4, rows.size(), "OR gate must have 4 truth table rows");

        assertFalse(out(rows.get(0).getOutputs(), g.getName())); // 0,0
        assertTrue(out(rows.get(1).getOutputs(), g.getName()));  // 0,1
        assertTrue(out(rows.get(2).getOutputs(), g.getName()));  // 1,0
        assertTrue(out(rows.get(3).getOutputs(), g.getName()));  // 1,1
    }


    // --------------------------------------------------------------------
    //  test 3: truth table for NOT gate
    // --------------------------------------------------------------------
    @Test
    void testTruthTableForSingleNOTGate() {

        Circuit circuit = new Circuit("C3");
        NotGate g = new NotGate();
        circuit.addGate(g);

        TruthTableGenerator gen = new TruthTableGenerator();
        List<TruthTableGenerator.TruthTableRow> rows = gen.generateTruthTable(circuit);

        assertEquals(2, rows.size(), "NOT gate must have 2 rows");

        assertTrue(out(rows.get(0).getOutputs(), g.getName()));  // input 0 → output 1
        assertFalse(out(rows.get(1).getOutputs(), g.getName())); // input 1 → output 0
    }


    // --------------------------------------------------------------------
    //  test 4: connected circuit: (A AND B) → NOT → output
    // --------------------------------------------------------------------
    @Test
    void testTruthTableForConnectedCircuit() {

        Circuit circuit = new Circuit("C4");

        AndGate and = new AndGate();
        NotGate not = new NotGate();

        circuit.addGate(and);
        circuit.addGate(not);

        // connect AND output → NOT input
        circuit.connectGates(and, 0, not, 0);

        TruthTableGenerator gen = new TruthTableGenerator();
        List<TruthTableGenerator.TruthTableRow> rows = gen.generateTruthTable(circuit);

        assertEquals(4, rows.size(), "Combined AND→NOT circuit should have 4 rows");

        // TRUTH TABLE of NAND
        // A B | AND | NOT
        // 0 0 |  0  |  1
        // 0 1 |  0  |  1
        // 1 0 |  0  |  1
        // 1 1 |  1  |  0

        assertTrue(out(rows.get(0).getOutputs(), not.getName()));  // 0,0 → 1
        assertTrue(out(rows.get(1).getOutputs(), not.getName()));  // 0,1 → 1
        assertTrue(out(rows.get(2).getOutputs(), not.getName()));  // 1,0 → 1
        assertFalse(out(rows.get(3).getOutputs(), not.getName())); // 1,1 → 0
    }
}
