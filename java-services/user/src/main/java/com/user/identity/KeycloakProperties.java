package com.user.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Filled from the "keycloak.admin.*" settings (see user-service.properties in the Config Server).
@ConfigurationProperties(prefix = "keycloak.admin")
public record KeycloakProperties(
        String serverUrl,
        String realm,
        String clientId,
        String clientSecret
) {
}
