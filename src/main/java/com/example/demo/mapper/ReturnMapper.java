package com.example.demo.mapper;

import com.example.demo.dto.BookToDecrement;
import com.example.demo.dto.ReturnCreatedEventData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class ReturnMapper {

    public ReturnCreatedEventData toEventData(UUID member_card_uuid, UUID borrowUid, LocalDate borrow_start_date, LocalDate borrow_end_date, LocalDate returnDate, Boolean returnLately, Long daysLate, BigDecimal lateFee, List<BookToDecrement> booksToProcess) {
        return ReturnCreatedEventData.builder()
                .member_card_uuid(member_card_uuid)
                .borrow_uuid(borrowUid)
                .borrow_start_date(String.valueOf(borrow_start_date))
                .borrow_end_date(String.valueOf(borrow_end_date))
                .borrow_return_date(String.valueOf(returnDate))
                .return_lately(returnLately)
                .days_late(Math.toIntExact(daysLate))
                .late_fee(lateFee)
                .returned_items(booksToProcess)
                .build();
    }
}
