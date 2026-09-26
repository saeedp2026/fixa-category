package com.example.fixaordering.service;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.domain.Customer;
import com.example.fixaordering.domain.Order;
import com.example.fixaordering.domain.OrderStatus;
import com.example.fixaordering.domain.Region;
import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.dto.CreateOrderRequest;
import com.example.fixaordering.dto.OrderCreatedResponse;
import com.example.fixaordering.exception.AddressNotOwnedException;
import com.example.fixaordering.exception.CustomerNotFoundException;
import com.example.fixaordering.exception.RegionDisabledException;
import com.example.fixaordering.exception.ServiceCategoryDisabledException;
import com.example.fixaordering.exception.ServiceCategoryNotFoundException;
import com.example.fixaordering.repository.AddressRepository;
import com.example.fixaordering.repository.CustomerRepository;
import com.example.fixaordering.repository.OrderRepository;
import com.example.fixaordering.repository.OrderSequenceRepository;
import com.example.fixaordering.repository.OrderStatusHistoryRepository;
import com.example.fixaordering.repository.RegionRepository;
import com.example.fixaordering.repository.ServiceCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired ServiceCategoryRepository serviceCategoryRepository;
    @Autowired RegionRepository regionRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired AddressRepository addressRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Autowired OrderSequenceRepository orderSequenceRepository;

    private ServiceCategory boiler;
    private ServiceCategory disabledCategory;
    private Region tehran;
    private Region disabledRegion;
    private Customer customer;
    private Address customerAddress;
    private Customer otherCustomer;
    private Address otherCustomerAddress;

    @BeforeEach
    void seed() {
        orderStatusHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        regionRepository.deleteAll();
        serviceCategoryRepository.deleteAll();
        orderSequenceRepository.deleteAll();

        boiler = serviceCategoryRepository.save(new ServiceCategory("Boiler Repair", true));
        disabledCategory = serviceCategoryRepository.save(new ServiceCategory("Window Cleaning", false));
        tehran = regionRepository.save(new Region("Tehran", true));
        disabledRegion = regionRepository.save(new Region("Karaj", false));
        customer = customerRepository.save(new Customer("Ali", "Ahmadi", "09123456789", "0023456789"));
        otherCustomer = customerRepository.save(new Customer("Sara", "Karimi", "09331234567", "0012345678"));
        customerAddress = addressRepository.save(new Address("Home", "Tehran, Valiasr St, No 5", customer, tehran));
        otherCustomerAddress = addressRepository.save(new Address("Office", "Tehran, Enghelab St, No 9", otherCustomer, tehran));
    }

    @Test
    @DisplayName("Valid order gets created with unique code, FINAL_ORDER status, and initial history record")
    void createOrder_persistsOrderWithFinalStatusAndInitialHistory() {
        CreateOrderRequest request = validRequest();

        OrderCreatedResponse created = orderService.createOrder(request);
        Optional<Order> savedOptional = orderRepository.findById(created.id());

        assertThat(savedOptional).isPresent();
        Order order = savedOptional.get();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.FINAL_ORDER);
        assertThat(order.getOrderCode()).isEqualTo(created.orderCode());
        assertThat(order.getRequestedDate()).isEqualTo(request.requestedDate());
        assertThat(order.getOrderCode()).matches("\\d{6}-\\d{5}");
        assertThat(orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(created.id())).hasSize(1);
    }

    @Test
    @DisplayName("Disabled service category is rejected and nothing is persisted")
    void createOrder_rejectsDisabledServiceCategory() {
        long ordersBefore = orderRepository.count();

        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(
                disabledCategory.getId(), customer.getId(), customerAddress.getId(),
                LocalDate.now().plusDays(3))))
                .isInstanceOf(ServiceCategoryDisabledException.class)
                .hasMessageContaining("disabled");

        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
    }

    @Test
    @DisplayName("Unknown service category is rejected")
    void createOrder_rejectsUnknownServiceCategory() {
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(
                987654L, customer.getId(), customerAddress.getId(), LocalDate.now().plusDays(3))))
                .isInstanceOf(ServiceCategoryNotFoundException.class)
                .hasMessageContaining("987654");
    }

    @Test
    @DisplayName("Unknown customer is rejected")
    void createOrder_rejectsUnknownCustomer() {
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(
                boiler.getId(), 987654L, customerAddress.getId(), LocalDate.now().plusDays(3))))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("987654");
    }

    @Test
    @DisplayName("Address belonging to another customer is rejected")
    void createOrder_rejectsAddressOfOtherCustomer() {
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(
                boiler.getId(), customer.getId(), otherCustomerAddress.getId(), LocalDate.now().plusDays(3))))
                .isInstanceOf(AddressNotOwnedException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    @DisplayName("Disabled region is rejected and nothing is persisted")
    void createOrder_rejectsDisabledRegion() {
        Customer another = customerRepository.save(new Customer("Reza", "Tabatabai", "09987654321", "00987654321"));
        Address addressInDisabledRegion = addressRepository.save(new Address("Home", "Karaj, Azadi St", another, disabledRegion));
        long ordersBefore = orderRepository.count();

        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(
                boiler.getId(), another.getId(), addressInDisabledRegion.getId(), LocalDate.now().plusDays(3))))
                .isInstanceOf(RegionDisabledException.class)
                .hasMessageContaining("disabled");

        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
    }

    private CreateOrderRequest validRequest() {
        return new CreateOrderRequest(boiler.getId(), customer.getId(), customerAddress.getId(),
                LocalDate.now().plusDays(3));
    }
}
