package com.example.demo.unit.service;

import com.example.demo.dto.BookPayload;
import com.example.demo.dto.BorrowCreatedEvent;
import com.example.demo.dto.ReturnCreatedEvent;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.mapper.ReturnMapper;
import com.example.demo.model.Borrow;
import com.example.demo.service.KafkaPayloadBuilderService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


class KafkaPayloadBuilderServiceTest {

    private KafkaPayloadBuilderService kafkaPayloadBuilderService;

    private BookPayload bookPayload = Instancio.create(BookPayload.class);

    private UUID borrowUUID, memberCardUUID;

    @BeforeEach
    void setup() {
        memberCardUUID = UUID.randomUUID();
        borrowUUID = UUID.randomUUID();
        bookPayload = Instancio.create(BookPayload.class);
        kafkaPayloadBuilderService = new KafkaPayloadBuilderService(new BorrowMapper(), new ReturnMapper());
    }

    @Test
    @DisplayName("Should build borrow Kafka payload with borrow metadata and inventory data")
    void testBuildBorrowPayload() {
        BorrowCreatedEvent borrowCreatedEvent = kafkaPayloadBuilderService.buildBorrowPayload(memberCardUUID, bookPayload, borrowUUID, "BORROW_TYPE", "BORROW", LocalDate.now(), LocalDate.now().plusWeeks(2));
        Assertions.assertEquals(borrowUUID, borrowCreatedEvent.metadata().event_uuid());
        Assertions.assertEquals("BORROW_TYPE", borrowCreatedEvent.metadata().event_type());
        Assertions.assertEquals("BORROW", borrowCreatedEvent.metadata().source_service());
        Assertions.assertNotNull(borrowCreatedEvent.data().borrowed_items());
        Assertions.assertEquals(memberCardUUID, borrowCreatedEvent.data().member_card_uuid());
        Assertions.assertSame(borrowUUID, borrowCreatedEvent.data().borrow_uuid());
    }

    @Test
    @DisplayName("Should build return Kafka payload for on-time return")
    void testBuildReturnPayload() {
        Long daysLate = 0L;
        ReturnCreatedEvent returnCreatedEvent = kafkaPayloadBuilderService.buildReturnPayload(memberCardUUID, bookPayload, borrowUUID, "RETURN_TYPE", "BORROW", LocalDate.now(), LocalDate.now().plusWeeks(2), LocalDate.now().plusDays(2), false, daysLate, BigDecimal.ZERO);
        Assertions.assertEquals(borrowUUID, returnCreatedEvent.metadata().event_uuid());
        Assertions.assertEquals(memberCardUUID, returnCreatedEvent.data().member_card_uuid());
        Assertions.assertEquals("RETURN_TYPE", returnCreatedEvent.metadata().event_type());
        Assertions.assertEquals("BORROW", returnCreatedEvent.metadata().source_service());
        Assertions.assertNotNull(returnCreatedEvent.data().returned_items());
        Assertions.assertNotNull(returnCreatedEvent.data().returned_items());
        Assertions.assertSame(borrowUUID, returnCreatedEvent.data().borrow_uuid());
        Assertions.assertFalse(returnCreatedEvent.data().returned_items().isEmpty());
    }

    @Test
    @DisplayName("Should build return Kafka payload with overdue return details")
    void testBuildReturnPayloadWithLateReturn() {
        Long daysLate = 5L;
        ReturnCreatedEvent returnCreatedEvent = kafkaPayloadBuilderService.buildReturnPayload(memberCardUUID, bookPayload, borrowUUID, "RETURN_TYPE", "BORROW", LocalDate.now(), LocalDate.now().plusWeeks(2), LocalDate.now().plusDays(2), true, daysLate, BigDecimal.valueOf(500));
        Assertions.assertEquals(borrowUUID, returnCreatedEvent.metadata().event_uuid());
        Assertions.assertEquals(memberCardUUID, returnCreatedEvent.data().member_card_uuid());
        Assertions.assertEquals("RETURN_TYPE", returnCreatedEvent.metadata().event_type());
        Assertions.assertEquals("BORROW", returnCreatedEvent.metadata().source_service());
        Assertions.assertNotNull(returnCreatedEvent.data().returned_items());
        Assertions.assertSame(borrowUUID, returnCreatedEvent.data().borrow_uuid());
        Assertions.assertFalse(returnCreatedEvent.data().returned_items().isEmpty());
        Assertions.assertEquals(5, returnCreatedEvent.data().days_late());
        Assertions.assertEquals(BigDecimal.valueOf(500), returnCreatedEvent.data().late_fee());
    }

    @Test
    @DisplayName("Should build borrow entities from loan request payload")
    void testBuildBorrowEntities() {
        List<Borrow> borrowList = kafkaPayloadBuilderService.buildBorrowEntities(bookPayload, borrowUUID, memberCardUUID, LocalDate.now(), LocalDate.now().plusWeeks(2));
        Assertions.assertNotNull(borrowList);
        Assertions.assertEquals(LocalDate.now().plusWeeks(2), borrowList.getFirst().getBorrowEndDate());
        Assertions.assertEquals(LocalDate.now(), borrowList.getFirst().getBorrowStartDate());
        Assertions.assertEquals(borrowUUID, borrowList.getFirst().getBorrowUuid());
        Assertions.assertEquals(memberCardUUID, borrowList.getFirst().getMemberCardUuid());
        Assertions.assertEquals(bookPayload.data().getFirst().book_uuid(), borrowList.getFirst().getBookUuid());
    }
}