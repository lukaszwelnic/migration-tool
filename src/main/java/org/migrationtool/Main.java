package org.migrationtool;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        DatabaseConnector databaseConnector = null;

        try {
            databaseConnector = new DatabaseConnector();

            Connection connection = databaseConnector.getConnection();
            if (connection != null && databaseConnector.isValid()) {
                System.out.println("Database connection is successful!");
            } else {
                System.out.println("Database connection failed or is not valid.");
            }

            // Perform migrations

        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } finally {
            // Close the connection when done
            if (databaseConnector != null) {
                databaseConnector.closeConnection();
                System.out.println("Database connection has been closed.");
            }
        }
    }
}