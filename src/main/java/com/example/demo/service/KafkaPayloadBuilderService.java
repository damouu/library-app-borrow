package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.mapper.ReturnMapper;
import com.example.demo.model.Borrow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaPayloadBuilderService {

    private final BorrowMapper borrowMapper;

    private final ReturnMapper returnMapper;

    public BorrowEventPayload buildBorrowPayload(UUID memberCardUUID, BookPayload booksArrayJson, UUID borrowUid, String eventType, String sourceService, LocalDate startDate, LocalDate endDate) {

        List<BookChapterReference> references = booksArrayJson.getData().stream().map(item -> new BookChapterReference(item.getBook_uuid(), item.getChapter_uuid())).toList();

        BorrowEventData dataPayload = borrowMapper.toEventData(memberCardUUID, borrowUid, startDate, endDate, references);

        Metadata metadataPayload = buildMetadata(eventType, sourceService, borrowUid);

        return BorrowEventPayload.builder().metadata(metadataPayload).data(dataPayload).build();
    }

    public ReturnEventPayload buildReturnPayload(UUID memberCardUUID, BookPayload booksArrayJson, UUID borrowUid, String eventType, String sourceService, LocalDate startDate, LocalDate endDate, LocalDate returnDate, Boolean returnLately, Long daysLate, BigDecimal lateFee) {

        List<BookToDecrement> booksToProcess = booksArrayJson.getData().stream().map(details -> new BookToDecrement(details.getBook_uuid())).toList();

        ReturnEventData dataPayload = returnMapper.toEventData(memberCardUUID, borrowUid, startDate, endDate, returnDate, returnLately, daysLate, lateFee, booksToProcess);

        Metadata metadataPayload = buildMetadata(eventType, sourceService, borrowUid);

        return ReturnEventPayload.builder().metadata(metadataPayload).data(dataPayload).build();
    }

    public List<Borrow> buildBorrowEntities(BookPayload booksArrayJson, UUID borrowUid, UUID memberCardUUID, LocalDate startDate, LocalDate endDate) {
        return booksArrayJson.getData().stream().map(details -> Borrow.builder().borrowStartDate(startDate).borrowEndDate(endDate).borrowUuid(borrowUid).memberCardUuid(memberCardUUID).bookUuid(details.getBook_uuid()).chapterUuid(details.getChapter_uuid()).build()).toList();
    }

    private Metadata buildMetadata(String eventType, String sourceService, UUID eventUUID) {
        return Metadata.builder().event_uuid(eventUUID).event_type(eventType).source_service(sourceService).timestamp(LocalDateTime.now().toString()).build();
    }
}