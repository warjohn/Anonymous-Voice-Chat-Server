package org.example.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up the database connection and initialization.
 * This class is annotated with @Configuration, indicating that it contains Spring configuration.
 */
@Configuration
public class DataBaseConfig {

    // Path to the SQLite database file
    private static final String DB_PATH = "src/main/resources/data/data.db";

    /**
     * Creates and initializes a bean for managing the database.
     * This method is annotated with @Bean, meaning it will be managed by the Spring container.
     *
     * @return an instance of the DataBase class, fully initialized and ready to use
     */
    @Bean
    public DataBase dataBase() {
        // Create a new instance of the DataBase class with the specified database path
        DataBase databasemanager = new DataBase(DB_PATH);

        try {
            // Open a connection to the database
            databasemanager.openConnection();

            // Create the 'users' table if it does not already exist
            databasemanager.createUsersTable();

            // Create the 'messages' table if it does not already exist
            databasemanager.createTable();
        } catch (Exception e) {
            // If any error occurs during initialization, throw a runtime exception
            throw new RuntimeException("Error with opening or creating the database", e);
        }

        // Return the fully initialized DataBase instance
        return databasemanager;
    }
}