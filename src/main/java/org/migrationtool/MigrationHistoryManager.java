package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.nio.file.Files;
import java.sql.*;
import java.util.HexFormat;

public class MigrationHistoryManager {

    private final DatabaseConnector databaseConnector;
    private static final Logger logger = LogManager.getLogger(MigrationHistoryManager.class);

    public MigrationHistoryManager(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    public void addMigration(MigrationFile migration, boolean success, Connection connection) {
        logger.info("Applying migration: Version {}, Description {}", migration.version(), migration.description());
        String sql = "INSERT INTO migration_history (version, description, file_type, script_name, checksum, success) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, migration.version());
            stmt.setString(2, migration.description());
            stmt.setString(3, "SQL");
            stmt.setString(4, migration.filePath().getFileName().toString());
            stmt.setString(5, getChecksum(migration));
            stmt.setBoolean(6, success);
            stmt.executeUpdate();

            logger.info("Migration history updated: Version {} - Success: {}", migration.version(), success);
        } catch (SQLException e) {
            logger.error("Failed to record migration {}: {}", migration.version(), e.getMessage(), e);
        }
    }

    public boolean isMigrationApplied(MigrationFile migration, Connection connection) throws SQLException {
        String sql = "SELECT checksum FROM migration_history WHERE version = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, migration.version());
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    String storedChecksum = resultSet.getString("checksum");
                    String currentChecksum = getChecksum(migration);

                    if (!storedChecksum.equals(currentChecksum)) {
                        logger.warn("Checksum mismatch for version {}! Possible file modification detected.", migration.version());

                        markMigrationAsFailed(migration, connection);

                        throw new RuntimeException("Migration file modified after application! Failing execution.");
                    }
                    logger.info("Migration exists AND checksum matches");
                    return true;
                }
            }
        }
        logger.info("Migration not applied");
        return false;
    }

    public void logAppliedMigrations() {
        String sql = "SELECT version FROM migration_history WHERE success = true ORDER BY CAST(version AS INTEGER)";

        try (Connection connection = databaseConnector.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {

            // Log each applied migration version
            while (resultSet.next()) {
                String version = resultSet.getString("version");
                logger.info("Applied Migration: {}", version);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving applied migrations: {}", e.getMessage(), e);
        }
    }

    public void clearMigrationHistory() {
        String sql = "DELETE FROM migration_history";

        try (Connection connection = databaseConnector.getConnection();
             Statement stmt = connection.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);
            logger.info("Cleared migration history. {} rows deleted.", rowsAffected);
        } catch (SQLException e) {
            logger.error("Error clearing migration history: {}", e.getMessage(), e);
        }
    }

    private String getChecksum(MigrationFile migration) {
        try {
            byte[] fileBytes = Files.readAllBytes(migration.filePath());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute checksum for " + migration.filePath(), e);
        }
    }

    private void markMigrationAsFailed(MigrationFile migration, Connection connection) {
        String sql = "UPDATE migration_history SET success = false WHERE version = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, migration.version());
            stmt.executeUpdate();
            logger.info("Marked migration {} as failed due to checksum mismatch.", migration.version());
        } catch (SQLException e) {
            logger.error("Failed to update migration status for version {}: {}", migration.version(), e.getMessage(), e);
        }
    }

}
