package com.example.fixaordering.controller;

import com.example.fixaordering.dto.ChangeOrderStatusRequest;
import com.example.fixaordering.dto.CreateOrderRequest;
import com.example.fixaordering.dto.OrderCreatedResponse;
import com.example.fixaordering.dto.OrderResponse;
import com.example.fixaordering.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderCreatedResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderCreatedResponse created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/{id}/status")
    public OrderResponse changeStatus(@PathVariable Long id,
                                      @Valid @RequestBody ChangeOrderStatusRequest request) {
        return orderService.changeStatus(id, request);
    }
}