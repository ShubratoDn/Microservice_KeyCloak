package com.product.service.impl;

import com.product.dto.PagedResponse;
import com.product.dto.ProductRequest;
import com.product.dto.ProductResponse;
import com.product.entity.Category;
import com.product.entity.Product;
import com.product.entity.ProductStatus;
import com.product.exception.DuplicateResourceException;
import com.product.exception.ResourceNotFoundException;
import com.product.mapper.ProductMapper;
import com.product.repository.CategoryRepository;
import com.product.repository.ProductRepository;
import com.product.service.ProductService;
import com.product.specification.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final String PRODUCT = "Product";
    private static final String CATEGORY = "Category";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = productMapper.normalizeSku(request.sku());
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("A product with SKU '%s' already exists".formatted(sku));
        }

        Category category = findCategory(request.categoryId());
        Product saved = productRepository.save(productMapper.toEntity(request, category));
        log.info("Created product with id {}", saved.getId());
        return productMapper.toResponse(saved);
    }

    @Override
    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findProduct(id));
    }

    @Override
    public PagedResponse<ProductResponse> search(
            Long categoryId, ProductStatus status, String keyword, Pageable pageable) {

        Specification<Product> specification = (root, query, cb) -> cb.conjunction();

        if (categoryId != null) {
            specification = specification.and(ProductSpecifications.hasCategory(categoryId));
        }
        if (status != null) {
            specification = specification.and(ProductSpecifications.hasStatus(status));
        }
        if (StringUtils.hasText(keyword)) {
            specification = specification.and(ProductSpecifications.nameOrSkuContains(keyword.trim()));
        }

        return PagedResponse.from(
                productRepository.findAll(specification, pageable).map(productMapper::toResponse));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);

        String sku = productMapper.normalizeSku(request.sku());
        if (productRepository.existsBySkuAndIdNot(sku, id)) {
            throw new DuplicateResourceException("A product with SKU '%s' already exists".formatted(sku));
        }

        Category category = findCategory(request.categoryId());
        productMapper.applyToEntity(request, product, category);
        log.info("Updated product with id {}", id);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateStatus(Long id, ProductStatus status) {
        Product product = findProduct(id);
        product.setStatus(status);
        log.info("Changed status of product {} to {}", id, status);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, int quantity) {
        Product product = findProduct(id);
        product.setStockQuantity(quantity);
        log.info("Changed stock of product {} to {}", id, quantity);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productRepository.delete(findProduct(id));
        log.info("Deleted product with id {}", id);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY, id));
    }
}
