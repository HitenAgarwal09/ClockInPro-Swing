package ui;
import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.awt.image.BufferedImage;

public class LoginUI extends JFrame {

    // Color Palette (matching the image)
    private static final Color BG_LEFT        = new Color(220, 217, 212); // warm grey left panel
    private static final Color BG_RIGHT       = new Color(235, 233, 229); // slightly lighter right panel
    private static final Color FIELD_BG       = new Color(245, 244, 241); // input field background
    private static final Color FIELD_BORDER   = new Color(210, 207, 202); // subtle border
    private static final Color ACCENT         = new Color(110, 60, 65);   // dark mauve/burgundy
    private static final Color ACCENT_HOVER   = new Color(90, 45, 50);
    private static final Color TEXT_DARK      = new Color(30, 28, 28);
    private static final Color TEXT_MID       = new Color(90, 85, 80);
    private static final Color TEXT_LIGHT     = new Color(160, 155, 150);
    private static final Color DIVIDER        = new Color(200, 197, 192);
    private static final Color LINK_COLOR     = new Color(110, 60, 65);
    private JTextField userField;
    private JPasswordField passField;


    public LoginUI() {
        setTitle("ClockInPro - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 660));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);

        // Custom title bar color on some L&F — use default for compatibility
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new GridLayout(1, 2, 0, 0));
        root.add(buildLeftPanel());
        root.add(buildRightPanel());
        setContentPane(root);
        setVisible(true);
    }

