package org.yourcompany.yourproject.businessLayer.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.CircuitComponent;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;

/**
 * Enhanced truth table generator that handles hierarchical circuits
 */
public class TruthTableGenerator {

    /**
     * Generates truth table by directly simulating the circuit for all input
     * combinations
     */
    public List<TruthTableRow> generateTruthTable(Circuit circuit) {
        System.out.println("=== TRUTH TABLE GENERATION STARTED ===");
        System.out.println("Circuit: " + circuit.getName());
        System.out.println("Gates: " + circuit.getGates().size());

        for (ComponentBase gate : circuit.getGates()) {
            System.out.println("  Gate: " + gate.getName() + " (" + gate.getClass().getSimpleName() + ")");
            if (gate instanceof CircuitComponent) {
                CircuitComponent comp = (CircuitComponent) gate;
                System.out.println("    -> CircuitComponent referencing: " + comp.getCircuitDisplayName());
            }
        }
        // 1. Find all input ports (unconnected gate inputs)
        List<InputPort> inputPorts = findInputPorts(circuit);
        System.out.println("Input ports: " + inputPorts);

        if (inputPorts.isEmpty()) {
            System.out.println("No input ports found!");
            return new ArrayList<>();
        }

        // 2. Get all gate names for outputs (show everything)
        List<String> allGateNames = getAllGateNames(circuit);
        System.out.println("All gates: " + allGateNames);

        // 3. Generate all input combinations
        List<TruthTableRow> table = new ArrayList<>();
        int inputCount = inputPorts.size();
        int combinations = (int) Math.pow(2, inputCount);

        System.out.println("Generating " + combinations + " combinations");

        for (int i = 0; i < combinations; i++) {
            System.out.println("\n--- Combination " + i + " ---");

            // Reset circuit (including hierarchical circuits)
            resetAllGates(circuit);

            // Apply inputs
            Map<String, Boolean> inputs = applyInputCombination(inputPorts, i);
            System.out.println("Inputs: " + inputs);

            // Simulate circuit (with hierarchical support)
            simulateCircuitHierarchical(circuit);

            // Collect outputs from all gates
            Map<String, Boolean> outputs = collectAllOutputs(circuit, allGateNames);
            System.out.println("Outputs: " + outputs);

            table.add(new TruthTableRow(inputs, outputs));
        }

        // Print with circuit order
        printTruthTable(table, circuit);

        return table;
    }

    /**
     * Enhanced circuit simulation that handles hierarchical circuits
     */
    private void simulateCircuitHierarchical(Circuit circuit) {
        // Multiple passes to ensure proper propagation through hierarchical circuits
        boolean changed;
        int maxIterations = 50;
        int iterations = 0;

        do {
            changed = false;

            // First, propagate signals through wires
            propagateSignals(circuit);

            // Then evaluate all gates (including circuit components)
            for (ComponentBase gate : circuit.getGates()) {
                // Store old output values
                boolean[] oldOutputs = new boolean[gate.getOutputs()];
                for (int i = 0; i < gate.getOutputs(); i++) {
                    oldOutputs[i] = gate.getOutputValue(i);
                }

                // Evaluate the gate (this handles CircuitComponent internally)
                evaluateGateEnhanced(gate);

                // Check if outputs changed
                for (int i = 0; i < gate.getOutputs(); i++) {
                    if (oldOutputs[i] != gate.getOutputValue(i)) {
                        changed = true;
                        break;
                    }
                }
            }

            iterations++;
            if (iterations > maxIterations) {
                System.out.println("Warning: Simulation exceeded maximum iterations");
                break;
            }

        } while (changed);

        System.out.println("Circuit simulation completed in " + iterations + " iterations");
    }

