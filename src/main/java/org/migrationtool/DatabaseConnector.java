package org.migrationtool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:postgresql://localhost:5432/migration_db";
    private static final String USER = "migration_user";
    private static final String PASSWORD = "password";

    private final Connection connection;

    // Init database connection
    public DatabaseConnector() throws SQLException {
        this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Gets connection
    public Connection getConnection() {
        return connection;
    }

    // Closes connection
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    //Validates the connection
    public boolean isValid() {
        try {
            return connection != null && connection.isValid(2); //Timeout of 2s
        } catch (SQLException e) {
            return false;
        }
    }
}
