package org.migrationtool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectorTest {

    private static final Logger logger = LogManager.getLogger(DatabaseConnectorTest.class);
    private DatabaseConnector databaseConnector;

    @BeforeEach
    void initializeDatabaseConnector() throws SQLException {
        // Initialize DatabaseConnector before each test
        databaseConnector = new DatabaseConnector();
    }

    @Test
    void testGetConnection() {
        assertNotNull(databaseConnector.getConnection(), "Connection should not be null");
        logger.info("Connection is successful.");
    }

    @Test
    void testIsValid() {
        assertTrue(databaseConnector.isValid(), "Connection should be valid");
        logger.info("Connection is valid.");
    }

    @Test
    void testCloseConnection() {
        try {
            // Close the connection and verify it's closed
            databaseConnector.closeConnection();
            assertTrue(databaseConnector.getConnection().isClosed(), "Connection should be closed");
            logger.info("Connection is closed.");
        } catch (SQLException e) {
            fail("Failed to close connection: " + e.getMessage());
            logger.error("Error occurred while closing connection: {}", e.getMessage(), e);
        }
    }
}
