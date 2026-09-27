package com.example.fixaordering.service.validation;

import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.dto.CreateOrderRequest;
import com.example.fixaordering.exception.ServiceCategoryDisabledException;
import com.example.fixaordering.exception.ServiceCategoryHasChildException;
import com.example.fixaordering.exception.ServiceCategoryNotFoundException;
import com.example.fixaordering.repository.ServiceCategoryRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class ServiceCategoryValidationStep implements OrderValidationStep {

    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategoryValidationStep(ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    @Override
    public void validate(CreateOrderRequest request) {
        if (serviceCategoryRepository.existsByParentId(request.serviceCategoryId())) {
            throw new ServiceCategoryHasChildException(request.serviceCategoryId());
        }
        ServiceCategory category = serviceCategoryRepository.findById(request.serviceCategoryId())
                .orElseThrow(() -> new ServiceCategoryNotFoundException(request.serviceCategoryId()));
        if (serviceCategoryRepository.countDisabledInAncestry(category.getId()) > 0) {
            throw new ServiceCategoryDisabledException(request.serviceCategoryId());
        }
    }
}