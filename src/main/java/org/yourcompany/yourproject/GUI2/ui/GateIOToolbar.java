package org.yourcompany.yourproject.GUI2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.yourcompany.yourproject.GUI2.Controller.CanvasController;
import org.yourcompany.yourproject.businessLayer.analysis.TruthTableGenerator;
import org.yourcompany.yourproject.businessLayer.analysis.TruthTableGenerator.TruthTableRow;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;

/**
 * Toolbar to display and edit inputs/outputs for the selected gate, and show
 * truth table
 * With VS Code-like collapsible/draggable functionality
 */
public class GateIOToolbar extends JPanel {

    private final CanvasController controller;
    private JLabel gateNameLabel;
    private final JPanel inputsPanel;
    private final JPanel outputsPanel;

    private ComponentBase selectedGate;
    private Circuit currentCircuit;

    private JPanel dragHandle;
    // Button Colors (Dark Theme)
    private static final Color BUTTON_BG_DARK = new Color(28, 28, 30); // Black/Dark (#1C1C1E)
    private static final Color BUTTON_FG_LIGHT = Color.WHITE; // White text
    private static final Color BUTTON_BG_HOVER = new Color(108, 108, 110); // Soft Grey (#6C6C6E)
    private static final Color SIDEBAR_BG = new Color(34, 34, 34); // Slightly lighter dark for sidebar
    private static final Color PANEL_BG_DARK = new Color(45, 45, 48); // Dark grey for panels
    private static final Color TEXT_LIGHT = new Color(240, 240, 240); // Light text

