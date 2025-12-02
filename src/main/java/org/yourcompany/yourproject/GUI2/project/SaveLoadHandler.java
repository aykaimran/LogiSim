package org.yourcompany.yourproject.GUI2.project;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;
import org.yourcompany.yourproject.businessLayer.service.ProjectService;

public class SaveLoadHandler {
    
    private final ProjectService projectService;
    private Map<ComponentBase, Integer> componentToIdMap;
    private Map<Integer, ComponentBase> idToComponentMap;
    private Map<Connector, Integer> connectorToIdMap;
    
    public SaveLoadHandler() {
        this.projectService = new ProjectService();
        this.componentToIdMap = new HashMap<>();
        this.idToComponentMap = new HashMap<>();
        this.connectorToIdMap = new HashMap<>();
    }
    
    // =============================
    //          SAVE PROJECT
    // =============================
    public boolean saveProject(Project project) {
        if (project == null) {
            System.out.println("SaveLoadHandler: Project is null");
            return false;
        }
        
        System.out.println("=== SAVE PROJECT: " + project.getName() + " ===");
        
        try {
            // Clear previous mappings
            componentToIdMap.clear();
            idToComponentMap.clear();
            connectorToIdMap.clear();
            
            // Step 1: Save the main project
            int projectId = saveProjectEntity(project);
            if (projectId == -1) {
                System.out.println("Failed to save project entity");
                return false;
            }
            
            // Step 2: Save all circuits
            List<Circuit> circuits = project.getCircuits();
            if (circuits != null) {
                for (Circuit circuit : circuits) {
                    if (!saveCircuit(projectId, circuit)) {
                        System.out.println("Failed to save circuit: " + circuit.getName());
                        return false;
                    }
                }
            }
            
            System.out.println("Project saved successfully with ID: " + projectId);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error saving project: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private int saveProjectEntity(Project project) {
        // Use ProjectService with new signature (no description)
        boolean success = projectService.saveProject(project.getName());
        if (!success) {
            return -1;
        }
        
        // Find the project ID we just saved
        return findProjectIdByName(project.getName());
    }
    
    private boolean saveCircuit(int projectId, Circuit circuit) {
        if (circuit == null) return false;
        
        System.out.println("Saving circuit: " + circuit.getName());
        
        // Save circuit entity using ProjectService (no parent circuit ID)
        boolean circuitSuccess = projectService.addCircuit(projectId, circuit.getName());
        if (!circuitSuccess) {
            return false;
        }
        
        // Find the circuit ID we just saved
        int circuitId = findCircuitIdByName(projectId, circuit.getName());
        if (circuitId == -1) {
            return false;
        }
        
        // Save components (using getGates() - assuming this returns ComponentBase objects)
        List<ComponentBase> components = circuit.getGates();
        if (components != null) {
            for (ComponentBase component : components) {
                if (!saveComponent(circuitId, component)) {
                    System.out.println("Failed to save component: " + component.getName());
                    return false;
                }
            }
        }
        
        // Save connectors (using getWires() - assuming this returns Connector objects)
        List<Connector> connectors = circuit.getWires();
        if (connectors != null) {
            for (Connector connector : connectors) {
                if (!saveConnector(circuitId, connector)) {
                    System.out.println("Failed to save connector");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean saveComponent(int circuitId, ComponentBase component) {
        if (component == null) return false;
        
        // Generate a unique ID for the component (using hash of name and position)
        String componentId = generateComponentId(component);
        
        // Save component entity using ProjectService with new signature
        boolean componentSuccess = projectService.addComponent(
            circuitId, 
            component.getName(),
            getComponentType(component),
            component.getInputs(),
            component.getOutputs(),
            component.getPosition().x,
            component.getPosition().y,
            componentId
        );
        
        if (!componentSuccess) {
            return false;
        }
        
        // Find the component ID we just saved
        int dbComponentId = findComponentIdByUniqueId(circuitId, componentId);
        if (dbComponentId == -1) {
            return false;
        }
        
        // Store the mapping
        componentToIdMap.put(component, dbComponentId);
        idToComponentMap.put(dbComponentId, component);
        
        return true;
    }
    
    private String generateComponentId(ComponentBase component) {
        return component.getName() + "_" + component.getPosition().x + "_" + component.getPosition().y + "_" + System.currentTimeMillis();
    }
    
    private String getComponentType(ComponentBase component) {
        // Determine the component type for database storage
        if (component instanceof AndGate) {
            return "AND";
        } else if (component instanceof OrGate) {
            return "OR";
        } else if (component instanceof NotGate) {
            return "NOT";
        } else {
            return component.getClass().getSimpleName().toUpperCase().replace("GATE", "");
        }
    }
    
    private boolean saveConnector(int circuitId, Connector connector) {
        if (connector == null || !connector.isConnected()) {
            System.out.println("Connector is null or not properly connected");
            return false;
        }
        
        ComponentBase fromComponent = connector.getFromGate();
        ComponentBase toComponent = connector.getToGate();
        
        if (fromComponent == null || toComponent == null) {
            System.out.println("Connector has null components");
            return false;
        }
        
        // Get the database component IDs from our mapping
        Integer fromComponentId = componentToIdMap.get(fromComponent);
        Integer toComponentId = componentToIdMap.get(toComponent);
        
        if (fromComponentId == null || toComponentId == null) {
            System.out.println("Could not find component IDs for connected components");
            return false;
        }
        
        // Generate a unique ID for the connector
        String connectorId = generateConnectorId(connector);
        
        // Save connector using ProjectService with new signature
        boolean connSuccess = projectService.addConnector(
            circuitId,
            connector.getName() != null ? connector.getName() : "Wire",
            "BLACK", // Default color
            fromComponentId,
            connector.getFromPort(),
            toComponentId,
            connector.getToPort(),
            false, // Default signal value
            connectorId
        );
        
        if (connSuccess) {
            // Store connector mapping if needed
            int dbConnectorId = findConnectorIdByUniqueId(circuitId, connectorId);
            if (dbConnectorId != -1) {
                connectorToIdMap.put(connector, dbConnectorId);
            }
        }
        
        return connSuccess;
    }
    
    private String generateConnectorId(Connector connector) {
        return "Wire_" + connector.getFromPort() + "_to_" + connector.getToPort() + "_" + System.currentTimeMillis();
    }
    
    // =============================
    //          LOAD PROJECT
    // =============================
    public Project loadProject(String projectName) {
        System.out.println("=== LOAD PROJECT: " + projectName + " ===");
        
        try {
            // Clear previous mappings
            componentToIdMap.clear();
            idToComponentMap.clear();
            connectorToIdMap.clear();
            
            // Step 1: Find project by name
            int projectId = findProjectIdByName(projectName);
            if (projectId == -1) {
                System.out.println("Project not found: " + projectName);
                return null;
            }
            
            System.out.println("Found project ID: " + projectId);
            
            // Step 2: Load project data using ProjectService
            Hashtable<String, String> projectData = projectService.loadProject(projectId);
            if (projectData == null) {
                System.out.println("Failed to load project data");
                return null;
            }
            
            // Step 3: Create project object
            Project project = new Project(projectData.get("name"));
            
            // Step 4: Load circuits with detailed debugging
            List<Circuit> circuits = loadCircuitsForProject(projectId);
            if (circuits != null && !circuits.isEmpty()) {
                System.out.println("Successfully loaded " + circuits.size() + " circuits");
                for (Circuit circuit : circuits) {
                    project.getCircuits().add(circuit);
                    System.out.println("Added circuit to project: " + circuit.getName());
                }
            } else {
                System.out.println("No circuits loaded for project");
            }
            
            System.out.println("Project loaded successfully: " + project.getName() + " with " + project.getCircuits().size() + " circuits");
            return project;
            
        } catch (Exception e) {
            System.err.println("Error loading project: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private List<Circuit> loadCircuitsForProject(int projectId) {
        List<Circuit> circuits = new ArrayList<>();
        
        try {
            System.out.println("=== LOADING CIRCUITS FOR PROJECT: " + projectId + " ===");
            ArrayList<Hashtable<String, String>> circuitDataList = projectService.loadCircuitsByProject(projectId);
            
            if (circuitDataList != null) {
                System.out.println("Found " + circuitDataList.size() + " circuits in database");
                
                for (Hashtable<String, String> circuitData : circuitDataList) {
                    int circuitId = Integer.parseInt(circuitData.get("circuit_id"));
                    String circuitName = circuitData.get("name");
                    System.out.println("Loading circuit: " + circuitName + " (ID: " + circuitId + ")");
                    
                    Circuit circuit = loadCircuit(circuitId);
                    if (circuit != null) {
                        circuits.add(circuit);
                        System.out.println("✓ Successfully loaded circuit: " + circuitName);
                    } else {
                        System.out.println("✗ Failed to load circuit: " + circuitName);
                    }
                }
            } else {
                System.out.println("No circuit data returned from service");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading circuits for project " + projectId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return circuits;
    }
    
    private Circuit loadCircuit(int circuitId) {
        System.out.println("=== LOAD CIRCUIT ID: " + circuitId + " ===");
        
        try {
            // Load circuit basic data using loadCircuitsByProject or get circuit by ID
            // First, we need to get the circuit name - we'll search through all projects
            String circuitName = findCircuitNameById(circuitId);
            if (circuitName == null) {
                System.out.println("Circuit name not found for ID: " + circuitId);
                return null;
            }
            
            System.out.println("Loading circuit: " + circuitName + " (ID: " + circuitId + ")");
            
            Circuit circuit = new Circuit(circuitName);
            
            // Load components first
            List<ComponentBase> components = loadComponentsForCircuit(circuitId);
            if (components != null && !components.isEmpty()) {
                System.out.println("Loaded " + components.size() + " components");
                for (ComponentBase component : components) {
                    circuit.addGate(component);
                }
            } else {
                System.out.println("No components found for circuit " + circuitId);
            }
            
            // Load connectors after all components are loaded
            List<Connector> connectors = loadConnectorsForCircuit(circuitId);
            if (connectors != null && !connectors.isEmpty()) {
                System.out.println("Loaded " + connectors.size() + " connectors");
                for (Connector connector : connectors) {
                    circuit.addWire(connector);
                }
            } else {
                System.out.println("No connectors found for circuit " + circuitId);
            }
            
            System.out.println("Circuit loaded successfully: " + circuitName);
            return circuit;
            
        } catch (Exception e) {
            System.err.println("Error loading circuit ID " + circuitId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String findCircuitNameById(int circuitId) {
        try {
            // Get all projects and search for the circuit
            ArrayList<Hashtable<String, String>> allProjects = projectService.getAllProjects();
            for (Hashtable<String, String> project : allProjects) {
                int projectId = Integer.parseInt(project.get("project_id"));
                ArrayList<Hashtable<String, String>> circuits = projectService.loadCircuitsByProject(projectId);
                for (Hashtable<String, String> circuit : circuits) {
                    int currentCircuitId = Integer.parseInt(circuit.get("circuit_id"));
                    if (currentCircuitId == circuitId) {
                        return circuit.get("name");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding circuit name for ID " + circuitId + ": " + e.getMessage());
        }
        return null;
    }
    
    private List<ComponentBase> loadComponentsForCircuit(int circuitId) {
        List<ComponentBase> components = new ArrayList<>();
        
        try {
            System.out.println("=== LOADING COMPONENTS FOR CIRCUIT: " + circuitId + " ===");
            ArrayList<Hashtable<String, String>> componentDataList = projectService.loadComponentsByCircuit(circuitId);
            
            if (componentDataList != null) {
                System.out.println("Found " + componentDataList.size() + " components in database");
                
                for (Hashtable<String, String> componentData : componentDataList) {
                    System.out.println("Component data: " + componentData);
                    ComponentBase component = loadComponent(componentData);
                    if (component != null) {
                        components.add(component);
                        
                        // Store the mapping
                        int componentId = Integer.parseInt(componentData.get("component_id"));
                        idToComponentMap.put(componentId, component);
                        componentToIdMap.put(component, componentId);
                        
                        System.out.println("✓ Loaded component: " + component.getName() + " (ID: " + componentId + ")");
                    } else {
                        System.out.println("✗ Failed to load component from data: " + componentData);
                    }
                }
            } else {
                System.out.println("No components data returned from service");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading components for circuit " + circuitId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return components;
    }
    
    private ComponentBase loadComponent(Hashtable<String, String> componentData) {
        try {
            String type = componentData.get("type");
            String name = componentData.get("name");
            int inputs = Integer.parseInt(componentData.get("inputs"));
            int outputs = Integer.parseInt(componentData.get("outputs"));
            int x = Integer.parseInt(componentData.get("position_x"));
            int y = Integer.parseInt(componentData.get("position_y"));
            
            System.out.println("Loading component - Type: " + type + ", Name: " + name + 
                             ", Inputs: " + inputs + ", Outputs: " + outputs + 
                             ", Position: (" + x + ", " + y + ")");

            // Create appropriate component based on type
            ComponentBase component = createComponentByType(type, inputs, outputs);
            if (component != null) {
                component.setName(name);
                component.setPosition(new Point(x, y));
                
                // Set input/output counts to match saved data
                if (component.getInputs() != inputs) {
                    System.out.println("Warning: Input count mismatch for " + type + 
                                     " - saved: " + inputs + ", created: " + component.getInputs());
                }
                if (component.getOutputs() != outputs) {
                    System.out.println("Warning: Output count mismatch for " + type + 
                                     " - saved: " + outputs + ", created: " + component.getOutputs());
                }
                
                System.out.println("✓ Successfully loaded component: " + type + " '" + name + "' at (" + x + "," + y + ")");
            } else {
                System.err.println("✗ Failed to create component of type: " + type);
            }
            
            return component;
            
        } catch (Exception e) {
            System.err.println("Error loading component from data: " + componentData);
            e.printStackTrace();
            return null;
        }
    }
    
    private ComponentBase createComponentByType(String type, int inputs, int outputs) {
        try {
            switch (type.toUpperCase()) {
                case "AND":
                    AndGate andGate = new AndGate();
                    System.out.println("✓ Created AND gate with proper shape");
                    return andGate;
                    
                case "OR":
                    OrGate orGate = new OrGate();
                    System.out.println("✓ Created OR gate with proper shape");
                    return orGate;
                    
                case "NOT":
                    NotGate notGate = new NotGate();
                    System.out.println("✓ Created NOT gate with proper shape");
                    return notGate;
                    
                // Add other gate types as needed
                case "NAND":
                    // return new NandGate();
                case "NOR":
                    // return new NorGate();
                case "XOR":
                    // return new XorGate();
                    
                case "INPUT":
                    ComponentBase input = createGenericComponent("INPUT", 0, 1);
                    input.setName("INPUT");
                    return input;
                    
                case "OUTPUT":
                    ComponentBase output = createGenericComponent("OUTPUT", 1, 0);
                    output.setName("OUTPUT");
                    return output;
                    
                default:
                    System.out.println("Creating generic component for type: " + type);
                    ComponentBase generic = createGenericComponent(type, inputs, outputs);
                    generic.setName(type);
                    return generic;
            }
        } catch (Exception e) {
            System.err.println("Error creating component of type '" + type + "': " + e.getMessage());
            e.printStackTrace();
            ComponentBase fallback = createGenericComponent(type, inputs, outputs);
            fallback.setName(type);
            return fallback;
        }
    }
    
    private ComponentBase createGenericComponent(String type, int numInputs, int numOutputs) {
        // Create a generic component as fallback
        return new ComponentBase(type, numInputs, numOutputs) {
            @Override
            protected void computeOutput() {
                // Default logic: pass first input to all outputs
                boolean inputValue = getInputs() > 0 ? getInputValue(0) : false;
                for (int i = 0; i < getOutputs(); i++) {
                    setOutputValue(i, inputValue);
                }
            }
            
            @Override
            public void evaluate() {
                computeOutput();
            }
            
            @Override
            public ComponentBase copy() {
                ComponentBase copy = createGenericComponent(this.getName(), this.getInputs(), this.getOutputs());
                copy.setPosition(new Point(this.getPosition()));
                return copy;
            }

            @Override
            public void update() {
                evaluate();
            }
        };
    }
    
    private List<Connector> loadConnectorsForCircuit(int circuitId) {
        List<Connector> connectors = new ArrayList<>();
        
        try {
            System.out.println("=== LOADING CONNECTORS FOR CIRCUIT: " + circuitId + " ===");
            ArrayList<Hashtable<String, String>> connectorDataList = projectService.loadConnectorsByCircuit(circuitId);
            
            if (connectorDataList != null) {
                System.out.println("Found " + connectorDataList.size() + " connectors in database");
                
                for (Hashtable<String, String> connectorData : connectorDataList) {
                    System.out.println("Connector data: " + connectorData);
                    Connector connector = loadConnector(connectorData);
                    if (connector != null) {
                        connectors.add(connector);
                        System.out.println("✓ Loaded connector: " + connectorData.get("from_component_id") + " -> " + connectorData.get("to_component_id"));
                    } else {
                        System.out.println("✗ Failed to load connector from data: " + connectorData);
                    }
                }
            } else {
                System.out.println("No connectors data returned from service");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading connectors for circuit " + circuitId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return connectors;
    }
    
    private Connector loadConnector(Hashtable<String, String> connectorData) {
        try {
            int fromComponentId = Integer.parseInt(connectorData.get("from_component_id"));
            int fromPort = Integer.parseInt(connectorData.get("from_port"));
            int toComponentId = Integer.parseInt(connectorData.get("to_component_id"));
            int toPort = Integer.parseInt(connectorData.get("to_port"));
            
            // Find the actual component objects using our mapping
            ComponentBase fromComponent = idToComponentMap.get(fromComponentId);
            ComponentBase toComponent = idToComponentMap.get(toComponentId);
            
            if (fromComponent == null || toComponent == null) {
                System.out.println("Could not find components for connector: " + 
                                 fromComponentId + " -> " + toComponentId);
                System.out.println("Available component mappings: " + idToComponentMap.keySet());
                return null;
            }
            
            // Create the connector using your constructor
            Connector connector = new Connector(fromComponent, fromPort, toComponent, toPort);
            
            System.out.println("Loaded connector: " + fromComponentId + ":" + fromPort + 
                             " -> " + toComponentId + ":" + toPort);
            
            return connector;
            
        } catch (Exception e) {
            System.err.println("Error loading connector: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // =============================
    //          UTILITY METHODS
    // =============================
    
    // NEW METHOD: Get project data by name
    public Hashtable<String, String> getProjectByName(String projectName) {
        int projectId = findProjectIdByName(projectName);
        if (projectId == -1) {
            return null;
        }
        return projectService.loadProject(projectId);
    }
    
    // NEW METHOD: Delete project by name
    public boolean deleteProject(String projectName) {
        int projectId = findProjectIdByName(projectName);
        if (projectId == -1) {
            return false;
        }
        return projectService.deleteProject(projectId);
    }
    
    private int findProjectIdByName(String projectName) {
        ArrayList<Hashtable<String, String>> allProjects = projectService.getAllProjects();
        for (Hashtable<String, String> project : allProjects) {
            if (projectName.equals(project.get("name"))) {
                return Integer.parseInt(project.get("project_id"));
            }
        }
        return -1;
    }
    
    private int findCircuitIdByName(int projectId, String circuitName) {
        ArrayList<Hashtable<String, String>> circuits = projectService.loadCircuitsByProject(projectId);
        for (Hashtable<String, String> circuit : circuits) {
            if (circuitName.equals(circuit.get("name"))) {
                return Integer.parseInt(circuit.get("circuit_id"));
            }
        }
        return -1;
    }
    
    private int findComponentIdByUniqueId(int circuitId, String uniqueId) {
        ArrayList<Hashtable<String, String>> components = projectService.loadComponentsByCircuit(circuitId);
        for (Hashtable<String, String> component : components) {
            if (uniqueId.equals(component.get("id"))) {
                return Integer.parseInt(component.get("component_id"));
            }
        }
        return -1;
    }
    
    private int findConnectorIdByUniqueId(int circuitId, String uniqueId) {
        ArrayList<Hashtable<String, String>> connectors = projectService.loadConnectorsByCircuit(circuitId);
        for (Hashtable<String, String> connector : connectors) {
            if (uniqueId.equals(connector.get("id"))) {
                return Integer.parseInt(connector.get("connector_id"));
            }
        }
        return -1;
    }
    
    public List<String> getAllProjectNames() {
        List<String> projectNames = new ArrayList<>();
        
        try {
            // Use the existing ProjectService to get all projects
            ArrayList<Hashtable<String, String>> projects = projectService.getAllProjects();
            
            if (projects != null) {
                for (Hashtable<String, String> project : projects) {
                    String projectName = project.get("name");
                    if (projectName != null && !projectName.trim().isEmpty()) {
                        projectNames.add(projectName);
                    }
                }
            }
            
            System.out.println("Found " + projectNames.size() + " projects in database");
            return projectNames;
            
        } catch (Exception e) {
            System.err.println("Error getting project names: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list instead of null
        }
    }
    
    public boolean projectExists(String projectName) {
        return findProjectIdByName(projectName) != -1;
    }
    
    public void clearMappings() {
        componentToIdMap.clear();
        idToComponentMap.clear();
        connectorToIdMap.clear();
    }
    
    // Method to update component position in database
    public boolean updateComponentPosition(ComponentBase component, int newX, int newY) {
        Integer componentId = componentToIdMap.get(component);
        if (componentId == null) {
            return false;
        }
        return projectService.updateComponentPosition(componentId, newX, newY);
    }
}