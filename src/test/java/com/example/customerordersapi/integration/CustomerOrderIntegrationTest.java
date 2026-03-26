package com.example.customerordersapi.integration;

import com.example.customerordersapi.dto.CustomerCreateRequest;
import com.example.customerordersapi.dto.OrderCreateRequest;
import com.example.customerordersapi.repository.CustomerRepository;
import com.example.customerordersapi.repository.OrderRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerOrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void createCustomerThenCreateOrderAndList() throws Exception {
        CustomerCreateRequest customerReq = new CustomerCreateRequest("Alice", "alice@example.com");
        MvcResult createCustomer = mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(customerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> customerBody = objectMapper.readValue(
                createCustomer.getResponse().getContentAsString(), new TypeReference<>() {});
        Long customerId = ((Number) customerBody.get("id")).longValue();

        OrderCreateRequest orderReq = new OrderCreateRequest(LocalDate.of(2024, 3, 15), new BigDecimal("99.99"));
        mockMvc.perform(post("/api/customers/" + customerId + "/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(99.99));

        mockMvc.perform(get("/api/customers/" + customerId + "/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].orderDate").value("2024-03-15"));
    }

    @Test
    void getMissingCustomer_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
