package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@JsonIgnoreProperties(ignoreUnknown = true)
public record ReturnCreatedEventData(
        UUID member_card_uuid,

        UUID borrow_uuid,

        String borrow_start_date,

        String borrow_end_date,

        String borrow_return_date,

        Boolean return_lately,

        Long days_late,

        BigDecimal late_fee,

        List<BookToDecrement> returned_items
) {
}