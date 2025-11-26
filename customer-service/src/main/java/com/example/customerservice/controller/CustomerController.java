package com.example.customerservice.controller;

import com.example.customerservice.model.Customer;
import com.example.customerservice.model.dto.request.CreateCustomerRequest;
import com.example.customerservice.model.dto.request.UpdateCustomerRequest;
import com.example.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    public Customer create(@Valid @RequestBody CreateCustomerRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Customer update(@PathVariable String id,
                           @Valid @RequestBody UpdateCustomerRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable String id) {
        return service.getAndRecordRead(id);
    }

}