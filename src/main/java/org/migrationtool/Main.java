package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.List;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Migration Tool...");

        DatabaseConnector databaseConnector = new DatabaseConnector();
        MigrationExecutor executor = new MigrationExecutor(databaseConnector);
        MigrationHistoryManager history = new MigrationHistoryManager();

        if (args.length == 0) {
            logger.info("Usage: ./gradlew run --args=\"migrate\"|\"status\"|\"reset\"");
            return;
        }

        // Initialize the components needed for migration
        try (Connection connection = databaseConnector.getConnection()) {
            if (!databaseConnector.isValid()) {
                logger.error("Database connection is invalid. Exiting...");
                return;
            }

            List<MigrationFile> migrations = MigrationLoader.loadMigrations();

            switch (args[0].toLowerCase()) {
                case "migrate":
                    logger.info("Starting migration process...");
                    executor.executeMigrations(migrations);
                    break;

                case "status":
                    logger.info("Fetching migration status...");
                    history.getAppliedMigrations(connection);
                    break;

                case "reset":
                    logger.info("Resetting migration history...");
                    history.clearMigrationHistory(connection);
                    break;

                default:
                    logger.error("Unknown command: {}", args[0]);
                    logger.info("Usage: ./gradlew run --args=\"migrate\"|\"status\"|\"reset\"");
                    break;
            }
        } catch (Exception e) {
            logger.error("An error occurred during migration: {}", e.getMessage());
        }
    }
}
