package com.example.fixaordering.service;

import com.example.fixaordering.domain.OrderSequence;
import com.example.fixaordering.repository.OrderSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderSequenceAllocator {

    private final OrderSequenceRepository sequenceRepository;

    public OrderSequenceAllocator(OrderSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long next(String sequenceDate) {
        OrderSequence sequence = sequenceRepository.findBySequenceDateForUpdate(sequenceDate)
                .orElseGet(() -> sequenceRepository.save(new OrderSequence(sequenceDate)));
        return sequence.next();
    }
}