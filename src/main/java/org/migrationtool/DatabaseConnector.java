package org.migrationtool;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DatabaseConnector implements AutoCloseable {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String URL = "jdbc:postgresql://localhost:5432/" + dotenv.get("DB_NAME");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    private static final Logger logger = LogManager.getLogger(DatabaseConnector.class);

    private Connection connection;

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            logger.info("Initializing database connection...");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Database connection established to URL: {}", URL);
            initMigrationHistoryTable(connection);  // Initialize the migration history table on first use
        } else {
            logger.debug("Reusing existing connection.");
        }
        return connection;
    }

    // Closes connection
    @Override
    public void close() {
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

    // Checks if the connection is valid
    public boolean isValid() {
        try {
            if (connection == null) {
                logger.warn("Connection is not established yet.");
                return false;
            }
            boolean valid = connection.isValid(2);
            logger.info("Connection is valid: {}", valid);
            return valid;
        } catch (SQLException e) {
            logger.error("Error validating connection: {}", e.getMessage(), e);
            return false;
        }
    }

    // Creates the migration_history table if it doesn't exist
    private void initMigrationHistoryTable(Connection connection) {
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

        try (Statement stmt = connection.createStatement()) { // Ensure connection is initialized
            stmt.execute(createTableSQL);
            logger.info("Migration history table is ready.");
        } catch (SQLException e) {
            logger.error("Error creating migration history table: {}", e.getMessage(), e);
        }
    }
}

