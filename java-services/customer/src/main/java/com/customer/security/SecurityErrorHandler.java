package com.customer.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Sends a clean JSON error (same shape as the other errors of this service) when:
//   - the token is missing or not valid         -> 401 Unauthorized
//   - the token is valid but has no permission  -> 403 Forbidden
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final BearerTokenAuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // This sets the 401 status and the WWW-Authenticate header (it says why the token failed).
        bearerEntryPoint.commence(request, response, authException);
        writeBody(response, HttpStatus.UNAUTHORIZED, "Unauthorized",
                "A valid access token is required to use this API");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        writeBody(response, HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to perform this action");
    }

    private void writeBody(HttpServletResponse response, HttpStatus status, String title, String detail)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}"
                        .formatted(title, status.value(), detail));
    }
}
