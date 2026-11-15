package com.order.dto;

import com.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
