package com.example.fixaordering.service;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.domain.OrderStatusHistoryRecorder;
import com.example.fixaordering.domain.Customer;
import com.example.fixaordering.domain.Region;
import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.dto.CreateOrderRequest;
import com.example.fixaordering.repository.AddressRepository;
import com.example.fixaordering.repository.CustomerRepository;
import com.example.fixaordering.repository.OrderRepository;
import com.example.fixaordering.repository.OrderStatusHistoryRepository;
import com.example.fixaordering.repository.RegionRepository;
import com.example.fixaordering.repository.ServiceCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderTransactionRollbackTest {

    @Autowired OrderService orderService;
    @Autowired ServiceCategoryRepository serviceCategoryRepository;
    @Autowired RegionRepository regionRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired AddressRepository addressRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderStatusHistoryRepository orderStatusHistoryRepository;

    private ServiceCategory category;
    private Customer customer;
    private Address address;

    @TestConfiguration
    static class ForcedFailureConfig {

        @Bean
        @Primary
        OrderStatusHistoryRecorder failingRecorder() {
            return (order, previousStatus, newStatus) -> {
                throw new IllegalStateException("Simulated persistence failure while recording status history");
            };
        }
    }

    @BeforeEach
    void seed() {
        orderStatusHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        regionRepository.deleteAll();
        serviceCategoryRepository.deleteAll();

        category = serviceCategoryRepository.save(new ServiceCategory("Boiler Repair", true));
        Region tehran = regionRepository.save(new Region("Tehran", true));
        customer = customerRepository.save(new Customer("Ali", "Ahmadi", "09123456789", "0023456789"));
        address = addressRepository.save(new Address("Home", "Tehran, Valiasr St, No 5", customer, tehran));
    }

    @Test
    void orderCreationFailsAfterOrderInsert_orderAndHistoryAreNotPersisted() {
        long ordersBefore = orderRepository.count();
        long historyBefore = orderStatusHistoryRepository.count();

        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(
                category.getId(), customer.getId(), address.getId(), LocalDate.now().plusDays(3))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Simulated persistence failure");

        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
        assertThat(orderStatusHistoryRepository.count()).isEqualTo(historyBefore);
    }
}