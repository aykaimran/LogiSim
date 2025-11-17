package org.yourcompany.yourproject.businessLayer.components.gates;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

public class OrGate extends ComponentBase {
    private boolean[] inputValues;

    public OrGate() { super("OR Gate"); }

    @Override
    public void evaluate() {
        boolean result = false;
        if (inputValues != null) {
            for (boolean b : inputValues) result |= b;
        }
        setOutputs(result ? 1 : 0);
    }

    public void setInputValues(boolean[] inputs) { this.inputValues = inputs; }
    public boolean[] getInputValues() { return inputValues; }
}