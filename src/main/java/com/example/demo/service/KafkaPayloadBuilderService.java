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


/**
 * The type Kafka payload builder service.
 */
@Service
@RequiredArgsConstructor
public class KafkaPayloadBuilderService {

    private final BorrowMapper borrowMapper;

    private final ReturnMapper returnMapper;


    /**
     * Build borrow payload borrow created event.
     *
     * @param memberCardUUID the member card uuid
     * @param booksArrayJson the books array json
     * @param borrowUid      the borrow uid
     * @param eventType      the event type
     * @param sourceService  the source service
     * @param startDate      the start date
     * @param endDate        the end date
     * @return the borrow created event
     */
    public BorrowCreatedEvent buildBorrowPayload(UUID memberCardUUID, BookPayload booksArrayJson, UUID borrowUid, String eventType, String sourceService, LocalDate startDate, LocalDate endDate) {
        List<BookChapterReference> references = booksArrayJson.data().stream().map(item -> new BookChapterReference(item.book_uuid(), item.chapter_uuid())).toList();
        BorrowCreatedEventData borrowCreatedEventData = borrowMapper.toEventData(memberCardUUID, borrowUid, startDate, endDate, references);
        Metadata metadata = buildMetadata(eventType, sourceService, borrowUid);
        return new BorrowCreatedEvent(metadata, borrowCreatedEventData);
    }


    /**
     * Build return payload return created event.
     *
     * @param memberCardUUID the member card uuid
     * @param booksArrayJson the books array json
     * @param borrowUid      the borrow uid
     * @param eventType      the event type
     * @param sourceService  the source service
     * @param startDate      the start date
     * @param endDate        the end date
     * @param returnDate     the return date
     * @param returnLately   the return lately
     * @param daysLate       the days late
     * @param lateFee        the late fee
     * @return the return created event
     */
    public ReturnCreatedEvent buildReturnPayload(UUID memberCardUUID, BookPayload booksArrayJson, UUID borrowUid, String eventType, String sourceService, LocalDate startDate, LocalDate endDate, LocalDate returnDate, Boolean returnLately, Long daysLate, BigDecimal lateFee) {
        List<BookToDecrement> booksToProcess = booksArrayJson.data().stream().map(details -> new BookToDecrement(details.book_uuid(), details.chapter_uuid())).toList();
        ReturnCreatedEventData returnCreatedEventData = returnMapper.toEventData(memberCardUUID, borrowUid, startDate, endDate, returnDate, returnLately, daysLate, lateFee, booksToProcess);
        Metadata metadata = buildMetadata(eventType, sourceService, borrowUid);
        return new ReturnCreatedEvent(metadata, returnCreatedEventData);
    }


    /**
     * Build borrow entities list.
     *
     * @param booksArrayJson the books array json
     * @param borrowUid      the borrow uid
     * @param memberCardUUID the member card uuid
     * @param startDate      the start date
     * @param endDate        the end date
     * @return the list
     */
    public List<Borrow> buildBorrowEntities(BookPayload booksArrayJson, UUID borrowUid, UUID memberCardUUID, LocalDate startDate, LocalDate endDate) {
        return booksArrayJson.data().stream().map(details -> Borrow.builder().borrowStartDate(startDate).borrowEndDate(endDate).borrowUuid(borrowUid).memberCardUuid(memberCardUUID).bookUuid(details.book_uuid()).chapterUuid(details.chapter_uuid()).build()).toList();
    }


    /**
     * Build metadata metadata.
     *
     * @param eventType     the event type
     * @param sourceService the source service
     * @param eventUUID     the event uuid
     * @return the metadata
     */
    public Metadata buildMetadata(String eventType, String sourceService, UUID eventUUID) {
        return new Metadata(LocalDateTime.now().toString(), sourceService, eventType, eventUUID);
    }
}