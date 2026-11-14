package com.customer.dto;

import com.customer.entity.CustomerStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        CustomerStatus status,
        List<AddressResponse> addresses,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
