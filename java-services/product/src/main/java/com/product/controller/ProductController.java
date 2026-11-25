package com.product.controller;

import com.product.dto.PagedResponse;
import com.product.dto.ProductRequest;
import com.product.dto.ProductResponse;
import com.product.dto.StatusUpdateRequest;
import com.product.dto.StockUpdateRequest;
import com.product.entity.ProductStatus;
import com.product.security.Permissions;
import com.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize(Permissions.HAS_PRODUCT_ADD)
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PreAuthorize(Permissions.HAS_PRODUCT_VIEW)
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PreAuthorize(Permissions.HAS_PRODUCT_VIEW)
    @GetMapping
    public PagedResponse<ProductResponse> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(name = "q", required = false) String keyword,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return productService.search(categoryId, status, keyword, pageable);
    }

    @PreAuthorize(Permissions.HAS_PRODUCT_EDIT)
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PreAuthorize(Permissions.HAS_PRODUCT_EDIT)
    @PatchMapping("/{id}/status")
    public ProductResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return productService.updateStatus(id, request.status());
    }

    @PreAuthorize(Permissions.HAS_PRODUCT_EDIT)
    @PatchMapping("/{id}/stock")
    public ProductResponse updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return productService.updateStock(id, request.quantity());
    }

    @PreAuthorize(Permissions.HAS_PRODUCT_DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
