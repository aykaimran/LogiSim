package org.yourcompany.yourproject.businessLayer.components.gates;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;

public class NotGate extends ComponentBase {
    private boolean inputValue;

    public NotGate() { super("NOT Gate"); }

    @Override
    public void evaluate() {
        setOutputs(!inputValue ? 1 : 0);
    }

    public void setInputValue(boolean input) { this.inputValue = input; }
    public boolean getInputValue() { return inputValue; }
}