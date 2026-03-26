package com.example.customerordersapi.controller;

import com.example.customerordersapi.dto.OrderCreateRequest;
import com.example.customerordersapi.entity.OrderEntity;
import com.example.customerordersapi.exception.BadRequestException;
import com.example.customerordersapi.exception.GlobalExceptionHandler;
import com.example.customerordersapi.service.OrderService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void create_returnsCreatedOrder() throws Exception {
        OrderCreateRequest req = new OrderCreateRequest(LocalDate.of(2024, 1, 10), new BigDecimal("12.50"));
        OrderEntity saved = new OrderEntity(LocalDate.of(2024, 1, 10), new BigDecimal("12.50"));
        ReflectionTestUtils.setField(saved, "id", 10L);

        when(orderService.createForCustomer(eq(1L), any(OrderCreateRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/customers/1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.amount").value(12.50));
    }

    @Test
    void list_withoutDateRange_usesStandardListing() throws Exception {
        OrderEntity o1 = new OrderEntity(LocalDate.of(2024, 1, 10), new BigDecimal("12.50"));
        ReflectionTestUtils.setField(o1, "id", 10L);
        when(orderService.listForCustomer(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(o1), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/customers/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(10));
    }

    @Test
    void list_withDateRange_usesDateRangeListing() throws Exception {
        OrderEntity o1 = new OrderEntity(LocalDate.of(2024, 1, 10), new BigDecimal("12.50"));
        ReflectionTestUtils.setField(o1, "id", 10L);
        when(orderService.listForCustomerByDateRange(
                eq(1L), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(o1), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/customers/1/orders")
                        .queryParam("from", "2024-01-01")
                        .queryParam("to", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(10));
    }

    @Test
    void list_withInvalidDateRange_returnsBadRequest() throws Exception {
        doThrow(new BadRequestException("Both 'from' and 'to' are required (yyyy-MM-dd)."))
                .when(orderService)
                .listForCustomerByDateRange(eq(1L), any(), any(), any(Pageable.class));

        mockMvc.perform(get("/api/customers/1/orders")
                        .queryParam("from", "2024-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void getOne_returnsOrder() throws Exception {
        OrderEntity o1 = new OrderEntity(LocalDate.of(2024, 1, 10), new BigDecimal("12.50"));
        ReflectionTestUtils.setField(o1, "id", 10L);
        when(orderService.getOrder(1L, 10L)).thenReturn(o1);

        mockMvc.perform(get("/api/customers/1/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.orderDate").value("2024-01-10"));
    }

    @Test
    void update_returnsUpdatedOrder() throws Exception {
        OrderCreateRequest req = new OrderCreateRequest(LocalDate.of(2024, 2, 2), new BigDecimal("20.00"));
        OrderEntity updated = new OrderEntity(LocalDate.of(2024, 2, 2), new BigDecimal("20.00"));
        ReflectionTestUtils.setField(updated, "id", 10L);
        when(orderService.updateOrder(eq(1L), eq(10L), any(OrderCreateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/customers/1/orders/10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.amount").value(20.00));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/customers/1/orders/10"))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(1L, 10L);
    }
}
