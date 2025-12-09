package org.yourcompany.yourproject.GUI2.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class FrontPage extends JFrame {

    // --- COLOR PALETTE ---
    private static final Color BG_DARK = new Color(30, 30, 35);
    private static final Color ACCENT_COLOR = new Color(64, 224, 208); // Cyan
    
    private CircuitPanel backgroundPanel;

    public FrontPage() {
        setTitle("LogiSim - Welcome");
        
        // 1. Full Screen Settings
        setUndecorated(true); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 2. Main Container using OverlayLayout
        // OverlayLayout automatically stacks panels on top of each other 
        // and stretches them to fill the frame. This FIXES the centering issue.
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        setContentPane(layeredPane);

        // --- LAYER 1: Background Animation (Bottom) ---
        backgroundPanel = new CircuitPanel();
        // We set alignment to 0.5f (Center) for OverlayLayout safety
        backgroundPanel.setAlignmentX(0.5f);
        backgroundPanel.setAlignmentY(0.5f);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        // --- LAYER 2: Centered Content (Middle) ---
        JPanel contentPanel = createContentPanel();
        contentPanel.setAlignmentX(0.5f);
        contentPanel.setAlignmentY(0.5f);
        layeredPane.add(contentPanel, JLayeredPane.PALETTE_LAYER);
        
        // --- LAYER 3: Close Button (Top Right) ---
        JPanel controlsPanel = createControlsPanel();
        controlsPanel.setAlignmentX(0.5f);
        controlsPanel.setAlignmentY(0.5f);
        layeredPane.add(controlsPanel, JLayeredPane.MODAL_LAYER);

        setVisible(true);
    }

    // Creates the panel with the "X" button
    private JPanel createControlsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false); // Transparent
        
        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(200, 50, 50)); // Red
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setPreferredSize(new Dimension(50, 30));
        
        // Hover effect for close button
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setBackground(Color.RED); }
            public void mouseExited(MouseEvent e) { closeBtn.setBackground(new Color(200, 50, 50)); }
        });
        
        closeBtn.addActionListener(e -> System.exit(0));

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRight.setOpaque(false);
        topRight.add(closeBtn);
        
        p.add(topRight, BorderLayout.NORTH);
        return p;
    }

    private JPanel createContentPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 20, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // --- 1. THE LOGO ---
        JLabel logoLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int s = Math.min(getWidth(), getHeight());
                int x = (getWidth() - s) / 2;
                
                g2.setColor(new Color(255, 255, 255, 20)); 
                g2.fillRoundRect(x + 10, 10, s - 20, s - 20, 20, 20);
                g2.setStroke(new BasicStroke(3));
                g2.setColor(ACCENT_COLOR);
                g2.drawRoundRect(x + 10, 10, s - 20, s - 20, 20, 20);
                
                g2.drawLine(x + s/2, 10, x + s/2, s/2);
                g2.drawLine(x + s/2, s/2, x + s - 30, s - 30);
                g2.fillOval(x + s/2 - 5, s/2 - 5, 10, 10); 
            }
        };
        logoLabel.setPreferredSize(new Dimension(120, 120));
        p.add(logoLabel, gbc);

        // --- 2. TITLE ---
        gbc.gridy++;
        JLabel title = new JLabel("LOGISIM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 52));
        title.setForeground(Color.WHITE);
        p.add(title, gbc);

        // --- 3. SUBTITLE ---
        gbc.gridy++;
        JLabel subtitle = new JLabel("Build. Simulate. Analyze.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(new Color(200, 200, 200));
        p.add(subtitle, gbc);

        // --- 4. SPACER ---
        gbc.gridy++;
        p.add(Box.createVerticalStrut(30), gbc);

        // --- 5. BUTTON ---
        gbc.gridy++;
        ModernButton startBtn = new ModernButton("Initialize System");
        startBtn.setPreferredSize(new Dimension(220, 55));
        
        startBtn.addActionListener(e -> {
            backgroundPanel.stopAnimation();
            dispose();
            new MainUI(); 
        });
        
        p.add(startBtn, gbc);
        return p;
    }

    // =========================================================================
    //  ANIMATED CIRCUIT BACKGROUND (Updated for Full Screen Particles)
    // =========================================================================
    class CircuitPanel extends JPanel {
        private final List<Particle> particles = new ArrayList<>();
        private final Timer timer;
        private final int PARTICLE_COUNT = 80; // Increased count for bigger screen
        private final int CONNECTION_DIST = 150;

        public CircuitPanel() {
            setBackground(BG_DARK);
            
            // Generate particles
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                particles.add(new Particle());
            }

            timer = new Timer(16, e -> {
                for (Particle p : particles) p.update();
                repaint();
            });
            timer.start();
        }
        
        public void stopAnimation() { timer.stop(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(64, 224, 208, 40));
            g2.setStroke(new BasicStroke(1));

            for (int i = 0; i < particles.size(); i++) {
                Particle p1 = particles.get(i);
                g2.setColor(new Color(64, 224, 208, 150));
                g2.fillOval((int)p1.x, (int)p1.y, 4, 4);

                for (int j = i + 1; j < particles.size(); j++) {
                    Particle p2 = particles.get(j);
                    double dist = Math.hypot(p1.x - p2.x, p1.y - p2.y);
                    if (dist < CONNECTION_DIST) {
                        int alpha = (int) ((1.0 - (dist / CONNECTION_DIST)) * 100);
                        g2.setColor(new Color(64, 224, 208, alpha));
                        g2.drawLine((int)p1.x + 2, (int)p1.y + 2, (int)p2.x + 2, (int)p2.y + 2);
                    }
                }
            }
        }

        private class Particle {
            double x, y, dx, dy;

            Particle() {
                // FIX: Get ACTUAL screen size so particles spawn everywhere
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                x = Math.random() * screen.width;
                y = Math.random() * screen.height;
                dx = (Math.random() - 0.5) * 1.5;
                dy = (Math.random() - 0.5) * 1.5;
            }

            void update() {
                x += dx;
                y += dy;
                if (x < 0 || x > getWidth()) dx = -dx;
                if (y < 0 || y > getHeight()) dy = -dy;
            }
        }
    }

    // =========================================================================
    //  MODERN BUTTON CLASS (Unchanged)
    // =========================================================================
    class ModernButton extends JButton {
        private Color hoverColor = new Color(80, 240, 220);
        private Color baseColor = ACCENT_COLOR;
        private boolean isHovered = false;

        public ModernButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(BG_DARK);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered ? hoverColor : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 100), 0, getHeight()/2, new Color(255, 255, 255, 0));
            g2.setPaint(shine);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FrontPage::new);
    }
}