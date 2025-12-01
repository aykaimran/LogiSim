
package org.yourcompany.yourproject.businessLayer.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.CircuitComponent;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;

public class BooleanExpressionGenerator {

    private final Map<ComponentBase, String> memo = new HashMap<>();
    private Circuit topLevelCircuit;

    /**
     * Public entry point
     */
    public String getExpression(ComponentBase gate, Circuit circuit) {
        memo.clear();
        topLevelCircuit = circuit; // Store the top-level circuit
        return buildExpression(gate, 0, circuit);
    }

    /**
     * Main recursive expression builder
     */
    private String buildExpression(ComponentBase gate, int port, Circuit currentCircuit) {

        String key = gate.getName() + "_" + System.identityHashCode(gate);
        if (memo.containsKey(gate)) {
            return memo.get(gate);
        }

        String gateType = gate.getClass().getSimpleName();
        String expr;

        // External input (no inputs) - this is a primary input
        if (gate.getInputs() == 0 && !(gate instanceof CircuitComponent)) {
            expr = gate.getName();
            memo.put(gate, expr);
            return expr;
        }

        // Handle CircuitComponent specially
        if (gate instanceof CircuitComponent) {
            CircuitComponent cc = (CircuitComponent) gate;
            Circuit inner = cc.getReferencedCircuit();

            // Get inner circuit output
            List<ComponentBase> outputs = inner.identifyOutputNodes();
            if (outputs.isEmpty()) {
                expr = "[" + cc.getCircuitDisplayName() + "_EMPTY]";
                memo.put(gate, expr);
                return expr;
            }

            ComponentBase innerOut = outputs.get(0);

            // Build expression for inner circuit output
            String innerExpr = buildExpression(innerOut, 0, inner);
            expr = "[" + cc.getCircuitDisplayName() + ": " + innerExpr + "]";
            memo.put(gate, expr);
            return expr;
        }

        // For regular gates, resolve inputs recursively
        List<String> inputExprs = new ArrayList<>();
        for (int i = 0; i < gate.getInputs(); i++) {
            String srcExpr = findInputSourceExpression(gate, i, currentCircuit);
            inputExprs.add(srcExpr);
        }

        // Gate type expression building
        switch (gateType) {
            case "AndGate":
                expr = "(" + inputExprs.get(0) + " · " + inputExprs.get(1) + ")";
                break;
            case "OrGate":
                expr = "(" + inputExprs.get(0) + " + " + inputExprs.get(1) + ")";
                break;
            case "NotGate":
                expr = "¬" + inputExprs.get(0);
                break;
            default:
                expr = gate.getName() + "(" + String.join(", ", inputExprs) + ")";
        }

        memo.put(gate, expr);
        return expr;
    }

    /**
     * Finds the source boolean expression of an input pin of a gate.
     */
    private String findInputSourceExpression(ComponentBase gate, int port, Circuit currentCircuit) {

        // Search for wires inside CURRENT circuit
        for (Connector wire : currentCircuit.getWires()) {
            if (wire.getToGate() == gate && wire.getToPort() == port) {
                return buildExpression(wire.getFromGate(), wire.getFromPort(), currentCircuit);
            }
        }

        if (currentCircuit != topLevelCircuit) {
            Circuit parentCircuit = findParentCircuit(currentCircuit, topLevelCircuit);
            if (parentCircuit != null) {
                // Find the CircuitComponent in parent that references currentCircuit
                CircuitComponent parentComponent = findCircuitComponentForCircuit(parentCircuit, currentCircuit);
                if (parentComponent != null) {
                    return findInputSourceExpression(parentComponent, port, parentCircuit);
                }
            }
        }

        // No wire found → treat as external input
        return gate.getName() + "_IN" + port;
    }

    /**
     * Find the parent circuit that contains a reference to the given nested circuit
     */
    private Circuit findParentCircuit(Circuit nestedCircuit, Circuit searchCircuit) {
        for (ComponentBase component : searchCircuit.getGates()) {
            if (component instanceof CircuitComponent) {
                CircuitComponent cc = (CircuitComponent) component;
                if (cc.getReferencedCircuit() == nestedCircuit) {
                    return searchCircuit;
                }
                // Recursively search in nested circuits
                Circuit result = findParentCircuit(nestedCircuit, cc.getReferencedCircuit());
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Find the CircuitComponent in a circuit that references the given nested
     * circuit
     */
    private CircuitComponent findCircuitComponentForCircuit(Circuit parentCircuit, Circuit nestedCircuit) {
        for (ComponentBase component : parentCircuit.getGates()) {
            if (component instanceof CircuitComponent) {
                CircuitComponent cc = (CircuitComponent) component;
                if (cc.getReferencedCircuit() == nestedCircuit) {
                    return cc;
                }
            }
        }
        return null;
    }
}