package org.yourcompany.yourproject.businessLayer.components;

import java.util.List;

public class CircuitComponent extends ComponentBase {
    private Circuit referencedCircuit;
    private String circuitName;
    
    public CircuitComponent(Circuit circuit, String name) {
        super("CIRCUIT:" + name, countCircuitInputs(circuit), countCircuitOutputs(circuit));
        this.referencedCircuit = circuit;
        this.circuitName = name;
        
    }
    
    @Override
    public void evaluate() {
        computeOutput();
    }
    
    @Override
    public void update() {
        computeOutput();
    }
    
    @Override
    protected void computeOutput() {
        if (referencedCircuit != null) {
            try {
                // Set the inputs from this component to the referenced circuit
                setCircuitInputs();
                
                // Evaluate the entire referenced circuit
                referencedCircuit.evaluate();
                
                // Get the outputs from the referenced circuit
                getCircuitOutputs();
                
            } catch (Exception e) {
                System.err.println("Error evaluating circuit component '" + circuitName + "': " + e.getMessage());
                // Fallback: set all outputs to false
                for (int i = 0; i < getOutputs(); i++) {
                    setOutputValue(i, false);
                }
            }
        }
    }
  /**
 * Set the inputs from this component to the referenced circuit - IMPROVED
 */
private void setCircuitInputs() {
    if (referencedCircuit == null) return;
    
    // Get ALL input ports from the internal circuit
    List<ComponentBase> internalInputNodes = referencedCircuit.identifyInputNodes();
    
    System.out.println("CircuitComponent: Transferring " + getInputs() + " inputs to " + 
                      internalInputNodes.size() + " internal input nodes");
    
    // For each of our inputs, find a corresponding input in the internal circuit
    for (int ourInputIndex = 0; ourInputIndex < getInputs(); ourInputIndex++) {
        if (ourInputIndex < internalInputNodes.size()) {
            ComponentBase internalInputGate = internalInputNodes.get(ourInputIndex);
            Boolean inputValue = getInputValue(ourInputIndex);
            
            // Set the input on the internal gate
            internalInputGate.setInputValueDirect(0, inputValue);
            
            System.out.println("  Setting internal gate " + internalInputGate.getName() + 
                             " input to " + inputValue);
        }
    }
}
    
    /**
     * Get the outputs from the referenced circuit and set them as our outputs
     */
    private void getCircuitOutputs() {
        List<ComponentBase> outputNodes = referencedCircuit.identifyOutputNodes();
        
        // Simple approach: get outputs sequentially from output nodes
        int outputIndex = 0;
        for (ComponentBase outputGate : outputNodes) {
            if (outputIndex < getOutputs()) {
                Boolean outputValue = outputGate.getOutputValue(0);
                setOutputValue(outputIndex, outputValue);
                outputIndex++;
            }
        }
    }
    
    @Override
    public ComponentBase copy() {
        // Create a deep copy of the referenced circuit
        Circuit circuitCopy = referencedCircuit.createCopy(circuitName + "_Copy");
        return new CircuitComponent(circuitCopy, circuitName);
    }
    
    // Helper methods
    private static int countCircuitInputs(Circuit circuit) {
        if (circuit == null) return 0;
        return circuit.countCircuitInputs();
    }
    
    private static int countCircuitOutputs(Circuit circuit) {
        if (circuit == null) return 0;
        return circuit.countCircuitOutputs();
    }
    
    public String getCircuitDisplayName() {
        return circuitName;
    }
    
    public Circuit getReferencedCircuit() {
        return referencedCircuit;
    }
}