package com.product.dto;

import com.product.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name must be at most 200 characters")
        String name,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must be at most 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$",
                message = "SKU may contain only letters, numbers, hyphen and underscore")
        String sku,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Price can have at most 10 digits and 2 decimal places")
        BigDecimal price,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        ProductStatus status,

        @NotNull(message = "Category id is required")
        Long categoryId
) {
}
