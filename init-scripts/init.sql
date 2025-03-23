-- Create the migration_user with a secure password
CREATE USER ${DB_USER} WITH ENCRYPTED PASSWORD ${DB_PASSWORD};

-- Create the migration database
CREATE DATABASE ${DB_NAME} OWNER ${DB_USER};

-- Grant privileges on the database to migration_user
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};

-- Connect to the migration_db
\c ${DB_NAME};

-- Grant schema usage and table creation permissions
GRANT USAGE, CREATE ON SCHEMA public TO ${DB_USER};

-- Ensure migration_user can manage tables inside the public schema
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${DB_USER};