    public GateIOToolbar(CanvasController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(new Color(235, 246, 255));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(70, 130, 180)));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        // // Header panel
        // JPanel headerPanel = createHeaderPanel();
        // contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Gate I/O Panel
        JPanel gateIOPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        gateIOPanel.setOpaque(false);
        this.gateNameLabel = new JLabel();

        inputsPanel = new JPanel();
        inputsPanel.setOpaque(false);
        inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));

        outputsPanel = new JPanel();
        outputsPanel.setOpaque(false);
        outputsPanel.setLayout(new BoxLayout(outputsPanel, BoxLayout.Y_AXIS));

        gateIOPanel.add(createSectionPanel("Inputs", inputsPanel));
        gateIOPanel.add(createSectionPanel("Outputs", outputsPanel));

        contentPanel.add(gateIOPanel, BorderLayout.CENTER);
        // add(dragHandle, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        refreshContent();
    }

    /** Public getters to access input/output panels externally */
    public JPanel getInputsPanel() {
        return inputsPanel;
    }

    public JPanel getOutputsPanel() {
        return outputsPanel;
    }

    /** Refresh only inputs panel externally */
    public void refreshInputs() {
        SwingUtilities.invokeLater(() -> {
            if (selectedGate != null)
                renderInputs();
            inputsPanel.revalidate();
            inputsPanel.repaint();
        });
    }

    /** Refresh only outputs panel externally */
    public void refreshOutputs() {
        SwingUtilities.invokeLater(() -> {
            outputsPanel.removeAll();
            if (selectedGate != null)
                renderOutputs();
            outputsPanel.revalidate();
            outputsPanel.repaint();
        });
    }

    /** Existing method to set selected gate */
    public void setSelectedGate(ComponentBase gate) {
        this.selectedGate = gate;
        SwingUtilities.invokeLater(this::refreshContent);
    }

    /** Existing refreshContent method updates both panels */
    private void refreshContent() {
        inputsPanel.removeAll();
        outputsPanel.removeAll();

        if (selectedGate == null) {
            gateNameLabel.setText("No gate selected");
            inputsPanel.add(createInfoLabel("Select a gate to edit inputs."));
            outputsPanel.add(createInfoLabel("Outputs will appear here."));
        } else {
            gateNameLabel.setText("Selected gate: " + selectedGate.getName());
            renderInputs();
            renderOutputs();
        }

        revalidate();
        repaint();
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.DARK_GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Set the current circuit for analysis
     */
    public void setCurrentCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
    }

    private JPanel createSectionPanel(String title, JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(70, 130, 180));

        wrapper.add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Get output names in the order they appear in the circuit
     */
    private List<String> getOutputsInCircuitOrder() {
        if (currentCircuit == null) {
            return new ArrayList<>();
        }

        List<String> outputNames = new ArrayList<>();
        for (ComponentBase gate : currentCircuit.getGates()) {
            outputNames.add(gate.getName());
        }
        return outputNames;
    }

    /**
     * Create header row for truth table
     */
    private JPanel createHeaderRow(List<String> inputNames, List<String> outputNames) {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        headerPanel.setOpaque(true);
        headerPanel.setBackground(new Color(220, 235, 247));

        // Add input headers
        for (String inputName : inputNames) {
            JLabel label = new JLabel(inputName);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            label.setForeground(new Color(70, 130, 180));
            label.setPreferredSize(new Dimension(60, 20));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            headerPanel.add(label);
        }

        // Add separator
        JLabel separator = new JLabel("|");
        separator.setForeground(Color.GRAY);
        separator.setPreferredSize(new Dimension(20, 20));
        separator.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(separator);

        // Add output headers
        for (String outputName : outputNames) {
            JLabel label = new JLabel(outputName);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            label.setForeground(new Color(70, 130, 180));
            label.setPreferredSize(new Dimension(60, 20));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            headerPanel.add(label);
        }

        return headerPanel;
    }

    /**
     * Create data row for truth table
     */
    private JPanel createDataRow(List<String> inputNames, List<String> outputNames, TruthTableRow row) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        rowPanel.setOpaque(true);
        rowPanel.setBackground(Color.WHITE);

        // Add input values
        for (String inputName : inputNames) {
            boolean value = row.getInput(inputName);
            JLabel label = new JLabel(value ? "1" : "0");
            label.setFont(new Font("Arial", Font.BOLD, 11));
            label.setForeground(Color.BLACK);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(60, 20));
            rowPanel.add(label);
        }

        // Add separator
        JLabel separator = new JLabel("|");
        separator.setForeground(Color.GRAY);
        separator.setPreferredSize(new Dimension(20, 20));
        separator.setHorizontalAlignment(SwingConstants.CENTER);
        rowPanel.add(separator);

        // Add output values
        for (String outputName : outputNames) {
            boolean value = row.getOutput(outputName);
            JLabel label = new JLabel(value ? "1" : "0");
            label.setFont(new Font("Arial", Font.BOLD, 11));
            label.setForeground(value ? new Color(0, 128, 0) : Color.RED);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(60, 20));
            rowPanel.add(label);
        }

        return rowPanel;
    }

    private void renderInputs() {
        int inputCount = selectedGate.getInputs();
        if (inputCount == 0) {
            inputsPanel.add(createInfoLabel("No inputs."));
            return;
        }

        for (int i = 0; i < inputCount; i++) {
            final int index = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel label = new JLabel("Input " + (index + 1) + ":");
            label.setForeground(new Color(70, 130, 180));

            boolean currentValue = selectedGate.getInputValue(index);

            // Create combo box with HIGH/LOW options
            JComboBox<String> comboBox = new JComboBox<>(new String[] { "LOW(0)", "HIGH(1)" });

            comboBox.setBackground(BUTTON_BG_DARK);
            comboBox.setForeground(TEXT_LIGHT);
            comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            comboBox.setMaximumSize(new Dimension(100, 25));

            comboBox.setSelectedItem(currentValue ? "HIGH(1)" : "LOW(0)");
            Connector connector = selectedGate.getInputConnector(index);
            boolean connected = connector != null;
            comboBox.setEnabled(!connected && controller != null);

            comboBox.addActionListener(e -> {
                if (controller != null && selectedGate != null && comboBox.isEnabled()) {
                    boolean newValue = comboBox.getSelectedItem().equals("HIGH(1)");
                    controller.setGateInput(selectedGate, index, newValue);
                    // Update outputs to reflect the change
                    SwingUtilities.invokeLater(() -> {
                        refreshOutputs();
                        // Update combo box to show current state (in case it was changed by the
                        // controller)
                        comboBox.setSelectedItem(selectedGate.getInputValue(index) ? "HIGH(1)" : "LOW(0)");
                    });
                }
            });

            JLabel statusLabel = new JLabel(connected ? "Port already connected" : "Manual");
            statusLabel.setForeground(connected ? Color.RED.darker() : new Color(34, 139, 34));

            row.add(label);
            row.add(comboBox);
            row.add(statusLabel);
            inputsPanel.add(row);
        }
    }

    private void renderOutputs() {
        int outputCount = selectedGate.getOutputs();
        if (outputCount == 0) {
            outputsPanel.add(createInfoLabel("No outputs."));
            return;
        }

        for (int i = 0; i < outputCount; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel label = new JLabel("Output " + (i + 1) + ":");
            label.setForeground(new Color(70, 130, 180));

            boolean value = selectedGate.getOutputValue(i);
            JLabel valueLabel = new JLabel(value ? "HIGH" : "LOW");
            valueLabel.setForeground(value ? new Color(34, 139, 34) : Color.RED.darker());
            valueLabel.setFont(new Font("Arial", Font.BOLD, 12));

            row.add(label);
            row.add(valueLabel);
            outputsPanel.add(row);
        }
    }

    public String generateFormattedTruthTable() {

        if (currentCircuit == null || currentCircuit.getGates().isEmpty()) {
            return "No circuit available for analysis.";
        }

        try {
            TruthTableGenerator generator = new TruthTableGenerator();
            List<TruthTableRow> table = generator.generateTruthTable(currentCircuit);

            if (table == null || table.isEmpty()) {
                return "No truth table data available for the current circuit configuration.";
            }

            StringBuilder sb = new StringBuilder();

            // 1. Get header names
            Map<String, Boolean> firstInputs = table.get(0).getInputs();
            Map<String, Boolean> firstOutputs = table.get(0).getOutputs();

            List<String> inputNames = new ArrayList<>(firstInputs.keySet());
            List<String> outputNames = getOutputsInCircuitOrder();
            Collections.sort(inputNames);

            // 2. Use fixed column width for consistency
            int colWidth = 10;

            // 3. Build Header
            // Input headers
            for (String name : inputNames) {
                String display = name.length() > colWidth ? name.substring(0, colWidth - 2) + ".." : name;
                sb.append(String.format("%-" + colWidth + "s", display));
            }

            // Output separator and headers
            if (!outputNames.isEmpty()) {
                sb.append(" | ");
                for (String name : outputNames) {
                    String display = name.length() > colWidth ? name.substring(0, colWidth - 2) + ".." : name;
                    sb.append(String.format("%-" + colWidth + "s", display));
                }
            }
            sb.append("\n");

            // 4. Separator line
            int totalChars = inputNames.size() * colWidth;
            if (!outputNames.isEmpty()) {
                totalChars += 3 + (outputNames.size() * colWidth); // 3 for " | "
            }
            for (int i = 0; i < totalChars; i++) {
                sb.append("-");
            }
            sb.append("\n");

            // 5. Data Rows
            for (TruthTableRow row : table) {
                // Input values
                for (String name : inputNames) {
                    sb.append(String.format("%-" + colWidth + "s", row.getInput(name) ? "1" : "0"));
                }

                // Output values
                if (!outputNames.isEmpty()) {
                    sb.append(" | ");
                    for (String name : outputNames) {
                        sb.append(String.format("%-" + colWidth + "s", row.getOutput(name) ? "1" : "0"));
                    }
                }
                sb.append("\n");
            }

            return sb.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Error generating table: " + ex.getMessage();
        }
    }
}