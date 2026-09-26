package com.example.fixaordering.mapper;

import com.example.fixaordering.domain.OrderStatusHistory;
import com.example.fixaordering.dto.OrderStatusHistoryResponse;

public final class OrderStatusHistoryMapper {

    private OrderStatusHistoryMapper() {
    }

    public static OrderStatusHistoryResponse toResponse(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(history.getId(), history.getPreviousStatus(),
                history.getNewStatus(), history.getChangedAt());
    }
}
