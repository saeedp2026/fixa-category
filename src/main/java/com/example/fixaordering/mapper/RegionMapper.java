package com.example.fixaordering.mapper;

import com.example.fixaordering.domain.Region;
import com.example.fixaordering.dto.RegionResponse;

public final class RegionMapper {

    private RegionMapper() {
    }

    public static RegionResponse toResponse(Region region) {
        return new RegionResponse(region.getId(), region.getName(), region.isEnabled());
    }
}
