package com.user.identity;

import com.user.entity.UserType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class UserTypeRoles {

    private static final List<String> BUSINESS_AREAS = List.of("CUSTOMER", "PRODUCT", "ORDER");

    private UserTypeRoles() {
    }

    public static Set<String> rolesFor(UserType type) {
        Set<String> roles = new LinkedHashSet<>();
        roles.add(type.name());

        switch (type) {
            case ADMIN -> {
                BUSINESS_AREAS.forEach(area -> addAll(roles, area, "ADD", "VIEW", "EDIT", "DELETE"));
                addAll(roles, "USER", "ADD", "VIEW", "EDIT", "DELETE");
            }
            case MANAGER -> {
                BUSINESS_AREAS.forEach(area -> addAll(roles, area, "ADD", "VIEW", "EDIT"));
                addAll(roles, "USER", "VIEW");
            }
            case STAFF -> {
                addAll(roles, "CUSTOMER", "ADD", "VIEW");
                addAll(roles, "PRODUCT", "VIEW");
                addAll(roles, "ORDER", "ADD", "VIEW");
            }
        }
        return roles;
    }

    private static void addAll(Set<String> roles, String area, String... actions) {
        for (String action : actions) {
            roles.add(area + "_" + action);
        }
    }
}
