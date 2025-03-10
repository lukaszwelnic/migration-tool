package org.migrationtool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectorTest {

    private DatabaseConnector databaseConnector;

    @BeforeEach
    void initializeDatabaseConnector() throws SQLException {
        // Initialize DatabaseConnector before each test
        databaseConnector = new DatabaseConnector();
    }

    @Test
    void testGetConnection() {
        assertNotNull(databaseConnector.getConnection(), "Connection should not be null");
        System.out.println("Connection is successful.");
    }

    @Test
    void testIsValid() {
        assertTrue(databaseConnector.isValid(), "Connection should be valid");
        System.out.println("Connection is valid.");
    }

    @Test
    void testCloseConnection() {
        try {
            // Close the connection and verify it's closed
            databaseConnector.closeConnection();
            assertTrue(databaseConnector.getConnection().isClosed(), "Connection should be closed");
            System.out.println("Connection is closed.");
        } catch (SQLException e) {
            fail("Failed to close connection: " + e.getMessage());
        }
    }
}