    /**
     * Enhanced gate evaluation that handles CircuitComponent
     */
    private void evaluateGateEnhanced(ComponentBase gate) {
        String gateType = gate.getClass().getSimpleName();

        if (gate instanceof CircuitComponent) {
            // PROPERLY handle CircuitComponent
            CircuitComponent circuitComp = (CircuitComponent) gate;
            System.out.println("  Evaluating CircuitComponent: " + circuitComp.getCircuitDisplayName());

            // Manually transfer inputs and evaluate internal circuit
            Circuit internalCircuit = circuitComp.getReferencedCircuit();
            if (internalCircuit != null) {
                // Transfer inputs from CircuitComponent to internal circuit
                transferInputsToInternalCircuit(circuitComp, internalCircuit);

                // Evaluate the internal circuit
                simulateCircuitHierarchical(internalCircuit);

                // Transfer outputs back
                transferOutputsFromInternalCircuit(circuitComp, internalCircuit);
            }
        } else {
            // Handle basic gates
            boolean output = false;

            switch (gateType) {
                case "AndGate":
                    if (gate.getInputs() >= 2) {
                        output = gate.getInputValue(0) && gate.getInputValue(1);
                    } else if (gate.getInputs() == 1) {
                        output = gate.getInputValue(0);
                    }
                    break;

                case "OrGate":
                    if (gate.getInputs() >= 2) {
                        output = gate.getInputValue(0) || gate.getInputValue(1);
                    } else if (gate.getInputs() == 1) {
                        output = gate.getInputValue(0);
                    }
                    break;

                case "NotGate":
                    if (gate.getInputs() >= 1) {
                        output = !gate.getInputValue(0);
                    }
                    break;

                default:
                    output = false;
            }

            // Set the output
            if (gate.getOutputs() > 0) {
                gate.setOutputValue(0, output);
            }

            System.out.println("  " + gate.getName() + " (" + gateType + ") -> " + output +
                    " [inputs: " + getInputValuesString(gate) + "]");
        }
    }

    /**
     * Transfer inputs from CircuitComponent to its internal circuit
     */
    private void transferInputsToInternalCircuit(CircuitComponent circuitComp, Circuit internalCircuit) {
        // Get ALL input ports from the internal circuit, not just input nodes
        List<InputPort> internalInputs = findInputPorts(internalCircuit);

        System.out.println("  Internal circuit has " + internalInputs.size() + " input ports");

        // Transfer inputs sequentially
        for (int i = 0; i < Math.min(circuitComp.getInputs(), internalInputs.size()); i++) {
            Boolean inputValue = circuitComp.getInputValue(i);
            InputPort internalPort = internalInputs.get(i);

            System.out.println("    Setting internal input " + internalPort.name + " to " + inputValue);

            internalPort.gate.setInputValueDirect(internalPort.portIndex, inputValue);
        }
    }

    /**
     * Transfer outputs from internal circuit back to CircuitComponent
     */
    private void transferOutputsFromInternalCircuit(CircuitComponent circuitComp, Circuit internalCircuit) {
        // Get ALL gates that can produce outputs
        List<ComponentBase> outputGates = internalCircuit.identifyOutputNodes();

        System.out.println("  Internal circuit has " + outputGates.size() + " output gates");

        // Transfer outputs sequentially
        for (int i = 0; i < Math.min(circuitComp.getOutputs(), outputGates.size()); i++) {
            ComponentBase outputGate = outputGates.get(i);
            Boolean outputValue = outputGate.getOutputValue(0); // Get first output

            System.out.println("    Setting CircuitComponent output " + i + " to " + outputValue);

            circuitComp.setOutputValueDirect(i, outputValue);
        }
    }

    /**
     * Propagate signals through all wires in the circuit
     */
    private void propagateSignals(Circuit circuit) {
        for (Connector wire : circuit.getWires()) {
            wire.propagateSignal();
        }
    }

    private String getInputValuesString(ComponentBase gate) {
        List<String> inputValues = new ArrayList<>();
        for (int i = 0; i < gate.getInputs(); i++) {
            inputValues.add(gate.getInputValue(i) ? "1" : "0");
        }
        return String.join(",", inputValues);
    }

