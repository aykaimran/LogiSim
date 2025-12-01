package org.yourcompany.yourproject.businessLayer.components;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
public abstract class ComponentBase {
  

    private String name;
    private int inputs;
    private int outputs;
    private Point position;
    private List<Boolean> inputValues;
    private List<Boolean> outputValues;
    private List<Connector> inputConnectors; // Wires connected to inputs
    private List<Connector> outputConnectors; // Wires connected to outputs
    private String id; // Unique identifier for the component

    public ComponentBase(String name, int inputs, int outputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.position = new Point(0, 0);
        this.inputValues = new ArrayList<>();
        this.outputValues = new ArrayList<>();
        this.inputConnectors = new ArrayList<>();
        this.outputConnectors = new ArrayList<>();
        this.id = generateId();
        
        // Initialize input/output values to false
        for (int i = 0; i < inputs; i++) {
            this.inputValues.add(false);
            this.inputConnectors.add(null);
        }
        for (int i = 0; i < outputs; i++) {
            this.outputValues.add(false);
            this.outputConnectors.add(null);
        }
    }

    private String generateId() {
        return name + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getInputs() { return inputs; }
    public void setInputs(int inputs) { this.inputs = inputs; }

    public int getOutputs() { return outputs; }
    public void setOutputs(int outputs) { this.outputs = outputs; }

    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }
    public void setPosition(int x, int y) { this.position = new Point(x, y); }

    public Boolean getInputValue(int index) {
        if (index >= 0 && index < inputValues.size()) {
            return inputValues.get(index);
        }
        return false;
    }

    public void setInputValue(int index, Boolean value) {
        if (index >= 0 && index < inputValues.size()) {
            inputValues.set(index, value);
            // When input changes, recompute output
            computeOutput();
        }
    }

/**
 * Set input value directly without triggering computeOutput (for copying state)
 */
public void setInputValueDirect(int index, Boolean value) {
    if (index >= 0 && index < inputValues.size()) {
        inputValues.set(index, value);
        // Don't call computeOutput here - that's the key difference
    }
}

/**
 * Set output value directly without triggering propagation (for copying state)
 */
public void setOutputValueDirect(int index, Boolean value) {
    if (index >= 0 && index < outputValues.size()) {
        outputValues.set(index, value);
        // Don't call propagateOutput here
    }
}

/**
 * Get direct access to input values list (package-private)
 */
public List<Boolean> getInputValuesDirect() {
    return inputValues;
}

/**
 * Get direct access to output values list (package-private)  
 */
public List<Boolean> getOutputValuesDirect() {
    return outputValues;
}

    public Boolean getOutputValue(int index) {
        if (index >= 0 && index < outputValues.size()) {
            return outputValues.get(index);
        }
        return false;
    }

    public void setOutputValue(int index, Boolean value) {
        if (index >= 0 && index < outputValues.size()) {
            outputValues.set(index, value);
            // Propagate output signal to connected wires
            propagateOutput(index);
        }
    }

    public List<Boolean> getInputValues() { return new ArrayList<>(inputValues); }
    public List<Boolean> getOutputValues() { return new ArrayList<>(outputValues); }

    public String getId() { return id; }

    // Port connection management
    public void connectInput(int portIndex, Connector wire) {
        if (portIndex >= 0 && portIndex < inputConnectors.size()) {
            inputConnectors.set(portIndex, wire);
        }
    }

    public void connectOutput(int portIndex, Connector wire) {
        if (portIndex >= 0 && portIndex < outputConnectors.size()) {
            outputConnectors.set(portIndex, wire);
        }
    }

    public Connector getInputConnector(int portIndex) {
        if (portIndex >= 0 && portIndex < inputConnectors.size()) {
            return inputConnectors.get(portIndex);
        }
        return null;
    }

    public Connector getOutputConnector(int portIndex) {
        if (portIndex >= 0 && portIndex < outputConnectors.size()) {
            return outputConnectors.get(portIndex);
        }
        return null;
    }

    public List<Connector> getInputConnectors() { return new ArrayList<>(inputConnectors); }
    public List<Connector> getOutputConnectors() { return new ArrayList<>(outputConnectors); }

    // Abstract method for computing output based on inputs (to be implemented by subclasses)
    protected abstract void computeOutput();

    // Propagate output signal to connected wires
    private void propagateOutput(int outputIndex) {
        Connector wire = getOutputConnector(outputIndex);
        if (wire != null && wire.getToGate() != null) {
            Boolean outputValue = getOutputValue(outputIndex);
            int inputPortIndex = wire.getToPort();
            wire.getToGate().setInputValue(inputPortIndex, outputValue);
        }
    }

    // Update input from connected wire
    public void updateInputFromWire(int inputIndex) {
        Connector wire = getInputConnector(inputIndex);
        if (wire != null && wire.getFromGate() != null) {
            int outputPortIndex = wire.getFromPort();
            Boolean inputValue = wire.getFromGate().getOutputValue(outputPortIndex);
            setInputValue(inputIndex, inputValue);
        }
    }

    // Get port positions for visual rendering (relative to component position)
    public Point getInputPortPosition(int portIndex) {
        if (inputs == 0) return position;
        int gateHeight = 70;
        int spacing = gateHeight / (inputs + 1);
        int y = position.y + spacing * (portIndex + 1);
        return new Point(position.x, y);
    }

    public Point getOutputPortPosition(int portIndex) {
        if (outputs == 0) return position;
        int gateHeight = 70;
        int gateWidth = 100;
        int spacing = gateHeight / (outputs + 1);
        int y = position.y + spacing * (portIndex + 1);
        return new Point(position.x + gateWidth, y);
    }

    //evaluate method to be implemented by subclasses
    public abstract void evaluate();
    

// Add a helper to get single output (for 1-output gates)
public Boolean getOutput() {
    return outputs > 0 ? outputValues.get(0) : false;
}

public void setOutput(Boolean value) {
    if (outputs > 0) {
        outputValues.set(0, value);
        propagateOutput(0);
    }
}
public abstract ComponentBase copy();

public abstract void update();
}
