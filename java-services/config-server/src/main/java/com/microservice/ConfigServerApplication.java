package com.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server: gives settings (properties) to the other microservices.
 */
// @SpringBootApplication = turns on Spring Boot (auto-setup, component scanning).
@SpringBootApplication
// @EnableConfigServer = makes this app act as a Config Server.
// Without this line it would be a normal Spring Boot app.
@EnableConfigServer
public class ConfigServerApplication {

	// Starting point of the app. Run this method to start the server.
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
