package com.product.dto;

import com.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        ProductStatus status,
        CategorySummaryResponse category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
