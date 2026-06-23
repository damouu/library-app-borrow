package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BorrowAggregate(
        UUID borrowUuid,

        UUID memberCardUuid,

        LocalDate startDate,

        LocalDate endDate,

        List<LoanItemDetails> items
) {
}