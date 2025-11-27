package com.example.customerservice.service;

import com.example.customerservice.base.AbstractBaseServiceTest;
import com.example.customerservice.model.Customer;
import com.example.customerservice.model.dto.request.CreateCustomerRequest;
import com.example.customerservice.model.dto.request.UpdateCustomerRequest;
import com.example.customerservice.model.entity.CustomerEntity;
import com.example.customerservice.model.entity.OutboxEventEntity;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest extends AbstractBaseServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void create_shouldPersistCustomerAndWriteOutboxEvent() throws Exception {
        // given
        CreateCustomerRequest request =
                new CreateCustomerRequest("john.doe@example.com", "John", "Doe");

        CustomerEntity savedEntity = new CustomerEntity();
        savedEntity.setId("123");
        savedEntity.setEmail(request.email());
        savedEntity.setFirstName(request.firstName());
        savedEntity.setLastName(request.lastName());

        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(savedEntity);

        Customer expectedCustomer = new Customer(
                "123",
                "john.doe@example.com",
                "John",
                "Doe"
        );

        Map<String, Object> expectedPayloadMap = Map.of(
                "eventType", "CUSTOMER_CREATED",
                "customer", expectedCustomer
        );

        when(objectMapper.writeValueAsString(expectedPayloadMap))
                .thenReturn("serialized-payload");

        // when
        Customer result = customerService.create(request);

        // then
        assertEquals(expectedCustomer.id(), result.id());
        assertEquals(expectedCustomer.email(), result.email());
        assertEquals(expectedCustomer.firstName(), result.firstName());
        assertEquals(expectedCustomer.lastName(), result.lastName());

        verify(customerRepository).save(any(CustomerEntity.class));
        verify(objectMapper).writeValueAsString(expectedPayloadMap);
        verify(outboxEventRepository).save(any(OutboxEventEntity.class));
        verifyNoMoreInteractions(outboxEventRepository);
    }

    @Test
    void update_shouldUpdateExistingCustomerAndWriteOutboxEvent() throws Exception {
        // given
        String id = "123";
        UpdateCustomerRequest request =
                new UpdateCustomerRequest("new.mail@example.com", "NewName", "NewLast");

        CustomerEntity existing = new CustomerEntity();
        existing.setId(id);
        existing.setEmail("old@example.com");
        existing.setFirstName("Old");
        existing.setLastName("Name");

        CustomerEntity updated = new CustomerEntity();
        updated.setId(id);
        updated.setEmail(request.email());
        updated.setFirstName(request.firstName());
        updated.setLastName(request.lastName());

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(updated);

        Customer expectedCustomer = new Customer(
                id,
                request.email(),
                request.firstName(),
                request.lastName()
        );

        Map<String, Object> expectedPayloadMap = Map.of(
                "eventType", "CUSTOMER_UPDATED",
                "customer", expectedCustomer
        );

        when(objectMapper.writeValueAsString(expectedPayloadMap))
                .thenReturn("serialized-payload");

        // when
        Customer result = customerService.update(id, request);

        // then
        assertEquals(expectedCustomer.id(), result.id());
        assertEquals(expectedCustomer.email(), result.email());
        assertEquals(expectedCustomer.firstName(), result.firstName());
        assertEquals(expectedCustomer.lastName(), result.lastName());

        verify(customerRepository).findById(id);
        verify(customerRepository).save(existing);
        verify(objectMapper).writeValueAsString(expectedPayloadMap);
        verify(outboxEventRepository).save(any(OutboxEventEntity.class));
        verifyNoMoreInteractions(outboxEventRepository);
    }

    @Test
    void update_shouldThrowWhenCustomerNotFound() {
        // given
        String id = "missing-id";
        UpdateCustomerRequest request =
                new UpdateCustomerRequest("mail@example.com", "Name", "Surname");

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> customerService.update(id, request)
        );
        assertEquals("Customer not found", ex.getMessage());

        verify(customerRepository).findById(id);
        verify(customerRepository, never()).save(any());
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    void delete_shouldDeleteExistingCustomerAndWriteOutboxEvent() throws Exception {
        // given
        String id = "123";

        CustomerEntity existing = new CustomerEntity();
        existing.setId(id);
        existing.setEmail("delete@example.com");
        existing.setFirstName("Del");
        existing.setLastName("Eted");

        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        Customer expectedCustomer = new Customer(
                id,
                existing.getEmail(),
                existing.getFirstName(),
                existing.getLastName()
        );

        Map<String, Object> expectedPayloadMap = Map.of(
                "eventType", "CUSTOMER_DELETED",
                "customer", expectedCustomer
        );

        when(objectMapper.writeValueAsString(expectedPayloadMap))
                .thenReturn("serialized-payload");

        // when
        customerService.delete(id);

        // then
        verify(customerRepository).findById(id);
        verify(customerRepository).delete(existing);
        verify(objectMapper).writeValueAsString(expectedPayloadMap);
        verify(outboxEventRepository).save(any(OutboxEventEntity.class));
        verifyNoMoreInteractions(outboxEventRepository);
    }

    @Test
    void delete_shouldThrowWhenCustomerNotFound() {
        // given
        String id = "missing-id";
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> customerService.delete(id)
        );
        assertEquals("Customer not found", ex.getMessage());

        verify(customerRepository).findById(id);
        verify(customerRepository, never()).delete(any());
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    void getAndRecordRead_shouldReturnCustomerAndWriteReadOutboxEvent() throws Exception {
        // given
        String id = "123";

        CustomerEntity entity = new CustomerEntity();
        entity.setId(id);
        entity.setEmail("read@example.com");
        entity.setFirstName("Re");
        entity.setLastName("Ad");

        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));

        Customer expectedCustomer = new Customer(
                id,
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName()
        );

        Map<String, Object> expectedPayloadMap = Map.of(
                "eventType", "CUSTOMER_READ",
                "customer", expectedCustomer
        );

        when(objectMapper.writeValueAsString(expectedPayloadMap))
                .thenReturn("serialized-payload");

        // when
        Customer result = customerService.getAndRecordRead(id);

        // then
        assertEquals(expectedCustomer.id(), result.id());
        assertEquals(expectedCustomer.email(), result.email());
        assertEquals(expectedCustomer.firstName(), result.firstName());
        assertEquals(expectedCustomer.lastName(), result.lastName());

        verify(customerRepository).findById(id);
        verify(objectMapper).writeValueAsString(expectedPayloadMap);
        verify(outboxEventRepository).save(any(OutboxEventEntity.class));
        verifyNoMoreInteractions(outboxEventRepository);
    }

}
