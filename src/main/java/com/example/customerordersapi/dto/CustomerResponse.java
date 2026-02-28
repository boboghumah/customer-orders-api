package com.example.customerordersapi.dto;

public record CustomerResponse(

        Long id,
        String name,
        String email,
        long totalOrders

) {}
