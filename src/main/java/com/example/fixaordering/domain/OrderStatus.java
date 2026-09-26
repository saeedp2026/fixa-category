package com.example.fixaordering.domain;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {

    FINAL_ORDER,
    TECHNICIAN_ACCEPTED,
    ORDER_IN_PROGRESS,
    ORDER_COMPLETED,
    CLIENT_CANCELLED,
    ADMIN_CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            FINAL_ORDER, Set.of(TECHNICIAN_ACCEPTED, CLIENT_CANCELLED, ADMIN_CANCELLED),
            TECHNICIAN_ACCEPTED, Set.of(ORDER_IN_PROGRESS),
            ORDER_IN_PROGRESS, Set.of(ORDER_COMPLETED),
            ORDER_COMPLETED, Set.of(),
            CLIENT_CANCELLED, Set.of(),
            ADMIN_CANCELLED, Set.of());

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }
}