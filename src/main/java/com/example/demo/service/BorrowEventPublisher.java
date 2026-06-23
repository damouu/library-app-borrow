package com.example.demo.service;

import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.BorrowCreatedEvent;
import com.example.demo.factory.BorrowEventFactory;
import com.example.demo.model.Borrow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowEventPublisher {

    private final KafkaTemplate<UUID, Object> kafkaTemplate;

    private final BorrowEventFactory borrowEventFactory;

    public void publishBorrowCreated(BorrowAggregate aggregate, List<Borrow> entities) {
        UUID eventId = UUID.randomUUID();
        log.info("Publishing BORROW_CREATED event {}", eventId);
        BorrowCreatedEvent event = borrowEventFactory.create(aggregate, entities, eventId);
        kafkaTemplate.send("library.borrow.v1", eventId, event);
        log.info("Published BORROW_CREATED event {}", eventId);
    }
}

