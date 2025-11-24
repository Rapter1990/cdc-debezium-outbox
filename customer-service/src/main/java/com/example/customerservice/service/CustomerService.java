package com.example.customerservice.service;

import com.example.customerservice.model.Customer;
import com.example.customerservice.model.dto.request.CreateCustomerRequest;
import com.example.customerservice.model.dto.request.UpdateCustomerRequest;
import com.example.customerservice.model.entity.CustomerEntity;
import com.example.customerservice.model.entity.OutboxEventEntity;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Customer create(CreateCustomerRequest request) {
        CustomerEntity entity = new CustomerEntity();
        entity.setEmail(request.email());
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());

        CustomerEntity savedEntity = customerRepository.save(entity);
        Customer dto = toDto(savedEntity);

        saveOutboxEvent("CUSTOMER_CREATED", dto);

        return dto;
    }

    @Transactional
    public Customer update(String id, UpdateCustomerRequest request) {
        CustomerEntity updatedEntity = customerRepository.findById(id)
                .map(existing -> {
                    existing.setEmail(request.email());
                    existing.setFirstName(request.firstName());
                    existing.setLastName(request.lastName());
                    return customerRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Customer dto = toDto(updatedEntity);
        saveOutboxEvent("CUSTOMER_UPDATED", dto);

        return dto;
    }

    @Transactional
    public void delete(String id) {
        CustomerEntity existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Customer dto = toDto(existing);

        customerRepository.delete(existing);
        saveOutboxEvent("CUSTOMER_DELETED", dto);
    }

    /**
     * Read + record outbox event.
     */
    @Transactional
    public Customer getAndRecordRead(String id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Customer dto = toDto(entity);
        saveOutboxEvent("CUSTOMER_READ", dto);

        return dto;
    }

    private Customer toDto(CustomerEntity entity) {
        return new Customer(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName()
        );
    }

    private void saveOutboxEvent(String eventType, Customer customer) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "eventType", eventType,
                            "customer", customer
                    )
            );

            OutboxEventEntity event = OutboxEventEntity.builder()
                    .aggregatetype("customers")
                    .aggregateid(customer.id())
                    .type(eventType)
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }
}