package com.example.fixaordering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String details;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    protected Address() {
    }

    public Address(String name, String details, Customer customer, Region region) {
        this.name = name;
        this.details = details;
        this.customer = customer;
        this.region = region;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Region getRegion() {
        return region;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }
}