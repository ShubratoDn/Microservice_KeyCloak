package com.customer.dto;

import com.customer.entity.CustomerStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required")
        CustomerStatus status
) {
}
