package org.yourcompany.yourproject.GUI2.ui;

import javax.swing.*;

import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

import java.awt.*;

public class ComponentPalette extends JPanel {
    private JButton Band, Bor, Bnot, Bconnector;
    private ComponentPaletteButton dragSource;


    public ComponentPalette() {
        setLayout(new GridLayout(4, 1, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Component Palette"));

        Band = new JButton("AND Gate");
        Bor = new JButton("OR Gate");
        Bnot = new JButton("NOT Gate");
        Bconnector = new JButton("Connector");

        add(Band);
        add(Bor);
        add(Bnot);
        add(Bconnector);
        // Add tooltips
        Band.setToolTipText("AND Gate: Output is HIGH when all inputs are HIGH (2 inputs, 1 output)");
        Bor.setToolTipText("OR Gate: Output is HIGH when any input is HIGH (2 inputs, 1 output)");
        Bnot.setToolTipText("NOT Gate: Output is the inverse of the input (1 input, 1 output)");
   
    }
    //draw and or and not gates here 

    public JButton getAndButton() {
        return Band;
    }

    public JButton getOrButton() {
        return Bor;
    }

    public JButton getNotButton() {
        return Bnot;
    }

    public ComponentPaletteButton getDragSource() {
        return dragSource;
    }

    public void clearDragSource() {
        dragSource = null;
    }

    public static class ComponentPaletteButton {
        private JButton button;
        private String componentType;

        public ComponentPaletteButton(JButton button, String componentType) {
            this.button = button;
            this.componentType = componentType;
        }

        public JButton getButton() {
            return button;
        }

        public String getComponentType() {
            return componentType;
        }

        public ComponentBase createComponent() {
            switch (componentType) {
                case "AND Gate":
                    return new AndGate();
                case "OR Gate":
                    return new OrGate();
                case "NOT Gate":
                    return new NotGate();
                default:
                    return null;
            }
        }
    }
}

