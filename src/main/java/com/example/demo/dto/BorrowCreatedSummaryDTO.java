package com.example.demo.dto;

import java.util.List;
import java.util.UUID;

public record BorrowCreatedSummaryDTO(
        UUID borrow_uuid,

        UUID memberCardUuid,

        String borrowStartDate,

        String borrowEndDate,

        List<LoanItemDetails> books
) {
}
