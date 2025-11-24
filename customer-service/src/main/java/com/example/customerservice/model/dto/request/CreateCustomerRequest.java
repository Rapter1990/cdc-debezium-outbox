package com.example.customerservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName
) {}
