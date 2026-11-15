package com.product.mapper;

import com.product.dto.CategoryRequest;
import com.product.dto.CategoryResponse;
import com.product.dto.CategorySummaryResponse;
import com.product.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        applyToEntity(request, category);
        return category;
    }

    public void applyToEntity(CategoryRequest request, Category category) {
        category.setName(normalizeName(request.name()));
        category.setDescription(request.description() == null ? null : request.description().trim());
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public CategorySummaryResponse toSummaryResponse(Category category) {
        return new CategorySummaryResponse(category.getId(), category.getName());
    }

    public String normalizeName(String name) {
        return name.trim();
    }
}
