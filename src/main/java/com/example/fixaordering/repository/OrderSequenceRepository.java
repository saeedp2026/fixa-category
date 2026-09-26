package com.example.fixaordering.repository;

import com.example.fixaordering.domain.OrderSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderSequenceRepository extends JpaRepository<OrderSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from OrderSequence s where s.sequenceDate = :sequenceDate")
    Optional<OrderSequence> findBySequenceDateForUpdate(@Param("sequenceDate") String sequenceDate);
}