package com.example.fixaordering.dto;

import com.example.fixaordering.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeOrderStatusRequest(@NotNull OrderStatus newStatus) {
}
