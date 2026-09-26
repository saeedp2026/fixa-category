package com.example.fixaordering.repository;

import com.example.fixaordering.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("""
            select a from Address a
            join fetch a.region
            order by a.id
            """)
    List<Address> findAllWithRegion();
}
