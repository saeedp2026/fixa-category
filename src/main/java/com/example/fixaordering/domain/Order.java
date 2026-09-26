package com.example.fixaordering.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_orders_order_code", columnNames = "order_code"))
public class Order extends BaseEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 20)
    private String orderCode;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @OrderBy("changedAt ASC, id ASC")
    private final List<OrderStatusHistory> statusHistory = new ArrayList<>();

    protected Order() {
    }

    private Order(String orderCode, LocalDate requestedDate, Customer customer,
                  ServiceCategory serviceCategory, Address address) {
        this.orderCode = orderCode;
        this.requestedDate = requestedDate;
        this.customer = customer;
        this.serviceCategory = serviceCategory;
        this.address = address;
        this.status = OrderStatus.FINAL_ORDER;
    }

    public static Order register(String orderCode, LocalDate requestedDate, Customer customer,
                                 ServiceCategory serviceCategory, Address address) {
        return new Order(orderCode, requestedDate, customer, serviceCategory, address);
    }

    public String getOrderCode() {
        return orderCode;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public ServiceCategory getServiceCategory() {
        return serviceCategory;
    }

    public Address getAddress() {
        return address;
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public OrderStatusHistory newStatusHistoryEntry(OrderStatus previousStatus, OrderStatus newStatus) {
        OrderStatusHistory entry = new OrderStatusHistory(this, previousStatus, newStatus, LocalDateTime.now());
        this.statusHistory.add(entry);
        return entry;
    }

    OrderStatus applyTransition(OrderStatus newStatus) {
        OrderStatus previous = this.status;
        this.status = newStatus;
        return previous;
    }
}