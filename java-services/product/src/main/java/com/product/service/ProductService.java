package com.product.service;

import com.product.dto.PagedResponse;
import com.product.dto.ProductRequest;
import com.product.dto.ProductResponse;
import com.product.entity.ProductStatus;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse getById(Long id);

    PagedResponse<ProductResponse> search(Long categoryId, ProductStatus status, String keyword, Pageable pageable);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse updateStatus(Long id, ProductStatus status);

    ProductResponse updateStock(Long id, int quantity);

    void delete(Long id);
}
