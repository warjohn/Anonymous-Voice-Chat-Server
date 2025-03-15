package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class to bootstrap the Spring Boot application.
 * This class is annotated with @SpringBootApplication and @EnableAsync,
 * making it the entry point for the application.
 */
@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class, // Exclude automatic configuration of data sources
                HibernateJpaAutoConfiguration.class // Exclude automatic configuration of Hibernate/JPA
        }
)
@EnableAsync // Enable asynchronous processing in the application
public class DemoApplication {

    /**
     * The main method that serves as the entry point for the Spring Boot application.
     * It starts the application using SpringApplication.run().
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        // Start the Spring Boot application
        SpringApplication.run(DemoApplication.class, args);
    }
}