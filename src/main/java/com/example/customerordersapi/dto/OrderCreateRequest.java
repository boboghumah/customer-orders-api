package com.example.customerordersapi.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderCreateRequest(
        @NotNull(message = "orderDate is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate orderDate,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be > 0")
        BigDecimal amount
) {}