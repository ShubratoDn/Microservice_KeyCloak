package com.user.service.impl;

import com.user.dto.CreateUserRequest;
import com.user.dto.UpdateUserRequest;
import com.user.dto.UserResponse;
import com.user.entity.Gender;
import com.user.entity.Religion;
import com.user.entity.UserProfile;
import com.user.entity.UserType;
import com.user.exception.BusinessRuleException;
import com.user.exception.DuplicateResourceException;
import com.user.identity.KeycloakAdminClient;
import com.user.mapper.UserMapper;
import com.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String KEYCLOAK_ID = "11111111-1111-1111-1111-111111111111";
    private static final String MY_KEYCLOAK_ID = "22222222-2222-2222-2222-222222222222";

    @Mock
    private UserProfileRepository userRepository;
    @Mock
    private KeycloakAdminClient keycloak;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, new UserMapper(), keycloak);
    }

    @Test
    void createRegistersTheLoginInKeycloakGivesRolesAndSavesTheProfile() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(keycloak.createUser("jane@example.com", "jane@example.com", "Jane", null, "Secret123")).thenReturn(KEYCLOAK_ID);
        when(userRepository.save(any(UserProfile.class))).thenAnswer(call -> call.getArgument(0));

        UserResponse response = service.create(createRequest(UserType.STAFF), "admin.user");

        ArgumentCaptor<Collection<String>> roles = ArgumentCaptor.forClass(Collection.class);
        verify(keycloak).assignRealmRoles(eq(KEYCLOAK_ID), roles.capture());
        assertThat(roles.getValue()).contains("STAFF", "ORDER_ADD");
        assertThat(response.email()).isEqualTo("jane@example.com");
        assertThat(response.keycloakId()).isEqualTo(KEYCLOAK_ID);
        assertThat(response.createdBy()).isEqualTo("admin.user");
        assertThat(response.lastName()).isNull();
    }

    @Test
    void createUsesTheGivenUsernameAsTheLoginName() {
        when(keycloak.createUser("jane.doe", "jane@example.com", "Jane", null, "Secret123")).thenReturn(KEYCLOAK_ID);
        when(userRepository.save(any(UserProfile.class))).thenAnswer(call -> call.getArgument(0));

        UserResponse response = service.create(createRequest(UserType.STAFF, " Jane.Doe "), "admin.user");

        assertThat(response.username()).isEqualTo("jane.doe");
        assertThat(response.email()).isEqualTo("jane@example.com");
    }

    @Test
    void createRejectsADuplicateUsernameBeforeTouchingKeycloak() {
        when(userRepository.existsByUsername("jane.doe")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest(UserType.STAFF, "jane.doe"), "admin.user"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(keycloak, never()).createUser(any(), any(), any(), any(), any());
    }

    @Test
    void createRejectsADuplicateEmailBeforeTouchingKeycloak() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest(UserType.STAFF), "admin.user"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(keycloak, never()).createUser(any(), any(), any(), any(), any());
    }

    @Test
    void createRemovesTheKeycloakUserWhenSavingTheProfileFails() {
        when(keycloak.createUser(any(), any(), any(), any(), any())).thenReturn(KEYCLOAK_ID);
        when(userRepository.save(any(UserProfile.class))).thenThrow(new IllegalStateException("database is down"));

        assertThatThrownBy(() -> service.create(createRequest(UserType.MANAGER), "admin.user"))
                .isInstanceOf(IllegalStateException.class);

        verify(keycloak).deleteUser(KEYCLOAK_ID);
    }

    @Test
    void updateSwapsKeycloakRolesWhenTheUserTypeChanges() {
        UserProfile staff = existingUser(UserType.STAFF);
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(userRepository.save(any(UserProfile.class))).thenAnswer(call -> call.getArgument(0));

        service.update(5L, updateRequest(UserType.MANAGER), MY_KEYCLOAK_ID);

        ArgumentCaptor<Collection<String>> removed = ArgumentCaptor.forClass(Collection.class);
        verify(keycloak).removeRealmRoles(eq(KEYCLOAK_ID), removed.capture());
        assertThat(removed.getValue()).contains("STAFF");
        ArgumentCaptor<Collection<String>> added = ArgumentCaptor.forClass(Collection.class);
        verify(keycloak).assignRealmRoles(eq(KEYCLOAK_ID), added.capture());
        assertThat(added.getValue()).contains("MANAGER", "ORDER_EDIT");
    }

    @Test
    void updateDoesNotTouchRolesWhenTheUserTypeStaysTheSame() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(existingUser(UserType.STAFF)));
        when(userRepository.save(any(UserProfile.class))).thenAnswer(call -> call.getArgument(0));

        service.update(5L, updateRequest(UserType.STAFF), MY_KEYCLOAK_ID);

        verify(keycloak, never()).removeRealmRoles(any(), anyCollection());
        verify(keycloak, never()).assignRealmRoles(any(), anyCollection());
    }

    @Test
    void anAdminCannotChangeTheTypeOfTheirOwnAccount() {
        UserProfile me = existingUser(UserType.ADMIN);
        me.setKeycloakId(MY_KEYCLOAK_ID);
        when(userRepository.findById(5L)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> service.update(5L, updateRequest(UserType.STAFF), MY_KEYCLOAK_ID))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void anAdminCannotDisableOrDeleteTheirOwnAccount() {
        UserProfile me = existingUser(UserType.ADMIN);
        me.setKeycloakId(MY_KEYCLOAK_ID);
        when(userRepository.findById(5L)).thenReturn(Optional.of(me));

        assertThatThrownBy(() -> service.updateStatus(5L, false, MY_KEYCLOAK_ID))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.delete(5L, MY_KEYCLOAK_ID))
                .isInstanceOf(BusinessRuleException.class);

        verify(keycloak, never()).setEnabled(any(), eq(false));
        verify(keycloak, never()).deleteUser(any());
    }

    @Test
    void deleteRemovesTheKeycloakAccountAndTheProfile() {
        UserProfile user = existingUser(UserType.STAFF);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        service.delete(5L, MY_KEYCLOAK_ID);

        verify(keycloak).deleteUser(KEYCLOAK_ID);
        verify(userRepository).delete(user);
    }

    private CreateUserRequest createRequest(UserType type) {
        return createRequest(type, null);
    }

    private CreateUserRequest createRequest(UserType type, String username) {
        return new CreateUserRequest("Jane", " ", " Jane@Example.com ", username, "+8801700000000", "12 Lake Road",
                null, Gender.FEMALE, Religion.OTHER, type, "Secret123");
    }

    private UpdateUserRequest updateRequest(UserType type) {
        return new UpdateUserRequest("Jane", "Doe", "+8801700000000", "12 Lake Road",
                "Village Road", Gender.FEMALE, null, type);
    }

    private UserProfile existingUser(UserType type) {
        return UserProfile.builder()
                .id(5L)
                .keycloakId(KEYCLOAK_ID)
                .firstName("Jane")
                .email("jane@example.com")
                .phoneNumber("+8801700000000")
                .presentAddress("12 Lake Road")
                .gender(Gender.FEMALE)
                .userType(type)
                .build();
    }
}
