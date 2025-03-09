package org.migrationtool;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    void testDatabaseConnection(){
        try (Connection connection = DatabaseConnector.getConnection()) {
            assertNotNull(connection);
            System.out.println("Database connection successful!");
        } catch (Exception e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }
}
