package com.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Discovery Service (Eureka Server): a phonebook where microservices
 * register themselves and find each other.
 */
// @SpringBootApplication = turns on Spring Boot (auto-setup, component scanning).
@SpringBootApplication
// @EnableEurekaServer = makes this app act as a Eureka Server (the registry).
@EnableEurekaServer
public class DiscoveryApplication {

    // Starting point of the app. Run this method to start the server.
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryApplication.class, args);
    }

}
