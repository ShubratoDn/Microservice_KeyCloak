package com.customer.dto;

import com.customer.entity.CustomerStatus;

public record CustomerSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        CustomerStatus status
) {
}
