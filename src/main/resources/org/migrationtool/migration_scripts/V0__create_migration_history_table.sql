CREATE TABLE migration_history (
    id SERIAL PRIMARY KEY,
    version VARCHAR(50) UNIQUE NOT NULL,            -- Version number (e.g., V1, V2, V3)
    description VARCHAR(255) NOT NULL,              -- Description of the migration
    file_type VARCHAR(255) NOT NULL,                -- File type of the migration script
    script_name VARCHAR(255) UNIQUE NOT NULL,       -- File name of the migration script
    checksum VARCHAR(64) NOT NULL,                  -- Hash checksum of the script
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- When it was applied
    success BOOLEAN NOT NULL DEFAULT TRUE           -- Whether migration succeeded
);