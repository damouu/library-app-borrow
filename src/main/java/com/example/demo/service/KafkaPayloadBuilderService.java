package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Borrow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class KafkaPayloadBuilderService {

    public BorrowEventPayload buildBorrowPayload(UUID memberCardUUID, BookPayload booksArrayJson, UUID borrowUid, String eventType, String sourceService, LocalDate startDate, LocalDate endDate) {

        List<BookChapterReference> references = booksArrayJson.getData().stream().map(item -> new BookChapterReference(item.getBook_uuid(), item.getChapter_uuid())).toList();

        BorrowEventData dataPayload = BorrowEventData.builder().member_card_uuid(memberCardUUID).borrow_uuid(borrowUid).borrow_start_date(String.valueOf(startDate)).borrow_end_date(String.valueOf(endDate)).borrowed_items(references).build();

        Metadata metadataPayload = Metadata.builder().event_uuid(borrowUid).event_type(eventType).timestamp(LocalDate.now().toString()).source_service(sourceService).build();

        return BorrowEventPayload.builder().metadata(metadataPayload).data(dataPayload).build();
    }

    public ReturnEventPayload buildReturnPayload(UUID memberCardUUID, BookPayload booksArrayJson, UUID borrowUid, String eventType, String sourceService, LocalDate startDate, LocalDate endDate, LocalDate returnDate, Boolean returnLately, Long daysLate, BigDecimal lateFee) {

        List<BookToDecrement> booksToProcess = booksArrayJson.getData().stream().map(details -> new BookToDecrement(details.getBook_uuid())).toList();

        ReturnEventData dataPayload = ReturnEventData.builder().member_card_uuid(memberCardUUID).borrow_uuid(borrowUid).borrow_start_date(String.valueOf(startDate)).borrow_end_date(String.valueOf(endDate)).borrow_return_date(String.valueOf(returnDate)).return_lately(returnLately).days_late(Math.toIntExact(daysLate)).late_fee(lateFee).returned_items(booksToProcess).build();

        Metadata metadataPayload = Metadata.builder().event_uuid(borrowUid).event_type(eventType).timestamp(LocalDate.now().toString()).source_service(sourceService).build();

        return ReturnEventPayload.builder().metadata(metadataPayload).data(dataPayload).build();
    }

    public List<Borrow> buildBorrowEntities(BookPayload booksArrayJson, UUID borrowUid, UUID memberCardUUID, LocalDate startDate, LocalDate endDate) {
        return booksArrayJson.getData().stream().map(details -> Borrow.builder().borrowStartDate(startDate).borrowEndDate(endDate).borrowUuid(borrowUid).memberCardUuid(memberCardUUID).bookUuid(details.getBook_uuid()).chapterUuid(details.getChapter_uuid()).build()).toList();
    }
}