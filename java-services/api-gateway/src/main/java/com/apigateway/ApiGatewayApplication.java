package com.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway: the single front door. Clients call this service, and it
 * forwards each request to the right microservice (see the routes in
 * config-server/.../configurations/api-gateway.properties).
 */
// @SpringBootApplication = turns on Spring Boot (auto-setup, component scanning).
// The gateway needs no extra annotation: the gateway starter sets up the routes,
// and the Eureka client starter registers it and lets "lb://" find other services.
@SpringBootApplication
public class ApiGatewayApplication {

    // Starting point of the app. Run this method to start the gateway.
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
