package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Migration Tool...");

        try (DatabaseConnector databaseConnector = new DatabaseConnector()) {
            MigrationExecutor migrationExecutor = new MigrationExecutor(databaseConnector);

            // Test database connection
            if (!databaseConnector.isValid()) {
                logger.error("Database connection is not valid. Exiting...");
                return;
            }

            // Get the list of available migration files
            List<MigrationFile> availableMigrations = MigrationLoader.loadMigrations();
            logger.info("Available Migrations: {}", availableMigrations);

            // Execute migrations
            migrationExecutor.executeMigrations(availableMigrations);

        } catch (SQLException e) {
            logger.error("Error initializing database connection: {}", e.getMessage(), e);
        }
    }
}
