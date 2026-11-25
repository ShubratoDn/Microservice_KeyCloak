package com.user.identity;

import com.user.entity.UserType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTypeRolesTest {

    @Test
    void adminGetsEveryPermissionAndTheAdminMarkerRole() {
        assertThat(UserTypeRoles.rolesFor(UserType.ADMIN))
                .contains("ADMIN", "CUSTOMER_DELETE", "PRODUCT_DELETE", "ORDER_DELETE",
                        "USER_ADD", "USER_VIEW", "USER_EDIT", "USER_DELETE")
                .hasSize(17);
    }

    @Test
    void managerCanAddViewAndEditButNotDeleteOrManageUsers() {
        assertThat(UserTypeRoles.rolesFor(UserType.MANAGER))
                .contains("MANAGER", "ORDER_ADD", "ORDER_EDIT", "CUSTOMER_EDIT", "PRODUCT_EDIT", "USER_VIEW")
                .doesNotContain("ORDER_DELETE", "CUSTOMER_DELETE", "PRODUCT_DELETE", "USER_ADD", "USER_EDIT", "USER_DELETE");
    }

    @Test
    void staffCanTakeOrdersButCannotChangeOrDeleteAnything() {
        assertThat(UserTypeRoles.rolesFor(UserType.STAFF))
                .containsExactlyInAnyOrder("STAFF", "CUSTOMER_ADD", "CUSTOMER_VIEW", "PRODUCT_VIEW",
                        "ORDER_ADD", "ORDER_VIEW");
    }

    @Test
    void everyTypeCanReadWhatItNeedsToCreateAnOrder() {
        for (UserType type : UserType.values()) {
            assertThat(UserTypeRoles.rolesFor(type)).contains("CUSTOMER_VIEW", "PRODUCT_VIEW", "ORDER_ADD");
        }
    }
}
