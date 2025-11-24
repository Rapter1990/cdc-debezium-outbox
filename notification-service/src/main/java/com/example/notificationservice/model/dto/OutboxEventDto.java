package com.example.notificationservice.model.dto;

public record OutboxEventDto(
        String eventType,
        CustomerDto customer
) {}
