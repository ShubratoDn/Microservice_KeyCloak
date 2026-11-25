package com.user.service.impl;

import com.user.dto.CreateUserRequest;
import com.user.dto.PagedResponse;
import com.user.dto.UpdateUserRequest;
import com.user.dto.UserResponse;
import com.user.dto.UserSummaryResponse;
import com.user.entity.UserProfile;
import com.user.entity.UserType;
import com.user.exception.BusinessRuleException;
import com.user.exception.DuplicateResourceException;
import com.user.exception.ResourceNotFoundException;
import com.user.identity.KeycloakAdminClient;
import com.user.identity.UserTypeRoles;
import com.user.mapper.UserMapper;
import com.user.repository.UserProfileRepository;
import com.user.service.UserService;
import com.user.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER = "User";

    private final UserProfileRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakAdminClient keycloak;

    // Not transactional on purpose: the calls to Keycloak must not hold a database
    // transaction open. If saving the profile fails, the new Keycloak user is removed again.
    @Override
    public UserResponse create(CreateUserRequest request, String createdBy) {
        String email = userMapper.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A user with email '%s' already exists".formatted(email));
        }

        String username = userMapper.resolveUsername(request);
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("A user with username '%s' already exists".formatted(username));
        }

        String keycloakId = keycloak.createUser(
                username, email, request.firstName().trim(), blankToNull(request.lastName()), request.password());
        try {
            keycloak.assignRealmRoles(keycloakId, UserTypeRoles.rolesFor(request.userType()));
            UserProfile saved = userRepository.save(userMapper.toEntity(request, keycloakId, createdBy));
            log.info("Created {} user {} (keycloak id {})", saved.getUserType(), saved.getId(), keycloakId);
            return userMapper.toResponse(saved);
        } catch (RuntimeException ex) {
            rollbackKeycloakUser(keycloakId);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> search(UserType userType, String keyword, Pageable pageable) {
        Specification<UserProfile> specification = Specification.unrestricted();

        if (userType != null) {
            specification = specification.and(UserSpecifications.hasUserType(userType));
        }
        if (keyword != null && !keyword.isBlank()) {
            specification = specification.and(UserSpecifications.matchesKeyword(keyword));
        }

        return PagedResponse.from(
                userRepository.findAll(specification, pageable).map(userMapper::toSummaryResponse));
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request, String currentKeycloakId) {
        UserProfile user = findUser(id);
        UserType oldType = user.getUserType();

        if (request.userType() != oldType && user.getKeycloakId().equals(currentKeycloakId)) {
            throw new BusinessRuleException("You cannot change the type of your own account");
        }

        keycloak.updateNames(user.getKeycloakId(), request.firstName().trim(), blankToNull(request.lastName()));
        if (request.userType() != oldType) {
            changeRoles(user.getKeycloakId(), oldType, request.userType());
        }

        userMapper.applyToEntity(request, user);
        UserProfile saved = userRepository.save(user);
        log.info("Updated user {}", id);
        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse updateStatus(Long id, boolean enabled, String currentKeycloakId) {
        UserProfile user = findUser(id);

        if (!enabled && user.getKeycloakId().equals(currentKeycloakId)) {
            throw new BusinessRuleException("You cannot disable your own account");
        }

        keycloak.setEnabled(user.getKeycloakId(), enabled);
        user.setEnabled(enabled);
        UserProfile saved = userRepository.save(user);
        log.info("Changed user {} to {}", id, enabled ? "enabled" : "disabled");
        return userMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id, String currentKeycloakId) {
        UserProfile user = findUser(id);

        if (user.getKeycloakId().equals(currentKeycloakId)) {
            throw new BusinessRuleException("You cannot delete your own account");
        }

        keycloak.deleteUser(user.getKeycloakId());
        userRepository.delete(user);
        log.info("Deleted user {}", id);
    }

    private void changeRoles(String keycloakId, UserType oldType, UserType newType) {
        Set<String> oldRoles = UserTypeRoles.rolesFor(oldType);
        Set<String> newRoles = UserTypeRoles.rolesFor(newType);

        Set<String> toRemove = new LinkedHashSet<>(oldRoles);
        toRemove.removeAll(newRoles);
        Set<String> toAdd = new LinkedHashSet<>(newRoles);
        toAdd.removeAll(oldRoles);

        if (!toRemove.isEmpty()) {
            keycloak.removeRealmRoles(keycloakId, toRemove);
        }
        if (!toAdd.isEmpty()) {
            keycloak.assignRealmRoles(keycloakId, toAdd);
        }
    }

    private void rollbackKeycloakUser(String keycloakId) {
        try {
            keycloak.deleteUser(keycloakId);
        } catch (RuntimeException ex) {
            log.error("Could not remove Keycloak user {} after a failed registration", keycloakId, ex);
        }
    }

    private UserProfile findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER, id));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
