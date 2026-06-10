package com.example.demo.mapper;

import com.example.demo.dto.BookChapterReference;
import com.example.demo.dto.BorrowEventData;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class BorrowMapper {

    public BorrowEventData toEventData(UUID member_card_uuid, UUID borrowUid, LocalDate borrow_start_date, LocalDate borrow_end_date, List<BookChapterReference> references) {
        return BorrowEventData.builder()
                .member_card_uuid(member_card_uuid)
                .borrow_uuid(borrowUid)
                .borrowed_items(references)
                .borrow_start_date(String.valueOf(borrow_start_date))
                .borrow_end_date(String.valueOf(borrow_end_date))
                .build();
    }
}
