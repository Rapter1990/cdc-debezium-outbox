package com.example.customerservice.controller;

import com.example.customerservice.base.AbstractRestControllerTest;
import com.example.customerservice.model.Customer;
import com.example.customerservice.model.dto.request.CreateCustomerRequest;
import com.example.customerservice.model.dto.request.UpdateCustomerRequest;
import com.example.customerservice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerControllerTest extends AbstractRestControllerTest {

    @MockitoBean
    private CustomerService customerService;

    @Test
    void create_shouldReturnCreatedCustomerAndCallService() throws Exception {

        // Given
        CreateCustomerRequest request =
                new CreateCustomerRequest("customermail@example.com",
                        "Customer First Name", "Customer Last Name");

        Customer returnedCustomer =
                new Customer("123", "customermail@example.com", "Customer First Name", "Customer Last Name");

        // When
        when(customerService.create(any(CreateCustomerRequest.class)))
                .thenReturn(returnedCustomer);

        String requestJson = objectMapper.writeValueAsString(request);

        // Then
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.email").value("customermail@example.com"))
                .andExpect(jsonPath("$.firstName").value("Customer First Name"))
                .andExpect(jsonPath("$.lastName").value("Customer Last Name"));

        // Verify
        verify(customerService).create(any(CreateCustomerRequest.class));

    }

    @Test
    void update_shouldReturnUpdatedCustomerAndCallService() throws Exception {

        // Given
        String id = "123";
        UpdateCustomerRequest request =
                new UpdateCustomerRequest("new.mail@example.com", "NewName", "NewLast");

        Customer returnedCustomer =
                new Customer(id, "new.mail@example.com", "NewName", "NewLast");

        // When
        when(customerService.update(eq(id), any(UpdateCustomerRequest.class)))
                .thenReturn(returnedCustomer);

        String requestJson = objectMapper.writeValueAsString(request);

        // Then
        mockMvc.perform(put("/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("new.mail@example.com"))
                .andExpect(jsonPath("$.firstName").value("NewName"))
                .andExpect(jsonPath("$.lastName").value("NewLast"));

        // Verify
        verify(customerService).update(eq(id), any(UpdateCustomerRequest.class));

    }

    @Test
    void delete_shouldCallServiceDeleteAndReturnOk() throws Exception {

        // Given
        String id = "123";

        // When & Then
        mockMvc.perform(delete("/customers/{id}", id))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify
        verify(customerService).delete(id);

    }

    @Test
    void get_shouldReturnCustomerAndCallService() throws Exception {

        // Given
        String id = "123";

        Customer returnedCustomer =
                new Customer(id, "read@example.com", "Re", "Ad");

        // when
        when(customerService.getAndRecordRead(id))
                .thenReturn(returnedCustomer);

        // Then
        mockMvc.perform(get("/customers/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("read@example.com"))
                .andExpect(jsonPath("$.firstName").value("Re"))
                .andExpect(jsonPath("$.lastName").value("Ad"));

        // Verify
        verify(customerService).getAndRecordRead(id);

    }

}
