package com.example.fixaordering.domain;

public interface OrderStatusHistoryRecorder {

    void record(Order order, OrderStatus previousStatus, OrderStatus newStatus);
}