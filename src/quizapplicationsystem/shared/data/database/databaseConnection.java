/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quizapplicationsystem.shared.data.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single place that hands out JDBC connections to the MySQL database. The
 * admin, teacher, and student areas all go through here, so connection
 * settings live in exactly one spot.
 *
 * @author shiro
 */
public class databaseConnection {

    // --- Connection settings ---------------------------------------------
    // jdbc:mysql://<host>:<port>/<database>
    private static final String URL =
            "jdbc:mysql://localhost:3306/quiz_application_system";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Laragon's default root password is empty

    // Load the driver once when this class is first used. With a modern
    // Connector/J jar on the classpath this is optional (the driver
    // auto-registers), but it fails fast with a clear message if the jar
    // is missing.
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL JDBC driver not found. Add mysql-connector-j to the "
                    + "project libraries. Cause: " + e.getMessage());
        }
    }

    /** Private: this class is a factory, not something you instantiate. */
    private databaseConnection() {
    }

    /**
     * Opens a new connection to the database. The caller is responsible for
     * closing it — use try-with-resources.
     *
     * @return an open {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Quick manual check that the database is reachable. You can call this
     * from a temporary main method or a test.
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connected to the database successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