    // ── Left Panel ───────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(205, 200, 194),
                        getWidth(), getHeight(), new Color(190, 185, 178));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Text block at bottom-left
        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);
        textBlock.setBorder(new EmptyBorder(0, 40, 55, 20));

        JLabel welcome = new JLabel("WELCOME TO");
        welcome.setFont(new Font("Georgia", Font.PLAIN, 16));
        welcome.setForeground(new Color(70, 65, 60));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("CLOCKINPRO");
        brand.setFont(new Font("Georgia", Font.BOLD, 34));
        brand.setForeground(TEXT_DARK);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html>Smart Attendance,<br>Better Management.</html>");
        tagline.setFont(new Font("Georgia", Font.PLAIN, 15));
        tagline.setForeground(TEXT_MID);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        textBlock.add(welcome);
        textBlock.add(Box.createVerticalStrut(4));
        textBlock.add(brand);
        textBlock.add(Box.createVerticalStrut(10));
        textBlock.add(tagline);

        panel.add(textBlock, BorderLayout.SOUTH);

        // Clock icon top-left
        JPanel clockWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 28));
        clockWrap.setOpaque(false);
        clockWrap.add(buildClockIcon());
        panel.add(clockWrap, BorderLayout.NORTH);

        return panel;
    }

    private JComponent buildClockIcon() {
        return new JComponent() {
            { setPreferredSize(new Dimension(72, 72)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d = 68;
                // Circle
                g2.setColor(new Color(55, 50, 48));
                g2.fillOval(2, 2, d, d);
                // Hour hand
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 2 + d / 2, cy = 2 + d / 2;
                g2.drawLine(cx, cy, cx - 10, cy - 16);
                // Minute hand
                g2.drawLine(cx, cy, cx + 14, cy - 6);
                // Center dot
                g2.setColor(new Color(200, 100, 80));
                g2.fillOval(cx - 3, cy - 3, 6, 6);
                g2.dispose();
            }
        };
    }

    // ── Right Panel ──────────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_RIGHT);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel card = buildCard();
        outer.add(card);
        return outer;
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(238, 236, 232));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(215, 212, 208));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 44, 30, 44));
        card.setPreferredSize(new Dimension(560, 430));

        // ── Header ──
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent avatar = buildAvatarIcon();
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.setBorder(new EmptyBorder(0, 16, 0, 0));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel sub = new JLabel("Enter your credentials to login");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MID);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);

        header.add(avatar);
        header.add(titleBlock);
        card.add(header);
        card.add(Box.createVerticalStrut(34));

        // ── Fields ──
        userField = styledTextField("Enter your username");
        card.add(buildField("Username", userField));
        card.add(Box.createVerticalStrut(14));

        // Password with eye toggle
        JPanel pwRow = buildPasswordRow("Password", "Enter your password");
        card.add(pwRow);
        card.add(Box.createVerticalStrut(34));

        // ── Login Button ──
        JButton loginBtn = new JButton("Login") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? ACCENT_HOVER : ACCENT;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(460, 44));
        loginBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setBorderPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> {

            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter username and password");
                return;
            }

            try {
                Connection conn = DatabaseConnection.getConnection();

                String query = "SELECT * FROM users WHERE username=? AND password=?";
                PreparedStatement ps = conn.prepareStatement(query);

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login Successful!");
//                    try {
//
//                        String query2 = "INSERT INTO attendance (username, date, check_in) VALUES (?, CURDATE(), CURTIME())";
//
//                        PreparedStatement ps2 = conn.prepareStatement(query2);
//
//                        ps2.setString(1, username);
//
//                        ps2.executeUpdate();
//
//                        System.out.println("Check-in saved!");
//
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }

                    // 👉 NEXT (later)
                    new DashboardUI(username);
                    dispose();

                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Credentials");
                }

                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error");
            }
        });
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(14));

        // ── OR divider ──
        card.add(buildOrDivider());
        card.add(Box.createVerticalStrut(14));

        // ── Register link ──
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerRow.setOpaque(false);
        registerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dontHaveLbl = new JLabel("Don't have an account?");
        dontHaveLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dontHaveLbl.setForeground(TEXT_MID);

        JLabel registerLink = new JLabel("Register here");
        registerLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerLink.setForeground(LINK_COLOR);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new RegisterUI();
                LoginUI.this.dispose();
            }
            @Override public void mouseEntered(MouseEvent e) { registerLink.setForeground(ACCENT_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { registerLink.setForeground(LINK_COLOR); }
        });

        registerRow.add(dontHaveLbl);
        registerRow.add(registerLink);
        card.add(registerRow);

        return card;
    }

    // ── Avatar Icon ──────────────────────────────────────────────────────────
    private JComponent buildAvatarIcon() {
        return new JComponent() {
            { setPreferredSize(new Dimension(58, 58)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Circle background
                g2.setColor(new Color(50, 46, 44));
                g2.fillOval(0, 0, 56, 56);
                // Head
                g2.setColor(new Color(200, 196, 192));
                g2.fillOval(18, 10, 20, 20);
                // Body
                g2.fillArc(8, 34, 40, 30, 0, 180);
                g2.dispose();
            }
        };
    }

    // ── Field Row ────────────────────────────────────────────────────────────
//    private JPanel buildField(String label, String placeholder, boolean isPassword) {
//        JPanel row = new JPanel(new BorderLayout(14, 0));
//        row.setOpaque(false);
//        row.setAlignmentX(Component.LEFT_ALIGNMENT);
//        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
//
//        JLabel lbl = fieldLabel(label);
//        row.add(lbl, BorderLayout.WEST);
//        lbl.setPreferredSize(new Dimension(130, 36));
//
//        JTextField tf = styledTextField(placeholder);
//        row.add(tf, BorderLayout.CENTER);
//        return row;
//    }
    private JPanel buildField(String label, JTextField tf) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));

        JLabel lbl = fieldLabel(label);
        lbl.setPreferredSize(new Dimension(130, 36));
        row.add(lbl, BorderLayout.WEST);

        row.add(tf, BorderLayout.CENTER);

        return row;
    }

    // ── Password Row with Eye Toggle ─────────────────────────────────────────
    private JPanel buildPasswordRow(String label, String placeholder) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));

        JLabel lbl = fieldLabel(label);
        lbl.setPreferredSize(new Dimension(130, 36));
        row.add(lbl, BorderLayout.WEST);

