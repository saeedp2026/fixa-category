package com.example.fixaordering.controller;

import com.example.fixaordering.dto.AddressResponse;
import com.example.fixaordering.dto.CustomerResponse;
import com.example.fixaordering.dto.ServiceCategoryResponse;
import com.example.fixaordering.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/customers")
    public List<CustomerResponse> getCustomerList() {
        return catalogService.getCustomerList();
    }

    @GetMapping("/api/service-categories")
    public List<ServiceCategoryResponse> getServiceCategoryList() {
        return catalogService.getServiceCategoryList();
    }

    @GetMapping("/api/addresses")
    public List<AddressResponse> getAddressList() {
        return catalogService.getAddressList();
    }
}
