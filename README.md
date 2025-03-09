# Database Migration Tool

## Overview
This is a custom Java 17 library for managing database migrations, similar to Liquibase or Flyway. It reads SQL scripts from files and executes them in a controlled manner to ensure idempotent and transactional database updates.

## Features
- Reads and executes SQL migration scripts
- Versioning and checksum validation
- Ensures transactional execution
- Supports PostgreSQL via JDBC
- Logs migration details with Logback
- Uses Docker for easy database setup
- Idempotent execution (ensures no duplicate migrations)
- Supports OneToOne, OneToMany, and ManyToMany table relationships

## Technologies Used
- **Java 17**
- **JDBC** (PostgreSQL driver)
- **Gradle** for build automation
- **Docker & Docker Compose** for database setup
- **Logback** for logging
- **JUnit 5** for testing

## Running Tests
```sh
./gradlew test
```

## License
This project is licensed under the MIT License.