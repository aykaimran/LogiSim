package org.yourcompany.yourproject.businessLayer.components;
import java.util.ArrayList;
import java.util.List;

public class Circuit {
    private String name;
    private List<ComponentBase> gates = new ArrayList<>();
    private List<Connector> wires = new ArrayList<>();

    // Constructor
    public Circuit(String name) {
        this.name = name;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ComponentBase> getGates() {
        return gates;
    }

    public void setGates(List<ComponentBase> gates) {
        this.gates = gates;
    }

    public List<Connector> getWires() {
        return wires;
    }

    public void setWires(List<Connector> wires) {
        this.wires = wires;
    }
}