
package com.example.fixaordering.exception;

public class AddressNotFoundException extends ResourceNotFoundException {

    public AddressNotFoundException(Long id) {
        super("ADDRESS_NOT_FOUND", "Address with id %d was not found.".formatted(id));
    }
}

