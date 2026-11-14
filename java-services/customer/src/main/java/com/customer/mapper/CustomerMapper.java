package com.customer.mapper;

import com.customer.dto.AddressRequest;
import com.customer.dto.AddressResponse;
import com.customer.dto.CustomerRequest;
import com.customer.dto.CustomerResponse;
import com.customer.dto.CustomerSummaryResponse;
import com.customer.entity.Address;
import com.customer.entity.Customer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        applyToEntity(request, customer);
        return customer;
    }

    public void applyToEntity(CustomerRequest request, Customer customer) {
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setEmail(normalizeEmail(request.email()));
        customer.setPhoneNumber(request.phoneNumber() == null ? null : request.phoneNumber().trim());

        customer.getAddresses().clear();
        if (request.addresses() != null) {
            request.addresses().forEach(address -> customer.addAddress(toAddress(address)));
        }
    }

    public CustomerResponse toResponse(Customer customer) {
        List<AddressResponse> addresses = customer.getAddresses().stream()
                .map(this::toAddressResponse)
                .toList();

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getStatus(),
                addresses,
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public CustomerSummaryResponse toSummaryResponse(Customer customer) {
        return new CustomerSummaryResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getStatus()
        );
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private Address toAddress(AddressRequest request) {
        return Address.builder()
                .type(request.type())
                .street(request.street().trim())
                .city(request.city().trim())
                .state(request.state() == null ? null : request.state().trim())
                .postalCode(request.postalCode().trim())
                .country(request.country().trim())
                .build();
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getType(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}
