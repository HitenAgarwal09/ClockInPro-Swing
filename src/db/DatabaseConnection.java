package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/clockinpro";
    private static final String USER = "root";
    private static final String PASSWORD = "Hiten@09"; // 🔴 replace with your MySQL password

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database!");
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}