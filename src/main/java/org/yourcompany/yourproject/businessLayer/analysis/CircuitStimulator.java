/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.yourcompany.yourproject.businessLayer.analysis;

import org.yourcompany.yourproject.businessLayer.components.*;
import org.yourcompany.yourproject.businessLayer.components.gates.*;
import java.util.HashMap;
import java.util.Map
import java.util.List;
public class CircuitStimulator {

    public static Map<String, Boolean> run(Circuit circuit, Map<String, Boolean> externalInputs) {

        // 1️⃣ Initialize all gate inputs from external inputs
        for (ComponentBase gate : circuit.getGates()) {
            if (gate instanceof AndGate andGate) {
                boolean[] inputs = new boolean[gate.getInputs()];
                for (int i = 0; i < inputs.length; i++) {
                    String key = gate.getName() + "_in" + i;
                    inputs[i] = externalInputs.getOrDefault(key, false);
                }
                andGate.setInputValues(inputs);

            } else if (gate instanceof OrGate orGate) {
                boolean[] inputs = new boolean[gate.getInputs()];
                for (int i = 0; i < inputs.length; i++) {
                    String key = gate.getName() + "_in" + i;
                    inputs[i] = externalInputs.getOrDefault(key, false);
                }
                orGate.setInputValues(inputs);

            } else if (gate instanceof NotGate notGate) {
                String key = gate.getName() + "_in0";
                notGate.setInputValue(externalInputs.getOrDefault(key, false));
            }
        }

        // 2️⃣ Iteratively propagate signals until outputs stabilize
        boolean changed;
        do {
            changed = false;

            // Evaluate all gates
            for (ComponentBase gate : circuit.getGates()) {
                int oldOutput = gate.getOutputs();
                gate.evaluate();
                if (oldOutput != gate.getOutputs()) changed = true;
            }

            // Propagate outputs through connectors to input pins
            for (Connector conn : circuit.getWires()) {
                ComponentBase from = conn.getFromGate();
                ComponentBase to   = conn.getToGate();
                int toPin          = conn.getToPort();
                boolean signal     = from.getOutputs() == 1;

                if (to instanceof AndGate andGate) {
                    boolean[] inputs = andGate.getInputValues();
                    if (inputs == null) inputs = new boolean[to.getInputs()];
                    if (inputs[toPin] != signal) changed = true;
                    inputs[toPin] = signal;
                    andGate.setInputValues(inputs);

                } else if (to instanceof OrGate orGate) {
                    boolean[] inputs = orGate.getInputValues();
                    if (inputs == null) inputs = new boolean[to.getInputs()];
                    if (inputs[toPin] != signal) changed = true;
                    inputs[toPin] = signal;
                    orGate.setInputValues(inputs);

                } else if (to instanceof NotGate notGate) {
                    if (notGate.getInputValue() != signal) changed = true;
                    notGate.setInputValue(signal);
                }
            }

        } while (changed); // repeat until no changes

        // 3️⃣ Collect outputs
        Map<String, Boolean> outputs = new HashMap<>();
        for (ComponentBase gate : circuit.getGates()) {
            outputs.put(gate.getName(), gate.getOutputs() == 1);
        }

        return outputs;
    }
}