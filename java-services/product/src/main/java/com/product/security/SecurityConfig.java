package com.product.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

// This class is the security setup of this service.
//
// How login works here (simple version):
//   1. The user logs in at Keycloak and gets a token (a JWT).
//   2. The client sends the token in the header:  Authorization: Bearer <token>
//   3. This service checks the token. Is it real? Is it from our Keycloak realm?
//      Is it not expired? If not, the answer is 401 Unauthorized.
//   4. The roles inside the token (like ORDER_VIEW) become "authorities".
//      @PreAuthorize on each controller method checks them.
//      If the role is missing, the answer is 403 Forbidden.
//
// The Keycloak address (issuer-uri) is NOT written here.
// It comes from the Config Server (configurations/application.properties).
@Configuration
@EnableWebSecurity
// Turns on @PreAuthorize("hasAuthority('...')") on controller methods.
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler errorHandler) throws Exception {
        http
                // CSRF protects browser sessions with cookies. We use tokens, not cookies, so it is not needed.
                .csrf(AbstractHttpConfigurer::disable)
                // No server session. Every request must bring its own token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Every request needs a valid token. The exact role is checked later by @PreAuthorize.
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // This service is a "resource server": it only checks tokens, it never creates them.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler));
        return http.build();
    }

    // Tells Spring how to turn a token into authorities (we read the Keycloak realm roles).
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}
