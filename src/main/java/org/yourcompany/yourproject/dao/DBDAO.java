
// package org.yourcompany.yourproject.dao;

// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.sql.Timestamp;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Hashtable;
// import java.util.Map;

// import javax.swing.JOptionPane;

// public class DBDAO implements IDAO {
//     private static final Map<Integer, Integer> taskIdMap = new HashMap<>();

//     @Override
//     public boolean save(Hashtable<String, String> data) {
//         String type = data.get("type");

//         try (Connection conn = DatabaseUtil.getConnection()) {
//             switch (type) {
//                 case "project": {
//                     String sql = "INSERT INTO project (name) VALUES (?) RETURNING project_id";
//                     try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//                         stmt.setString(1, data.get("name"));
//                         ResultSet rs = stmt.executeQuery();
//                         if (rs.next()) {
//                             int id = rs.getInt("project_id");
//                             data.put("project_id", String.valueOf(id));
//                         }
//                     }
//                     break;
//                 }
//                 case "task": {
//                     String sql = """
//                                 INSERT INTO task (project_id, title, start_time, end_time) VALUES (?, ?, ?, ?)
//                                 RETURNING task_id
//                             """;
//                     try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//                         stmt.setInt(1, Integer.parseInt(data.get("project_id")));
//                         stmt.setString(2, data.get("title"));

//                         String startStr = data.get("start").trim().replace("T", " ");
//                         String endStr = data.get("end").trim().replace("T", " ");

//                         // add seconds if missing
//                         if (startStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
//                             startStr += ":00";
//                         }
//                         if (endStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
//                             endStr += ":00";
//                         }

//                         stmt.setTimestamp(3, Timestamp.valueOf(startStr));
//                         stmt.setTimestamp(4, Timestamp.valueOf(endStr));

//                         ResultSet rs = stmt.executeQuery();
//                         if (rs.next()) {
//                             int dbTaskId = rs.getInt("task_id");
//                             int localId = Integer.parseInt(data.get("id"));
//                             taskIdMap.put(localId, dbTaskId);
//                         }
//                     }
//                     break;
//                 }

//                 case "resource": {
//                     String sql = "INSERT INTO resource (project_id, name) VALUES (?, ?) ON CONFLICT DO NOTHING";
//                     try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//                         stmt.setInt(1, Integer.parseInt(data.get("project_id")));
//                         stmt.setString(2, data.get("name"));
//                         stmt.executeUpdate();
//                     }
//                     break;
//                 }

//                 case "allocation": {
//                     int localTaskId = Integer.parseInt(data.get("task_id"));
//                     Integer dbTaskId = taskIdMap.get(localTaskId);
//                     if (dbTaskId == null) {
//                         System.err.println("skipping allocation: No DB task found for local task_id=" + localTaskId);
//                         return false;
//                     }

//                     String sql = """
//                                 INSERT INTO allocation (task_id, resource_name, percentage) VALUES (?, ?, ?)
//                                 ON CONFLICT DO NOTHING
//                             """;
//                     try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//                         stmt.setInt(1, dbTaskId);
//                         stmt.setString(2, data.get("resource_name"));
//                         stmt.setInt(3, Integer.parseInt(data.get("percentage")));
//                         stmt.executeUpdate();
//                     }
//                     break;
//                 }
//                 case "task_dependency": {
//                     int localTaskId = Integer.parseInt(data.get("task_id"));
//                     int localDepId = Integer.parseInt(data.get("depends_on_id"));

//                     Integer dbTaskId = taskIdMap.get(localTaskId);
//                     Integer dbDepId = taskIdMap.get(localDepId);

//                     if (dbTaskId == null || dbDepId == null) {
//                         System.err.println("skipping dependency: no DB task mapping found for " + localTaskId + " → "
//                                 + localDepId);
//                         return false;
//                     }

//                     String sql = """
//                                 INSERT INTO task_dependency (task_id, depends_on_id) VALUES (?, ?)
//                                 ON CONFLICT DO NOTHING
//                             """;
//                     try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//                         stmt.setInt(1, dbTaskId);
//                         stmt.setInt(2, dbDepId);
//                         stmt.executeUpdate();
//                     }
//                     break;
//                 }

//                 default:
//                     throw new IllegalArgumentException("Unknown save type: " + type);
//             }

//             return true;
//         } catch (Exception e) {
//             e.printStackTrace();
//             return false;
//         }
//     }

//     @Override
//     public boolean delete(String id) {
//         String sql = "DELETE FROM task WHERE task_id = ?";
//         try (Connection conn = DatabaseUtil.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//             stmt.setInt(1, Integer.parseInt(id));
//             stmt.executeUpdate();
//             return true;
//         } catch (Exception e) {
//             e.printStackTrace();
//             return false;
//         }
//     }

