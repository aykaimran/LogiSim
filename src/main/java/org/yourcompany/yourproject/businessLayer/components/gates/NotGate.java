package org.yourcompany.yourproject.businessLayer.components.gates;
import java.awt.Point;

import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

public class NotGate extends ComponentBase {
    private boolean inputValue;

    private static int counter = 0;

    public NotGate() {
        super("NOT" + (++counter), 1, 1);
    }
    public NotGate(NotGate other) {
    super(other.getName(), other.getInputs(), other.getOutputs());
    setPosition(new Point(other.getPosition()));
    // Copy input/output values
    for (int i = 0; i < other.getInputs(); i++) {
        setInputValue(i, other.getInputValue(i));
    }
    for (int i = 0; i < other.getOutputs(); i++) {
        setOutputValue(i, other.getOutputValue(i));
    }
    // Also copy internal inputValue field
    this.inputValue = other.getInputValue();
}

    // ... your existing constructor ...
    
    @Override
    public void update() {
        if (getInputs() >= 1) {
            boolean result = !getInputValue(0);
            setOutputValue(0, result);
        } else {
            setOutputValue(0, true); // NOT gate with no input defaults to true
        }
    }

@Override
public ComponentBase copy() {
    NotGate copy = new NotGate();
    copy.setPosition(this.getPosition().x, this.getPosition().y);
    copy.setName(this.getName() + "_Copy");
    // Copy any other relevant properties
    return copy;
}
    @Override
    public void computeOutput() {
        // NOT logic: output is the inverse of the input
        Boolean input = getInputValue(0);
        Boolean result = !input;
        setOutputValue(0, result);
    }

  @Override
public void evaluate() {
    Boolean input = getInputValue(0); // get the value from the input pin
    setOutputValue(0, !input);        // correctly invert it
}


    public void setInputValue(boolean input) { this.inputValue = input; }
    public boolean getInputValue() { return inputValue; }
}