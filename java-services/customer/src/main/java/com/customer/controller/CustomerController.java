package com.customer.controller;

import com.customer.dto.CustomerRequest;
import com.customer.dto.CustomerResponse;
import com.customer.dto.CustomerSummaryResponse;
import com.customer.dto.PagedResponse;
import com.customer.dto.StatusUpdateRequest;
import com.customer.security.Permissions;
import com.customer.service.CustomerService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PreAuthorize(Permissions.HAS_CUSTOMER_ADD)
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PreAuthorize(Permissions.HAS_CUSTOMER_VIEW)
    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return customerService.getById(id);
    }

    @PreAuthorize(Permissions.HAS_CUSTOMER_VIEW)
    @GetMapping
    public PagedResponse<CustomerSummaryResponse> getAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return customerService.getAll(pageable);
    }

    @PreAuthorize(Permissions.HAS_CUSTOMER_EDIT)
    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @PreAuthorize(Permissions.HAS_CUSTOMER_EDIT)
    @PatchMapping("/{id}/status")
    public CustomerResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return customerService.updateStatus(id, request.status());
    }

    @PreAuthorize(Permissions.HAS_CUSTOMER_DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
