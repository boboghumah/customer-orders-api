package com.example.customerordersapi.mapper;

import com.example.customerordersapi.dto.CustomerResponse;
import com.example.customerordersapi.entity.CustomerEntity;

public class CustomerMapper {
    public static CustomerResponse toResponse(CustomerEntity c, long totalOrders){
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), totalOrders);
    }
}
