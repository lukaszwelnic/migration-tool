package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public boolean isMigrationApplied(String version, Connection connection) {
        String sql = "SELECT COUNT(*) FROM migration_history WHERE version = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, version);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking migration history for version {}: {}", version, e.getMessage(), e);
            throw new RuntimeException("Database error while checking migration history", e);  // More explicit failure
        }
        return false;
    }

    public List<String> getAppliedMigrations() {
        List<String> migrations = new ArrayList<>();
        String sql = "SELECT version FROM migration_history ORDER BY version";

        try (Connection connection = databaseConnector.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {

            while (resultSet.next()) {
                migrations.add(resultSet.getString("version"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving applied migrations: {}", e.getMessage(), e);
        }
        return migrations;
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
        return Integer.toHexString(migration.filePath().toString().hashCode());
    }
}
