package com.datastream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the DataStream gRPC API application.
 */
@SpringBootApplication
public class DatastreamApiApplication {

    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DatastreamApiApplication.class, args);
    }
}
