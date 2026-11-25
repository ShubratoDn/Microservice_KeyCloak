package com.product.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// Keycloak puts the realm roles of the user inside the token like this:
//   "realm_access": { "roles": ["ORDER_VIEW", "ORDER_ADD", ...] }
// This class reads that list and makes one authority for each role.
// The names are kept exactly as they are (no ROLE_ prefix), so we check them
// with hasAuthority('ORDER_VIEW').
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(String.valueOf(role)))
                .toList();
    }
}
