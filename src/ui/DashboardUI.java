package ui;

import java.time.LocalDate;
import javax.swing.table.DefaultTableModel;
import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DashboardUI extends JFrame {

    private JLabel fullNameValue;
    private JLabel usernameValue;
    private JLabel departmentValue;
    private JLabel roleValue;
    private JLabel emailValue;
    private JLabel phoneValue;
    private JLabel attendanceCardLabel;
    private JLabel payrollCardLabel;
    private JLabel hoursCardLabel;
    private JLabel statusCardLabel;
    private JTable payrollTable;
    private DefaultTableModel payrollModel;
    private JTable attendanceTable;
    private DefaultTableModel attendanceModel;
    private String username;
    //    private JLabel checkInLabel;
//    private JLabel checkOutLabel;
    private JLabel firstCheckInLabel;
    private JLabel lastCheckOutLabel;
    private JLabel sessionLabel;
    private JLabel statusLabel;

    // COLORS (Modern Palette)
    private static final Color BG = new Color(235, 233, 229);
    private static final Color SIDEBAR = new Color(220, 217, 212);
    private static final Color HOVER = new Color(210, 207, 202);
    private static final Color TEXT = new Color(30, 28, 28);
    private static final Color BORDER = new Color(210, 207, 202);
    private static final Color ACCENT = new Color(110, 60, 65);
    private static final Color ACCENT_HOVER = new Color(90, 45, 50);
    private static final Color CARD_BG = new Color(245, 244, 241);

    // Card Layout
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public DashboardUI(String username) {

        this.username = username;

        setTitle("ClockInPro Dashboard");
        setSize(1250, 720);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        // CENTER CONTENT
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add Pages
        contentPanel.add(buildDashboardPage(), "dashboard");
        contentPanel.add(buildAttendancePage(), "attendance");
        contentPanel.add(buildPayrollPage(), "payroll");
        contentPanel.add(buildProfilePage(), "profile");

        add(contentPanel, BorderLayout.CENTER);
        loadTodayAttendance();
        loadAttendanceTable();
        loadPayrollTable();
        loadDashboardAnalytics();
        loadProfileData();
        setVisible(true);
    }

    // ================= SIDEBAR =================

    private JPanel buildSidebar() {

        JPanel sidebar = new JPanel();
        // Increase width to 320 for better proportion on full screen
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("ClockInPro");
        logo.setForeground(TEXT);
        // Increase font size and padding
        logo.setFont(new Font("Georgia", Font.BOLD, 32));
        logo.setBorder(new EmptyBorder(45, 40, 50, 10));

        sidebar.add(logo);

        sidebar.add(navItem("Dashboard", "dashboard"));
        sidebar.add(navItem("Attendance", "attendance"));
        sidebar.add(navItem("Payroll", "payroll"));
        sidebar.add(navItem("Profile", "profile"));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = createStyledButton("Logout");
        // Make the logout button wider
        logoutBtn.setMaximumSize(new Dimension(220, 50));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginUI();
        });

        sidebar.add(logoutBtn);
        // Add more space at the bottom
        sidebar.add(Box.createVerticalStrut(40));

        return sidebar;
    }

    // ================= NAVIGATION ITEM =================

    private JPanel navItem(String text, String cardName) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SIDEBAR);
        // Taller nav items
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        // More padding
        panel.setBorder(new EmptyBorder(8, 40, 8, 20));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        panel.add(label, BorderLayout.CENTER);

        // Hover + Click
        panel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(ACCENT);
                label.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(SIDEBAR);
                label.setForeground(TEXT);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(contentPanel, cardName);
            }
        });

        return panel;
    }

    // ================= DASHBOARD PAGE =================

    private JPanel buildDashboardPage() {

        JPanel page = new JPanel();
        page.setBackground(BG);
        page.setLayout(new BorderLayout());

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel welcome = new JLabel("Welcome, " + username + " 👋");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel sub = new JLabel("Dashboard Overview");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        sub.setForeground(ACCENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(welcome);
        left.add(Box.createVerticalStrut(5));
        left.add(sub);

        header.add(left, BorderLayout.WEST);

        page.add(header, BorderLayout.NORTH);

        // CARDS
        JPanel cards = new JPanel(new GridLayout(2, 2, 20, 20));
        cards.setBackground(BG);
        cards.setBorder(new EmptyBorder(20, 30, 30, 30));

        //cards.add(createCard("Today's Status", "Not Checked In"));
        cards.add(buildStatusCard());
        attendanceCardLabel = new JLabel("0%");
        payrollCardLabel = new JLabel("₹0");
        hoursCardLabel = new JLabel("0 Hours");

        cards.add(createDynamicCard("Attendance Rate", attendanceCardLabel));
        cards.add(createDynamicCard("Monthly Payroll", payrollCardLabel));
        cards.add(createDynamicCard("Working Hours", hoursCardLabel));

        page.add(cards, BorderLayout.CENTER);

        return page;
    }

    private JPanel createDynamicCard(String title, JLabel valueLabel) {

        JPanel card = new JPanel();
        card.setBackground(CARD_BG);

        card.setBorder(new CompoundBorder(
                new RoundedBorder(18, BORDER, CARD_BG),
                new EmptyBorder(20, 20, 20, 20)
        ));

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setForeground(ACCENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(TEXT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(valueLabel);

        return card;
    }

    private void loadDashboardAnalytics() {

        try {

            Connection conn = DatabaseConnection.getConnection();

            // ================= TOTAL HOURS =================

            String hoursQuery =
                    "SELECT SUM(TIMESTAMPDIFF(MINUTE, check_in, check_out))/60.0 AS total_hours " +
                            "FROM attendance " +
                            "WHERE username=? " +
                            "AND check_out IS NOT NULL";

            PreparedStatement ps1 = conn.prepareStatement(hoursQuery);

            ps1.setString(1, username);

            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {

                double hours = rs1.getDouble("total_hours");

                hoursCardLabel.setText(String.format("%.2f Hours", hours));
            }

            // ================= MONTHLY PAYROLL =================

            String payrollQuery =
                    "SELECT SUM(" +
                            "(TIMESTAMPDIFF(MINUTE, a.check_in, a.check_out)/60.0) * s.salary_per_hour" +
                            ") AS total_salary " +

                            "FROM attendance a " +

                            "JOIN users u ON a.username = u.username " +

                            "JOIN salary_structure s " +
                            "ON u.department = s.department " +
                            "AND u.role = s.role " +

                            "WHERE a.username=? " +
                            "AND a.check_out IS NOT NULL " +
                            "AND MONTH(a.date)=MONTH(CURDATE())";

            PreparedStatement ps2 = conn.prepareStatement(payrollQuery);

            ps2.setString(1, username);

            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {

                double salary = rs2.getDouble("total_salary");

                payrollCardLabel.setText("₹" + String.format("%.2f", salary));
            }

            // ================= ATTENDANCE RATE =================

            String attendanceQuery =
                    "SELECT COUNT(DISTINCT date) AS present_days " +
                            "FROM attendance " +
                            "WHERE username=?";

            PreparedStatement ps3 = conn.prepareStatement(attendanceQuery);

            ps3.setString(1, username);

            ResultSet rs3 = ps3.executeQuery();

            if (rs3.next()) {

                int present = rs3.getInt("present_days");

                LocalDate today = LocalDate.now();

                int totalDays = today.getDayOfMonth();

                double percentage = (present * 100.0) / totalDays;

                attendanceCardLabel.setText(
                        String.format("%.0f%%", percentage)
                );
            }

            // ================= CURRENT STATUS =================

            String statusQuery =
                    "SELECT * FROM attendance " +
                            "WHERE username=? " +
                            "AND date=CURDATE() " +
                            "AND check_out IS NULL";

            PreparedStatement ps4 = conn.prepareStatement(statusQuery);

            ps4.setString(1, username);

            ResultSet rs4 = ps4.executeQuery();

            if (rs4.next()) {

                statusCardLabel.setText("Working");

            } else {

                statusCardLabel.setText("Offline");
            }

            conn.close();

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }



    private JPanel buildStatusCard() {

        JPanel card = new JPanel();
        card.setBackground(CARD_BG);

        card.setBorder(new CompoundBorder(
                new RoundedBorder(18, BORDER, CARD_BG),
                new EmptyBorder(20, 20, 20, 20)
        ));

        card.setLayout(new GridLayout(5, 1, 10, 10));

        JLabel title = new JLabel("Today's Status");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        firstCheckInLabel = infoLabel("First Check In: --");
        lastCheckOutLabel = infoLabel("Last Check Out: --");
        sessionLabel = infoLabel("Sessions: 0");
        statusLabel = infoLabel("Status: Offline");

        card.add(title);
        card.add(firstCheckInLabel);
        card.add(lastCheckOutLabel);
        card.add(sessionLabel);
        card.add(statusLabel);

        return card;
    }

    // ================= ATTENDANCE PAGE =================

    private JPanel buildAttendancePage() {

        JPanel page = createPage("Attendance");

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        // ===== TOP BUTTONS =====

        JPanel top = new JPanel();
        top.setOpaque(false);

        JButton checkInBtn = actionButton("Check In");
        JButton checkOutBtn = actionButton("Check Out");

        top.add(checkInBtn);
        top.add(checkOutBtn);

        container.add(top, BorderLayout.NORTH);

        // ===== TABLE =====

        String[] columns = {
                "Date",
                "Check In",
                "Check Out"
        };

        attendanceModel = new DefaultTableModel(columns, 0);

        attendanceTable = new JTable(attendanceModel);

        attendanceTable.setRowHeight(28);
        attendanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);

        scrollPane.setBorder(new EmptyBorder(20, 0, 0, 0));

        container.add(scrollPane, BorderLayout.CENTER);

        page.add(container, BorderLayout.CENTER);

        // ================= CHECK IN =================

        checkInBtn.addActionListener(e -> {

            try {

                Connection conn = DatabaseConnection.getConnection();

                String query = "INSERT INTO attendance (username, date, check_in) VALUES (?, CURDATE(), CURTIME())";

                PreparedStatement ps = conn.prepareStatement(query);

                ps.setString(1, username);

                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Checked In Successfully!");

                loadTodayAttendance();
                loadAttendanceTable();
                loadDashboardAnalytics();
                loadPayrollTable();

                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ================= CHECK OUT =================

        checkOutBtn.addActionListener(e -> {

            try {

                Connection conn = DatabaseConnection.getConnection();

                //String query = "UPDATE attendance SET check_out = CURTIME() WHERE username=? AND date=CURDATE()";
                String query =
                        "UPDATE attendance " +
                                "SET check_out = CURTIME() " +
                                "WHERE id = (" +
                                "SELECT id FROM (" +
                                "SELECT id FROM attendance " +
                                "WHERE username=? " +
                                "AND date=CURDATE() " +
                                "AND check_out IS NULL " +
                                "ORDER BY check_in DESC " +
                                "LIMIT 1" +
                                ") temp)";
                PreparedStatement ps = conn.prepareStatement(query);

                ps.setString(1, username);

                int rows = ps.executeUpdate();

                if (rows > 0) {

                    JOptionPane.showMessageDialog(this, "Checked Out Successfully!");

                    loadTodayAttendance();
                    loadAttendanceTable();
                    loadDashboardAnalytics();
                    loadPayrollTable();

                } else {

                    JOptionPane.showMessageDialog(this, "No check-in found!");
                }

                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return page;
    }

    private void loadAttendanceTable() {

        try {

            Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT date, check_in, check_out FROM attendance WHERE username=? ORDER BY date DESC";

            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            // Clear old rows
            attendanceModel.setRowCount(0);

            while (rs.next()) {

                attendanceModel.addRow(new Object[] {
                        rs.getDate("date"),
                        rs.getString("check_in"),
                        rs.getString("check_out")
                });
            }

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadTodayAttendance() {

        try {

            Connection conn = DatabaseConnection.getConnection();

            // First check-in
            String firstInQuery =
                    "SELECT MIN(check_in) AS first_in " +
                            "FROM attendance " +
                            "WHERE username=? AND date=CURDATE()";

            PreparedStatement ps1 = conn.prepareStatement(firstInQuery);

            ps1.setString(1, username);

            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {

                String firstIn = rs1.getString("first_in");

                firstCheckInLabel.setText(
                        "First Check In: " +
                                (firstIn == null ? "--" : firstIn)
                );
            }

            // Last checkout
            String lastOutQuery =
                    "SELECT MAX(check_out) AS last_out " +
                            "FROM attendance " +
                            "WHERE username=? AND date=CURDATE()";

            PreparedStatement ps2 = conn.prepareStatement(lastOutQuery);

            ps2.setString(1, username);

            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {

                String lastOut = rs2.getString("last_out");

                lastCheckOutLabel.setText(
                        "Last Check Out: " +
                                (lastOut == null ? "--" : lastOut)
                );
            }

            // Sessions count
            String sessionQuery =
                    "SELECT COUNT(*) AS total_sessions " +
                            "FROM attendance " +
                            "WHERE username=? AND date=CURDATE()";

            PreparedStatement ps3 = conn.prepareStatement(sessionQuery);

            ps3.setString(1, username);

            ResultSet rs3 = ps3.executeQuery();

            if (rs3.next()) {

                int sessions = rs3.getInt("total_sessions");

                sessionLabel.setText("Sessions: " + sessions);
            }

            // Current status
            String statusQuery =
                    "SELECT * FROM attendance " +
                            "WHERE username=? " +
                            "AND date=CURDATE() " +
                            "AND check_out IS NULL";

            PreparedStatement ps4 = conn.prepareStatement(statusQuery);

            ps4.setString(1, username);

            ResultSet rs4 = ps4.executeQuery();

            if (rs4.next()) {

                statusLabel.setText("Status: Working");

            } else {

                statusLabel.setText("Status: Offline");
            }

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    // ================= PAYROLL PAGE =================

    private JPanel buildPayrollPage() {

        JPanel page = createPage("Payroll");

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        // ===== TABLE =====

        String[] columns = {
                "Date",
                "Check In",
                "Check Out",
                "Hours",
                "Rate/Hour",
                "Salary"
        };

        payrollModel = new DefaultTableModel(columns, 0);

        payrollTable = new JTable(payrollModel);

        payrollTable.setRowHeight(28);
        payrollTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(payrollTable);

        scrollPane.setBorder(new EmptyBorder(20, 0, 0, 0));

        container.add(scrollPane, BorderLayout.CENTER);

        page.add(container, BorderLayout.CENTER);

        return page;
    }

    private void loadPayrollTable() {

        try {

            Connection conn = DatabaseConnection.getConnection();

            String query =
                    "SELECT a.date, a.check_in, a.check_out, " +
                            "TIMESTAMPDIFF(MINUTE, a.check_in, a.check_out)/60.0 AS hours, " +
                            "s.salary_per_hour " +
                            "FROM attendance a " +
                            "JOIN users u ON a.username = u.username " +
                            "JOIN salary_structure s " +
                            "ON u.department = s.department " +
                            "AND u.role = s.role " +
                            "WHERE a.username=? " +
                            "AND a.check_out IS NOT NULL " +
                            "ORDER BY a.date DESC";

            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            // Clear old rows
            payrollModel.setRowCount(0);

            while (rs.next()) {

                double hours = rs.getDouble("hours");

                double rate = rs.getDouble("salary_per_hour");

                double salary = hours * rate;

                payrollModel.addRow(new Object[] {

                        rs.getDate("date"),
                        rs.getString("check_in"),
                        rs.getString("check_out"),

                        String.format("%.2f", hours),

                        "₹" + rate,

                        "₹" + String.format("%.2f", salary)
                });
            }

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= PROFILE PAGE =================

    private JPanel buildProfilePage() {

        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(30, 35, 30, 35));

        // ================= HEADER =================

        JLabel heading = new JLabel("Profile");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 32));
        heading.setForeground(TEXT);

        page.add(heading, BorderLayout.NORTH);

        // ================= MAIN CONTENT =================

        JPanel main = new JPanel(new BorderLayout(30, 0));
        main.setOpaque(false);
        JPanel leftCard = new JPanel();
        leftCard.setPreferredSize(new Dimension(320, 0));
        leftCard.setBackground(CARD_BG);
        leftCard.setBorder(new CompoundBorder(
                new RoundedBorder(18, BORDER, CARD_BG),
                new EmptyBorder(30, 30, 30, 30)
        ));

        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));

        // Avatar
        JPanel avatar = new JPanel();
        avatar.setPreferredSize(new Dimension(120, 120));
        avatar.setMaximumSize(new Dimension(120, 120));
        avatar.setBackground(SIDEBAR);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel initial = new JLabel(username.substring(0,1).toUpperCase());
        initial.setForeground(Color.WHITE);
        initial.setFont(new Font("Segoe UI", Font.BOLD, 48));

        avatar.add(initial);
        avatar.setBorder(new LineBorder(HOVER, 3, true));

        JLabel userLabel = new JLabel(username);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        userLabel.setForeground(TEXT);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Employee");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        roleLabel.setForeground(ACCENT);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton editBtn = createStyledButton("Edit Profile");
        editBtn.setMaximumSize(new Dimension(180, 42));
        editBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftCard.add(Box.createVerticalGlue());
        leftCard.add(avatar);
        leftCard.add(Box.createVerticalStrut(25));
        leftCard.add(userLabel);
        leftCard.add(Box.createVerticalStrut(8));
        leftCard.add(roleLabel);
        leftCard.add(Box.createVerticalStrut(30));
        leftCard.add(editBtn);
        leftCard.add(Box.createVerticalGlue());
        JPanel rightCard = new JPanel();
        rightCard.setBackground(CARD_BG);
        rightCard.setBorder(new CompoundBorder(
                new RoundedBorder(18, BORDER, CARD_BG),
                new EmptyBorder(30, 35, 30, 35)
        ));

        rightCard.setLayout(new GridLayout(6, 2, 25, 25));

        fullNameValue = profileValue("--");
        usernameValue = profileValue("--");
        departmentValue = profileValue("--");
        roleValue = profileValue("--");
        emailValue = profileValue("--");
        phoneValue = profileValue("--");

        rightCard.add(profileTitle("Full Name"));
        rightCard.add(fullNameValue);

        rightCard.add(profileTitle("Username"));
        rightCard.add(usernameValue);

        rightCard.add(profileTitle("Department"));
        rightCard.add(departmentValue);

        rightCard.add(profileTitle("Role"));
        rightCard.add(roleValue);

        rightCard.add(profileTitle("Email"));
        rightCard.add(emailValue);

        rightCard.add(profileTitle("Phone"));
        rightCard.add(phoneValue);

        main.add(leftCard, BorderLayout.WEST);
        main.add(rightCard, BorderLayout.CENTER);

        page.add(main, BorderLayout.CENTER);

        return page;
    }

    private JLabel profileTitle(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(ACCENT);

        return label;
    }

    private JLabel profileValue(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        label.setForeground(TEXT);

        return label;
    }

    private void loadProfileData() {

        try {

            Connection conn = DatabaseConnection.getConnection();

            String query =
                    "SELECT * FROM users WHERE username=?";

            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                fullNameValue.setText(rs.getString("full_name"));

                usernameValue.setText(rs.getString("username"));

                departmentValue.setText(rs.getString("department"));

                roleValue.setText(rs.getString("role"));

                emailValue.setText(rs.getString("email"));

                phoneValue.setText(rs.getString("phone"));
            }

            conn.close();

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    // ================= PAGE TEMPLATE =================

    private JPanel createPage(String title) {

        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 30));
        heading.setForeground(TEXT);

        page.add(heading, BorderLayout.NORTH);

        return page;
    }

    // ================= CARD =================

    private JPanel createCard(String title, String value) {

        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new RoundedBorder(18, BORDER, CARD_BG),
                new EmptyBorder(20, 20, 20, 20)
        ));

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setForeground(ACCENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(valueLabel);

        return card;
    }

    // ================= UTIL =================

    private JLabel infoLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(TEXT);

        return label;
    }

    private JButton actionButton(String text) {
        JButton btn = createStyledButton(text);
        btn.setPreferredSize(new Dimension(160, 45));
        return btn;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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