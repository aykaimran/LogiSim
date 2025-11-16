package org.yourcompany.yourproject.businessLayer.components;
public abstract class ComponentBase {
    private String name;
    private int inputs;
    private int outputs;

    public ComponentBase(String name) {
        this.name = name;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getInputs() { return inputs; }
    public void setInputs(int inputs) { this.inputs = inputs; }

    public int getOutputs() { return outputs; }
    public void setOutputs(int outputs) { this.outputs = outputs; }

    //evaluate method to be implemented by subclasses
    public abstract void evaluate();
}