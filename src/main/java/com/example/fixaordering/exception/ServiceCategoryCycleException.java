package com.example.fixaordering.exception;

import org.springframework.http.HttpStatus;

public class ServiceCategoryCycleException extends BusinessException {

    public ServiceCategoryCycleException(Long id, String name) {
        super("SERVICE_CATEGORY_CYCLE",
                "Saving service category '%s' (id %s) would create a cycle in the service category hierarchy."
                        .formatted(name, id == null ? "new" : id),
                HttpStatus.CONFLICT);
    }
}
