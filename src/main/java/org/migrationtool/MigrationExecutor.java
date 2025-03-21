package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.List;

public class MigrationExecutor {

    private final DatabaseConnector databaseConnector;
    private final MigrationHistoryManager migrationHistoryManager;

    private static final Logger logger = LogManager.getLogger(MigrationExecutor.class);

    public MigrationExecutor(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
        this.migrationHistoryManager = new MigrationHistoryManager(databaseConnector);
    }

    public void executeMigrations(List<MigrationFile> migrationFiles) {
        try (Connection connection = databaseConnector.getConnection()) {
            connection.setAutoCommit(false); // Begin transaction

            for (MigrationFile migration : migrationFiles) {
                try {
                    if (migrationHistoryManager.isMigrationApplied(migration, connection)) {
                        logger.info("Skipping already applied migration: Version {}", migration.version());
                        continue;
                    }

                    // apply that function whether the migration exists in the table or not. Modify isMigrationApplied function?
                    //migrationHistoryManager.markMigration(migration, migrationHistoryManager.getChecksum(migration), false, connection);

                    executeSingleMigration(migration, connection);
                    migrationHistoryManager.addMigration(migration, true, connection);
                    connection.commit();

                } catch (RuntimeException e) { // Checksum failure
                    logger.error("Checksum validation failed for version {}. Aborting.", migration.version());
                    connection.rollback();
                    migrationHistoryManager.addMigration(migration, false, connection);
                    connection.commit();
                    return;
                } catch (SQLException | IOException e) { // Other failures
                    logger.error("Migration failed for version {}: {}", migration.version(), e.getMessage(), e);
                    connection.rollback();
                    migrationHistoryManager.addMigration(migration, false, connection);
                    connection.commit();
                    return;
                }
            }

            logger.info("All migrations executed successfully.");
        } catch (SQLException e) {
            logger.error("Transaction error: {}", e.getMessage(), e);
        }
    }


    private void executeSingleMigration(MigrationFile migration, Connection connection) throws IOException, SQLException {
        String migrationScript = Files.readString(migration.filePath());
        try (Statement stmt = connection.createStatement()) {
            logger.info("Executing migration: Version {} - {}", migration.version(), migration.description());
            stmt.execute(migrationScript);
            logger.info("Migration {} executed successfully.", migration.version());
        }
    }
}
