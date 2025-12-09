package org.yourcompany.yourproject.GUI2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.yourcompany.yourproject.GUI2.project.ExportHandler;
import org.yourcompany.yourproject.GUI2.project.Project;
import org.yourcompany.yourproject.GUI2.project.SaveLoadHandler;
import org.yourcompany.yourproject.businessLayer.analysis.BooleanExpressionGenerator;
import org.yourcompany.yourproject.businessLayer.components.Circuit;
import org.yourcompany.yourproject.businessLayer.components.CircuitComponent;
import org.yourcompany.yourproject.businessLayer.components.ComponentBase;
import org.yourcompany.yourproject.businessLayer.components.Connector;
import org.yourcompany.yourproject.businessLayer.components.gates.AndGate;
import org.yourcompany.yourproject.businessLayer.components.gates.NotGate;
import org.yourcompany.yourproject.businessLayer.components.gates.OrGate;

public class MainUI extends JFrame {

    // --- COLOR PALETTE ---
    private static final Color BG_LIGHT = new Color(245, 246, 250); // General background
    private static final Color BORDER_SOFT = new Color(220, 220, 220); // Soft border

    // // Button Colors (Dark Theme)
    private static final Color BUTTON_BG_DARK = new Color(28, 28, 30); // Black/Dark (#1C1C1E)
    private static final Color BUTTON_FG_LIGHT = Color.WHITE; // White text
    private static final Color BUTTON_BG_HOVER = new Color(108, 108, 110); // Soft Grey (#6C6C6E)
    private static final Color SIDEBAR_BG = new Color(34, 34, 34); // Slightly lighter dark for sidebar
    private static final Color PANEL_BG_DARK = new Color(45, 45, 48); // Dark grey for panels
    private static final Color TEXT_LIGHT = new Color(240, 240, 240); // Light text
    private JPanel truthTablePanel;

    // Circuit Area Grid Colors
    private static final Color CIRCUIT_BG = Color.WHITE;
    private static final Color GRID_COLOR = new Color(240, 240, 240); // Light Gray for lines
    private static final int GRID_SIZE = 20;
    private JPanel inputsPanel;
    private JPanel outputsPanel;
    // Icon Mappings (Placeholder or use actual icons if available)
    private static final Map<String, Icon> ICONS = createIconMap();

    private Map<String, Circuit> circuitObjects = new HashMap<>();
    private Circuit selectedCircuit;
    private Set<String> circuitsPlacedInCurrentDesign = new HashSet<>();

    private JButton Bnewcircuit, Bnew, Bexport,
            Bremove, Bsave, Bload, Bdelete, Bconnect, BremoveComponent, Bboolean, Btruthtable;
    // Banalyze,Bsimulate,
    private ProjectPanel projectPanel;
    private CircuitPanel circuitPanel;
    private Map<String, java.util.List<String>> projectCircuits = new HashMap<>();
    private String selectedProject = null;
    private String currentCircuitName = null;
    private GateIOToolbar gateToolbar;
    // private ComponentPalette componentPalette;
    private Map<String, JButton> gateButtonMap = new HashMap<>();

    public MainUI() {

        setTitle("LogiSim - Circuit Simulator (Vertical Sidebar)");
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_LIGHT);

        // Initialize all buttons with new sidebar style
        Bnewcircuit = createSidebarButton("New Circuit", ICONS.get("NewCircuit"));
        Bsave = createSidebarButton("Save Project", ICONS.get("Save"));
        Bload = createSidebarButton("Load new project", ICONS.get("Load"));
        Bdelete = createSidebarButton("Delete Project", ICONS.get("Delete"));
        Bremove = createSidebarButton("Remove", ICONS.get("Remove"));
        BremoveComponent = createSidebarButton("Remove Gate", ICONS.get("Remove"));
        Bconnect = createSidebarButton("Connect another circuit", ICONS.get("Connect"));

        Bnew = createSidebarButton("New Project", ICONS.get("NewProject"));

        Bexport = createSidebarButton("Export", ICONS.get("Export"));
        Bboolean = new JButton("Boolean Expression");
        Btruthtable = new JButton("Truth Table");
        // -----------------------
        // LEFT PANEL - VERTICAL SIDEBAR MENU (New Structure)
        // -----------------------

        JPanel componentPanel = createComponentPalette();
        JPanel circuitActionsPanel = createCircuitActionsPanel();
        JPanel projectActionsPanel = createProjectActionsPanel();

