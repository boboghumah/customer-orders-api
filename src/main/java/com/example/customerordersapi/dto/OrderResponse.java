package com.example.customerordersapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderResponse(
        Long id,
        LocalDate orderDate,
        BigDecimal amount
) {}
