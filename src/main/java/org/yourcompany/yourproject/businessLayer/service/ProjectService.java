package org.yourcompany.yourproject.businessLayer.service;

import java.util.ArrayList;
import java.util.Hashtable;

import org.yourcompany.yourproject.dataAccessLayer.dao.DBDAO;

public class ProjectService {
    private final DBDAO db = new DBDAO();

    // Save a project and return its ID
    public int saveProject(String name, String description) {
        Hashtable<String, String> project = new Hashtable<>();
        project.put("table", "projects");
        project.put("name", name);
        project.put("description", description);
        return db.save(project);
    }

    // Add a circuit and return its ID
    public int addCircuit(int projectId, String name, Integer parentCircuitId) {
        Hashtable<String, String> circuit = new Hashtable<>();
        circuit.put("table", "circuits");
        circuit.put("project_id", String.valueOf(projectId));
        if (parentCircuitId != null)
            circuit.put("parent_circuit_id", String.valueOf(parentCircuitId));
        circuit.put("name", name);
        return db.save(circuit);
    }

    // Add a gate and return its ID
    public int addGate(int circuitId, String type, String label, int x, int y, int numInputs, String color) {
        Hashtable<String, String> gate = new Hashtable<>();
        gate.put("table", "gates");
        gate.put("circuit_id", String.valueOf(circuitId));
        gate.put("type", type);
        gate.put("label", label);
        gate.put("x_pos", String.valueOf(x));
        gate.put("y_pos", String.valueOf(y));
        gate.put("num_inputs", String.valueOf(numInputs));
        gate.put("color", color);
        return db.save(gate);
    }

    // Add a pin and return its ID
    public int addPin(int gateId, String type, int number) {
        Hashtable<String, String> pin = new Hashtable<>();
        pin.put("table", "pins");
        pin.put("gate_id", String.valueOf(gateId));
        pin.put("pin_type", type);
        pin.put("pin_number", String.valueOf(number));
        return db.save(pin);
    }

    // Add a gate connection and return its ID
    public int addGateConnection(int fromGate, int fromPin, int toGate, int toPin, String color) {
        Hashtable<String, String> conn = new Hashtable<>();
        conn.put("table", "connections");
        conn.put("from_gate_id", String.valueOf(fromGate));
        conn.put("from_pin_number", String.valueOf(fromPin));
        conn.put("to_gate_id", String.valueOf(toGate));
        conn.put("to_pin_number", String.valueOf(toPin));
        conn.put("color", color);
        return db.save(conn);
    }

    // Load a project with circuits count
    public Hashtable<String, String> loadProject(int projectId) {
        Hashtable<String, String> project = db.load("projects:" + projectId);
        if (project == null) return null;

        ArrayList<Hashtable<String, String>> circuits = db.loadCircuitsByProject(projectId);
        project.put("CircuitsCount", String.valueOf(circuits.size()));
        return project;
    }

    // Delete entities
    public boolean deleteProject(int projectId) { return db.delete("projects:" + projectId); }
    public boolean deleteCircuit(int circuitId) { return db.delete("circuits:" + circuitId); }

    // Get all entities
    public ArrayList<Hashtable<String, String>> getAllProjects() { return db.loadAll("projects"); }
    public ArrayList<Hashtable<String, String>> getAllCircuits() { return db.loadAll("circuits"); }
    public ArrayList<Hashtable<String, String>> getAllGates() { return db.loadAll("gates"); }
    public ArrayList<Hashtable<String, String>> getAllPins() { return db.loadAll("pins"); }
    public ArrayList<Hashtable<String, String>> getAllGateConnections() { return db.loadAll("connections"); }
}
