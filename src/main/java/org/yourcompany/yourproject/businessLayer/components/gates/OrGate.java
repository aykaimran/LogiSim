package org.yourcompany.yourproject.businessLayer.components.gates;

import java.awt.Point;

import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

public class OrGate extends ComponentBase {

    private static int counter = 0;

    public OrGate() {
        super("Or" + (++counter), 2, 1);
    }
    public OrGate(OrGate other) {
    super(other.getName(), other.getInputs(), other.getOutputs());
    setPosition(new Point(other.getPosition()));
    // Copy input/output values
    for (int i = 0; i < other.getInputs(); i++) {
        setInputValue(i, other.getInputValue(i));
    }
    for (int i = 0; i < other.getOutputs(); i++) {
        setOutputValue(i, other.getOutputValue(i));
    }
}
@Override
public ComponentBase copy() {
    OrGate copy = new OrGate();
    copy.setPosition(this.getPosition().x, this.getPosition().y);
    copy.setName(this.getName() + "_Copy");
    // Copy any other relevant properties
    return copy;
}
   @Override
    public void update() {
        if (getInputs() >= 2) {
            boolean result = getInputValue(0) || getInputValue(1);
            setOutputValue(0, result);
        } else if (getInputs() == 1) {
            setOutputValue(0, getInputValue(0));
        } else {
            setOutputValue(0, false);
        }
    }


    @Override
    public void computeOutput() {
        // OR logic
        Boolean input1 = getInputValue(0);
        Boolean input2 = getInputValue(1);
        Boolean result = input1 || input2;
        setOutputValue(0, result); // use ComponentBase method
    }

    @Override
    public void evaluate() {
        computeOutput(); // just call computeOutput
    }
}
