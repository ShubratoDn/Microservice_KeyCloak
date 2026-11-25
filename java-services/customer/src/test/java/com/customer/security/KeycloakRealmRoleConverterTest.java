package com.customer.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void mapsRealmRolesToAuthoritiesWithoutPrefix() {
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("roles", List.of("CUSTOMER_VIEW", "CUSTOMER_ADD"))));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("CUSTOMER_VIEW", "CUSTOMER_ADD");
    }

    @Test
    void returnsNoAuthoritiesWhenTokenHasNoRealmAccess() {
        assertThat(converter.convert(jwtWithClaims(Map.of("sub", "user")))).isEmpty();
    }

    @Test
    void returnsNoAuthoritiesWhenRolesClaimIsMissing() {
        assertThat(converter.convert(jwtWithClaims(Map.of("realm_access", Map.of())))).isEmpty();
    }

    private Jwt jwtWithClaims(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(existing -> existing.putAll(claims))
                .build();
    }
}
