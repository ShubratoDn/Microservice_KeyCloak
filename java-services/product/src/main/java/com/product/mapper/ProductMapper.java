package com.product.mapper;

import com.product.dto.ProductRequest;
import com.product.dto.ProductResponse;
import com.product.entity.Category;
import com.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public Product toEntity(ProductRequest request, Category category) {
        Product product = new Product();
        applyToEntity(request, product, category);
        return product;
    }

    public void applyToEntity(ProductRequest request, Product product, Category category) {
        product.setName(request.name().trim());
        product.setDescription(request.description() == null ? null : request.description().trim());
        product.setSku(normalizeSku(request.sku()));
        product.setPrice(request.price());
        product.setCategory(category);

        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                categoryMapper.toSummaryResponse(product.getCategory()),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }
}
