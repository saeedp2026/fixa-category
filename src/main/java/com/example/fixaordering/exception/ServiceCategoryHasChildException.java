
package com.example.fixaordering.exception;

import org.springframework.http.HttpStatus;

public class ServiceCategoryHasChildException extends BusinessException {

    public ServiceCategoryHasChildException(Long id) {
        super("SERVICE_CATEGORY_HAS_CHILD",
                "The selected service category (id %d) has child."
                        .formatted(id),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

