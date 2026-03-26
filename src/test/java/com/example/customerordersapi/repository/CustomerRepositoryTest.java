package com.example.customerordersapi.repository;

import com.example.customerordersapi.entity.CustomerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void existsByEmail_returnsTrueWhenPresent() {
        CustomerEntity customer = new CustomerEntity("Alice", "alice@example.com");
        customerRepository.save(customer);

        assertTrue(customerRepository.existsByEmail("alice@example.com"));
        assertFalse(customerRepository.existsByEmail("missing@example.com"));
    }
}