        // Use a JSplitPane to manage the vertical space between the three panels
        JSplitPane circuitProjectSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, circuitActionsPanel,
                projectActionsPanel);
        circuitProjectSplit.setDividerSize(1);
        circuitProjectSplit.setBorder(null);

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, componentPanel, circuitProjectSplit);
        leftSplit.setDividerLocation(250); // Set initial divider location for components
        leftSplit.setResizeWeight(0.0); // Make the component palette fixed height
        leftSplit.setDividerSize(1);
        leftSplit.setBorder(null);

        // Final container for the entire left sidebar
        JPanel leftSidebarContainer = new JPanel(new BorderLayout());
        leftSidebarContainer.setPreferredSize(new Dimension(180, 0)); // Wider for the text menu
        leftSidebarContainer.setBackground(SIDEBAR_BG);
        leftSidebarContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_SOFT.darker()));

        leftSidebarContainer.add(leftSplit, BorderLayout.CENTER);

        add(leftSidebarContainer, BorderLayout.WEST);

        // -----------------------
        // CENTER CONTAINER - PROJECT PANEL (Top/Horizontal) + CIRCUIT PANEL (Bottom)
        // -----------------------

        projectPanel = new ProjectPanel();

        // Custom CircuitPanel for Grid Lines
        circuitPanel = new CircuitPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw Grid Lines
                g.setColor(GRID_COLOR);
                int w = getWidth();
                int h = getHeight();

                for (int x = 0; x < w; x += GRID_SIZE) {
                    g.drawLine(x, 0, x, h);
                }

                for (int y = 0; y < h; y += GRID_SIZE) {
                    g.drawLine(0, y, w, y);
                }
            }
        };
        gateToolbar = new GateIOToolbar(circuitPanel.getController());// circuitPanel.getController());
        circuitPanel.setGateSelectionListener(gateToolbar::setSelectedGate);

        // Styling for projectPanel - match dark theme
        projectPanel.setBackground(PANEL_BG_DARK);
        projectPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1),
                "PROJECT BROWSER",
                0, 2,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_LIGHT));
        projectPanel.list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        projectPanel.list.setBackground(PANEL_BG_DARK);
        projectPanel.list.setForeground(TEXT_LIGHT);
        projectPanel.setPreferredSize(new Dimension(0, 200));

        // Styling for circuitPanel
        circuitPanel.setBackground(CIRCUIT_BG);
        circuitPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1),
                "CIRCUIT DESIGN AREA",
                0,
                2,
                new Font("Segoe UI", Font.BOLD, 10),
                BUTTON_BG_DARK));
        // Create horizontal split for project browser and truth table
        truthTablePanel = createIntegratedTruthTablePanel();
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectPanel, truthTablePanel);
        topSplit.setDividerLocation(0.5);
        topSplit.setOneTouchExpandable(true);
        topSplit.setDividerSize(6);
        topSplit.setResizeWeight(0.5);

        // Create vertical split for top (project+truth) and bottom (circuit)
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, circuitPanel);
        centerSplit.setDividerLocation(300);
        centerSplit.setOneTouchExpandable(true);
        centerSplit.setDividerSize(6);
        centerSplit.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(centerSplit, BorderLayout.CENTER);
        setupEventListeners();
        // setupPaletteListeners();
        setupGatePaletteListeners();

        setVisible(true);

    }

    /**
     * Create Integrated Truth Table Panel with integrated Gate I/O functionality
     */
    private JPanel createIntegratedTruthTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG_DARK);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1),
                "GATE I/O & TRUTH TABLE",
                0, 2,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_LIGHT));

        // Create main content panel with vertical split
        JSplitPane contentSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        contentSplit.setDividerLocation(200);
        contentSplit.setDividerSize(3);
        contentSplit.setBackground(PANEL_BG_DARK);

        // Top part: Gate Information and I/O
        JPanel gateIOPanel = createGateIOPanel();
        contentSplit.setTopComponent(gateIOPanel);

        // Bottom part: Truth Table
        JPanel truthTableContentPanel = createTruthTableContentPanel();
        contentSplit.setBottomComponent(truthTableContentPanel);

        panel.add(contentSplit, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create Gate I/O Panel (top part)
     */
    private JPanel createGateIOPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // I/O panels container
        JPanel ioContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        ioContainer.setBackground(PANEL_BG_DARK);
        if (gateToolbar == null) {
            gateToolbar = new GateIOToolbar(circuitPanel.getController());
        }
        inputsPanel = gateToolbar.getInputsPanel();
        outputsPanel = gateToolbar.getOutputsPanel();

        // Optional: If empty, show placeholder labels
        if (inputsPanel.getComponentCount() == 0) {
            JLabel noInputsLabel = new JLabel("No inputs");
            noInputsLabel.setForeground(Color.GRAY);
            noInputsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            inputsPanel.add(noInputsLabel);
        }

        if (outputsPanel.getComponentCount() == 0) {
            JLabel noOutputsLabel = new JLabel("No outputs");
            noOutputsLabel.setForeground(Color.GRAY);
            noOutputsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            outputsPanel.add(noOutputsLabel);
        }

        // Add titled borders
        inputsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_SOFT),
                "INPUTS",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_LIGHT));

        outputsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_SOFT),
                "OUTPUTS",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_LIGHT));

        ioContainer.add(inputsPanel);
        ioContainer.add(outputsPanel);

        panel.add(ioContainer, BorderLayout.CENTER);

        // Boolean Expression button
        Bboolean = createComponentButton("BOOLEAN EXPRESSION");
        Bboolean.addActionListener(e -> {
            if (circuitPanel.getCurrentCircuit() == null) {
                showCustomMessageDialog(
                        "No circuit is currently open!",
                        "No Circuit",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get all gates in the current circuit
            List<ComponentBase> gates = circuitPanel.getCurrentCircuit().getGates();
            if (gates.isEmpty()) {
                showCustomMessageDialog(
                        "No Gates",
                        "The circuit is empty!",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create a dialog to select which gate to analyze
            String[] gateNames = new String[gates.size()];
            for (int i = 0; i < gates.size(); i++) {
                ComponentBase gate = gates.get(i);
                Point pos = gate.getPosition();
                gateNames[i] = gate.getName() + " at (" + pos.x + ", " + pos.y + ")";
            }

            String selectedGateName = (String) showCustomInputDialogWithOptions(
                    "Analyze Boolean Expression",
                    "Select a gate to analyze:",
                    JOptionPane.QUESTION_MESSAGE,
                    gateNames,
                    gateNames[0]);

            if (selectedGateName != null) {

                // Find selected gate index
                int selectedIndex = -1;
                for (int i = 0; i < gateNames.length; i++) {
                    if (gateNames[i].equals(selectedGateName)) {
                        selectedIndex = i;
                        break;
                    }
                }

                if (selectedIndex != -1) {
                    ComponentBase selectedGate = gates.get(selectedIndex);

                    // Generate Boolean expression
                    BooleanExpressionGenerator gen = new BooleanExpressionGenerator();
                    String expr = gen.getExpression(selectedGate, circuitPanel.getCurrentCircuit());

                    // Show result
                    showCustomMessageDialog(
                            "Boolean Expression",
                            "Boolean Expression for " + selectedGate.getName() + ":\n\n" + expr,
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(PANEL_BG_DARK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(Bboolean);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create Truth Table Content Panel (bottom part)
     */
    private JPanel createTruthTableContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Btruthtable = createComponentButton("TRUTH TABLE");
        Btruthtable.addActionListener(e -> {
            showTruthTableDialog();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(PANEL_BG_DARK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(Btruthtable);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Displays the full generated truth table in a large, scrollable themed dialog.
     */
    private void showTruthTableDialog() {
        // 1. Ensure the GateIOToolbar object is available
        if (gateToolbar == null) {
            showCustomMessageDialog("Analysis Panel Error", "Analysis panel not initialized.",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Get the formatted table string from the toolbar's logic
        String tableContent = gateToolbar.generateFormattedTruthTable();

        showCustomTextAreaDialog(
                "Full Circuit Analysis",
                "Truth Table for Circuit: " + (currentCircuitName != null ? currentCircuitName : "N/A"),
                tableContent);
    }

    /**
     * Creates and shows a custom, themed dialog optimized for displaying large,
     * scrollable text content (like a Truth Table).
     * * @param title The title of the dialog window.
     * 
     * @param contentTitle The main message/title displayed inside the panel.
     * @param textContent  The large string content to display.
     */
    private void showCustomTextAreaDialog(String title, String contentTitle, String textContent) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 500); // Increased size for better table viewing
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(PANEL_BG_DARK);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(PANEL_BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Message label
        JLabel titleLabel = new JLabel(contentTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_LIGHT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Text Area for Content (Truth Table)
        JTextArea textArea = new JTextArea();
        textArea.setText(textContent);
        textArea.setEditable(false);

        // Apply Dark Theme styling from constants
        textArea.setBackground(BUTTON_BG_DARK); // Use darkest theme color for text area background
        textArea.setForeground(TEXT_LIGHT); // Light text
        textArea.setCaretColor(TEXT_LIGHT);
        // Crucial for tables: Monospaced Font
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));

        // Scroll Pane wrapper
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 450));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center the OK button
        buttonPanel.setBackground(PANEL_BG_DARK);

        // Use your custom theme button helper
        JButton okButton = createThemeButton("CLOSE");

        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);

        // OK button action (closes dialog)
        okButton.addActionListener(e -> {
            dialog.dispose();
        });

        // Show dialog
        dialog.setVisible(true);
    }

    /**
     * NEW Palette Listeners for AND / OR / NOT gate buttons
     * These buttons come from createComponentPalette()
     */
    private void setupGatePaletteListeners() {

        // Find the three gate buttons created earlier
        JButton andBtn = gateButtonMap.get("AND");
        JButton orBtn = gateButtonMap.get("OR");
        JButton notBtn = gateButtonMap.get("NOT");

        // AND Gate
        andBtn.addActionListener(e -> addGateFromPalette("AND Gate", 200, 150));
        setupDragAndDrop(andBtn, "AND Gate");

        // OR Gate
        orBtn.addActionListener(e -> addGateFromPalette("OR Gate", 200, 150));
        setupDragAndDrop(orBtn, "OR Gate");

        // NOT Gate
        notBtn.addActionListener(e -> addGateFromPalette("NOT Gate", 200, 150));
        setupDragAndDrop(notBtn, "NOT Gate");
    }

    // --- New Panel Creation Methods for the Sidebar ---
    private JPanel createComponentPalette() {

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(SIDEBAR_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JLabel title = createSidebarTitle("GATES");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(title);
        wrapper.add(Box.createVerticalStrut(10));

        JPanel gatePanel = new JPanel();
        gatePanel.setLayout(new BoxLayout(gatePanel, BoxLayout.Y_AXIS));
        gatePanel.setBackground(SIDEBAR_BG);

        JButton andBtn = createComponentButton("AND");
        JButton orBtn = createComponentButton("OR");
        JButton notBtn = createComponentButton("NOT");

        // store buttons in map for listener setup
        gateButtonMap.put("AND", andBtn);
        gateButtonMap.put("OR", orBtn);
        gateButtonMap.put("NOT", notBtn);

        gatePanel.add(andBtn);
        gatePanel.add(Box.createVerticalStrut(5));
        gatePanel.add(orBtn);
        gatePanel.add(Box.createVerticalStrut(5));
        gatePanel.add(notBtn);

        wrapper.add(gatePanel);
        wrapper.add(Box.createVerticalGlue());

        wrapper.setPreferredSize(new Dimension(180, 250));

        return wrapper;
    }

    private String showCustomInputDialog(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(PANEL_BG_DARK);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(PANEL_BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Message label
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(TEXT_LIGHT);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(messageLabel, BorderLayout.NORTH);

        // Input field
        JTextField inputField = new JTextField();
        inputField.setBackground(BUTTON_BG_DARK);
        inputField.setForeground(TEXT_LIGHT);
        inputField.setCaretColor(TEXT_LIGHT);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mainPanel.add(inputField, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(PANEL_BG_DARK);

        JButton okButton = createThemeButton("OK");
        JButton cancelButton = createThemeButton("Cancel");

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);

        // Result holder
        final String[] result = { null };

        // OK button action
        okButton.addActionListener(e -> {
            result[0] = inputField.getText().trim();
            dialog.dispose();
        });

        // Cancel button action
        cancelButton.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });

        // Enter key support
        inputField.addActionListener(e -> okButton.doClick());

        // Show dialog
        dialog.setVisible(true);

        return result[0];
    }

    private int showCustomConfirmDialog(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(PANEL_BG_DARK);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(PANEL_BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Message label
        JLabel messageLabel = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(TEXT_LIGHT);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(messageLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(PANEL_BG_DARK);

        JButton yesButton = createThemeButton("Yes");
        JButton noButton = createThemeButton("No");

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);

        // Result holder
        final int[] result = { JOptionPane.CANCEL_OPTION };

        // Button actions
        yesButton.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        noButton.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        // Show dialog
        dialog.setVisible(true);

        return result[0];
    }

    private void showCustomMessageDialog(String title, String message, int messageType) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(PANEL_BG_DARK);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(PANEL_BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Message label
        JLabel messageLabel = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(TEXT_LIGHT);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(messageLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(PANEL_BG_DARK);

        JButton okButton = createThemeButton("OK");
        buttonPanel.add(okButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);

        // OK button action
        okButton.addActionListener(e -> dialog.dispose());

        // Show dialog
        dialog.setVisible(true);
    }

    private JButton createThemeButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_BG_DARK);
        button.setForeground(BUTTON_FG_LIGHT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_BG_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_BG_DARK);
            }
        });

        return button;
    }

    private Object showCustomInputDialogWithOptions(String title, String message, int messageType, Object[] options,
            Object initialValue) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(PANEL_BG_DARK);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(PANEL_BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Message label
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(TEXT_LIGHT);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(messageLabel, BorderLayout.NORTH);

        // Options list
        JList<Object> optionsList = new JList<>(options);
        optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionsList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        optionsList.setBackground(BUTTON_BG_DARK);
        optionsList.setForeground(TEXT_LIGHT);
        optionsList.setSelectionBackground(BUTTON_BG_HOVER);

        if (initialValue != null) {
            optionsList.setSelectedValue(initialValue, true);
        }

        JScrollPane scrollPane = new JScrollPane(optionsList);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(PANEL_BG_DARK);

        JButton okButton = createThemeButton("OK");
        JButton cancelButton = createThemeButton("Cancel");

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);

        // Result holder
        final Object[] result = { null };

        // OK button action
        okButton.addActionListener(e -> {
            result[0] = optionsList.getSelectedValue();
            dialog.dispose();
        });

        // Cancel button action
        cancelButton.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });

        // Enter key support
        optionsList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okButton.doClick();
                }
            }
        });

        // Double-click support
        optionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    okButton.doClick();
                }
            }
        });

        // Show dialog
        dialog.setVisible(true);

        return result[0];
    }

    private JDialog createThemedDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(PANEL_BG_DARK);
        return dialog;
    }

    private JPanel createCircuitActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel title = createSidebarTitle("CIRCUIT ACTIONS");
        panel.add(title);
        panel.add(Box.createVerticalStrut(5));

        JPanel buttonsWrapper = new JPanel();
        buttonsWrapper.setLayout(new BoxLayout(buttonsWrapper, BoxLayout.Y_AXIS));
        buttonsWrapper.setBackground(SIDEBAR_BG);
        buttonsWrapper.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // Group 1: Creation/Addition
        buttonsWrapper.add(Bnewcircuit);

        // Group 2: Management
        // buttonsWrapper.add(Bsave);
        // buttonsWrapper.add(Bload);
        // buttonsWrapper.add(Bdelete);
        buttonsWrapper.add(Bremove);
        buttonsWrapper.add(BremoveComponent);
        buttonsWrapper.add(Bconnect);
        panel.add(buttonsWrapper);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /**
     * Add a gate from the palette when clicked
     */
    private void addGateFromPalette(String gateType, int x, int y) {
        // Check if circuit is open
        if (circuitPanel.getCurrentCircuit() == null) {
            showCustomMessageDialog(
                    "No Circuit Selected",
                    "Please open or create a circuit first.\n" +
                            "1. Select a project\n" +
                            "2. Create a new circuit or open an existing one",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ComponentBase gate = createGateFromType(gateType);
        if (gate != null) {
            circuitPanel.addGateToCircuit(gate, x, y);

        }

    }

    /**
     * Create a gate instance from type string
     */
    private ComponentBase createGateFromType(String gateType) {
        switch (gateType) {
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

    /**
     * Setup drag and drop for palette buttons
     */
    private void setupDragAndDrop(JButton button, String gateType) {
        final boolean[] isDragging = { false };
        final Point[] dragStartPoint = { null };

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Don't check circuit here - allow drag to start
                isDragging[0] = true;
                dragStartPoint[0] = e.getPoint();
                button.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));

                if (isDragging[0]) {
                    // Convert screen coordinates to circuitPanel coordinates
                    Point screenPoint = e.getLocationOnScreen();
                    Point circuitPoint = new Point(screenPoint);
                    SwingUtilities.convertPointFromScreen(circuitPoint, circuitPanel);

                    // Check if released over circuit panel
                    if (circuitPanel.contains(circuitPoint)) {
                        // Check if circuit is open
                        if (circuitPanel.getCurrentCircuit() == null) {
                            showCustomMessageDialog(
                                    "No Circuit Selected",
                                    "Please open or create a circuit first.\n" +
                                            "1. Select a project\n" +
                                            "2. Create a new circuit or open an existing one",

                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            ComponentBase gate = createGateFromType(gateType);
                            if (gate != null) {
                                // Use the mouse position in circuit panel coordinates
                                circuitPanel.addGateToCircuit(gate, circuitPoint.x, circuitPoint.y);
                            }
                        }
                    }
                }

                isDragging[0] = false;
                dragStartPoint[0] = null;
            }
        });

        button.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (isDragging[0]) {
                    Point screenPoint = e.getLocationOnScreen();
                    Point circuitPoint = new Point(screenPoint);
                    SwingUtilities.convertPointFromScreen(circuitPoint, circuitPanel);

                    if (circuitPanel.contains(circuitPoint) && circuitPanel.getCurrentCircuit() != null) {
                        circuitPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                    } else {
                        circuitPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        });
    }

    private JPanel createProjectActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel title = createSidebarTitle("PROJECT ACTIONS");
        panel.add(title);
        panel.add(Box.createVerticalStrut(5));

        JPanel buttonsWrapper = new JPanel();
        buttonsWrapper.setLayout(new BoxLayout(buttonsWrapper, BoxLayout.Y_AXIS));
        buttonsWrapper.setBackground(SIDEBAR_BG);
        buttonsWrapper.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // Group 1: Project Creation
        buttonsWrapper.add(Bnew);
        buttonsWrapper.add(Bsave);
        buttonsWrapper.add(Bload);
        buttonsWrapper.add(Bdelete);
        // buttonsWrapper.add(Bsimulate);
        // buttonsWrapper.add(Banalyze);
        buttonsWrapper.add(Bexport); // This is the button we needed to make space for

        panel.add(buttonsWrapper);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JLabel createSidebarTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(new Color(150, 150, 150)); // Light grey title
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // -----------------------
    // Helper: Create small, menu-style button
    // -----------------------
    private JButton createSidebarButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setHorizontalAlignment(SwingConstants.LEFT); // Align text left
        btn.setIconTextGap(10); // Spacing between icon and text
        btn.setFocusPainted(false);
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(BUTTON_FG_LIGHT);

        // MODIFICATION: Reduced font size and padding to save vertical space
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10)); // Reduced vertical padding from 8 to 6

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Hover effect: Dark -> Soft Gray background
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BUTTON_BG_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(SIDEBAR_BG);
            }
        });

        return btn;
    }

    // -----------------------
    // Helper: Custom JSeparator (Horizontal for menu)
    // -----------------------
    private JSeparator JSeparator(int orientation, Color color) {
        JSeparator sep = new JSeparator(orientation);
        sep.setForeground(color);
        if (orientation == SwingConstants.HORIZONTAL) {
            // Make horizontal separator full width but thin
            sep.setPreferredSize(new Dimension(100, 1));
            sep.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Add vertical padding
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, sep.getPreferredSize().height + 10));
        } else {
            // For vertical (if ever needed in the future)
            sep.setPreferredSize(new Dimension(1, 20));
        }
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    // -----------------------
    // Helper: Create component block button (DARK THEME)
    // -----------------------
    private JButton createComponentButton(String text) {
        JButton btn = new JButton(text);

        // Component buttons remain 60px height but are flexible width
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        btn.setPreferredSize(new Dimension(100, 60));
        btn.setMinimumSize(new Dimension(100, 60));

        btn.setFocusPainted(false);
        btn.setBackground(BUTTON_BG_DARK); // Default Black
        btn.setForeground(BUTTON_FG_LIGHT); // White Text
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));

        // Ensure the button's alignment in the vertical stack is correct
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BUTTON_BG_DARK, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect: Black -> Soft Gray
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BUTTON_BG_HOVER); // Soft Gray on hover
                btn.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BUTTON_BG_HOVER, 1, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(BUTTON_BG_DARK); // Back to Black
                btn.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BUTTON_BG_DARK, 1, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            }
        });

        return btn;
    }

    // Helper for icons (using blank icons since image files aren't available)
    private static Map<String, Icon> createIconMap() {
        // In a real application, you would load SVG or PNG icons here
        Map<String, Icon> map = new HashMap<>();
        int size = 16;
        Icon blankIcon = new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));

        map.put("NewCircuit", blankIcon);
        map.put("AddComponent", blankIcon);
        map.put("AddCircuit", blankIcon);
        map.put("Save", blankIcon);
        map.put("Load", blankIcon);
        map.put("Delete", blankIcon);
        map.put("NewProject", blankIcon);
        map.put("Simulate", blankIcon);
        map.put("Analyze", blankIcon);
        map.put("Export", blankIcon);
        map.put("Remove", blankIcon);
        map.put("Connect", blankIcon);

        return map;
    }

    // -----------------------
    // Event Listeners & Utility (Unchanged - uses buttons now in new layout)
    // -----------------------
    private void setupEventListeners() {
        // Project Buttons
        Bnew.addActionListener(e -> {
            String name = showCustomInputDialog("New Project", "Enter Project Name:");
            if (name == null)
                return;
            name = name.trim();
            if (name.isEmpty())
                return;
            if (projectCircuits.containsKey(name)) {
                showCustomMessageDialog("Error", "Project already exists.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Circuit newCircuit = new Circuit(name);
            circuitObjects.put(selectedProject + ":" + name, newCircuit);

            projectPanel.model.addElement(name);
            projectCircuits.put(name, new ArrayList<>());
        });

        BremoveComponent.addActionListener(e -> {
            if (circuitPanel.getCurrentCircuit() == null) {
                showCustomMessageDialog(
                        "No Circuit",
                        "No circuit is currently open!",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get all gates in the current circuit
            List<ComponentBase> gates = circuitPanel.getCurrentCircuit().getGates();
            if (gates.isEmpty()) {
                showCustomMessageDialog(
                        "No Gates",
                        "The circuit is empty!",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create a dialog to select which gate to remove
            String[] gateNames = new String[gates.size()];
            for (int i = 0; i < gates.size(); i++) {
                ComponentBase gate = gates.get(i);
                Point pos = gate.getPosition();
                gateNames[i] = gate.getName() + " at (" + pos.x + ", " + pos.y + ")";
            }

            String selectedGateName = (String) showCustomInputDialogWithOptions(
                    "Remove Gate",
                    "Select a gate to remove:",
                    JOptionPane.QUESTION_MESSAGE,
                    gateNames,
                    gateNames[0]);

            if (selectedGateName != null) {
                // Find the selected gate
                int selectedIndex = -1;
                for (int i = 0; i < gateNames.length; i++) {
                    if (gateNames[i].equals(selectedGateName)) {
                        selectedIndex = i;
                        break;
                    }
                }

                if (selectedIndex != -1) {
                    ComponentBase gateToRemove = gates.get(selectedIndex);

                    int confirm = showCustomConfirmDialog("Confirm Removal",
                            "Remove gate: " + gateToRemove.getName() + "?");

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Remove the gate using circuit panel's method
                        circuitPanel.getCurrentCircuit().removeGate(gateToRemove);

                        // Refresh the circuit panel
                        circuitPanel.addCircuitComponents(circuitPanel.getCurrentCircuit());

                        // showCustomMessageDialog(
                        // "Gate removed successfully!",
                        // "Removal Complete",
                        // JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // Delete button - CHANGE TO USE PROJECT NAME
        Bdelete.addActionListener(e -> {
            String projectName = getSelectedProjectName(); // Get project name instead of ID
            SaveLoadHandler handler = new SaveLoadHandler();
            if (handler.deleteProject(projectName)) { // Uses String name
                showCustomMessageDialog("Success", "Project deleted successfully!", JOptionPane.INFORMATION_MESSAGE);
                refreshProjectListUI();
            } else {
                showCustomMessageDialog("Error", "Failed to delete project.", JOptionPane.ERROR_MESSAGE);
            }
        });

        Bexport.addActionListener(e -> {
            String selected = projectPanel.list.getSelectedValue();

            if (selected == null) {
                showCustomMessageDialog("No Selection", "No circuit selected!", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String circuitName = selected.startsWith("   → ") ? selected.substring(5) : selected;

            // Quick validation
            if (currentCircuitName == null || !currentCircuitName.equals(circuitName)) {
                showCustomMessageDialog(
                        "Please open circuit '" + circuitName + "' in the design area before exporting.",
                        "Circuit Not Open",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (circuitPanel.getCurrentCircuit() == null || circuitPanel.getCurrentCircuit().getGates().isEmpty()) {
                showCustomMessageDialog(
                        "The circuit is empty! There's nothing to export.",
                        "Empty Circuit",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Fast export
            ExportHandler.exportCircuit(circuitName, circuitPanel);
        });

        // Load button
        Bload.addActionListener(e -> {
            SaveLoadHandler handler = new SaveLoadHandler();
            List<String> availableProjects = handler.getAllProjectNames();

            if (availableProjects == null || availableProjects.isEmpty()) {
                showCustomMessageDialog("No Projects", "No projects found in database.",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog loadDialog = createThemedDialog("Load Project", 350, 400);

            JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            dialogPanel.setBackground(PANEL_BG_DARK);

            JLabel titleLabel = new JLabel("Select a project to load:");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(TEXT_LIGHT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            dialogPanel.add(titleLabel, BorderLayout.NORTH);

            DefaultListModel<String> projectListModel = new DefaultListModel<>();
            for (String projectName : availableProjects) {
                projectListModel.addElement(projectName);
            }

            JList<String> projectList = new JList<>(projectListModel);
            projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            projectList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            projectList.setBackground(BUTTON_BG_DARK);
            projectList.setForeground(TEXT_LIGHT);
            projectList.setSelectionBackground(BUTTON_BG_HOVER);

            JScrollPane scrollPane = new JScrollPane(projectList);
            scrollPane.setPreferredSize(new Dimension(300, 250));
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT));
            dialogPanel.add(scrollPane, BorderLayout.CENTER);

            JLabel infoLabel = new JLabel(availableProjects.size() + " project(s) found");
            infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            infoLabel.setForeground(Color.GRAY);
            infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            dialogPanel.add(infoLabel, BorderLayout.SOUTH);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(PANEL_BG_DARK);

            JButton loadButton = createThemeButton("Load Selected");
            JButton cancelButton = createThemeButton("Cancel");

            buttonPanel.add(loadButton);
            buttonPanel.add(cancelButton);

            loadDialog.add(dialogPanel, BorderLayout.CENTER);
            loadDialog.add(buttonPanel, BorderLayout.SOUTH);

            loadButton.addActionListener(loadEvent -> {
                String selectedProjectName = projectList.getSelectedValue();
                if (selectedProjectName == null) {
                    showCustomMessageDialog("No Selection", "Please select a project first.",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Project project = handler.loadProject(selectedProjectName);
                if (project != null) {
                    loadProjectIntoUI(project);
                    loadDialog.dispose();
                    showCustomMessageDialog("Load Successful",
                            "Project '" + selectedProjectName + "' loaded successfully!",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showCustomMessageDialog("Load Error",
                            "Failed to load project: " + selectedProjectName,
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // Cancel button action
            cancelButton.addActionListener(cancelEvent -> {
                loadDialog.dispose();
            });

            // Double-click to load
            projectList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        loadButton.doClick();
                    }
                }
            });

            // Enter key to load
            projectList.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        loadButton.doClick();
                    }
                }
            });

            loadDialog.setVisible(true);
        });

        // Save button
        Bsave.addActionListener(e -> {
            Project project = getCurrentProjectFromUI(); // builds Project with Circuits
            SaveLoadHandler handler = new SaveLoadHandler();
            if (handler.saveProject(project)) {
                showCustomMessageDialog("Success", "Project saved successfully!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showCustomMessageDialog("Error", "Failed to save project.", JOptionPane.ERROR_MESSAGE);
            }
        });
        Bconnect.addActionListener(e -> {
            connectCircuitAsComponent();
        });

        // CREATE CIRCUIT (inside selected project)
        Bnewcircuit.addActionListener(e -> {
            if (selectedProject == null) {
                showCustomMessageDialog("No Project", "Select or create a project first.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String name = showCustomInputDialog("New Circuit", "Enter Circuit Name:");
            if (name == null)
                return;
            name = name.trim();
            if (name.isEmpty())
                return;

            // avoid duplicate circuit names within same project
            List<String> list = projectCircuits.get(selectedProject);
            if (list == null) {
                list = new ArrayList<>();
                projectCircuits.put(selectedProject, list);
            }
            if (list.contains(name)) {
                showCustomMessageDialog("Duplicate", "Circuit with this name already exists in the project.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            list.add(name);

            // Create a new Circuit object for this circuit
            Circuit newCircuit = new Circuit(name);
            String key = selectedProject + ":" + name;
            circuitObjects.put(key, newCircuit);

            // Insert project sub-item in the JList model just after the project element
            DefaultListModel<String> model = projectPanel.model;
            int projIndex = model.indexOf(selectedProject);
            if (projIndex >= 0) {
                model.add(projIndex + modelIndexCountUnderProject(model, projIndex) + 1, "   → " + name);
            } else {
                model.addElement("   → " + name);
            }
        });
        Bremove.addActionListener(e -> {
            String selected = projectPanel.list.getSelectedValue();
            if (selected == null)
                return;
            int confirm = showCustomConfirmDialog("Confirm Removal",
                    "Are you sure you want to remove \"" + selected.trim() + "\"?");
            if (confirm != JOptionPane.YES_OPTION)
                return;
            DefaultListModel<String> model = projectPanel.model;
            if (selected.startsWith("    → ")) {
                String circuitName = selected.substring(5);
                if (selectedProject != null) {
                    java.util.List<String> circuits = projectCircuits.get(selectedProject);
                    if (circuits != null)
                        circuits.remove(circuitName);
                    circuitObjects.remove(selectedProject + ":" + circuitName);
                    model.removeElement(selected);
                    if (circuitName.equals(currentCircuitName)) {
                        circuitPanel.clearCircuit();
                        currentCircuitName = null;
                    }
                    circuitsPlacedInCurrentDesign.remove(circuitName);
                }
            } else {
                String projectName = selected;
                java.util.List<String> circuits = projectCircuits.get(projectName);
                if (circuits != null) {
                    for (String cir : circuits) {
                        circuitObjects.remove(projectName + ":" + cir);
                        circuitsPlacedInCurrentDesign.remove(cir);
                        model.removeElement("    → " + cir);
                    }
                }
                projectCircuits.remove(projectName);
                model.removeElement(projectName);
                if (selectedProject != null && selectedProject.equals(projectName)) {
                    circuitPanel.clearCircuit();
                    selectedProject = null;
                    currentCircuitName = null;
                }
            }
        });

        // Clicking an item in list to open project or circuit
        projectPanel.list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            String selected = projectPanel.list.getSelectedValue();
            if (selected == null)
                return;

            if (selected.startsWith("   → ")) {
                // circuit clicked
                String circuitName = selected.substring(5);
                currentCircuitName = circuitName;

                // Get the circuit object and load it into the panel
                if (selectedProject != null) {
                    String key = selectedProject + ":" + circuitName;
                    Circuit circuit = circuitObjects.get(key);
                    if (circuit != null) {
                        circuitPanel.setCurrentCircuit(circuit);
                        circuitPanel.addCircuitComponents(circuit);
                        circuitPanel.refreshCircuitLabel(); // Ensure label is updated
                        gateToolbar.setCurrentCircuit(circuit);
                    } else {
                        // Circuit doesn't exist yet - create it and set it
                        circuit = new Circuit(circuitName);
                        circuitObjects.put(key, circuit);
                        circuitPanel.setCurrentCircuit(circuit);
                        circuitPanel.refreshCircuitLabel();
                    }
                } else {
                    // No project selected - still create circuit for editing
                    Circuit circuit = new Circuit(circuitName);
                    circuitPanel.setCurrentCircuit(circuit);
                    circuitPanel.refreshCircuitLabel();
                }
            } else {
                // project clicked
                selectedProject = selected;
                currentCircuitName = null;
                circuitPanel.setCurrentCircuit(null);
                circuitPanel.showCircuit("No circuit selected");
            }
        });

    }

    private Circuit deepCopyCircuit(Circuit original) {
        Circuit copy = new Circuit(original.getName());
        copy.getGates().addAll(original.getGates());
        copy.getWires().addAll(original.getWires());
        return copy;
    }

    private Project getCurrentProjectFromUI() {
        String projectName = projectPanel.list.getSelectedValue();
        if (projectName == null || projectName.startsWith("    → ")) {
            showCustomMessageDialog("Selection Required", "Select a project first!", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Project project = new Project(projectName);
        java.util.List<String> circuits = projectCircuits.get(projectName);
        if (circuits != null) {
            for (String circuitName : circuits) {
                Circuit cir = circuitObjects.get(projectName + ":" + circuitName);
                if (cir != null)
                    project.getCircuits().add(cir);
            }
        }
        return project;
    }

    private int getSelectedProjectId() {
        String projectName = projectPanel.list.getSelectedValue();
        if (projectName == null || projectName.startsWith("    → ")) {
            showCustomMessageDialog("Selection Required", "Select a project first!", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        SaveLoadHandler handler = new SaveLoadHandler();
        Map<String, String> projectData = handler.getProjectByName(projectName);
        if (projectData == null) {
            showCustomMessageDialog("Project Not Found", "Project not found!", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        return Integer.parseInt(projectData.get("Id"));
    }

    private void loadProjectIntoUI(Project project) {
        if (project == null) {
            System.out.println("LoadProjectIntoUI: Project is null");
            return;
        }

        System.out.println("Loading project into UI: " + project.getName());

        String projectName = project.getName();

        // Check if project already exists in UI
        if (projectCircuits.containsKey(projectName)) {
            // int option = JOptionPane.showConfirmDialog(this,
            // "Project '" + projectName + "' already exists. Do you want to reload it?",
            // "Project Exists",
            // JOptionPane.YES_NO_OPTION,
            // JOptionPane.QUESTION_MESSAGE);
            int option = showCustomConfirmDialog("Confirm Reload",
                    "Project '" + projectName + "' already exists. Do you want to reload it?");

            if (option != JOptionPane.YES_OPTION) {
                return; // User chose not to reload
            }

            // Remove existing project data
            removeProjectFromUI(projectName);
        }

        // Add project to UI (appends to existing projects)
        projectPanel.model.addElement(projectName);
        List<String> circuitsList = new ArrayList<>();
        projectCircuits.put(projectName, circuitsList);

        // Add circuits to UI
        for (Circuit circuit : project.getCircuits()) {
            String circuitName = circuit.getName();
            String key = projectName + ":" + circuitName;

            // Store the circuit object
            circuitObjects.put(key, circuit);
            circuitsList.add(circuitName);

            // Add to project panel
            projectPanel.model.addElement("   → " + circuitName);

            System.out.println("Added circuit to UI: " + circuitName);
        }

        // Auto-select the loaded project and first circuit
        selectedProject = projectName;
        if (!project.getCircuits().isEmpty()) {
            Circuit firstCircuit = project.getCircuits().get(0);
            currentCircuitName = firstCircuit.getName();

            // Load the first circuit into the circuit panel
            String key = projectName + ":" + currentCircuitName;
            Circuit circuit = circuitObjects.get(key);
            if (circuit != null) {
                circuitPanel.setCurrentCircuit(circuit);
                circuitPanel.addCircuitComponents(circuit);
                circuitPanel.refreshCircuitLabel();
                gateToolbar.setCurrentCircuit(circuit);
                System.out.println("Auto-loaded circuit: " + currentCircuitName);
            }
        } else {
            // No circuits in project
            currentCircuitName = null;
            circuitPanel.setCurrentCircuit(null);
            circuitPanel.showCircuit("Project loaded - No circuits");
            System.out.println("Project loaded but no circuits found");
        }

        // Select the loaded project in the list
        projectPanel.list.setSelectedValue(projectName, true);

        // Refresh the UI
        revalidate();
        repaint();

        System.out.println("Project loading completed: " + projectName);
    }

    /**
     * Remove existing project data from UI
     */
    private void removeProjectFromUI(String projectName) {
        // Remove from projectCircuits map
        List<String> circuits = projectCircuits.remove(projectName);

        // Remove from circuitObjects map
        if (circuits != null) {
            for (String circuitName : circuits) {
                String key = projectName + ":" + circuitName;
                circuitObjects.remove(key);
                circuitsPlacedInCurrentDesign.remove(circuitName);
            }
        }

        // Remove from JList model
        DefaultListModel<String> model = projectPanel.model;
        int projectIndex = model.indexOf(projectName);
        if (projectIndex >= 0) {
            // Remove all circuits under this project first
            int i = projectIndex + 1;
            while (i < model.size() && model.get(i).startsWith("   → ")) {
                model.remove(i);
                // Don't increment i because size decreases
            }
            // Remove the project itself
            model.remove(projectIndex);
        }

        // Clear circuit panel if the removed project was selected
        if (selectedProject != null && selectedProject.equals(projectName)) {
            circuitPanel.clearCircuit();
            selectedProject = null;
            currentCircuitName = null;
        }
    }

    private void refreshProjectListUI() {
        projectPanel.model.clear();
        for (String projectName : projectCircuits.keySet()) {
            projectPanel.model.addElement(projectName);
            List<String> circuits = projectCircuits.get(projectName);
            if (circuits != null) {
                for (String cir : circuits) {
                    projectPanel.model.addElement("   → " + cir);
                }
            }
        }
    }

    // ---------------------------------------------FUNCTIONS-------------------------------------------------------------------------
    private int modelIndexCountUnderProject(DefaultListModel<String> model, int projIndex) {
        int count = 0;
        for (int i = projIndex + 1; i < model.size(); i++) {
            String s = model.get(i);
            if (s.startsWith("   → "))
                count++;
            else
                break; // next project reached
        }
        return count;
    }

    private void simulateCircuit() {
        if (selectedProject == null || currentCircuitName == null) {
            showCustomMessageDialog(
                    "Please select a project and open a circuit first.",
                    "No Circuit Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String key = selectedProject + ":" + currentCircuitName;
        Circuit circuit = circuitObjects.get(key);

        if (circuit == null || circuit.getGates().isEmpty()) {
            showCustomMessageDialog(
                    "The circuit is empty. Add some gates first.",
                    "Empty Circuit",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            // Evaluate the entire circuit (this will handle circuit components internally)
            circuit.evaluate();

            // Get the final output
            boolean finalOutput = getFinalCircuitOutput(circuit);

            // Display results
            showFinalOutput(finalOutput, circuit.getName());

        } catch (Exception ex) {
            showCustomMessageDialog(
                    "Error during simulation: " + ex.getMessage(),
                    "Simulation Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Method to display the final output
    private void showFinalOutput(boolean output, String circuitName) {
        String outputValue = output ? "1" : "0";
        String message = "<html>" +
                "<div style='text-align:center; font-family:Segoe UI, sans-serif; padding:10px; "
                + "background:#f2f8ff; border:2px solid #8cb3d9; border-radius:10px;'> " +

                "<h2 style='margin:0; color:#2a4f78;'>⚡ Circuit Simulation</h2>" +

                "<p style='margin:8px 0; font-size:14px; color:#345;'>"
                + "<b>Circuit:</b> " + circuitName +
                "</p>" +

                "<div style='margin-top:12px; padding:12px; "
                + "background:#ffffff; border:2px solid #8cb3d9; "
                + "border-radius:10px;'> " +

                "<p style='font-size:22px; font-weight:bold; margin:0; color:"
                + (output ? "#1e8449" : "#c0392b") + ";'>"
                + "Final Output: " + outputValue +
                "</p>" +
                "</div>" +

                "</div>" +
                "</html>";

        JOptionPane.showMessageDialog(this,
                message,
                "Simulation Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean getFinalCircuitOutput(Circuit circuit) {
        // Look for gates that have no outgoing connections (final outputs)
        for (ComponentBase gate : circuit.getGates()) {
            boolean hasOutputConnections = false;

            for (Connector wire : circuit.getWires()) {
                if (wire.getFromGate() == gate) {
                    hasOutputConnections = true;
                    break;
                }
            }

            // If no output connections, this is likely a final output
            if (!hasOutputConnections && gate.getOutputs() > 0) {
                return gate.getOutputValue(0);
            }
        }

        // Fallback: return first gate's output
        if (!circuit.getGates().isEmpty()) {
            return circuit.getGates().get(0).getOutputValue(0);
        }

        return false;
    }

    /**
     * Remove a circuit from all other circuits that use it as a component
     */
    private void removeCircuitFromAllParentCircuits(String circuitNameToRemove) {
        System.out.println("DEBUG: Removing circuit '" + circuitNameToRemove + "' from all parent circuits");

        // Iterate through all projects and circuits
        for (Map.Entry<String, List<String>> projectEntry : projectCircuits.entrySet()) {
            String projectName = projectEntry.getKey();
            List<String> circuits = projectEntry.getValue();

            for (String circuitName : circuits) {
                // Skip the circuit we're removing
                if (circuitName.equals(circuitNameToRemove)) {
                    continue;
                }

                // Get the circuit object
                String key = projectName + ":" + circuitName;
                Circuit circuit = circuitObjects.get(key);

                if (circuit != null) {
                    // Look for CircuitComponent gates that reference the circuit to remove
                    List<ComponentBase> gatesToRemove = new ArrayList<>();

                    for (ComponentBase gate : circuit.getGates()) {
                        if (gate instanceof CircuitComponent) {
                            CircuitComponent circuitComp = (CircuitComponent) gate;
                            String referencedCircuitName = circuitComp.getCircuitDisplayName();

                            if (circuitNameToRemove.equals(referencedCircuitName)) {
                                gatesToRemove.add(gate);
                                System.out.println("DEBUG: Found and marking for removal: " +
                                        circuitComp.getCircuitDisplayName() + " from circuit " + circuitName);
                            }
                        }
                    }

                    // Remove the found CircuitComponent gates
                    for (ComponentBase gateToRemove : gatesToRemove) {
                        circuit.removeGate(gateToRemove);
                        System.out.println("DEBUG: Removed circuit component from " + circuitName);
                    }

                    // If this is the currently displayed circuit, refresh the display
                    if (circuitName.equals(currentCircuitName) && selectedProject != null
                            && selectedProject.equals(projectName)) {
                        circuitPanel.addCircuitComponents(circuit);
                        circuitPanel.repaint();
                    }
                }
            }
        }

        System.out.println("DEBUG: Finished removing circuit '" + circuitNameToRemove + "' from all parent circuits");
    }

    /**
     * Check if a circuit is being used by any other circuits as a component
     */
    private boolean isCircuitUsedByOthers(String circuitName) {
        for (Map.Entry<String, List<String>> projectEntry : projectCircuits.entrySet()) {
            String projectName = projectEntry.getKey();
            List<String> circuits = projectEntry.getValue();

            for (String otherCircuitName : circuits) {
                if (otherCircuitName.equals(circuitName)) {
                    continue; // Skip self
                }

                String key = projectName + ":" + otherCircuitName;
                Circuit circuit = circuitObjects.get(key);

                if (circuit != null) {
                    for (ComponentBase gate : circuit.getGates()) {
                        if (gate instanceof CircuitComponent) {
                            CircuitComponent circuitComp = (CircuitComponent) gate;
                            if (circuitName.equals(circuitComp.getCircuitDisplayName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private String getSelectedProjectName() {
        if (projectPanel.list != null && projectPanel.list.getSelectedValue() != null) {
            String selected = projectPanel.list.getSelectedValue().toString();
            // Return only if it's a project (not a circuit)
            if (!selected.startsWith("   → ")) {
                return selected;
            }
        }

        // If no project is selected in UI but we have a loaded project, return it
        if (selectedProject != null) {
            return selectedProject;
        }

        showCustomMessageDialog("Selection Required", "Please select a project first!", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    /**
     * Create a styled button for dialogs
     */
    private JButton createDialogButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(173, 216, 230)); // Light blue
        button.setForeground(new Color(70, 130, 180)); // Dark blue
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(183, 226, 240));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(173, 216, 230));
            }
        });

        return button;
    }

    private void connectCircuitAsComponent() {
        if (selectedProject == null) {
            showCustomMessageDialog("No Project", "Select a project first.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentCircuitName == null) {
            showCustomMessageDialog("No Circuit", "Open a circuit first.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> circuitsInProject = projectCircuits.get(selectedProject);
        if (circuitsInProject == null || circuitsInProject.isEmpty()) {
            showCustomMessageDialog("No Circuits", "No circuits in this project.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prepare available circuits (exclude current circuit)
        List<String> availableCircuits = new ArrayList<>();
        for (String circuitName : circuitsInProject) {
            if (!circuitName.equals(currentCircuitName)) {
                availableCircuits.add(circuitName);
            }
        }

        if (availableCircuits.isEmpty()) {
            showCustomMessageDialog("No Circuits Available",
                    "No other circuits available to connect.\nCreate another circuit first.",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create selection dialog
        JDialog connectDialog = createThemedDialog("Connect Circuit as Component", 400, 300);

        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialogPanel.setBackground(PANEL_BG_DARK);

        // Title
        JLabel titleLabel = new JLabel("Select a circuit to use as component in: " + currentCircuitName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_LIGHT);
        dialogPanel.add(titleLabel, BorderLayout.NORTH);

        // Circuit list
        DefaultListModel<String> circuitListModel = new DefaultListModel<>();
        for (String circuitName : availableCircuits) {
            circuitListModel.addElement(circuitName);

            // Show circuit details
            String key = selectedProject + ":" + circuitName;
            Circuit circuit = circuitObjects.get(key);
            if (circuit != null) {
                int inputCount = countCircuitInputs(circuit);
                int outputCount = countCircuitOutputs(circuit);
                circuitListModel.addElement("    → Inputs: " + inputCount + ", Outputs: " + outputCount);
            }
        }

        JList<String> circuitList = new JList<>(circuitListModel);
        circuitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        circuitList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        circuitList.setBackground(BUTTON_BG_DARK);
        circuitList.setForeground(TEXT_LIGHT);
        circuitList.setSelectionBackground(BUTTON_BG_HOVER);

        // Only allow selection of circuit names (not details)
        circuitList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value.toString().startsWith("    →")) {
                    setForeground(Color.GRAY);
                    setFont(getFont().deriveFont(Font.ITALIC, 11f));
                    if (isSelected) {
                        setBackground(list.getSelectionBackground());
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(circuitList);
        scrollPane.setPreferredSize(new Dimension(350, 150));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT));
        dialogPanel.add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(PANEL_BG_DARK);
        infoArea.setForeground(TEXT_LIGHT);
        infoArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_SOFT),
                "Circuit Info",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 10),
                TEXT_LIGHT));
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoArea.setText("Selected circuit will appear as a component box with input/output pins.\n" +
                "You can connect its pins to other gates in the current circuit.");
        dialogPanel.add(infoArea, BorderLayout.SOUTH);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PANEL_BG_DARK);

        JButton connectButton = createThemeButton("Connect as Component");
        JButton cancelButton = createThemeButton("Cancel");

        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);

        // Add components to dialog
        connectDialog.add(dialogPanel, BorderLayout.CENTER);
        connectDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Connect button action
        connectButton.addActionListener(connectEvent -> {
            String selectedCircuitName = circuitList.getSelectedValue();
            if (selectedCircuitName == null || selectedCircuitName.startsWith("    →")) {
                showCustomMessageDialog("Invalid Selection",
                        "Please select a circuit name (not the details line).",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get the circuit to connect
            String keyToConnect = selectedProject + ":" + selectedCircuitName;
            Circuit circuitToConnect = circuitObjects.get(keyToConnect);

            // Get the current circuit
            String currentKey = selectedProject + ":" + currentCircuitName;
            Circuit currentCircuit = circuitObjects.get(currentKey);

            if (circuitToConnect != null && currentCircuit != null) {
                // Add the circuit as a component to the current circuit
                boolean success = addCircuitAsComponent(currentCircuit, circuitToConnect, selectedCircuitName);
                if (success) {
                    // Refresh the circuit panel to show the new component
                    circuitPanel.setCurrentCircuit(currentCircuit);
                    circuitPanel.addCircuitComponents(currentCircuit);

                    connectDialog.dispose();
                    showCustomMessageDialog("Circuit Connected",
                            "Circuit '" + selectedCircuitName + "' added as component!\n" +
                                    "It will appear as a box with input/output pins that you can connect to other gates.",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showCustomMessageDialog("Error",
                            "Failed to add circuit as component.",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Cancel button action
        cancelButton.addActionListener(cancelEvent -> {
            connectDialog.dispose();
        });

        // Double-click to connect
        circuitList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = circuitList.getSelectedValue();
                    if (selected != null && !selected.startsWith("    →")) {
                        connectButton.doClick();
                    }
                }
            }
        });

        connectDialog.setVisible(true);
    }

    /**
     * Add a circuit as a component to another circuit
     */
    private boolean addCircuitAsComponent(Circuit parentCircuit, Circuit childCircuit, String childCircuitName) {
        try {
            // Create a special component that represents the child circuit
            CircuitComponent circuitComponent = new CircuitComponent(childCircuit, childCircuitName);

            // Position it at a reasonable location
            int x = 100 + (parentCircuit.getGates().size() * 120) % 600;
            int y = 100 + (parentCircuit.getGates().size() * 80) % 400;
            circuitComponent.setPosition(x, y);

            // Add to parent circuit
            parentCircuit.addGate(circuitComponent);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int countCircuitInputs(Circuit circuit) {
        if (circuit == null)
            return 0;
        return circuit.countCircuitInputs();
    }

    /**
     * Count the number of output pins a circuit would have as a component
     */
    private int countCircuitOutputs(Circuit circuit) {
        if (circuit == null)
            return 0;
        return circuit.countCircuitOutputs();
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(MainUI::new);
    // }
}