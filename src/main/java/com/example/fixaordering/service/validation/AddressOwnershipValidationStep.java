package com.example.fixaordering.service.validation;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.dto.CreateOrderRequest;
import com.example.fixaordering.exception.AddressNotFoundException;
import com.example.fixaordering.exception.AddressNotOwnedException;
import com.example.fixaordering.repository.AddressRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(3)
@Component
public class AddressOwnershipValidationStep implements OrderValidationStep {

    private final AddressRepository addressRepository;

    public AddressOwnershipValidationStep(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public void validate(CreateOrderRequest request) {
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new AddressNotFoundException(request.addressId()));
        if (!address.getCustomer().getId().equals(request.customerId())) {
            throw new AddressNotOwnedException(request.addressId(), request.customerId());
        }
    }
}