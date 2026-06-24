package com.example.demo.unit.service;

import com.example.demo.dto.*;
import com.example.demo.event.publisher.KafkaBorrowEventPublisher;
import com.example.demo.factory.BorrowEventFactory;
import com.example.demo.factory.ReturnBorrowEventFactory;
import com.example.demo.model.Borrow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BorrowEventPublisherTest {

    @Mock
    private KafkaTemplate<UUID, Object> kafkaTemplate;

    @Mock
    private BorrowEventFactory borrowEventFactory;

    @Mock
    private ReturnBorrowEventFactory returnBorrowEventFactory;

    @InjectMocks
    private KafkaBorrowEventPublisher borrowEventPublisher;

    @Test
    void should_publish_borrow_created_event() {
        BorrowAggregate aggregate = new BorrowAggregate(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusWeeks(2), List.of());
        Borrow borrow = Borrow.builder().borrowUuid(UUID.randomUUID()).memberCardUuid(UUID.randomUUID()).bookUuid(UUID.randomUUID()).chapterUuid(UUID.randomUUID()).borrowStartDate(LocalDate.now()).borrowEndDate(LocalDate.now().plusWeeks(2)).build();
        List<Borrow> entities = List.of(borrow);
        BorrowCreatedEvent event = new BorrowCreatedEvent(new Metadata(LocalDateTime.now().toString(), "library-app-borrow-v2", "BORROW_CREATED", UUID.randomUUID()), new BorrowCreatedEventData(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().toString(), LocalDate.now().plusWeeks(2).toString(), List.of()));
        when(borrowEventFactory.create(any(), anyList(), any())).thenReturn(event);
        ArgumentCaptor<UUID> eventIdCaptor = ArgumentCaptor.forClass(UUID.class);
        borrowEventPublisher.publishBorrowCreated(aggregate, entities);
        verify(borrowEventFactory).create(eq(aggregate), eq(entities), eventIdCaptor.capture());
        UUID capturedEventId = eventIdCaptor.getValue();
        assertThat(capturedEventId).isNotNull();
        verify(kafkaTemplate).send(eq("library.borrow.v1"), eq(capturedEventId), eq(event));
    }

    @Test
    void should_publish_return_borrow_created_event() {
        ReturnBorrowAggregate aggregate = new ReturnBorrowAggregate(UUID.randomUUID(), UUID.randomUUID(), "2026-01-01", "2026-01-14", "2026-01-15", true, 1L, new BigDecimal("5.00"), List.of());
        Borrow borrow = Borrow.builder().borrowUuid(UUID.randomUUID()).memberCardUuid(UUID.randomUUID()).bookUuid(UUID.randomUUID()).chapterUuid(UUID.randomUUID()).build();
        List<Borrow> entities = List.of(borrow);
        Metadata metadata = new Metadata(LocalDateTime.now().toString(), "library-app-borrow-v2", "RETURN_BORROW_CREATED", UUID.randomUUID());
        ReturnCreatedEventData data = new ReturnCreatedEventData(aggregate.memberCardUuid(), aggregate.borrowUuid(), aggregate.startDate(), aggregate.endDate(), aggregate.returnDate(), aggregate.isLate(), aggregate.daysLate(), aggregate.fineAmount(), List.of());
        ReturnCreatedEvent event = new ReturnCreatedEvent(metadata, data);
        when(returnBorrowEventFactory.create(eq(aggregate), eq(entities), any(UUID.class))).thenReturn(event);
        ArgumentCaptor<UUID> eventIdCaptor = ArgumentCaptor.forClass(UUID.class);
        borrowEventPublisher.publishReturnBorrowCreated(aggregate, entities);
        verify(returnBorrowEventFactory).create(eq(aggregate), eq(entities), eventIdCaptor.capture());
        UUID capturedEventId = eventIdCaptor.getValue();
        assertNotNull(capturedEventId);
        verify(kafkaTemplate).send(eq("library.return.v1"), eq(capturedEventId), eq(event));
    }
}