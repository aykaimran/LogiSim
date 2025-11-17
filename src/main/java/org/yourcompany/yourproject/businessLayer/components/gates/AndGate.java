package org.yourcompany.yourproject.businessLayer.components.gates;

import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

public class AndGate extends ComponentBase {
     private boolean[] inputValues;
    public AndGate() {
        super("AND Gate");
    }
    @Override
    public void evaluate() {
        // AND: outputs 1 if all inputs are 1
        boolean result = true;
        if (inputValues != null) {
            for (boolean b : inputValues) result &= b;
        }
        setOutputs(result ? 1 : 0);
    }

    public void setInputValues(boolean[] inputs) {
        this.inputValues = inputs;
    }
    public boolean[] getInputValues() {
        return inputValues;
    }
}