//     @Override
//     public Hashtable<String, String> load(String id) {
//         Hashtable<String, String> record = new Hashtable<>();
//         String sql = "SELECT * FROM task WHERE task_id = ?";
//         try (Connection conn = DatabaseUtil.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//             stmt.setInt(1, Integer.parseInt(id));
//             ResultSet rs = stmt.executeQuery();
//             if (rs.next()) {
//                 record.put("id", String.valueOf(rs.getInt("task_id")));
//                 record.put("title", rs.getString("title"));
//                 record.put("start", rs.getTimestamp("start_time").toString());
//                 record.put("end", rs.getTimestamp("end_time").toString());
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         return record;
//     }

//     @Override
//     public ArrayList<Hashtable<String, String>> load() {
//         ArrayList<Hashtable<String, String>> records = new ArrayList<>();
//         String sql = "SELECT * FROM task ORDER BY task_id";
//         try (Connection conn = DatabaseUtil.getConnection();
//                 Statement stmt = conn.createStatement();
//                 ResultSet rs = stmt.executeQuery(sql)) {

//             while (rs.next()) {
//                 Hashtable<String, String> record = new Hashtable<>();
//                 record.put("id", String.valueOf(rs.getInt("task_id")));
//                 record.put("title", rs.getString("title"));
//                 record.put("start", rs.getTimestamp("start_time").toString());
//                 record.put("end", rs.getTimestamp("end_time").toString());
//                 records.add(record);
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         return records;
//     }

//     public Project loadProject(int projectId) {
//         Project project = new Project("Loaded Project");
//         Map<Integer, Task> taskMap = new HashMap<>();
//         Map<String, Resource> resourceMap = new HashMap<>();

//         try (Connection conn = DatabaseUtil.getConnection()) {
//             String sqlTasks = "SELECT * FROM task WHERE project_id = ? ORDER BY task_id";
//             try (PreparedStatement stmt = conn.prepareStatement(sqlTasks)) {
//                 stmt.setInt(1, projectId);
//                 ResultSet rs = stmt.executeQuery();
//                 while (rs.next()) {
//                     int id = rs.getInt("task_id");
//                     String title = rs.getString("title");
//                     Timestamp start = rs.getTimestamp("start_time");
//                     Timestamp end = rs.getTimestamp("end_time");

//                     Task task = new Task(id, title, start.toLocalDateTime(), end.toLocalDateTime());
//                     project.addTask(task);
//                     taskMap.put(id, task);
//                 }
//             }
//             String sqlDeps = "SELECT * FROM task_dependency";
//             try (Statement stmt = conn.createStatement();
//                     ResultSet rs = stmt.executeQuery(sqlDeps)) {
//                 while (rs.next()) {
//                     int taskId = rs.getInt("task_id");
//                     int dependsOnId = rs.getInt("depends_on_id");
//                     Task task = taskMap.get(taskId);
//                     Task dep = taskMap.get(dependsOnId);
//                     if (task != null && dep != null) {
//                         task.addDependency(dep);
//                     }
//                 }
//             }
//             String sqlRes = "SELECT * FROM resource WHERE project_id = ?";
//             try (PreparedStatement stmt = conn.prepareStatement(sqlRes)) {
//                 stmt.setInt(1, projectId);
//                 ResultSet rs = stmt.executeQuery();
//                 while (rs.next()) {
//                     String name = rs.getString("name");
//                     Resource res = new Resource(name);
//                     resourceMap.put(name, res);
//                     project.addResource(res);
//                 }
//             }

//             String sqlAlloc = """
//                         SELECT * FROM allocation
//                         WHERE task_id IN (SELECT task_id FROM task WHERE project_id = ?)
//                     """;
//             try (PreparedStatement stmt = conn.prepareStatement(sqlAlloc)) {
//                 stmt.setInt(1, projectId);
//                 ResultSet rs = stmt.executeQuery();
//                 while (rs.next()) {
//                     int taskId = rs.getInt("task_id");
//                     String resName = rs.getString("resource_name");
//                     int percent = rs.getInt("percentage");

//                     Task task = taskMap.get(taskId);
//                     Resource res = resourceMap.get(resName);
//                     if (task != null && res != null) {
//                         Allocation alloc = new Allocation(res, task, percent);
//                         res.addAllocation(alloc);
//                         task.addAllocation(alloc);
//                     }
//                 }
//             }

//         } catch (Exception e) {
//             e.printStackTrace();
//         }

//         return project;
//     }

//     public boolean updateTask(Hashtable<String, String> data) {
//         String sql = """
//                     UPDATE task SET title = ?, start_time = ?, end_time = ? WHERE task_id = ?
//                 """;

