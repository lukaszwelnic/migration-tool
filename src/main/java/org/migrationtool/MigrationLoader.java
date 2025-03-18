package org.migrationtool;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MigrationLoader {

    private static final String MIGRATIONS_DIRECTORY = "src/main/resources/org/migrationtool/migration-scripts";
    private static final Pattern MIGRATION_SCRIPT_PATTERN = Pattern.compile("^V(\\d+)__(.+)\\.sql$");
    private static final Logger logger = LogManager.getLogger(MigrationLoader.class);

    public static List<MigrationFile> loadMigrations() {
        List<MigrationFile> migrations = new ArrayList<>();
        try {
            List<Path> files = Files.list(Paths.get(MIGRATIONS_DIRECTORY))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .sorted(Comparator.comparingInt(MigrationLoader::extractVersion))
                    .toList();

            for (Path file : files) {
                Matcher matcher = MIGRATION_SCRIPT_PATTERN.matcher(file.getFileName().toString());
                if (matcher.matches()) {
                    String version = matcher.group(1);
                    String description = matcher.group(2).replace("_", " ");
                    migrations.add(new MigrationFile(version, description, file));
                } else {
                    logger.warn("Skipping invalid migration file: {}", file.getFileName());
                }
            }
        } catch (IOException e) {
            logger.error("Error loading migration files: {}", e.getMessage(), e);
        }
        return migrations;
    }

    private static int extractVersion(Path path) {
        Matcher matcher = MIGRATION_SCRIPT_PATTERN.matcher(path.getFileName().toString());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return Integer.MAX_VALUE;
    }
}
