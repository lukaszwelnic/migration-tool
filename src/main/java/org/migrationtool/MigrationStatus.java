package org.migrationtool;

public enum MigrationStatus {
    SUCCESSFUL_UNCHANGED,   // Migration was successful and unchanged
    SUCCESSFUL_CHANGED,     // Migration was successful but changed
    FAILED_CHANGED,         // Migration failed but checksum changed, so retry is possible
    FAILED_UNCHANGED,       // Migration failed and checksum is the same, so we skip it
    NOT_APPLIED             // Migration is not yet in the history table
    }