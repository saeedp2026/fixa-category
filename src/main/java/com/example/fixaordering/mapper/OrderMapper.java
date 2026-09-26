package com.example.fixaordering.mapper;

import com.example.fixaordering.domain.Order;
import com.example.fixaordering.dto.OrderCreatedResponse;
import com.example.fixaordering.dto.OrderStatusHistoryResponse;
import com.example.fixaordering.dto.OrderResponse;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderCreatedResponse toCreatedResponse(Order order) {
        return new OrderCreatedResponse(order.getId(), order.getOrderCode());
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderStatusHistoryResponse> history = order.getStatusHistory().stream()
                .map(OrderStatusHistoryMapper::toResponse)
                .toList();
        return new OrderResponse(order.getId(), order.getOrderCode(), order.getRequestedDate(), order.getStatus(),
                CustomerMapper.toResponse(order.getCustomer()),
                ServiceCategoryMapper.toResponse(order.getServiceCategory()),
                AddressMapper.toResponse(order.getAddress()),
                history);
    }
}
