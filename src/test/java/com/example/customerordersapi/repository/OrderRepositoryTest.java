package com.example.customerordersapi.repository;

import com.example.customerordersapi.entity.CustomerEntity;
import com.example.customerordersapi.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findByCustomerId_returnsOnlyThatCustomersOrders() {
        CustomerEntity c1 = customerRepository.save(new CustomerEntity("Alice", "alice@example.com"));
        CustomerEntity c2 = customerRepository.save(new CustomerEntity("Bob", "bob@example.com"));

        OrderEntity o1 = new OrderEntity(LocalDate.of(2024, 1, 1), new BigDecimal("10.00"));
        o1.setCustomer(c1);
        orderRepository.save(o1);

        OrderEntity o2 = new OrderEntity(LocalDate.of(2024, 1, 2), new BigDecimal("20.00"));
        o2.setCustomer(c1);
        orderRepository.save(o2);

        OrderEntity o3 = new OrderEntity(LocalDate.of(2024, 1, 3), new BigDecimal("30.00"));
        o3.setCustomer(c2);
        orderRepository.save(o3);

        Page<OrderEntity> page = orderRepository.findByCustomerId(c1.getId(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void findByCustomerIdAndOrderDateBetween_filtersByDateRange() {
        CustomerEntity c1 = customerRepository.save(new CustomerEntity("Alice", "alice@example.com"));

        OrderEntity jan = new OrderEntity(LocalDate.of(2024, 1, 10), new BigDecimal("10.00"));
        jan.setCustomer(c1);
        orderRepository.save(jan);

        OrderEntity feb = new OrderEntity(LocalDate.of(2024, 2, 10), new BigDecimal("20.00"));
        feb.setCustomer(c1);
        orderRepository.save(feb);

        Page<OrderEntity> page = orderRepository.findByCustomerIdAndOrderDateBetween(
                c1.getId(), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals(LocalDate.of(2024, 1, 10), page.getContent().get(0).getOrderDate());
    }

    @Test
    void countByCustomerId_countsOrders() {
        CustomerEntity c1 = customerRepository.save(new CustomerEntity("Alice", "alice@example.com"));

        OrderEntity o1 = new OrderEntity(LocalDate.of(2024, 1, 1), new BigDecimal("10.00"));
        o1.setCustomer(c1);
        orderRepository.save(o1);

        OrderEntity o2 = new OrderEntity(LocalDate.of(2024, 1, 2), new BigDecimal("20.00"));
        o2.setCustomer(c1);
        orderRepository.save(o2);

        assertEquals(2, orderRepository.countByCustomerId(c1.getId()));
    }
}
