
package com.example.fixaordering.exception;

public class ServiceCategoryNotFoundException extends ResourceNotFoundException {

    public ServiceCategoryNotFoundException(Long id) {
        super("SERVICE_CATEGORY_NOT_FOUND", "Service category with id %d was not found.".formatted(id));
    }
}

