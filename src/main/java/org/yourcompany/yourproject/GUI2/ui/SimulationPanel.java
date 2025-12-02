package org.yourcompany.yourproject.GUI2.ui;

import javax.swing.*;
import java.awt.*;

public class SimulationPanel extends JPanel {
    private JButton BstartSim, BstopSim, BtruthTable, Bequation;

    public SimulationPanel() {
        setLayout(new FlowLayout());
        setBorder(BorderFactory.createTitledBorder("Simulation Controls"));

        BstartSim = new JButton("Start Simulation");
        BstopSim = new JButton("Stop Simulation");
        BtruthTable = new JButton("Generate Truth Table");
        Bequation = new JButton("Generate Equation");

        add(BstartSim);
        add(BstopSim);
        add(BtruthTable);
        add(Bequation);
    }
}

