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
        List<BookToDecrement> booksToProcess = booksArrayJson.getData().stream().map(details -> new BookToDecrement(details.getBook_uuid())).toList();

        InventoryDataEvent inventoryData = InventoryDataEvent.builder().books(booksToProcess).build();

        List<ChapterDetails> chapters = booksArrayJson.getData().stream().map(LoanItemDetails::getChapter).toList();

        BorrowNotificationDataEvent notificationData = BorrowNotificationDataEvent.builder().borrow_uuid(borrowUid).borrow_start_date(startDate.toString()).borrow_end_date(endDate.toString()).chapters(chapters).build();

        BorrowEventData dataPayload = BorrowEventData.builder().notificationData(notificationData).inventoryData(inventoryData).build();

        Metadata metadataPayload = Metadata.builder().event_uuid(borrowUid).event_type(eventType).timestamp(LocalDate.now().toString()).source_service(sourceService).memberCardUUID(memberCardUUID).build();

        return BorrowEventPayload.builder().metadata(metadataPayload).data(dataPayload).build();
    }

    public ReturnEventPayload buildReturnPayload(UUID memberCardUUID, BookPayload booksArrayJson, UUID borrowUid, String eventType, String sourceService, LocalDate startDate, LocalDate endDate, LocalDate returnDate, Boolean returnLately, Long daysLate, BigDecimal lateFee) {
        List<BookToDecrement> booksToProcess = booksArrayJson.getData().stream().map(details -> new BookToDecrement(details.getBook_uuid())).toList();

        InventoryDataEvent inventoryData = InventoryDataEvent.builder().books(booksToProcess).build();

        List<ChapterDetails> chapters = booksArrayJson.getData().stream().map(LoanItemDetails::getChapter).toList();

        ReturnNotificationDataEvent returnNotificationDataEvent = ReturnNotificationDataEvent.builder().borrow_uuid(borrowUid).borrow_start_date(startDate.toString()).borrow_end_date(endDate.toString()).borrow_return_date(returnDate.toString()).return_lately(returnLately).days_late(Math.toIntExact(daysLate)).late_fee(lateFee).chapters(chapters).build();

        ReturnEventData dataPayload = ReturnEventData.builder().returnNotificationDataEvent(returnNotificationDataEvent).inventoryData(inventoryData).build();

        Metadata metadataPayload = Metadata.builder().event_uuid(borrowUid).event_type(eventType).timestamp(LocalDate.now().toString()).source_service(sourceService).memberCardUUID(memberCardUUID).build();

        return ReturnEventPayload.builder().metadata(metadataPayload).data(dataPayload).build();
    }

    public List<Borrow> buildBorrowEntities(BookPayload booksArrayJson, UUID borrowUid, UUID memberCardUUID, LocalDate startDate, LocalDate endDate) {
        return booksArrayJson.getData().stream().map(details -> Borrow.builder().borrowStartDate(startDate).borrowEndDate(endDate).borrowUuid(borrowUid).memberCardUuid(memberCardUUID).bookUuid(details.getBook_uuid()).chapterUUID(details.getChapter().getChapterUUID()).build()).toList();
    }
}