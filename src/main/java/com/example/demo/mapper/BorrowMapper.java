package com.example.demo.mapper;

import com.example.demo.dto.*;
import com.example.demo.model.Borrow;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class BorrowMapper {

    public BorrowCreatedEventData toEventData(UUID member_card_uuid, UUID borrowUid, LocalDate borrow_start_date, LocalDate borrow_end_date, List<BookChapterReference> references) {
        return new BorrowCreatedEventData(member_card_uuid, borrowUid, String.valueOf(borrow_start_date), String.valueOf(borrow_end_date), references);
    }

    public List<Borrow> toEntities(BorrowAggregate aggregate) {
        return aggregate.items().stream().map(item -> Borrow.builder().memberCardUuid(aggregate.memberCardUuid()).borrowUuid(aggregate.borrowUuid()).borrowStartDate(aggregate.startDate()).borrowEndDate(aggregate.endDate()).bookUuid(item.book_uuid()).chapterUuid(item.chapter_uuid()).build()).toList();
    }

    public BorrowCreatedSummaryDTO toSummaryDTO(BorrowAggregate aggregate) {
        if (aggregate == null || aggregate.items() == null || aggregate.items().isEmpty()) {
            throw new IllegalArgumentException("Borrow aggregate cannot be empty");
        }
        List<LoanItemDetails> items = aggregate.items().stream().map(b -> new LoanItemDetails(b.book_uuid(), b.chapter_uuid())).toList();
        return new BorrowCreatedSummaryDTO(aggregate.borrowUuid(), aggregate.memberCardUuid(), aggregate.startDate().toString(), aggregate.endDate().toString(), items);
    }
}
