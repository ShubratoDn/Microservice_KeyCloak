package com.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequest(
        @NotNull(message = "Customer id is required")
        Long customerId,

        @NotBlank(message = "Shipping address is required")
        @Size(max = 500, message = "Shipping address must be at most 500 characters")
        String shippingAddress,

        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes,

        @NotEmpty(message = "An order must contain at least one item")
        List<@NotNull(message = "Order item must not be null") @Valid OrderItemRequest> items
) {
}
