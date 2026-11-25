package com.user.dto;

import com.user.entity.UserType;

public record UserSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String username,
        String phoneNumber,
        UserType userType,
        boolean enabled
) {
}
