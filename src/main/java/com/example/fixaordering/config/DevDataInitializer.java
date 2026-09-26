package com.example.fixaordering.config;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.domain.Customer;
import com.example.fixaordering.domain.Region;
import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.repository.AddressRepository;
import com.example.fixaordering.repository.CustomerRepository;
import com.example.fixaordering.repository.RegionRepository;
import com.example.fixaordering.repository.ServiceCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final RegionRepository regionRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public DevDataInitializer(ServiceCategoryRepository serviceCategoryRepository,
                              RegionRepository regionRepository,
                              CustomerRepository customerRepository,
                              AddressRepository addressRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.regionRepository = regionRepository;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Region tehran = regionRepository.save(new Region("Tehran", true));
        Region karaj = regionRepository.save(new Region("Karaj", false));
        Region esf = regionRepository.save(new Region("esf", true));

        serviceCategoryRepository.save(new ServiceCategory("Boiler Repair", true));
        serviceCategoryRepository.save(new ServiceCategory("Air Conditioner Service", true));
        serviceCategoryRepository.save(new ServiceCategory("Window Cleaning", false));

        Customer customer = customerRepository.save(new Customer("Ali", "Ahmadi", "09123456789", "0023456789"));
        addressRepository.save(new Address("Work", "Tehran, Valiasr St, No 5", customer, tehran));
        addressRepository.save(new Address("Home", "Karaj, Velayat St, No 2", customer, karaj));
        Customer customer2 = customerRepository.save(new Customer("Sara", "Karimi", "09331234567", "0012345678"));
        addressRepository.save(new Address("Home", "Esfahan, Bozorgmehr St, No 1", customer2, esf));

    }
}