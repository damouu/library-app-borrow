package com.example.demo.event.publisher;

import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.BorrowCreatedEvent;
import com.example.demo.dto.ReturnBorrowAggregate;
import com.example.demo.dto.ReturnCreatedEvent;
import com.example.demo.factory.BorrowEventFactory;
import com.example.demo.factory.ReturnBorrowEventFactory;
import com.example.demo.model.Borrow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaBorrowEventPublisher implements BorrowEventPublisher {

    private final KafkaTemplate<UUID, Object> kafkaTemplate;
    private final BorrowEventFactory borrowEventFactory;
    private final ReturnBorrowEventFactory returnBorrowEventFactory;

    @Override
    public void publishBorrowCreated(BorrowAggregate aggregate, List<Borrow> entities) {
        UUID eventId = UUID.randomUUID();
        log.info("Publishing BORROW_CREATED event {}", eventId);
        BorrowCreatedEvent event = borrowEventFactory.create(aggregate, entities, eventId);
        kafkaTemplate.send("library.borrow.v1", eventId, event);
        log.info("Published BORROW_CREATED event {}", eventId);
    }

    @Override
    public void publishReturnBorrowCreated(ReturnBorrowAggregate aggregate, List<Borrow> entities) {
        UUID eventId = UUID.randomUUID();
        log.info("Publishing RETURNED_BORROW_CREATED event {}", eventId);
        ReturnCreatedEvent event = returnBorrowEventFactory.create(aggregate, entities, eventId);
        kafkaTemplate.send("library.return.v1", eventId, event);
        log.info("Published RETURNED_BORROW_CREATED event {}", eventId);
    }
}