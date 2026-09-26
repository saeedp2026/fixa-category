package com.example.fixaordering.domain;

import com.example.fixaordering.repository.OrderStatusHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class RepositoryOrderStatusHistoryRecorder implements OrderStatusHistoryRecorder {

    private final OrderStatusHistoryRepository historyRepository;

    public RepositoryOrderStatusHistoryRecorder(OrderStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public void record(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        historyRepository.save(order.newStatusHistoryEntry(previousStatus, newStatus));
    }
}