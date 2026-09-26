package com.example.fixaordering.controller;

import com.example.fixaordering.domain.Address;
import com.example.fixaordering.domain.Customer;
import com.example.fixaordering.domain.Region;
import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.repository.AddressRepository;
import com.example.fixaordering.repository.CustomerRepository;
import com.example.fixaordering.repository.OrderRepository;
import com.example.fixaordering.repository.OrderStatusHistoryRepository;
import com.example.fixaordering.repository.RegionRepository;
import com.example.fixaordering.repository.ServiceCategoryRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ServiceCategoryRepository serviceCategoryRepository;
    @Autowired RegionRepository regionRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired AddressRepository addressRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderStatusHistoryRepository orderStatusHistoryRepository;

    private ServiceCategory category;
    private ServiceCategory disabledCategory;
    private Region region;
    private Customer customer;
    private Address address;
    private Address otherCustomerAddress;
    private Address addressInDisabledRegion;

    @BeforeEach
    void seed() {
        orderStatusHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        regionRepository.deleteAll();
        serviceCategoryRepository.deleteAll();

        category = serviceCategoryRepository.save(new ServiceCategory("Boiler Repair", true));
        disabledCategory = serviceCategoryRepository.save(new ServiceCategory("Window Cleaning", false));
        region = regionRepository.save(new Region("Tehran", true));
        Region disabledRegion = regionRepository.save(new Region("Karaj", false));
        customer = customerRepository.save(new Customer("Ali", "Ahmadi", "09123456789", "0023456789"));
        Customer otherCustomer = customerRepository.save(new Customer("Sara", "Karimi", "09331234567", "0012345678"));
        address = addressRepository.save(new Address("Home", "Tehran, Valiasr St, No 5", customer, region));
        otherCustomerAddress = addressRepository.save(new Address("Office", "Tehran, Enghelab St, No 9", otherCustomer, region));
        addressInDisabledRegion = addressRepository.save(new Address("Home", "Karaj, Azadi St", customer, disabledRegion));
    }

    @Test
    @DisplayName("POST /api/orders returns 201 with id and unique order code")
    void createOrderReturns201() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(category.getId(), customer.getId(), address.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.orderCode").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns full order details with customer, category, address and history")
    void getOrderReturnsFullDetails() throws Exception {
        Number id = createdOrderId();

        mockMvc.perform(get("/api/orders/{id}", id.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.longValue()))
                .andExpect(jsonPath("$.orderCode").isNotEmpty())
                .andExpect(jsonPath("$.requestedDate").isNotEmpty())
                .andExpect(jsonPath("$.status").value("FINAL_ORDER"))
                .andExpect(jsonPath("$.customer.firstName").value("Ali"))
                .andExpect(jsonPath("$.serviceCategory.name").value("Boiler Repair"))
                .andExpect(jsonPath("$.address.name").value("Home"))
                .andExpect(jsonPath("$.address.region.name").value("Tehran"))
                .andExpect(jsonPath("$.statusHistory[0].newStatus").value("FINAL_ORDER"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} for an unknown order returns 404 with business error")
    void getUnknownOrderReturns404() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 987654))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/orders with null fields returns 400 with field level validation errors")
    void createOrderWithNullFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serviceCategoryId": null, "customerId": null, "addressId": null,
                                 "requestedDate": null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.serviceCategoryId").value("must not be null"))
                .andExpect(jsonPath("$.customerId").value("must not be null"))
                .andExpect(jsonPath("$.addressId").value("must not be null"))
                .andExpect(jsonPath("$.requestedDate").value("must not be null"));
    }

    @Test
    @DisplayName("POST /api/orders with a past requested date is rejected")
    void createOrderWithPastDateIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJsonWithDate(category.getId(), customer.getId(), address.getId(),
                                LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.requestedDate").value("must be a future or present date"));
    }

    @Test
    @DisplayName("POST /api/orders for a disabled service category returns business error")
    void createOrderWithDisabledCategoryIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(disabledCategory.getId(), customer.getId(), address.getId())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("SERVICE_CATEGORY_DISABLED"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/orders with an address of another customer returns business error")
    void createOrderWithForeignAddressIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(category.getId(), customer.getId(), otherCustomerAddress.getId())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ADDRESS_NOT_OWNED_BY_CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/orders for an address in a disabled region returns business error")
    void createOrderInDisabledRegionIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(category.getId(), customer.getId(), addressInDisabledRegion.getId())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("REGION_DISABLED"));
    }

    @Test
    @DisplayName("Valid transition FINAL_ORDER -> TECHNICIAN_ACCEPTED updates status and records history")
    void validTransitionSucceeds() throws Exception {
        Number id = createdOrderId();

        mockMvc.perform(post("/api/orders/{id}/status", id.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("TECHNICIAN_ACCEPTED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TECHNICIAN_ACCEPTED"));

        mockMvc.perform(get("/api/orders/{id}", id.longValue()))
                .andExpect(jsonPath("$.statusHistory[1].previousStatus").value("FINAL_ORDER"))
                .andExpect(jsonPath("$.statusHistory[1].newStatus").value("TECHNICIAN_ACCEPTED"));
    }

    @Test
    @DisplayName("Invalid transition FINAL_ORDER -> ORDER_COMPLETED is rejected with 409 and status unchanged")
    void invalidTransitionIsRejected() throws Exception {
        Number id = createdOrderId();

        mockMvc.perform(post("/api/orders/{id}/status", id.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("ORDER_COMPLETED")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(get("/api/orders/{id}", id.longValue()))
                .andExpect(jsonPath("$.status").value("FINAL_ORDER"));
    }

    @Test
    @DisplayName("ORDER_COMPLETED -> FINAL_ORDER is rejected")
    void completedToFinalIsRejected() throws Exception {
        Number id = createdOrderId();
        transition(id, "TECHNICIAN_ACCEPTED");
        transition(id, "ORDER_IN_PROGRESS");
        transition(id, "ORDER_COMPLETED");

        mockMvc.perform(post("/api/orders/{id}/status", id.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("FINAL_ORDER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }

    @Test
    @DisplayName("Cancelled orders are terminal")
    void cancelledIsTerminal() throws Exception {
        Number id = createdOrderId();

        mockMvc.perform(post("/api/orders/{id}/status", id.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("CLIENT_CANCELLED")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{id}/status", id.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson("TECHNICIAN_ACCEPTED")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }

    @Test
    @DisplayName("GET lookup lists return customers, service categories and addresses")
    void lookupListsAreReturned() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ali"))
                .andExpect(jsonPath("$[0].lastName").value("Ahmadi"))
                .andExpect(jsonPath("$[0].mobile").value("09123456789"))
                .andExpect(jsonPath("$[0].nationalCode").value("0023456789"));

        mockMvc.perform(get("/api/service-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Boiler Repair"));

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Home"))
                .andExpect(jsonPath("$[0].region.name").value("Tehran"));
    }

    private Number createdOrderId() throws Exception {
        String created = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(category.getId(), customer.getId(), address.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.parse(created).read("$.id");
    }

    private void transition(Number id, String newStatus) throws Exception {
        mockMvc.perform(post("/api/orders/{id}/status", id.longValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusJson(newStatus)))
                .andExpect(status().isOk());
    }

    private String statusJson(String newStatus) {
        return """
                {"newStatus": "%s"}
                """.formatted(newStatus);
    }

    private String orderJson(Number categoryId, Number customerId, Number addressId) {
        return orderJsonWithDate(categoryId, customerId, addressId, LocalDate.now().plusDays(3));
    }

    private String orderJsonWithDate(Number categoryId, Number customerId, Number addressId, LocalDate date) {
        return """
                {
                  "serviceCategoryId": %d,
                  "customerId": %d,
                  "addressId": %d,
                  "requestedDate": "%s"
                }
                """.formatted(categoryId.longValue(), customerId.longValue(), addressId.longValue(), date);
    }
}