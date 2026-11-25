package com.user.dto;

import com.user.entity.Gender;
import com.user.entity.Religion;
import com.user.entity.UserType;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String keycloakId,
        String firstName,
        String lastName,
        String email,
        String username,
        String phoneNumber,
        String presentAddress,
        String permanentAddress,
        Gender gender,
        Religion religion,
        UserType userType,
        boolean enabled,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
