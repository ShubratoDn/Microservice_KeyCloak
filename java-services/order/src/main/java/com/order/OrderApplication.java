package com.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// @EnableFeignClients turns on OpenFeign.
// Feign lets this service call another service by writing a simple Java
// interface (see the "client" package) instead of writing HTTP code.
// Spring scans this package, finds every interface marked with
// @FeignClient, and creates the working code for it automatically.
// The service name in @FeignClient (for example "customer-service") is
// looked up in Eureka, so we never write the other service's port here.
@SpringBootApplication
@EnableFeignClients
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
