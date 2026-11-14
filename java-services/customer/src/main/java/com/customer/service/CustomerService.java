package com.customer.service;

import com.customer.dto.CustomerRequest;
import com.customer.dto.CustomerResponse;
import com.customer.dto.CustomerSummaryResponse;
import com.customer.dto.PagedResponse;
import com.customer.entity.CustomerStatus;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse getById(Long id);

    PagedResponse<CustomerSummaryResponse> getAll(Pageable pageable);

    CustomerResponse update(Long id, CustomerRequest request);

    CustomerResponse updateStatus(Long id, CustomerStatus status);

    void delete(Long id);
}
