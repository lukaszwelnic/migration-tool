package org.migrationtool;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.migrationtool.MigrationLoader.loadMigrations;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        DatabaseConnector databaseConnector = null;

        try {
            // Initialize DatabaseConnector
            databaseConnector = new DatabaseConnector();

            // Get database connection
            Connection connection = databaseConnector.getConnection();

            // Check if the connection is valid
            if (connection != null && databaseConnector.isValid()) {
                logger.info("Database connection is successful!");
            } else {
                logger.warn("Database connection failed or is not valid.");
            }

            logger.info("Starting migrations...");

            // Perform migration steps here
            // migrationService.executeMigrations();

            logger.info("Migrations completed.");

        } catch (SQLException e) {
            logger.error("Error connecting to the database: {}", e.getMessage(), e);
        } finally {
            if (databaseConnector != null) {
                databaseConnector.closeConnection();
                logger.info("Database connection has been closed.");
            }
        }

        List<MigrationFile> migrations = loadMigrations();
        migrations.forEach(System.out::println);
    }
}
