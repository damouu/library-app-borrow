package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReturnBorrowCreatedSummaryDTO(
        UUID borrowUuid,

        UUID memberCardUuid,

        String startDate,

        String endDate,

        String returnDate,

        boolean isLate,

        long daysLate,

        BigDecimal fineAmount,

        List<LoanItemDetails> items
) {
}
