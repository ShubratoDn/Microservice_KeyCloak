package com.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// This class is the security setup of the gateway (the front door).
//
// The gateway checks ONE thing: "does the request have a valid Keycloak token?"
// If not, it sends 401 right here and the request never reaches a service.
// If yes, the gateway forwards the request AND the token (Authorization header)
// to the service. The service checks the token again and decides if the user
// has the right role (for example ORDER_VIEW). So the roles are checked inside
// each service, and the gateway is only the first guard.
//
// The Keycloak address (issuer-uri) is NOT written here.
// It comes from the Config Server (configurations/application.properties).
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler errorHandler) throws Exception {
        http
                // We use tokens, not cookies, so CSRF protection is not needed.
                .csrf(AbstractHttpConfigurer::disable)
                // No server session. Every request must bring its own token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Every request needs a valid token.
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // The gateway only checks tokens, it never creates them.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler));
        return http.build();
    }
}
