package org.yourcompany.yourproject.service;

public class DatabaseSeeder {

    public static void main(String[] args) {
        ProjectService projectService = new ProjectService();

        // ===== 1️⃣ Create a Project =====
        int projectId = projectService.saveProject("Half Adder", "A simple circuit that adds two bits (A, B)");
        System.out.println("✅ Project inserted: Half Adder (ID=" + projectId + ")");

        // ===== 2️⃣ Create a Circuit =====
        int circuitId = projectService.addCircuit(projectId, "Main Circuit", null);
        System.out.println("✅ Circuit inserted: Main Circuit (ID=" + circuitId + ")");

        // ===== 3️⃣ Create Gates =====
        int xorGateId = projectService.addGate(circuitId, "XOR", "XOR1", 100, 200, 2, "Blue");
        int andGateId = projectService.addGate(circuitId, "AND", "AND1", 300, 250, 2, "Green");
        int orGateId = projectService.addGate(circuitId, "OR", "OR1", 500, 250, 2, "Red");
        System.out.println("✅ Gates inserted: XOR1(ID=" + xorGateId + "), AND1(ID=" + andGateId + "), OR1(ID=" + orGateId + ")");

        // ===== 4️⃣ Create Pins =====
        projectService.addPin(xorGateId, "INPUT", 1);
        projectService.addPin(xorGateId, "INPUT", 2);
        projectService.addPin(xorGateId, "OUTPUT", 1);

        projectService.addPin(andGateId, "INPUT", 1);
        projectService.addPin(andGateId, "INPUT", 2);
        projectService.addPin(andGateId, "OUTPUT", 1);

        projectService.addPin(orGateId, "INPUT", 1);
        projectService.addPin(orGateId, "INPUT", 2);
        projectService.addPin(orGateId, "OUTPUT", 1);

        System.out.println("✅ Pins inserted");

        // ===== 5️⃣ Create Gate Connections =====
        projectService.addGateConnection(xorGateId, 1, andGateId, 1, "Black"); // XOR → AND
        projectService.addGateConnection(andGateId, 1, orGateId, 1, "Gray");   // AND → OR
        System.out.println("✅ Connections inserted");
    }
}
