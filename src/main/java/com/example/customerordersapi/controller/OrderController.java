package com.example.customerordersapi.controller;

import com.example.customerordersapi.dto.OrderCreateRequest;
import com.example.customerordersapi.dto.OrderResponse;
import com.example.customerordersapi.mapper.OrderMapper;
import com.example.customerordersapi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/customers/{customerId}/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@PathVariable Long customerId, @Valid @RequestBody OrderCreateRequest req) {
        return OrderMapper.toResponse(orderService.createForCustomer(customerId, req));
    }

    @GetMapping
    public Page<OrderResponse> list(
            @PathVariable Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        if (from != null || to != null) {
            return orderService.listForCustomerByDateRange(customerId, from, to, pageable)
                    .map(OrderMapper::toResponse);
        }
        return orderService.listForCustomer(customerId, pageable)
                .map(OrderMapper::toResponse);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOne(@PathVariable Long customerId, @PathVariable Long orderId) {
        return OrderMapper.toResponse(orderService.getOrder(customerId, orderId));
    }

    @PutMapping("/{orderId}")
    public OrderResponse update(@PathVariable Long customerId, @PathVariable Long orderId,
                                @Valid @RequestBody OrderCreateRequest req) {
        return OrderMapper.toResponse(orderService.updateOrder(customerId, orderId, req));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long customerId, @PathVariable Long orderId) {
        orderService.deleteOrder(customerId, orderId);
    }
}
