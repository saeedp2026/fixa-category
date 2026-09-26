package com.example.fixaordering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_sequences")
public class OrderSequence extends BaseEntity {

    @Column(name = "sequence_date", nullable = false, unique = true, length = 6)
    private String sequenceDate;

    @Column(name = "last_value", nullable = false)
    private long lastValue;

    protected OrderSequence() {
    }

    public OrderSequence(String sequenceDate) {
        this.sequenceDate = sequenceDate;
        this.lastValue = 0;
    }

    public String getSequenceDate() {
        return sequenceDate;
    }

    public long next() {
        this.lastValue++;
        return this.lastValue;
    }
}