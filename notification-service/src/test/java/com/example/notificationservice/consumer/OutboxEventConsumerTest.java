package com.example.notificationservice.consumer;

import com.example.notificationservice.base.AbstractBaseServiceTest;

import com.example.notificationservice.base.AbstractBaseServiceTest;
import com.example.notificationservice.model.dto.CustomerDto;
import com.example.notificationservice.model.dto.OutboxEventDto;
import com.example.notificationservice.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OutboxEventConsumerTest extends AbstractBaseServiceTest {

    @InjectMocks
    private OutboxEventConsumer consumer;

    @Mock
    private MailService mailService;

    @Mock
    private ObjectMapper objectMapper;


    @Test
    void onOutboxEvent_shouldSendEmailForCustomerCreated() throws Exception {
        // given
        String rawMessage = "{\"eventType\":\"CUSTOMER_CREATED\"}";
        CustomerDto customer = new CustomerDto("id-1", "john.doe@example.com", "John", "Doe");
        OutboxEventDto dto = new OutboxEventDto("CUSTOMER_CREATED", customer);

        when(objectMapper.readValue(rawMessage, OutboxEventDto.class)).thenReturn(dto);

        // when
        consumer.onOutboxEvent(rawMessage);

        // then
        verify(mailService).send(eq("Customer Created"), contains("New customer created"));
        verifyNoMoreInteractions(mailService);
    }

    @Test
    void onOutboxEvent_shouldSendEmailForCustomerUpdated() throws Exception {
        // given
        String rawMessage = "{\"eventType\":\"CUSTOMER_UPDATED\"}";
        CustomerDto customer = new CustomerDto("id-2", "jane.doe@example.com", "Jane", "Doe");
        OutboxEventDto dto = new OutboxEventDto("CUSTOMER_UPDATED", customer);

        when(objectMapper.readValue(rawMessage, OutboxEventDto.class)).thenReturn(dto);

        // when
        consumer.onOutboxEvent(rawMessage);

        // then
        verify(mailService).send(eq("Customer Updated"), contains("Customer updated"));
        verifyNoMoreInteractions(mailService);
    }

    @Test
    void onOutboxEvent_shouldSendEmailForCustomerDeleted() throws Exception {
        // given
        String rawMessage = "{\"eventType\":\"CUSTOMER_DELETED\"}";
        CustomerDto customer = new CustomerDto("id-3", "delete@example.com", "Del", "Eted");
        OutboxEventDto dto = new OutboxEventDto("CUSTOMER_DELETED", customer);

        when(objectMapper.readValue(rawMessage, OutboxEventDto.class)).thenReturn(dto);

        // when
        consumer.onOutboxEvent(rawMessage);

        // then
        verify(mailService).send(eq("Customer Deleted"), contains("Customer deleted"));
        verifyNoMoreInteractions(mailService);
    }

    @Test
    void onOutboxEvent_shouldSendEmailForCustomerRead() throws Exception {
        // given
        String rawMessage = "{\"eventType\":\"CUSTOMER_READ\"}";
        CustomerDto customer = new CustomerDto("id-4", "read@example.com", "Re", "Ad");
        OutboxEventDto dto = new OutboxEventDto("CUSTOMER_READ", customer);

        when(objectMapper.readValue(rawMessage, OutboxEventDto.class)).thenReturn(dto);

        // when
        consumer.onOutboxEvent(rawMessage);

        // then
        verify(mailService).send(eq("Customer Read"), contains("was read"));
        verifyNoMoreInteractions(mailService);
    }

    @Test
    void onOutboxEvent_shouldIgnoreUnknownEventType() throws Exception {
        // given
        String rawMessage = "{\"eventType\":\"UNKNOWN_EVENT\"}";
        CustomerDto customer = new CustomerDto("id-x", "unknown@example.com", "Un", "Known");
        OutboxEventDto dto = new OutboxEventDto("UNKNOWN_EVENT", customer);

        when(objectMapper.readValue(rawMessage, OutboxEventDto.class)).thenReturn(dto);

        // when
        consumer.onOutboxEvent(rawMessage);

        // then
        verifyNoInteractions(mailService);
    }

    @Test
    void onOutboxEvent_shouldSendCdcEventNoEmailWhenCustomerEmailIsMissing() throws Exception {
        // given
        String rawMessage = "{\"eventType\":\"CUSTOMER_CREATED\"}";
        CustomerDto customer = new CustomerDto("id-5", "   ", "No", "Email");
        OutboxEventDto dto = new OutboxEventDto("CUSTOMER_CREATED", customer);

        when(objectMapper.readValue(rawMessage, OutboxEventDto.class)).thenReturn(dto);

        // when
        consumer.onOutboxEvent(rawMessage);

        // then
        verify(mailService).send(eq("CDC Event (no email)"), contains("Payload:\n" + rawMessage));
        verifyNoMoreInteractions(mailService);
    }

    @Test
    void onOutboxEvent_shouldHandleJsonDeserializationError() throws Exception {
        // given
        String rawMessage = "invalid-json";

        when(objectMapper.readValue(rawMessage, OutboxEventDto.class))
                .thenThrow(new RuntimeException("JSON parse error"));

        // when
        consumer.onOutboxEvent(rawMessage);

        // then
        verifyNoInteractions(mailService);
    }
}