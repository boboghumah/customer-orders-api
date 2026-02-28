package com.example.customerordersapi.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    public OrderEntity(){

    }
    public OrderEntity(LocalDate orderDate, BigDecimal amount){
        this.orderDate = orderDate;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }
    public LocalDate getOrderDate() {
        return orderDate;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }
}
