

# User Guide — LogiSim

LogiSim is an application designed to help beginners learn and practice digital logic circuits.  
This guide explains how to create projects, build circuits, and run simulations.

---

## 1. Getting Started

When the application starts, you will see the **main dashboard** where you can:

- Create a new project  
- Open an existing project  
- View recently opened projects  

A project contains one or more circuits.

---

## 2. Creating a Project

### 2.1 Create a New Project
1. Click **New Project**
2. Enter a project name
3. The project opens with an empty workspace

### 2.2 Adding Circuits
1. Click **Add Circuit**
2. Give it a name (e.g., “HalfAdder”)
3. It appears in the project panel  
4. You may add multiple circuits  
5. One circuit can be reused as a component inside another circuit

---

## 3. Designing a Circuit

### 3.1 Component Palette
The left panel contains common components:
- AND, OR, NOT gates  
- NAND, NOR, XOR gates  
- Switch  
- LED  
- Constant voltage sources  
- Custom circuits from your project  

To add a component:
1. Click the component in the palette  
2. Click on the workspace to place it  

### 3.2 Positioning Components
You can:
- Drag components to reposition  
- Delete components  
- Rename (optional)  

### 3.3 Inputs and Outputs
Each component has:
- **Input terminals**  
- **Output terminals**

Hovering over them will show labels.

### 3.4 Connectors (Wires)
To connect components:
1. Click on the output terminal of a component  
2. Drag to the input terminal of another component  
3. A connector (wire) is created  

You can use different wire colors to visually separate paths.

### 3.5 Exporting Circuit Diagram
Go to:
File → Export → PNG / JPEG
This saves your circuit as an image.

---

## 4. Running a Simulation

### 4.1 Providing Input Values
Components like Switches or Input Sources allow the user to specify input values (0 or 1).

### 4.2 Observing Output
Outputs are displayed on LEDs or output terminals of components.

As the user changes inputs:
- The circuit updates in real-time  
- All connected components process the signal  

### 4.3 Truth Table Generation
You can generate the complete truth table of the circuit:
- Select **Tools → Generate Truth Table**
- The application will try all input combinations  
- Table includes:
  - Input values
  - Corresponding output values
  - Boolean expression (optional)

---

## 5. Saving and Loading Projects

### To Save:
File → Save Project

This stores:
- Circuits  
- Components  
- Connectors  
- Wire colors  
- Layouts  

### To Open:
File → Open Project

---

## 6. Additional Features

### 6.1 Using Subcircuits
You can reuse a circuit as a component inside another circuit.  
This helps in building large systems using modular design.

### 6.2 Error Handling
If something is wrong (e.g., unconnected input), the application highlights the issue.

---

## 7. Conclusion
This user guide provides the basic operations needed to create and simulate circuits using LogiSim.  
For advanced features, refer to the developer documentation and Javadoc.

