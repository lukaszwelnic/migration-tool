package org.migrationtool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

public class MigrationHistoryManager {

    private final Connection connection;

    private static final Logger logger = LogManager.getLogger(MigrationHistoryManager.class);

    public MigrationHistoryManager(Connection connection) {
        this.connection = connection;
    }

    // Record a migration in the history table
    public void addMigration(String version, String description, String fileType, String scriptName, String checksum, boolean success) throws SQLException {
        String sql = "INSERT INTO migration_history (version, description, file_type, script_name, checksum, success) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, version);
            stmt.setString(2, description);
            stmt.setString(3, fileType);
            stmt.setString(4, scriptName);
            stmt.setString(5, checksum);
            stmt.setBoolean(6, success);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("An error has occurred", e);
        }
    }

    // Check if a migration has already been applied
    public boolean isMigrationApplied(String version) {
        String sql = "SELECT COUNT(*) FROM migration_history WHERE version = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, version);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("An error has occurred", e);
        }
        return false;
    }

    // Get all applied migrations
    public List<String> getAppliedMigrations() {
        List<String> migrations = new ArrayList<>();
        String sql = "SELECT version FROM migration_history ORDER BY version";

        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                migrations.add(resultSet.getString("version"));
            }
        } catch (SQLException e) {
            logger.error("An error has occurred", e);
        }
        return migrations;
    }

    // Clear migration history (for testing purposes or re-initialization)
    public void clearMigrationHistory() {
        String sql = "DELETE FROM migration_history";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.error("An error has occurred", e);
        }
    }

}
