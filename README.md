# Database Migration Tool

## Overview
This is a custom Java 17 tool/library for managing database migrations, similar to Liquibase or Flyway. It reads SQL scripts from files and executes them in a controlled manner to ensure idempotent and transactional database updates.

## Features
- Reads and executes SQL migration scripts
- Versioning and checksum validation
- Ensures transactional execution
- Supports PostgreSQL via JDBC
- Logs migration details with Log4j
- Supports automatic rollback on failures (via transactions)
- Idempotent execution (ensures no duplicate migrations)
- Supports **OneToOne, OneToMany, and ManyToMany** table relationships
- CLI commands for managing migrations
- **Docker support** for easy database setup

## Technologies Used
- **Java 17**
- **JDBC** (PostgreSQL driver)
- **Gradle** for build automation
- **Docker & Docker Compose** for database setup
- **Log4j** for logging

## Setting Up the Database with Docker
To quickly set up a PostgreSQL database, use the provided `docker-compose.yml` file as follows:

### Start the Database:
```sh
docker compose up -d
```  

### Stop the Database:
```sh
docker compose down
```  

### Connecting to PostgreSQL inside Docker:
To connect to the PostgreSQL instance running inside the container, use:

```sh
docker exec -it migration_app psql -h db -U ${DB_USER} -d ${DB_NAME}
```

**Example usage:**
```sh
docker exec -it postgres_migration psql -h db -U migration_user -d migration_db
```

## Building the Project
```sh
./gradlew clean build
```  

## Running Tests
```sh
./gradlew test
```  

## Running the Migration Tool
```sh
./gradlew run --args=<command>
```  

### Available Commands
- `migrate` - Applies pending migrations to the database.
- `status` - Displays applied and failed migrations.
- `reset` - Clears the migration history.
- `help` - Displays information about available commands.

**Example Usage:**
```sh
./gradlew run --args=migrate
```  

## License
This project is licensed under the MIT License.