package com.example.customerordersapi.mapper;

import com.example.customerordersapi.dto.OrderResponse;
import com.example.customerordersapi.entity.OrderEntity;

public class OrderMapper {
    public static OrderResponse toResponse(OrderEntity o){
        return new OrderResponse(o.getId(), o.getOrderDate(), o.getAmount());
    }
}
