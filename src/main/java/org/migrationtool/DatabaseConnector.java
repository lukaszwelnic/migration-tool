package org.migrationtool;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String URL = "jdbc:postgresql://localhost:5432/migration_db";
    private static final String USER = "migration_user";
    private static final String PASSWORD = "password";

    private static final Logger logger = LogManager.getLogger(DatabaseConnector.class);

    private final Connection connection;

    // Init database connection
    public DatabaseConnector() throws SQLException {
        this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        logger.info("Database connection established to URL: {}", URL);
    }

    // Gets connection
    public Connection getConnection() {
        if (connection != null) {
            logger.debug("Returning connection object.");
        }
        return connection;
    }

    // Closes connection
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.info("Connection closed successfully.");
                }
            } catch (SQLException e) {
                logger.error("Error closing connection: {}", e.getMessage(), e);
            }
        }
    }

    // Validates the connection
    public boolean isValid() {
        try {
            boolean valid = connection != null && connection.isValid(2); // Timeout of 2s
            if (valid) {
                logger.info("Connection is valid.");
            } else {
                logger.warn("Connection is not valid.");
            }
            return valid;
        } catch (SQLException e) {
            logger.error("Error validating connection: {}", e.getMessage(), e);
            return false;
        }
    }
}
