package main;
import db.DatabaseConnection;
import ui.LoginUI;

import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        DatabaseConnection.getConnection();
        SwingUtilities.invokeLater(() -> new LoginUI());
    }
}