package com.example.customerordersapi.controller;

import com.example.customerordersapi.dto.CustomerCreateRequest;
import com.example.customerordersapi.dto.CustomerResponse;
import com.example.customerordersapi.mapper.CustomerMapper;
import com.example.customerordersapi.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerCreateRequest req) {
        var saved = customerService.create(req);
        return CustomerMapper.toResponse(saved, 0);
    }

    @GetMapping
    public Page<CustomerResponse> list(Pageable pageable) {
        return customerService.list(pageable)
                .map(c -> CustomerMapper.toResponse(c, customerService.totalOrders(c.getId())));
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        var c = customerService.get(id);
        return CustomerMapper.toResponse(c, customerService.totalOrders(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}
