package org.yourcompany.yourproject.businessLayer.analysis;

import java.util.HashMap;
import java.util.Map;

import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;

public class CircuitSimulator {

    public static Map<String, Boolean> run(Circuit circuit, Map<String, Boolean> externalInputs) {

        // initialize all gate inputs from external inputs
        for (ComponentBase gate : circuit.getGates()) {
            for (int i = 0; i < gate.getInputs(); i++) {
                String key = gate.getName() + "_in" + i;
                Boolean value = externalInputs.getOrDefault(key, false);
                gate.setInputValue(i, value);
            }
        }

        // iteratively propagate signals until outputs stabilize
        boolean changed;
        do {
            changed = false;

            // evaluate all gates
            for (ComponentBase gate : circuit.getGates()) {
                Boolean oldOutput = gate.getOutputValue(0);
                gate.evaluate();
                if (!oldOutput.equals(gate.getOutputValue(0))) changed = true;
            }

            // propagate outputs through connectors to input pins
            for (Connector conn : circuit.getWires()) {
                ComponentBase from = conn.getFromGate();
                ComponentBase to   = conn.getToGate();
                int toPin          = conn.getToPort();
                Boolean signal     = from.getOutputValue(conn.getFromPort());

                if (!signal.equals(to.getInputValue(toPin))) changed = true;
                to.setInputValue(toPin, signal);
            }

        } while (changed);

        // collect outputs
        Map<String, Boolean> outputs = new HashMap<>();
        for (ComponentBase gate : circuit.getGates()) {
            outputs.put(gate.getName(), gate.getOutputValue(0));
        }

        return outputs;
    }
}
