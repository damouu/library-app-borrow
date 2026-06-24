package com.example.demo.mapper;

import com.example.demo.dto.*;
import com.example.demo.model.Borrow;
import com.example.demo.dto.ReturnBorrowAggregate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class ReturnMapper {

    public ReturnCreatedEventData toEventData(UUID member_card_uuid, UUID borrowUid, String borrow_start_date, String borrow_end_date, String borrow_return_date, Boolean returnLately, Long daysLate, BigDecimal lateFee, List<BookToDecrement> booksToProcess) {
        return new ReturnCreatedEventData(
                member_card_uuid,
                borrowUid,
                borrow_start_date,
                borrow_end_date,
                borrow_return_date,
                returnLately,
                daysLate,
                lateFee,
                booksToProcess
        );
    }

    public ReturnBorrowCreatedSummaryDTO toSummaryDTO(ReturnBorrowAggregate aggregate) {
        if (aggregate == null || aggregate.items() == null || aggregate.items().isEmpty()) {
            throw new IllegalArgumentException("ReturnBorrow aggregate cannot be empty");
        }
        List<LoanItemDetails> items = aggregate.items().stream().map(b -> new LoanItemDetails(b.book_uuid(), b.chapter_uuid())).toList();
        return new ReturnBorrowCreatedSummaryDTO(
                aggregate.borrowUuid(),
                aggregate.memberCardUuid(),
                aggregate.startDate(),
                aggregate.endDate(),
                aggregate.returnDate(),
                aggregate.isLate(),
                aggregate.daysLate(),
                aggregate.fineAmount(),
                items
        );
    }

    public List<BookToDecrement> toBookToDecrement(List<Borrow> entities) {
        return entities.stream()
                .map(e -> new BookToDecrement(e.getBookUuid(), e.getChapterUuid()))
                .toList();
    }
}
