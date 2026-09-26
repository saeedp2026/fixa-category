package com.example.fixaordering.service.validation;

import com.example.fixaordering.dto.CreateOrderRequest;

public interface OrderValidationStep {

    void validate(CreateOrderRequest request);
}
