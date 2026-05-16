package ui;
import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class RegisterUI  extends JFrame {

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

    private JTextField nameField, emailField, phoneField, userField;
    private JComboBox<String> deptCb, roleCb;
    private JPasswordField passField, confPassField;
    private JLabel errorLabel;

    public RegisterUI () {
        setTitle("ClockInPro - Register");
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
            private Image bgImage = loadImage();

            private Image loadImage() {
                // 1. Try loading from classpath (Best practice, works in JARs and properly configured IDEs)
                java.net.URL imgURL = getClass().getResource("/assests/register.png");
                if (imgURL != null) return new ImageIcon(imgURL).getImage();

                // 2. Fallback: Running from project root
                java.io.File f = new java.io.File("assests/register.png");
                if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();

                // 3. Fallback: Running from a 'bin' or 'out' subdirectory
                f = new java.io.File("../assests/register.png");
                if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();

                return null;
            }

            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null && bgImage.getWidth(null) > 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    double panelAspect = (double) getWidth() / getHeight();
                    double imgAspect = (double) bgImage.getWidth(null) / bgImage.getHeight(null);
                    int drawW = getWidth();
                    int drawH = getHeight();
                    int x = 0;
                    int y = 0;
                    if (panelAspect > imgAspect) {
                        drawH = (int) (getWidth() / imgAspect);
                        y = (getHeight() - drawH) / 2;
                    } else {
                        drawW = (int) (getHeight() * imgAspect);
                        x = (getWidth() - drawW) / 2;
                    }
                    g2.drawImage(bgImage, x, y, drawW, drawH, this);
                    g2.dispose();
                } else {
                    g.setColor(new Color(220, 217, 212));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        return panel;
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
        card.setPreferredSize(new Dimension(560, 670));

        // ── Header ──
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent avatar = buildAvatarIcon();
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.setBorder(new EmptyBorder(0, 16, 0, 0));

        JLabel title = new JLabel("Create Your Account");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel sub = new JLabel("Fill in the details to get started");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MID);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);

        header.add(avatar);
        header.add(titleBlock);
        card.add(header);
        card.add(Box.createVerticalStrut(24));

        // ── Fields ──
        JTextField[] tfArr = new JTextField[1];
        card.add(buildField("Full Name", "Enter your full name", tfArr)); nameField = tfArr[0];
        card.add(Box.createVerticalStrut(10));
        card.add(buildField("Email", "Enter your email address", tfArr)); emailField = tfArr[0];
        card.add(Box.createVerticalStrut(10));
        card.add(buildField("Phone", "Enter your phone number", tfArr)); phoneField = tfArr[0];
        card.add(Box.createVerticalStrut(10));

        JComboBox<String>[] cbArr = new JComboBox[1];
        card.add(buildDropdown("Department", new String[]{"Select department", "HR", "Engineering", "Sales", "Finance", "Operations"}, cbArr)); deptCb = cbArr[0];
        card.add(Box.createVerticalStrut(10));
        card.add(buildDropdown("Role", new String[]{"Select role", "Employee", "Manager", "Admin", "HR Executive"}, cbArr)); roleCb = cbArr[0];
        card.add(Box.createVerticalStrut(10));

        card.add(buildField("Username", "Choose a username", tfArr)); userField = tfArr[0];
        card.add(Box.createVerticalStrut(10));

        // Password with eye toggle
        JPasswordField[] pfArr = new JPasswordField[1];
        JPanel pwRow = buildPasswordRow("Password", "Enter your password", pfArr); passField = pfArr[0];
        card.add(pwRow);
        card.add(Box.createVerticalStrut(10));
        JPanel cpwRow = buildPasswordRow("Confirm Password", "Confirm your password", pfArr); confPassField = pfArr[0];
        card.add(cpwRow);
        card.add(Box.createVerticalStrut(20));

        // Error Label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        errorLabel.setForeground(new Color(220, 60, 60)); // Red
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel errorRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        errorRow.setOpaque(false);
        errorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorRow.add(errorLabel);

        card.add(errorRow);
        card.add(Box.createVerticalStrut(5));

        // ── Register Button ──
        JButton registerBtn = new JButton("Register") {
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
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(460, 44));
        registerBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> {
            errorLabel.setForeground(new Color(220, 60, 60)); // Reset to red
            errorLabel.setText(" ");

            if (nameField.getText().trim().isEmpty()) {
                errorLabel.setText("Full Name is required.");
                return;
            }
            String email = emailField.getText().trim();
            if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
                errorLabel.setText("A valid Email is required.");
                return;
            }
            if (phoneField.getText().trim().isEmpty()) {
                errorLabel.setText("Phone number is required.");
                return;
            }
            if (deptCb.getSelectedIndex() <= 0) {
                errorLabel.setText("Please select a Department.");
                return;
            }
            if (roleCb.getSelectedIndex() <= 0) {
                errorLabel.setText("Please select a Role.");
                return;
            }
            if (userField.getText().trim().isEmpty()) {
                errorLabel.setText("Username is required.");
                return;
            }
            if (passField.getPassword().length == 0) {
                errorLabel.setText("Password is required.");
                return;
            }
            if (confPassField.getPassword().length == 0) {
                errorLabel.setText("Please confirm your password.");
                return;
            }
            if (!java.util.Arrays.equals(passField.getPassword(), confPassField.getPassword())) {
                errorLabel.setText("Passwords do not match.");
                return;
            }

            try {
                Connection conn = DatabaseConnection.getConnection();

                String query = "INSERT INTO users (full_name, email, phone, department, role, username, password) VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement ps = conn.prepareStatement(query);

                ps.setString(1, nameField.getText());
                ps.setString(2, emailField.getText());
                ps.setString(3, phoneField.getText());
                ps.setString(4, deptCb.getSelectedItem().toString());
                ps.setString(5, roleCb.getSelectedItem().toString());
                ps.setString(6, userField.getText());
                ps.setString(7, new String(passField.getPassword()));

                ps.executeUpdate();

                errorLabel.setForeground(new Color(50, 150, 50)); // Green
                errorLabel.setText("Registration successful!");

                JOptionPane.showMessageDialog(this, "User Registered Successfully!");

                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setForeground(new Color(220, 60, 60));
                errorLabel.setText("Error saving data.");
            }
        });
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(14));

        // ── OR divider ──
        card.add(buildOrDivider());
        card.add(Box.createVerticalStrut(10));

        // ── Login link ──
        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        loginRow.setOpaque(false);
        loginRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel alreadyLbl = new JLabel("Already have an account?");
        alreadyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        alreadyLbl.setForeground(TEXT_MID);

        JLabel loginLink = new JLabel("Login here");
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginLink.setForeground(LINK_COLOR);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new LoginUI();
                RegisterUI.this.dispose();
            }
            @Override public void mouseEntered(MouseEvent e) { loginLink.setForeground(ACCENT_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { loginLink.setForeground(LINK_COLOR); }
        });

        loginRow.add(alreadyLbl);
        loginRow.add(loginLink);
        card.add(loginRow);

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
    private JPanel buildField(String label, String placeholder, JTextField[] outTf) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));

        JLabel lbl = fieldLabel(label);
        row.add(lbl, BorderLayout.WEST);
        lbl.setPreferredSize(new Dimension(130, 36));

        JTextField tf = styledTextField(placeholder);
        if (outTf != null) outTf[0] = tf;
        row.add(tf, BorderLayout.CENTER);
        return row;
    }

    // ── Password Row with Eye Toggle ─────────────────────────────────────────
    private JPanel buildPasswordRow(String label, String placeholder, JPasswordField[] outPf) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));

        JLabel lbl = fieldLabel(label);
        lbl.setPreferredSize(new Dimension(130, 36));
        row.add(lbl, BorderLayout.WEST);

        JPasswordField pf = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0) {
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
        if (outPf != null) outPf[0] = pf;
        pf.setEchoChar('●');
        pf.setOpaque(false);
        pf.setBorder(new EmptyBorder(0, 12, 0, 12));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setForeground(TEXT_DARK);

        boolean[] isVisible = {false};

        // Eye button
        JButton eye = new JButton() {
            { setPreferredSize(new Dimension(36, 36));
                setBorderPainted(false); setContentAreaFilled(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFocusPainted(false);
                addActionListener(e -> {
                    isVisible[0] = !isVisible[0];
                    pf.setEchoChar(isVisible[0] ? (char)0 : '●');
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
        fieldWrap.add(pf, BorderLayout.CENTER);
        fieldWrap.add(eye, BorderLayout.EAST);
        fieldWrap.setPreferredSize(new Dimension(0, 38));

        row.add(fieldWrap, BorderLayout.CENTER);
        return row;
    }

    // ── Dropdown Row ─────────────────────────────────────────────────────────
    private JPanel buildDropdown(String label, String[] items, JComboBox<String>[] outCb) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));

        JLabel lbl = fieldLabel(label);
        lbl.setPreferredSize(new Dimension(130, 36));
        row.add(lbl, BorderLayout.WEST);

        JComboBox<String> cb = new JComboBox<String>(items) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        if (outCb != null) outCb[0] = cb;
        cb.setOpaque(false);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(FIELD_BG);
        cb.setForeground(TEXT_LIGHT);
        cb.setBorder(new RoundedBorder(8, FIELD_BORDER, FIELD_BG));
        cb.setPreferredSize(new Dimension(0, 38));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,
                                                                    Object value, int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, idx, sel, focus);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setBackground(sel ? ACCENT : FIELD_BG);
                setForeground(sel ? Color.WHITE : (idx == 0 ? TEXT_LIGHT : TEXT_DARK));
                setBorder(new EmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
        row.add(cb, BorderLayout.CENTER);
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


//    }

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