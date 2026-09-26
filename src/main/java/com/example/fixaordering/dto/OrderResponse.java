package com.example.fixaordering.dto;

import com.example.fixaordering.domain.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderCode,
        LocalDate requestedDate,
        OrderStatus status,
        CustomerResponse customer,
        ServiceCategoryResponse serviceCategory,
        AddressResponse address,
        List<OrderStatusHistoryResponse> statusHistory) {
}
