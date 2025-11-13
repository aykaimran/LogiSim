package org.yourcompany.yourproject.service;

import java.util.Hashtable;

import org.yourcompany.yourproject.dao.DBDAO;

public class DatabaseSeeder {

    public static void main(String[] args) {
        DBDAO dao = new DBDAO();

        // ===== 1️⃣ Create a project =====
        Hashtable<String, String> project = new Hashtable<>();
        project.put("table", "projects");
        project.put("name", "Half Adder");
        project.put("description", "A simple circuit that adds two bits (A, B).");
        dao.save(project);

        System.out.println("✅ Project inserted: Half Adder");

        // ===== 2️⃣ Create a circuit =====
        Hashtable<String, String> circuit = new Hashtable<>();
        circuit.put("table", "circuits");
        circuit.put("project_id", "1"); // assuming project_id = 1
        circuit.put("name", "Main Circuit");
        dao.save(circuit);

        System.out.println("✅ Circuit inserted: Main Circuit");

        // ===== 3️⃣ Create gates =====
        Hashtable<String, String> g1 = new Hashtable<>();
        g1.put("table", "gates");
        g1.put("circuit_id", "1");
        g1.put("type", "XOR");
        g1.put("label", "XOR1");
        g1.put("x_pos", "100");
        g1.put("y_pos", "200");
        g1.put("rotation", "0");
        g1.put("color", "blue");
        g1.put("num_inputs", "2");
        dao.save(g1);

        Hashtable<String, String> g2 = new Hashtable<>();
        g2.put("table", "gates");
        g2.put("circuit_id", "1");
        g2.put("type", "AND");
        g2.put("label", "AND1");
        g2.put("x_pos", "300");
        g2.put("y_pos", "250");
        g2.put("rotation", "0");
        g2.put("color", "green");
        g2.put("num_inputs", "2");
        dao.save(g2);

        Hashtable<String, String> g3 = new Hashtable<>();
        g3.put("table", "gates");
        g3.put("circuit_id", "1");
        g3.put("type", "OR");
        g3.put("label", "OR1");
        g3.put("x_pos", "500");
        g3.put("y_pos", "250");
        g3.put("rotation", "0");
        g3.put("color", "red");
        g3.put("num_inputs", "2");
        dao.save(g3);

        System.out.println("✅ Gates inserted: XOR1, AND1, OR1");

        // ===== 4️⃣ Create pins =====
        // For gate 1 (XOR)
        insertPin(dao, 1, "INPUT", 1);
        insertPin(dao, 1, "INPUT", 2);
        insertPin(dao, 1, "OUTPUT", 1);

        // For gate 2 (AND)
        insertPin(dao, 2, "INPUT", 1);
        insertPin(dao, 2, "INPUT", 2);
        insertPin(dao, 2, "OUTPUT", 1);

        // For gate 3 (OR)
        insertPin(dao, 3, "INPUT", 1);
        insertPin(dao, 3, "INPUT", 2);
        insertPin(dao, 3, "OUTPUT", 1);

        System.out.println("✅ Pins inserted");

        // ===== 5️⃣ Create connections =====
        insertConnection(dao, 1, 1, 2, 1, "black"); // XOR output -> AND input
        insertConnection(dao, 2, 1, 3, 1, "gray");  // AND output -> OR input

        System.out.println("✅ Connections inserted");
    }

    private static void insertPin(DBDAO dao, int gateId, String type, int number) {
        try (var conn = org.yourcompany.yourproject.util.DatabaseUtil.getConnection();
             var stmt = conn.prepareStatement(
                     "INSERT INTO pins (gate_id, pin_type, pin_number) VALUES (?, ?, ?)")) {
            stmt.setInt(1, gateId);
            stmt.setString(2, type);
            stmt.setInt(3, number);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertConnection(DBDAO dao, int fromGate, int fromPin, int toGate, int toPin, String color) {
        try (var conn = org.yourcompany.yourproject.util.DatabaseUtil.getConnection();
             var stmt = conn.prepareStatement(
                     "INSERT INTO connections (from_gate_id, from_pin_number, to_gate_id, to_pin_number, color) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, fromGate);
            stmt.setInt(2, fromPin);
            stmt.setInt(3, toGate);
            stmt.setInt(4, toPin);
            stmt.setString(5, color);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
