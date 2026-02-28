package com.example.customerordersapi.repository;

import com.example.customerordersapi.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findByCustomerId(Long customerId, Pageable pageable);

    Page<OrderEntity> findByCustomerIdAndOrderDateBetween(
            Long customerId, LocalDate from, LocalDate to, Pageable pageable);

    long countByCustomerId(Long customerId);
}
