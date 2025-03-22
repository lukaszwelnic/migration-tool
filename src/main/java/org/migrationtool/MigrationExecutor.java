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
        this.migrationHistoryManager = new MigrationHistoryManager();
    }

    public void executeMigrations(List<MigrationFile> migrationFiles) {
        try (Connection connection = databaseConnector.getConnection()) {
            connection.setAutoCommit(false); // Begin transaction

            for (MigrationFile migration : migrationFiles) {
                try {
                    MigrationStatus status = migrationHistoryManager.getMigrationStatus(migration, connection);
                    String checksum = migrationHistoryManager.getChecksum(migration);

                    switch (status) {
                        case SUCCESSFUL_UNCHANGED -> {
                            logger.info("Skipping migration {} due to status: {}", migration.version(), status);
                            continue;
                        }
                        case SUCCESSFUL_CHANGED -> {
                            logger.info("Reapplying migration {} due to script changes.", migration.version());
                            migrationHistoryManager.updateMigration(migration, checksum, false, connection);
                            connection.commit();
                        }
                        case FAILED_UNCHANGED -> {
                            logger.error("Migration {} previously failed and is unchanged. Aborting", migration.version());
                            return;
                        }
                        case FAILED_CHANGED -> {
                            logger.info("Migration {} previously failed but changed. Executing.", migration.version());
                            migrationHistoryManager.updateMigration(migration, checksum, false, connection);
                            connection.commit();
                        }
                        case NOT_APPLIED -> {
                            logger.info("Migration {} was not applied previously. Executing.", migration.version());
                            migrationHistoryManager.addMigration(migration, false, connection);
                            connection.commit();
                        }
                    }

                    executeSingleMigration(migration, connection);

                    // Mark migration as successful
                    migrationHistoryManager.updateMigration(migration, checksum, true, connection);
                    connection.commit();

                } catch (RuntimeException | SQLException | IOException e) { // Other failures
                    logger.error("Migration failed for version {}. Aborting.", migration.version(), e);
                    connection.rollback();
                    migrationHistoryManager.updateMigration(migration, migrationHistoryManager.getChecksum(migration), false, connection);
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
