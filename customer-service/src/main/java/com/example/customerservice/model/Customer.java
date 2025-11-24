package com.example.customerservice.model;

public record Customer(
        String id,
        String email,
        String firstName,
        String lastName
) {}
