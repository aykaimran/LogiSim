package org.yourcompany.yourproject.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;

import org.yourcompany.yourproject.util.DatabaseUtil;

/**
 * DBDAO handles all database operations related to projects, circuits, gates, pins, and connections.
 * It implements the IDAO interface for general operations, but also provides
 * specialized methods for each entity.
 */
public class DBDAO implements IDAO {

    // ====== GENERIC METHODS (From IDAO Interface) ======

    @Override
    public boolean save(Hashtable<String, String> data) {
        // Generic save (you can customize this for specific entities)
        String table = data.get("table");
        if (table == null) return false;

        switch (table) {
            case "projects":
                return saveProject(data);
            case "circuits":
                return saveCircuit(data);
            case "gates":
                return saveGate(data);
            default:
                return false;
        }
    }

    @Override
    public boolean delete(String id) {
        // You may want to delete by project_id or circuit_id
        String[] parts = id.split(":"); // e.g. "projects:3"
        if (parts.length != 2) return false;
        String table = parts[0];
        int recordId = Integer.parseInt(parts[1]);

        String sql = "DELETE FROM " + table + " WHERE " + table.substring(0, table.length() - 1) + "_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Hashtable<String, String> load(String id) {
        String[] parts = id.split(":"); // e.g. "projects:3"
        if (parts.length != 2) return null;
        String table = parts[0];
        int recordId = Integer.parseInt(parts[1]);

        String sql = "SELECT * FROM " + table + " WHERE " + table.substring(0, table.length() - 1) + "_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            ResultSet rs = stmt.executeQuery();

            ResultSetMetaData meta = rs.getMetaData();
            Hashtable<String, String> record = new Hashtable<>();

            if (rs.next()) {
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    record.put(meta.getColumnName(i), rs.getString(i));
                }
            }
            return record;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<Hashtable<String, String>> load() {
        // Loads all projects by default
        return loadAll("projects");
    }

    // ====== HELPER: Load all from any table ======

    public ArrayList<Hashtable<String, String>> loadAll(String table) {
        ArrayList<Hashtable<String, String>> records = new ArrayList<>();
        String sql = "SELECT * FROM " + table;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Hashtable<String, String> row = new Hashtable<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnName(i), rs.getString(i));
                }
                records.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // ====== PROJECT-SPECIFIC METHODS ======

    public boolean saveProject(Hashtable<String, String> data) {
        String sql = "INSERT INTO projects (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, data.get("name"));
            stmt.setString(2, data.getOrDefault("description", ""));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Hashtable<String, String>> loadAllProjects() {
        return loadAll("projects");
    }

    // ====== CIRCUIT-SPECIFIC METHODS ======

    public boolean saveCircuit(Hashtable<String, String> data) {
        String sql = "INSERT INTO circuits (project_id, parent_circuit_id, name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(data.get("project_id")));
            if (data.get("parent_circuit_id") != null)
                stmt.setInt(2, Integer.parseInt(data.get("parent_circuit_id")));
            else
                stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, data.get("name"));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Hashtable<String, String>> loadCircuitsByProject(int projectId) {
        ArrayList<Hashtable<String, String>> circuits = new ArrayList<>();
        String sql = "SELECT * FROM circuits WHERE project_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            while (rs.next()) {
                Hashtable<String, String> circuit = new Hashtable<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    circuit.put(meta.getColumnName(i), rs.getString(i));
                }
                circuits.add(circuit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return circuits;
    }

    // ====== GATE-SPECIFIC METHODS ======

    public boolean saveGate(Hashtable<String, String> data) {
        String sql = "INSERT INTO gates (circuit_id, type, label, x_pos, y_pos, rotation, color, num_inputs) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(data.get("circuit_id")));
            stmt.setString(2, data.get("type"));
            stmt.setString(3, data.getOrDefault("label", ""));
            stmt.setInt(4, Integer.parseInt(data.get("x_pos")));
            stmt.setInt(5, Integer.parseInt(data.get("y_pos")));
            stmt.setInt(6, Integer.parseInt(data.getOrDefault("rotation", "0")));
            stmt.setString(7, data.getOrDefault("color", "black"));
            stmt.setInt(8, Integer.parseInt(data.get("num_inputs")));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Hashtable<String, String>> loadGatesByCircuit(int circuitId) {
        return loadByForeignKey("gates", "circuit_id", circuitId);
    }

    // ====== GENERIC HELPER FOR FOREIGN KEY LOADING ======

    private ArrayList<Hashtable<String, String>> loadByForeignKey(String table, String keyColumn, int keyValue) {
        ArrayList<Hashtable<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM " + table + " WHERE " + keyColumn + " = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, keyValue);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Hashtable<String, String> row = new Hashtable<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnName(i), rs.getString(i));
                }
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
