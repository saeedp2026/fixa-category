package com.example.fixaordering.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateOrderRequest(
        @NotNull Long serviceCategoryId,
        @NotNull Long customerId,
        @NotNull Long addressId,
        @NotNull @FutureOrPresent(message = "must be a future or present date") LocalDate requestedDate) {
}
