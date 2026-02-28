package com.example.customerordersapi.service;

import com.example.customerordersapi.dto.OrderCreateRequest;
import com.example.customerordersapi.entity.CustomerEntity;
import com.example.customerordersapi.entity.OrderEntity;
import com.example.customerordersapi.exception.BadRequestException;
import com.example.customerordersapi.exception.NotFoundException;
import com.example.customerordersapi.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepo, CustomerService customerService) {
        this.orderRepo = orderRepo;
        this.customerService = customerService;
    }

    public OrderEntity createForCustomer(Long customerId, OrderCreateRequest req) {
        CustomerEntity customer = customerService.get(customerId);
        OrderEntity order = new OrderEntity(req.orderDate(), req.amount());
        order.setCustomer(customer);
        customer.addOrder(order); // keeps relationship consistent
        return orderRepo.save(order);
    }

    public Page<OrderEntity> listForCustomer(Long customerId, Pageable pageable) {
        customerService.get(customerId); // 404 if customer doesn't exist
        return orderRepo.findByCustomerId(customerId, pageable);
    }

    public Page<OrderEntity> listForCustomerByDateRange(Long customerId, LocalDate from, LocalDate to, Pageable pageable) {
        customerService.get(customerId);

        if (from == null || to == null) throw new BadRequestException("Both 'from' and 'to' are required (yyyy-MM-dd).");
        if (from.isAfter(to)) throw new BadRequestException("'from' must be <= 'to'.");

        return orderRepo.findByCustomerIdAndOrderDateBetween(customerId, from, to, pageable);
    }

    public OrderEntity getOrder(Long customerId, Long orderId) {
        customerService.get(customerId);

        OrderEntity order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new NotFoundException("Order " + orderId + " does not belong to customer " + customerId);
        }
        return order;
    }

    public OrderEntity updateOrder(Long customerId, Long orderId, OrderCreateRequest req) {
        OrderEntity order = getOrder(customerId, orderId);
        order.setOrderDate(req.orderDate());
        order.setAmount(req.amount());
        return orderRepo.save(order);
    }

    public void deleteOrder(Long customerId, Long orderId) {
        OrderEntity order = getOrder(customerId, orderId);
        orderRepo.delete(order);
    }
}
