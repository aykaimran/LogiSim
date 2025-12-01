package org.yourcompany.yourproject.businessLayer.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Circuit {
    private String name;
    private List<ComponentBase> gates = new ArrayList<>();
    private List<Connector> wires = new ArrayList<>();
    private List<ComponentBase> inputNodes = new ArrayList<>();
    private List<ComponentBase> outputNodes = new ArrayList<>();

    // Constructor
    public Circuit(String name) {
        this.name = name;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ComponentBase> getGates() {
        return gates;
    }

    public void setGates(List<ComponentBase> gates) {
        this.gates = gates;
    }

    public List<Connector> getWires() {
        return wires;
    }

    public void setWires(List<Connector> wires) {
        this.wires = wires;
    }

    public List<ComponentBase> getInputNodes() {
        return inputNodes;
    }

    public List<ComponentBase> getOutputNodes() {
        return outputNodes;
    }

    // Add a component (gate) to the circuit
    public void addGate(ComponentBase gate) {
        if (gate != null && !gates.contains(gate)) {
            gates.add(gate);
        }
    }

    // Remove a component from the circuit
    public void removeGate(ComponentBase gate) {
        if (gate != null) {
            // Disconnect all wires connected to this gate
            List<Connector> toRemove = new ArrayList<>();
            for (Connector wire : wires) {
                if (wire.getFromGate() == gate || wire.getToGate() == gate) {
                    wire.disconnect();
                    toRemove.add(wire);
                }
            }
            wires.removeAll(toRemove);
            gates.remove(gate);

            // Also remove from input/output nodes if present
            inputNodes.remove(gate);
            outputNodes.remove(gate);
        }
    }

    // Add a wire/connector to the circuit
    public void addWire(Connector wire) {
        if (wire != null && !wires.contains(wire)) {
            wires.add(wire);
            wire.updateConnectionPoints();
        }
    }

    // Remove a wire from the circuit
    public void removeWire(Connector wire) {
        if (wire != null) {
            wire.disconnect();
            wires.remove(wire);
        }
    }

    // Create a connection between two gates
    public Connector connectGates(ComponentBase fromGate, int fromPort, ComponentBase toGate, int toPort) {
        if (fromGate == null || toGate == null)
            return null;
        if (fromPort < 0 || fromPort >= fromGate.getOutputs())
            return null;
        if (toPort < 0 || toPort >= toGate.getInputs())
            return null;

        // Check if output port is already connected
        if (fromGate.getOutputConnector(fromPort) != null) {
            return null; // Port already connected
        }

        // Check if input port is already connected
        if (toGate.getInputConnector(toPort) != null) {
            return null; // Port already connected
        }

        // Create new connector
        Connector wire = new Connector(fromGate, fromPort, toGate, toPort);
        addWire(wire);
        return wire;
    }

    // Update all wire positions (should be called when gates are moved)
    public void updateWirePositions() {
        for (Connector wire : wires) {
            wire.updateConnectionPoints();
        }
    }

    // Find a gate by its ID
    public ComponentBase getGateById(String id) {
        for (ComponentBase gate : gates) {
            if (gate.getId().equals(id)) {
                return gate;
            }
        }
        return null;
    }

    // Find a wire by its ID
    public Connector getWireById(String id) {
        for (Connector wire : wires) {
            if (wire.getId().equals(id)) {
                return wire;
            }
        }
        return null;
    }

    // Propagate signals through the circuit
    public void propagateSignals() {
        for (Connector wire : wires) {
            wire.propagateSignal();
        }
    }

    // Update component positions in the circuit
    public void updateComponentPositions() {
        updateWirePositions();
    }

    // Clear all components and wires
    public void clear() {
        for (Connector wire : wires) {
            wire.disconnect();
        }
        wires.clear();
        gates.clear();
        inputNodes.clear();
        outputNodes.clear();
    }

    /**
     * Get gates in topological order for proper evaluation
     */
    private List<ComponentBase> getTopologicalOrder() {
        List<ComponentBase> result = new ArrayList<>();
        Map<ComponentBase, Integer> inDegree = new HashMap<>();
        Queue<ComponentBase> queue = new LinkedList<>();

        // Calculate in-degree for each gate
        for (ComponentBase gate : gates) {
            inDegree.put(gate, 0);
        }

        for (Connector wire : wires) {
            ComponentBase toGate = wire.getToGate();
            inDegree.put(toGate, inDegree.get(toGate) + 1);
        }

        // Add gates with zero in-degree to queue
        for (ComponentBase gate : gates) {
            if (inDegree.get(gate) == 0) {
                queue.add(gate);
            }
        }

        // Process queue
        while (!queue.isEmpty()) {
            ComponentBase current = queue.poll();
            result.add(current);

            // Decrease in-degree of neighbors
            for (int i = 0; i < current.getOutputs(); i++) {
                Connector outputConnector = current.getOutputConnector(i);
                if (outputConnector != null) {
                    ComponentBase neighbor = outputConnector.getToGate();
                    if (gates.contains(neighbor)) {
                        int newDegree = inDegree.get(neighbor) - 1;
                        inDegree.put(neighbor, newDegree);
                        if (newDegree == 0) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // If not all gates are processed, return original order
        if (result.size() != gates.size()) {
            System.err.println("Warning: Circuit may have cycles, using original order for: " + name);
            return new ArrayList<>(gates);
        }

        return result;
    }

    /**
     * Get input value for a circuit component (for hierarchical circuits)
     */
    public Boolean getInputValue(int index) {
        List<ComponentBase> inputNodes = identifyInputNodes();
        int currentIndex = 0;

        for (ComponentBase inputNode : inputNodes) {
            for (int port = 0; port < inputNode.getInputs(); port++) {
                Connector connector = inputNode.getInputConnector(port);
                if (connector == null || !gates.contains(connector.getFromGate())) {
                    if (currentIndex == index) {
                        return inputNode.getInputValue(port);
                    }
                    currentIndex++;
                }
            }
        }

        return false;
    }

    /**
     * Set input value for a circuit component (for hierarchical circuits)
     */
    public void setInputValue(int index, Boolean value) {
        List<ComponentBase> inputNodes = identifyInputNodes();
        int currentIndex = 0;

        for (ComponentBase inputNode : inputNodes) {
            for (int port = 0; port < inputNode.getInputs(); port++) {
                Connector connector = inputNode.getInputConnector(port);
                if (connector == null || !gates.contains(connector.getFromGate())) {
                    if (currentIndex == index) {
                        inputNode.setInputValueDirect(port, value);
                        // Immediately evaluate to propagate changes
                        inputNode.evaluate();
                        return;
                    }
                    currentIndex++;
                }
            }
        }
    }

    /**
     * Get output value for a circuit component (for hierarchical circuits)
     */
    public Boolean getOutputValue(int index) {
        List<ComponentBase> outputNodes = identifyOutputNodes();
        int currentIndex = 0;

        for (ComponentBase outputNode : outputNodes) {
            for (int port = 0; port < outputNode.getOutputs(); port++) {
                Connector connector = outputNode.getOutputConnector(port);
                if (connector == null || !gates.contains(connector.getToGate())) {
                    if (currentIndex == index) {
                        return outputNode.getOutputValue(port);
                    }
                    currentIndex++;
                }
            }
        }

        return false;
    }

    /**
     * Create a proper deep copy of the circuit with all internal logic preserved
     */
    public Circuit createCopy(String newName) {
        Circuit copy = new Circuit(newName);

        // Create mapping from original gates to copied gates
        Map<ComponentBase, ComponentBase> gateMap = new HashMap<>();

        // First pass: copy all basic gates
        for (ComponentBase originalGate : gates) {
            if (!(originalGate instanceof CircuitComponent)) {
                ComponentBase copiedGate = originalGate.copy();
                if (copiedGate != null) {
                    copiedGate.setPosition(originalGate.getPosition().x, originalGate.getPosition().y);
                    copy.addGate(copiedGate);
                    gateMap.put(originalGate, copiedGate);
                }
            }
        }

        // Second pass: copy circuit components (they might reference other circuits)
        for (ComponentBase originalGate : gates) {
            if (originalGate instanceof CircuitComponent) {
                CircuitComponent originalComp = (CircuitComponent) originalGate;
                CircuitComponent copiedComp = (CircuitComponent) originalComp.copy();
                if (copiedComp != null) {
                    copiedComp.setPosition(originalGate.getPosition().x, originalGate.getPosition().y);
                    copy.addGate(copiedComp);
                    gateMap.put(originalGate, copiedComp);
                }
            }
        }

        // Copy all wires using the gate mapping
        for (Connector originalWire : wires) {
            ComponentBase fromGate = originalWire.getFromGate();
            ComponentBase toGate = originalWire.getToGate();

            ComponentBase copiedFromGate = gateMap.get(fromGate);
            ComponentBase copiedToGate = gateMap.get(toGate);

            if (copiedFromGate != null && copiedToGate != null) {
                int fromPort = originalWire.getFromPort();
                int toPort = originalWire.getToPort();

                copy.connectGates(copiedFromGate, fromPort, copiedToGate, toPort);
            }
        }

        return copy;
    }

    /**
     * Debug method to print circuit structure
     */
    public void printCircuitStructure() {
        System.out.println("=== Circuit: " + name + " ===");
        System.out.println("Gates: " + gates.size());
        System.out.println("Wires: " + wires.size());
        System.out.println("Input nodes: " + identifyInputNodes().size());
        System.out.println("Output nodes: " + identifyOutputNodes().size());

        for (ComponentBase gate : gates) {
            System.out.println("  " + gate.getId() + " at " + gate.getPosition());
        }
    }

    @Override
    public String toString() {
        return "Circuit{" +
                "name='" + name + '\'' +
                ", gates=" + gates.size() +
                ", wires=" + wires.size() +
                '}';
    }

    /**
     * Evaluate the entire circuit using sequential propagation
     */
    public void evaluate() {
        if (gates.isEmpty())
            return;

        // Reset all gate states
        resetGateStates();

        // Get all gates in evaluation order (inputs first, then dependencies)
        List<ComponentBase> evaluationOrder = getEvaluationOrder();

        // Evaluate each gate in order
        for (ComponentBase gate : evaluationOrder) {
            // Propagate inputs to this gate first
            propagateInputsToGate(gate);

            // Then evaluate the gate
            gate.evaluate();
        }

        // One final propagation to ensure outputs are set
        propagateSignals();
    }

    /**
     * Get gates in proper evaluation order (topological sort)
     */
    private List<ComponentBase> getEvaluationOrder() {
        List<ComponentBase> result = new ArrayList<>();
        List<ComponentBase> remaining = new ArrayList<>(gates);

        // Start with input nodes (gates with no internal dependencies)
        List<ComponentBase> inputNodes = identifyInputNodes();
        result.addAll(inputNodes);
        remaining.removeAll(inputNodes);

        // Then add the rest in the order they appear (simple approach)
        result.addAll(remaining);

        return result;
    }

    /**
     * Propagate all input signals to a specific gate
     */
    private void propagateInputsToGate(ComponentBase gate) {
        for (int i = 0; i < gate.getInputs(); i++) {
            Connector inputConnector = gate.getInputConnector(i);
            if (inputConnector != null) {
                inputConnector.propagateSignal();
            }
        }
    }

    /**
     * Improved reset that preserves external connections
     */
    private void resetGateStates() {
        for (ComponentBase gate : gates) {
            // Only reset inputs that are connected internally
            for (int i = 0; i < gate.getInputs(); i++) {
                Connector connector = gate.getInputConnector(i);
                if (connector != null && gates.contains(connector.getFromGate())) {
                    gate.setInputValueDirect(i, false);
                }
            }
            // Reset all outputs
            for (int i = 0; i < gate.getOutputs(); i++) {
                gate.setOutputValueDirect(i, false);
            }
        }
    }

    /**
     * SIMPLIFIED input node identification
     */
    public List<ComponentBase> identifyInputNodes() {
        List<ComponentBase> inputs = new ArrayList<>();

        for (ComponentBase gate : gates) {
            // Consider a gate an input node if it has at least one unconnected input
            // or is connected to something outside this circuit
            for (int i = 0; i < gate.getInputs(); i++) {
                Connector connector = gate.getInputConnector(i);
                if (connector == null) {
                    inputs.add(gate);
                    break;
                } else if (!gates.contains(connector.getFromGate())) {
                    inputs.add(gate);
                    break;
                }
            }
        }

        return inputs;
    }

    /**
     * SIMPLIFIED output node identification
     */
    public List<ComponentBase> identifyOutputNodes() {
        List<ComponentBase> outputs = new ArrayList<>();

        for (ComponentBase gate : gates) {
            // Consider a gate an output node if it has at least one unconnected output
            // or is connected to something outside this circuit
            for (int i = 0; i < gate.getOutputs(); i++) {
                Connector connector = gate.getOutputConnector(i);
                if (connector == null) {
                    outputs.add(gate);
                    break;
                } else if (!gates.contains(connector.getToGate())) {
                    outputs.add(gate);
                    break;
                }
            }
        }

        return outputs;
    }

    /**
     * Count circuit inputs - SIMPLIFIED
     */

    public int countCircuitInputs() {
        int count = 0;

        for (ComponentBase gate : gates) {
            for (int i = 0; i < gate.getInputs(); i++) {
                Connector c = gate.getInputConnector(i);

                // Port is external if NOT connected OR connected from outside
                if (c == null || !gates.contains(c.getFromGate())) {
                    count++;
                }
            }
        }
        return count;
    }

    public void addComponent(ComponentBase component) {
        addGate(component);
    }

    /**
     * Count circuit outputs - SIMPLIFIED
     */

    public int countCircuitOutputs() {
        int count = 0;

        for (ComponentBase gate : gates) {
            for (int i = 0; i < gate.getOutputs(); i++) {
                Connector c = gate.getOutputConnector(i);

                // Port is external if NOT connected OR connected to outside
                if (c == null || !gates.contains(c.getToGate())) {
                    count++;
                }
            }
        }
        return count;
    }

}