package com.example.fixaordering.service;

import com.example.fixaordering.dto.AddressResponse;
import com.example.fixaordering.dto.CustomerResponse;
import com.example.fixaordering.dto.RegionResponse;
import com.example.fixaordering.dto.ServiceCategoryResponse;
import com.example.fixaordering.mapper.AddressMapper;
import com.example.fixaordering.mapper.CustomerMapper;
import com.example.fixaordering.mapper.RegionMapper;
import com.example.fixaordering.mapper.ServiceCategoryMapper;
import com.example.fixaordering.repository.AddressRepository;
import com.example.fixaordering.repository.CustomerRepository;
import com.example.fixaordering.repository.RegionRepository;
import com.example.fixaordering.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final CustomerRepository customerRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final RegionRepository regionRepository;
    private final AddressRepository addressRepository;

    public CatalogService(CustomerRepository customerRepository,
                          ServiceCategoryRepository serviceCategoryRepository,
                          RegionRepository regionRepository,
                          AddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.regionRepository = regionRepository;
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
    public List<RegionResponse> getRegionList() {
        return regionRepository.findAll().stream()
                .map(RegionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressList() {
        return addressRepository.findAllWithRegion().stream()
                .map(AddressMapper::toResponse)
                .toList();
    }
}
