package com.example.fixaordering.repository;

import com.example.fixaordering.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            select o from Order o
            join fetch o.customer
            join fetch o.serviceCategory
            join fetch o.address
            join fetch o.address.region
            where o.id = :id
            """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}