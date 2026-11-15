package com.order.client.dto;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        CustomerStatus status
) {
}
