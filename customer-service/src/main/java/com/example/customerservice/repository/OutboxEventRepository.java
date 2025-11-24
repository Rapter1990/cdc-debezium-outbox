package com.example.customerservice.repository;

import com.example.customerservice.model.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {
}
