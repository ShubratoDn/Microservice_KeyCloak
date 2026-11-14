package com.customer.service.impl;

import com.customer.dto.CustomerRequest;
import com.customer.dto.CustomerResponse;
import com.customer.dto.CustomerSummaryResponse;
import com.customer.dto.PagedResponse;
import com.customer.entity.Customer;
import com.customer.entity.CustomerStatus;
import com.customer.exception.DuplicateResourceException;
import com.customer.exception.ResourceNotFoundException;
import com.customer.mapper.CustomerMapper;
import com.customer.repository.CustomerRepository;
import com.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private static final String CUSTOMER = "Customer";

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String email = customerMapper.normalizeEmail(request.email());
        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A customer with email '%s' already exists".formatted(email));
        }

        Customer saved = customerRepository.save(customerMapper.toEntity(request));
        log.info("Created customer with id {}", saved.getId());
        return customerMapper.toResponse(saved);
    }

    @Override
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(findCustomer(id));
    }

    @Override
    public PagedResponse<CustomerSummaryResponse> getAll(Pageable pageable) {
        return PagedResponse.from(
                customerRepository.findAll(pageable).map(customerMapper::toSummaryResponse));
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findCustomer(id);

        String email = customerMapper.normalizeEmail(request.email());
        if (customerRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("A customer with email '%s' already exists".formatted(email));
        }

        customerMapper.applyToEntity(request, customer);
        log.info("Updated customer with id {}", id);
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateStatus(Long id, CustomerStatus status) {
        Customer customer = findCustomer(id);
        customer.setStatus(status);
        log.info("Changed status of customer {} to {}", id, status);
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        customerRepository.delete(findCustomer(id));
        log.info("Deleted customer with id {}", id);
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER, id));
    }
}
