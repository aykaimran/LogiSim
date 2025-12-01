

# Architecture & Design — LogiSim

This document describes the architecture and design of LogiSim, the logic circuit simulator.  
It explains the system layers, key classes, components, and their responsibilities.

---

## 1. System Overview

LogiSim allows users to design, simulate, and visualize logic circuits.  
Key features:

- Create projects and multiple circuits  
- Use common components (logic gates, switches, LEDs)  
- Connect components using wires  
- Run simulations and generate truth tables  
- Export circuit diagrams  

---

## 2. Layered Architecture

LogiSim follows a **layered architecture**:

### 2.1 Presentation Layer (UI)
- Responsible for user interaction
- Displays:
  - Component palette  
  - Workspace / circuit design view  
  - Simulation outputs (LEDs, truth table)
- Handles drag-and-drop of components and wires
- Provides menus for project/file operations

### 2.2 Business Layer (Logic)
- Processes simulation logic
- Key responsibilities:
  - Execute logic of each component (AND, OR, NOT, etc.)  
  - Propagate signals through connectors  
  - Generate truth tables  
  - Manage modular circuits (subcircuits)

### 2.3 Data Layer (Persistence)
- Responsible for saving/loading projects
- Stores:
  - Circuit structure  
  - Components and connections  
  - Component positions and properties  
- Uses standard Java serialization or XML/JSON (depending on implementation)

---

## 3. Key Classes

| Class Name     | Responsibility |
|----------------|----------------|
| `Project`      | Contains multiple circuits and manages project data |
| `Circuit`      | Contains components and connectors, can be nested in another circuit |
| `Component`    | Abstract base class for all components (logic gates, switches, LEDs) |
| `Gate`         | Extends `Component`; implements logic functions like AND, OR, NOT |
| `Connector`    | Connects output of one component to input of another; carries signals |
| `Switch`       | User-provided input component (0/1) |
| `LED`          | Displays output value |
| `SimulationManager` | Handles circuit execution and signal propagation |
| `FileManager`  | Handles saving/loading project data |
| `UIManager`    | Manages workspace, palette, menus, and user interactions |

> **Note:** Developers can extend `Component` to add new logic gates or custom circuits.

---

## 4. Class Diagram Overview
├─ Circuit
│ ├─ Component (abstract)
│ │ ├─ Gate (AND, OR, NOT…)
│ │ ├─ Switch
│ │ └─ LED
│ └─ Connector
└─ FileManager

**Relationships:**

- `Project` contains multiple `Circuit` objects  
- `Circuit` contains `Component` and `Connector` objects  
- `Gate`, `Switch`, `LED` extend `Component`  
- `Connector` links `Component` outputs to inputs  

---

## 5. Design Patterns Used

- **Factory Pattern:** For creating components dynamically from user selection  
- **Observer Pattern:** To update UI when simulation state changes  
- **Composite Pattern:** To allow circuits to contain subcircuits like components  

---

## 6. Data Flow

1. User creates a circuit in the UI  
2. Components and connectors are added to the `Circuit` object  
3. SimulationManager propagates input signals through connectors  
4. Outputs are displayed on LEDs or exported as truth tables  
5. FileManager persists the project data to disk

---

## 7. Constraints & Assumptions

- Must use **layered architecture**: UI, Business, Data  
- Handle exceptions gracefully and log errors  
- Components have well-defined inputs and outputs  
- Modular circuits can be reused in other circuits  
- Unit tests are written following TDD  
- Project is maintained in GitHub with regular commits  

---

## 8. Conclusion

This document provides a blueprint for developers to understand, extend, and maintain LogiSim.  
It defines the responsibilities of components, system layers, and overall architecture.