    private List<InputPort> findInputPorts(Circuit circuit) {
        List<InputPort> inputPorts = new ArrayList<>();

        for (ComponentBase gate : circuit.getGates()) {
            // Special handling for CircuitComponent - only unconnected inputs are external
            if (gate instanceof CircuitComponent) {
                CircuitComponent comp = (CircuitComponent) gate;
                for (int port = 0; port < gate.getInputs(); port++) {
                    // Check if this CircuitComponent input is connected internally
                    if (!isInputConnected(circuit, gate, port)) {
                        inputPorts.add(new InputPort(gate, port));
                    } else {
                        System.out.println("  CircuitComponent input " + gate.getName() + "_IN" + port
                                + " is connected internally, skipping");
                    }
                }
            } else {
                // Regular gates - only unconnected inputs
                for (int port = 0; port < gate.getInputs(); port++) {
                    if (!isInputConnected(circuit, gate, port)) {
                        inputPorts.add(new InputPort(gate, port));
                    }
                }
            }
        }

        System.out.println("Found " + inputPorts.size() + " input ports: " + inputPorts);
        return inputPorts;
    }

    private boolean isInputConnected(Circuit circuit, ComponentBase gate, int port) {
        // Check if there's a wire connected to this input port
        for (Connector wire : circuit.getWires()) {
            if (wire.getToGate() == gate && wire.getToPort() == port) {
                // For CircuitComponent, also check if the source is within the same circuit
                if (gate instanceof CircuitComponent) {
                    ComponentBase sourceGate = wire.getFromGate();
                    // If source is within the same circuit, it's internally connected
                    if (sourceGate != null && circuit.getGates().contains(sourceGate)) {
                        return true;
                    }
                } else {
                    // For regular gates, any connection means it's connected
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getAllGateNames(Circuit circuit) {
        List<String> names = new ArrayList<>();
        for (ComponentBase gate : circuit.getGates()) {
            names.add(gate.getName());
        }
        return names;
    }

    /**
     * Enhanced reset that handles hierarchical circuits
     */
    private void resetAllGates(Circuit circuit) {
        for (ComponentBase gate : circuit.getGates()) {
            // Reset inputs
            for (int i = 0; i < gate.getInputs(); i++) {
                gate.setInputValueDirect(i, false);
            }
            // Reset outputs
            for (int i = 0; i < gate.getOutputs(); i++) {
                gate.setOutputValueDirect(i, false);
            }

            // If it's a CircuitComponent, reset its internal circuit too
            if (gate instanceof CircuitComponent) {
                CircuitComponent circuitComp = (CircuitComponent) gate;
                Circuit internalCircuit = circuitComp.getReferencedCircuit();
                if (internalCircuit != null) {
                    resetInternalCircuit(internalCircuit);
                }
            }
        }
    }

    /**
     * Recursively reset internal circuits
     */
    private void resetInternalCircuit(Circuit circuit) {
        for (ComponentBase gate : circuit.getGates()) {
            // Reset inputs
            for (int i = 0; i < gate.getInputs(); i++) {
                gate.setInputValueDirect(i, false);
            }
            // Reset outputs
            for (int i = 0; i < gate.getOutputs(); i++) {
                gate.setOutputValueDirect(i, false);
            }

            // Recursively reset any nested CircuitComponents
            if (gate instanceof CircuitComponent) {
                CircuitComponent circuitComp = (CircuitComponent) gate;
                Circuit internalCircuit = circuitComp.getReferencedCircuit();
                if (internalCircuit != null) {
                    resetInternalCircuit(internalCircuit);
                }
            }
        }
    }

    private Map<String, Boolean> applyInputCombination(List<InputPort> inputPorts, int combination) {
        Map<String, Boolean> inputs = new HashMap<>();

        for (int i = 0; i < inputPorts.size(); i++) {
            InputPort port = inputPorts.get(i);
            boolean value = ((combination >> i) & 1) == 1;

            // Set the input value on the gate
            port.gate.setInputValueDirect(port.portIndex, value);
            inputs.put(port.name, value);
        }

        return inputs;
    }

    private Map<String, Boolean> collectAllOutputs(Circuit circuit, List<String> gateNames) {
        Map<String, Boolean> outputs = new HashMap<>();

        for (String gateName : gateNames) {
            ComponentBase gate = findGateByName(circuit, gateName);
            if (gate != null && gate.getOutputs() > 0) {
                outputs.put(gateName, gate.getOutputValue(0));
            }
        }

        return outputs;
    }

    private ComponentBase findGateByName(Circuit circuit, String name) {
        for (ComponentBase gate : circuit.getGates()) {
            if (gate.getName().equals(name)) {
                return gate;
            }
        }
        return null;
    }

    /**
     * Print formatted truth table in circuit order
     */
    public static void printTruthTable(List<TruthTableRow> table, Circuit circuit) {
        if (table.isEmpty())
            return;

        // Get all input names (alphabetical order is fine for inputs)
        List<String> inputNames = new ArrayList<>(table.get(0).getInputs().keySet());
        Collections.sort(inputNames);

        // Get output names in CIRCUIT ORDER (the order gates appear in the circuit)
        List<String> outputNames = getOutputsInCircuitOrder(circuit);

        // Print header
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRUTH TABLE (Hierarchical Circuits Supported)");
        System.out.println("=".repeat(80));

        // Input headers
        System.out.print("| ");
        for (String input : inputNames) {
            System.out.printf("%-8s | ", input);
        }

        // Output headers - in circuit order
        System.out.print("| ");
        for (String output : outputNames) {
            System.out.printf("%-8s | ", output);
        }
        System.out.println();

        // Separator
        System.out.print("|");
        for (int i = 0; i < inputNames.size(); i++) {
            System.out.print("----------|");
        }
        System.out.print("|");
        for (int i = 0; i < outputNames.size(); i++) {
            System.out.print("----------|");
        }
        System.out.println();

        // Rows
        for (TruthTableRow row : table) {
            // Input values
            System.out.print("| ");
            for (String input : inputNames) {
                String value = row.getInput(input) ? "1" : "0";
                System.out.printf("%-8s | ", value);
            }

            // Output values - in circuit order
            System.out.print("| ");
            for (String output : outputNames) {
                String value = row.getOutput(output) ? "1" : "0";
                System.out.printf("%-8s | ", value);
            }
            System.out.println();
        }

        System.out.println("=".repeat(80));
    }

    /**
     * Get output names in the order they appear in the circuit (not alphabetical)
     */
    private static List<String> getOutputsInCircuitOrder(Circuit circuit) {
        List<String> outputNames = new ArrayList<>();
        for (ComponentBase gate : circuit.getGates()) {
            outputNames.add(gate.getName());
        }
        return outputNames;
    }

    // InputPort inner class
    private static class InputPort {
        final ComponentBase gate;
        final int portIndex;
        final String name;

        InputPort(ComponentBase gate, int portIndex) {
            this.gate = gate;
            this.portIndex = portIndex;
            this.name = gate.getName() + "_IN" + portIndex;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // TruthTableRow inner class
    public static class TruthTableRow {
        private final Map<String, Boolean> inputs;
        private final Map<String, Boolean> outputs;

        public TruthTableRow(Map<String, Boolean> inputs, Map<String, Boolean> outputs) {
            this.inputs = inputs;
            this.outputs = outputs;
        }

        public Map<String, Boolean> getInputs() {
            return new HashMap<>(inputs);
        }

        public Map<String, Boolean> getOutputs() {
            return new HashMap<>(outputs);
        }

        public boolean getInput(String name) {
            return inputs.getOrDefault(name, false);
        }

        public boolean getOutput(String name) {
            return outputs.getOrDefault(name, false);
        }
    }
}