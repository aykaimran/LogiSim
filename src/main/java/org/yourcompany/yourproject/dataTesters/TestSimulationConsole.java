
package org.yourcompany.yourproject.dataTesters;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.yourcompany.yourproject.businessLayer.analysis.CircuitSimulator;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

public class TestSimulationConsole {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Circuit circuit = new Circuit("TestCircuit");
        System.out.println("=== LOGIC SIMULATION TEST ===");

        Map<String, ComponentBase> gateMap = new HashMap<>();
        int gateCounter = 1;

        // ========= ADD GATES =========
        System.out.print("How many gates? ");
        int gcount = Integer.parseInt(sc.nextLine());

        for (int i = 0; i < gcount; i++) {
            System.out.print("Gate type (AND/OR/NOT): ");
            String type = sc.nextLine().trim().toUpperCase();

            ComponentBase gate;
            String gateName = "";
            switch (type) {
                case "AND" -> {
                    gate = new AndGate();
                    gate.setInputs(2);
                    gateName = "AND_" + gateCounter++;
                }
                case "OR" -> {
                    gate = new OrGate();
                    gate.setInputs(2);
                    gateName = "OR_" + gateCounter++;
                }
                case "NOT" -> {
                    gate = new NotGate();
                    gate.setInputs(1);
                    gateName = "NOT_" + gateCounter++;
                }
                default -> {
                    System.out.println("Invalid type, skipping.");
                    continue;
                }
            }

            gate.setName(gateName);
            circuit.getGates().add(gate);
            gateMap.put(gateName, gate);
            System.out.println("Added gate: " + gateName);
        }

        // ========= ADD CONNECTIONS =========
        System.out.print("\nHow many connections? ");
        int ccount = Integer.parseInt(sc.nextLine());

        for (int i = 0; i < ccount; i++) {
            System.out.print("Connection (fromGateName toGateName toPinIndex): ");
            String[] parts = sc.nextLine().split(" ");
            if (parts.length < 3) {
                System.out.println("Invalid input, skipping.");
                continue;
            }

            String fromName = parts[0];
            String toName = parts[1];
            int toPin;
            try {
                toPin = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid pin index, skipping.");
                continue;
            }

            ComponentBase from = gateMap.get(fromName);
            ComponentBase to = gateMap.get(toName);

            if (from != null && to != null) {
                Connector conn = new Connector(fromName + "_to_" + toName, null, null, null);
                conn.setFromGate(from);
                conn.setToGate(to);
                conn.setToPort(toPin);
                circuit.getWires().add(conn);
                System.out.println("Connected " + fromName + " -> " + toName + " (pin " + toPin + ")");
            } else {
                System.out.println("Invalid gate names, skipping.");
            }
        }

        // ========= EXTERNAL INPUTS =========
        Map<String, Boolean> externalInputs = new HashMap<>();
        System.out.println("\nProvide external inputs (0/1) for unconnected pins:");

        for (ComponentBase gate : circuit.getGates()) {
            for (int i = 0; i < gate.getInputs(); i++) {
                boolean connected = false;
                for (Connector conn : circuit.getWires()) {
                    if (conn.getToGate() == gate && conn.getToPort() == i) {
                        connected = true;
                        break;
                    }
                }
                if (!connected) {
                    System.out.print(gate.getName() + "_in" + i + " = ");
                    externalInputs.put(gate.getName() + "_in" + i, sc.nextInt() == 1);
                }
            }
        }

        // ========= RUN SIMULATION =========
        Map<String, Boolean> outputs = CircuitSimulator.run(circuit, externalInputs);

        // ========= PRINT RESULTS =========
        System.out.println("\n=== SIMULATION OUTPUTS ===");
        for (Map.Entry<String, Boolean> entry : outputs.entrySet()) {
            System.out.println(entry.getKey() + " = " + (entry.getValue() ? 1 : 0));
        }

        sc.close();
    }
}
