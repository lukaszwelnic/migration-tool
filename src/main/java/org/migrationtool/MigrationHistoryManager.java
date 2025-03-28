package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.nio.file.Files;
import java.sql.*;
import java.util.HexFormat;

public class MigrationHistoryManager {

    private static final Logger logger = LogManager.getLogger(MigrationHistoryManager.class);

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

    public void updateMigration(MigrationFile migration, String checksum, boolean success, Connection connection) {
        String sql = "UPDATE migration_history SET checksum = ?, success = ? WHERE version = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, checksum);
            stmt.setBoolean(2, success);
            stmt.setString(3, migration.version());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update migration status for version {}: {}", migration.version(), e.getMessage(), e);
        }
    }

    public MigrationStatus getMigrationStatus(MigrationFile migration, Connection connection) throws SQLException {
        String sql = "SELECT success, checksum FROM migration_history WHERE version = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, migration.version());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean success = rs.getBoolean("success");
                    String recordedChecksum = rs.getString("checksum");
                    String newChecksum = getChecksum(migration);

                    if (success) {
                        if (newChecksum.equals(recordedChecksum)) {
                            return MigrationStatus.SUCCESSFUL_UNCHANGED; // Migration was successful and hasn't changed
                        } else {
                            return MigrationStatus.SUCCESSFUL_CHANGED; // Migration was successful but has changed
                        }
                    } else {
                        if (newChecksum.equals(recordedChecksum)) {
                            return MigrationStatus.FAILED_UNCHANGED; // Previously failed and unchanged
                        } else {
                            return MigrationStatus.FAILED_CHANGED; // Previously failed but checksum changed
                        }
                    }
                }
            }
        }
        return MigrationStatus.NOT_APPLIED; // No record in the history
    }

    public void getMigrationStatus(Connection connection) {
        String sql = "SELECT version, success FROM migration_history ORDER BY CAST(version AS INTEGER)";

        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {

            boolean hasMigrations = false;

            while (resultSet.next()) {
                hasMigrations = true;
                String version = resultSet.getString("version");
                boolean success = resultSet.getBoolean("success");

                if (success) {
                    logger.info("✅ Applied Migration: {}", version);
                } else {
                    logger.warn("❌ Failed Migration: {}", version);
                }
            }

            if (!hasMigrations) {
                logger.info("No migrations found.");
            }

        } catch (SQLException e) {
            logger.error("Error retrieving migrations: {}", e.getMessage(), e);
        }
    }

    public void clearMigrationHistory(Connection connection) {
        String sql = "DELETE FROM migration_history";

        try (Statement stmt = connection.createStatement()) {

            int rowsAffected = stmt.executeUpdate(sql);
            logger.info("Cleared migration history. {} rows deleted.", rowsAffected);
        } catch (SQLException e) {
            logger.error("Error clearing migration history: {}", e.getMessage(), e);
        }
    }

    public String getChecksum(MigrationFile migration) {
        try {
            byte[] fileBytes = Files.readAllBytes(migration.filePath());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute checksum for " + migration.filePath(), e);
        }
    }

}
