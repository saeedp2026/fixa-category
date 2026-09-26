package com.example.fixaordering.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class OrderCodeGenerator {

    private static final DateTimeFormatter DATE_PART = DateTimeFormatter.ofPattern("yyMMdd");

    private final OrderSequenceAllocator sequenceAllocator;
    private final Clock clock;

    public OrderCodeGenerator(OrderSequenceAllocator sequenceAllocator, Clock clock) {
        this.sequenceAllocator = sequenceAllocator;
        this.clock = clock;
    }

    public String generate() {
        String sequenceDate = LocalDate.now(clock).format(DATE_PART);
        long value;
        try {
            value = sequenceAllocator.next(sequenceDate);
        } catch (DataIntegrityViolationException ex) {
            // Rare race: two first orders of a new day both insert the daily sequence row.
            // The failed transaction is rolled back completely, so a retry gets a valid state.
            value = sequenceAllocator.next(sequenceDate);
        }
        return sequenceDate + "-" + "%05d".formatted(value);
    }
}