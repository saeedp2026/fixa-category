package com.example.fixaordering.domain;

import com.example.fixaordering.exception.InvalidOrderStatusTransitionException;
import org.springframework.stereotype.Service;

@Service
public class OrderStateMachine {

    private final OrderStatusHistoryRecorder historyRecorder;

    public OrderStateMachine(OrderStatusHistoryRecorder historyRecorder) {
        this.historyRecorder = historyRecorder;
    }

    public void changeStatus(Order order, OrderStatus newStatus) {
        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusTransitionException(current, newStatus);
        }
        order.applyTransition(newStatus);
        historyRecorder.record(order, current, newStatus);
    }

    public void recordInitialStatus(Order order) {
        historyRecorder.record(order, null, order.getStatus());
    }
}