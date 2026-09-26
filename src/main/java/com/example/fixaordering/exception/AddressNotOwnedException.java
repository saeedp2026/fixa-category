
package com.example.fixaordering.exception;

import org.springframework.http.HttpStatus;

public class AddressNotOwnedException extends BusinessException {

    public AddressNotOwnedException(Long addressId, Long customerId) {
        super("ADDRESS_NOT_OWNED_BY_CUSTOMER",
                "Address %d does not belong to customer %d.".formatted(addressId, customerId),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

