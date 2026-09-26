package com.example.fixaordering.mapper;

import com.example.fixaordering.domain.Customer;
import com.example.fixaordering.dto.CustomerResponse;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getFirstName(), customer.getLastName(),
                customer.getMobile(), customer.getNationalCode());
    }
}
