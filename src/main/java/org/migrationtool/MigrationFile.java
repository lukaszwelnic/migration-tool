package org.migrationtool;

import java.nio.file.Path;

public record MigrationFile(String version, String description, Path filePath) {

    @Override
    public String toString() {
        return "MigrationFile{version='" + version + "', description='" + description + "', filePath=" + filePath + "}";
    }
}