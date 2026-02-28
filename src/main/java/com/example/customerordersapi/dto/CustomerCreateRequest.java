package com.example.customerordersapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerCreateRequest(

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be <= 100 characters")
    String name,

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
            @Size(max = 120, message = "email must be <= 120 characters")
    String email

) {}
