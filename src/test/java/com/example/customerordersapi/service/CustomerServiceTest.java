package com.example.customerordersapi.service;

import com.example.customerordersapi.dto.CustomerCreateRequest;
import com.example.customerordersapi.entity.CustomerEntity;
import com.example.customerordersapi.exception.BadRequestException;
import com.example.customerordersapi.exception.NotFoundException;
import com.example.customerordersapi.repository.CustomerRepository;
import com.example.customerordersapi.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void create_savesCustomerWhenEmailUnique() {
        CustomerCreateRequest req = new CustomerCreateRequest("Alice", "alice@example.com");
        when(customerRepository.existsByEmail(req.email())).thenReturn(false);
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        CustomerEntity saved = customerService.create(req);

        assertNotNull(saved.getId());
        assertEquals("Alice", saved.getName());
        assertEquals("alice@example.com", saved.getEmail());
        verify(customerRepository).existsByEmail(req.email());
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    void create_throwsWhenEmailExists() {
        CustomerCreateRequest req = new CustomerCreateRequest("Alice", "alice@example.com");
        when(customerRepository.existsByEmail(req.email())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> customerService.create(req));
        verify(customerRepository).existsByEmail(req.email());
    }

    @Test
    void list_returnsPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        CustomerEntity c1 = new CustomerEntity("Alice", "alice@example.com");
        Page<CustomerEntity> page = new PageImpl<>(List.of(c1), pageable, 1);
        when(customerRepository.findAll(pageable)).thenReturn(page);

        Page<CustomerEntity> result = customerService.list(pageable);

        assertEquals(1, result.getTotalElements());
        verify(customerRepository).findAll(pageable);
    }

    @Test
    void get_returnsCustomerWhenFound() {
        CustomerEntity c1 = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(c1, "id", 1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c1));

        CustomerEntity result = customerService.get(1L);

        assertEquals(1L, result.getId());
        verify(customerRepository).findById(1L);
    }

    @Test
    void get_throwsWhenMissing() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> customerService.get(99L));
        verify(customerRepository).findById(99L);
    }

    @Test
    void totalOrders_delegatesToRepository() {
        when(orderRepository.countByCustomerId(1L)).thenReturn(3L);

        long total = customerService.totalOrders(1L);

        assertEquals(3L, total);
        verify(orderRepository).countByCustomerId(1L);
    }

    @Test
    void delete_removesCustomerAndOrders() {
        CustomerEntity c1 = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(c1, "id", 1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c1));

        customerService.delete(1L);

        verify(customerRepository).delete(c1);
    }
}
