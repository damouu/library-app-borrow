package com.example.demo.unit.service;

import com.example.demo.dto.BookPayload;
import com.example.demo.dto.BorrowEventPayload;
import com.example.demo.dto.ReturnEventPayload;
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
        kafkaPayloadBuilderService = new KafkaPayloadBuilderService();
    }

    @Test
    @DisplayName("Should build borrow Kafka payload with borrow metadata and inventory data")
    void testBuildBorrowPayload() {
        BorrowEventPayload borrowEventPayload = kafkaPayloadBuilderService.buildBorrowPayload(memberCardUUID, bookPayload, borrowUUID, "BORROW_TYPE", "BORROW", LocalDate.now(), LocalDate.now().plusWeeks(2));
        Assertions.assertEquals(borrowUUID, borrowEventPayload.getMetadata().getEvent_uuid());
        Assertions.assertEquals("BORROW_TYPE", borrowEventPayload.getMetadata().getEvent_type());
        Assertions.assertEquals("BORROW", borrowEventPayload.getMetadata().getSource_service());
        Assertions.assertNotNull(borrowEventPayload.getData().getBorrowed_chapters());
        Assertions.assertEquals(memberCardUUID, borrowEventPayload.getData().getMemberCardUUID());
        Assertions.assertSame(borrowUUID, borrowEventPayload.getData().getBorrow_uuid());
    }

    @Test
    @DisplayName("Should build return Kafka payload for on-time return")
    void testBuildReturnPayload() {
        Long daysLate = 0L;
        ReturnEventPayload returnEventPayload = kafkaPayloadBuilderService.buildReturnPayload(memberCardUUID, bookPayload, borrowUUID, "RETURN_TYPE", "BORROW", LocalDate.now(), LocalDate.now().plusWeeks(2), LocalDate.now().plusDays(2), false, daysLate, BigDecimal.ZERO);
        Assertions.assertEquals(borrowUUID, returnEventPayload.getMetadata().getEvent_uuid());
        Assertions.assertEquals(memberCardUUID, returnEventPayload.getData().getMemberCardUUID());
        Assertions.assertEquals("RETURN_TYPE", returnEventPayload.getMetadata().getEvent_type());
        Assertions.assertEquals("BORROW", returnEventPayload.getMetadata().getSource_service());
        Assertions.assertNotNull(returnEventPayload.getData().getReturned_books());
        Assertions.assertNotNull(returnEventPayload.getData().getReturn_lately());
        Assertions.assertSame(borrowUUID, returnEventPayload.getData().getBorrow_uuid());
        Assertions.assertFalse(returnEventPayload.getData().getReturn_lately());
    }

    @Test
    @DisplayName("Should build return Kafka payload with overdue return details")
    void testBuildReturnPayloadWithLateReturn() {
        Long daysLate = 5L;
        ReturnEventPayload returnEventPayload = kafkaPayloadBuilderService.buildReturnPayload(memberCardUUID, bookPayload, borrowUUID, "RETURN_TYPE", "BORROW", LocalDate.now(), LocalDate.now().plusWeeks(2), LocalDate.now().plusDays(2), true, daysLate, BigDecimal.valueOf(500));
        Assertions.assertEquals(borrowUUID, returnEventPayload.getMetadata().getEvent_uuid());
        Assertions.assertEquals(memberCardUUID, returnEventPayload.getData().getMemberCardUUID());
        Assertions.assertEquals("RETURN_TYPE", returnEventPayload.getMetadata().getEvent_type());
        Assertions.assertEquals("BORROW", returnEventPayload.getMetadata().getSource_service());
        Assertions.assertNotNull(returnEventPayload.getData().getReturned_books());
        Assertions.assertSame(borrowUUID, returnEventPayload.getData().getBorrow_uuid());
        Assertions.assertTrue(returnEventPayload.getData().getReturn_lately());
        Assertions.assertEquals(5, returnEventPayload.getData().getDays_late());
        Assertions.assertEquals(BigDecimal.valueOf(500), returnEventPayload.getData().getLate_fee());
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
        Assertions.assertEquals(bookPayload.getData().getFirst().getBook_uuid(), borrowList.getFirst().getBookUuid());
    }
}