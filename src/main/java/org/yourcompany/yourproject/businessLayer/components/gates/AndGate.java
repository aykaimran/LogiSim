package org.yourcompany.yourproject.businessLayer.components.gates;

import java.awt.Point;

import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

public class AndGate extends ComponentBase {
static int counter=0;
    public AndGate() {
        super("AND"+ (++counter), 2, 1); // AND gate has 2 inputs and 1 output
    }
    @Override
    public void update() {
        if (getInputs() >= 2) {
            boolean result = getInputValue(0) && getInputValue(1);
            setOutputValue(0, result);
        } else if (getInputs() == 1) {
            setOutputValue(0, getInputValue(0));
        } else {
            setOutputValue(0, false);
        }
    }
    
    
    
    public AndGate(AndGate other) {
    super(other.getName(), other.getInputs(), other.getOutputs());
    setPosition(new Point(other.getPosition()));
    // Copy input/output values
    for (int i = 0; i < other.getInputs(); i++) setInputValue(i, other.getInputValue(i));
    for (int i = 0; i < other.getOutputs(); i++) setOutputValue(i, other.getOutputValue(i));
}
// In AndGate class (and similar for other gates)
@Override
public ComponentBase copy() {
    AndGate copy = new AndGate();
    copy.setPosition(this.getPosition().x, this.getPosition().y);
    copy.setName(this.getName() + "_Copy");
    return copy;
}

    @Override
    protected void computeOutput() {
        // AND logic: output is true only if all inputs are true
        Boolean input1 = getInputValue(0);
        Boolean input2 = getInputValue(1);
        Boolean result = input1 && input2;
        setOutputValue(0, result);
    }

    @Override
    public void evaluate() {
        // Simply call computeOutput to update output based on current inputs
        computeOutput();
    }
}
