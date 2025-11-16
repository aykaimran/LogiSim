
package org.yourcompany.yourproject.dataAccessLayer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;

import org.yourcompany.yourproject.dataAccessLayer.util.DatabaseUtil;

public class DBDAO implements IDAO {

    // =============================
    //          SAVE ROUTER
    // =============================
    // Returns generated ID, or -1 on failure
    public int save(Hashtable<String, String> data) {
        String table = data.get("table");
        if (table == null) return -1;

        return switch (table) {
            case "projects" -> saveProject(data);
            case "circuits" -> saveCircuit(data);
            case "gates" -> saveGate(data);
            case "pins" -> savePin(data);
            case "connections" -> saveGateConnection(data);
            default -> -1;
        };
    }

    // =============================
    //          DELETE
    // =============================
    @Override
    public boolean delete(String id) {
        String[] parts = id.split(":");
        if (parts.length != 2) return false;

        String table = parts[0];
        int pkId = Integer.parseInt(parts[1]);

        String pkColumn = switch (table) {
            case "projects" -> "project_id";
            case "circuits" -> "circuit_id";
            case "gates" -> "gate_id";
            case "pins" -> "pin_id";
            case "connections" -> "connection_id";
            default -> null;
        };

        if (pkColumn == null) return false;

        String sql = "DELETE FROM " + table + " WHERE " + pkColumn + " = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =============================
    //          LOAD BY ID
    // =============================
    @Override
    public Hashtable<String, String> load(String id) {
        String[] parts = id.split(":");
        if (parts.length != 2) return null;

        String table = parts[0];
        int pkId = Integer.parseInt(parts[1]);

        String pkColumn = switch (table) {
            case "projects" -> "project_id";
            case "circuits" -> "circuit_id";
            case "gates" -> "gate_id";
            case "pins" -> "pin_id";
            case "connections" -> "connection_id";
            default -> null;
        };

        if (pkColumn == null) return null;

        String sql = "SELECT * FROM " + table + " WHERE " + pkColumn + " = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pkId);
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
        return loadAll("projects");
    }

    // =============================
    //          LOAD ALL
    // =============================
    public ArrayList<Hashtable<String, String>> loadAll(String table) {
        ArrayList<Hashtable<String, String>> list = new ArrayList<>();
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
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =============================
    //         SAVE PROJECT
    // =============================
    public int saveProject(Hashtable<String, String> data) {
        String sql = "INSERT INTO projects (name, description) VALUES (?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, data.get("name"));
            stmt.setString(2, data.getOrDefault("description", ""));
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //         SAVE CIRCUIT
    // =============================
    public int saveCircuit(Hashtable<String, String> data) {
        String sql = "INSERT INTO circuits (project_id, parent_circuit_id, name) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, Integer.parseInt(data.get("project_id")));
            if (data.get("parent_circuit_id") != null)
                stmt.setInt(2, Integer.parseInt(data.get("parent_circuit_id")));
            else
                stmt.setNull(2, Types.INTEGER);

            stmt.setString(3, data.get("name"));
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //         SAVE GATE
    // =============================
    public int saveGate(Hashtable<String, String> data) {
        String sql = """
            INSERT INTO gates
            (circuit_id, type, label, x_pos, y_pos, rotation, color, num_inputs)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, Integer.parseInt(data.get("circuit_id")));
            stmt.setString(2, data.get("type"));
            stmt.setString(3, data.getOrDefault("label", ""));
            stmt.setInt(4, Integer.parseInt(data.get("x_pos")));
            stmt.setInt(5, Integer.parseInt(data.get("y_pos")));
            stmt.setInt(6, Integer.parseInt(data.getOrDefault("rotation", "0")));
            stmt.setString(7, data.getOrDefault("color", "black"));
            stmt.setInt(8, Integer.parseInt(data.get("num_inputs")));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //         SAVE PIN
    // =============================
    public int savePin(Hashtable<String, String> data) {
        String sql = "INSERT INTO pins (gate_id, pin_type, pin_number) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, Integer.parseInt(data.get("gate_id")));
            stmt.setString(2, data.get("pin_type"));
            stmt.setInt(3, Integer.parseInt(data.get("pin_number")));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //         SAVE CONNECTION
    // =============================
    public int saveGateConnection(Hashtable<String, String> data) {
        String sql = """
            INSERT INTO connections
            (from_gate_id, from_pin_number, to_gate_id, to_pin_number, color)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, Integer.parseInt(data.get("from_gate_id")));
            stmt.setInt(2, Integer.parseInt(data.get("from_pin_number")));
            stmt.setInt(3, Integer.parseInt(data.get("to_gate_id")));
            stmt.setInt(4, Integer.parseInt(data.get("to_pin_number")));
            stmt.setString(5, data.getOrDefault("color", "black"));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // =============================
    //      GENERIC FOREIGN KEY LOADER
    // =============================
    public ArrayList<Hashtable<String, String>> loadByForeignKey(String table, String column, int value) {
        ArrayList<Hashtable<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM " + table + " WHERE " + column + " = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
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

    public ArrayList<Hashtable<String, String>> loadCircuitsByProject(int projectId) {
        return loadByForeignKey("circuits", "project_id", projectId);
    }
}
