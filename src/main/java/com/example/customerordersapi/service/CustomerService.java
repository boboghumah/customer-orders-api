package com.example.customerordersapi.service;


import com.example.customerordersapi.dto.CustomerCreateRequest;
import com.example.customerordersapi.entity.CustomerEntity;
import com.example.customerordersapi.exception.BadRequestException;
import com.example.customerordersapi.exception.NotFoundException;
import com.example.customerordersapi.repository.CustomerRepository;
import com.example.customerordersapi.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final OrderRepository orderRepo;

    public CustomerService(CustomerRepository customerRepo, OrderRepository orderRepo) {
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
    }

    public CustomerEntity create(CustomerCreateRequest req) {
        if (customerRepo.existsByEmail(req.email())) {
            throw new BadRequestException("Email already exists: " + req.email());
        }
        return customerRepo.save(new CustomerEntity(req.name(), req.email()));
    }

    public Page<CustomerEntity> list(Pageable pageable) {
        return customerRepo.findAll(pageable);
    }

    public CustomerEntity get(Long id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }

    public long totalOrders(Long customerId) {
        return orderRepo.countByCustomerId(customerId);
    }

    public void delete(Long id) {
        CustomerEntity c = get(id);
        customerRepo.delete(c); // cascade deletes orders
    }
}
