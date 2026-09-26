
package com.example.fixaordering.exception;

public class CustomerNotFoundException extends ResourceNotFoundException {

    public CustomerNotFoundException(Long id) {
        super("CUSTOMER_NOT_FOUND", "Customer with id %d was not found.".formatted(id));
    }
}

