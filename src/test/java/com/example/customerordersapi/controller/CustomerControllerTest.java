package com.example.customerordersapi.controller;

import com.example.customerordersapi.dto.CustomerCreateRequest;
import com.example.customerordersapi.entity.CustomerEntity;
import com.example.customerordersapi.exception.GlobalExceptionHandler;
import com.example.customerordersapi.exception.NotFoundException;
import com.example.customerordersapi.service.CustomerService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void create_returnsCreatedCustomer() throws Exception {
        CustomerCreateRequest req = new CustomerCreateRequest("Alice", "alice@example.com");
        CustomerEntity saved = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(saved, "id", 1L);
        when(customerService.create(any(CustomerCreateRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.totalOrders").value(0));
    }

    @Test
    void list_returnsPagedCustomers() throws Exception {
        CustomerEntity c1 = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(c1, "id", 1L);
        CustomerEntity c2 = new CustomerEntity("Bob", "bob@example.com");
        ReflectionTestUtils.setField(c2, "id", 2L);

        when(customerService.list(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 20), 2));
        when(customerService.totalOrders(1L)).thenReturn(2L);
        when(customerService.totalOrders(2L)).thenReturn(0L);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].totalOrders").value(2))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].totalOrders").value(0))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void get_returnsCustomer() throws Exception {
        CustomerEntity c1 = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(c1, "id", 1L);
        when(customerService.get(1L)).thenReturn(c1);
        when(customerService.totalOrders(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalOrders").value(5));
    }

    @Test
    void get_returnsNotFoundWhenMissing() throws Exception {
        doThrow(new NotFoundException("Customer not found: 99")).when(customerService).get(99L);

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).delete(1L);
    }

    @Test
    void create_returnsValidationErrors() throws Exception {
        CustomerCreateRequest req = new CustomerCreateRequest("", "not-an-email");

        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").value("name is required"))
                .andExpect(jsonPath("$.validationErrors.email").value("email must be valid"));
    }
}
