package ui;

import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.PrintWriter;

public class AdminDashboardUI extends JFrame {

    private String username;

    // COLORS (Modern Palette)
    private static final Color BG = new Color(235, 233, 229);
    private static final Color SIDEBAR = new Color(220, 217, 212);
    private static final Color HOVER = new Color(210, 207, 202);
    private static final Color TEXT = new Color(30, 28, 28);
    private static final Color BORDER = new Color(210, 207, 202);
    private static final Color ACCENT = new Color(110, 60, 65);
    private static final Color ACCENT_HOVER = new Color(90, 45, 50);
    private static final Color CARD_BG = new Color(245, 244, 241);

    // CARD LAYOUT
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel totalEmployeesLabel;
    private JLabel presentLabel;
    private JLabel absentLabel;
    private JLabel payrollLabel;
    private JTable attendanceTable;
    private DefaultTableModel attendanceModel;
    private JTable payrollTable;
    private DefaultTableModel payrollModel;
    private JTable employeeTable;
    private DefaultTableModel employeeModel;

    private JTextField searchField;

    public AdminDashboardUI(String username) {

        this.username = username;

        setTitle("ClockInPro Admin Dashboard");
        setSize(1250, 720);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        // CONTENT PANEL
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // ADD PAGES
        contentPanel.add(buildDashboardPage(), "dashboard");
        contentPanel.add(buildAttendancePage(), "attendance");
        contentPanel.add(buildPayrollPage(), "payroll");
        contentPanel.add(buildEmployeesPage(), "employees");
        contentPanel.add(buildReportsPage(), "reports");

        add(contentPanel, BorderLayout.CENTER);
        loadAdminStats();
        loadAttendanceTable();
        loadPayrollTable();
        loadEmployees();
        setVisible(true);
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private JPanel buildSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("ClockInPro");
        logo.setForeground(TEXT);
        logo.setFont(new Font("Georgia", Font.BOLD, 32));
        logo.setBorder(new EmptyBorder(45, 40, 50, 10));

        sidebar.add(logo);

        sidebar.add(navItem("Dashboard", "dashboard"));
        sidebar.add(navItem("Attendance", "attendance"));
        sidebar.add(navItem("Payroll", "payroll"));
        sidebar.add(navItem("Employees", "employees"));
        sidebar.add(navItem("Reports", "reports"));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = createStyledButton("Logout");

        logoutBtn.setMaximumSize(new Dimension(220, 50));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginUI();
        });

        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(40));

        return sidebar;
    }

    // =========================================================
    // NAVIGATION ITEM
    // =========================================================

    private JPanel navItem(String text, String cardName) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(SIDEBAR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        panel.setBorder(new EmptyBorder(8, 40, 8, 20));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);

        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        panel.add(label, BorderLayout.CENTER);

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

    // =========================================================
    // DASHBOARD PAGE
    // =========================================================

    private JPanel buildDashboardPage() {

        JPanel page = createPage("Admin Dashboard");

        JPanel cards = new JPanel(new GridLayout(2, 2, 20, 20));

        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(20, 0, 0, 0));

        totalEmployeesLabel = new JLabel("0");
        presentLabel = new JLabel("0");
        absentLabel = new JLabel("0");
        payrollLabel = new JLabel("₹0");

        cards.add(createDynamicCard("Total Employees", totalEmployeesLabel));
        cards.add(createDynamicCard("Present Today", presentLabel));
        cards.add(createDynamicCard("Absent Today", absentLabel));
        cards.add(createDynamicCard("Total Payroll", payrollLabel));

        page.add(cards, BorderLayout.CENTER);

        return page;
    }

    // =========================================================
    // ATTENDANCE PAGE
    // =========================================================

    private JPanel buildAttendancePage() {

        JPanel page = createPage("Attendance Monitoring");

        JPanel container = new JPanel(new BorderLayout());

        container.setOpaque(false);

        // =====================================================
        // TABLE
        // =====================================================

        String[] columns = {
                "Employee",
                "Date",
                "Check In",
                "Check Out",
                "Hours"
        };

        attendanceModel =
                new DefaultTableModel(columns, 0);

        attendanceTable =
                new JTable(attendanceModel);

        attendanceTable.setRowHeight(30);

        attendanceTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        JScrollPane scrollPane =
                new JScrollPane(attendanceTable);

        scrollPane.setBorder(
                new EmptyBorder(20, 0, 0, 0)
        );

        container.add(scrollPane, BorderLayout.CENTER);

        page.add(container, BorderLayout.CENTER);

        return page;
    }

    private void loadAttendanceTable() {

        try {

            Connection conn =
                    DatabaseConnection.getConnection();

            String query =

                    "SELECT username, date, check_in, check_out, " +
                            "TIMESTAMPDIFF(MINUTE, check_in, check_out)/60.0 AS hours " +
                            "FROM attendance " +
                            "ORDER BY date DESC, check_in DESC";

            PreparedStatement ps =
                    conn.prepareStatement(query);

            ResultSet rs = ps.executeQuery();

            // CLEAR OLD DATA
            attendanceModel.setRowCount(0);

            while (rs.next()) {

                String hours = "--";

                if (rs.getString("check_out") != null) {

                    hours = String.format(
                            "%.2f",
                            rs.getDouble("hours")
                    );
                }

                attendanceModel.addRow(new Object[] {

                        rs.getString("username"),
                        rs.getDate("date"),
                        rs.getString("check_in"),
                        rs.getString("check_out"),
                        hours
                });
            }

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // =========================================================
    // PAYROLL PAGE
    // =========================================================

    private JPanel buildPayrollPage() {

        JPanel page = createPage("Payroll Management");

        JPanel container = new JPanel(new BorderLayout());

        container.setOpaque(false);

        // =====================================================
        // TABLE
        // =====================================================

        String[] columns = {
                "Employee",
                "Department",
                "Role",
                "Total Hours",
                "Total Salary"
        };

        payrollModel =
                new DefaultTableModel(columns, 0);

        payrollTable =
                new JTable(payrollModel);

        payrollTable.setRowHeight(30);

        payrollTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        JScrollPane scrollPane =
                new JScrollPane(payrollTable);

        scrollPane.setBorder(
                new EmptyBorder(20, 0, 0, 0)
        );

        container.add(scrollPane, BorderLayout.CENTER);

        page.add(container, BorderLayout.CENTER);

        return page;
    }

    private void loadPayrollTable() {

        try {

            Connection conn =
                    DatabaseConnection.getConnection();

            String query =

                    "SELECT " +

                            "u.username, " +
                            "u.department, " +
                            "u.role, " +

                            "SUM(" +
                            "TIMESTAMPDIFF(MINUTE, a.check_in, a.check_out)/60.0" +
                            ") AS total_hours, " +

                            "SUM(" +
                            "(TIMESTAMPDIFF(MINUTE, a.check_in, a.check_out)/60.0)" +
                            "* s.salary_per_hour" +
                            ") AS total_salary " +

                            "FROM attendance a " +

                            "JOIN users u " +
                            "ON a.username = u.username " +

                            "JOIN salary_structure s " +
                            "ON u.department = s.department " +
                            "AND u.role = s.role " +

                            "WHERE a.check_out IS NOT NULL " +

                            "GROUP BY u.username, u.department, u.role";

            PreparedStatement ps =
                    conn.prepareStatement(query);

            ResultSet rs = ps.executeQuery();

            // CLEAR OLD DATA
            payrollModel.setRowCount(0);

            while (rs.next()) {
                System.out.println("Payroll rows loading...");
                payrollModel.addRow(new Object[] {

                        rs.getString("username"),

                        rs.getString("department"),

                        rs.getString("role"),

                        String.format(
                                "%.2f",
                                rs.getDouble("total_hours")
                        ),

                        "₹" + String.format(
                                "%.2f",
                                rs.getDouble("total_salary")
                        )
                });
            }

            conn.close();

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    // =========================================================
    // EMPLOYEES PAGE
    // =========================================================

    private JPanel buildEmployeesPage() {

        JPanel page = createPage("Employee Management");

        JPanel container = new JPanel(new BorderLayout(15, 15));

        container.setOpaque(false);

        // =====================================================
        // TOP PANEL
        // =====================================================

        JPanel topPanel = new JPanel(new BorderLayout(15, 0));

        topPanel.setOpaque(false);

        // SEARCH FIELD
        searchField = new JTextField();

        searchField.setPreferredSize(
                new Dimension(250, 40)
        );

        searchField.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        searchField.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));

        topPanel.add(searchField, BorderLayout.WEST);

        // =====================================================
        // BUTTON PANEL
        // =====================================================

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        buttonPanel.setOpaque(false);

        JButton updateBtn = actionButton("Update");
        updateBtn.addActionListener(e -> {

            int selectedRow =
                    employeeTable.getSelectedRow();

            if (selectedRow == -1) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select an employee"
                );

                return;
            }

            int id = (int)
                    employeeModel.getValueAt(selectedRow, 0);

            String fullName = (String)
                    employeeModel.getValueAt(selectedRow, 1);

            String username = (String)
                    employeeModel.getValueAt(selectedRow, 2);

            String department = (String)
                    employeeModel.getValueAt(selectedRow, 3);

            String role = (String)
                    employeeModel.getValueAt(selectedRow, 4);

            String email = (String)
                    employeeModel.getValueAt(selectedRow, 5);

            String phone = (String)
                    employeeModel.getValueAt(selectedRow, 6);

            // =====================================================
            // INPUT FIELDS
            // =====================================================

            JTextField nameField =
                    new JTextField(fullName);

            JTextField userField =
                    new JTextField(username);

            JTextField emailField =
                    new JTextField(email);

            JTextField phoneField =
                    new JTextField(phone);

            JComboBox<String> deptBox =
                    new JComboBox<>(new String[] {
                            "HR",
                            "Sales",
                            "Engineering",
                            "Finance",
                            "Operations"
                    });

            deptBox.setSelectedItem(department);

            JComboBox<String> roleBox =
                    new JComboBox<>(new String[] {
                            "Employee",
                            "Manager",
                            "Admin",
                            "HR Executive"
                    });

            roleBox.setSelectedItem(role);

            JPanel panel = new JPanel(
                    new GridLayout(0, 1, 10, 10)
            );

            panel.add(new JLabel("Full Name"));
            panel.add(nameField);

            panel.add(new JLabel("Username"));
            panel.add(userField);

            panel.add(new JLabel("Department"));
            panel.add(deptBox);

            panel.add(new JLabel("Role"));
            panel.add(roleBox);

            panel.add(new JLabel("Email"));
            panel.add(emailField);

            panel.add(new JLabel("Phone"));
            panel.add(phoneField);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Update Employee",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {

                updateEmployee(
                        id,
                        nameField.getText(),
                        userField.getText(),
                        deptBox.getSelectedItem().toString(),
                        roleBox.getSelectedItem().toString(),
                        emailField.getText(),
                        phoneField.getText()
                );
            }
        });
        JButton deleteBtn = actionButton("Delete");
        JButton searchBtn = actionButton("Search");

        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        deleteBtn.addActionListener(e -> {

            int selectedRow =
                    employeeTable.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select an employee"
                );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete selected employee?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                int employeeId = (int)
                        employeeModel.getValueAt(selectedRow, 0);

                deleteEmployee(employeeId);
            }
        });
        buttonPanel.add(searchBtn);
        searchBtn.addActionListener(e -> {

            String keyword =
                    searchField.getText().trim();

            searchEmployees(keyword);
        });


        topPanel.add(buttonPanel, BorderLayout.EAST);

        container.add(topPanel, BorderLayout.NORTH);

        // =====================================================
        // TABLE
        // =====================================================

        String[] columns = {
                "ID",
                "Full Name",
                "Username",
                "Department",
                "Role",
                "Email",
                "Phone"
        };

        employeeModel =
                new DefaultTableModel(columns, 0);

        employeeTable =
                new JTable(employeeModel);

        employeeTable.setRowHeight(30);

        employeeTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        JScrollPane scrollPane =
                new JScrollPane(employeeTable);

        container.add(scrollPane, BorderLayout.CENTER);
        page.add(container, BorderLayout.CENTER);
        return page;
    }

    private void deleteEmployee(int id) {

        try {
            Connection conn =
                    DatabaseConnection.getConnection();

            String query =
                    "DELETE FROM users WHERE id=?";

            PreparedStatement ps =
                    conn.prepareStatement(query);

            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Employee deleted successfully"
                );
                loadEmployees();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Delete failed"
                );
            }

            conn.close();

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private void searchEmployees(String keyword) {

        try {

            Connection conn =
                    DatabaseConnection.getConnection();

            String query =

                    "SELECT * FROM users " +

                            "WHERE " +

                            "full_name LIKE ? " +
                            "OR username LIKE ? " +
                            "OR department LIKE ? " +
                            "OR role LIKE ?";

            PreparedStatement ps =
                    conn.prepareStatement(query);

            String searchText = "%" + keyword + "%";

            ps.setString(1, searchText);
            ps.setString(2, searchText);
            ps.setString(3, searchText);
            ps.setString(4, searchText);

            ResultSet rs = ps.executeQuery();

            // CLEAR OLD DATA
            employeeModel.setRowCount(0);

            while (rs.next()) {

                employeeModel.addRow(new Object[] {

                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("department"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("phone")
                });
            }
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateEmployee(

            int id,
            String fullName,
            String username,
            String department,
            String role,
            String email,
            String phone
    ) {

        try {

            Connection conn =
                    DatabaseConnection.getConnection();

            String query =

                    "UPDATE users SET " +

                            "full_name=?, " +
                            "username=?, " +
                            "department=?, " +
                            "role=?, " +
                            "email=?, " +
                            "phone=? " +

                            "WHERE id=?";

            PreparedStatement ps =
                    conn.prepareStatement(query);

            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, department);
            ps.setString(4, role);
            ps.setString(5, email);
            ps.setString(6, phone);

            ps.setInt(7, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Employee updated successfully"
                );

                loadEmployees();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Update failed"
                );
            }

            conn.close();

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private JButton actionButton(String text) {
        JButton btn = createStyledButton(text);
        btn.setPreferredSize(new Dimension(160, 45));
        return btn;
    }

    // =========================================================
    // PAGE TEMPLATE
    // =========================================================

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

    // =========================================================
    // CARD
    // =========================================================

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

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(TEXT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(valueLabel);

        return card;
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

    private void loadAdminStats() {

        try {
            Connection conn = DatabaseConnection.getConnection();

            // =====================================================
            // TOTAL EMPLOYEES
            // =====================================================

            String totalQuery =
                    "SELECT COUNT(*) AS total FROM users";

            PreparedStatement ps1 =
                    conn.prepareStatement(totalQuery);

            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {
                totalEmployeesLabel.setText(
                        rs1.getString("total")
                );
            }

            // =====================================================
            // PRESENT TODAY
            // =====================================================

            String presentQuery =
                    "SELECT COUNT(DISTINCT username) AS present " +
                            "FROM attendance " +
                            "WHERE date = CURDATE()";

            PreparedStatement ps2 =
                    conn.prepareStatement(presentQuery);

            ResultSet rs2 = ps2.executeQuery();
            int present = 0;

            if (rs2.next()) {
                present = rs2.getInt("present");
                presentLabel.setText(
                        String.valueOf(present)
                );
            }

            // =====================================================
            // ABSENT TODAY
            // =====================================================

            int totalEmployees =
                    Integer.parseInt(totalEmployeesLabel.getText());

            int absent = totalEmployees - present;

            absentLabel.setText(String.valueOf(absent));

            // =====================================================
            // TOTAL PAYROLL
            // =====================================================

            String payrollQuery =

                    "SELECT SUM(" +

                            "(TIMESTAMPDIFF(MINUTE, a.check_in, a.check_out)/60.0) " +
                            "* s.salary_per_hour" +
                            ") AS total_salary " +
                            "FROM attendance a " +
                            "JOIN users u " +
                            "ON a.username = u.username " +
                            "JOIN salary_structure s " +
                            "ON u.department = s.department " +
                            "AND u.role = s.role " +
                            "WHERE a.check_out IS NOT NULL " +
                            "AND MONTH(a.date)=MONTH(CURDATE())";

            PreparedStatement ps3 =
                    conn.prepareStatement(payrollQuery);

            ResultSet rs3 = ps3.executeQuery();

            if (rs3.next()) {

                double salary =
                        rs3.getDouble("total_salary");

                payrollLabel.setText(
                        "₹" + String.format("%.2f", salary)
                );
            }

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadEmployees() {

        try {
            Connection conn =
                    DatabaseConnection.getConnection();
            String query =
                    "SELECT * FROM users";

            PreparedStatement ps =
                    conn.prepareStatement(query);

            ResultSet rs = ps.executeQuery();

            // CLEAR OLD DATA
            employeeModel.setRowCount(0);

            while (rs.next()) {

                employeeModel.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("department"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("phone")
                });
            }

            conn.close();

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private JPanel buildReportsPage() {

        JPanel page = createPage("Export Reports");

        JPanel center = new JPanel();

        center.setOpaque(false);

        JButton attendanceBtn =
                actionButton("Export Attendance");

        JButton payrollBtn =
                actionButton("Export Payroll");

        center.add(attendanceBtn);

        center.add(Box.createHorizontalStrut(20));

        center.add(payrollBtn);

        page.add(center, BorderLayout.CENTER);

        // =====================================================
        // EXPORT ATTENDANCE
        // =====================================================

        attendanceBtn.addActionListener(e -> {

            exportAttendanceCSV();
        });

        // =====================================================
        // EXPORT PAYROLL
        // =====================================================

        payrollBtn.addActionListener(e -> {

            exportPayrollCSV();
        });

        return page;
    }

    private void exportAttendanceCSV() {

        try {

            JFileChooser chooser =
                    new JFileChooser();

            chooser.setDialogTitle(
                    "Save Attendance Report"
            );

            int result = chooser.showSaveDialog(this);

            if (result != JFileChooser.APPROVE_OPTION)
                return;

            File file = chooser.getSelectedFile();

            PrintWriter pw =
                    new PrintWriter(file);

            // HEADER
            pw.println(
                    "Employee,Date,Check In,Check Out,Hours"
            );

            for (int i = 0;
                 i < attendanceModel.getRowCount();
                 i++) {

                for (int j = 0;
                     j < attendanceModel.getColumnCount();
                     j++) {

                    pw.print(
                            attendanceModel.getValueAt(i, j)
                    );

                    if (j != attendanceModel.getColumnCount() - 1) {

                        pw.print(",");
                    }
                }

                pw.println();
            }

            pw.close();

            JOptionPane.showMessageDialog(
                    this,
                    "Attendance report exported successfully"
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private void exportPayrollCSV() {

        try {

            JFileChooser chooser =
                    new JFileChooser();

            chooser.setDialogTitle(
                    "Save Payroll Report"
            );

            int result = chooser.showSaveDialog(this);

            if (result != JFileChooser.APPROVE_OPTION)
                return;

            File file = chooser.getSelectedFile();

            PrintWriter pw =
                    new PrintWriter(file);

            // HEADER
            pw.println(
                    "Employee,Department,Role,Hours,Salary"
            );

            for (int i = 0;
                 i < payrollModel.getRowCount();
                 i++) {

                for (int j = 0;
                     j < payrollModel.getColumnCount();
                     j++) {

                    pw.print(
                            payrollModel.getValueAt(i, j)
                    );

                    if (j != payrollModel.getColumnCount() - 1) {

                        pw.print(",");
                    }
                }

                pw.println();
            }

            pw.close();

            JOptionPane.showMessageDialog(
                    this,
                    "Payroll report exported successfully"
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
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

        RoundedBorder(int r, Color border, Color bg) {
            radius = r;
            borderColor = border;
            bgColor = bg;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 10, 4, 10);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}