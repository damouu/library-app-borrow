package com.example.demo.dto;

import java.util.UUID;


public record LoanItemDetails(
        UUID book_uuid,

        UUID chapter_uuid
) {
}
