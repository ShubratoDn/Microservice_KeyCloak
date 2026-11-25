package com.user.mapper;

import com.user.dto.CreateUserRequest;
import com.user.dto.UpdateUserRequest;
import com.user.dto.UserResponse;
import com.user.dto.UserSummaryResponse;
import com.user.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfile toEntity(CreateUserRequest request, String keycloakId, String createdBy) {
        return UserProfile.builder()
                .keycloakId(keycloakId)
                .firstName(request.firstName().trim())
                .lastName(blankToNull(request.lastName()))
                .email(normalizeEmail(request.email()))
                .username(resolveUsername(request))
                .phoneNumber(request.phoneNumber().trim())
                .presentAddress(request.presentAddress().trim())
                .permanentAddress(blankToNull(request.permanentAddress()))
                .gender(request.gender())
                .religion(request.religion())
                .userType(request.userType())
                .createdBy(createdBy)
                .build();
    }

    public void applyToEntity(UpdateUserRequest request, UserProfile user) {
        user.setFirstName(request.firstName().trim());
        user.setLastName(blankToNull(request.lastName()));
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setPresentAddress(request.presentAddress().trim());
        user.setPermanentAddress(blankToNull(request.permanentAddress()));
        user.setGender(request.gender());
        user.setReligion(request.religion());
        user.setUserType(request.userType());
    }

    public UserResponse toResponse(UserProfile user) {
        return new UserResponse(
                user.getId(),
                user.getKeycloakId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getPhoneNumber(),
                user.getPresentAddress(),
                user.getPermanentAddress(),
                user.getGender(),
                user.getReligion(),
                user.getUserType(),
                user.isEnabled(),
                user.getCreatedBy(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UserSummaryResponse toSummaryResponse(UserProfile user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getPhoneNumber(),
                user.getUserType(),
                user.isEnabled()
        );
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    // Keycloak stores usernames in lower case. If no username is given, the email is the login name.
    public String resolveUsername(CreateUserRequest request) {
        String username = blankToNull(request.username());
        return username == null ? normalizeEmail(request.email()) : username.toLowerCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
