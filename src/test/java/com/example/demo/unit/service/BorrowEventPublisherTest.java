package com.example.demo.unit.service;

import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.BorrowCreatedEvent;
import com.example.demo.dto.BorrowCreatedEventData;
import com.example.demo.dto.Metadata;
import com.example.demo.factory.BorrowEventFactory;
import com.example.demo.model.Borrow;
import com.example.demo.service.BorrowEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowEventPublisherTest {

    @Mock
    private KafkaTemplate<UUID, Object> kafkaTemplate;

    @Mock
    private BorrowEventFactory borrowEventFactory;

    @InjectMocks
    private BorrowEventPublisher borrowEventPublisher;

    @Test
    void should_publish_borrow_created_event() {
        BorrowAggregate aggregate = new BorrowAggregate(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusWeeks(2), List.of());
        Borrow borrow = Borrow.builder().borrowUuid(UUID.randomUUID()).memberCardUuid(UUID.randomUUID()).bookUuid(UUID.randomUUID()).chapterUuid(UUID.randomUUID()).borrowStartDate(LocalDate.now()).borrowEndDate(LocalDate.now().plusWeeks(2)).build();
        List<Borrow> entities = List.of(borrow);
        BorrowCreatedEvent event = new BorrowCreatedEvent(new Metadata(LocalDateTime.now().toString(), "library-app-borrow-v1", "BORROW_CREATED", UUID.randomUUID()), new BorrowCreatedEventData(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().toString(), LocalDate.now().plusWeeks(2).toString(), List.of()));
        when(borrowEventFactory.create(any(), anyList(), any())).thenReturn(event);
        ArgumentCaptor<UUID> eventIdCaptor = ArgumentCaptor.forClass(UUID.class);
        borrowEventPublisher.publishBorrowCreated(aggregate, entities);
        verify(borrowEventFactory).create(eq(aggregate), eq(entities), eventIdCaptor.capture());
        UUID capturedEventId = eventIdCaptor.getValue();
        assertThat(capturedEventId).isNotNull();
        verify(kafkaTemplate).send(eq("library.borrow.v1"), eq(capturedEventId), eq(event));
    }
}