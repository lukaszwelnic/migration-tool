package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.List;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        DatabaseConnector databaseConnector = new DatabaseConnector();
        MigrationExecutor executor = new MigrationExecutor(databaseConnector);
        MigrationHistoryManager history = new MigrationHistoryManager();

        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();

        if ("help".equals(command)) {
            printHelp();
            return;
        }

        logger.info("Starting Migration Tool...");

        // Initialize the components needed for migration
        try (Connection connection = databaseConnector.getConnection()) {
            if (!databaseConnector.isValid()) {
                logger.error("Database connection is invalid. Exiting...");
                return;
            }

            List<MigrationFile> migrations = MigrationLoader.loadMigrations();

            switch (command) {
                case "migrate":
                    logger.info("Starting migration process...");
                    executor.executeMigrations(migrations);
                    break;

                case "status":
                    logger.info("Fetching migration status...");
                    history.getMigrationStatus(connection);
                    break;

                case "reset":
                    logger.info("Resetting migration history...");
                    history.clearMigrationHistory(connection);
                    break;

                default:
                    logger.error("Unknown command: {}", command);
                    printHelp();
                    break;
            }
        } catch (Exception e) {
            logger.error("An error occurred during migration: {}", e.getMessage(), e);
        }
    }

    private static void printHelp() {
        logger.info("Available commands:");
        logger.info(" migrate  - Applies pending migrations to the database.");
        logger.info(" status   - Shows the applied and failed migrations.");
        logger.info(" reset    - Clears the migration history.");
        logger.info(" help     - Displays this help message.");
        logger.info("Usage: ./gradlew run --args=<command>");
    }
}
