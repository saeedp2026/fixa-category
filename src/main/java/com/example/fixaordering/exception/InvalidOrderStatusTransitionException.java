
package com.example.fixaordering.exception;

import com.example.fixaordering.domain.OrderStatus;
import org.springframework.http.HttpStatus;

public class InvalidOrderStatusTransitionException extends BusinessException {

    public InvalidOrderStatusTransitionException(OrderStatus current, OrderStatus target) {
        super("INVALID_STATUS_TRANSITION",
                "Transition from %s to %s is not allowed.".formatted(current, target),
                HttpStatus.CONFLICT);
    }
}

