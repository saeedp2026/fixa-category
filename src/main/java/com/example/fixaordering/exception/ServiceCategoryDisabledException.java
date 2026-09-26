
package com.example.fixaordering.exception;

import org.springframework.http.HttpStatus;

public class ServiceCategoryDisabledException extends BusinessException {

    public ServiceCategoryDisabledException(Long id) {
        super("SERVICE_CATEGORY_DISABLED",
                "The selected service category (id %d) is disabled.".formatted(id),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

