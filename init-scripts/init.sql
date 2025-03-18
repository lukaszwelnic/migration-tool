-- Create the migration_user with a secure password
CREATE USER migration_user WITH ENCRYPTED PASSWORD 'password';

-- Create the migration database (if not already created)
CREATE DATABASE migration_db OWNER migration_user;

-- Grant privileges on the database to migration_user
GRANT ALL PRIVILEGES ON DATABASE migration_db TO migration_user;

-- Connect to the migration_db
\c migration_db;

-- Grant schema usage and table creation permissions
GRANT USAGE, CREATE ON SCHEMA public TO migration_user;

-- Ensure migration_user can manage tables inside the public schema
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO migration_user;
