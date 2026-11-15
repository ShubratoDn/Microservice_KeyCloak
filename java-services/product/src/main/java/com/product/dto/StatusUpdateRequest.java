package com.product.dto;

import com.product.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required")
        ProductStatus status
) {
}
