package com.product.service;

import com.product.dto.CategoryRequest;
import com.product.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse getById(Long id);

    List<CategoryResponse> getAll();

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
