package org.yourcompany.yourproject.GUI2.project;

import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;
    private List<Circuit> circuits = new ArrayList<>(); // must be Frontend.components.Circuit

    public Project(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public List<Circuit> getCircuits() { return circuits; }

    @Override
    public String toString() { return name; }
}
