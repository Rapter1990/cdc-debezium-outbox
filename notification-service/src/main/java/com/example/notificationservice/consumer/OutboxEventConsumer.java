package com.example.notificationservice.consumer;

import com.example.notificationservice.model.dto.CustomerDto;
import com.example.notificationservice.model.dto.OutboxEventDto;
import com.example.notificationservice.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventConsumer {

    private final MailService mailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${notification.outbox-topic}", groupId = "notification-group")
    public void onOutboxEvent(String message) {
        try {
            OutboxEventDto event = objectMapper.readValue(message, OutboxEventDto.class);
            String eventType = event.eventType();
            CustomerDto customer = event.customer();

            if (customer == null || customer.email() == null || customer.email().isBlank()) {
                log.warn("Outbox event without customer email, payload={}", message);
                mailService.send("CDC Event (no email)", "Payload:\n" + message);
                return;
            }

            String email = customer.email();
            String subject;
            String body;

            switch (eventType) {
                case "CUSTOMER_CREATED" -> {
                    subject = "Customer Created";
                    body = "New customer created: " + customer;
                }
                case "CUSTOMER_UPDATED" -> {
                    subject = "Customer Updated";
                    body = "Customer updated: " + customer;
                }
                case "CUSTOMER_DELETED" -> {
                    subject = "Customer Deleted";
                    body = "Customer deleted (last known state): " + customer;
                }
                case "CUSTOMER_READ" -> {
                    subject = "Customer Read";
                    body = "Customer with id=" + customer.id() +
                            " and email=" + customer.email() + " was read.";
                }
                default -> {
                    log.info("Ignoring unknown eventType={} payload={}", eventType, message);
                    return;
                }
            }

            mailService.send(subject, body);
            log.info("Processed outbox event type={} for email={}", eventType, email);

        } catch (Exception e) {
            log.error("Error processing outbox event", e);
        }
    }
}
