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

    public ReturnCreatedEventData toEventData(UUID member_card_uuid, UUID borrowUid, LocalDate borrow_start_date, LocalDate borrow_end_date, LocalDate borrow_return_date, Boolean returnLately, Long daysLate, BigDecimal lateFee, List<BookToDecrement> booksToProcess) {

        return new ReturnCreatedEventData(
                member_card_uuid,
                borrowUid,
                String.valueOf(borrow_start_date),
                String.valueOf(borrow_end_date),
                String.valueOf(borrow_return_date),
                returnLately,
                daysLate,
                lateFee,
                booksToProcess
        );
    }
}
