package com.user.identity;

import com.user.exception.DuplicateResourceException;
import com.user.exception.IdentityProviderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
public class KeycloakAdminClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };
    private static final String USERS = "/admin/realms/{realm}/users";

    private final KeycloakProperties properties;
    private final RestClient restClient;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    public KeycloakAdminClient(KeycloakProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.serverUrl()).build();
    }

    public String createUser(String username, String email, String firstName, String lastName, String temporaryPassword) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("firstName", firstName);
        if (lastName != null) {
            body.put("lastName", lastName);
        }
        body.put("enabled", true);
        body.put("emailVerified", true);
        body.put("credentials", List.of(Map.of("type", "password", "value", temporaryPassword, "temporary", true)));

        URI location = execute(() -> restClient.post()
                .uri(USERS, properties.realm())
                .headers(this::bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation());

        if (location == null) {
            throw new IdentityProviderException("Keycloak did not return the id of the new user", null);
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public void assignRealmRoles(String userId, Collection<String> roleNames) {
        List<Map<String, Object>> roles = roleRepresentations(roleNames);
        execute(() -> restClient.post()
                .uri(USERS + "/{id}/role-mappings/realm", properties.realm(), userId)
                .headers(this::bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve()
                .toBodilessEntity());
    }

    public void removeRealmRoles(String userId, Collection<String> roleNames) {
        List<Map<String, Object>> roles = roleRepresentations(roleNames);
        execute(() -> restClient.method(HttpMethod.DELETE)
                .uri(USERS + "/{id}/role-mappings/realm", properties.realm(), userId)
                .headers(this::bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve()
                .toBodilessEntity());
    }

    public void updateNames(String userId, String firstName, String lastName) {
        modifyUser(userId, user -> {
            user.put("firstName", firstName);
            user.put("lastName", lastName == null ? "" : lastName);
        });
    }

    public void setEnabled(String userId, boolean enabled) {
        modifyUser(userId, user -> user.put("enabled", enabled));
    }

    public void deleteUser(String userId) {
        try {
            execute(() -> restClient.delete()
                    .uri(USERS + "/{id}", properties.realm(), userId)
                    .headers(this::bearer)
                    .retrieve()
                    .toBodilessEntity());
        } catch (IdentityProviderException ex) {
            if (!(ex.getCause() instanceof RestClientResponseException response)
                    || response.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw ex;
            }
        }
    }

    private void modifyUser(String userId, Consumer<Map<String, Object>> change) {
        Map<String, Object> user = execute(() -> restClient.get()
                .uri(USERS + "/{id}", properties.realm(), userId)
                .headers(this::bearer)
                .retrieve()
                .body(MAP));
        change.accept(user);
        execute(() -> restClient.put()
                .uri(USERS + "/{id}", properties.realm(), userId)
                .headers(this::bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .toBodilessEntity());
    }

    private List<Map<String, Object>> roleRepresentations(Collection<String> roleNames) {
        List<Map<String, Object>> roles = new ArrayList<>();
        for (String name : roleNames) {
            Map<String, Object> role = execute(() -> restClient.get()
                    .uri("/admin/realms/{realm}/roles/{name}", properties.realm(), name)
                    .headers(this::bearer)
                    .retrieve()
                    .body(MAP));
            roles.add(Map.of("id", role.get("id"), "name", role.get("name")));
        }
        return roles;
    }

    private void bearer(HttpHeaders headers) {
        headers.setBearerAuth(token());
    }

    private synchronized String token() {
        if (accessToken == null || Instant.now().isAfter(accessTokenExpiresAt.minusSeconds(10))) {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", properties.clientId());
            form.add("client_secret", properties.clientSecret());

            Map<String, Object> response = execute(() -> restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(MAP));

            accessToken = (String) response.get("access_token");
            accessTokenExpiresAt = Instant.now().plusSeconds(((Number) response.get("expires_in")).longValue());
        }
        return accessToken;
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new DuplicateResourceException("A user with this email already exists in the identity provider");
            }
            log.error("Keycloak answered {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IdentityProviderException("Keycloak request failed with status " + ex.getStatusCode().value(), ex);
        } catch (RestClientException ex) {
            log.error("Cannot reach Keycloak", ex);
            throw new IdentityProviderException("Cannot reach Keycloak", ex);
        }
    }
}
