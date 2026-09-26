package com.example.fixaordering.mapper;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.dto.AddressResponse;
import com.example.fixaordering.dto.RegionResponse;

public final class AddressMapper {

    private AddressMapper() {
    }

    public static AddressResponse toResponse(Address address) {
        RegionResponse region = address.getRegion() == null ? null : RegionMapper.toResponse(address.getRegion());
        return new AddressResponse(address.getId(), address.getName(), address.getDetails(), region);
    }
}
