package com.product.service.impl;

import com.product.dto.CategoryRequest;
import com.product.dto.CategoryResponse;
import com.product.entity.Category;
import com.product.exception.DuplicateResourceException;
import com.product.exception.ResourceInUseException;
import com.product.exception.ResourceNotFoundException;
import com.product.mapper.CategoryMapper;
import com.product.repository.CategoryRepository;
import com.product.repository.ProductRepository;
import com.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY = "Category";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = categoryMapper.normalizeName(request.name());
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A category named '%s' already exists".formatted(name));
        }

        Category saved = categoryRepository.save(categoryMapper.toEntity(request));
        log.info("Created category with id {}", saved.getId());
        return categoryMapper.toResponse(saved);
    }

    @Override
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(findCategory(id));
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findCategory(id);

        String name = categoryMapper.normalizeName(request.name());
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("A category named '%s' already exists".formatted(name));
        }

        categoryMapper.applyToEntity(request, category);
        log.info("Updated category with id {}", id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findCategory(id);

        if (productRepository.existsByCategoryId(id)) {
            throw new ResourceInUseException(
                    "Category '%s' still has products. Move or delete them first".formatted(category.getName()));
        }

        categoryRepository.delete(category);
        log.info("Deleted category with id {}", id);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, id));
    }
}