//         try (Connection conn = DatabaseUtil.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {

//             String startStr = data.get("start").trim().replace("T", " ");
//             String endStr = data.get("end").trim().replace("T", " ");
//             if (startStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$"))
//                 startStr += ":00";
//             if (endStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$"))
//                 endStr += ":00";
//             stmt.setString(1, data.get("title"));
//             stmt.setTimestamp(2, Timestamp.valueOf(startStr));
//             stmt.setTimestamp(3, Timestamp.valueOf(endStr));
//             stmt.setInt(4, Integer.parseInt(data.get("task_id")));

//             stmt.executeUpdate();
//             return true;
//         } catch (Exception e) {
//             e.printStackTrace();
//             return false;
//         }
//     }

//     public void deleteDependenciesForTask(String taskId) {
//         String sql = "DELETE FROM task_dependency WHERE task_id = ?";
//         try (Connection conn = DatabaseUtil.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//             stmt.setInt(1, Integer.parseInt(taskId));
//             stmt.executeUpdate();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

//     public void addTaskDependency(int taskId, int dependsOnId) {
//         String sql = "INSERT INTO task_dependency (task_id, depends_on_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
//         try (Connection conn = DatabaseUtil.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//             stmt.setInt(1, taskId);
//             stmt.setInt(2, dependsOnId);
//             stmt.executeUpdate();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

//     public void deleteProjectTasks(int projectId) {
//         String deleteAllocations = """
//                     DELETE FROM allocation WHERE task_id IN (SELECT id FROM task WHERE project_id = ?)
//                 """;

//         String deleteDependencies = """
//                     DELETE FROM task_dependency WHERE task_id IN (SELECT id FROM task WHERE project_id = ?)
//                     OR depends_on_id IN (SELECT id FROM task WHERE project_id = ?)
//                 """;

//         String deleteTasks = "DELETE FROM task WHERE project_id = ?";
//         try (Connection conn = DatabaseUtil.getConnection()) {
//             conn.setAutoCommit(false);
//             try (PreparedStatement ps1 = conn.prepareStatement(deleteAllocations);
//                     PreparedStatement ps2 = conn.prepareStatement(deleteDependencies);
//                     PreparedStatement ps3 = conn.prepareStatement(deleteTasks)) {

//                 ps1.setInt(1, projectId);
//                 ps2.setInt(1, projectId);
//                 ps2.setInt(2, projectId);
//                 ps3.setInt(1, projectId);

//                 ps1.executeUpdate();
//                 ps2.executeUpdate();
//                 ps3.executeUpdate();

//                 conn.commit();
//             } catch (SQLException ex) {
//                 conn.rollback();
//                 throw ex;
//             }
//         } catch (SQLException e) {
//             e.printStackTrace();
//             JOptionPane.showMessageDialog(null, "Error deleting project tasks: " + e.getMessage());
//         }
//     }

//     public void addAllocation(int taskId, int resourceId, int percentage) {
//         String sql = """
//                     INSERT INTO allocation (task_id, resource_id, percentage)
//                     VALUES (?, ?, ?)
//                     ON CONFLICT DO NOTHING
//                 """;
//         try (Connection conn = DatabaseUtil.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//             stmt.setInt(1, taskId);
//             stmt.setInt(2, resourceId);
//             stmt.setInt(3, percentage);
//             stmt.executeUpdate();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

//     public void deleteAllocationsForTask(String taskId) {
//         String sql = "DELETE FROM allocation WHERE task_id = ?";
//         try (Connection conn = DatabaseUtil.getConnection();
//                 PreparedStatement stmt = conn.prepareStatement(sql)) {
//             stmt.setInt(1, Integer.parseInt(taskId));
//             stmt.executeUpdate();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

//     public int getOrCreateResourceId(String name, int projectId) {
//         String selectSql = "SELECT resource_id FROM resource WHERE name = ? AND project_id = ?";
//         String insertSql = "INSERT INTO resource (project_id, name) VALUES (?, ?) RETURNING resource_id";

//         try (Connection conn = DatabaseUtil.getConnection()) {
//             // try to find existing resource
//             try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
//                 stmt.setString(1, name);
//                 stmt.setInt(2, projectId);
//                 ResultSet rs = stmt.executeQuery();
//                 if (rs.next())
//                     return rs.getInt("resource_id");
//             }

//             // otherwise insert
//             try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
//                 stmt.setInt(1, projectId);
//                 stmt.setString(2, name);
//                 ResultSet rs = stmt.executeQuery();
//                 if (rs.next())
//                     return rs.getInt("resource_id");
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         return -1; // failed
//     }

// }
