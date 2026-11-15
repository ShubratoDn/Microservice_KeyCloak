package com.product.specification;

import com.product.entity.Product;
import com.product.entity.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private static final char ESCAPE_CHAR = '\\';

    private ProductSpecifications() {
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Product> nameOrSkuContains(String keyword) {
        String pattern = "%" + escapeLike(keyword.toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern, ESCAPE_CHAR),
                cb.like(cb.lower(root.get("sku")), pattern, ESCAPE_CHAR)
        );
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
