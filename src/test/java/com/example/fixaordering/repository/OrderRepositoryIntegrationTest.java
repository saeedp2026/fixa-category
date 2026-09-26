package com.example.fixaordering.repository;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.domain.Customer;
import com.example.fixaordering.domain.Order;
import com.example.fixaordering.domain.OrderStatus;
import com.example.fixaordering.domain.Region;
import com.example.fixaordering.domain.ServiceCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class OrderRepositoryIntegrationTest {

    private static final String ORDER_CODE = "260926-00001";

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findByIdWithDetailsLoadsOrderWithAllAssociationsAndHistory() {
        Customer customer = persisted(new Customer("Ali", "Ahmadi", "09123456789", "0023456789"));
        Region region = persisted(new Region("Tehran", true));
        ServiceCategory category = persisted(new ServiceCategory("Boiler Repair", true));
        Address address = persisted(new Address("Home", "Tehran, Valiasr St, No 5", customer, region));

        Order order = Order.register(ORDER_CODE, LocalDate.now(), customer, category, address);
        order.newStatusHistoryEntry(null, OrderStatus.FINAL_ORDER);
        Order saved = orderRepository.saveAndFlush(order);

        Order loaded = orderRepository.findByIdWithDetails(saved.getId()).orElseThrow();

        assertThat(loaded.getStatus()).isEqualTo(OrderStatus.FINAL_ORDER);
        assertThat(loaded.getCustomer().getFirstName()).isEqualTo("Ali");
        assertThat(loaded.getServiceCategory().getName()).isEqualTo("Boiler Repair");
        assertThat(loaded.getAddress().getName()).isEqualTo("Home");
        assertThat(loaded.getAddress().getRegion().getName()).isEqualTo("Tehran");
        assertThat(loaded.getStatusHistory()).hasSize(1);
    }

    @Test
    void duplicateOrderCodeIsRejectedByDatabaseUniqueConstraint() {
        Customer customer = persisted(new Customer("Ali", "Ahmadi", "09123456789", "0023456789"));
        Region region = persisted(new Region("Tehran", true));
        ServiceCategory category = persisted(new ServiceCategory("Boiler Repair", true));
        Address address = persisted(new Address("Home", "Tehran, Valiasr St, No 5", customer, region));

        orderRepository.saveAndFlush(Order.register(ORDER_CODE, LocalDate.now(), customer, category, address));

        assertThatThrownBy(() -> orderRepository
                .saveAndFlush(Order.register(ORDER_CODE, LocalDate.now(), customer, category, address)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private <T> T persisted(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }
}