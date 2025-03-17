package org.migrationtool;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String URL = "jdbc:postgresql://localhost:5432/" + dotenv.get("DB_NAME");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    private static final Logger logger = LogManager.getLogger(DatabaseConnector.class);

    private final Connection connection;

    // Init database connection
    public DatabaseConnector() throws SQLException {
        this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        logger.info("Database connection established to URL: {}", URL);
        initMigrationHistoryTable();  // Initialize the migration history table
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

    // Creates the migration_history table if it doesn't exist
    private void initMigrationHistoryTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS migration_history ("
                + "id SERIAL PRIMARY KEY, "
                + "version VARCHAR(50) UNIQUE NOT NULL, "
                + "description VARCHAR(255) NOT NULL, "
                + "file_type VARCHAR(255) NOT NULL, "
                + "script_name VARCHAR(255) UNIQUE NOT NULL, "
                + "checksum VARCHAR(64) NOT NULL, "
                + "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "success BOOLEAN NOT NULL DEFAULT TRUE"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Migration history table is ready.");
        } catch (SQLException e) {
            logger.error("Error creating migration history table: {}", e.getMessage(), e);
        }
    }
}
