package com.example.fixaordering.service.validation;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.domain.Region;
import com.example.fixaordering.dto.CreateOrderRequest;
import com.example.fixaordering.exception.AddressNotFoundException;
import com.example.fixaordering.exception.RegionDisabledException;
import com.example.fixaordering.repository.AddressRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(4)
@Component
public class RegionAvailabilityValidationStep implements OrderValidationStep {

    private final AddressRepository addressRepository;

    public RegionAvailabilityValidationStep(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public void validate(CreateOrderRequest request) {
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new AddressNotFoundException(request.addressId()));
        Region region = address.getRegion();
        if (!region.isEnabled()) {
            throw new RegionDisabledException(region.getId());
        }
    }
}