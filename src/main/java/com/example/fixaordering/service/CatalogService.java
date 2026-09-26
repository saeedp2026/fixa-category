package com.example.fixaordering.service;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.domain.Customer;
import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.dto.AddressResponse;
import com.example.fixaordering.dto.CustomerResponse;
import com.example.fixaordering.dto.ServiceCategoryResponse;
import com.example.fixaordering.mapper.AddressMapper;
import com.example.fixaordering.mapper.CustomerMapper;
import com.example.fixaordering.mapper.ServiceCategoryMapper;
import com.example.fixaordering.repository.AddressRepository;
import com.example.fixaordering.repository.CustomerRepository;
import com.example.fixaordering.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final CustomerRepository customerRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final AddressRepository addressRepository;

    public CatalogService(CustomerRepository customerRepository,
                          ServiceCategoryRepository serviceCategoryRepository,
                          AddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomerList() {
        return customerRepository.findAll().stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceCategoryResponse> getServiceCategoryList() {
        return serviceCategoryRepository.findAll().stream()
                .map(ServiceCategoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressList() {
        return addressRepository.findAllWithRegion().stream()
                .map(AddressMapper::toResponse)
                .toList();
    }
}
