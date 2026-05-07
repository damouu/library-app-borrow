package com.example.demo.unit.service;

import com.example.demo.configuration.KafkaTopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaTopicBuilderTest {

    private final KafkaTopicBuilder kafkaTopicBuilder = new KafkaTopicBuilder();

    @Test
    @DisplayName("Should create Kafka topic for borrow events")
    void shouldCreateBorrowTopic() {

        NewTopic topic = kafkaTopicBuilder.borrowTopic();

        assertNotNull(topic);
        assertEquals("library.borrow.v1", topic.name());
    }

    @Test
    @DisplayName("Should create Kafka topic for return events")
    void shouldCreateReturnTopic() {

        NewTopic topic = kafkaTopicBuilder.returnTopic();

        assertNotNull(topic);
        assertEquals("library.return.v1", topic.name());
    }
}