package com.example.fixaordering.service;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.domain.Customer;
import com.example.fixaordering.domain.Order;
import com.example.fixaordering.domain.OrderStateMachine;
import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.dto.ChangeOrderStatusRequest;
import com.example.fixaordering.dto.CreateOrderRequest;
import com.example.fixaordering.dto.OrderCreatedResponse;
import com.example.fixaordering.dto.OrderResponse;
import com.example.fixaordering.exception.AddressNotFoundException;
import com.example.fixaordering.exception.CustomerNotFoundException;
import com.example.fixaordering.exception.OrderNotFoundException;
import com.example.fixaordering.exception.ServiceCategoryNotFoundException;
import com.example.fixaordering.mapper.OrderMapper;
import com.example.fixaordering.repository.AddressRepository;
import com.example.fixaordering.repository.CustomerRepository;
import com.example.fixaordering.repository.OrderRepository;
import com.example.fixaordering.repository.ServiceCategoryRepository;
import com.example.fixaordering.service.validation.OrderValidationStep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final List<OrderValidationStep> validationSteps;
    private final CustomerRepository customerRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderCodeGenerator orderCodeGenerator;
    private final OrderStateMachine orderStateMachine;

    public OrderService(List<OrderValidationStep> validationSteps,
                        CustomerRepository customerRepository,
                        ServiceCategoryRepository serviceCategoryRepository,
                        AddressRepository addressRepository,
                        OrderRepository orderRepository,
                        OrderCodeGenerator orderCodeGenerator,
                        OrderStateMachine orderStateMachine) {
        this.validationSteps = validationSteps;
        this.customerRepository = customerRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.orderCodeGenerator = orderCodeGenerator;
        this.orderStateMachine = orderStateMachine;
    }

    @Transactional
    public OrderCreatedResponse createOrder(CreateOrderRequest request) {
        validationSteps.forEach(step -> step.validate(request));

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));
        ServiceCategory serviceCategory = serviceCategoryRepository.findById(request.serviceCategoryId())
                .orElseThrow(() -> new ServiceCategoryNotFoundException(request.serviceCategoryId()));
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new AddressNotFoundException(request.addressId()));

        String orderCode = orderCodeGenerator.generate();

        Order order = orderRepository.save(
                Order.register(orderCode, request.requestedDate(), customer, serviceCategory, address));
        orderStateMachine.recordInitialStatus(order);

        return OrderMapper.toCreatedResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse changeStatus(Long id, ChangeOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        orderStateMachine.changeStatus(order, request.newStatus());
        return OrderMapper.toResponse(order);
    }
}