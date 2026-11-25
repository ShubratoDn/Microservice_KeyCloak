package com.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// The Config Server is not running in tests, so we give the Keycloak address here.
// It is only contacted when the first token is checked, so Keycloak does not need to run.
@SpringBootTest(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/Learn_Microservices")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
