package com.example.fixaordering.service.validation;

import com.example.fixaordering.dto.CreateOrderRequest;
import com.example.fixaordering.exception.CustomerNotFoundException;
import com.example.fixaordering.repository.CustomerRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class CustomerValidationStep implements OrderValidationStep {

    private final CustomerRepository customerRepository;

    public CustomerValidationStep(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void validate(CreateOrderRequest request) {
        customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));
    }
}