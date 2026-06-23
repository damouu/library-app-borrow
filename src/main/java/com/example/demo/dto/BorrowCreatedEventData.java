package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;


@JsonIgnoreProperties(ignoreUnknown = true)
public record BorrowCreatedEventData(
        UUID member_card_uuid,

        UUID borrow_uuid,

        String borrow_start_date,

        String borrow_end_date,
        
        List<BookChapterReference> borrowed_items
) {
}