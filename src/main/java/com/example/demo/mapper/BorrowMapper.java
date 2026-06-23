package com.example.demo.mapper;

import com.example.demo.dto.BookChapterReference;
import com.example.demo.dto.BorrowCreatedEventData;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class BorrowMapper {

    public BorrowCreatedEventData toEventData(UUID member_card_uuid, UUID borrowUid, LocalDate borrow_start_date, LocalDate borrow_end_date, List<BookChapterReference> references) {

        return new BorrowCreatedEventData(
                member_card_uuid,
                borrowUid,
                String.valueOf(borrow_start_date),
                String.valueOf(borrow_end_date),
                references
        );
    }
}
