package com.example.fixaordering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 20)
    private String mobile;

    @Column(name = "national_code", nullable = false, length = 20)
    private String nationalCode;

    protected Customer() {
    }

    public Customer(String firstName, String lastName, String mobile, String nationalCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.nationalCode = nationalCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public String getNationalCode() {
        return nationalCode;
    }
}