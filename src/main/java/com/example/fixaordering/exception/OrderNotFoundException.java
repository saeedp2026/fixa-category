
package com.example.fixaordering.exception;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {
        super("ORDER_NOT_FOUND", "Order with id %d was not found.".formatted(id));
    }
}

