package com.customer.dto;

import com.customer.entity.AddressType;

public record AddressResponse(
        Long id,
        AddressType type,
        String street,
        String city,
        String state,
        String postalCode,
        String country
) {
}
