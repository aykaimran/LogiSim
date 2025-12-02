package org.yourcompany.yourproject.GUI2.ui;

import javax.swing.*;
import java.awt.*;

public class ProjectPanel extends JPanel {

    public DefaultListModel<String> model;
    public JList<String> list;

    public ProjectPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 800));
        setBorder(BorderFactory.createTitledBorder("Projects"));

        model = new DefaultListModel<>();
        list = new JList<>(model);

        add(new JScrollPane(list), BorderLayout.CENTER);
    }
}
