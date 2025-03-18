package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class MigrationExecutor {

    private final DatabaseConnector databaseConnector;
    private final MigrationHistoryManager migrationHistoryManager;

    private static final Logger logger = LogManager.getLogger(MigrationExecutor.class);

    public MigrationExecutor(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
        this.migrationHistoryManager = new MigrationHistoryManager(databaseConnector.getConnection());
    }

    // Execute all pending migrations
    public void executeMigrations(List<MigrationFile> migrationFiles) {
        for (MigrationFile migration : migrationFiles) {
            if (!migrationHistoryManager.isMigrationApplied(migration.version())) {
                try {
                    // Execute the migration
                    executeSingleMigration(migration);
                    // Record the successful migration
                    recordMigration(migration, true);
                } catch (SQLException | IOException e) {
                    logger.error("Migration failed for {}: {}", migration.filePath(), e.getMessage(), e);
                    try {
                        // Mark migration as failed in history
                        recordMigration(migration, false);
                    } catch (SQLException ex) {
                        logger.error("Failed to record failed migration for {}: {}", migration.filePath(), ex.getMessage(), ex);
                    }
                }
            }
        }
    }

    private void recordMigration(MigrationFile migration, boolean success) throws SQLException {
        migrationHistoryManager.addMigration(
                migration.version(),
                migration.description(),
                "SQL",
                migration.filePath().getFileName().toString(),
                getChecksum(migration),
                success
        );
    }

    // Execute a single migration
    private void executeSingleMigration(MigrationFile migration) throws IOException, SQLException {
        String migrationScript = Files.readString(migration.filePath());
        try (var stmt = databaseConnector.getConnection().createStatement()) {
            stmt.execute(migrationScript);
            logger.info("Migration executed successfully: {}", migration.description());
        }
    }

    private String getChecksum(MigrationFile migration) {
        return Integer.toHexString(migration.filePath().toString().hashCode());
    }
}
