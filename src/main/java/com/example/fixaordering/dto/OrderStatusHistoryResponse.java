package com.example.fixaordering.dto;

import com.example.fixaordering.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(Long id, OrderStatus previousStatus, OrderStatus newStatus, LocalDateTime changedAt) {
}