//        JPasswordField pf = new JPasswordField() {
//            @Override protected void paintComponent(Graphics g) {
//                super.paintComponent(g);
//                if (getPassword().length == 0) {
//                    Graphics2D g2p = (Graphics2D) g.create();
//                    g2p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                    g2p.setColor(TEXT_LIGHT);
//                    g2p.setFont(getFont());
//                    FontMetrics fm = g2p.getFontMetrics();
//                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
//                    g2p.drawString(placeholder, getInsets().left, y);
//                    g2p.dispose();
//                }
//            }
//        };
        passField = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // draw placeholder ONLY when empty and not focused
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g2p = (Graphics2D) g.create();
                    g2p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2p.setColor(TEXT_LIGHT);
                    g2p.setFont(getFont());

                    FontMetrics fm = g2p.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                    g2p.drawString("Enter your password", getInsets().left, y);
                    g2p.dispose();
                }
            }
        };

        passField.setEchoChar('●'); // IMPORTANT
        styleComponent(passField);
        passField.setOpaque(false);
        passField.setBorder(new EmptyBorder(0, 12, 0, 12));
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passField.setForeground(TEXT_DARK);

        boolean[] isVisible = {false};

        // Eye button
        JButton eye = new JButton() {
            { setPreferredSize(new Dimension(36, 36));
                setBorderPainted(false); setContentAreaFilled(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFocusPainted(false);
                addActionListener(e -> {
                    isVisible[0] = !isVisible[0];
                    passField.setEchoChar(isVisible[0] ? (char)0 : '●');
                    repaint();
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEXT_LIGHT);
                int cx = getWidth()/2, cy = getHeight()/2;
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawArc(cx-9, cy-6, 18, 12, 0, 180);
                g2.drawArc(cx-9, cy-6, 18, 12, 0, -180);
                g2.fillOval(cx-3, cy-3, 6, 6);
                if (!isVisible[0]) {
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawLine(cx-10, cy+7, cx+10, cy-7);
                }
                g2.dispose();
            }
        };

        JPanel fieldWrap = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(FIELD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        fieldWrap.setOpaque(false);
        fieldWrap.add(passField, BorderLayout.CENTER);
        fieldWrap.add(eye, BorderLayout.EAST);
        fieldWrap.setPreferredSize(new Dimension(0, 38));

        row.add(fieldWrap, BorderLayout.CENTER);
        return row;
    }

    // ── OR Divider ────────────────────────────────────────────────────────────
    private JPanel buildOrDivider() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        JSeparator left = new JSeparator(); left.setForeground(DIVIDER);
        p.add(left, gbc);

        gbc.weightx = 0;
        gbc.insets = new Insets(0, 10, 0, 10);
        JLabel or = new JLabel("OR");
        or.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        or.setForeground(TEXT_LIGHT);
        p.add(or, gbc);

        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JSeparator right = new JSeparator(); right.setForeground(DIVIDER);
        p.add(right, gbc);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);

                if (getText().isEmpty()) {
                    Graphics2D g2p = (Graphics2D) g.create();
                    g2p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2p.setColor(TEXT_LIGHT);
                    g2p.setFont(getFont());
                    FontMetrics fm = g2p.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2p.drawString(placeholder, getInsets().left, y);
                    g2p.dispose();
                }
            }
        };
        styleComponent(tf);
        tf.setForeground(TEXT_DARK);
        return tf;
    }

    private void styleComponent(JComponent c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(FIELD_BG);
        c.setOpaque(false);
        c.setBorder(new CompoundBorder(
                new RoundedBorder(8, FIELD_BORDER, FIELD_BG),
                new EmptyBorder(0, 12, 0, 12)));
        c.setPreferredSize(new Dimension(0, 38));
    }

    // ── Rounded Border Helper ─────────────────────────────────────────────────
    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color borderColor, bgColor;
        RoundedBorder(int r, Color border, Color bg) { radius = r; borderColor = border; bgColor = bg; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 10, 4, 10); }
        @Override public boolean isBorderOpaque() { return false; }
    }
}
