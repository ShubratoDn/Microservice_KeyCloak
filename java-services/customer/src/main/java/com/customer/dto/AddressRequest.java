package com.customer.dto;

import com.customer.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotNull(message = "Address type is required")
        AddressType type,

        @NotBlank(message = "Street is required")
        @Size(max = 255, message = "Street must be at most 255 characters")
        String street,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be at most 100 characters")
        String city,

        @Size(max = 100, message = "State must be at most 100 characters")
        String state,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must be at most 20 characters")
        String postalCode,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must be at most 100 characters")
        String country
) {
}
