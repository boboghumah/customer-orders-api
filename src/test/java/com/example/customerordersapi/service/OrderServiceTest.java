package com.example.customerordersapi.service;

import com.example.customerordersapi.dto.OrderCreateRequest;
import com.example.customerordersapi.entity.CustomerEntity;
import com.example.customerordersapi.entity.OrderEntity;
import com.example.customerordersapi.exception.BadRequestException;
import com.example.customerordersapi.exception.NotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createForCustomer_savesOrderAndSetsRelationship() {
        CustomerEntity customer = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(customer, "id", 1L);
        when(customerService.get(1L)).thenReturn(customer);

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            return saved;
        });

        OrderCreateRequest req = new OrderCreateRequest(LocalDate.of(2024, 6, 1), new BigDecimal("12.50"));

        OrderEntity saved = orderService.createForCustomer(1L, req);

        assertEquals(new BigDecimal("12.50"), saved.getAmount());
        assertEquals(LocalDate.of(2024, 6, 1), saved.getOrderDate());
        assertEquals(customer, saved.getCustomer());
        assertTrue(customer.getOrders().contains(saved));
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    void listForCustomer_delegatesToRepository() {
        CustomerEntity customer = new CustomerEntity("Alice", "alice@example.com");
        when(customerService.get(1L)).thenReturn(customer);
        PageRequest pageable = PageRequest.of(0, 20);
        Page<OrderEntity> page = new PageImpl<>(List.of(new OrderEntity()), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(page);

        Page<OrderEntity> result = orderService.listForCustomer(1L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findByCustomerId(1L, pageable);
    }

    @Test
    void listForCustomerByDateRange_throwsWhenMissingBounds() {
        when(customerService.get(1L)).thenReturn(new CustomerEntity("Alice", "alice@example.com"));

        assertThrows(BadRequestException.class,
                () -> orderService.listForCustomerByDateRange(1L, null, LocalDate.now(), PageRequest.of(0, 20)));
        assertThrows(BadRequestException.class,
                () -> orderService.listForCustomerByDateRange(1L, LocalDate.now(), null, PageRequest.of(0, 20)));
    }

    @Test
    void listForCustomerByDateRange_throwsWhenFromAfterTo() {
        when(customerService.get(1L)).thenReturn(new CustomerEntity("Alice", "alice@example.com"));

        assertThrows(BadRequestException.class,
                () -> orderService.listForCustomerByDateRange(1L,
                        LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1), PageRequest.of(0, 20)));
    }

    @Test
    void listForCustomerByDateRange_returnsResults() {
        when(customerService.get(1L)).thenReturn(new CustomerEntity("Alice", "alice@example.com"));
        PageRequest pageable = PageRequest.of(0, 20);
        Page<OrderEntity> page = new PageImpl<>(List.of(new OrderEntity()), pageable, 1);
        when(orderRepository.findByCustomerIdAndOrderDateBetween(
                1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), pageable)).thenReturn(page);

        Page<OrderEntity> result = orderService.listForCustomerByDateRange(
                1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), pageable);

        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findByCustomerIdAndOrderDateBetween(
                1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), pageable);
    }

    @Test
    void getOrder_throwsWhenMissing() {
        when(customerService.get(1L)).thenReturn(new CustomerEntity("Alice", "alice@example.com"));
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrder(1L, 99L));
    }

    @Test
    void getOrder_throwsWhenCustomerMismatch() {
        CustomerEntity customer = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(customer, "id", 1L);

        CustomerEntity otherCustomer = new CustomerEntity("Bob", "bob@example.com");
        ReflectionTestUtils.setField(otherCustomer, "id", 2L);

        OrderEntity order = new OrderEntity(LocalDate.now(), new BigDecimal("10.00"));
        order.setCustomer(otherCustomer);
        ReflectionTestUtils.setField(order, "id", 5L);

        when(customerService.get(1L)).thenReturn(customer);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThrows(NotFoundException.class, () -> orderService.getOrder(1L, 5L));
    }

    @Test
    void updateOrder_updatesFields() {
        CustomerEntity customer = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(customer, "id", 1L);

        OrderEntity order = new OrderEntity(LocalDate.of(2024, 1, 1), new BigDecimal("10.00"));
        order.setCustomer(customer);
        ReflectionTestUtils.setField(order, "id", 5L);

        when(customerService.get(1L)).thenReturn(customer);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderCreateRequest req = new OrderCreateRequest(LocalDate.of(2024, 2, 2), new BigDecimal("20.00"));
        OrderEntity updated = orderService.updateOrder(1L, 5L, req);

        assertEquals(LocalDate.of(2024, 2, 2), updated.getOrderDate());
        assertEquals(new BigDecimal("20.00"), updated.getAmount());
        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrder_removesOrder() {
        CustomerEntity customer = new CustomerEntity("Alice", "alice@example.com");
        ReflectionTestUtils.setField(customer, "id", 1L);

        OrderEntity order = new OrderEntity(LocalDate.of(2024, 1, 1), new BigDecimal("10.00"));
        order.setCustomer(customer);
        ReflectionTestUtils.setField(order, "id", 5L);

        when(customerService.get(1L)).thenReturn(customer);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L, 5L);

        verify(orderRepository).delete(order);
    }
}
