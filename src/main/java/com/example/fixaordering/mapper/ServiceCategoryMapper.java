package com.example.fixaordering.mapper;

import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.dto.ServiceCategoryResponse;

public final class ServiceCategoryMapper {

    private ServiceCategoryMapper() {
    }

    public static ServiceCategoryResponse toResponse(ServiceCategory serviceCategory) {
        return new ServiceCategoryResponse(serviceCategory.getId(), serviceCategory.getName(),
                serviceCategory.isEnabled());
    }
}
