package com.example.notificationservice.base;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class AbstractTestContainerConfiguration {

    private static final DockerImageName KAFKA_IMAGE = DockerImageName
            .parse("confluentinc/cp-kafka:7.5.0")
            .asCompatibleSubstituteFor("apache/kafka");

    @Container
    static final KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

    private static final String TOPIC = "outbox.event.customers";

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("notification.outbox-topic", () -> TOPIC);
        registry.add("spring.kafka.consumer.group-id", () -> "notification-group");
    }

}