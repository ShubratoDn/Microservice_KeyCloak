package com.user.dto;

import com.user.entity.Gender;
import com.user.entity.Religion;
import com.user.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,
        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,
        @Pattern(regexp = "^[A-Za-z0-9._@-]{3,100}$",
                message = "Username must be 3 to 100 characters: letters, numbers, dot, underscore, hyphen or @")
        String username,
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$", message = "Phone number is not valid")
        String phoneNumber,
        @NotBlank(message = "Present address is required")
        @Size(max = 500, message = "Present address must be at most 500 characters")
        String presentAddress,
        @Size(max = 500, message = "Permanent address must be at most 500 characters")
        String permanentAddress,
        @NotNull(message = "Gender is required")
        Gender gender,
        Religion religion,
        @NotNull(message = "User type is required")
        UserType userType,
        @NotBlank(message = "Temporary password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        String password
) {
}
