package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Migration Tool...");

        try (DatabaseConnector databaseConnector = new DatabaseConnector()) {
            MigrationExecutor executor = new MigrationExecutor(databaseConnector);
            MigrationHistoryManager history = new MigrationHistoryManager(databaseConnector);
            history.clearMigrationHistory();

            // Test database connection
            //isValid - maybe use it in executor?

            // Get the list of available migration files
            List<MigrationFile> migrations = MigrationLoader.loadMigrations();
            logger.info("Available Migrations: {}", migrations);

            // Execute migrations
            executor.executeMigrations(migrations);

            // get applied migrations

            // clear migration history

        }
    }
}
